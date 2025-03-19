package com.infrasight.kodtest.apiclient;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infrasight.kodtest.dto.ApiRecord;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.infrasight.kodtest.TestVariables.API_PORT;

public class ApiClient {
    private static final String API_BASE_URL = "http://localhost:"+API_PORT+"/api/";
    private static final int PAGINATION_MAX_TAKE = 25;
    private static final String PAGINATION_RANGE_HEADER_PATTERN = "items (\\d+)-(\\d+)/(\\d+)";
    private static final int MAX_RETRIES_FOR_TOO_MANY_REQUESTS = 10;

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final String accessToken;

    protected ApiClient(OkHttpClient client, String accessToken) {
        this.client = client;
        this.objectMapper = new ObjectMapper();
        this.accessToken = accessToken;
    }

    protected <T extends ApiRecord> List<T> getRecordsFromEndpoint(String endpoint, Class<T> clazz) throws IOException {
        return getRecordsFromEndpoint(endpoint, clazz, null);
    }

    /**
     * Fetches records (Account, Group, Relationship) from the specified API endpoint.
     * This method retrieves data in a paginated manner using the "skip" and "take" query parameters.
     * If the API response returns an HTTP 429 (Too Many Requests), the request is retried.
     */
    protected <T extends ApiRecord> List<T> getRecordsFromEndpoint(String endpoint, Class<T> clazz, String filter) throws IOException {
        List<T> result = new ArrayList<>();
        int skip = 0;
        int totalItems = Integer.MAX_VALUE;

        while (skip < totalItems) {
            String url = String.format("%s%s?skip=%d&take=%d", API_BASE_URL, endpoint, skip, PAGINATION_MAX_TAKE);
            url = addFilter(filter, url);

            try (Response response = sendGetRequest(url)) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Request failed: " + response.code() + " - " + response.message());
                }

                result.addAll(parseResponseBody(clazz, response));

                // Process Content-Range header
                String contentRange = response.header("Content-Range");
                if (contentRange != null) {
                    Matcher matcher = Pattern.compile(PAGINATION_RANGE_HEADER_PATTERN).matcher(contentRange);
                    if (matcher.matches()) {
                        int endIndex = Integer.parseInt(matcher.group(2));
                        totalItems = Integer.parseInt(matcher.group(3));
                        skip = endIndex + 1;
                    } else if (contentRange.startsWith("items */")) { // No more items
                        break;
                    }
                } else { // no Content-Range header
                    break;
                }
            }
        }

        return result;
    }

    private static String addFilter(String filter, String url) {
        return filter == null ? url : url + "&filter=" + URLEncoder.encode(filter, StandardCharsets.UTF_8);
    }

    private Response sendGetRequest(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .build();

        return sendRequest(request);
    }

    private Response sendRequest(Request request) {
        int nbrAttemptTooManyRequests = 0;

        while (nbrAttemptTooManyRequests < MAX_RETRIES_FOR_TOO_MANY_REQUESTS) {
            try {
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    return response;
                } else if (response.code() == 429) {
                    response.close();
                    nbrAttemptTooManyRequests++;
                } else {
                    throw new RuntimeException("Request failed: " + response.code() + " - " + response.message());
                }
            } catch (IOException e) {
                throw new RuntimeException("Error making request: " + e.getMessage(), e);
            }
        }

        throw new RuntimeException("Max retries reached. Request failed.");
    }

    private <T> List<T> parseResponseBody(Class<T> clazz, Response response) throws IOException {
        JavaType responseType = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
        if (response.body() == null) {
            throw new RuntimeException("Request failed as body was null");
        }
        return objectMapper.readValue(response.body().string(), responseType);
    }
}
