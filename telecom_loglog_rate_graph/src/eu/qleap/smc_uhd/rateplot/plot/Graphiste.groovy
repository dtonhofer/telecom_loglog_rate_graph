package eu.qleap.smc_uhd.rateplot.plot

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Polygon
import java.awt.RenderingHints
import java.awt.geom.Line2D
import java.awt.geom.Path2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage

import javax.imageio.ImageIO

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static com.example.BasicChecks.*

import eu.qleap.smc_uhd.rateplot.desc.RateData
import eu.qleap.smc_uhd.rateplot.desc.RateRegion
import eu.qleap.smc_uhd.rateplot.desc.Vdsl2Plot
import eu.qleap.smc_uhd.rateplot.desc.RateRegion.Characteristic
import eu.qleap.smc_uhd.rateplot.desc.RateRegion.Level
import eu.qleap.smc_uhd.rateplot.desc.Vdsl2Plot.Profile

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2013, Q-LEAP S.A.
 *                     14 rue Aldringen
 *                     L-1118 Luxembourg
 *
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 *******************************************************************************
 *******************************************************************************
 * The main drawing function, pulls things together
 *
 * 2013.10.30 - Created
 ******************************************************************************/

class Graphiste {

    private final static CLASS = Graphiste.class.name
    private final static Logger LOGGER_findShapeInRateDataHints = LoggerFactory.getLogger("${CLASS}.findShapeInRateDataHints")
    private final static Logger LOGGER_findColorInRateDataHints = LoggerFactory.getLogger("${CLASS}.findColorInRateDataHints")

    // Absolute value: Width of whole image (including borders) in pixel

    final static double imageWidth  = 5000

    // We write some stuff on the right side

    final static double imageExtensionRightRelative  = 0.4 // image enlarged on the right for text
    final static double imageExtensionRight          = imageExtensionRightRelative * imageWidth

    // --- Value space ---
    // Range to be displayed (in kbps); x coord is "down"; y coord is "up"

    final static Point2D rangeMin_kbps = new Point2D.Double(200,200) // (1 Mbps, 1 Mbps)
    final static Point2D rangeMax_kbps = new Point2D.Double(2.0 * 1000 * 1000,2.0 * 1000 * 1000) // (2 Gbps, 2 Gbps)

    // --- LogLog space ---
    // Range of the displayed loglog space

    final static Point2D loglogMin      = GeometryHelpers.log10(rangeMin_kbps)
    final static Point2D loglogMax      = GeometryHelpers.log10(rangeMax_kbps)
    final static Point2D loglogRange    = GeometryHelpers.delta(loglogMin, loglogMax)

    // Set various values relative to "imageWidth"

    final static double relativeImageHeight        = 1.0
    final static double relativeImageContourSize   = 10/100.0 // 10% contour
    final static double relativeAxisLineWidth      = 1/250.0
    final static double relativeMajorGridLineWidth = 1/500.0
    final static double relativeMajorGridExtension = 1/80.0
    final static double relativeMinorGridLineWidth = 1/750.0
    final static double relativeSymbolRadius       = 1/100.0
    final static double relativeSymbolLineWidth    = 1/750.0
    final static double relativeControurDashLength = 1/200.0
    final static double relativeFontSizeCorrection = 1/500.0
    final static double relativeDescOffset         = 1/80.0

    // Compute corresponding absolute values

    final static double imageHeight                = imageWidth * relativeImageHeight
    final static double absoluteAxisLineWidth      = imageWidth * relativeAxisLineWidth
    final static double absoluteMajorGridLineWidth = imageWidth * relativeMajorGridLineWidth
    final static double absoluteMajorGridExtension = imageWidth * relativeMajorGridExtension
    final static double absoluteMinorGridLineWidth = imageWidth * relativeMinorGridLineWidth
    final static double absoluteSymbolRadius       = imageWidth * relativeSymbolRadius
    final static double absoluteSymbolLineWidth    = imageWidth * relativeSymbolLineWidth
    final static double absoluteLinkLineWidth      = imageWidth * relativeSymbolLineWidth * 2
    final static double absoluteContourDashLength  = imageWidth * relativeControurDashLength
    final static double absoluteFontSizeCorrection = imageWidth * relativeFontSizeCorrection
    final static double absoluteDescOffset         = imageWidth * relativeDescOffset
    final static double absoluteDescPosition       = imageWidth * (1-relativeImageContourSize) + absoluteDescOffset
    final static double absoluteLineStandoff       = imageWidth * relativeSymbolRadius
    final static double absoluteConvolutionRadius  = imageWidth * relativeSymbolRadius / 2

