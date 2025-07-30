package com.loopers.support.dto;

public record PageCond(
        Integer offset,
        Integer count) {
    private static final Integer DEFAULT_OFFSET = 0;
    private static final Integer DEFAULT_COUNT = 10;

    public PageCond() {
        this(DEFAULT_OFFSET, DEFAULT_COUNT);
    }

    public PageCond(Integer offset, Integer count) {
        this.offset = offset == null ? DEFAULT_OFFSET : offset;
        this.count = count == null ? DEFAULT_COUNT : count;
    }
}
