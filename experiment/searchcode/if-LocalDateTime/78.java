import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.ZoneId;
return convertToTimestamp((LocalDateTime) value);
}

@Override
public LocalDateTime toBeanType(Object value) {
if (value instanceof LocalDateTime) return (LocalDateTime) value;

