/* Copyright (c) 2008-2010, developers of the Ascension Log Visualizer
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.googlecode.logVisualizer.util.textualLogs;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;

import com.googlecode.logVisualizer.Settings;
import com.googlecode.logVisualizer.logData.*;
import com.googlecode.logVisualizer.logData.LogDataHolder.StatClass;
import com.googlecode.logVisualizer.logData.consumables.Consumable;
import com.googlecode.logVisualizer.logData.consumables.Consumable.ConsumableVersion;
import com.googlecode.logVisualizer.logData.logSummary.AreaStatgains;
import com.googlecode.logVisualizer.logData.logSummary.LevelData;
import com.googlecode.logVisualizer.logData.turn.SingleTurn;
import com.googlecode.logVisualizer.logData.turn.TurnInterval;
import com.googlecode.logVisualizer.logData.turn.SingleTurn.TurnVersion;
import com.googlecode.logVisualizer.logData.turn.TurnInterval.FreeRunaways;
import com.googlecode.logVisualizer.logData.turn.turnAction.DayChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.FamiliarChange;
import com.googlecode.logVisualizer.logData.turn.turnAction.PlayerSnapshot;
import com.googlecode.logVisualizer.logData.turn.turnAction.Pull;
import com.googlecode.logVisualizer.parser.UsefulPatterns;
import com.googlecode.logVisualizer.util.DataCounter;
import com.googlecode.logVisualizer.util.DataNumberPair;
import com.googlecode.logVisualizer.util.DataTablesHandler;
import com.googlecode.logVisualizer.util.Pair;

/**
 * This utility class creates a parsed ascension log from a
 * {@link LogDataHolder}. The format of the parsed log is similar to the one
 * which the AFH parser uses.
 * <p>
 * Note that this class should only be used to create parsed ascension logs from
 * mafia logs. Using pre-parsed logs as the basis will not work, because those
 * do not contain enough data.
 * <p>
 * All methods in this class throw a {@link NullPointerException} if a null
 * object reference is passed in any parameter.
 */
public final class TextLogCreator {
    private static final Map<String, String> TEXT_LOG_ADDITIONS_MAP = new HashMap<String, String>();

    private static final Map<String, String> HTML_LOG_ADDITIONS_MAP = new HashMap<String, String>();

    private static final Map<String, String> BBCODE_LOG_ADDITIONS_MAP = new HashMap<String, String>();

    private static final String NEW_LINE = System.getProperty("line.separator");

    private static final String COMMA = ", ";

    private static final String OPENING_TURN_BRACKET = " [";

    private static final String CLOSING_TURN_BRACKET = "] ";

    private static final String ITEM_PREFIX = "     +>";

    private static final String ITEM_MIDDLE_STRING = "Got ";

    private static final String CONSUMABLE_PREFIX = "     o> ";

    private static final String PULL_PREFIX = "     #> Turn";

    private static final String LEVEL_CHANGE_PREFIX = "     => Level ";

    private static final String HUNTED_COMBAT_PREFIX = "     *>";

    private static final String HUNTED_COMBAT_MIDDLE_STRING = "Started hunting ";

    private static final String DISINTEGRATED_COMBAT_PREFIX = "     }>";

    private static final String DISINTEGRATED_COMBAT_MIDDLE_STRING = "Disintegrated ";

    private static final String FAMILIAR_CHANGE_PREFIX = "     -> Turn";

    private static final String SEMIRARE_PREFIX = "     #>";

    private static final String SEMIRARE_MIDDLE_STRING = "Semirare: ";

    private static final String BAD_MOON_PREFIX = "     %>";

    private static final String BAD_MOON_MIDDLE_STRING = "Badmoon: ";

    private static final String FREE_RUNAWAYS_PREFIX = "     &> ";

    private static final String ADVENTURES_LEFT_STRING = "Adventure count at day start: ";

    private static final String CURRENT_MEAT_STRING = "Current meat: ";

    private static final DayChange NO_DAY_CHANGE = new DayChange(Integer.MAX_VALUE,
                                                                 Integer.MAX_VALUE);

    static {
        TEXT_LOG_ADDITIONS_MAP.put("notesStart", "[/code]");
        TEXT_LOG_ADDITIONS_MAP.put("notesEnd", "[code]");

        HTML_LOG_ADDITIONS_MAP.put("logHeaderStart", "<i>");
        HTML_LOG_ADDITIONS_MAP.put("logHeaderEnd", "</i>");
        HTML_LOG_ADDITIONS_MAP.put("turnStart", "<b>");
        HTML_LOG_ADDITIONS_MAP.put("turnEnd", "</b>");
        HTML_LOG_ADDITIONS_MAP.put("dayChangeLineStart", "<b>");
        HTML_LOG_ADDITIONS_MAP.put("dayChangeLineEnd", "</b>");
        HTML_LOG_ADDITIONS_MAP.put("statgainStart", "<font color=#808080>");
        HTML_LOG_ADDITIONS_MAP.put("statgainEnd", "</font>");
        HTML_LOG_ADDITIONS_MAP.put("pullStart", "<font color=#008B8B>");
        HTML_LOG_ADDITIONS_MAP.put("pullEnd", "</font>");
        HTML_LOG_ADDITIONS_MAP.put("consumableStart", "<font color=#009933><b>");
        HTML_LOG_ADDITIONS_MAP.put("consumableEnd", "</b></font>");
        HTML_LOG_ADDITIONS_MAP.put("itemStart", "<font color=#0000CD>");
        HTML_LOG_ADDITIONS_MAP.put("itemEnd", "</font>");
        HTML_LOG_ADDITIONS_MAP.put("familiarStart", "<font color=#B03030>");
        HTML_LOG_ADDITIONS_MAP.put("familiarEnd", "</font>");
        HTML_LOG_ADDITIONS_MAP.put("huntedStart", "<font color=#006400><b>");
        HTML_LOG_ADDITIONS_MAP.put("huntedEnd", "</b></font>");
        HTML_LOG_ADDITIONS_MAP.put("yellowRayStart", "<font color=#B8860B><b>");
        HTML_LOG_ADDITIONS_MAP.put("yellowRayEnd", "</b></font>");
        HTML_LOG_ADDITIONS_MAP.put("specialEncounterStart", "<font color=#8B008B><b>");
        HTML_LOG_ADDITIONS_MAP.put("specialEncounterEnd", "</b></font>");
        HTML_LOG_ADDITIONS_MAP.put("levelStart", "<font color=#DC143C><b>");
        HTML_LOG_ADDITIONS_MAP.put("levelEnd", "</b></font>");
        HTML_LOG_ADDITIONS_MAP.put("runawayStart", "<font color=#CD853F><b>");
        HTML_LOG_ADDITIONS_MAP.put("runawayEnd", "</b></font>");
        HTML_LOG_ADDITIONS_MAP.put("notesStart", "<br>");
        HTML_LOG_ADDITIONS_MAP.put("notesEnd", "<br><br>");

        BBCODE_LOG_ADDITIONS_MAP.put("logHeaderStart", "[i]");
        BBCODE_LOG_ADDITIONS_MAP.put("logHeaderEnd", "[/i][quote]");
        BBCODE_LOG_ADDITIONS_MAP.put("turnRundownEnd", "[/quote]");
        BBCODE_LOG_ADDITIONS_MAP.put("turnStart", "[b]");
        BBCODE_LOG_ADDITIONS_MAP.put("turnEnd", "[/b]");
        BBCODE_LOG_ADDITIONS_MAP.put("dayChangeLineStart", "[b]");
        BBCODE_LOG_ADDITIONS_MAP.put("dayChangeLineEnd", "[/b]");
        BBCODE_LOG_ADDITIONS_MAP.put("statgainStart", "[color=#808080]");
        BBCODE_LOG_ADDITIONS_MAP.put("statgainEnd", "[/color]");
        BBCODE_LOG_ADDITIONS_MAP.put("pullStart", "[color=#008B8B]");
        BBCODE_LOG_ADDITIONS_MAP.put("pullEnd", "[/color]");
        BBCODE_LOG_ADDITIONS_MAP.put("consumableStart", "[color=#009933][b]");
        BBCODE_LOG_ADDITIONS_MAP.put("consumableEnd", "[/b][/color]");
        BBCODE_LOG_ADDITIONS_MAP.put("itemStart", "[color=#0000CD]");
        BBCODE_LOG_ADDITIONS_MAP.put("itemEnd", "[/color]");
        BBCODE_LOG_ADDITIONS_MAP.put("familiarStart", "[color=#B03030]");
        BBCODE_LOG_ADDITIONS_MAP.put("familiarEnd", "[/color]");
        BBCODE_LOG_ADDITIONS_MAP.put("huntedStart", "[color=#006400][b]");
        BBCODE_LOG_ADDITIONS_MAP.put("huntedEnd", "[/b][/color]");
        BBCODE_LOG_ADDITIONS_MAP.put("yellowRayStart", "[color=#B8860B][b]");
        BBCODE_LOG_ADDITIONS_MAP.put("yellowRayEnd", "[/b][/color]");
        BBCODE_LOG_ADDITIONS_MAP.put("specialEncounterStart", "[color=#8B008B][b]");
        BBCODE_LOG_ADDITIONS_MAP.put("specialEncounterEnd", "[/b][/color]");
        BBCODE_LOG_ADDITIONS_MAP.put("levelStart", "[color=#DC143C][b]");
        BBCODE_LOG_ADDITIONS_MAP.put("levelEnd", "[/b][/color]");
        BBCODE_LOG_ADDITIONS_MAP.put("runawayStart", "[color=#CD853F][b]");
        BBCODE_LOG_ADDITIONS_MAP.put("runawayEnd", "[/b][/color]");
        BBCODE_LOG_ADDITIONS_MAP.put("notesStart", "[/quote]" + NEW_LINE);
        BBCODE_LOG_ADDITIONS_MAP.put("notesEnd", NEW_LINE + NEW_LINE + "[quote]");
    }

