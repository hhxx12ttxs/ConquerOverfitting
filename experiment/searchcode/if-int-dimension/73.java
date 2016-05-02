
import ij.ImagePlus;
import infovis.panel.DoubleBoundedRangeModel;
import infovis.panel.dqinter.DoubleRangeSlider;
import java.awt.Dimension;
import java.awt.Point;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Vector;
import java.util.prefs.Preferences;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * PetCtFrame.java
 *
 * Created on Dec 8, 2009, 10:51:52 AM
 */

/**
 *
 * @author Ilan
 */
public class PetCtFrame extends javax.swing.JFrame {

    /** Creates new form PetCtFrame */
    public PetCtFrame() {
        initComponents();
		init();
    }


	/**
	 * This routine first and foremost calls a dialog to choose the elements of the frame.
	 */
	protected void init() {
		chooseDlg = new ChoosePetCt(this, true);
		chooseDlg.setVisible(true);
		Vector<Integer> chosen = chooseDlg.chosenOnes;
		foundData = 0;
		if( chosen != null) foundData = chosen.size();
		if( foundData < 2) return;
		jPrefer = chooseDlg.jPrefer;
		int i, j, k;
		ImagePlus currImg;
		Dimension sz1 = new Dimension();
		sz1.height = jPrefer.getInt("petct dialog height", 0);
		if( sz1.height > 0) {
			i = jPrefer.getInt("petct dialog x", 0);
			j = jPrefer.getInt("petct dialog y", 0);
			sz1.width = jPrefer.getInt("petct dialog width", 0);
			setSize(sz1);
			setLocation(i,j);
		}
		hotIronFuse = jPrefer.getBoolean("hot iron fuse", false);
		autoResize = jPrefer.getBoolean("auto resize", false);
//		fuseFactor = jPrefer.getInt("fusion factor", 120);
		ctLevel = new int[5];
		ctWidth = new int[5];
		ctLevel[0] = 56;	// Chest-Abdomen
		ctWidth[0] = 340;
		ctLevel[1] = -498;	// Lung
		ctWidth[1] = 1464;
		ctLevel[2] = 93;	// Liver
		ctWidth[2] = 108;
		ctLevel[3] = 570;	// Bone
		ctWidth[3] = 3080;
		ctLevel[4] = 40;	// Brain-Sinus
		ctWidth[4] = 80;
		Vector<ImagePlus> imgList = new Vector<ImagePlus>();
		Vector<Integer> seriesType = new Vector<Integer>();
		for( i=0; i<foundData; i++) {
			j = chosen.elementAt(i);
			k = chooseDlg.seriesType.elementAt(j);
			seriesType.add(k);
			currImg = chooseDlg.imgList.elementAt(j);
			imgList.add(currImg);
		}
		initDualSlider();
		chooseDlg = null;	// free it
		petCtPanel1.LoadData(this, imgList, seriesType);
		updateMenu();
	}

