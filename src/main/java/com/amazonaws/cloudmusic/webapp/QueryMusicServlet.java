package com.amazonaws.cloudmusic.webapp;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.cloudmusic.util.AWSUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet("/queryMusic")
public class QueryMusicServlet extends HttpServlet {
    private Table musicTable;

    @Override
    public void init() {
        DynamoDB dynamoDB = new DynamoDB(
                AmazonDynamoDBClientBuilder.standard()
                        .withRegion(Regions.US_EAST_1)
                        .withCredentials(new AWSStaticCredentialsProvider(AWSUtil.loadCredentials()))
                        .build()
        );
        musicTable = dynamoDB.getTable("music");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String title = request.getParameter("title");
        String artist = request.getParameter("artist");
        String album = request.getParameter("album");
        String year = request.getParameter("year");

        List<String> missing = new ArrayList<>();
        if (isEmpty(title) && isEmpty(artist) && isEmpty(album) && isEmpty(year)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"At least one query field is required.\"}");
            return;
        }

        // Scan with filters (since querying on non-key fields)
        ScanSpec scanSpec = new ScanSpec();
        List<Map<String, Object>> results = new ArrayList<>();

        try {
            ItemCollection<ScanOutcome> items = musicTable.scan(scanSpec);
            for (Item item : items) {
                boolean match = true;
                if (!isEmpty(title) && !item.getString("title").toLowerCase().contains(title.toLowerCase())) match = false;
                if (!isEmpty(artist) && !item.getString("artist").toLowerCase().contains(artist.toLowerCase())) match = false;
                if (!isEmpty(album) && !item.getString("album").toLowerCase().contains(album.toLowerCase())) match = false;
                if (!isEmpty(year) && !item.getString("year").equals(year)) match = false;

                if (match) {
                    Map<String, Object> song = new HashMap<>();
                    song.put("title", item.getString("title"));
                    song.put("artist", item.getString("artist"));
                    song.put("album", item.getString("album"));
                    song.put("year", item.getString("year"));
                    song.put("imageUrl", item.getString("image_url"));  // S3 image

                    results.add(song);
                }
            }

            response.setContentType("application/json");
            new ObjectMapper().writeValue(response.getWriter(), results);

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\":\"Query failed.\"}");
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
