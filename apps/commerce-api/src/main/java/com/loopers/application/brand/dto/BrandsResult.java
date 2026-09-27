package com.loopers.application.brand.dto;

import com.loopers.domain.brand.Brand;

public record BrandsResult(Long id, String brandName, String logoUrl, String description) {
    public static BrandsResult from(Brand brand) {
        return new BrandsResult(brand.getId(), brand.getName(), brand.getLogoUrl(), brand.getDescription());
    }
}
