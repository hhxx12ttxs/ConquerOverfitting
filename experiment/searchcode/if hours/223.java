package domain.hours;

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;

import java.util.List;
import java.util.Map;
import java.util.function.ToIntFunction;

public class Timesheet {
    private static final String PAYROLL_TEMPLATE = " ordinary hours worked: {0} * {1} = {2} hours overtime worked: {3} * {4} = {5} Total: {6}";
    private static final int HOURLY_RATE = 10;
    private static final int OVERTIME_RATE = 15;
    private static final String CURRENCY = " Euro";
    private static final String HOURLY_WAGE = HOURLY_RATE + CURRENCY;
    private static final String OVERTIME_RENUMERATION = OVERTIME_RATE
	    + CURRENCY;
    private Map<Long, Integer> regularHoursPerSerialNumber;
    private Map<Long, Integer> overtimeHoursPerSerialNumber;

    public Timesheet(List<WorkingHours> hours) {
	regularHoursPerSerialNumber = calculateHours(hours,
		WorkingHours::getRegularHours);
	overtimeHoursPerSerialNumber = calculateHours(hours,
		WorkingHours::getOvertimeHours);
    }

    private Map<Long, Integer> calculateHours(List<WorkingHours> hours,
	    ToIntFunction<? super WorkingHours> getHoursFunction) {
	return hours.stream()//
		.collect(//
			groupingBy(WorkingHours::getSerialNumber,//
				summingInt(getHoursFunction)));
    }

    public int getRegularHoursFor(long serialNumber) {
	return getHours(serialNumber, regularHoursPerSerialNumber);
    }

    private int getHours(long serialNumber, Map<Long, Integer> timesheet) {
	Integer hours = timesheet.get(serialNumber);

	if (hours == null)
	    return 0;

	return hours;
    }

    public int getOvertimeHoursFor(long serialNumber) {
	return getHours(serialNumber, overtimeHoursPerSerialNumber);
    }

    public String getOutput(long serialNumber) {
	int regularHours = getRegularHoursFor(serialNumber);
	int overtimeHours = getOvertimeHoursFor(serialNumber);

	return format(PAYROLL_TEMPLATE, regularHours, HOURLY_WAGE,
		getTotalStandardPay(regularHours), overtimeHours,
		OVERTIME_RENUMERATION, getTotalOvertimePay(overtimeHours),
		getTotalPay(regularHours, overtimeHours));
    }

    private static String getTotalStandardPay(int hours) {
	return hours * HOURLY_RATE + CURRENCY;
    }

    private static String getTotalOvertimePay(int overtimeHours) {
	return overtimeHours * OVERTIME_RATE + CURRENCY;
    }

    private static String getTotalPay(int regularHours, int overtimeHours) {
	return (regularHours * HOURLY_RATE + overtimeHours * OVERTIME_RATE)
		+ CURRENCY;
    }

}

