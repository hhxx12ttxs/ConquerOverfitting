package org.slf4j.impl;

import java.util.logging.Level;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

/**
 * Route slf4j logging events to a {@link java.util.logging.Logger} instance.
 * <p>
 * The mapping of slf4j events to {@link java.util.logging.Logger} events
 * <table>
 * <tr>
 * <th>Slf4J: {@link org.slf4j.Logger}</th>
 * <th>Java Util Logging: {@link java.util.logging.Logger}</th>
 * </tr>
 * <tr>
 * <td>{@link org.slf4j.Logger#trace(String)}</td>
 * <td>{@link java.util.logging.Logger#finest(String)}</td>
 * </tr>
 * <tr>
 * <td>{@link org.slf4j.Logger#debug(String)}</td>
 * <td>{@link java.util.logging.Logger#fine(String)}</td>
 * </tr>
 * <tr>
 * <td>{@link org.slf4j.Logger#info(String)}</td>
 * <td>{@link java.util.logging.Logger#info(String)}</td>
 * </tr>
 * <tr>
 * <td>{@link org.slf4j.Logger#warn(String)}</td>
 * <td>{@link java.util.logging.Logger#warning(String)}</td>
 * </tr>
 * <tr>
 * <td>{@link org.slf4j.Logger#error(String)}</td>
 * <td>{@link java.util.logging.Logger#severe(String)}</td>
 * </tr>
 * </table>
 */
public class JulLogger extends MarkerIgnoringBase {
	private static final long serialVersionUID = -4095140620852805486L;
	private java.util.logging.Logger logger;
	private static final Level TRACE = Level.FINEST;
	private static final Level DEBUG = Level.FINE;
	private static final Level INFO = Level.INFO;
	private static final Level WARN = Level.WARNING;
	private static final Level ERROR = Level.SEVERE;

	public JulLogger(String name) {
		logger = java.util.logging.Logger.getLogger(name);
	}

	@Override
	public String getName() {
		return logger.getName();
	}

	@Override
	public boolean isTraceEnabled() {
		return logger.isLoggable(TRACE);
	}

	private void log(Level level, String msg, Throwable t) {

	}

	private void formattedLog(Level level, String format, Object... args) {
		if (!logger.isLoggable(level)) {
			return;
		}

		FormattingTuple msg = MessageFormatter.format(format, args);
		log(level, msg.getMessage(), null);
	}

	/**
	 * @see JulLogger#TRACE
	 * @see java.util.logging.Level#FINEST
	 */
	@Override
	public void trace(String msg) {
		log(TRACE, msg, null);
	}

	/**
	 * @see JulLogger#TRACE
	 * @see java.util.logging.Level#FINEST
	 */
	@Override
	public void trace(String format, Object arg) {
		formattedLog(TRACE, format, arg);
	}

	/**
	 * @see JulLogger#TRACE
	 * @see java.util.logging.Level#FINEST
	 */
	@Override
	public void trace(String format, Object arg1, Object arg2) {
		formattedLog(TRACE, format, arg1, arg2);
	}

	/**
	 * @see JulLogger#TRACE
	 * @see java.util.logging.Level#FINEST
	 */
	@Override
	public void trace(String format, Object[] argArray) {
		formattedLog(TRACE, format, argArray);
	}

	/**
	 * @see JulLogger#TRACE
	 * @see java.util.logging.Level#FINEST
	 */
	@Override
	public void trace(String msg, Throwable t) {
		log(TRACE, msg, t);
	}

	@Override
	public boolean isDebugEnabled() {
		return logger.isLoggable(DEBUG);
	}

	@Override
	public void debug(String msg) {
		log(DEBUG, msg, null);
	}

	@Override
	public void debug(String format, Object arg) {
		formattedLog(DEBUG, format, arg);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		formattedLog(DEBUG, format, arg1, arg2);
	}

	@Override
	public void debug(String format, Object[] argArray) {
		formattedLog(DEBUG, format, argArray);
	}

	@Override
	public void debug(String msg, Throwable t) {
		log(DEBUG, msg, t);
	}

	@Override
	public boolean isInfoEnabled() {
		return logger.isLoggable(INFO);
	}

	@Override
	public void info(String msg) {
		log(INFO, msg, null);
	}

	@Override
	public void info(String format, Object arg) {
		formattedLog(INFO, format, arg);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		formattedLog(INFO, format, arg1, arg2);
	}

	@Override
	public void info(String format, Object[] argArray) {
		formattedLog(INFO, format, argArray);
	}

	@Override
	public void info(String msg, Throwable t) {
		log(INFO, msg, t);
	}

	@Override
	public boolean isWarnEnabled() {
		return logger.isLoggable(WARN);
	}

	@Override
	public void warn(String msg) {
		log(WARN, msg, null);
	}

	@Override
	public void warn(String format, Object arg) {
		formattedLog(WARN, format, arg);
	}

	@Override
	public void warn(String format, Object[] argArray) {
		formattedLog(WARN, format, argArray);
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		formattedLog(WARN, format, arg1, arg2);
	}

	@Override
	public void warn(String msg, Throwable t) {
		log(WARN, msg, t);
	}

	@Override
	public boolean isErrorEnabled() {
		return logger.isLoggable(ERROR);
	}

	@Override
	public void error(String msg) {
		log(ERROR, msg, null);
	}

	@Override
	public void error(String format, Object arg) {
		formattedLog(ERROR, format, arg);
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		formattedLog(ERROR, format, arg1, arg2);
	}

	@Override
	public void error(String format, Object[] argArray) {
		formattedLog(ERROR, format, argArray);
	}

	@Override
	public void error(String msg, Throwable t) {
		log(ERROR, msg, t);
	}

}

