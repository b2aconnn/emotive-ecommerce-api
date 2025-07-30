package com.loopers.application.product;

import com.loopers.support.dto.PageCond;

public record ProductsCond(
        String searchKeyword,
        ProductsSortType sortBy,
        PageCond pageCond) {

    public ProductsCond() {
        this(null, null, new PageCond());
    }

    public ProductsCond(String searchKeyword, ProductsSortType sortBy) {
        this(searchKeyword, sortBy, new PageCond());
    }
}
