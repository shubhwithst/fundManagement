package com.moneymanager.service;

import com.moneymanager.dto.ExpenseDTO;
import com.moneymanager.dto.IncomeDTO;
import com.moneymanager.dto.RecentTransactionDTO;
import com.moneymanager.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Stream.concat;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProfileService profileService;
    private final IncomeService incomeService;
    private final ExpenseService expenseService;

    public Map<String, Object> getDashboardData() {
        ProfileEntity profile = profileService.getCurrentProfile();
        Map<String, Object> returnValue = new LinkedHashMap<>();
        List<IncomeDTO> latestIncome = incomeService.getLatest5IncomeForCurrentUser();
        List<ExpenseDTO> latestExpanse = expenseService.getLatest5ExpenseForCurrentUser();
        List<RecentTransactionDTO> recentTransaction = concat(latestIncome.stream().map(income -> RecentTransactionDTO.builder()
                        .id(income.getId())
                        .profileId(profile.getId())
                        .name(income.getName())
                        .icon(income.getIcon())
                        .amount(income.getAmount())
                        .createdAt(income.getCreatedAt())
                        .updateAt(income.getUpdatedAt())
                        .date(income.getDate())
                        .type("income")
                        .build()),
                latestExpanse.stream().map(expanse -> RecentTransactionDTO.builder()
                        .id(expanse.getId())
                        .name(expanse.getName())
                        .profileId(profile.getId())
                        .icon(expanse.getIcon())
                        .amount(expanse.getAmount())
                        .updateAt(expanse.getUpdatedAt())
                        .type("expense")
                        .createdAt(expanse.getCreatedAt())
                        .date(expanse.getDate())
                        .build()))
                .sorted((a, b) -> {
                    int cmp = b.getDate().compareTo(a.getDate());
                    if (cmp == 0 && a.getCreatedAt() != null && b.getCreatedAt() != null) {
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    }
                    return cmp;
                }).collect(Collectors.toList());

        returnValue.put("total_Balance", incomeService.getTotalIncomeForCurrentUser().subtract(expenseService.getTotalExpenseForCurrentUser()));
        returnValue.put("total_Income", incomeService.getTotalIncomeForCurrentUser());
        returnValue.put("total_Expense", expenseService.getTotalExpenseForCurrentUser());
        returnValue.put("recent_5Expenses", latestExpanse);
        returnValue.put("recent_5Incomes", latestIncome);
        returnValue.put("recent_Transactions", recentTransaction);
        return returnValue;
    }

}
