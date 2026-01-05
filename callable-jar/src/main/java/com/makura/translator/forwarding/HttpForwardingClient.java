package com.makura.translator.forwarding;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Simple HTTP client for forwarding messages to downstream systems
 * Uses standard Java HTTP client (no external dependencies)
 */
public class HttpForwardingClient {

    private final int connectTimeout;
    private final int readTimeout;

    public HttpForwardingClient() {
        this(5000, 30000);
    }

    public HttpForwardingClient(int connectTimeout, int readTimeout) {
        this.connectTimeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * Forward target message to downstream endpoint
     */
    public String forward(String endpoint, String targetMessage, String apiKey) throws ForwardingException {
        HttpURLConnection connection = null;
        try {
            URL url = URI.create(endpoint).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/xml");
            if (apiKey != null && !apiKey.isEmpty()) {
                connection.setRequestProperty("X-API-Key", apiKey);
            }
            connection.setConnectTimeout(connectTimeout);
            connection.setReadTimeout(readTimeout);
            connection.setDoOutput(true);

            // Send request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = targetMessage.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read response
            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    return response.toString();
                }
            } else {
                throw new ForwardingException("HTTP error code: " + responseCode);
            }

        } catch (Exception e) {
            throw new ForwardingException("Failed to forward message to " + endpoint + ": " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    public static class ForwardingException extends Exception {
        public ForwardingException(String message) {
            super(message);
        }

        public ForwardingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}



