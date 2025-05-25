package com.mathotech.autopartshub.repository;

import com.mathotech.autopartshub.model.Order;
import com.mathotech.autopartshub.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Page<Order> findByBuyerId(Long buyerId, Pageable pageable);
    
    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.seller.id = :sellerId")
    Page<Order> findBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);
    
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    @Query("SELECT o FROM Order o JOIN o.items i WHERE i.seller.id = :sellerId AND o.status = :status")
    Page<Order> findBySellerIdAndStatus(
            @Param("sellerId") Long sellerId, 
            @Param("status") OrderStatus status, 
            Pageable pageable);
    
    Page<Order> findByBuyerIdAndStatus(Long buyerId, OrderStatus status, Pageable pageable);
    
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime cutoffTime);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.buyer.id = :buyerId AND o.status = :status")
    long countByBuyerIdAndStatus(@Param("buyerId") Long buyerId, @Param("status") OrderStatus status);
}
