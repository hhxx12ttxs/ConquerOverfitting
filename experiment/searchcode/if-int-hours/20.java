package wisematches.server.web.i18n;

import org.joda.time.DateMidnight;
import org.joda.time.DateTime;
import org.joda.time.Years;
import org.springframework.context.MessageSource;
import wisematches.personality.Language;
import wisematches.personality.player.Player;
import wisematches.personality.player.computer.ComputerPlayer;
import wisematches.playground.GameBoard;
import wisematches.playground.criteria.CriterionViolation;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class GameMessageSource {
	private MessageSource messageSource;

	private static final Map<Locale, DateFormat> DATE_FORMATTER = new ConcurrentHashMap<Locale, DateFormat>();
	private static final Map<Locale, DateFormat> TIME_FORMATTER = new ConcurrentHashMap<Locale, DateFormat>();

	static {
		for (Language lang : Language.values()) {
			DATE_FORMATTER.put(lang.locale(), DateFormat.getDateInstance(DateFormat.LONG, lang.locale()));
			TIME_FORMATTER.put(lang.locale(), DateFormat.getTimeInstance(DateFormat.SHORT, lang.locale()));
		}
	}

	public GameMessageSource() {
	}

	public String getPlayerNick(Player p, Locale locale) {
		if (p instanceof ComputerPlayer) {
			return getRobotNick((ComputerPlayer) p, locale);
		}
		return p.getNickname();
	}

	public String getRobotNick(ComputerPlayer player, Locale locale) {
		return messageSource.getMessage("game.player." + player.getNickname(), null, locale);
	}

	public String getMessage(String code, Locale locale) {
		return getMessage(code, locale, null);
	}

	public String getMessage(String code, Locale locale, Object a1) {
		return getMessage(code, locale, new Object[]{a1});
	}

	public String getMessage(String code, Locale locale, Object a1, Object a2) {
		return getMessage(code, locale, new Object[]{a1, a2});
	}

	public String getMessage(String code, Locale locale, Object a1, Object a2, Object a3) {
		return getMessage(code, locale, new Object[]{a1, a2, a3});
	}

	public String getMessage(String code, Locale locale, Object[] args) {
		return messageSource.getMessage(code, args, locale);
	}

	public String formatViolation(CriterionViolation violation, Locale locale, boolean isShort) {
		return getMessage("game.join.err." + violation.getCode() + (isShort ? ".label" : ".description"), locale, violation.getExpected(), violation.getReceived());
	}

	public String formatDate(Date date, Locale locale) {
		return DATE_FORMATTER.get(locale).format(date);
	}

	public String formatTime(Date date, Locale locale) {
		return TIME_FORMATTER.get(locale).format(date);
	}

	public String formatSpentTime(GameBoard board, Locale locale) {
		return formatTimeMinutes(getSpentMinutes(board), locale);
	}

	public String formatElapsedTime(Date date, Locale locale) {
		final long startTime = date.getTime();
		final long endTime = System.currentTimeMillis();
		return formatTimeMinutes((endTime - startTime) / 1000 / 60, locale);
	}

	public String formatRemainedTime(GameBoard board, Locale locale) {
		return formatTimeMinutes(getRemainedMinutes(board), locale);
	}

	public int getAge(Date date) {
		DateMidnight birthdate = new DateMidnight(date);
		DateTime now = new DateTime();
		Years age = Years.yearsBetween(birthdate, now);
		return age.getYears();
	}

	public long getTimeMillis(Date date) {
		return date.getTime();
	}

	public long getSpentMinutes(GameBoard board) {
		final long startTime = board.getStartedTime().getTime();
		final long endTime;
		if (board.isGameActive()) {
			endTime = System.currentTimeMillis();
		} else {
			endTime = board.getFinishedTime().getTime();
		}
		return (endTime - startTime) / 1000 / 60;
	}

	public long getRemainedMinutes(GameBoard board) {
		final int daysPerMove = board.getGameSettings().getDaysPerMove();
		final long elapsedTime = System.currentTimeMillis() - board.getLastMoveTime().getTime();

		final long minutesPerMove = daysPerMove * 24 * 60;
		final long minutesElapsed = (elapsedTime / 1000 / 60);
		return minutesPerMove - minutesElapsed;
	}

	public String formatTimeMillis(long time, Locale locale) {
		return formatTimeMinutes(time / 60 / 1000, locale);
	}

	public String formatTimeMinutes(long time, Locale locale) {
		final int days = (int) (time / 60 / 24);
		final int hours = (int) ((time - (days * 24 * 60)) / 60);
		final int minutes = (int) (time % 60);

		final Language language = Language.byLocale(locale);
		if (days == 0 && hours == 0 && minutes == 0) {
			return language.getMomentAgoLabel();
		}

		if (hours <= 0 && minutes <= 0) {
			return days + " " + language.getDaysLabel(days);
		}
		if (days <= 0 && minutes <= 0) {
			return hours + " " + language.getHoursLabel(hours);
		}
		if (days <= 0 && hours <= 0) {
			return minutes + " " + language.getMinutesLabel(minutes);
		}

		final StringBuilder b = new StringBuilder();
		if (days > 0) {
			b.append(days).append(language.getDaysCode()).append(" ");
		}
		if (hours > 0) {
			b.append(hours).append(language.getHoursCode()).append(" ");
		}
		if ((days == 0 || hours == 0) && (minutes > 0)) {
			b.append(minutes).append(language.getMinutesCode()).append(" ");
		}
		return b.toString();
	}

	public String formatDays(int days, Locale locale) {
		return Language.byLocale(locale).getDaysLabel(days);
	}

	public String formatHours(int hours, Locale locale) {
		return Language.byLocale(locale).getHoursLabel(hours);
	}

	public String formatMinutes(int minutes, Locale locale) {
		return Language.byLocale(locale).getMinutesLabel(minutes);
	}

	public String getWordEnding(int quantity, Locale locale) {
		return Language.byLocale(locale).getWordEnding(quantity);
	}

	public String getNumeralEnding(int value, Locale locale) {
		return Language.byLocale(locale).getNumeralEnding(value);
	}

	/**
	 * Taken from here: http://stackoverflow.com/questions/1224996/java-convert-string-to-html-string
	 *
	 * @param string
	 * @return
	 */
	public static String stringToHTMLString(String string) {
		final StringBuilder sb = new StringBuilder(string.length());
		// true if last char was blank
		boolean lastWasBlankChar = false;
		int len = string.length();
		char c;

		for (int i = 0; i < len; i++) {
			c = string.charAt(i);
			if (c == ' ') {
				// blank gets extra work,
				// this solves the problem you get if you replace all
				// blanks with &nbsp;, if you do that you loss
				// word breaking
				if (lastWasBlankChar) {
					lastWasBlankChar = false;
					sb.append("&nbsp;");
				} else {
					lastWasBlankChar = true;
					sb.append(' ');
				}
			} else {
				lastWasBlankChar = false;
				//
				// HTML Special Chars
				switch (c) {
					case '"':
						sb.append("&quot;");
						break;
					case '&':
						sb.append("&amp;");
						break;
					case '<':
						sb.append("&lt;");
						break;
					case '>':
						sb.append("&gt;");
						break;
					case '\n':
						// Handle Newline
						sb.append("<br>");
						break;
					default:
						int ci = 0xffff & c;
						if (ci < 160)
							// nothing special only 7 Bit
							sb.append(c);
						else {
							// Not 7 Bit use the unicode system
							sb.append("&#");
							sb.append(String.valueOf(ci));
							sb.append(';');
						}
						break;
				}
			}
		}
		return sb.toString();
	}

	public void setMessageSource(MessageSource messageSource) {
		this.messageSource = messageSource;
	}
}

