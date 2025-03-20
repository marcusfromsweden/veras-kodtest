package com.infrasight.kodtest.helper;

import com.infrasight.kodtest.api.model.Account;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for operations related to Account objects.
 */
public class AccountHelper {

    private static final ZoneId UTC_ZONE = ZoneId.of("UTC");

    private AccountHelper() {
        // Prevent instantiation
    }

    /**
     * Filters accounts based on their employment date.
     *
     * @param accounts            The list of accounts to filter.
     * @param employmentStartDate The start date for filtering.
     * @param employmentEndDate   The end date for filtering.
     * @return A list of accounts whose employment date falls within the specified range.
     */
    public static List<Account> filterAccountsByEmploymentDate(List<Account> accounts,
                                                               LocalDate employmentStartDate,
                                                               LocalDate employmentEndDate) {
        return accounts.stream()
                .filter(account -> {
                    LocalDate employmentDate = Instant.ofEpochSecond(account.getEmployedSince())
                            .atZone(UTC_ZONE)
                            .toLocalDate();

                    return (employmentDate.isEqual(employmentStartDate) || employmentDate.isAfter(employmentStartDate)) &&
                            (employmentDate.isEqual(employmentEndDate) || employmentDate.isBefore(employmentEndDate));
                })
                .collect(Collectors.toList());
    }
}
