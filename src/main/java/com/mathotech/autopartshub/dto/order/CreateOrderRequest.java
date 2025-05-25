package com.mathotech.autopartshub.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderRequest {
    
    @NotBlank(message = "Shipping address is required")
    @Size(max = 255, message = "Shipping address must be less than 255 characters")
    private String shippingAddress;
    
    @NotBlank(message = "Shipping city is required")
    @Size(max = 100, message = "Shipping city must be less than 100 characters")
    private String shippingCity;
    
    @NotBlank(message = "Shipping postal code is required")
    @Size(max = 20, message = "Shipping postal code must be less than 20 characters")
    private String shippingPostalCode;
    
    @NotBlank(message = "Shipping province is required")
    @Size(max = 100, message = "Shipping province must be less than 100 characters")
    private String shippingProvince;
    
    @NotBlank(message = "Contact phone is required")
    @Size(max = 20, message = "Contact phone must be less than 20 characters")
    private String contactPhone;
    
    @NotBlank(message = "Contact email is required")
    @Email(message = "Contact email must be valid")
    @Size(max = 100, message = "Contact email must be less than 100 characters")
    private String contactEmail;
    
    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<CreateOrderItemRequest> items;
}
