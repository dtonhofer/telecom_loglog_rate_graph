package eu.qleap.smc_uhd.rateplot.desc

import java.awt.Color;
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
 * Represent a "UHD region" which is illustrated with an appropriately-colored
 * polygon in the final graph.  
 * 
 * 2013.11.03 - Created
 * 2013.11.18 - Reviewed and adapted
 ******************************************************************************/

class RateRegion {

    enum Level          {
        Niveau0, Niveau1, Niveau2, Niveau3
    }
    enum Characteristic {
        Good, SlowUpload
    }

    final Level level
    final Characteristic chara
    final Color color
    final List<Point2D> corners_valuespace    
    
    private RateRegion(Level l, Characteristic c, Point2D ... p) {
        checkNotNull(l, "level")
        checkNotNull(c, "characteristic")
        checkNotNull(p, "array of points")        
        checkTrue(p.length >= 3, "size of array of points is too small: '%d'", p.length)
        this.level = l
        this.chara = c
        this.color = determineColor(l, c)
        this.corners_valuespace = Collections.unmodifiableList(p as List)
    }

    // everything is in "mbps"

    final private static mbps = 1000 // 1000 kbps

    final private static double superslow = 100 // 100 kbps

    final private static double n0down = 25 * mbps
    final private static double n0up = 10 * mbps

    final private static double n1down = 30 * mbps
    final private static double n1up = n1down / 2.0

    final private static double n2down = 100 * mbps
    final private static double n2up = n2down / 2.0

    final private static double n3down = 1000 * mbps
    final private static double n3up = n3down / 2.0

    final private static double n4down = 1000000 * mbps
    final private static double n4up = n4down / 2.0

    final private static double infinity = 1000000000

    final private static Point2D n0_a1 = new Point2D.Double(n0down, n0up)
    final private static Point2D n0_a2 = new Point2D.Double(n0down, n0down)
    final private static Point2D n0_a3 = new Point2D.Double(n1down, n1down)
    final private static Point2D n0_a4 = new Point2D.Double(n1down, n1up)
    final private static Point2D n0_a5 = new Point2D.Double(n4down, n1up)
    final private static Point2D n0_a6 = new Point2D.Double(n4down, n0up)
    
    final private static Point2D n1_a1 = new Point2D.Double(n1down, n1up)
    final private static Point2D n1_a2 = new Point2D.Double(n1down, n1down)
    final private static Point2D n1_a3 = new Point2D.Double(n2down, n2down)
    final private static Point2D n1_a4 = new Point2D.Double(n2down, n2up)
    final private static Point2D n1_a5 = new Point2D.Double(n4down, n2up)
    final private static Point2D n1_a6 = new Point2D.Double(n4down, n1up)
    // final private static Point2D n1_a7 = new Point2D.Double(n2down, n1up)
    
    final private static Point2D n2_a1 = new Point2D.Double(n2down, n2up)
    final private static Point2D n2_a2 = new Point2D.Double(n2down, n2down)
    final private static Point2D n2_a3 = new Point2D.Double(n3down, n3down)
    final private static Point2D n2_a4 = new Point2D.Double(n3down, n3up)
    final private static Point2D n2_a5 = new Point2D.Double(n4down, n3up)
    final private static Point2D n2_a6 = new Point2D.Double(n4down, n2up)
    // final private static Point2D n2_a7 = new Point2D.Double(n3down, n2up)
    
    final private static Point2D n3_a1 = new Point2D.Double(n3down, n3up)
    final private static Point2D n3_a2 = new Point2D.Double(n3down, n3down)
    final private static Point2D n3_a3 = new Point2D.Double(n4down, n4down)
    final private static Point2D n3_a4 = new Point2D.Double(n4down, n3up)

    final private static Point2D n1_a1_slow = new Point2D.Double(n1down, n1up)
    final private static Point2D n1_a2_slow = new Point2D.Double(n1down, superslow)
    final private static Point2D n1_a3_slow = new Point2D.Double(n2down, superslow)
    final private static Point2D n1_a4_slow = new Point2D.Double(n2down, n1up)

    final private static Point2D n2_a1_slow = new Point2D.Double(n2down, n1up)
    final private static Point2D n2_a2_slow = new Point2D.Double(n2down, superslow)
    final private static Point2D n2_a3_slow = new Point2D.Double(n3down, superslow)
    final private static Point2D n2_a4_slow = new Point2D.Double(n3down, n1up)

