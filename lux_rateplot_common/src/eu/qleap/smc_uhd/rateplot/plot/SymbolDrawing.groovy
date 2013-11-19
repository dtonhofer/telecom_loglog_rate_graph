package eu.qleap.smc_uhd.rateplot.plot

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Line2D
import java.awt.geom.Point2D

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2013, Q-LEAP S.A.
 *                     14 rue Aldringen
 *                     L-1118 Luxembourg
 *
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 *******************************************************************************
 *******************************************************************************
 * Library to draw symbols
 *
 * 2013.10.30 - Created
 ******************************************************************************/

class SymbolDrawing {
    
    private final Graphiste  g
    private final Graphics2D g2

    private final static double SQRT_3_HALF = Math.sqrt(3)/2.0
    private final static double SQRT_2      = Math.sqrt(2)

    /**
     * Constructor
     */

    SymbolDrawing(Graphiste g, Graphics2D g2) {
        this.g  = g
        this.g2 = g2
    }

    /**
     * Helper
     */

    private void setBrush(Color col) {
        g2.setStroke(new BasicStroke(g.absoluteSymbolLineWidth as Float, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND) )
        g2.setColor(col)
    }

    /**
     * Given a point in image space (Y increases from bottom to top), draw a delta around that point in color "col" 
     */

    void drawDelta(Point2D p_img, Color col) {
        setBrush(col)
        def rcoords = [
            [0, 1],
            [SQRT_3_HALF, -1/2.0],
            [-SQRT_3_HALF, -1/2.0]
        ]
        def points = rcoords.collect {
            double tx = it[0]
            double ty = it[1]
            new Point2D.Double( p_img.x + tx * g.absoluteSymbolRadius , p_img.y + ty * g.absoluteSymbolRadius)
        }
        [0, 1, 2].each {
            def line_img = new Line2D.Double(points[it-1],points[it])
            g2.draw(g.flip(line_img))
        }
    }

    void drawNabla(Point2D p_img, Color col) {
        setBrush(col)
        def rcoords = [
            [0, -1],
            [SQRT_3_HALF, 1/2.0],
            [-SQRT_3_HALF, 1/2.0]
        ]
        def points = rcoords.collect {
            double tx = it[0]
            double ty = it[1]
            new Point2D.Double( p_img.x + tx * g.absoluteSymbolRadius , p_img.y + ty * g.absoluteSymbolRadius)
        }
        [0, 1, 2].each {
            def line_img = new Line2D.Double(points[it-1],points[it])
            g2.draw(g.flip(line_img))
        }
    }

    void drawCircle(Point2D p_img, Color col) {
        setBrush(col)
        int diameter = g.absoluteSymbolRadius * 2.0 as Integer
        def upper_left_corner_img = new Point2D.Double(p_img.x - g.absoluteSymbolRadius,p_img.y + g.absoluteSymbolRadius)
        def upper_left_corner_bmp = g.flip(upper_left_corner_img)
        g2.drawArc(
                upper_left_corner_bmp.x as Integer,
                upper_left_corner_bmp.y as Integer,
                diameter,
                diameter,
                0,
                360)
    }

    void drawSquare(Point2D p_img, Color col) {       
        setBrush(col)
        def rcoords = [
            [+1, +1],
            [+1, -1],
            [-1, -1],
            [-1, +1]
        ]
        def points_img = rcoords.collect {
            double tx = it[0]
            double ty = it[1]
            new Point2D.Double( p_img.x + tx * g.absoluteSymbolRadius , p_img.y + ty * g.absoluteSymbolRadius)
        }
        [0, 1, 2, 3].each {
            def line_img = new Line2D.Double(points_img[it-1],points_img[it])
            g2.draw(g.flip(line_img))
        }
    }

    void drawCross(Point2D p_img, Color col) {
        setBrush(col)
        def rcoords = [
            [+SQRT_2 / 2, +SQRT_2 / 2],
            [+SQRT_2 / 2, -SQRT_2 / 2],
            [-SQRT_2 / 2, -SQRT_2 / 2],
            [-SQRT_2 / 2, +SQRT_2 / 2]]
        def points_img = rcoords.collect {
            double tx = it[0]
            double ty = it[1]
            new Point2D.Double( p_img.x + tx * g.absoluteSymbolRadius , p_img.y + ty * g.absoluteSymbolRadius)
        }
        def line1_img = new Line2D.Double(points_img[0], points_img[2])
        def line2_img = new Line2D.Double(points_img[1], points_img[3])
        g2.draw(g.flip(line1_img))
        g2.draw(g.flip(line2_img))
    }

}
