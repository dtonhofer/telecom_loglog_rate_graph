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
 * An 'exponentially decaying average over time' is computed; the average is
 * recomputed every time a new measurement is offered, i.e. every time 
 * measure() is called.
 * 
 * We just have to remember 1) The average obtained at the last measurement
 *                          2) When the last measurement was made (t)
 * 
 * Given:    average     = the last average obtained at time t
 *           value       = a new measurement obtained at time dt
 *           kappa(dt)   = a weighting factor that depends on dt
 *           
 * We compute the new average as a weighted sum:
 * 
 *           average    := value * (1-kappa) + average * kappa
 * 
 * To exponentially discount the old average, kappa is computed as
 * 
 *           kappa       = e^(dt * ln(fi)) = fi^dt in [0,1]
 *           
 * Where fi in [0,1] is a constant that expresses the decay speed.
 * 
 * If dt= 1 second, then kappa = fi
 * 
 * If dt -> 0   , then kappa -> 1 (all the weight lies with the old average)
 * If dt -> +oo , then kappa -> 0 (all the weight lies with the new valze)
 * 
 * In this case, we are working with discrete time intervals (milliseconds).
 * So it can happen that two values are observed in the same millisecond, 
 * and the second reevaluation will indeed have dt = 0. It would then be
 * disregarded. 
 * 
 * Instead, we are applying a correction:
 * 
 * If value a was observed and then value b was observed in the same
 * millisecond, then the evaluation would have giveb:
 * 
 *           average    := (a + b) * (1-kappa) + average * kappa
 *                       = (a * (1-kappa) + average * kappa) + b * (1 - kappa)
 *                       = first_computed_average + correction
 *                       
 * So we just add b * (1 - kappa) to the average first computed.
 *                       
 * 2003.09.24 - First release
 * 2004.06.21 - Added to String() 
 * 2004.06.22 - This things seems to be programmed wrong? No, it's just the
 *              commentary that is wrong. Add the possibility to have 
 *              varying decay rate.
 * 2005.03.21 - Warning for negative dt in 'measure' made less shocking.
 *              I.e. we don't stack trace. And indeed, it may happen that
 *              dt < 0 if NTP sets the clock back
 * 2005.11.18 - Reused for integration into AlphaAppender (code copy). Internal
 *              Log4J logging removed. Package visible only. 
 * 2006.02.12 - Simplified. Can now have two measurements at the same millisecond.
 *              Checked math. Subtle problem where > 1 measurements occur in the
 *              same millisecond fixed.
 * 2009.08.26 - Class moved to a new, separate package - com.mplify.log4jlogging
 * 2009.08.28 - Unified with the TimeDecayingAverage of com.mplify.helpers,
 *              which was a copy with an additional measure() and has now been
 *              deleted
 ******************************************************************************/

public class TimeDecayingAverage {

    public final static double LOWER_LIMIT_DECAY_PER_SECOND = 0.00001D;

    /*
     * Running values
     */

    private double average = 0; // the current average
    private double kappa = 0; // the current kappa (initial value is only used if the first measurement is made exactlay at 'when')
    private long when = System.currentTimeMillis() - 1000; // last measurement's time: 1 second ago so that kappa's initial value is not used

    /*
     * The exponential decay value passed for the computation of 'average' may be passed at construction time, but may
     * also vary over time (is that a good idea?), it must lie between 0 and 1.
     */

    private double phi;
    private double ln_phi;

    /**
     * Constructor. Pass the phi ("decay per second") value, a scalar between 0 and 1. This assumes that the
     * measurements have been 0 since the beginning of time.
     */

    public TimeDecayingAverage(double phi) {
        this(phi, 0D);
    }

    /**
     * Constructor. Pass the phi ("decay per second") value, a scalar between 0 and 1. This assumes that the
     * measurements have been 'average' since the beginning of time.
     */

    public TimeDecayingAverage(double phi, double average) {
        setPhi(phi);
        this.average = average;
    }

    /**
     * Assigning the decay-per-second. It must lie between 0.00001 and 1
     */

    public synchronized void setPhi(double phi) {
        if (phi < LOWER_LIMIT_DECAY_PER_SECOND || 1 < phi) {
            throw new IllegalArgumentException("The passed 'decayPerSecond' is " + phi + "; make sure that it lies between " + LOWER_LIMIT_DECAY_PER_SECOND + " and 1");
        }
        this.phi = phi;
        this.ln_phi = Math.log(phi); // negative value
    }

    /**
     * Getting the decay-per-second, aka. the phi
     */

    public synchronized double getPhi() {
        return phi;
    }

    /**
     * Getter for average computed lastly (i.e. actually the 'current average' or the 'latest average') Immediately
     * after creation, returns 0 or the value passed at startup time.
     */

    public synchronized double getAverage() {
        return average;
    }

    /**
     * Getter for last time at measurement was made, returns a long Returns the creation time of the structure if no
     * measurement has ever been made.
     */

    public synchronized long getWhen() {
        return when;
    }

    /**
     * Recompute given a new data point 'newValue' measured at 'newDate'. The new 'time-decaying average' is returned.
     * This is used in the special case where the value is just the time difference in ms to the last measurement.
     */

    public synchronized double measure(long newWhen) {
        return measure(newWhen - when, newWhen);
    }

    /**
     * Recompute given a new data point 'newValue' measured at 'newDate'. The new 'time-decaying average' is returned.
     */

    public synchronized double measure(double value, long when) {
        // compute difference to last time in (seconds)
        double dt = (when - this.when) / 1000.0D;
        if (dt < 0) {
            // if the time is negative, we just skip computation (happens if NTP sets the clock back)
        } else if (dt == 0) {
            // if no time went by we have the special case where the average is just corrected
            this.average = value * (1.0 - this.kappa) + this.average;
        } else {
            // a positive dt means reevalute fully			
            this.kappa = Math.exp(dt * this.ln_phi);
            this.average = value * (1.0 - this.kappa) + this.average * this.kappa;
            this.when = when;
        }
        return average;
    }
}
