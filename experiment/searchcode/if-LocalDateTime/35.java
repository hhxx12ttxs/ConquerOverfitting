package be.swsb.fiazard.util.representation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public static String toString(LocalDateTime localDateTime) {
if (localDateTime == null) {
return null;
}
return localDateTime.format(FORMATTER);
}
}

