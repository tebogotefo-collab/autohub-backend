package com.mathotech.autopartshub.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponse {
    private String paymentUrl;  // URL to redirect the user to for payment
    private String paymentId;   // Reference ID for the payment
}
