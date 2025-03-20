package com.infrasight.kodtest.api.client;

import com.infrasight.kodtest.api.model.Account;

import java.util.List;

/**
 * API client for fetching Account-related data from the API.
 */
public class AccountApiClient {
    private static final String ENDPOINT = "accounts";
    private final ApiClient apiClient;

    public AccountApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<Account> getAccountsByEmployeeId(String employeeId) {
        return apiClient.getRecords(ENDPOINT, Account.class, "employeeId=" + employeeId);
    }

    public Account getAccountById(String accountId) {
        List<Account> accounts = apiClient.getRecords(ENDPOINT, Account.class, "id=" + accountId);
        if (accounts.isEmpty()) {
            throw new ApiClientException("No Account found for accountId " + accountId);
        } else if (accounts.size() > 1) {
            throw new ApiClientException("Multiple Accounts found for accountId " + accountId);
        }
        return accounts.get(0);
    }

    public List<Account> getAccountsByFirstName(String firstName) {
        return apiClient.getRecords(ENDPOINT, Account.class, "firstName=" + firstName);
    }

    public List<Account> getAllAccounts() {
        return apiClient.getRecords(ENDPOINT, Account.class, null);
    }
}
