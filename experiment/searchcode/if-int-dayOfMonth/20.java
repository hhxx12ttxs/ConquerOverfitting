public class MonthlyScheduled extends Scheduled
{

String dayOfMonth;

public MonthlyScheduled(String minute, String hour, String meridiem, String dayOfMonth, String description) throws InvalidInputException
private void validateInput(String dayOfMonth) throws InvalidInputException
{
int dayOfMonthInt = Integer.valueOf(dayOfMonth);
if (dayOfMonthInt < 1 || dayOfMonthInt > 28)