    // Positions of the axis, the coordinates are in "image space" with
    // x increasing from left to right
    // y increasing from lower to higher (the other way than Image2D space)

    final static Point2D axisLowerLeft  = new Point2D.Double(imageWidth * relativeImageContourSize     , imageHeight * relativeImageContourSize)
    final static Point2D axisLowerRight = new Point2D.Double(imageWidth * (1-relativeImageContourSize) , imageHeight * relativeImageContourSize)
    final static Point2D axisUpperLeft  = new Point2D.Double(imageWidth * relativeImageContourSize     , imageHeight * (1 - relativeImageContourSize))
    final static Point2D axisUpperRight = new Point2D.Double(imageWidth * (1-relativeImageContourSize) , imageHeight * (1 - relativeImageContourSize))
    final static Point2D log10Stretch   = computeLog10Stretch()

    // We are working with these; BufferedImage is not final because we will replace it when blurring

    private final static Font  gridLabelMajorFont = new Font("SansSerif", Font.PLAIN, fontSizeInPoints(1.2));
    private final static Font  gridLabelMinorFont = new Font("SansSerif", Font.PLAIN, fontSizeInPoints(0.7));
    private final static Font  descFont           = new Font("SansSerif", Font.PLAIN, fontSizeInPoints(1));
    private final static Color linkLineColor      = new Color(0xfff000,true)
    private final static Color majorGridLineColor = new Color(0xcbcbcb)
    private final static Color minorGridLineColor = new Color(0x555555)
    private final static Color majorGridFontColor = new Color(0xcbcbcb)
    private final static Color minorGridFontColor = new Color(0x555555)


    BufferedImage draw(Collection<RateData> rdColl, boolean backgroundBlur, boolean drawVdsl2Limits)  {
        BufferedImage img = new BufferedImage((imageWidth + imageExtensionRight) as Integer, imageHeight as Integer, BufferedImage.TYPE_INT_RGB)
        //
        // Background creates a new "img"
        //
        img = drawBackgroundWithRegions(img, backgroundBlur)
        //
        // Draw grid on top of background (using the same img)
        //
        drawGrid(img)
        //
        // Draw symbols at data points (using the same img), collecting text information into "tdAll"
        //
        List<TextData> tdAll = []
        //
        // Draw the lines representing possible VDSL2 regimes , collecting text information into "tdAll"
        //
        if (drawVdsl2Limits) {
            def tdVdsl2 = drawVdsl2ProfileLines(img, Color.YELLOW)
            tdAll.addAll(tdVdsl2)
        }
        //
        // Draw symbols at data points (using the same img), collecting text information into "tdAll"
        //
        def tdSymbols = drawSymbolsInGraph(img, rdColl)
        tdAll.addAll(tdSymbols)
        //
        // Draw text information and the "link lines"  (using the same img)
        //
        drawTextAlongRightSideOfGraph(img, tdAll)
        return img
    }

    /**
     * Draw grid to "img" using a local Graphics2D
     */

    private void drawGrid(BufferedImage img) {
        Graphics2D g2
        try  {
            def major, drawContour
            g2 = img.createGraphics()
            GridDrawing gdw = new GridDrawing(this,g2)
            //
            // draw minor grid, collecting labels
            //
            List<GridLabel> xLabelsMinor = gdw.drawGridForConstantX(major = false)
            List<GridLabel> yLabelsMinor = gdw.drawGridForConstantY(major = false)
            //
            // draw major grid over that, collecting labels
            //
            List<GridLabel> xLabelsMajor = gdw.drawGridForConstantX(major = true)
            List<GridLabel> yLabelsMajor = gdw.drawGridForConstantY(major = true)
            //
            // draw bounding box (with the axis) over that
            //
            List<GridLabel> zeroLabels = gdw.drawAxisBoundingBox()
            //
            // draw labels collected
            //
            GridTextDrawing gtdw = new GridTextDrawing(this, g2, gridLabelMinorFont, gridLabelMajorFont, minorGridFontColor, majorGridFontColor, drawContour = false, absoluteDescOffset)
            gtdw.drawGridLabels(xLabelsMinor + yLabelsMinor + xLabelsMajor + yLabelsMajor + zeroLabels)
        }
        finally {
            g2?.dispose()
        }
    }

