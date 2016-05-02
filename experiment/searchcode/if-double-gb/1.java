package detect;

// java libraries
import Utility.ImageProc;
import Utility.Matrix;
import Utility.StopWatch;
import Utility.Suppression;
import april.jmat.LinAlg;
import april.util.ParameterGUI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.util.*;

// april libraries
import april.util.*;
 
public class BuoyDetect implements ParameterListener {
    public final static boolean IS_DEBUG = false;

    DrawThread draw;
    ImageSource isrc;
    BufferedImage last_image = null;

    // String directory;
    // File[] imageFiles = null;
    // int currentFile = 0;

    // Detection stuff
    Filter filter;             // filter being used to produce the response pyramid
    Filter LoG_filter;         // Laplacian of Gaussian filter
    Filter MoG_filter;         // Magnitude of Gradiant filter
    ScaleFilter Sfilter;       // enforces geometric constriants on detections
    ShoreDetector shoreD;      // a detector for the shore
    Classifier Avgcolorclass;  // for a detection spits out a class label
    Classifier Histcolorclass;
    Classifier classifier;     // classifier used
    String near_dist_limit;
    String classify_dist;

    ParameterGUI pg;
    boolean do_recompute = false;
    Object lock = new Object();

    public static void main(String[] args){      
	if(args.length == 0){
	    System.err.println("Error: An argument for an imagesource must be provided!");
	    System.exit(-1);
	}
	new BuoyDetect(args).start();
    }

    public static void Output(String msg){
	if(IS_DEBUG){
	    System.out.println(msg);
	}
    }

    public BuoyDetect(String[] args){

        draw = new DrawThread("Buoy Detect");
	isrc = ImageSourceFactory.getSourceFromArgs(args);
	if(isrc == null || !isrc.start()){
	    System.err.println("Error: Could not acquire the image source!");
	    System.exit(-1);
	}

	// this.directory = directory;
	// imageFiles = new File(directory).listFiles();
	// if(imageFiles == null || imageFiles.length == 0){
	//     System.err.println("Error: A directory name must be provided!");
	//     System.exit(-1);
	// }
	
	    
	//imageFilename = args[0];

	pg = new ParameterGUI();
	pg.addButtons("save_region", "Save Region",
		      "clear_region", "Clear Region");
	pg.addDoubleSlider("hist_near_dist_limit", "Histogram Pre Near Detect Distance Limit", 0.0, 1.0, 0.36);
	pg.addDoubleSlider("hist_classify_distance", "Histogram Classification Distance Limit", 0.0, 1.0, 0.26);
	pg.addDoubleSlider("rgb_near_dist_limit", "Avg RGB Pre Near Detect Distance Limit", 0.0, 10.0, 7.2);
	pg.addDoubleSlider("rgb_classify_distance", "Avg RGB Classification Distance Limit", 0.0, 10.0, 2.7);

	pg.addCheckBoxes("shore_detect", "Use Shore Detect", false
			 );
	pg.addCheckBoxes("MoG", "Use Mag of Grad", false,
			 "LoG", "Use Laplacian of Gaussian", true
			 );
	pg.addCheckBoxes("grad_descent", "Use Gradient Descent", true,
			 "near_detect", "Use Near Detect", true
			 );
	pg.addCheckBoxes("scale_filter", "Use Scale Filter", true
			 );
	pg.addCheckBoxes("rgb_classify", "Use RGB Classifier", false,
			 "hist_classify", "Use Hist Classifier", true
			 );			 
	pg.addCheckBoxes("show_water", "Show Water", false,
			 "show_red", "Show Red", true,
			 "show_green", "Show Green", true,
			 "show_yellow", "Show Yellow", true,
			 "show_shore", "Show Shore", false
			 );
	draw.enableBuffer("water", pg.gb("show_water"));
	draw.enableBuffer("red", pg.gb("show_red"));
	draw.enableBuffer("green", pg.gb("show_green"));
	draw.enableBuffer("yellow", pg.gb("show_yellow"));
	draw.enableBuffer("shore", pg.gb("show_shore"));
	pg.addListener(this);

        // training
        StopWatch sw = new StopWatch();
        sw.start("Average Color classifier");
        Avgcolorclass = new RGBNearestNeighbor();
        //colorclass = new HSVNearestNeighbor();
        sw.stop();
        
        sw.start("Histogram Classifier");
        //Histcolorclass = new RGBNearestHistogram();
        Histcolorclass = new HSNearestHistogram();
        sw.stop();
        
        sw.start("Laplacian of Gaussian Filter");         
        LoG_filter = new LofGFilter(Constants.LOFGSIGMA);
        //filter = new EigenBuoyFilter();
	sw.stop();

        sw.start("Magnitude of Gradient Filter"); 
        MoG_filter = new MagofGradFilter();
        sw.stop();

        sw.start("Scale filter");
        Sfilter = new ScaleFilter();
        sw.stop();
        
	shoreD = new ShoreDetector();


	// Set the initial filter
	if(pg.gb("MoG")){
	    filter = MoG_filter;
	} 
	else if(pg.gb("LoG")){
	    filter = LoG_filter;
	}
	else {
	    System.err.println("Error: no filter was set!");
	    System.exit(-1);
	}

	// Set the initial classifier
	if(pg.gb("rgb_classify")){
	    classifier = Avgcolorclass;
	    near_dist_limit = "rgb_near_dist_limit";
	    classify_dist = "rgb_classify_distance";
	} 
	else if(pg.gb("hist_classify")){
	    classifier = Histcolorclass;
	    near_dist_limit = "hist_near_dist_limit";
	    classify_dist = "hist_classify_distance";
	}
	else {
	    System.err.println("Error: no filter was set!");
	    System.exit(-1);
	}

        System.out.println(sw.prettyPrint());
        
        // start up visualization
	draw.addParameterGUI(isrc.getParameterGUI());
	draw.addParameterGUI(pg);
	draw.addParameterGUI(Avgcolorclass.getParameterGUI());
	draw.addParameterGUI(Histcolorclass.getParameterGUI());
	draw.addParameterGUI(filter.getParameterGUI());
        draw.addParameterGUI(Sfilter.getParameterGUI());
	draw.start();
        
    }

