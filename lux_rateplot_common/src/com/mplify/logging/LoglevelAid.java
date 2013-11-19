package com.mplify.logging;

import org.slf4j.Logger;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2010, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released under the MIT License (http://opensource.org/licenses/MIT) 
 *******************************************************************************
 *******************************************************************************
 * A function that allows to "log by level". This used to be possible in
 * Log4J, but no longer is possible in SLF4J, at least in the current 
 * instantiation (SLF4J API 1.6.1)
 * 
 * There is a function for checking whether a Logger can log at a given Level:
 * 
 * isEnabledFor(Logger logger, Level level)
 * 
 * Then there are functions that log via a Logger at a given Level, each taking
 * various arguments:
 * 
 * String
 * Throwable
 * Format String + Array Of Objects, declared as varargs.
 * (The SLF4J functions taking a "Format String + Object" or 
 *  "Format String + Object + Object" exist to avoid creating the Array of
 *  Objects in a vararg situation I suppose; add them?
 * How about:
 * Format String + Throwable + Array Of Objects
 *                      
 * 2011.10.19 - Created
 ******************************************************************************/

public class LoglevelAid {

    /**
     * Allowed levels
     * Import using "import com.mplify.logging.LogLevel.Level"
     * These constants are ordered by their declaration order: 
     * TRACE < DEBUG < INFO < WARN < ERROR
     */

    public static enum Loglevel {
        
        TRACE, DEBUG, INFO, WARN, ERROR

    }

    /**
     * Cannot be instantiated
     */

    private LoglevelAid() {
        // NOP
    }

    /**
     * Log at the specified level. If the "logger" is null, nothing is logged.
     * If the "level" is null, nothing is logged. If the "txt" is null,
     * behaviour depends on the SLF4J implementation.
     */

    public static void log(Logger logger, Loglevel level, String txt) {
        if (logger != null && level != null) {
            switch (level) {
            case TRACE:
                logger.trace(txt);
                break;
            case DEBUG:
                logger.debug(txt);
                break;
            case INFO:
                logger.info(txt);
                break;
            case WARN:
                logger.warn(txt);
                break;
            case ERROR:
                logger.error(txt);
                break;
            }
        }
    }
    
    /**
     * Log at the specified level. If the "logger" is null, nothing is logged.
     * If the "level" is null, nothing is logged. If the "format" or the "argArray"
     * are null, behaviour depends on the SLF4J-backing implementation.
     */

    public static void log(Logger logger, Loglevel level, String format, Object[] argArray) {
        if (logger != null && level != null) {
            switch (level) {
            case TRACE:
                logger.trace(format, argArray);
                break;
            case DEBUG:
                logger.debug(format, argArray);
                break;
            case INFO:
                logger.info(format, argArray);
                break;
            case WARN:
                logger.warn(format, argArray);
                break;
            case ERROR:
                logger.error(format, argArray);
                break;
            }
        }
    }

    /**
     * Log at the specified level, with a Throwable on top. If the "logger" is null, nothing is logged.
     * If the "level" is null, nothing is logged. If the "format" or the "argArray" or the "throwable"
     * are null, behaviour depends on the SLF4J-backing implementation.
     */
    
    public static void log(Logger logger, Loglevel level, String txt, Throwable throwable) {
        if (logger != null && level != null) {
            switch (level) {
            case TRACE:
                logger.trace(txt, throwable);
                break;
            case DEBUG:
                logger.debug(txt, throwable);
                break;
            case INFO:
                logger.info(txt, throwable);
                break;
            case WARN:
                logger.warn(txt, throwable);
                break;
            case ERROR:
                logger.error(txt, throwable);
                break;
            }
        }
    }
    
    /**
     * Check whether a SLF4J logger is enabled for a certain loglevel. 
     * If the "logger" or the "level" is null, false is returned.
     */

    public static boolean isEnabledFor(Logger logger, Loglevel level) {
        boolean res = false;
        if (logger != null && level != null) {
            switch (level) {
            case TRACE:
                res = logger.isTraceEnabled();
                break;
            case DEBUG:
                res = logger.isDebugEnabled();
                break;
            case INFO:
                res = logger.isInfoEnabled();
                break;
            case WARN:
                res = logger.isWarnEnabled();
                break;
            case ERROR:
                res = logger.isErrorEnabled();
                break;
            }
        }
        return res;
    }
}
