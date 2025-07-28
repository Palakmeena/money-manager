package com.example.moneymanager.service;

import com.example.moneymanager.dto.CategoryDTO;
import com.example.moneymanager.entity.CategoryEntity;
import com.example.moneymanager.entity.ProfileEntity;
import com.example.moneymanager.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;

    public CategoryDTO saveCategory(CategoryDTO categoryDTO){
        ProfileEntity  profile=  profileService.getCurrentProfile();
        if(categoryRepository.existsByNameAndProfileId(categoryDTO.getName(),profile.getId())) {
            throw new RuntimeException("Category with this name already exists");
        }
        CategoryEntity newCategory=toEntity(categoryDTO,profile);
        newCategory=categoryRepository.save(newCategory);
        return  toDTO(newCategory);

    }

    public List<CategoryDTO> getCategoriesForCurrentUser(){
        ProfileEntity profile=profileService.getCurrentProfile();
        List<CategoryEntity>categories=categoryRepository.findByProfileId(profile.getId());
        return categories.stream().map(this::toDTO).toList();
    }


    public  List<CategoryDTO> getCategoriesTypeForCurrentUser(String type){
      ProfileEntity profile=  profileService.getCurrentProfile();
    List<CategoryEntity>   entities= categoryRepository.findByTypeAndProfileId(type,profile.getId());
    return entities.stream().map(this::toDTO).toList();

    }

    public CategoryDTO updateCategory(Long categoryId,CategoryDTO dto){
        ProfileEntity profile=profileService.getCurrentProfile();
        CategoryEntity existingCategory=   categoryRepository.findByIdAndProfileId(categoryId,profile.getId())
                .orElseThrow(()->new RuntimeException("Category not found or not accessible"));
        existingCategory.setName(dto.getName());
        existingCategory.setIcon(dto.getIcon());
        existingCategory=  categoryRepository.save(existingCategory);
        return toDTO(existingCategory);

    }

    private CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profile){
        return CategoryEntity.builder()
                .name(categoryDTO.getName())
                .icon(categoryDTO.getIcon())
                .profile(profile)
                .type(categoryDTO.getType())
                .build();
    }

    private CategoryDTO toDTO(CategoryEntity entity){
        return CategoryDTO.builder()
                .id(entity.getId())
                .profileId(entity.getProfile()!=null ? entity.getProfile().getId():null)
                .name(entity.getName())
                .icon(entity.getIcon())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .type(entity.getType())
                .build();
    }
}
