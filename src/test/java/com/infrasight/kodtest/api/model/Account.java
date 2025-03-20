package com.infrasight.kodtest.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Account extends ApiRecord {
    @SuppressWarnings("unused")
    private String id;
    @SuppressWarnings("unused")
    private int salary;
    @SuppressWarnings("unused")
    private boolean active;
    @SuppressWarnings("unused")
    private String firstName;
    @SuppressWarnings("unused")
    private String salaryCurrency;
    @SuppressWarnings("unused")
    private long employedSince;
    @SuppressWarnings("unused")
    private String lastName;

    public String getId() {
        return id;
    }

    public int getSalary() {
        return salary;
    }

    public boolean isActive() {
        return active;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSalaryCurrency() {
        return salaryCurrency;
    }

    public long getEmployedSince() {
        return employedSince;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id='" + id + '\'' +
                ", salary=" + salary +
                ", active=" + active +
                ", firstName='" + firstName + '\'' +
                ", salaryCurrency='" + salaryCurrency + '\'' +
                ", employedSince=" + employedSince +
                ", lastName='" + lastName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}