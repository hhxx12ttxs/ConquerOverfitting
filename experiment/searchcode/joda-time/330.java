package wisematches.server.web.i18n;

import wisematches.personality.Language;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public final class TimeDeclension {
	public static final TimeDeclension RUSSIAN = new TimeDeclension(
			"???? ?????????",
			new String[]{"?", "????", "???", "????"},
			new String[]{"?", "???", "????", "?????"},
			new String[]{"?", "??????", "??????", "?????"});

	public static final TimeDeclension ENGLISH = new TimeDeclension(
			"a moment",
			new String[]{"d", "day", "days", "days"},
			new String[]{"h", "hour", "hours", "hours"},
			new String[]{"m", "minute", "minutes", "minutes"}
	);

	private String moment;
	private final String[] DAYS;
	private final String[] HOURS;
	private final String[] MINUTES;

	private TimeDeclension(String moment, String[] DAYS, String[] HOURS, String[] MINUTES) {
		this.moment = moment;
		this.DAYS = DAYS;
		this.HOURS = HOURS;
		this.MINUTES = MINUTES;
	}

	public String days() {
		return DAYS[0];
	}

	public String hours() {
		return HOURS[0];
	}

	public String minutes() {
		return MINUTES[0];
	}

	public String days(int days) {
		return DAYS[index(days)];
	}

	public String hours(int hours) {
		return HOURS[index(hours)];
	}

	public String minutes(int minutes) {
		return MINUTES[index(minutes)];
	}

	private static int index(int value) {
		int v = value % 100;
		if (v > 20) {
			v %= 10;
		}
		if (v == 1) {
			return 1;
		} else if (v > 1 && v < 5) {
			return 2;
		}
		return 3;
	}

	public static TimeDeclension declension(String language) {
		return declension(Language.byCode(language));
	}

	public static TimeDeclension declension(Language l) {
		switch (l) {
			case RU:
				return RUSSIAN;
		}
		return ENGLISH;
	}

	public String momentAgo() {
		return moment;
	}
}

