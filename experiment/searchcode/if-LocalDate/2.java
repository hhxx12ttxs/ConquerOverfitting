public static Double religious(LocalDate date){
if(date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) return 1.0;
public static Double state(LocalDate localDate){
if(STATE_HOLIDAYS.containsKey(localDate)) return 1.0;

