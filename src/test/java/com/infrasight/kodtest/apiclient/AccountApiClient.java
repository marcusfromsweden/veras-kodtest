package com.infrasight.kodtest.apiclient;

import com.infrasight.kodtest.dto.Account;
import okhttp3.OkHttpClient;

import java.io.IOException;
import java.util.List;

public class AccountApiClient extends ApiClient {
    private static final String ENDPOINT = "accounts";

    public AccountApiClient(OkHttpClient httpClient, String accessToken) {
        super(httpClient, accessToken);
    }

    public List<Account> getAccountsByEmployeeId(String employeeId) throws IOException {
        return getRecordsFromEndpoint(ENDPOINT, Account.class, "employeeId=" + employeeId);
    }

    public Account getAccountById(String accountId) throws IOException {
        List<Account> accounts = getRecordsFromEndpoint(ENDPOINT, Account.class, "id=" + accountId);
        if (accounts.isEmpty()) {
            throw new RuntimeException("No Account found for accountId " + accountId);
        } else if (accounts.size() > 1) {
            throw new RuntimeException("More than one Account found for accountId " + accountId);
        }
        return accounts.get(0);
    }

    public List<Account> getAccountsByFirstName(String firstName) throws IOException {
        return getRecordsFromEndpoint(ENDPOINT, Account.class, "firstName=" + firstName);
    }

    public List<Account> getAllAccounts() throws IOException {
        return getRecordsFromEndpoint(ENDPOINT, Account.class);
    }
}
