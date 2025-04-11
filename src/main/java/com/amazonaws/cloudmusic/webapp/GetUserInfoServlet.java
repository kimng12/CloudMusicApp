package com.amazonaws.cloudmusic.webapp;

import com.amazonaws.cloudmusic.authentication.LoginHandler;
import com.amazonaws.services.dynamodbv2.document.Item;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;


@WebServlet("/getUserInfo")
public class GetUserInfoServlet extends HttpServlet {
    private LoginHandler loginHandler;

    @Override
    public void init() {
        loginHandler = new LoginHandler();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Check Session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("email") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"User not logged in.\"}");
            return;
        }
        String email = (String) session.getAttribute("email");

        // Retrieve raw user information
        String username = loginHandler.getUsername(email);
        Item userItem = loginHandler.getUserItem(email);

        // Turn the raw subs information into expected format
        List<Map<String, Object>> rawSubs = userItem.getList("subscriptions");
        List<Map<String, String>> cleanedSubs = new ArrayList<>();

        if (rawSubs != null) {
            for (Map<String, Object> wrapper : rawSubs) {
                Map<String, Object> rawMap = (Map<String, Object>) wrapper;

                String title = (String) rawMap.get("title");
                String artist = (String) rawMap.get("artist");
                String album = (String) rawMap.get("album");
                String year = rawMap.get("year").toString();
                String imageUrl = (String) rawMap.get("image_url");

                Map<String, String> song = new HashMap<>();
                song.put("title", title);
                song.put("artist", artist);
                song.put("album", album);
                song.put("year", year);
                song.put("imageUrl", imageUrl);

                cleanedSubs.add(song);
            }
        }

        // Write Response
        Map<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("subscriptions", cleanedSubs);

        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getWriter(), result);
    }

}
