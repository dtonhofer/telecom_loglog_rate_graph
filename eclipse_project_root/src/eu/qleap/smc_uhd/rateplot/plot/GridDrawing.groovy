package eu.qleap.smc_uhd.rateplot.plot

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D;
import java.awt.geom.Line2D
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
 * Draws the loglog grid
 *
 * 2013.10.30 - Created
 ******************************************************************************/

class GridDrawing {

    private final Graphiste  g
    private final Graphics2D g2

    /**
     * Constructor
     */

    GridDrawing(Graphiste g, Graphics2D g2) {
        this.g  = g
        this.g2 = g2
    }

    /**
     * Setting the style of the grid lines. If the index is 1, a "major" grid line is assumed
     */

    private void setMinorOrMajorGridStyle(def isMajor) {
        def capStyle   = BasicStroke.CAP_ROUND
        def joinStyle  = BasicStroke.JOIN_ROUND
        if (isMajor) {
            g2.setStroke(new BasicStroke(g.absoluteMajorGridLineWidth as Float, capStyle, joinStyle))
            g2.setColor(g.majorGridLineColor)
        }
        else {
            // Minor
            g2.setStroke(new BasicStroke(g.absoluteMinorGridLineWidth as Float, capStyle, joinStyle))
            g2.setColor(g.minorGridLineColor)
        }
    }

    /**
     * Draw the major/minor grid for constant Xs; return a list of the points at which labels can be written (major values)
     * One can order only the minor or major grid; this is done to have a consistent image layering
     */

    List<GridLabel> drawGridForConstantX(def wantMajor) {
        List<GridLabel> res = []
        def isXAxis
        //
        // loop through values space, in integer coordinates, and in kbps
        //
        long x_kbps     = MathHelpers.integerExp(MathHelpers.integerLog(g.rangeMin_kbps.x))
        long x_kbps_max = MathHelpers.integerExp(MathHelpers.integerLog(g.rangeMax_kbps.x)+1)
        setMinorOrMajorGridStyle(wantMajor)
        while (x_kbps <= x_kbps_max) {
            //
            // Multiply x by 1,2,...,8,9 to get the "minor" axis; multiplying by 1 means this is a "major" axis
            //
            (1..9).each {
                boolean isMajor = (it == 1)
                if (wantMajor == isMajor) {
                    long cur_x_kbps = x_kbps * it
                    if (g.rangeMin_kbps.x + 0.5 < cur_x_kbps && cur_x_kbps < g.rangeMax_kbps.x - 0.5) {
                        // draw something as this is "in range"
                        // the 0.5 and "<" are to make sure that no line is drawn directly on the X axis
                        Point2D p_in_graph = g.mapToImagespace(new Point2D.Double(cur_x_kbps as Double, 1))
                        double x = p_in_graph.x
                        double extension = 0.0
                        if (isMajor) {
                            // extend the line somewhat in case of major axis
                            extension = g.absoluteMajorGridExtension
                        }
                        def vertLine = new Line2D.Double(x,g.axisLowerLeft.y - extension, x, g.axisUpperRight.y)
                        g2.draw(g.flip(vertLine))
                        res << new GridLabel(isXAxis = true, isMajor, vertLine.p1, cur_x_kbps)
                    }
                }
            }
            //
            // Multiply by 10 to jump to the next major axis
            //
            x_kbps = x_kbps * 10
        }
        return res
    }

    /**
     * Draw the major/minor grid for constant Ys
     */

    List<GridLabel> drawGridForConstantY(def wantMajor) {
        List<GridLabel> res = []
        def isXAxis
        //
        // loop through values space, in integer coordinates, and in kbps
        //
        long y_kbps     = MathHelpers.integerExp(MathHelpers.integerLog(g.rangeMin_kbps.y))
        long y_kbps_max = MathHelpers.integerExp(MathHelpers.integerLog(g.rangeMax_kbps.y)+1)
        setMinorOrMajorGridStyle(wantMajor)
        while (y_kbps <= y_kbps_max) {
            //
            // Multiply y by 1,2,...,8,9 to get the minor axis; multiplying by 1 means this is a major axis
            //
            (1..9).each {
                boolean isMajor = (it == 1)
                if (wantMajor == isMajor) {
                    long cur_y_kbps = y_kbps * it
                    if (g.rangeMin_kbps.y + 0.5 < cur_y_kbps && cur_y_kbps < g.rangeMax_kbps.y - 0.5) {
                        // draw something as this is "in range"
                        // the 0.5 and "<" are to make sure that no line is drawn directly on the Y axis
                        Point2D p_in_graph = g.mapToImagespace(new Point2D.Double(1, cur_y_kbps as Double))
                        double y = p_in_graph.y
                        double extension = 0.0
                        if (isMajor) {
                            // extend the line somewhat in case of major axis
                            extension = g.absoluteMajorGridExtension
                        }
                        def horzLine = new Line2D.Double(g.axisLowerLeft.x - extension, y, g.axisUpperRight.x,y)
                        g2.draw(g.flip(horzLine))
                        res << new GridLabel(isXAxis = false, isMajor , horzLine.p1, cur_y_kbps)
                    }
                }
            }
            //
            // Multiply by 10 to jump to the next major axis
            //
            y_kbps = y_kbps * 10
        }
        return res
    }

    /**
     * Draw the main axis, as lines; returns the labels for the 0 axes
     */

    private List<GridLabel> drawAxisBoundingBox() {
        List<GridLabel> res = []
        def isXAxis, isMajor
        def lineX = new Line2D.Double(g.axisLowerLeft, g.axisLowerRight)
        def lineY = new Line2D.Double(g.axisLowerLeft, g.axisUpperLeft)
        def capStyle   = BasicStroke.CAP_ROUND
        def joinStyle  = BasicStroke.JOIN_ROUND
        def miterLimit = 1.0
        //
        // Style it
        //
        g2.setStroke(new BasicStroke(g.absoluteAxisLineWidth as Float, capStyle, joinStyle))
        g2.setColor(Color.WHITE)
        //
        // Draw it
        //
        g2.draw(g.flip(lineY))
        g2.draw(g.flip(lineX))
        res << new GridLabel(isXAxis = true, isMajor = true, lineX.p1, g.rangeMin_kbps.x as Long)
        res << new GridLabel(isXAxis = false, isMajor = true, lineY.p1, g.rangeMin_kbps.x  as Long)
        //
        // Also draw a dashed bounding box
        //
        g2.setStroke(new BasicStroke(g.absoluteAxisLineWidth/2.0 as Float, capStyle, joinStyle, miterLimit, [
            g.absoluteContourDashLength,
            g.absoluteContourDashLength ] as float[], 0))
        def lineA = new Line2D.Double(g.axisUpperLeft, g.axisUpperRight)
        def lineB = new Line2D.Double(g.axisLowerRight, g.axisUpperRight)
        g2.draw(g.flip(lineA))
        g2.draw(g.flip(lineB))
        return res
    }

}
