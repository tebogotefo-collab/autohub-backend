package com.mathotech.autopartshub.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "listings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Size(max = 5000)
    @Column(nullable = false, length = 5000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Condition condition;

    @NotNull
    @DecimalMin(value = "0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;  // In ZAR

    @Column(name = "oem_part_number")
    private String oemPartNumber;

    @Column(name = "aftermarket_part_number")
    private String aftermarketPartNumber;

    @NotNull
    @Column(nullable = false)
    private Integer quantity;

    @Size(max = 500)
    @Column(name = "warranty_information")
    private String warrantyInformation;

    @Column(precision = 3, scale = 2)
    private BigDecimal averageRating;

    @Column
    private Integer totalRatings;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(nullable = false, name = "is_featured")
    private boolean featured = false;

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListingImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CompatibilityMapping> compatibilityMappings = new HashSet<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
