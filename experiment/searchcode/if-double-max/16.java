<<<<<<< HEAD
//Copyright 2005 Harvard University / Howard Hughes Medical Institute (HHMI) All Rights Reserved
package org.nrg.plexiViewer.lite.gui;


import java.awt.*;
import java.awt.event.*;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;

public class PlexiAdjusterWindow extends Frame implements AdjustmentListener, WindowListener, Runnable {

    static final int AUTO_THRESHOLD = 5000;
    static final String[] channelLabels = {"Red", "Green", "Blue", "Cyan", "Magenta", "Yellow", "RGB"};
    static final int[] channelConstants = {4, 2, 1, 3, 5, 6, 7};
    
    ContrastPlot plot = new ContrastPlot();
	Thread thread = null;
	
	private static PlexiAdjusterWindow adjuster=null;
	
    public boolean instance;
        
    int minSliderValue=-1, maxSliderValue=-1, brightnessValue=-1, contrastValue=-1;
    int sliderRange = 256;
    boolean doAutoAdjust,doReset,doSet,doApplyLut,doThreshold,doUpdate;
    
    Panel panel, tPanel;
    Button autoB, resetB, setB, applyB, threshB, updateB;
    int previousImageID;
    int previousType;
    Object previousSnapshot;
    ImageJ ij;
    double min, max;
    double previousMin, previousMax;
    double defaultMin, defaultMax;
    int contrast, brightness;
    boolean RGBImage;
    Scrollbar minSlider, maxSlider, contrastSlider, brightnessSlider;
    Label minLabel, maxLabel, windowLabel, levelLabel;
    boolean done;
    int autoThreshold;
    GridBagLayout gridbag;
    GridBagConstraints c;
    int y = 0;
    boolean windowLevel, balance;
    Font monoFont = new Font("Monospaced", Font.PLAIN, 12);
    Font sanFont = new Font("SansSerif", Font.PLAIN, 12);
    int channels = 7; // RGB
    Choice choice;
	ImagePlus imp;

    public PlexiAdjusterWindow() {
        super("B&C");
        addWindowListener(this);
		instance=false;
    }
    
	public static PlexiAdjusterWindow GetInstance() {
			if (adjuster==null)
				adjuster = new PlexiAdjusterWindow();
			return adjuster;		
	}
    
    /*public void show(java.util.List imageViewers) {
		init();
    	Iterator iter = imageViewers.iterator();
    	while (iter.hasNext()) {
			XNATImageViewerI mrimage = (XNATImageViewerI)iter.next();
			show(mrimage.getImageCopy());
    	}
    }*/

	//Use in conjunction with method setCurrentWindow     
	public void display() {
		if (WindowManager.getCurrentWindow()!=null)
		{
		  imp = WindowManager.getCurrentWindow().getImagePlus();
		  //System.out.println("Adjuster " + imp.getTitle()); 	
		  if (!instance) {
			  init();
			  instance = true;
		  }else {
			  this.setVisible(true);
		  }
		  initRest();
		}else {
			System.out.println("XNATAdjuster::There is no image to display");  		
		}
		
	  }
    
    private void init() {
		windowLevel = false;
		balance=false;
        ij = IJ.getInstance();
        gridbag = new GridBagLayout();
        c = new GridBagConstraints();
        setLayout(gridbag);
        
        // plot
        c.gridx = 0;
        y = 0;
        c.gridy = y++;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(10, 10, 0, 10);
        gridbag.setConstraints(plot, c);
        add(plot); 
        
        // min and max labels
        if (!windowLevel) {
            panel = new Panel();
            c.gridy = y++;
            c.insets = new Insets(0, 10, 0, 10);
            gridbag.setConstraints(panel, c);
            panel.setLayout(new BorderLayout());
            minLabel = new Label("      ", Label.LEFT);
            minLabel.setFont(monoFont);
            panel.add("West", minLabel);
            maxLabel = new Label("      " , Label.RIGHT);
            maxLabel.setFont(monoFont);
            panel.add("East", maxLabel);
            add(panel);
        }

        // min slider
        if (!windowLevel) {
            minSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange/2, 1, 0, sliderRange);
            c.gridy = y++;
            c.insets = new Insets(2, 10, 0, 10);
            gridbag.setConstraints(minSlider, c);
            add(minSlider);
            minSlider.addAdjustmentListener(this);
            minSlider.setUnitIncrement(1);
            addLabel("Minimum", null);
        }

