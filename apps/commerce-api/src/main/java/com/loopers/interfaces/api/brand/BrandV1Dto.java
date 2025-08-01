package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandInfo;
import com.loopers.application.user.UserInfo;

import java.time.LocalDate;

public class BrandV1Dto {
    public record InfoResponse(Long brandId,
                                 String brandName,
                                 String logoUrl,
                                 String description) {
        public static InfoResponse from(BrandInfo info) {
            return new InfoResponse(
                    info.id(),
                    info.brandName(),
                    info.logoUrl(),
                    info.description()
            );
        }
    }
}