    /**
     * Helper
     */

    private static void clipToGraphArea(Graphics2D g2) {
        assert g2
        Point2D axisUpperLeft_bitmapspace  = flip(axisUpperLeft)
        Point2D axisLowerRight_bitmapspace = flip(axisLowerRight)
        int clip_x      = axisUpperLeft_bitmapspace.x
        int clip_y      = axisUpperLeft_bitmapspace.y
        int clip_width  = axisLowerRight_bitmapspace.x - axisUpperLeft_bitmapspace.x
        int clip_height = axisLowerRight_bitmapspace.y - axisUpperLeft_bitmapspace.y
        g2.setClip(clip_x, clip_y, clip_width, clip_height)
    }

    /**
     * Helper
     */

    private static Polygon generateBitmapspacePolygon(RateRegion rr) {
        assert rr
        Polygon p_bitmapspace = new Polygon()
        rr.corners_valuespace.each { Point2D c_valuespace ->
            Point2D c_imagespace  = mapToImagespace(c_valuespace)
            Point2D c_bitmapspace = flip(c_imagespace)
            int x = c_bitmapspace.x
            int y = c_bitmapspace.y
            p_bitmapspace.addPoint(x,y)
        }
        return p_bitmapspace
    }

    /**
     * Draw background to "img", returning a new image
     */

    private BufferedImage drawBackgroundWithRegions(BufferedImage img, boolean blur) {
        Graphics2D g2
        try {
            g2 = img.createGraphics()
            clipToGraphArea(g2)
            Level.values().each { def l ->
                Characteristic.values().each { def c ->
                    if (l == Level.Niveau0 && c != Characteristic.Good) {
                        // For Niveau#0, there is only "Good" Characteristic, so skip
                    }
                    else {                    
                        RateRegion rr = RateRegion.obtain(l, c)
                        g2.setColor(rr.color)
                        g2.fill(generateBitmapspacePolygon(rr))
                    }
                }
            }
        }
        finally {
            g2?.dispose()
        }
        if (blur) {
            // these operations are very slow
            img = Convolve.getGaussianBlurFilter(absoluteConvolutionRadius as Integer, true).filter(img, null)
            img = Convolve.getGaussianBlurFilter(absoluteConvolutionRadius as Integer, false).filter(img, null)
        }
        return img
    }

    /**
     * x = 1 means "as large as Symbol"
     */

    private static int fontSizeInPoints(float x) {
        assert x > 0
        return absoluteSymbolRadius * x as Integer
    }

    private Shape findShapeInRateDataHints(RateData rd) {
        def logger = LOGGER_findShapeInRateDataHints
        Shape   shape
        rd.hints.each {
            if (it instanceof Shape) {
                if (shape) {
                    logger.warn("RateData '${rd.desc}' contains multiple 'shape' hints")
                }
                else {
                    shape = it
                }

            }
        }
        if (!shape) {
            logger.warn("RateData '${rd.desc}' contains no 'shape' hints -- using CROSS")
            shape = Shape.CROSS
        }
        return shape
    }

    private Color findColorInRateDataHints(RateData rd) {
        def logger = LOGGER_findColorInRateDataHints
        Color color
        rd.hints.each {
            if (it instanceof Color) {
                if (color) {
                    logger.warn("RateData '${rd.desc}' contains multiple 'color' hints")
                }
                else {
                    color = it
                }

            }
        }
        if (!color) {
            logger.warn("RateData '${rd.desc}' contains no 'color' hints -- using WHITE")
            color = Color.WHITE
        }
        return color
    }

