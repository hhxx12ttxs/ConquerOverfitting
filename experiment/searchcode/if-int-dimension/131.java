
import ij.ImagePlus;
import infovis.panel.DoubleBoundedRangeModel;
import infovis.panel.dqinter.DoubleRangeSlider;
import java.awt.Dimension;
import java.awt.Point;
import java.net.URL;
import java.text.DateFormat;
import java.util.Vector;
import java.util.prefs.Preferences;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.GroupLayout;
import javax.swing.JPopupMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * Display3Frame.java
 *
 * Created on Jan 14, 2010, 1:36:01 PM
 */

/**
 *
 * @author Ilan
 */
public class Display3Frame extends javax.swing.JFrame {

    /** Creates new form Display3Frame */
    public Display3Frame() {
        initComponents();
    }

	void init0() {
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
		initDualSlider();
		setCineButtons(false);
	}

	public boolean init1(ChoosePetCt dlg1) {
		if( openMode > 0) return false;
		jPrefer = dlg1.jPrefer;
		Vector<Integer> chosen = dlg1.chosenOnes;
		if( chosen == null || chosen.size() != 1) return false;
		int i, j, k;
		ImagePlus currImg;
		Dimension sz1 = new Dimension();
		sz1.height = jPrefer.getInt("display3 dialog height", 0);
		if( sz1.height > 0) {
			i = jPrefer.getInt("display3 dialog x", 0);
			j = jPrefer.getInt("display3 dialog y", 0);
			sz1.width = jPrefer.getInt("display3 dialog width", 0);
			setSize(sz1);
			setLocation(i,j);
		}
		init0();
		k = chosen.elementAt(0);
		currImg = dlg1.imgList.elementAt(k);
		display3Panel1.LoadData(this, currImg);
		initFinish();
		openMode = 1;
		return true;
	}

	public boolean init2(JFijiPipe petCtPipe, PetCtFrame par1) {
		if( openMode > 0) return false;
		Dimension sz1 = new Dimension();
		jPrefer = par1.jPrefer;
		sz1.height = jPrefer.getInt("display3 dialog height", 0);
		if( sz1.height > 0) {
			sz1.width = jPrefer.getInt("display3 dialog width", 0);
			setSize(sz1);
		}
		setLocationRelativeTo(par1);
		init0();
		display3Panel1.LoadData(this, petCtPipe);
		int slice = par1.getPetCtPanel1().petAxial;
		display3Panel1.d3Axial = petCtPipe.findCtPos(slice);
		slice = par1.getPetCtPanel1().petCoronal;
		slice = (int) (petCtPipe.corSagFactor * slice + petCtPipe.corSagOffset);
		display3Panel1.d3Coronal = slice;
		slice = par1.getPetCtPanel1().petSagital;
		slice = (int) (petCtPipe.corSagFactor * slice + petCtPipe.corSagOffset);
		display3Panel1.d3Sagital = slice;
		initFinish();
		openMode = 2;
		return true;
	}

