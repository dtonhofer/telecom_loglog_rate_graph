package com.mplify.logging;

import com.mplify.checkers.Check;
import com.mplify.logging.storyhelpers.Dedent;
import com.mplify.logging.storyhelpers.Doublet;
import com.mplify.logging.storyhelpers.Indent;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2010, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released under the MIT License (http://opensource.org/licenses/MIT) 
 *******************************************************************************
 *******************************************************************************
 * Simple throwable handling
 * 
 * 2011.08.04 - Created from odds and sods
 * 2011.09.05 - throwableToSimpleMultilineStory() reviewed
 * 2011.10.17 - throwableToOneLinerDoublet() reviewed
 * 2011.10.18 - Change in output
 * 2013.01.07 - throwableToOneLinerDoublet() does not print stacktrace line
 ******************************************************************************/

public class LogFacilitiesForThrowables {

    /**
     * Make a one-liner "Doublet" out of a Throwable for quick and short logging using Doublet.toString()
     * Null cannot be passed here.
     */

    public static Doublet throwableToOneLinerDoublet(Throwable t) {
        Check.notNull(t,"throwable");
        String msg = (t.getMessage() != null) ? t.getMessage() : "-- no message --";
        /*
         * A single line of the stack trace is generally not useful
        StackTraceElement[] ste = t.getStackTrace();
        String loc = (ste!=null && ste.length>0) ? ste[0].toString() : "-- no location --";        
        return new Doublet(t.getClass().getName(), msg + " at " + loc); // generally very uninformative...
        */
        return new Doublet(t.getClass().getName(), msg); // "msg" may be multiline...
    }

    /**
     * Helper to write simplified dump of throwable, preceded by the "header" string.
     * "Header" may be null, in which case only the throwable is stringified. The
     * throwable results in one text line per throwable found in the "cause" sequence. 
     */

    public static Story throwableToSimpleMultilineStory(String header, Throwable t) {
        return throwableToSimpleMultilineStory(header, t, false);
    }
    
    public static Story throwableToSimpleMultilineStory(String header, Throwable t, boolean addStackTrace) {
        Check.notNull(t,"throwable");
        Story story = new Story();
        //
        // header, but only if not empty
        //
        if (header != null && !header.isEmpty()) {
            story.add(header);
        }
        //
        // Write first throwable, possible with its stack trace
        // Insert a space at the beginning, because the Eclipse console makes a link out of the Exception,
        // which is messified if the fully qualified name contacts the string before it
        //        
        story.add(Indent.CI);        
        story.add(throwableToOneLinerDoublet(t).toString());
        if (addStackTrace) {
            story.add(LogFacilitiesForThrowables.textifyStacktrace("", t.getStackTrace(), 0).toString());
        }        
        //
        // write subsequent throwables, successively indented (this does not really work if the one-liner-doublet
        // takes several lines)
        //
        int indent = 3;        
        Throwable cur = t.getCause();
        while (cur!=null) {
            String indentStr = LogFacilities.getSpaceString(indent);
            story.add(indentStr + "└" + throwableToOneLinerDoublet(cur).toString());            
            cur = cur.getCause();
            indent = indent + 3;
        }        
        story.add(Dedent.CI);
        return story;
    }
    
    
    /**
     * Given a stack trace, generally generated with
     * 
     * StackTraceElement[] stes = Thread.currentThread().getStackTrace();
     * 
     * write it out from back (topmost stackframe) to deepest stackframe, as is the Java custom skipping the highest
     * "numberOfFramesToSkip" because the caller deems these to be without interest.
     * 
     * prefix is added in each line
     */

    public static StringBuilder textifyStacktrace(String prefix, StackTraceElement[] stes, int numberOfFramesToSkip) {
        StringBuilder buf = new StringBuilder();
        for (int i = numberOfFramesToSkip; i < stes.length; i++) {
            // for (int i = stes.length-1; i >= numberOfFramesToSkip; i--) {
            buf.append(prefix);
            buf.append(stes[i].toString());
            buf.append("\n");
        }
        return buf;
    }

}
