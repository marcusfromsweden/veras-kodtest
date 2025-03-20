package com.infrasight.kodtest.api.client;

import com.infrasight.kodtest.api.model.Group;

import java.util.List;
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
     * Retrieves all group IDs from the API.
     *
     * @return a set of all group IDs.
     */
    public Set<String> getAllGroupIds() {
        return getAllGroups().stream()
                .map(Group::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Retrieves the IDs of all active groups.
     *
     * @return a set of group IDs
     */
    public Set<String> getGroupIdsForActiveGroups() {
        return getAllGroups().stream()
                .filter(Group::isActive)
                .map(Group::getId)
                .collect(Collectors.toSet());
    }

    /**
     * Fetches all groups from the API.
     *
     * @return A list of all groups.
     */
    private List<Group> getAllGroups() {
        return apiClient.getRecords(ENDPOINT, Group.class, null);
    }
}
