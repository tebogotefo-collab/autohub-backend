package com.mathotech.autopartshub.dto.listing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListingImageDto {
    private Long id;
    private String imageUrl;
    private boolean primary;
}
