package com.amazonaws.cloudmusic.s3;

// Import
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ImageUploader {
    private static final String BUCKET_NAME = "s3970589-a1-s3-bucket";

    public static void main(String[] args) {
        AmazonS3 s3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build();

        Set<String> downloaded = new HashSet<>();

        try {
            ObjectMapper mapper = new ObjectMapper();
            File jsonFile = new File("data/2025a1.json");
            JsonNode root = mapper.readTree(jsonFile);
            JsonNode songs = root.get("songs");

            File tempDir = new File("tmp/images");
            if (!tempDir.exists()) tempDir.mkdirs();

            for (JsonNode song : songs) {
                String imgUrl = song.get("img_url").asText();
                String fileName = imgUrl.substring(imgUrl.lastIndexOf("/") + 1);

                if (downloaded.contains(fileName)) continue;
                downloaded.add(fileName);

                File imageFile = new File(tempDir, fileName);

                // 1. Download image
                try (InputStream in = new URL(imgUrl).openStream();
                     OutputStream out = new FileOutputStream(imageFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    System.out.println("Downloaded: " + fileName);
                }

                // 2. Upload to S3
                s3.putObject(BUCKET_NAME, "images/" + fileName, imageFile);
                System.out.println("Uploaded to S3: images/" + fileName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
