package com.loopers.interfaces.api.brand.dto;

import com.loopers.application.brand.dto.BrandsResult;

public record BrandsResponse(Long id,
                              String brandName,
                              String logoUrl,
                              String description) {
    public static BrandsResponse from(BrandsResult result) {
        return new BrandsResponse(
                result.id(),
                result.brandName(),
                result.logoUrl(),
                result.description()
        );
    }
}
