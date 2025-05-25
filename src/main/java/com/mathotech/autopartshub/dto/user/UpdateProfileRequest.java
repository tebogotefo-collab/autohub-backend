package com.mathotech.autopartshub.dto.user;

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
public class UpdateProfileRequest {
    
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName;
    
    @Size(max = 20, message = "Phone number must be less than 20 characters")
    private String phoneNumber;
    
    // Seller-specific fields (only used if user is a seller)
    private UpdateSellerRequest sellerDetails;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UpdateSellerRequest {
        @NotBlank(message = "Business name is required for sellers")
        @Size(max = 100, message = "Business name must be less than 100 characters")
        private String businessName;
        
        @Size(max = 500, message = "Description must be less than 500 characters")
        private String description;
    }
}
