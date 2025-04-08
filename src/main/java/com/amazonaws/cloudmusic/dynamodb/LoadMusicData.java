package com.amazonaws.cloudmusic.dynamodb;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LoadMusicData {
    public static void main(String[] args) {
        AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();

        String tableName = "music";

        try {
            // Load and parse the JSON file
            ObjectMapper mapper = new ObjectMapper();
            File jsonFile = new File("data/2025a1.json");

            JsonNode root = mapper.readTree(jsonFile);
            JsonNode songsArray = root.get("songs");

            if (songsArray != null && songsArray.isArray()) {
                Iterator<JsonNode> iterator = songsArray.elements();

                while (iterator.hasNext()) {
                    JsonNode song = iterator.next();

                    Map<String, AttributeValue> item = new HashMap<>();
                    item.put("title", new AttributeValue(song.get("title").asText()));
                    item.put("artist", new AttributeValue(song.get("artist").asText()));
                    item.put("year", new AttributeValue(song.get("year").asText()));
                    item.put("album", new AttributeValue(song.get("album").asText()));
                    item.put("image_url", new AttributeValue(song.get("img_url").asText()));

                    PutItemRequest request = new PutItemRequest()
                            .withTableName(tableName)
                            .withItem(item);

                    dynamoDB.putItem(request);
                    System.out.println("Inserted: " + song.get("title").asText());
                }
            } else {
                System.err.println("The file does not contain a valid 'songs' array.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dynamoDB.shutdown();
        }
    }
}
