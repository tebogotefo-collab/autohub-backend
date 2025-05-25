package com.mathotech.autopartshub.service;

import com.mathotech.autopartshub.dto.notification.NotificationDto;
import com.mathotech.autopartshub.model.Notification;
import com.mathotech.autopartshub.model.NotificationType;
import com.mathotech.autopartshub.model.User;
import com.mathotech.autopartshub.repository.NotificationRepository;
import com.mathotech.autopartshub.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public NotificationDto createNotification(Long userId, NotificationType type, String title, 
                                              String message, Long referenceId, String referenceType,
                                              String actionUrl) {
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .referenceType(referenceType)
                .actionUrl(actionUrl)
                .read(false)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Created notification: {}", savedNotification);
        
        return mapToDto(savedNotification);
    }

    @Transactional
    public void createOrderCreatedNotification(Long userId, Long orderId) {
        createNotification(
                userId,
                NotificationType.ORDER_CREATED,
                "Order Created",
                "Your order has been successfully created and is awaiting payment.",
                orderId,
                "Order",
                "/orders/" + orderId
        );
    }

    @Transactional
    public void createOrderStatusUpdatedNotification(Long userId, Long orderId, String oldStatus, String newStatus) {
        createNotification(
                userId,
                NotificationType.ORDER_STATUS_UPDATED,
                "Order Status Updated",
                "Your order status has changed from " + oldStatus + " to " + newStatus + ".",
                orderId,
                "Order",
                "/orders/" + orderId
        );
    }

    @Transactional
    public void createPaymentSuccessfulNotification(Long userId, Long orderId) {
        createNotification(
                userId,
                NotificationType.PAYMENT_SUCCESSFUL,
                "Payment Successful",
                "Your payment has been successfully processed for order #" + orderId + ".",
                orderId,
                "Order",
                "/orders/" + orderId
        );
    }

    @Transactional
    public void createPaymentFailedNotification(Long userId, Long orderId) {
        createNotification(
                userId,
                NotificationType.PAYMENT_FAILED,
                "Payment Failed",
                "There was an issue processing your payment for order #" + orderId + ". Please try again.",
                orderId,
                "Order",
                "/payment/retry/" + orderId
        );
    }

    @Transactional
    public void createNewReviewNotification(Long sellerId, Long reviewId, String productName) {
        createNotification(
                sellerId,
                NotificationType.NEW_REVIEW,
                "New Review Received",
                "Your product \"" + productName + "\" has received a new review.",
                reviewId,
                "Review",
                "/seller/reviews/" + reviewId
        );
    }

    @Transactional
    public void createReviewResponseNotification(Long reviewerId, Long reviewId, String sellerName) {
        createNotification(
                reviewerId,
                NotificationType.REVIEW_RESPONSE,
                "Seller Responded to Your Review",
                sellerName + " has responded to your review.",
                reviewId,
                "Review",
                "/account/reviews/" + reviewId
        );
    }

    @Transactional
    public void createListingSoldNotification(Long sellerId, Long listingId, String listingTitle) {
        createNotification(
                sellerId,
                NotificationType.LISTING_SOLD,
                "Product Sold",
                "Your product \"" + listingTitle + "\" has been sold.",
                listingId,
                "Listing",
                "/seller/sales"
        );
    }

    public Page<NotificationDto> getUserNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::mapToDto);
    }

    public Page<NotificationDto> getUnreadNotifications(Long userId, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false, pageable);
        return notifications.map(this::mapToDto);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndRead(userId, false);
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        int updated = notificationRepository.markAsRead(notificationId, userId);
        if (updated == 0) {
            throw new EntityNotFoundException("Notification not found or does not belong to user");
        }
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    private NotificationDto mapToDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceId(notification.getReferenceId())
                .referenceType(notification.getReferenceType())
                .actionUrl(notification.getActionUrl())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