    public void start(){

	while(true){

	    // // acquire and set image
	    // BufferedImage img = null;
	    // try {
	    // 	img = ImageIO.read(imageFiles[currentFile]);
	    // } catch(IOException ex) {
	    // 	img = null;
	    // }

	    // if(img == null){
	    // 	System.err.println("Error: Could not read a BufferedImage from file '"+imageFiles[currentFile].getName()+"'");

	    // 	// goto next image
	    // 	currentFile++;
	    // 	currentFile %= imageFiles.length;
		
	    // 	continue;
	    // }

	    double frame_rate = isrc.getFrameRate();
	    Output("Frame Rate: "+frame_rate);
	    draw.setFrameRate(frame_rate);

	    // acquire image
	    BufferedImage read_img = null;
	    while(true){
		read_img = isrc.getImage();
		if(read_img != null)
		    break;
		Output("Error: Received null image!");
	    }
	    last_image = read_img;
	    BufferedImage img = ImageProc.cloneImage(read_img);

	    // classifier testing
	    // Detection D = new Detection(376,108,10,40);
	    // System.out.println(Avgcolorclass.classify(img, D));

	    //draw.clearAll();
	    //draw.setImage(img);
                
	    long time_start = System.currentTimeMillis();
	    DetectionResult dr = processImage(img);
	    //imagePyramidTest(img);
	    long time_end = System.currentTimeMillis();

	    drawDetection(img, dr.resolvedDetections, new int[] {255, 0, 0});
	    if(dr.descended != null)
		drawDetection(img, dr.descended, new int[] {0,0,255});
	
	    for (int i = 0; i < dr.scales.size(); i++) {
		Output("scales = " + dr.scales.get(i));
	    }

            // draw.setDetections(resolvedDetections);
            // try {
            //     Thread.sleep(2000);
            // } catch (InterruptedException ex) {
            //     Logger.getLogger(BuoyDetect.class.getName()).log(Level.SEVERE, null, ex);
            // }
            // draw.setDetections(descended);
        
	    //while (true) {

		dr.filterDetections(pg.gd(classify_dist));
		draw.setImage(img);
		draw.setDetections(dr.getAllDetections());
		draw.setLine(shoreD.getLine());
		draw.setStopWatch(dr.sw);

	    // 	try {
	    // 	    Thread.sleep(50);
	    // 	} catch (InterruptedException ex) {
	    // 	    Logger.getLogger(BuoyDetect.class.getName()).log(Level.SEVERE, null, ex);
	    // 	}

	    // 	if(do_recompute){
	    // 	    synchronized(lock){
	    // 		do_recompute = false;
	    // 	    }
	    // 	    break;
	    // 	}
	    // }

	}

    }

