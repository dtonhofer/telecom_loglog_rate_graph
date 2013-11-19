package com.mplify.checkers;

import org.junit.Test;

import static org.junit.Assert.*;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2013, Q-LEAP S.A.
 *                     14 rue Aldringen
 *                     L-1118 Luxembourg
 *
 * Released under the MIT License: http://opensource.org/licenses/MIT
 *******************************************************************************
 *******************************************************************************
 * A test case!
 * 
 * 2013.11.18 - Created to test the correct replacement of placeholders
 ******************************************************************************/

@SuppressWarnings("static-method")
public class TestCaseFormatter {

    @Test
    public void testPlaceholderReplacement() {
        assertEquals("",Formatter.replaceSlf4JPlaceholders(""));
        assertEquals("%s",Formatter.replaceSlf4JPlaceholders("{}"));
        assertEquals("%s%s",Formatter.replaceSlf4JPlaceholders("{}{}"));
        assertEquals("xyz",Formatter.replaceSlf4JPlaceholders("xyz"));        
        assertEquals("x%syz",Formatter.replaceSlf4JPlaceholders("x{}yz"));
        assertEquals("xy%sz",Formatter.replaceSlf4JPlaceholders("xy{}z"));
        assertEquals("x%sy%sz",Formatter.replaceSlf4JPlaceholders("x{}y{}z"));
        assertEquals("%sxyz",Formatter.replaceSlf4JPlaceholders("{}xyz"));
        assertEquals("xyz%s",Formatter.replaceSlf4JPlaceholders("xyz{}"));
        assertEquals("%sxyz%s",Formatter.replaceSlf4JPlaceholders("{}xyz{}"));
        assertEquals("%sx%sy%sz%s",Formatter.replaceSlf4JPlaceholders("{}x{}y{}z{}"));
        assertEquals("xyz%s%s",Formatter.replaceSlf4JPlaceholders("xyz{}{}"));
        assertEquals("%s%sxyz",Formatter.replaceSlf4JPlaceholders("{}{}xyz"));
    }
    
    @Test
    public void testPlaceholderReplacementWithExtras() {
        assertEquals("xy{}z",Formatter.replaceSlf4JPlaceholders("xy\\{}z"));
        assertEquals("xyz{}",Formatter.replaceSlf4JPlaceholders("xyz\\{}"));
        assertEquals("xy\\%sz",Formatter.replaceSlf4JPlaceholders("xy\\\\{}z"));
        assertEquals("xyz\\%s",Formatter.replaceSlf4JPlaceholders("xyz\\\\{}"));        
    }

    @Test
    public void testPlaceholderReplacementWithExtras2() {
        assertEquals("xy%%%sz",Formatter.replaceSlf4JPlaceholders("xy%{}z"));
    }
}
