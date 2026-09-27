package com.loopers.interfaces.api.brand.dto;

import com.loopers.application.brand.dto.BrandResult;

public record BrandInfoResponse(Long brandId, String brandName, String logoUrl, String description) {
    public static BrandInfoResponse from(BrandResult result) {
        return new BrandInfoResponse(result.id(), result.brandName(), result.logoUrl(), result.description());
    }
}
