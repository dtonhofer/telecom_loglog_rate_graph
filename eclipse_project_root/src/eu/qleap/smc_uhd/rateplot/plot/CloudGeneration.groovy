package eu.qleap.smc_uhd.rateplot.plot

import java.awt.Color
import java.awt.Graphics2D
import java.awt.Point
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import java.awt.image.RenderedImage

import javax.imageio.ImageIO
import javax.naming.LimitExceededException;

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import name.heavycarbon.logstarter.LogbackStarter


/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 *******************************************************************************
 * Generation a "cloud of points" around an "ADSL point" using a simple and
 * naive model.
 * 
 * Spaces:
 * =======
 * 
 * "Valuespace"
 * ------------
 *  
 * The 2D space of (download rate, upload rate) in (kbit/s,kbit/s)
 * 
 * The area of valuespace that interests us is given by its "lower-left corner"
 * min_valuespace and its "upper-right corner" max_valuespace.
 * 
 * Points in this space are supposed to be "infinitesimally precise" (i.e. reals
 * in a continuous space). This is an idealization, as they are really quantized 
 * as doubles. Computation can be regarded as introducing noise.
 * 
 * "Imagespace"
 * ------------
 * 
 * The 2D space of the graph image, with x increasing from left to right and
 * y increasing from bottom to top. Coordinates in here are still computed in
 * Doubles (Point2D)
 * 
 * Only part of the imagespace is occupied by the actual graph. Other parts are
 * occupied by filler, legend etc.
 * 
 * There exists a mapping from "Valuespace" to "Bitmapspace" which demands 
 * translation and log-compression along both axis. The function
 * 
 * Graphiste.mapToImagespace(Point2D p_valuespace, boolean noisy = false) 
 * 
 * does that. Its result is a point in Imagespace, and one may want to check
 * whether it still lies in the area of the graph itself.
 *
 * Points in this space are supposed to be "infinitesimally precise" (i.e. reals
 * in a continuous space). This is an idealization, as they are really quantized 
 * as doubles. Computation can be regarded as introducing noise.
 * 
 * "Bitmapspace"
 * -------------
 * 
 * The 2D space of the positions in the bitmap, with x increasing from left to
 * right and y increasing from top to bottom. Coordinates in here are computed in
 * Integers (Point)
 * 
 * One may want to apply clipping operations to keep drawing operations inside
 * the graph area.
 * 
 * Points in this space are supposed to be "discrete tiles". Mapping from
 * "Imagespace" to "Bitmapspace" involves collapsing the area underneath a tile
 * into a single (x,y) position. 
 * 
 * "accumulator"
 * -------------
 * 
 * We quantized the "valuespace" along its x and y axis by quant_x and quant_y
 * respectively. This yields a 2D "array of int" which will accumulate our
 * results from the monte-carlo simulation.
 * 
 *  Mapping back from the accumulator to valuespace is done by mapping to the
 *  middle of the corresponding tile in valuespace.
 * 
 * Principle:
 * ==========
 * 
 * Suppose we have ADSL product "X" running at a given advertised speed
 * ("advertised_valuespace", the speed in valuespace), which is assumed to be
 * optimistic ("up to...")
 * 
 * 
 *
 * 2013.10.30 - Created
 ******************************************************************************/

class CloudGeneration {

    private final static String CLASS = CloudGeneration.class.name
    private final static Logger LOGGER_init = LoggerFactory.getLogger("${CLASS}.init")
    private final static Logger LOGGER_accumulate_valuespace = LoggerFactory.getLogger("${CLASS}.accumulate_valuespace")
    private final static Logger LOGGER_accumulate_imagespace = LoggerFactory.getLogger("${CLASS}.accumulate_imagespace")
    private final static Logger LOGGER_valuespaceToAccumulatorPosition = LoggerFactory.getLogger("${CLASS}.valuespaceToAccumulatorPosition")

    /*
     * The advertised speed of the product
     */

    private final Point2D advertised_valuespace = new Point2D.Double(30 * 1000,10 * 1000) // (30 mbit/s, 10 mbit/s) in kbit/s

    /*
     * Valuespace and its extension
     */