    public void parameterChanged(ParameterGUI pg, String name){
	if(name.startsWith("show_"))
	    draw.enableBuffer(name.substring(5), pg.gb(name));
	// else if(name.equals("recompute"))
	//     synchronized(lock){
	// 	do_recompute = true;
	//     }
	// else if(name.equals("next_image"))
	//     synchronized(lock){
	// 	currentFile++;
	// 	currentFile %= imageFiles.length;
	// 	pg.si("image_slider", currentFile);
	// 	do_recompute = true;
	//     }
	// else if(name.equals("prev_image"))
	//     synchronized(lock){
	// 	currentFile--;
	// 	if(currentFile < 0)
	// 	    currentFile = imageFiles.length-1;
	// 	pg.si("image_slider", currentFile);
	// 	do_recompute = true;
	//     }
	// else if(name.equals("image_slider"))
	//     synchronized(lock){
	// 	currentFile = pg.gi("image_slider");
	// 	do_recompute = true;
	//     }
	else if(name.equals("MoG")){
	    if(pg.gb("MoG")){
		System.out.println("Setting Magnitude of Gradient");
		synchronized(lock){
		    pg.sb("LoG", false);
		    filter = MoG_filter;
		}
	    }
	    else {
		pg.sb("MoG", true);
	    }
	} 
	else if(name.equals("LoG")){
	    if(pg.gb("LoG")){
		System.out.println("Setting Laplacian of Gaussian");
		synchronized(lock){
		    pg.sb("MoG", false);
		    filter = LoG_filter;
		}
	    }
	    else {
		pg.sb("LoG", true);
	    }
	} 

	else if(name.equals("hist_classify")){
	    if(pg.gb("hist_classify")){
		System.out.println("Setting Histogram Classifier");
		synchronized(lock){
		    pg.sb("rgb_classify", false);
		    classifier = Histcolorclass;
		    near_dist_limit = "hist_near_dist_limit";
		    classify_dist = "hist_classify_distance";
		}
	    }
	    else {
		pg.sb("hist_classify", true);
	    }
	} 
	else if(name.equals("rgb_classify")){
	    if(pg.gb("rgb_classify")){
		System.out.println("Setting Avg RGB Classifier");
		synchronized(lock){
		    pg.sb("hist_classify", false);
		    classifier = Avgcolorclass;
		    near_dist_limit = "rgb_near_dist_limit";
		    classify_dist = "rgb_classify_distance";
		}
	    }
	    else {
		pg.sb("rgb_classify", true);
	    }
	} 
	else if(name.equals("save_region")){
	    try {
		Region region = draw.getSelectedRegion();
		BufferedImage cropped_img = ImageProc.cropImage(last_image, 
						      region.x0, region.y0, 
						      region.x1, region.y1);	   
		long time = System.currentTimeMillis();
		ImageIO.write(cropped_img, "png", new File(time+".png"));
	    } catch(Exception ex){
		ex.printStackTrace();
		System.err.println("Error: Could not write cropped image to file!");
	    }
	}
	else if(name.equals("clear_region")){
	    draw.clearSelectedRegion();
	}
    }

    public void imagePyramidTest(BufferedImage img){
	final double NUM_ITER = 200;
	long time_start = System.currentTimeMillis();
	for(int i = 0; i < NUM_ITER; ++i){
	    ImagePyramid imgP = new ImagePyramid(img,6);
	}
	long time_end = System.currentTimeMillis();
	Output("Average time: "+((double)(time_end-time_start)/NUM_ITER));
    }


    /** The Real MEAT! **/

