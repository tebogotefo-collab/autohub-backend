package com.mathotech.autopartshub.dto.review;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSellerReviewRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    @NotNull(message = "Seller ID is required")
    private Long sellerId;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;
    
    @NotBlank(message = "Comment is required")
    @Size(max = 1000, message = "Comment must be less than 1000 characters")
    private String comment;
}
