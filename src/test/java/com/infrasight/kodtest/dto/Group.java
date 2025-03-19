package com.infrasight.kodtest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Group extends ApiRecord {
    @SuppressWarnings("unused")
    private String id;
    @SuppressWarnings("unused")
    private boolean active;
    @SuppressWarnings("unused")
    private String name;

    public String getId() {
        return id;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return "Group{" +
                ", id='" + id + '\'' +
                ", active=" + active +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return Objects.equals(id, group.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}