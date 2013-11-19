package com.mplify.logging.storyhelpers;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2008, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released under the MIT License (http://opensource.org/licenses/MIT) 
 *******************************************************************************
 *******************************************************************************
 * This class is used to signal indentation in logging vector-of-strings
 *
 * 1999.06.00 - Implemented for Synapse S.A.
 * 2000.03.09 - Now an independent class
 * 2001.09.04 - Renamed functions for m-plify
 * 2001.10.20 - Added toString()
 * 2005.11.28 - Moved to project m3p_tomcat_common, package com.mplify.logging
 ******************************************************************************/

public class Indent {

    // canonical instance with 2 indents to be used by client code instead of
    // "new Indent(2)"

    public static Indent CI = new Indent(2);

    private final int n;

    public Indent(int delta) {
        n = Math.max(0, delta);
    }

    public int getCount() {
        return n;
    }

    @Override
    public String toString() {
        return ("Indent>>>" + Integer.toString(n));
    }
}