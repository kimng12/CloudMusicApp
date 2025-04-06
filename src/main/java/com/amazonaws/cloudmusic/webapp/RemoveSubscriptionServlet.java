package com.amazonaws.cloudmusic.webapp;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.cloudmusic.util.AWSUtil;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/removeSong")
public class RemoveSubscriptionServlet extends HttpServlet {
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
        String titleToRemove = request.getParameter("title");

        if (titleToRemove == null || titleToRemove.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"Missing song title to remove.\"}");
            return;
        }

        try {
            GetItemSpec spec = new GetItemSpec().withPrimaryKey("email", email);
            Item userItem = loginTable.getItem(spec);
            List<Map<String, Object>> subscriptions = userItem.getList("subscriptions");

            if (subscriptions == null || subscriptions.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write("{\"status\":\"nothing to remove\"}");
                return;
            }

            // Remove song by matching the titles
            subscriptions.removeIf(song ->
                    titleToRemove.equals(song.get("title"))
            );

            UpdateItemSpec updateSpec = new UpdateItemSpec()
                    .withPrimaryKey("email", email)
                    .withUpdateExpression("set subscriptions = :s")
                    .withValueMap(new ValueMap().withList(":s", subscriptions))
                    .withReturnValues(ReturnValue.UPDATED_NEW);

            loginTable.updateItem(updateSpec);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"status\":\"removed\"}");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Failed to remove subscription.\"}");
        }
    }
}
