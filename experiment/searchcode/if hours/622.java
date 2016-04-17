package workers;

import java.time.LocalDate;

public class PermanentEmployee extends HourlyEmployee {

	public PermanentEmployee(String familyName, String givenName, LocalDate startingDate, double hourlyRate,
			int contractedHours) {
		super(familyName, givenName, startingDate, hourlyRate, contractedHours, "permanent");
	}

	@Override
	public double calculateMonthlyWage() {
		if (getWorkedHours() < getContractedHours()) {
			return getWorkedHours() * getHourlyRate();
		}
		return (getContractedHours() + (getWorkedHours() - getContractedHours()) * 1.5) * getHourlyRate();
	}

}

