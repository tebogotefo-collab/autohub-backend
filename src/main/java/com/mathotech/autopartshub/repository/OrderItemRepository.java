package com.mathotech.autopartshub.repository;

import com.mathotech.autopartshub.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    
    List<OrderItem> findByOrderId(Long orderId);
    
    List<OrderItem> findBySellerId(Long sellerId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId AND oi.seller.id = :sellerId")
    List<OrderItem> findByOrderIdAndSellerId(
            @Param("orderId") Long orderId, 
            @Param("sellerId") Long sellerId);
    
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.listing.id = :listingId")
    long countByListingId(@Param("listingId") Long listingId);
}
