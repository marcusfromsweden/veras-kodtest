package com.infrasight.kodtest.service.resolver;

import com.infrasight.kodtest.api.client.RelationshipApiClient;
import com.infrasight.kodtest.api.model.Relationship;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class GroupMemberAccountResolver {
    private final RelationshipApiClient relationshipApiClient;
    private final Set<String> allGroupIds;
    private final Set<String> idsOfActiveGroups;

    public GroupMemberAccountResolver(RelationshipApiClient relationshipApiClient,
                                      Set<String> allGroupIds,
                                      Set<String> idsOfActiveGroups) {
        this.relationshipApiClient = relationshipApiClient;
        this.allGroupIds = allGroupIds;
        this.idsOfActiveGroups = idsOfActiveGroups;
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
        if (allGroupIds.contains(groupOrMemberId)) {
            if (!idsOfActiveGroups.contains(groupOrMemberId)) {
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
}
