package com.infrasight.kodtest.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Relationship extends ApiRecord {
    @SuppressWarnings("unused")
    private String id;
    @SuppressWarnings("unused")
    private String groupId;
    @SuppressWarnings("unused")
    private String memberId;
    @SuppressWarnings("unused")
    private String accountId;

    public String getGroupId() {
        return groupId;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getAccountId() {
        return accountId;
    }

    @Override
    public String toString() {
        return "Relationship{" +
                ", id='" + id + '\'' +
                ", groupId='" + groupId + '\'' +
                ", memberId='" + memberId + '\'' +
                ", accountId='" + accountId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Relationship that = (Relationship) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}