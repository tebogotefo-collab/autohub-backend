package com.mathotech.autopartshub.dto.user;

import com.mathotech.autopartshub.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Role role;
    private boolean verified;
    private LocalDateTime createdAt;
    
    // Additional seller-specific information (populated only if user is a seller)
    private SellerProfileDto sellerProfile;
}
