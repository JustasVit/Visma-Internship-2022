package com.visma.task.controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.visma.task.dto.MeetingDto;
import com.visma.task.models.Meeting;
import com.visma.task.models.MeetingFilter;
import com.visma.task.models.User;
import com.visma.task.services.MeetingService;
import com.visma.task.services.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api/meetings")
public class MeetingController {

    private MeetingService meetingService;
    private UserService userService;

    @PostMapping("/filter")
    public ResponseEntity<List<MeetingDto>> getFilteredMeetings(@RequestBody MeetingFilter meetingFilter) throws Exception {
        return new ResponseEntity<>(meetingService.getFilteredMeetings(meetingFilter)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList()), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<MeetingDto> createNewMeeting(@RequestBody MeetingDto meetingDto) throws Exception {
        meetingDto.setResponsiblePerson(userService.getUser(getUsernameOfLoggedInUser()).getId());
        Meeting insertedMeeting = meetingService.insertMeeting(convertToEntity(meetingDto));
        return new ResponseEntity<>(convertToDto(insertedMeeting), HttpStatus.OK);
    }

    @PostMapping("/user")
    public ResponseEntity<ObjectNode> addUserToMeeting(@RequestBody ObjectNode objectNode) throws Exception {
        long userId = objectNode.get("userId").asLong();
        long meetingId = objectNode.get("meetingId").asLong();
        LocalDateTime date =LocalDateTime.parse(objectNode.get("date").asText(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        User userLoggedIn = userService.getUser(getUsernameOfLoggedInUser());
        Meeting meeting = meetingService.getMeeting(meetingId);
        if(meeting.getResponsiblePerson() == userLoggedIn.getId()){
            meetingService.addUserToMeeting(userId,meetingId,date);
            return new ResponseEntity<>(objectNode,HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

    }

    @DeleteMapping("/user")
    public ResponseEntity removeUserFromMeeting(@RequestBody ObjectNode objectNode) throws Exception {
        long userId = objectNode.get("userId").asLong();
        long meetingId = objectNode.get("meetingId").asLong();

        User userLoggedIn = userService.getUser(getUsernameOfLoggedInUser());
        Meeting meeting = meetingService.getMeeting(meetingId);
        if(meeting.getResponsiblePerson() == userLoggedIn.getId()){
            meetingService.removeUserFromMeeting(userId,meetingId);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteExistingMeeting(@PathVariable long id) throws Exception {

        User userLoggedIn = userService.getUser(getUsernameOfLoggedInUser());
        Meeting meeting = meetingService.getMeeting(id);
        if(meeting.getResponsiblePerson() == userLoggedIn.getId()) {
            meetingService.deleteMeeting(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }

    private MeetingDto convertToDto(Meeting meeting){
        return new MeetingDto(meeting);
    }

    private Meeting convertToEntity(MeetingDto meetingDto){
        return new Meeting(meetingDto);
    }

    private String getUsernameOfLoggedInUser(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails)principal).getUsername();
        } else {
            return principal.toString();
        }
    }
}
