package com.loopers.support.converter;

import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class DateConverter {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static LocalDate convertToLocalDate(String dateString) {
        if (Objects.isNull(dateString) || dateString.isBlank()) {
            throw new IllegalArgumentException(dateString + "을(를) 입력해주세요.");
        }

        try {
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(dateString + " 형식이 올바르지 않습니다. (yyyy-MM-dd 형식)");
        }
    }
}
