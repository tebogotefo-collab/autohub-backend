package com.mathotech.autopartshub.controller;

import com.mathotech.autopartshub.dto.listing.ListingDto;
import com.mathotech.autopartshub.model.Condition;
import com.mathotech.autopartshub.model.User;
import com.mathotech.autopartshub.service.ListingService;
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

import java.util.List;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @GetMapping
    public ResponseEntity<Page<ListingDto>> getAllListings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(listingService.getAllListings(pageRequest));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ListingDto>> getListingsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(listingService.getListingsByCategory(categoryId, pageRequest));
    }

    @GetMapping("/brand/{brandId}")
    public ResponseEntity<Page<ListingDto>> getListingsByBrand(
            @PathVariable Long brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(listingService.getListingsByBrand(brandId, pageRequest));
    }

    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<Page<ListingDto>> getListingsBySeller(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(listingService.getListingsBySeller(sellerId, pageRequest));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<ListingDto>> getListingsByFilters(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Condition condition,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(listingService.getListingsByFilters(categoryId, brandId, condition, pageRequest));
    }

    @GetMapping("/vehicle-compatibility")
    public ResponseEntity<Page<ListingDto>> getListingsByVehicleCompatibility(
            @RequestParam String make,
            @RequestParam String model,
            @RequestParam Integer year,
            @RequestParam(required = false) String engine,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("ASC") ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(listingService.getListingsByVehicleCompatibility(make, model, year, engine, pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListingDto> getListingById(@PathVariable Long id) {
        return ResponseEntity.ok(listingService.getListingById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<ListingDto> createListing(
            @Valid @RequestBody ListingDto listingDto,
            @AuthenticationPrincipal User user) {
        return new ResponseEntity<>(listingService.createListing(listingDto, user.getId()), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<ListingDto> updateListing(
            @PathVariable Long id,
            @Valid @RequestBody ListingDto listingDto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(listingService.updateListing(id, listingDto, user.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<Void> deleteListing(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        listingService.deleteListing(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<Void> toggleListingActive(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        listingService.toggleListingActive(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-featured")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> toggleListingFeatured(@PathVariable Long id) {
        listingService.toggleListingFeatured(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ListingDto>> getFeaturedListings() {
        return ResponseEntity.ok(listingService.getFeaturedListings());
    }
}
