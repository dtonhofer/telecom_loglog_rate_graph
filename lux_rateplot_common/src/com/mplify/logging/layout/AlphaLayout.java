package com.mplify.logging.layout;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.status.ErrorStatus;
import ch.qos.logback.core.status.WarnStatus;

import com.mplify.checkers.Check;
import com.mplify.logging.LogFacilitiesForThrowables;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2010, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released under the MIT License (http://opensource.org/licenses/MIT) 
 *******************************************************************************
 *******************************************************************************
 * An Layout that formats log events into strings for the Logback
 * logging framework (http://logback.qos.ch)
 * 
 * See http://logback.qos.ch/manual/layouts.html
 * 
 * A log event is displayed in a 'strict-column' manner, i.e. the output is as 
 * follows, with fixed-width columns (which means that you have to have a 
 * -wide- screen or a small font):
 *
 * | thread | timestamp | log level | logger name | log message
 *
 * The 'thread' and 'log level' columns can be switched on and off individually.
 *
 * > The thread column contains the name of the thread for which the logging
 *   event occurred.
 *
 * > The timestamp columns contains a timestamp in "YYYY.MM.DD hh:mm:ss:mmm"
 *   format.
 *
 * > The log level column contains the level name i.e. TRACE, DEBUG, INFO, WARN,
 *   ERROR, FATAL
 *
 * > The logger name column contains the name of the logger, often the
 *   fully-qualified method name i.e. something like "pack.subpack.class.method"
 *   or "pack.subpack.class$innerClass.method"
 *   
 *   The name is right-justified in the column and cut off on its *left* if it's
 *   too long to fit the column. That way, one may lose part of the package name
 *   but not the method name.
 *
 * > The log message contains the event message, preceded by a line number.
 *   If there is only one line:   "00>Text"
 *   If there are several lines:  "00>Text"
 *                                "01>Text" etc...
 *                                
 * > In case of a Throwable is logged, the Thowable class name, message and stack trace
 *   are written as a table with causes tacked on to the right. 
 *   
 *   As an example of a Throwable without cause:
 *   
 *   01> **** | [com.mplify.masterslave.clientside_exceptions.CommunicationException]                                
 *   02> **** | [rxid=4e9c5004,[RX_PortRequest]]CLOSE did not receive response before wakeup but was in tasks-in-prog
 *   03> **** +------------------------------------------------------------------------------------------------------
 *   04> **** | com.mplify.masterslave.clientside.Master.execute(Master.java:538)                                    
 *   05> **** | com.mplify.masterslave.comm_client.CommClient.executeTask(CommClient.java:318)                       
 *   06> **** | com.mplify.masterslave.comm_port_proxy.CommPortProxy.close(CommPortProxy.java:151)                   
 *   07> **** | com.mplify.msgserver.gsmterminal.GsmTerminal.close(GsmTerminal.java:330)                             
 *   08> **** | com.mplify.msgserver.dd_gsmterminal.DealerDeviceGsmTerminal.cleanup(DealerDeviceGsmTerminal.java:471)
 *   09> **** | com.mplify.msgserver.ddr.DealerDeviceRunnable.run(DealerDeviceRunnable.java:379)                     
 *   10> **** | java.lang.Thread.run(Thread.java:662)                                                                
 * 
 * 2001.11.19 - Implemented for the first time based on Log4J.
 * 2003.01.20 - Modified for the new Log4J Category -> Logger; also, one can now
 *              switch off the 'thread' display, which is useful if you are in
 *              the IDE, which does not have too large a window.
 *              This has also become a JUnit test case.... Problems with JUnit
 *              approach:
 *              > lots of code needed to implement test harness; JUnit says
 *                'the names of the procedures shall be' but not much more
 *              > often difficult to know how/what to test and to actually
 *                compare the results against something (against an algorithm
 *                that does the same stuff? against a file? what?)
 *              > auxiliary stuff may be needed (files, etc)
 *              > how do you know testing code works; debugging it takes time
 *              > time better spent making sure the real code works, maybe
 *              > adding test cases involves generally a lot of copy & paste
 *                which will come back to haunt you later
 *              > some cases may either not be tested or not be implemented because
 *                testing them is difficult (the latter may be also a good thing)
 *              > have to weigh how much to test with JUnit, how much should be
 *                tested 'by hand' at writing time
 *              Note that the JUnit test case has been implemented as an inner
 *              class of the class-to-test; sounds like a good idea...
 * 2003.03.28 - The JUnit test case has been moved to its own class.
 *              Native function calls to obtain Process Id (PID) have been
 *              added.
 * 2003.04.01 - Moved to a new packe so that one can feed the resulting jar
 *              to Tomcat without upsetting it (i.e. this class must be on
 *              its own in a jar visible to Tomcat through its common
 *              classloader. Otherwise classloading goes haywire as the
 *              common classloader will look for classes that are in the
 *              'shared' directory and thus invisible to it)
 * 2003.06.13 - Review due to Eclipse migration 
 * 2003.07.06 - Copied to the com.mplify.win2k.diskmon package from m3p.utils.log4j
 * 2004.10.05 - Fixed minor nag concerning reload of libObtainPID.so
 * 2005.02.03 - Justification of thread column done with right-alignement
 * 2006.01.26 - Slight review during debugging and while trying to find out
 *              why calls to native methods suddenly give problems.
 * 2008.06.15 - Handling a throwable gives an array which may have null Strings?
 *              Added special check towards the end of format()
 * 2009.01.31 - Commented out TID/PID handling because printing out PID/TID
 *              is not really useful and leads to too much baggage.
 *              To be cleaned up at some time.
 *              Also changed code so that the whole stack trace is printed,
 *              not only the elided part obtained with Log4J's ti.getThrowableStrRep()
 *              Additionally, formatInt() reduces to an array lookup instead of
 *              a String operation. Finally, used StringBuilder where possible.
 * 2009.02.20 - Increased width of thread name field from 15 to 30
 * 2009.08.26 - Class moved to a new, separate package - com.mplify.log4jlogging
 * 2011.10.17 - Code was reorganized (cut into smaller methods) for readability.
 * 2011.10.19 - Review commentary, which is old.
 *              Removed the commentary on creating a shared object library to
 *              display the TID/PID via JNI calls as that is cumbersome and no longer
 *              of interest. Removed the attendent code for good (it was only
 *              commented out up to now) 
 *              Migrated from calls to Log4J to calls to logback (http://logback.qos.ch)
 *              Package reorganized.
 * 2011.10.26 - Debugging
 * 2011.10.31 - More debugging and styling
 * 2012.06.07 - Moved code to print Throwable out to a separate class for clarity
 * 2012.12.10 - appendOneMessage(), appendOneLine(), handleLevel() made static
 * 
 * TODO: DOES THIS NEED ANY LOCKING??? Probably not as it survived well so far.
 * TODO: Relative time since start of server
 * TODO: ALPHA_DATE_FORMAT_INSTANCE cache
 ******************************************************************************/

public class AlphaLayout extends LayoutBase<ILoggingEvent> {

    private static final String CLASS = AlphaLayout.class.getName();

    // a long which continuously increases to count the log message
    private final static AtomicLong contNumber = new AtomicLong();

    // if true, also displays 'date change' lines, which look like this: ****** DATE CHANGE --> 2003.01.23 ******
    private final boolean displayDateChange;

    // separator between fields
    private static final char SEPARATOR_CHAR = '|';

    // a long whitespace string used to add spaces
    private static final String SPACE_FILLER = "                                                                                 ";
    private static final int SPACE_FILLER_LEN = SPACE_FILLER.length();

    // contains the YYYY-MM-DD to be printed out, volatile because accessible by multiple threads; used to display date change
    private static volatile String storedThisDayYMD = "";

    // format for writing out a date
    private static final String ALPHA_DATE_FORMAT_STRING = "yyyy.MM.dd HH:mm:ss:SSS";

    // used in getting a substring
    private static final int ALPHA_DATE_FORMAT_YMD_LENGTH = "yyyy.MM.dd".length();

    // the class that formats; not static; also needs synchronized access
    private final SimpleDateFormat ALPHA_DATE_FORMAT_INSTANCE = new SimpleDateFormat(ALPHA_DATE_FORMAT_STRING);

    // index of column holding the continuously increasing number of the message
    private static final int COLINDEX_CONTNUMBER = 0;

    // index of column holding the thread name, see columns[]
    private static final int COLINDEX_THREADNAME = 1;

    // index of column holding the timestamp, see columns[]
    private static final int COLINDEX_TIMESTAMP = 2;

    // index of column holding the name of the logger, see columns[]
    private static final int COLINDEX_LOGGERNAME = 3;

    // index of column holding the log level, see columns[]
    private static final int COLINDEX_LOGLEVEL = 4;

    // index of column holding the message, see columns[]
    private static final int COLINDEX_LOGMESSAGE = 5;

    // array of information about the columns; filled in constructor
    private final ColumnDesc[] columns = new ColumnDesc[6];

    // width of the field holding the continuous number
    private static final int WIDTH_CONTNUMBER = 8;

    // width of the field holding the log level (DEBUG, INFO, ...)
    private static final int WIDTH_LOGLEVEL = 5;

    // width of the field holding the logger name (generally, the procedure name)
    private static final int WIDTH_LOGGERNAME = 80;

    // width of the field holding the message (infinite, it's not used anyway)
    private static final int WIDTH_LOGMESSAGE = 100000;

    // width of the field holding the thread name
    private static final int WIDTH_THREAD = 50;

    // width of the field holding the timestamp
    private static final int WIDTH_TIMESTAMP = 23;

    // values which are used often
    private final int contNumberField_RightSeparatorPos;
    private final int contNumberField_Width;

    public static final String LINE_SEP;

    static {
        String ls = System.getProperty("line.separator");
        LINE_SEP = (ls == null) ? "\n" : ls;
    }

    /**
     * ColumnDesc is a structure holding information about an output column
     */

    private static class ColumnDesc {

        private final int width; // width of the column
        private boolean show; // actually show that column

        // char position of separator between prev column and this column, -1 means none
        private int leftSeparatorPos;

        // char position of separator between this column and next column, -1 means none
        private int rightSeparatorPos;

        public ColumnDesc(int width) {
            this.width = width; // set final var
            this.show = true; // show by default
            this.leftSeparatorPos = -1; // this will have to be recomputed
            this.rightSeparatorPos = -1; // this will have to be recomputed
        }

        public int getWidth() {
            return width;
        }

        public boolean isShow() {
            return show;
        }

        public int getLeftSeparatorPos() {
            return leftSeparatorPos;
        }

        public int getRightSeparatorPos() {
            return rightSeparatorPos;
        }

        public void setShow(boolean in) {
            show = in;
        }

        public void setLeftSeparatorPos(int in) {
            leftSeparatorPos = in;
        }

        public void setRightSeparatorPos(int in) {
            rightSeparatorPos = in;
        }
    }

    /**
     * An unmodifiable map that maps a logging level to a string representation, suitably whitespace-extended and
     * separator-terminated and thus ready for use during logging with minimal wass. Will save CPU cycles.
     */

    private static final Map<Level, String> levelMap;

    static {
        Map<Level, String> myLevelMap = new HashMap();
        Set<Level> levels = new HashSet();
        levels.add(Level.TRACE);
        levels.add(Level.DEBUG);
        levels.add(Level.INFO);
        levels.add(Level.WARN);
        levels.add(Level.ERROR);
        StringBuilder buf = new StringBuilder();
        for (Level l : levels) {
            appendAndFillOnTheRight(buf, l.toString(), WIDTH_LOGLEVEL); // adds spaces and a separator and justifies
            myLevelMap.put(l, buf.toString());
            buf.delete(0, buf.length());
        }
        levelMap = Collections.unmodifiableMap(myLevelMap);
    }

    /**
     * Default constructor which sets up for display of all columns 
     */

    public AlphaLayout() {
        this(true, true, true);
    }

    /**
     * Constructor which takes additional booleans to switch off features.
     */

    public AlphaLayout(boolean displayDateChange, boolean displayLogLevelColumn, boolean displayThreadColumn) {
        columns[COLINDEX_CONTNUMBER] = new ColumnDesc(WIDTH_CONTNUMBER);
        columns[COLINDEX_THREADNAME] = new ColumnDesc(WIDTH_THREAD);
        columns[COLINDEX_TIMESTAMP] = new ColumnDesc(WIDTH_TIMESTAMP);
        columns[COLINDEX_LOGLEVEL] = new ColumnDesc(WIDTH_LOGLEVEL);
        columns[COLINDEX_LOGGERNAME] = new ColumnDesc(WIDTH_LOGGERNAME);
        columns[COLINDEX_LOGMESSAGE] = new ColumnDesc(WIDTH_LOGMESSAGE);
        // copy the passed values
        this.displayDateChange = displayDateChange;
        columns[COLINDEX_LOGLEVEL].setShow(displayLogLevelColumn);
        columns[COLINDEX_THREADNAME].setShow(displayThreadColumn);
        // then compute where to put the columns
        computeColumnSeparatorPositions();
        // then extract two values which are used often
        this.contNumberField_RightSeparatorPos = columns[COLINDEX_CONTNUMBER].getRightSeparatorPos();
        this.contNumberField_Width = columns[COLINDEX_CONTNUMBER].getWidth();
    }

    /**
     * Constructor helper:
     * Recompute the columnStart values; called if a column has been 'switched off' The indexes are the char-columns of
     * the separators, counting from column 0
     */

    private void computeColumnSeparatorPositions() {
        // set the column separators, left side
        {
            int currentIndex = -1;
            for (int i = 0; i < columns.length; i++) {
                ColumnDesc column = columns[i];
                if (column.isShow()) {
                    // the best thing is that this code also works on the first column if we start with '-1'
                    column.setLeftSeparatorPos(currentIndex);
                    currentIndex = currentIndex + column.getWidth() + 1;
                }
            }
        }
        // set the column separators, right side, from the ones computed for the left side
        {
            // assume the last column is always displayed (not unreasonable)!
            int currentIndex = columns[columns.length - 1].getLeftSeparatorPos();
            for (int i = columns.length - 1; i >= 0; i--) {
                ColumnDesc column = columns[i];
                if (column.isShow()) {
                    column.setRightSeparatorPos(currentIndex);
                    currentIndex = column.getLeftSeparatorPos();
                }
            }
        }
    }

    /**
     * Write the 'text' to the 'buf', then add whitespace on the right and then the separator so that the buffer is filled up to
     * 'rightSeparatorPos' included
     */

    private static void appendAndFillOnTheRight(StringBuilder buf, String text, int rightSeparatorPos) {
        buf.append(text);
        int spacesNeeded = rightSeparatorPos - buf.length();
        if (spacesNeeded > 0) {
            int startAt = SPACE_FILLER_LEN - spacesNeeded;
            assert startAt >= 0;
            buf.append(SPACE_FILLER.substring(startAt));
        } else {
            // too much text? don't care, don't cut
        }
        // exactly the right size or too much text; in both cases, add the separator
        buf.append(SEPARATOR_CHAR);
    }

    /**
     * Write the 'text' to the 'buf', but add whitespace on the left to field up the field; also add the separator so that the buffer is filled up to
     * 'rightSeparatorPos' included
     */

    private static void appendAndFillOnTheLeft(StringBuilder buf, String text, int rightSeparatorPos, int fieldWidth) {
        int spacesNeeded = fieldWidth - text.length();
        if (spacesNeeded > 0) {
            int startAt = SPACE_FILLER_LEN - spacesNeeded;
            assert startAt >= 0;
            buf.append(SPACE_FILLER.substring(startAt));
        } else {
            // too much text? don't care, don't cut
        }
        buf.append(text);
        // exactly the right size or too much text; in both cases, add the separator
        buf.append(SEPARATOR_CHAR);
    }

    /**
     * ENTRY POINT FOR LOGBACK: Do the layout of the event, return string.
     */

    @Override
    public String doLayout(ILoggingEvent event) {
        //
        // If we are passed a "null" event, that's bad... but what to do?
        //
        if (event == null) {
            String txt = "(null) logging event passed";
            addStatus(new WarnStatus(txt, CLASS + ".doLayout()"));
            return txt; // will be logged normally but not in the correct layout
        }
        assert event != null;
        //
        // Process the "base", which is the common prefix to all the lines written to describe "event"
        //
        try {
            BaseReturnValue baseRetVal = handleBase(event);
            //
            // Write the possibly multiline message and possibly a throwable representation into a new "buf"
            //
            StringBuilder buf = new StringBuilder();
            {
                int lineCount = 0;
                lineCount = appendMessage(baseRetVal.base, event, buf, lineCount);
                lineCount = ThrowableAlphaLayoutForLogback.appendThrowableChain(baseRetVal.base, event, buf, lineCount);
            }
            //
            // Append the possible "dateChangeAlert" (on the next line and w/o the "base")
            //
            if (baseRetVal.dateChangeAlert != null) {
                buf.insert(0, baseRetVal.dateChangeAlert);
            }
            //
            // Return the resulting non-null string
            //
            return buf.toString();
        } catch (Throwable exe) {
            String txt = LogFacilitiesForThrowables.throwableToSimpleMultilineStory("While formatting logging event", exe, true).toString();
            addStatus(new ErrorStatus(txt, CLASS + ".doLayout()"));
            return txt; // will be logged normally but not in the correct layout
        }
    }

    /**
     * Helper class
     */

    private static class BaseReturnValue {

        public final String dateChangeAlert; // may be null
        public final String base; // not null

        BaseReturnValue(String dateChangeAlert, String base) {
            assert base != null;
            this.dateChangeAlert = dateChangeAlert;
            this.base = base;
        }
    }

    /**
     * Helper.
     */

    private void handleContinuousNumber(StringBuilder buf) {
        assert buf != null;
        long val = contNumber.getAndIncrement();
        appendAndFillOnTheLeft(buf, Long.toString(val), contNumberField_RightSeparatorPos, contNumberField_Width);
    }

    /**
     * Helper to build a "base" which is repeated over a multiline log. Returns prefix and base.
     */

    private BaseReturnValue handleBase(ILoggingEvent event) {
        assert event != null;
        StringBuilder buf = new StringBuilder();
        String dateChangeAlert = null;
        for (int i = 0; i < COLINDEX_LOGMESSAGE; i++) {
            switch (i) {
            case COLINDEX_CONTNUMBER:
                //
                // Append the "continuous number"; this also increases it!
                //
                handleContinuousNumber(buf);
                break;
            case COLINDEX_LOGGERNAME:
                //
                // Append the name of logger (already formatted and all)
                //
                handleLoggerName(event, buf);
                break;
            case COLINDEX_LOGLEVEL:
                //
                // Append the string for the log level (already formatted and all) and append it to "buf"
                //
                if (columns[COLINDEX_LOGLEVEL].isShow()) {
                    handleLevel(event, buf);
                }
                break;
            case COLINDEX_THREADNAME:
                //
                // Write thread name to "buf" (if so asked)
                //
                if (columns[COLINDEX_THREADNAME].isShow()) {
                    handleThreadName(event, buf);
                }
                break;
            case COLINDEX_TIMESTAMP:
                //
                // Write timestampt to "buf"; also determine whether a special message should be written that
                // indicates that the date changed. If handleTimestamp() returns null, nothing is written.
                //
                dateChangeAlert = handleTimestamp(event, buf);
                break;
            default:
                Check.cannotHappen("Unhandled column index " + i);
            }
        }
        //
        // We have got the 'base' of the logging string now, which will be repeated for every line of the text describing the event
        //
        return new BaseReturnValue(dateChangeAlert, buf.toString());
    }

    /**
     * Helper
     */

    private static String cutLeft(String str, int fieldWidth) {
        int length = str.length();
        return str.substring(Math.max(0, length - fieldWidth), length);
    }

    /**
     * Helper
     */

    private void handleThreadName(ILoggingEvent event, StringBuilder buf) {
        assert event != null;
        assert buf != null;
        String threadName = event.getThreadName();
        if (threadName == null) {
            // In which case, append nothing to "buf", making the problem easily spottable.
        } else {
            threadName = cutLeft(threadName, columns[COLINDEX_THREADNAME].getWidth());
            appendAndFillOnTheRight(buf, threadName, columns[COLINDEX_THREADNAME].getRightSeparatorPos());
        }
    }

    /**
     * Format a date. Note that SimpleDateFormat is NOT thread safe!!
     * TODO: How fast is this? Do we win something if we cache formatted dates?  
     */

    private String formatDate(Date date) {
        synchronized (ALPHA_DATE_FORMAT_INSTANCE) {
            return ALPHA_DATE_FORMAT_INSTANCE.format(date);
        }
    }

    /**
     * Helper. Returns a "dateChangeAlert", which may be null, but if not null, should be printed out.
     */

    private String handleTimestamp(ILoggingEvent event, StringBuilder buf) {
        assert event != null;
        assert buf != null;
        Date when = new Date(event.getTimeStamp());
        String dateOfThisEvent = formatDate(when);
        String dateCut = cutLeft(dateOfThisEvent, columns[COLINDEX_TIMESTAMP].getWidth());
        appendAndFillOnTheRight(buf, dateCut, columns[COLINDEX_TIMESTAMP].getRightSeparatorPos());
        //
        // If the new YMD date and the old YMD date are different, set the "dateChangeAlert" saying that the date changed.
        // Then store the new date. At the first call, the old YMD date is "", so one can suppress the "dateChangeAlert" on
        // the first call.
        //
        {
            String dateChangeAlert = null;
            String newThisDayYMD = dateOfThisEvent.substring(0, ALPHA_DATE_FORMAT_YMD_LENGTH);
            if (!newThisDayYMD.equals(storedThisDayYMD)) {
                if (displayDateChange && !"".equals(storedThisDayYMD)) {
                    dateChangeAlert = "****** DATE CHANGE " + storedThisDayYMD + " --> " + newThisDayYMD + " ******\n";
                }
                storedThisDayYMD = newThisDayYMD;
            }
            return dateChangeAlert; // may be null
        }
    }

    /**
     * Helper.
     */

    private static void handleLevel(ILoggingEvent event, StringBuilder buf) {
        assert event != null;
        assert buf != null;
        String reps = levelMap.get(event.getLevel());
        if (reps == null) {
            // 'reps' might be null if the level is unknown (or even if event.getLevel() yields null)!
            // In which case, append nothing to "buf", making the problem easily spottable.
        } else {
            buf.append(reps);
        }
    }

    /**
     * Helper.
     */

    private void handleLoggerName(ILoggingEvent event, StringBuilder buf) {
        assert event != null;
        assert buf != null;
        // TODO: BUFFERING ON THIS USING LRU BUFFER
        String loggerName = event.getLoggerName();
        if (loggerName == null) {
            // In which case, append nothing to "buf", making the problem easily spottable.
        } else {
            loggerName = cutLeft(loggerName, columns[COLINDEX_LOGGERNAME].getWidth());
            appendAndFillOnTheRight(buf, loggerName, columns[COLINDEX_LOGGERNAME].getRightSeparatorPos());
        }
    }

    /**
     * Helper
     */

    private static int posOfFirstLinebreak(String x) {
        assert x != null;
        int crPos = x.indexOf('\r');
        int lfPos = x.indexOf('\n');
        if (crPos >= 0 && lfPos >= 0) {
            // both LF and CR exist, take first position
            return Math.min(crPos, lfPos);
        } else {
            // one or both of the positions does not exist, take the highest one ("max") as
            // -1 means 'not exists'. If both do not exist, this return -1, which is correct
            return Math.max(crPos, lfPos);
        }
    }

    /**
     * Append a single line (which is supposed to NOT have a terminating CR, LF or CRLF)
     */

    private static void appendOneLine(String base, String line, StringBuilder buf, int lineCount) {
        assert base != null;
        assert line != null;
        assert buf != null;
        buf.append(base);
        buf.append(PreformattedInt.formatInt(lineCount));
        buf.append(">");
        buf.append(line); // do NOT trim
        buf.append(LINE_SEP);
    }

    /**
     * Cut the message up into discrete lines (using a LineNumberReader)
     * Write those lines out with 'base' prepended; note that here we don't bother looking at width of the output string at all.
     * The string filled into "buf" will be non-empty and will have a final 'line separator'
     */

    private static int appendMessage(String base, ILoggingEvent event, StringBuilder buf, int lineCountIn) {
        assert base != null;
        assert event != null;
        assert buf != null;
        int lineCount = lineCountIn;
        String txt = event.getFormattedMessage();
        if (txt == null) {
            appendOneLine(base, "**** (null) message in logging event", buf, lineCount++);
        } else {
            // It would be cool if "event.getFormattedMessage()" were an array of string! Well, we have to work it out ourselves.
            if (posOfFirstLinebreak(txt) < 0) {
                // Generally the case: a simple one-liner without CR nor LF
                appendOneLine(base, txt, buf, lineCount++);
            } else {
                try {
                    // More rarely the case: multiline; break apart with the Java "LineNumberReader"
                    // Note that the "LineNumberReader" suppresses the last line (final lone line termination) if it's empty.
                    LineNumberReader lnr = new LineNumberReader(new StringReader(txt));
                    String line;
                    while ((line = lnr.readLine()) != null) {
                        appendOneLine(base, line, buf, lineCount++);
                    }
                } catch (IOException exe) {
                    // An IOException on a StringReader never happens; just throw as RuntimeException
                    throw new IllegalStateException(exe);
                }
            }
        }
        return lineCount;
    }

}