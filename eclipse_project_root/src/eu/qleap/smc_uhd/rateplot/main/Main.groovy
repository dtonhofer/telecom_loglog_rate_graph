package eu.qleap.smc_uhd.rateplot.main;

import java.awt.image.BufferedImage;

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Image
import java.awt.image.BufferedImage
import java.awt.image.RenderedImage

import javax.imageio.ImageIO
import javax.swing.JFrame

import eu.qleap.smc_uhd.rateplot.desc.Operator
import eu.qleap.smc_uhd.rateplot.desc.Origin
import eu.qleap.smc_uhd.rateplot.desc.RateData
import eu.qleap.smc_uhd.rateplot.plot.Graphiste
import eu.qleap.smc_uhd.rateplot.plot.Shape

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 *******************************************************************************
 * 1) Define "RateData"
 * 2) Select the "RateData" one is interested in
 * 3) Generate the image of the loglog plot
 * 4) Display the image in a window (the image is also written to disk)
 *
 * 2013.10.30 - Created
 ******************************************************************************/


class Main {

    /**
     * What shall be displayed?
     */

    enum Selection {
        POST, CABLEOPERATORS
    }

    /**
     * This is run when Swing comes up
     */

    private static void createAndShowGUI(BufferedImage img) {
        JFrame frame = new JFrame("Display")
        //
        // Set the canvas and the listeners
        //
        TransformingCanvas canvas = new TransformingCanvas(img, 0.3)
        def translateMousor = new TranslateMousor(canvas)
        def scaleMousor     = new ScaleMousor(canvas)
        canvas.addMouseListener(translateMousor)
        canvas.addMouseMotionListener(translateMousor)
        canvas.addMouseWheelListener(scaleMousor)
        //
        // configure frame
        //
        frame.setLayout(new BorderLayout());
        frame.getContentPane().add(canvas, BorderLayout.CENTER)
        frame.setSize(800, 800)
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        // frame.pack()
        frame.setVisible(true)
        /*
         JLabel label = new JLabel(new ImageIcon(img));
         frame.getContentPane().add(label);
         frame.pack();
         frame.setVisible(true);
         */
    }

    /**
     * Build ensemble of "RateData" elements to be displayed
     */

    private static List buildForPost() {
        List res = []
        final withVdsl = false
        //
        // Select
        //
        res += RateData.defineRateData_LuxDSL()
        res += RateData.defineRateData_LuxFibre()
        res += RateData.defineRateData_DSL(withVdsl)
        //
        // Give drawing hints
        //
        res.each { RateData it ->
            if (it.desc.contains("SpeedSurf")) {
                it.hints << Shape.DELTA   << new Color(0xfd9814)
            }
            if (it.origin == Origin.TECHNICAL) {
                it.hints << Shape.CIRCLE  << new Color(0x593eff)
            }
            if (it.desc.contains("LuxFibre S")) {
                it.hints << Shape.SQUARE << new Color(0xf13111)
            }
            else if (it.desc.contains("LuxFibre")) {
                it.hints << Shape.NABLA << new Color(0xf13111)
            }
        }
        return res
    }

    /**
     * Build ensemble of "RateData" elements to be displayed
     */

    private static List buildForCableOperators() {
        List res = []
        final withPro = false
        //
        // Select
        //
        res += RateData.defineRateData_EuroDOCSIS()
        res += RateData.defineRateData_Eltrona()
        res += RateData.defineRateData_Eschspeed(false)
        res += RateData.defineRateData_LOLCable()
        res += RateData.defineRateData_Numericable()
        //
        // Give drawing hints
        //
        res.each { RateData it ->
            switch (it.operator) {
                case Operator.VILLE_ESCH:        it.hints << Shape.DELTA  << new Color(0x189b1e) ; break; // green
                case Operator.NUMERICABLE:       it.hints << Shape.NABLA  << new Color(0xfd9814) ; break; // orange
                case Operator.LUXEMBOURG_ONLINE: it.hints << Shape.SQUARE << new Color(0xf13111) ; break; // reddish
                case Operator.ELTRONA:           it.hints << Shape.CIRCLE << new Color(0xbc3eff) ; break; // magenta
                default:                         it.hints << Shape.CROSS  << Color.YELLOW ; break; // yellow for DOCSIS 
            }
        }
        return res
    }

    /**
     * http://docs.oracle.com/javase/tutorial/2d/images/saveimage.html
     */

    static void saveImage(Image img, String format, File file) {
        assert img
        assert file
        assert format
        RenderedImage rimg = img
        ImageIO.write(rimg, format, file)
    }

    /**
     * Main
     */

    static private final blurBackground = true

    static main(def argv) {
        [
            Selection.POST,
            Selection.CABLEOPERATORS
        ].each { Selection selection ->
            List rdList = []
            boolean drawVdsl2Limits
            if (selection == Selection.POST) {
                rdList = buildForPost()
                drawVdsl2Limits = true
            }
            if (selection == Selection.CABLEOPERATORS) {
                rdList = buildForCableOperators()
                drawVdsl2Limits = false
            }
            //
            // Generate
            //            
            BufferedImage img = (new Graphiste()).draw(rdList, blurBackground, drawVdsl2Limits)
            //
            // Build interface displaying image
            //
            javax.swing.SwingUtilities.invokeLater( { createAndShowGUI(img) } as Runnable )
            //
            // Dump the image to an appropriate file in PNG format
            //
            final format = "png"
            saveImage(img, format, new File("/home/hobbes/graph.${selection}.${format}"))
        }
    }

}
