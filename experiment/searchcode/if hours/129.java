package hours.controller;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import hours.ui.HoursView;
import hours.viewmodel.DayType;
import hours.viewmodel.HoursResult;
import hours.viewmodel.IHoursModel;
import hours.viewmodel.Time;

public class HoursStatusController {

	private HoursView hoursView;
	private IHoursModel hoursModel;

	public HoursStatusController(IHoursModel hoursModel, HoursView hoursView) {
		this.hoursModel = hoursModel;
		this.hoursView = hoursView;
	}

	public void showStatus() {
		HoursResult result = hoursModel.query();

		List<String> lines = new ArrayList<>();
		String totalHours = String.format("Total required work hours: %d", result.getTotalRequiredHours());
		lines.add(totalHours);

		String currentRequired = String.format("Current required work hours: %d", result.getCurrentRequiredHours());
		lines.add(currentRequired);

		String workHours = String.format("Work hours: %02d:%02d", result.getWorkHours(), result.getWorkMinutes());
		lines.add(workHours);

		// balance - without today
		Time required = new Time(result.getCurrentRequiredHours(), 0);
		Time worked = new Time(result.getWorkHours(), result.getWorkMinutes());
		Time diff = worked.getTimeDiff(required);
		String balance = diff.isNegative() ? "minus" : "extra";
		String hoursDiff = String.format("You have %s %s hours", balance, diff.toString());
		lines.add(hoursDiff);

		for (DayType type : EnumSet.complementOf(EnumSet.of(DayType.Work))) {
			if (type.equals(DayType.Work)) {
				continue;
			}

			String typeStatus = String.format("%s days: %d", type.toString(), hoursModel.getNumberOfDays(type));
			lines.add(typeStatus);

		}

		hoursView.setStatusLines(lines);
	}

	// private String getLineForTime(String period, Time time) {
	// String hoursDiff;
	// if (time.isNegative()) {
	// hoursDiff = String.format("%s, you have minus %s hours", period,
	// time.toString());
	// } else {
	// hoursDiff = String.format("%s, you have extra %s hours", period,
	// time.toString());
	// }
	// return hoursDiff;
	// }

}

