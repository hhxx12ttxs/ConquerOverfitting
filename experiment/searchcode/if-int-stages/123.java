package org.ambientdynamix.contextplugins.zeo;

import java.util.Date;

public interface IZeoSleepRecord {
	/**
	 * Returns the localized unix timestamp that marks the beginning of a sleep episode.
	 */
	public long getLocalizedStartOfNight();

	/**
	 * Returns the localized Date associated with the sleep episode..
	 */
	public Date getLocalizedStartOfNightDate();

	/**
	 * Returns the unix timestamp that marks the beginning of a sleep episode.
	 */
	public long getStartOfNight();

	/**
	 * Returns the Date associated with the sleep episode..
	 */
	public Date getStartOfNightDate();

	/**
	 * Returns the timestamp that marks the end of the record.
	 */
	public long getEndOfNight();

	/**
	 * Returns the Date that marks the end of the record.
	 */
	public Date getEndOfNightDate();

	/**
	 * Returns the timezone string in the form of "America/New_York" that represents the timezone Android device was in
	 * when record was recorded.
	 */
	public String getTimeZone();

	/**
	 * Returns the metric by which Zeo determines how well the user slept.
	 */
	public int getZqScore();

	/**
	 * Returns the number of times user awoke throughout the night.
	 */
	public int getAwakenings();

	/**
	 * Returns the number of 30 second sleep epochs that the user was in deep sleep.
	 */
	public int getTimeInDeep();

	/**
	 * Returns the number of 30 second sleep epochs that the user was in light sleep.
	 */
	public int getTimeInLight();

	/**
	 * Returns the number of 30 second sleep epochs that the user was in REM sleep.
	 */
	public int getTimeInRem();

	/**
	 * Returns the number of 30 second epochs that the user was awake.
	 */
	public int getTimeInWake();

	/**
	 * Returns the
	 */
	public int getTimeToWake();

	/**
	 * Returns the number of 30 second sleep epochs before sleep onset.
	 */
	public int getTimeToZ();

	/**
	 * Returns the originating source for this record one of: 0 = data source is primary (a headband); 1 = data source
	 * is remote server (myzeo.com).
	 */
	public int getSource();

	/**
	 * Returns true if the originating source for this record is remote (server); false, otherwise.
	 */
	public boolean isSourceRemote();

	/**
	 * Returns true if the originating source for this record is local (headband); false, otherwise.
	 */
	public boolean isSourceLocal();

	/**
	 * Returns the reason that this sleep record concluded. The possible values are: 0 = Complete record; 1 = Record is
	 * still active; 2 = Headband battery died; 3 = Headband disconnected; 4 = Service was killed on Android device
	 */
	public int getEndReason();

	/**
	 * Returns the byte array containing sleep stages where each stage corresponds to 30 seconds of sleep.
	 */
	public byte[] getBaseHypnogram();

	/**
	 * Returns the string array containing sleep stages where each stage corresponds to 30 seconds of sleep.
	 */
	public String[] getBaseHypnogramStrings();

	/**
	 * Returns the byte array containing sleep stages where each stage corresponds to 5 minutes of sleep.
	 */
	public byte[] getDisplayHypnogram();

	/**
	 * Returns the string array containing sleep stages where each stage corresponds to 5 minutes of sleep.
	 */
	public String[] getDisplayHypnogramStrings();
}
