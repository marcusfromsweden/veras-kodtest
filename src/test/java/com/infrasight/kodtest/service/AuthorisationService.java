package com.infrasight.kodtest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infrasight.kodtest.api.model.AuthCredentials;
import okhttp3.*;

import java.io.IOException;

public class AuthorisationService {
    private static final String DYNAMIC_PORT_API_URL = "http://localhost:%s/api/auth";
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final int port;

    public AuthorisationService(OkHttpClient client, int port) {
        this.client = client;
        this.objectMapper = new ObjectMapper();
        this.port = port;
    }

    public String authenticate(String username, String password) throws IOException {
        String credentials = objectMapper.writeValueAsString(
                new AuthCredentials(username, password)
        );

        RequestBody requestBody = RequestBody.create(credentials, MediaType.get("application/json"));
        String url = String.format(DYNAMIC_PORT_API_URL, port);
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected response: " + response.code());
            }

            if (response.body() == null) {
                throw new RuntimeException("No body in response");
            }
            JsonNode jsonResponse = objectMapper.readTree(response.body().string());
            return jsonResponse.get("token").asText();
        }
    }
}
