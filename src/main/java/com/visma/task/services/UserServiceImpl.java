package com.visma.task.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.visma.task.exceptions.UserException;
import com.visma.task.models.User;
import com.visma.task.utils.JsonManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final JsonManager jsonManager;
    @Value("${json_file}")
    private String jsonFile;

    public UserServiceImpl(JsonManager jsonManager){
        this.jsonManager = jsonManager;
    }

    public List<User> getAllUsers() throws JsonProcessingException {
        JsonNode usersNode = jsonManager.getJsonNode("users");
        return Arrays.asList(jsonManager.deserializeNode(User[].class,usersNode));
    }

    public User getUser(long id) throws JsonProcessingException {
        return getAllUsers().stream().filter(user -> user.getId() == id).findFirst().orElse(null);
    }

    public User getUser(String username) throws JsonProcessingException {
        JsonNode usersNode = jsonManager.getJsonNode("users");
        for(int i = 0; i < usersNode.size(); i++){
            if(usersNode.get(i).get("username").asText().equals(username)){
                return jsonManager.deserializeNode(User.class, usersNode.get(i));
            }
        }
        return null;
    }

    public User insertUser(User user) throws Exception {
        List<User> currentUsers = getAllUsers();

        for(User currentUser: currentUsers){
            if(user.getUsername().equals(currentUser.getUsername())){
                throw new UserException("User with this username already exists");
            }
        }

        setCorrectUserId(user, currentUsers);

        JsonNode rootNode = jsonManager.getRootNode();
        ArrayNode usersNode = (ArrayNode) rootNode.get("users");
        ObjectNode newUserNode = jsonManager.serializeObject(ObjectNode.class,user);
        usersNode.insert(0,newUserNode);
        jsonManager.persistNode(Paths.get(jsonFile).toFile(),rootNode);

        return user;
    }

    private void setCorrectUserId(User user, List<User> currentUsers){
        User userWithHighestId = currentUsers
                .stream()
                .max(Comparator.comparing(User::getId))
                .orElse(null);

        if(userWithHighestId != null){
            user.setId(userWithHighestId.getId() + 1);
        } else {
            user.setId(1);
        }
    }
}
