package com.mplify.logging;

import org.junit.Test;

import com.mplify.logging.LoglevelAid.Loglevel;

import static org.junit.Assert.*;

//@SuppressWarnings("static-method")
public class TestCaseLogFacilities {
  
    enum SomeValue { ALPHA, BETA }
    
    @SuppressWarnings("static-method")
    @Test
    public void testGoodFormatting() {
        assertEquals("This is a s '10' string", LogFacilities.formatForMe("This is a s '%s' string", Integer.valueOf(10)));
        assertEquals("This is a s 'xy' string", LogFacilities.formatForMe("This is a s '%s' string", "xy"));
        assertEquals("This is a s 'xy' string", LogFacilities.formatForMe("This is a s '%s' string", "xy"));
        assertEquals("This is a s 'ALPHA' string", LogFacilities.formatForMe("This is a s '%s' string", SomeValue.ALPHA));
        assertEquals("This is a d '12' string", LogFacilities.formatForMe("This is a d '%d' string", Integer.valueOf(12)));
        
    }

    @SuppressWarnings("static-method")
    @Test
    public void testBadFormatting() {
        // this breaks off because of a bad conversion
        assertEquals("This is a d '", LogFacilities.formatForMe("This is a d '%d' string", "zc")); 
    }

    @SuppressWarnings("static-method")
    @Test
    public void testLogLevelOrderingSymmetry() {
        assertTrue(Loglevel.DEBUG.compareTo(Loglevel.DEBUG) == 0);
        assertTrue(Loglevel.INFO.compareTo(Loglevel.DEBUG) > 0);
        assertTrue(Loglevel.DEBUG.compareTo(Loglevel.INFO) < 0);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testLogLevelOrdering() {
        assertTrue(Loglevel.TRACE.compareTo(Loglevel.DEBUG) < 0);
        assertTrue(Loglevel.DEBUG.compareTo(Loglevel.INFO) < 0);
        assertTrue(Loglevel.INFO.compareTo(Loglevel.WARN) < 0);
        assertTrue(Loglevel.WARN.compareTo(Loglevel.ERROR) < 0);
        assertTrue(Loglevel.ERROR.compareTo(Loglevel.TRACE) > 0);
    }
}
