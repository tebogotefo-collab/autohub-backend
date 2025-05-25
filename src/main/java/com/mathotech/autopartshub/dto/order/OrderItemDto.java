package com.mathotech.autopartshub.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDto {
    private Long id;
    private Long listingId;
    private Long sellerId;
    private String sellerBusinessName;
    private String listingTitle;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
}
