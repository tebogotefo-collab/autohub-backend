package com.mathotech.autopartshub.dto.listing;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompatibilityMappingDto {
    private Long id;
    
    @NotBlank(message = "Make is required")
    private String make;
    
    @NotBlank(message = "Model is required")
    private String model;
    
    @NotNull(message = "Year start is required")
    private Integer yearStart;
    
    @NotNull(message = "Year end is required")
    private Integer yearEnd;
    
    private String engine;
    private String trim;
}
