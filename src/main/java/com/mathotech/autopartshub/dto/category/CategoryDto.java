package com.mathotech.autopartshub.dto.category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDto {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    
    @Builder.Default
    private List<CategoryDto> subcategories = new ArrayList<>();
}