        // max slider
        if (!windowLevel) {
            maxSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange/2, 1, 0, sliderRange);
            c.gridy = y++;
            c.insets = new Insets(2, 10, 0, 10);
            gridbag.setConstraints(maxSlider, c);
            add(maxSlider);
            maxSlider.addAdjustmentListener(this);
            maxSlider.setUnitIncrement(1);
            addLabel("Maximum", null);
        }
        
        // brightness slider
        brightnessSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange/2, 1, 0, sliderRange);
        c.gridy = y++;
        c.insets = new Insets(windowLevel?12:2, 10, 0, 10);
        gridbag.setConstraints(brightnessSlider, c);
        add(brightnessSlider);
        brightnessSlider.addAdjustmentListener(this);
        brightnessSlider.setUnitIncrement(1);
        if (windowLevel)
            addLabel("Level: ", levelLabel=new TrimmedLabel("        "));
        else
            addLabel("Brightness", null);
            
        // contrast slider
        if (!balance) {
            contrastSlider = new Scrollbar(Scrollbar.HORIZONTAL, sliderRange/2, 1, 0, sliderRange);
            c.gridy = y++;
            c.insets = new Insets(2, 10, 0, 10);
            gridbag.setConstraints(contrastSlider, c);
            add(contrastSlider);
            contrastSlider.addAdjustmentListener(this);
            contrastSlider.setUnitIncrement(1);
            if (windowLevel)
                addLabel("Window: ", windowLabel=new TrimmedLabel("        "));
            else
                addLabel("Contrast", null);
        }
        
        pack();
        GUI.center(this);
        show();
    }
     
    void initRest() {
		if (done)
			done = false;
		if (thread==null) { 
			thread = new Thread(this, "ContrastAdjuster");
			thread.start();
		}
			
		setup();
    }
        
    void addLabel(String text, Label label2) {
        panel = new Panel();
        c.gridy = y++;
        c.insets = new Insets(0, 10, 0, 0);
        gridbag.setConstraints(panel, c);
        panel.setLayout(new FlowLayout(label2==null?FlowLayout.CENTER:FlowLayout.LEFT, 0, 0));
        Label label= new TrimmedLabel(text);
        label.setFont(sanFont);
        panel.add(label);
        if (label2!=null) {
            label2.setFont(monoFont);
            label2.setAlignment(Label.LEFT);
            panel.add(label2);
        }
        add(panel);
    }

    void setup() {
        if (imp!=null) {
            //IJ.write("setup");
           // System.out.println("Adjuster " + imp.getTitle());
            ImageProcessor ip = imp.getProcessor();
            setup(imp);
            updatePlot();
            updateLabels(imp, ip);
            imp.updateAndDraw();
        }
    }
    
    public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
        if (e.getSource()==minSlider)
            minSliderValue = minSlider.getValue();
        else if (e.getSource()==maxSlider)
            maxSliderValue = maxSlider.getValue();
        else if (e.getSource()==contrastSlider)
            contrastValue = contrastSlider.getValue();
        else
            brightnessValue = brightnessSlider.getValue();
		notify();

    }

    
    ImageProcessor setup(ImagePlus imp) {
        ImageProcessor ip = imp.getProcessor();
        int type = imp.getType();
        RGBImage = type==ImagePlus.COLOR_RGB;
        boolean snapshotChanged = RGBImage && previousSnapshot!=null && ((ColorProcessor)ip).getSnapshotPixels()!=previousSnapshot;
        if (imp.getID()!=previousImageID || snapshotChanged || type!=previousType)
            setupNewImage(imp, ip);
        previousImageID = imp.getID();
        previousType = type;
        return ip;
    }

    void setupNewImage(ImagePlus imp, ImageProcessor ip)  {
        //IJ.write("setupNewImage");
        previousMin = min;
        previousMax = max;
        if (RGBImage) {
            ip.snapshot();
            previousSnapshot = ((ColorProcessor)ip).getSnapshotPixels();
        } else
            previousSnapshot = null;
        double min2 = ip.getMin();
        double max2 = ip.getMax();
        if (imp.getType()==ImagePlus.COLOR_RGB)
            {min2=0.0; max2=255.0;}
        if ((ip instanceof ShortProcessor) || (ip instanceof FloatProcessor)) {
            ip.resetMinAndMax();
            defaultMin = ip.getMin();
            defaultMax = ip.getMax();
        } else {
            defaultMin = 0;
            defaultMax = 255;
        }
        setMinAndMax(ip, min2, max2);
        min = ip.getMin();
        max = ip.getMax();
        if (IJ.debugMode) {
            IJ.log("min: " + min);
            IJ.log("max: " + max);
            IJ.log("defaultMin: " + defaultMin);
            IJ.log("defaultMax: " + defaultMax);
        }
        plot.defaultMin = defaultMin;
        plot.defaultMax = defaultMax;
        plot.histogram = null;
        updateScrollBars(null);
        if (!doReset)
            plotHistogram(imp);
        autoThreshold = 0;
    }
    
    void setMinAndMax(ImageProcessor ip, double min, double max) {
        if (channels!=7 && ip instanceof ColorProcessor)
            ((ColorProcessor)ip).setMinAndMax(min, max, channels);
        else {
			ip.setMinAndMax(min, max);
        }
    }

    void updatePlot() {
       plot.min = min;
       plot.max = max;
       plot.repaint();
    }
    
    void updateLabels(ImagePlus imp, ImageProcessor ip) {
        double min = ip.getMin();
        double max = ip.getMax();
        int type = imp.getType();
        Calibration cal = imp.getCalibration();
        boolean realValue = type==ImagePlus.GRAY32;
        if (cal.calibrated()) {
            min = cal.getCValue((int)min);
            max = cal.getCValue((int)max);
            if (type!=ImagePlus.GRAY16)
                realValue = true;
        }
        int digits = realValue?2:0;
        if (windowLevel) {
            //IJ.log(min+" "+max);
            double window = max-min;
            double level = min+(window)/2.0;
            windowLabel.setText(IJ.d2s(window, digits));
            levelLabel.setText(IJ.d2s(level, digits));
        } else {
           minLabel.setText(IJ.d2s(min, digits));
           maxLabel.setText(IJ.d2s(max, digits));
        }
    }

    void updateScrollBars(Scrollbar sb) {
        if (sb==null || sb!=contrastSlider) {
            double mid = sliderRange/2;
            double c = ((defaultMax-defaultMin)/(max-min))*mid;
            if (c>mid)
                c = sliderRange - ((max-min)/(defaultMax-defaultMin))*mid;
            contrast = (int)c;
            if (contrastSlider!=null)
                contrastSlider.setValue(contrast);
        }
        if (sb==null || sb!=brightnessSlider) {
            double level = min + (max-min)/2.0;
            double normalizedLevel = 1.0 - (level - defaultMin)/(defaultMax-defaultMin);
            brightness = (int)(normalizedLevel*sliderRange);
            brightnessSlider.setValue(brightness);
        }
        if (minSlider!=null && (sb==null || sb!=minSlider))
            minSlider.setValue(scaleDown(min));
        if (maxSlider!=null && (sb==null || sb!=maxSlider)) 
            maxSlider.setValue(scaleDown(max));
    }
    
    int scaleDown(double v) {
        if (v<defaultMin) v = defaultMin;
        if (v>defaultMax) v = defaultMax;
        return (int)((v-defaultMin)*255.0/(defaultMax-defaultMin));
    }
    

    void adjustMin(ImagePlus imp, ImageProcessor ip, double minvalue) {
        //IJ.log((int)min+" "+(int)max+" "+minvalue+" "+defaultMin+" "+defaultMax);
        min = defaultMin + minvalue*(defaultMax-defaultMin)/255.0;
        if (max>defaultMax)
            max = defaultMax;
        if (min>max)
            max = min;
        setMinAndMax(ip, min, max);
        if (min==max)
            setThreshold(ip);
        updateScrollBars(minSlider);
    }

    void adjustMax(ImagePlus imp, ImageProcessor ip, double maxvalue) {
        //IJ.log(min+" "+max+" "+maxvalue);
        max = defaultMin + maxvalue*(defaultMax-defaultMin)/255.0;
        if (min<0)
            min = 0;
        if (max<min)
            min = max;
        setMinAndMax(ip, min, max);
        if (min==max)
            setThreshold(ip);
        updateScrollBars(maxSlider);
    }

    void adjustBrightness(ImagePlus imp, ImageProcessor ip, double bvalue) {
        double center = defaultMin + (defaultMax-defaultMin)*((sliderRange-bvalue)/sliderRange);
        double width = max-min;
        min = center - width/2.0;
        max = center + width/2.0;
        setMinAndMax(ip, min, max);
        if (min==max)
            setThreshold(ip);
        updateScrollBars(brightnessSlider);
    }

    void adjustContrast(ImagePlus imp, ImageProcessor ip, int cvalue) {
        double slope;
        double center = min + (max-min)/2.0;
        double range = defaultMax-defaultMin;
        double mid = sliderRange/2;
        if (cvalue<=mid)
            slope = cvalue/mid;
        else
            slope = mid/(sliderRange-cvalue);
        if (slope>0.0) {
            min = center-(0.5*range)/slope;
            max = center+(0.5*range)/slope;
        }
        setMinAndMax(ip, min, max);
        updateScrollBars(contrastSlider);
    }

    void reset(ImagePlus imp, ImageProcessor ip) {
        if (RGBImage)
            ip.reset();
        if ((ip instanceof ShortProcessor) || (ip instanceof FloatProcessor)) {
            ip.resetMinAndMax();
            defaultMin = ip.getMin();
            defaultMax = ip.getMax();
            plot.defaultMin = defaultMin;
            plot.defaultMax = defaultMax;
        }
        min = defaultMin;
        max = defaultMax;
        setMinAndMax(ip, min, max);
        updateScrollBars(null);
        plotHistogram(imp);
        autoThreshold = 0;
    }

    void update(ImagePlus imp, ImageProcessor ip) {
        if (previousMin==0.0 && previousMax==0.0 || imp.getType()!=previousType)
            IJ.beep();
        else {
            min = previousMin;
            max = previousMax;
            setMinAndMax(ip, min, max);
            updateScrollBars(null);
            plotHistogram(imp);
        }
    }

    void plotHistogram(ImagePlus imp) {
        ImageStatistics stats;
        if (balance && (channels==4 || channels==2 || channels==1) && imp.getType()==ImagePlus.COLOR_RGB) {
            int w = imp.getWidth();
            int h = imp.getHeight();
            byte[] r = new byte[w*h];
            byte[] g = new byte[w*h];
            byte[] b = new byte[w*h];
            ((ColorProcessor)imp.getProcessor()).getRGB(r,g,b);
            byte[] pixels=null;
            if (channels==4)
                pixels = r;
            else if (channels==2)
                pixels = g;
            else if (channels==1)
                pixels = b;
            ImageProcessor ip = new ByteProcessor(w, h, pixels, null);
            stats = ImageStatistics.getStatistics(ip, 0, imp.getCalibration());
        } else
            stats = imp.getStatistics();
        plot.setHistogram(stats);
    }

    void apply(ImagePlus imp, ImageProcessor ip) {
        if (RGBImage)
            imp.unlock();
        if (!imp.lock())
            return;
        if (imp.getType()==ImagePlus.COLOR_RGB) {
            if (imp.getStackSize()>1)
                applyRGBStack(imp);
            else {
                ip.snapshot();
                reset(imp, ip);
                imp.changes = true;
            }
            imp.unlock();
            return;
        }
        if (imp.getType()!=ImagePlus.GRAY8) {
            IJ.beep();
            IJ.showStatus("Apply requires an 8-bit grayscale image or an RGB stack");
            imp.unlock();
            return;
        }
        int[] table = new int[256];
        int min = (int)ip.getMin();
        int max = (int)ip.getMax();
        for (int i=0; i<256; i++) {
            if (i<=min)
                table[i] = 0;
            else if (i>=max)
                table[i] = 255;
            else
                table[i] = (int)(((double)(i-min)/(max-min))*255);
        }
        if (imp.getStackSize()>1) {
            ImageStack stack = imp.getStack();
            YesNoCancelDialog d = new YesNoCancelDialog(this,
                "Entire Stack?", "Apply LUT to all "+stack.getSize()+" slices in the stack?");
            if (d.cancelPressed())
                {imp.unlock(); return;}
            if (d.yesPressed())
                new StackProcessor(stack, ip).applyTable(table);
            else
                ip.applyTable(table);
        } else
            ip.applyTable(table);
        reset(imp, ip);
        imp.changes = true;
        imp.unlock();
    }

    void applyRGBStack(ImagePlus imp) {
        int current = imp.getCurrentSlice();
        int n = imp.getStackSize();
        if (!IJ.showMessageWithCancel("Update Entire Stack?",
        "Apply brightness and contrast settings\n"+
        "to all "+n+" slices in the stack?\n \n"+
        "NOTE: There is no Undo for this operation."))
            return;
        for (int i=1; i<=n; i++) {
            if (i!=current) {
                imp.setSlice(i);
                ImageProcessor ip = imp.getProcessor();
                setMinAndMax(ip, min, max);
                IJ.showProgress((double)i/n);
            }
        }
        imp.setSlice(current);
        imp.changes = true;
    }

    void threshold(ImagePlus imp, ImageProcessor ip) {
        int threshold = (int)((defaultMax-defaultMin)/2.0);
        min = threshold;
        max = threshold;
        setMinAndMax(ip, min, max);
        setThreshold(ip);
        updateScrollBars(null);
    }

    void setThreshold(ImageProcessor ip) {
        if (!(ip instanceof ByteProcessor))
            return;
        if (((ByteProcessor)ip).isInvertedLut())
            ip.setThreshold(max, 255, ImageProcessor.NO_LUT_UPDATE);
        else
            ip.setThreshold(0, max, ImageProcessor.NO_LUT_UPDATE);
    }

    void autoAdjust(ImagePlus imp, ImageProcessor ip) {
        if (RGBImage)
            ip.reset();
        Calibration cal = imp.getCalibration();
        imp.setCalibration(null);
        ImageStatistics stats = imp.getStatistics(); // get uncalibrated stats
        imp.setCalibration(cal);
        int[] histogram = stats.histogram;
        if (autoThreshold<10)
            autoThreshold = AUTO_THRESHOLD;
        else
            autoThreshold /= 2;
        int threshold = stats.pixelCount/autoThreshold;
        int i = -1;
        boolean found = false;
        do {
            i++;
            found = histogram[i] > threshold;
        } while (!found && i<255);
        int hmin = i;
        i = 256;
        do {
            i--;
            found = histogram[i] > threshold;
        } while (!found && i>0);
        int hmax = i;
        if (hmax>=hmin) {
            imp.killRoi();
            min = stats.histMin+hmin*stats.binSize;
            max = stats.histMin+hmax*stats.binSize;
            if (min==max)
                {min=stats.min; max=stats.max;}
            setMinAndMax(ip, min, max);
        } else {
            reset(imp, ip);
            return;
        }
        updateScrollBars(null);
    }
    
    void setMinAndMax(ImagePlus imp, ImageProcessor ip) {
        min = ip.getMin();
        max = ip.getMax();
        Calibration cal = imp.getCalibration();
        int digits = (ip instanceof FloatProcessor)||cal.calibrated()?2:0;
        double minValue = cal.getCValue(min);
        double maxValue = cal.getCValue(max);
        GenericDialog gd = new GenericDialog("Set Min and Max");
        gd.addNumericField("Minimum Displayed Value: ", minValue, digits);
        gd.addNumericField("Maximum Displayed Value: ", maxValue, digits);
        gd.showDialog();
        if (gd.wasCanceled())
            return;
        minValue = gd.getNextNumber();
        maxValue = gd.getNextNumber();
        minValue = cal.getRawValue(minValue);
        maxValue = cal.getRawValue(maxValue);
        if (maxValue>=minValue) {
            min = minValue;
            max = maxValue;
            setMinAndMax(ip, min, max);
            updateScrollBars(null);
        }
    }

    void setWindowLevel(ImagePlus imp, ImageProcessor ip) {
        min = ip.getMin();
        max = ip.getMax();
        Calibration cal = imp.getCalibration();
        int digits = (ip instanceof FloatProcessor)||cal.calibrated()?2:0;
        double minValue = cal.getCValue(min);
        double maxValue = cal.getCValue(max);
        //IJ.log("setWindowLevel: "+min+" "+max);
        double windowValue = maxValue - minValue;
        double levelValue = minValue + windowValue/2.0;
        GenericDialog gd = new GenericDialog("Set W&L");
        gd.addNumericField("Window Center (Level): ", levelValue, digits);
        gd.addNumericField("Window Width: ", windowValue, digits);
        gd.showDialog();
        if (gd.wasCanceled())
            return;
        levelValue = gd.getNextNumber();
        windowValue = gd.getNextNumber();
        minValue = levelValue-(windowValue/2.0);
        maxValue = levelValue+(windowValue/2.0);
        minValue = cal.getRawValue(minValue);
        maxValue = cal.getRawValue(maxValue);
        if (maxValue>=minValue) {
            min = minValue;
            max = maxValue;
            setMinAndMax(ip, minValue, maxValue);
            updateScrollBars(null);
        }
    }

    static final int RESET=0, AUTO=1, SET=2, APPLY=3, THRESHOLD=4, MIN=5, MAX=6, 
        BRIGHTNESS=7, CONTRAST=8, UPDATE=9;

    // Separate thread that does the potentially time-consuming processing 
    public void run() {
		while (!done) {
			 synchronized(this) {
				 try {wait();}
				 catch(InterruptedException e) {}
			 }
			 doUpdate();
		 }
    }

    void doUpdate() {
        ImageProcessor ip;
        int action;
        int minvalue = minSliderValue;
        int maxvalue = maxSliderValue;
        int bvalue = brightnessValue;
        int cvalue = contrastValue;
        if (doReset) action = RESET;
        else if (doAutoAdjust) action = AUTO;
        else if (doSet) action = SET;
        else if (doApplyLut) action = APPLY;
        else if (doThreshold) action = THRESHOLD;
        else if (doUpdate) action = UPDATE;
        else if (minSliderValue>=0) action = MIN;
        else if (maxSliderValue>=0) action = MAX;
        else if (brightnessValue>=0) action = BRIGHTNESS;
        else if (contrastValue>=0) action = CONTRAST;
        else return;
        minSliderValue = maxSliderValue = brightnessValue = contrastValue = -1;
        doReset = doAutoAdjust = doSet = doApplyLut = doThreshold = doUpdate = false;
        //imp = WindowManager.getCurrentImage();
        if (imp==null) {
            IJ.beep();
            IJ.showStatus("No image");
            return;
        }
        if (action!=UPDATE)
            ip = setup(imp);
        else
            ip = imp.getProcessor();
        if (RGBImage && !imp.lock())
            {imp=null; return;}
        //IJ.write("setup: "+(imp==null?"null":imp.getTitle()));
        switch (action) {
            case RESET: reset(imp, ip); break;
            case AUTO: autoAdjust(imp, ip); break;
            case SET: if (windowLevel) setWindowLevel(imp, ip); else setMinAndMax(imp, ip); break;
            case APPLY: apply(imp, ip); break;
            case THRESHOLD: threshold(imp, ip); break;
            case UPDATE: update(imp, ip); break;
            case MIN: adjustMin(imp, ip, minvalue); break;
            case MAX: adjustMax(imp, ip, maxvalue); break;
            case BRIGHTNESS: adjustBrightness(imp, ip, bvalue); break;
            case CONTRAST: adjustContrast(imp, ip, cvalue); break;
        }
        updatePlot();
        updateLabels(imp, ip);
        imp.updateAndDraw();
        if (RGBImage)
            imp.unlock();
    }

    public void windowClosing(WindowEvent e) {
		if (e.getSource()==this) {
			close();
		}
    }

    /** Overrides close() in PlugInFrame. */
    public void close() {
	   done = true;
	   synchronized(this) {
		   notify();
	   }
	   thread= null;
	   adjuster=null;
	   setVisible(false);
	   dispose();
	   

    }
		public void windowOpened(WindowEvent e) {}
		public void windowClosed(WindowEvent e) { instance = false;}
		public void windowIconified(WindowEvent e) {}
		public void windowDeiconified(WindowEvent e) {}
		public void windowDeactivated(WindowEvent e) {}

    public void windowActivated(WindowEvent e) {
        
		if (WindowManager.getCurrentWindow()!=null){
			imp = WindowManager.getCurrentWindow().getImagePlus(); 	
		} 
        setup();
    }

 
} // ContrastAdjuster class


