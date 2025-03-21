package com.amazonaws.cloudmusic.dynamodb;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.*;
public class CreateLoginTable {
    public static void main(String[] args) {
        AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();

        CreateTableRequest request = new CreateTableRequest()
                .withTableName("login")
                .withKeySchema(new KeySchemaElement("email", KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition("email", ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput(5L, 5L));

        try {
            dynamoDB.createTable(request);
            System.out.println("Login table created successfully!");
        } catch (ResourceInUseException e) {
            System.out.println("Login table already exists.");
        } catch (AmazonDynamoDBException e) {
            System.err.println("Error creating table: " + e.getMessage());
        }

        dynamoDB.shutdown();
    }
}
