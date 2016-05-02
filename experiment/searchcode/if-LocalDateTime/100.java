package com.carhistory.entity.converter;

import javax.persistence.AttributeConverter;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Created by jarec on 1.6.14.
 */
public class TimestampConverter implements AttributeConverter<LocalDateTime, Timestamp> {

    @Override
    public Timestamp convertToDatabaseColumn(LocalDateTime attribute) {
        if (attribute == null) {
            return null;
        }

        long millis = attribute.toInstant(ZoneOffset.UTC).toEpochMilli();
        return new Timestamp(millis);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(Timestamp dbData) {
        if (dbData == null) {
            return null;
        }
        long millis = dbData.getTime();
        LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(millis / 1000, (int) ((millis % 1000) * 1000000), ZoneOffset.UTC);
        return localDateTime;
    }
}

