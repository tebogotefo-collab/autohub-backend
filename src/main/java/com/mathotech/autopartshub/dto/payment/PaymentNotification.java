package com.mathotech.autopartshub.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentNotification {
    // PayFast ITN (Instant Transaction Notification) fields
    private String merchantId;
    private String merchantKey;
    private String paymentId;      // m_payment_id
    private BigDecimal amountGross;
    private String paymentStatus;  // COMPLETE, PENDING, FAILED, etc.
    private String signature;      // MD5 hash for verification
    
    // Custom fields we'll use
    private String orderId;        // Custom field for our order ID
    private String token;          // PayFast payment token
}
