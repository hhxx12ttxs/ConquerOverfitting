
import ij.ImagePlus;
import ij.io.FileInfo;
import java.awt.Color;
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
import java.util.Vector;
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
 * PetCtPanel.java
 *
 * Created on Dec 23, 2009, 8:14:04 AM
 */

/**
 *
 * @author Ilan
 */
public class PetCtPanel extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {
	static final int VW_MIP = 1;
	static final int VW_MIP_FUSED = 2;
	static final int VW_MIP_UNCORRECTED = 3;
	static final int VW_PET_CT_FUSED = 4;
	static final int VW_PET_CT_FUSED_UNCORRECTED = 5;

	static final int SZ_MIP_AXIAL = 0;
	static final int SZ_AXIAL = 1;
	static final int SZ_CORONAL = 2;

	static final int CINE_RUNNING = 0;
	static final int CINE_STOPPED = 1;
	static final int CINE_FORWARD = 2;
	static final int CINE_SIDE = 3;

	myMouse mouse1 = new myMouse();
	bkgdLoadData work2 = null;
	PetCtFrame parent;
	Vector<ImagePlus> inImgList = null;
	Vector<Integer> inSeriesType = null;
	JFijiPipe petPipe=null, upetPipe=null, ctPipe=null, mipPipe = null, mriPipe=null;
	int sliderOwner = -1, petColor = JFijiPipe.COLOR_INVERSE, upetOffset = 0, petIndx = 0;
	int m_masterFlg = 0, m_sliceType = 0, petAxial=0, petCoronal, petSagital, CTval;
	boolean paintingFlg = false, cinePaint = false, SUVonceFlg = false, SUVflg = false;
	boolean  runCine = false,showMip = true;
	boolean MRIflg = false, zoomTog = false;
	double minSlider = 0, maxSlider = 1000, SUVorCount, saveWidth, saveLevel;
	double petRelativeZoom = 1.0;
	Point[] curPosition = new Point[3];
	private Timer m_timer = null;

    /** Creates new form PetCtPanel */
    public PetCtPanel() {
        initComponents();
		init();
    }

