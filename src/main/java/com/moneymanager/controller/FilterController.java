package com.moneymanager.controller;

import com.moneymanager.dto.ExpenseDTO;
import com.moneymanager.dto.FilterDTO;
import com.moneymanager.dto.IncomeDTO;
import com.moneymanager.service.ExpenseService;
import com.moneymanager.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    @PostMapping
    public ResponseEntity<?> filterTransactions(@RequestBody FilterDTO filterDTO) {
        LocalDate startDate = filterDTO.getStartDate() != null ? filterDTO.getStartDate() : LocalDate.MIN;
        LocalDate endDate = filterDTO.getEndDate() != null ? filterDTO.getEndDate() : LocalDate.now();
        String keyword = filterDTO.getKeyword() != null ? filterDTO.getKeyword() : "";
        String sortField = filterDTO.getSortField() != null ? filterDTO.getSortField() : "date";
        Sort.Direction direction = "desc".equalsIgnoreCase(filterDTO.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortField);
        if ("income".equalsIgnoreCase(filterDTO.getType())) {
            List<IncomeDTO> income = incomeService.filterIncomes(startDate, endDate, keyword, sort);
            return ResponseEntity.ok(income);
        } else if ("expense".equalsIgnoreCase(filterDTO.getType())) {
            List<ExpenseDTO> expanses = expenseService.filterExpanses(startDate, endDate, keyword, sort);
            return ResponseEntity.ok(expanses);
        } else {
            return ResponseEntity.badRequest().body("Invalid type must be 'income' of 'expense' ");
        }
    }
}
