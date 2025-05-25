package com.mathotech.autopartshub.repository;

import com.mathotech.autopartshub.model.ListingImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListingImageRepository extends JpaRepository<ListingImage, Long> {
    List<ListingImage> findByListingId(Long listingId);
    
    Optional<ListingImage> findByListingIdAndPrimaryTrue(Long listingId);
    
    void deleteByListingId(Long listingId);
}
