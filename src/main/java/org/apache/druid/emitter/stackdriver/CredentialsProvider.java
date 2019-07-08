package org.apache.druid.emitter.stackdriver;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;

/**
 * Retrieves access token from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
 */
public class CredentialsProvider {
    private GoogleCredentials credentials;

    CredentialsProvider() throws IOException {
        String credentialsFile = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        File initialFile = new File(credentialsFile);
        InputStream targetStream = new FileInputStream(initialFile);
        credentials = GoogleCredentials.fromStream(targetStream).createScoped(
                Collections.singletonList("https://www.googleapis.com/auth/monitoring")
        );
    }

    public String getAccessToken() throws IOException {
        return credentials.refreshAccessToken().getTokenValue();
    }
}
