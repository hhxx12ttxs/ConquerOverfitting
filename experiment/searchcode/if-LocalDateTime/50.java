import java.time.LocalDateTime;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
public Timestamp convertToDatabaseColumn(LocalDateTime localDateTime) {
if (localDateTime != null) {
return Timestamp.valueOf(localDateTime);

