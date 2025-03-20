package com.infrasight.kodtest;

import com.infrasight.kodtest.api.client.*;
import com.infrasight.kodtest.api.model.Account;
import com.infrasight.kodtest.api.model.Relationship;
import com.infrasight.kodtest.helper.AccountHelper;
import com.infrasight.kodtest.helper.SalaryHelper;
import com.infrasight.kodtest.resolver.GroupAssociationResolver;
import com.infrasight.kodtest.resolver.GroupMemberAccountResolver;
import okhttp3.OkHttpClient;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.infrasight.kodtest.TestVariables.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Simple concrete class for JUnit tests with uses {@link TestsSetup} as a
 * foundation for starting/stopping the API server for tests.
 * <p>
 * You may configure port, api user and api port in {@link TestVariables} if
 * needed.
 * <p>
 * Solution comments:<p/>
 * <ul>
 *   <li> Active group and Accounts are taken into consideration.</li>
 *   <li> {@link ApiClient} is used as the abstraction layer against the API.</li>
 *   <li> {@link AccountApiClient}, {@link RelationshipApiClient} and {@link GroupApiClient} extends {@link ApiClient},
 *        providing access to the API for their corresponding pojos.</li>
 *   <li> For simplicity all API clients are instantiated before each test, as well as the required authorisation.</li>
 *   <li> All API-calls goes via the same method in ApiClient (getElements), which handles pagination. One could consider adding
 *        another method for calls that fetch one object, like getAccountById in AccountApiClient...</li>
 *  </ul>
 */
public class Tests extends TestsSetup {

    public static final String VERAS_EMPLOYEE_ID = "1337";
    public static final String VERAS_FIRST_NAME = "Vera";
    public static final String VERAS_LAST_NAME = "Scope";

    private AccountApiClient accountApiClient;
    private RelationshipApiClient relationshipApiClient;
    private GroupApiClient groupApiClient;

    @Before
    public void setUp() throws IOException {
        OkHttpClient httpClient = getHttpClientBuilder().build();
        AuthenticationApiClient authenticationApiClient = new AuthenticationApiClient(httpClient, API_PORT);
        String accessToken = authenticationApiClient.authenticate(API_USER, API_PASSWORD);
        String apiBaseUrl = String.format("http://localhost:%d/api/", API_PORT);

        ApiClient apiClient = new ApiClient(httpClient, apiBaseUrl, accessToken);
        accountApiClient = new AccountApiClient(apiClient);
        relationshipApiClient = new RelationshipApiClient(apiClient);
        groupApiClient = new GroupApiClient(apiClient);
    }

    /**
     * Simple example test which asserts that the Kodtest API is up and running.
     */
    @Test
    public void connectionTest() {
        assertTrue(serverUp);
    }

    @Test
    public void assignment1() {
        assertTrue(serverUp);

        // getting active accounts by employment ID
        List<Account> activeAccountsViaEmployeeId = accountApiClient.getAccountsByEmployeeId(VERAS_EMPLOYEE_ID)
                .stream()
                .filter(Account::isActive)
                .collect(Collectors.toList());
        assertEquals("One account expected for Vera (via employeeId)", 1, activeAccountsViaEmployeeId.size());
        Account accountForVera = activeAccountsViaEmployeeId.get(0);
        assertEquals("First name correct on account", VERAS_FIRST_NAME, accountForVera.getFirstName());
        assertEquals("Last name correct on account", VERAS_LAST_NAME, accountForVera.getLastName());

        // getting active accounts by first and last name (to check for more accounts)
        List<Account> activeAccountsByFirstAndLastName = accountApiClient.getAccountsByFirstName(VERAS_FIRST_NAME)
                .stream()
                .filter(a -> VERAS_LAST_NAME.equals(a.getLastName()))
                .filter(Account::isActive)
                .collect(Collectors.toList());

        for (Account account : activeAccountsByFirstAndLastName) {
            assertEquals("Account id via first and last name is the same as Veras account (via employeeId)",
                    accountForVera.getId(), account.getId());
        }
    }

    @Test
    public void assignment2() {
        assertTrue(serverUp);

        List<Account> accountsViaEmployeeId = accountApiClient.getAccountsByEmployeeId(VERAS_EMPLOYEE_ID)
                .stream()
                .filter(Account::isActive)
                .collect(Collectors.toList());
        assertEquals("One account expected for Vera (via employeeId)", 1, accountsViaEmployeeId.size());
        Account accountForVera = accountsViaEmployeeId.iterator().next();

        GroupAssociationResolver groupAssociationResolver = new GroupAssociationResolver(relationshipApiClient, groupApiClient);
        Set<String> groupIds = groupAssociationResolver.getDirectGroupIdsForGroupMember(accountForVera.getId());

        assertEquals("Number of direct groups for Vera", 3, groupIds.size());

        List<String> expectedGroupIds = new ArrayList<>();
        expectedGroupIds.add("grp_malmo");
        expectedGroupIds.add("grp_itkonsulter");
        expectedGroupIds.add("grp_köpenhamn");

        for (String groupId : expectedGroupIds) {
            assertTrue("Expected group ID found", groupIds.contains(groupId));
        }
    }

