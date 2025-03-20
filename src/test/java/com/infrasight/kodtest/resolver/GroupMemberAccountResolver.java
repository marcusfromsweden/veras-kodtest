package com.infrasight.kodtest.resolver;

import com.infrasight.kodtest.api.client.GroupApiClient;
import com.infrasight.kodtest.api.client.RelationshipApiClient;
import com.infrasight.kodtest.api.model.Relationship;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GroupMemberAccountResolver {
    private final RelationshipApiClient relationshipApiClient;
    private final GroupApiClient groupApiClient;
    private Set<String> allGroupIds;
    private Set<String> idsOfActiveGroups;

    public GroupMemberAccountResolver(RelationshipApiClient relationshipApiClient,
                                      GroupApiClient groupApiClient) {
        this.relationshipApiClient = relationshipApiClient;
        this.groupApiClient = groupApiClient;
    }

    /**
     * Method for fetching all account IDs under a group. Handles group with groups.
     */
    public Set<String> getAccountIdsForGroup(String groupId) {
        Set<String> foundAccountIds = ConcurrentHashMap.newKeySet();

        List<Relationship> relationships = relationshipApiClient.getRelationshipsByGroupId(groupId);

        for (Relationship relationship : relationships) {
            getAccountIdsForGroupRecursively(relationship.getMemberId(), foundAccountIds);
        }

        return foundAccountIds;
    }

    private void getAccountIdsForGroupRecursively(String groupOrMemberId, Set<String> foundAccountIds) {
        if (getIdsOfAllGroups().contains(groupOrMemberId)) {
            if (!getIdsOfActiveGroups().contains(groupOrMemberId)) {
                return;
            }
            List<Relationship> groupRelationships = relationshipApiClient.getRelationshipsByGroupId(groupOrMemberId);

            for (Relationship groupRelationship : groupRelationships) {
                getAccountIdsForGroupRecursively(groupRelationship.getMemberId(), foundAccountIds);
            }
        } else {
            foundAccountIds.add(groupOrMemberId);
        }
    }

    private Set<String> getIdsOfAllGroups() {
        if (allGroupIds == null) {
            allGroupIds = groupApiClient.getAllGroupIds();
        }
        return allGroupIds;
    }

    private Set<String> getIdsOfActiveGroups() {
        if (idsOfActiveGroups == null) {
            idsOfActiveGroups = groupApiClient.getGroupIdsForActiveGroups();
        }
        return idsOfActiveGroups;
    }
}
