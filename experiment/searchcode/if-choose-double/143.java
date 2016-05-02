
import ij.ImagePlus;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.vecmath.Point3d;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Display3Panel.java
 *
 * Created on Jan 14, 2010, 1:51:49 PM
 */

/**
 *
 * @author Ilan
 */
public class Display3Panel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
	static final int cineRunning = 0;
	static final int cineStopped = 1;
	static final int cineForward = 2;
	static final int cineSide = 3;

	myMouse mouse1 = new myMouse();
	bkgdLoadData work2 = null;
	Display3Frame parent;
	int d3Color = JFijiPipe.COLOR_INVERSE;
	int d3Axial, d3Coronal, d3Sagital;
	JFijiPipe d3Pipe = null, mipPipe = null;
	boolean paintingFlg = false, cinePaint = false, showMip = false, runCine = false;
	boolean splitCursor = false, resizeOnce = false;
	double minSlider = 0, maxSlider = 1000, SUVorCount, saveWidth, saveLevel;
	private Timer m_timer = null;

    /** Creates new form Display3Panel */
    public Display3Panel() {
        initComponents();
		init();
    }

	void init() {
		addMouseListener( this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		m_timer = new Timer(200, new CineAction());
	}

	class CineAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if( paintingFlg == false && showMip) {
				cinePaint = true;
				repaint();
			}
		}
	}

	class myMouse {
		int xPos, yPos, widthX, widthY, page=1, xDrag, yDrag, button1;
		double zoomZ, zoom1;
		Point3d pan3d = null;

		int getMousePage(MouseEvent evt0, boolean save) {
			double scale = getScale();
			widthX = (int) (scale * d3Pipe.data1.width);
			Dimension dm2;
			int x1, y1;
			dm2 = getSize();
			x1 = evt0.getX();
			y1 = evt0.getY();
			if( x1 < 0) x1 = 0;	// these 2 lines should never happen
			if( y1 < 0) y1 = 0;
			int ret1 = (x1/widthX) + 1;
			if( ret1 > 3) ret1 = 0;
			if( save) {
				widthY = dm2.height;
				xPos = x1;
				yPos = y1;
				page = ret1;
			}
			return ret1;
		}

		Point getDragOffset(MouseEvent evt0, boolean save) {
			int x = evt0.getX();
			int y = evt0.getY();
			Point pt1 = new Point();
			if( save) {
				double scale = getScale();
				widthX = (int) (scale * d3Pipe.data1.width);
				button1 = evt0.getButton();
				xDrag = x;
				yDrag = y;
				return pt1;
			}
			pt1.x = 1000 * (x - xDrag) / widthX;
			pt1.y = 1000 * (yDrag - y) / widthX;
			return pt1;
		}

		Point getPanOffset(MouseEvent evt0, boolean save) {
			int x = evt0.getX();
			int y = evt0.getY();
			Point pt1 = new Point();
			if( save) {
				getMousePage(evt0, true);
				button1 = evt0.getButton();
				return pt1;
			}
			pt1.x = 1000 * (xPos - x) / widthX;
			pt1.y = 1000 * (yPos - y) / widthY;
			return pt1;
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int i, j;
		i = e.getButton();
		j = e.getClickCount();
		if( i != MouseEvent.BUTTON1) return;
		if( j == 1) {
			chooseNewPosition(e);
			return;
		}
	}

	@Override
	public void mouseEntered(MouseEvent me) {}
	@Override
	public void mouseExited(MouseEvent me) {}
	@Override
	public void mouseReleased(MouseEvent me) {}

	@Override
	public void mousePressed(MouseEvent me) {
		winLevelDrag(me, true);
		// watch out that resizing doesn't change the gray scale - look at cursor
		if(getCursor() != Cursor.getDefaultCursor()) mouse1.button1 = MouseEvent.NOBUTTON;
		maybeShowPopupMenu(me);
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		if( mouse1.button1 != MouseEvent.BUTTON1) return;
		winLevelDrag( me, false);
	}

	@Override
	public void mouseMoved(MouseEvent me) {}

	@Override
	public void mouseWheelMoved(MouseWheelEvent mwe) {
		int i,j;
		i = mouse1.getMousePage(mwe, false);
		j = mwe.getWheelRotation();
		if( j > 0) j = 1;
		else j = -1;
		incrSlicePosition(i, j);
	}

	void incrSlicePosition( int page, int diff) {
		int i = page;
		if( i <= 0)  i = mouse1.page;
		switch( i) {
			case 1:
				d3Axial += diff;
				break;

			case 2:
				d3Coronal += diff;
				d3Pipe.oldCorWidth = -1;	// cause update
				break;

			case 3:
				d3Sagital += diff;
				d3Pipe.oldCorWidth = -1;	// cause update
				break;
		}
		checkLimits();
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		paintingFlg = true;
		Graphics2D g2d = (Graphics2D) g.create();
		drawAll(g2d);
		g2d.dispose();	// clean up
		paintingFlg = false;
		cinePaint = false;
	}

	double getWidthSlider() {
		double retVal = d3Pipe.winWidth;
		return retVal;
	}

	double getLevelSlider() {
		double retVal = d3Pipe.winLevel;
		return retVal;
	}

	void ActionSliderChanged( double width, double level) {
		d3Pipe.winWidth = width;
		d3Pipe.winLevel = level;
		if( mipPipe != null) {
			mipPipe.winWidth = width;
			mipPipe.winLevel = level;
		}
		repaint();
	}

	void maybeShowPopupMenu(MouseEvent arg0) {
		if( arg0.getButton() != MouseEvent.BUTTON3) return;
		parent.hideAllPopupMenus();
		JPopupMenu pop1;
		if( arg0.getID() != MouseEvent.MOUSE_PRESSED) return;
		Point pt1 = arg0.getLocationOnScreen();
		pop1 = parent.getjPopupD3();
		pop1.setLocation(pt1);
		parent.updateCheckmarks();
		pop1.setVisible(true);
	}

	/**
	 * The routine which processes user mouse drags to change gray scale values.
	 *
	 * @param arg0 the current mouse position
	 * @param starting true if mouse pressed event, false if drag
	 */
	protected void winLevelDrag( MouseEvent arg0, boolean starting) {
		Point pt1 = mouse1.getDragOffset(arg0, starting);
		double width1, level1, delta;
		if( starting) {
			saveLevel = getLevelSlider();
			saveWidth = getWidthSlider();
			return;
		}
		delta = (maxSlider - minSlider) / 1000;
		width1 = pt1.x * delta + saveWidth;
		level1 = pt1.y * delta + saveLevel;
		setWinLevel(width1, level1, true);
	}

	void setWinLevel( double width1, double level1, boolean check) {
		double win1, base1;
		base1 = level1 - width1/2;
		win1 = level1 + width1/2;
		if( check) {
			if( base1 < minSlider) base1 = minSlider;
			if( win1 > maxSlider) win1 = maxSlider;
		}
		parent.setDualSliderValues(base1, win1);
	}

	void changeCurrentSlider() {
		int sliderDigits = 0;
		minSlider = 0;
		maxSlider = 1000;
		if( d3Pipe.data1.seriesType == ChoosePetCt.seriesCt) {
			minSlider = d3Pipe.data1.minVal;
			maxSlider = d3Pipe.data1.maxVal;
		}
		if( d3Pipe.data1.SUVfactor > 0) {
			sliderDigits = 1;
			maxSlider = d3Pipe.data1.sliderSUVMax;
		}
		parent.switchDualSlider( sliderDigits);
	}

	protected void LoadData(Display3Frame par1, ImagePlus inImg) {
		parent = par1;
		d3Pipe = new JFijiPipe();
		d3Pipe.LoadData(inImg);
		LoadLDataSub();
	}

	protected void LoadData(Display3Frame par1, JFijiPipe srcPipe) {
		parent = par1;
		d3Pipe = new JFijiPipe();
		d3Pipe.LoadData(srcPipe);
		LoadLDataSub();
		setWinLevel(srcPipe.winWidth, srcPipe.winLevel, false);
	}
	
	void LoadLDataSub() {
		int numFrms = d3Pipe.data1.numFrms;
		d3Axial = numFrms / 2;
		d3Coronal = d3Sagital = d3Pipe.data1.width / 2;
		checkLimits();
		d3Pipe.imgPos = new Point[3];
		d3Pipe.imgPos[0] = new Point(0, 0);
		d3Pipe.imgPos[1] = new Point(1, 0);
		d3Pipe.imgPos[2] = new Point(2, 0);
		parent.fillPatientData();
		parent.setTitle(parent.getTitleBar());
		changeCurrentSlider();
	}

	void presetWindowLevels(int indx) {
		JFijiPipe currPipe = d3Pipe;
		switch( indx) {
			case 0:
				int slice = d3Axial;
				currPipe.AutoFocus(slice);
				break;

			case 1:	// Chest-Abdomen
			case 2:	// Lung
			case 3:	// Liver
			case 4:	// Bone
			case 5:	// Brain-Sinus
				currPipe.winWidth = parent.ctWidth[indx-1];
				currPipe.winLevel = parent.ctLevel[indx-1];
				break;

			case 9:
				if(currPipe.data1.SUVfactor <= 0) break;	// ignore
				currPipe.winWidth = 10;
				currPipe.winLevel = 5;
				break;

			default:
				return;

		}
		changeCurrentSlider();	// update the values on the gray scale bar
		repaint();
	}

	void ActionMipCine(int type) {
		switch( type) {
			case cineRunning:	// this is used for both start and stop
				runCine = !runCine;
				break;

			case cineForward:
				runCine = false;
				mipPipe.cineIndx = 0;
				break;

			case cineSide:
				runCine = false;
				mipPipe.cineIndx = 3*JFijiPipe.NUM_MIP/4;
				break;
		}
		if (runCine) {
			m_timer.start();
			parent.setCineButtons(true);
		}
		else {
			m_timer.stop();
			repaint();
		}
	}

	void chooseNewPosition( MouseEvent arg0) {
		int i, x, y, width0, width1;
		i = mouse1.getMousePage(arg0, true);
		width1 = mouse1.widthX;
		width0 = d3Pipe.data1.width;
		x = (mouse1.xPos % width1) * width0 / width1;
		y = (int) (mouse1.yPos * width0 / (d3Pipe.data1.y2xFactor * width1));
		switch (i) {
			case 1:
				d3Coronal = mouse1.yPos * width0 / width1;
				d3Sagital = x;
				break;

			case 2:
				d3Sagital = x;
				d3Axial = y;
				break;

			case 3:
				d3Axial = y;
				if( showMip) {
					Point pt2 = new Point();
					mipPipe.data1.setCosSin(mipPipe.cineIndx);
					mipPipe.data1.getMipLocation(x, y, pt2, null, 0);
					d3Coronal = pt2.y;
					d3Sagital = pt2.x;
					break;
				}
				d3Coronal = x;
				break;
		}
		d3Pipe.oldCorWidth = -1;	// cause update
		checkLimits();
		repaint();
	}

	void drawAll(Graphics2D g) {
		if( d3Pipe == null) return;
		if( !resizeOnce) {
			resizeOnce = true;
			parent.fitWindow();
		}
		double scl1 = getScale();
		d3Pipe.prepareFrame(d3Axial, 0, d3Color, 0);
		d3Pipe.drawImages(g, scl1, this);
		d3Pipe.prepareCoronalSagital(d3Coronal, d3Sagital);
		d3Pipe.drawCorSagImages(g, scl1, this, true);	// coronal
		if( showMip) drawMip(g, scl1);
		else d3Pipe.drawCorSagImages(g, scl1, this, false);	// sagital

		d3Pipe.drawMarkers(g, 0, d3Sagital, d3Coronal, scl1, true, splitCursor);
		d3Pipe.drawMarkers(g, 1, d3Sagital, d3Axial, scl1, false, splitCursor);
		if( !showMip) d3Pipe.drawMarkers(g, 2, d3Coronal, d3Axial, scl1, false, splitCursor);
	}

	void checkLimits() {
		if( d3Axial < 0) d3Axial = 0;
		if( d3Axial >= d3Pipe.data1.numFrms) d3Axial = d3Pipe.data1.numFrms - 1;
		if( d3Coronal < 0) d3Coronal = 0;
		if( d3Coronal >= d3Pipe.data1.height) d3Coronal = d3Pipe.data1.height - 1;
		if( d3Sagital < 0) d3Sagital = 0;
		if( d3Sagital >= d3Pipe.data1.width) d3Sagital = d3Pipe.data1.width - 1;
	}

	void drawMip(Graphics2D g, double scl1) {
		if( mipPipe == null) {
			loadMip();
			ActionMipCine(cineRunning);
			return;
		}
		if( mipPipe.data1 == null) return;
		int i = mipPipe.data1.numFrms;
		if( i < JFijiPipe.NUM_MIP) {
			cinePaint = false;
			if (i <= 0) return;
		}
		mipPipe.makeDspImage(JFijiPipe.COLOR_INVERSE, 1);
		mipPipe.drawCine(g, scl1, this, cinePaint);
	}

	void loadMip() {
		work2 = new bkgdLoadData();
		work2.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String propertyName = evt.getPropertyName();
				if( propertyName.equals("state")) {
					SwingWorker.StateValue state = (SwingWorker.StateValue) evt.getNewValue();
					if( state == SwingWorker.StateValue.DONE) {
						work2 = null;
					}
				}
			}
		});
		work2.execute();
	}

	double getScale() {
		double scale0, scale1 = 0;
		JFijiPipe pipe1 = d3Pipe;
		if( pipe1 == null) return scale1;
		Dimension sz1, dim1 = getSize();
		sz1 = getWindowDim(pipe1);
		scale0 = ((double)dim1.width) / sz1.width;
		scale1 = ((double)dim1.height) / sz1.height;
		if( scale1 > scale0) scale1 = scale0;
		return scale1;
	}

	// in calculation of Window dimension use zoom1 = 1.0
	Dimension getWindowDim(JFijiPipe pipe1) {
		Dimension sz1 = null;
		double scale0;
		int width1, heigh0, heigh1;
		width1 = pipe1.data1.width;
		heigh1 = (int) (pipe1.data1.height * pipe1.data1.y2XMip + 0.5);
		scale0 = pipe1.zoomX * pipe1.data1.y2xFactor;
		heigh0 = (int) (pipe1.data1.numFrms * scale0 + 0.5);
		if( heigh1 < heigh0) heigh1 = heigh0;
		sz1 = new Dimension(width1*3, heigh1);
		return sz1;
	}

	int getCineState() {
		int retVal = cineRunning;
		if( !runCine) {
			retVal = cineStopped;
			int pos = 0;
			if( mipPipe != null) pos = mipPipe.cineIndx;
			if( pos == 0) retVal = cineForward;
			if( pos == 3*JFijiPipe.NUM_MIP/4) retVal = cineSide;
		}
		return retVal;
	}

	/**
	 * To keep the main thread responsive, the heavy data loading is done in the background.
	 */
	protected class bkgdLoadData extends SwingWorker {
		@Override
		protected Void doInBackground() {
			doMipBuild();
			return null;
		}
	}

	void doMipBuild() {
		mipPipe = new JFijiPipe();
		mipPipe.sliceType = JFijiPipe.DSP_CORONAL;
		mipPipe.winWidth = 800;
		mipPipe.winLevel = 400;
		mipPipe.useSrcPetWinLev = true;
		if( !mipPipe.LoadMIPData(d3Pipe)) {
			JOptionPane.showMessageDialog(this, "Failed to build the MIP data");
			mipPipe = null;
			return;
		}
	}

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}

