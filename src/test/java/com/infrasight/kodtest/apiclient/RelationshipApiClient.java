package com.infrasight.kodtest.apiclient;

import com.infrasight.kodtest.dto.Relationship;

import java.io.IOException;
import java.util.List;

/**
 * API client for fetching Relationship-related data from the API.
 */
public class RelationshipApiClient {
    private static final String ENDPOINT = "relationships";
    private final ApiClient apiClient;

    public RelationshipApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Fetches all relationships where the given member ID is involved.
     *
     * @param memberId The ID of the member.
     * @return A list of relationships associated with the member.
     * @throws IOException If an API request fails.
     */
    public List<Relationship> getRelationshipsByMemberId(String memberId) throws IOException {
        return apiClient.fetchRecords(ENDPOINT, Relationship.class, "memberId=" + memberId);
    }

    /**
     * Fetches all relationships associated with a specific group ID.
     *
     * @param groupId The ID of the group.
     * @return A list of relationships for the given group.
     * @throws IOException If an API request fails.
     */
    public List<Relationship> getRelationshipsByGroupId(String groupId) throws IOException {
        return apiClient.fetchRecords(ENDPOINT, Relationship.class, "groupId=" + groupId);
    }

    /**
     * Fetches the relationship where the given managed ID is involved.
     * If multiple relationships are found, an exception is thrown.
     *
     * @param managedId The ID of the managed entity. //todo update as managedId is the Id of the manager for an employee
     * @return The unique relationship associated with the managed ID.
     * @throws IOException If an API request fails.
     * @throws ApiClientException If no or multiple relationships are found.
     */
    public Relationship getRelationshipsByManagedId(String managedId) throws IOException {
        List<Relationship> relationships = apiClient.fetchRecords(ENDPOINT, Relationship.class, "managedId=" + managedId);
        if (relationships.isEmpty()) {
            throw new ApiClientException("No Relationship found for managedId " + managedId);
        } else if (relationships.size() > 1) {
            throw new ApiClientException("More than one Relationship found for managedId " + managedId);
        }
        return relationships.get(0);
    }
}
