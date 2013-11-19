package com.mplify.logging;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2010, M-PLIFY S.A.
 *                     68, avenue de la Liberté
 *                     L-1930 Luxembourg
 *
 * 2013-01: Released under the MIT License (http://opensource.org/licenses/MIT) 
 *******************************************************************************
 *******************************************************************************
 * An 'exponentially decaying average *rate* over time' (in Hz, of course) is
 * computed; the average rate is recomputed every time a new measurement is 
 * offered, i.e. every time measure() is called.
 * 
 * 2003.09.24 - First release
 * 2004.06.21 - Added to String(), fixed stupid bullshit error that computed
 *              dx/dt where dx depended on a never initialized value. Beurk!
 * 2005.11.18 - Reused for integration into AlphaAppender (code copy). Internal 
 *              calls to Log4J removed. Package visible only.
 * 2006.02.12 - Simplified. Can now have two measurements at the same millisecond.
 *              Revised math.
 * 2009.08.28 - Unified with the TimeDecayingRateAverage of com.mplify.helpers
 ******************************************************************************/

public class TimeDecayingRateAverage {

    private TimeDecayingAverage tda;
    private double oldDt = 1; // an arbitrary nonzero interval  

    /**
     * Constructor. Pass 'phi' aka 'decayPerSecond', a scalar between 0 and 1, used in performing the exponential decay.
     * Assumes that the measurements have been 0 since the beginning of time.
     */

    public TimeDecayingRateAverage(double phi) {
        this(phi, 0D);
    }

    /**
     * Constructor. Pass 'phi' aka 'decayPerSecond', a scalar between 0 and 1, used in performing the exponential decay.
     * Also set a rate that is supposed to have been valid from the beginning of time
     */

    public TimeDecayingRateAverage(double phi, double rate) {
        tda = new TimeDecayingAverage(phi, rate);
    }

    /**
     * Get 'phi' aka. 'decay per second' which determines how strongly the value is discounted.
     */

    public double getPhi() {
        return tda.getPhi();
    }

    /**
     * Set 'phi' aka. 'decay per second' which determines how strongly the value is discounted.
     */

    public void setPhi(double phi) {
        tda.setPhi(phi);
    }

    /**
     * Getter for average rate last computed
     */

    public double getAverageRate() {
        return tda.getAverage();
    }

    /**
     * Compute the new rate given a new measurement of an additional 'value' units taken at 'when. The new average rate
     * is returned.
     */

    public synchronized double measure(double value, long when) {
        // compute difference to last time in (seconds)
        double dt = (when - tda.getWhen()) / 1000.0D;
        if (dt <= 0) {
            // If the time is negative, we use the 'value' and consider that no time
            // has elapsed, essentially correcting the result (happens if NTP sets the clock back)
            // This also covers the case where two measurements fall into the same millisecond 
            return tda.measure(value / oldDt, tda.getWhen()); // reusing the 'when' of tda will cause it to just correct its values
        } else {
            // a positive dt means reevalute fully
            this.oldDt = dt;
            return tda.measure(value / dt, when);
        }
    }
}
