package com.visma.task.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.visma.task.exceptions.DateException;
import com.visma.task.exceptions.MeetingException;
import com.visma.task.exceptions.UserException;
import com.visma.task.models.Meeting;
import com.visma.task.models.MeetingFilter;
import com.visma.task.utils.JsonManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MeetingServiceImpl implements MeetingService {

    private final JsonManager jsonManager;
    private final UserService userService;
    @Value("${json_file}")
    private String jsonFile;

    public MeetingServiceImpl(JsonManager jsonManager, UserService userService) {
        this.jsonManager = jsonManager;
        this.userService = userService;
    }

    public List<Meeting> getAllMeetings() throws JsonProcessingException {
        JsonNode meetingsNode = jsonManager.getJsonNode("meetings");
        return Arrays.asList(jsonManager.deserializeNode(Meeting[].class, meetingsNode));
    }

    public List<Meeting> getFilteredMeetings(MeetingFilter meetingFilter) throws JsonProcessingException {
        List<Meeting> allMeetings = getAllMeetings();

        return allMeetings.stream()
                .filter(meeting -> meetingFilter.getCategory() == null
                        || meetingFilter.getCategory().equals(meeting.getCategory()))
                .filter(meeting -> meetingFilter.getResponsiblePersonId() == 0
                        || meetingFilter.getResponsiblePersonId() == meeting.getResponsiblePerson())
                .filter(meeting -> meetingFilter.getDescription() == null
                        || meeting.getDescription().toLowerCase().contains(meetingFilter.getDescription().toLowerCase()))
                .filter(meeting -> meetingFilter.getType() == null
                        || meetingFilter.getType().equals(meeting.getType()))
                .filter(meeting -> meetingFilter.getStartingDate() == null
                        || meetingFilter.getStartingDate().equals(meeting.getStartDate())
                        || meetingFilter.getStartingDate().isBefore(meeting.getStartDate()))
                .filter(meeting -> meetingFilter.getEndingDate() == null
                        || meetingFilter.getEndingDate().equals(meeting.getEndDate())
                        || meetingFilter.getEndingDate().isAfter(meeting.getEndDate()))
                .filter(meeting -> {
                    try{
                        return meetingFilter.getMinimumAttendeeCount() == 0
                                || meetingFilter.getMinimumAttendeeCount() < getMeetingAttendeeCount(meeting.getId());
                    } catch (JsonProcessingException jpe){
                        jpe.printStackTrace();
                    }
                    return false;
                })

                .collect(Collectors.toList());
    }

    public Meeting getMeeting(long id) throws JsonProcessingException, MeetingException {
        return getAllMeetings()
                .stream()
                .filter(meeting -> meeting.getId() == id)
                .findFirst()
                .orElseThrow(() -> new MeetingException(String.format("Meeting with id %d doesn't exist",id)));
    }

    public Meeting insertMeeting(Meeting meeting) throws Exception {
        if (userService.getUser(meeting.getResponsiblePerson()) == null) {
            throw new UserException("Responsible person doesn't exist.");
        } else if (isUserBusy(meeting.getResponsiblePerson(), meeting)) {
            throw new DateException("Responsible person has a meeting at given time.");
        }

        if(!isMeetingValid(meeting)){
            throw new MeetingException("Invalid meeting data");
        }

        if (meeting.getStartDate().isAfter(meeting.getEndDate()) || meeting.getStartDate().equals(meeting.getEndDate())) {
            throw new DateException("Meeting's interval is incorrect");
        }

        List<Meeting> existingMeetings = getAllMeetings();
        setCorrectMeetingId(meeting, existingMeetings);

        JsonNode rootNode = jsonManager.getRootNode();
        ArrayNode meetingsNode = (ArrayNode) rootNode.get("meetings");
        ObjectNode newMeetingNode = jsonManager.serializeObject(ObjectNode.class, meeting);
        meetingsNode.insert(0, newMeetingNode);
        jsonManager.persistNode(Paths.get(jsonFile).toFile(), rootNode);
        addUserToMeeting(meeting.getResponsiblePerson(), meeting.getId(), meeting.getStartDate());
        return meeting;
    }


    public void deleteMeeting(long id) throws IOException, MeetingException {
        JsonNode rootNode = jsonManager.getRootNode();
        ArrayNode meetingsNode = (ArrayNode) rootNode.get("meetings");
        ArrayNode usersMeetingsNode = (ArrayNode) rootNode.get("users_meetings");

        boolean removed = false;
        for (int i = 0; i < meetingsNode.size(); i++) {
            if (meetingsNode.get(i).get("id").asLong() == id) {
                meetingsNode.remove(i);
                for (int j = 0; j < usersMeetingsNode.size(); j++) {
                    if (usersMeetingsNode.get(j).get("meetingId").asLong() == id) {
                        usersMeetingsNode.remove(j);
                        removed = true;
                    }
                }
                if(removed) {
                    jsonManager.persistNode(Paths.get(jsonFile).toFile(), rootNode);
                } else {
                    throw new MeetingException(String.format("Meeting with id %d doesn't exist",id));
                }
            }
        }
    }

    public void addUserToMeeting(long userId, long meetingId, LocalDateTime startingDate) throws Exception {
        Meeting meeting = getMeeting(meetingId);

        if (userService.getUser(userId) != null && !isUserBusy(userId, meeting) && !isUserInMeeting(userId, meetingId)) {
            JsonNode rootNode = jsonManager.getRootNode();
            ArrayNode usersMeetingsNode = (ArrayNode) rootNode.get("users_meetings");
            ObjectNode userMeetingNode = new ObjectMapper().createObjectNode();
            userMeetingNode.put("userId", userId);
            userMeetingNode.put("meetingId", meetingId);

            if (startingDate.isBefore(meeting.getEndDate())
                    && (startingDate.isAfter(meeting.getStartDate())
                    || startingDate.isEqual(meeting.getStartDate()))) {
                userMeetingNode.put("start", startingDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            } else {
                throw new DateException("User can't be added to the meeting because the date is out of the meeting's time interval.");
            }
            userMeetingNode.put("end", meeting.getEndDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            usersMeetingsNode.add(userMeetingNode);
            jsonManager.persistNode(Paths.get(jsonFile).toFile(), rootNode);
        } else {
            throw new UserException("User can't be added to the meeting.");
        }
    }

    public void removeUserFromMeeting(long userId, long meetingId) throws Exception {
        if (!isUserInMeeting(userId, meetingId))
            throw new UserException("Can't remove user from the meeting because he is not participating");
        if (isUserResponsibleForMeeting(userId, meetingId))
            throw new UserException("Can't remove user which is responsible for the meeting");

        JsonNode rootNode = jsonManager.getRootNode();
        ArrayNode usersMeetingsNode = (ArrayNode) rootNode.get("users_meetings");

        for (int i = 0; i < usersMeetingsNode.size(); i++) {
            if (usersMeetingsNode.get(i).get("userId").asLong() == userId
                    && usersMeetingsNode.get(i).get("meetingId").asLong() == meetingId) {
                usersMeetingsNode.remove(i);
                jsonManager.persistNode(Paths.get(jsonFile).toFile(), rootNode);
            }
        }
    }

    private boolean isUserBusy(long userId, Meeting meeting) throws JsonProcessingException {
        ArrayNode usersMeetingsNode = (ArrayNode) jsonManager.getJsonNode("users_meetings");
        for (int i = 0; i < usersMeetingsNode.size(); i++) {
            if (usersMeetingsNode.get(i).get("userId").asLong() == userId) {

                String startDateString = usersMeetingsNode.get(i).get("start").asText();
                String endDateString = usersMeetingsNode.get(i).get("end").asText();

                LocalDateTime startDate = LocalDateTime.parse(startDateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                LocalDateTime endDate = LocalDateTime.parse(endDateString, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                LocalDateTime maxStartDate = startDate.isAfter(meeting.getStartDate()) ? startDate : meeting.getStartDate();
                LocalDateTime minEndDate = endDate.isBefore(meeting.getEndDate()) ? endDate : meeting.getEndDate();
                boolean meetingsOverlap = maxStartDate.isBefore(minEndDate) || maxStartDate.isEqual(minEndDate);

                if (meetingsOverlap) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isMeetingValid(Meeting meeting){
        return (meeting.getName() != null &&
                meeting.getDescription() != null &&
                meeting.getResponsiblePerson() != 0 &&
                meeting.getCategory() != null &&
                meeting.getType() != null &&
                meeting.getStartDate() != null &&
                meeting.getEndDate() != null);
    }

    private boolean isUserInMeeting(long userId, long meetingId) throws JsonProcessingException {
        ArrayNode usersMeetingsNode = (ArrayNode) jsonManager.getJsonNode("users_meetings");
        for (int i = 0; i < usersMeetingsNode.size(); i++) {
            if (usersMeetingsNode.get(i).get("userId").asLong() == userId
                    && usersMeetingsNode.get(i).get("meetingId").asLong() == meetingId) {
                return true;
            }
        }
        return false;
    }

    private boolean isUserResponsibleForMeeting(long userId, long meetingId) throws JsonProcessingException {
        ArrayNode meetingsNode = (ArrayNode) jsonManager.getJsonNode("meetings");
        for (int i = 0; i < meetingsNode.size(); i++) {
            if (meetingsNode.get(i).get("id").asLong() == meetingId && meetingsNode.get(i).get("responsiblePerson").asLong() == userId) {
                return true;
            }
        }
        return false;
    }

    private void setCorrectMeetingId(Meeting meeting, List<Meeting> existingMeetings) {
        Meeting meetingWithHighestId = existingMeetings
                .stream()
                .max(Comparator.comparing(Meeting::getId))
                .orElse(null);
        if(meetingWithHighestId != null){
            meeting.setId(meetingWithHighestId.getId() + 1);
        } else {
            meeting.setId(1);
        }
    }

    private int getMeetingAttendeeCount(long meetingId) throws JsonProcessingException {
        ArrayNode usersMeetingsNode = (ArrayNode) jsonManager.getJsonNode("users_meetings");
        int attendeeCount = 0;
        for (int i = 0; i < usersMeetingsNode.size(); i++) {
            if (usersMeetingsNode.get(i).get("meetingId").asLong() == meetingId) {
                attendeeCount++;
            }
        }
        return attendeeCount;
    }
}
