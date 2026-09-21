package com.loopers.application.brand.dto;

public record BrandsCondition(
        String searchKeyword,
        Integer offset,
        Integer size) {
    private static final Integer DEFAULT_OFFSET = 0;
    private static final Integer DEFAULT_SIZE = 20;

    /**
     * offset/size는 domain repository 계약상 null이 아니어야 하므로 여기서 기본값을 채운다.
     * 편의 생성자가 아니라 compact 생성자에 두는 이유: @ModelAttribute는 record를 canonical
     * 생성자로 바인딩하므로, 쿼리 파라미터가 없으면 편의 생성자를 거치지 않고 null이 그대로 들어온다.
     */
    public BrandsCondition {
        if (offset == null) {
            offset = DEFAULT_OFFSET;
        }
        if (size == null) {
            size = DEFAULT_SIZE;
        }
    }

    public BrandsCondition() {
        this(null, DEFAULT_OFFSET, DEFAULT_SIZE);
    }
}