    private final Point2D min_valuespace = new Point2D.Double(1  * 1000,1  * 1000) // (1  mbit/s, 1  mbit/s) in kbit/s
    private final Point2D max_valuespace = new Point2D.Double(32 * 1000,32 * 1000) // (32 mbit/s, 32 mbit/s) in kbit/s
    private final double x_ext_valuespace = max_valuespace.x - min_valuespace.x
    private final double y_ext_valuespace = max_valuespace.y - min_valuespace.y

    /*
     * The imagespace boundaries corresponding to valuespace
     */

    private final Point2D min_imagespace = Graphiste.mapToImagespace(min_valuespace)
    private final Point2D max_imagespace = Graphiste.mapToImagespace(max_valuespace)

    /*
     * Extension of the image space
     */

    private final double x_ext_accumulator_imagespace = Math.ceil(max_imagespace.x - min_imagespace.x)
    private final double y_ext_accumulator_imagespace = Math.ceil(max_imagespace.y - min_imagespace.y)

    /*
     * The valuespace is quantized along its x and y axis, in "kilobit/s" 
     */

    private final double quant_x = 30
    private final double quant_y = 30

    /*
     * The quantized valuespace thus consists of "rectangular blocks in valuespace" 
     */

    private final int x_ext_accumulator_valuespace = Math.ceil(x_ext_valuespace / quant_x)
    private final int y_ext_accumulator_valuespace = Math.ceil(y_ext_valuespace / quant_y)

    /*
     * The "accumulator" with one integer per "tile of quantized valuespace":
     * Not sure whether this is an actual Java array in Groovy  
     */

    private final int[][] accumulator_valuespace = new int[x_ext_accumulator_valuespace][y_ext_accumulator_valuespace]

    /**
     * We also need an accumulator in imagespace.
     */

    private final int[][] accumulator_imagespace = new int[x_ext_accumulator_imagespace][y_ext_accumulator_imagespace]

    /*
     * How large is the simulation
     */

    private final static int MONTE_CARLO_ROUNDS = 10000
    private final static int RWALK_ROUNDS       = 500

    /*
     * How to obtains rands
     */

    private final Random rand = new Random()

    /**
     * Simple random walk from "current"
     */

    private final double scale      = 100
    private final double push_scale = 200

    /**
     * The line from (0,0) to (advertised_valuespace.x / advertised_valuespace.y) ...
     */

    private final double    advv_slope        = advertised_valuespace.y / advertised_valuespace.x
    private final Point2D   advv_orthoRaw     = new Point2D.Double(advertised_valuespace.y, -advertised_valuespace.x)
    private final double    advv_orthoLength  = advv_orthoRaw.distance(0,0)
    private final Point2D   advv_orthoUnitary = new Point2D.Double(advv_orthoRaw.x / advv_orthoLength, advv_orthoRaw.y / advv_orthoLength)
    private final double    advv_distMult     = Math.sqrt(1 + (advv_slope * advv_slope))

    /**
     * Calibrate: Value goes from 1 to 0 at "push_scale", then stays 0
     */

    private double calibrateStrength(double distance) {
        return Math.max(0,1.0 - (distance / push_scale))
    }

    private Point2D step(Point2D current) {
        assert advv_slope <= 1.0
        //
        // Loop until found
        //
        Point2D newPos
        while (newPos == null) {
            double newPosx = current.x
            double newPosy = current.y
            //
            // jump around into any direction -- always by "scale"
            //
            JUMP: {
                double angle = rand.nextDouble() * 3.14156 * 2
                newPosx += Math.cos(angle) * scale
                newPosy += Math.sin(angle) * scale
            }
            //
            // push away from the advertised values: maximum "x" (the max download speed)
            //
            PUSH_OFF_X: {
                double distance = advertised_valuespace.x - current.x
                assert distance >= 0
                double xStrength = calibrateStrength(distance)**2
                assert xStrength >= 0
                newPosx -= (xStrength * scale)
            }
            //
            // push away from the advertised values: the "advv" line (linking upload to download speed)
            //
            PUSH_OFF_ADVV: {
                double distanceFromAdvvLine = Math.abs( advv_slope * current.x - current.y ) / advv_distMult
                double advvStrength = calibrateStrength(distanceFromAdvvLine)**2
                newPosx += advv_orthoUnitary.x * advvStrength * scale
                newPosy += advv_orthoUnitary.y * advvStrength * scale
            }
            //
            // check the new position
            //
            newPos = new Point2D.Double(newPosx, newPosy)
            if (newPos.x > advertised_valuespace.x || newPos.y > advertised_valuespace.y || newPos.x <= 0 || newPos.y <= 0 || newPos.y > advv_slope * newPos.x) {
                // try again
                newPos = null
            }
        }
        return newPos
    }