    public DetectionResult processImage(BufferedImage img){

        StopWatch sw = new StopWatch();
	DetectionResult dr = new DetectionResult(filter);

        // construct the multiresolution image pyramid
        sw.start("image pyramid");
	dr.imgP = new ImagePyramid(img);
        sw.stop();

        // for magnitude of gradient filter frontloading work
        sw.start("filter initialization");
        filter.initialize(dr.imgP);
        sw.stop();
                        
        // try and detect the shore
        sw.start("shore detector");
	if(pg.gb("shore_detect")){
	    shoreD.detect(dr.imgP.levels.get(0).data);
	    dr.shoreD = shoreD;
	}
        sw.stop();
        
        // generate response pyramid
        dr.offset = new int[2];
        dr.scales = new ArrayList<Double>();
        sw.start("response pyramid");
	dr.responseP = generateResponseP(dr.imgP, filter, dr.scales, dr.offset);
        sw.stop();

        // accumulate all the maxima across all of the levels
	dr.multiScaleRPeaks = new ArrayList<ArrayList<Detection>>();


        // perform suppression
        if ((filter.getMode() == Constants.MODEBOTH) && (filter.getMagMode() == Constants.MAGMODEHIGH)) {
            // then we want the minima which are large negative numbers
            sw.start("finding all Maxima");
            dr.multiScaleRPeaks.addAll(findAllMaxima(dr.responseP, dr.scales, dr.offset, filter.getThresholds()[1]));
            sw.stop();

            // and the maxima which are large positive numbers
            sw.start("finding all Minima");
            dr.multiScaleRPeaks.addAll(findAllMinima(dr.responseP, dr.scales, dr.offset, filter.getThresholds()[0]));
            sw.stop();
        }
        

        else if ((filter.getMode() == Constants.MODEPOSITIVE) && (filter.getMagMode() == Constants.MAGMODELOW)) {
            sw.start("finding all Minima");
            dr.multiScaleRPeaks.addAll(findAllMinima(dr.responseP, dr.scales, dr.offset, filter.getThresholds()[0]));
            sw.stop();
            

        } else if ((filter.getMode() == Constants.MODEPOSITIVE) && (filter.getMagMode() == Constants.MAGMODEHIGH)) {
            sw.start("finding all Maxima");
            dr.multiScaleRPeaks.addAll(findAllMaxima(dr.responseP, dr.scales, dr.offset, filter.getThresholds()[0]));
            sw.stop();

        } else {
            assert false: "Warning unsupported filter mode";
        }

               
        // now enforce scale consistency
        sw.start("scale filtering");
	dr.scaleConsistent = new ArrayList<Detection>();
	for (ArrayList<Detection> peakResponse : dr.multiScaleRPeaks) {
	    for (Detection d : peakResponse) {
		if (!pg.gb("scale_filter") || Sfilter.isConsistent(d)) {
		    dr.scaleConsistent.add(d); // the above operation also automatically refines scale estimate
		}   
	    }
	}
        sw.stop();
        

        // now resolve the multiple overlaping responses
        sw.start("resolution");
	dr.resolvedDetections = resolveDetections(dr.scaleConsistent, filter.getMagMode());
        sw.stop();
        

        // now classify each detection
	dr.numberD = new int[Constants.numLABELS];
        dr.buoys = new ArrayList<Detection>();
	dr.water = new ArrayList<Detection>();
        sw.start("classify");
        for (Detection d: dr.resolvedDetections) {
            int result = classifier.classify(img, d);
            dr.numberD[result]++;
            if ((result == Constants.RED) || 
		(result == Constants.YELLOW) || 
		(result == Constants.GREEN)) {
                dr.buoys.add(d);
            }
	    else {
		dr.water.add(d);
	    }
        }
        sw.stop();
        

        // now do gradient ascent for each of the buoys to try and achieve a better fit
	
        sw.start("gradient descent");
	if(pg.gb("grad_descent")){
	    dr.descended = new ArrayList<Detection>(dr.buoys.size());
	    for (Detection d: dr.buoys) {
		double[] rgbTarget = Avgcolorclass.getNearest(img, d); // this works for the average color classifier
		// scale the color // this doesn't enforce that the other classifier has to agree.
		//LinAlg.scaleEquals(rgbTarget, 255);
		System.out.print("target color = ");
		Utility.Matrix.print(rgbTarget);
		dr.descended.add(RGBGradientDescent.descend(img, d, rgbTarget));
	    }
	}
        sw.stop();
	
	final int NUM_STEPS = 6;
	 sw.start("near detect");
	 if(pg.gb("near_detect")){
	     
	     double dist_limit = pg.gd(near_dist_limit);
	     ArrayList<Detection> det = dr.buoys;
	     if(dr.descended != null)
	     	 det = dr.descended;

	     dr.nearBuoys = new ArrayList<Detection>(det.size());

	     int i = 0;
	     for(Detection d : det){

		 // skip any detections that are too far away
		 if(d.dist >= dist_limit)
		     continue;

		 double rad = d.scale/2.0;
		 double step = rad/(double)NUM_STEPS;

		 Detection best_det = d;
		 for(int y = -(NUM_STEPS-1); y < NUM_STEPS; ++y){
		     for(int x = -(NUM_STEPS-1); x < NUM_STEPS; ++x){
			 Detection nd = d.copy();
			 nd.cCol += Math.round(x*step);
			 nd.cRow += Math.round(y*step);
			 if(nd.cCol < 0 || nd.cCol >= img.getWidth() ||
			    nd.cRow < 0 || nd.cRow >= img.getHeight())
			     continue;
		    
			 int result = classifier.classify(img, nd);
			 if ((result == Constants.RED) || 
			     (result == Constants.YELLOW) || 
			     (result == Constants.GREEN)) {
			     //System.out.println("nd.dist="+nd.dist);
			     if(nd.dist < best_det.dist)
				 best_det = nd;
			 }
		     }
		 }
	    
		 dr.nearBuoys.add(best_det);
		 ++i;
	     }
	 }
	 sw.stop();
	

        Output("found: red " + dr.numberD[0] + 
	       ", green "+ dr.numberD[1] + 
	       ", yellow: " + dr.numberD[2] + 
	       ", water " + dr.numberD[3]);

        Output(sw.prettyPrint());

	dr.sw = sw;
	return dr;
        
        // process response pyramid
        /*System.out.println("processing response pyramid");
        ArrayList<Detection> peakResponse = new ArrayList<Detection>();
        if ((filter.getMode() == 1) || (filter.getMode() == 2)) {
            sw.start("non maxima suppression");
            ArrayList<Detection> trueMaxima = processResponsePMax(responseP,scales,offset,Constants.MAXRESPONSETHREOLD_LofG);
            peakResponse.addAll(trueMaxima);
            sw.stop();
        } 
        
        if ((filter.getMode() == 0) || (filter.getMode() == 2)) {
            sw.start("non minima suppression");
            ArrayList<Detection> trueMinima = processResponsePMin(responseP,scales,offset,Constants.MINRESPONSETHRESHOLD_LofG);
            peakResponse.addAll(trueMinima);
            sw.stop();
        }
        System.out.println("found " + peakResponse.size() + " peaks");
        
        // filter the peaks by imposing geometric constraints using the ScaleFilter
        ArrayList<Detection> scaleConsistent = new ArrayList<Detection>();
        sw.start("scale filtering");
        for (Detection d: peakResponse) {
            if(Sfilter.isConsistent(d)) {
                scaleConsistent.add(d); // the above operation also automatically refines scale estimate
            }
        }
        sw.stop();*/
        
        // classify each found minima
        /*int result;
        sw.start("classify");
        int[] numberD = new int[Constants.numLABELS];
        for (Detection d: scaleConsistent) {
        //for (Detection d: peakResponse) {
            result = Avgcolorclass.classify(img, d);
            numberD[result]++;
        }
        sw.stop();
        
        System.out.println("found: red " + numberD[0] + ", green "+ numberD[1] + ", yellow: " + numberD[2] + ", water " + numberD[3]);
        
        System.out.println("drawing maxima");
        //drawDetection(img, peakResponse);
        drawDetection(img, scaleConsistent);
        draw.setImage(img);
        //draw.setDetections(peakResponse);
        draw.setDetections(scaleConsistent);
        draw.setLine(shoreD.getLine());
	
        System.out.println(sw.prettyPrint());
        
        for (int i = 0; i < scales.size(); i++) {
            System.out.println("scales = " + scales.get(i));
        }*/
        
        
 
        /*
        while (true) {
            for (int i = 0; i < responseP.levels.size(); i++) {
                BufferedImage full = Utility.Matrix.visualize(responseP.levels.get(i).data, 1);
                //drawDetection(full, allMinima.get(i));
                draw.setImage(full);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(BuoyDetect.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }*/
        
        /*
        System.out.println("done!");
        
        try {
            File out = new File("testResponse.png");
            ImageIO.write(Utility.Matrix.visualize(responseP.levels.get(0).data,1), "png", out);
        } catch (IOException ex) {}
        
	// We now have an arraylist of responses...
	// Non-maximal suppression time!
         * */

    }
    
