package eu.qleap.smc_uhd.rateplot.plot

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 *******************************************************************************
 * Exponentiation and logarithm; always to the power of 10. 
 *
 * 2013.10.30 - Created
 ******************************************************************************/

class MathHelpers {

    /**
     * Integer 10^x
     */

    static long integerExp(int n) {
        assert n >= 0
        long res = 1
        while (n > 0) {
            res = res * 10
            n = n-1
        }
        return res
    }

    /**
     * Integer log_10(x)
     */

    static int integerLog(double x) {
        assert x >= 1.0
        int l = Math.floor(Math.log10(x))
        assert l >= 0
        assert (integerExp(l) as Double) <= x
        return l
    }
}
