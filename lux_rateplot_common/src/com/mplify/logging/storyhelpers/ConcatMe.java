package com.mplify.logging.storyhelpers;

import java.io.IOException;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2011, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released under the MIT License (http://opensource.org/licenses/MIT) 
 *******************************************************************************
 *******************************************************************************
 * A little class holding up to three strings for later concatenation.
 * This should be more efficient than calling arbitrary concatenation 
 * operations at will.
 * 
 * 2011.08.03 - Moved out of the "StoryHandling" class. This is old code, 
 *              from 1999 or so. Added more concat() functions.
 ******************************************************************************/

public class ConcatMe {

    /*
     * For c1,c2,c3 one may assume that if non-null, the string is single line
     * and has no final newline The strings are filled from "front to back",
     * i.e. the cases (A,null,null), (A,B,null), (A,B,C) are possible
     */

    private final String c1, c2, c3;

    public ConcatMe(String x) {
        assert x != null;
        this.c1 = x;
        this.c2 = null;
        this.c3 = null;
    }

    public ConcatMe(String x, String y) {
        assert x != null;
        assert y != null;
        this.c1 = x;
        this.c2 = y;
        this.c3 = null;
    }

    public ConcatMe(String x, String y, String z) {
        assert x != null;
        assert y != null;
        assert z != null;
        this.c1 = x;
        this.c2 = y;
        this.c3 = z;
    }

    public void concat(Appendable a) {
        assert a != null;
        try {
            a.append(c1);
            if (c2 != null) {
                a.append(c2);
                if (c3 != null) {
                    a.append(c3);
                }
            }
        } catch (IOException exe) {
            String msg = "IOException occurred during append: ";
            msg += exe.getClass().getName();
            if (exe.getMessage() != null) {
                msg += ": ";
                msg += exe.getMessage().trim();
            }
            System.err.println(msg);
        }
    }

    public String getC1() {
        return c1;
    }

    public String getC2() {
        return c2;
    }

    public String getC3() {
        return c3;
    }

}