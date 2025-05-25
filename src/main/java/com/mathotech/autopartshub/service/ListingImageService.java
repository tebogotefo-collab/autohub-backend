package com.mathotech.autopartshub.service;

import com.mathotech.autopartshub.dto.listing.ListingImageDto;
import com.mathotech.autopartshub.model.Listing;
import com.mathotech.autopartshub.model.ListingImage;
import com.mathotech.autopartshub.repository.ListingImageRepository;
import com.mathotech.autopartshub.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingImageService {

    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;
    private final FileStorageService fileStorageService;

    public List<ListingImageDto> getListingImages(Long listingId) {
        return listingImageRepository.findByListingId(listingId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ListingImageDto addImageToListing(Long listingId, MultipartFile file, boolean isPrimary, Long sellerId) {
        // Find listing and check ownership
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found with id: " + listingId));
        
        if (!listing.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("You are not authorized to add images to this listing");
        }
        
        // Store the file
        String fileName = fileStorageService.storeFile(file);
        
        // If this is marked as primary, remove primary flag from all other images
        if (isPrimary) {
            listingImageRepository.findByListingIdAndPrimaryTrue(listingId)
                    .ifPresent(image -> {
                        image.setPrimary(false);
                        listingImageRepository.save(image);
                    });
        }
        
        // Generate a URL path for the image
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/images/")
                .path(fileName)
                .toUriString();
        
        // Create and save the listing image entity
        ListingImage listingImage = new ListingImage();
        listingImage.setListing(listing);
        listingImage.setImageUrl(fileDownloadUri);
        listingImage.setPrimary(isPrimary);
        
        ListingImage savedImage = listingImageRepository.save(listingImage);
        
        return mapToDto(savedImage);
    }

    @Transactional
    public void deleteImage(Long imageId, Long sellerId) {
        ListingImage image = listingImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));
        
        // Check ownership
        if (!image.getListing().getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("You are not authorized to delete this image");
        }
        
        // Extract the filename from the URL
        String imageUrl = image.getImageUrl();
        String fileName = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
        
        // Delete the file from storage
        fileStorageService.deleteFile(fileName);
        
        // Delete the image record
        listingImageRepository.deleteById(imageId);
    }

    @Transactional
    public void setPrimaryImage(Long imageId, Long sellerId) {
        ListingImage image = listingImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));
        
        // Check ownership
        if (!image.getListing().getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("You are not authorized to update this image");
        }
        
        // Clear primary flag from all images of this listing
        listingImageRepository.findByListingIdAndPrimaryTrue(image.getListing().getId())
                .ifPresent(primaryImage -> {
                    primaryImage.setPrimary(false);
                    listingImageRepository.save(primaryImage);
                });
        
        // Set this image as primary
        image.setPrimary(true);
        listingImageRepository.save(image);
    }

    private ListingImageDto mapToDto(ListingImage image) {
        return ListingImageDto.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .primary(image.isPrimary())
                .build();
    }
}
