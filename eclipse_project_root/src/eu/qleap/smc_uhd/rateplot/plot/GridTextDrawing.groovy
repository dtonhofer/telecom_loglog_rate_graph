package eu.qleap.smc_uhd.rateplot.plot

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D

import static name.heavycarbon.checks.BasicChecks.*;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 *******************************************************************************
 * Facility to draw text along X/Y grid
 *
 * 2013.10.30 - Created
 ******************************************************************************/

class GridTextDrawing {

    private final Graphiste  g
    private final Graphics2D g2
    private final Map fontMap
    private final Map colorMap
    private final boolean drawContour
    private final Double offsetText

    /**
     * Keys allowing is to distinguish between the text style on major marks and the one on minor marks
     */

    private final static String MAJOR = 'major'
    private final static String MINOR = 'minor'

    /**
     * How to set the y-coordinate of the (rotated) text relative to the location
     */

    enum WhereIsY {

        TOP, BOTTOM, BASELINE, MIDDLE
    }

    /**
     * How to set the x-coordinate of the (rotated) text relative to the location
     */

    enum WhereIsX {

        LEFT, RIGHT, BASELINE, MIDDLE
    }

    /**
     * Constructor
     */

    GridTextDrawing(Graphiste g, Graphics2D g2, Font minorFont, Font majorFont, Color minorColor, Color majorColor, boolean drawContour, double offsetText) {
        assert g
        assert g2
        assert minorFont
        assert majorFont
        assert minorColor
        assert majorColor
        this.g  = g
        this.g2 = g2
        this.fontMap  = [ (MAJOR) : majorFont, (MINOR) : minorFont ]
        this.colorMap = [ (MAJOR) : majorColor, (MINOR) : minorColor ]
        this.drawContour = drawContour
        this.offsetText = offsetText
    }

    /**
     * Write text
     */

    void drawGridLabels(List<GridLabel> gll) {
        def rotateEast
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_GASP)
        [MINOR, MAJOR].each { String m ->
            Font font = fontMap[m]
            Color color = colorMap[m]
            assert font
            assert color
            g2.setFont(font)
            g2.setColor(color)
            gll.each { GridLabel gl ->
                if (gl.isMajor() == (m == MAJOR)) {
                    if (gl.isXAxis) {
                        drawTextForAxis(gl.text, font, gl.location, drawContour, WhereIsX.MIDDLE, WhereIsY.TOP, rotateEast = true)
                    }
                    else {
                        drawTextForAxis(gl.text, font, gl.location, drawContour, WhereIsX.RIGHT, WhereIsY.MIDDLE, rotateEast = false)
                    }
                }
            }
        }
    }

    /**
     * Helper
     */

    private void drawTextForAxis(String text, Font font, Point2D where_imagespace, boolean drawContour, WhereIsX whereIsX, WhereIsY whereIsY, boolean rotateEast) {
        FontMetrics metrics = g2.getFontMetrics(font)
        //
        // Save the transform, move the origin to the (flipped) location
        //
        def t_sav    = g2.getTransform()
        def flip_loc = g.flip(where_imagespace)
        //
        // This means (0,0) --- is moved to ---> (flip_loc.x,flip_loc.y) in the current bitmap coordinates
        // But this happens last
        //
        g2.translate(flip_loc.x,flip_loc.y)
        //
        // The contour is most probably needed. It is of course in bitmap coordinates, with text assumes sitting at (0,0)
        //
        Rectangle2D txtcon = metrics.getStringBounds(text, 0, text.size() , g2)
        //
        // If we are going to rotate (around (0,0)), use a rotated bounding box for finding the translations
        //
        Rectangle2D txtconr
        if (rotateEast) {
            // Rotate the bounding box by hand
            txtconr = new Rectangle2D.Double(-txtcon.maxY, txtcon.minX, txtcon.height, txtcon.width)
        }
        else {
            txtconr = txtcon
        }
        //
        // We may want to set the Y coordinate to not lie on the baseline
        //
        if (whereIsY == WhereIsY.TOP) {
            // This means (0,0) --- is moved to ---> (0,-txtcon.minY) in the current bitmap coordinates
            // ...or alternatively the uppermost point (0,txtcon.minY) --- is moved to ---> (0,0) in the current bitmap coordinates
            g2.translate(0, -txtconr.minY)
        }
        else if (whereIsY == WhereIsY.BOTTOM) {
            // This means (0,0) --- is moved to ---> (0,-txtcon.maxY) in the current bitmap coordinates
            // ...or alternatively the lowermost point (0,txtcon.maxY) --- is moved to ---> (0,0) in the current bitmap coordinates
            g2.translate(0, -txtconr.maxY)
        }
        else if (whereIsY == WhereIsY.BASELINE || whereIsY == null) {
            // This means no change
            g2.translate(0, 0)
        }
        else if (whereIsY == WhereIsY.MIDDLE) {
            // Similar to TOP, but adjusted by half the height
            g2.translate(0, -(txtconr.minY + txtconr.height/2))
        }
        else {
            instaFail("Unhandled 'WhereIsY' value '${whereIsY}'")
        }
        //
        // We may want to set the X coordinate to not lie on the baseline
        //
        if (whereIsX == WhereIsX.LEFT) {
            g2.translate(-txtconr.minX, 0)
        }
        else if (whereIsX == WhereIsX.RIGHT) {
            g2.translate(-txtconr.maxX, 0)
        }
        else if (whereIsX == WhereIsX.BASELINE || whereIsX == null) {
            g2.translate(0, 0)
        }
        else if (whereIsX == WhereIsX.MIDDLE) {
            // This means: (0,0) --- is moved to ---> ( -w/2, 0) in the current bitmap coordinates
            // (0,0) is the leftmost point on the font baseline and its moved to the horizontal middle of the font baseline
            g2.translate(-(txtconr.minX + txtconr.width/2), 0)
        }
        else {
            instaFail("Unhandled 'WhereIsX' value '${whereIsX}'")
        }
        //
        // Rotation east (in the bitmap system, 90 degree clockwise)
        //
        if (rotateEast) {
            def numquadrants
            g2.transform(AffineTransform.getQuadrantRotateInstance(numquadrants = 1))
            if (offsetText) {
                g2.translate(+offsetText,0)
            }
        }
        else {
            if (offsetText) {
                g2.translate(-offsetText,0)
            }
        }
        //
        // Drawing the contour; it is affected by the rotation
        //
        if (drawContour) {
            assert txtcon
            g2.setColor(Color.GRAY)
            int x = txtcon.x
            int y = txtcon.y
            int w = txtcon.width
            int h = txtcon.height
            g2.fillRect(x,y,w,h)
        }
        //
        // Finally draw the text at the translated origin, possibly rotated
        //
        g2.drawString(text, 0, 0)
        //
        // Restore the previous transform
        //
        g2.setTransform(t_sav)
    }

}
