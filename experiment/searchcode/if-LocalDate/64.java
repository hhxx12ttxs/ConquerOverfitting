if (o == null || getClass() != o.getClass()) { return false; }

LocalDate localDate = (LocalDate) o;

if (day != localDate.day) { return false; }
if (month != localDate.month) { return false; }
if (year != localDate.year) { return false; }

