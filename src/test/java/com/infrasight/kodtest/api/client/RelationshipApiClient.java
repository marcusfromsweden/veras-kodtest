package com.infrasight.kodtest.api.client;

import com.infrasight.kodtest.api.model.Relationship;
import com.infrasight.kodtest.exception.RelationshipApiClientException;

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
     * Retrieves relationships associated with a given member ID.
     *
     * @param memberId The unique identifier of the member whose relationships should be retrieved.
     * @return A list of {@link Relationship} objects associated with the given member ID.
     * If no relationships exist, an empty list is returned.
     */
    public List<Relationship> getRelationshipsByMemberId(String memberId) {
        return apiClient.getRecords(ENDPOINT, Relationship.class, "memberId=" + memberId);
    }

    /**
     * Retrieves relationships for a given group ID.
     *
     * @param groupId The unique identifier of the group.
     * @return A list of {@link Relationship} objects.
     * If no relationships exist, an empty list is returned.
     */
    public List<Relationship> getRelationshipsByGroupId(String groupId) {
        return apiClient.getRecords(ENDPOINT, Relationship.class, "groupId=" + groupId);
    }

    /**
     * Retrieves the relationship for a given managed account ID.
     *
     * @param managedId The account ID of an employee that is managed (by a specific manager.)
     * @return The {@link Relationship} associated with the given managed account ID.
     * @throws RelationshipApiClientException If none or multiple relationships are found.
     */
    public Relationship getRelationshipsByManagedId(String managedId) {
        List<Relationship> relationships = apiClient.getRecords(ENDPOINT, Relationship.class, "managedId=" + managedId);
        if (relationships.isEmpty()) {
            throw new RelationshipApiClientException(String.format("No Relationship found for managedId %s", managedId));
        } else if (relationships.size() > 1) {
            throw new RelationshipApiClientException(String.format("More than one Relationship found for managedId %s", managedId));
        }
        return relationships.get(0);
    }
}
