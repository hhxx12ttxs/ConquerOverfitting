package hack;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MensaDataTest {
	// no instances
	private MensaDataTest() {

	}

	private static final String MENSA_URL = "http://www.studentenwerk-mannheim.de/mensa/wo_hs.normal.php";
	/**
	 * Rough guess. Used for performance in string-building.
	 */
	private static final int NUM_MENSA_WEBSITE_CHARS = 10000;
	public static final int NUM_MENSADAYS = 5;
	private static final int NUM_MENUES = 6;
	private static final String SAVE_PATH = System.getProperty("user.dir")
			+ File.separator + "cached_sites" + File.separator;
	/**
	 * Absolute pathname to a cache file, for usage in
	 * {@link String#format(String, Object...)}.
	 */
	private static final String CACHE_PATH_FORMAT = SAVE_PATH + "%d_%d.ser";
	private static final Pattern CACHE_FILE_PATTERN = Pattern
			.compile("\\d{4,}_\\d+?\\.ser");
	/**
	 * Signalises whether the save path is existent or not.
	 */
	private static final boolean CACHING_POSSIBLE;
	static {
		File saveDir = new File(SAVE_PATH);
		CACHING_POSSIBLE = saveDir.exists() || saveDir.mkdir();
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		System.out.println(getCacheSize());
		Calendar c = Calendar.getInstance();
		System.out.println(isCached(c.get(Calendar.WEEK_OF_YEAR),
				c.get(Calendar.YEAR)));
		MensaWeek week = getCurrentWeek();
		for (MensaDay d : week) {
			if (!d.isMensaOpen()) {
				System.out.println(d.getDate() + "\nclosed");
			} else {
				MensaOpenDay dop = (MensaOpenDay) d;
				System.out.println(dop.getDate());
				System.out.println(dop.getVegetarian());
				System.out.println(dop.getMenu1());
				System.out.println(dop.getMenu2());
				System.out.println(dop.getDessert());
				System.out.println(dop.getSpecial());
				System.out.println(dop.getWok());
			}
		}
		cache(week);
		week = getNextWeek(week);
		cache(week);
		week = getNextWeek(week);
		cache(week);
		week = getNextWeek(week);
		cache(week);

		// deleteOldCaches();

	}

	/**
	 * Gets the next MensaWeek relative to the given MensaWeek.
	 * 
	 * @param currentWeek
	 *            Relative current week.
	 * @return The next week relative to currentWeek.
	 * @throws IOException
	 *             if error occurs. Contains detailed error information in
	 *             german.
	 */
	public static MensaWeek getNextWeek(MensaWeek currentWeek)
			throws IOException {
		return getPlusWeek(currentWeek.getWeekOfYear(), 1);
	}

	/**
	 * Gets the previous MensaWeek relative to the given MensaWeek.
	 * 
	 * @param currentWeek
	 *            Relative current week.
	 * @return The previous week relative to currentWeek.
	 * @throws IOException
	 *             if error occurs. Contains detailed error information in
	 *             german.
	 */
	public static MensaWeek getPreviousWeek(MensaWeek currentWeek)
			throws IOException {
		return getPlusWeek(currentWeek.getWeekOfYear(), -1);
	}

	/**
	 * Gets the current MensaWeek, as specified by the internal
	 * Android-Calendar.
	 * 
	 * @return The current MensaWeek.
	 * @throws IOException
	 *             if error occurs. Contains detailed error information in
	 *             german.
	 */
	public static MensaWeek getCurrentWeek() throws IOException {
		Calendar c = Calendar.getInstance();
		return getPlusWeek(c.get(Calendar.WEEK_OF_YEAR), 0);
	}

	/**
	 * Helper Method. Helps avoiding redundancy.
	 * 
	 * @throws IOException
	 *             if error occurs. Contains detailed error information in
	 *             german.
	 */
	private static MensaWeek getPlusWeek(int weekOfYear, int weeks)
			throws IOException {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.WEEK_OF_YEAR, weekOfYear);
		c.add(Calendar.WEEK_OF_YEAR, weeks);
		return parseMensaWeek(c.get(Calendar.WEEK_OF_YEAR),
				c.get(Calendar.YEAR));
	}

	/**
	 * Parses the requested week into a MensaWeek. If already cached, the cached
	 * MensaWeek will be returned.
	 * 
	 * @param weekOfYear
	 *            Requested week of the year.
	 * @param year
	 *            Requested year.
	 * @return The MensaWeek with all information.
	 * @throws IOException
	 *             if error occurs. Contains detailed error information in
	 *             german.
	 */
	private static MensaWeek parseMensaWeek(int weekOfYear, int year)
			throws IOException {
		if (isCached(weekOfYear, year)) {
			return getCached(weekOfYear, year);
		}
		StringBuilder wholeSite = readWebsite(weekOfYear, year);

		// date of monday
		Calendar calendar = Calendar.getInstance();
		calendar.setLenient(false);
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.WEEK_OF_YEAR, weekOfYear);
		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

		MensaDay[] days = new MensaDay[NUM_MENSADAYS]; // [0] = monday etc.

		// find table with menu information
		int idx = wholeSite.indexOf("'WoTab_hs'>") + "WoDate_hs'>".length();
		// in <td with day-info
		idx = wholeSite.indexOf("<td", idx) + "<td".length();

		for (int day = 0; day < days.length; day++) {
			// skip td with day-info
			idx = wholeSite.indexOf("<td", idx);
			// end of row containing menu-information
			int endIdx = wholeSite.indexOf("</tr>", idx);
			// is only one <td> in this row?
			if (wholeSite.indexOf("<", wholeSite.indexOf("</td>", idx)
					+ "</td>".length()) == endIdx) {
				days[day] = new MensaClosedDay(new SimpleDate(calendar));
			} else { // all menues
				MensaOpenDay.Builder dayBuilder = new MensaOpenDay.Builder();
				dayBuilder.date(new SimpleDate(calendar));
				MensaMenu[] menus = new MensaMenu[NUM_MENUES];
				MensaMenu.Builder menuBuilder = new MensaMenu.Builder();

				// contains all soups, to check if a menu has a soup w/o h4-tags
				List<String> foundSoups = new ArrayList<String>(2);

				for (int menu = 0; menu < menus.length; menu++) {

					// after closing <td .... >
					idx = wholeSite.indexOf(">", idx) + ">".length();
					// first opening tag right behind <td ...... >
					int soupIdx = wholeSite.indexOf("<", idx);

					String tdContent; // menu description
					String soup; // soup description. null, if none
					int endOfTd = wholeSite.indexOf("</td>", idx);

					// soup (in first <h4>) ?
					if (wholeSite.substring(soupIdx, soupIdx + "<h4>".length())
							.equals("<h4>")) {
						// get soup description
						int soupStart = soupIdx + "<h4>".length();
						int soupEnd = wholeSite.indexOf("</h4>", soupStart);

						soup = prettify(wholeSite.substring(soupStart, soupEnd));

						if (!foundSoups.contains(soup)) {
							foundSoups.add(soup);
						}

						// place cursor after the soup-tags
						idx = soupEnd + "</h4>".length();

						tdContent = wholeSite.substring(idx, endOfTd);

					} else { // could still be hidden soup without h4-tags
						tdContent = wholeSite.substring(idx, endOfTd);
						soup = null;

						for (String foundSoup : foundSoups) {
							if (tdContent.contains(foundSoup)) {
								// remove the soup from tdContent
								tdContent = tdContent.replace(foundSoup, "");
								soup = foundSoup;
								tdContent = prettifyAfterSoupRemoval(tdContent);
								break;
							}
						}
					}
					menuBuilder.soupDescription(soup);
					menuBuilder.menuDescription(prettify(tdContent));

					// next <td> is price
					idx = endOfTd + "</td>".length();
					idx = wholeSite.indexOf("<td", idx);

					// after <td......>
					int priceStart = wholeSite.indexOf(">", idx) + ">".length();
					int priceEnd = wholeSite.indexOf("</td>", priceStart);

					menuBuilder.priceInEuro(prettifyPrice(wholeSite.substring(
							priceStart, priceEnd)));

					// add salad
					menuBuilder.hasSalad(menu == 1 || menu == 2
							|| tdContent.toLowerCase().contains("salat"));

					idx = wholeSite.indexOf("<td", priceEnd) + "<td".length();

					menus[menu] = menuBuilder.build();
				}
				assert menus.length == 6;
				days[day] = dayBuilder.vegetarian(menus[0]).menu1(menus[1])
						.menu2(menus[2]).dessert(menus[3]).special(menus[4])
						.wok(menus[5]).build();
			}
			calendar.add(Calendar.DATE, 1);
		}
		return new MensaWeek(days, weekOfYear, year);
	}

	/**
	 * Gets a cached version of this MensaWeek. Assumes, that
	 * {@link #isCached(int, int)} returned {@code true} for these parameters.
	 * 
	 * @param weekOfYear
	 *            the week
	 * @param year
	 *            the year
	 * @return The cached MensaWeek.
	 * @throws IOException
	 *             on reading error.
	 */
	private static MensaWeek getCached(int weekOfYear, int year)
			throws IOException {
		if (!isCached(weekOfYear, year)) {
			throw new AssertionError("week is not in cache");
		}
		MensaWeek week = null;
		ObjectInputStream objIn = null;
		try {
			objIn = new ObjectInputStream(new BufferedInputStream(
					new FileInputStream(String.format(CACHE_PATH_FORMAT, year,
							weekOfYear))));
			week = (MensaWeek) objIn.readObject();
		} catch (IOException e) {
			throw new IOException("Fehler beim Laden des Cache.", e);
		} catch (ClassNotFoundException e) {
			throw new IOException(
					"Fehler beim Laden des Cache: Version zu alt. Eventuell Ordner cached_sites loeschen.",
					e);
		} finally {
			Util.closeConnection(objIn);
		}
		return week;
	}

	/**
	 * Deletes caches that are older/newer than one week relative to the
	 * internal Android-Calendar. Ignores Files that don't match the
	 * cache-scheme.
	 * 
	 * Run this function every time you want to clean up the cache!
	 */
	public static void deleteOldCaches() {
		// calculate difference in weeks
		Calendar now = Calendar.getInstance();

		int currWeek = now.get(Calendar.WEEK_OF_YEAR);
		int currYear = now.get(Calendar.YEAR);

		File[] cachedFiles = new File(SAVE_PATH).listFiles();
		for (File cachedFile : cachedFiles) {
			String cacheName = cachedFile.getName();
			if (!CACHE_FILE_PATTERN.matcher(cacheName).matches()) {
				continue;
			}
			final int cacheWeek = Integer.parseInt(cacheName.substring(
					cacheName.indexOf('_') + 1, cacheName.indexOf('.')));
			final int weekDiff = Math.abs(currWeek - cacheWeek);
			final int cacheYear = Integer.parseInt(cacheName.substring(0,
					cacheName.indexOf('_')));
			final int yearDiff = Math.abs(currYear - cacheYear);

			final boolean delete;
			if (yearDiff == 0) {
				delete = weekDiff > 1;
			} else if (yearDiff > 1) {
				delete = true;
			} else { // yearDiff is 1
				/*
				 * real weekDiff is 1, but: last week of year(e.g. 52) - first
				 * week of next year(1) (and vice versa) != 1.
				 */
				int possibleDiff;

				if (currWeek > cacheWeek) { // cached week was last year
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.YEAR, cacheYear);
					possibleDiff = cal.getMaximum(Calendar.WEEK_OF_YEAR) - 1;
				} else { // cached week lies in future
					possibleDiff = now.getMaximum(Calendar.WEEK_OF_YEAR) - 1;
				}
				delete = weekDiff != possibleDiff;
			}
			if (delete) {
				if (!cachedFile.delete()) {
					throw new AssertionError("cannot delete caches");
				}
			}
		}
	}

	/**
	 * Helper Method. Tests if week is in cache.
	 * 
	 * @param weekOfYear
	 * @param year
	 * @return {@code true} if in cache, {@code false} otherwise.
	 */
	private static boolean isCached(int weekOfYear, int year) {
		if (!CACHING_POSSIBLE) {
			return false;
		}
		File f = new File(String.format(CACHE_PATH_FORMAT, year, weekOfYear));
		return f.isFile();
	}

	/**
	 * Returns the number of cache-files in use. May be used to
	 * {@link #deleteOldCaches()}, if number gets too big.
	 * 
	 * @return Number of cache-files.
	 */
	public static int getCacheSize() {
		File[] filesInDir = new File(SAVE_PATH).listFiles();
		int sum = 0;
		for (File f : filesInDir) {
			final String fileName = f.getName();
			if (CACHE_FILE_PATTERN.matcher(fileName).matches()) {
				sum++;
			}
		}
		return sum;
	}

	/**
	 * Reads in the mensa-page of the current week from the internet.
	 * 
	 * @throws IOException
	 *             If reading error occurs. Contains detailed error-information.
	 */
	private static StringBuilder readWebsite(int weekOfYear, int year)
			throws IOException {
		URL mensaURL;
		String weekURL = MENSA_URL + "?+kw=" + weekOfYear;
		try {
			mensaURL = new URL(weekURL);
		} catch (MalformedURLException e) {
			throw new AssertionError("URL: " + weekURL + " malformed.");
		}
		BufferedReader in = null;
		StringBuilder wholeSite;
		try {
			// assumed encoding: UTF-8. no feasible way to get this
			in = new BufferedReader(new InputStreamReader(
					mensaURL.openStream(), "UTF-8"));

			wholeSite = new StringBuilder(NUM_MENSA_WEBSITE_CHARS);

			for (String s = in.readLine(); s != null; s = in.readLine()) {
				wholeSite.append(s);
			}
		} catch (IOException e) {
			throw new IOException(
					"Verbindung zu "
							+ MENSA_URL
							+ " fehlgeschlagen. Internetverbindung ist eventuell nicht eingeschaltet, oder die URL existiert (noch) nicht.",
					e);
		} finally {
			Util.closeConnection(in);
		}
		if (wholeSite.lastIndexOf("Freigabebereichs") != -1) {
			throw new IOException(
					"Die geforderte Kalenderwoche ist auรerhalb des Freigabebereichs!");
		}
		return wholeSite;
	}

	/**
	 * Saves the page to harddisk if not already saved.
	 * 
	 * @param week
	 *            the week to cache.
	 * @throws IOException
	 *             if something goes wrong, contains detailed error message.
	 */
	public static void cache(MensaWeek week) throws IOException {
		if (!CACHING_POSSIBLE) {
			throw new IOException(
					"Caching ist nicht mรถglich. Das Verzeichnis cached_sites konnte womoeglich nicht erstellt werden.");
		}
		File out = new File(String.format(CACHE_PATH_FORMAT, week.getYear(),
				week.getWeekOfYear()));
		if (out.exists()) {
			return;
		}
		ObjectOutputStream objOut = null;
		try {
			objOut = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(out)));
			objOut.writeObject(week);
		} catch (IOException e) {
			throw new IOException(
					"Caching fehlgeschlagen. Android verbietet eventuell den Zugriff.",
					e);
		} finally {
			Util.closeConnection(objOut);
		}
	}

	/**
	 * Tries to remove garbage in front of the menu information.
	 * {@link #prettify(String)} should be called after this.
	 * 
	 * @param s
	 *            String to prettify.
	 * @return The pretty String.
	 */
	private static String prettifyAfterSoupRemoval(String s) {
		// remove non-word chars before the text
		Matcher m = Pattern.compile("\\w").matcher(s);
		if (m.find()) {
			s = s.substring(m.start());
		}
		return s;
	}

	/**
	 * Same as {@link #prettify(String)}, but removes "Portion" from price.
	 * 
	 * @param price
	 *            String to prettify.
	 * @return
	 */
	private static String prettifyPrice(String price) {
		price = prettify(price);
		price = price.replaceAll("Portion", "");
		return price;
	}

	/**
	 * Removes some regular html-tags, too much whitespace etc.
	 * 
	 * @param s
	 *            String to prettify.
	 * @return The pretty String.
	 */
	private static String prettify(String s) {
		s = s.trim();
		s = s.replaceAll("\\s", " ");
		s = s.replaceAll("\\s{2,}", " ");
		s = s.replaceAll("<br/>", "");
		s = s.replaceAll("&nbsp;", "");
		return s;
	}

}
