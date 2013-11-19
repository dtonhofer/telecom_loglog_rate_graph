package com.mplify.logging.storyhelpers;

import java.util.Date;

import com.mplify.logging.DateTexter;
import com.mplify.logging.LogFacilities;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2010, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released under the MIT License (http://opensource.org/licenses/MIT) 
 *******************************************************************************
 *******************************************************************************
 * A doublet with a String supposed to be on the left of a separator and a 
 * String supposed to be on the right of a separator. The String to the left
 * is trimmed before being assigned internally! 
 *
 * 2007.04.11 - Created as we need this stuff for some extensions   
 * 2011.07.22 - Constructor reviewed, added shielding. 
 *              Deprecated use of "adjustmentLength"   
 * 2011.09.05 - Ameliorated comments         
 ******************************************************************************/

public class Doublet {

    private final String left; // not null, trimmed, may be multiline
    private final String right; // not null, may be multiline

    /*
     * Flag bits
     */

    public static final int PRINT_DASHES = 2;
    public static final int MANGLE_STRING = 4;

    /**
     * Create from a "left" and "right" string. The "left" must not be null,
     * will be trimmed and can be multiline. The "right" may be anything. If
     * null, it will yield the String "(null)"
     */

    public Doublet(String left, Object right) {
        this(left, right, 0, null);
    }

    /**
     * Special case to avoid autoboxing
     */

    public Doublet(String left, boolean right) {
        this(left, Boolean.valueOf(right), 0, null);
    }

    /**
     * Special case to avoid autoboxing
     */

    public Doublet(String left, int right) {
        this(left, Integer.valueOf(right), 0, null);
    }

    /**
     * Create from a "left" and "right" string. The "left" must not be null,
     * will be trimmed and can be multiline. The "right" may be anything. If
     * null, it will yield the String "(null)". The "flags" is a value OR-ed
     * from "PRINT_NULL", "PRINT_DASHES", "MANGLE_STRING" or 0.
     */

    public Doublet(String left, Object right, int flags) {
        this(left, right, flags, null);
    }

    /**
     * Create from a "left" and "right" string. The "left" must not be null,
     * will be trimmed and can be multiline. The "right" may be anything. If
     * null, it will yield the String "(null)". The "flags" is a value OR-ed
     * from "PRINT_NULL", "PRINT_DASHES", "MANGLE_STRING" or 0 and it determines
     * modifications on the "right" string. A suffix can be passed to append for
     * example units (e.g. "seconds"), which is merged into the "right" string
     * with no intervening whitespace.
     */

    public Doublet(String left, Object right, int flags, String suffix) {
        if (left == null) {
            throw new IllegalArgumentException("The passed 'left' String is (null)");
        }
        //
        // Handle left side; trim on both sides; remains multiline
        //
        this.left = left.trim();
        //
        // Handle right side
        //
        String myRight;
        if (right == null) {
            myRight = null;
        } else {
            if (right instanceof Date) {
                //
                // Special handling for Date? ok, but a bit bizarre... what else
                // needs "special handling"?
                //
                myRight = DateTexter.ALTERNATE0.inUTC((Date) right) + " UTC";
            } else {
                //
                // Note that "right" may actually be a "Story" and yield a
                // multiline result
                //
                myRight = safelyStringify(right);
                if (myRight != null) {
                    if (suffix != null) {
                        myRight = myRight + suffix;
                    }
                    if ((flags & MANGLE_STRING) != 0) {
                        myRight = "'" + LogFacilities.mangleString(myRight) + "'";
                    }
                }
            }
        }
        //
        // If the result is "null", special handling
        //
        if (myRight == null) {
            if ((flags & PRINT_DASHES) != 0) {
                myRight = "--";
            } else {
                myRight = "(null)";
            }
        }
        //
        // Done
        //
        assert myRight != null;
        this.right = myRight;
    }

    /**
     * Get the "left" string. Won't return null
     */

    public String getLeft() {
        assert left != null;
        return left;
    }

    /**
     * Get the "right" string. Won't return null
     */

    public String getRight() {
        assert right != null;
        return right;
    }

    /**
     * Quickly check whether "right" has multiline text
     */

    public boolean isMultiline() {
        return right.indexOf('\n') >= 0;
    }

    /**
     * Make a naive string.
     */

    @Override
    public String toString() {
        return left + ": " + right;
    }

    /**
     * Helper. May return null. Won't accept null.
     */

    static String safelyStringify(Object x) {
        assert x != null;
        String myStr;
        try {
            myStr = x.toString();
        } catch (Exception exe) {
            String msg = "Exception occurred during stringification of a ";
            msg += x.getClass().getName();
            msg += ": ";
            msg += exe.getClass().getName();
            if (exe.getMessage() != null) {
                msg += ": ";
                msg += exe.getMessage().trim();
            }
            myStr = msg;
            System.err.println(msg);
        }
        // myStr may STILL BE NULL!
        return myStr;
    }

}