class ContrastPlot extends Canvas implements MouseListener {
    
    static final int WIDTH = 128, HEIGHT=64;
    double defaultMin = 0;
    double defaultMax = 255;
    double min = 0;
    double max = 255;
    int[] histogram;
    int hmax;
    java.awt.Image os;
    Graphics osg;
    
    public ContrastPlot() {
        addMouseListener(this);
        setSize(WIDTH+1, HEIGHT+1);
    }

    /** Overrides Component getPreferredSize(). Added to work 
        around a bug in Java 1.4.1 on Mac OS X.*/
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH+1, HEIGHT+1);
    }

    void setHistogram(ImageStatistics stats) {
        histogram = stats.histogram;
        if (histogram.length!=256)
            {histogram=null; return;}
        for (int i=0; i<128; i++)
            histogram[i] = (histogram[2*i]+histogram[2*i+1])/2;
        int maxCount = 0;
        int mode = 0;
        for (int i=0; i<128; i++) {
            if (histogram[i]>maxCount) {
                maxCount = histogram[i];
                mode = i;
            }
        }
        int maxCount2 = 0;
        for (int i=0; i<128; i++) {
            if ((histogram[i]>maxCount2) && (i!=mode))
                maxCount2 = histogram[i];
        }
        hmax = stats.maxCount;
        if ((hmax>(maxCount2*2)) && (maxCount2!=0)) {
            hmax = (int)(maxCount2*1.5);
            histogram[mode] = hmax;
        }
        os = null;
    }

    public void update(Graphics g) {
        paint(g);
    }

    public void paint(Graphics g) {
        int x1, y1, x2, y2;
        double scale = (double)WIDTH/(defaultMax-defaultMin);
        double slope = 0.0;
        if (max!=min)
            slope = HEIGHT/(max-min);
        if (min>=defaultMin) {
            x1 = (int)(scale*(min-defaultMin));
            y1 = HEIGHT;
        } else {
            x1 = 0;
            if (max>min)
                y1 = HEIGHT-(int)((defaultMin-min)*slope);
            else
                y1 = HEIGHT;
        }
        if (max<=defaultMax) {
            x2 = (int)(scale*(max-defaultMin));
            y2 = 0;
        } else {
            x2 = WIDTH;
            if (max>min)
                y2 = HEIGHT-(int)((defaultMax-min)*slope);
            else
                y2 = 0;
        }
        if (histogram!=null) {
            if (os==null) {
                os = createImage(WIDTH,HEIGHT);
                osg = os.getGraphics();
                osg.setColor(Color.white);
                osg.fillRect(0, 0, WIDTH, HEIGHT);
                osg.setColor(Color.gray);
                for (int i = 0; i < WIDTH; i++)
                    osg.drawLine(i, HEIGHT, i, HEIGHT - ((int)(HEIGHT * histogram[i])/hmax));
                osg.dispose();
            }
            g.drawImage(os, 0, 0, this);
        } else {
            g.setColor(Color.white);
            g.fillRect(0, 0, WIDTH, HEIGHT);
        }
        g.setColor(Color.black);
        g.drawLine(x1, y1, x2, y2);
        g.drawLine(x2, HEIGHT-5, x2, HEIGHT);
        g.drawRect(0, 0, WIDTH, HEIGHT);
     }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}

} // ContrastPlot class