    // given an arrayList of detections which may overlap how do we want to sort them out and perhaps cluster them
    // to clean up our data
    // magnitude mode tells us whether large magnitudes or small magnitudes are better
    private ArrayList<Detection> resolveDetections(ArrayList<Detection> detections, int MagnitudeMode) {
        assert ((MagnitudeMode == Constants.MAGMODEHIGH) || (MagnitudeMode == Constants.MAGMODELOW)) : "invalid mode!!";
        ArrayList<Detection> trueD = new ArrayList<Detection>();
        ArrayList<Detection> overLapSet = new ArrayList<Detection>();
        
        // while the list is not empty
        while(!detections.isEmpty()) {
            // select first element and test for collisions between others
            Detection root = detections.get(0);
            overLapSet.add(root);
            double rootS2 = root.scale/2.0;
            double[] rootLoc = new double[] {root.cCol, root.cRow};
            double[] otherLoc = new double[2];
            
            // also keep track of a max response and an average
            Detection bestD = root;
            double avgR = Math.abs(root.Response);
            
            // compare with all others, note would it be cheaper to make a KD tree and then use it?
            for (int i = 1; i < detections.size(); i++) {
                Detection other = detections.get(i);
                otherLoc[0] = other.cCol;
                otherLoc[1] = other.cRow;
                // do the detections overlap, how much do they need to overlap by?
                // TODO should they have to overlap by a significant amount?
                if (LinAlg.magnitude(LinAlg.subtract(rootLoc,otherLoc)) <= (rootS2 + other.scale/2.0)) {
                    overLapSet.add(other);  // add to our overlap set
                    avgR += Math.abs(other.Response); // add into average response
                    if ((MagnitudeMode == Constants.MAGMODEHIGH) && (Math.abs(other.Response) > Math.abs(bestD.Response))) {
                        bestD = other;       // keep track of the maxima
                    } else if ((MagnitudeMode == Constants.MAGMODELOW) && (Math.abs(other.Response) < Math.abs(bestD.Response))) {
                        bestD = other;
                    }
                }
            }
            avgR /= overLapSet.size();
            
            // if a single detection dominated the response then use it and throw away the others
            if ((MagnitudeMode == Constants.MAGMODEHIGH) && (Math.abs(bestD.Response)/avgR >= 3.0)) {    // 3 times the average response
                //System.out.println("maxD: cRow="+bestD.cRow+" cCol="+bestD.cCol);
                trueD.add(bestD);
            } else if ((MagnitudeMode == Constants.MAGMODEHIGH) && (avgR/Math.abs(bestD.Response) >= 3.0)) {
                trueD.add(bestD);
            }
            
            // otherwise fabricate a new detection as an average of the others
            else {
	      double[] Loc = new double[]{0, 0};
                double scale = 0;
                double totalweight = 0;
                for (Detection d : overLapSet) { // weighted sum of centroids
                    //System.out.println("d: cCol="+d.cCol+" cRow="+d.cRow);
                    if (MagnitudeMode == Constants.MAGMODEHIGH) {
                        Loc[0] += Math.abs(d.Response) * d.cCol;
                        Loc[1] += Math.abs(d.Response) * d.cRow;
                        scale += Math.abs(d.Response) * d.scale;
                        totalweight += Math.abs(d.Response);
                    } else if (MagnitudeMode == Constants.MAGMODELOW) {
                        Loc[0] +=  d.cCol/Math.abs(d.Response);
                        Loc[1] +=  d.cRow/Math.abs(d.Response);
                        scale +=  d.scale/Math.abs(d.Response);
                        totalweight += Math.abs(1/d.Response);
                    } 
                }
                // normalize
                Loc[0] /= totalweight;
                Loc[1] /= totalweight;
                scale /= totalweight;
                /*double totalR = avgR * overLapSet.size();
                if (MagnitudeMode == Constants.MAGMODEHIGH) {
                    Loc[0] /= totalR;
                    Loc[1] /= totalR;
                    scale /= totalR;
                } else if (MagnitudeMode == Constants.MAGMODELOW) {
                    Loc[0] *= totalR;
                    Loc[1] *= totalR;
                    scale *= totalR;
                }*/
                
                // synthesize a new detection to approximate the lot
                Detection avgD = new Detection((int) Math.round(Loc[1]),(int) Math.round(Loc[0]), avgR, scale);
		//System.out.println("avgD: cRow="+avgD.cRow+" cCol="+avgD.cCol+" - Loc[0]="+Loc[0]+" Loc[1]="+Loc[1]);
                trueD.add(avgD);
            }
            
            // now clean up
            if (!detections.removeAll(overLapSet)) {
                System.err.println("removal went wrong in resolve detections!");
                assert false;
            }
            overLapSet.clear();
        }
        
        return trueD;
    }
    
