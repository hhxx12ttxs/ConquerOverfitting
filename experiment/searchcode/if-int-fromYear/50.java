public class RangeDateVisitor implements FlightVisitor {
private int fromYear;
private int fromMonth;
private int fromDay;
private int beforeYear;
public RangeDateVisitor(int fromYear, int fromMonth, int fromDay,
int beforeYear, int beforeMonth, int beforeDay) {

