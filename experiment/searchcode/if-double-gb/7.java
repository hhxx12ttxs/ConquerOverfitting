/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package detect;

import Utility.Line;
import Utility.LineFit;
import Utility.Matrix;
import Utility.Suppression;
import Utility.ImageProc;
import java.util.ArrayList;
import java.awt.*;
import java.awt.image.*;

import april.vis.*;
import april.jmat.*;
import april.util.*;

/**
 *
 * @author jrpeterson ajbonkoski
 * 
 * detects the shore in the image as a strong gradient in the vertical direction, 
 * with the eventual goal of  not just filtering detection above the shore line but also simply not processing the area above the shore
 */
public class ShoreDetector implements Detector {
    
    private double[][] Gy;
    //private static int DOWNSAMPLERATE = 20;

    
    private Line Shore = null;
    private int height;
    private int downsample_height;
    private int downsample_width;
    private ParameterGUI pg = new ParameterGUI();

    public ShoreDetector(){
	pg.addIntSlider("downsample", "Downsample", 1, 20, 8);
	pg.addDoubleSlider("response_scale", "Response Scaling", 1.0, 1000.0, 100);
	pg.addDoubleSlider("suppression_thresh", "Suppression Thresh", 1.0, 1000.0, 500.0);
	pg.addCheckBoxes("show_downsample", "Show Downsample", false);
	pg.addCheckBoxes("filter_response", "Show Response", false);
	pg.addCheckBoxes("filter_suppression", "Show Suppression", false);
	pg.addCheckBoxes("filter_cols", "Show Columns", false);
	pg.addCheckBoxes("show_ransac", "Show Ransac Points", true);
	pg.addCheckBoxes("show_line", "Show Line", true);
	pg.addDoubleSlider("min_thresh", "Min Sobel Thresh", 0.0, 256.0, 0.0);
	pg.addDoubleSlider("max_thresh", "Max Sobel Thresh", 0.0, 256.0, 256.0);
    }

    // takes in the original image and detects the shore
    public BufferedImage detect(double[][] Oimg) {
	BufferedImage ret = null;
	height = Oimg.length;

        //double[][] Ky = new double[][] {{-1, 0, 1},{-2, 0, 2},{-1, 0, 1}};
	double[][] Ky = new double[][] {{-1, -2, -1},{0, 0, 0},{1, 2, 1}};

        // downsample the crud out of the original image

	int down = pg.gi("downsample");
	int new_height = Oimg.length/down;
	int new_width = Oimg[0].length/down;
	downsample_height = new_height;
	downsample_width = new_width;

        double[][] Simg = Matrix.scale(Oimg, new_width, new_height);
	//System.out.println("after downsample");

        //double[][] Simg = Oimg;
	if(pg.gb("show_downsample"))
	    ret = ImageProc.greytoImg(Simg);

        // obtain the gradient in the y direction
        Gy = Matrix.convolveC(Simg, Ky);

	double min_thresh = pg.gd("min_thresh");
	double max_thresh = pg.gd("max_thresh");
	for(int y = 0; y < Gy.length; ++y){
	    for(int x = 0; x < Gy[0].length; ++x){
		int val = ((int)Gy[y][x])&0xff;
		if(val < min_thresh || val > max_thresh)
		    Gy[y][x] = 0.0;
	    }
	}

	if(pg.gb("filter_response"))
	    ret = ImageProc.greytoImg(Gy, pg.gd("response_scale"));

        // nonmaximum suppression
        ArrayList<int[]> peaksI = Suppression.nonMaxima(Gy, 1, pg.gd("suppression_thresh"));

        // build suppression map for visualization
	double[][] suppressionMap = new double[new_height][new_width];
	ArrayList<double[]> peaksD = new ArrayList<double[]>(peaksI.size());
	for (int[] p : peaksI) {
	    suppressionMap[p[0]][p[1]] = 255.0;
	    peaksD.add(new double[]{p[0]*down, p[1]*down});
        }
	if(pg.gb("filter_suppression"))
	    ret = ImageProc.greytoImg(suppressionMap);

	// column based filtering
	ArrayList<double[]> colPeaks = filterByColumn(peaksI, new_width, down);
        // build column map for visualization
	double[][] columnMap = new double[new_height][new_width];
	for (double[] p : colPeaks) {
	    columnMap[(int)p[0]/down][(int)p[1]/down] = 255.0;
        }
	if(pg.gb("filter_cols"))
	    ret = ImageProc.greytoImg(columnMap);
        
	
        // fit a single line to the points
        Shore = LineFit.RANSAC(peaksD);
	
	return ret;
    }

    ArrayList<double[]> filterByColumn(ArrayList<int[]> peaks, int width, int scale){

	// initialize best to -1
	double[] best = new double[width];
	for(int i = 0; i < width; ++i)
	    best[i] = -1;
	
	for(int[] peak : peaks){
	    int x = peak[1];
	    int y = peak[0];
	    if(x >= width) continue;

	    // check if smallest and expand point
	    isMoreMax(best, x-2, (double)y);
	    isMoreMax(best, x-1, (double)y);
	    isMoreMax(best, x, (double)y);
	    isMoreMax(best, x+1, (double)y);
	    isMoreMax(best, x+2, (double)y);
	}

	ArrayList<double[]> ret = new ArrayList<double[]>();
	for(int i = 0; i < width; ++i)
	    if(best[i] != -1)
		ret.add(new double[]{best[i]*scale, (double)i*scale});

	return ret;
    }

    void isMoreMax(double[] best, int x, double y){
	if(x < 0 || x >= best.length)
	    return;
	
	if(y > best[x] || best[x] == -1)
	    best[x] = y;
    }

    public VisObject getVis(){
	if(Shore == null)
	    return null;
	
	double[][] rot_offset = LinAlg.rotateZ(-Math.PI/2.0);

	VisChain chain = new VisChain();

	if(pg.gb("show_line")){
	    double [] c = Shore.getCentroid();
	    VzRectangle vzline =  new VzRectangle(1000, 3.0, new VzMesh.Style(Color.YELLOW));
	    double[] dir = Shore.getDirection();
	    double theta = Math.atan2(dir[1],dir[0]);
	    // rotate about the y axis to lay it down along the x axis and then rotate about z
	    VisChain lineChain = new VisChain(LinAlg.translate(c[1], height - c[0], 0.0),
					      LinAlg.rotateZ(theta),
					      rot_offset,
					      vzline);
	    chain.add(lineChain);
	}


	if(pg.gb("show_ransac")){
	    ArrayList<double[]> ransac_pts = Shore.getPoints();
	    for(double[] pt : ransac_pts){
		chain.add(new VisChain(LinAlg.translate(pt[1], height - pt[0], 0.0),
				       rot_offset,
				       new VzCircle(2, new VzMesh.Style(Color.RED))));
	    }
	}

	return chain;

    }

    public ParameterGUI getParameterGUI(){
	return pg;
    }
    
    public Line getLine() {
        return Shore;
    }
    
    public double[][] getGrad() {
        return Gy;
    }
}