	void init() {
		addMouseListener( this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		m_masterFlg = VW_MIP;
		m_sliceType = JFijiPipe.DSP_AXIAL;
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
		int xPos, yPos, widthX, widthY, page, xDrag, yDrag, button1;
		double zoomZ, zoomY, zoom1;
		Point3d pan3d = null;

		int getMousePage(MouseEvent evt0, boolean save) {
			double scale = getScalePet();
			widthX = (int) (scale * petPipe.data1.width);
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
				double scale = getScalePet();
				widthX = (int) (scale * petPipe.data1.width);
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
		changeSliderParameters( e);
		if( i != MouseEvent.BUTTON1) return;
		if( j == 1) {
			processMouseSingleClick(e);
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
	public void mousePressed(MouseEvent e) {
		if(zoomTog) panDrag( e, true);
		else winLevelDrag(e, true);
		// watch out that resizing doesn't change the gray scale - look at cursor
		if(getCursor() != Cursor.getDefaultCursor()) mouse1.button1 = MouseEvent.NOBUTTON;
		maybeShowPopupMenu(e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if( mouse1.button1 != MouseEvent.BUTTON1) return;
		if(zoomTog) panDrag( e, false);
		else winLevelDrag( e, false);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		int j;
		if( curPosition[0] == null) return;
		j = mouse1.widthX/20;	// tolerance
		Point pos1 = e.getPoint();
		if( Math.abs(pos1.x - mouse1.xPos) > j || Math.abs(pos1.y - mouse1.yPos) > j) {
			curPosition[0] = null;	// kill cross hairs
			repaint();
		}
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int i,j;
		i = mouse1.getMousePage(e, false);
		j = e.getWheelRotation();
		if( j > 0) j = -1;
		else j = 1;
		if(zoomTog) {
			if( petPipe != null) petPipe.setZoom(j);
			if( upetPipe != null) upetPipe.setZoom(j);
			if( ctPipe != null) ctPipe.setZoom(j);
			if( mriPipe != null) mriPipe.setZoom(j);
			updateMultYOff();
			repaint();
		}
		else {
			boolean sliceChg = !showMip;
			if( showMip) {
				if( i==1 || i==2) sliceChg = true;
				if( i==3 && !runCine) {
					mipPipe.cineIndx -= j;
					while( mipPipe.cineIndx < 0) mipPipe.cineIndx += JFijiPipe.NUM_MIP;
					while( mipPipe.cineIndx >= JFijiPipe.NUM_MIP) mipPipe.cineIndx -= JFijiPipe.NUM_MIP;
					parent.setCineButtons(true);
					repaint();
				}
			}
			if( sliceChg) incrSlicePosition( j, false);
		}
	}

	void incrSlicePosition( int diff, boolean spinMIP) {
		if( spinMIP && mouse1.page == 3 && showMip && !runCine) {
			mipPipe.cineIndx -= diff;
			while( mipPipe.cineIndx < 0) mipPipe.cineIndx += JFijiPipe.NUM_MIP;
			while( mipPipe.cineIndx >= JFijiPipe.NUM_MIP) mipPipe.cineIndx -= JFijiPipe.NUM_MIP;
			parent.setCineButtons(true);
		}
		else switch( m_sliceType) {
			case JFijiPipe.DSP_AXIAL:
				petAxial -= diff;
				break;

			case JFijiPipe.DSP_CORONAL:
				petCoronal -= diff;
				break;

			case JFijiPipe.DSP_SAGITAL:
				petSagital -= diff;
				break;
		}
		checkLimits();
		calculateSUVandCT();
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
		double retVal = 0;
		switch( sliderOwner) {
			case 0:
				retVal = getCorrectedOrUncorrectedPipe(false).winWidth;
				break;

			case 1:
				retVal = getMriOrCtPipe().winWidth;
				break;

			case 2:
				if( !showMip) {
					retVal = petPipe.fuseWidth;
					break;
				}
				retVal = mipPipe.winWidth;
				if( mipPipe.useSrcPetWinLev) retVal = petPipe.winWidth;
				break;
		}
		return retVal;
	}

	double getLevelSlider() {
		double retVal = 0;
		switch( sliderOwner) {
			case 0:
				retVal = getCorrectedOrUncorrectedPipe(false).winLevel;
				break;

			case 1:
				retVal = getMriOrCtPipe().winLevel;
				break;

			case 2:
				if( !showMip) {
					retVal = petPipe.fuseLevel;
					break;
				}
				retVal = mipPipe.winLevel;
				if( mipPipe.useSrcPetWinLev) retVal = petPipe.winLevel;
				break;
		}
		return retVal;
	}

	void ActionSliderChanged( double width, double level) {
		JFijiPipe currPipe = null;
		switch( sliderOwner) {
			case 0:
				currPipe = getCorrectedOrUncorrectedPipe(false);
				currPipe.winWidth = width;
				currPipe.winLevel = level;
				break;

			case 1:
				currPipe = getMriOrCtPipe();
				currPipe.winWidth = width;
				currPipe.winLevel = level;
			break;

			case 2:
				if( !showMip) {
					petPipe.fuseWidth = width;
					petPipe.fuseLevel = level;
					break;
				}
				mipPipe.winWidth = width;
				mipPipe.winLevel = level;
				break;
		}
		repaint();
	}

	void maybeShowPopupMenu(MouseEvent arg0) {
		if( arg0.getButton() != MouseEvent.BUTTON3) return;
		parent.hideAllPopupMenus();
		JPopupMenu pop1;
		if( arg0.getID() != MouseEvent.MOUSE_PRESSED) return;
		Point pt1 = arg0.getLocationOnScreen();
		int pos3 = mouse1.getMousePage(arg0,false);
		switch( pos3) {
			case 1:
				pop1 = parent.getjPopupPetMenu();
				pop1.setLocation(pt1);
				parent.updatePetCheckmarks(petColor);
				pop1.setVisible(true);
				break;

			case 2:
				pop1 = parent.getjPopupCtMenu();
				pop1.setLocation(pt1);
				parent.updateCtCheckmarks();
				pop1.setVisible(true);
				break;

			case 3:
				if( !showMip) break;
				pop1 = parent.getjPopupMipMenu();
				pop1.setLocation(pt1);
				parent.updateMipCheckmarks();
				pop1.setVisible(true);
				break;

			default:
				return;	// not one of the 3 sections
		}
	}

	/**
	 * The routine which processes user mouse drags to change gray scale values.
	 *
	 * @param arg0 the current mouse position
	 * @param starting true if mouse pressed event, false if drag
	 */
	protected void winLevelDrag( MouseEvent arg0, boolean starting) {
		Point pt1 = mouse1.getDragOffset(arg0, starting);
		double width1, level1, delta, win1, base1;
		if( starting) {
			saveLevel = getLevelSlider();
			saveWidth = getWidthSlider();
			return;
		}
		delta = (maxSlider - minSlider) / 1000;
		width1 = pt1.x * delta + saveWidth;
		level1 = pt1.y * delta + saveLevel;
		base1 = level1 - width1/2;
		win1 = level1 + width1/2;
		if( base1 < minSlider) base1 = minSlider;
		if( win1 > maxSlider) win1 = maxSlider;
		parent.setDualSliderValues(base1, win1);
	}

	protected void panDrag( MouseEvent arg0, boolean starting) {
		Point pt1 = mouse1.getPanOffset(arg0, starting);
		if( starting) {
			mouse1.pan3d = new Point3d(petPipe.pan);
			mouse1.zoom1 = petPipe.zoom1;
			mouse1.zoomZ = petPipe.data1.width * mouse1.widthY;
			mouse1.zoomZ /= mouse1.widthX * petPipe.data1.numFrms * petPipe.zoomX * petPipe.data1.y2xFactor;
			mouse1.zoomY = petPipe.zoomY;
			return;
		}
		double x1 = pt1.x / 1000.;
		double y1 = pt1.y / 1000.;
		Point3d pan1 = new Point3d(mouse1.pan3d);
		double maxZ = (mouse1.zoom1 - 1.0) / mouse1.zoom1;
		if( maxZ < 0) maxZ = 0;
		double maxY = maxZ;
		switch( m_sliceType) {
			case JFijiPipe.DSP_AXIAL:
				maxY = (mouse1.zoom1 - mouse1.zoomY) / mouse1.zoom1;
				if( maxY < 0) maxY = 0;
				pan1.x += x1;
				pan1.y += y1;
				break;

			case JFijiPipe.DSP_CORONAL:
				pan1.x += x1;
				pan1.z += y1;
				break;

			case JFijiPipe.DSP_SAGITAL:
				pan1.y += x1;
				pan1.z += y1;
				break;
		}
		if( pan1.x > maxZ) pan1.x = maxZ;
		if( pan1.x < -maxZ) pan1.x = -maxZ;
		if( pan1.y > maxY) pan1.y = maxY;
		if( pan1.y < -maxY) pan1.y = -maxY;
		maxZ = (mouse1.zoom1 - mouse1.zoomZ) / mouse1.zoom1;
		// maxZ = mous1.zoom1 - mouse1.zoomZ;	// probably better
		if( maxZ < 0) maxZ = 0;
		if( pan1.z > maxZ) pan1.z = maxZ;
		if( pan1.z < 0) pan1.z = 0;
		if( !petPipe.updatePanValue(pan1, false)) return;
		if( upetPipe != null) upetPipe.updatePanValue(pan1, true);
		if( ctPipe != null) ctPipe.updatePanValue(pan1, true);
		if( mriPipe != null) mriPipe.updatePanValue(pan1, true);
		repaint();
	}

	void changeSliderParameters(MouseEvent arg0) {
		int pos3 = mouse1.getMousePage(arg0,false);
		if( pos3 <= 0) return;	// not one of the 3 sections
		sliderOwner = pos3 - 1;
		changeCurrentSlider();
	}

	void changeCurrentSlider() {
		int sliderDigits = 0;
		minSlider = 0;
		maxSlider = 1000;
		switch( sliderOwner) {
			case 2:
				if( !showMip) break;	// for MIP fall through to case 0

			case 0:
				// if it is uncorrected, use maxSlider = 1000
				if( petPipe != getCorrectedOrUncorrectedPipe(true) && sliderOwner == 0) break;
				maxSlider = petPipe.data1.sliderSUVMax;
				if( petPipe.data1.SUVfactor > 0) sliderDigits = 1;
				break;

			case 1:
				if( MRIflg) break;
				minSlider = ctPipe.data1.minVal;
				maxSlider = ctPipe.data1.maxVal;
				break;
		}
		parent.switchDualSlider( sliderDigits);
	}

	/**
	 * LoadData takes an imgList which contains the relevant series
	 * for constrcting a Pet-Ct study. It loads the data by parsing the Dicom
	 * information and then filling the vector which points to the binary data
	 * in the ImagePlus structure. The raw data is not copied so as to save
	 * memory. A MIP image is constructed from the PET data.
	 *
	 * This is all done in the background by a swing worker thread.
	 *
	 * @param par1 parent PetCtFrame
	 * @param imgList the list of chosen studies, currently loaded into memory
	 * @param seriesType for each study in imgList, its series type=ct,pet etc.
	 */
	protected void LoadData(PetCtFrame par1, Vector<ImagePlus> imgList, Vector<Integer>seriesType) {
		parent = par1;
		inImgList = imgList;
		inSeriesType = seriesType;
		work2 = new bkgdLoadData();
		work2.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				String propertyName = evt.getPropertyName();
				if( propertyName.equals("state")) {
					SwingWorker.StateValue state = (SwingWorker.StateValue) evt.getNewValue();
					if( state == SwingWorker.StateValue.DONE) {
						work2 = null;
						inImgList = null;
						inSeriesType = null;
					}
				}
			}
		});
		work2.execute();
	}

	void doActualLoadData() {
		JFijiPipe currPipe;
		ImagePlus currImg;
		boolean isDicom = true;
		int format, i, k, n = inImgList.size();
		for( i=0; i<n; i++) {
			currPipe = null;
			k = inSeriesType.elementAt(i);
			switch(k) {
				case ChoosePetCt.seriesBqmlPet:
				case ChoosePetCt.seriesSpect:
				case ChoosePetCt.seriesGmlPet:
					currPipe = petPipe = new JFijiPipe();
					break;

				case ChoosePetCt.seriesUPet:
					currPipe = upetPipe = new JFijiPipe();
					break;

				case ChoosePetCt.seriesCt:
					currPipe = ctPipe = new JFijiPipe();
					break;

				case ChoosePetCt.seriesMRI:
					currPipe = mriPipe = new JFijiPipe();
					break;

			}
			if( currPipe == null) continue;
			currImg = inImgList.elementAt(i);
			currPipe.LoadData(currImg);
			format = currPipe.data1.fileFormat;
			if( format != FileInfo.DICOM && format != FileInfo.UNKNOWN) isDicom = false;
		}
		if( petPipe == null) {	// make upet be pet
			petPipe = upetPipe;
			upetPipe = null;
		}
		if( petPipe == null) return;	// error
		presetWindowLevels(1);	// CT window
		mipPipe = new JFijiPipe();
		mipPipe.LoadMIPData(petPipe);
		setPetRelativeZoom();
		mipPipe.data1.y2XMip = mipPipe.data1.y2xFactor;
		petPipe.zoomX = mipPipe.zoomX = petRelativeZoom;
		if( upetPipe != null) upetPipe.zoomX = petRelativeZoom;
		petCoronal = petSagital = petPipe.data1.width / 2;
		for( i=0; i<n; i++) {
			currPipe = null;
			k = inSeriesType.elementAt(i);
			switch(k) {
				case ChoosePetCt.seriesCt:
					currPipe = ctPipe;
					break;

				case ChoosePetCt.seriesMRI:
					currPipe = mriPipe;
					break;
			}
			if( currPipe != null) {
				currPipe.corSagFactor = (petPipe.zoomX * currPipe.data1.width) / petPipe.data1.width;
				currPipe.corSagOffset = (currPipe.data1.width * ( 1- petPipe.zoomX) + 1)/2;
			}
		}
		parent.fillPatientData();
		parent.fillSUV();
		parent.setTitle(parent.getTitleBar());
		parent.changeLayout(JFijiPipe.DSP_AXIAL);
		ActionMipCine(PetCtPanel.CINE_RUNNING);
		if( !isDicom) JOptionPane.showMessageDialog(parent, "This data may not display properly.\nNot all the data is DICOM.");
	}

	void presetWindowLevels(int indx) {
		JFijiPipe currPipe = getCorrectedOrUncorrectedPipe(true);
		if( currPipe == null) return;
		switch( indx) {
			case 0:
				int slice = petAxial + upetOffset;
				currPipe.AutoFocus(slice);
				break;

			case 1:	// Chest-Abdomen
			case 2:	// Lung
			case 3:	// Liver
			case 4:	// Bone
			case 5:	// Brain-Sinus
				if( ctPipe == null) break;
				ctPipe.winWidth = parent.ctWidth[indx-1];
				ctPipe.winLevel = parent.ctLevel[indx-1];
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
			case CINE_RUNNING:	// this is used for both start and stop
				runCine = !runCine;
				break;

			case CINE_FORWARD:
				runCine = false;
				mipPipe.cineIndx = 0;
				break;

			case CINE_SIDE:
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

	private void drawAll(Graphics2D g) {
		switch(m_masterFlg) {
			case VW_MIP:
			case VW_MIP_FUSED:
			case VW_MIP_UNCORRECTED:
				layoutMip(g, false);
				break;

			case VW_PET_CT_FUSED:
			case VW_PET_CT_FUSED_UNCORRECTED:
				layoutMip(g, true);
				break;
		}
	}

	void layoutMip(Graphics2D g, boolean fused) {
		if( work2 != null) return;	// still loading
		if( mipPipe == null) return;
		double scl1 = getScalePet();
		int fusePos = 2;	// slot number for fused
		if( !fused) {
			mipPipe.makeDspImage(JFijiPipe.COLOR_INVERSE, 1);
			mipPipe.drawCine(g, scl1, this, cinePaint);
			fusePos = 0;
		}
		JFijiPipe pet1 = getCorrectedOrUncorrectedPipe(true);
		if( pet1 != null) {
			int i =JFijiPipe.COLOR_BLUES;
			if( parent.hotIronFuse) i =JFijiPipe.COLOR_HOTIRON;
			pet1.prepareFused(i);
			if( m_sliceType == JFijiPipe.DSP_AXIAL) {
				pet1.prepareFrame( petAxial + upetOffset, 0,petColor, 0);
				pet1.drawImages(g, scl1, this);
			}
			if( m_sliceType ==JFijiPipe.DSP_CORONAL) {
				pet1.prepareCoronalSagital(petCoronal, -1);
				pet1.drawCorSagImages(g, scl1, this, true);	// coronal
			}
			if( m_sliceType == JFijiPipe.DSP_SAGITAL) {
				pet1.prepareCoronalSagital(-1, petSagital);
				pet1.drawCorSagImages(g, scl1, this, false);	// sagital
			}
		} else printMissing(g);
		Point pt1 = new Point(fusePos, 0);
		JFijiPipe ct1 = getMriOrCtPipe();
		if( ct1 != null) {
			int ctPos = ct1.findCtPos(petAxial);
			if( ctPos >= 0) {
				double scl2 = scl1 * petPipe.data1.width / ct1.data1.width;
				ct1.prepareFused(JFijiPipe.COLOR_GRAY);
				if( m_sliceType == JFijiPipe.DSP_AXIAL) {
					ct1.prepareFrame(ctPos, 0, JFijiPipe.COLOR_GRAY, 0);
					ct1.drawImages(g, scl2, this);
				}
				if( m_sliceType == JFijiPipe.DSP_CORONAL) {
					ct1.prepareCoronalSagital(petCoronal, -1);
					ct1.drawCorSagImages(g, scl2, this, true);	// coronal
				}
				if( m_sliceType == JFijiPipe.DSP_SAGITAL) {
					ct1.prepareCoronalSagital(-1, petSagital);
					ct1.drawCorSagImages(g, scl2, this, false);	// sagital
				}
				if(m_masterFlg == VW_MIP_FUSED || fused) {
					petPipe.drawFusedImage(g, scl1, ct1, pt1, this);
				}
			}
		}
		draw3CursorsAndSUV(g);
	}

	double getScalePet() {
		double scale0, scale1 = 0;
		Dimension sz1, dim1 = getSize();
		sz1 = getWindowDim();
		if( sz1 == null) return 0;
		scale0 = ((double)dim1.width) / sz1.width;
		scale1 = ((double)dim1.height) / sz1.height;
		if( scale1 > scale0) scale1 = scale0;
		return scale1;
	}


	// in calculation of Window dimension use zoom1 = 1.0
	Dimension getWindowDim() {
		Dimension sz1 = null;
		JFijiPipe pipe1= petPipe;
		double scale0;
		int width1, heigh0, heigh1, type1;
		type1 = SZ_MIP_AXIAL;
		if( parent.autoResize) {
			if( m_sliceType == JFijiPipe.DSP_AXIAL) {
				if( !showMip) type1 = SZ_AXIAL;
			}
			else type1 = SZ_CORONAL;
		}
		width1 = pipe1.data1.width;
		heigh1 = (int) (pipe1.data1.height * pipe1.data1.y2XMip + 0.5);
		scale0 = pipe1.zoomX * pipe1.data1.y2xFactor;
		heigh0 = (int) (pipe1.data1.numFrms * scale0 + 0.5);
		switch( type1) {
			case SZ_MIP_AXIAL:
				if( heigh1 < heigh0) heigh1 = heigh0;
				break;
				
			case SZ_AXIAL:
				break;
				
			case SZ_CORONAL:
				heigh1 = heigh0;
				break;
		}
		sz1 = new Dimension(width1*3, heigh1);
		return sz1;
	}

	int getCineState() {
		int retVal = CINE_RUNNING;
		if( !runCine) {
			retVal = CINE_STOPPED;
			int pos = 0;
			if( mipPipe != null) pos = mipPipe.cineIndx;
			if( pos == 0) retVal = CINE_FORWARD;
			if( pos == 3*JFijiPipe.NUM_MIP/4) retVal = CINE_SIDE;
		}
		return retVal;
	}

	void checkLimits() {
		if( petAxial < 0) petAxial = 0;
		if( petAxial >= petPipe.data1.numFrms)
			petAxial = petPipe.data1.numFrms - 1;

		if( petCoronal < 0) petCoronal = 0;
		if( petCoronal > petPipe.data1.width)
			petCoronal = petPipe.data1.width - 1;

		if( petSagital < 0) petSagital = 0;
		if( petSagital > petPipe.data1.width)
			petSagital = petPipe.data1.width - 1;
	}

	/**
	 * Routine to calculate relative zoom if reconstruction diameters are different for PET and CT.
	 * The CT or MRI slice is inspected to get its size and pixel spacing.
	 * It is compared to the PET and the ratio calculated.
	 * If different then the ratio of the integral values of the matrix sizes is used.
	 */
	protected void setPetRelativeZoom() {
		petRelativeZoom = 1.0;
		JFijiPipe ct1Pipe = ctPipe;
		if( ctPipe == null) ct1Pipe = mriPipe;
		if( ct1Pipe == null) return;
		float [] ctPixelSpacing = ct1Pipe.data1.pixelSpacing;
		int ctWidth = ct1Pipe.data1.width;
		int i, origWidth, zoomWidth;
		double z1, z2;
		origWidth = petPipe.data1.width;
		z1 = petPipe.data1.pixelSpacing[0] * origWidth;
		z2 = ctPixelSpacing[0] * ctWidth;
		petRelativeZoom = z1 / z2;
		if( petRelativeZoom > 0.999 && petRelativeZoom < 1.001) {
			petRelativeZoom = 1.0;
			return;	// done
		}
		petRelativeZoom = PetCtFrame.round(petRelativeZoom, 4);
		zoomWidth = (int) ( origWidth / petRelativeZoom + 0.5);
		if( ((zoomWidth ^ origWidth) & 1) != 0) {
			if( petRelativeZoom > 1.0) zoomWidth++;
			else zoomWidth--;
		}
		// this is the real value we want
		petRelativeZoom = ((double) origWidth) / zoomWidth;
	}

	void updatePipeInfo() {
		Point[] imgPos;
		double petZoom;
		int offType = 0;
		imgPos = new Point[3];	// for axial, coronal, sagital - all in same place
		imgPos[0] = imgPos[1] = imgPos[2] = new Point(0,0);
		if( m_masterFlg == VW_MIP_FUSED) {
			offType = 1;
			imgPos[0].x = -1;	// hide the PET image
		}
		if( m_masterFlg == VW_PET_CT_FUSED) offType = 2;
		petPipe.imgPos = imgPos;
		petPipe.numDisp = 1;
		petPipe.offscrMode = offType;
		petPipe.sliceType = m_sliceType;
		petZoom = petPipe.zoom1;
		petPipe.dirtyFlg = true;	// force update
		petPipe.corSrc = null;
//		petPipe.fuseFactor = parent.fuseFactor;
		if( upetPipe != null) {	// same as corrected PET
			upetPipe.imgPos = imgPos;
			upetPipe.numDisp = 1;
			upetPipe.offscrMode = offType;
			upetPipe.sliceType = m_sliceType;
			upetPipe.zoom1 = petZoom;
			upetPipe.dirtyFlg = true;	// force update
			upetPipe.corSrc = null;
		}

		imgPos = new Point[3];
		imgPos[0] = imgPos[1] = imgPos[2] = new Point(1,0);
		if( ctPipe != null) {
			ctPipe.imgPos = imgPos;
			ctPipe.numDisp = 1;
			ctPipe.srcPet = petPipe;
			ctPipe.offscrMode = offType;
			ctPipe.sliceType = m_sliceType;
			ctPipe.zoom1 = petZoom;
			ctPipe.dirtyFlg = true;	// force update
			ctPipe.corSrc = null;
		}
		if( mriPipe != null) {
			mriPipe.imgPos = imgPos;
			mriPipe.numDisp = 1;
			mriPipe.srcPet = petPipe;
			mriPipe.offscrMode = offType;
			mriPipe.sliceType = m_sliceType;
			mriPipe.zoom1 = petZoom;
			mriPipe.dirtyFlg = true;	// force update
			mriPipe.corSrc = null;
		}
		if( parent.autoResize) parent.fitWindow();
		updateMultYOff();
		changeCurrentSlider();
	}

	double updateMultYOff() {
		double multYOff = 0, zoomY = 1.0;
		if( m_sliceType == JFijiPipe.DSP_AXIAL && (!parent.autoResize || showMip)) {
			int width = petPipe.data1.width;
			int height = (int) (petPipe.data1.width * petPipe.zoom1 + 0.5);
			int num = (int) (petPipe.data1.numFrms * petPipe.data1.y2xFactor + 0.5);
			if( num > width) {
				if( height >= num) height = num;
				multYOff = 0.5*(num-height)/width;
				zoomY = ((double) height) / width;
			}
		}
		petPipe.multYOff = multYOff;
		petPipe.zoomY = zoomY;
		if( upetPipe != null) {
			upetPipe.multYOff = multYOff;
			upetPipe.zoomY = zoomY;
		}
		if( ctPipe != null) {
			ctPipe.multYOff = multYOff;
			ctPipe.zoomY = zoomY;
		}
		if( mriPipe != null) {
			mriPipe.multYOff = multYOff;
			mriPipe.zoomY = zoomY;
		}
		return multYOff;
	}

	JFijiPipe getMriOrCtPipe() {
		JFijiPipe currPipe = ctPipe;
		if( MRIflg && mriPipe != null) currPipe = mriPipe;
		return currPipe;
	}

	/**
	 * Routine to figure out if to use Attenuation corrected or uncorrected pipe.
	 * This starts out with Attenuation corrected data, but will change if the user
	 * wants to see uncorrected data. It has an additional task of checking for
	 * missing uncorrected slices. For misssing slices it returns null.
	 *
	 * This routine is also used for general purposes where if a particular slice
	 * is missing it is of no importance. Thus a maybeNull flag has been added.
	 *
	 * @return corrected or uncorrected pipe or null
	 */
	protected JFijiPipe getCorrectedOrUncorrectedPipe(boolean maybeNull) {
		JFijiPipe pet1 = petPipe;
		upetOffset = 0;
		if((m_masterFlg == VW_MIP_UNCORRECTED ||
				m_masterFlg == VW_PET_CT_FUSED_UNCORRECTED) && upetPipe != null) {
			float z0, z1;
			int numFrms = upetPipe.data1.numFrms;
			double spacing;
			pet1 = upetPipe;
			z0 = petPipe.data1.zpos.elementAt(petAxial);
			if( petAxial >= numFrms) upetOffset = numFrms - petAxial - 1;
			z1 = upetPipe.data1.zpos.elementAt(petAxial+upetOffset);
			if( z0 != z1) {
				if( maybeNull) pet1 = null;
				spacing = petPipe.data1.spacingBetweenSlices;
				if( spacing == 0) spacing = 1.0;	// sanity
				if( Math.abs((z0-z1)/spacing) <= 0.5) return upetPipe;
				while( z1 < z0 && petAxial + upetOffset > 0) {
					upetOffset--;
					z1 = upetPipe.data1.zpos.elementAt(petAxial+upetOffset);
					if( Math.abs((z0-z1)/spacing) <= 0.5) return upetPipe;
				}
				while( z1 > z0 && petAxial + upetOffset < numFrms - 1) {
					upetOffset++;
					z1 = upetPipe.data1.zpos.elementAt(petAxial+upetOffset);
					if( Math.abs((z0-z1)/spacing) <= 0.5) return upetPipe;
				}
			}
		}
		return pet1;
	}

	void processMouseSingleClick(MouseEvent arg0) {
		int pos3 = mouse1.getMousePage(arg0, true);
		if( pos3 <= 0) return;	// not one of the 3 sections
		Point pt1 = new Point(mouse1.xPos % mouse1.widthX, mouse1.yPos);
		Point pt2 = new Point();
		double scl1 = getScalePet();	// assume corrected = uncorrected
		int i;
		if( pos3 == 3 && (m_masterFlg == VW_MIP || m_masterFlg == VW_MIP_FUSED ||
				m_masterFlg == VW_MIP_UNCORRECTED)) {
			curPosition[2] = pt1;
			Point pt3 = mipPipe.scrn2Pos(pt1, scl1, 1);
			petAxial = pt3.y;
			mipPipe.data1.setCosSin(mipPipe.cineIndx);
			mipPipe.data1.getMipLocation(pt3.x, pt3.y, pt2, null, 0);
			petCoronal = pt2.y;
			petSagital = pt2.x;
			checkLimits();
			i = 2;
			switch( m_sliceType) {
				case JFijiPipe.DSP_AXIAL:
					i = 0;
					if( petPipe.zoom1 > 1.0) i = 3;
					break;

				case JFijiPipe.DSP_CORONAL:
					pt2.x = petSagital;
					pt2.y = petAxial;
					break;

				case JFijiPipe.DSP_SAGITAL:
					pt2.x = petCoronal;
					pt2.y = petAxial;
					break;
			}
			pt3 = petPipe.pos2Scrn(pt2, scl1, i);
			curPosition[0] = pt3;
			curPosition[1] = pt3;
			calculateSUVandCT();
			repaint();
			return;
		}
		i = 0;
		if( petPipe.zoom1 > 1.0) i = 3;
		if( m_sliceType != JFijiPipe.DSP_AXIAL) i = 2;
		pt2 = petPipe.scrn2Pos(pt1, scl1, i);
		switch( m_sliceType) {
			case JFijiPipe.DSP_AXIAL:
				petCoronal = pt2.y;
				petSagital = pt2.x;
				break;

			case JFijiPipe.DSP_CORONAL:
				petSagital = pt2.x;
				petAxial = pt2.y;
				break;

			case JFijiPipe.DSP_SAGITAL:
				petCoronal = pt2.x;
				petAxial = pt2.y;
				break;
		}
		curPosition[0] = pt1;
		curPosition[1] = pt1;
		curPosition[2] = pt1;
		calculateSUVandCT();
		repaint();
	}

	void calculateSUVandCT() {
		if(curPosition[0] == null) return;	// don't waste time
		int z1, y0, deltaX, numPts, digits, width1;
		JFijiPipe.lineEntry currLine = null;
		double[] sumVals = null;
//		parent.hideAllPopupMenus();
		double spac1, sclData, totalSum, totalMax, currDbl, radMm = 10.0;
		upetOffset = 0;
		SUVorCount = 0;
		CTval = 0;
		JFijiPipe pet1 = getCorrectedOrUncorrectedPipe(true);
		if( pet1 == null) return;
		width1 = pet1.data1.width;
		z1 = petAxial + upetOffset;
		Point pt1 = new Point(petSagital, petCoronal);
		if( pt1.y < 0 || pt1.y >= width1) return;
		currLine = pet1.data1.getLineOfData(0, pt1.y, z1);
		spac1 = currLine.pixelSpacing[0];
		if( spac1 <= 0) return;
		deltaX = (int) (radMm/spac1);
		sumVals = sumLine(currLine, deltaX, pt1.x);
		sclData = currLine.slope;
		totalSum = sumVals[0] * sclData;
		totalMax = sumVals[1] * sclData;
		numPts = (int) sumVals[2];
		y0 = 0;
		while( deltaX > 0) {
			currDbl = (radMm/spac1);
			y0++;
			if( currDbl < y0) break;
			deltaX = (int) Math.sqrt(currDbl*currDbl - y0*y0);
			if( pt1.y - y0 >= 0) {
				currLine = pet1.data1.getLineOfData(0, pt1.y - y0, z1);
				sumVals = sumLine(currLine, deltaX, pt1.x);
				totalSum += sumVals[0] * sclData;
				currDbl = sumVals[1] * sclData;
				if( currDbl > totalMax) totalMax = currDbl;
				numPts += sumVals[2];
			}
			if( pt1.y + y0 < width1) {
				currLine = pet1.data1.getLineOfData(0, pt1.y + y0, z1);
				sumVals = sumLine(currLine, deltaX, pt1.x);
				totalSum += sumVals[0] * sclData;
				currDbl = sumVals[1] * sclData;
				if( currDbl > totalMax) totalMax = currDbl;
				numPts += sumVals[2];
			}
		}
		totalSum /= numPts;
		digits = 0;
		SUVflg = false;
		currDbl = currLine.SUVfactor;
		if(currDbl > 0) {
			SUVflg = true;
			digits = 2;
			totalSum = totalSum / currDbl;
			totalMax = totalMax / currDbl;
			if( totalMax < 1.0) digits = 3;
		}
		SUVorCount = PetCtFrame.round(totalMax, digits);

		// now do the CT or MRI part
		JFijiPipe currPipe = getMriOrCtPipe();
		if( currPipe == null) return;
		pt1.x = shift2Ct(currPipe, pt1.x);
		pt1.y = shift2Ct(currPipe, pt1.y);
		z1 = currPipe.findCtPos(petAxial);
		currLine = currPipe.data1.getLineOfData(0, pt1.y, z1);
		spac1 = currLine.pixelSpacing[0];
		if( spac1 <= 0) return;
		deltaX = (int) (4.0/spac1);
		sumVals = sumLine(currLine, deltaX, pt1.x);
		CTval = (int) (sumVals[0]/sumVals[2] + currPipe.data1.rescaleIntercept);
	}

	int shift2Ct( JFijiPipe pipe1, int pos1) {
		double scl1;
		int ret1, cenPet, cenCt;
		cenPet = petPipe.data1.width / 2;
		cenCt = pipe1.data1.width / 2;
		scl1 = (petPipe.zoomX * cenCt) / (pipe1.zoomX * cenPet);
		ret1 = (int) (scl1*( pos1 - cenPet));
		ret1 += cenCt;
		return ret1;
	}

	double[] sumLine(JFijiPipe.lineEntry currLine, int n1, int center) {
		double[] ret1 = new double[3];	// sum, max, numPts
		int i, j, size1;
		double currVal;
		boolean fltFlg = false;
		if( currLine.pixFloat != null) fltFlg = true;
		if( fltFlg) {
			size1 = currLine.pixFloat.length;
			ret1[0] = ret1[1] = currLine.pixFloat[center];	// sum and max = center point
		} else {
			size1 = currLine.pixels.length;
			ret1[0] = ret1[1] = currLine.pixels[center];	// sum and max = center point
		}
		ret1[2] = 1;	// 1 point so far
		for( i=1; i<=n1; i++) {
			j = center+i;
			if( j < size1) {
				ret1[2]++;
				if( fltFlg) currVal = currLine.pixFloat[j];
				else currVal = currLine.pixels[j];
				if( currVal > ret1[1]) ret1[1] = currVal;
				ret1[0] += currVal;
			}
			j = center-i;
			if( j>= 0) {
				ret1[2]++;
				if( fltFlg) currVal = currLine.pixFloat[j];
				else currVal = currLine.pixels[j];
				if( currVal > ret1[1]) ret1[1] = currVal;
				ret1[0] += currVal;
			}
		}
		return ret1;
	}

	void draw3CursorsAndSUV(Graphics2D g) {
		if(curPosition[0] == null) return;	// don't waste time
		int i, offY;
		String tmp;
		Point pt0, pt1;
		int widthX = mouse1.widthX;
		int width5 = widthX/20;	// 5%
		Color oldColor = g.getColor();
		g.setColor(Color.RED);
		tmp = "MaxCount = ";
		if( SUVflg) tmp = "SUVmax = ";
		tmp = tmp + SUVorCount;
		offY = (int) (getScalePet() * petPipe.multYOff * petPipe.data1.width) - 2;
		g.drawString(tmp, 0, widthX + offY);
		tmp = "CT = " + CTval;
		if( MRIflg) tmp = "MRI = " + CTval;
		g.drawString(tmp, widthX, widthX + offY);
		g.setColor(Color.GREEN);
		for(i=0; i<3; i++) {
			pt0 = curPosition[i];
			if( pt0 == null || pt0.x < 0 || pt0.x >= widthX) continue;
			pt1 = new Point(pt0.x, pt0.y);
			pt1.x += widthX*i;
			g.drawLine(pt1.x-width5, pt1.y, pt1.x+width5, pt1.y);
			g.drawLine(pt1.x, pt1.y-width5, pt1.x, pt1.y+width5);
		}
		g.setColor(oldColor);
	}

	void printMissing(Graphics2D g) {
		Color oldColor = g.getColor();
		g.setColor(Color.RED);
		int offY = (int) (getScalePet() * petPipe.multYOff * petPipe.data1.width);
		int width2 = mouse1.widthX / 2;
		g.drawString("Missing slice", width2, width2 + offY);
		g.setColor(oldColor);
	}

	/**
	 * To keep the main thread responsive, the heavy data loading is done in the background.
	 */
	protected class bkgdLoadData extends SwingWorker {
		@Override
		protected Void doInBackground() {
			doActualLoadData();
			return null;
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

