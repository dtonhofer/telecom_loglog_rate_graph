package com.mplify.checkers;

import java.util.regex.Pattern;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2010, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released by M-PLIFY S.A. 
 *          under the MIT License: http://opensource.org/licenses/MIT 
 *******************************************************************************
 *******************************************************************************
 * Formatting of messages on behalf of "Check". The "formatString" can use the
 * SLF4J placeholder "{}" or the print-style placeholders from java.util.Formatting 
 * 
 * See http://slf4j.org/faq.html#string_contents 
 * 
 * See http://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html 
 * 
 * 2013.11.18 - Extend "formatForMeLow" so that it accepts the SLF4J 
 *              placeholder '{}'
 ******************************************************************************/

class Formatter {

    /**
     * Create a "last ditch effort" string to format arguments, after everything else failed. 
     * formatForMe() should be called rarely, so this is called rarely, no need to optimize for speed
     */

    private static String buildLastDitchEffortFormatStr(Object... args) {
        assert args != null;
        StringBuilder buf = new StringBuilder("An error occurred and there were " + args.length + " arguments: ");
        for (int i = 0; i < args.length; i++) {
            buf.append(" '%s'");
        }
        return buf.toString();
    }

    /**
     * If "formatStr" contains the SLF4J placeholder "{}", replace that placeholder with "%s"
     * This is fraught with special cases....
     * \\{} ----> \%s : A SLF4J placeholder with an escaped backslash
     * \{} ----> {} : An escaped SLF4J placeholder, yields the literal {}
     * %{} ----> %%%s : An SLF4J placeholder with prepended percent, yields the formatting string %s, with escaped % prepended
     * {} ----> %s : An SLF4J placeholder, yields the formatting string %s
     */

    static Pattern PATTERN = Pattern.compile("\\{\\}"); // access is threadsafe
    
    static String replaceSlf4JPlaceholders(String formatStringIn) {
        assert formatStringIn != null;
        if (formatStringIn.indexOf("{}") >= 0) {
            String[] splits = PATTERN.split(formatStringIn,-1);
            StringBuilder recompose = new StringBuilder();
            for (int i = 0; i < splits.length - 1; i++) {
                String split = splits[i];
                if (split.length() >= 2 && split.endsWith("\\\\")) {
                    recompose.append(split.substring(0, split.length() - 1));
                    recompose.append("%s");
                } else if (split.length() >= 1) {
                    if (split.endsWith("\\")) {
                        recompose.append(split.substring(0, split.length() - 1));
                        recompose.append("{}");
                    } else if (split.endsWith("%")) {
                        recompose.append(split);
                        recompose.append("%%s");
                    } else {
                        recompose.append(split);
                        recompose.append("%s");
                    }
                } else {
                    recompose.append(split);
                    recompose.append("%s");
                }
            }
            recompose.append(splits[splits.length-1]);
            return recompose.toString();
        } else {
            return formatStringIn;
        }
    }

    /**
     * Format using a printf-like formatter. The "formatStrIn" can be null, meaning "use a default".
     */

    private static String formatForMeLow(String formatStr, Object... args) {
        assert args != null;
        //
        // If "formatStr" is null, select a default format string
        //
        String formatStrLocal;
        if (formatStr == null) {
            formatStrLocal = buildLastDitchEffortFormatStr(args);
        } else {
            formatStrLocal = replaceSlf4JPlaceholders(formatStr);
        }
        //
        // Format to "res"
        //
        StringBuilder res = new StringBuilder();
        try (java.util.Formatter formatter = new java.util.Formatter(res)) {
            formatter.format(formatStrLocal, args);
            return res.toString();
        } catch (Exception exe) {
            String msg = "Exception occurred during formatting of log message using format string " + formatStrLocal + ": ";
            msg += exe.getClass().getName();
            if (exe.getMessage() != null) {
                msg += ": ";
                msg += exe.getMessage().trim();
            }
            System.err.println(msg);
            return null;
        }
    }

    /**
     * Generate a string given a "formatStr", which contains formatting information according to
     * java.util.Formatter (http://docs.oracle.com/javase/7/docs/api/java/util/Formatter.html)
     * or formatting information according to SLF4J (http://slf4j.org/faq.html#logging_performance) 
     */

    public static String formatForMe(String formatStr, Object... args) {
        //
        // In a particularly rare case of a call from Groovy, "args" can be null.
        // This is actually a Groovy bug which should be fixed at some point.
        //
        Object[] argsLocal = (args != null ? args : new Object[] {});
        //
        // Now format. Problems may occur - in particular, args[] may be too short for the format spec.
        // A (null) args[i] yields the string "null".
        // For objects and the %s format specifier, Java invokes .toString() on the object (which could throw).
        //
        String output = formatForMeLow(formatStr, argsLocal);
        //
        // If the returned value is "null", assume "formatStrIn" was bad and call again with
        // "null", thus choosing a default formatting string.
        //
        if (output == null) {
            output = formatForMeLow(null, argsLocal);
        }
        assert output != null;
        return output;
    }
}