    // expects an empty arraylist to be passed in, this uses filter dimensions along with
    // image pyramid levels to determine a characteristic scale for each response
    private ImagePyramid generateResponseP(ImagePyramid imgP, Filter f, ArrayList<Double> scales, int[] offset) {
        assert (f.getWidth() % 2 != 0) : "want an odd filter so don't move the image";
        assert (f.getHeight() % 2 != 0);
        assert scales.isEmpty() : "warning expected empty scales";
       
        ImagePyramid responseP = new ImagePyramid(imgP.levels.size());
        //System.out.println("filter size = " + f.getWidth() + " x " + f.getHeight());
        int istart = (int) Math.ceil(f.getHeight()/2.0);// start in row direction
        int jstart = (int) Math.ceil(f.getWidth()/2.0);
        offset[0] = istart; // store offset data so others will know how to convert this back to image coordinates
        offset[1] = jstart; 
        
        // for each level of the image
        for (int l = 0; l < imgP.levels.size(); l++) {
            Output("level " + l + " out of " + imgP.levels.size());
            ImageSample img = imgP.levels.get(l);
            //System.out.println("img level " + l + " dimensions" + img.data[0].length + " x " + img.data.length);
            
            // where to end for this level
            int iend = img.data.length - (int) Math.ceil(f.getHeight()/2.0); 
            int jend = img.data[0].length - (int) Math.ceil(f.getWidth()/2.0);
            
            //System.out.println("Ending point = " + iend + " " + jend);
            
            // use that difference to construct response matrix for this level of the pyramid
            if ((iend - istart <= 0) || (jend - jstart <= 0)) {
                Output("terminating filter dimensions larger than remaining image");
                break;
            }
            double[][] response = new double[iend - istart][jend - jstart];
            //System.out.println("response level " + l + " rows = " + response.length + " columns " + response[0].length);
            
            // set up the level of the response pyramid
            responseP.levels.add(new ImageSample(response,img.downsample));
            
            // compute the scale associated with this level
            scales.add((double)(img.downsample * f.getHeight()));
           
            
            // speed up by not copying windows or any of that stupidity
            for (int i = 0; i < response.length; i++) {
               
                for (int j = 0; j < response[0].length; j++) {  
                    //System.out.println("row " + i + " col " + j + " image dimensions " + img.data.length + " x " + img.data[0].length );
                    response[i][j] = f.measureResponse(img.data,i,j, l);
                }
            }
        }
        // sliding window across the image to generate response save in new image pyramid
        // compute scale factor that describes the characteristic scale of each level of the pyramid
        return responseP;
    }
    
    // draws the minima on the image
    // for now only as single points
    // TODO make it draw circles it would be pretty
    private void drawDetection(BufferedImage original, ArrayList<Detection> detections, int[] rgb) {
        if (original.getType() == BufferedImage.TYPE_INT_RGB) {
            int color = ((rgb[0] & 0xff) << 16) | ((rgb[1] & 0xff) << 8) | ((rgb[2] & 0xff));
            int data[] = ((DataBufferInt) (original.getRaster().getDataBuffer())).getData();

            int imgWidth = original.getWidth();
            int imgHeight = original.getHeight();

            //if (original.getType() == BufferedImage.T)
            for (int i = 0; i < detections.size(); i++) {
                int y = detections.get(i).cRow;
                int x = detections.get(i).cCol;
                data[y * imgWidth + x] = color; 
            }
        } else if (original.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            byte data[] = ((DataBufferByte) (original.getRaster().getDataBuffer())).getData();

            int imgWidth = original.getWidth();
            int imgHeight = original.getHeight();

            //if (original.getType() == BufferedImage.T)
            for (int i = 0; i < detections.size(); i++) {
                int y = detections.get(i).cRow;
                int x = detections.get(i).cCol;
                //System.out.println("x="+x+" y="+y);
                data[3*(y * imgWidth + x)] = (byte) (rgb[2] & 0xff); // blue
                data[3*(y * imgWidth + x) + 1] = (byte) (rgb[1] & 0xff); // green
                data[3*(y * imgWidth + x) + 2] = (byte) (rgb[0] & 0xff); // red
            }
        } else {
            System.err.println("Unsupported image format type = " + original.getType());
            assert false;
        }
    }
    
