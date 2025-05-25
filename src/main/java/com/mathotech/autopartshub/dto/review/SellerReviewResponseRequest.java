package com.mathotech.autopartshub.dto.review;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerReviewResponseRequest {
    
    @NotNull(message = "Review ID is required")
    private Long reviewId;
    
    @NotBlank(message = "Response is required")
    @Size(max = 1000, message = "Response must be less than 1000 characters")
    private String response;
}
