package eu.qleap.smc_uhd.rateplot.main;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

/* 34567890123456789012345678901234567890123456789012345678901234567890123456789
 * *****************************************************************************
 * Copyright (c) 2013, Q-LEAP S.A. 
 *                     14 rue Aldringen
 *                     L-1118 Luxembourg
 *
 * Distributed under the MIT License (http://opensource.org/licenses/MIT)
 *******************************************************************************
 *******************************************************************************
 * A simple Mouse wheel listener 
 * 
 * 2013.10.30 - Created
 ******************************************************************************/

public class ScaleMousor implements MouseWheelListener {

    final TransformingCanvas canvas;
    
    public ScaleMousor(TransformingCanvas canvas) {
        this.canvas = canvas;
    }
    
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            // make it a reasonable amount of zoom
            // .1 gives a nice slow transition
            canvas.scale += (.1 * e.getWheelRotation());
            // don't cross negative threshold.
            // also, setting scale to 0 has bad effects
            canvas.scale = Math.max(0.00001, canvas.scale);
            canvas.repaint();
        }
    }
}
