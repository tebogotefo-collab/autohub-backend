package com.mathotech.autopartshub.dto.review;

import com.mathotech.autopartshub.model.ReviewType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private Long id;
    private Long reviewerId;
    private String reviewerName;
    private Long orderItemId;
    private ReviewType type;
    private Long targetId;
    private String targetName;
    private Integer rating;
    private String title;
    private String comment;
    private String imageUrl;
    private boolean verifiedPurchase;
    private String sellerResponse;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
