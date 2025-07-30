package com.loopers.domain.brand.dto.command;

public record BrandCreateCommand(
    String name,
    String logoUrl,
    String description
) {}
