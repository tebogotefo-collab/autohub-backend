package com.mathotech.autopartshub.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;  // The ordered item being reviewed (null for seller reviews)

    @ManyToOne
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;  // The user who wrote the review

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewType type;  // PRODUCT or SELLER

    @Column(name = "target_id", nullable = false)
    private Long targetId;  // ID of the listing or seller being reviewed

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer rating;  // 1-5 star rating

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String comment;

    @Size(max = 255)
    @Column(name = "image_url")
    private String imageUrl;  // Optional image attachment

    @Column(name = "verified_purchase", nullable = false)
    private boolean verifiedPurchase;  // Whether the reviewer actually purchased the item

    @Column(name = "seller_response", length = 1000)
    private String sellerResponse;  // Optional seller response to the review

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
