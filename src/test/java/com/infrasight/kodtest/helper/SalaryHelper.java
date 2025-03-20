package com.infrasight.kodtest.helper;

import com.infrasight.kodtest.api.model.Account;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalaryHelper {

    private SalaryHelper() {
    }

    public static double calculateTotalSalaryInSEK(List<Account> accounts) {
        Map<String, Double> currencyConversion = getCurrencyConversion();
        double totalSalary = 0;
        for (Account account : accounts) {
            int salary = account.getSalary();
            totalSalary += roundUpToTwoDecimals(salary * currencyConversion.get(account.getSalaryCurrency()));
        }
        return totalSalary;
    }

    private static Map<String, Double> getCurrencyConversion() {
        Map<String, Double> currencyConversion = new HashMap<>();
        currencyConversion.put("EUR", 11.0);
        currencyConversion.put("DKK", 1.48);
        currencyConversion.put("SEK", 1.0);
        return currencyConversion;
    }

    private static double roundUpToTwoDecimals(double number) {
        return BigDecimal.valueOf(number)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
