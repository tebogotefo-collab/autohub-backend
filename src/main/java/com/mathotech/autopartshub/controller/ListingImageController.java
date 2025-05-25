package com.mathotech.autopartshub.controller;

import com.mathotech.autopartshub.dto.listing.ListingImageDto;
import com.mathotech.autopartshub.model.User;
import com.mathotech.autopartshub.service.FileStorageService;
import com.mathotech.autopartshub.service.ListingImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ListingImageController {

    private final ListingImageService listingImageService;
    private final FileStorageService fileStorageService;

    @GetMapping("/api/listings/{listingId}/images")
    public ResponseEntity<List<ListingImageDto>> getListingImages(@PathVariable Long listingId) {
        return ResponseEntity.ok(listingImageService.getListingImages(listingId));
    }

    @PostMapping("/api/listings/{listingId}/images")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<ListingImageDto> uploadImage(
            @PathVariable Long listingId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "primary", defaultValue = "false") boolean primary,
            @AuthenticationPrincipal User user) {
        ListingImageDto imageDto = listingImageService.addImageToListing(listingId, file, primary, user.getId());
        return new ResponseEntity<>(imageDto, HttpStatus.CREATED);
    }

    @DeleteMapping("/api/images/{imageId}")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long imageId,
            @AuthenticationPrincipal User user) {
        listingImageService.deleteImage(imageId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/images/{imageId}/set-primary")
    @PreAuthorize("hasRole('ROLE_SELLER')")
    public ResponseEntity<Void> setPrimaryImage(
            @PathVariable Long imageId,
            @AuthenticationPrincipal User user) {
        listingImageService.setPrimaryImage(imageId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/images/{fileName:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        
        // Try to determine file's content type
        String contentType = null;
        try {
            contentType = determineContentType(resource);
        } catch (IOException ex) {
            contentType = "application/octet-stream";
        }
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    
    private String determineContentType(Resource resource) throws IOException {
        String fileName = resource.getFilename();
        if (fileName == null) {
            return "application/octet-stream";
        }
        
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "webp":
                return "image/webp";
            default:
                return "application/octet-stream";
        }
    }
}
