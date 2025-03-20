package com.infrasight.kodtest.api.client;

import com.infrasight.kodtest.api.model.Account;
import com.infrasight.kodtest.exception.AccountApiClientException;

import java.util.List;

/**
 * API client for fetching Account-related data from the API.
 */
public class AccountApiClient {
    private static final String ENDPOINT = "accounts";
    private static final String PARAM_EMPLOYEE_ID = "employeeId";
    private static final String PARAM_ID = "id";
    private static final String PARAM_FIRST_NAME = "firstName";

    private final ApiClient apiClient;

    public AccountApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public List<Account> getAccountsByEmployeeId(String employeeId) {
        return apiClient.getRecords(ENDPOINT, Account.class, String.format("%s=%s", PARAM_EMPLOYEE_ID, employeeId));
    }

    public Account getAccountById(String accountId) {
        List<Account> accounts = apiClient.getRecords(ENDPOINT, Account.class, String.format("%s=%s", PARAM_ID, accountId));
        if (accounts.isEmpty()) {
            throw new AccountApiClientException(String.format("No Account found for accountId %s", accountId));
        } else if (accounts.size() > 1) {
            throw new AccountApiClientException(String.format("Multiple Accounts found for accountId %s", accountId));
        }
        return accounts.get(0);
    }

    public List<Account> getAccountsByFirstName(String firstName) {
        return apiClient.getRecords(ENDPOINT, Account.class, String.format("%s=%s", PARAM_FIRST_NAME, firstName));
    }

}