class TrimmedLabel extends Label {
    int trim = IJ.isMacOSX() ?0:6;

    public TrimmedLabel(String title) {
        super(title);
    }

    public Dimension getMinimumSize() {
        return new Dimension(super.getMinimumSize().width, super.getMinimumSize().height-trim);
    }

    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

} // TrimmedLabel class


  
=======
/*
 * Copyright (c) 2012, Metron, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Metron, Inc. nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL METRON, INC. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.metsci.glimpse.plot.timeline.event;

import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Center;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.End;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Icon;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Label;
import static com.metsci.glimpse.plot.timeline.data.EventSelection.Location.Start;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import com.metsci.glimpse.axis.Axis1D;
import com.metsci.glimpse.axis.tagged.TaggedAxis1D;
import com.metsci.glimpse.event.mouse.GlimpseMouseEvent;
import com.metsci.glimpse.plot.timeline.data.Epoch;
import com.metsci.glimpse.plot.timeline.data.EventSelection;
import com.metsci.glimpse.plot.timeline.data.EventSelection.Location;
import com.metsci.glimpse.util.units.time.TimeStamp;

/**
 * Helper class which maintains sorted Event data structures for {@code EventPlotInfo}.
 * 
 * @author ulman
 */
public class EventManager
{
    protected static final double BUFFER_MULTIPLIER = 2;
    protected static final double OVERLAP_HEURISTIC = 20.0;
    protected static final int PICK_BUFFER_PIXELS = 10;

