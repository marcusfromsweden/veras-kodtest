package com.infrasight.kodtest.api.client;

import com.infrasight.kodtest.api.model.Account;
import com.infrasight.kodtest.exception.AccountApiClientException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    /**
     * Retrieves accounts associated with a given employee ID.
     *
     * @param employeeId The unique identifier of the employee whose accounts are to be retrieved.
     * @return A list of {@link Account} objects associated with the given employee ID.
     */
    public List<Account> getAccountsByEmployeeId(String employeeId) {
        return apiClient.getRecords(ENDPOINT, Account.class, String.format("%s=%s", PARAM_EMPLOYEE_ID, employeeId));
    }

    /**
     * Retrieves an account by ID.
     *
     * @param accountId The unique identifier of the account to retrieve.
     * @return The {@link Account} object corresponding to the given account ID.
     * @throws AccountApiClientException If none or multiple accounts are found.
     */
    public Account getAccountById(String accountId) {
        List<Account> accounts = apiClient.getRecords(ENDPOINT, Account.class, String.format("%s=%s", PARAM_ID, accountId));
        if (accounts.isEmpty()) {
            throw new AccountApiClientException(String.format("No Account found for accountId %s", accountId));
        } else if (accounts.size() > 1) {
            throw new AccountApiClientException(String.format("Multiple Accounts found for accountId %s", accountId));
        }
        return accounts.get(0);
    }

    /**
     * Retrieves accounts matching the specified first name.
     *
     * @param firstName The first name used to filter the accounts.
     * @return A list of {@link Account} objects with the specified first name.
     * If no matching accounts are found, an empty list is returned.
     */
    public List<Account> getAccountsByFirstName(String firstName) {
        return apiClient.getRecords(ENDPOINT, Account.class, String.format("%s=%s", PARAM_FIRST_NAME, firstName));
    }

    /**
     * Retrieves active accounts from the provided set of account IDs.
     *
     * @param accountIds A set of account IDs to retrieve and filter.
     * @return A list of active {@link Account} objects corresponding to the provided IDs.
     * @throws AccountApiClientException If none or multiple accounts are found.
     */
    public List<Account> getActiveAccountsByIds(Set<String> accountIds) {
        List<Account> accounts = new ArrayList<>();
        for (String accountId : accountIds) {
            Account account = getAccountById(accountId);
            if (account.isActive()) {
                accounts.add(account);
            }
        }
        return accounts;
    }
}
