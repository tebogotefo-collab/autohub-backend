package com.mathotech.autopartshub.dto.notification;

import com.mathotech.autopartshub.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private Long referenceId;
    private String referenceType;
    private String actionUrl;
    private boolean read;
    private LocalDateTime createdAt;
}
