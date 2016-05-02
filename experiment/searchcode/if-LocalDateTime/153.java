package kr.steelheart.site.core;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;


@Converter(autoApply = true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, Date> {

	@Override
	public Date convertToDatabaseColumn(final LocalDateTime value) {
		if (value == null) {
			return null;
		}

		Instant instant = value.atZone(ZoneId.systemDefault()).toInstant();
		Date timestamp = Date.from(instant);

		return timestamp;
	}


	@Override
	public LocalDateTime convertToEntityAttribute(final Date value) {
		if (value == null) {
			return null;
		}

		Instant instant = Instant.ofEpochMilli(value.getTime());
		return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
	}

}

