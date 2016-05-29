public static int weekyear(LocalDate day) {
if (day == null) throw new NullPointerException();
return day.weekOfWeekyear().get();
public static boolean isSameWeekyear(LocalDate lDay, LocalDate rDay) {
return weekyear(lDay) == weekyear(rDay);
}

public static int month(LocalDate day) {
if (day == null) throw new NullPointerException();

