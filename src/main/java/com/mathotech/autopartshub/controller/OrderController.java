package com.mathotech.autopartshub.controller;

import com.mathotech.autopartshub.dto.order.CreateOrderRequest;
import com.mathotech.autopartshub.dto.order.OrderDto;
import com.mathotech.autopartshub.dto.order.UpdateOrderStatusRequest;
import com.mathotech.autopartshub.model.OrderStatus;
import com.mathotech.autopartshub.model.User;
import com.mathotech.autopartshub.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('ROLE_BUYER')")
    public ResponseEntity<OrderDto> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal User user) {
        return new ResponseEntity<>(orderService.createOrder(request, user.getId()), HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrderById(
            @PathVariable Long orderId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.getOrderById(orderId, user.getId(), user.getRole()));
    }

    @GetMapping("/buyer")
    @PreAuthorize("hasRole('ROLE_BUYER')")
    public ResponseEntity<Page<OrderDto>> getCurrentBuyerOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @AuthenticationPrincipal User user) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<OrderDto> orders;
        if (status != null) {
            orders = orderService.getOrdersByBuyerAndStatus(user.getId(), status, pageRequest);
        } else {
            orders = orderService.getOrdersByBuyer(user.getId(), pageRequest);
        }
        
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/seller")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<Page<OrderDto>> getCurrentSellerOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @AuthenticationPrincipal User user) {
        
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        Page<OrderDto> orders;
        if (status != null) {
            orders = orderService.getOrdersBySellerAndStatus(user.getId(), status, pageRequest);
        } else {
            orders = orderService.getOrdersBySeller(user.getId(), pageRequest);
        }
        
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(orderService.updateOrderStatus(request, orderId, user.getId()));
    }
}
