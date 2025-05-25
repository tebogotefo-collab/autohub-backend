package com.mathotech.autopartshub.service;

import com.mathotech.autopartshub.dto.payment.PaymentNotification;
import com.mathotech.autopartshub.dto.payment.PaymentRequest;
import com.mathotech.autopartshub.dto.payment.PaymentResponse;
import com.mathotech.autopartshub.dto.order.UpdateOrderStatusRequest;
import com.mathotech.autopartshub.model.Order;
import com.mathotech.autopartshub.model.OrderStatus;
import com.mathotech.autopartshub.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    @Value("${app.payment.payfast.merchant-id}")
    private String merchantId;

    @Value("${app.payment.payfast.merchant-key}")
    private String merchantKey;

    @Value("${app.payment.payfast.passphrase}")
    private String passphrase;

    @Value("${app.payment.payfast.url}")
    private String payfastUrl;

    @Value("${app.payment.payfast.validate-url}")
    private String payfastValidateUrl;

    @Value("${app.base-url}")
    private String baseUrl;
    
    @Value("${app.payment.payfast.allowed-ips:}")
    private List<String> allowedIps;
    
    private final RestTemplate restTemplate = new RestTemplate();

    public PaymentResponse initiatePayment(PaymentRequest request, Long buyerId) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + request.getOrderId()));

        // Check if order belongs to buyer
        if (!order.getBuyer().getId().equals(buyerId)) {
            throw new RuntimeException("Order does not belong to the current user");
        }

        // Check if order is in the correct state
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new RuntimeException("Order is not in PENDING_PAYMENT status");
        }
        
        // Check if payment was already initiated
        if (order.getPaymentId() != null && !order.getPaymentId().isEmpty()) {
            log.warn("Payment already initiated for order {}", order.getId());
            // If you want to allow re-initiating payment, you could implement that logic here
        }

        // Prepare PayFast payment parameters
        Map<String, String> paymentParams = new HashMap<>();
        paymentParams.put("merchant_id", merchantId);
        paymentParams.put("merchant_key", merchantKey);
        paymentParams.put("return_url", request.getReturnUrl() != null ? request.getReturnUrl() 
                : baseUrl + "/api/payments/return");
        paymentParams.put("cancel_url", request.getCancelUrl() != null ? request.getCancelUrl() 
                : baseUrl + "/api/payments/cancel");
        paymentParams.put("notify_url", request.getNotifyUrl() != null ? request.getNotifyUrl() 
                : baseUrl + "/api/payments/notify");
        
        // Order details
        paymentParams.put("m_payment_id", order.getId().toString());
        paymentParams.put("amount", order.getTotal().toString());
        paymentParams.put("item_name", "Order #" + order.getId());
        
        // Customer details
        paymentParams.put("name_first", order.getBuyer().getFirstName());
        paymentParams.put("name_last", order.getBuyer().getLastName());
        paymentParams.put("email_address", order.getContactEmail());
        
        // Custom fields
        paymentParams.put("custom_str1", order.getId().toString());  // Store our order ID for reference
        
        // Generate signature
        String signature = generateSignature(paymentParams);
        paymentParams.put("signature", signature);
        
        // Build the payment URL
        String queryString = paymentParams.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + UriComponentsBuilder.fromUriString("").queryParam(entry.getKey(), entry.getValue()).build().getQuery().substring(entry.getKey().length() + 1))
                .collect(Collectors.joining("&"));
        
        String paymentUrl = payfastUrl + "?" + queryString;
        
        // Return payment response
        return PaymentResponse.builder()
                .paymentUrl(paymentUrl)
                .paymentId(order.getId().toString())
                .build();
    }

    @Transactional
    public boolean handlePaymentNotification(PaymentNotification notification, HttpServletRequest request) {
        // Log the notification for auditing
        log.info("Received payment notification: {}", notification);
        
        // 1. First, validate IP address
        String clientIp = getClientIp(request);
        if (!validateIpAddress(clientIp)) {
            log.error("Payment notification received from unauthorized IP address: {}", clientIp);
            return false;
        }
        
        // 2. Validate the notification data integrity
        if (!validatePaymentNotification(notification)) {
            log.error("Invalid payment notification received: {}", notification);
            return false;
        }
        
        // 3. Verify the payment with PayFast
        if (!verifyPaymentWithPayFast(notification)) {
            log.error("Failed to verify payment with PayFast: {}", notification.getPaymentId());
            return false;
        }
        
        // 4. Find the order
        Long orderId;
        try {
            orderId = Long.parseLong(notification.getOrderId());
        } catch (NumberFormatException e) {
            log.error("Invalid order ID in payment notification: {}", notification.getOrderId());
            return false;
        }
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        
        // 5. Verify payment amount with a small tolerance for currency conversion issues
        if (Math.abs(notification.getAmountGross().doubleValue() - order.getTotal().doubleValue()) > 0.01) {
            log.error("Payment amount mismatch for order {}: expected {}, received {}", 
                    orderId, order.getTotal(), notification.getAmountGross());
            return false;
        }
        
        // 6. Handle the payment status
        if ("COMPLETE".equals(notification.getPaymentStatus())) {
            // Check if the order is already paid
            if (order.getStatus() == OrderStatus.PAYMENT_COMPLETED ||
                order.getStatus() == OrderStatus.PROCESSING ||
                order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED) {
                log.info("Order {} is already paid, ignoring duplicate notification", orderId);
                return true;
            }
            
            // If order is cancelled, reject the payment
            if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REFUNDED) {
                log.warn("Payment received for cancelled/refunded order {}", orderId);
                // You might want to trigger a refund process here
                return false;
            }
            
            // Update order status to PAYMENT_COMPLETED
            UpdateOrderStatusRequest updateRequest = new UpdateOrderStatusRequest();
            updateRequest.setStatus(OrderStatus.PAYMENT_COMPLETED);
            
            try {
                // We're using system ID 0 to bypass permission checks
                // In a real system, we'd have a proper system user for this
                orderService.updateOrderStatus(updateRequest, orderId, 0L);
                
                // Store the payment ID reference
                order.setPaymentId(notification.getPaymentId());
                orderRepository.save(order);
                
                log.info("Payment completed for order {}", orderId);
                return true;
            } catch (Exception e) {
                log.error("Failed to update order status after payment: {}", e.getMessage(), e);
                return false;
            }
        } else if ("FAILED".equals(notification.getPaymentStatus())) {
            log.warn("Payment failed for order {}", orderId);
            // You might want to notify the buyer about failed payment
            return true; // We return true to acknowledge the notification
        } else {
            log.info("Payment status '{}' received for order {}", notification.getPaymentStatus(), orderId);
            return true; // We return true to acknowledge the notification
        }
    }

    private boolean validatePaymentNotification(PaymentNotification notification) {
        // Check required fields
        if (notification.getPaymentId() == null || notification.getOrderId() == null ||
            notification.getMerchantId() == null || notification.getAmountGross() == null) {
            log.error("Missing required fields in payment notification");
            return false;
        }
        
        // Verify merchant ID
        if (!merchantId.equals(notification.getMerchantId())) {
            log.error("Merchant ID mismatch in payment notification");
            return false;
        }
        
        // Verify signature if provided
        if (notification.getSignature() != null && !notification.getSignature().isEmpty()) {
            Map<String, String> dataForSignature = new TreeMap<>();
            // Add all notification fields except signature
            // This is simplified, in reality you would add all notification fields
            dataForSignature.put("merchant_id", notification.getMerchantId());
            dataForSignature.put("merchant_key", merchantKey);
            dataForSignature.put("payment_id", notification.getPaymentId());
            dataForSignature.put("m_payment_id", notification.getOrderId());
            dataForSignature.put("amount_gross", notification.getAmountGross().toString());
            dataForSignature.put("payment_status", notification.getPaymentStatus());
            
            String expectedSignature = generateSignature(dataForSignature);
            if (!expectedSignature.equals(notification.getSignature())) {
                log.error("Signature validation failed for payment notification");
                return false;
            }
        }
        
        return true;
    }
    
    private boolean validateIpAddress(String ipAddress) {
        // If no allowed IPs are configured, accept all (not recommended for production)
        if (allowedIps == null || allowedIps.isEmpty()) {
            log.warn("No allowed IPs configured for payment notifications");
            return true;
        }
        
        // Check if the IP is in the allowed list
        return allowedIps.contains(ipAddress);
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // If IP contains multiple addresses (X-Forwarded-For can contain a chain), use the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }
    
    private boolean verifyPaymentWithPayFast(PaymentNotification notification) {
        try {
            // Prepare parameters for verification request
            Map<String, String> verifyParams = new HashMap<>();
            verifyParams.put("merchant_id", notification.getMerchantId());
            verifyParams.put("merchant_key", merchantKey);
            verifyParams.put("payment_id", notification.getPaymentId());
            verifyParams.put("m_payment_id", notification.getOrderId());
            verifyParams.put("amount_gross", notification.getAmountGross().toString());
            
            // Build request body
            String requestBody = verifyParams.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            // Create HTTP entity
            HttpEntity<String> request = new HttpEntity<>(requestBody, headers);
            
            // Make verification request to PayFast
            ResponseEntity<String> response = restTemplate.postForEntity(payfastValidateUrl, request, String.class);
            
            // Check response
            if (response.getStatusCode().is2xxSuccessful()) {
                String responseBody = response.getBody();
                if (responseBody != null && responseBody.trim().equalsIgnoreCase("VALID")) {
                    log.info("Payment verification successful for payment ID: {}", notification.getPaymentId());
                    return true;
                } else {
                    log.error("Payment verification failed: {}", responseBody);
                    return false;
                }
            } else {
                log.error("Payment verification request failed with status: {}", response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Error during payment verification: {}", e.getMessage(), e);
            return false;
        }
    }

    private String generateSignature(Map<String, String> data) {
        // Sort the parameters alphabetically
        Map<String, String> sortedData = new TreeMap<>(data);
        
        // Create the parameter string
        String paramString = sortedData.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));
        
        // Add the passphrase if set
        if (passphrase != null && !passphrase.isEmpty()) {
            paramString += "&passphrase=" + passphrase;
        }
        
        try {
            // For PayFast, use MD5 hash
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(paramString.getBytes());
            
            // Convert to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("Failed to generate payment signature: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate payment signature", e);
        }
    }
}
