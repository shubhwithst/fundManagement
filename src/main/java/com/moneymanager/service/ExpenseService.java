package com.moneymanager.service;

import com.moneymanager.dto.ExpenseDTO;
import com.moneymanager.entity.CategoryEntity;
import com.moneymanager.entity.ExpenseEntity;
import com.moneymanager.entity.ProfileEntity;
import com.moneymanager.repository.CategoryRepository;
import com.moneymanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expanseRepository;
    private final ProfileService profileService;

    // adding a new expense to the database
    public ExpenseDTO addExpense(ExpenseDTO dto) {
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        ExpenseEntity expenseEntity = toEntity(dto, currentProfile, category);
        ExpenseEntity savedEntity = expanseRepository.save(expenseEntity);
        return toDTO(savedEntity);
    }

    public List<ExpenseDTO> getCurrentMonthExpensesForCurrentUser() {
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<ExpenseEntity> list = expanseRepository.findByProfileIdAndDateBetween(currentProfile.getId(), startDate, endDate);
        return list.stream().map(this::toDTO).toList();
    }

    // delete expense by the Current user id ...
    public void deleteExpense(Long expenseId){
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity expense = expanseRepository.findById(expenseId).orElseThrow(() -> new RuntimeException("Expense Not Found ..."));
        if(!expense.getProfile().getId().equals(profile.getId())){
            throw  new RuntimeException("Unauthorised to delete the expense");
        }
        expanseRepository.delete(expense);
    }

    // Get Latest top 5 Expense for the current user ....
    public List<ExpenseDTO> getLatest5ExpenseForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list = expanseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return list.stream().map(this::toDTO).toList();
    }

    // Get the total Expenses for the current user
    public BigDecimal getTotalExpenseForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal totalExpanseByProfileId = expanseRepository.findTotalExpanseByProfileId(profile.getId());
        return totalExpanseByProfileId != null ? totalExpanseByProfileId : BigDecimal.ZERO;
    }

    // Filter expense //
    public List<ExpenseDTO> filterExpanses(LocalDate startDate, LocalDate endDate , String keyword, Sort sort){
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        List<ExpenseEntity> list = expanseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(currentProfile.getId(), startDate, endDate, keyword, sort);
        return list.stream().map(this::toDTO).toList();
    }

    // Notification ...
    public List<ExpenseDTO> getExpenseForUserOnDate(Long profileId, LocalDate date){
        List<ExpenseEntity> list = expanseRepository.findByProfileIdAndDate(profileId, date);
        return list.stream().map(this::toDTO).toList();
    }

    // helper methods to DTO and Entity conversion
    private ExpenseEntity toEntity(ExpenseDTO dto, ProfileEntity profile, CategoryEntity category) {
        return ExpenseEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private ExpenseDTO toDTO(ExpenseEntity entity) {
        return ExpenseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .amount(entity.getAmount())
                .date(entity.getDate())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : "N/A")
                .date(entity.getDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