    private List<TextData> drawSymbolsInGraph(BufferedImage img, Collection<RateData> coll) {
        Graphics2D g2
        def tdList = [] // text data is accumulated here
        try {
            g2 = img.createGraphics()
            SymbolDrawing sym = new SymbolDrawing(this, g2) // a symbol-drawing facility
            coll.each { RateData rd ->
                if (rd.down_mbps && rd.up_mbps) {
                    Point2D valueSpacePos = new Point2D.Double(rd.down_mbps * 1000,rd.up_mbps * 1000) //  (DOWN KBPS,UP KBPS)
                    Point2D imgSpacePos   = mapToImagespace(valueSpacePos)
                    Color   color         = findColorInRateDataHints(rd)
                    Shape   shape         = findShapeInRateDataHints(rd)
                    switch (shape) {
                        case Shape.SQUARE: sym.drawSquare(imgSpacePos, color); break
                        case Shape.CIRCLE: sym.drawCircle(imgSpacePos, color); break
                        case Shape.DELTA:  sym.drawDelta(imgSpacePos, color); break
                        case Shape.NABLA:  sym.drawNabla(imgSpacePos, color); break
                        case Shape.CROSS:  sym.drawCross(imgSpacePos, color); break
                        default:
                            instaFail("Cannot draw shape '${shape}'")
                    }
                    TEXT: {
                        String up_txt   = GridLabel.textSpeed((rd.up_mbps * 1000) as Long)
                        String down_txt = GridLabel.textSpeed((rd.down_mbps * 1000) as Long)
                        String txt      = "${rd.desc} (Down: ${down_txt}, Up: ${up_txt})"
                        tdList << new TextData(imgSpacePos, txt, color, descFont, this)
                    }
                }
            }
        }
        finally {
            g2?.dispose()
        }
        return tdList
    }

    private void drawTextAlongRightSideOfGraph(BufferedImage img, List<TextData> tdList) {
        Graphics2D g2
        try {
            g2 = img.createGraphics()
            // Sort the text by position so that line interesections are not too numerous
            List<TextData> tdListNew = arrangeText(tdList, g2)
            // Finally draw text and link lines
            g2.setFont(descFont)
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
            tdListNew.each { TextData td ->
                def withContour
                td.drawText(g2, withContour = false)
                td.drawLinkLine(g2)
            }
        }
        finally {
            g2?.dispose()
        }
    }

    private List<TextData> arrangeText(List<TextData> tdList, Graphics2D g2) {
        def mutate
        //
        // Sort text by increasing y coordinate first, and by decreasing x second
        // This avoids overlapping "explanatory text lines" later
        //
        tdList.sort(mutate = true) { TextData a, TextData b ->
            double dx = a.dotpos_imagespace.x - b.dotpos_imagespace.x
            double dy = a.dotpos_imagespace.y - b.dotpos_imagespace.y
            if (Math.abs(dy) < 1) {
                // considered the same
                if (Math.abs(dx) < 1) {
                    // considered the same
                    return 0
                }
                else {
                    return -dx
                }
            }
            else {
                return dy
            }
        }
        //
        // Something simple:
        // Arrange text by moving it into position, piece by piece ("stacking on top")
        // Making sure that there is no overlap
        //
        double relativeTextSeparation = 1
        List<TextData> res = []
        tdList.each { TextData cur ->
            if (res.isEmpty()) {
                // first entry - keep text at its current position
                res << cur
            }
            else {
                // check that the current text does not overlap the previous; if it does, move it
                TextData prev = res[-1]
                Rectangle2D prevRect = prev.getContourInImagespace(g2)
                Rectangle2D curRect  = cur.getContourInImagespace(g2)
                double lowestYAllowed = prevRect.y + prevRect.height * (1 + relativeTextSeparation)
                if (curRect.y < lowestYAllowed) {
                    double delta = lowestYAllowed - curRect.y
                    cur.textpos_imagespace = new Point2D.Double(cur.textpos_imagespace.x, cur.textpos_imagespace.y + delta)
                }
                res << cur
            }
        }
        return res
    }

    /**
     * Helper to transform a Line2D in our space into the Java2D image space by flipping the y axis
     */

    private static Line2D flip(Line2D l) {
        return new Line2D.Double(l.x1, (imageHeight - 1) - l.y1, l.x2, (imageHeight - 1) - l.y2)
    }

