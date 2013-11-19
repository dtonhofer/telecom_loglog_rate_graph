package eu.qleap.smc_uhd.rateplot.plot;

import java.awt.geom.Point2D;

class GeometryHelpers {

    /**
     * Helper
     */
    
    static Point2D log10(Point2D p) {
        return new Point2D.Double(Math.log10(p.x),Math.log10(p.y))
    }
    
    /**
     * Helper; compute b - a
     */
    
    static Point2D delta(Point2D a, Point2D b) {
        return new Point2D.Double(b.x - a.x, b.y - a.y)
    }

    /**
     * Helper: compute p * a
     */
    
    static Point2D mult(Point2D p, double a) {
        return new Point2D.Double(p.x * a, p.y * a)
    }

    /**
     * Helper: compute a * b
     */
    
    static Point2D dot(Point2D a, Point2D b) {
        return new Point2D.Double(a.x * b.x, a.y * b.y)
    }

    /**
     * Helper: compute a + b
     */
    
    static Point2D add(Point2D a, Point2D b) {
        return new Point2D.Double(a.x + b.x, a.y + b.y)
    }

}
