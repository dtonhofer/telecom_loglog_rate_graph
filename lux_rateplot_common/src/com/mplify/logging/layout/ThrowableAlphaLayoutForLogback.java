package com.mplify.logging.layout;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;

import com.mplify.logging.Story;
import com.mplify.logging.ThrowableEntry;
import com.mplify.logging.ThrowableEntryChain;
import com.mplify.logging.storyhelpers.ConcatMe;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2010, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released under the MIT License (http://opensource.org/licenses/MIT) 
 *******************************************************************************
 *******************************************************************************
 * Specialized functions dealing with "throwable" printout in the context
 * of the "Logback" (http://logback.qos.ch/) logging implementation 
 *  
 * 2012.06.07 - Code carved out of AlphaLayout yields this
 ******************************************************************************/

class ThrowableAlphaLayoutForLogback {

    /**
     * Class cannot be instantiated
     */
    
    private ThrowableAlphaLayoutForLogback() {
        // NOP
    }
    
    /**
     * Handle a possibly long chain of throwables
     */

    public static int appendThrowableChain(String base, ILoggingEvent event, StringBuilder buf, int lineCountIn) {
        assert base != null;
        assert event != null;
        assert buf != null;
        int lineCount = lineCountIn;
        IThrowableProxy tproxy = event.getThrowableProxy();
        if (tproxy != null) {
            // Build a structure which contains the chain of throwable causes,
            // along with all the stack traces (with a String per stack position),
            // Use "toStory2()" which prints nice chains
            // >>>>>>>>            
            Story story = makeChain(tproxy).toStory2();
            // <<<<<<<<
            lineCount = appendToBuffer(base, buf, story.cutUp(), lineCount);
        }
        return lineCount;
    }
    
    /**
     * Helper
     */
    
    private static ThrowableEntryChain makeChain(IThrowableProxy tproxy) {
        LinkedList<ThrowableEntry> res = new LinkedList();
        chainCausesOfThrowableIntoList(res, tproxy);
        return new ThrowableEntryChain(res);        
    }
    
    /**
     * Append to buffer
     */
    
    private static int appendToBuffer(String base, StringBuilder buf, List<ConcatMe> concats, int lineCountIn) {
        assert concats != null;
        int lineCount = lineCountIn;
        for (ConcatMe c : concats) {
            buf.append(base);
            buf.append(PreformattedInt.formatInt(lineCount++));
            buf.append("> **** ");
            c.concat(buf); // concats the "ConcatMe" to the "buf"; should be singleline!
            buf.append(AlphaLayout.LINE_SEP);
        }
        return lineCount;
    }
    
    /**
     * Helper method. Similar to the method in "ThrowableEntryChain", but takes a proxy.
     */

    private static void chainCausesOfThrowableIntoList(LinkedList<ThrowableEntry> res, IThrowableProxy tproxy) {
        assert tproxy != null;
        assert res != null;
        // this exception
        {
            String className = tproxy.getClassName(); // not null
            String message = tproxy.getMessage(); // may be null
            res.add(new ThrowableEntry(className, message, makeStackTraceOfStrings(tproxy)));
        }
        // its cause; a tail-recursive call
        if (tproxy.getCause() != null) {
            chainCausesOfThrowableIntoList(res, tproxy.getCause());
        }
    }


    /**
     * Helper. Similar to the method in "ThrowableEntryChain", but takes a proxy. 
     * Return ArrayList<String>, which is an ArrayList for fast indexed access.
     */

    private static ArrayList<String> makeStackTraceOfStrings(IThrowableProxy tproxy) {
        assert tproxy != null;
        ArrayList<String> res;
        StackTraceElementProxy[] arrayIn = tproxy.getStackTraceElementProxyArray();
        if (arrayIn == null) {
            arrayIn = new StackTraceElementProxy[0]; // not supposed to happen, but I'm paranoid
        }
        res = new ArrayList(arrayIn.length);
        for (int i = 0; i < arrayIn.length; i++) {
            res.add(arrayIn[i].toString()); // just stringify
        }
        return res;
    }

}
