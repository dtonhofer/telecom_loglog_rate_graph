package eu.qleap.smc_uhd.rateplot.plot;

import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

import com.mplify.checkers.Check;

public class Convolve {

    /**
     * From http://www.java2s.com/Code/Java/Advanced-Graphics/GaussianBlurDemo.htm
     * See http://docs.oracle.com/javase/7/docs/api/java/awt/image/ConvolveOp.html
     */

    private static ConvolveOp getGaussianBlurFilter(int radius, boolean horizontal) {
        Check.isTrue(radius >= 1, "Radius must be >= 1");
        int size = radius * 2 + 1;
        float[] data = new float[size];
        float sigma = radius / 3.0f;
        float twoSigmaSquare = 2.0f * sigma * sigma;
        float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
        float total = 0.0f;
        for (int i = -radius; i <= radius; i++) {
            float distance = i * i;
            int index = i + radius;
            data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
            total += data[index];
        }
        for (int i = 0; i < data.length; i++) {
            data[i] /= total;
        }
        Kernel kernel = null;
        if (horizontal) {
            kernel = new Kernel(size, 1, data);
        } else {
            kernel = new Kernel(1, size, data);
        }
        return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
    }
    
}