    private final Map<String, String> logAdditionsMap;

    private final Set<String> localeOnetimeItemsSet = new HashSet<String>(DataTablesHandler.getOnetimeItems());

    private final StringBuilder log;

    private final Iterator<FamiliarChange> familiarChangeIter;

    private FamiliarChange currentFamChange;

    private final Iterator<Pull> pullIter;

    private Pull currentPull;

    private final Iterator<LevelData> levelIter;

    private LevelData nextLevel;

    private final Iterator<DataNumberPair<String>> huntedCombatIter;

    private DataNumberPair<String> currentHuntedCombat;

    private final Iterator<DataNumberPair<String>> disintegratedCombatIter;

    private DataNumberPair<String> currentDisintegratedCombat;

    private boolean isShowNotes = true;

    /**
     * Creates a list of all turn interval print-outs as they are composed in a
     * turn rundown inside a textual ascension log.
     * 
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @return The turn rundown list.
     */
    public static List<String> getTurnRundownList(
                                                  final LogDataHolder logData) {
        final TextLogCreator logCreator = new TextLogCreator(logData, TextualLogVersion.TEXT_LOG);
        logCreator.isShowNotes = false;
        return logCreator.createTurnRundownList(logData);
    }

    /**
     * Creates a parsed ascension log from the given {@link LogDataHolder} and
     * returns it as a String.
     * 
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @param logVersion
     *            The wanted version of the textual log output.
     * @return The textual ascension log.
     */
    public static String getTextualLog(
                                       final LogDataHolder logData,
                                       final TextualLogVersion logVersion) {
        // Sometimes, geek jokes are fun! ;)
        int logDate = 404;
        if (UsefulPatterns.USUAL_FORMAT_LOG_NAME.matcher(logData.getLogName()).matches())
            logDate = UsefulPatterns.getLogDate(logData.getLogName());

        return getTextualLog(logData, logDate, logVersion);
    }

    /**
     * Creates a parsed ascension log from the given {@link LogDataHolder} and
     * returns it as a String.
     * 
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @param ascensionStartDate
     *            The real-time start date of the ascension as saved by
     *            KolMafia.
     * @param logVersion
     *            The wanted version of the textual log output.
     * @return The textual ascension log.
     */
    public static String getTextualLog(
                                       final LogDataHolder logData, final int ascensionStartDate,
                                       final TextualLogVersion logVersion) {
        final TextLogCreator logCreator = new TextLogCreator(logData, logVersion);

        final String logOutput = logCreator.createTextLog(logData, ascensionStartDate);
        if (logVersion == TextualLogVersion.HTML_LOG)
            return "<html><body>" + logOutput.replace(NEW_LINE, "<br>" + NEW_LINE)
                   + "</body></html>";
        else
            return logOutput;
    }

