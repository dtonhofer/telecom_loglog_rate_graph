package eu.qleap.smc_uhd.rateplot.main;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 *******************************************************************************
 * A simple Swing handler for mouse events
 * 
 * 2013.10.30 - Created
 ******************************************************************************/

public class TranslateMousor implements MouseListener, MouseMotionListener {
    
    private int lastOffsetX;
    private int lastOffsetY;

    private final TransformingCanvas canvas;
    
    public TranslateMousor(TransformingCanvas canvas) {
        this.canvas = canvas;
    }
    
    public void mousePressed(MouseEvent e) {
        // capture starting point
        lastOffsetX = e.getX();
        lastOffsetY = e.getY();
    }

    public void mouseDragged(MouseEvent e) {

        // new x and y are defined by current mouse location subtracted
        // by previously processed mouse location
        int newX = e.getX() - lastOffsetX;
        int newY = e.getY() - lastOffsetY;

        // increment last offset to last processed by drag event.
        lastOffsetX += newX;
        lastOffsetY += newY;

        // update the canvas locations
        canvas.translateX += newX;
        canvas.translateY += newY;

        // schedule a repaint.
        canvas.repaint();
    }

    public void mouseClicked(MouseEvent e) {
        // NOP
    }

    public void mouseEntered(MouseEvent e) {
        // NOP        
    }

    public void mouseExited(MouseEvent e) {
        // NOP        
    }

    public void mouseMoved(MouseEvent e) {
        // NOP        
    }

    public void mouseReleased(MouseEvent e) {
        // NOP        
    }
}
