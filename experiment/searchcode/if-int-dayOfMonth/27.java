public enum Criteria {
EQUALS,NOT_EQUALS,BEFORE,AFTER,IN;
}
private int dayOfMonth;
private Criteria criterium;
Calendar c = Calendar.getInstance();
c.setTime(container.getDate());
if (c.get(Calendar.DAY_OF_MONTH) == dayOfMonth)
return true;

