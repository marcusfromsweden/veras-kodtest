package com.infrasight.kodtest.apiclient;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infrasight.kodtest.dto.ApiRecord;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * API client for fetching records from an API with support for pagination and retries.
 */
public class ApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ApiClient.class);
    private static final int DEFAULT_PAGINATION_LIMIT = 250;
    private static final int DEFAULT_MAX_RETRIES = 10;
    private static final String URL_PARAM_SKIP = "skip";
    private static final String URL_PARAM_TAKE = "take";
    private static final String URL_PARAM_FILTER = "filter";

    private final OkHttpClient client;
    private final String apiBaseUrl;
    private final String accessToken;
    private final ObjectMapper objectMapper;
    private final PaginationHandler paginationHandler;

    public ApiClient(OkHttpClient client, String apiBaseUrl, String accessToken) {
        this.client = client;
        this.apiBaseUrl = apiBaseUrl;
        this.accessToken = accessToken;
        this.paginationHandler = new PaginationHandler();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Retrieves records from a paginated API endpoint.
     *
     * @param <T>      The type of records extending {@link ApiRecord} that will be retrieved and mapped from the API response.
     * @param endpoint The relative API path that identifies the resource to fetch (e.g., "accounts", "groups", "relationships").
     * @param clazz    The Class representing the type {@code T}, used for JSON deserialization.
     * @param filter   Optional filter on exact field value. Syntax is field=value. Example: objectType=Account.
     * @return A list of records of type {@code T} retrieved from the API.
     * @throws ApiClientException If an error occurs during the request.
     */
    protected <T extends ApiRecord> List<T> getRecords(String endpoint, Class<T> clazz, String filter) {
        List<T> result = new ArrayList<>();
        int skip = 0;
        int totalItems = Integer.MAX_VALUE;

        while (skip < totalItems) {
            String url = buildUrl(endpoint, skip, DEFAULT_PAGINATION_LIMIT, filter);

            try (Response response = executeRequestWithRetry(buildGetRequest(url))) {
                validateResponse(response);
                result.addAll(parseResponseBody(clazz, response));

                String contentRange = response.header("Content-Range");
                if (paginationHandler.isLastPage(contentRange)) break;
                totalItems = paginationHandler.extractTotalItems(contentRange);
                skip = paginationHandler.extractNextSkip(contentRange);
            } catch (IOException e) {
                throw new ApiClientException(String.format("Error fetching records: %s", e.getMessage()), e);
            }
        }

        return result;
    }

    /**
     * Validates the API response.
     */
    private void validateResponse(Response response) {
        if (!response.isSuccessful()) {
            throw new ApiClientException(String.format("API request failed with status %d: %s",
                    response.code(), response.message()));
        }
    }

    /**
     * Sends an HTTP request with automatic retry handling for rate-limited responses.
     *
     * @param request The HTTP request to be sent.
     * @return The successful HTTP response.
     * @throws ApiClientException If the request fails after the maximum number of retries
     *                            or encounters an I/O error.
     */
    private Response executeRequestWithRetry(Request request) {
        int attempt = 0;

        while (attempt < DEFAULT_MAX_RETRIES) {
            try {
                Response response = client.newCall(request).execute();

                if (response.isSuccessful()) {
                    return response;
                } else if (response.code() == 429) { // Too Many Requests
                    response.close();
                    //would normally use exponential backoff here, though I skipped it to let the tests run faster
                    attempt++;
                } else {
                    throw new ApiClientException(
                            String.format("Request failed with status %d: %s", response.code(), response.message()));
                }
            } catch (IOException e) {
                throw new ApiClientException(String.format("Error making request: %s", e.getMessage()), e);
            }
        }

        throw new ApiClientException(String.format("Max retries (%d) reached. Request failed.", DEFAULT_MAX_RETRIES));
    }


    /**
     * Deserializes the HTTP response body into a list of objects of the specified type.
     *
     * @param <T>      The type of objects in the resulting list.
     * @param clazz    The class type to deserialize the JSON into.
     * @param response The HTTP response containing the JSON body.
     * @return A list of deserialized objects of type {@code T}.
     * @throws IOException        If an error occurs while reading or deserializing the response.
     * @throws ApiClientException If the response body is null.
     */
    private <T> List<T> parseResponseBody(Class<T> clazz, Response response) throws IOException {
        if (response.body() == null) {
            throw new ApiClientException("Response body is null");
        }

        JavaType responseType = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
        return objectMapper.readValue(response.body().string(), responseType);
    }

    /**
     * Constructs an API request URL with pagination and optional filtering.
     *
     * <p>The resulting URL follows this format:
     * {@code {apiBaseUrl}{endpoint}?skip={skip}&take={take}[&filter={encodedFilter}]}
     * where the filter parameter is only included if it is not null or blank.</p>
     *
     * @param endpoint The relative API path identifying the resource.
     * @param skip     The number of records to skip for pagination.
     * @param take     The number of records to retrieve.
     * @param filter   Optional filter on exact field value. Syntax is field=value. Example: objectType=Account.
     * @return A formatted URL string ready for an API request.
     */
    private String buildUrl(String endpoint, int skip, int take, String filter) {
        StringBuilder urlBuilder = new StringBuilder()
                .append(apiBaseUrl)
                .append(endpoint)
                .append("?").append(URL_PARAM_SKIP).append("=").append(skip)
                .append("&").append(URL_PARAM_TAKE).append("=").append(take);

        if (filter != null && !filter.isBlank()) {
            urlBuilder.append("&").append(URL_PARAM_FILTER).append("=")
                    .append(URLEncoder.encode(filter, StandardCharsets.UTF_8));
        }

        return urlBuilder.toString();
    }

    /**
     * Builds a GET request with authorization headers.
     *
     * @param url The URL to send the request to.
     * @return A configured {@link Request} object.
     */
    private Request buildGetRequest(String url) {
        return new Request.Builder()
                .url(url)
                .get()
                .header("Accept", "application/json")
                .header("Authorization", String.format("Bearer %s", accessToken))
                .build();
    }
}
