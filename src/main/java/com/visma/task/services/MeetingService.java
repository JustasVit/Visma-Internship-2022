package com.visma.task.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.visma.task.exceptions.MeetingException;
import com.visma.task.models.Meeting;
import com.visma.task.models.MeetingFilter;
import java.time.LocalDateTime;
import java.util.List;

public interface MeetingService {
    List<Meeting> getAllMeetings() throws JsonProcessingException;

    Meeting getMeeting(long id) throws JsonProcessingException, MeetingException;

    List<Meeting> getFilteredMeetings(MeetingFilter meetingFilter) throws Exception;

    Meeting insertMeeting(Meeting meeting) throws Exception;

    void deleteMeeting(long id) throws Exception;

    void addUserToMeeting(long userId, long meetingId, LocalDateTime startingDate) throws Exception;

    void removeUserFromMeeting(long userId, long meetingId) throws Exception;

}