    private Point2D walkRandomlyNew(Point2D bestValueForEndpoint) {
        Point2D cur = new Point2D.Double(bestValueForEndpoint.x,bestValueForEndpoint.y)
        double limitingSlope = cur.y / cur.x
        for (int i=0 ; i < RWALK_ROUNDS ; i++) {
            cur = step(cur)
        }
        return cur
    }

    /**
     * Transform. Returns null if out of range
     */

    private Point valuespaceToAccumulatorPosition(Point2D p_valuespace) {
        def logger = LOGGER_valuespaceToAccumulatorPosition
        int x = Math.floor((p_valuespace.x - min_valuespace.x) / quant_x)
        int y = Math.floor((p_valuespace.y - min_valuespace.y) / quant_y)
        if (x < 0 || x_ext_accumulator_valuespace <= x || y < 0 || y_ext_accumulator_valuespace <= y) {
            logger.warn("Valuespace point ${p_valuespace} discarded as it is out of range of accumulator: (${x},${y}) not in (0,0)-(${x_ext_accumulator_valuespace},${y_ext_accumulator_valuespace})")
            return null
        }
        else {
            return new Point(x,y)
        }
    }

    /**
     * Accumulate
     */

    private void accumulate_valuespace(Point2D p_valuespace,int l) {
        Point p_acc = valuespaceToAccumulatorPosition(p_valuespace)
        if (p_acc) {
            int x = p_acc.x
            int y = p_acc.y
            accumulator_valuespace[x][y] += l
        }
    }

    /**
     * Accumulate
     */

    private void accumulate_imagespace(Point2D p_imagespace,int l) {
        def logger = LOGGER_accumulate_imagespace
        int x = Math.floor(p_imagespace.x - min_imagespace.x)
        int y = Math.floor(p_imagespace.y - min_imagespace.y)
        if (x < 0 || x_ext_accumulator_imagespace <= x || y < 0 || y_ext_accumulator_imagespace <= y) {
            logger.warn("Imagespace point ${p_imagespace} discarded as it is out of range of accumulator: (${x},${y}) not in (0,0)-(${x_ext_accumulator_imagespace},${y_ext_accumulator_imagespace})")
        }
        else {
            accumulator_imagespace[x][y]+=l
        }
    }

    /**
     * Traverse the valuespace accumulator and re-accumulate in imagespace; returns the total 
     */

    private int accumulateIntoImagespace() {
        double x_value = min_valuespace.x
        double quant_x_half = quant_x / 2.0
        double quant_y_half = quant_y / 2.0
        int total = 0
        for (int i=0; i < x_ext_accumulator_valuespace; i++) {
            double y_value = min_valuespace.y
            for (int j=0; j < y_ext_accumulator_valuespace; j++) {
                Point2D tileMiddle_valuespace = new Point2D.Double(x_value + quant_x_half,y_value + quant_y_half)
                Point2D p_imagespace = Graphiste.mapToImagespace(tileMiddle_valuespace)
                int l = accumulator_valuespace[i][j]
                accumulate_imagespace(p_imagespace,l)
                y_value += quant_y
                total += l
            }
            x_value += quant_x
        }
        return total
    }

    /**
     * Render the image space accumulator to a bitmap
     */

    private BufferedImage renderAccumulatorOfImagespace() {
        //
        // Create color image, keep as is with alpha value
        //
        BufferedImage img = new BufferedImage(x_ext_accumulator_imagespace as Integer, y_ext_accumulator_imagespace as Integer, BufferedImage.TYPE_INT_ARGB)
        Graphics2D    gfx = img.createGraphics()
        //
        // Find max value over accumulator so that we can scale poperly
        //
        double maxValue = 0
        for (int i=0; i < x_ext_accumulator_imagespace; i++) {
            for (int j=0; j < y_ext_accumulator_imagespace; j++) {
                maxValue = Math.max(maxValue, accumulator_imagespace[i][j] )
            }
        }
        //
        // Generate points in bitmap
        //
        int y_ext_accumulator_imagespace_int = y_ext_accumulator_imagespace
        double maxValue_d = maxValue
        for (int i=0; i < x_ext_accumulator_imagespace; i++) {
            for (int j=0; j < y_ext_accumulator_imagespace; j++) {
                double v = Math.sqrt(accumulator_imagespace[i][j] / maxValue_d)
                assert v >= 0 && v <= 1.0
                int g = Math.floor(v * 255)
                if (g > 0) {
                    int alpha = 0xFF
                    int col = alpha
                    col = col << 8 | g
                    col = col << 8 | g
                    col = col << 8 | g
                    img.setRGB(i, y_ext_accumulator_imagespace_int-1-j, col)
                }
            }
        }
        return img
    }

