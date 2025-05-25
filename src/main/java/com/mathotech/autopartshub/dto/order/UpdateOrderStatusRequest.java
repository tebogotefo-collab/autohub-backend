package com.mathotech.autopartshub.dto.order;

import com.mathotech.autopartshub.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateOrderStatusRequest {
    
    @NotNull(message = "Order status is required")
    private OrderStatus status;
    
    private String trackingNumber;  // Required only for SHIPPED status
}
