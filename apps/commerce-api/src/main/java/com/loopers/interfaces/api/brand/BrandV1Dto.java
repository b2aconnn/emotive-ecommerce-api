package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandResult;

public class BrandV1Dto {
    public record InfoResponse(Long brandId,
                                 String brandName,
                                 String logoUrl,
                                 String description) {
        public static InfoResponse from(BrandResult info) {
            if (info == null) {
                return null;
            }

            return new InfoResponse(
                    info.id(),
                    info.brandName(),
                    info.logoUrl(),
                    info.description()
            );
        }
    }
}