    protected EventPlotInfo info;
    protected ReentrantLock lock;

    protected Map<Object, Event> eventMap;
    protected Map<Object, Row> rowMap;
    protected List<Row> rows;

    protected boolean aggregateNearbyEvents = false;
    protected int maxAggregateSize = 30;
    protected int maxAggregateGap = 5;

    protected boolean shouldStack = true;
    protected boolean isHorizontal = true;

    protected boolean visibleEventsDirty = true;
    protected double prevMin;
    protected double prevMax;

    protected class Row
    {
        int index;

        // all Events in the Row
        EventIntervalQuadTree events;

        // all visible Events in the Row (some Events may be aggregated)
        // will not be filled in if aggregation is not turned on (in that
        // case it is unneeded because the events map can be queried instead)
        EventIntervalQuadTree visibleAggregateEvents;

        // all visible Events (including aggregated events, if turned on)
        // sorted by starting timestamp
        List<Event> visibleEvents;

        public Row( int index )
        {
            this.index = index;
            this.visibleAggregateEvents = new EventIntervalQuadTree( );
            this.events = new EventIntervalQuadTree( );
        }

        public void addEvent( Event event )
        {
            this.events.add( event );
            rowMap.put( event.getId( ), this );
        }

        public void removeEvent( Event event )
        {
            this.events.remove( event );
            rowMap.remove( event.getId( ) );
        }

        public void calculateVisibleEvents( Axis1D axis, TimeStamp min, TimeStamp max )
        {
            if ( aggregateNearbyEvents )
            {
                calculateVisibleEventsAggregated( axis, min, max );
            }
            else
            {
                calculateVisibleEventsNormal( min, max );
            }
        }

        public void calculateVisibleEventsAggregated( Axis1D axis, TimeStamp min, TimeStamp max )
        {
            // calculate size of bin in system (time) units
            double ppv = axis.getPixelsPerValue( );
            double maxDuration = maxAggregateSize / ppv;
            double maxGap = maxAggregateGap / ppv;

            // expand the visible window slightly
            // since we only aggregate visible Events, we don't want weird
            // visual artifacts (aggregate groups appearing and disappearing)
            // as Events scroll off the screen
            TimeStamp expandedMin = min.subtract( maxDuration * BUFFER_MULTIPLIER );
            TimeStamp expandedMax = max.add( maxDuration * BUFFER_MULTIPLIER );

            List<Event> visible = calculateVisibleEventsNormal0( events, expandedMin, expandedMax );

            EventIntervalQuadTree events = new EventIntervalQuadTree( );

            Set<Event> children = new HashSet<Event>( );
            TimeStamp childrenMin = null;
            TimeStamp childrenMax = null;
            for ( Event event : visible )
            {
                // only aggregate small events
                boolean isDurationSmall = event.getDuration( ) < maxDuration;

                // only aggregate events with small gaps between them
                double gap = childrenMax == null ? 0 : childrenMax.durationBefore( event.getStartTime( ) );
                boolean isGapSmall = gap < maxGap;

                // if the gap is large, end the current aggregate group
                if ( !isGapSmall )
                {
                    addAggregateEvent( events, children, childrenMin, childrenMax, min, max );
                    children.clear( );
                    childrenMin = null;
                    childrenMax = null;
                }

                // if the event is small enough to be aggregated, add it to the child list
                if ( isDurationSmall )
                {
                    children.add( event );

                    // events are in start time order, so this will never change after being set
                    if ( childrenMin == null ) childrenMin = event.getStartTime( );

                    if ( childrenMax == null || childrenMax.isBefore( event.getEndTime( ) ) ) childrenMax = event.getEndTime( );
                }
                // otherwise just add it to the result map
                else
                {
                    if ( isVisible( event, min, max ) ) events.add( event );
                }
            }

            // add any remaining child events
            addAggregateEvent( events, children, childrenMin, childrenMax, min, max );

            this.visibleAggregateEvents = events;
            this.visibleEvents = calculateVisibleEventsNormal0( events.getAll( ) );
        }

