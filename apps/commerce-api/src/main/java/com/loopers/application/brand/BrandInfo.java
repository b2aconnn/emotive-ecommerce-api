package com.loopers.application.brand;

import com.loopers.domain.brand.Brand;

public record BrandInfo(Long id,
                        String brandName,
                        String logoUrl,
                        String description) {
    public static BrandInfo from(Brand brand) {
        return new BrandInfo(
                brand.getId(),
                brand.getName(),
                brand.getLogoUrl(),
                brand.getDescription()
        );
    }
}
