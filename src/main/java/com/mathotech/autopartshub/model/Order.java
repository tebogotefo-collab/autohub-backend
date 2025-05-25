package com.mathotech.autopartshub.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;
    
    @NotBlank
    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;  // Sum of order item prices * quantities (in ZAR)

    @Column(name = "shipping_cost", precision = 10, scale = 2, nullable = false)
    private BigDecimal shippingFee;  // Shipping cost (in ZAR) - name kept for backward compatibility

    @Column(name = "tax_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal taxAmount;  // VAT amount (in ZAR)

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal total;  // Total order amount with tax and shipping (in ZAR) - name kept for backward compatibility

    @Column(name = "shipping_address")
    private String shippingAddress;

    @Column(name = "shipping_city")
    private String shippingCity;

    @Column(name = "shipping_postal_code")
    private String shippingPostalCode;

    @Column(name = "shipping_state")
    private String shippingState;
    
    @Column(name = "shipping_country", nullable = false)
    @Builder.Default
    private String shippingCountry = "South Africa";
    
    @Column(name = "shipping_method", nullable = false)
    @Builder.Default
    private String shippingMethod = "Standard";

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "contact_email")
    private String contactEmail;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "payment_method", nullable = false)
    @Builder.Default
    private String paymentMethod = "PayFast";
    
    @Column(name = "payment_transaction_id")
    private String paymentId;  // Reference to external payment system ID - name kept for backward compatibility
    
    @Column(name = "notes")
    private String notes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @Column(name = "shipped_date")
    private LocalDateTime shippedDate;

    @Column(name = "delivered_date")
    private LocalDateTime deliveredDate;

    @Column(name = "cancelled_date")
    private LocalDateTime cancelledDate;
}