    /**
     * Creates a parsed ascension log from the given {@link LogDataHolder} and
     * saves it to the given file.
     * 
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @param saveDest
     *            The file in which the parsed ascension log should be saved in.
     * @param logVersion
     *            The wanted version of the textual log output.
     * @throws IllegalArgumentException
     *             if saveDest doesn't exist or is a directory
     */
    public static void saveTextualLogToFile(
                                            final LogDataHolder logData, final File saveDest,
                                            final TextualLogVersion logVersion)
                                                                               throws IOException {
        if (!saveDest.exists())
            throw new IllegalArgumentException("The file doesn't exist.");
        if (saveDest.isDirectory())
            throw new IllegalArgumentException("The file is a directory.");

        final PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(saveDest),
                                                                      50000));
        writer.print(getTextualLog(logData, logVersion));
        writer.close();
    }

    /**
     * Creates a parsed ascension log from the given {@link LogDataHolder} and
     * saves it to the given file.
     * 
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @param ascensionStartDate
     *            The real-time start date of the ascension as saved by
     *            KolMafia.
     * @param saveDest
     *            The file in which the parsed ascension log should be saved in.
     * @param logVersion
     *            The wanted version of the textual log output.
     * @throws IllegalArgumentException
     *             if saveDest doesn't exist or is a directory
     */
    public static void saveTextualLogToFile(
                                            final LogDataHolder logData,
                                            final int ascensionStartDate, final File saveDest,
                                            final TextualLogVersion logVersion)
                                                                               throws IOException {
        if (!saveDest.exists())
            throw new IllegalArgumentException("The file doesn't exist.");
        if (saveDest.isDirectory())
            throw new IllegalArgumentException("The file is a directory.");

        final PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(saveDest),
                                                                      50000));
        writer.print(getTextualLog(logData, ascensionStartDate, logVersion));
        writer.close();
    }

    /**
     * Sets up a TextLogCreator instance for further use.
     * 
     * @param logData
     *            The ascension log data from which the parsed ascension log
     *            should be created.
     * @param logVersion
     *            The wanted version of the textual log output.
     */
    private TextLogCreator(
                           final LogDataHolder logData, final TextualLogVersion logVersion) {
        if (logData == null)
            throw new NullPointerException("The LogDataHolder must not be null.");

        switch (logVersion) {
            case HTML_LOG:
                logAdditionsMap = Collections.unmodifiableMap(HTML_LOG_ADDITIONS_MAP);
                break;
            case BBCODE_LOG:
                logAdditionsMap = Collections.unmodifiableMap(BBCODE_LOG_ADDITIONS_MAP);
                break;
            default:
                logAdditionsMap = Collections.unmodifiableMap(TEXT_LOG_ADDITIONS_MAP);
        }

        // Most logs stay below 50000 characters.
        log = new StringBuilder(50000);

        familiarChangeIter = logData.getFamiliarChanges().iterator();
        pullIter = logData.getPulls().iterator();
        levelIter = logData.getLevels().iterator();
        huntedCombatIter = logData.getLogSummary().getHuntedCombats().iterator();
        disintegratedCombatIter = logData.getLogSummary().getDisintegratedCombats().iterator();
    }

    /**
     * Creates a parsed ascension log in a style similar to the format used by
     * the AFH parser.
     * 
     * @param logData
     *            The LogDataHolder from which the ascension log should be
     *            created.
     * @param ascensionStartDate
     *            The real-time start date of the ascension as saved by
     *            KolMafia.
     */
    private List<String> createTurnRundownList(
                                               final LogDataHolder logData) {
        final List<String> turnRundown = new ArrayList<String>(logData.getTurnsSpent().size());

        currentFamChange = familiarChangeIter.hasNext() ? familiarChangeIter.next() : null;
        currentPull = pullIter.hasNext() ? pullIter.next() : null;
        currentHuntedCombat = huntedCombatIter.hasNext() ? huntedCombatIter.next() : null;
        currentDisintegratedCombat = disintegratedCombatIter.hasNext() ? disintegratedCombatIter.next()
                                                                      : null;

        // Level 1 can be skipped.
        levelIter.next();
        nextLevel = levelIter.hasNext() ? levelIter.next() : null;

        // Day 1 day change is handled differently and can be ignored here.
        final Iterator<DayChange> dayChangeIter = logData.getDayChanges().iterator();
        DayChange nextDayChange = dayChangeIter.next();
        nextDayChange = dayChangeIter.hasNext() ? dayChangeIter.next() : NO_DAY_CHANGE;
        int currentDayNumber = 1;

        for (final TurnInterval ti : logData.getTurnsSpent()) {
            if (!nextDayChange.equals(NO_DAY_CHANGE)
                && ti.getEndTurn() >= nextDayChange.getTurnNumber())
                if (ti.getEndTurn() == nextDayChange.getTurnNumber()) {
                    printTurnIntervalContents(ti, currentDayNumber);

                    final int currentStringLenght = log.length();
                    final Pair<Integer, DayChange> newDayChangeData = printDayChanges(logData,
                                                                                      ti.getEndTurn(),
                                                                                      nextDayChange,
                                                                                      dayChangeIter);
                    currentDayNumber = newDayChangeData.getVar1();
                    nextDayChange = newDayChangeData.getVar2();
                    log.delete(currentStringLenght, log.length());

                    // Consumables usage or pulls that happened nominally on the
                    // last turn before the day change, but were actually done
                    // on the next day.
                    printCurrentConsumables(ti.getConsumablesUsed(), currentDayNumber);
                    printCurrentPulls(currentDayNumber, ti.getEndTurn());
                } else if (ti.getStartTurn() < nextDayChange.getTurnNumber()) {
                    SingleTurn dayChangeTurn = null;
                    for (final SingleTurn st : ti.getTurns())
                        if (st.getTurnNumber() > nextDayChange.getTurnNumber()) {
                            dayChangeTurn = st;
                            break;
                        }

                    final TurnInterval turnsBeforeDayChange = new TurnInterval(ti.getTurns()
                                                                                 .headSet(dayChangeTurn),
                                                                               dayChangeTurn.getAreaName());
                    final TurnInterval turnsAfterDayChange = new TurnInterval(ti.getTurns()
                                                                                .tailSet(dayChangeTurn),
                                                                              dayChangeTurn.getAreaName());

                    printTurnIntervalContents(turnsBeforeDayChange, currentDayNumber);

                    final int currentStringLenght = log.length();
                    final Pair<Integer, DayChange> newDayChangeData = printDayChanges(logData,
                                                                                      ti.getEndTurn(),
                                                                                      nextDayChange,
                                                                                      dayChangeIter);
                    currentDayNumber = newDayChangeData.getVar1();
                    nextDayChange = newDayChangeData.getVar2();
                    log.delete(currentStringLenght, log.length());

                    // Consumables usage or pulls that happened nominally on the
                    // last turn before the day change, but were actually done
                    // on the next day.
                    printCurrentConsumables(turnsBeforeDayChange.getConsumablesUsed(),
                                            currentDayNumber);
                    printCurrentPulls(currentDayNumber, turnsBeforeDayChange.getEndTurn());
                    log.append(NEW_LINE);
                    printTurnIntervalContents(turnsAfterDayChange, currentDayNumber);
                } else {
                    final int currentStringLenght = log.length();
                    final Pair<Integer, DayChange> newDayChangeData = printDayChanges(logData,
                                                                                      ti.getEndTurn(),
                                                                                      nextDayChange,
                                                                                      dayChangeIter);
                    currentDayNumber = newDayChangeData.getVar1();
                    nextDayChange = newDayChangeData.getVar2();
                    log.delete(currentStringLenght, log.length());

                    printTurnIntervalContents(ti, currentDayNumber);
                }
            else
                printTurnIntervalContents(ti, currentDayNumber);

            turnRundown.add(log.toString());
            log.delete(0, log.length());
        }

        return turnRundown;
    }

    /**
     * Creates a parsed ascension log in a style similar to the format used by
     * the AFH parser.
     * 
     * @param logData
     *            The LogDataHolder from which the ascension log should be
     *            created.
     * @param ascensionStartDate
     *            The real-time start date of the ascension as saved by
     *            KolMafia.
     */
    private String createTextLog(
                                 final LogDataHolder logData, final int ascensionStartDate) {
        currentFamChange = familiarChangeIter.hasNext() ? familiarChangeIter.next() : null;
        currentPull = pullIter.hasNext() ? pullIter.next() : null;
        currentHuntedCombat = huntedCombatIter.hasNext() ? huntedCombatIter.next() : null;
        currentDisintegratedCombat = disintegratedCombatIter.hasNext() ? disintegratedCombatIter.next()
                                                                      : null;

        // Level 1 can be skipped.
        levelIter.next();
        nextLevel = levelIter.hasNext() ? levelIter.next() : null;

        // Day 1 day change is handled differently and can be ignored here.
        final Iterator<DayChange> dayChangeIter = logData.getDayChanges().iterator();
        DayChange nextDayChange = dayChangeIter.next();
        nextDayChange = dayChangeIter.hasNext() ? dayChangeIter.next() : NO_DAY_CHANGE;
        int currentDayNumber = 1;

        // Add the log file header.
        write("NEW " + logData.getCharacterClass() + " ASCENSION STARTED " + ascensionStartDate
              + NEW_LINE);
        write("------------------------------" + NEW_LINE + NEW_LINE);
        write(logAdditionsMap.get("logHeaderStart"));
        write("This log was created by the Ascension Log Visualizer "
              + Settings.getSettingString("Version") + "." + NEW_LINE);
        write("The basic idea and the format of this parser have been burrowed from the AFH MafiaLog Parser by VladimirPootin and QuantumNightmare."
              + NEW_LINE + NEW_LINE);
        write(logAdditionsMap.get("logHeaderEnd"));
        write(logAdditionsMap.get("dayChangeLineStart"));
        write("===Day 1===");
        write(logAdditionsMap.get("dayChangeLineEnd"));
        write(NEW_LINE + NEW_LINE);

        for (final TurnInterval ti : logData.getTurnsSpent())
            if (!nextDayChange.equals(NO_DAY_CHANGE)
                && ti.getEndTurn() >= nextDayChange.getTurnNumber())
                if (ti.getEndTurn() == nextDayChange.getTurnNumber()) {
                    printTurnIntervalContents(ti, currentDayNumber);

                    final Pair<Integer, DayChange> newDayChangeData = printDayChanges(logData,
                                                                                      ti.getEndTurn(),
                                                                                      nextDayChange,
                                                                                      dayChangeIter);
                    currentDayNumber = newDayChangeData.getVar1();
                    nextDayChange = newDayChangeData.getVar2();

                    // Consumables usage or pulls that happened nominally on the
                    // last turn before the day change, but were actually done
                    // on the next day.
                    printCurrentConsumables(ti.getConsumablesUsed(), currentDayNumber);
                    printCurrentPulls(currentDayNumber, ti.getEndTurn());
                } else if (ti.getStartTurn() < nextDayChange.getTurnNumber()) {
                    SingleTurn dayChangeTurn = null;
                    for (final SingleTurn st : ti.getTurns())
                        if (st.getTurnNumber() > nextDayChange.getTurnNumber()) {
                            dayChangeTurn = st;
                            break;
                        }

                    final TurnInterval turnsBeforeDayChange = new TurnInterval(ti.getTurns()
                                                                                 .headSet(dayChangeTurn),
                                                                               dayChangeTurn.getAreaName());
                    final TurnInterval turnsAfterDayChange = new TurnInterval(ti.getTurns()
                                                                                .tailSet(dayChangeTurn),
                                                                              dayChangeTurn.getAreaName());
                    turnsAfterDayChange.incrementSuccessfulFreeRunaways(ti.getFreeRunaways()
                                                                          .getNumberOfSuccessfulRunaways());

                    printTurnIntervalContents(turnsBeforeDayChange, currentDayNumber);

                    final Pair<Integer, DayChange> newDayChangeData = printDayChanges(logData,
                                                                                      ti.getEndTurn(),
                                                                                      nextDayChange,
                                                                                      dayChangeIter);
                    currentDayNumber = newDayChangeData.getVar1();
                    nextDayChange = newDayChangeData.getVar2();

                    // Consumables usage or pulls that happened nominally on the
                    // last turn before the day change, but were actually done
                    // on the next day.
                    printCurrentConsumables(turnsBeforeDayChange.getConsumablesUsed(),
                                            currentDayNumber);
                    printCurrentPulls(currentDayNumber, turnsBeforeDayChange.getEndTurn());

                    printTurnIntervalContents(turnsAfterDayChange, currentDayNumber);

                    // Print the notes from the actual interval.
                    printNotes(ti);
                } else {
                    final Pair<Integer, DayChange> newDayChangeData = printDayChanges(logData,
                                                                                      ti.getEndTurn(),
                                                                                      nextDayChange,
                                                                                      dayChangeIter);
                    currentDayNumber = newDayChangeData.getVar1();
                    nextDayChange = newDayChangeData.getVar2();

                    printTurnIntervalContents(ti, currentDayNumber);
                }
            else
                printTurnIntervalContents(ti, currentDayNumber);

        write(NEW_LINE + "Turn rundown finished!");
        write(logAdditionsMap.get("turnRundownEnd"));
        write(NEW_LINE + NEW_LINE);

        printLogSummaries(logData);

        return log.toString();
    }

    /**
     * Prints all day changes that occurred and returns the new current day
     * number and the next day change. If no day change occurred, the old values
     * will be returned.
     */
    private Pair<Integer, DayChange> printDayChanges(
                                                     final LogDataHolder logData,
                                                     final int currentTurnNumber,
                                                     DayChange nextDayChange,
                                                     final Iterator<DayChange> dayChangeIter) {
        int currentDayNumber = nextDayChange.getDayNumber() - 1;
        while (!nextDayChange.equals(NO_DAY_CHANGE)
               && currentTurnNumber >= nextDayChange.getTurnNumber()) {
            final PlayerSnapshot currentSnapshot = logData.getFirstPlayerSnapshotAfterTurn(nextDayChange.getTurnNumber());

            write(NEW_LINE);
            write(logAdditionsMap.get("dayChangeLineStart"));
            write(nextDayChange.toString());
            write(logAdditionsMap.get("dayChangeLineEnd"));
            if (currentSnapshot != null) {
                write(NEW_LINE);
                write(ADVENTURES_LEFT_STRING);
                write(currentSnapshot.getAdventuresLeft());
                write(NEW_LINE);
                write(CURRENT_MEAT_STRING);
                write(currentSnapshot.getCurrentMeat());
            }
            write(NEW_LINE);
            write(NEW_LINE);

            currentDayNumber = nextDayChange.getDayNumber();
            nextDayChange = dayChangeIter.hasNext() ? dayChangeIter.next() : NO_DAY_CHANGE;
        }

        return Pair.of(currentDayNumber, nextDayChange);
    }

    /**
     * Prints all pulls from the given day up to the given turn number.
     */
    private void printCurrentPulls(
                                   final int currentDayNumber, final int currentTurnNumber) {
        while (currentPull != null && currentTurnNumber >= currentPull.getTurnNumber()) {
            // Only pulls of the current day should be added here.
            if (currentPull.getDayNumber() > currentDayNumber)
                break;

            write(PULL_PREFIX);
            write(OPENING_TURN_BRACKET);
            write(currentPull.getTurnNumber());
            write(CLOSING_TURN_BRACKET);
            write("pulled");
            write(UsefulPatterns.WHITE_SPACE);
            write(logAdditionsMap.get("pullStart"));
            write(currentPull.getAmount());
            write(UsefulPatterns.WHITE_SPACE);
            write(currentPull.getItemName());
            write(logAdditionsMap.get("pullEnd"));
            write(NEW_LINE);

            currentPull = pullIter.hasNext() ? pullIter.next() : null;
        }
    }

    /**
     * Prints all consumables from the given day.
     */
    private void printCurrentConsumables(
                                         final Collection<Consumable> consumables,
                                         final int currentDayNumber) {
        for (final Consumable c : consumables)
            if (c.getDayNumberOfUsage() == currentDayNumber)
                if (c.getAdventureGain() > 0 || !c.getStatGain().isAllStatsZero()) {
                    write(CONSUMABLE_PREFIX);

                    if (c.getConsumableVersion() == ConsumableVersion.FOOD)
                        write("Ate ");
                    else if (c.getConsumableVersion() == ConsumableVersion.BOOZE)
                        write("Drank ");
                    else
                        write("Used ");

                    write(logAdditionsMap.get("consumableStart"));
                    write(c.getAmount());
                    write(UsefulPatterns.WHITE_SPACE);
                    write(c.getName());
                    write(logAdditionsMap.get("consumableEnd"));

                    if (c.getAdventureGain() > 0
                        || c.getConsumableVersion() == ConsumableVersion.FOOD
                        || c.getConsumableVersion() == ConsumableVersion.BOOZE) {
                        write(UsefulPatterns.WHITE_SPACE);
                        write(UsefulPatterns.ROUND_BRACKET_OPEN);
                        write(c.getAdventureGain());
                        write(UsefulPatterns.WHITE_SPACE);
                        write("adventures gained");
                        write(UsefulPatterns.ROUND_BRACKET_CLOSE);
                    }

                    write(UsefulPatterns.WHITE_SPACE);
                    write(logAdditionsMap.get("statgainStart"));
                    write(c.getStatGain().toString());
                    write(logAdditionsMap.get("statgainEnd"));
                    write(NEW_LINE);
                }
    }

    /**
     * Prints the notes contained inside the given turn interval. If the
     * interval contains no notes, this method won't print anything.
     */
    private void printNotes(
                            final TurnInterval ti) {
        if (ti.getNotes().length() > 0) {
            write(logAdditionsMap.get("notesStart"));
            write(ti.getNotes().replaceAll("[\r\n]|\r\n", NEW_LINE));
            write(logAdditionsMap.get("notesEnd"));
            write(NEW_LINE);
        }
    }

    private void printItemAcquisitionStartString(
                                                 final int turnNumber) {
        write(ITEM_PREFIX);
        write(OPENING_TURN_BRACKET);
        write(turnNumber);
        write(CLOSING_TURN_BRACKET);
        write(ITEM_MIDDLE_STRING);
    }

    /**
     * @param ti
     *            The turn interval whose contents should be printed.
     */
    private void printTurnIntervalContents(
                                           final TurnInterval ti, final int currentDayNumber) {
        write(logAdditionsMap.get("turnStart"));
        write(UsefulPatterns.SQUARE_BRACKET_OPEN);
        if (ti.getTotalTurns() > 1) {
            write(ti.getStartTurn() + 1);
            write(UsefulPatterns.MINUS);
        }
        write(ti.getEndTurn());
        write(UsefulPatterns.SQUARE_BRACKET_CLOSE);
        write(logAdditionsMap.get("turnEnd"));

        write(UsefulPatterns.WHITE_SPACE);
        write(ti.getAreaName());
        write(UsefulPatterns.WHITE_SPACE);

        write(logAdditionsMap.get("statgainStart"));
        write(ti.getStatGain().toString());
        write(logAdditionsMap.get("statgainEnd"));
        write(NEW_LINE);

        for (final SingleTurn st : ti.getTurns()) {
            if (DataTablesHandler.isSemirareEncounter(st)) {
                write(SEMIRARE_PREFIX);
                write(OPENING_TURN_BRACKET);
                write(st.getTurnNumber());
                write(CLOSING_TURN_BRACKET);
                write(SEMIRARE_MIDDLE_STRING);
                write(logAdditionsMap.get("specialEncounterStart"));
                write(st.getEncounterName());
                write(logAdditionsMap.get("specialEncounterEnd"));
                write(NEW_LINE);
            }
            if (DataTablesHandler.isBadMoonEncounter(st)) {
                write(BAD_MOON_PREFIX);
                write(OPENING_TURN_BRACKET);
                write(st.getTurnNumber());
                write(CLOSING_TURN_BRACKET);
                write(BAD_MOON_MIDDLE_STRING);
                write(logAdditionsMap.get("specialEncounterStart"));
                write(st.getEncounterName());
                write(logAdditionsMap.get("specialEncounterEnd"));
                write(NEW_LINE);
            }

            final List<Item> importantItems = new ArrayList<Item>();
            for (final Item i : st.getDroppedItems()) {
                final String itemName = i.getName().toLowerCase(Locale.ENGLISH);
                if (DataTablesHandler.isImportantItem(itemName))
                    importantItems.add(i);
                if (localeOnetimeItemsSet.contains(itemName)) {
                    importantItems.add(i);
                    localeOnetimeItemsSet.remove(itemName);
                }
            }

            final Iterator<Item> aquiredItemsIter = importantItems.iterator();
            if (aquiredItemsIter.hasNext()) {
                printItemAcquisitionStartString(st.getTurnNumber());

                int itemCounter = 0;
                while (aquiredItemsIter.hasNext()) {
                    final Item currentItem = aquiredItemsIter.next();
                    for (int i = currentItem.getAmount(); i > 0; i--) {
                        write(logAdditionsMap.get("itemStart"));
                        write(currentItem.getName());
                        write(logAdditionsMap.get("itemEnd"));
                        itemCounter++;

                        if ((aquiredItemsIter.hasNext() || i > 1) && itemCounter >= 4) {
                            write(NEW_LINE);
                            printItemAcquisitionStartString(st.getTurnNumber());
                            itemCounter = 0;
                        } else if (i > 1)
                            write(COMMA);
                    }

                    if (aquiredItemsIter.hasNext() && itemCounter != 0)
                        write(COMMA);
                }

                write(NEW_LINE);
            }
        }

        printCurrentConsumables(ti.getConsumablesUsed(), currentDayNumber);

        printCurrentPulls(currentDayNumber, ti.getEndTurn());

        while (currentHuntedCombat != null && ti.getEndTurn() >= currentHuntedCombat.getNumber()) {
            write(HUNTED_COMBAT_PREFIX);
            write(OPENING_TURN_BRACKET);
            write(currentHuntedCombat.getNumber());
            write(CLOSING_TURN_BRACKET);
            write(HUNTED_COMBAT_MIDDLE_STRING);
            write(logAdditionsMap.get("huntedStart"));
            write(currentHuntedCombat.getData());
            write(logAdditionsMap.get("huntedEnd"));
            write(NEW_LINE);

            currentHuntedCombat = huntedCombatIter.hasNext() ? huntedCombatIter.next() : null;
        }

        while (currentDisintegratedCombat != null
               && ti.getEndTurn() >= currentDisintegratedCombat.getNumber()) {
            write(DISINTEGRATED_COMBAT_PREFIX);
            write(OPENING_TURN_BRACKET);
            write(currentDisintegratedCombat.getNumber());
            write(CLOSING_TURN_BRACKET);
            write(DISINTEGRATED_COMBAT_MIDDLE_STRING);
            write(logAdditionsMap.get("yellowRayStart"));
            write(currentDisintegratedCombat.getData());
            write(logAdditionsMap.get("yellowRayEnd"));
            write(NEW_LINE);

            currentDisintegratedCombat = disintegratedCombatIter.hasNext() ? disintegratedCombatIter.next()
                                                                          : null;
        }

        while (currentFamChange != null && ti.getEndTurn() >= currentFamChange.getTurnNumber()) {
            write(FAMILIAR_CHANGE_PREFIX);
            write(OPENING_TURN_BRACKET);
            write(currentFamChange.getTurnNumber());
            write(CLOSING_TURN_BRACKET);
            write(logAdditionsMap.get("familiarStart"));
            write(currentFamChange.getFamiliarName());
            write(logAdditionsMap.get("familiarEnd"));
            write(NEW_LINE);

            currentFamChange = familiarChangeIter.hasNext() ? familiarChangeIter.next() : null;
        }

        final FreeRunaways freeRunaways = ti.getFreeRunaways();
        if (freeRunaways.getNumberOfAttemptedRunaways() > 0) {
            write(logAdditionsMap.get("runawayStart"));
            write(FREE_RUNAWAYS_PREFIX);
            write(freeRunaways.getNumberOfSuccessfulRunaways());
            write(UsefulPatterns.WHITE_SPACE);
            write("/");
            write(UsefulPatterns.WHITE_SPACE);
            write(freeRunaways.getNumberOfAttemptedRunaways());
            write(UsefulPatterns.WHITE_SPACE);
            write("free retreats");
            write(logAdditionsMap.get("runawayEnd"));
            write(NEW_LINE);
        }

        while (nextLevel != null && ti.getEndTurn() >= nextLevel.getLevelReachedOnTurn()) {
            final int musStat = (int) Math.sqrt(nextLevel.getStatsAtLevelReached().mus);
            final int mystStat = (int) Math.sqrt(nextLevel.getStatsAtLevelReached().myst);
            final int moxStat = (int) Math.sqrt(nextLevel.getStatsAtLevelReached().mox);

            write(logAdditionsMap.get("levelStart"));
            write(LEVEL_CHANGE_PREFIX);
            write(nextLevel.getLevelNumber());
            write(" (Turn ");
            write(nextLevel.getLevelReachedOnTurn());
            write(")! (");
            write(musStat);
            write("/");
            write(mystStat);
            write("/");
            write(moxStat);
            write(UsefulPatterns.ROUND_BRACKET_CLOSE);
            write(logAdditionsMap.get("levelEnd"));
            write(NEW_LINE);

            nextLevel = levelIter.hasNext() ? levelIter.next() : null;
        }

        if (isShowNotes)
            printNotes(ti);
    }

    private void printLogSummaries(
                                   final LogDataHolder logData) {
        // Turns spent per area summary
        write("ADVENTURES" + NEW_LINE + "----------" + NEW_LINE);
        for (final DataNumberPair<String> dn : logData.getLogSummary().getTurnsPerArea()) {
            write(dn.getData());
            write(": ");
            write(dn.getNumber());
            write(NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Quest Turns summary
        write("QUEST TURNS" + NEW_LINE + "----------" + NEW_LINE);
        write("Mosquito Larva: "
              + logData.getLogSummary().getQuestTurncounts().getMosquitoQuestTurns() + NEW_LINE);
        write("Opening the Hidden Temple: "
              + logData.getLogSummary().getQuestTurncounts().getTempleOpeningTurns() + NEW_LINE);
        write("Tavern quest: " + logData.getLogSummary().getQuestTurncounts().getTavernQuestTurns()
              + NEW_LINE);
        write("Bat quest: " + logData.getLogSummary().getQuestTurncounts().getBatQuestTurns()
              + NEW_LINE);
        write("Cobb's Knob quest: "
              + logData.getLogSummary().getQuestTurncounts().getKnobQuestTurns() + NEW_LINE);
        write("Friars' part 1: "
              + logData.getLogSummary().getQuestTurncounts().getFriarsQuestTurns() + NEW_LINE);
        write("Defiled Cyrpt quest: "
              + logData.getLogSummary().getQuestTurncounts().getCyrptQuestTurns() + NEW_LINE);
        write("Trapzor quest: "
              + logData.getLogSummary().getQuestTurncounts().getTrapzorQuestTurns() + NEW_LINE);
        write("Orc Chasm quest: "
              + logData.getLogSummary().getQuestTurncounts().getChasmQuestTurns() + NEW_LINE);
        write("Airship: " + logData.getLogSummary().getQuestTurncounts().getAirshipQuestTurns()
              + NEW_LINE);
        write("Giant's Castle: "
              + logData.getLogSummary().getQuestTurncounts().getCastleQuestTurns() + NEW_LINE);
        write("Opening the Ballroom: "
              + logData.getLogSummary().getQuestTurncounts().getBallroomOpeningTurns() + NEW_LINE);
        write("Pirate quest: " + logData.getLogSummary().getQuestTurncounts().getPirateQuestTurns()
              + NEW_LINE);
        write("Black Forest quest: "
              + logData.getLogSummary().getQuestTurncounts().getBlackForrestQuestTurns() + NEW_LINE);
        write("Desert Oasis quest: "
              + logData.getLogSummary().getQuestTurncounts().getDesertOasisQuestTurns() + NEW_LINE);
        write("Spookyraven quest: "
              + logData.getLogSummary().getQuestTurncounts().getSpookyravenQuestTurns() + NEW_LINE);
        write("Hidden City quest: "
              + logData.getLogSummary().getQuestTurncounts().getTempleCityQuestTurns() + NEW_LINE);
        write("Palindome quest: "
              + logData.getLogSummary().getQuestTurncounts().getPalindomeQuestTurns() + NEW_LINE);
        write("Pyramid quest: "
              + logData.getLogSummary().getQuestTurncounts().getPyramidQuestTurns() + NEW_LINE);
        write("Starting the War: "
              + logData.getLogSummary().getQuestTurncounts().getWarIslandOpeningTurns() + NEW_LINE);
        write("War Island quest: "
              + logData.getLogSummary().getQuestTurncounts().getWarIslandQuestTurns() + NEW_LINE);
        write("DoD quest: " + logData.getLogSummary().getQuestTurncounts().getDodQuestTurns()
              + NEW_LINE);
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Pulls summary
        write("PULLS" + NEW_LINE + "----------" + NEW_LINE);
        final DataCounter<String> pullsCounter = new DataCounter<String>((int) (logData.getPulls()
                                                                                       .size() * 1.4) + 1);
        for (final Pull p : logData.getPulls())
            pullsCounter.addDataElement(p.getItemName(), p.getAmount());
        final List<DataNumberPair<String>> pulls = pullsCounter.getCountedData();
        // ordered from highest to lowest amount
        Collections.sort(pulls, new Comparator<DataNumberPair<String>>() {

            public int compare(
                               final DataNumberPair<String> o1, final DataNumberPair<String> o2) {
                return o2.compareTo(o1);
            }
        });
        for (final DataNumberPair<String> dn : pulls) {
            write("Pulled ");
            write(dn.getNumber());
            write(" ");
            write(dn.getData());
            write(NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Level summary
        write("LEVELS" + NEW_LINE + "----------" + NEW_LINE);
        final NumberFormat formatter = NumberFormat.getNumberInstance(Locale.ENGLISH);
        formatter.setMaximumFractionDigits(1);
        formatter.setMinimumFractionDigits(1);
        LevelData lastLevel = null;
        for (final LevelData ld : logData.getLevels()) {
            final int turnDifference = lastLevel != null ? ld.getLevelReachedOnTurn()
                                                           - lastLevel.getLevelReachedOnTurn() : 0;
            final double statsPerTurn = lastLevel != null ? lastLevel.getStatGainPerTurn() : 0;
            final int combatTurns = lastLevel != null ? lastLevel.getCombatTurns() : 0;
            final int noncombatTurns = lastLevel != null ? lastLevel.getNoncombatTurns() : 0;
            final int otherTurns = lastLevel != null ? lastLevel.getOtherTurns() : 0;

            write(ld.toString());
            write(COMMA);
            write(turnDifference);
            write(" from last level. (");
            write(formatter.format(statsPerTurn));
            write(" substats / turn)");
            write(NEW_LINE);

            write("   Combats: ");
            write(combatTurns);
            write(NEW_LINE);
            write("   Noncombats: ");
            write(noncombatTurns);
            write(NEW_LINE);
            write("   Other: ");
            write(otherTurns);
            write(NEW_LINE);

            lastLevel = ld;
        }
        write(NEW_LINE + NEW_LINE);
        final int totalTurns = logData.getTurnsSpent().last().getEndTurn();
        write("Total COMBATS: " + logData.getLogSummary().getTotalTurnsCombat() + " ("
              + Math.round(logData.getLogSummary().getTotalTurnsCombat() * 1000.0 / totalTurns)
              / 10.0 + "%)" + NEW_LINE);
        write("Total NONCOMBATS: " + logData.getLogSummary().getTotalTurnsNoncombat() + " ("
              + Math.round(logData.getLogSummary().getTotalTurnsNoncombat() * 1000.0 / totalTurns)
              / 10.0 + "%)" + NEW_LINE);
        write("Total OTHER: " + logData.getLogSummary().getTotalTurnsOther() + " ("
              + Math.round(logData.getLogSummary().getTotalTurnsOther() * 1000.0 / totalTurns)
              / 10.0 + "%)" + NEW_LINE);
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Stats summary
        write("STATS" + NEW_LINE + "----------" + NEW_LINE);
        final Statgain totalStats = logData.getLogSummary().getTotalStatgains();
        final Statgain combatStats = logData.getLogSummary().getCombatsStatgains();
        final Statgain noncombatStats = logData.getLogSummary().getNoncombatsStatgains();
        final Statgain otherStats = logData.getLogSummary().getOthersStatgains();
        final Statgain foodStats = logData.getLogSummary().getFoodConsumablesStatgains();
        final Statgain boozeStats = logData.getLogSummary().getBoozeConsumablesStatgains();
        final Statgain usingStats = logData.getLogSummary().getUsedConsumablesStatgains();
        write("           \tMuscle\tMyst\tMoxie" + NEW_LINE);
        write("Totals:   \t" + totalStats.mus + "\t" + totalStats.myst + "\t" + totalStats.mox
              + NEW_LINE);
        write("Combats:\t" + combatStats.mus + "\t" + combatStats.myst + "\t" + combatStats.mox
              + NEW_LINE);
        write("Noncombats:\t" + noncombatStats.mus + "\t" + noncombatStats.myst + "\t"
              + noncombatStats.mox + NEW_LINE);
        write("Others:   \t" + otherStats.mus + "\t" + otherStats.myst + "\t" + otherStats.mox
              + NEW_LINE);
        write("Eating:   \t" + foodStats.mus + "\t" + foodStats.myst + "\t" + foodStats.mox
              + NEW_LINE);
        write("Drinking:\t" + boozeStats.mus + "\t" + boozeStats.myst + "\t" + boozeStats.mox
              + NEW_LINE);
        write("Using:   \t" + usingStats.mus + "\t" + usingStats.myst + "\t" + usingStats.mox
              + NEW_LINE);
        write(NEW_LINE + NEW_LINE);
        final List<AreaStatgains> areas = new ArrayList<AreaStatgains>(logData.getLogSummary()
                                                                              .getAreasStatgains());
        Collections.sort(areas, new Comparator<AreaStatgains>() {
            public int compare(
                               final AreaStatgains o1, final AreaStatgains o2) {
                if (logData.getCharacterClass().getStatClass() == StatClass.MUSCLE)
                    return o2.getStatgain().mus - o1.getStatgain().mus;
                else if (logData.getCharacterClass().getStatClass() == StatClass.MYSTICALITY)
                    return o2.getStatgain().myst - o1.getStatgain().myst;
                else
                    return o2.getStatgain().mox - o1.getStatgain().mox;
            }
        });
        write("Top 10 mainstat gaining areas:" + NEW_LINE + NEW_LINE);
        for (int i = 0; i < areas.size() && i < 10; i++) {
            write(areas.get(i).toString());
            write(NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // +Stat Breakdown summary
        final List<StatgiverItem> statGivers = new ArrayList<StatgiverItem>(20);
        for (final Pair<String, Double> p : DataTablesHandler.getStatsItems())
            statGivers.add(new StatgiverItem(p.getVar1(), p.getVar2()));
        final StatgiverItem serpentineSword = new StatgiverItem("serpentine sword", 1.25);
        final StatgiverItem snakeShield = new StatgiverItem("snake shield", 1.25);

        final Iterator<LevelData> lvlIndex = logData.getLevels().iterator();
        LevelData nextLvl = lvlIndex.hasNext() ? lvlIndex.next() : null;
        int currentLvlNumber = 1;
        for (final TurnInterval ti : logData.getTurnsSpent())
            for (final SingleTurn st : ti.getTurns()) {
                while (nextLvl != null && nextLvl.getLevelReachedOnTurn() < st.getTurnNumber()) {
                    currentLvlNumber = nextLvl.getLevelNumber();
                    nextLvl = lvlIndex.hasNext() ? lvlIndex.next() : null;
                }

                if (currentLvlNumber >= 13)
                    break;

                if (st.getTurnVersion() == TurnVersion.COMBAT) {
                    for (final StatgiverItem sgi : statGivers)
                        sgi.incrementLvlStatgain(currentLvlNumber,
                                                 st.getUsedEquipment()
                                                   .getNumberOfEquips(sgi.getItemName()));

                    // Special cases
                    final int serpentineSwordEquips = st.getUsedEquipment()
                                                        .getNumberOfEquips(serpentineSword.getItemName());
                    serpentineSword.incrementLvlStatgain(currentLvlNumber, serpentineSwordEquips);
                    if (serpentineSwordEquips == 1)
                        snakeShield.incrementLvlStatgain(currentLvlNumber,
                                                         st.getUsedEquipment()
                                                           .getNumberOfEquips(snakeShield.getItemName()));
                }
            }
        // Add special cases to list for text print out.
        statGivers.add(serpentineSword);
        statGivers.add(snakeShield);

        // Sort item list from highest total stat gain to lowest.
        Collections.sort(statGivers, new Comparator<StatgiverItem>() {
            public int compare(
                               final StatgiverItem o1, final StatgiverItem o2) {
                return o2.getTotalStats() - o1.getTotalStats();
            }
        });

        write("+STAT BREAKDOWN" + NEW_LINE + "----------" + NEW_LINE);
        write("Need to gain level (last is total):                          \t10\t39\t105\t231\t441\t759\t1209\t1815\t2601\t3591\t4809\t6279\t21904"
              + NEW_LINE);
        for (final StatgiverItem sgi : statGivers)
            if (sgi.getTotalStats() > 0) {
                write(sgi.toString());
                write(NEW_LINE);
            }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Familiars summary
        write("FAMILIARS" + NEW_LINE + "----------" + NEW_LINE);
        for (final DataNumberPair<String> dn : logData.getLogSummary().getFamiliarUsage()) {
            write(dn.getData());
            write(" : ");
            write(dn.getNumber());
            write(" combat turns (");
            write(String.valueOf(Math.round(dn.getNumber() * 1000.0
                                            / logData.getLogSummary().getTotalTurnsCombat()) / 10.0));
            write("%)");
            write(NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Semi-rares summary
        write("SEMI-RARES" + NEW_LINE + "----------" + NEW_LINE);
        for (final DataNumberPair<String> dn : logData.getLogSummary().getSemirares()) {
            write(dn.getNumber());
            write(" : ");
            write(dn.getData());
            write(NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Hunted combats summary
        write("HUNTED COMBATS" + NEW_LINE + "----------" + NEW_LINE);
        for (final DataNumberPair<String> dn : logData.getLogSummary().getHuntedCombats()) {
            write(dn.getNumber());
            write(" : ");
            write(dn.getData());
            write(NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Disintegrated combats summary
        write("HE-BOULDER YELLOW RAYS" + NEW_LINE + "----------" + NEW_LINE);
        for (final DataNumberPair<String> dn : logData.getLogSummary().getDisintegratedCombats()) {
            write(dn.getNumber());
            write(" : ");
            write(dn.getData());
            write(NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Skills cast summary
        write("CASTS" + NEW_LINE + "----------" + NEW_LINE);
        for (final Skill s : logData.getLogSummary().getSkillsCast()) {
            write(s.toString());
            write(NEW_LINE);
        }
        write(NEW_LINE + "------------------" + NEW_LINE + "| Total Casts    |  "
              + logData.getLogSummary().getTotalAmountSkillCasts() + NEW_LINE
              + "------------------" + NEW_LINE);
        write(NEW_LINE + "------------------" + NEW_LINE + "| Total MP Spent    |  "
              + logData.getLogSummary().getTotalMPUsed() + NEW_LINE + "------------------"
              + NEW_LINE);
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // MP summary
        final MPGain mpGains = logData.getLogSummary().getMPGains();
        write("MP GAINS" + NEW_LINE + "----------" + NEW_LINE);
        write("Total mp gained: " + mpGains.getTotalMPGains() + NEW_LINE + NEW_LINE);
        write("Inside Encounters: " + mpGains.getEncounterMPGain() + NEW_LINE);
        write("Starfish Familiars: " + mpGains.getStarfishMPGain() + NEW_LINE);
        write("Resting: " + mpGains.getRestingMPGain() + NEW_LINE);
        write("Outside Encounters: " + mpGains.getOutOfEncounterMPGain() + NEW_LINE);
        write("Consumables: " + mpGains.getConsumableMPGain() + NEW_LINE);
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Consumables summary
        write("EATING AND DRINKING AND USING" + NEW_LINE + "----------" + NEW_LINE);
        write("Adventures gained eating: " + logData.getLogSummary().getTotalTurnsFromFood()
              + NEW_LINE);
        write("Adventures gained drinking: " + logData.getLogSummary().getTotalTurnsFromBooze()
              + NEW_LINE);
        write("Adventures gained using: " + logData.getLogSummary().getTotalTurnsFromOther()
              + NEW_LINE);
        write("Adventures gained rollover: " + logData.getLogSummary().getTotalTurnsFromRollover()
              + NEW_LINE);
        write(NEW_LINE);
        for (final Consumable c : logData.getLogSummary().getFoodConsumablesUsed()) {
            write(c.toString());
            write(NEW_LINE);
        }
        write(NEW_LINE);
        for (final Consumable c : logData.getLogSummary().getBoozeConsumablesUsed()) {
            write(c.toString());
            write(NEW_LINE);
        }
        write(NEW_LINE);
        for (final Consumable c : logData.getLogSummary().getSpleenConsumablesUsed())
            if (c.getAdventureGain() > 0) {
                write(c.toString());
                write(NEW_LINE);
            }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Meat summary
        write("MEAT" + NEW_LINE + "----------" + NEW_LINE);
        write("Total meat gained: " + logData.getLogSummary().getTotalMeatGain() + NEW_LINE);
        write("Total meat spent: " + logData.getLogSummary().getTotalMeatSpent() + NEW_LINE
              + NEW_LINE);
        for (final DataNumberPair<MeatGain> dnp : logData.getLogSummary()
                                                         .getMeatSummary()
                                                         .getAllLevelsMeatData()) {
            write("Level " + dnp.getNumber() + UsefulPatterns.COLON + NEW_LINE);
            write("   Meat gain inside Encounters: " + dnp.getData().encounterMeatGain + NEW_LINE);
            write("   Meat gain outside Encounters: " + dnp.getData().otherMeatGain + NEW_LINE);
            write("   Meat spent: " + dnp.getData().meatSpent + NEW_LINE);
        }
        write(NEW_LINE + NEW_LINE + NEW_LINE);

        // Bottlenecks summary
        final List<DataNumberPair<String>> lostCombats = logData.getLostCombats();
        write("BOTTLENECKS" + NEW_LINE + "----------" + NEW_LINE);
        write("Sewered " + logData.getLogSummary().getSewer().getTurnsSpent() + " times for "
              + logData.getLogSummary().getSewer().getTrinketsFound() + " trinkets" + NEW_LINE);
        write("Spent " + logData.getLogSummary().get8BitRealm().getTurnsSpent()
              + " turns in the 8-Bit Realm" + NEW_LINE);
        write("Fought " + logData.getLogSummary().get8BitRealm().getBloopersFound() + " bloopers"
              + NEW_LINE);
        write("Fought " + logData.getLogSummary().get8BitRealm().getBulletsFound()
              + " bullet bills" + NEW_LINE);
        write("Spent " + logData.getLogSummary().getGoatlet().getTurnsSpent()
              + " turns in the Goatlet" + NEW_LINE);
        write("Fought " + logData.getLogSummary().getGoatlet().getDairyGoatsFound()
              + " dairy goats for " + logData.getLogSummary().getGoatlet().getCheeseFound()
              + " cheeses and " + logData.getLogSummary().getGoatlet().getMilkFound()
              + " glasses of milk" + NEW_LINE);
        write("Number of lost combats: " + lostCombats.size() + NEW_LINE);
        for (final DataNumberPair<String> dnp : lostCombats)
            write("     " + dnp + NEW_LINE);
        write(NEW_LINE);
        write("Free runaways: ");
        write(logData.getLogSummary().getFreeRunaways().toString());
        write(" overall");
        write(NEW_LINE);
    }

    private void write(
                       final String s) {
        if (s != null)
            log.append(s);
        else
            log.append(UsefulPatterns.EMPTY_STRING);
    }

    private void write(
                       final int i) {
        log.append(i);
    }

    /**
     * Enumeration to specify the wanted textual log output.
     */
    public static enum TextualLogVersion {
        TEXT_LOG, HTML_LOG, BBCODE_LOG;
    }
}

