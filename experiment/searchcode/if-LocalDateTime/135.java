package lib.easyjava.io.output;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import lib.easyjava.io.file.FileWriter;

/**
 * Logs status messages to a file during a program's execution
 *
 * @author Rob Rua (robrua@alumni.cmu.edu)
 */
public class Logger {
    public static enum Level {
        ERROR(3), FATAL(4), NULL(5), OUTPUT(1), VERBOSE(0), WARN(2);
        private final int importance;

        private Level(final int importance) {
            this.importance = importance;
        }

        public int getImportance() {
            return importance;
        }
    }

    private static final String DEFAULT_LOG_FILE = "default.log";
    private static final Map<File, Logger> instances = new HashMap<File, Logger>();
    private static Level staticLevel = Level.OUTPUT;
    private static boolean staticOnly = false;
    private static FileWriter staticWriter;

    /**
     * Closes the default log file. The log should be fine if this isn't called,
     * but it's ideal to call it if the default log was ever used
     */
    public static void closeDefaultLogger() {
        if(staticWriter != null) {
            try {
                staticWriter.close();
            }
            catch(final IOException e) {}
            staticWriter = null;
        }
    }

    /**
     * Writes an error-level exception to the default log
     *
     * @param exception
     *            the exception to log
     */
    public static void defaultError(final Exception exception) {
        defaultLog(exception, Level.ERROR);
    }

    /**
     * Writes an error-level message to the default log
     *
     * @param content
     *            the message to log
     */
    public static void defaultError(final String content) {
        defaultLog(content, Level.ERROR);
    }

    /**
     * Writes a fatal-level exception to the default log
     *
     * @param exception
     *            the exception to log
     */
    public static void defaultFatal(final Exception exception) {
        defaultLog(exception, Level.FATAL);
    }

    /**
     * Writes a fatal-level message to the default log
     *
     * @param content
     *            the message to log
     */
    public static void defaultFatal(final String content) {
        defaultLog(content, Level.FATAL);
    }

    /**
     * Writes the exception to the default log file at the given importance
     * level
     *
     * @param exception
     *            the exception to log
     * @param level
     *            the importance level of the message
     */
    public static void defaultLog(final Exception exception, final Level level) {
        defaultLog(exception.toString(), level);
    }

