package com.mathotech.autopartshub.dto.listing;

import com.mathotech.autopartshub.model.Condition;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListingDto {
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be less than 200 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(max = 5000, message = "Description must be less than 5000 characters")
    private String description;
    
    private Long sellerId;
    private String sellerBusinessName;
    
    @NotNull(message = "Category is required")
    private Long categoryId;
    private String categoryName;
    
    private Long brandId;
    private String brandName;
    
    @NotNull(message = "Condition is required")
    private Condition condition;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    private String oemPartNumber;
    private String aftermarketPartNumber;
    
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
    
    @Size(max = 500, message = "Warranty information must be less than 500 characters")
    private String warrantyInformation;
    
    private BigDecimal averageRating;
    private Integer totalRatings;
    
    private boolean active;
    private boolean featured;
    
    @Builder.Default
    private List<ListingImageDto> images = new ArrayList<>();
    
    @Builder.Default
    private Set<CompatibilityMappingDto> compatibilityMappings = new HashSet<>();
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
