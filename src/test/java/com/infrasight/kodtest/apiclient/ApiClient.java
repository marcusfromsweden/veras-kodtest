package com.infrasight.kodtest.apiclient;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infrasight.kodtest.dto.Account;
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
import java.util.concurrent.TimeUnit;

/**
 * API client for fetching records from an API with support for pagination and retries.
 */
public class ApiClient {
    private static final int DEFAULT_PAGINATION_LIMIT = 25;
    private static final String PAGINATION_HEADER_PATTERN = "items (\\d+)-(\\d+)/(\\d+)";
    private static final int DEFAULT_MAX_RETRIES = 10;
    private static final long BASE_RETRY_DELAY_MS = 100; // Base delay for exponential backoff

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;
    private final String apiBaseUrl;
    private final String accessToken;

    public ApiClient(OkHttpClient client, String apiBaseUrl, String accessToken) {
        this.client = client;
        this.objectMapper = new ObjectMapper();
        this.apiBaseUrl = apiBaseUrl;
        this.accessToken = accessToken;
    }

    /**
     * Fetches all records from a paginated API endpoint.
     *
     * @param <T>      The type of records extending {@link ApiRecord}.
     * @param endpoint The API endpoint (e.g., "users", "orders").
     * @param clazz    The class type for JSON deserialization.
     * @param filter   Optional filter for API requests.
     * @return A list of records retrieved from the API.
     * @throws ApiClientException If an error occurs during the request.
     */
    public <T extends ApiRecord> List<T> fetchRecords(String endpoint, Class<T> clazz, String filter) {
        List<T> result = new ArrayList<>();
        int skip = 0;
        int totalItems = Integer.MAX_VALUE;

        while (skip < totalItems) {
            String url = buildUrl(endpoint, skip, DEFAULT_PAGINATION_LIMIT, filter);

            try (Response response = sendGetRequest(url)) {
                if (!response.isSuccessful()) {
                    throw new ApiClientException("Request failed: " + response.code() + " - " + response.message());
                }

                result.addAll(parseResponseBody(clazz, response));
                totalItems = extractTotalItems(response, totalItems);
                skip += DEFAULT_PAGINATION_LIMIT;

                // Process Content-Range header
                String contentRange = response.header("Content-Range");
                if (contentRange != null) {
                    Matcher matcher = Pattern.compile(PAGINATION_HEADER_PATTERN).matcher(contentRange);
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
            } catch (IOException e) {
                throw new ApiClientException("Error fetching records: " + e.getMessage(), e);
            }
        }

        return result;
    }

    /**
     * Sends an HTTP GET request with retry logic and exponential backoff.
     *
     * @param url The URL to send the request to.
     * @return The HTTP response.
     * @throws ApiClientException If the request fails after retries.
     */
    private Response sendGetRequest(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + accessToken)
                .build();

        return sendRequestWithRetry(request);
    }

    /**
     * Executes an HTTP request with retry logic for handling rate limits (HTTP 429).
     */
    private Response sendRequestWithRetry(Request request) {
        int attempt = 0;

        while (attempt < DEFAULT_MAX_RETRIES) {
            try {
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    return response;
                } else if (response.code() == 429) { // Too Many Requests
                    response.close();
                    //sleepWithBackoff(attempt); //todo remove
                    attempt++;
                } else {
                    throw new ApiClientException("Request failed: " + response.code() + " - " + response.message());
                }
            } catch (IOException e) {
                throw new ApiClientException("Error making request: " + e.getMessage(), e);
            }
        }

        throw new ApiClientException("Max retries reached. Request failed.");
    }

    /**
     * Parses the response body into a list of objects.
     */
    private <T> List<T> parseResponseBody(Class<T> clazz, Response response) throws IOException {
        JavaType responseType = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
        if (response.body() == null) {
            throw new ApiClientException("Response body is null");
        }
        return objectMapper.readValue(response.body().string(), responseType);
    }

    /**
     * Extracts the total number of items from the "Content-Range" header.
     */
    private int extractTotalItems(Response response, int currentTotal) {
        String contentRange = response.header("Content-Range");
        if (contentRange != null) {
            Matcher matcher = Pattern.compile(PAGINATION_HEADER_PATTERN).matcher(contentRange);
            if (matcher.matches()) {
                return Integer.parseInt(matcher.group(3));
            }
        }
        return currentTotal;
    }

    /**
     * Builds a URL with pagination and optional filters.
     */
    private String buildUrl(String endpoint, int skip, int take, String filter) {
        String url = String.format("%s%s?skip=%d&take=%d", apiBaseUrl, endpoint, skip, take);
        return (filter == null) ? url : url + "&filter=" + URLEncoder.encode(filter, StandardCharsets.UTF_8);
    }

    //todo remove?
    /**
     * Sleeps using an exponential backoff strategy to handle rate limits.
     */
    private void sleepWithBackoff(int attempt) {
        try {
            long delay = BASE_RETRY_DELAY_MS * (long) Math.pow(2, attempt);
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