    @Test
    public void assignment3() {
        assertTrue(serverUp);

        List<Account> accountsViaEmployeeId = accountApiClient.getAccountsByEmployeeId(VERAS_EMPLOYEE_ID);
        Account accountForVera = accountsViaEmployeeId.iterator().next();
        GroupAssociationResolver groupAssociationResolver = new GroupAssociationResolver(relationshipApiClient, groupApiClient);
        Set<String> groupIds = groupAssociationResolver.getAllGroupIdsForGroupMember(accountForVera.getId());

        List<String> expectedGroupIds = new ArrayList<>();
        expectedGroupIds.add("grp_inhyrda");
        expectedGroupIds.add("grp_malmo");
        expectedGroupIds.add("grp_choklad");
        expectedGroupIds.add("grp_itkonsulter");
        expectedGroupIds.add("grp_sverige");
        expectedGroupIds.add("grp_danmark");
        expectedGroupIds.add("grp_konfektyr");
        expectedGroupIds.add("grp_köpenhamn");
        expectedGroupIds.add("grp_chokladfabrik");

        assertEquals("Expected number of groups match", expectedGroupIds.size(), groupIds.size());

        for (String groupId : expectedGroupIds) {
            assertTrue("Expected group ID found", groupIds.contains(groupId));
        }
    }

    @Test
    public void assignment4() {
        assertTrue(serverUp);

        GroupMemberAccountResolver groupMemberAccountResolver = new GroupMemberAccountResolver(relationshipApiClient, groupApiClient);
        Set<String> accountIdsForInterimStaff = groupMemberAccountResolver.getAccountIdsForGroup("grp_inhyrda");
        List<Account> accountsForInterimStaff = accountApiClient.getActiveAccountsByIds(accountIdsForInterimStaff);
        double totalInterimStaffSalary = SalaryHelper.calculateTotalSalaryInSEK(accountsForInterimStaff);

        double expectedTotalSalary = 24650836.8;
        assertEquals("Total interim staff salary match", expectedTotalSalary, totalInterimStaffSalary, 1.0);
    }

    @Test
    public void assignment5() {
        assertTrue(serverUp);

        GroupMemberAccountResolver groupMemberAccountResolver = new GroupMemberAccountResolver(relationshipApiClient, groupApiClient);
        Set<String> accountIdsForSalesStaff = groupMemberAccountResolver.getAccountIdsForGroup("grp_saljare");
        Set<String> accountIdsForSwedishEmployees = groupMemberAccountResolver.getAccountIdsForGroup("grp_sverige");

        // collecting account IDs for Swedish sales staff
        Set<String> accountIdsForSwedishSalesStaff = accountIdsForSalesStaff.stream()
                .filter(accountIdsForSwedishEmployees::contains)
                .collect(Collectors.toSet());

        // collecting active accounts
        List<Account> accountsForSwedishSalesStaff = accountApiClient.getActiveAccountsByIds(accountIdsForSwedishSalesStaff);

        // filtering on employment date
        List<Account> resultingAccounts = AccountHelper.filterAccountsByEmploymentDate(
                accountsForSwedishSalesStaff,
                LocalDate.of(2019, 1, 1),
                LocalDate.of(2022, 12, 31));

        // collecting managers accounts
        Map<String, Integer> managerIdToAccountCounts = new HashMap<>();
        for (Account account : resultingAccounts) {
            Relationship managerRelationship = relationshipApiClient.getRelationshipsByManagedId(account.getId());
            String managerId = managerRelationship.getAccountId();
            managerIdToAccountCounts.put(managerId, managerIdToAccountCounts.getOrDefault(managerId, 0) + 1);
        }

        Map<String, Integer> expectedManagerIdToAccounts = new HashMap<>();
        expectedManagerIdToAccounts.put("acc43", 8);
        expectedManagerIdToAccounts.put("acc62", 7);
        expectedManagerIdToAccounts.put("acc808", 2);
        expectedManagerIdToAccounts.put("acc818", 1);
        expectedManagerIdToAccounts.put("acc706", 3);
        expectedManagerIdToAccounts.put("acc4", 5);
        expectedManagerIdToAccounts.put("acc802", 1);
        expectedManagerIdToAccounts.put("acc710", 1);

        assertEquals("Number of managers", expectedManagerIdToAccounts.size(), managerIdToAccountCounts.size());
        for (Map.Entry<String, Integer> entry : expectedManagerIdToAccounts.entrySet()) {
            assertEquals("Number of sales staff for manager with ID " + entry.getKey(), entry.getValue(), managerIdToAccountCounts.get(entry.getKey()));
        }

        Map<String, Account> managerAccounts = new HashMap<>();
        for (String managerId : managerIdToAccountCounts.keySet()) {
            managerAccounts.put(managerId, accountApiClient.getAccountById(managerId));
        }

        System.out.println("Managers sorted by number of sales staff:");
        managerIdToAccountCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) // Sort by value (descending)
                .forEach(entry ->
                        System.out.println(managerAccounts.get(entry.getKey()).getFullName() + ": " + entry.getValue())
                );
    }
}
