import org.joda.time.LocalDateTime;

public class MonthRange {

private LocalDateTime start;

private LocalDateTime end;

public MonthRange(LocalDateTime start, LocalDateTime end) {
Validate.notNull(start);

