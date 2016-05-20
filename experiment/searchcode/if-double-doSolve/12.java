/* gvSIG. Geographic Information System of the Valencian Government
 *
 * Copyright (C) 2007-2008 Infrastructures and Transports Department
 * of the Valencian Government (CIT)
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 */

/*
 * AUTHORS (In addition to CIT):
 * 2009 Software Colaborativo (www.scolab.es)   development
 */

package org.gvsig.graph.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.SystemColor;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import org.gvsig.exceptions.BaseException;
import org.gvsig.graph.core.GraphException;
import org.gvsig.graph.core.GvFlag;
import org.gvsig.graph.core.IFlagListener;
import org.gvsig.graph.core.Network;
import org.gvsig.graph.core.NetworkUtils;
import org.gvsig.graph.solvers.IDijkstraListener;
import org.gvsig.graph.solvers.OneToManySolver;
import org.gvsig.graph.solvers.ReverseOneToManySolver;
import org.gvsig.graph.solvers.SelectDijkstraListener;
import org.gvsig.graph.tools.FlagListener;
import org.gvsig.graph.tools.SingleFlagListener;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.IWindowListener;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SingleLayerIterator;
import com.iver.cit.gvsig.fmap.tools.Behavior.PointBehavior;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.Locale;

public class ConnectivityControlPanel extends JPanel implements IWindow,
			IWindowListener, IFlagListener {

	private static final long serialVersionUID = 1L;
	private JPanel jPanelSouth = null;
	private JPanel jPanelCenter = null;
	private JPanel jPanelNorth = null;
	private JLabel jLblConnectivity = null;
	private JLabel jLblIcon = null;
	private JTextArea jTextArea = null;
	private JLabel jLblOriginPoint = null;
	private JTextField jTxtOriginPointX = null;
	private JTextField jTxtOriginPointY = null;

	private JToggleButton jToggleButtonSetFlag = null;
	private JLabel jLblAssociatedLayer = null;
	private JComboBox jCboAssociatedLayer = null;
	private JButton jBtnCalculate = null;
	private JButton jBtnClose = null;
	private JPanel jPanelOptions = null;
	private JRadioButton jRadioBtnNormalDirection = null;
	private JRadioButton jRadioBtnReverseDirection = null;
	private JCheckBox jChkBoxUseMaxDist = null;
	private JCheckBox jChkBoxUseMaxCost = null;
	private JTextField jTxtMaxDist = null;
	private JTextField jTxtMaxCost = null;
	private WindowInfo wi;
	private MapControl mapCtrl;

	private SelectDijkstraListener selectListener;
	private GvFlag sourceFlag;
	protected SingleFlagListener flagListener;
	private String lastSelectedTool;
	private JLabel jLblTolerance = null;
	private JTextField jTxtTolerance = null;
	
	/**
	 * This is the default constructor
	 */
	public ConnectivityControlPanel() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(343, 406);
		this.setLayout(new BorderLayout());
		this.add(getJPanelNorth(), BorderLayout.NORTH);
		this.add(getJPanelSouth(), BorderLayout.SOUTH);
		this.add(getJPanelCenter(), BorderLayout.CENTER);
	}

	/**
	 * This method initializes jPanelSouth
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelSouth() {
		if (jPanelSouth == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = 0;
			jPanelSouth = new JPanel();
			jPanelSouth.setLayout(new GridBagLayout());
			jPanelSouth.setPreferredSize(new Dimension(134, 30));
			jPanelSouth.add(getJBtnCalculate(), new GridBagConstraints());
			jPanelSouth.add(getJBtnClose(), gridBagConstraints);
		}
		return jPanelSouth;
	}

	/**
	 * This method initializes jPanelCenter
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelCenter() {
		if (jPanelCenter == null) {
			jLblTolerance = new JLabel();
			jLblTolerance.setBounds(new Rectangle(17, 90, 76, 19));
			jLblTolerance.setText(_T("Tolerance") + ":");
			jLblAssociatedLayer = new JLabel();
			jLblAssociatedLayer.setBounds(new Rectangle(15, 57, 111, 14));
			jLblAssociatedLayer.setText(_T("Associated_layer") + ":");
			jLblOriginPoint = new JLabel();
			jLblOriginPoint.setBounds(new Rectangle(15, 17, 111, 14));
			jLblOriginPoint.setText(_T("Origin_point") + ":");
			jPanelCenter = new JPanel();
			jPanelCenter.setLayout(null);
			jPanelCenter.add(jLblOriginPoint, null);

			JLabel jLblX = new JLabel();
			jLblX.setBounds(new Rectangle(118, 2, 111, 14));
			jLblX.setText("X:");
			JLabel jLblY = new JLabel();
			jLblY.setBounds(new Rectangle(218, 2, 111, 14));
			jLblY.setText("Y:");

			jPanelCenter.add(jLblX, null);
			jPanelCenter.add(jLblY, null);

			jPanelCenter.add(getJTxtOriginPointX(), null);
			jPanelCenter.add(getJTxtOriginPointY(), null);
			jPanelCenter.add(getJToggleButtonSetFlag(), null);
			jPanelCenter.add(jLblAssociatedLayer, null);
			jPanelCenter.add(getJCboAssociatedLayer(), null);
			jPanelCenter.add(getJPanelOptions(), null);
			jPanelCenter.add(jLblTolerance, null);
			jPanelCenter.add(getJTxtTolerance(), null);
		}
		return jPanelCenter;
	}

	/**
	 * This method initializes jPanelNorth
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelNorth() {
		if (jPanelNorth == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = GridBagConstraints.SOUTHWEST;
			gridBagConstraints1.weightx = 1.0D;
			gridBagConstraints1.insets = new Insets(2, 5, 0, 0);
			gridBagConstraints1.gridy = 1;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.anchor = GridBagConstraints.EAST;
			gridBagConstraints2.gridy = 1;
			jLblIcon = new JLabel();
			jLblIcon.setText("");
			jLblIcon.setIcon(new ImageIcon(this.getClass().getClassLoader()
					.getResource("images/wizard_connectivity.png")));
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints.gridwidth = 1;
			gridBagConstraints.ipadx = 7;
			gridBagConstraints.gridheight = 0;
			gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints.insets = new Insets(2, 5, 0, 0);
			gridBagConstraints.ipady = 3;
			gridBagConstraints.weightx = 1.0D;
			gridBagConstraints.gridy = 0;
			jLblConnectivity = new JLabel();
			jLblConnectivity.setText(PluginServices.getText(this,
					"connectivity"));
			jLblConnectivity.setFont(new Font("Dialog", Font.BOLD, 14));
			jPanelNorth = new JPanel();
			jPanelNorth.setLayout(new GridBagLayout());
			jPanelNorth.setPreferredSize(new Dimension(0, 70));
			jPanelNorth.setBackground(SystemColor.text);
			jPanelNorth.setBorder(BorderFactory
					.createEtchedBorder(EtchedBorder.LOWERED));
			jPanelNorth.add(jLblConnectivity, gridBagConstraints);
			jPanelNorth.add(jLblIcon, gridBagConstraints2);
			jPanelNorth.add(getJTextArea(), gridBagConstraints1);
		}
		return jPanelNorth;
	}

	private JTextArea getJTextArea() {
		if (jTextArea == null) {
			jTextArea = new JTextArea();
			jTextArea.setPreferredSize(new Dimension(100, 40));
			jTextArea.setText(PluginServices.getText(this, "connectivity_analysis"));
			jTextArea.setEditable(false);
			jTextArea.setLineWrap(true);
		}
		return jTextArea;
	}

	private JTextField getJTxtOriginPointX() {
		if (jTxtOriginPointX == null) {
			jTxtOriginPointX = new JTextField();
			jTxtOriginPointX.setBounds(new Rectangle(112, 16, 92, 20));
			jTxtOriginPointX.setHorizontalAlignment(JTextField.RIGHT);
			
		}
		return jTxtOriginPointX;
	}

	private JTextField getJTxtOriginPointY() {
		if (jTxtOriginPointY == null) {
			jTxtOriginPointY = new JTextField();
			jTxtOriginPointY.setBounds(new Rectangle(216, 16, 92, 20));
			jTxtOriginPointY.setHorizontalAlignment(JTextField.RIGHT);
		}
		return jTxtOriginPointY;
	}

	/**
	 * This method initializes jToggleButtonSetFlag
	 * 
	 * @return javax.swing.JToggleButton
	 */
	private JToggleButton getJToggleButtonSetFlag() {
		if (jToggleButtonSetFlag == null) {
			jToggleButtonSetFlag = new JToggleButton();
			jToggleButtonSetFlag.setIcon(new ImageIcon(this.getClass()
					.getClassLoader().getResource(
							"images/disconnect_co_004.gif")));
			jToggleButtonSetFlag.setSize(new Dimension(22, 22));
			jToggleButtonSetFlag.setLocation(new Point(311, 16));
			jToggleButtonSetFlag.setToolTipText(PluginServices.getText(this, "set_source_flag"));
			ItemListener itemListener = new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					int state = itemEvent.getStateChange();
					if (state == ItemEvent.SELECTED) {
						doSelectTool();
					} else {						
						System.out.println("Deselected");
					}
				}
			};
			// Attach Listeners
			jToggleButtonSetFlag.addItemListener(itemListener);
		}
		return jToggleButtonSetFlag;
	}

	protected void doSelectTool() {
		System.out.println("Selected");
		if (!mapCtrl.hasTool("addSingleFlag")) // We create it for the first time.
        {
        	flagListener = new SingleFlagListener(mapCtrl);
        	flagListener.setMode(FlagListener.TO_ARC);
            mapCtrl.addMapTool("addSingleFlag", new PointBehavior(flagListener));
        }
		MapContext map = mapCtrl.getMapContext();
		SingleLayerIterator it = new SingleLayerIterator(map.getLayers());
		FLyrVect layer = null;

		while (it.hasNext()) {
			FLayer aux = it.next();
			if (!aux.isActive())
				continue;
			Network net = (Network) aux.getProperty("network");
			if (net != null) {
				net.addFlagListener(this);
			}
		}
		
        mapCtrl.setTool("addSingleFlag");
		
	}

	/**
	 * This method initializes jCboAssociatedLayer
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJCboAssociatedLayer() {
		if (jCboAssociatedLayer == null) {
			jCboAssociatedLayer = new JComboBox();
			jCboAssociatedLayer.setBounds(new Rectangle(112, 54, 192, 20));
		}
		return jCboAssociatedLayer;
	}

	/**
	 * This method initializes jBtnCalculate
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJBtnCalculate() {
		if (jBtnCalculate == null) {
			jBtnCalculate = new JButton();
			jBtnCalculate.setText(_T("Calculate"));
			jBtnCalculate.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					doConnectivityAnalisys();
				}
			});

		}
		return jBtnCalculate;
	}

	protected void doConnectivityAnalisys() {
		MapContext map = mapCtrl.getMapContext();
		SingleLayerIterator it = new SingleLayerIterator(map.getLayers());
		FLyrVect layer = null;

		while (it.hasNext()) {
			FLayer aux = it.next();
			if (!aux.isActive())
				continue;
			Network net = (Network) aux.getProperty("network");
			if (net != null) {
				GvFlag[] flags = null;
				if (getJCboAssociatedLayer().getSelectedIndex() != 0) {
					// use associated layer to put flags.
					String layerName = (String) getJCboAssociatedLayer()
							.getSelectedItem();
					layer = (FLyrVect) map.getLayers().getLayer(
							layerName);
					
					double tolerance = Double.parseDouble(getJTxtTolerance().getText());

					try {
						PluginServices.getMainFrame().getStatusBar().setInfoText(_T("putting_flags_on_network"));
						PluginServices.getMDIManager().setWaitCursor();
						flags = NetworkUtils.putFlagsOnNetwork(layer, net,
								tolerance);
						PluginServices.getMDIManager().restoreCursor();
						PluginServices.getMainFrame().getStatusBar().setInfoText(_T("ready"));
					} catch (BaseException e) {
						PluginServices.getMDIManager().restoreCursor();
						// TODO Auto-generated catch block
						e.printStackTrace();
						JOptionPane.showMessageDialog((Component) PluginServices.getMDIManager().getActiveWindow(),
								e.getMessage());
						return;
					}
				}
				selectListener = new SelectDijkstraListener(net, mapCtrl
						.getMapContext());
				selectListener.startSelection();
				try {
					net.getLayer().getRecordset().clearSelection();
					if (layer != null)
						layer.getRecordset().clearSelection();
					
					if (getJRadioBtnNormalDirection().isSelected()) {
						OneToManySolver solver = new OneToManySolver();
						doSolve(net, selectListener, solver);
					}
					else {
						ReverseOneToManySolver solver = new ReverseOneToManySolver();
						doSolve(net, selectListener, solver);
					}
//					if (flags != null)
//						for (int i=0; i < flags.length; i++)
//							net.removeFlag(flags[i]);
				} catch (GraphException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ReadDriverException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				selectListener.stopSelection();
				if (flags != null) {
					for (int i = 0; i < flags.length; i++) {
						if (flags[i].getCost() != -1) {
							Integer index = (Integer) flags[i].getProperties().get("rec");
							FBitSet bs;
							try {
								bs = layer.getRecordset().getSelection();
								bs.set(index.intValue());
							} catch (ReadDriverException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				mapCtrl.drawMap(true);
				PluginServices.getMainFrame().enableControls();
			}
		} // WHILE
	}


	private void doSolve(Network net, IDijkstraListener selectListener, OneToManySolver solver)
			throws GraphException {
		
		solver.setNetwork(net);
		solver.addListener(selectListener);
		try {			
			if (getJChkBoxUseMaxCost().isSelected()) {
				solver.setMaxCost(Double.parseDouble(getJTxtMaxCost().getText()));
			}
			if (getJChkBoxUseMaxDist().isSelected()) {
				solver.setMaxDistance(Double
						.parseDouble(getJTxtMaxDist().getText()));
			}
		}
		catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, PluginServices.getText(this,
					"error_parsering_number:") + e.getMessage());
			return;
		}
		solver.addListener(selectListener);

		try {
			double x = Double.parseDouble(getJTxtOriginPointX().getText());
			double y = Double.parseDouble(getJTxtOriginPointY().getText());
			double tol = mapCtrl.getViewPort().toMapDistance(FlagListener.pixelTolerance);
			sourceFlag = net.createFlag(x, y, tol);
			if (sourceFlag == null) {
				JOptionPane.showMessageDialog(this, _T("Error positioning point on network."));
				return;				
			}
			solver.setSourceFlag(sourceFlag);
		}
		catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, _T("Error in origin coordinates:" + e.getMessage()));
			return;
		}
		long t1 = System.currentTimeMillis();
		solver.putDestinationsOnNetwork(net.getFlags());
		solver.calculate();
		solver.removeDestinationsFromNetwork(net.getFlags());
		long t2 = System.currentTimeMillis();
		System.out.println("tiempo:" + (t2 - t1));

		GvFlag flags[] = net.getFlags();

		for (int i = 0; i < flags.length; i++) {
			System.out.println("Flag " + i + " " + flags[i].getCost());
		}
	}

	/**
	 * This method initializes jBtnClose
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJBtnClose() {
		if (jBtnClose == null) {
			jBtnClose = new JButton();
			jBtnClose.setText(_T("Close"));
			jBtnClose.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					close();
				}

			});
		}
		return jBtnClose;
	}

	protected void close() {
		PluginServices.getMDIManager().closeWindow(this);
	}

	/**
	 * This method initializes jPanelOptions
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelOptions() {
		if (jPanelOptions == null) {
			jPanelOptions = new JPanel();
			jPanelOptions.setLayout(null);
			jPanelOptions.setBounds(new Rectangle(14, 135, 315, 154));
			jPanelOptions.setBorder(BorderFactory.createTitledBorder(null,
					_T("Options"), TitledBorder.DEFAULT_JUSTIFICATION,
					TitledBorder.DEFAULT_POSITION, new Font("Tahoma",
							Font.PLAIN, 11), Color.black));
			ButtonGroup group = new ButtonGroup();
			group.add(getJRadioBtnNormalDirection());
			group.add(getJRadioBtnReverseDirection());
			jPanelOptions.add(getJRadioBtnNormalDirection(), null);
			jPanelOptions.add(getJRadioBtnReverseDirection(), null);
			jPanelOptions.add(getJChkBoxUseMaxDist(), null);
			jPanelOptions.add(getJChkBoxUseMaxCost(), null);
			jPanelOptions.add(getJTxtMaxDist(), null);
			jPanelOptions.add(getJTxtMaxCost(), null);
		}
		return jPanelOptions;
	}

	/**
	 * This method initializes jRadioBtnNormalDirection
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJRadioBtnNormalDirection() {
		if (jRadioBtnNormalDirection == null) {
			jRadioBtnNormalDirection = new JRadioButton();
			jRadioBtnNormalDirection.setBounds(new Rectangle(15, 25, 200, 21));
			jRadioBtnNormalDirection.setSelected(true);
			jRadioBtnNormalDirection.setText(_T("Normal_direction"));
		}
		return jRadioBtnNormalDirection;
	}

	/**
	 * This method initializes jRadioBtnReverseDirection
	 * 
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getJRadioBtnReverseDirection() {
		if (jRadioBtnReverseDirection == null) {
			jRadioBtnReverseDirection = new JRadioButton();
			jRadioBtnReverseDirection.setBounds(new Rectangle(15, 53, 180, 23));
			jRadioBtnReverseDirection.setText(_T("Reverse_direction"));
			jRadioBtnReverseDirection.setSelected(false);
		}
		return jRadioBtnReverseDirection;
	}

	/**
	 * This method initializes jChkBoxUseMaxDist
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJChkBoxUseMaxDist() {
		if (jChkBoxUseMaxDist == null) {
			jChkBoxUseMaxDist = new JCheckBox();
			jChkBoxUseMaxDist.setBounds(new Rectangle(15, 85, 168, 21));
			jChkBoxUseMaxDist.setText(_T("Use_max_distance") + ":");
		}
		return jChkBoxUseMaxDist;
	}

	/**
	 * This method initializes jChkBoxUseMaxCost
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJChkBoxUseMaxCost() {
		if (jChkBoxUseMaxCost == null) {
			jChkBoxUseMaxCost = new JCheckBox();
			jChkBoxUseMaxCost.setBounds(new Rectangle(15, 112, 167, 23));
			jChkBoxUseMaxCost.setText(_T("Use_max_cost") + ":");
		}
		return jChkBoxUseMaxCost;
	}

	/**
	 * This method initializes jTxtMaxDist
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTxtMaxDist() {
		if (jTxtMaxDist == null) {
			jTxtMaxDist = new JTextField();
			jTxtMaxDist.setBounds(new Rectangle(190, 86, 95, 20));
		}
		return jTxtMaxDist;
	}

	/**
	 * This method initializes jTxtMaxCost
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTxtMaxCost() {
		if (jTxtMaxCost == null) {
			jTxtMaxCost = new JTextField();
			jTxtMaxCost.setBounds(new Rectangle(190, 112, 95, 20));
		}
		return jTxtMaxCost;
	}

	public WindowInfo getWindowInfo() {
		if (wi == null) {
			wi = new WindowInfo(WindowInfo.MODELESSDIALOG | WindowInfo.PALETTE);
			wi.setWidth(365);
			wi.setHeight(406);
			wi.setMinimumSize(new Dimension(345, 406));
			wi.setTitle(PluginServices.getText(this, "connectivity_analysis")
					+ "...");
		}
		return wi;

	}

	public void setMapControl(MapControl mc) throws ReadDriverException {
		this.mapCtrl = mc;
		this.lastSelectedTool = mc.getCurrentTool();
		SingleLayerIterator it = new SingleLayerIterator(mc.getMapContext()
				.getLayers());
		DefaultComboBoxModel model = (DefaultComboBoxModel) getJCboAssociatedLayer()
				.getModel();
		model.removeAllElements();
		model.addElement(PluginServices.getText(this, "none"));
		while (it.hasNext()) {
			FLayer aux = it.next();
			if (aux instanceof FLyrVect) {
				FLyrVect lv = (FLyrVect) aux;
				if (!lv.isAvailable())
					continue;
				if (lv.getShapeType() == FShape.POINT) {
					model.addElement(lv.getName());
				}
				if (!aux.isActive())
					continue;
				Network net = (Network) aux.getProperty("network");
				if (net != null) {
					GvFlag[] flags = net.getFlags(); 
					if (flags.length == 1) {
						NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
						nf.setGroupingUsed(false);
						nf.setMaximumFractionDigits(6);
						String auxX = nf.format(flags[0].getOriginalPoint().getX());
						String auxY = nf.format(flags[0].getOriginalPoint().getY());
						getJTxtOriginPointX().setText(auxX);
						getJTxtOriginPointY().setText(auxY);
					}
				}
				
			}
		}
	}

	public Object getWindowProfile() {
		// TODO Auto-generated method stub
		return null;
	}

	public void windowActivated() {
		// TODO Auto-generated method stub
		
	}

	public void windowClosed() {
		this.mapCtrl.setTool(lastSelectedTool);
		
	}

	public void flagsChanged(int reason) {
		if (reason == IFlagListener.FLAG_ADDED) {
			MapContext map = mapCtrl.getMapContext();
			SingleLayerIterator it = new SingleLayerIterator(map.getLayers());
			FLyrVect layer = null;

			while (it.hasNext()) {
				FLayer aux = it.next();
				if (!aux.isActive())
					continue;
				Network net = (Network) aux.getProperty("network");
				if (net != null) {
					GvFlag[] flags = net.getFlags(); 
					if (flags.length == 1) {
						NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
						nf.setGroupingUsed(false);
						nf.setMaximumFractionDigits(6);
						String auxX = nf.format(flags[0].getOriginalPoint().getX());
						String auxY = nf.format(flags[0].getOriginalPoint().getY());
						getJTxtOriginPointX().setText(auxX);
						getJTxtOriginPointY().setText(auxY);
					}
				}
			} // while
		}
		
	}
	private String _T(String str) {
		return PluginServices.getText(this, str);
	}

	/**
	 * This method initializes jTxtTolerance	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTxtTolerance() {
		if (jTxtTolerance == null) {
			jTxtTolerance = new JTextField();
			jTxtTolerance.setBounds(new Rectangle(112, 89, 78, 20));
			jTxtTolerance.setHorizontalAlignment(JTextField.RIGHT);
			jTxtTolerance.setText("10");
		}
		return jTxtTolerance;
	}

} // @jve:decl-index=0:visual-constraint="10,10"

