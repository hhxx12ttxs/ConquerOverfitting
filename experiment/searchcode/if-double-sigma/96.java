/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package detect;

import Utility.Matrix;
import april.util.ParameterGUI;

/**
 *
 * @author jrpeterson
 * 
 * laplacian of gaussian nothing too fancy
 */

public class LofGFilter implements Filter{
    private double[][] filter;
    
    ParameterGUI pg;
    
    LofGFilter(double sigma) {
        int size = (int) sigma*6;
        if (size % 2 == 0) {
            size++; // needs to be odd or else
        }
        filter = Matrix.LofG2D(sigma, size); // is this far enough out for a good approximation? 2 sigma in both directions?
        //Matrix.print(filter);
        
        pg = new ParameterGUI();
	pg.addDoubleSlider("slider", "Sigma", 0.1, 20, sigma);
    }
    
   

    @Override
    public int getHeight() {
        return filter.length;
    }
    
    @Override
    public int getWidth() {
        return filter[0].length;
    }

    @Override
    public ParameterGUI getParameterGUI() {
        return pg;
    }

    @Override
    public double measureResponse(double[][] window) {
        assert ((window.length == filter.length) && (window[0].length == filter[0].length)) : "Warning: incompatible window!";
        return Matrix.resolve(window, filter);
    }

    @Override
    // argh laplacian of gaussian is actually pretty wierd can take on both positive maxima
    // and negative minima that we care about
    public int getMode() {
        return Constants.MODEBOTH;
    }
    
    @Override
    public int getMagMode() {
        return Constants.MAGMODEHIGH;
    }

    @Override
    public double measureResponse(double[][] img, int startrow, int startcol, int level) {
        return Matrix.resolveSub(img, startrow, startcol,filter);
    }

    @Override
    public void initialize(ImagePyramid imgP) {
        // nothing to do
    }
    
    @Override
    public double[][] getFilter() {
        return filter;
    }
    
    @Override
    public double[] getThresholds() {
        return new double[] {Constants.MINRESPONSETHRESHOLD_LofG, Constants.MAXRESPONSETHREOLD_LofG};
    }
    
}

