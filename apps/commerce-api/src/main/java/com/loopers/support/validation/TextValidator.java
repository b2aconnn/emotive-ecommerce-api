package com.loopers.support.validation;

public class TextValidator {
    public static void requireText(String str, String message) {
        if (str == null || str.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
