package com.mathotech.autopartshub.controller;

import com.mathotech.autopartshub.dto.payment.PaymentNotification;
import com.mathotech.autopartshub.dto.payment.PaymentRequest;
import com.mathotech.autopartshub.dto.payment.PaymentResponse;
import com.mathotech.autopartshub.model.User;
import com.mathotech.autopartshub.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    @PreAuthorize("hasRole('ROLE_BUYER')")
    public ResponseEntity<PaymentResponse> initiatePayment(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(paymentService.initiatePayment(request, user.getId()));
    }

    @PostMapping("/notify")
    public ResponseEntity<String> handlePaymentNotification(
            @RequestBody PaymentNotification notification,
            jakarta.servlet.http.HttpServletRequest request) {
        log.info("Received payment notification from IP: {}", getClientIp(request));
        
        boolean success = paymentService.handlePaymentNotification(notification, request);
        
        if (success) {
            return ResponseEntity.ok("Payment notification processed successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to process payment notification");
        }
    }
    
    private String getClientIp(jakarta.servlet.http.HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // If multiple IPs, take the first one (client)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @GetMapping("/return")
    public ResponseEntity<String> handlePaymentReturn(
            @RequestParam("m_payment_id") String orderId,
            @RequestParam(value = "pf_payment_id", required = false) String paymentId) {
        log.info("User returned from payment gateway. Order ID: {}, Payment ID: {}", orderId, paymentId);
        
        // This endpoint is just for user redirection after payment
        // The actual payment confirmation is handled by the notify endpoint
        
        return ResponseEntity.ok("Payment completed. You will be redirected to your order details.");
    }

    @GetMapping("/cancel")
    public ResponseEntity<String> handlePaymentCancel(
            @RequestParam("m_payment_id") String orderId) {
        log.info("User cancelled payment for order ID: {}", orderId);
        
        // This endpoint is just for user redirection after cancelling payment
        
        return ResponseEntity.ok("Payment cancelled. You will be redirected to your cart.");
    }
}
