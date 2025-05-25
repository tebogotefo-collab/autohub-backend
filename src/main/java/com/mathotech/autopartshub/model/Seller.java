package com.mathotech.autopartshub.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sellers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Seller {

    @Id
    @Column(name = "user_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String businessName;

    @Size(max = 500)
    private String description;

    @Column(name = "bank_account_token")
    private String bankAccountToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SellerVerificationStatus verificationStatus;

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column
    private Integer totalRatings;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum SellerVerificationStatus {
        UNVERIFIED,
        PENDING,
        VERIFIED,
        REJECTED
    }
}
