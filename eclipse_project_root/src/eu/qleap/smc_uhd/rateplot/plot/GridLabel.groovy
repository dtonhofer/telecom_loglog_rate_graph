package eu.qleap.smc_uhd.rateplot.plot

import java.awt.geom.Point2D

import static com.example.BasicChecks.*

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2013, Q-LEAP S.A.
 *                     14 rue Aldringen
 *                     L-1118 Luxembourg
 *
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 *******************************************************************************
 *******************************************************************************
 * A single label on the loglog grid
 *
 * 2013.10.30 - Created
 ******************************************************************************/

class GridLabel {

    final boolean        isXAxis
    final boolean        isMajor
    final Point2D.Double location
    final long           kbps

    GridLabel(boolean isXAxis, boolean isMajor, Point2D.Double location, long kbps) {
        this.isXAxis    = isXAxis
        this.isMajor    = isMajor
        this.location   = location
        this.kbps       = kbps
    }
    
    boolean isMinor() {
        return !isMajor
    }

    boolean isMajor() {
        return isMajor
    }

    static String textSpeed(long kbps) {
        if (kbps < 1000) {
            // below 1 Mbit/s, return the value in kilobit/s ("kbit/s")
            return "${kbps} kbit/s"
        }
        else if (kbps < 1000000) {
            // below 1 Gbit/s, return the value in megabit/s ("Mbit/s")
            if ((kbps % 1000) == 0) {
                long x = kbps / 1000
                return "${x} Mbit/s"
            }
            else {
                double x  = kbps / 1000
                String xx = sprintf("%.2f", x)
                return "${xx} MBit/s"
            }
        }
        else if (kbps < 1000000000) {
            // below 1 Tbit/s, return the value in gigabit/s ("Gbit/s")
            if ((kbps % 1000000) == 0) {
                long x = kbps / 1000000
                return "${x} Gbit/s"
            }
            else {
                double x  = kbps / 1000000
                String xx = sprintf("%.2f", x)
                return "${xx} GBit/s"
            }
        }
        else {
            instaFail("Terabit/s is a bit large")
        }
    }
    
    String getText() {
        textSpeed(kbps)
    }
}
