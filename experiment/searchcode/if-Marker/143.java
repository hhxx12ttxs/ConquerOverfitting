package com.c4biz.osgiutils.logging.reader;

public final class LoggingConstants {
	
	private LoggingConstants() {
		// utility class
	}
	
	/**
	 * If Logging using slf4j API and Logback as backend you can add Marker.
	 * you can build a Marker- Graph - if Marker IS_BUNDLE is contained, then
	 * the Marker Name is the Bundles Symbolic name.
	 */
	public static final String IS_BUNDLE_MARKER = "OSGI_BUNDLE";	 //$NON-NLS-1$
	
	/**
	 * If Logging using slf4j API and Logback as backend you can add Marker.
	 * you can build a Marker- Graph - if Marker IS_BUNDLE is contained, then
	 * the Marker Name is the Bundles Symbolic name.
	 * If no Bundle is contained in the LogEntry, NO_BUNDLE was printed
	 */
	public static final String NO_BUNDLE = "?";	 //$NON-NLS-1$
	
	/**
	 * ExtendedLogEntry contain a Logger. If those extended LogEntries were catched
	 * from your Listener and newly logged using slf4j, the Loggername was used,
	 * and a EXTENDED_OSGI_LOG Marker added
	 * if no LoggerName or BundleName exists, EXTENDED_OSGI_LOG was used as loggername 
	 */
	public static final String IS_EXTENDED_OSGI_LOG_MARKER ="EXTENDED_OSGI_LOG"; //$NON-NLS-1$
	
	/**
	 * LogEntry were catched from your Listener and newly logged using slf4j, 
	 * the Bundlename was usedm if exists - otherwise OSGI_LOG was used,
	 * and a OSGI_LOG Marker added
	 */
	public static final String IS_OSGI_LOG_MARKER ="OSGI_LOG"; //$NON-NLS-1$
	
	/**
	 * Bundlenames in LogEntries can be surrounded by a Prefix and Postfix
	 * per ex. ...the log... - [B:MyBundleName]
	 */
	public static final String BUNDLE_PREFIX = " [B:"; //$NON-NLS-1$

	/**
	 * Bundlenames in LogEntries can be surrounded by a Prefix and Postfix
	 * per ex. ...the log... - [B:MyBundleName]
	 */
	public static final String BUNDLE_POSTFIX = "] "; //$NON-NLS-1$
	
	/**
	 * Service-references in LogEntries can be surrounded by a Prefix and Postfix
	 * per ex. ...the log... - [S:MyBundleName]
	 */
	public static final String SERVICE_PREFIX = " [S:"; //$NON-NLS-1$

	/**
	 * Service-references in LogEntries can be surrounded by a Prefix and Postfix
	 * per ex. ...the log... - [S:MyBundleName]
	 */
	public static final String SERVICE_POSTFIX = "] "; //$NON-NLS-1$

}
