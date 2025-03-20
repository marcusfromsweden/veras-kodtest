package com.infrasight.kodtest.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infrasight.kodtest.api.model.AuthCredentials;
import com.infrasight.kodtest.exception.AuthenticationServiceException;
import okhttp3.*;


import java.io.IOException;

public class AuthenticationService {
    private static final String AUTH_PATH = "api/auth";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final OkHttpClient client;
    private final String baseUrl;

    public AuthenticationService(OkHttpClient client, int port) {
        this.client = client;
        this.baseUrl = buildBaseUrl(port);
    }

    public String authenticate(String username, String password) {
        String credentials = serializeCredentials(new AuthCredentials(username, password));
        RequestBody requestBody = RequestBody.create(credentials, MediaType.get("application/json"));
        String url = buildAuthUrl();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            validateResponse(response);
            JsonNode jsonResponse = parseResponseBody(response);
            return extractToken(jsonResponse);
        } catch (IOException e) {
            throw new AuthenticationServiceException(String.format("Failed to authenticate user '%s'", username), e);
        }
    }

    /**
     * Constructs the base API URL dynamically using OkHttp's HttpUrl.Builder.
     *
     * @param port The API port number.
     * @return The constructed base URL as a string.
     */
    private String buildBaseUrl(int port) {
        return new HttpUrl.Builder()
                .scheme("http")
                .host("localhost")
                .port(port)
                .build()
                .toString();
    }

    /**
     * Builds the full authentication endpoint URL dynamically using HttpUrl.Builder.
     *
     * @return The fully formatted authentication URL.
     * @throws AuthenticationServiceException If the base URL is invalid.
     */
    private String buildAuthUrl() {
        HttpUrl base = HttpUrl.parse(baseUrl);
        if (base == null) {
            throw new AuthenticationServiceException("Invalid base URL: " + baseUrl);
        }

        return base.newBuilder()
                .addPathSegments(AUTH_PATH)
                .build()
                .toString();
    }

    /**
     * Serializes authentication credentials to a JSON string.
     *
     * @param credentials The authentication credentials.
     * @return The JSON string representation of the credentials.
     * @throws AuthenticationServiceException If serialization fails.
     */
    private String serializeCredentials(AuthCredentials credentials) {
        try {
            return OBJECT_MAPPER.writeValueAsString(credentials);
        } catch (JsonProcessingException e) {
            throw new AuthenticationServiceException("Failed to serialize authentication request", e);
        }
    }

    /**
     * Validates the HTTP response.
     *
     * @param response The HTTP response to validate.
     * @throws AuthenticationServiceException If the response is unsuccessful.
     */
    private void validateResponse(Response response) {
        if (!response.isSuccessful()) {
            throw new AuthenticationServiceException(
                    String.format("Unexpected response: %s", response.code()));
        }
    }

    /**
     * Parses and validates the response body.
     *
     * @param response The HTTP response containing the JSON body.
     * @return The parsed JSON node.
     * @throws AuthenticationServiceException If the response body is null.
     */
    private JsonNode parseResponseBody(Response response) throws IOException {
        if (response.body() == null) {
            throw new AuthenticationServiceException("No body in response");
        }
        return OBJECT_MAPPER.readTree(response.body().string());
    }

    /**
     * Extracts and validates the authentication token from the JSON response.
     *
     * @param jsonResponse The JSON response containing the token.
     * @return The extracted token.
     * @throws AuthenticationServiceException If the token is missing or null.
     */
    private String extractToken(JsonNode jsonResponse) {
        if (!jsonResponse.has("token") || jsonResponse.get("token").isNull()) {
            throw new AuthenticationServiceException("Authentication response does not contain a valid token");
        }
        return jsonResponse.get("token").asText();
    }
}
