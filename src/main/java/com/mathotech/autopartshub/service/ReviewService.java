package com.mathotech.autopartshub.service;

import com.mathotech.autopartshub.dto.review.CreateProductReviewRequest;
import com.mathotech.autopartshub.dto.review.CreateSellerReviewRequest;
import com.mathotech.autopartshub.dto.review.ReviewDto;
import com.mathotech.autopartshub.dto.review.SellerReviewResponseRequest;
import com.mathotech.autopartshub.model.*;
import com.mathotech.autopartshub.repository.ListingRepository;
import com.mathotech.autopartshub.repository.OrderItemRepository;
import com.mathotech.autopartshub.repository.OrderRepository;
import com.mathotech.autopartshub.repository.ReviewRepository;
import com.mathotech.autopartshub.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    
    @Transactional
    public ReviewDto createProductReview(CreateProductReviewRequest request, Long buyerId) {
        // Verify that the order item exists and belongs to the buyer
        OrderItem orderItem = orderItemRepository.findById(request.getOrderItemId())
                .orElseThrow(() -> new EntityNotFoundException("Order item not found"));
        
        if (!orderItem.getOrder().getBuyer().getId().equals(buyerId)) {
            throw new AccessDeniedException("You can only review items you purchased");
        }
        
        // Verify that the order is DELIVERED
        if (orderItem.getOrder().getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("You can only review items from delivered orders");
        }
        
        // Check if a review already exists for this order item
        if (reviewRepository.existsByOrderItemIdAndReviewerId(orderItem.getId(), buyerId)) {
            throw new IllegalStateException("You have already reviewed this item");
        }
        
        User reviewer = userRepository.findById(buyerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        Listing listing = orderItem.getListing();
        
        Review review = Review.builder()
                .orderItem(orderItem)
                .reviewer(reviewer)
                .type(ReviewType.PRODUCT)
                .targetId(listing.getId())
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .imageUrl(request.getImageUrl())
                .verifiedPurchase(true) // Since we verified the purchase
                .build();
        
        Review savedReview = reviewRepository.save(review);
        
        return mapToDto(savedReview, listing.getTitle());
    }
    
    @Transactional
    public ReviewDto createSellerReview(CreateSellerReviewRequest request, Long buyerId) {
        // Verify that the order exists and belongs to the buyer
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        
        if (!order.getBuyer().getId().equals(buyerId)) {
            throw new AccessDeniedException("You can only review sellers you purchased from");
        }
        
        // Verify that the order is DELIVERED
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("You can only review sellers after order delivery");
        }
        
        // Verify that the seller is associated with this order
        boolean sellerFound = order.getItems().stream()
                .anyMatch(item -> item.getListing().getSeller().getId().equals(request.getSellerId()));
        
        if (!sellerFound) {
            throw new IllegalStateException("The specified seller is not associated with this order");
        }
        
        User reviewer = userRepository.findById(buyerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        User seller = userRepository.findById(request.getSellerId())
                .orElseThrow(() -> new EntityNotFoundException("Seller not found"));
        
        Review review = Review.builder()
                .reviewer(reviewer)
                .type(ReviewType.SELLER)
                .targetId(request.getSellerId())
                .rating(request.getRating())
                .title(request.getTitle())
                .comment(request.getComment())
                .verifiedPurchase(true) // Since we verified the purchase
                .build();
        
        Review savedReview = reviewRepository.save(review);
        
        String sellerName = seller.getSeller() != null ? 
                seller.getSeller().getBusinessName() : seller.getFirstName() + " " + seller.getLastName();
        
        return mapToDto(savedReview, sellerName);
    }
    
    @Transactional
    public ReviewDto addSellerResponse(SellerReviewResponseRequest request, Long sellerId) {
        Review review = reviewRepository.findById(request.getReviewId())
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));
        
        // Check if the review is for this seller or for one of their listings
        boolean isSellerReview = review.getType() == ReviewType.SELLER && review.getTargetId().equals(sellerId);
        boolean isProductReview = review.getType() == ReviewType.PRODUCT && 
                listingRepository.findById(review.getTargetId())
                        .map(listing -> listing.getSeller().getId().equals(sellerId))
                        .orElse(false);
        
        if (!isSellerReview && !isProductReview) {
            throw new AccessDeniedException("You can only respond to reviews for your products or yourself");
        }
        
        review.setSellerResponse(request.getResponse());
        Review updatedReview = reviewRepository.save(review);
        
        String targetName = "";
        if (review.getType() == ReviewType.PRODUCT) {
            targetName = listingRepository.findById(review.getTargetId())
                    .map(Listing::getTitle)
                    .orElse("Unknown Product");
        } else {
            targetName = userRepository.findById(review.getTargetId())
                    .map(user -> {
                        if (user.getSeller() != null) {
                            return user.getSeller().getBusinessName();
                        }
                        return user.getFirstName() + " " + user.getLastName();
                    })
                    .orElse("Unknown Seller");
        }
        
        return mapToDto(updatedReview, targetName);
    }
    
    public Page<ReviewDto> getProductReviews(Long listingId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByTypeAndTargetId(ReviewType.PRODUCT, listingId, pageable);
        
        String productName = listingRepository.findById(listingId)
                .map(Listing::getTitle)
                .orElse("Unknown Product");
        
        return reviews.map(review -> mapToDto(review, productName));
    }
    
    public Page<ReviewDto> getSellerReviews(Long sellerId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByTypeAndTargetId(ReviewType.SELLER, sellerId, pageable);
        
        String sellerName = userRepository.findById(sellerId)
                .map(user -> {
                    if (user.getSeller() != null) {
                        return user.getSeller().getBusinessName();
                    }
                    return user.getFirstName() + " " + user.getLastName();
                })
                .orElse("Unknown Seller");
        
        return reviews.map(review -> mapToDto(review, sellerName));
    }
    
    public Page<ReviewDto> getUserReviews(Long userId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByReviewerId(userId, pageable);
        
        return reviews.map(review -> {
            String targetName = "";
            if (review.getType() == ReviewType.PRODUCT) {
                targetName = listingRepository.findById(review.getTargetId())
                        .map(Listing::getTitle)
                        .orElse("Unknown Product");
            } else {
                targetName = userRepository.findById(review.getTargetId())
                        .map(user -> {
                            if (user.getSeller() != null) {
                                return user.getSeller().getBusinessName();
                            }
                            return user.getFirstName() + " " + user.getLastName();
                        })
                        .orElse("Unknown Seller");
            }
            
            return mapToDto(review, targetName);
        });
    }
    
    private ReviewDto mapToDto(Review review, String targetName) {
        User reviewer = review.getReviewer();
        
        return ReviewDto.builder()
                .id(review.getId())
                .reviewerId(reviewer.getId())
                .reviewerName(reviewer.getFirstName() + " " + reviewer.getLastName())
                .orderItemId(review.getOrderItem() != null ? review.getOrderItem().getId() : null)
                .type(review.getType())
                .targetId(review.getTargetId())
                .targetName(targetName)
                .rating(review.getRating())
                .title(review.getTitle())
                .comment(review.getComment())
                .imageUrl(review.getImageUrl())
                .verifiedPurchase(review.isVerifiedPurchase())
                .sellerResponse(review.getSellerResponse())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
