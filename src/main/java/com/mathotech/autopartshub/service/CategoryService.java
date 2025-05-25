package com.mathotech.autopartshub.service;

import com.mathotech.autopartshub.dto.category.CategoryDto;
import com.mathotech.autopartshub.model.Category;
import com.mathotech.autopartshub.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Cacheable(value = "categories")
    public List<CategoryDto> getAllParentCategories() {
        return categoryRepository.findByParentIsNull().stream()
                .map(this::mapToDtoWithSubcategories)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "categories", key = "#id")
    public CategoryDto getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        return mapToDtoWithSubcategories(category);
    }

    @Cacheable(value = "subcategories", key = "#parentId")
    public List<CategoryDto> getSubcategories(Long parentId) {
        return categoryRepository.findByParentId(parentId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = {"categories", "subcategories"}, allEntries = true)
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = new Category();
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        
        if (categoryDto.getParentId() != null) {
            Optional<Category> parentOpt = categoryRepository.findById(categoryDto.getParentId());
            parentOpt.ifPresent(category::setParent);
        }
        
        Category savedCategory = categoryRepository.save(category);
        return mapToDto(savedCategory);
    }

    @Transactional
    @CacheEvict(value = {"categories", "subcategories"}, allEntries = true)
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        
        if (categoryDto.getParentId() != null) {
            Optional<Category> parentOpt = categoryRepository.findById(categoryDto.getParentId());
            parentOpt.ifPresent(category::setParent);
        } else {
            category.setParent(null);
        }
        
        Category updatedCategory = categoryRepository.save(category);
        return mapToDto(updatedCategory);
    }

    @Transactional
    @CacheEvict(value = {"categories", "subcategories"}, allEntries = true)
    public void deleteCategory(Long id) {
        // First, check if the category has subcategories
        List<Category> subcategories = categoryRepository.findByParentId(id);
        if (!subcategories.isEmpty()) {
            throw new RuntimeException("Cannot delete category with subcategories");
        }
        
        categoryRepository.deleteById(id);
    }

    private CategoryDto mapToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
        }
        
        return dto;
    }

    private CategoryDto mapToDtoWithSubcategories(Category category) {
        CategoryDto dto = mapToDto(category);
        
        if (category.getSubcategories() != null && !category.getSubcategories().isEmpty()) {
            List<CategoryDto> subcategoryDtos = category.getSubcategories().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            dto.setSubcategories(subcategoryDtos);
        }
        
        return dto;
    }
}
