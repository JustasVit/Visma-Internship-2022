package com.visma.task.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.visma.task.models.User;
import com.visma.task.utils.JsonManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.File;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private JsonManager jsonManager;

    private UserServiceImpl userService;

    private JsonNode usersNode;

    private User[] users;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        userService = new UserServiceImpl(jsonManager);
        String usersNodeString = "[{\"id\":2,\"username\":\"Vardenis\",\"password\":\"slaptazodis321\"}," +
                "{\"id\":1,\"username\":\"Vardauskas\",\"password\":\"slaptazodis123\"}]";
        usersNode = new ObjectMapper().readTree(usersNodeString);

        User user1 = new User(1L,"Vardauskas","slaptazodis123");
        User user2 = new User(2L,"Vardenis","slaptazodis30");

         users = new User[]{user1, user2};
    }

    @Test
    void getUserByIdTest() throws JsonProcessingException {
        when(jsonManager.getJsonNode("users")).thenReturn(usersNode);
        when(jsonManager.deserializeNode(User[].class,usersNode)).thenReturn(users);

        User user = userService.getUser(1L);
        assertThat(user).isEqualTo(users[0]);
    }

    @Test
    void getUserByUsernameTest() throws JsonProcessingException {
        User user1 = new User(1L,"Vardauskas","slaptazodis123");
        User user2 = new User(2L,"Vardenis","slaptazodis30");

        User users[] = {user1,user2};

        when(jsonManager.getJsonNode("users")).thenReturn(usersNode);
        when(jsonManager.deserializeNode(User.class, usersNode.get(1))).thenReturn(user1);

        User user = userService.getUser("Vardauskas");
        assertThat(user).isEqualTo(user1);
    }

    @Test
    void insertUserTest() throws Exception {

        User[] users = new User[]{};

        String emptyRootNodeString = "{\"users\":[],\"meetings\":[],\"users_meetings\":[]}";
        JsonNode emptyRootNode = new ObjectMapper().readTree(emptyRootNodeString);

        ReflectionTestUtils.setField(userService, "jsonFile", "");

        ObjectNode userNode = new ObjectMapper().createObjectNode();
        userNode.put("id",1);
        userNode.put("username","Vardauskas");
        userNode.put("password","slaptazodis123");

        when(jsonManager.getJsonNode("users")).thenReturn(usersNode);
        when(jsonManager.deserializeNode(User[].class,usersNode)).thenReturn(users);
        when(jsonManager.serializeObject(any(Class.class),any(User.class))).thenReturn(userNode);
        when(jsonManager.getRootNode()).thenReturn(emptyRootNode);
        doNothing().when(jsonManager).persistNode(any(File.class), any(JsonNode.class));

        User insertedUser = userService.insertUser(new User(1L,"Vardauskas","slaptazodis123"));
        verify(jsonManager,times(1)).persistNode(any(File.class),any(ObjectNode.class));

    }
}
