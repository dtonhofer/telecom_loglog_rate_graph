package eu.qleap.smc_uhd.rateplot.desc;

import static com.example.BasicChecks.*;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2013, Q-LEAP S.A.
 *                     14 rue Aldringen
 *                     L-1118 Luxembourg
 *
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 *******************************************************************************
 *******************************************************************************
 * How is the signal carried?
 *
 * COAX:   Coaxial cable originally meant for analog TV.
 *         Cable is branching off some "head station" that is being fed by fiber,
 *         so this is actually FTTN + COAX Tree
 *      
 * RADIO:  Digital radio network (GSM, UMTS, LTE)
 * 
 * FTTN:   Landline fiber network, continues over VDSL to the house.
 *         Considered the same as FTTC.
 * 
 * FTTH:   Landline fiber network, with the fiber entering the home.
 *         We do not distinguish whether copper lines are being used inside
 *         the house or not (i.e. FTTB is considered the same)
 *        
 * COPPER: Use of existing phone line down to the switching centre.
 * 
 * LANDLINE: Unspecified landline
 * 
 * 2013.08.XX - Written
 * 2013.09.04 - Adapted to Groovy
 ******************************************************************************/

enum Bearer {

    COAX(0), RADIO(1), FTTN(2), FTTH(3), COPPER(4), LANDLINE(5)

    final int value // numeric identifier
    
    private final static Map lookup = [:] // to map integer to Bearer instance
    
    Bearer(int value) {
        this.value = value
    }

    static Bearer obtain(int x) {
        Bearer res = Bearer.values().find {
            Bearer b -> b.value == x
        }        
        if (res == null) {
            instaFail("Illegal value {} passed", x)
            throw new Error(NEVER_GETTING_HERE_BUT_KEEPING_COMPILER_HAPPY)
        }
        return res
    }
}