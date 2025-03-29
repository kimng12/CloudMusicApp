package com.amazonaws.cloudmusic.authentication;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
public class RegisterHandler {
    private final DynamoDB dynamoDB;
    private final Table loginTable;

    public RegisterHandler() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();
        this.dynamoDB = new DynamoDB(client);
        this.loginTable = dynamoDB.getTable("login");
    }

    // Creates user with given email, password, username and subscription status
    public String putItemInTable(String email, String password,String user_name) {
        String message = "";
        try {
            
            GetItemSpec spec = new GetItemSpec().withPrimaryKey("email", email);
            Item user = loginTable.getItem(spec);
            if (user==null){
                List<Map<String,String>> subscriptions = new ArrayList<>();

                Item item = new Item().withPrimaryKey("email", email)
                            .withString("password",password)
                            .withString("user_name",user_name)
                            .withList("subscriptions",subscriptions);
                this.loginTable.putItem(item);
                message = String.format("Succesfully added user %s",email);
            }else{
                message = "The email already exists";
            }
        } catch (Exception e) {
            message = "Register error: \" + e.getMessage()";
        }
        return message;
    }

}
