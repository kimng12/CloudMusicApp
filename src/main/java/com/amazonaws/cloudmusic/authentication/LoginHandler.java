package com.amazonaws.cloudmusic.authentication;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;

public class LoginHandler {
    private final DynamoDB dynamoDB;
    private final Table loginTable;

    public LoginHandler() {
        AmazonDynamoDB client = null;
        try (InputStream input =  getClass().getClassLoader().getResourceAsStream("aws-credentials.properties")) {
            if (input != null) {
                Properties properties = new Properties();
                properties.load(input);
                String accessKey = properties.getProperty("aws_access_key_id");
                String secretKey = properties.getProperty("aws_secret_access_key");
                String sessionToken = properties.getProperty("aws_session_token");
                BasicSessionCredentials awsCreds = new BasicSessionCredentials(accessKey, secretKey,sessionToken);
                client = AmazonDynamoDBClientBuilder.standard()
                        .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                        .withRegion(Regions.US_EAST_1)
                        .build();
            }else{
                client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (client == null) {
            throw new IllegalStateException("DynamoDB client could not be initialized");
        }
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
