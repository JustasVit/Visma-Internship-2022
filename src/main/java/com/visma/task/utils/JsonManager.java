package com.visma.task.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class JsonManager {

    private ObjectMapper mapper = new ObjectMapper();

    private void writeToJson(String filename, String str){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(str);
            writer.close();
        } catch (IOException e){}
    }

    private String readFromJson(String filename){
        try {
            return Files.readString(Path.of(filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void createInitialJson() throws JsonProcessingException {
        ObjectNode rootNode = mapper.createObjectNode();

        ArrayNode users  = mapper.createArrayNode();
        ArrayNode meetings  = mapper.createArrayNode();
        ArrayNode usersMeetings  = mapper.createArrayNode();

        rootNode.set("users", users);
        rootNode.set("meetings", meetings);
        rootNode.set("users_meetings", usersMeetings);

        String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);
        writeToJson("data.json",jsonString);
    }

    public JsonNode getJsonNode(String name) throws JsonProcessingException {
        String json = readFromJson("data.json");
        return mapper.readTree(json).get(name);
    }

    public JsonNode getRootNode() throws JsonProcessingException {
        String json = readFromJson("data.json");
        return mapper.readTree(json);
    }

    public <T> T deserializeNode(Class<T> classType, JsonNode jsonNode) throws JsonProcessingException {
        return mapper.treeToValue(jsonNode,classType);
    }

    public <T1,T2> T1 serializeObject(Class<T1> classType, T2 serializableObject){
        return mapper.convertValue(serializableObject,classType);
    }

    public <T> void persistNode(File file,T node) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file,node);
    }
}
