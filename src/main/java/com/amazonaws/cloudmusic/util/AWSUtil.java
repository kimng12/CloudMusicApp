package com.amazonaws.cloudmusic.util;

import com.amazonaws.auth.BasicSessionCredentials;

import java.io.InputStream;
import java.util.Properties;

public class AWSUtil {
    public static BasicSessionCredentials loadCredentials() {
        try (InputStream input = AWSUtil.class.getClassLoader().getResourceAsStream("aws-credentials.properties")) {
            Properties props = new Properties();
            props.load(input);

            return new BasicSessionCredentials(
                    props.getProperty("aws_access_key_id"),
                    props.getProperty("aws_secret_access_key"),
                    props.getProperty("aws_session_token")
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to load AWS credentials", e);
        }
    }
}
