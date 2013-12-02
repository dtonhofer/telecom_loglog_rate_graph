package eu.qleap.smc_uhd.rateplot.desc

import java.awt.geom.Point2D

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2013, Q-LEAP S.A.
 *                     14 rue Aldringen
 *                     L-1118 Luxembourg
 *
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 *******************************************************************************
 *******************************************************************************
 * Generate the lines (in this case List<Point2D>) or the possible 
 * downstream/upstream values that VDSL2 implementations may have. 
 *
 * See http://de.wikipedia.org/wiki/Very_High_Speed_Digital_Subscriber_Line
 * 
 * 2013.11.03 - Created
 ******************************************************************************/

class Vdsl2Plot {

    enum Profile {

        p_8x("8x","Down+Up = 50 Mbit/s"), 
        p_12x("12x", "Down+Up = 68 Mbit/s"),
        p_17a("17a", "Down+Up = 100 Mbit/s"),
        p_30a("30a", "Down+Up = 200 Mbit/s")

        final String p_text
        final String rate_text

        Profile(String p_text, rate_text) {
            this.p_text = p_text
            this.rate_text = rate_text
        }
    }

    final static Map rateInKbpsForProfile = Collections.unmodifiableMap(
    [
        (Profile.p_8x) : 50 * 1000,
        (Profile.p_12x) : 68 * 1000,
        (Profile.p_17a) : 100 * 1000,
        (Profile.p_30a) : 200 * 1000
    ]
    )

    static List<Point2D> getValuespacePoints(Profile p, double step_kbps) {
        List<Point2D> res = []
        assert p
        double rate_kbps = rateInKbpsForProfile[p]
        assert rate_kbps
        //
        // Start with the highest downstream rate: rate_kpbs less 1 mbps
        // This yields the upstream rate because "downstream rate + upstream rate = rate of profile"
        //
        double down_kbps = rate_kbps - 1000
        double up_kbps = rate_kbps - down_kbps
        def stop = false
        while (!stop) {
            res << new Point2D.Double(down_kbps, up_kbps)
            down_kbps = down_kbps - step_kbps
            up_kbps = rate_kbps - down_kbps
            stop = (up_kbps > down_kbps)
        }
        return res
    }
}