        protected void addAggregateEvent( EventIntervalQuadTree events, Set<Event> children, TimeStamp childrenMin, TimeStamp childrenMax, TimeStamp min, TimeStamp max )
        {
            // if there is only one or zero events in the current group, just add a regular event
            if ( children.size( ) <= 1 )
            {
                for ( Event child : children )
                    if ( isVisible( child, min, max ) ) events.add( child );
            }
            // otherwise create an aggregate group and add it to the result map
            else
            {
                AggregateEvent aggregate = new AggregateEvent( children, childrenMin, childrenMax );

                if ( isVisible( aggregate, min, max ) ) events.add( aggregate );
            }
        }

        protected boolean isVisible( Event event, TimeStamp min, TimeStamp max )
        {
            return ! ( event.getEndTime( ).isBefore( min ) || event.getStartTime( ).isAfter( max ) );
        }

        protected List<Event> calculateVisibleEventsNormal0( EventIntervalQuadTree events, TimeStamp min, TimeStamp max )
        {
            return calculateVisibleEventsNormal0( events.get( min, true, max, true ) );
        }

        protected List<Event> calculateVisibleEventsNormal0( Collection<Event> visible )
        {
            ArrayList<Event> visible_start_sorted = new ArrayList<Event>( visible.size( ) );
            visible_start_sorted.addAll( visible );
            Collections.sort( visible_start_sorted, Event.getStartTimeComparator( ) );
            return visible_start_sorted;
        }

        public void calculateVisibleEventsNormal( TimeStamp min, TimeStamp max )
        {
            this.visibleEvents = calculateVisibleEventsNormal0( this.events, min, max );
        }

        public Collection<Event> getOverlappingEvents( Event event )
        {
            return this.events.get( event.getStartTime( ), false, event.getEndTime( ), false );
        }

        public Collection<Event> getNearestVisibleEvents( TimeStamp timeStart, TimeStamp timeEnd )
        {
            if ( aggregateNearbyEvents )
            {
                return this.visibleAggregateEvents.get( timeStart, timeEnd );
            }
            else
            {
                return this.events.get( timeStart, timeEnd );
            }
        }

        public boolean isEmpty( )
        {
            return this.events.isEmpty( );
        }

        public int size( )
        {
            return this.events.size( );
        }

        public int getIndex( )
        {
            return this.index;
        }

        public void setIndex( int index )
        {
            this.index = index;
        }
    }

    public EventManager( EventPlotInfo info )
    {
        this.info = info;

        this.lock = info.getStackedPlot( ).getLock( );

        this.rows = new ArrayList<Row>( );
        this.eventMap = new HashMap<Object, Event>( );
        this.rowMap = new HashMap<Object, Row>( );

        this.isHorizontal = info.getStackedTimePlot( ).isTimeAxisHorizontal( );
    }

    public void lock( )
    {
        this.lock.lock( );
    }

    public void unlock( )
    {
        this.lock.unlock( );
    }

    public List<Row> getRows( )
    {
        return Collections.unmodifiableList( rows );
    }

    /**
     * @see #setStackOverlappingEvents(boolean)
     */
    public boolean isStackOverlappingEvents( )
    {
        return this.shouldStack;
    }

    /**
     * If true, Events will be automatically placed into rows in order to
     * avoid overlap. Any row requested by {@link Event#setFixedRow(int)} will
     * be ignored.
     */
    public void setStackOverlappingEvents( boolean stack )
    {
        this.shouldStack = stack;
        this.validate( );
    }

    /**
     * @see #setMaxAggregatedGroupSize(int)
     */
    public int getMaxAggregatedGroupSize( )
    {
        return this.maxAggregateSize;
    }

    /**
     * Sets the maximum pixel size above which an Event will not be aggregated
     * with nearby Events (in order to reduce visual clutter).
     * 
     * @see #setAggregateNearbyEvents(boolean)
     */
    public void setMaxAggregatedGroupSize( int size )
    {
        this.maxAggregateSize = size;
        this.validate( );
    }

    public int getMaxAggregatedEventGapSize( )
    {
        return this.maxAggregateGap;
    }

    /**
     * Sets the maximum pixel distance between adjacent events above which
     * events will not be aggregated into a single Event (in order to reduce
     * visual clutter).
     * 
     * @param size
     */
    public void setMaxAggregatedEventGapSize( int size )
    {
        this.maxAggregateGap = size;
        this.validate( );
    }

    /**
     * @see #setAggregateNearbyEvents(boolean)
     */
    public boolean isAggregateNearbyEvents( )
    {
        return this.aggregateNearbyEvents;
    }

    /**
     * If true, nearby events in the same row will be combined into one
     * event to reduce visual clutter.
     */
    public void setAggregateNearbyEvents( boolean aggregate )
    {
        this.aggregateNearbyEvents = aggregate;
        this.validate( );
    }

