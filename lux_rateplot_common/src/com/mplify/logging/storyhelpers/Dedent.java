package com.mplify.logging.storyhelpers;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2010, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released under the MIT License (http://opensource.org/licenses/MIT) 
 *******************************************************************************
 *******************************************************************************
 * This class is used to signal dedentation in logging vector-of-strings
 *
 * 1999.06.00 - Implemented for Synapse S.A.
 * 2000.03.09 - Now an independent class
 * 2001.09.04 - Renamed for m-plify
 * 2001.10.20 - Added toString()
 * 2005.11.28 - Moved to project m3p_tomcat_common, package com.mplify.logging
 * 2007.04.11 - No longer contains the 'dedentation count'; basically, this 
 *              just dedents the previous indent. Thus we can use a canonical
 *              instance "CI" instead of innumerable new() calls.
 ******************************************************************************/

public class Dedent {

    // canonical instance to be used by client code instead of "new Dedent()"

    public static Dedent CI = new Dedent();

    @Override
    public String toString() {
        return "Dedent<<<";
    }
}