	private void initDualSlider() {
		dSlide = new DoubleRangeSlider(0, 1000, 0 ,1000);
		dSlide.setEnabled(true);
		GroupLayout jPanelDualLayout = (GroupLayout) jPanel1.getLayout();
		jPanelDualLayout.setHorizontalGroup(
			jPanelDualLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup(jPanelDualLayout.createSequentialGroup()
			.addComponent(dSlide))
			);
		jPanelDualLayout.setVerticalGroup(
			jPanelDualLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
			.addGroup( jPanelDualLayout.createSequentialGroup()
			.addComponent(dSlide))
			);
		DoubleBoundedRangeModel range1 = dSlide.getModel();
		range1.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged( ChangeEvent e) {
				double oldLevel, newLevel, oldWidth, newWidth, hiVal, loVal;
				String tmp;
				hiVal = dSlide.getHighValue();
				loVal = dSlide.getLowValue();
				oldLevel = round(Double.valueOf( jLevelVal.getText()), slideDigits);
				oldWidth = round(Double.valueOf( jWidthVal.getText()), slideDigits);
				newWidth = round(hiVal-loVal, slideDigits);
				newLevel = round((hiVal+loVal)/2, slideDigits);
				if( newLevel == oldLevel && newWidth == oldWidth) return;
				tmp = myFormat(newWidth, slideDigits);
				jWidthVal.setText(tmp);
				tmp = myFormat(newLevel, slideDigits);
				jLevelVal.setText(tmp);
				petCtPanel1.ActionSliderChanged(newWidth, newLevel);
			}
		});
	}

	void openHelp() {
		String helpHS = "/resources/javahelp/PetCtHelp.xml";
		try {
			HelpBroker hb = null;
			URL hsURL = getClass().getResource(helpHS);
			HelpSet hs = new HelpSet(null, hsURL);
			hb = hs.createHelpBroker();
			hb.setDisplayed(true);
		} catch (Exception e) { e.printStackTrace();}
	}

	void updateAxCoSaButtons() {
		int type = petCtPanel1.m_sliceType;
		jButAxial.setSelected(type == JFijiPipe.DSP_AXIAL);
		jButCoronal.setSelected(type == JFijiPipe.DSP_CORONAL);
		jButSagital.setSelected(type == JFijiPipe.DSP_SAGITAL);
	}

	void updateMenu() {
		boolean mipTog = petCtPanel1.showMip;
		jButMip.setSelected(mipTog);
		setCineButtons(mipTog);
	}

	void setDualSliderValues( double loVal, double hiVal) {
		dSlide.setLowValue(loVal);
		dSlide.setHighValue(hiVal);
	}

	void switchDualSlider( int digits) {
		slideDigits = digits;
		double min1, max1, width1, level1;
		min1 = petCtPanel1.minSlider;
		max1 = petCtPanel1.maxSlider;
		width1 = petCtPanel1.getWidthSlider();
		level1 = petCtPanel1.getLevelSlider();
		dSlide.setMinimum(min1);
		dSlide.setMaximum(max1);
		dSlide.setLowValue(level1 - width1 / 2);
		dSlide.setHighValue(level1 + width1 / 2);
	}

	void changeLayout(int type) {
		petCtPanel1.m_sliceType = type;
		petCtPanel1.updatePipeInfo();
		updateAxCoSaButtons();
		repaint();
	}

	void fillPatientData() {
		JFijiPipe pet1 = petCtPanel1.petPipe;
		String meta = pet1.data1.metaData;
		if( meta == null) return;
		String tmp = ChoosePetCt.getDicomValue(meta, "0010,0010");
		m_patName = ChoosePetCt.compressPatName(tmp);
		m_patID = ChoosePetCt.getDicomValue(meta, "0010,0020");
		m_styName = ChoosePetCt.getDicomValue(meta, "0008,1030");
	}

		void fillSUV() {
		double SUV = 0;
		SUVDialog dlg1 = new SUVDialog(this, true);
		SUV = dlg1.calculateSUV(petCtPanel1.petPipe, false);
		petCtPanel1.petPipe.data1.setSUVfactor( SUV);
		petCtPanel1.mipPipe.data1.setSUVfactor( SUV);
		jMenuBrain.setEnabled(SUV > 0);
		petCtPanel1.changeCurrentSlider();
	}

	String getTitleBar() {
		JFijiPipe pet1 = petCtPanel1.petPipe;
		String str1 = m_patName + "   " + m_patID + "   ";
		str1 += DateFormat.getDateInstance(DateFormat.MEDIUM).format(pet1.data1.serTime);
		str1 += "   " +m_styName;
		return str1;
	}

	void setCineButtons(boolean vis1) {
		jButFwd.setVisible(vis1);
		jButFront.setVisible(vis1);
		jButSide.setVisible(vis1);
		if(!vis1) return;
		int val =  petCtPanel1.getCineState();
		boolean cineRun = false, cineFor = false, cineSid = false;
		switch( val) {
			case PetCtPanel.CINE_RUNNING:
				cineRun = true;
				break;

			case PetCtPanel.CINE_FORWARD:
				cineFor = true;
				break;

			case PetCtPanel.CINE_SIDE:
				cineSid = true;
				break;
		}
		jButFwd.setSelected(cineRun);
		jButFront.setSelected(cineFor);
		jButSide.setSelected(cineSid);
	}

	void savePrefs() {
		if( foundData < 2) return;
		Dimension sz1 = getSize();
		Point pt1 = getLocation();
		jPrefer.putInt("petct dialog x", pt1.x);
		jPrefer.putInt("petct dialog y", pt1.y);
		jPrefer.putInt("petct dialog width", sz1.width);
		jPrefer.putInt("petct dialog height", sz1.height);
	}

	public JPopupMenu getjPopupCtMenu() {
		return jPopupCtMenu;
	}

	public JPopupMenu getjPopupMipMenu() {
		return jPopupMipMenu;
	}

	public JPopupMenu getjPopupPetMenu() {
		return jPopupPetMenu;
	}

	void hideAllPopupMenus() {
		jPopupPetMenu.setVisible(false);
		jPopupCtMenu.setVisible(false);
		jPopupMipMenu.setVisible(false);
	}

	private void changePetColor( int indx) {
		petCtPanel1.petColor = indx;
		hideAllPopupMenus();
		petCtPanel1.repaint();
	}

	void getOptionDlg() {
		PetOptions petDlg = new PetOptions(this, true);
		petDlg.setVisible(true);
		petCtPanel1.updatePipeInfo();
		petCtPanel1.repaint();
	}

	void AttenDlg() {
		ManualAttenuation dlg1 = new ManualAttenuation(this, false);
		dlg1.setVisible(true);
	}

	void fitWindow() {
		Dimension sz1, sz0, sz2;
		double area1, scale1;
		sz1 = getSize();
		sz0 = petCtPanel1.getSize();
		// get the difference between the panel and the whole window
		sz1.height -= sz0.height;
		sz1.width -= sz0.width;
		area1 = sz0.width * sz0.height;
		sz2 = petCtPanel1.getWindowDim();
		if( autoResize) scale1 = ((double) sz0.width) / sz2.width;
		else scale1 = Math.sqrt(area1 / (sz2.width * sz2.height));
		sz2.width = (int) (scale1* sz2.width + 0.5) + sz1.width;
		sz2.height = (int) (scale1* sz2.height + 0.5) + sz1.height;
		setSize(sz2);
	}

	/**
	 * Called when user clicks on Auto reset, or one of the CT preset levels.
	 * This routine is used to reset the gray scale after the user has changed it.
	 * It is a convenient way for him to fix the gray scale back to known levels.
	 *
	 * @param indx the key to which level to set.
	 */
	protected void setWindowLevelAndWidth( int indx) {
		hideAllPopupMenus();
		petCtPanel1.presetWindowLevels(indx);
	}

	void updatePetCheckmarks( int indx) {
		jCheckBlues.setSelected(indx == JFijiPipe.COLOR_BLUES);
		jCheckGrayScale.setSelected(indx == JFijiPipe.COLOR_GRAY);
		jCheckInverse.setSelected(indx == JFijiPipe.COLOR_INVERSE);
		jCheckHotIron.setSelected(indx == JFijiPipe.COLOR_HOTIRON);
		jCheckUncorrected.setEnabled( petCtPanel1.upetPipe != null);
		jCheckFused.setEnabled(petCtPanel1.showMip);
		int i = petCtPanel1.m_masterFlg;
		switch(i) {
			case PetCtPanel.VW_PET_CT_FUSED:
				i = PetCtPanel.VW_MIP;
				break;

			case PetCtPanel.VW_PET_CT_FUSED_UNCORRECTED:
				i = PetCtPanel.VW_MIP_UNCORRECTED;
				break;
		}
		jCheckCorrected.setSelected(i == PetCtPanel.VW_MIP);
		jCheckUncorrected.setSelected(i == PetCtPanel.VW_MIP_UNCORRECTED);
		jCheckFused.setSelected(i == PetCtPanel.VW_MIP_FUSED);
		if( pet3 != null && pet3.openMode == 0) pet3 = null;
		jCheck3Pet.setSelected(pet3 != null);
		boolean colorFlg = !jCheckFused.isSelected();
		jCheckHotIron.setEnabled(colorFlg);
		jCheckBlues.setEnabled(colorFlg);
		jCheckGrayScale.setEnabled(colorFlg);
		jCheckInverse.setEnabled(colorFlg);
	}

	void updateCtCheckmarks() {
		int i, indx = -1;	// no ctlevel
		jCheckMri.setEnabled(petCtPanel1.mriPipe != null && petCtPanel1.ctPipe != null);
		jCheckMri.setSelected(petCtPanel1.MRIflg);
		if( ct3 != null && ct3.openMode == 0) ct3 = null;
		jCheck3Ct.setSelected(ct3 != null);
		if( petCtPanel1.ctPipe == null) return;
		for( i=0; i<5; i++) {
			if( (int)petCtPanel1.ctPipe.winLevel == ctLevel[i] && (int)petCtPanel1.ctPipe.winWidth == ctWidth[i]) {
				indx = i;
				break;
			}
		}
		jCheckChest.setSelected(indx == 0);
		jCheckLung.setSelected(indx == 1);
		jCheckLiver.setSelected(indx == 2);
		jCheckBone.setSelected(indx == 3);
		jCheckBrain.setSelected(indx == 4);
	}

	void updateMipCheckmarks() {
		jCheckUsePetWin.setSelected(petCtPanel1.mipPipe.useSrcPetWinLev);
	}

	void launch3Pet() {
		if( pet3 == null) {
			pet3 = new Display3Frame();
			JFijiPipe pip1 = petCtPanel1.getCorrectedOrUncorrectedPipe(false);
			if( !pet3.init2(pip1, this)) return;
			pet3.setVisible(true);
			hideAllPopupMenus();
			return;
		}
		pet3.dispose();
		pet3 = null;
	}

	void launch3Ct() {
		if( ct3 == null) {
			ct3 = new Display3Frame();
			JFijiPipe pip1 = petCtPanel1.getMriOrCtPipe();
			if( !ct3.init2(pip1, this)) return;
			ct3.setVisible(true);
			hideAllPopupMenus();
			return;
		}
		ct3.dispose();
		ct3 = null;
	}

	void setPetDisplayMode( int mode1) {
		int mod2 = mode1;
		if( !petCtPanel1.showMip) switch( mod2) {
			case PetCtPanel.VW_MIP:
			case PetCtPanel.VW_MIP_FUSED:
				mod2 = PetCtPanel.VW_PET_CT_FUSED;
				break;

			case PetCtPanel.VW_MIP_UNCORRECTED:
				mod2 = PetCtPanel.VW_PET_CT_FUSED_UNCORRECTED;
				break;
		}
		petCtPanel1.m_masterFlg = mod2;
		hideAllPopupMenus();
		updateMenu();
		petCtPanel1.updatePipeInfo();
		petCtPanel1.repaint();
	}

	void setExternalSpinners(boolean toggle) {
		boolean active = jButScroll.isSelected();
		setExternalSpinnersSub( toggle, active, this);
	}

	static void setExternalSpinnersSub(boolean toggle, boolean active, JFrame frm) {
		if( !toggle) {
			if( !active) return;
			active = false;	// toggle it now
		}
		if( !active) {
			extList.remove(frm);
			if( extList.isEmpty()) {
				if( extScroll != null) extScroll.dispose();
				extScroll = null;
			}
			return;
		}
		if( extScroll == null) {
			extScroll = new SyncScroll(null, false);
			extScroll.setVisible(true);
			extScroll.init(frm);
		}
		extList.add(frm);
		extScroll.setVisible(true);
	}

	void externalSpinnerChange( int diff) {
		petCtPanel1.incrSlicePosition(diff, true);
	}

	void resetButScroll() {
		jButScroll.setSelected(false);
	}

	static double round( double in, int numOfDigits) {
		double factorOfTen = 1;
		while( numOfDigits-- > 0) factorOfTen *= 10;
		return (Math.round(in*factorOfTen)/factorOfTen);
	}

	static String myFormat( double in, int numOfDigits) {
		String for1;
		int i = numOfDigits;
		for1 = "0";
		if( i>0) {
			for1 = "0.";
			while( i-- > 0) for1 += "0";
		}
		DecimalFormat for2 = new DecimalFormat(for1);
		return for2.format(in);
	}

	public PetCtPanel getPetCtPanel1() {
		return petCtPanel1;
	}

	@Override
	public void dispose() {
		savePrefs();
		setExternalSpinners(false);
		petCtPanel1.ctPipe = petCtPanel1.mipPipe = petCtPanel1.mriPipe = null;
		petCtPanel1.petPipe = petCtPanel1.upetPipe = null;
		super.dispose();
	}

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupPetMenu = new javax.swing.JPopupMenu();
        jMenuAutoPet = new javax.swing.JMenuItem();
        jMenuBrain = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jCheckFused = new javax.swing.JCheckBoxMenuItem();
        jCheckCorrected = new javax.swing.JCheckBoxMenuItem();
        jCheckUncorrected = new javax.swing.JCheckBoxMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        jCheck3Pet = new javax.swing.JCheckBoxMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jCheckInverse = new javax.swing.JCheckBoxMenuItem();
        jCheckGrayScale = new javax.swing.JCheckBoxMenuItem();
        jCheckBlues = new javax.swing.JCheckBoxMenuItem();
        jCheckHotIron = new javax.swing.JCheckBoxMenuItem();
        jPopupCtMenu = new javax.swing.JPopupMenu();
        jCheckChest = new javax.swing.JCheckBoxMenuItem();
        jCheckLung = new javax.swing.JCheckBoxMenuItem();
        jCheckLiver = new javax.swing.JCheckBoxMenuItem();
        jCheckBone = new javax.swing.JCheckBoxMenuItem();
        jCheckBrain = new javax.swing.JCheckBoxMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        jCheck3Ct = new javax.swing.JCheckBoxMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        jCheckMri = new javax.swing.JCheckBoxMenuItem();
        jPopupMipMenu = new javax.swing.JPopupMenu();
        jCheckUsePetWin = new javax.swing.JCheckBoxMenuItem();
        jToolBar1 = new javax.swing.JToolBar();
        jButAxial = new javax.swing.JToggleButton();
        jButCoronal = new javax.swing.JToggleButton();
        jButSagital = new javax.swing.JToggleButton();
        jButZoom = new javax.swing.JToggleButton();
        jButScroll = new javax.swing.JToggleButton();
        jButMip = new javax.swing.JToggleButton();
        jButFwd = new javax.swing.JToggleButton();
        jButFront = new javax.swing.JToggleButton();
        jButSide = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jWidthVal = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLevelVal = new javax.swing.JTextField();
        petCtPanel1 = new PetCtPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuExit = new javax.swing.JMenuItem();
        jMenuEdit = new javax.swing.JMenu();
        jMenuOptions = new javax.swing.JMenuItem();
        jMenuAttenuation = new javax.swing.JMenuItem();
        jMenuFitWindow = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuContents = new javax.swing.JMenuItem();
        jMenuAbout = new javax.swing.JMenuItem();

        jMenuAutoPet.setText("Auto level");
        jMenuAutoPet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuAutoPetActionPerformed(evt);
            }
        });
        jPopupPetMenu.add(jMenuAutoPet);

        jMenuBrain.setText("Brain");
        jMenuBrain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuBrainActionPerformed(evt);
            }
        });
        jPopupPetMenu.add(jMenuBrain);
        jPopupPetMenu.add(jSeparator3);

        jCheckFused.setSelected(true);
        jCheckFused.setText("Fused");
        jCheckFused.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckFusedActionPerformed(evt);
            }
        });
        jPopupPetMenu.add(jCheckFused);

        jCheckCorrected.setSelected(true);
        jCheckCorrected.setText("Attenuation corrected");
        jCheckCorrected.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckCorrectedActionPerformed(evt);
            }
        });
        jPopupPetMenu.add(jCheckCorrected);

        jCheckUncorrected.setSelected(true);
        jCheckUncorrected.setText("Uncorrected");
        jCheckUncorrected.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckUncorrectedActionPerformed(evt);
            }
        });
        jPopupPetMenu.add(jCheckUncorrected);
        jPopupPetMenu.add(jSeparator4);

        jCheck3Pet.setSelected(true);
        jCheck3Pet.setText("3 PET");
        jCheck3Pet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheck3PetActionPerformed(evt);
            }
        });
        jPopupPetMenu.add(jCheck3Pet);
        jPopupPetMenu.add(jSeparator2);

        jCheckInverse.setSelected(true);
        jCheckInverse.setText("Inverse");
        jCheckInverse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckInverseActionPerformed(evt);
            }
        });
        jPopupPetMenu.add(jCheckInverse);

        jCheckGrayScale.setSelected(true);
        jCheckGrayScale.setText("Gray scale");
        jCheckGrayScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckGrayScaleActionPerformed(evt);
            }
        });
        jPopupPetMenu.add(jCheckGrayScale);

        jCheckBlues.setSelected(true);
        jCheckBlues.setText("The blues");
        jCheckBlues.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBluesActionPerformed(evt);
            }
        });
        jPopupPetMenu.add(jCheckBlues);

        jCheckHotIron.setSelected(true);
        jCheckHotIron.setText("Hot iron");
        jCheckHotIron.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckHotIronActionPerformed(evt);
            }
        });
        jPopupPetMenu.add(jCheckHotIron);

        jCheckChest.setSelected(true);
        jCheckChest.setText("Chest-Abdomen");
        jCheckChest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckChestActionPerformed(evt);
            }
        });
        jPopupCtMenu.add(jCheckChest);

        jCheckLung.setSelected(true);
        jCheckLung.setText("Lung");
        jCheckLung.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckLungActionPerformed(evt);
            }
        });
        jPopupCtMenu.add(jCheckLung);

        jCheckLiver.setSelected(true);
        jCheckLiver.setText("Liver");
        jCheckLiver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckLiverActionPerformed(evt);
            }
        });
        jPopupCtMenu.add(jCheckLiver);

        jCheckBone.setText("Bone");
        jCheckBone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoneActionPerformed(evt);
            }
        });
        jPopupCtMenu.add(jCheckBone);

        jCheckBrain.setSelected(true);
        jCheckBrain.setText("Brain-Sinus");
        jCheckBrain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBrainActionPerformed(evt);
            }
        });
        jPopupCtMenu.add(jCheckBrain);
        jPopupCtMenu.add(jSeparator5);

        jCheck3Ct.setSelected(true);
        jCheck3Ct.setText("3 CT");
        jCheck3Ct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheck3CtActionPerformed(evt);
            }
        });
        jPopupCtMenu.add(jCheck3Ct);
        jPopupCtMenu.add(jSeparator6);

        jCheckMri.setSelected(true);
        jCheckMri.setText("MRI");
        jCheckMri.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckMriActionPerformed(evt);
            }
        });
        jPopupCtMenu.add(jCheckMri);

        jCheckUsePetWin.setSelected(true);
        jCheckUsePetWin.setText("Use PET gray scale");
        jCheckUsePetWin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckUsePetWinActionPerformed(evt);
            }
        });
        jPopupMipMenu.add(jCheckUsePetWin);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jToolBar1.setRollover(true);

        jButAxial.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/axial.gif"))); // NOI18N
        jButAxial.setFocusable(false);
        jButAxial.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButAxial.setPreferredSize(new java.awt.Dimension(17, 10));
        jButAxial.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButAxial.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButAxialActionPerformed(evt);
            }
        });
        jToolBar1.add(jButAxial);

        jButCoronal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/coronal.gif"))); // NOI18N
        jButCoronal.setFocusable(false);
        jButCoronal.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButCoronal.setMinimumSize(new java.awt.Dimension(17, 10));
        jButCoronal.setPreferredSize(new java.awt.Dimension(17, 10));
        jButCoronal.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButCoronal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButCoronalActionPerformed(evt);
            }
        });
        jToolBar1.add(jButCoronal);

        jButSagital.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/sagital.gif"))); // NOI18N
        jButSagital.setFocusable(false);
        jButSagital.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButSagital.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButSagital.setMinimumSize(new java.awt.Dimension(17, 10));
        jButSagital.setPreferredSize(new java.awt.Dimension(17, 10));
        jButSagital.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButSagital.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButSagitalActionPerformed(evt);
            }
        });
        jToolBar1.add(jButSagital);

        jButZoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/zoom.gif"))); // NOI18N
        jButZoom.setFocusable(false);
        jButZoom.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButZoom.setPreferredSize(new java.awt.Dimension(17, 10));
        jButZoom.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButZoom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButZoomActionPerformed(evt);
            }
        });
        jToolBar1.add(jButZoom);

        jButScroll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/arrows.gif"))); // NOI18N
        jButScroll.setFocusable(false);
        jButScroll.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButScroll.setPreferredSize(new java.awt.Dimension(19, 10));
        jButScroll.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButScroll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButScrollActionPerformed(evt);
            }
        });
        jToolBar1.add(jButScroll);

        jButMip.setText("MIP");
        jButMip.setFocusable(false);
        jButMip.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButMip.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButMip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButMipActionPerformed(evt);
            }
        });
        jToolBar1.add(jButMip);

        jButFwd.setSelected(true);
        jButFwd.setText(">>");
        jButFwd.setFocusable(false);
        jButFwd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButFwd.setMargin(new java.awt.Insets(2, 2, 2, 2));
        jButFwd.setMaximumSize(new java.awt.Dimension(35, 23));
        jButFwd.setMinimumSize(new java.awt.Dimension(20, 9));
        jButFwd.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButFwd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButFwdActionPerformed(evt);
            }
        });
        jToolBar1.add(jButFwd);

        jButFront.setText("F");
        jButFront.setFocusable(false);
        jButFront.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButFront.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButFront.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButFrontActionPerformed(evt);
            }
        });
        jToolBar1.add(jButFront);

        jButSide.setText("S");
        jButSide.setFocusable(false);
        jButSide.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButSide.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButSide.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButSideActionPerformed(evt);
            }
        });
        jToolBar1.add(jButSide);
        jToolBar1.add(jSeparator1);

        jWidthVal.setText("0");
        jWidthVal.setMaximumSize(new java.awt.Dimension(50, 20));
        jWidthVal.setPreferredSize(new java.awt.Dimension(44, 20));
        jToolBar1.add(jWidthVal);

        jPanel1.setPreferredSize(new java.awt.Dimension(100, 23));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 153, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 23, Short.MAX_VALUE)
        );

        jToolBar1.add(jPanel1);

        jLevelVal.setText("0");
        jLevelVal.setMaximumSize(new java.awt.Dimension(50, 20));
        jLevelVal.setPreferredSize(new java.awt.Dimension(40, 20));
        jToolBar1.add(jLevelVal);

        javax.swing.GroupLayout petCtPanel1Layout = new javax.swing.GroupLayout(petCtPanel1);
        petCtPanel1.setLayout(petCtPanel1Layout);
        petCtPanel1Layout.setHorizontalGroup(
            petCtPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 437, Short.MAX_VALUE)
        );
        petCtPanel1Layout.setVerticalGroup(
            petCtPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 248, Short.MAX_VALUE)
        );

        jMenuFile.setText("File");

        jMenuExit.setText("Exit");
        jMenuExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuExitActionPerformed(evt);
            }
        });
        jMenuFile.add(jMenuExit);

        jMenuBar1.add(jMenuFile);

        jMenuEdit.setText("Edit");

        jMenuOptions.setText("Options");
        jMenuOptions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuOptionsActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuOptions);

        jMenuAttenuation.setText("Attenuation Correction");
        jMenuAttenuation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuAttenuationActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuAttenuation);

        jMenuFitWindow.setText("Fit Window to data");
        jMenuFitWindow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuFitWindowActionPerformed(evt);
            }
        });
        jMenuEdit.add(jMenuFitWindow);

        jMenuBar1.add(jMenuEdit);

        jMenuHelp.setText("Help");

        jMenuContents.setText("Contents");
        jMenuContents.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuContentsActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuContents);

        jMenuAbout.setText("About");
        jMenuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuAboutActionPerformed(evt);
            }
        });
        jMenuHelp.add(jMenuAbout);

        jMenuBar1.add(jMenuHelp);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 437, Short.MAX_VALUE)
            .addComponent(petCtPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(petCtPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void jMenuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuExitActionPerformed
		dispose();
	}//GEN-LAST:event_jMenuExitActionPerformed

	private void jButFrontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButFrontActionPerformed
		petCtPanel1.ActionMipCine(PetCtPanel.CINE_FORWARD);
		setCineButtons(true);
	}//GEN-LAST:event_jButFrontActionPerformed

	private void jMenuAutoPetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAutoPetActionPerformed
		setWindowLevelAndWidth(0);
}//GEN-LAST:event_jMenuAutoPetActionPerformed

	private void jMenuBrainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuBrainActionPerformed
		setWindowLevelAndWidth(9);
}//GEN-LAST:event_jMenuBrainActionPerformed

	private void jCheckFusedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckFusedActionPerformed
		setPetDisplayMode( PetCtPanel.VW_MIP_FUSED);
}//GEN-LAST:event_jCheckFusedActionPerformed

	private void jCheckCorrectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckCorrectedActionPerformed
		setPetDisplayMode( PetCtPanel.VW_MIP);
}//GEN-LAST:event_jCheckCorrectedActionPerformed

	private void jCheckUncorrectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckUncorrectedActionPerformed
		setPetDisplayMode( PetCtPanel.VW_MIP_UNCORRECTED);
}//GEN-LAST:event_jCheckUncorrectedActionPerformed

	private void jCheckInverseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckInverseActionPerformed
		changePetColor(JFijiPipe.COLOR_INVERSE);
}//GEN-LAST:event_jCheckInverseActionPerformed

	private void jCheckGrayScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckGrayScaleActionPerformed
		changePetColor(JFijiPipe.COLOR_GRAY);
}//GEN-LAST:event_jCheckGrayScaleActionPerformed

	private void jCheckBluesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBluesActionPerformed
		changePetColor(JFijiPipe.COLOR_BLUES);
}//GEN-LAST:event_jCheckBluesActionPerformed

	private void jCheckHotIronActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckHotIronActionPerformed
		changePetColor(JFijiPipe.COLOR_HOTIRON);
}//GEN-LAST:event_jCheckHotIronActionPerformed

	private void jCheckChestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckChestActionPerformed
		setWindowLevelAndWidth(1);
}//GEN-LAST:event_jCheckChestActionPerformed

	private void jCheckLungActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckLungActionPerformed
		setWindowLevelAndWidth(2);
}//GEN-LAST:event_jCheckLungActionPerformed

	private void jCheckLiverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckLiverActionPerformed
		setWindowLevelAndWidth(3);
}//GEN-LAST:event_jCheckLiverActionPerformed

	private void jCheckBoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoneActionPerformed
		setWindowLevelAndWidth(4);
}//GEN-LAST:event_jCheckBoneActionPerformed

	private void jCheckBrainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBrainActionPerformed
		setWindowLevelAndWidth(5);
}//GEN-LAST:event_jCheckBrainActionPerformed

	private void jCheckMriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckMriActionPerformed
		petCtPanel1.MRIflg = !petCtPanel1.MRIflg;
		hideAllPopupMenus();
		petCtPanel1.repaint();
}//GEN-LAST:event_jCheckMriActionPerformed

	private void jCheckUsePetWinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckUsePetWinActionPerformed
		petCtPanel1.mipPipe.useSrcPetWinLev = !petCtPanel1.mipPipe.useSrcPetWinLev;
		hideAllPopupMenus();
		petCtPanel1.repaint();
}//GEN-LAST:event_jCheckUsePetWinActionPerformed

	private void jButAxialActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButAxialActionPerformed
		changeLayout( JFijiPipe.DSP_AXIAL);
	}//GEN-LAST:event_jButAxialActionPerformed

	private void jButCoronalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButCoronalActionPerformed
		changeLayout( JFijiPipe.DSP_CORONAL);
	}//GEN-LAST:event_jButCoronalActionPerformed

	private void jButSagitalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButSagitalActionPerformed
		changeLayout( JFijiPipe.DSP_SAGITAL);
	}//GEN-LAST:event_jButSagitalActionPerformed

	private void jButZoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButZoomActionPerformed
		petCtPanel1.zoomTog = !petCtPanel1.zoomTog;
		hideAllPopupMenus();
		petCtPanel1.repaint();
	}//GEN-LAST:event_jButZoomActionPerformed

	private void jButMipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButMipActionPerformed
		petCtPanel1.showMip = !petCtPanel1.showMip;
		setPetDisplayMode(PetCtPanel.VW_MIP_FUSED);
	}//GEN-LAST:event_jButMipActionPerformed

	private void jButFwdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButFwdActionPerformed
		petCtPanel1.ActionMipCine(PetCtPanel.CINE_RUNNING);
		setCineButtons(true);
	}//GEN-LAST:event_jButFwdActionPerformed

	private void jButSideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButSideActionPerformed
		petCtPanel1.ActionMipCine(PetCtPanel.CINE_SIDE);
		setCineButtons(true);
	}//GEN-LAST:event_jButSideActionPerformed

	private void jMenuOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuOptionsActionPerformed
		getOptionDlg();
	}//GEN-LAST:event_jMenuOptionsActionPerformed

	private void jMenuAttenuationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAttenuationActionPerformed
		AttenDlg();
	}//GEN-LAST:event_jMenuAttenuationActionPerformed

	private void jCheck3PetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheck3PetActionPerformed
		launch3Pet();
	}//GEN-LAST:event_jCheck3PetActionPerformed

	private void jCheck3CtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheck3CtActionPerformed
		launch3Ct();
	}//GEN-LAST:event_jCheck3CtActionPerformed

	private void jMenuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAboutActionPerformed
		PetCtAbout dlg = new PetCtAbout(this, true);
		dlg.setVisible(true);
	}//GEN-LAST:event_jMenuAboutActionPerformed

	private void jMenuContentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuContentsActionPerformed
		openHelp();
	}//GEN-LAST:event_jMenuContentsActionPerformed

	private void jMenuFitWindowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuFitWindowActionPerformed
		fitWindow();
	}//GEN-LAST:event_jMenuFitWindowActionPerformed

	private void jButScrollActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButScrollActionPerformed
		setExternalSpinners(true);
	}//GEN-LAST:event_jButScrollActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton jButAxial;
    private javax.swing.JToggleButton jButCoronal;
    private javax.swing.JToggleButton jButFront;
    private javax.swing.JToggleButton jButFwd;
    private javax.swing.JToggleButton jButMip;
    private javax.swing.JToggleButton jButSagital;
    private javax.swing.JToggleButton jButScroll;
    private javax.swing.JToggleButton jButSide;
    private javax.swing.JToggleButton jButZoom;
    private javax.swing.JCheckBoxMenuItem jCheck3Ct;
    private javax.swing.JCheckBoxMenuItem jCheck3Pet;
    private javax.swing.JCheckBoxMenuItem jCheckBlues;
    private javax.swing.JCheckBoxMenuItem jCheckBone;
    private javax.swing.JCheckBoxMenuItem jCheckBrain;
    private javax.swing.JCheckBoxMenuItem jCheckChest;
    private javax.swing.JCheckBoxMenuItem jCheckCorrected;
    private javax.swing.JCheckBoxMenuItem jCheckFused;
    private javax.swing.JCheckBoxMenuItem jCheckGrayScale;
    private javax.swing.JCheckBoxMenuItem jCheckHotIron;
    private javax.swing.JCheckBoxMenuItem jCheckInverse;
    private javax.swing.JCheckBoxMenuItem jCheckLiver;
    private javax.swing.JCheckBoxMenuItem jCheckLung;
    private javax.swing.JCheckBoxMenuItem jCheckMri;
    private javax.swing.JCheckBoxMenuItem jCheckUncorrected;
    private javax.swing.JCheckBoxMenuItem jCheckUsePetWin;
    private javax.swing.JTextField jLevelVal;
    private javax.swing.JMenuItem jMenuAbout;
    private javax.swing.JMenuItem jMenuAttenuation;
    private javax.swing.JMenuItem jMenuAutoPet;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuBrain;
    private javax.swing.JMenuItem jMenuContents;
    private javax.swing.JMenu jMenuEdit;
    private javax.swing.JMenuItem jMenuExit;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenuItem jMenuFitWindow;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JMenuItem jMenuOptions;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPopupMenu jPopupCtMenu;
    private javax.swing.JPopupMenu jPopupMipMenu;
    private javax.swing.JPopupMenu jPopupPetMenu;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JTextField jWidthVal;
    private PetCtPanel petCtPanel1;
    // End of variables declaration//GEN-END:variables
	int foundData = 0;
	boolean hotIronFuse = false, autoResize = false;
//	int fuseFactor = 120;
	Preferences jPrefer = null;
	String m_patName, m_patID, m_styName;
	DoubleRangeSlider dSlide = null;
	Display3Frame ct3 =  null, pet3 = null;
	ChoosePetCt chooseDlg = null;
	int slideDigits = 0;
	int [] ctLevel = null;
	int [] ctWidth = null;
	static SyncScroll extScroll = null;
	static Vector<Object> extList = new Vector<Object>();
}

