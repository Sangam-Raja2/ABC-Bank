package com.sangam.abcbank.creditcardservice.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

/**
 * NOTE: this duplicates the converter you already added in banking-service.
 * Consider moving it into common-security (or a new common-jpa module) so both
 * services share one implementation instead of drifting out of sync.
 */
@Converter(autoApply = true)
public class YearMonthConverter implements AttributeConverter<YearMonth, String> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    @Override
    public String convertToDatabaseColumn(YearMonth attribute) {
        return attribute == null ? null : attribute.format(FORMATTER);
    }

    @Override
    public YearMonth convertToEntityAttribute(String dbData) {
        return dbData == null ? null : YearMonth.parse(dbData, FORMATTER);
    }
}
