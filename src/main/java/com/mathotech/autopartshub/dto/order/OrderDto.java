package com.mathotech.autopartshub.dto.order;

import com.mathotech.autopartshub.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private Long id;
    private Long buyerId;
    private String buyerName; // Combination of buyer's first name and last name
    private String buyerEmail;
    private OrderStatus status;
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private String shippingAddress;
    private String shippingCity;
    private String shippingPostalCode;
    private String shippingProvince;
    private String contactPhone;
    private String contactEmail;
    private String trackingNumber;
    private String paymentId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paymentDate;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private LocalDateTime cancelledDate;
    
    @Builder.Default
    private List<OrderItemDto> items = new ArrayList<>();
}
