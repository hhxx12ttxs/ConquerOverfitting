import org.dozer.Mapper;
import org.joda.time.LocalDate;
import org.springframework.stereotype.Component;
private LocalDate convertLocalDate(LocalDate source) {
LocalDate result = null;

if (source != null) {
result = new LocalDate(source);

