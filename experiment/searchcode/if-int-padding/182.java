package com.uncorrelated.kbird;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.color.CMMException;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;

public class Knocking extends JFrame implements WindowListener, Runnable {
	private ImgCanvas canvas = null;
	private BufferedImage image = null, transformed = null;
	private int FrameRate = 10;
	private final static int BenchmarkFrameRate = 150;
	private SwingBufferedImage[] swingBI = new SwingBufferedImage[10];
	private JSlider jsl1, jsl2, jsl3;
	private JCheckBox jcb4, jcb_fr;
	private JButton stop_b;
	private JPopupMenu pmenu;
	private JMenuItem[] jmi;
	private boolean IsDecline = false;
	private Thread thread = null;
	private volatile long waitOfThread;
	private volatile boolean flag = true;
	private ResourceBundle rb = ResourceBundle.getBundle("com.uncorrelated.kbird.Knocking");
	private int MaximumImageSize = 480;
	private int MaximumIconSize = 128;
	private int DoubleClickInterval = 1000;
	private static volatile int NumberOfRestWindow = 0;
	private boolean ShowFrameRate = false;
	private Logger logger = null;

	/* For concurrent programming */
	private class Task {
		public SwingBufferedImage[] calcuration = null;
		public BufferedImage source = null;
		public BufferedImage rendered = null;
	}
	Task[] rbuf = null;
	private Semaphore csem = new Semaphore(0, true);
	private Semaphore rsem = new Semaphore(0, true);
	private Semaphore tsem = new Semaphore(0, true);
	private volatile int cp = 0, rp = 0, tp = 0;
	
	private Thread[] rthread = new Thread[4];
	private Thread cthread = null;

	public class Renderer implements Runnable {
		private double et_sum = 0;
		private int et_count = 0;
		public void run() {
			while(flag){
				try {
					csem.acquire(1);
					long t1 = System.nanoTime();
					Task t = null;
					synchronized(rbuf){
						t = rbuf[rp];
						rp = (1 + rp)%rbuf.length;
					}
					synchronized(t){
						SwingBufferedImage[] a = t.calcuration;
						BufferedImage src = t.source;
						int w = src.getWidth();
						int h = src.getHeight();
						BufferedImage bi = new BufferedImage(w, h, src.getType());
						int[] rgb = src.getRGB(0, 0, w, h, null, 0, w);
						for(int c=0;c<a.length;c++){
							rgb = a[c].transform(rgb, w, h);
						}
						bi.setRGB(0, 0, w, h, rgb, 0, w);
						t.rendered = bi;
					}
					if(null!=logger){
						long t2 = System.nanoTime();
						double et = ((double)t2 - t1)/1000000000;
						et_sum += et;
						et_count++;
						logger.log(Level.INFO, "rendering time: {0} average: {1}",
								new Object[]{ new Double(et),
								new Double(et_sum/et_count)});
					}
					rsem.release(1);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public class Calcurator implements Runnable {
		public void run() {
			while(flag){
				try {
					tsem.acquire(1);
					int ptr;
					synchronized(rbuf){
						ptr = cp;
						cp = (1 + cp)%rbuf.length;
					}
					SwingBufferedImage[] a = new SwingBufferedImage[swingBI.length];
					for (int c = 0; c < swingBI.length; c++){
						synchronized (swingBI[c]) {
							swingBI[c].move();
							a[c] = (SwingBufferedImage)swingBI[c].clone();
						}
					}
					Task t = new Task();
					synchronized(t){
						t.calcuration = a;
						synchronized(image){
							t.source = image;
						}
					}
					rbuf[ptr] = t;
					csem.release(1);
				} catch (InterruptedException e) {
				}
			}
		}
	}
	/******************************/
	
	public Knocking(String fname) throws IOException {
		super("Knocking Bird");
		setUI();

		NumberOfRestWindow++;

		initImage(fname, this.getClass().getResource(rb.getString("default_image")));
		MaximumImageSize = parseInt(rb.getString("maximum_image_size"), MaximumImageSize);
		MaximumIconSize = parseInt(rb.getString("maximum_icon_size"), MaximumIconSize);
		DoubleClickInterval = parseInt(rb.getString("double_click_interval"), DoubleClickInterval);
		FrameRate = parseInt(rb.getString("frame_rate"), FrameRate);
		IsDecline = "true".equals(rb.getString("decrease"));
		if("true".equals(rb.getString("debug")))
				logger = Logger.getLogger(this.getClass().getName());

		setResizable(false);
		addWindowListener(this);

		for (int c = 0; c < swingBI.length; c++)
			swingBI[c] = new SwingBufferedImage();

		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);

		GridBagConstraints gbc1 = new GridBagConstraints();
		gbc1.ipady = 8;
		gbc1.gridx = gbc1.gridy = 0;
		canvas = new ImgCanvas();
		canvas.setSize(image.getWidth(), image.getHeight());
		gbl.setConstraints(canvas, gbc1);
		add(canvas);

		GridBagConstraints gbc2 = new GridBagConstraints();
		gbc2.gridx = 0;
		gbc2.gridy = 1;
		Container jsls1 = new Container();
		jsls1.setLayout(new FlowLayout());
		Dimension dm = new Dimension(96, 16);
		JLabel jl1 = new JLabel(rb.getString("param1"));
		jl1.setPreferredSize(dm);
		jsls1.add(jl1);
		jsls1.add(jsl1 = new JSlider(5, 100, 50));
		jsl1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int v = jsl1.getValue();
				for(int c=0;c<swingBI.length;c++){
					synchronized(swingBI[c]){
						swingBI[c].changePower(v);
					}
				}
			}
		});
		gbl.setConstraints(jsls1, gbc2);
		add(jsls1);

