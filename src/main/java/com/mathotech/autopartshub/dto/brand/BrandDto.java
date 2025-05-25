package com.mathotech.autopartshub.dto.brand;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BrandDto {
    private Long id;
    
    @NotBlank(message = "Brand name is required")
    @Size(max = 100, message = "Brand name must be less than 100 characters")
    private String name;
    
    @Size(max = 500, message = "Description must be less than 500 characters")
    private String description;
    
    private String logoUrl;
}
