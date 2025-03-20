package com.infrasight.kodtest.service;

import com.infrasight.kodtest.api.client.RelationshipApiClient;
import com.infrasight.kodtest.api.model.Relationship;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class AccountService {
    private final RelationshipApiClient relationshipApiClient;
    private final Set<String> allGroupIds;
    private final Set<String> idsOfActiveGroups;

    public AccountService(RelationshipApiClient relationshipApiClient,
                          Set<String> allGroupIds,
                          Set<String> idsOfActiveGroups) {
        this.relationshipApiClient = relationshipApiClient;
        this.allGroupIds = allGroupIds;
        this.idsOfActiveGroups = idsOfActiveGroups;
    }

    /**
     * Method for fetching all accounts under a group. Handles group with groups.
     */
    public Set<String> getAccountIdsByGroupId(String groupId) {
        Set<String> foundAccountIds = ConcurrentHashMap.newKeySet();

        List<Relationship> relationships = relationshipApiClient.getRelationshipsByGroupId(groupId);

        for (Relationship relationship : relationships) {
            getAccountIdsRecursively(relationship.getMemberId(), foundAccountIds);
        }

        return foundAccountIds;
    }

    private void getAccountIdsRecursively(String groupOrMemberId, Set<String> foundAccountIds) {
        if (allGroupIds.contains(groupOrMemberId)) {
            if (!idsOfActiveGroups.contains(groupOrMemberId)) {
                return;
            }
            List<Relationship> groupRelationships = relationshipApiClient.getRelationshipsByGroupId(groupOrMemberId);

            for (Relationship groupRelationship : groupRelationships) {
                getAccountIdsRecursively(groupRelationship.getMemberId(), foundAccountIds);
            }
        } else {
            foundAccountIds.add(groupOrMemberId);
        }
    }
}