		GridBagConstraints gbc3 = new GridBagConstraints();
		gbc3.gridx = 0;
		gbc3.gridy = 2;
		Container jsls2 = new Container();
		jsls2.setLayout(new FlowLayout());
		JLabel jl2 = new JLabel(rb.getString("param2"));
		jl2.setPreferredSize(dm);
		jsls2.add(jl2);
		jsls2.add(jsl2 = new JSlider(1, 100, parseInt(rb.getString("default_speed"), 15)));
		jsl2.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int v = jsl2.getValue();
				for(int c=0;c<swingBI.length;c++){
					synchronized(swingBI[c]){
						swingBI[c].setSpeed(v);
					}
				}
			}
		});
		gbl.setConstraints(jsls2, gbc3);
		add(jsls2);

		GridBagConstraints gbc4 = new GridBagConstraints();
		gbc4.gridx = 0;
		gbc4.gridy = 3;
		Container jsls3 = new Container();
		jsls3.setLayout(new FlowLayout());
		JLabel jl3 = new JLabel(rb.getString("param3"));
		jl3.setPreferredSize(dm);
		jsls3.add(jl3);
		jsls3.add(jsl3 = new JSlider(50, 150, 100));
		jsl3.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				double v = getCoefficient();
				for(int c=0;c<swingBI.length;c++){
					synchronized(swingBI[c]){
						swingBI[c].setCoefficient(v);
					}
				}
			}
		});
		gbl.setConstraints(jsls3, gbc4);
		add(jsls3);
		
		jcb4 = new JCheckBox(rb.getString("param4"), IsDecline);
		jcb4.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				IsDecline = ((JCheckBox)e.getSource()).isSelected();
				for(int c=0;c<swingBI.length;c++){
					synchronized(swingBI[c]){
						swingBI[c].setDecline(IsDecline);
					}
				}
				pmenu.setVisible(false);
			}
		});

		jcb_fr = new JCheckBox(rb.getString("menu_item7"));
		jcb_fr.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				benchmark();
			}
		});

		GridBagConstraints gbc6 = new GridBagConstraints();
		gbc6.gridx = 0;
		gbc6.gridy = 4;
		Container btns = new Container();
		btns.setLayout(new GridLayout(1, 3));
		btns.add(jcb4);
		btns.add(jcb_fr);
		stop_b = new JButton(rb.getString("button1"));
		stop_b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stopAllSwing();
			}
		});
		btns.add(stop_b);
		gbl.setConstraints(btns, gbc6);
		add(btns);

		Dimension d_btns = btns.getPreferredSize();
		d_btns.width = 300;
		btns.setPreferredSize(d_btns);

		pmenu = new JPopupMenu();
		pmenu.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				int n = canvas.getOvalNumber();
				if(n<0){
					for(int c=0;c<3;c++)
						jmi[c].setEnabled(false);
					return;
				}
				SwingBufferedImage sbi = swingBI[n];
				jmi[2].setEnabled(true);
				if(sbi.isSuspend()){
					jmi[0].setEnabled(false);
					jmi[1].setEnabled(true);
				} else {
					jmi[0].setEnabled(true);
					jmi[1].setEnabled(false);
				}
			}
			
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
			
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});

		jmi = new JMenuItem[8];
		int jmi_c = 0;
		
		jmi[jmi_c] = new JMenuItem(rb.getString("menu_item3"),
				new ImageIcon(this.getClass().getResource(rb.getString("icon_pause"))));
		jmi[jmi_c].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.suspendOval();
			}
		});
		pmenu.add(jmi[jmi_c++]);
		
		jmi[jmi_c] = new JMenuItem(rb.getString("menu_item4"),
				new ImageIcon(this.getClass().getResource(rb.getString("icon_restart"))));
		jmi[jmi_c].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.resumeOval();
			}
		});
		pmenu.add(jmi[jmi_c++]);

		jmi[jmi_c] = new JMenuItem(rb.getString("menu_item1"),
				new ImageIcon(this.getClass().getResource(rb.getString("icon_stop"))));
		jmi[jmi_c].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				canvas.stopOval();
			}
		});
		pmenu.add(jmi[jmi_c++]);

		Dimension d_jmi = jmi[0].getPreferredSize();
		for(int c=0;c<3;c++){
			Dimension d_jmi_t = jmi[c].getPreferredSize();
			if(d_jmi_t.width > d_jmi.width)
				d_jmi = d_jmi_t;
		}
		pmenu.setPopupSize(d_jmi.width, 3*(d_jmi.height + 2));

		JMenuBar jmb = new JMenuBar();
		JMenu[] jm = new JMenu[3];
		int jm_c = 0;
		jm[jm_c] = new JMenu(rb.getString("menu1"));
		
		jmi[jmi_c] = new JMenuItem(rb.getString("menu_item2"),
				new ImageIcon(this.getClass().getResource(rb.getString("icon_file_open"))));
		jmi[jmi_c].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openFileChooser();
			}
		});
		jmi[jmi_c].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
		
		jm[jm_c].add(jmi[jmi_c++]);
		jmb.add(jm[jm_c++]);

		jm[jm_c] = new JMenu(rb.getString("menu2"));
		
		jmi[jmi_c] = new JMenuItem(rb.getString("menu_item5"),
				new ImageIcon(this.getClass().getResource(rb.getString("icon_edit_copy"))));
		jmi[jmi_c].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				copy();
			}
		});
		jmi[jmi_c].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
		jm[jm_c].add(jmi[jmi_c++]);
		
		jmi[jmi_c] = new JMenuItem(rb.getString("menu_item6"),
				new ImageIcon(this.getClass().getResource(rb.getString("icon_edit_paste"))));
		jmi[jmi_c].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				paste();
			}
		});
		jmi[jmi_c].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));
		jm[jm_c].add(jmi[jmi_c++]);

		jmb.add(jm[jm_c++]);

