package com.infrasight.kodtest.apiclient;

import com.infrasight.kodtest.dto.Group;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * API client for fetching Group-related data from the API.
 */
public class GroupApiClient {
    private static final String ENDPOINT = "groups";
    private final ApiClient apiClient;

    public GroupApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Fetches all group IDs from the API.
     *
     * @return A set of all group IDs.
     * @throws IOException If an API request fails.
     */
    public Set<String> getAllGroupIds() throws IOException {
        return apiClient.getRecords(ENDPOINT, Group.class, null).stream()
                .map(Group::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Fetches group IDs for active groups.
     *
     * @return A set of active group IDs.
     * @throws IOException If an API request fails.
     */
    public Set<String> getGroupIdsForActiveGroups() throws IOException {
        return apiClient.getRecords(ENDPOINT, Group.class, null).stream()
                .filter(Group::isActive)
                .map(Group::getId)
                .collect(Collectors.toSet());
    }
}
