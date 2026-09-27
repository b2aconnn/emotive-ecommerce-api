package com.loopers.interfaces.api.brand.dto;

import com.loopers.application.brand.dto.BrandsCondition;

import io.swagger.v3.oas.annotations.media.Schema;

public record BrandsRequest(
        @Schema(description = "브랜드명 부분 검색어. 비어있으면 전체 조회합니다.", example = "나이키")
        String searchKeyword,

        @Schema(description = "건너뛸 건수. 생략하면 0 입니다.", defaultValue = "0", example = "0")
        Integer offset,

        @Schema(description = "조회할 최대 건수. 생략하면 20 입니다.", defaultValue = "20", example = "20")
        Integer size) {

    public BrandsCondition toCondition() {
        return new BrandsCondition(searchKeyword, offset, size);
    }
}
