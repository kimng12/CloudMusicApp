package com.amazonaws.cloudmusic.webapp;

import com.amazonaws.cloudmusic.authentication.LoginHandler;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.amazonaws.cloudmusic.util.AWSUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/subscribeSong")
public class SubscribeServlet extends HttpServlet {
    private Table loginTable;

    @Override
    public void init() {
        DynamoDB dynamoDB = new DynamoDB(
                AmazonDynamoDBClientBuilder.standard()
                        .withRegion(Regions.US_EAST_1)
                        .withCredentials(new AWSStaticCredentialsProvider(AWSUtil.loadCredentials()))
                        .build()
        );
        loginTable = dynamoDB.getTable("login");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("email") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"User not logged in.\"}");
            return;
        }

        String email = (String) session.getAttribute("email");

        Map<String, Object> songData = new ObjectMapper().readValue(request.getReader(), Map.class);

        Map<String, Object> song = new HashMap<>();
        song.put("title", songData.get("title"));
        song.put("artist", songData.get("artist"));
        song.put("album", songData.get("album"));
        song.put("year", songData.get("year"));
        song.put("image_url", songData.get("imageUrl")); // Use "image_url" for storage, matches DB convention

        try {
            // Fetch existing subscriptions
            GetItemSpec spec = new GetItemSpec().withPrimaryKey("email", email);
            Item userItem = loginTable.getItem(spec);
            List<Map<String, Object>> subscriptions = userItem.getList("subscriptions");

            if (subscriptions == null) {
                subscriptions = new ArrayList<>();
            }

            // Check for duplicates (based on title + artist)
            boolean alreadySubscribed = subscriptions.stream().anyMatch(s ->
                    s.get("title").equals(song.get("title")) &&
                            s.get("artist").equals(song.get("artist"))
            );

            if (!alreadySubscribed) {
                subscriptions.add(song);

                UpdateItemSpec updateSpec = new UpdateItemSpec()
                        .withPrimaryKey("email", email)
                        .withUpdateExpression("set subscriptions = :s")
                        .withValueMap(new ValueMap().withList(":s", subscriptions))
                        .withReturnValues(ReturnValue.UPDATED_NEW);

                loginTable.updateItem(updateSpec);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"status\":\"success\"}");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Failed to subscribe to song.\"}");
        }
    }
}
