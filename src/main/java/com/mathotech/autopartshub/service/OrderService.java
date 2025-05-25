package com.mathotech.autopartshub.service;

import com.mathotech.autopartshub.dto.order.CreateOrderRequest;
import com.mathotech.autopartshub.dto.order.OrderDto;
import com.mathotech.autopartshub.dto.order.OrderItemDto;
import com.mathotech.autopartshub.dto.order.UpdateOrderStatusRequest;
import com.mathotech.autopartshub.model.*;
import com.mathotech.autopartshub.repository.ListingRepository;
import com.mathotech.autopartshub.repository.OrderItemRepository;
import com.mathotech.autopartshub.repository.OrderRepository;
import com.mathotech.autopartshub.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final NotificationService notificationService;
    
    @Value("${app.tax-rate:0.15}")  // Default VAT rate in South Africa is 15%
    private BigDecimal taxRate;

    @Value("${app.shipping-fee:100.00}")  // Default shipping fee in ZAR
    private BigDecimal defaultShippingFee;

    @Transactional
    public OrderDto createOrder(CreateOrderRequest request, Long buyerId) {
        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + buyerId));

        // Validate items and calculate subtotal
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (var itemRequest : request.getItems()) {
            Listing listing = listingRepository.findById(itemRequest.getListingId())
                    .orElseThrow(() -> new EntityNotFoundException("Listing not found with id: " + itemRequest.getListingId()));

            // Check if listing is active
            if (!listing.isActive()) {
                throw new IllegalStateException("Listing is not active: " + listing.getTitle());
            }

            // Check if the requested quantity is available
            if (listing.getQuantity() < itemRequest.getQuantity()) {
                throw new IllegalStateException("Not enough quantity available for: " + listing.getTitle());
            }

            // Calculate item total price
            BigDecimal itemTotalPrice = listing.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            subtotal = subtotal.add(itemTotalPrice);

            // Prepare order item (will be saved with the order)
            OrderItem orderItem = new OrderItem();
            orderItem.setListing(listing);
            orderItem.setSeller(listing.getSeller());
            orderItem.setListingTitle(listing.getTitle());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(listing.getPrice());
            orderItem.setTotalPrice(itemTotalPrice);

            orderItems.add(orderItem);

            // Update listing quantity
            listing.setQuantity(listing.getQuantity() - itemRequest.getQuantity());
            listingRepository.save(listing);
        }

        // Calculate tax and total
        BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(defaultShippingFee).add(taxAmount);

        // Create new order
        Order order = new Order();
        order.setBuyer(buyer);
        order.setStatus(OrderStatus.PENDING_PAYMENT);
        order.setSubtotal(subtotal);
        order.setShippingFee(defaultShippingFee);
        order.setTaxAmount(taxAmount);
        order.setTotal(total);
        order.setShippingAddress(request.getShippingAddress());
        order.setShippingCity(request.getShippingCity());
        order.setShippingPostalCode(request.getShippingPostalCode());
        order.setShippingState(request.getShippingProvince()); // Using province from request for state
        order.setContactPhone(request.getContactPhone());
        order.setContactEmail(request.getContactEmail());

        // Save the order
        Order savedOrder = orderRepository.save(order);

        // Connect order items to the saved order
        for (OrderItem item : orderItems) {
            item.setOrder(savedOrder);
        }
        
        orderItems = orderItemRepository.saveAll(orderItems);
        savedOrder.setItems(orderItems);
        
        // Send notification to buyer about order creation
        notificationService.createOrderCreatedNotification(buyerId, savedOrder.getId());
        
        // Send notifications to sellers about their new orders
        Set<Long> sellerIds = orderItems.stream()
                .map(item -> item.getSeller().getId())
                .collect(Collectors.toSet());
                
        sellerIds.forEach(sellerId -> {
            notificationService.createNotification(
                sellerId,
                NotificationType.LISTING_SOLD,
                "New Order Received",
                "You have received a new order.",
                savedOrder.getId(),
                "Order",
                "/seller/orders/" + savedOrder.getId()
            );
        });

        return mapToDto(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long orderId, Long userId, Role userRole) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        // Check permissions: buyers can only see their own orders, sellers only orders with their items
        if (userRole == Role.ROLE_BUYER && !order.getBuyer().getId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to view this order");
        } else if (userRole == Role.ROLE_SELLER) {
            boolean hasPermission = order.getItems().stream()
                    .anyMatch(item -> item.getSeller().getId().equals(userId));
            if (!hasPermission) {
                throw new AccessDeniedException("You do not have permission to view this order");
            }
        }
        
        return mapToDto(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByBuyer(Long buyerId, Pageable pageable) {
        Page<Order> orders = orderRepository.findByBuyerId(buyerId, pageable);
        return orders.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersBySeller(Long sellerId, Pageable pageable) {
        Page<Order> orders = orderRepository.findBySellerId(sellerId, pageable);
        return orders.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByBuyerAndStatus(Long buyerId, OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findByBuyerIdAndStatus(buyerId, status, pageable);
        return orders.map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersBySellerAndStatus(Long sellerId, OrderStatus status, Pageable pageable) {
        Page<Order> orders = orderRepository.findBySellerIdAndStatus(sellerId, status, pageable);
        return orders.map(this::mapToDto);
    }

    @Transactional
    public OrderDto updateOrderStatus(UpdateOrderStatusRequest request, Long orderId, Long userId) {
        // Find order and check permissions
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        
        Role userRole = user.getRole();
        
        // No change needed if status is the same
        if (order.getStatus() == request.getStatus()) {
            return mapToDto(order);
        }
        
        // Store old status for notification
        OrderStatus oldStatus = order.getStatus();
        
        // Verify permissions for status change based on role
        if (userRole == Role.ROLE_BUYER) {
            // Buyers can only cancel their own orders in certain statuses
            if (!order.getBuyer().getId().equals(userId)) {
                throw new AccessDeniedException("You can only update your own orders");
            }
            
            if (request.getStatus() != OrderStatus.CANCELLED) {
                throw new AccessDeniedException("Buyers can only cancel orders");
            }
            
            if (order.getStatus() != OrderStatus.PENDING_PAYMENT && 
                order.getStatus() != OrderStatus.PAYMENT_COMPLETED) {
                throw new IllegalStateException("Orders can only be cancelled if they are pending payment or payment completed");
            }
        } else if (userRole == Role.ROLE_SELLER) {
            // Sellers can only update orders containing their products
            boolean hasSellerItems = order.getItems().stream()
                    .anyMatch(item -> item.getSeller().getId().equals(userId));
                    
            if (!hasSellerItems) {
                throw new AccessDeniedException("You can only update orders containing your products");
            }
            
            // Limit status updates for sellers
            if (request.getStatus() != OrderStatus.PROCESSING && 
                request.getStatus() != OrderStatus.SHIPPED && 
                request.getStatus() != OrderStatus.DELIVERED) {
                throw new AccessDeniedException("Sellers can only update order status to Processing, Shipped, or Delivered");
            }
        }
        // No restrictions for admins (Role.ROLE_ADMIN)
        
        // Check valid transitions for everyone
        validateStatusTransition(order.getStatus(), request.getStatus());
        
        // Update order status and relevant fields
        order.setStatus(request.getStatus());
        if (request.getTrackingNumber() != null) {
            order.setTrackingNumber(request.getTrackingNumber());
        }
        
        // Update timestamp fields based on new status
        switch (request.getStatus()) {
            case PAYMENT_COMPLETED:
                order.setPaymentDate(LocalDateTime.now());
                break;
            case SHIPPED:
                order.setShippedDate(LocalDateTime.now());
                break;
            case DELIVERED:
                order.setDeliveredDate(LocalDateTime.now());
                break;
            case CANCELLED:
                order.setCancelledDate(LocalDateTime.now());
                // Return items to inventory if order is cancelled
                returnItemsToInventory(order);
                break;
            case REFUNDED:
                // Handle refund logic if needed
                break;
            default:
                break;
        }
        
        Order updatedOrder = orderRepository.save(order);
        
        // Send notification to buyer about status change
        notificationService.createOrderStatusUpdatedNotification(
            order.getBuyer().getId(), 
            order.getId(), 
            oldStatus.toString(), 
            request.getStatus().toString()
        );
        
        // Send notification to relevant sellers too
        Set<Long> sellerIds = order.getItems().stream()
            .map(item -> item.getListing().getSeller().getId())
            .collect(Collectors.toSet());
            
        sellerIds.forEach(sellerId -> {
            notificationService.createOrderStatusUpdatedNotification(
                sellerId,
                order.getId(),
                oldStatus.toString(),
                request.getStatus().toString()
            );
        });
        
        return mapToDto(updatedOrder);
    }
    
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Define valid status transitions using a switch expression
        boolean validTransition = switch (currentStatus) {
            case PENDING_PAYMENT -> newStatus == OrderStatus.PAYMENT_COMPLETED || 
                                    newStatus == OrderStatus.CANCELLED;
            case PAYMENT_COMPLETED -> newStatus == OrderStatus.PROCESSING || 
                                      newStatus == OrderStatus.CANCELLED || 
                                      newStatus == OrderStatus.REFUNDED;
            case PROCESSING -> newStatus == OrderStatus.SHIPPED || 
                               newStatus == OrderStatus.CANCELLED || 
                               newStatus == OrderStatus.REFUNDED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED || 
                            newStatus == OrderStatus.REFUNDED;
            case DELIVERED -> newStatus == OrderStatus.REFUNDED;
            // Once cancelled or refunded, no further transitions are allowed
            case CANCELLED, REFUNDED -> false;
        };
        
        if (!validTransition) {
            throw new IllegalStateException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }
    
    private void returnItemsToInventory(Order order) {
        // Return quantities to listings
        for (OrderItem item : order.getItems()) {
            Listing listing = item.getListing();
            listing.setQuantity(listing.getQuantity() + item.getQuantity());
            listingRepository.save(listing);
        }
    }

    private OrderDto mapToDto(Order order) {
        OrderDto dto = OrderDto.builder()
                .id(order.getId())
                .buyerId(order.getBuyer().getId())
                .buyerName(order.getBuyer().getFirstName() + " " + order.getBuyer().getLastName())
                .buyerEmail(order.getBuyer().getEmail())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .shippingFee(order.getShippingFee())
                .taxAmount(order.getTaxAmount())
                .total(order.getTotal())
                .shippingAddress(order.getShippingAddress())
                .shippingCity(order.getShippingCity())
                .shippingPostalCode(order.getShippingPostalCode())
                .shippingProvince(order.getShippingState()) // Using state for province in DTO
                .contactPhone(order.getContactPhone())
                .contactEmail(order.getContactEmail())
                .trackingNumber(order.getTrackingNumber())
                .paymentId(order.getPaymentId())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .paymentDate(order.getPaymentDate())
                .shippedDate(order.getShippedDate())
                .deliveredDate(order.getDeliveredDate())
                .cancelledDate(order.getCancelledDate())
                .build();
        
        // Map order items
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            List<OrderItemDto> itemDtos = order.getItems().stream()
                    .map(item -> OrderItemDto.builder()
                            .id(item.getId())
                            .listingId(item.getListing().getId())
                            .sellerId(item.getSeller().getId())
                            .sellerBusinessName(item.getSeller().getBusinessName())
                            .listingTitle(item.getListingTitle())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .totalPrice(item.getTotalPrice())
                            .createdAt(item.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());
            
            dto.setItems(itemDtos);
        }
        
        return dto;
    }
}
