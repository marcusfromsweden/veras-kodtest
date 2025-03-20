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
     * Retrieves the IDs of all accounts associated with a given group, including accounts
     * from subgroups if the group structure is hierarchical.
     *
     * @param groupId the ID of the group for which to retrieve account IDs.
     * @return a set of account IDs belonging to the specified group and its sub-groups.
     */
    public Set<String> getAccountIdsForGroup(String groupId) {
        Set<String> foundAccountIds = ConcurrentHashMap.newKeySet();

        List<Relationship> relationships = relationshipApiClient.getRelationshipsByGroupId(groupId);

        for (Relationship relationship : relationships) {
            getAccountIdsForGroupRecursively(relationship.getMemberId(), foundAccountIds);
        }

        return foundAccountIds;
    }

    /**
     * Recursively collects account IDs. If the provided ID represents an active group, its members are processed recursively.
     * Otherwise, the ID is added to the set of found account IDs.
     * <p>
     * Inactive groups are ignored, and their members are not processed.
     * </p>
     *
     * @param groupOrMemberId the ID of a group or account.
     * @param foundAccountIds the set to which discovered account IDs are added.
     */
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
