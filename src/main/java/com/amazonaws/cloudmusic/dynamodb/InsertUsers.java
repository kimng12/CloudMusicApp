package com.amazonaws.cloudmusic.dynamodb;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;

import java.util.HashMap;
import java.util.Map;

public class InsertUsers {
    public static String generatePassword(int offset) {
        String base = "012345";
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < base.length(); j++) {
            // Get digit from base and add offset, then modulo 10
            int d = base.charAt(j) - '0';
            d = (d + offset) % 10;
            sb.append(d);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();

        String tableName = "login";
        String studentId = "s3970589";

        for (int i = 0; i < 10; i++) {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("email", new AttributeValue().withS(studentId + i + "@student.rmit.edu.au"));
            item.put("user_name", new AttributeValue().withS("FirstnameLastname" + i));
            // Generate password pattern: "012345", "123456", ..., "901234"
            String password = generatePassword(i);
            item.put("password", new AttributeValue().withS(password));

            PutItemRequest request = new PutItemRequest().withTableName(tableName).withItem(item);

            try {
                dynamoDB.putItem(request);
                System.out.println("User added: " + item.get("email").getS() + " | Password: " + password);
            } catch (AmazonDynamoDBException e) {
                System.err.println("Error inserting user: " + e.getMessage());
            }
        }

        dynamoDB.shutdown();
    }
}
