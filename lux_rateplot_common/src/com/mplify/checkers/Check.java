package com.mplify.checkers;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

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
 * Function for checking arguments of a method (or more generally doing
 * assert-like checks) and throwing accordingly.
 * 
 * This is practically the same as Guava's "Preconditions", see
 * 
 *    http://code.google.com/p/guava-libraries/wiki/PreconditionsExplained
 * 
 * For more complete approaches, see: 
 * 
 *    http://en.wikipedia.org/wiki/Design_by_contract
 *
 *    ...for Java and Groovy DbC facilities.
 * 
 * These functions throw "CheckFailedException" derived from RuntimeException
 * instead of "IllegalArgumentException" and "IllegalStateException".
 *
 * Why: 
 * 
 * "IllegalArgumentException" is meant to be thrown
 * during argument check ("Thrown to indicate that a method has been passed 
 * an illegal or inappropriate argument."), but this is not consistently used.
 * 
 * "IllegalStateException" is meant to be throw if the state is incorrect
 * ("Signals that a method has been invoked at an illegal or inappropriate 
 *  time. In other words, the Java environment or Java application is not
 *  in an appropriate state for the requested operation."), but this is not 
 * consistently used. 
 * 
 * As one is never sure whether "IllegalArgumentException" or
 * "IllegalStateException" should be thrown, just throw "CheckFailedException".
 * 
 * 2010.11.25 - Created because bored to add multiliners to methods entries
 * 2011.02.16 - Added _isInstanceOf() taking multiple parameters
 * 2011.05.30 - Added _cannotHappen()
 * 2011.05.31 - Now throw CheckFailedException instead of IllegalArgumentException.
 *              Some rewrites and renames.
 * 2011.06.16 - Added fail()   
 * 2011.07.12 - formatForMe() has been moved to LogFacilities           
 * 2011.10.19 - Moved the package com.mplify.checkers to "01_ignition"
 *              because it's really used "very early".
 * 2011.11.29 - Added validate() to unify validation calls  
 * 2012.01.18 - In validateIt(): made sure that unreachable validate method
 *              does not throw up the stack     
 * 2012.07.17 - Added inRange() for longs
 * 2012.12.28 - Added NEVER_GET_HERE_BUT_NEEDED_FOR_KEEPING_COMPILER_HAPPY
 *              which is used to tell the developer what's up
 * 2013.02.21 - Added "imply()" 
 * 2013.06.21 - Renamed "_check" to "Check" for consistency
 * 
 * TODO: 
 * 
 * Additional methods taking one or two values before the varargs method
 * is used, for efficiency purpose.
 ******************************************************************************/

public class Check {

    private final static boolean formatterAlwaysOn = false;
    private final static String innocuousText = "Testing of formatting: ";
    
    /**
     * This is used when Check.cannotHappen() or Check.fail() is called. This will *always* 
     * result in a runtime exception, but the compile-time verifier demands a proper return
     * after the call.
     * 
     * Insert "throw new Error(Check.NEVER_GET_HERE_BUT_NEEDED_FOR_KEEPING_COMPILER_HAPPY);"
     * 
     * in that case to tell the compiler who's boss. 
     */
    
    public static final String NEVER_GET_HERE_BUT_NEEDED_FOR_KEEPING_COMPILER_HAPPY = "Never get here but needed for keeping compiler happy";
       
    /**
     * Check that object "x" is not null, throw CheckException if it is.
     */

    public static void notNull(Object x, String name) {
        if (x == null) {
            throw new CheckFailedException("The object '" + name + "' is (null)");
        }
    }

    /**
     * Check that object "x" is not null, throw CheckException if it is.
     */

    public static void notNull(Object x) {
        if (x == null) {
            throw new CheckFailedException("The passed object is (null)");
        }
    }
    
    /**
     * Check that Collection "x" is not null and contains elements, throw CheckFailedException otherwise.
     */

    public static void notNullAndNotEmptyCollection(Collection<?> x, String name) {
        if (x == null) {
            throw new CheckFailedException("The Collection '" + name + "' is (null)");
        }
        if (x.isEmpty()) {
            throw new CheckFailedException("The Collection '" + name + "' is empty");
        }
    }

    /**
     * Check that Map "x" is not null and contains elements, throw CheckFailedException otherwise.
     */

    public static void notNullAndNotEmptyMap(Map<?,?> x, String name) {
        if (x == null) {
            throw new CheckFailedException("The Map '" + name + "' is (null)");
        }
        if (x.isEmpty()) {
            throw new CheckFailedException("The Map '" + name + "' is empty");
        }
    }

    /**
     * Check that String "x" is not null and contains stuff other than whitespace, throw CheckFailedException otherwise.
     */

    public static void notNullAndNotOnlyWhitespace(String x, String name) {
        if (x == null) {
            throw new CheckFailedException("The String '" + name + "' is (null)");
        }
        if (x.isEmpty()) {
            throw new CheckFailedException("The String '" + name + "' is empty");
        }
        int len = x.length();
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(x.charAt(i))) {
                return; // OUTTA HERE; not whitespace only
            }
        }
        throw new CheckFailedException("The String '" + name + "' is fully whitespace");
    }

    /**
     * This is a specialized check for database retrieval, it throws "UnexpectedDataException", a
     * subclass of RuntimeException
     */

    public static void storeFieldNotNull(Object x, String name) {
        if (x == null) {
            throw new UnexpectedDataException("Field '" + name + "' has (null) content, which should not happen");
        }
    }

    /**
     * Check whether a condition yields "true". If not, the "txt" is interpreted as a printf/SLF4J format
     * string and combined with the varargs to form the error message in the thrown CheckFailedException.
     */

    public static void isTrue(boolean x, String txt, Object... args) {
        if (!x) {
            throw new CheckFailedException(Formatter.formatForMe(txt, args));
        }
        if (formatterAlwaysOn) {
            System.err.println(Formatter.formatForMe(innocuousText + txt, args));
        }
    }

    /**
     * Check whether a condition yields "false". If yes, the "txt" is interpreted as a printf/SLF4J format
     * string and combined with the varargs to form the error message in the thrown CheckFailedException.
     */

    public static void isFalse(boolean x, String txt, Object... args) {
        if (x) {
            throw new CheckFailedException(Formatter.formatForMe(txt, args));
        }
        if (formatterAlwaysOn) {
            System.err.println(Formatter.formatForMe(innocuousText + txt, args));
        }        
    }

    /**
     * Just throw. The "txt" is interpreted as a printf/SLF4J format
     * string and combined with the varargs to form the error message in the thrown CheckFailedException.
     */

    public static void fail(String txt, Object... args) {
        throw new CheckFailedException(Formatter.formatForMe(txt, args));
    }

    /**
     * This is used in places that are not supposed to be traversed, e.g. "defaults"
     * of switch statements. Actually throws "Error" instead of "Exception" as 
     * calling this indicates a program error that needs code fixing presto. This will
     * most probably kill the thread or the whole process. 
     */

    public static void cannotHappen() {
        throw new Error("Can't happen!");
    }

    /**
     * This is used in places that are not supposed to be traversed, e.g. "defaults"
     * of switch statements. Actually throws "Error" instead of "Exception" as 
     * calling this indicates a program error that needs code fixing presto. This will
     * most probably kill the thread or the whole process. 
     */

    public static void cannotHappen(String txt) {
        throw new Error("Can't happen: " + txt);
    }

    /**
     * This is used in places that are not supposed to be traversed, e.g. "defaults"
     * of switch statements. Actually throws "Error" instead of "Exception" as 
     * calling this indicates a program error that needs code fixing presto. This will
     * most probably kill the thread or the whole process. 
     */

    public static void cannotHappen(String txt, Throwable cause) {
        throw new Error("Can't happen: " + txt, cause);
    }

    /**
     * This is used in places that are not supposed to be traversed, e.g. "defaults"
     * of switch statements. Actually throws "Error" instead of "Exception" as 
     * calling this indicates a program error that needs code fixing presto. This will
     * most probably kill the thread or the whole process. 
     */

    public static void cannotHappen(Throwable cause) {
        throw new Error("Can't happen", cause);
    }

    /**
     * This is used in places that are not supposed to be traversed, e.g. "defaults"
     * of switch statements. Actually throws "Error" instead of "Exception" as 
     * calling this indicates a program error that needs code fixing presto. This will
     * most probably kill the thread or the whole process. 
     */

    public static void cannotHappen(String txt, Object... args) {
        throw new Error("Can't happen: " + Formatter.formatForMe(txt, args));
    }

    /**
     * Check that object "obj" is an instance of class "clazz". 
     * Passing "null" as either "obj" or "clazz" will result in an 
     * IllegalArgumentException.
     */

    public static void notNullAndInstanceOf(Object x, String name, Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("The comparison class to compare against object '" + name + "' is (null)");
        }
        if (x == null) {
            throw new CheckFailedException("The object '" + name + "' is (null)");
        }
        if (!clazz.isAssignableFrom(x.getClass())) {
            throw new CheckFailedException("The object '" + name + "' is not of class " + clazz.getName() + " but of class " + x.getClass().getName());
        }
    }

    /**
     * Check that object "obj" is an instance of class "clazz". 
     * Passing "null" as either "obj" or "clazz" will result in an 
     * IllegalArgumentException. The "txt" is interpreted as a printf/SLF4J 
     * format string and combined with the varargs to form the error message in
     * the thrown CheckFailedException.
     */

    public static void notNullAndInstanceOf(Object x, String name, Class<?> clazz, String txt, Object... args) {
        if (clazz == null) {
            throw new IllegalArgumentException("The comparison class to compare against object '" + name + "' is (null)");
        }
        if (x == null) {
            throw new CheckFailedException("The object '" + name + "' is (null)");
        }
        if (!clazz.isAssignableFrom(x.getClass())) {
            throw new CheckFailedException("The object '" + name + "' is not of class " + clazz.getName() + " but of class " + x.getClass().getName() + ": " + Formatter.formatForMe(txt, args));
        }
        if (formatterAlwaysOn) {
            System.err.println(Formatter.formatForMe(innocuousText + txt, args));
        }        
    }

    /**
     * Is x > 0? Throw CheckFailedException if not
     */

    public static void largerThanZero(int x, String name) {
        if (x <= 0) {
            throw new CheckFailedException("The object '" + name + "' is less than or equal to 0: " + x);
        }
    }

    /**
     * Is x null or else x > 0? Throw CheckFailedException if not
     */

    public static void nullOrElseLargerThanZero(Integer x, String name) {
        if (x != null) {
            if (x.intValue() <= 0) {
                throw new CheckFailedException("The object '" + name + "' is less than or equal to 0: " + x);
            }
        }
    }

    /**
     * Is x >= 0? Throw CheckFailedException if not
     */

    public static void largerOrEqualToZero(int x, String name) {
        if (x < 0) {
            throw new CheckFailedException("The object '" + name + "' is smaller than 0: " + x);
        }
    }

    /**
     * Special for fields read from the database. Throws UnexpectedDataException
     */

    public static void storeFieldLargerOrEqualToZero(int x, String name) {
        if (x < 0) {
            throw new UnexpectedDataException("Integer field '" + name + "'  is smaller than 0: " + x);
        }
    }

    /**
     * Special for fields read from the database. Throws UnexpectedDataException
     */

    public static void storeFieldLargerThanZero(int x, String name) {
        if (x <= 0) {
            throw new UnexpectedDataException("Integer field '" + name + "' is less than or equal to 0: " + x);
        }
    }

    /**
     * Check that 'value' is in the given range [lowestAllowed,highestAllowed]. Throw CheckFailedException
     */

    public static void inRange(int value, String name, int lowestAllowed, int highestAllowed) {
        if (value < lowestAllowed || highestAllowed < value) {
            throw new CheckFailedException("The integer value '" + name + "' is out of range [" + lowestAllowed + "," + highestAllowed + "]: " + value);
        }
    }

    /**
     * Check that 'value' is in the given range [lowestAllowed,highestAllowed]. Throw CheckFailedException
     */

    public static void inRange(long value, String name, long lowestAllowed, long highestAllowed) {
        if (value < lowestAllowed || highestAllowed < value) {
            throw new CheckFailedException("The long value '" + name + "' is out of range [" + lowestAllowed + "," + highestAllowed + "]: " + value);
        }
    }

    /**
     * Check whether assertions are "on"
     */
    
    private static boolean isAssertionsOn() {
        boolean assertionsAreOn = false;       
        // the next instruction assigns true to assertionsAreOn only if assertions are on!
        assert (assertionsAreOn=true) == true; 
        return assertionsAreOn;
    }

    /**
     * This call is used when "validate()" is called on structures that have it.
     * The method looks for a parameterless validate() method and invokes it.
     * This method does not care for whether assert is set and throws CheckFailedException 
     * instead of AssertionError. 
     */
    
    public static void validateIt(Object obj) {
        validateIt(obj,false,false);
    }
    
    /**
     * This call is used when "validate()" is called on structures that have it.
     * The method looks for a parameterless validate() method and invokes it. 
     */
   
    public static void validateIt(Object obj, boolean dependsOnAssert, boolean yieldsAssertionError) {
        if (obj == null) {
            throw new CheckFailedException("The passed object is (null), cannot validate it");
        }
        //
        // Return if "depends on assert" and assertions are currently off
        //
        if (dependsOnAssert && !isAssertionsOn()) {
            return;
        }
        //
        // Get the method "validate()", return if it does not exist. The return parameter is not interesting
        //
        Method m = null;
        try {
            m = obj.getClass().getMethod("validate", (Class<?>[])null);
        }
        catch (NoSuchMethodException exe) {
            // Why the brutal throw, Java? Ok, do nothing and return at once
            return;
        }
        assert m!=null;
        //
        // Invoke the method "validate()". This may generate AssertionError or a softer Exception
        //
        Object res = null;
        Throwable tlow = null; // may contain more info
        try {
            res = m.invoke(obj);
        }
        catch (IllegalAccessException exe) {
            // Possible the class is is not public, we are ok with that
            System.err.println("IllegalAccessException while calling validate() of " +  obj.getClass().getName());
            return;
        }
        catch (Throwable t) {
            tlow = t;
        }
        //
        // So what's up? If a Throwable was thrown or validation said "FALSE", assume the game's up
        //
        if (tlow != null || Boolean.FALSE.equals(res)) {
            // Should this yield an AssertionError or a more harmless CheckFailedException? 
            // The CheckFailedException is preferred, but the caller may change that.
            String msg = "Validation of object of type '" + obj.getClass().getName() + "' failed";
            if (yieldsAssertionError) {
                throw new AssertionError(msg);
            }
            else {
                if (tlow == null) {
                    throw new CheckFailedException(msg);
                }
                else {
                    throw new CheckFailedException(msg, tlow);
                }
            }
        }
    }
    
    /**
     * Helper for implications; unfortunately the consequent cannot be lazily evaluated :-(
     */
    
    public static boolean imply(boolean antecedent, boolean consequent) {
        return !antecedent || consequent;
    }
}
