package com.moneymanager.service;

import com.moneymanager.dto.CategoryDTO;
import com.moneymanager.entity.CategoryEntity;
import com.moneymanager.entity.ProfileEntity;
import com.moneymanager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;

    // save category
    public CategoryDTO saveCategory(CategoryDTO categoryDTO) {
        ProfileEntity profileEntity = profileService.getCurrentProfile();
        if (categoryRepository.existsByNameAndProfileId(categoryDTO.getName(), profileEntity.getId())) {
            throw new RuntimeException("Category with the same name already exists");
        }
        CategoryEntity newCategory = toEntity(categoryDTO, profileEntity);
        newCategory = categoryRepository.save(newCategory);
        return toDto(newCategory);
    }

    // get category by for current user
    public List<CategoryDTO> getCategoryForCurrentUser() {
        ProfileEntity profileEntity = profileService.getCurrentProfile();
        List<CategoryEntity> categoryEntities = categoryRepository.findByProfileId(profileEntity.getId());
        return categoryEntities.stream().map(this::toDto).toList();
    }

    // get category by type for current user
    public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String type) {
        ProfileEntity currentProfile = profileService.getCurrentProfile();
        List<CategoryEntity> entity = categoryRepository.findByTypeAndProfileId(type, currentProfile.getId());
        return entity.stream().map(this::toDto).toList();

    }

    //update category
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        ProfileEntity profileEntity = profileService.getCurrentProfile();
        CategoryEntity existingCategory = categoryRepository.findByIdAndProfileId(categoryId, profileEntity.getId())
                .orElseThrow(() -> new RuntimeException("Category not found or not accessible"));

        existingCategory.setName(categoryDTO.getName());
        existingCategory.setIcon(categoryDTO.getIcon());
        // existingCategory.setType(categoryDTO.getType());
        CategoryEntity updatedCategory = categoryRepository.save(existingCategory);
        return toDto(updatedCategory);
    }

    // helper methods
    private CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profileEntity) {
        return CategoryEntity.builder()
                .name(categoryDTO.getName())
                .type(categoryDTO.getType())
                .icon(categoryDTO.getIcon())
                .profile(profileEntity)
                .build();
    }

    private CategoryDTO toDto(CategoryEntity categoryEntity) {
        return CategoryDTO.builder()
                .id(categoryEntity.getId())
                .name(categoryEntity.getName())
                .type(categoryEntity.getType())
                .icon(categoryEntity.getIcon())
                .CreatedAt(categoryEntity.getCreatedAt())
                .UpdatedAt(categoryEntity.getUpdatedAt())
                .profileId(categoryEntity.getProfile() != null ? categoryEntity.getProfile().getId() : null)
                .build();
    }

}
