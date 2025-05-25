package com.mathotech.autopartshub.controller;

import com.mathotech.autopartshub.dto.brand.BrandDto;
import com.mathotech.autopartshub.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public ResponseEntity<List<BrandDto>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<BrandDto>> getBrandsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortBy));
        return ResponseEntity.ok(brandService.getBrandsPage(pageRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BrandDto> getBrandById(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.getBrandById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<BrandDto> createBrand(@Valid @RequestBody BrandDto brandDto) {
        return new ResponseEntity<>(brandService.createBrand(brandDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<BrandDto> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandDto brandDto) {
        return ResponseEntity.ok(brandService.updateBrand(id, brandDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
}