    public void validate( )
    {
        lock.lock( );
        try
        {
            this.rebuildRows0( );
            this.visibleEventsDirty = true;
            this.info.updateSize( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public int getRowCount( )
    {
        lock.lock( );
        try
        {
            return Math.max( 1, this.rows.size( ) );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void setRow( Object eventId, int rowIndex )
    {
        lock.lock( );
        try
        {
            Event event = getEvent( eventId );
            if ( event == null ) return;

            int oldRowIndex = getRow( eventId );
            Row oldRow = rows.get( oldRowIndex );
            if ( oldRow != null ) oldRow.removeEvent( event );

            ensureRows0( rowIndex );
            Row newRow = rows.get( rowIndex );
            newRow.addEvent( event );

            // row was set manually so don't automatically
            // adjust the other rows to avoid overlap

            this.visibleEventsDirty = true;
            this.info.updateSize( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public int getRow( Object eventId )
    {
        lock.lock( );
        try
        {
            Row row = rowMap.get( eventId );
            if ( row != null )
            {
                return row.getIndex( );
            }
            else
            {
                return 0;
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void addEvent( Event event )
    {
        if ( event == null ) return;

        lock.lock( );
        try
        {
            // remove the event if it already exists
            this.removeEvent( event.getId( ) );

            this.eventMap.put( event.getId( ), event );
            this.addEvent0( event );
            this.visibleEventsDirty = true;
            this.info.updateSize( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public Event removeEvent( Object id )
    {
        lock.lock( );
        try
        {
            Event event = this.eventMap.remove( id );

            if ( event != null )
            {
                this.removeEvent0( event );
                this.visibleEventsDirty = true;
                this.info.updateSize( );
            }

            return event;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void removeAllEvents( )
    {
        lock.lock( );
        try
        {
            for ( Event event : this.eventMap.values( ) )
            {
                event.setEventPlotInfo( null );
            }

            this.eventMap.clear( );
            this.rowMap.clear( );
            this.rows.clear( );

            this.visibleEventsDirty = true;
            this.info.updateSize( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public void moveEvent( Event event, TimeStamp newStartTime, TimeStamp newEndTime )
    {
        lock.lock( );
        try
        {

            Event eventOld = Event.createDummyEvent( event );

            Row oldRow = rowMap.get( event.getId( ) );
            if ( oldRow == null ) return;

            if ( event.isFixedRow( ) )
            {
                // update the event times (its row will stay the same)
                // remove and add it to update start/end time indexes
                oldRow.removeEvent( event );
                event.setTimes0( newStartTime, newEndTime );
                oldRow.addEvent( event );

                // displace the events this event has shifted on to
                displaceEvents0( oldRow, event );
            }
            else
            {
                // remove the event from its old row
                oldRow.removeEvent( event );

                // update the event times
                event.setTimes0( newStartTime, newEndTime );

                // add the moved version of the event back in
                // (which might land it on a different row if it
                //  has been moved over top of another event)
                addEvent0( event );
            }

            // now shift events to fill the space left by moving the event
            shiftEvents0( eventOld, oldRow );
            clearEmptyRows0( );

            this.visibleEventsDirty = true;
            this.info.updateSize( );

        }
        finally
        {
            lock.unlock( );
        }
    }

    public Set<Event> getEvents( )
    {
        lock.lock( );
        try
        {
            return Collections.unmodifiableSet( new HashSet<Event>( this.eventMap.values( ) ) );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public Event getEvent( Object id )
    {
        lock.lock( );
        try
        {
            return this.eventMap.get( id );
        }
        finally
        {
            lock.unlock( );
        }
    }

    public Set<EventSelection> getNearestEvents( GlimpseMouseEvent e )
    {
        lock.lock( );
        try
        {
            Row row = getNearestRow( e );

            if ( row != null )
            {
                Axis1D axis = e.getAxis1D( );
                double value = isHorizontal ? e.getAxisCoordinatesX( ) : e.getAxisCoordinatesY( );
                double buffer = PICK_BUFFER_PIXELS / axis.getPixelsPerValue( );

                Epoch epoch = info.getStackedTimePlot( ).getEpoch( );

                TimeStamp time = epoch.toTimeStamp( value );
                TimeStamp timeStart = epoch.toTimeStamp( value - buffer );
                TimeStamp timeEnd = epoch.toTimeStamp( value + buffer );

                Collection<Event> events = row.getNearestVisibleEvents( timeStart, timeEnd );
                Set<EventSelection> eventSelections = createEventSelection( axis, events, time );
                return eventSelections;
            }

            return Collections.emptySet( );
        }
        finally
        {
            lock.unlock( );
        }
    }

    // find the event which minimizes: abs(clickPos-eventEnd)+abs(clickPos-eventStart)
    // this is a heuristic for the single event "closest" to the click position
    // we don't want to require that the click be inside the event because we want
    // to make selection of instantaneous events possible
    // (but if the click *is* inside an event, it gets priority)
    public EventSelection getNearestEvent( Set<EventSelection> events, GlimpseMouseEvent e )
    {
        lock.lock( );
        try
        {
            Epoch epoch = info.getStackedTimePlot( ).getEpoch( );
            double value = isHorizontal ? e.getAxisCoordinatesX( ) : e.getAxisCoordinatesY( );
            TimeStamp time = epoch.toTimeStamp( value );

            double bestDist = Double.MAX_VALUE;
            EventSelection bestEvent = null;

            for ( EventSelection s : events )
            {
                Event event = s.getEvent( );

                if ( event.contains( time ) )
                {
                    return s;
                }
                else
                {
                    double dist = distance0( event, time );
                    if ( bestEvent == null || dist < bestDist )
                    {
                        bestDist = dist;
                        bestEvent = s;
                    }
                }
            }

            return bestEvent;
        }
        finally
        {
            lock.unlock( );
        }
    }

    public EventSelection getNearestEvent( GlimpseMouseEvent e )
    {
        return getNearestEvent( getNearestEvents( e ), e );
    }

    // heuristic distance measure for use in getNearestEvent( )
    protected double distance0( Event event, TimeStamp time )
    {
        double startDiff = Math.abs( time.durationAfter( event.getStartTime( ) ) );
        double endDiff = Math.abs( time.durationAfter( event.getEndTime( ) ) );
        return Math.min( startDiff, endDiff );
    }

    // must be called while holding lock
    protected Row getNearestRow( GlimpseMouseEvent e )
    {
        int value = isHorizontal ? e.getY( ) : e.getTargetStack( ).getBounds( ).getWidth( ) - e.getX( );

        int rowIndex = ( int ) Math.floor( value / ( double ) ( info.getRowSize( ) + info.getEventPadding( ) ) );
        rowIndex = info.getRowCount( ) - 1 - rowIndex;

        if ( rowIndex >= 0 && rowIndex < rows.size( ) )
        {
            return rows.get( rowIndex );
        }

        return null;
    }

    public void calculateVisibleEvents( Axis1D axis )
    {
        lock.lock( );
        try
        {
            if ( visibleEventsDirty || axis.getMin( ) != prevMin || axis.getMax( ) != prevMax )
            {
                calculateVisibleEvents( axis.getMin( ), axis.getMax( ) );
            }
        }
        finally
        {
            lock.unlock( );
        }
    }

    // must be called while holding lock
    private Set<EventSelection> createEventSelection( Axis1D axis, Collection<Event> events, TimeStamp clickTime )
    {
        Set<EventSelection> set = new HashSet<EventSelection>( );

        for ( Event event : events )
        {
            set.add( createEventSelection( axis, event, clickTime ) );
        }

        return set;
    }

    // must be called while holding lock
    private EventSelection createEventSelection( Axis1D axis, Event event, TimeStamp t )
    {
        double buffer = PICK_BUFFER_PIXELS / axis.getPixelsPerValue( );

        TimeStamp t1 = t.subtract( buffer );
        TimeStamp t2 = t.add( buffer );

        TimeStamp e1 = event.getStartTime( );
        TimeStamp e2 = event.getEndTime( );

        EnumSet<Location> locations = EnumSet.noneOf( Location.class );

        boolean start = t2.isAfterOrEquals( e1 ) && t1.isBeforeOrEquals( e1 );
        boolean end = t2.isAfterOrEquals( e2 ) && t1.isBeforeOrEquals( e2 );

        TimeStamp i1 = event.getIconStartTime( );
        TimeStamp i2 = event.getIconEndTime( );
        boolean icon = event.isIconVisible( ) && i1 != null && i2 != null && t.isAfterOrEquals( i1 ) && t.isBeforeOrEquals( i2 );

        TimeStamp l1 = event.getLabelStartTime( );
        TimeStamp l2 = event.getLabelEndTime( );
        boolean text = event.isLabelVisible( ) && l1 != null && l2 != null && t.isAfterOrEquals( l1 ) && t.isBeforeOrEquals( l2 );

        if ( text ) locations.add( Label );
        if ( icon ) locations.add( Icon );
        if ( start ) locations.add( Start );
        if ( end ) locations.add( End );
        if ( ( !start && !end ) || ( start && end ) ) locations.add( Center );

        return new EventSelection( event, locations );
    }

    // must be called while holding lock
    private void rebuildRows0( )
    {
        rows.clear( );
        rowMap.clear( );

        for ( Event event : eventMap.values( ) )
        {
            addEvent0( event );
        }
    }

    // must be called while holding lock
    private void ensureRows0( int requestedIndex )
    {
        int currentRowCount = rows.size( );
        while ( requestedIndex >= currentRowCount )
        {
            rows.add( new Row( currentRowCount++ ) );
        }
    }

    // must be called while holding lock
    private void displaceEvents0( Row row, Event event )
    {
        Set<Event> overlapEvents = new HashSet<Event>( row.getOverlappingEvents( event ) );
        for ( Event overlapEvent : overlapEvents )
        {
            displaceEvent0( overlapEvent );
        }
    }

    // must be called while holding lock
    // move an event which has been overlapped by another event
    private void displaceEvent0( Event oldEvent )
    {
        if ( !oldEvent.isFixedRow( ) )
        {
            Row oldRow = rowMap.get( oldEvent.getId( ) );
            oldRow.removeEvent( oldEvent );
            addEvent0( oldEvent );
            shiftEvents0( oldEvent, oldRow );
        }
        else
        {
            // if the displaced event requested the row it is in, don't move it
        }
    }

    // must be called while holding lock
    private void clearEmptyRows0( )
    {
        // clear empty rows until we find a non-empty one
        for ( int i = rows.size( ) - 1; i >= 0; i-- )
        {
            if ( rows.get( i ).isEmpty( ) )
            {
                rows.remove( i );
            }
            else
            {
                break;
            }
        }
    }

    // must be called while holding lock
    private void removeEvent0( Event event )
    {
        // remove the event then determine if other events should be
        // shifted down to fill its place
        eventMap.remove( event.getId( ) );

        Row row = rowMap.remove( event.getId( ) );
        if ( row == null ) return;
        row.removeEvent( event );

        shiftEvents0( event, row );
        clearEmptyRows0( );
    }

    // must be called while holding lock
    private void shiftEvents0( Event event, Row toRow )
    {
        // determine if the removal of this event allows others to shift down
        int size = rows.size( );
        for ( int i = size - 1; i > toRow.index; i-- )
        {
            Row fromRow = rows.get( i );

            // check to see if any of these candidates can be moved down to
            // fill the spot in toRow left by the deleted event
            HashSet<Event> events = new HashSet<Event>( fromRow.getOverlappingEvents( event ) );
            for ( Event e : events )
                moveEventIfRoom0( e, fromRow, toRow );
        }
    }

    // must be called while holding lock
    private void moveEventIfRoom0( Event event, Row fromRow, Row toRow )
    {
        // move the event if there is room for it and it hasn't explicitly requested its current row
        if ( !event.isFixedRow( ) && toRow.getOverlappingEvents( event ).isEmpty( ) )
        {
            fromRow.removeEvent( event );
            toRow.addEvent( event );
            shiftEvents0( event, fromRow );
        }
    }

    // must be called while holding lock
    private Row addEvent0( Event event )
    {
        Row row = null;
        if ( shouldStack && !event.isFixedRow( ) )
        {
            row = getRowWithLeastOverlaps( event );

            // put the event into the non-overlapping spot we've found for it
            row.addEvent( event );
        }
        else
        {
            // the requested row index must be less than the maximum row count and greater than or equal to 0
            int requestedRow = Math.min( Math.max( 0, event.getFixedRow( ) ), info.getRowMaxCount( ) - 1 );
            ensureRows0( requestedRow );
            row = rows.get( requestedRow );

            row.addEvent( event );

            // this spot might overlap with other events, move them out of the way
            if ( shouldStack )
            {
                displaceEvents0( row, event );
            }
        }

        return row;
    }

    // must be called while holding lock
    //
    // If plot.getMaxRowCount() is large, we'll always be able to simply
    // make a new row (which will have no overlaps. If we're constrained
    // regarding the number of rows we can create, we may have to accept
    // some overlaps.
    private Row getRowWithLeastOverlaps( Event event )
    {
        int size = rows.size( );
        int max = info.getRowMaxCount( );

        double leastTime = Double.POSITIVE_INFINITY;
        Row leastRow = null;

        for ( int i = 0; i < size; i++ )
        {
            Row candidate = rows.get( i );

            double overlapTime = getTotalOverlapTime( candidate, event );

            if ( overlapTime < leastTime )
            {
                leastTime = overlapTime;
                leastRow = candidate;
            }
        }

        // if we didn't find an empty row, and there's room to make
        // a new row, then make a new row, which will have 0 overlap
        if ( leastTime != 0.0 && size < max )
        {
            leastTime = 0;
            leastRow = new Row( size );
            rows.add( leastRow );
        }

        return leastRow;
    }

    // must be called while holding lock
    private double getTotalOverlapTime( Row candidate, Event event )
    {
        double totalOverlap = 0;

        //XXX Heuristic: we want overlaps with very small events (in the
        // limit we have 0 duration events) to count for something, so
        // we make the minimum time penalty for any overlap be 1/20th
        // of the total duration of either event
        double minOverlap1 = event.getDuration( ) / OVERLAP_HEURISTIC;

        Collection<Event> events = candidate.getOverlappingEvents( event );
        for ( Event overlapEvent : events )
        {
            double minOverlap = Math.max( minOverlap1, overlapEvent.getDuration( ) / OVERLAP_HEURISTIC );
            double overlap = event.getOverlapTime( overlapEvent );

            totalOverlap += Math.max( minOverlap, overlap );
        }

        return totalOverlap;
    }

    // must be called while holding lock
    private void calculateVisibleEvents( double min, double max )
    {
        Epoch epoch = info.getStackedTimePlot( ).getEpoch( );
        TaggedAxis1D axis = info.getStackedTimePlot( ).getTimeAxis( );

        for ( Row row : rows )
        {
            row.calculateVisibleEvents( axis, epoch.toTimeStamp( min ), epoch.toTimeStamp( max ) );
        }

        this.visibleEventsDirty = false;
    }
}

>>>>>>> 76aa07461566a5976980e6696204781271955163