    /**
     * Helper to transform a Line2D in our space into the Java2D image space by flipping the y axis
     */

    private static Point2D flip(Point2D p) {
        return new Point2D.Double(p.x, (imageHeight - 1) -p.y)
    }

    /**
     * Helper to transform a Rectangle2D in our space into the Java2D image space by flipping the y axis
     */

    private static Rectangle2D flip(Rectangle2D r) {
        return new Rectangle2D.Double(r.x, (imageHeight - 1) -(r.y + r.height), r.width, r.height)
    }

    /**
     * Helper to compute a "stretching" tuple able to linearly map loglog values into graph positions
     */

    private static Point2D computeLog10Stretch() {
        Point2D graphSize    = GeometryHelpers.delta(axisLowerLeft,axisUpperRight)
        Point2D valueSize    = GeometryHelpers.delta(loglogMin,loglogMax)
        return new Point2D.Double(graphSize.x/valueSize.x,graphSize.y/valueSize.y)
    }

    /**
     * Helper; given a value in (kbps_down, kbps_up) map it to the absolute point in the (x,y) graph
     */

    static Point2D mapToImagespace(Point2D p_valuespace, boolean noisy = false) {
        // "p_valuespace" exists in the "value space" bounded by the box [rangeMin_kbps, rangeMax_kbps]
        // First map "p_valuespace" into the loglog space; the visible part of that space is bounded by the box [loglogMin, loglogMax]
        Point2D p_loglog = GeometryHelpers.log10(p_valuespace)
        // Shift the point so that (0,0) corresponds to loglogMin
        Point2D p_loglog_shifted = GeometryHelpers.delta(loglogMin, p_loglog)
        // Linearly stretch the point into the image space
        Point2D p_stretched_into_graph = GeometryHelpers.dot(p_loglog_shifted, log10Stretch)
        // Adjust offest and return
        Point2D p_imagespace = GeometryHelpers.add(p_stretched_into_graph, axisLowerLeft)
        if (noisy) {
            System.out << "p_valuespace                : " << p_valuespace << "\n"
            System.out << " --> p_loglog               : " << p_loglog << "\n"
            System.out << " --> p_loglog_shifted       : " << p_loglog_shifted << "\n"
            System.out << " --> p_stretched_into_graph : " << p_stretched_into_graph << "\n"
            System.out << " --> p_imagespace           : " << p_imagespace << "\n"
        }
        return p_imagespace
    }

    /**
     * Write out available writers (png, jpg etc...)
     */

    static void writeOutAvailableWriterFormats() {
        ImageIO.getWriterFormatNames().each {
            System.out << it << "\n"
        }
    }

    private void setStrokeForProfileLine(Graphics2D  g2, Color color) {
        g2.setStroke(new BasicStroke(absoluteSymbolLineWidth as Float, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))        
        g2.setColor(color)
    }
     
    private List<TextData> drawVdsl2ProfileLines(BufferedImage img, Color color) {
        Graphics2D  g2
        def tdList = []
        try {            
            g2 = img.createGraphics()
            setStrokeForProfileLine(g2, color)
            Vdsl2Plot.Profile.values().each { Profile p ->
                double step_kbps = 100
                List<Point2D> points_valuespace = Vdsl2Plot.getValuespacePoints(p, step_kbps)
                assert points_valuespace.size()>=2
                Path2D path = new Path2D.Double()
                FIRST: {
                    // this is the highest downstream, also use it as legend
                    Point2D first_valuespace  = points_valuespace[0]
                    Point2D first_imagespace  = mapToImagespace(first_valuespace)
                    Point2D first_bitmapspace = flip(first_imagespace)
                    tdList << new TextData(first_imagespace, "VDSL2, ${p.p_text}, (${p.rate_text})", color, descFont, this)
                    path.moveTo(first_bitmapspace.x, first_bitmapspace.y)
                    points_valuespace.remove(0)
                }
                points_valuespace.each { Point2D point ->
                    Point2D next_bitmapspace = flip(mapToImagespace(point))
                    path.lineTo(next_bitmapspace.x, next_bitmapspace.y)
                }                
                g2.draw(path)
            }
        }
        finally {
            g2?.dispose()
        }
        return tdList
    }

}
