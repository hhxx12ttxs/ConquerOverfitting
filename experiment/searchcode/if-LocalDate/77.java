import java.util.NoSuchElementException;

import org.joda.time.LocalDate;

public class LocalDateRange implements Iterable<LocalDate> {
return current != null;
}

public LocalDate next() {
if (current == null) {
throw new NoSuchElementException();

