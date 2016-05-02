/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
 *
 * Copyright (C) 2005 IVER T.I. and Generalitat Valenciana.
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib??ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */

/* CVS MESSAGES:
 *
 * $Id: RouteControlPanel.java 34304 2010-12-22 11:00:05Z fpenarrubia $
 * $Log$
 * Revision 1.26  2007-09-07 11:29:47  fjp
 * Casi compila. Falta arreglar lo de FArrowSymbol y retocar el graphiclist de FMap.
 *
 * Revision 1.25.2.5  2007/08/06 16:54:34  fjp
 * Versi?n en desarrollo con velocidades y un esbozo de ?reas de influencia
 *
 * Revision 1.25.2.4  2007/06/14 10:02:25  fjp
 * Pliego de redes a publicar SIN el cuadro de di?logo setVelocities (bueno, con , pero invisible)
 *
 * Revision 1.25.2.3  2007/05/24 11:33:36  fjp
 * Para que puedas a?adir los puntos que est?n cerca de la red. Los que no lo est?n, lanzan un error informando de qu? punto no est? cerca de la red.
 *
 * Revision 1.25.2.2  2007/05/15 07:08:21  fjp
 * Para calcular matrices de distancias
 *
 * Revision 1.25  2006/11/14 18:32:32  fjp
 * *** empty log message ***
 *
 * Revision 1.24  2006/11/14 16:12:01  fjp
 * *** empty log message ***
 *
 * Revision 1.23  2006/11/14 09:23:30  fjp
 * cargar paradas desde cualquier tema de puntos
 *
 * Revision 1.22  2006/11/10 13:57:04  fjp
 * *** empty log message ***
 *
 * Revision 1.21  2006/11/09 21:08:32  azabala
 * *** empty log message ***
 *
 * Revision 1.20  2006/11/09 12:51:12  jaume
 * *** empty log message ***
 *
 * Revision 1.19  2006/11/09 11:00:43  jaume
 * *** empty log message ***
 *
 * Revision 1.18  2006/11/09 10:59:53  jaume
 * *** empty log message ***
 *
 * Revision 1.17  2006/11/09 10:27:50  fjp
 * *** empty log message ***
 *
 * Revision 1.16  2006/11/09 10:24:11  fjp
 * *** empty log message ***
 *
 * Revision 1.15  2006/11/09 09:16:35  fjp
 * Ya va!!
 *
 * Revision 1.14  2006/11/08 20:14:52  azabala
 * *** empty log message ***
 *
 * Revision 1.13  2006/11/08 19:32:22  azabala
 * saveroute and saveflags modifications
 *
 * Revision 1.12  2006/11/08 18:16:28  fjp
 * *** empty log message ***
 *
 * Revision 1.11  2006/11/08 16:48:19  fjp
 * *** empty log message ***
 *
 * Revision 1.10  2006/11/08 16:00:39  fjp
 * Por terminar el enlace flags-cuadro de di?logo
 *
 * Revision 1.9  2006/11/08 13:18:46  fjp
 * Por terminar el enlace flags-cuadro de di?logo
 *
 * Revision 1.8  2006/11/07 19:49:38  azabala
 * *** empty log message ***
 *
 * Revision 1.7  2006/11/06 13:13:53  azabala
 * *** empty log message ***
 *
 * Revision 1.6  2006/11/06 10:29:32  jaume
 * *** empty log message ***
 *
 * Revision 1.5  2006/11/03 19:39:29  azabala
 * *** empty log message ***
 *
 * Revision 1.4  2006/10/27 18:26:22  azabala
 * added implementation of load stages method
 *
 * Revision 1.3  2006/10/27 12:41:09  jaume
 * GUI
 *
 * Revision 1.2  2006/10/26 16:31:21  jaume
 * GUI
 *
 * Revision 1.1  2006/10/25 10:50:41  jaume
 * movement of classes and gui stuff
 *
 * Revision 1.4  2006/10/24 08:04:41  jaume
 * *** empty log message ***
 *
 * Revision 1.3  2006/10/23 16:00:20  jaume
 * *** empty log message ***
 *
 * Revision 1.2  2006/10/23 08:05:39  jaume
 * GUI
 *
 * Revision 1.1  2006/10/20 12:02:50  jaume
 * GUI
 *
 *
 */
package org.gvsig.graph.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;
import org.gvsig.exceptions.BaseException;
import org.gvsig.fmap.layers.LayerListenerAdapter;
import org.gvsig.graph.core.GraphException;
import org.gvsig.graph.core.GvFlag;
import org.gvsig.graph.core.IFlagListener;
import org.gvsig.graph.core.Network;
import org.gvsig.graph.core.NetworkUtils;
import org.gvsig.graph.solvers.FlagsMemoryDriver;
import org.gvsig.graph.solvers.Route;
import org.gvsig.graph.solvers.RouteMemoryDriver;
import org.gvsig.graph.tools.FlagListener;
import org.gvsig.gui.beans.swing.GridBagLayoutPanel;
import org.gvsig.gui.beans.swing.JBlank;
import org.gvsig.gui.beans.swing.JButton;

