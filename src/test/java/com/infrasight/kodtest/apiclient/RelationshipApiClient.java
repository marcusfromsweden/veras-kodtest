package com.infrasight.kodtest.apiclient;

import com.infrasight.kodtest.dto.Relationship;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.List;

public class RelationshipApiClient extends ApiClient {
    private static final String ENDPOINT = "relationships";

    public RelationshipApiClient(OkHttpClient client, String accessToken) {
        super(client, accessToken);
    }

    public List<Relationship> getRelationshipsByMemberId(String memberId) throws IOException {
        return getRecordsFromEndpoint(ENDPOINT, Relationship.class, "memberId=" + memberId);
    }

    public List<Relationship> getRelationshipsByGroupId(String groupId) throws IOException {
        return getRecordsFromEndpoint(ENDPOINT, Relationship.class, "groupId=" + groupId);
    }

    public Relationship getRelationshipsByManagedId(String managedId) throws IOException {
        List<Relationship> relationships = getRecordsFromEndpoint(ENDPOINT, Relationship.class, "managedId=" + managedId);
        if (relationships.isEmpty()) {
            throw new RuntimeException("No Relationship found for managedId " + managedId);
        } else if (relationships.size() > 1) {
            throw new RuntimeException("More than one Relationship found for managedId " + managedId);
        }
        return relationships.get(0);
    }

}
