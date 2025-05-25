package com.mathotech.autopartshub.controller;

import com.mathotech.autopartshub.dto.review.CreateProductReviewRequest;
import com.mathotech.autopartshub.dto.review.CreateSellerReviewRequest;
import com.mathotech.autopartshub.dto.review.ReviewDto;
import com.mathotech.autopartshub.dto.review.SellerReviewResponseRequest;
import com.mathotech.autopartshub.model.User;
import com.mathotech.autopartshub.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/products")
    @PreAuthorize("hasRole('ROLE_BUYER')")
    public ResponseEntity<ReviewDto> createProductReview(
            @Valid @RequestBody CreateProductReviewRequest request,
            @AuthenticationPrincipal User user) {
        return new ResponseEntity<>(
                reviewService.createProductReview(request, user.getId()),
                HttpStatus.CREATED);
    }

    @PostMapping("/sellers")
    @PreAuthorize("hasRole('ROLE_BUYER')")
    public ResponseEntity<ReviewDto> createSellerReview(
            @Valid @RequestBody CreateSellerReviewRequest request,
            @AuthenticationPrincipal User user) {
        return new ResponseEntity<>(
                reviewService.createSellerReview(request, user.getId()),
                HttpStatus.CREATED);
    }

    @PostMapping("/respond")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<ReviewDto> respondToReview(
            @Valid @RequestBody SellerReviewResponseRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(reviewService.addSellerResponse(request, user.getId()));
    }

    @GetMapping("/products/{listingId}")
    public ResponseEntity<Page<ReviewDto>> getProductReviews(
            @PathVariable Long listingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(reviewService.getProductReviews(listingId, pageable));
    }

    @GetMapping("/sellers/{sellerId}")
    public ResponseEntity<Page<ReviewDto>> getSellerReviews(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(reviewService.getSellerReviews(sellerId, pageable));
    }

    @GetMapping("/user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<ReviewDto>> getUserReviews(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = Sort.by(sortDir.equals("asc") ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(reviewService.getUserReviews(user.getId(), pageable));
    }
}
