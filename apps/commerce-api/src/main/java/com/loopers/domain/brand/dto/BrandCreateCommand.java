package com.loopers.domain.brand.dto;

public record BrandCreateCommand(
    String name,
    String logoUrl,
    String description
) {}