    private ArrayList<ArrayList<Detection>> findAllMaxima(ImagePyramid responseP,ArrayList<Double> scales, int[] offset, double Threshold) {
        boolean adjustPixel = true; // adjust pixel coordinates for display over the original or to be displayed over
        // each of the levels they were taken from
        
        assert (responseP.levels.size() == scales.size()) : "Expected same size";
        //ArrayList<ArrayList<double[]>> allMinima = new ArrayList<ArrayList<double[]>>();
        ArrayList<ArrayList<Detection>> allMaxima = new ArrayList<ArrayList<Detection>>();
        
        // find all the local minima at each scale
        // convert to more convinient type for later
        Output("Finding all maxima");
        for (int s = 0; s < responseP.levels.size(); s++) {
            double[][] curScaleRes = responseP.levels.get(s).data;
            double pixelrate = responseP.levels.get(s).downsample;
            ArrayList<int[]> maxima = Suppression.nonMaxima(curScaleRes, 1, Threshold); // use 1 neighborhood
            
            ArrayList<Detection> maxDescriptor = new ArrayList<Detection>(maxima.size());
            //ArrayList<double[]> minDescriptor = new ArrayList<double[]>(minima.size());
            Output("level " + s);
            double curScale = scales.get(s);
            for (int m = 0; m < maxima.size(); m++) {
                int[] max = maxima.get(m);
                double value = curScaleRes[max[0]][max[1]];
                
                // row column value scale where pixel values are in full image size
                //double[] descriptor;
                Detection descriptor;
                if (adjustPixel) {
                    //descriptor = new double[] {(double) ((min[0] + offset[0])*pixelrate), 
                    //    (double) ((min[1] + offset[1])*pixelrate), value, scales.get(s)};
                    descriptor = new Detection((int) ((max[0]+offset[0])*pixelrate),(int) ((max[1]+offset[1])*pixelrate), value, curScale);
                } else {
                    descriptor = new Detection(max[0]+offset[0],max[1]+offset[1],value,curScale);
                }
                maxDescriptor.add(descriptor);
            }
            
            allMaxima.add(maxDescriptor);
        }
        return allMaxima;
    }
    
    private ArrayList<ArrayList<Detection>> findAllMinima(ImagePyramid responseP,ArrayList<Double> scales, int[] offset, double Threshold) {
        boolean adjustPixel = true; // adjust pixel coordinates for display over the original or to be displayed over
        // each of the levels they were taken from
        
        assert (responseP.levels.size() == scales.size()) : "Expected same size";
        //ArrayList<ArrayList<double[]>> allMinima = new ArrayList<ArrayList<double[]>>();
        ArrayList<ArrayList<Detection>> allMinima = new ArrayList<ArrayList<Detection>>();
        
        // find all the local minima at each scale
        // convert to more convinient type for later
        Output("Finding all minima");
        for (int s = 0; s < responseP.levels.size(); s++) {
            double[][] curScaleRes = responseP.levels.get(s).data;
            double pixelrate = responseP.levels.get(s).downsample;
            ArrayList<int[]> minima = Suppression.nonMinima(curScaleRes, 1, Threshold); // use 1 neighborhood
            
            ArrayList<Detection> minDescriptor = new ArrayList<Detection>(minima.size());
            //ArrayList<double[]> minDescriptor = new ArrayList<double[]>(minima.size());
            Output("level " + s);
            double curScale = scales.get(s);
            for (int m = 0; m < minima.size(); m++) {
                int[] min = minima.get(m);
                double value = curScaleRes[min[0]][min[1]];
                
                // row column value scale where pixel values are in full image size
                //double[] descriptor;
                Detection descriptor;
                if (adjustPixel) {
                    //descriptor = new double[] {(double) ((min[0] + offset[0])*pixelrate), 
                    //    (double) ((min[1] + offset[1])*pixelrate), value, scales.get(s)};
                    descriptor = new Detection((int) ((min[0]+offset[0])*pixelrate),(int) ((min[1]+offset[1])*pixelrate), value, curScale);
                } else {
                    descriptor = new Detection(min[0]+offset[0],min[1]+offset[1],value,curScale);
                }
                minDescriptor.add(descriptor);
            }
            
            allMinima.add(minDescriptor);
        }
        return allMinima;
    }
    
