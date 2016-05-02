/*
                         PUBLIC DOMAIN NOTICE
                     NIH Chemical Genomics Center
         National Center for Advancing Translational Sciences

This software/database is a "United States Government Work" under the
terms of the United States Copyright Act.  It was written as part of
the author's official duties as United States Government employee and
thus cannot be copyrighted.  This software/database is freely
available to the public for use. The NIH Chemical Genomics Center
(NCGC) and the U.S. Government have not placed any restriction on its
use or reproduction. 

Although all reasonable efforts have been taken to ensure the accuracy
and reliability of the software and data, the NCGC and the U.S.
Government do not and cannot warrant the performance or results that
may be obtained by using this software or data. The NCGC and the U.S.
Government disclaim all warranties, express or implied, including
warranties of performance, merchantability or fitness for any
particular purpose.

Please cite the authors in any work or product based on this material.

*/
package tripod.ui.kinome;

import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import javax.imageio.*;

import net.sf.ehcache.Element;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

public class KinomePanel extends JComponent 
    implements MouseMotionListener, MouseListener {

    private static final Logger logger = 
	Logger.getLogger(KinomePanel.class.getName());

    private static final int PADDING = 15;
    private static final String KINOME_IMAGE_CACHE = "KinomeImageCache";

    public static final String PROP_DATASET_ADDED = "DatasetAdded";
    public static final String PROP_DATASET_VISIBLE = "DatasetVisible";
    public static final String PROP_CLEAR = "Clear";


    final static BufferedImage KinomeImage = KinomeData.KinomeImage;
    static final Color DEFAULT_BORDER = new Color (100, 150, 250);
    static final Color DEFAULT_HOVER_COLOR = DEFAULT_BORDER;
    static final BasicStroke DEFAULT_HOVER_STROKE = new BasicStroke (3.f);

    static class KinaseRenderer {
	String kinase;
        Rectangle2D bounds;
        Rectangle bbox; // original bounding box
        Shape shape = new Ellipse2D.Float(0.f, 0.f, 10.f, 10.f);
        Color color = Color.gray;
        Stroke stroke = new BasicStroke (2.f);
        boolean selected = false;

	KinaseRenderer (String kinase, Rectangle bbox) {
	    this.kinase = escapeGreek (kinase);
            this.bbox = bbox;

            // transformed coordinates
            bounds = new Rectangle2D.Double();
	    bounds.setRect(getShape().getBounds2D());            
	}

        public void setSelected (boolean selected) { 
            this.selected = selected; 
        }
        public boolean isSelected () { return selected; }

        public void setShape (Shape shape) { this.shape = shape; }
        public void setColor (Color color) { this.color = color; }
        public void setStroke (Stroke stroke) { this.stroke = stroke; }

	public Rectangle2D getBounds () { return bounds; }
        public Rectangle getBbox () { return bbox; }

	public Shape getShape () { return shape; }
	public Color getColor () { return color; }
        public Stroke getStroke () { return stroke; }

	public boolean contains (Point pt) {
	    return bounds.contains(pt.x, pt.y);
	}
        public String getKinase () { return kinase; }
    }

    class KinaseAnnotation implements KinomeItemRenderer, 
				      Comparable<KinaseAnnotation> {
	String kinase;
	KinomeDataset kds;
	KinomeRendererFactory factory;
	KinomeItemRenderer kir;
	Rectangle2D bounds; // in current coordinate system
	String text;

	KinaseAnnotation (String kinase, KinomeDataset kds) {
	    this.kinase = kinase;
	    this.kds = kds;
	    this.factory = rendererFactory.get(kds);
	    this.kir = factory.getRenderer(kinase, getValue ());
	    this.bounds = new Rectangle2D.Double();
	    bounds.setRect(getShape().getBounds2D());
	    text = escapeGreek (kinase)+": "+ getValue ();
	    if (kds.getName() != null) {
		text = kds.getName() + ", " +text;
	    }
            //logger.info("Rendering "+text);
	}
	public Number getValue () { return kds.getValue(kinase); }
	public Rectangle2D getBounds () { return bounds; }
	public Shape getShape () { return kir.getShape(); }
	public Color getColor () { return kir.getColor(); }

	/* natural ordering allows the annotation to be rendered
	 * small values (bigger shape) first, then larger values (smaller
	 * shape) ontop
	 */
	public int compareTo (KinaseAnnotation ka) {
	    double d = getValue().doubleValue() - ka.getValue().doubleValue();
	    if (d < 0.) return -1;
	    else if (d > 0.) return 1;
	    return kinase.compareTo(ka.kinase);
	}
	public boolean contains (Point pt) {
	    return bounds.contains(pt.x, pt.y);
	}
	public String getText () { return text; }
	public String toString () { return getText (); }

	KinomeDataset getDataset () { return kds; }
	KinomeItemRenderer getRenderer () { return kir; }
    }

    static Map<String, String> GREEK = new TreeMap<String, String>();
    static {
	GREEK.put("_ALPHA", "\u03B1");
	GREEK.put("_BETA", "\u03B2");
	GREEK.put("_GAMMA", "\u03B3");
	GREEK.put("_DELTA", "\u03B4");
	GREEK.put("_EPSILON", "\u03B5");
	GREEK.put("_ZETA", "\u03B6");
	GREEK.put("_ETA", "\u03B7");
	GREEK.put("_THETA", "\u03B8");
	GREEK.put("_IOTA", "\u03B9");
	GREEK.put("_KAPPA", "\u03BA");
	GREEK.put("_LAMDA", "\u03BB");
	GREEK.put("_MU", "\u03BC");
	GREEK.put("_NU", "\u03BD");
	GREEK.put("_XI", "\u03BE");
	GREEK.put("_OMICRON", "\u03BF");
	GREEK.put("_PI", "\u03C0");
	GREEK.put("_RHO", "\u03C1");
	GREEK.put("_SIGMA", "\u03C3");
	GREEK.put("_TAU", "\u03C4");
	GREEK.put("_UPSILON", "\u03C5");
	GREEK.put("_PHI", "\u03C6");
	GREEK.put("_CHI", "\u03C7");
	GREEK.put("_PSI", "\u03C8");
	GREEK.put("_OMEGA", "\u03C9");
    }

    static public String escapeGreek (String s) {
	int pos = s.indexOf("_");
	if (pos >= 0) {
	    String sub = s.substring(0, pos);
	    s = s.toUpperCase();
	    for (Map.Entry<String, String> me : GREEK.entrySet()) {
		s = s.replaceAll(me.getKey(), me.getValue());
	    }
	    s = sub + s.substring(pos);
	}
	return s;
    }

    static {
        try {
            // stop the evil new version check!
            System.setProperty("net.sf.ehcache.skipUpdateCheck", "true");
        }
        catch (Exception ex) {}

        Cache cache = new Cache
            (KINOME_IMAGE_CACHE, 10, false, false, 120, 120);
        CacheManager.getInstance().addCache(cache);
    }

	
    public static Image getKinomeImage (int iw, int ih) {
        String key = String.valueOf(iw)+"x"+String.valueOf(ih);

        Cache cache = CacheManager.getInstance().getCache(KINOME_IMAGE_CACHE);
	Element value = cache.get(key);

        Image image;
	if (value == null) {
            image = KinomeImage.getScaledInstance
		(iw, ih, /*Image.SCALE_AREA_AVERAGING*/Image.SCALE_SMOOTH);
	    cache.put(new Element (key, image));
	}
        else {
            image = (Image)value.getObjectValue();
        }

	return image;
    }

    private Map<KinomeDataset, KinomeRendererFactory> rendererFactory 
	= new HashMap<KinomeDataset, KinomeRendererFactory>();
    protected java.util.List<KinomeDataset> datasets = 
	new ArrayList<KinomeDataset>();

    // default renderer if none is given for a dataset
    private KinomeRendererFactory defaultRenderer = 
	new DefaultKinomeRendererFactory ();

    // current rendered kinome
    protected BufferedImage cachedImage = null;

    // mapping of kinase to the dataset and current coordinate system
    protected Set<KinaseAnnotation> annotations =
	new TreeSet<KinaseAnnotation>();

    // all kinases in the kinome
    protected java.util.List<KinaseRenderer> kinases = 
        new ArrayList<KinaseRenderer>();

    protected KinaseAnnotation hover = null;
    protected KinaseRenderer kinase = null;

    protected boolean paintBorder = true;
    protected boolean monochrome = false;
    protected boolean trackKinase = false;
    protected int padding = PADDING; // default padding

    // entries are KinomeDataset's names; if exists, then don't show
    //  the dataset
    protected Set<String> hidden = new HashSet<String>();

    // property events
    protected PropertyChangeSupport pcs = new PropertyChangeSupport (this);

    public KinomePanel () {
	this (true);
    }

    public KinomePanel (boolean paintBorder) {
	setOpaque (false);
	addMouseMotionListener (this);
        addMouseListener (this);

	this.paintBorder = paintBorder;

        for (Map.Entry<String, Rectangle> me : 
                 KinomeData.KinaseBox.entrySet()) {
            KinaseRenderer kr = new KinaseRenderer 
                (me.getKey(), me.getValue());
            kinases.add(kr);
        }
    }

    public void setPaintBorder (boolean paintBorder) {
	this.paintBorder = paintBorder;
	repaint ();
    }
    public boolean isPaintBorder () { return paintBorder; }

    public void setMonochrome (boolean monochrome) { 
	this.monochrome = monochrome;
	repaint ();
    }
    public boolean isMonochrome () { return monochrome; }

    public void setPadding (int padding) {
	this.padding = padding;
	invalidateAndRepaint ();
    }
    public int getPadding () {  return padding; }

    public void setVisible (String name, boolean visible) {
        boolean old = !hidden.contains(name);
        if (visible) hidden.remove(name);
        else hidden.add(name);
        invalidateAndRepaint ();
        pcs.firePropertyChange(new PropertyChangeEvent 
                               (this, PROP_DATASET_VISIBLE, old, visible));
    }
    public boolean isVisible (String name) {
        return !hidden.contains(name);
    }

    public void clear () {
	/*
	for (KinomeDataset kds : datasets) {
	    rendererFactory.remove(kds);
	}
	*/
	rendererFactory.clear();
	datasets.clear();
        invalidateAndRepaint ();
        pcs.firePropertyChange(new PropertyChangeEvent
                               (this, PROP_CLEAR, null, null));
    }

    public void clearLabels () {
        for (KinaseRenderer kr : kinases) {
            kr.setSelected(false);
        }
        repaint ();
    }

    public void add (KinomeDataset kds) {
	add (kds, null);
    }

    public void add (KinomeDataset kds, KinomeRendererFactory renderer) {
	if (renderer == null) {
	    renderer = defaultRenderer;
	}
	rendererFactory.put(kds, renderer);
	if (datasets.indexOf(kds) < 0) {
	    datasets.add(kds);
	}
	invalidateAndRepaint ();
        pcs.firePropertyChange(new PropertyChangeEvent 
                               // abusing this event
                               (this, PROP_DATASET_ADDED, kds, renderer));
    }

    public int count () { return datasets.size(); }
    public java.util.List<KinomeDataset> getDatasets () {
	return Collections.unmodifiableList(datasets);
    }

    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    void invalidateAndRepaint () {
	cachedImage = null; // invalidate
	repaint ();
    }

    @Override
    protected void paintComponent (Graphics g) {
	Graphics2D g2 = (Graphics2D)g;
	g2.setRenderingHint(RenderingHints.KEY_RENDERING, 
			    RenderingHints.VALUE_RENDER_QUALITY);
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			    RenderingHints.VALUE_ANTIALIAS_ON);
	Rectangle r = getBounds ();

	g2.setPaint(getBackground ());
	g2.setComposite(AlphaComposite.SrcAtop);

	int shadow = (int)(Math.min(r.width,r.height)*0.01+0.5);
	shadow = Math.max(shadow, 1);

	int round = Math.max(r.width/4, r.height/4);
	if (paintBorder) {
	    g2.fillRoundRect(0, 0, r.width, r.height, round, round);
	}
	else {
	    g2.fill(r);
	}

	if (cachedImage == null || cachedImage.getWidth() != r.width
	    || cachedImage.getHeight() != r.height) {
	    cachedImage = createBufferedImage (g2, r.width, r.height);
	}

	if (monochrome) {
	    g2.setComposite(AlphaComposite.Xor);
	}

	g2.drawImage(cachedImage, 0, 0, null);
	if (paintBorder) {
	    g2.setComposite(AlphaComposite.SrcAtop);
	    paintCellShadowBorder (g2, shadow/2, shadow/2, r.width-shadow, 
				   r.height-shadow, shadow);
	}

        g2.setPaint(Color.black);
        for (KinaseRenderer kr : kinases) {
            if (kr.isSelected()) {
                Rectangle2D bounds = kr.getBounds();
                g2.drawString(kr.getKinase(), 
                              (float)(bounds.getX()+bounds.getWidth()), 
                              (float)(bounds.getY()+bounds.getHeight()));
            }
        }

        if (kinase != null) {
	    g2.setPaint(kinase.getColor());
	    g2.setStroke(kinase.getStroke());
	    Rectangle2D bounds = kinase.getBounds();
	    g2.translate(bounds.getX(), bounds.getY());
	    g2.draw(kinase.getShape());
	    g2.translate(-bounds.getX(), -bounds.getY());
        }

	if (hover != null) {
	    g2.setPaint(DEFAULT_HOVER_COLOR);
	    g2.setStroke(DEFAULT_HOVER_STROKE);
	    Rectangle2D bounds = hover.getBounds();
	    g2.translate(bounds.getX(), bounds.getY());
	    g2.draw(hover.getShape());
	    g2.translate(-bounds.getX(), -bounds.getY());
	}
    }

    BufferedImage createBufferedImage (Graphics2D g, int width, int height) {
	GraphicsConfiguration gc = g.getDeviceConfiguration();
	BufferedImage image = gc.createCompatibleImage
	    (width, height, Transparency.TRANSLUCENT);
	Graphics2D g2 = image.createGraphics();
	g2.setRenderingHint(RenderingHints.KEY_RENDERING, 
			    RenderingHints.VALUE_RENDER_QUALITY);
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			    RenderingHints.VALUE_ANTIALIAS_ON);
	g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, 
			    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
	g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
			    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, 
			    RenderingHints.VALUE_COLOR_RENDER_QUALITY);
	g2.setRenderingHint(RenderingHints.KEY_DITHERING, 
			    RenderingHints.VALUE_DITHER_ENABLE);
	drawKinomeImage (g2, width, height);
	g2.dispose();

	return image;
    }

    void drawKinomeImage (Graphics2D g2, int width, int height) {
	// now draw the image
	double xs = (double)(width - padding) / KinomeImage.getWidth();
	double ys = (double)(height - padding)/ KinomeImage.getHeight();
	double scale = Math.min(xs, ys);

	double nw = scale*KinomeImage.getWidth();
	double nh = scale*KinomeImage.getHeight();
	
	g2.translate((width - nw)/2., (height - nh)/2.); // center
	int iw = (int)(nw+0.5), ih = (int)(nh+0.5);

	g2.drawImage(getKinomeImage (iw, ih), 0, 0, null);
	g2.scale(scale, scale); // scale

	// this doesn't let us control how the image should be scaled
	//g2.drawImage(KinomeImage, 0, 0, null);

	AffineTransform afx = g2.getTransform(); // copy
	g2.setTransform(new AffineTransform ()); // reset
	g2.setComposite(AlphaComposite.getInstance
			(AlphaComposite.SRC_OVER, 0.8f));
        setupKinases (afx);

	annotations.clear();
	for (KinomeDataset kds : datasets) {
            if (!hidden.contains(kds.getName())) {
                annotateDataset (afx, kds);
            }
	}
	drawAnnotations (g2);
    }

    /**
     * transform all kinase bounding boxes into the current image
     * coordinate
     */
    void setupKinases (AffineTransform afx) {
	Point2D.Double src = new Point2D.Double();
	Point2D.Double dst = new Point2D.Double();

        for (KinaseRenderer kr : kinases) {
            Rectangle b = kr.getBbox();
	    src.x = b.getCenterX();
	    src.y = b.getCenterY();
	    afx.transform(src, dst);

	    Rectangle2D r = kr.getBounds();
	    dst.x -= r.getWidth()/2.;
	    dst.y -= r.getHeight()/2.;
	    r.setRect(dst.x, dst.y, r.getWidth(), r.getHeight());
        }
    }

    void annotateDataset (AffineTransform afx, KinomeDataset kds) {
	Point2D.Double src = new Point2D.Double();
	Point2D.Double dst = new Point2D.Double();

	for (String kinase : kds.getKinases()) {
	    Rectangle b = KinomeDataset.bbox(kinase);
	    src.x = b.getCenterX();
	    src.y = b.getCenterY();
	    afx.transform(src, dst);

	    KinaseAnnotation ka = new KinaseAnnotation (kinase, kds);
	    Rectangle2D r = ka.getBounds();
	    dst.x -= r.getWidth()/2.;
	    dst.y -= r.getHeight()/2.;
	    // update the coordinate
	    r.setRect(dst.x, dst.y, r.getWidth(), r.getHeight());
	    annotations.add(ka);
	}
    }

    void drawAnnotations (Graphics2D g2) {
	Color bg = getBackground ();
	for (KinaseAnnotation ka : annotations) {
	    Rectangle2D r = ka.getBounds();
	    g2.translate(r.getX(), r.getY());
	    GradientPaint gp = new GradientPaint (0.f, 0.f, bg,
						  (float)r.getWidth(),
						  (float)r.getHeight(),
						  ka.getColor());
	    g2.setPaint(gp);
	    g2.fill(ka.getShape());
	    g2.translate(-r.getX(), -r.getY());
	}
    }

    public void mouseMoved (MouseEvent e) {
	hover = null;
        kinase = null;

	for (KinaseAnnotation ka : annotations) {
	    if (ka.contains(e.getPoint())) {
		hover = ka;
	    }
	}

        for (KinaseRenderer kr : kinases) {
            if (kr.contains(e.getPoint())) {
                kinase = kr;
            }
        }

	if (hover != null) {
	    setToolTipText (hover.getText());
	}
	else if (kinase != null) {
	    setToolTipText (kinase.getKinase());
	}
	repaint ();
    }


    public void mouseDragged (MouseEvent e) {
    }

    public void mouseEntered (MouseEvent e) {}
    public void mouseExited (MouseEvent e) {}
    public void mousePressed (MouseEvent e) {
        //doMouseAction (e);
    }
    public void mouseClicked (MouseEvent e) {
        doMouseAction (e);
    }
    public void mouseReleased (MouseEvent e) {
        //doMouseAction (e);
    }

    protected void doMouseAction (MouseEvent e) {
        if (kinase != null && e.getClickCount() > 0) {
            logger.info("Toggling "+kinase.getKinase()+" "+kinase.isSelected());
            kinase.setSelected(!kinase.isSelected());

            repaint ();
        }
    }

    @Override
    public Color getBackground () {
	Color bg = super.getBackground();
	if (bg == null) {
	    bg = Color.white;
	}
	return bg;
    }

    protected void paintCellShadowBorder (Graphics2D g2, int x, int y, 
					  int width, int height, 
					  int shadow) {
	int roundness = Math.max(width/4, height/4);
	if (shadow <= 2) {
	    g2.setColor(mixColor (DEFAULT_BORDER, getBackground (), .9f));
	    g2.setStroke(new BasicStroke (2.f));
	    g2.drawRoundRect(x, y, width, height, roundness, roundness);
	}
	else {
	    int sw = shadow*2;
	    for (int i = sw-1; i >= 2; i-=2) {
		float pct = (float)(sw - i) / (sw - 1);
		g2.setColor(mixColor (DEFAULT_BORDER, getBackground (), pct));
		g2.setStroke(new BasicStroke (i));
		g2.drawRoundRect(x, y, width, height, roundness, roundness);
	    }
	}
    }

    static protected Color mixColor (Color c1, Color c2, float p) {
	float[] a1 = c1.getComponents(null);
	float[] a2 = c2.getComponents(null);
	for (int i = 0; i < 4; ++i) {
	    a1[i] = (a1[i]*p) + (a2[i]*(1.f-p));
	}
	return new Color (a1[0], a1[1], a1[2], a1[3]);
    }

    public void setRendererFactory (KinomeRendererFactory renderer) {
	if (renderer == null) {
	    throw new IllegalArgumentException
		("Item renderer factory can't be null");
	}
	this.defaultRenderer = renderer;
    }
    public KinomeRendererFactory getRendererFactory () { 
	return defaultRenderer; 
    }

    public void exportImage (OutputStream os) throws IOException {
	Rectangle r = getBounds ();
	exportImage (r.width, r.height, os);
    }

    public void exportImage (int width, int height, OutputStream os) 
	throws IOException {

	BufferedImage image = new BufferedImage 
	    (width, height, BufferedImage.TYPE_INT_ARGB);
	Graphics2D g2 = image.createGraphics();
	g2.setRenderingHint(RenderingHints.KEY_RENDERING, 
			    RenderingHints.VALUE_RENDER_QUALITY);
	g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
			    RenderingHints.VALUE_ANTIALIAS_ON);
	g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, 
			    RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
	g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
			    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, 
			    RenderingHints.VALUE_COLOR_RENDER_QUALITY);
	g2.setRenderingHint(RenderingHints.KEY_DITHERING, 
			    RenderingHints.VALUE_DITHER_ENABLE);

	g2.setPaint(getBackground ());
	g2.setComposite(AlphaComposite.Src);

	int shadow = (int)(Math.min(width, height)*0.01+0.5);
	shadow = Math.max(shadow, 1);

	Rectangle r = new Rectangle (0, 0, width, height);

	int round = Math.max(width/4, height/4);
	if (paintBorder) {
	    g2.fillRoundRect(0, 0, width, height, round, round);
	}
	else {
	    g2.fill(r);
	}

	if (monochrome) {
	    g2.setComposite(AlphaComposite.Xor);
	}
	else {
	    g2.setComposite(AlphaComposite.SrcOver);
	}

	drawKinomeImage (g2, width, height);
	if (paintBorder) {
	    g2.setComposite(AlphaComposite.SrcAtop);
	    paintCellShadowBorder (g2, shadow/2, shadow/2, width-shadow, 
				   height-shadow, shadow);
	}

        g2.setPaint(Color.black);
        for (KinaseRenderer kr : kinases) {
            if (kr.isSelected()) {
                Rectangle2D bounds = kr.getBounds();
                g2.drawString(kr.getKinase(), 
                              (float)(bounds.getX()+bounds.getWidth()), 
                              (float)(bounds.getY()+bounds.getHeight()));
            }
        }

	g2.dispose();

	ImageIO.write(image, "png", os);
    }

    public static void main (String[] argv) throws Exception {
	KinomePanel kp = new KinomePanel ();
	//kp.setBackground(Color.white);
	kp.setMonochrome(false);
	kp.setPaintBorder(true);

	KinomeDataset kds1 = new KinomeDataset ("cmpd 1");
	kds1.setValue("ABL1",78);
	kds1.setValue("ALK",2100);
	kds1.setValue("AURKC",1500);
	kds1.setValue("AXL",250);
	kds1.setValue("CSF1R",1200);
	kds1.setValue("DDR1",11);
	kds1.setValue("DDR2",320);
	kds1.setValue("EGFR",9.6);
	kds1.setValue("EPHA3",2000);
	kds1.setValue("EPHA6",50);
	kds1.setValue("EPHA7",2400);
	kds1.setValue("EPHB4",520);
	kds1.setValue("FLT1",260);
	kds1.setValue("FLT3",850);
	kds1.setValue("FLT4",1100);
	kds1.setValue("FRK",170);
	kds1.setValue("KIT",260);
	kds1.setValue("LOK",81);
	kds1.setValue("LTK",550);
	kds1.setValue("MAP4K3",1500);
	kds1.setValue("MAP4K5",450);
	kds1.setValue("MERTK",1400);
	kds1.setValue("MET",5700);
	kds1.setValue("MKNK1",360);
	kds1.setValue("MKNK2",1700);
	kds1.setValue("PDGFRA",230);
	kds1.setValue("PDGFRB",88);
	kds1.setValue("RET",34);
	kds1.setValue("SLK",95);
	kds1.setValue("STK33",1400);
	kds1.setValue("TIE1",1500);
	kds1.setValue("TIE2",1000);
	kds1.setValue("VEGFR2",820);
	kds1.setValue("BLK",66);
	kds1.setValue("CIT",1800);
	kds1.setValue("FGFR1",560);
	kds1.setValue("FGR",270);
	kds1.setValue("FYN",360);
	kds1.setValue("LCK",17);
	kds1.setValue("LYN",110);
	kds1.setValue("PTK6",160);
	kds1.setValue("TNNI3K",2800);
	kds1.setValue("ZAK",5100);
	kds1.setValue("ABL2",69);
	kds1.setValue("CSK",2500);
	kds1.setValue("CSNK1E",3000);
	kds1.setValue("EPHA2",1100);
	kds1.setValue("EPHA4",1600);
	kds1.setValue("EPHA5",240);
	kds1.setValue("EPHA8",91);
	kds1.setValue("EPHB1",290);
	kds1.setValue("EPHB2",440);
	kds1.setValue("FGFR2",1100);
	kds1.setValue("GAK",86);
	kds1.setValue("HCK",360);
	kds1.setValue("MAP4K4",1400);
	kds1.setValue("RIPK2",4.6);
	kds1.setValue("RPS6KA6",240);
	kds1.setValue("SNF1LK",1900);
	kds1.setValue("SRC",70);
	kds1.setValue("SRMS",1900);
	kds1.setValue("TESK1",3700);
	kds1.setValue("TNIK",2300);
	kds1.setValue("YES",120);
	kds1.setValue("ERBB2",2600);
	kds1.setValue("ERBB4",480);
	kds1.setValue("MEK2",1100);
	kds1.setValue("PLK4",620);
	kds1.setValue("DMPK2",2200);
	kds1.setValue("EPHA1",230);
	kds1.setValue("MRCKA",2600);
	kds1.setValue("MRCKB",2500);
	kds1.setValue("ERK3",1500);
	kds1.setValue("PHKG1",1100);
	kds1.setValue("FGFR3",1600);
	kds1.setValue("MEK1",1800);
	kds1.setValue("SNF1LK2",430);
	kds1.setValue("TYRO3",93);
	kds1.setValue("ACVR1",150);
	kds1.setValue("ADCK3",4500);
	kds1.setValue("ADCK4",1700);
	kds1.setValue("RPS6KA1",400);
	kds1.setValue("TXK",3700);
	kds1.setValue("ACVRL1",470);

	KinomeDataset kds2 = new KinomeDataset ("cmpd 2");
	kds2.setValue("ALK",2300);
	kds2.setValue("CDK7",1800);
	kds2.setValue("CLK1",1200);
	kds2.setValue("CDK2",3400);
	kds2.setValue("CDK5",1900);
	kds2.setValue("CLK2",700);
	kds2.setValue("CSNK1D",260);
	kds2.setValue("CSNK1E",320);
	kds2.setValue("CSNK1G3",2900);
	kds2.setValue("TTK",1600);
	kds2.setValue("DYRK1B",1100);

	KinomeDataset kds3 = new KinomeDataset ("cmpd 3");
	kds3.setValue("CSF1R",2600);
	kds3.setValue("DDR1",1100);
	kds3.setValue("PDGFRB",8400);
	kds3.setValue("ABL1",730);
	kds3.setValue("BLK",3100);
	kds3.setValue("FGR",1300);
	kds3.setValue("FYN",2100);
	kds3.setValue("LCK",4600);
	kds3.setValue("LYN",1700);
	kds3.setValue("ABL2",1900);
	kds3.setValue("SRC",5500);
	kds3.setValue("YES",1600);
	kds3.setValue("p38_alpha",2.8);
	kds3.setValue("p38_beta",74);

	kp.add(kds3, new DefaultKinomeRendererFactory (new Color (0,153,153)));
	kp.add(kds2, new DefaultKinomeRendererFactory (Color.blue));
	kp.add(kds1, null);
	
	kp.exportImage(400, 600, new FileOutputStream ("kinome.png"));

	JFrame f = new JFrame ();
	f.setTitle("KinomePanel test");
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	f.getContentPane().add(kp);
	f.pack();
	f.setSize(400, 600);
	f.setVisible(true);
    }
}

