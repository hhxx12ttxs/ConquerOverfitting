public DaysCondition(boolean days[]) {
this.days = days;
}

public DaysCondition(String setDays) {
if (setDays.contains(&quot;0&quot;)) {
days[0]=true;
}if (setDays.contains(&quot;1&quot;)) {
days[1]=true;

