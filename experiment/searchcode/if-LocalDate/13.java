import java.time.LocalDate;

public class ParserToLocalDate {

public static LocalDate parserLocalDate(String date) {

if (date.isEmpty()) {
public static LocalDate parserLocalDate(Date date) {

if (date == null) {
return null;
}

String dateSQL = date.toString();

