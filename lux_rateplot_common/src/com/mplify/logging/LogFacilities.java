package com.mplify.logging;

import java.util.Formatter;

import org.slf4j.Logger;

import com.mplify.logging.LoglevelAid.Loglevel;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2010, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released under the MIT License (http://opensource.org/licenses/MIT) 
 *******************************************************************************
 *******************************************************************************
 * Odds and sods used in logging.
 *
 * Needs SLF4J API (http://www.slf4j.org/)
 * 
 * 2004.10.19 - Created
 * 2004.10.10 - Moved buildDurationString() from MrtgPortThread to here
 * 2004.10.20 - Stuff from M3pException has been moved here
 * 2004.11.30 - Http-specific stuff has been moved to LogFacilitiesForHttp
 * 2005.11.07 - Added comments
 * 2005.11.28 - Renamed some methods. moved the ones related to AlphaLayout to
 *              AlphaLayout.
 * 2005.11.28 - Moved to project m3p_tomcat_common, package com.mplify.logging             
 * 2006.XX.XX - Moved to project mp3_ignition, which is one of the lowermost 
 *              projects
 * 2009.01.19 - Reversed the order in which the stack trace is printed out in
 *              textifyStacktrace() 
 * 2009.08.28 - Switched from usage of StringBuffer to usage of StringBuilder 
 * 2010.10.26 - mangleString() tries to break a line at a place that makes more 
 *              sense
 * 2011.03.02 - Added logWithStacktrace()
 * 2011.03.18 - Modified "mangleString()" to fix line length counting and also
 *              added indentation
 * 2011.04.03 - Skipping the last two stackframes in logWithStacktrace()   
 * 2011.04.07 - Added makeExeMessage()
 * 2011.05.17 - makeExeMessage() reviewed, added makeCauseStack()
 * 2011.07.12 - formatForMe() has been moved from LogFacilities to here.
 * 2011.07.12 - logThatWeAreCalledNow() has been moved from HttpClientDataSource
 *              to here.
 * 2011.07.25 - encodeHtml() has been moved to here; it is used to HTMLized
 *              pure text       
 * 2011.08.03 - Review, unused code thrown away.    
 * 2011.10.19 - Adapted for LOG4J --> SLF4J migration
 ******************************************************************************/

public class LogFacilities {

    /**
     * In debug mode, the filer characters are not 'space' but '.' for part of a prepared string-of-spaces and '!' for
     * additionally appended spaces (which is costly). If you see too many '!', increase the size of the spaceStrings[]
     * array.
     */

    private final static boolean DEBUG = false;

    private final static char CHEAP_FILLER;
    private final static char EXPENSIVE_FILLER;

    static {
        if (DEBUG) {
            CHEAP_FILLER = '.';
            EXPENSIVE_FILLER = '!';
        } else {
            CHEAP_FILLER = ' ';
            EXPENSIVE_FILLER = ' ';
        }
    }

    /**
     * An array of only-space-strings of various length
     */

    private final static String[] spaceStrings = new String[50];

    static {
        spaceStrings[0] = "";
        for (int i = 1; i < spaceStrings.length; i++) {
            spaceStrings[i] = spaceStrings[i - 1] + CHEAP_FILLER;
        }
    }

    /**
     * Unreachable constructor
     */

    private LogFacilities() {
        // unreachable
    }

    /**
     * Helper for trimming; called often, should be efficient
     */

    public static String rightSideTrim(String x, int startIndexIn) {
        assert x != null;
        assert startIndexIn >= 0;
        int maxIndex = x.length();
        int startIndexCur = startIndexIn;
        while (startIndexCur < maxIndex && Character.isWhitespace(x.charAt(startIndexCur))) {
            startIndexCur++;
        }
        return x.substring(startIndexCur, maxIndex);
    }

    /*
     * Print out the classloader hierarchy. Very useful for debugging Tomcat
     */

    public static void printClassloaderHierarchy(Logger logger, Loglevel level) {
        if (LoglevelAid.isEnabledFor(logger, level)) {
            ClassLoader current = Thread.currentThread().getContextClassLoader();
            StringBuilder buf = new StringBuilder(current.toString().trim());
            // buf now contains a whole bunch of text; let's prettify it with a nice frame
            buf.insert(0, " +-----------------------------------------\n");
            for (int i = 0; i < buf.length(); i++) {
                if (buf.charAt(i) == '\n') {
                    buf.replace(i, i + 1, "\n |");
                    i += 2;
                }
            }
            buf.append("\n +-----------------------------------------\n");
            LoglevelAid.log(logger, level, buf.toString());
        }
    }

    /**
     * Transform unprintables in a String into their hex representation. Passing null yields "(null)"
     */

    public static String mangleString(String str) {
        return mangleString(str, -1, false);
    }

    /**
     * Transform unprintables in a String into their hex representation, but add CRs to print on several lines
     * Passing null yields "(null)"
     */

    public static String mangleString(String str, int maxColumnCount, boolean keepNewlines) {
        if (str == null) {
            return "(null)";
        }
        StringBuilder buf = new StringBuilder();
        int limit = str.length();
        int curColumn = 0;
        int myMaxColumnCount = maxColumnCount;
        if (myMaxColumnCount <= 0) {
            myMaxColumnCount = Integer.MAX_VALUE;
        }
        boolean addIndent = false;
        String indentStr = "    ";
        for (int i = 0; i < limit; i++) {
            char val = str.charAt(i);
            if (curColumn >= myMaxColumnCount - 1 && (val == ',' || val == ' ')) {
                // try to break at a good place
                buf.append(val);
                buf.append("\n");
                curColumn = 0;
                addIndent = true;
            } else {
                // standard case
                if (addIndent) {
                    buf.append(indentStr);
                    curColumn = indentStr.length();
                    addIndent = false;
                }
                if (keepNewlines && val == '\n') {
                    buf.append(val);
                    curColumn = 0;
                } else {
                    if ((val < 0x20) || (0x7F <= val && val <= 0xA0) || (0xFF < val)) {
                        buf.append("[");
                        String hex = Integer.toHexString(val);
                        if (hex.length() == 1) {
                            buf.append("0");
                        }
                        buf.append(hex);
                        buf.append("]");
                        curColumn = curColumn + 2 + Math.max(2, hex.length());
                    } else {
                        buf.append(val);
                        curColumn++;
                    }
                }
            }

        }
        return buf.toString();
    }

    /**
     * Helper function for printing: create a string consisting of n dashes. If 'n' is <=0 then the empty strings is
     * returned. TODO: Not very efficient, one should pre-buffer a number of those strings.
     */

    public static String getDashString(int n) {
        if (n <= 0) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < n; i++) {
            buf.append("-");
        }
        return buf.toString();
    }

    /**
     * Create a string of n spaces. if n<=0, "" is returned
     */

    public static String getSpaceString(int n) {
        if (n <= 0) {
            return "";
        } else if (n < spaceStrings.length) {
            return spaceStrings[n];
        } else {
            StringBuilder buf = new StringBuilder(spaceStrings[spaceStrings.length - 1]);
            for (int i = spaceStrings.length - 1; i < n; i++) {
                buf.append(EXPENSIVE_FILLER);
            }
            return buf.toString();
        }
    }

    /**
     * Mangle an array of byte into an ASCII dump
     */

    public static String asciiDump(byte[] data) {
        assert (data != null);
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            byte x = data[i];
            if ((x & 0x80) != 0 || x == 0x7F || x < 0x20) {
                // not ASCII or ASCII control - mask the byte and hexdump it
                buf.append("(0x" + Integer.toHexString(x & 0xFF) + ")");
            } else {
                // ASCII, append "as is"
                buf.append((char) (x));
            }
        }
        return buf.toString();
    }

    /**
     * Create a string to "save your ass". formatForMe() should be called rarely, so this is called rarely
     */

    private static String buildSaveMyAssFormatStr(Object... args) {
        assert args != null;
        StringBuilder buf = new StringBuilder("(null) or bad format string passed. There were " + args.length + " arguments");
        for (int i = 0; i < args.length; i++) {
            buf.append(" '%s'");
        }
        return buf.toString();
    }

    /**
     * Helper
     */

    private static String formatForMeLow(String formatStrIn, Object... args) {
        assert args != null;
        StringBuilder resultBuf = new StringBuilder();
        String formatStr = (formatStrIn != null ? formatStrIn : buildSaveMyAssFormatStr(args));
        try (Formatter formatter = new Formatter(resultBuf)) {
            formatter.format(formatStr, args).toString();
        } catch (Exception exe) {
            String msg = "Exception occurred during formatting: ";
            msg += exe.getClass().getName();
            if (exe.getMessage() != null) {
                msg += ": ";
                msg += exe.getMessage().trim();
            }
            System.err.println(msg);
        }
        return resultBuf.toString();
    }

    /**
     * Helper to format a "txt" with "objects" using the printf-like formatter.
     * See http://download.oracle.com/javase/1.5.0/docs/api/java/util/Formatter.html for formatting
     */

    public static String formatForMe(String formatStrIn, Object... args) {
        assert args != null;
        //
        // Now format. Problems may occur - in particular, args[] may be too short for the format specs...
        // A (null) args[i] yields "null". For objects and the %s format specifier, Java invokes .toString()
        // on the object (which could throw).
        //
        String output = formatForMeLow(formatStrIn, args);
        if (output == null) {
            // maybe "formatStrIn" was bad -- again, choosing a default formatStr
            output = formatForMeLow(null, args);
        }
        if (output == null) {
            // forget this formatting business
            output = "Could not properly format a string based on " + LogFacilities.mangleString(formatStrIn);
        }
        assert output != null;
        return output;
    }

    /*
     * A pretty expensive half-arsed encoder that should be replaced by org.apache.commons.lang.StringEscapeUtils and
     * for which the result should be buffered. From:
     * http://www.owasp.org/index.php/How_to_perform_HTML_entity_encoding_in_Java
     */

    public static void encodeHtml(String s, StringBuilder builder) {
        if (s != null) {
            int len = s.length();
            for (int i = 0; i < len; i++) {
                char c = s.charAt(i);
                if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9') {
                    builder.append(c);
                } else if (c == '\n') {
                    builder.append("<br>");
                } else if (c == ' ') {
                    builder.append("&nbsp;");
                } else if (c < ' ') {
                    // do nothing -- control character
                } else {
                    builder.append("&#");
                    builder.append((int) c);
                    builder.append(";");
                }
            }
        }
    }

    /**
     * Log the stack trace; useful to find out where you are especially in
     * a "callback" context. There is an internal loglevel check. 
     * 
     */

    public static void logThatWeAreNowHere(Logger logger, Loglevel level, String txt, Object... args) {
        // a null logger or null log level are enabled for nothing!
        if (LoglevelAid.isEnabledFor(logger, level)) {
            StackTraceElement[] stes = Thread.currentThread().getStackTrace();
            StringBuilder buf = LogFacilitiesForThrowables.textifyStacktrace("    |", stes, 2); // skip 2 frames
            buf.insert(0, formatForMe(txt, args).trim() + "\n");
            logger.debug(buf.toString());
        }
    }

}