    // converts response pyramid into list of minima in pixels and scale
    // row column scale
    // TODO at the moment only looks for minima, make this vary with the mode
    private ArrayList<Detection> processResponsePMax(ImagePyramid responseP,ArrayList<Double> scales, int[] offset, double Threshold) {
        assert (responseP.levels.size() == scales.size()) : "Expected same size";
        
        
        ArrayList<ArrayList<Detection>> allMaxima = findAllMaxima(responseP,scales,offset, Threshold);
        
       
        // now for the fun part
        // all of the descriptors are in the pixel space of the original image
        // also the minima are ordered which may be able to take advantage of
        // TODO take advantage of ordered lists
        ArrayList<Detection> trueMaxima = new ArrayList<Detection>();
        
        Output("Finding true maxima");
        // at each scale which is each sublist, compare against other scales
        for (int s = 0; s < allMaxima.size(); s++) {
            double radiusThreshold2 = (scales.get(s)/2.0)*(scales.get(s)/2.0); // distance threshold now depends on scale
            ArrayList<Detection> curmaxima = allMaxima.get(s);
            Output("level " + s);
            // for each minima
            for (int m = 0; m < curmaxima.size(); m++) {
                
                // compare against minima above and below
                Detection curmax = curmaxima.get(m);
                boolean isMax = true;
                
                int startl;
                if (s > 0) {
                    startl = s-1;
                } else {
                    startl = s+1;
                }
                int endl;
                if (s < allMaxima.size() - 1) {
                    endl = s + 1;
                } else {
                    endl = s - 1;
                }
                
                for (int p = startl; p <= endl; p += 2) {
                    //System.out.println("comparing with level " + p);
                    ArrayList<Detection> othermaxima = allMaxima.get(p);
                    //ArrayList<Integer> removeList = new ArrayList<Integer>(); // will remove all of the ones after done iterating
                    
                    for (int k = 0; k < othermaxima.size(); k++) {
                        Detection thatmax = othermaxima.get(k);
                        
                        // if it is close enough
                        double r2 = (thatmax.cRow - curmax.cRow)*(thatmax.cRow - curmax.cRow) + 
                                (thatmax.cCol - curmax.cCol)*(thatmax.cCol - curmax.cCol);
                        if (r2 <= radiusThreshold2) {
                            // if find one greater than the one we are considering want to clean up and move on
                          if (thatmax.Response >curmax.Response) {  
			  //if (Math.abs(thatmax.Response) > Math.abs(curmax.Response)) {
                                isMax = false;
                                break;
                            } 
                        }
                    }
                    if (!isMax) {
                        break;
                    }
                }
                
                if (isMax) {
                    trueMaxima.add(curmax);
                }
            }
        }
        
        return trueMaxima;
    }
    
    private ArrayList<Detection> processResponsePMin(ImagePyramid responseP,ArrayList<Double> scales, int[] offset, double Threshold) {
        assert (responseP.levels.size() == scales.size()) : "Expected same size";
        
        ArrayList<ArrayList<Detection>> allMinima = findAllMinima(responseP,scales,offset, Threshold);
       
        // now for the fun part
        // all of the descriptors are in the pixel space of the original image
        // also the minima are ordered which may be able to take advantage of
        // TODO take advantage of ordered lists
        ArrayList<Detection> trueMinima = new ArrayList<Detection>();
        
        Output("Finding true minima");
        // at each scale which is each sublist, compare against other scales
        for (int s = 0; s < allMinima.size(); s++) {
            double radiusThreshold2 = (scales.get(s)/2.0)*(scales.get(s)/2.0);
            ArrayList<Detection> curminima = allMinima.get(s);
            Output("level " + s);
            // for each minima
            for (int m = 0; m < curminima.size(); m++) {
                
                // compare against minima above and below
                Detection curmin = curminima.get(m);
                boolean isMin = true;
                
                int startl;
                if (s > 0) {
                    startl = s-1;
                } else {
                    startl = s+1;
                }
                int endl;
                if (s < allMinima.size() - 1) {
                    endl = s + 1;
                } else {
                    endl = s - 1;
                }
                
                for (int p = startl; p <= endl; p += 2) {
                    //System.out.println("comparing with level " + p);
                    ArrayList<Detection> otherminima = allMinima.get(p);
                    //ArrayList<Integer> removeList = new ArrayList<Integer>(); // will remove all of the ones after done iterating
                    
                    for (int k = 0; k < otherminima.size(); k++) {
                        Detection thatmin = otherminima.get(k);
                        
                        // if it is close enough
                        double r2 = (thatmin.cRow - curmin.cRow)*(thatmin.cRow - curmin.cRow) + 
                                (thatmin.cCol - curmin.cCol)*(thatmin.cCol - curmin.cCol);
                        if (r2 <= radiusThreshold2) {
                            // if find one greater than the one we are considering want to clean up and move on
                          if (thatmin.Response < curmin.Response) {
			  //if (Math.abs(thatmin.Response) < Math.abs(curmin.Response)) {
                                isMin = false;
                                break;
                            } 
                        }
                    }
                    if (!isMin) {
                        break;
                    }
                }
                
                if (isMin) {
                    trueMinima.add(curmin);
                }
            }
        }
        
        return trueMinima;
    }
    
    
    
}

