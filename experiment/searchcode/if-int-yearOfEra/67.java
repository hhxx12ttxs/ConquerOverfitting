int yearOfEra = entity.getInt(YEAR_OF_ERA);

if (yearOfEra == Integer.MIN_VALUE) {
entity.with(ValidationElement.ERROR_MESSAGE, &quot;Missing Minguo year.&quot;);
return null;
}

int prolepticYear = toProlepticYear(era, yearOfEra);

if (entity.contains(MONTH_OF_YEAR)) {

