package com.infrasight.kodtest.service;

import com.infrasight.kodtest.api.client.GroupApiClient;
import com.infrasight.kodtest.api.client.RelationshipApiClient;
import com.infrasight.kodtest.api.model.Relationship;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GroupRelationshipService {
    private final RelationshipApiClient relationshipApiClient;
    private final GroupApiClient groupApiClient;

    public GroupRelationshipService(RelationshipApiClient relationshipApiClient,
                                    GroupApiClient groupApiClient) {
        this.relationshipApiClient = relationshipApiClient;
        this.groupApiClient = groupApiClient;
    }

    /**
     * Method for fetching immediate group IDs for a member.
     */
    public Set<String> getDirectGroupIdsForGroupMember(String memberId) {
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
     * Method for fetching all group IDs for a member. Handles group with groups.
     */
    public Set<String> getAllGroupIdsForGroupMember(String memberId) {
        Set<String> foundGroupIds = new HashSet<>();
        Set<String> idsOfActiveGroups = groupApiClient.getGroupIdsForActiveGroups();
        List<Relationship> relationshipsForAccount = relationshipApiClient.getRelationshipsByMemberId(memberId);
        for (Relationship relationship : relationshipsForAccount) {
            getActiveGroupIdsRecursively(relationship, foundGroupIds, new HashSet<>(), idsOfActiveGroups);
        }

        return foundGroupIds;
    }

    private void getActiveGroupIdsRecursively(Relationship relationship,
                                              Set<String> foundGroupIds,
                                              Set<String> processedMemberIds,
                                              Set<String> idsOfActiveGroups) {
        String groupId = relationship.getGroupId();
        if (!idsOfActiveGroups.contains(groupId)) {
            return;
        }

        foundGroupIds.add(groupId);
        processedMemberIds.add(relationship.getMemberId());
        if (!processedMemberIds.contains(groupId)) { // to handle circular references
            List<Relationship> groupRelationships = relationshipApiClient.getRelationshipsByMemberId(groupId);
            for (Relationship groupRelationship : groupRelationships) {
                getActiveGroupIdsRecursively(groupRelationship, foundGroupIds, processedMemberIds, idsOfActiveGroups);
            }
        }
    }

}