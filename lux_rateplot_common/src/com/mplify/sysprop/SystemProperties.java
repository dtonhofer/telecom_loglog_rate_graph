package com.mplify.sysprop;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2009, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released under the MIT License (http://opensource.org/licenses/MIT) 
 *******************************************************************************
 *******************************************************************************
 * Helper methods to access system properties
 * 
 * 2009.09.11 - Carved out
 ******************************************************************************/

public class SystemProperties {

    /**
     * Possible "stages"
     */

    public final static String STAGE_TESTCASE = "testcase";
    public final static String STAGE_TEST = "test";
    public final static String STAGE_PROD = "prod";

    /**
     * Property names
     * "stage"      : used to indicate whether the executable is in "test", "prod" or is used as a "testcase"
     * "instance"   : name of the daemon, e.g. "msgserver"; used by "AlphaAppender" for mail destination & subject, also used to select "contact points"
     * "confighome" : location of configfiles; Log4JStarter looks in that directory for config files; need not be given
     */

    public final static String PROP_STAGE = "stage";
    public final static String PROP_INSTANCE = "instance";
    public final static String PROP_CONFIGHOME = "confighome";
    
    /**
     * Cannot be instantiated
     */

    private SystemProperties() {
        // NOP
    }
    
    /**
     * Mark this JVM instance as being in stage "testcase" by explicitly setting a system property
     */
    
    public static void setAsInTestcaseStage() {
        System.setProperty(PROP_STAGE, STAGE_TESTCASE);
    }

    /**
     * Getting the "stage". Namification means the value is trimmed and lowercased
     */

    public static String getStage(boolean throwIfNotFound,boolean namify) {
        return extract(PROP_STAGE, throwIfNotFound, namify);
    }

    /**
     * Getting the "instance". Normalization means the value is trimmed and lowercased
     */

    public static String getInstance(boolean throwIfNotFound,boolean namify) {
        return extract(PROP_INSTANCE, throwIfNotFound, namify);
    }

    /**
     * Getting the "confighome". Returns null if not exists, otherwise trimmed value
     */

    public static String getConfighome(boolean throwIfNotFound) {
        String res = extract(PROP_CONFIGHOME, throwIfNotFound, false);
        if (res!=null) {
            res = res.trim();
        }
        return res;
    }

    /**
     * Common extractor
     */

    private static String extract(String propname, boolean throwIfNotFound,boolean namify) {
        String value = System.getProperty(propname);
        if (value == null && throwIfNotFound) {
            throw new IllegalArgumentException("The '" + propname + "' system property is not set");
        } else if (value == null) {
            return null;
        } else {
            if (namify) {
                return value.trim().toLowerCase();
            }
            else {
                return value;
            }
        }
    }
}
