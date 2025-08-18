package com.loopers.application.product;

public record ProductsCondition(
        String searchKeyword,
        Long brandId,
        ProductsSortType sortBy,
        Integer offset,
        Integer size) {
    private static final Integer DEFAULT_OFFSET = 0;
    private static final Integer DEFAULT_SIZE = 20;

    public ProductsCondition() {
        this(null, null, null, DEFAULT_OFFSET, DEFAULT_SIZE);
    }

    public ProductsCondition(ProductsSortType sortBy) {
        this(null, null, sortBy, DEFAULT_OFFSET, DEFAULT_SIZE);
    }

    public ProductsCondition(Long brandId) {
        this(null, brandId, null, DEFAULT_OFFSET, DEFAULT_SIZE);
    }
}