    /**
     * Render the value space accumulator to a bitmap
     */

    private BufferedImage renderAccumulatorOfValuespace(Point2D start) {
        //
        // Create grayscale image, fill with black
        //
        BufferedImage img = new BufferedImage(x_ext_accumulator_valuespace as Integer, y_ext_accumulator_valuespace as Integer, BufferedImage.TYPE_BYTE_GRAY)
        Graphics2D    gfx = img.createGraphics()
        gfx.setPaint ( Color.BLACK )
        gfx.fillRect ( 0, 0, img.width, img.height )
        //
        // Find max value over accumulator so that we can scale poperly
        //
        int maxValue = 0
        for (int i=0; i < x_ext_accumulator_valuespace; i++) {
            for (int j=0; j < y_ext_accumulator_valuespace; j++) {
                maxValue = Math.max(maxValue, accumulator_valuespace[i][j] )
            }
        }
        //
        // Generate points in bitmap; use sqrt to "lift" it a bit
        //
        int y_ext_accumulator_valuespace_int = y_ext_accumulator_valuespace
        double maxValue_d = maxValue
        for (int i=0; i < x_ext_accumulator_valuespace; i++) {
            for (int j=0; j < y_ext_accumulator_valuespace; j++) {
                double v = Math.sqrt(accumulator_valuespace[i][j] / maxValue_d)
                assert v >= 0 && v <= 1.0
                int r = Math.floor(v * 255)
                int g = r
                int b = r
                int col = (r << 16) | (g << 8) | b
                img.setRGB(i, y_ext_accumulator_valuespace_int-1-j, col)
            }
        }
        //
        // Additionally, plant a fat point at position "start"
        //
        gfx.setColor(Color.WHITE)
        Point start_acc = valuespaceToAccumulatorPosition(start)
        int radius = 5
        if (start_acc) {
            gfx.fillOval(start_acc.x-radius as Integer, y_ext_accumulator_valuespace_int - start_acc.y -5 as Integer,2*radius,2*radius)
        }
        return img
    }

    /**
     * http://docs.oracle.com/javase/tutorial/2d/images/saveimage.html
     */

    void saveImage(BufferedImage img,String txt) {
        RenderedImage rimg = img
        String format = "png"
        File file = new File("/home/hobbes/graph.${txt}.${format}")
        ImageIO.write(rimg, format, file)
    }

    /**
     * Constructor runs it!
     */

    CloudGeneration() {
        def logger = LOGGER_init
        assert x_ext_valuespace > 0
        assert y_ext_valuespace > 0
        logger.info("Valuespace accumulator size: ${x_ext_accumulator_valuespace} x ${y_ext_accumulator_valuespace}")
        logger.info("Imagespace accumulator size: ${x_ext_accumulator_imagespace} x ${y_ext_accumulator_imagespace}")
        //
        // Run Monte Carlo
        //
        for (int i=0; i<MONTE_CARLO_ROUNDS ;i++) {
            //
            // Suppose we have a endpoint (modem) that manages the speed AS ADVERTISED (optimistic)
            // For this endpoint, the *actual speed* attained is given by the result of a random walk:
            //
            Point2D finalValueForEndpoint = walkRandomlyNew(advertised_valuespace)
            accumulate_valuespace(finalValueForEndpoint,1)
        }
        //
        // For verification, render the valuespace to disk
        //
        saveImage(renderAccumulatorOfValuespace(advertised_valuespace),"valuespace")
        //
        // Accumulate into the loglog imagespace
        //
        accumulateIntoImagespace()
        //
        // For verification, render the imagespace to disk
        //
        saveImage(renderAccumulatorOfImagespace(),"imagespace")
    }


    static void main(String[] argv) {
        new LogbackStarter()
        new CloudGeneration()
    }
}
