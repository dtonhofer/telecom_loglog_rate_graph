package com.mplify.logging;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2010, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released under the MIT License (http://opensource.org/licenses/MIT) 
 *******************************************************************************
 *******************************************************************************
 * Date generation
 *
 * TODO: Most of this probably needs to be thrown away.
 * 
 * 1999.02.00 - Working on it
 * 1999.10.24 - Uploaded to server
 * 1999.11.10 - FindSQLEscapable() and SQLize() separated into functions for
 *              mSQL and and Interbase; decision is made on SCMSGlobal's
 *              ThisMachine() return value
 * 1999.11.21 - MakeLogFilename() added
 * 1999.11.22 - MakeLogFilename() modified; now takes boolean 'addrandom'
 * 1999.11.25 - Added CoveredP() as system-wide way of 'interval cover'
 * 1999.11.26 - Added TextAMonth()
 * 1999.12.07 - Annoying initialization messages in static() removed
 * 1999.12.12 - FuseVectors() added
 * 2000.04.07 - Modfied naming for logfile
 * 2000.07.21 - Added function for taking apart parameters passed to main()
 * 2001.09.04 - Reformed for m-plify; most of the functions left out at first.
 * 2001.11.20 - Added mangleString()
 * 2001.11.20 - Added the MEDIUM format for writing dates
 * 2001.11.22 - Added verifyStopfile(), testForStopFile(), testWatchdogPort()
 * 2001.11.29 - Added onlyDigitsP()
 * 2002.01.17 - Added texters for Calendar values
 * 2002.01.22 - Added 'cookPolyline()'
 * 2002.03.21 - Added XML-escaping procedures
 * 2003.06.13 - Added setUpAlphaLayoutOnRootLogger()
 * 2004.01.13 - Indent now does a lookup in spaceStrings[]
 * 2004.01.20 - writeVector() reviewed and renamed writeStory()
 * 2004.03.04 - Added facilities for logging Web Requests
 * 2004.04.17 - Modified parseCommaSeparatedAtoms() somewhat
 * 2004.08.05 - scanServices() moved here from PropertiesReader
 * 2004.08.11 - Added loadPropertiesFromResource()
 * 2004.10.19 - Completly ripped apart until only 'date' facilities were left,
 *              then moved to m3p.utils.date and renamed to DateToText
 * 2005.05.02 - Nice weather today. The class becomes an enumeration type
 *              enumerating the different formats. It then exports non-static
 *              but thread-safe formatting methods. One could also add
 *              static one but we don't bother. Using a homegrown 'enumeration
 *              type' eliminates the need for a MAX_FORMAT and MIN_FORMAT.
 * 2005.05.02 - Fused in part of mm3p.utils.divers.DateParserFormatter. 
 *              Also texts Times - but that is of course a weakness because
 *              dates are dates and not times. Use LocalTime in preference to 
 *              this if you deal with times. 
 * 2005.10.18 - Frenchified, i.e. added ALTERNATE 8
 * 2009.01.26 - Moved to m3p_ignition (com.mplify.logging) in the standalone
 *              message server.
 * 2010.12.27 - Added buildDurationStringShort() 
 * 2012.01.03 - Added a buildDurationStringShort() that takes a BigDecimal,
 *              for Groovy.
 ******************************************************************************/

public class DateTexter {

    /*
     * Trivial timezones
     */

    private final static TimeZone DEFAULT = TimeZone.getDefault();
    private final static TimeZone UTC = TimeZone.getTimeZone("UTC");

    /*
     * Members. The DateToText has a 'name' and formats according to a
     * 'formatText'. The unique "SimpleDateFormat" (to which access is
     * synchronized) is set up with "formatText".
     */

    private final String name;
    private final SimpleDateFormat sdf; // TODO: Should be a per-instance pool,
                                        // not a single instance

    /*
     * Possible format values
     */

    static final public DateTexter COMPLETE;
    static final public DateTexter EXTENDED;
    static final public DateTexter NORMAL;
    static final public DateTexter SHORT;
    static final public DateTexter ALTERNATE0;
    static final public DateTexter ALTERNATE1;
    static final public DateTexter ALTERNATE2;
    static final public DateTexter ALTERNATE3;
    static final public DateTexter ALTERNATE4;
    static final public DateTexter ALTERNATE5;
    static final public DateTexter ALTERNATE6;
    static final public DateTexter ALTERNATE7;
    static final public DateTexter ALTERNATE8;
    static final public DateTexter ALTERNATE9;
    static final public DateTexter TIME0;
    static final public DateTexter TIME1;
    static final public DateTexter TIME2;

    /*
     * An list of the values, public but immutable
     */

    public final static List<DateTexter> LIST;

    /*
     * Static construction takes care to assign unique (and constant) numeric
     * identifiers to the various instances. They must be stable because they
     * are used in the database. At the same time, the values are assigned to
     * the 'list', which will then be made immutable and assigned to LIST.
     */

    static {
        ArrayList<DateTexter> list = new ArrayList();
        list.add(COMPLETE = new DateTexter("complete", "yyyy.MM.dd HH:mm:ss:SSS (EEEE) (zzzz)"));
        list.add(EXTENDED = new DateTexter("extended", "yyyy.MM.dd HH:mm:ss:SSS"));
        list.add(NORMAL = new DateTexter("normal", "yyyy.MM.dd HH:mm:ss"));
        list.add(SHORT = new DateTexter("short", "yyyy.MM.dd"));
        list.add(ALTERNATE0 = new DateTexter("alternate0", "yyyy-MM-dd HH:mm:ss"));
        list.add(ALTERNATE1 = new DateTexter("alternate1", "yyyy-MM-dd HH:mm"));
        list.add(ALTERNATE2 = new DateTexter("alternate2", "yyyy-MM-dd"));
        list.add(ALTERNATE3 = new DateTexter("alternate3", "dd/MM/yyyy HH:mm:ss"));
        list.add(ALTERNATE4 = new DateTexter("alternate4", "dd/MM/yyyy HH:mm"));
        list.add(ALTERNATE5 = new DateTexter("alternate5", "dd/MM/yyyy"));
        list.add(ALTERNATE6 = new DateTexter("alternate6", "yyyy.MM.dd"));
        list.add(ALTERNATE7 = new DateTexter("alternate7", "yyyy_MM_dd_HH_mm_ss"));
        list.add(ALTERNATE8 = new DateTexter("alternate8")); // special, needs
                                                             // more than a
                                                             // simple pattern
        list.add(ALTERNATE9 = new DateTexter("alternate9", "yyyyMMddHHmmss"));
        list.add(TIME0 = new DateTexter("time0", "HH:mm:ss:SSS"));
        list.add(TIME1 = new DateTexter("time1", "HH:mm:ss"));
        list.add(TIME2 = new DateTexter("time2", "HH:mm"));
        LIST = Collections.unmodifiableList(list);
    }

    /**
     * The constructor, can only called by this class. The class defines the
     * possible cases using the constructor and that's it. Constructor is
     * PRIVATE!
     */

    private DateTexter(String name, String formatText) {
        this.name = name;
        this.sdf = new SimpleDateFormat(formatText);
        // formatting always takes place in some timezone; by default, use the
        // 'default' timezone
        // which should be 'local time'
        this.sdf.setCalendar(Calendar.getInstance(TimeZone.getDefault()));
    }

    /**
     * Some don't have a format text
     */

    private DateTexter(String name) {
        this.name = name;
        if ("alternate8".equals(name)) {
            this.sdf = new SimpleDateFormat("d '$MONTH$' yyyy à HH:mm:ss");
        } else {
            throw new IllegalStateException("Can't be called for '" + name + "'");
        }
    }

    /**
     * Create a string for a date expressed in the passed TimeZone. Synchronized
     * so that threads do not mess up the SimpleDateFormat structure when
     * several try to format through this same method. Returns (null) if called
     * with a (null) 'when'. Throws if called with a (null) TimeZone.
     */

    public synchronized String textADate(Date when, TimeZone tz) {
        if (tz == null) {
            throw new IllegalArgumentException("The passed 'time zone' is (null)");
        }
        if (when == null) {
            return "(null)";
        }
        if ("alternate8".equals(name)) {
            final String[] mn = { "janvier", "février", "mars", "avril", "mai", "juin", "juillet", "août", "septembre", "octobre", "novembre", "décembre" };
            sdf.getCalendar().setTimeZone(tz);
            sdf.getCalendar().setTime(when);
            sdf.getCalendar().get(Calendar.MONTH);
            String template = sdf.format(when);
            return template.replace("$MONTH$", mn[sdf.getCalendar().get(Calendar.MONTH)]);
        } else {
            sdf.getCalendar().setTimeZone(tz);
            return sdf.format(when);
        }
    }

    /**
     * Create a string for a date expressed in the default TimeZone.
     */

    public synchronized String inDefault(Date when) {
        return textADate(when, DEFAULT);
    }

    /**
     * Create a string for a date expressed in UTC.
     */

    public synchronized String inUTC(Date when) {
        return textADate(when, UTC);
    }

    /**
     * Access the 'name'
     */

    public String getName() {
        return name;
    }

    /**
     * An old helper function: Translate the opaque Calendar weekday to a string
     */

    public static String weekdayValueToString(int x) {
        switch (x) {
        case Calendar.MONDAY:
            return "Monday";
        case Calendar.TUESDAY:
            return "Tuesday";
        case Calendar.WEDNESDAY:
            return "Wednesday";
        case Calendar.THURSDAY:
            return "Thursday";
        case Calendar.FRIDAY:
            return "Friday";
        case Calendar.SATURDAY:
            return "Saturday";
        case Calendar.SUNDAY:
            return "Sunday";
        default:
            return "<unknown weekday value " + x + ">";
        }
    }

    private static class DurationVector {

        public int days, hours, minutes, seconds;

    }

    /**
     * Helper
     */

    private static DurationVector breakDurationUp(long duration_s) {
        DurationVector res = new DurationVector();
        final int MINUTE_S = 60;
        final int HOUR_S = 60 * MINUTE_S;
        final int DAY_S = 24 * HOUR_S;
        res.days = (int) (duration_s / DAY_S);
        res.hours = (int) ((duration_s - res.days * DAY_S) / HOUR_S);
        res.minutes = (int) ((duration_s - res.days * DAY_S - res.hours * HOUR_S) / MINUTE_S);
        res.seconds = (int) ((duration_s - res.days * DAY_S - res.hours * HOUR_S - res.minutes * MINUTE_S));
        return res;
    }

    /**
     * Helper function, generates a duration string in h,m,s notation from a
     * duration in s
     */

    public static String buildDurationString(long duration_s, boolean noSeconds, boolean elide) {
        StringBuilder result = new StringBuilder();
        if (duration_s == 0) {
            return "0";
        }
        DurationVector dv = breakDurationUp(duration_s);
        boolean front = true; // indicates whether we are writing at the front
        if (dv.days > 0) {
            result.append(dv.days);
            result.append(" day");
            if (dv.days != 1) {
                result.append("s");
            }
            front = false;
        }
        if (dv.hours > 0 || (!front && !elide)) {
            if (!front) {
                result.append(", ");
            }
            result.append(dv.hours);
            result.append(" hour");
            if (dv.hours != 1) {
                result.append("s");
            }
            front = false;
        }
        if (dv.minutes > 0 || (!front && !elide)) {
            if (!front) {
                result.append(", ");
            }
            result.append(dv.minutes);
            result.append(" minute");
            if (dv.minutes != 1) {
                result.append("s");
            }
            front = false;
        }
        if (!noSeconds) {
            if (dv.seconds > 0 || (!front && !elide)) {
                if (!front) {
                    result.append(", ");
                }
                result.append(dv.seconds);
                result.append(" second");
                if (dv.seconds != 1) {
                    result.append("s");
                }
                front = false;
            }
        }
        return result.toString();
    }

    /**
     * Helper function, generates a duration string in h,m,s notation from a
     * duration in s This one is used by Groovy.
     */

    public static String buildDurationStringShort(BigDecimal duration_s) {
        return buildDurationStringShort(duration_s.longValue());
    }

    /**
     * Helper function, generates a duration string in h,m,s notation from a
     * duration in s
     */

    public static String buildDurationStringShort(long duration_s) {
        StringBuilder result = new StringBuilder();
        if (duration_s == 0) {
            return "0s";
        }
        DurationVector dv = breakDurationUp(duration_s);
        boolean front = true; // indicates whether we are writing at the front
        if (dv.days > 0) {
            result.append(dv.days);
            result.append(" days");
            front = false;
        }
        if (dv.hours > 0 || !front) {
            if (!front) {
                result.append(", ");
                if (dv.hours < 10) {
                    result.append("0");
                }
            }
            result.append(dv.hours);
            front = false;
        }
        if (dv.minutes > 0 || !front) {
            if (!front) {
                result.append(":");
                if (dv.minutes < 10) {
                    result.append("0");
                }
            }
            result.append(dv.minutes);
            front = false;
        }
        if (dv.seconds > 0 || !front) {
            if (!front) {
                result.append(":");
                if (dv.seconds < 10) {
                    result.append("0");
                }
            }
            result.append(dv.seconds);
            front = false;
        }
        result.append("s");
        return result.toString();
    }

}