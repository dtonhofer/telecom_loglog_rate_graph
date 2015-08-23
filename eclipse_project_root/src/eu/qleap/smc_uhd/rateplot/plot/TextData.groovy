package eu.qleap.smc_uhd.rateplot.plot

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D


/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 *******************************************************************************
 * Text data associate to a value in the graph
 *
 * 2013.10.30 - Created
 ******************************************************************************/

class TextData {

    final private Graphiste g          // needed for drawing   

    final Point2D dotpos_imagespace    // where the "dot" (not the text) will be placed
    final String text                  // text to display
    final Color  color                 // color to use
    final Font   font                  // font to use

    Point2D textpos_imagespace         // this will vary: where will the text be?

    /**
     * Constructor
     */

    TextData(Point2D dotpos_imagespace, String text, Color color, Font font, Graphiste g) {
        this.dotpos_imagespace  = dotpos_imagespace
        this.text               = text
        this.color              = color
        this.font               = font
        this.g                  = g
        // initially the text is at its assigned x position and at the y position of the dot:
        this.textpos_imagespace = new Point2D.Double(g.absoluteDescPosition, dotpos_imagespace.y)
    }

    /**
     * Getting the text contour only (in image space), if the text *were to be* be drawn
     */

    Rectangle2D getContourInImagespace(Graphics2D g2) {
        FontMetrics metrics        = g2.getFontMetrics(font)
        Rectangle2D res            = metrics.getStringBounds(text, 0, text.size() , g2) // text assumes at origin (0,0)
        Rectangle2D res_imageSpace = new Rectangle2D.Double(textpos_imagespace.x + res.x ,textpos_imagespace.y - ( res.y + res.height ), res.width, res.height)
        return res_imageSpace

    }

    /**
     * Drawing the text at the given image space location "location"
     */

    void drawText(Graphics2D g2, boolean drawContour) {
        def t_sav = g2.getTransform()
        def where = g.flip(textpos_imagespace)
        g2.setFont(font)
        g2.translate(where.x,where.y)
        //
        // Drawing the contour at the translated origin (which is what we want)
        //
        if (drawContour) {
            FontMetrics metrics = g2.getFontMetrics(font)
            Rectangle2D contour_bitmapspace = metrics.getStringBounds(text, 0, text.size() , g2)
            g2.setColor(Color.GRAY)
            int x = contour_bitmapspace.x
            int y = contour_bitmapspace.y
            int w = contour_bitmapspace.width
            int h = contour_bitmapspace.height
            g2.fillRect(x,y,w,h)
        }
        //
        // Draw the text at the *translated origin*
        //
        g2.setColor(color)
        g2.drawString(text, 0, 0)
        //
        // Restore the previous transform
        //
        g2.setTransform(t_sav)
    }

    /**
     * Draw a line from the point to the text. This does not really work if the text is very high above the
     * symbol's 'x' posiiton.
     */

    void drawLinkLine(Graphics2D g2) {
        // dotpos_imagespace --> middle of text
        Rectangle2D text_contour_imagespace  = getContourInImagespace(g2)
        Rectangle2D text_contour_bitmapspace = g.flip(text_contour_imagespace) 
        //
        // Where is the endpoint of the line in front of the text
        //
        Point2D textdot_imagespace
        TDIS: {
            def x = text_contour_imagespace.x  - g.absoluteLineStandoff/2
            def y = text_contour_imagespace.y  + text_contour_imagespace.height/2
            textdot_imagespace = new Point2D.Double(x,y)
        }
        Point2D textdot_bitmapspace  = g.flip(textdot_imagespace)
        //
        // Where is the point where the yellow line goes from slope 1 to slope 0
        //        
        double dh = textdot_imagespace.y - dotpos_imagespace.y
        Point2D curvepos_imagespace = new Point2D.Double(dotpos_imagespace.x + dh, dotpos_imagespace.y + dh)
        Point2D curvepos_bitmapspace = g.flip(curvepos_imagespace)
        //
        // Where is the symbol position
        //        
        Point2D dotpos_bitmapspace   = g.flip(dotpos_imagespace)        
        //
        // Style it
        //
        def alphaValue = 128
        g2.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),alphaValue))
        g2.setStroke(new BasicStroke(g.absoluteLinkLineWidth as Float))
        //
        // Line in front of text
        //
        LINE: {
            int x1 = textdot_bitmapspace.x
            int y1 = textdot_bitmapspace.y + text_contour_bitmapspace.height/2
            int x2 = x1
            int y2 = textdot_bitmapspace.y - text_contour_bitmapspace.height/2
            g2.drawLine(x1, y1, x2, y2)
        }
        //
        // Line from symbol to text
        //
        PATH: {
            Path2D p = new Path2D.Double();
            p.moveTo(dotpos_bitmapspace.x,dotpos_bitmapspace.y)
            p.lineTo(curvepos_bitmapspace.x,curvepos_bitmapspace.y)
            p.lineTo(textdot_bitmapspace.x,textdot_bitmapspace.y)
            g2.draw(p)
        }
        //
        // Dot inside text
        //F
        DOT: {
            double radius = 2 * g.absoluteSymbolLineWidth
            int x = dotpos_bitmapspace.x - radius
            int y = dotpos_bitmapspace.y - radius
            int w = 2 * radius
            g2.fillOval(x,y,w,w)
        }
    }

}