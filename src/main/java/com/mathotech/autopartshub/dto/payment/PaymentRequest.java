package com.mathotech.autopartshub.dto.payment;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    
    @NotNull(message = "Order ID is required")
    private Long orderId;
    
    private String returnUrl;  // URL to redirect after successful payment
    private String cancelUrl;  // URL to redirect after cancelled payment
    private String notifyUrl;  // URL for payment gateway to send notification (webhook)
}
