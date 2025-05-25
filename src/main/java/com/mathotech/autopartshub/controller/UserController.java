package com.mathotech.autopartshub.controller;

import com.mathotech.autopartshub.dto.user.UpdateProfileRequest;
import com.mathotech.autopartshub.dto.user.UserProfileDto;
import com.mathotech.autopartshub.model.User;
import com.mathotech.autopartshub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getCurrentUserProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getUserProfile(user.getId()));
    }

    @GetMapping("/{userId}/profile")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<UserProfileDto> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateCurrentUserProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.updateUserProfile(user.getId(), user.getId(), request));
    }

    @PutMapping("/{userId}/profile")
    @PreAuthorize("hasRole('ROLE_ADMIN') or #userId == authentication.principal.id")
    public ResponseEntity<UserProfileDto> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.updateUserProfile(userId, user.getId(), request));
    }
}