    /**
     * Writes the content to the default log file at the given importance level
     *
     * @param content
     *            the message to log
     * @param level
     *            the importance level of the message
     */
    public static void defaultLog(final String content, final Level level) {
        if(staticLevel == Level.NULL) {
            return;
        }

        if(staticWriter == null) {
            try {
                staticWriter = new FileWriter(DEFAULT_LOG_FILE, false);
            }
            catch(final IOException e) {
                e.printStackTrace();
            }
        }

        if(level == staticLevel || !staticOnly && level.importance > staticLevel.importance) {
            try {
                staticWriter.writeLine(LocalDateTime.now() + " - " + level + " - " + content);
            }
            catch(final IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Writes an output-level exception to the default log
     *
     * @param exception
     *            the exception to log
     */
    public static void defaultOutput(final Exception exception) {
        defaultLog(exception, Level.OUTPUT);
    }

    /**
     * Writes an output-level message to the default log
     *
     * @param content
     *            the message to log
     */
    public static void defaultOutput(final String content) {
        defaultLog(content, Level.OUTPUT);
    }

    /**
     * Writes a verbose-level exception to the default log
     *
     * @param exception
     *            the exception to log
     */
    public static void defaultVerbose(final Exception exception) {
        defaultLog(exception, Level.VERBOSE);
    }

    /**
     * Writes a verbose-level message to the default log
     *
     * @param content
     *            the message to log
     */
    public static void defaultVerbose(final String content) {
        defaultLog(content, Level.VERBOSE);
    }

    /**
     * Writes a warn-level exception to the default log
     *
     * @param exception
     *            the exception to log
     */
    public static void defaultWarn(final Exception exception) {
        defaultLog(exception, Level.WARN);
    }

    /**
     * Writes a warn-level message to the default log
     *
     * @param content
     *            the message to log
     */
    public static void defaultWarn(final String content) {
        defaultLog(content, Level.WARN);
    }

    /**
     * Attempts to retrieve the logger for the given file
     *
     * @param file
     *            the file to get the logger for
     * @return the logger for that file
     * @throws IOException
     *             if no logger exists for the file and one can't be created
     */
    public static Logger getLogger(final File file) throws IOException {
        Logger logger = instances.get(file);
        if(logger == null) {
            logger = new Logger(file);
        }

        return logger;
    }

    /**
     * Attempts to retrieve the logger for the given file path
     *
     * @param filePath
     *            the file to get the logger for
     * @return the logger for that file
     * @throws IOException
     *             if no logger exists for the file and one can't be created
     */
    public static Logger getLogger(final String filePath) throws IOException {
        return getLogger(new File(filePath));
    }

    /**
     * Returns the null logger which doesn't log anything
     *
     * @return the null logger
     */
    public static Logger nullLogger() {
        Logger logger = null;
        try {
            logger = new Logger(Level.NULL);
        }
        catch(final IOException e) {}

        return logger;
    }

    /**
     * Sets the logger level for the default logger. Logs above the importance
     * of the set level will also be shown if only is set false.
     *
     * @param level
     *            the level to set
     * @param only
     *            determines whether to only show logs at this level, or to also
     *            show logs of higher importance
     */
    public static void setDefaultLevel(final Level level, final boolean only) {
        staticLevel = level;
        staticOnly = only;
    }

    private Level level;
    private boolean only;
    private FileWriter writer;

    /**
     * Creates a new console logger with default importance level OUTPUT
     *
     * @throws IOException
     *             if an error occurs initializing the logger
     */
    public Logger() throws IOException {
        this((File)null, false, Level.OUTPUT, false);
    }

    /**
     * Creates a new logger with default importance level OUTPUT
     *
     * @param file
     *            the file to log to
     * @throws IOException
     *             if the file can't be opened
     */
    public Logger(final File file) throws IOException {
        this(file, true, Level.OUTPUT, false);
    }

    /**
     * Creates a new logger with default importance level OUTPUT
     *
     * @param file
     *            the file to log to
     * @param append
     *            whether to append to the file
     * @throws IOException
     *             if the file can't be opened
     */
    public Logger(final File file, final boolean append) throws IOException {
        this(file, append, Level.OUTPUT, false);
    }

    /**
     * Creates a new logger
     *
     * @param file
     *            the file to log to
     * @param append
     *            whether to append to the file
     * @param level
     *            the minimum importance level to show logs from
     * @throws IOException
     *             if the file can't be opened
     */
    public Logger(final File file, final boolean append, final Level level) throws IOException {
        this(file, append, level, false);
    }

    /**
     * Creates a new logger
     *
     * @param file
     *            the file to log to
     * @param append
     *            whether to append to the file
     * @param level
     *            the minimum importance level to show logs from
     * @param only
     *            whether to show ONLY the logs at the given level or also those
     *            above that level
     * @throws IOException
     *             if the file can't be opened
     */
    public Logger(final File file, final boolean append, final Level level, final boolean only) throws IOException {
        if(file != null) {
            writer = new FileWriter(file, append);
        }

        this.level = level;
        this.only = only;
        instances.put(file, this);
    }

    /**
     * Creates a new console logger
     *
     * @param level
     *            the minimum importance level to show logs from
     * @throws IOException
     *             if an error occurs initializing the logger
     */
    public Logger(final Level level) throws IOException {
        this((File)null, false, level, false);
    }

    /**
     * Creates a new console logger
     *
     * @param level
     *            the minimum importance level to show logs from
     * @param only
     *            whether to show ONLY the logs at the given level or also those
     *            above that level
     * @throws IOException
     *             if an error occurs initializing the logger
     */
    public Logger(final Level level, final boolean only) throws IOException {
        this((File)null, false, level, only);
    }

    /**
     * Creates a new logger with default importance level OUTPUT
     *
     * @param filePath
     *            the file to log to
     * @throws IOException
     *             if the file can't be opened
     */
    public Logger(final String filePath) throws IOException {
        this(filePath, true, Level.OUTPUT, false);
    }

    /**
     * Creates a new logger with default importance level OUTPUT
     *
     * @param filePath
     *            the file to log to
     * @param append
     *            whether to append to the file
     * @throws IOException
     *             if the file can't be opened
     */
    public Logger(final String filePath, final boolean append) throws IOException {
        this(filePath, append, Level.OUTPUT, false);
    }

    /**
     * Creates a new logger
     *
     * @param filePath
     *            the file to log to
     * @param append
     *            whether to append to the file
     * @param level
     *            the minimum importance level to show logs from
     * @throws IOException
     *             if the file can't be opened
     */
    public Logger(final String filePath, final boolean append, final Level level) throws IOException {
        this(filePath, append, level, false);
    }

    /**
     * Creates a new logger
     *
     * @param filePath
     *            the file to log to
     * @param append
     *            whether to append to the file
     * @param level
     *            the minimum importance level to show logs from
     * @param only
     *            whether to show ONLY the logs at the given level or also those
     *            above that level
     * @throws IOException
     *             if the file can't be opened
     */
    public Logger(final String filePath, final boolean append, final Level level, final boolean only) throws IOException {
        this(new File(filePath), append, level, only);
    }

    /**
     * Closes the log file
     */
    public void close() {
        if(writer != null) {
            try {
                writer.close();
            }
            catch(final IOException e) {}
        }
    }

    /**
     * Writes an error-level exception to the log
     *
     * @param exception
     *            the exception to log
     */
    public void error(final Exception exception) {
        log(exception, Level.ERROR);
    }

    /**
     * Writes an error-level message to the log
     *
     * @param content
     *            the message to log
     */
    public void error(final String content) {
        log(content, Level.ERROR);
    }

    /**
     * Writes a fatal-level exception to the log
     *
     * @param exception
     *            the exception to log
     */
    public void fatal(final Exception exception) {
        log(exception, Level.FATAL);
    }

    /**
     * Writes a fatal-level message to the log
     *
     * @param content
     *            the message to log
     */
    public void fatal(final String content) {
        log(content, Level.FATAL);
    }

    /**
     * Writes the exception to the log file at the given importance level
     *
     * @param exception
     *            the exception to log
     * @param level
     *            the importance level of the message
     */
    public void log(final Exception exception, final Level level) {
        log(exception.toString(), level);
    }

    /**
     * Writes the content to the log file at the given importance level
     *
     * @param content
     *            the message to log
     * @param level
     *            the importance level of the message
     */
    public void log(final String content, final Level level) {
        if(this.level == Level.NULL) {
            return;
        }

        if(writer == null) {
            System.err.println(content);
            return;
        }

        if(level == this.level || !only && level.importance > this.level.importance) {
            try {
                writer.writeLine(LocalDateTime.now() + " - " + level + " - " + content);
            }
            catch(final IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Writes an output-level exception to the log
     *
     * @param exception
     *            the exception to log
     */
    public void output(final Exception exception) {
        log(exception, Level.OUTPUT);
    }

    /**
     * Writes an output-level message to the log
     *
     * @param content
     *            the message to log
     */
    public void output(final String content) {
        log(content, Level.OUTPUT);
    }

    /**
     * Sets the logger level. Logs above the importance of the set level will
     * also be shown if only is set false.
     *
     * @param level
     *            the level to set
     * @param only
     *            determines whether to only show logs at this level, or to also
     *            show logs of higher importance
     */
    public void setLevel(final Level level, final boolean only) {
        this.level = level;
        this.only = only;
    }

    /**
     * Writes a verbose-level exception to the log
     *
     * @param exception
     *            the exception to log
     */
    public void verbose(final Exception exception) {
        log(exception, Level.VERBOSE);
    }

    /**
     * Writes a verbose-level message to the log
     *
     * @param content
     *            the message to log
     */
    public void verbose(final String content) {
        log(content, Level.VERBOSE);
    }

    /**
     * Writes a warn-level exception to the log
     *
     * @param exception
     *            the exception to log
     */
    public void warn(final Exception exception) {
        log(exception, Level.WARN);
    }

    /**
     * Writes a warn-level message to the log
     *
     * @param content
     *            the message to log
     */
    public void warn(final String content) {
        log(content, Level.WARN);
    }
}