	void initFinish() {
		int ser = display3Panel1.d3Pipe.data1.seriesType;
		if( ser == ChoosePetCt.seriesCt) {
			jMenuAuto.setVisible(false);
			jMenuBrain.setVisible(false);
			display3Panel1.d3Color = JFijiPipe.COLOR_GRAY;
		} else {
			jCheckChest.setVisible(false);
			jCheckLung.setVisible(false);
			jCheckLiver.setVisible(false);
			jCheckBone.setVisible(false);
			jCheckBrain.setVisible(false);
			if( display3Panel1.d3Pipe.data1.SUVfactor <= 0) jMenuBrain.setVisible(false);
		}
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
				oldLevel = PetCtFrame.round(Double.valueOf( jLevelVal.getText()), slideDigits);
				oldWidth = PetCtFrame.round(Double.valueOf( jWidthVal.getText()), slideDigits);
				newWidth = PetCtFrame.round(hiVal-loVal, slideDigits);
				newLevel = PetCtFrame.round((hiVal+loVal)/2, slideDigits);
				if( newLevel == oldLevel && newWidth == oldWidth) return;
				tmp = PetCtFrame.myFormat(newWidth, slideDigits);
				jWidthVal.setText(tmp);
				tmp = PetCtFrame.myFormat(newLevel, slideDigits);
				jLevelVal.setText(tmp);
				display3Panel1.ActionSliderChanged(newWidth, newLevel);
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

	void updateMenu() {
		boolean mipTog = display3Panel1.showMip;
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
		min1 = display3Panel1.minSlider;
		max1 = display3Panel1.maxSlider;
		width1 = display3Panel1.getWidthSlider();
		level1 = display3Panel1.getLevelSlider();
		dSlide.setMinimum(min1);
		dSlide.setMaximum(max1);
		dSlide.setLowValue(level1 - width1 / 2);
		dSlide.setHighValue(level1 + width1 / 2);
	}

	void fillPatientData() {
		JFijiPipe pet1 = display3Panel1.d3Pipe;
		String meta = pet1.data1.metaData;
		if( meta == null) return;
		String tmp = ChoosePetCt.getDicomValue(meta, "0010,0010");
		m_patName = ChoosePetCt.compressPatName(tmp);
		m_patID = ChoosePetCt.getDicomValue(meta, "0010,0020");
		m_styName = ChoosePetCt.getDicomValue(meta, "0008,1030");
	}

	String getTitleBar() {
		JFijiPipe pet1 = display3Panel1.d3Pipe;
		String str1 = m_patName + "   " + m_patID + "   ";
		str1 += DateFormat.getDateInstance(DateFormat.MEDIUM).format(pet1.data1.serTime);
		str1 += "   " +m_styName;
		return str1;
	}

	void swapMipState() {
		display3Panel1.showMip = !display3Panel1.showMip;
		updateMenu();
		display3Panel1.repaint();
	}

	void setCineButtons(boolean vis1) {
		jButFwd.setVisible(vis1);
		jButFront.setVisible(vis1);
		jButSide.setVisible(vis1);
		if(!vis1) return;
		int val =  display3Panel1.getCineState();
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
		if( openMode <= 0) return;
		Dimension sz1 = getSize();
		jPrefer.putInt("display3 dialog width", sz1.width);
		jPrefer.putInt("display3 dialog height", sz1.height);
		if( openMode >= 2) return;
		Point pt1 = getLocation();
		jPrefer.putInt("display3 dialog x", pt1.x);
		jPrefer.putInt("display3 dialog y", pt1.y);
	}

	public JPopupMenu getjPopupD3() {
		return jPopupD3;
	}

	void hideAllPopupMenus() {
		jPopupD3.setVisible(false);
	}

	private void changePetColor( int indx) {
		display3Panel1.d3Color = indx;
		hideAllPopupMenus();
		display3Panel1.repaint();
	}

	private void flipSplitState() {
		display3Panel1.splitCursor = !display3Panel1.splitCursor;
		hideAllPopupMenus();
		display3Panel1.repaint();
	}

	void fitWindow() {
		Dimension sz1, sz0, sz2;
		double area1, scale1;
		sz1 = getSize();
		sz0 = display3Panel1.getSize();
		// get the difference between the panel and the whole window
		sz1.height -= sz0.height;
		sz1.width -= sz0.width;
		area1 = sz0.width * sz0.height;
		sz2 = display3Panel1.getWindowDim(display3Panel1.d3Pipe);
		scale1 = Math.sqrt(area1 / (sz2.width * sz2.height));
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
		display3Panel1.presetWindowLevels(indx);
	}

	void updateCheckmarks() {
		int i, indx = display3Panel1.d3Color;
		jCheckBlues.setSelected(indx == JFijiPipe.COLOR_BLUES);
		jCheckGrayScale.setSelected(indx == JFijiPipe.COLOR_GRAY);
		jCheckInverse.setSelected(indx == JFijiPipe.COLOR_INVERSE);
		jCheckHotIron.setSelected(indx == JFijiPipe.COLOR_HOTIRON);
		jCheckSplitCursor.setSelected(display3Panel1.splitCursor);
		if( display3Panel1.d3Pipe == null) return;
		indx = -1;
		for( i=0; i<5; i++) {
			if( (int)display3Panel1.d3Pipe.winLevel == ctLevel[i] && (int)display3Panel1.d3Pipe.winWidth == ctWidth[i]) {
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

	void setExternalSpinners(boolean toggle) {
		boolean active = jButScroll.isSelected();
		PetCtFrame.setExternalSpinnersSub( toggle, active, this);
	}

	void externalSpinnerChange( int diff) {
		display3Panel1.incrSlicePosition(0, -diff);
	}

	void resetButScroll() {
		jButScroll.setSelected(false);
	}

	@Override
	public void dispose() {
		savePrefs();
		setExternalSpinners(false);
		display3Panel1.d3Pipe = display3Panel1.mipPipe = null;
		openMode = 0;
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

        jPopupD3 = new javax.swing.JPopupMenu();
        jCheckSplitCursor = new javax.swing.JCheckBoxMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuAuto = new javax.swing.JMenuItem();
        jMenuBrain = new javax.swing.JMenuItem();
        jCheckChest = new javax.swing.JCheckBoxMenuItem();
        jCheckLung = new javax.swing.JCheckBoxMenuItem();
        jCheckLiver = new javax.swing.JCheckBoxMenuItem();
        jCheckBone = new javax.swing.JCheckBoxMenuItem();
        jCheckBrain = new javax.swing.JCheckBoxMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jCheckInverse = new javax.swing.JCheckBoxMenuItem();
        jCheckGrayScale = new javax.swing.JCheckBoxMenuItem();
        jCheckBlues = new javax.swing.JCheckBoxMenuItem();
        jCheckHotIron = new javax.swing.JCheckBoxMenuItem();
        jToolBar1 = new javax.swing.JToolBar();
        jButScroll = new javax.swing.JToggleButton();
        jButMip = new javax.swing.JToggleButton();
        jButFwd = new javax.swing.JToggleButton();
        jButFront = new javax.swing.JToggleButton();
        jButSide = new javax.swing.JToggleButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        jWidthVal = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        jLevelVal = new javax.swing.JTextField();
        display3Panel1 = new Display3Panel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenuFile = new javax.swing.JMenu();
        jMenuExit = new javax.swing.JMenuItem();
        jMenuEdit = new javax.swing.JMenu();
        jMenuFitWindow = new javax.swing.JMenuItem();
        jMenuHelp = new javax.swing.JMenu();
        jMenuContents = new javax.swing.JMenuItem();
        jMenuAbout = new javax.swing.JMenuItem();

        jCheckSplitCursor.setText("Split cursor");
        jCheckSplitCursor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckSplitCursorActionPerformed(evt);
            }
        });
        jPopupD3.add(jCheckSplitCursor);
        jPopupD3.add(jSeparator2);

        jMenuAuto.setText("Auto level");
        jMenuAuto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuAutoActionPerformed(evt);
            }
        });
        jPopupD3.add(jMenuAuto);

        jMenuBrain.setText("Brain");
        jMenuBrain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuBrainActionPerformed(evt);
            }
        });
        jPopupD3.add(jMenuBrain);

        jCheckChest.setText("Chest-Abdomen");
        jCheckChest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckChestActionPerformed(evt);
            }
        });
        jPopupD3.add(jCheckChest);

        jCheckLung.setText("Lung");
        jCheckLung.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckLungActionPerformed(evt);
            }
        });
        jPopupD3.add(jCheckLung);

        jCheckLiver.setText("Liver");
        jCheckLiver.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckLiverActionPerformed(evt);
            }
        });
        jPopupD3.add(jCheckLiver);

        jCheckBone.setText("Bone");
        jCheckBone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoneActionPerformed(evt);
            }
        });
        jPopupD3.add(jCheckBone);

        jCheckBrain.setText("Brain");
        jCheckBrain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBrainActionPerformed(evt);
            }
        });
        jPopupD3.add(jCheckBrain);
        jPopupD3.add(jSeparator3);

        jCheckInverse.setText("Inverse");
        jCheckInverse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckInverseActionPerformed(evt);
            }
        });
        jPopupD3.add(jCheckInverse);

        jCheckGrayScale.setSelected(true);
        jCheckGrayScale.setText("Gray scale");
        jCheckGrayScale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckGrayScaleActionPerformed(evt);
            }
        });
        jPopupD3.add(jCheckGrayScale);

        jCheckBlues.setSelected(true);
        jCheckBlues.setText("The Blues");
        jCheckBlues.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBluesActionPerformed(evt);
            }
        });
        jPopupD3.add(jCheckBlues);

        jCheckHotIron.setText("Hot Iron");
        jCheckHotIron.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckHotIronActionPerformed(evt);
            }
        });
        jPopupD3.add(jCheckHotIron);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jToolBar1.setRollover(true);

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

        jButFwd.setText(">>");
        jButFwd.setFocusable(false);
        jButFwd.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
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
            .addGap(0, 182, Short.MAX_VALUE)
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

        javax.swing.GroupLayout display3Panel1Layout = new javax.swing.GroupLayout(display3Panel1);
        display3Panel1.setLayout(display3Panel1Layout);
        display3Panel1Layout.setHorizontalGroup(
            display3Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        display3Panel1Layout.setVerticalGroup(
            display3Panel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
            .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addComponent(display3Panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(display3Panel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

	private void jMenuExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuExitActionPerformed
		dispose();
	}//GEN-LAST:event_jMenuExitActionPerformed

	private void jButMipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButMipActionPerformed
		swapMipState();
	}//GEN-LAST:event_jButMipActionPerformed

	private void jButFwdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButFwdActionPerformed
		display3Panel1.ActionMipCine(PetCtPanel.CINE_RUNNING);
		setCineButtons(true);
	}//GEN-LAST:event_jButFwdActionPerformed

	private void jButFrontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButFrontActionPerformed
		display3Panel1.ActionMipCine(PetCtPanel.CINE_FORWARD);
		setCineButtons(true);
	}//GEN-LAST:event_jButFrontActionPerformed

	private void jButSideActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButSideActionPerformed
		display3Panel1.ActionMipCine(PetCtPanel.CINE_SIDE);
		setCineButtons(true);
	}//GEN-LAST:event_jButSideActionPerformed

	private void jMenuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAboutActionPerformed
		PetCtAbout dlg = new PetCtAbout(this, true);
		dlg.setVisible(true);
	}//GEN-LAST:event_jMenuAboutActionPerformed

	private void jMenuContentsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuContentsActionPerformed
		openHelp();
	}//GEN-LAST:event_jMenuContentsActionPerformed

	private void jCheckSplitCursorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckSplitCursorActionPerformed
		flipSplitState();
	}//GEN-LAST:event_jCheckSplitCursorActionPerformed

	private void jMenuAutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuAutoActionPerformed
		setWindowLevelAndWidth(0);
	}//GEN-LAST:event_jMenuAutoActionPerformed

	private void jMenuBrainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuBrainActionPerformed
		setWindowLevelAndWidth(9);
	}//GEN-LAST:event_jMenuBrainActionPerformed

	private void jCheckLungActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckLungActionPerformed
		setWindowLevelAndWidth(2);
	}//GEN-LAST:event_jCheckLungActionPerformed

	private void jCheckChestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckChestActionPerformed
		setWindowLevelAndWidth(1);
	}//GEN-LAST:event_jCheckChestActionPerformed

	private void jCheckLiverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckLiverActionPerformed
		setWindowLevelAndWidth(3);
	}//GEN-LAST:event_jCheckLiverActionPerformed

	private void jCheckBoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoneActionPerformed
		setWindowLevelAndWidth(4);
	}//GEN-LAST:event_jCheckBoneActionPerformed

	private void jCheckBrainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBrainActionPerformed
		setWindowLevelAndWidth(5);
	}//GEN-LAST:event_jCheckBrainActionPerformed

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

	private void jMenuFitWindowActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuFitWindowActionPerformed
		fitWindow();
	}//GEN-LAST:event_jMenuFitWindowActionPerformed

	private void jButScrollActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButScrollActionPerformed
		setExternalSpinners(true);
	}//GEN-LAST:event_jButScrollActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private Display3Panel display3Panel1;
    private javax.swing.JToggleButton jButFront;
    private javax.swing.JToggleButton jButFwd;
    private javax.swing.JToggleButton jButMip;
    private javax.swing.JToggleButton jButScroll;
    private javax.swing.JToggleButton jButSide;
    private javax.swing.JCheckBoxMenuItem jCheckBlues;
    private javax.swing.JCheckBoxMenuItem jCheckBone;
    private javax.swing.JCheckBoxMenuItem jCheckBrain;
    private javax.swing.JCheckBoxMenuItem jCheckChest;
    private javax.swing.JCheckBoxMenuItem jCheckGrayScale;
    private javax.swing.JCheckBoxMenuItem jCheckHotIron;
    private javax.swing.JCheckBoxMenuItem jCheckInverse;
    private javax.swing.JCheckBoxMenuItem jCheckLiver;
    private javax.swing.JCheckBoxMenuItem jCheckLung;
    private javax.swing.JCheckBoxMenuItem jCheckSplitCursor;
    private javax.swing.JTextField jLevelVal;
    private javax.swing.JMenuItem jMenuAbout;
    private javax.swing.JMenuItem jMenuAuto;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuBrain;
    private javax.swing.JMenuItem jMenuContents;
    private javax.swing.JMenu jMenuEdit;
    private javax.swing.JMenuItem jMenuExit;
    private javax.swing.JMenu jMenuFile;
    private javax.swing.JMenuItem jMenuFitWindow;
    private javax.swing.JMenu jMenuHelp;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPopupMenu jPopupD3;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JTextField jWidthVal;
    // End of variables declaration//GEN-END:variables
	int openMode = 0;
	Preferences jPrefer = null;
	DoubleRangeSlider dSlide = null;
	String m_patName, m_patID, m_styName;
	int slideDigits = 0;
	int [] ctLevel = null;
	int [] ctWidth = null;
}

