package com.infrasight.kodtest.resolver;

import com.infrasight.kodtest.api.client.GroupApiClient;
import com.infrasight.kodtest.api.client.RelationshipApiClient;
import com.infrasight.kodtest.api.model.Relationship;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupAssociationResolver {
    private final RelationshipApiClient relationshipApiClient;
    private final GroupApiClient groupApiClient;

    public GroupAssociationResolver(RelationshipApiClient relationshipApiClient,
                                    GroupApiClient groupApiClient) {
        this.relationshipApiClient = relationshipApiClient;
        this.groupApiClient = groupApiClient;
    }

    /**
     * Retrieves the direct group IDs for a given member.
     * <p>
     * This method fetches all relationships where the given member ID is directly assigned to a group.
     * Only active groups are included in the result.
     * </p>
     *
     * @param memberId the ID of the member.
     * @return a set of group IDs to which the member is directly assigned.
     */
    public Set<String> getIdsForMembersDirectGroups(String memberId) {
        List<Relationship> relationshipsForVera = relationshipApiClient.getRelationshipsByMemberId(memberId);
        Set<String> idsOfActiveGroups = groupApiClient.getGroupIdsForActiveGroups();

        // getting IDs for active groups
        Set<String> groupIds = new HashSet<>();
        for (Relationship relationship : relationshipsForVera) {
            String groupId = relationship.getGroupId();
            if (idsOfActiveGroups.contains(groupId)) {
                groupIds.add(groupId);
            }
        }
        return groupIds;
    }

    /**
     * Retrieves all group IDs for a given member, including nested group memberships.
     * <p>
     * This method finds all groups a member belongs to, directly or indirectly, by recursively
     * following group relationships. Only active groups are considered.
     * </p>
     *
     * @param memberId the ID of the member.
     * @return a set of all group IDs the member belongs to, including indirect group memberships.
     */
    public Set<String> getIdsForMembersDirectAndIndirectGroups(String memberId) {
        Set<String> discoveredGroupIds = new HashSet<>();
        Set<String> idsOfActiveGroups = groupApiClient.getGroupIdsForActiveGroups();
        List<Relationship> relationshipsForAccount = relationshipApiClient.getRelationshipsByMemberId(memberId);
        for (Relationship relationship : relationshipsForAccount) {
            getIdsForMembersDirectAndIndirectGroupsRecursively(relationship, discoveredGroupIds, new HashSet<>(), idsOfActiveGroups);
        }

        return discoveredGroupIds;
    }

    /**
     * Recursively collects group IDs for a given relationship.
     * <p>
     * If the provided relationship points to an active group, its group memberships are processed recursively.
     * </p>
     *
     * @param relationship       the relationship to process.
     * @param discoveredGroupIds a set where discovered group IDs are stored.
     * @param processedMemberIds a set to track processed members, preventing infinite recursion (for circular references).
     * @param idsOfActiveGroups  a set containing the IDs of all active groups.
     */
    private void getIdsForMembersDirectAndIndirectGroupsRecursively(Relationship relationship,
                                                                    Set<String> discoveredGroupIds,
                                                                    Set<String> processedMemberIds,
                                                                    Set<String> idsOfActiveGroups) {
        String groupId = relationship.getGroupId();
        if (!idsOfActiveGroups.contains(groupId)) {
            return;
        }

        discoveredGroupIds.add(groupId);
        processedMemberIds.add(relationship.getMemberId());
        if (!processedMemberIds.contains(groupId)) {
            List<Relationship> groupRelationships = relationshipApiClient.getRelationshipsByMemberId(groupId);
            for (Relationship groupRelationship : groupRelationships) {
                getIdsForMembersDirectAndIndirectGroupsRecursively(groupRelationship, discoveredGroupIds, processedMemberIds, idsOfActiveGroups);
            }
        }
    }

}