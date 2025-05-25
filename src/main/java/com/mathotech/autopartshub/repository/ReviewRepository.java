package com.mathotech.autopartshub.repository;

import com.mathotech.autopartshub.model.Review;
import com.mathotech.autopartshub.model.ReviewType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    List<Review> findByTypeAndTargetId(ReviewType type, Long targetId);
    
    Page<Review> findByTypeAndTargetId(ReviewType type, Long targetId, Pageable pageable);
    
    List<Review> findByReviewerId(Long reviewerId);
    
    Page<Review> findByReviewerId(Long reviewerId, Pageable pageable);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.type = :type AND r.targetId = :targetId")
    Double calculateAverageRating(@Param("type") ReviewType type, @Param("targetId") Long targetId);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.type = :type AND r.targetId = :targetId")
    Long countByTypeAndTargetId(@Param("type") ReviewType type, @Param("targetId") Long targetId);
    
    Optional<Review> findByOrderItemIdAndReviewerId(Long orderItemId, Long reviewerId);
    
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM Review r " +
           "WHERE r.orderItem.id = :orderItemId AND r.reviewer.id = :reviewerId")
    boolean existsByOrderItemIdAndReviewerId(
            @Param("orderItemId") Long orderItemId, 
            @Param("reviewerId") Long reviewerId);
}
