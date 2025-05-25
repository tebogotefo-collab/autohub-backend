package com.mathotech.autopartshub.service;

import com.mathotech.autopartshub.dto.brand.BrandDto;
import com.mathotech.autopartshub.model.Brand;
import com.mathotech.autopartshub.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    @Cacheable(value = "brands")
    public List<BrandDto> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "brands", key = "'page:' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<BrandDto> getBrandsPage(Pageable pageable) {
        return brandRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    @Cacheable(value = "brands", key = "#id")
    public BrandDto getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));
        
        return mapToDto(brand);
    }

    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public BrandDto createBrand(BrandDto brandDto) {
        // Check if brand with the same name already exists
        if (brandRepository.existsByName(brandDto.getName())) {
            throw new RuntimeException("Brand with name '" + brandDto.getName() + "' already exists");
        }
        
        Brand brand = new Brand();
        brand.setName(brandDto.getName());
        brand.setDescription(brandDto.getDescription());
        brand.setLogoUrl(brandDto.getLogoUrl());
        
        Brand savedBrand = brandRepository.save(brand);
        return mapToDto(savedBrand);
    }

    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public BrandDto updateBrand(Long id, BrandDto brandDto) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));
        
        // Check if updated brand name conflicts with an existing brand (other than this one)
        if (!brand.getName().equals(brandDto.getName()) && 
                brandRepository.existsByName(brandDto.getName())) {
            throw new RuntimeException("Brand with name '" + brandDto.getName() + "' already exists");
        }
        
        brand.setName(brandDto.getName());
        brand.setDescription(brandDto.getDescription());
        brand.setLogoUrl(brandDto.getLogoUrl());
        
        Brand updatedBrand = brandRepository.save(brand);
        return mapToDto(updatedBrand);
    }

    @Transactional
    @CacheEvict(value = "brands", allEntries = true)
    public void deleteBrand(Long id) {
        // Note: In a real application, you might want to check if the brand is used by any listings
        // before allowing deletion, or implement a soft delete approach
        brandRepository.deleteById(id);
    }

    private BrandDto mapToDto(Brand brand) {
        return BrandDto.builder()
                .id(brand.getId())
                .name(brand.getName())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .build();
    }
}
