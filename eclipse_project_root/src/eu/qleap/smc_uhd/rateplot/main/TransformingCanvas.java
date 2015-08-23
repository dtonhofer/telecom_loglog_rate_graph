package eu.qleap.smc_uhd.rateplot.main;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;

import javax.swing.JComponent;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 *******************************************************************************
 * A simple Canvas able to display an Image 
 * 
 * 2013.10.30 - Created
 ******************************************************************************/

@SuppressWarnings("serial")
class TransformingCanvas extends JComponent {

    double translateX;
    double translateY;
    double scale;

    final Image img;

    TransformingCanvas(Image img, double initialScale) {
        translateX = 0;
        translateY = 0;
        scale = initialScale;
        setOpaque(true);
        setDoubleBuffered(true);
        this.img = img;
    }

    @Override
    public void paint(Graphics g) {
        AffineTransform tx = new AffineTransform();
        tx.translate(translateX, translateY);
        tx.scale(scale, scale);
        Graphics2D gg = (Graphics2D) g;
        // http://docs.oracle.com/javase/7/docs/api/java/awt/Graphics.html#drawImage%28java.awt.Image,%20int,%20int,%20java.awt.Color,%20java.awt.image.ImageObserver%29
        gg.setColor(Color.WHITE);
        gg.fillRect(0, 0, getWidth(), getHeight());
        gg.setTransform(tx);
        gg.drawImage(img, 0, 0, Color.WHITE, null);
        // super.paint(g);
    }
}
