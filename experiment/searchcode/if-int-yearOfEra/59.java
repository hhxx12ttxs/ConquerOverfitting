public MinguoDate date(Era era, int yearOfEra, int month, int dayOfMonth) {
return date(prolepticYear(era, yearOfEra), month, dayOfMonth);
public MinguoDate dateYearDay(Era era, int yearOfEra, int dayOfYear) {
return dateYearDay(prolepticYear(era, yearOfEra), dayOfYear);

