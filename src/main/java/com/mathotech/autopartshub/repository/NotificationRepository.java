package com.mathotech.autopartshub.repository;

import com.mathotech.autopartshub.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Page<Notification> findByUserIdAndReadOrderByCreatedAtDesc(Long userId, boolean read, Pageable pageable);
    
    long countByUserIdAndRead(Long userId, boolean read);
    
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.id = :id AND n.user.id = :userId")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.user.id = :userId")
    int markAllAsRead(@Param("userId") Long userId);
}
