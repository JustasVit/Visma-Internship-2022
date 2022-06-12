package com.visma.task.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.visma.task.enums.Category;
import com.visma.task.enums.Type;
import com.visma.task.exceptions.DateException;
import com.visma.task.exceptions.MeetingException;
import com.visma.task.exceptions.UserException;
import com.visma.task.models.Meeting;
import com.visma.task.models.MeetingFilter;
import com.visma.task.models.User;
import com.visma.task.utils.JsonManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MeetingServiceImplTest {

    private MeetingServiceImpl meetingService;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private JsonManager jsonManager;

    private Meeting[] meetings;

    private JsonNode meetingsNode;

    private JsonNode usersMeetingsNode;

    private JsonNode rootNode;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        meetingService = new MeetingServiceImpl(jsonManager, userService);

        String meetingsNodeString = "[{\"id\":2," +
                "\"name\":\"Java meet\"," +
                "\"responsiblePerson\":2," +
                "\"description\":\"Svarbus susirinkimas\"," +
                "\"category\":\"CODE_MONKEY\"," +
                "\"type\":\"LIVE\"," +
                "\"startDate\":\"2022-06-06 13:00\"," +
                "\"endDate\":\"2022-06-06 14:00\"}," +

                "{\"id\":1," +
                "\"name\":\"Team building\"," +
                "\"responsiblePerson\":1," +
                "\"description\":\"Team building meetas\"," +
                "\"category\":\"TEAM_BUILDING\"," +
                "\"type\":\"LIVE\"," +
                "\"startDate\":\"2022-05-05 17:00\"," +
                "\"endDate\":\"2022-05-05 19:00\"}]";

        meetingsNode = new ObjectMapper().readTree(meetingsNodeString);

        String usersMeetingsNodeString = "[{\"userId\":1,\"meetingId\":1,\"start\":\"2022-05-05 17:00\",\"end\":\"2022-05-05 19:00\"}," +
                "{\"userId\":3,\"meetingId\":1,\"start\":\"2022-05-05 17:00\",\"end\":\"2022-05-05 19:00\"}," +
                "{\"userId\":2,\"meetingId\":2,\"start\":\"2022-06-06 13:00\",\"end\":\"2022-06-06 14:00\"}]";

        usersMeetingsNode = new ObjectMapper().readTree(usersMeetingsNodeString);

        String rootNodeString = "{\"users\":[{\"id\":1,\"username\":\"Vardauskas\",\"password\":\"Slaptazodis123\"}," +
                "{\"id\":2,\"username\":\"Vardenis\",\"password\":\"Slaptazodis321\"}," +
                "{\"id\":3,\"username\":\"Vardelis\",\"password\":\"Slaptazodis312\"}], \"meetings\":" + meetingsNodeString +
                ", \"users_meetings\":" + usersMeetingsNodeString + "}";

        rootNode = new ObjectMapper().readTree(rootNodeString);

        Meeting meeting1 = new Meeting(1L,"Team building",
                1L,
                "Team building meetas",
                Category.TEAM_BUILDING,
                Type.LIVE,
                LocalDateTime.of(2022,5,5,17,0),
                LocalDateTime.of(2022,5,5,19,0));

        Meeting meeting2 = new Meeting(2L,"Team building",
                1L,
                "Java meet",
                Category.CODE_MONKEY,
                Type.LIVE,
                LocalDateTime.of(2022,6,6,13,0),
                LocalDateTime.of(2022,6,6,14,0));

        meetings = new Meeting[]{meeting1, meeting2};

    }

    @ParameterizedTest
    @DisplayName("Should always return meeting with id = 1")
    @ValueSource(strings = {"Team","Team building", "building", "Java", "Java meet"})
    void getFilteredMeetingsTest(String keyword) throws JsonProcessingException {

        when(jsonManager.getJsonNode("meetings")).thenReturn(meetingsNode);
        when(jsonManager.deserializeNode(Meeting[].class,meetingsNode)).thenReturn(meetings);

        MeetingFilter meetingFilter = MeetingFilter.builder().description(keyword).build();

        List<Meeting> filteredMeetings = meetingService.getFilteredMeetings(meetingFilter);

        assertThat(filteredMeetings.size()).isEqualTo(1);
    }

    @Test
    void getMeetingByIdTest() throws JsonProcessingException, MeetingException {

        when(jsonManager.getJsonNode("meetings")).thenReturn(meetingsNode);
        when(jsonManager.deserializeNode(Meeting[].class,meetingsNode)).thenReturn(meetings);

        Meeting meeting = meetingService.getMeeting(1L);

        assertThat(meeting.getId()).isEqualTo(1L);
    }

    @Test
    void insertMeeting_ThrowsDateException_IfResponsiblePersonIsBusy() throws JsonProcessingException {
        when(userService.getUser(1L)).thenReturn(new User(1L,"Vardauskas","Slaptazodis123"));
        when(jsonManager.getJsonNode("users_meetings")).thenReturn(usersMeetingsNode);

        assertThatExceptionOfType(DateException.class).isThrownBy(() -> meetingService.insertMeeting(meetings[0]));

    }

    @Test
    void insertMeeting_ThrowsUserException_IfResponsiblePersonDoesntExist() throws JsonProcessingException {

        when(userService.getUser(any(Long.class))).thenReturn(null);

        assertThatExceptionOfType(UserException.class).isThrownBy(() -> meetingService.insertMeeting(meetings[0]));

    }

    @Test
    void insertMeeting_ThrowsDateException_IfMeetingIntervalIsIncorrect() throws JsonProcessingException {

        when(userService.getUser(1L)).thenReturn(new User(1L,"Vardauskas","Slaptazodis123"));
        when(jsonManager.getJsonNode("users_meetings")).thenReturn(usersMeetingsNode);

        meetings[0].setEndDate(meetings[0].getStartDate());

        assertThatExceptionOfType(DateException.class).isThrownBy(() -> meetingService.insertMeeting(meetings[0]));

    }

    @Test
    void deleteMeetingTest() throws IOException, MeetingException {
        when(jsonManager.getRootNode()).thenReturn(rootNode);
        doNothing().when(jsonManager).persistNode(any(File.class), any(JsonNode.class));

        ReflectionTestUtils.setField(meetingService, "jsonFile", "");

        meetingService.deleteMeeting(1L);

        verify(jsonManager,times(1)).persistNode(any(File.class),any(ObjectNode.class));

    }

    @Test
    void addUserToMeeting_ThrowsException_IfMeetingDoesntExist() throws Exception {
        when(jsonManager.getJsonNode("meetings")).thenReturn(meetingsNode);
        when(jsonManager.deserializeNode(Meeting[].class,meetingsNode)).thenReturn(meetings);

        assertThatExceptionOfType(MeetingException.class)
                .isThrownBy(() -> meetingService
                        .addUserToMeeting(2,55, LocalDateTime.of(2022,5,5,17,0)));

    }

    @Test
    void addUserToMeeting_ThrowsException_IfUserDoesntExist() throws Exception {
        when(jsonManager.getJsonNode("meetings")).thenReturn(meetingsNode);
        when(jsonManager.deserializeNode(Meeting[].class,meetingsNode)).thenReturn(meetings);
        when(userService.getUser(2L)).thenReturn(null);

        assertThatExceptionOfType(UserException.class)
                .isThrownBy(() -> meetingService
                        .addUserToMeeting(2,1, LocalDateTime.of(2022,5,5,17,0)));

    }

    @Test
    void addUserToMeetingTest() throws Exception {
        when(jsonManager.getJsonNode("meetings")).thenReturn(meetingsNode);
        when(jsonManager.getJsonNode("users_meetings")).thenReturn(usersMeetingsNode);
        when(jsonManager.deserializeNode(Meeting[].class,meetingsNode)).thenReturn(meetings);
        when(userService.getUser(2L)).thenReturn(new User(2L, "Vardenis","Slaptazodis321"));
        when(jsonManager.getRootNode()).thenReturn(rootNode);
        doNothing().when(jsonManager).persistNode(any(File.class), any(JsonNode.class));

        ReflectionTestUtils.setField(meetingService, "jsonFile", "");

        meetingService.addUserToMeeting(2,1, LocalDateTime.of(2022,5,5,17,0));

        verify(jsonManager,times(1)).persistNode(any(File.class),any(ObjectNode.class));
    }

    @Test
    void removeUserFromMeeting_ThrowsUserException_IfUserIsNotInMeeting() throws Exception {
        when(jsonManager.getJsonNode("users_meetings")).thenReturn(usersMeetingsNode);

        assertThatExceptionOfType(UserException.class)
                .isThrownBy(() -> meetingService.removeUserFromMeeting(4L,1L));
    }

    @Test
    void removeUserFromMeeting_ThrowsUserException_IfUserIsResponsible() throws Exception {
        when(jsonManager.getJsonNode("users_meetings")).thenReturn(usersMeetingsNode);
        when(jsonManager.getJsonNode("meetings")).thenReturn(meetingsNode);

        assertThatExceptionOfType(UserException.class)
                .isThrownBy(() -> meetingService.removeUserFromMeeting(1L,1L));
    }

    @Test
    void removeUserFromMeetingTest() throws Exception {
        when(jsonManager.getJsonNode("users_meetings")).thenReturn(usersMeetingsNode);
        when(jsonManager.getJsonNode("meetings")).thenReturn(meetingsNode);
        when(jsonManager.getRootNode()).thenReturn(rootNode);

        ReflectionTestUtils.setField(meetingService, "jsonFile", "");

        meetingService.removeUserFromMeeting(3,1);
        verify(jsonManager,times(1)).persistNode(any(File.class),any(ObjectNode.class));
    }
}
