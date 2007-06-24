/*
 * Debug
 */

package org.iatrix.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ch.elexis.util.Log;

/**
 *
 * Class for managing debugging output
 *
 * @author danlutz@watz.ch
 */
public class Debug {
    private static final int NONE = 0;
    public static final int ERROR = 1;
    public static final int INFO = 2;
    public static final int DEBUG = 3;
    public static final int DEBUG2 = 4;
    
    private static final int LEVEL = DEBUG;
    
    private static List marks = new ArrayList();
    
    private static final boolean EXCEPTIONS = true;
    private static final boolean LOG = true;
    
    private static Log elexisLog = null;
    
    public static void exception(Throwable t) {
        if (LEVEL > NONE && EXCEPTIONS) {
            t.printStackTrace(System.err);
        }
    }
    
    public static void log(String prefix, String msg) {
        log(DEBUG, prefix, msg);
    }
    
    public static void log(int level, String prefix, String msg) {
        log(level, prefix + ": " + msg);
    }

    public static void log(String msg) {
        log(DEBUG, msg);
    }
    
    public static void log(int level, String msg) {
        if (LEVEL >= level && LOG) {
            String levelText = "";
            switch (level) {
            case ERROR:
                levelText = "ERROR: ";
                break;
            case INFO:
                levelText = "INFO: ";
                break;
            case DEBUG:
                levelText = "DEBUG: ";
                break;
            }
            println(levelText + msg);
        }
    }
    
    public static void markStart(String msg) {
        if (LEVEL > NONE) {
            Long millis = new Long(System.currentTimeMillis());
            marks.add("start " + msg + " " + millis);
        }
    }
    
    public static void markStop(String msg) {
        if (LEVEL > NONE) {
            Long millis = new Long(System.currentTimeMillis());
            marks.add("stop  " + msg + " " + millis);
        }
    }
    
    public static void outputMarks() {
        if (LEVEL > NONE) {
            for (Iterator it = marks.iterator(); it.hasNext();) {
                println(it.next().toString());
            }

            marks.clear();

            println("-------------------");
        }
    }
    
    private static void println(String s) {
        if (elexisLog == null) {
            elexisLog = Log.get("iatrix");
        }
        
        //Calendar cal = Calendar.getInstance();
        //String time = DateTime.formatTime(cal.getTime());
        Thread thread = Thread.currentThread();
        String threadString = thread.getName();
        
        String msg = /*time +*/ " [" + threadString + "] " + s;
        
        //System.err.println(msg);
        elexisLog.log(msg, Log.DEBUGMSG);
    }
}