/* about */
		jm[jm_c] = new JMenu(rb.getString("menu3"));

		jmi[jmi_c] = new JMenuItem(rb.getString("menu_item8"),
				new ImageIcon(this.getClass().getResource(rb.getString("icon_usage"))));
		jmi[jmi_c].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				help();
			}
		});
		jmi[jmi_c].setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
		jm[jm_c].add(jmi[jmi_c++]);

		jmi[jmi_c] = new JMenuItem(rb.getString("menu_item9"),
				new ImageIcon(this.getClass().getResource(rb.getString("icon_about"))));
		jmi[jmi_c].addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				about();
			}
		});
		jm[jm_c].add(jmi[jmi_c++]);

		jmb.add(jm[jm_c++]);
/*********/

		setJMenuBar(jmb);
		
		setSize();
		moveCenter();

		int number_of_core = Runtime.getRuntime().availableProcessors();
		rbuf = new Task[2 + number_of_core];
		tsem.release(rbuf.length);

		cthread = new Thread(new Calcurator());
		cthread.start();

		rthread = new Thread[number_of_core];
		for(int c=0;c<rthread.length;c++){
			rthread[c] = new Thread(new Renderer());
			rthread[c].start();
		}

		waitOfThread = 1000/FrameRate;
		thread = new Thread(this);
		thread.start();

		setVisible(true);

		setSize();
		moveCenter();
	}

	private int parseInt(String value, int d){
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e){
			return d;
		}
	}
	
	private void moveCenter(){
		Rectangle screen = getGraphicsConfiguration().getBounds();
		setLocation(screen.x + screen.width/2  - getSize().width/2,
				screen.y + screen.height/2 - getSize().height/2);
	}
	
	public double getCoefficient(){
		return ((double)200 - jsl3.getValue())/100;
	}
	
	private void stopAllSwing(){
		for (int c = 0; c < swingBI.length; c++){
			synchronized (swingBI[c]) {
				swingBI[c].reset();
			}
		}
	}
	
	public void run(){
		while (flag) {
			try {
				long t1 = System.currentTimeMillis();
				rsem.acquire(1);
				Task t = null;
				synchronized(rbuf){
					t = rbuf[tp];
					rbuf[tp] = null;
					tp = (1 + tp)%rbuf.length;
				}
				synchronized(transformed){
					synchronized(t){
						transformed = t.rendered;
					}
				}
				tsem.release(1);
				canvas.repaint();
				long t2 = System.currentTimeMillis();
				long wtime = waitOfThread - t2 + t1;
				if(0<wtime){
					synchronized (thread) {
						thread.wait(wtime);
					}
				}
			} catch (InterruptedException e) {
			}
		}
	}

	private void setSize() {
		if (null != image) {
			for (int c = 0; c < swingBI.length; c++){
				synchronized(swingBI[c]){
					swingBI[c].reset();
				}
			}
			synchronized(image){
				transformed = image = rescaleImage(image, MaximumImageSize);
			}
			Image icon = rescaleImage(image, MaximumIconSize);
			setIconImage(icon);
			if (null != canvas) {
				canvas.setSize(image.getWidth(), image.getHeight());
			}
			Dimension d = getPreferredSize();
			Insets is = getInsets();
			int width = d.width + is.left + is.right;
			int height = d.height + is.top + is.bottom;
			setSize(width, height);
			repaint();
		}
	}

	private void setImage(File file) {
		benchmark(false);
		FileInputStream fis;
		try {
			fis = new FileInputStream(file);
			synchronized(image) {
				image = ImageIO.read(fis);
			}
			fis.close();
			canvas.showMessage();
			setSize();
		} catch (CMMException e) {
			JOptionPane.showMessageDialog(this ,e.getMessage(),"CMMException" ,JOptionPane.INFORMATION_MESSAGE);
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(this ,e.getMessage(),"FileNotFoundException" ,JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this ,e.getMessage(),"IOException" ,JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void setImage(URL url) throws IOException {
		benchmark(false);
		synchronized(image) {
			image = ImageIO.read(url);
		}
		if(null!=canvas)
			canvas.showMessage();
		setSize();
	}

	private void initImage(String fname, URL url) throws IOException {
		if(null!=fname){
			try{
				FileInputStream fis = new FileInputStream(fname);
				image = ImageIO.read(fis);
				fis.close();
			} catch(IOException ex) {
				fname = null;
			}
		}
		if(null == fname){
			image = ImageIO.read(url);
			if(null!=canvas)
				canvas.showMessage();
		}
	}

	private void openFileChooser(){
		JFileChooser fc = new JFileChooser();
	    fc.setFileFilter(new FileFilter(){
			String[] exts = {".jpg", ".jpeg", ".png", ".gif"};
			public boolean accept(File f){
				for(int c=0;c<exts.length;c++){
					if(f.isDirectory())
						return true;
					if(f.getName().endsWith(exts[c]))
						return true;
				}
				return false;
			}
			public String getDescription() {
				return "JPG, PNG & GIF Images";
			}
		});
		int selected = fc.showOpenDialog(this);
		if (selected == JFileChooser.APPROVE_OPTION){
			setImage(fc.getSelectedFile());
		}
	}

	private void copy(){
		Toolkit kit = Toolkit.getDefaultToolkit();
		Clipboard clip = kit.getSystemClipboard();
		ImageSelection is = null;
		synchronized(transformed){
			is = new ImageSelection(transformed);
		}
		clip.setContents(is, is);
	}
	
	private void paste(){
		Toolkit kit = Toolkit.getDefaultToolkit();
		Clipboard clip = kit.getSystemClipboard();
		try {
			Image cimage = (Image)clip.getData(DataFlavor.imageFlavor);
			synchronized(image){
				image = ImageConverter.convert(cimage);
			}
			setSize();
		} catch (UnsupportedFlavorException e) {
			return;
		} catch (IOException e) {
			return;
		}
	}

	private void help() {
		try {
			HelpDialog hd = HelpDialog.getInstance(this, rb);
			hd.setVisible(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void about() {
		try {
			AboutDialog ad = AboutDialog.getInstance(this, rb);
			ad.setVisible(true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void setInputEnable(boolean f){
		jsl1.setEnabled(f);
		jsl2.setEnabled(f);
		jsl3.setEnabled(f);
		stop_b.setEnabled(f);
		jcb4.setEnabled(f);
	}
	
	private void benchmark(){
		benchmark(!ShowFrameRate);
	}

	private void benchmark(boolean flag){
		if(!flag){
			ShowFrameRate = false;
			stopAllSwing();
			setInputEnable(true);
			jcb_fr.setSelected(false);
			waitOfThread = 1000/FrameRate;
		} else {
			ShowFrameRate = true;
			setInputEnable(false);
			jcb4.setSelected(IsDecline = false);
			Random r = new Random();
			r.setSeed(BenchmarkFrameRate);
			int width = canvas.getWidth();
			int height = canvas.getHeight();
			int mx = width/6;
			int my = height/6;
			for(int c=0;c<9;c++){
				int power = r.nextInt(20) + 30;
				int cx = (2*(c % 3) + 1) * mx;
				int cy = (2*(c / 3) + 1) * my;
				int vx = power*(int)(r.nextFloat()*2*mx + mx)/100;
				if(0.5 > r.nextFloat())
					vx *= -1;
				int vy = power*(int)(r.nextFloat()*2*my + my)/100;
				if(0.5 > r.nextFloat())
					vy *= -1;
				int radius = (int)Math.sqrt(vx*vx + vy*vy);
				canvas.setOval(cx, cy, vx, vy, radius, power, 0.5);
			}
			canvas.setOval(width/2, height/2, 0, 3*my, 3*my, 25, 0.5);
			waitOfThread = 1000/BenchmarkFrameRate;
		}
	}

	public BufferedImage rescaleImage(BufferedImage image, int maximum_size) {
		int original_height = image.getHeight();
		int destination_height = original_height;
		int original_width = image.getWidth();
		int destination_width = original_width;
		boolean IsLarge = false;
		if (maximum_size < original_width) {
			destination_width = maximum_size;
			destination_height = destination_width * original_height
					/ original_width;
			IsLarge = true;
		}
		if (maximum_size < original_height) {
			destination_height = maximum_size;
			destination_width = destination_height * original_width
					/ original_height;
			IsLarge = true;
		}
		if (IsLarge) {
			float scaling = ((float) (destination_width + 1))
					/ ((float) original_width);
			HashMap<RenderingHints.Key, Object> hm = new HashMap<RenderingHints.Key, Object>();
			hm.put(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			hm.put(RenderingHints.KEY_DITHERING,
					RenderingHints.VALUE_DITHER_ENABLE);
			BufferedImage image2 = new BufferedImage(destination_width,
					destination_height, image.getType());
			AffineTransformOp atfp = new AffineTransformOp(
					AffineTransform.getScaleInstance(scaling, scaling),
					new RenderingHints(hm));
			atfp.filter(image, image2);
			image = image2;
		}
		return image;
	}

	private class ImgCanvas extends Canvas implements MouseListener,
			MouseMotionListener, DropTargetListener {
		private Point mpp = null, mrp = null, mmp = new Point(0, 0);
		private int cptr = 0;
		private int nof = 0, sec = (int)System.currentTimeMillis()/1000;
		private boolean IsMessage = true;
		private double FrameRate = 0F;
		private DecimalFormat FrameRateFormat = new DecimalFormat("###.##");

		public ImgCanvas() {
			addMouseListener(this);
			addMouseMotionListener(this);
		}
	
		public void paint(Graphics g) {
			Image dbuf = createImage(getWidth(), getHeight());
			Graphics gd = dbuf.getGraphics();
			synchronized (transformed) {
				gd.drawImage(transformed, 0, 0, this);
			}
			if (null != mpp && null != mmp && 0==MouseAcitivity) {
				int radius = length(mpp, mmp);
				drawBoldOval(gd, mpp, radius, radius, Color.yellow);
				gd.setColor(Color.black);
				gd.drawLine(mpp.x + 1, mpp.y, mmp.x + 1, mmp.y);
				gd.drawLine(mpp.x - 1, mpp.y, mmp.x - 1, mmp.y);
				gd.setColor(Color.yellow);
				gd.drawLine(mpp.x, mpp.y, mmp.x, mmp.y);
			}
			if(null!=getMousePosition())
				drawSwingOval(gd);
			if(IsMessage)
				showMessage(gd, rb.getString("message01"));
			else if(ShowFrameRate){
				nof++;
				int csec = (int)(System.currentTimeMillis()/1000);
				if(sec < csec){
					int mt = csec - sec;
					FrameRate = (double)nof/mt;
					sec = csec;
					nof = 0;
				}
				StringBuilder sb = new StringBuilder(rb.getString("message02"));
				sb.append(" ");
				sb.append(FrameRateFormat.format(FrameRate));
				sb.append("/sec");
				showMessage(gd, sb.toString());
			}
			g.drawImage(dbuf, 0, 0, this);
		}

		Color bgColor = new Color(0F, 0F, 0F, 0.5F);
		private void showMessage(Graphics g, String msg){
			Font font = new Font(rb.getString("font.message"), Font.PLAIN, 16);
			FontMetrics fm = g.getFontMetrics(font);
			int height = fm.getHeight();
			int width = fm.stringWidth(msg);
			Dimension d = getSize();
			g.setColor(Color.black);
			int padding = 10;
			g.setColor(bgColor);
			g.fillRoundRect((d.width - width - padding)/2, d.height/2 - height - padding, width + padding, height + padding, padding, padding);
			g.setFont(font);
			g.setColor(Color.yellow);
			g.drawString(msg, (d.width - width)/2, (d.height - height)/2);
		}
		
		private int length(Point p1, Point p2) {
			int dx = p1.x - p2.x;
			int dy = p1.y - p2.y;
			return (int) Math.sqrt(dx * dx + dy * dy);
		}
		
		private void drawOval(Graphics g, int x, int y, int r1, int r2) {
			g.drawArc(x - r1, y - r2, 2 * r1, 2 * r2, 0, 360);
		}

		private void drawBoldOval(Graphics g, Point p, int r1, int r2, Color color){
			g.setColor(Color.black);
			drawOval(g, p.x, p.y, r1 - 1, r2 - 1);
			drawOval(g, p.x, p.y, r1 + 1, r2 + 1);
			g.setColor(color);
			drawOval(g, p.x, p.y, r1, r2);
		}
		
		/*
		 * 0: No Position  
		 * 1: Border/Resize(South)
		 * 2: Border/Resize(East)
		 * 3: Move
		 */
		private int MouseStatus = 0;
		private int MouseAcitivity = 0;
		private long MouseClickedTime = 0;
		private int OvalNumber = 0;
		public void drawSwingOval(Graphics g){
			switch(MouseAcitivity){
			case 1:
				synchronized(swingBI[OvalNumber]){
					SwingBufferedImage sbi = swingBI[OvalNumber];
					int cx = sbi.getCenterX();
					int nr1 = 0, nr2 = 0;
					int dx = cx - mmp.x;
					nr1 = Math.abs(dx);
					nr2 = sbi.getRadius2();
					sbi.setRadius(nr1, nr2);
				}
				break;
			case 2:
				synchronized(swingBI[OvalNumber]){
					SwingBufferedImage sbi = swingBI[OvalNumber];
					int cy = sbi.getCenterY();
					int nr1 = 0, nr2 = 0;
					int dy = cy - mmp.y;
					nr1 = sbi.getRadius1();
					nr2 = Math.abs(dy);
					sbi.setRadius(nr1, nr2);
				}
				break;
			case 3:
				synchronized(swingBI[OvalNumber]){
					SwingBufferedImage sbi = swingBI[OvalNumber];
					int cx = sbi.getCenterX();
					int cy = sbi.getCenterY();
					int dx = mmp.x - mpp.x;
					int dy = mmp.y - mpp.y;
					mpp = mmp;
					sbi.setCenterX(cx + dx);
					sbi.setCenterY(cy + dy);
				}
				break;
			}
			MouseStatus = 0;
			for (int c = 0; c < swingBI.length; c++){
				int ptr = (cptr + c) % swingBI.length;
				SwingBufferedImage sbi = swingBI[ptr];
				int cx = sbi.getCenterX();
				int cy = sbi.getCenterY();
				int r1 = sbi.getRadius1();
				int r2 = sbi.getRadius2();
				int r = (r1 + r2)/2;
				if(r<=0)
					continue;
				float borderWidth = (float)10 / r;
				float distance = distance(cx, cy, r1, r2, mmp.x, mmp.y);
				if(distance < 1){
					drawBoldOval(g, new Point(cx, cy), r1, r2, Color.pink);
					if((1 - borderWidth) <= distance){
						int d1 = Math.abs(cx - mmp.x);
						int d2 = Math.abs(cy - mmp.y);
						MouseStatus = d1 > d2 ? 1 : 2;
						if(0 == MouseAcitivity)
							OvalNumber = ptr;
					} else if(0 == MouseAcitivity){
						MouseStatus = 3;
						OvalNumber = ptr;
					}
				} else if(0<MouseAcitivity){
					drawBoldOval(g, new Point(cx, cy), r1, r2, Color.pink);
				}
			}
			int ctype = Cursor.DEFAULT_CURSOR;
			switch(0<MouseAcitivity ? MouseAcitivity : MouseStatus){
			case 1:
				ctype = Cursor.E_RESIZE_CURSOR;
				break;
			case 2:
				ctype = Cursor.S_RESIZE_CURSOR;
				break;
			case 3:
				ctype = Cursor.MOVE_CURSOR;
				break;
			}
			setCursor(Cursor.getPredefinedCursor(ctype));
		}

		private float distance(int cx, int cy, int r1, int r2, int x, int y){
			int dx = x - cx;
			int dy = y - cy;
			float v = (float)dx*dx/r1/r1 + (float)dy*dy/r2/r2;
			return v;
		}
		
		public void update(Graphics g) {
			paint(g);
		}

		public void mouseClicked(MouseEvent arg0) {
		}

		public void mouseEntered(MouseEvent arg0) {
		}

		public void mouseExited(MouseEvent arg0) {
		}

		public void mousePressed(MouseEvent arg0) {
			IsMessage = false;
			if (MouseEvent.BUTTON1 == arg0.getButton()) {
				mpp = arg0.getPoint();
				MouseAcitivity = MouseStatus;
				toggleOval();
			} else if (arg0.isPopupTrigger()) {
				pmenu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
			}
		}

		private void toggleOval(){
			if(3 == MouseStatus){
				// ?????1????2?????
				long ctime = System.currentTimeMillis();
				if(ctime - MouseClickedTime < DoubleClickInterval){
					swingBI[OvalNumber].toggleSuspend();
					MouseClickedTime = 0;
				} else
					MouseClickedTime = ctime;
			}
		}

		public void suspendOval(){
			if(0 < MouseStatus){
				swingBI[OvalNumber].setSuspend(true);
				MouseClickedTime = 0;
			}
		}

		public void resumeOval(){
			if(0 < MouseStatus){
				swingBI[OvalNumber].setSuspend(false);
				MouseClickedTime = 0;
			}
		}

		public void stopOval(){
			if(0 < MouseStatus){
				synchronized(swingBI[OvalNumber]){
					swingBI[OvalNumber].reset();
				}
				MouseClickedTime = 0;
			}
		}

		public int getOvalNumber(){
			if(0 >= MouseStatus)
				return -1;
			return OvalNumber;
		}

		public void setOval(int cx, int cy, int vx, int vy, int r, int power, double coefficient){
			IsMessage = false;
			cptr = cptr % swingBI.length;
			SwingBufferedImage sbi = swingBI[cptr];
			synchronized(sbi){
				sbi.setCenterX(cx);
				sbi.setCenterY(cy);
				sbi.setRadius(r, r);
				sbi.setVector(vx, vy);
				sbi.setSpeed(jsl2.getValue());
				sbi.setDecline(IsDecline);
				sbi.setPower(power);
				sbi.setCoefficient(coefficient);
				cptr = (cptr + 1) % swingBI.length;
			}
			if(null!=logger){
				logger.log(Level.INFO, "cx:{0} cy:{1} vx:{2} vy:{3} r:{4} power:{5} coefficient:{6}"
						,new Object[]{
						new Integer(cx),
						new Integer(cy),
						new Integer(vx),
						new Integer(vy),
						new Integer(r),
						new Integer(power),
						new Double(coefficient)
				});
			}
		}
		
		public void mouseReleased(MouseEvent arg0) {
			if (MouseEvent.BUTTON1 == arg0.getButton()) {
				mrp = arg0.getPoint();
				if(0==MouseAcitivity && null!=mpp && null!=mrp){
					int r = length(mpp, mrp);
					int power = jsl1.getValue();
					int vx = 0 < r ? power * (mrp.x - mpp.x) / r : 0;
					int vy = 0 < r ? power * (mrp.y - mpp.y) / r : 0;
					setOval(mpp.x, mpp.y, vx, vy, r, power, getCoefficient());
				}
				MouseStatus = 0;
				MouseAcitivity = 0;
				OvalNumber = -1;
				mrp = mpp = null;
			} else if (arg0.isPopupTrigger()) {
				pmenu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
			}
		}

		public void mouseDragged(MouseEvent e) {
			mmp = e.getPoint();
			MouseClickedTime = 0;
		}

		public void mouseMoved(MouseEvent e) {
			mmp = e.getPoint();
		}

		private DropTarget dropTarget = new DropTarget(this,
				DnDConstants.ACTION_COPY, this, true);

		public void dragEnter(DropTargetDragEvent arg0) {
			arg0.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
		}

		public void dragExit(DropTargetEvent arg0) {
		}

		public void dragOver(DropTargetDragEvent arg0) {
			if (arg0.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				arg0.acceptDrag(DnDConstants.ACTION_COPY);
				return;
			} else if (arg0.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				arg0.acceptDrag(DnDConstants.ACTION_COPY);
				return;
			}
			arg0.rejectDrag();
		}

		public void drop(DropTargetDropEvent arg0) {
			try {
				if (arg0.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					arg0.acceptDrop(DnDConstants.ACTION_COPY);
					Transferable trans = arg0.getTransferable();
					java.util.List files = (java.util.List) trans
							.getTransferData(DataFlavor.javaFileListFlavor);
					Iterator it = files.iterator();
					if (it.hasNext()) {
						setImage((File) it.next());
					}
				} else if (arg0.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					if (arg0.isLocalTransfer()) {

					} else {
						arg0.acceptDrop(DnDConstants.ACTION_COPY);
						Transferable trans = arg0.getTransferable();
						String fnames = (String) trans
								.getTransferData(DataFlavor.stringFlavor);
						StringTokenizer st = new StringTokenizer(fnames, "\n");
						if(st.hasMoreTokens()){
							setImage(new URL(st.nextToken()));
						}
					}
				}
				arg0.dropComplete(true);
			} catch (UnsupportedFlavorException ex) {
				ex.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				arg0.dropComplete(false);
			}
		}

		public void dropActionChanged(DropTargetDragEvent arg0) {
		}

		public void showMessage(){
			IsMessage = true;
		}
	}

	private void setUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		SwingUtilities.updateComponentTreeUI(this);
	}

	public void windowOpened(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		flag = false;
		synchronized (thread) {
			thread.notify();
		}
		synchronized (cthread) {
			cthread.notify();
		}
		for(int c=0;c<rthread.length;c++){
			synchronized (rthread[c]) {
				rthread[c].notify();
			}
		}
		try {
			thread.join(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		if(0 == --NumberOfRestWindow)
			System.exit(0);
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public static void main(String[] args) throws IOException {
		Knocking[] kn = new Knocking[0<args.length ? args.length : 1];
		if(args.length<1){
			kn[0] = new Knocking(null);
		} else {
			for(int c=0;c<args.length && c<kn.length; c++){
				kn[c] = new Knocking(args[c]);
			}
		}
	}
}

