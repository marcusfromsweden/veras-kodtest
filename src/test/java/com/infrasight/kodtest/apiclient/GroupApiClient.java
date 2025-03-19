package com.infrasight.kodtest.apiclient;

import com.infrasight.kodtest.dto.Group;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

public class GroupApiClient extends ApiClient {
    private static final String ENDPOINT = "groups";

    public GroupApiClient(OkHttpClient httpClient, String accessToken) {
        super(httpClient, accessToken);
    }

    public Set<String> getAllGroupIds() throws IOException {
        return getRecordsFromEndpoint(ENDPOINT, Group.class).stream().map(Group::getId).collect(Collectors.toSet());
    }

    public Set<String> getGroupIdsForActiveGroups() throws IOException {
        return getRecordsFromEndpoint(ENDPOINT, Group.class).stream()
                .filter(Group::isActive).map(Group::getId).collect(Collectors.toSet());
    }
}
