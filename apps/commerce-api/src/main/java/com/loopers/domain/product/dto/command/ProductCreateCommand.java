package com.loopers.domain.product.dto.command;

import com.loopers.domain.brand.Brand;

public record ProductCreateCommand(
        String name,
        String mainImageUrl,
        String description,
        Long price,
        Brand brand
) {}
