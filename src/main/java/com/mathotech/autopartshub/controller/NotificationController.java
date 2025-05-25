package com.mathotech.autopartshub.controller;

import com.mathotech.autopartshub.dto.notification.NotificationDto;
import com.mathotech.autopartshub.model.User;
import com.mathotech.autopartshub.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificationDto>> getUserNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(notificationService.getUserNotifications(user.getId(), pageable));
    }

    @GetMapping("/unread")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<NotificationDto>> getUnreadNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(notificationService.getUnreadNotifications(user.getId(), pageable));
    }

    @GetMapping("/count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal User user) {
        long count = notificationService.getUnreadCount(user.getId());
        Map<String, Long> response = new HashMap<>();
        response.put("unreadCount", count);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        
        notificationService.markAsRead(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal User user) {
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok().build();
    }
}
