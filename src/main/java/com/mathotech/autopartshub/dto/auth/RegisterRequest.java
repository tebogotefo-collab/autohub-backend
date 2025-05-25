package com.mathotech.autopartshub.dto.auth;

import com.mathotech.autopartshub.model.Role;
import jakarta.validation.constraints.Email;
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
public class RegisterRequest {
    
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name must be less than 50 characters")
    private String firstName;
    
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name must be less than 50 characters")
    private String lastName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @Size(max = 20, message = "Phone number must be less than 20 characters")
    private String phoneNumber;
    
    @Builder.Default
    private Role role = Role.ROLE_BUYER; // Default to BUYER if not specified
    
    // Only used if role is SELLER
    private SellerInfo sellerInfo;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SellerInfo {
        @NotBlank(message = "Business name is required for sellers")
        @Size(max = 100, message = "Business name must be less than 100 characters")
        private String businessName;
        
        @Size(max = 500, message = "Description must be less than 500 characters")
        private String description;
    }
}