import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.SingletonWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.addlayer.AddLayerDialog;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.drivers.DriverAttributes;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerEvent;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.SingleLayerIterator;
import com.iver.cit.gvsig.project.documents.view.gui.IView;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.iver.cit.gvsig.util.GvSession;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

public class RouteControlPanel extends JPanel implements SingletonWindow,
		ActionListener, IFlagListener {
	private static Logger logger = Logger.getLogger(RouteControlPanel.class.getName());

	WindowInfo wi;

	protected JPanel westPanel = null;

	protected JScrollPane scrlStages = null;

	protected JTable tblStages = null;

	protected GridBagLayoutPanel eastPanel = null;

	private MyTableModel tableModel = new MyTableModel(); // @jve:decl-index=0:visual-constraint=""

	private JButton btnLoadStage = null;

	private JButton btnSaveStage = null;

	private JButton btnSetVelocities = null;

	Network network;

	private ArrayList routeFlags;

	JLabel lblCost;

	private JLabel lblFlagAmout;

	private JButton btnPullDownStage;

	private JButton btnPushUpStage;

	private JButton btnRemoveStage;

	private JPanel southPanel;

	String[] colName = new String[] {
			PluginServices.getText(this, "enable"),
			PluginServices.getText(this, "stage"),
			PluginServices.getText(this, "cost"), };

	private JButton btnCenterOnFlag;

	private JButton btnSaveRoute;

	MapControl mapCtrl;

	private JCheckBox chkTSP;

	private JCheckBox chkReturnToOrigin;

	private boolean bDoTSP = false;

	private boolean bReturnToOrigin = false;

	protected JPanel panelButtonsEast = new JPanel();

	private JTextField txtTolerance;

	private class MyTableModel extends AbstractTableModel {
		public int getColumnCount() {
			return 3;
		}

		public int getRowCount() {
			return _getFlags().size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			// Parece un fallo de java!! ?? Tengo que controlar que rowIndex no sea mayor que getRowCount()!!
			if (rowIndex >= getRowCount())
				return null;
			
			GvFlag flag = (GvFlag) _getFlags().get(rowIndex);
			switch (columnIndex) {
			case 0:
				return new Boolean(flag.isEnabled());
			case 1:
				return flag.getDescription();
			case 2:
				return new Double(flag.getCost());
			}

			return null;
		}

		public Class getColumnClass(int columnIndex) {
			switch (columnIndex)
			{
			case 0:
				return Boolean.class;
			case 1:
				return String.class;
			case 2:
				return Double.class;

			}
			return super.getColumnClass(columnIndex);
		}

		public String getColumnName(int column) {
			return colName[column];
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (columnIndex == 2)
				return false;
			return true;

		}

		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (rowIndex >= getRowCount())
				return;
			GvFlag flag = (GvFlag) _getFlags().get(rowIndex);
			switch (columnIndex)
			{
			case 0:
				Boolean bAux = (Boolean) aValue;
				flag.setEnabled(bAux.booleanValue());
				PluginServices.getMainFrame().enableControls();
				return;
			case 1:
				String strAux = (String) aValue;
				flag.setDescription(strAux);
				return;

//			case 2: // No es editable
//				Double dblAux = (Double) aValue;
//				flag.setCost(dblAux.doubleValue());
//				return;

			}

		}

	}

	/**
	 * This method initializes
	 *
	 */
	public RouteControlPanel(Network network) {
		super();
		this.network = network;
//		GvFlag[] flags = network.getFlags();
//		List vflags = _getFlags();
//		for (int i = 0; i < flags.length; i++) {
//			vflags.add(flags[i]);
//		}
		initialize();
	}

	public void refresh(){
//		GvFlag[] flags = network.getFlags();
//		List vflags = _getFlags();
//		List tempFlags = Arrays.asList(flags);
//		vflags.addAll(tempFlags);
		lblFlagAmout.setText(String.valueOf(_getFlags()
				.size()));
		updateTotalCost();
		ListSelectionModel rowSM = tblStages.getSelectionModel();
		getChkTSP().setEnabled((_getFlags().size() > 2));
		getChkReturnToOrigin().setEnabled((_getFlags().size() > 1));
		if (_getFlags().size() == 0)
		{
			getBtnCenterOnFlag().setEnabled(false);
			getBtnSaveStage().setEnabled(false);
		}
		else
		{
			getBtnCenterOnFlag().setEnabled(!rowSM.isSelectionEmpty());
			getBtnSaveStage().setEnabled(true);
		}
		getTblStages().revalidate();
		System.out.println("Actualizo tabla");

	}
	private void processLayer(FLayer layer) {
		if (layer.isActive()) {
			if (layer instanceof FLyrVect) {
				Network net = (Network) layer
						.getProperty("network");
				if (net != null) {
					network = net;
					network.addFlagListener(this);
					refresh();
				}// if
			}// if
		}// if

	}

	public void setMapControl(MapControl mapCtrl, Network net) {
		if (mapCtrl != null) {
			this.mapCtrl = mapCtrl;
			LayerListenerAdapter listener = new LayerListenerAdapter() {
				public void activationChanged(LayerEvent e) {
					processLayer(e.getSource());
				}
			};
			mapCtrl.getMapContext().getLayers().addLayerListener(listener);
			mapCtrl.getMapContext().getLayers().addLayerCollectionListener(
					listener);
			if (net != null) {
				network = net;
				network.addFlagListener(this);
			}
		}
	}

	public RouteControlPanel() {
		super();
		initialize();
	}

	public void setCostUnits(String unitsName) {
		colName[2] = unitsName;
		getTblStages().repaint();
	}

	/**
	 * This method initializes this
	 *
	 */
	protected void initialize() {
		BorderLayout borderLayout = new BorderLayout();
		borderLayout.setHgap(10);
		borderLayout.setVgap(10);
		JPanel cont = new JPanel(borderLayout);
//		cont.setPreferredSize(new Dimension(490, 320));
		this.setPreferredSize(new Dimension(460, 280));
		cont.add(getWestPanel(), BorderLayout.CENTER);
		cont.add(getEastPanel(), BorderLayout.EAST);
		cont.add(getSouthPanel(), BorderLayout.SOUTH);
		this.setLayout(new FlowLayout(FlowLayout.LEADING, 10, 10));
		this.add(cont);
		updateFlags();
	}

	protected void updateFlags() {
		lblFlagAmout.setText(String.valueOf(_getFlags().size()));
		updateTotalCost();
		getTblStages().repaint();
	}

	protected JPanel getSouthPanel() {
		if (southPanel == null) {
			southPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
			southPanel.add(getBtnRemoveStage());
			southPanel.add(new JBlank(50, 20));
			southPanel.add(getBtnPushUpStage());
			southPanel.add(getBtnPullDownStage());
			southPanel.add(new JLabel(PluginServices.getText(this,
					"flag_amount")));
			lblFlagAmout = new JLabel();
			lblFlagAmout.setFont(lblFlagAmout.getFont().deriveFont(Font.BOLD));
			southPanel.add(lblFlagAmout);
		}
		return southPanel;
	}

	public Object getWindowModel() {
		return this.getClass();
	}

	public WindowInfo getWindowInfo() {
		if (wi == null) {
			wi = new WindowInfo(WindowInfo.MODELESSDIALOG
				    | WindowInfo.MAXIMIZABLE
					| WindowInfo.ICONIFIABLE | WindowInfo.PALETTE);
			wi.setWidth((int) this.getPreferredSize().getWidth() + 10);
			wi.setHeight(300);
			wi.setTitle(PluginServices.getText(this, "route_control_panel"));
		}
		return wi;
	}

	public Object getWindowProfile(){
		return WindowInfo.TOOL_PROFILE;
	}


	/**
	 * This method initializes westPanel
	 *
	 * @return javax.swing.JPanel
	 */
	protected JPanel getWestPanel() {
		if (westPanel == null) {
			westPanel = new JPanel(new BorderLayout(5, 5));
			lblCost = new JLabel();
			lblCost.setFont(lblCost.getFont().deriveFont(Font.BOLD));
			GridBagLayoutPanel aux = new GridBagLayoutPanel();
			aux.addComponent(PluginServices.getText(this, "total_route_cost")
					+ ":", lblCost);
			aux.addComponent(getScrlStages());

			westPanel.add(aux);
		}
		return westPanel;
	}

	private JButton getBtnPullDownStage() {
		if (btnPullDownStage == null) {
			btnPullDownStage = new JButton(new ImageIcon(this.getClass()
					.getClassLoader().getResource("images/down-arrow.png")));
			// btnPullDownStage.setName("btnPullDownStage");
			btnPullDownStage.addActionListener(this);
		}
		return btnPullDownStage;
	}

	private JButton getBtnPushUpStage() {
		if (btnPushUpStage == null) {
			btnPushUpStage = new JButton(new ImageIcon(this.getClass()
					.getClassLoader().getResource("images/up-arrow.png")));
			// btnPushUpStage.setName("btnPushUpStage");
			btnPushUpStage.addActionListener(this);
		}
		return btnPushUpStage;
	}

	private JButton getBtnRemoveStage() {
		if (btnRemoveStage == null) {
			btnRemoveStage = new JButton(new ImageIcon(this.getClass()
					.getClassLoader().getResource("images/delete.png")));
			// btnRemoveStage.setName("btnRemoveStage");
			btnRemoveStage.addActionListener(this);
		}
		return btnRemoveStage;
	}

	/**
	 * This method initializes scrlStages
	 *
	 * @return javax.swing.JScrollPane
	 */
	protected JScrollPane getScrlStages() {
		if (scrlStages == null) {
			scrlStages = new JScrollPane();
			scrlStages.setViewportView(getTblStages());
			scrlStages.setPreferredSize(new Dimension(270, 200));
		}
		return scrlStages;
	}

	/**
	 * @deprecated
	 * @param flag
	 */
	public void addFlag(GvFlag flag) {
		_getFlags().add(flag);
		lblFlagAmout.setText(String.valueOf(_getFlags().size()));
		updateTotalCost();
		getTblStages().repaint();
	}

	private void updateTotalCost() {
		GvFlag[] flags = getFlags();
		double cost = 0;
		for (int i = 0; i < flags.length; i++) {
			if (!flags[i].isEnabled())
				continue;
			getBtnSaveStage().setEnabled(true);
			if (flags[i].getCost() >= Double.MAX_VALUE)
			{
				lblCost.setText(PluginServices.getText(this, "no_se_puede_pasar_por_todas_las_paradas"));
				return;
			}
			if (flags[i].getCost() < 0)
			{
				lblCost.setText(PluginServices.getText(this, "solucion_no_valida"));
				return;
			}

			cost = flags[i].getCost(); // The last flag shows total cost
		}
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		lblCost.setText(nf.format(cost) + getCostUnitName());
	}

	private String getCostUnitName() {
		if (colName[2].equals(PluginServices.getText(this, "cost")))
			return "";
		return colName[2];
	}

	public void removeFlag(GvFlag flag) {
		removeFlag(_getFlags().indexOf(flag));
	}

	public void removeFlag(int index) {
		_getFlags().remove(index);
		lblFlagAmout.setText(String.valueOf(_getFlags().size()));
		getTblStages().repaint();
	}

	/**
	 * This method initializes tblStages
	 *
	 * @return javax.swing.JTable
	 */
	protected JTable getTblStages() {
		if (tblStages == null) {
			tblStages = new JTable();
			tblStages
					.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
			tblStages.setModel(getTableModel());
			TableColumnModel cm = tblStages.getColumnModel();

			int tablePreferredWidth = (int) tblStages.getPreferredSize()
					.getWidth();
			int colSize = tblStages.getFontMetrics(tblStages.getFont())
					.stringWidth(tblStages.getModel().getColumnName(0)) * 2;
			cm.getColumn(0).setPreferredWidth((int) (colSize));
			cm.getColumn(0).setMinWidth((int) (colSize));
			cm.getColumn(0).setMaxWidth((int) (colSize));
			tablePreferredWidth -= colSize;
			cm.getColumn(1)
					.setPreferredWidth((int) (tablePreferredWidth * 0.7));
			cm.getColumn(2)
					.setPreferredWidth((int) (tablePreferredWidth * 0.3));

			// Ask to be notified of selection changes.
			ListSelectionModel rowSM = tblStages.getSelectionModel();
			rowSM.addListSelectionListener(new ListSelectionListener() {
			    public void valueChanged(ListSelectionEvent e) {
			        //Ignore extra messages.
			        if (e.getValueIsAdjusting())
			        	return;

			        ListSelectionModel lsm =
			            (ListSelectionModel)e.getSource();
		        	getBtnCenterOnFlag().setEnabled(!lsm.isSelectionEmpty());
					int[] selected = tblStages.getSelectedRows();
					if (selected.length == 0)
						return;
					GvFlag flag = (GvFlag) _getFlags().get(selected[0]);
					Point2D p = flag.getOriginalPoint();
		        	mapCtrl.repaint(); // borramos el de antes
		        	NetworkUtils.flashPoint(mapCtrl, p.getX(), p.getY());
			    }
			});

			tblStages.getModel().addTableModelListener(new TableModelListener() {

				public void tableChanged(TableModelEvent e) {
					System.out.println("Table model changed");
//					getBtnCenterOnFlag().setEnabled(false);
				}

			});


		}
		return tblStages;
	}

	protected int getFlagCount() {
		return _getFlags().size();
	}

	List _getFlags() {
//		if (routeFlags == null) {
//			routeFlags = new ArrayList();
//		}
//		return routeFlags;

		return network.getOriginaFlags();
	}

	protected boolean isSelected(int row) {
		return ((GvFlag) _getFlags().get(row)).isEnabled();
	}

	/**
	 * This method initializes eastPanel
	 *
	 * @return javax.swing.JPanel
	 */
	protected JPanel getEastPanel() {
		if (eastPanel == null) {
			GridLayout layout = new GridLayout();
			layout.setColumns(1);
			layout.setVgap(5);
			
			panelButtonsEast.add(getBtnLoadStage());
			panelButtonsEast.add(getBtnSaveStage());
			panelButtonsEast.add(getBtnSaveRoute());
			panelButtonsEast.add(getBtnCenterOnFlag());
			panelButtonsEast.add(getBtnSetVelocities());
			
			panelButtonsEast.add(new JLabel(PluginServices.getText(this, "tolerance") + ":"));
			panelButtonsEast.add(getTxtTolerance());
			panelButtonsEast.add(getChkTSP());
			panelButtonsEast.add(getChkReturnToOrigin());
			layout.setRows(panelButtonsEast.getComponentCount());
			panelButtonsEast.setLayout(layout);
			eastPanel = new GridBagLayoutPanel();
			eastPanel.addComponent(panelButtonsEast);
		}
		return eastPanel;
	}

	public JTextField getTxtTolerance() {
		if (txtTolerance == null) {
			txtTolerance = new JTextField();
			txtTolerance.setToolTipText(PluginServices.getText(this, "map_units"));
			txtTolerance.setText(PluginServices.getText(this,
					"10"));
		}
		return txtTolerance;
	}

	protected JCheckBox getChkTSP() {
		if (chkTSP == null) {
			chkTSP = new JCheckBox();
			chkTSP.setText(PluginServices.getText(this,
					"order_stops"));
//			chkTSP.setEnabled(false);
			chkTSP.addActionListener(this);
		}
		return chkTSP;
	}

	protected JCheckBox getChkReturnToOrigin() {
		if (chkReturnToOrigin == null) {
			chkReturnToOrigin = new JCheckBox();
			chkReturnToOrigin.setText(PluginServices.getText(this,
					"return_to_origin"));
//			chkReturnToOrigin.setEnabled(false);
			chkReturnToOrigin.addActionListener(this);
		}
		return chkReturnToOrigin;
	}

	JButton getBtnCenterOnFlag() {
		if (btnCenterOnFlag == null) {
			btnCenterOnFlag = new JButton();
			btnCenterOnFlag.setText(PluginServices.getText(this,
					"center_on_flag"));
			btnCenterOnFlag.setEnabled(false);
			btnCenterOnFlag.addActionListener(this);
		}
		return btnCenterOnFlag;
	}

	private JButton getBtnSetVelocities() {
		if (btnSetVelocities == null) {
			btnSetVelocities = new JButton();
			btnSetVelocities.setText(PluginServices.getText(this,
					"set_velocities"));
//			btnSetVelocities.setEnabled(false);

			// TODO: PONERLO VISIBLE CUANDO SE CORRIJA LO DE ABRIR EL
			// DIALOGO Y LEER LAS VELOCIDADES QUE HAB?A ANTES.
			btnSetVelocities.setVisible(true);
			btnSetVelocities.addActionListener(this);
		}
		return btnSetVelocities;
	}

	private JButton getBtnSaveRoute() {
		if (btnSaveRoute == null) {
			btnSaveRoute = new JButton();
			btnSaveRoute.setText(PluginServices.getText(this, "save_route"));
			btnSaveRoute.addActionListener(this);
		}
		return btnSaveRoute;
	}

	/**
	 * This method initializes defaultTableModel
	 *
	 * @return javax.swing.table.DefaultTableModel
	 */
	protected TableModel getTableModel() {
		return tableModel;
	}

	/**
	 * This method initializes btnLoadStage
	 *
	 * @return javax.swing.JButton
	 */
	protected JButton getBtnLoadStage() {
		if (btnLoadStage == null) {
			btnLoadStage = new JButton();
			btnLoadStage.setText(PluginServices.getText(this, "load_stages"));
			btnLoadStage.addActionListener(this);
		}
		return btnLoadStage;
	}

	/**
	 * This method initializes btnSaveStages
	 *
	 * @return javax.swing.JButton
	 */
	protected JButton getBtnSaveStage() {
		if (btnSaveStage == null) {
			btnSaveStage = new JButton();
			btnSaveStage.setText(PluginServices.getText(this, "save_stages"));
			btnSaveStage.setEnabled(false);
			btnSaveStage.addActionListener(this);
		}
		return btnSaveStage;
	}

	public void actionPerformed(ActionEvent e) {
		Component c = (Component) e.getSource();
		if (c.equals(getBtnLoadStage())) {
			loadStages();
		} else if (c.equals(getBtnSaveStage())) {
			saveStage();
		} else if (c.equals(getBtnPushUpStage())) {
			int[] selected = tblStages.getSelectedRows();
			if (selected.length == 0 || selected[0] == 0)
				return;
			tblStages.clearSelection();
			for (int i = 0; i < selected.length; i++) {
				Object aux = _getFlags().get(selected[i] - 1);
				_getFlags().set(selected[i] - 1, _getFlags().get(selected[i]));
				_getFlags().set(selected[i], aux);
				selected[i]--;
				tblStages.addRowSelectionInterval(selected[i], selected[i]);
			}
			invalidateSolution();
		} else if (c.equals(getBtnPullDownStage())) {
			// pull down
			int[] selected = tblStages.getSelectedRows();
			if (selected.length == 0
					|| selected[selected.length - 1] >= _getFlags().size() - 1)
				return;

			// move rows
			tblStages.clearSelection();
			for (int i = selected.length - 1; i >= 0; i--) {
				Object aux = _getFlags().get(selected[i] + 1);
				_getFlags().set(selected[i] + 1, _getFlags().get(selected[i]));
				_getFlags().set(selected[i], aux);
				selected[i]++;
				tblStages.addRowSelectionInterval(selected[i], selected[i]);
			}
			invalidateSolution();

		} else if (c.equals(getBtnRemoveStage())) {
			removeStage();
		} else if (c.equals(getBtnSaveRoute())) {
			saveRoute();
		} else if (c.equals(getBtnSetVelocities())) {
			setVelocities();
		} else if (c.equals(getChkTSP())) {
			bDoTSP = getChkTSP().isSelected();
		} else if (c.equals(getChkReturnToOrigin())) {
			bReturnToOrigin = getChkReturnToOrigin().isSelected();
		} else if (c.equals(getBtnCenterOnFlag())) {
			// Center on first selected flag.
			int[] selected = tblStages.getSelectedRows();
			if (selected.length == 0)
				return;
			GvFlag flag = (GvFlag) _getFlags().get(selected[0]);
			IWindow window = PluginServices.getMDIManager().getActiveWindow();
			if(! (window instanceof IView))
				return;
			IView view = (IView) window;
			ViewPort vp = view.getMapControl().getViewPort();
			Rectangle2D extent = vp.getAdjustedExtent();
			double xNC = flag.getOriginalPoint().getX();
			double yNC = flag.getOriginalPoint().getY();
			double width = extent.getWidth();
			double height = extent.getHeight();

			Rectangle2D.Double r = new Rectangle2D.Double();

			r.width = width;
			r.height = height;
			r.x = xNC - width/2;
			r.y = yNC - height/2;

			vp.setExtent(r);



		}
		repaint();
	}

	private void setVelocities() {
		if(network == null){
			IWindow window = PluginServices.getMDIManager().getActiveWindow();
			if(! (window instanceof IView))
				return;
			IView view = (View) window;
			MapControl mapControl = view.getMapControl();
			MapContext map = mapControl.getMapContext();
			FLayers layers = map.getLayers();
			SingleLayerIterator it = new SingleLayerIterator(layers);
			while (it.hasNext() && network == null) {
				FLayer aux = it.next();
				if (!aux.isActive())
					continue;
				network = (Network) aux.getProperty("network");
			}
		}
		if (network == null)
			return;

		ArrayList lstTipoTramo = network.getEdgeTypes();

		// I try to use a generic multiinputdialog. All this stuff can be extracted
		// from here and subclass MultiInputDlg
		Hashtable veloMeters  = network.getVelocities();
		ArrayList veloKm = new ArrayList(lstTipoTramo.size());
		if (veloMeters != null)
		{
			NumberFormat nf = NumberFormat.getInstance();
			for (int i=0; i<lstTipoTramo.size(); i++)
			{
				Object key = lstTipoTramo.get(i);
				Double velM = (Double) veloMeters.get(key);
				System.out.println("Vel. Metros / seg = " + velM);
				if (velM != null)
					veloKm.add(i, nf.format(velM.doubleValue() * 3.6));
				else
					veloKm.add(i, "0");
			} // for
		}
		else
		{
			for (int i=0; i<lstTipoTramo.size(); i++)
			{
				veloKm.add(i, "60");
			} // for
		}
		try
		{
			String msg = PluginServices.getText(this, "msg_set_velocities");
			MultiInputDlg dlg = new MultiInputDlg(msg, lstTipoTramo, veloKm);
			dlg.setResizable(true);
			String col1 = PluginServices.getText(this, "col_arc_type");
			String col2 = PluginServices.getText(this, "col_km_per_hour");
			dlg.setColumnNames(col1, col2);
			dlg.setModal(true);
			dlg.setVisible(true);
			if (dlg.isCanceled())
				return;

			veloKm = dlg.getRightValues(); //{120, 110, 90, 80, 70, 60, 50, 40};
			veloMeters = new Hashtable(veloKm.size());
			for (int i=0; i<veloKm.size(); i++)
			{
				Object key = lstTipoTramo.get(i);
				veloMeters.put(key, new Double(Double.parseDouble((String) veloKm.get(i)) / 3.6));
			}

			network.setVelocities(veloMeters);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}


	}

	private void invalidateSolution() {
		for (int i=0; i < _getFlags().size(); i++)
		{
			GvFlag flag = (GvFlag) _getFlags().get(i);
			flag.setCost(-1.0);
		}
		updateTotalCost();
	}

	private void saveStage(){
		if(network == null){
			IWindow window = PluginServices.getMDIManager().getActiveWindow();
			if(! (window instanceof IView))
				return;
			IView view = (View) window;
			MapControl mapControl = view.getMapControl();
			MapContext map = mapControl.getMapContext();
			FLayers layers = map.getLayers();
			SingleLayerIterator it = new SingleLayerIterator(layers);
			while (it.hasNext() && network == null) {
				FLayer aux = it.next();
				if (!aux.isActive())
					continue;
				network = (Network) aux.getProperty("network");
			}
		}
		if (network == null)
			return;

		List features = new ArrayList();
		GvFlag[] flags = network.getFlags();
		for(int i = 0; i < flags.length; i++){
			GvFlag flag = flags[i];
			// Solo se guardan los flags habilitados
			if (!flag.isEnabled())
				continue;
			FPoint2D point = new FPoint2D(flag.getOriginalPoint());
			Value[] values = new Value[6];
			values[GvFlag.ID_FLAG_INDEX] = ValueFactory.createValue(flag.getIdFlag());
			values[GvFlag.ID_ARC_INDEX] = ValueFactory.createValue(flag.getIdArc());
			values[GvFlag.DESCRIPTION_INDEX] = ValueFactory.createValue(flag.getDescription());
			values[GvFlag.DIREC_INDEX] = ValueFactory.createValue(flag.getDirec());
			values[GvFlag.PCT_INDEX] = ValueFactory.createValue(flag.getPct());
			values[GvFlag.COST_INDEX] = ValueFactory.createValue(flag.getCost());
			IGeometry geo = ShapeFactory.createPoint2D(point);
			DefaultFeature feature = new DefaultFeature(geo, values, new Integer(flag.getIdFlag()).toString() );
			features.add(feature);
		}

		FlagsMemoryDriver driver = new FlagsMemoryDriver(features);
		IProjection projection = AddLayerDialog.getLastProjection();
		FLyrVect routeLayer = (FLyrVect) LayerFactory.createLayer("Flags",
				driver, projection);

		FormatSelectionPanel selectionPanel =
			new FormatSelectionPanel(PluginServices.
					getText(null,
				"Seleccione_un_formato_para_guardar_los_flags"));
		PluginServices.getMDIManager().addWindow(selectionPanel);
		if (selectionPanel.isOkButtonPressed() == false)
			return;
		
		String format = selectionPanel.getSelectedFormat();
		com.iver.cit.gvsig.ExportTo export = new com.iver.cit.gvsig.ExportTo();
		IWindow window = PluginServices.getMDIManager().getActiveWindow();
		if(! (window instanceof IView))
			return;
		IView view = (View) window;
		MapControl mapControl = view.getMapControl();
		MapContext context = mapControl.getMapContext();
//		try {
			if (format.equalsIgnoreCase("SHP")) {
				export.saveToShp(context, routeLayer);
			} else if (format.equalsIgnoreCase("DXF")) {
				export.saveToDxf(context, routeLayer);
			} else if (format.equalsIgnoreCase("POSTGIS")) {
				export.saveToPostGIS(context, routeLayer);
			}
//		} catch (BaseException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}

	}
	private void saveRoute() {
		IWindow window = PluginServices.getMDIManager().getActiveWindow();
		if(! (window instanceof IView))
			return;
		IView view = (View) window;
		MapControl mapControl = view.getMapControl();
		List routes = (List) GvSession.getInstance().get(mapControl, "Route");
		if(routes == null || routes.size() == 0){
			JOptionPane.showMessageDialog(this, PluginServices.getText(this, "Ruta_borrada_o_inexistente"),
					PluginServices.getText(this, "Ruta_no_encontrada"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		Route lastRoute = (Route) routes.get(routes.size() - 1);
		RouteMemoryDriver driver = new RouteMemoryDriver(lastRoute.getFeatureList());
		IProjection projection = AddLayerDialog.getLastProjection();
		FLyrVect routeLayer = (FLyrVect) LayerFactory.createLayer("Route",
				driver, projection);

		FormatSelectionPanel selectionPanel = new FormatSelectionPanel(PluginServices.getText(null,
		"Seleccione_un_formato_para_guardar_la_ruta"));
		PluginServices.getMDIManager().addWindow(selectionPanel);

		String format = selectionPanel.getSelectedFormat();
		com.iver.cit.gvsig.ExportTo export = new com.iver.cit.gvsig.ExportTo();
		MapContext context = mapControl.getMapContext();
			if (format.equalsIgnoreCase("SHP")) {
				export.saveToShp(context, routeLayer);
			} else if (format.equalsIgnoreCase("DXF")) {
				export.saveToDxf(context, routeLayer);
//			} else if (format.equalsIgnoreCase("GML")) {
//				export.saveToGml(context, routeLayer);
			} else if (format.equalsIgnoreCase("POSTGIS")) {
				export.saveToPostGIS(context, routeLayer);
			}
	}

	/**
	 * Converts a point layer loaded in the active view's toc in a collection of
	 * flags of the active network
	 *
	 */
	protected void loadStages() {

		IWindow window = PluginServices.getMDIManager().getActiveWindow();
		if(! (window instanceof IView))
			return;
		IView view = (IView) window;
		MapControl mapControl = view.getMapControl();
		MapContext map = mapControl.getMapContext();
		FLayers layers = map.getLayers();

		String title = PluginServices.getText(this,
				"Seleccionar_capa_con_puntos_de_parada");
		String introductoryText = PluginServices.getText(this,
				"Seleccione_una_capa_de_puntos_para_crear_paradas");

		LayerSelectionPanel selectionPanel = new LayerSelectionPanel(layers,
				title, introductoryText, FLyrVect.class, FShape.POINT);
		selectionPanel.addGeometryTypeConstraint(FShape.MULTIPOINT);
		PluginServices.getMDIManager().addWindow(selectionPanel);

		if (!selectionPanel.wasFinishPressed())
			return;
		else {
			FLayer layer = selectionPanel.getLayer();
			if (layer == null)
				return;
			if (!(layer instanceof FLyrVect))
				return;
			FLyrVect vectLyr = (FLyrVect) layer;

			try {

				// TODO
				// Hay que refinar bastante, para que el usuario elija
				// con que red y con qu? capa lineal quiere trabajar
				// tal y como est?, si hubiese varias se trabajaria
				// con la primera que devuelva el iterador

				Network net = null;
				SingleLayerIterator it = new SingleLayerIterator(layers);
				while (it.hasNext() && net == null) {
					FLayer aux = it.next();
					if (!aux.isActive())
						continue;
					net = (Network) aux.getProperty("network");
				}
				if (net == null)
					return;

				// Por si queremos a?adir paradas a otras ya existentes
//				_getFlags().clear();

				GvFlag flag;
				ReadableVectorial reader = vectLyr.getSource();
				SelectableDataSource recordset = vectLyr.getRecordset();
				int numShapes = reader.getShapeCount();

				double realTol = Double.parseDouble(getTxtTolerance().getText());
//						.toMapDistance(FlagListener.pixelTolerance);
				reader.start();
				ICoordTrans ct = vectLyr.getCoordTrans();
				DriverAttributes attr = reader.getDriverAttributes();
				boolean bMustClone = false;
				if (attr != null) {
					if (attr.isLoadedInMemory()) {
						bMustClone = attr.isLoadedInMemory();
					}
				}
				
				ArrayList errors = new ArrayList();
				for (int i = 0; i < numShapes; i++) {
					IGeometry geom = reader.getShape(i);
					if (ct != null) {
						if (bMustClone)
							geom = geom.cloneGeometry();
						geom.reProject(ct);
					}
					
					Geometry geo = geom.toJTSGeometry();
					if (!((geo instanceof Point) || (geo instanceof MultiPoint)))
						continue;



					Coordinate[] coords = geo.getCoordinates();
					if (coords.length > 1) {
						logger.warn("The record " + i + " has " + coords.length + " coordinates. Pay attention!!");
					}
					for (int j = 0; j < coords.length; j++) {

						try {
							flag = net.addFlag(coords[j].x, coords[j].y, realTol);
							if (flag == null)
							{
								// segundo intento:
								flag = net.addFlag(coords[j].x, coords[j].y, 4*realTol);
								if (flag == null)
								{
									errors.add(new Integer(i));
								}
							}
							if (flag != null)
							{
							// TODO: Create use a IFlagListener to do this
							// inside flagsChanged. The best solution is
							// to put all the code in RouteControlPanel
								NetworkUtils.addGraphicFlag(mapControl, flag);
							}
						} catch (GraphException e) {
							e.printStackTrace();
							NotificationManager.addError("No se puedo situar el registro " + i +
									"Por favor, compruebe que est? encima de la red.", e);
						}

					} // for j
				} // for i
				reader.stop();
				mapControl.drawGraphics();
				if (errors.size() > 0) {
					String msg = PluginServices.getText(null, "these_records_are_out_from_network") + "\n";
					for (int i =0; i < errors.size(); i++) {
						msg = msg + i + " ";
					}
					msg = msg + "\n" + PluginServices.getText(null, "please_check_them_or_increase_tolerance");
					JOptionPane.showMessageDialog((Component) PluginServices.getMDIManager().getActiveWindow(), msg);
				}
				
				PluginServices.getMainFrame().enableControls();

			} catch (BaseException e1) {
				return;
			}
		}
	}

	/**
	 * Removes the selected flags in the component's table from: -the graphic
	 * layer of the active view -the active network -the own table
	 *
	 */
	private void removeStage() {
		int[] selected = tblStages.getSelectedRows();

		for (int i = selected.length - 1; i >= 0; i--) {
			try {
				// removeFlag(selected[i]);
				if (network != null) {
					final GvFlag flag = (GvFlag) _getFlags().get(selected[i]);
					NetworkUtils.clearFlagFromGraphics(mapCtrl, flag);
					network.removeFlag(flag);
					mapCtrl.repaint();
				}
			} catch (IndexOutOfBoundsException iobEx) {
			}

		}// for
		tblStages.clearSelection();
		invalidateSolution();
		mapCtrl.drawMap(false);
	}

	public GvFlag[] getFlags() {
		return (GvFlag[]) _getFlags().toArray(new GvFlag[0]);
	}

	public void flagsChanged(int reason) {
		refresh();
		mapCtrl.drawGraphics();

	}

	public boolean isTspSelected() {
		return bDoTSP;
	}

	public boolean isReturnToOriginSelected() {
		return bReturnToOrigin;
	}
} // @jve:decl-index=0:visual-constraint="17,9"

