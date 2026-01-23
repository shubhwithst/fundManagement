package com.moneymanager.service;

import com.moneymanager.dto.ExpenseDTO;
import com.moneymanager.dto.IncomeDTO;
import com.moneymanager.entity.CategoryEntity;
import com.moneymanager.entity.ExpenseEntity;
import com.moneymanager.entity.IncomeEntity;
import com.moneymanager.entity.ProfileEntity;
import com.moneymanager.repository.CategoryRepository;
import com.moneymanager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;

    // adding a new income to the database
    public IncomeDTO addIncome(IncomeDTO dto) {
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        IncomeEntity incomeEntity = toEntity(dto, currentProfile, category);
        IncomeEntity savedEntity = incomeRepository.save(incomeEntity);
        return toDTO(savedEntity);
    }

    public List<IncomeDTO> getCurrentMonthIncomesForCurrentUser() {
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<IncomeEntity> list = incomeRepository.findByProfileIdAndDateBetween(currentProfile.getId(), startDate, endDate);
        return list.stream().map(this::toDTO).toList();
    }

    // delete expense by the Current user id ...
    public void deleteIncome(Long incomeId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity income = incomeRepository.findById(incomeId).orElseThrow(() -> new RuntimeException("Income Not Found ..."));
        if (!income.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Unauthorised to delete the income");
        }
        incomeRepository.delete(income);
    }

    // Get Latest top 5 Income for the current user ....
    public List<IncomeDTO> getLatest5IncomeForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> list = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId());
        return list.stream().map(this::toDTO).toList();
    }

    // Get the total Income for the current user
    public BigDecimal getTotalIncomeForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal totalIncomeByProfileId = incomeRepository.findTotalExpanseByProfileId(profile.getId());
        return totalIncomeByProfileId != null ? totalIncomeByProfileId : BigDecimal.ZERO;
    }

    // Filter Income
    public List<IncomeDTO> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        List<IncomeEntity> list = incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(currentProfile.getId(), startDate, endDate, keyword, sort);
        return list.stream().map(this::toDTO).toList();
    }

    // helper methods to DTO and Entity conversion
    private IncomeEntity toEntity(IncomeDTO dto, ProfileEntity profile, CategoryEntity category) {
        return IncomeEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private IncomeDTO toDTO(IncomeEntity entity) {
        return IncomeDTO.builder()
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