    final private static Point2D n3_a1_slow = new Point2D.Double(n3down, n1up)
    final private static Point2D n3_a2_slow = new Point2D.Double(n3down, superslow)
    final private static Point2D n3_a3_slow = new Point2D.Double(n4down, superslow)
    final private static Point2D n3_a4_slow = new Point2D.Double(n4down, n1up)

    /**
     * This is called by "Graphiste" to get a specific "RateRegion"
     */

    static RateRegion obtain(Level l, Characteristic c) {
        if (l == Level.Niveau0) {
            if (c == Characteristic.Good) {
                return new RateRegion(l,c,n0_a1,n0_a2,n0_a3,n0_a4,n0_a5,n0_a6)
            }
            else {
                instaFail("Unknown characteristic '${c}'")
            }
        }
        else if (l == Level.Niveau1) {
            if (c == Characteristic.Good) {
                return new RateRegion(l,c,n1_a1,n1_a2,n1_a3,n1_a4,n1_a5,n1_a6)
            }
            else if (c == Characteristic.SlowUpload) {
                return new RateRegion(l,c,n1_a1_slow,n1_a2_slow,n1_a3_slow,n1_a4_slow)
            }
            else {
                instaFail("Unknown characteristic '${c}'")
            }
        }
        else if (l == Level.Niveau2) {
            if (c == Characteristic.Good) {
                return new RateRegion(l,c,n2_a1,n2_a2,n2_a3,n2_a4,n2_a5,n2_a6)
            }
            else if (c == Characteristic.SlowUpload) {
                return new RateRegion(l,c,n2_a1_slow,n2_a2_slow,n2_a3_slow,n2_a4_slow)
            }
            else {
                instaFail("Unknown characteristic '${c}'")
            }
        }
        else if (l == Level.Niveau3) {
            if (c == Characteristic.Good) {
                return new RateRegion(l,c,n3_a1,n3_a2,n3_a3,n3_a4,n3_a4)
            }
            else if (c == Characteristic.SlowUpload) {
                return new RateRegion(l,c,n3_a1_slow,n3_a2_slow,n3_a3_slow,n3_a4_slow)
            }
            else {
                instaFail("Unknown characteristic '${c}'")
            }
        }
        else {
            instaFail("Unknown level '${l}'")
        }
    }

    /**
     * Determine color for region
     */

    private static Color determineColor(Level l, Characteristic c) {
        assert l
        assert c
        Map levelMap = [
            (Level.Niveau0) : determineColorFromInteger(0),
            (Level.Niveau1) : determineColorFromInteger(1),
            (Level.Niveau2) : determineColorFromInteger(2),
            (Level.Niveau3) : determineColorFromInteger(3)
        ]
        Color color = levelMap[l]
        assert color
        float[] values = color.getRGBColorComponents(null)
        float red   = values[0]
        float green = values[1]
        float blue  = values[2]
        float alpha = 0.8
        switch (c) {
            case Characteristic.SlowUpload:
            // slow upload is darker and more alphaed, exept for the Niveau0, which is uniform in that regard
                if (l != Level.Niveau0) {
                    red   = 2 * red   / 3
                    green = 2 * green / 3
                    blue  = 2 * blue  / 3
                    alpha = alpha / 2
                }
                break
        }
        return new Color(red, green, blue, alpha)
    }


    /**
     * Helper; pass the "niveau", a value [0..3]
     */

    private static Color determineColorFromInteger(int n) {
        assert 0<=n && n<=3
        if (n == 0) {
            // fix color
            return new Color(0xffa800)
        }
        else {
            // a "dégradé" between greenish and blueish
            int high_col = 0x00a0ff
            int low_col  = 0x2cff00
            float num = 2
            float high_red   = ((high_col >> 16) & 0xFF) / 255.0
            float high_green = ((high_col >> 8) & 0xFF) / 255.0
            float high_blue  = ((high_col) & 0xFF) / 255.0
            float low_red    = ((low_col >> 16) & 0xFF)  / 255.0
            float low_green  = ((low_col >> 8) & 0xFF) / 255.0
            float low_blue   = ((low_col) & 0xFF) / 255.0
            float d_red    = high_red   - low_red
            float d_green  = high_green - low_green
            float d_blue   = high_blue  - low_blue
            float red   = Math.min(1.0, (low_red   + (n-1) * d_red/num))
            float green = Math.min(1.0, (low_green + (n-1) * d_green/num))
            float blue  = Math.min(1.0, (low_blue  + (n-1) * d_blue/num))
            return new Color(red, green, blue)
        }
    }

}
