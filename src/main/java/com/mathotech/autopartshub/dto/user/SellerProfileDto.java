package com.mathotech.autopartshub.dto.user;

import com.mathotech.autopartshub.model.Seller;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerProfileDto {
    private String businessName;
    private String description;
    private Seller.SellerVerificationStatus verificationStatus;
    private BigDecimal averageRating;
    private Integer totalRatings;
}
