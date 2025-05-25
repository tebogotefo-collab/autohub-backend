package com.mathotech.autopartshub.repository;

import com.mathotech.autopartshub.model.CompatibilityMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompatibilityMappingRepository extends JpaRepository<CompatibilityMapping, Long> {
    List<CompatibilityMapping> findByListingId(Long listingId);
    
    void deleteByListingId(Long listingId);
}
