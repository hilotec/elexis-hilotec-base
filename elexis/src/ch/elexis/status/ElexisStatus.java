package ch.elexis.status;

import org.eclipse.core.runtime.Status;

/** 
 * This class represents a Status of the Elexis Application. It can be logged or shown to the user or even ignored, depending
 * on the logLevel etc. and the StatusHandler implementation.
 * <b>
 * Following defined status levels are taken from <link>ch.elexis.util.Log</link> class.
 * Based on their values the StatusHandler implementation can control the logging.
 * Default value is ERRORS.
 * </b>
 */
public class ElexisStatus extends Status {

	public static final int LOG_FATALS = 1;
	public static final int LOG_ERRORS = 2;
	public static final int LOG_WARNINGS = 3;
	public static final int LOG_INFOS = 4;
	public static final int LOG_DEBUGMSG = 5;
	public static final int LOG_TRACE = 6;
	
	private int logLevel;
	
	public ElexisStatus(int severity, String pluginId, int code, String message,
			Exception exception) {
		super(severity, pluginId, code, message, exception);
		this.logLevel = LOG_ERRORS;
	}
	
	public ElexisStatus(int severity, String pluginId, int code,
			String message, Exception exception, int logLevel) {
		super(severity, pluginId, code, message, exception);
		this.logLevel = logLevel;
	}

	public ElexisStatus(int severity, String pluginId, int code,
			String message, int logLevel) {
		super(severity, pluginId, code, message, null);
		this.logLevel = logLevel;
	}
	
	public int getLogLevel() {
		return logLevel;
	}
}
