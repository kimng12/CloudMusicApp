package com.amazonaws.cloudmusic.authentication;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;

public class LoginHandler {
    private final DynamoDB dynamoDB;
    private final Table loginTable;

    public LoginHandler() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1) // adjust if needed
                .build();
        this.dynamoDB = new DynamoDB(client);
        this.loginTable = dynamoDB.getTable("login");
    }

    public boolean authenticate(String email, String password) {
        try {
            GetItemSpec spec = new GetItemSpec().withPrimaryKey("email", email);
            Item item = loginTable.getItem(spec);
            if (item != null && item.getString("password").equals(password)) {
                return true;
            }
        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return false;

        // TEST CODE: Use when not connected to dynamoDB and want to test locally
//        if (email.equals("test@example.com") && password.equals("1234")) {
//            return true;
//        }
//        return false;
    }

    public String getUsername(String email) {
        try {
            GetItemSpec spec = new GetItemSpec().withPrimaryKey("email", email);
            Item item = loginTable.getItem(spec);
            if (item != null) {
                return item.getString("user_name");
            }
        } catch (Exception e) {
            System.err.println("Error getting username: " + e.getMessage());
        }
        return "";

        // TEST CODE: Use when not connected to dynamoDB and want to test locally
//        if (email.equals("test@example.com")) {
//            return "Test User";
//        }
//        return "";
    }


}
