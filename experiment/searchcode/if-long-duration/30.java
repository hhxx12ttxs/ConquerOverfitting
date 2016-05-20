package controller;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;
import javax.swing.filechooser.FileNameExtensionFilter;

import layout.CircularLayout;
import layout.ForceDirectedLayout;
import layout.GridLayout;
import layout.ILayout;
import layout.RadialLayout;
import layout.RandomLayout;
import layout.TopologicalLayout;
import layout.Router.EdgeRouter;
import model.*;
import gui.AppletGUI;

/**
 * A controller class that controls the application
 * @author Daroczi Krisztian-Zoltan
 * @version 1.0
 */

public class AppController {
	/**
	 * The gui for the application
	 */
	private AppletGUI gui;
	/**
	 * The filename used to load or save the graph
	 */
	private String currentFileName;
	/**
	 * The fileChooser component
	 */
	private JFileChooser fc;
	/**
	 * The last selected object
	 */
	private Object selected;	
	/**
	 * State to indicate whether or not to highlight the strongly connected components
	 */
	private boolean showScc;	
	/**
	 * Shortest path starting vertex
	 */
	private int spStartId;
	/**
	 * Shortest path ending vertex
	 */
	private int spEndId;	
	/**
	 * State to indicate whether or not to highlight the shortest path
	 */
	private boolean showSp;				
	/**
	 * The shape of the selected vertex
	 */
	private VertexShape vs;			
	
	/**
	 * The last shortest path that was calculated 
	 */
	private ShortestPath prevSp;
	
	private boolean scrollActive;
	private Vector2D scrollStart;
	
	//save the settings of the last added vertex/edge
	
	private Color lastClr, lastHLClr;
	private int lastWidth, lastHeight, lastRadius;
	private VertexShape lastVertexShape;
	
	private Color lastEClr, lastEHLClr;
	private boolean lastRealDistance;
	private String lastLabel = "";
	private int lastThickness, lastCustomPointCount;
	private float lastCost;
	
	private Vector2D lastVertexPos, lastPopupPos;
	
	/**
	 * Indicates if an edge is in `process of creation'
	 */
	private boolean edgeCreation;
	
	private Vector<Vector2D> intermediaryPoints;
	
	private EdgeRouter er;
	
	public static boolean debug;
	
	/**
	 * The constructor that links the action listeners of the GUI to this class
	 * @param gui an instance of the {@link AppletGUI}
	 * @since Version 1.0
	 */
	public AppController(AppletGUI gui){
		AppController.debug = false;
		this.currentFileName = "";
		this.gui = gui;
		this.showScc = false;

		this.spStartId = -1;
		this.spEndId = -1;
		this.showSp = false;
		this.prevSp = null;
		this.scrollActive = false;
		this.scrollStart = new Vector2D(-1, -1);
		this.vs = VertexShape.ELLIPSE;
		this.edgeCreation = false;
		
		fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("xml files", "xml");
		fc.addChoosableFileFilter(filter);

		gui.addDrawAreaMIAdapter(new MIA());
		gui.addNewListener(new NewGraphActionListener());					//file-&gt;New
		gui.addShowLogEntryListener(new ShowHideLogActionListener());		//options-&gt;Show Log
		gui.addLoadListener(new LoadGraphActionListener());					//file-&gt;Load
		gui.addSaveListener(new SaveActionListener());						//file-&gt;Save
		gui.addSaveAsListener(new SaveAsActionListener());					//file-&gt;Save As
		gui.addExportListener(new ExportListener());						//file-&gt;Export as image
		
		gui.addHighlightSCCListener(new HighlightSCC());					//operation-&gt;Highlight SCC
		gui.addShortestPathListener(new ShortestPathListener());			//operation-&gt;Shortest Path
		gui.addAddEdgeListener(new AddEdgeListener());						//add-&gt;Edge
		gui.addAddVertexListener(new AddVertexListener());					//add-&gt;Vertex
		gui.addAntiAliasListener(new UseAntiAlias());						//options-&gt;Use AntiAlias
		gui.addScrollToCenter(new ScrollToCenterListener());				//options-&gt;Scroll to center
		gui.addDebugListener(new DebugListener());							//options-&gt;Debug
		
		gui.addGridLayoutKeepAspectRatioListener(
				new AddGridLayoutKeepAspectRatioListener());				//Layout->Grid Layout->Keep Aspect Ratio
		gui.addGridLayoutSquareLayoutListener(
				new AddGridLayoutSquareLayoutListener());					//Layout->Grid Layout->Square Layout
		gui.addRadialLayoutListener(new AddRadialLayoutListener());			//Layout->Cyclic Layouts->Radial Layout
		gui.addCircularLayoutListener(new AddCircularLayoutListener());		//Layout->Cyclic Layouts->Circular Layout
		gui.addTreeLayoutListener(new AddTreeLayoutListener());				//Layout->Topological Layout
		gui.addRandomLayoutListener(new AddRandomLayout());					//Layout->Random Layout
		gui.addSpringLayoutListener(new AddSpringLayoutListener());			//Layout->Spring Layout
		gui.addEdgeRoutingListener(new AddEdgeRoutingListener());			//Layout->Edge Routing
		
		gui.addAddVertexPopupListener(new AddVertexPopupActionListener());	//popup menu listener for adding a vertex
		gui.addAddEdgePopupListener(new AddEdgePopupActionListener());		//popup menu listener for adding an edge
		gui.addHighlightPopupListener(new AddHighlightPopupListener());		//pupup menu listener for highlighting
		
		gui.addDrawAreaKeyListener(new DrawAreaKeyListener());				//key listener for the drawArea
		//gui.addKeyListener(new DrawAreaKeyListener());
		//gui.addDrawAreaScrollListener(new DrawAreaHorizontalScrollListener());//scroll change listener 
		gui.addDrawAreaMouseWheelListener(new DrawAreaMouseWheelListener());//mouse wheel listener for the drawArea
		
		gui.addVertexColorPickListener(new VertexColorPickListener());		//pick a vertex color
		gui.addVertexHLColorPickListener(new VertexHLColorPickListener());	//pick a vertex highlight color
		gui.addVertexShapeListener(new VertexShapeListener());				//combobox listener
		gui.addVertexOkListener(new VertexOkListener());					//OK listener
		gui.addVertexDeleteListener(new VertexDeleteListener());			//Delete listener
		gui.addVertexRemoveEdgeListener(new VertexRemoveEdgeListener());	//Remove Edge of a Vertex
		
		gui.addEdgeColorPickListener(new EdgeColorPickListener());			//pick an edge color
		gui.addEdgeHLColorPickListener(new EdgeHLColorPickListener());		//pick an edge highlight color
		gui.addEdgeRealDistanceListener(new EdgeRealDistanceListener());	//set/unset the real distance bit
		gui.addEdgeOkListener(new EdgeOkListener());						//OK listener
		gui.addEdgeDeleteListener(new EdgeDeleteListener());				//Delete listener
		gui.addEdgeStartsListener(new EdgeStartsListener());				//starting vertex selection changes
		gui.addEdgeEndsListener(new EdgeEndsListener());					//ending vertex selection changes
		
		

		

		
		this.gui.addLogEntry("Startup: width="+ this.gui.width +"; height=" + this.gui.height);

		this.gui.addLogEntry("-----------------------------------------------------------------------------------------");
		this.gui.addLogEntry("Shortcut keys :");
		this.gui.addLogEntry("V -- Add Vertex");
		this.gui.addLogEntry("E -- Add Edge");
		this.gui.addLogEntry("H -- Highlight the Stronly connected components of a Vertex");
		this.gui.addLogEntry("[SPACE] -- show/hide shortest path between a pair of vertices");
		this.gui.addLogEntry("-----------------------------------------------------------------------------------------");
		this.gui.addLogEntry("Scroll -- Grab the canvas with your middle mouse button to scroll");
		this.gui.addLogEntry("Zoom   -- Use your mousewheel or touchpad scroll to zoom in/out");
		this.gui.addLogEntry("Use the `Options->Scroll to center' if you've lost your graphs' position");
		this.gui.addLogEntry("Create a vertex on a specific location: use the popup menu->`Add Vertex");
		this.gui.addLogEntry("Dynamically create edges: use the popup menu->`Add Edge' then follow the instructions");
		this.gui.addLogEntry("-----------------------------------------------------------------------------------------");
		this.gui.addLogEntry("Check out the changelog for further details");
		this.gui.addLogEntry("-----------------------------------------------------------------------------------------");
		
		//new graph
		gui.jmiNew.doClick();
		
		gui.jcmiDebug.setSelected(debug);
/*		
		///temp

		
		File file = new File("/home/kriszty/springTest.xml");
		XMLIO loader = new XMLIO(file.getAbsolutePath());
		gui.addLogEntry("Loading graph from " + file.getAbsolutePath());
		
		try {
			if (gui.drawArea.getGraph() == null)
				gui.jmiNew.doClick();
			MultiDirectedGraph dg = loader.load();
			gui.drawArea.setGraph(dg);
			//er = new EdgeRouter(gui.drawArea.getGraph(), new Vector2D(gui.drawArea.getW(), gui.drawArea.getH()));
			gui.addLogEntry("Graph loaded with " + dg.getVertexCount() + " vertices and " + dg.getEdgeCount() + " edges");
			currentFileName = "";
		} catch (XMLIOException ex) {
			gui.addLogEntry(ex.getMessage());
			JOptionPane.showMessageDialog(gui, ex.getMessage());
		} catch (MyException ex) {
			gui.addLogEntry(ex.getMessage());
			JOptionPane.showMessageDialog(gui, ex.getMessage());
		}

		 
		
		//</temp>
*/		
		this.gui.addLogEntry("IGE " + About.getVersion() + " (Built: " + About.getRelease() + ") started");
		
		this.repaintCanvas();
	}

	/**
	 * A method used by this controller class to repaint the canvas.
	 * The canvas it's not refreshed, if the log is open.
	 * @since Version 1.0
	 */
	public void repaintCanvas(){
		if ((selected != null) && (selected instanceof Edge)){
		//update the cost textfield of the
			Edge edge = (Edge) selected;
			if (!edge.getLabel().equals(""))
				gui.tfEdgeCost.setText(edge.getLabel());
			else
				gui.tfEdgeCost.setText(edge.getCost()+"");
		}
		if (!gui.jcmiShowLog.isSelected()){
			gui.drawArea.repaint();
			if ((er != null) && (debug)){
				er.draw(gui.drawArea.getGraphics());
			}
		}
		
		
		//<temp?>
		
		if ((showSp) && (spStartId != -1) && (spEndId != -1)){
			//now highlight the shortest path
			
			ShortestPath sp = null;
			try {
				
				//TODO remove after benchmark
				int counter = 0;
				long totalDuration = 0, min = Long.MAX_VALUE, max = Long.MIN_VALUE;
				while (counter <= 1){
					long before = System.nanoTime();
					
					sp = gui.drawArea.getGraph().getShortestPath(
							gui.drawArea.getGraph().getVertexById(spStartId),
							gui.drawArea.getGraph().getVertexById(spEndId),
							true
					);
					
					long duration = System.nanoTime() - before;
					if (duration < min)
						min = duration;
					if (duration > max)
						max = duration;
					totalDuration += duration;
					counter++;
				}
				//gui.addLogEntry("Average time is " + totalDuration / counter + " ns = " + (totalDuration / counter) / 1000000.0 + " ms; min="+min/1000000.0+" | max="+max/1000000.0);


			} catch (ShortestPathException e1) {
				showSp = false;
				spStartId = -1;
				spEndId = -1;
				gui.addLogEntry(e1.getMessage());
				gui.drawArea.getGraph().deHighlightEdges();
				gui.drawArea.getGraph().deHighlightVertices();
				repaintCanvas();
				return;
			}

			if ((prevSp == null) || (sp.getResultCost() != prevSp.getResultCost())){
				gui.addLogEntry("Shortest path changed...");
				prevSp = sp;

				gui.drawArea.getGraph().deHighlightEdges();
				gui.drawArea.getGraph().deHighlightVertices();
				
				String lg = "The lowest cost path from `" + gui.drawArea.getGraph().getVertexById(spStartId) + 
				"' to `" + gui.drawArea.getGraph().getVertexById(spEndId) + "' is: ";
				
				if (spStartId == spEndId){
					lg += "`" + gui.drawArea.getGraph().getVertexById(spEndId) + "'(0.0)  Total cost is : >>"
					+ sp.getResultCost() + "<< .";
					spStartId = -1;
					spEndId = -1;
				}else{
					Vector<Vertex> resultPath = sp.getResultPath();
					if ((resultPath != null) && (resultPath.size() > 0)){
						resultPath.get(0).setHighLight(true);
						resultPath.get(0).setPathStarter(true);
						lg += "`" + resultPath.get(0) + "' -->; ";
	
						for (int i = 1; i < resultPath.size(); i++){
							Vertex prev = resultPath.get(i - 1);
							Vertex v = resultPath.get(i);
							v.setHighLight(true);
	
							Edge edge = gui.drawArea.getGraph().getMinCostPath(prev, v);
	
							edge.setHighLight(true);
							if (i < resultPath.size() - 1)
								lg += "`" + v + "'(" + edge.getCost() + ") -->; ";
							else
								lg += "`" + v + "'(" + edge.getCost() + ") Total cost is : >>" + sp.getResultCost() + "<< .";
						}
						
						resultPath.get(resultPath.size() - 1).setPathEnder(true);
					}else{
						lg += "no path exists...";
						spStartId = -1;
						spEndId = -1;
					}
				}
				//showSp = false;
				gui.addLogEntry(lg);
			}
			//repaintCanvas();

		}
		//</temp>

		
	}
	
	/**
	 * Opens up the selected panel, and closes the others
	 * @param panelToOpen the panel to open
	 * @param create specify here whether or not the panel will be used to create, instead of modify
	 * @since Version 1.0
	 */
	public void showPanel(JPanel panelToOpen, boolean create){
		if (panelToOpen != null){
			//open up this panel, and hide everything else (in reverse order)
			gui.edgePanel.setVisible(false);
			gui.vertexPanel.setVisible(false);
			
			panelToOpen.setVisible(true);
			
			if (gui.drawArea.getGraph() == null){
				gui.jmiNew.doClick();
				return;
			}
			
			if (panelToOpen == gui.vertexPanel){
				if (create){
					gui.lblVertexTopText.setText("Create a vertex");
					gui.btnVertexDelete.setEnabled(false);
					gui.btnVertexRemoveEdge.setEnabled(false);
					gui.cmbVertexShape.setEnabled(true);
					gui.btnVertexOk.setText("Create");
					gui.cmbVertexEdgeCount.removeAllItems();
					gui.lblVertexEdgeCount.setText("Edges (0)");
					gui.tfVertexText.setText("");

					//use the last settings
					switch (lastVertexShape){
						case ELLIPSE:
							gui.cmbVertexShape.selectWithKeyChar('E');
							break;
						case RECTANGLE:
							gui.cmbVertexShape.selectWithKeyChar('R');
							break;
						case CIRCLE:
							gui.cmbVertexShape.selectWithKeyChar('C');
							break;
						default:
							gui.cmbVertexShape.selectWithKeyChar('E');
							break;
					}
					gui.btnVertexColor.setBackground(lastClr);
					gui.btnVertexHLColor.setBackground(lastHLClr);
					gui.slVertexWidth.setValue(lastWidth);
					gui.slVertexHeight.setValue(lastHeight);
					gui.slVertexRadius.setValue(lastRadius);

				}
			}else if (panelToOpen == gui.edgePanel){
				if (create){
					gui.lblEdgeTopText.setText("Create an edge");
					gui.cmbEdgeEnds.removeAllItems();
					gui.cmbEdgeStarts.removeAllItems();
					gui.btnEdgeOk.setText("Create");
					Vector<Vertex> vertices = new Vector<Vertex>();
					for (int i = 0; i < gui.drawArea.getGraph().getVertexCount(); i++)
						vertices.add(gui.drawArea.getGraph().getVertexByIndex(i));
					Collections.sort(vertices, new VertexComparator());

					for (int i = 0; i < gui.drawArea.getGraph().getVertexCount(); i++){
						gui.cmbEdgeStarts.addItem(vertices.get(i));
						gui.cmbEdgeEnds.addItem(vertices.get(i));
					}
					
					//restore last settings
					gui.btnEdgeColor.setBackground(lastEClr);
					gui.btnEdgeHLColor.setBackground(lastEHLClr);
					gui.chbEdgeReal.setSelected(lastRealDistance);
					if (!lastLabel.equals(""))
						gui.tfEdgeCost.setText(lastLabel);
					else
						gui.tfEdgeCost.setText(lastCost+"");
					
					gui.tfEdgeCost.setEnabled(!lastRealDistance);
					gui.btnEdgeDelete.setEnabled(false);
					gui.slEdgePointCount.setEnabled(true);
					gui.slEdgePointCount.setValue(lastCustomPointCount);
					gui.slEdgeThickness.setValue(lastThickness);
					
					//if an edge was created then select the source and destination vertices
					if ((edgeCreation) && (spStartId != -1) && (spEndId != -1)){
						Vertex s = gui.drawArea.getGraph().getVertexById(spStartId);
						Vertex d = gui.drawArea.getGraph().getVertexById(spEndId);
						int startIndex = 0, endIndex = 0;
						for (int i = 0; i < vertices.size(); i++){
							if (vertices.get(i) == s){
								startIndex = i;
								break;
							}
						}
						
						for (int i = 0; i < vertices.size(); i++){
							if (vertices.get(i) == d){
								endIndex = i;
							}
						}
						
						gui.cmbEdgeStarts.setSelectedIndex(startIndex);
						gui.cmbEdgeEnds.setSelectedIndex(endIndex);
						//gui.cmbEdgeEnds.selectWithKeyChar(d.toString().charAt(0));
						gui.slEdgePointCount.setValue(intermediaryPoints.size());
						//gui.slEdgePointCount.setEnabled(false);
					}
					

				}
			}
		}else{
			//just hide every panel
			gui.edgePanel.setVisible(false);
			gui.vertexPanel.setVisible(false);
		}
		repaintCanvas();
	}

	/**
	 * Updates the GUI components according to an edge
	 * @param e the edge
	 * @since Version 1.0
	 */
	public void fillEdgeProperties(Edge e){
		gui.lblEdgeTopText.setText("Modify this edge");
		gui.btnEdgeDelete.setEnabled(true);
		gui.btnEdgeOk.setText("Modify");
		
		//first populate the comboboxes
		gui.cmbEdgeEnds.removeAllItems();
		gui.cmbEdgeStarts.removeAllItems();
		Vector<Vertex> vertices = new Vector<Vertex>();
		for (int i = 0; i < gui.drawArea.getGraph().getVertexCount(); i++)
			vertices.add(gui.drawArea.getGraph().getVertexByIndex(i));
		Collections.sort(vertices, new VertexComparator());
		
		for (int i = 0; i < gui.drawArea.getGraph().getVertexCount(); i++){
			gui.cmbEdgeStarts.addItem(vertices.get(i));
			if (e.getStart().equals(vertices.get(i)))
				gui.cmbEdgeStarts.setSelectedIndex(i);
			gui.cmbEdgeEnds.addItem(vertices.get(i));
			if (e.getEnd().equals(vertices.get(i)))
				gui.cmbEdgeEnds.setSelectedIndex(i);
		}

		//load the colors
		gui.btnEdgeColor.setBackground(e.getColor());
		gui.btnEdgeHLColor.setBackground(e.getHighlightColor());

		//load the cost
		
		if (e.getLabel().equals(""))
			gui.tfEdgeCost.setText(e.getLabel());
		else
			gui.tfEdgeCost.setText(e.getCost()+"");
		
		gui.tfEdgeCost.setText(e.getCost()+"");

		//load the thickness
		gui.slEdgeThickness.setValue(e.getThickness());

		//load the state of the realDistance
		gui.tfEdgeCost.setEnabled(!e.getRealDistances());
		
		gui.chbEdgeReal.setSelected(e.getRealDistances());
		
		//load the number of intermediary drawing points
		gui.slEdgePointCount.setValue(e.getPointsSize());
	}

	/**
	 * Updates the GUI components according to a vertex
	 * @param v the vertex
	 * @since Version 1.0
	 */
	public void fillVertexProperties(Vertex v){
		//update panel controls with the common properties of every vertex
		gui.lblVertexTopText.setText("Modify this vertex");
		gui.btnVertexDelete.setEnabled(true);
		gui.btnVertexRemoveEdge.setEnabled(true);
		gui.cmbVertexShape.setEnabled(false);
		gui.btnVertexOk.setText("Modify");
		gui.cmbVertexEdgeCount.removeAllItems();

		gui.tfVertexText.setText(v.getText());
		gui.btnVertexColor.setBackground(v.getColor());
		gui.btnVertexHLColor.setBackground(v.getHighlightColor());

		gui.slVertexHeight.setValue(0);
		gui.slVertexWidth.setValue(0);
		gui.slVertexRadius.setValue(0);


		//now update the controls for the individual properties of the vertices
		if (selected instanceof Ellipse){
			Ellipse e = (Ellipse) v;
			gui.cmbVertexShape.setSelectedItem(VertexShape.ELLIPSE);
			gui.slVertexWidth.setValue(e.getWidth());
			gui.slVertexHeight.setValue(e.getHeight());
		}else if (selected instanceof Circle){
			Circle c = (Circle) v;
			gui.cmbVertexShape.setSelectedItem(VertexShape.CIRCLE);
			gui.slVertexRadius.setValue((int) c.getRadius());
		}else if (selected instanceof Rectangle){
			Rectangle r = (Rectangle) v;
			gui.cmbVertexShape.setSelectedItem(VertexShape.RECTANGLE);
			gui.slVertexWidth.setValue(r.getWidth());
			gui.slVertexHeight.setValue(r.getHeight());			
		}

		//now the edges
		Vector<Edge> out = v.out;

		gui.lblVertexEdgeCount.setText("Edges (" + out.size() +"):");
		
		Collections.sort(out, new EdgeComparator());
		
		for (int i = 0; i < out.size(); i++)
			gui.cmbVertexEdgeCount.addItem(out.get(i));

	}

	
	/**
	 * Method used to decide which panel to open and close
	 * @since Version 1.0
	 */
	public void updateOptionsPanelState(){
		if ((selected == null) || (gui.drawArea.getGraph() == null) || (gui.drawArea.getGraph().getVertexCount() == 0)){
			showPanel(gui.vertexPanel, true);
			return;
		}

		//if an edge was selected then open it to modify
		if (selected instanceof Edge){
			showPanel(gui.edgePanel, false);

			//now update the panel controls
			fillEdgeProperties((Edge) selected);
			return;
		}

		//now a vertex is selected => show the vertex panel
		showPanel(gui.vertexPanel, false);
		fillVertexProperties((Vertex) selected);
		repaintCanvas();
	}

	/**
	 * A class that compares to edges in string form
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	class EdgeComparator implements Comparator<Edge> {
		/**
		 * This method will be called, when a list of edges has to be sorted
		 * @since Version 1.0
		 * @param o1 the first edge
		 * @param o2 the second edge
		 * @return 0 if they are equal, negative if the first is less than the second, positive otherwise
		 */
		@Override
		public int compare(Edge o1, Edge o2) {
			return (o1.toString().compareTo(o2.toString()));
		}
		
	}
	
	/**
	 * A class that compares to vertices in string form
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	class VertexComparator implements Comparator<Vertex> {
		/**
		 * This method will be called, when a list of vertices has to be sorted
		 * @since Version 1.0
		 * @param o1 the first vertex
		 * @param o2 the second vertex
		 * @return 0 if they are equal, negative if the first is less than the second, positive otherwise
		 */
		@Override
		public int compare(Vertex o1, Vertex o2) {
			return (o1.toString().compareTo(o2.toString()));
		}
		
	}
	
	/**
	 * An inner class of the Controller to track the mouse motion of the <i>drawArea</i>
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class MIA extends MouseInputAdapter {
		private Vertex selectedShape;
		private Vector2D selectedPoint;
		private Edge selectedEdge;
		private int diffX;
		private int diffY;
		private int pDiffX;
		private int pDiffY;

		/**
		 * Sets up the listener
		 * @since Version 1.0
		 */
		public MIA(){
			selectedShape = null;
			selectedPoint = null;
			selectedEdge = null;
			diffX = 0;
			diffY = 0;
			pDiffX = 0;
			pDiffY = 0;
			selected = null;
		}
		
		
		
		@Override
		public void mousePressed(MouseEvent e){
			
			
			//activate scroll with the middle mouse button
			if (e.getButton() == 2){
				scrollActive = true;
				scrollStart.setX(e.getX());
				scrollStart.setY(e.getY());
				return;
			}
			
			if (e.getButton() == MouseEvent.BUTTON3){
	            gui.jpmPopup.show(e.getComponent(),
	                       e.getX(), e.getY());

				if (selected instanceof Edge){
					Edge edge = (Edge) selected;
					
					gui.jcmiHighLight.setSelected(edge.isHighLight());
				} else if ((selected instanceof Circle) || (selected instanceof Rectangle) || (selected instanceof Ellipse)){
					Vertex v = (Vertex) selected;
					gui.jcmiHighLight.setSelected(v.getHighLight());
				}
				repaintCanvas();
		        lastPopupPos.setX(e.getX());
		        lastPopupPos.setY(e.getY());
		        repaintCanvas();
				return;
			}
			
			//selection only with the left mouse button
			//if (e.getButton() != MouseEvent.BUTTON1)
			//	return;
			
			//try to select a vertex
			Stack <Vertex> overlappingShapes = gui.drawArea.getOverlappingVertices(new Vector2D(e.getX(), e.getY()));
			Map <Vector2D, Edge> overlappingPoints = gui.drawArea.getOverlappingPoints(new Vector2D(e.getX(), e.getY()));
			Vertex maxPriority = null, poppedShape = null;
			Vector2D pMaxPriority = null;

			if ((overlappingShapes != null) && (overlappingShapes.size() > 0)){
				maxPriority = overlappingShapes.pop();
				while(overlappingShapes.isEmpty() == false){
					poppedShape = overlappingShapes.pop();
					if (poppedShape.getPriority() > maxPriority.getPriority())
						maxPriority = poppedShape;
				}
			}

			selectedShape = maxPriority;

			if ((overlappingPoints != null) && (overlappingPoints.size() > 0)){
				for (Object obj : overlappingPoints.keySet()){
					pMaxPriority = (Vector2D) obj;
					break;
				}
				selectedEdge = overlappingPoints.get(pMaxPriority);
			}

			selectedPoint = pMaxPriority;


			if (selectedShape != null){

				for (int i = 0; i < gui.drawArea.getGraph().getVertexCount(); i++){
					if (gui.drawArea.getGraph().getVertexByIndex(i).getPriority() > selectedShape.getPriority()){
						Vertex v = gui.drawArea.getGraph().getVertexByIndex(i);
						v.setPriority(v.getPriority() - 1);
					}
				}
				//the priorities vary between 0 and rectCount-1
				selectedShape.setPriority(gui.drawArea.getGraph().getVertexCount() - 1);
				gui.addLogEntry("Vertex: `" + selectedShape + "' [inDeg=" 
						+ selectedShape.getInDegree() + " | outDeg=" + selectedShape.getOutDegree() + " ]");
				diffX = e.getX() - selectedShape.getLeft();
				diffY = e.getY() - selectedShape.getTop();
				selected = maxPriority;
				//if got to show the shortest path and the 1st vert. is not selected
				if ((showSp) && (spStartId == -1)){
					gui.addLogEntry("Calculating Shortest Path: starting vertex is `" + maxPriority + "'; select the destination vertex.");
					spStartId = maxPriority.getId();
				}else if ((showSp) && (spStartId != -1) && (spEndId == -1)){
					//if got to show the shortest path and the 1st vertex was already selected, but the 2nd wasn't
					spEndId = maxPriority.getId();
					gui.addLogEntry("Destination vertex is `" + maxPriority + "'.");
				}else if ((edgeCreation) && (spStartId == -1)){
					intermediaryPoints = new Vector<Vector2D>();
					gui.addLogEntry("Keep clicking to add intermediary points, finally select the destination vertex");
					spStartId = maxPriority.getId();
					repaintCanvas();
					return;	//don't add as a point the first clicks' position
				}else if ((edgeCreation) && (spStartId != -1) && (spEndId == -1)){
					spEndId = maxPriority.getId();
					gui.addLogEntry("Destination vertex is `" + maxPriority + "'.");
					gui.jmiAddEdge.doClick();
					gui.btnEdgeOk.doClick(); 
					return;	//don't close the panel
				}
			}
			if (selectedPoint != null){				
				gui.addLogEntry("Edge from `" + selectedEdge.getStart() +"' to `" + selectedEdge.getEnd() + "'");
				selected = overlappingPoints.get(pMaxPriority);
				pDiffX = e.getX() - selectedPoint.getX();
				pDiffY = e.getY() - selectedPoint.getY();
			}
			
			//selecting custom drawing points
			if ((edgeCreation) && (spStartId != -1) && (spEndId == -1)){
				intermediaryPoints.add(new Vector2D(e.getX(), e.getY()));
			}
			updateOptionsPanelState();
			repaintCanvas();
		}
		
		
		
		@Override
		public void mouseReleased(MouseEvent e){
			selectedPoint = null;
			
			if (scrollActive){
				
				gui.drawArea.setX(-(scrollStart.getX() - e.getX()));
				gui.drawArea.setY(-(scrollStart.getY() - e.getY()));

				scrollActive = false;
				scrollStart.setX(-1);
				scrollStart.setY(-1);
				Frame browserFrame;
				Component parentComponent;
				parentComponent = gui.getParent();
				while ( parentComponent != null && 
				         !(parentComponent instanceof Frame)) {      
				  parentComponent = parentComponent.getParent();
				}

				browserFrame = (Frame) parentComponent;      
				browserFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				repaintCanvas();
			}
			if (selectedShape != null){
				if (showScc){
					//TODO remove after benchmark
					Vector<Vertex> scc = null;
					int counter = 0;
					long totalDuration = 0, min = Long.MAX_VALUE, max = Long.MIN_VALUE;
					while (counter <= 1){
						long before = System.nanoTime();
						
						scc = gui.drawArea.getGraph().getSCC(selectedShape);
						
						long duration = System.nanoTime() - before;
						if (duration < min)
							min = duration;
						if (duration > max)
							max = duration;
						totalDuration += duration;
						counter++;
					}
					//gui.addLogEntry("Average time is " + totalDuration / counter + " ns = " + (totalDuration / counter) / 1000000.0 + " ms; min="+min/1000000.0+" | max="+max/1000000.0);

					for (int i = 0; i < scc.size(); i++){
						Vertex v = scc.get(i);
						v.setHighLight(false);
					}
				}

				selectedShape.setMoving(false);
				selectedShape.draw(gui.drawArea.getGraphics());
				selectedShape = null;
/*				if ((showSp) && (prevSp != null)){
					Vector<Vertex> resultPath = prevSp.getResultPath();
					if ((resultPath != null) && (resultPath.size() > 0))
						for (int i = 0; i < resultPath.size(); i++)
							resultPath.get(i).setHighLight(true);
				}*/
				repaintCanvas();
			} else if (selectedEdge != null){

				selectedEdge.setMoving(false);
				selectedEdge = null;
/*				if ((showSp) && (prevSp != null)){
					Vector<Vertex> resultPath = prevSp.getResultPath();
					if ((resultPath != null) && (resultPath.size() > 0)){
						resultPath.get(0).setHighLight(true);
	
						for (int i = 1; i < resultPath.size(); i++CROSSHAIR_CURSOR){
							Vertex prev = resultPath.get(i - 1);
							Vertex v = resultPath.get(i);
							v.setHighLight(true);
	
							Edge edge = gui.drawArea.getGraph().getMinCostPath(prev, v);
	
							edge.setHighLight(true);
						}
					}
				}*/
				repaintCanvas();
			}
		}
		
		/**
		 * Method fired when the mouse button is held down and the mouse is moving.
		 * This method is used to move vertices and edges around.
                 * @param e MouseEvent
		 * @since Version 1.0
		 */
		@Override
		public void mouseDragged(MouseEvent e) {
			if ((gui.drawArea.getGraph() == null) || (gui.drawArea.getGraph().getVertexCount() == 0)){
				updateOptionsPanelState();
				return;
			}

			if (scrollActive){
				Frame browserFrame;
				Component parentComponent;
				parentComponent = gui.getParent();
				while ( parentComponent != null && 
				         !(parentComponent instanceof Frame)) {      
				  parentComponent = parentComponent.getParent();
				}

				browserFrame = (Frame) parentComponent;      
				browserFrame.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
				return;
			}
			
			if ((selectedShape != null) || (selectedPoint != null)){
				
				if (selectedShape != null){
					selectedShape.setLeft(e.getX() - diffX);
					selectedShape.setTop(e.getY() - diffY);

					if (showScc == false)
						selectedShape.setMoving(true);
					else{
						gui.drawArea.getGraph().deHighlightEdges();
						gui.drawArea.getGraph().deHighlightVertices();
						Vector<Vertex> scc = gui.drawArea.getGraph().getSCC(selectedShape);
						String lg = "The strongly connected components of " + selectedShape + " are : `";
						for (int i = 0; i < scc.size(); i++){
							Vertex v = scc.get(i);
							v.setHighLight(true);
							if (i < scc.size() - 1)
								lg += v + "', `";
							else
								lg += v + "'";
						}
						gui.addLogEntry(lg);
					}

					selectedShape.draw(gui.drawArea.getGraphics());

					selectedPoint = null;
					selectedEdge = null;
				} else if (selectedPoint != null){
					int selectedPointIndex = selectedEdge.getPointIndex(selectedPoint);
					
					if (selectedPointIndex != -1){
						Vector2D newPoint = new Vector2D(e.getX() - pDiffX, e.getY() - pDiffY);
						selectedEdge.setPoint(selectedPointIndex, newPoint);
						selectedPoint = newPoint;
					}
					selectedShape = null;
					
				}
				if (selectedEdge != null){
					selectedEdge.setMoving(true);
					
					
					selectedEdge.draw(gui.drawArea.getGraphics());
					selectedShape = null;
				}
				repaintCanvas();
			}
		}

		/**
		 * Method fired when the mouse is moved, but no buttons are held down.
		 * @since Version 1.0 
		 */
		@Override
		public void mouseMoved(MouseEvent e) {

/*			selectedPoint = null;	

			if (selectedShape != null){
				if (showScc){
					Vector<Vertex> scc = gui.drawArea.getGraph().getSCC(selectedShape);
					for (int i = 0; i < scc.size(); i++){
						Vertex v = scc.get(i);
						v.setHighLight(false);
					}
				}
				selectedShape.setHighLight(false);
				selectedShape.draw(gui.drawArea.getGraphics());
				selectedShape = null;
				repaintCanvas();
			} else if (selectedEdge != null){
				selectedEdge.setHighLight(false);
				selectedEdge = null;
				repaintCanvas();
			}*/

			if (!debug)
				repaintCanvas();
		}

	}

	/**
	 * Inner class to handle the <i>File-&gt;New</i> event.
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class NewGraphActionListener implements ActionListener {

		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (showSp)
				gui.jmiShortestPath.doClick();
			if (showScc)
				gui.jcmiHighlightSCC.doClick();
			if (edgeCreation)
				gui.jcmiPopupAddEdge.doClick();
			
			showSp = false;
			spStartId = -1;
			spEndId = -1;
			prevSp = null;
			showScc = false;
			
			lastWidth = 100;
			lastHeight = 50;
			lastRadius = 30;
			lastClr = Color.WHITE;
			lastHLClr = Color.YELLOW;
			lastVertexShape = VertexShape.ELLIPSE;
			
			lastEClr = Color.decode("#ABCDEF");
			lastEHLClr = Color.RED;
			lastCost = 10.0f;
			lastRealDistance = true;
			lastCustomPointCount = 1;
			lastThickness = 1;
			
			lastVertexPos = new Vector2D(0, 0);
			lastPopupPos = new Vector2D(0, 0);
			edgeCreation = false;
			
			if (gui.drawArea.getGraph() != null)
				gui.addLogEntry("Destroying graph...");
			gui.drawArea.destroyGraph();
			repaintCanvas();
			gui.addLogEntry("New graph created");
			
			gui.drawArea.setGraph(new MultiDirectedGraph());
			currentFileName = "";
			selected = null;
			gui.drawArea.setZoomPercent(-1);
			updateOptionsPanelState();
		}

	}

	/**
	 * Inner class to handle the <i>File-&gt;Load</i> event.
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class LoadGraphActionListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			int ret = fc.showOpenDialog(gui);
			if (ret == JFileChooser.APPROVE_OPTION ){
				File file = fc.getSelectedFile();
				XMLIO loader = new XMLIO(file.getAbsolutePath());
				gui.addLogEntry("Loading graph from " + file.getAbsolutePath());

				try {
					if (gui.drawArea.getGraph() != null)
						gui.jmiNew.doClick();
					
					//TODO remove after benchmark
					int counter = 0;
					long totalDuration = 0, min = Long.MAX_VALUE, max = Long.MIN_VALUE;
					MultiDirectedGraph dg = null;
					while (counter <= 1){
						long before = System.nanoTime();
						
						dg = loader.load();
						
						long duration = System.nanoTime() - before;
						if (duration < min)
							min = duration;
						if (duration > max)
							max = duration;
						totalDuration += duration;
						counter++;
					}
					//gui.addLogEntry("Average time is " + totalDuration / counter + " ns = " + (totalDuration / counter) / 1000000.0 + " ms; min="+min/1000000.0+" | max="+max/1000000.0);

					
					
					gui.addLogEntry("Graph loaded with " + dg.getVertexCount() + " vertices and " + dg.getEdgeCount() + " edges");
					gui.drawArea.setGraph(dg);
					currentFileName = file.getAbsolutePath();
				} catch (XMLIOException ex) {
					gui.addLogEntry(ex.getMessage());
					JOptionPane.showMessageDialog(gui, ex.getMessage());
				} catch (MyException ex) {
					gui.addLogEntry(ex.getMessage());
					JOptionPane.showMessageDialog(gui, ex.getMessage());
				}
				selected = null;
				updateOptionsPanelState();
				repaintCanvas();
				er = null;
			} else
				gui.addLogEntry("Loading cancelled");
		}

	}

	/**
	 * Inner class to handle the <i>Options-&gt;Show Log</i> event.
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class ShowHideLogActionListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			/*
			if (gui.jcmiShowLog.isSelected()){
				gui.getContentPane().remove(gui.drawArea);
				gui.getContentPane().add(gui.logPanel, BorderLayout.CENTER);
			}else{
				gui.getContentPane().remove(gui.logPanel);
				gui.getContentPane().add(gui.drawArea, BorderLayout.CENTER);
			}
			gui.logPanel.setVisible(gui.jcmiShowLog.isSelected());
			//re-add the menubar to correct the z-index
			gui.setJMenuBar(null);
			gui.setJMenuBar(gui.menubar);
			repaintCanvas();
			*/
			
			if (gui.jcmiShowLog.isSelected()){
				gui.getContentPane().remove(gui.canvasScroll);
				gui.getContentPane().add(gui.logPanel, BorderLayout.CENTER);
			}else{
				gui.getContentPane().remove(gui.logPanel);
				gui.getContentPane().add(gui.canvasScroll, BorderLayout.CENTER);
			}
			gui.logPanel.setVisible(gui.jcmiShowLog.isSelected());
			//re-add the menubar to correct the z-index
			gui.setJMenuBar(null);
			gui.setJMenuBar(gui.menubar);
			repaintCanvas();
			
		}
	}
	
	/**
	 * Inner class to handle the <i>Options-&gt;Use AntiAlias</i> event.
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */	
	public class UseAntiAlias implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			gui.drawArea.setAntiAlias(gui.jcmiAntiAlias.isSelected());
			if (gui.jcmiAntiAlias.isSelected())
				gui.addLogEntry("Antialiased painting mode selected");
			else
				gui.addLogEntry("Antialiased painting mode deselected");
			repaintCanvas();
		}
		
	}
	
	/**
	 * Inner class to handle the <i>Options-&gt;Scroll to center</i> event.
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */	
	public class ScrollToCenterListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			//gui.drawArea.setX(-gui.drawArea.getX());
			//gui.drawArea.setY(-gui.drawArea.getY());
			gui.drawArea.scrollToCenter();
			repaintCanvas();
		}
		
	}
	
	/**
	 * Inner class to handle the <i>Options-&gt;Debug</i> event.
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	
	public class DebugListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			debug = !debug;
			gui.jcmiDebug.setSelected(debug);
			repaintCanvas();
		}
		
	}
	
	/**
	 * Inner class to handle the <i>File-&gt;Save As</i> event.
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class SaveAsActionListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (gui.drawArea.getGraph() == null){
				gui.addLogEntry("Nothing to save...");
				return;
			}
			int ret = fc.showSaveDialog(gui);
			if (ret == JFileChooser.APPROVE_OPTION ){
				File file = fc.getSelectedFile();
				XMLIO writer = new XMLIO(file.getAbsolutePath());
				currentFileName = file.getAbsolutePath();
				gui.addLogEntry("Saving graph to " + file.getAbsolutePath());
				try {
					writer.save(false, gui.drawArea.getGraph(), file.getAbsolutePath());
					gui.addLogEntry("Graph saved");
				} catch (XMLIOException ex) {
					gui.addLogEntry(ex.getMessage());
					JOptionPane.showMessageDialog(gui, ex.getMessage());
				}
			} else
				gui.addLogEntry("Saving cancelled");
		}
	}
	/**
	 * Inner class to handle the <i>File-&gt;Export as image</i> event.
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class ExportListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			
			BufferedImage bi = new BufferedImage(gui.drawArea.getW(), gui.drawArea.getH(), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = bi.createGraphics();
	        g.drawImage(gui.drawArea.dBuff, 0, 0, null);

			FileNameExtensionFilter oldFilter = new FileNameExtensionFilter("xml files", "xml");
			fc.removeChoosableFileFilter(oldFilter);
			FileNameExtensionFilter newFilter = new FileNameExtensionFilter("image files", "png", "jpg", "bmp");
			fc.addChoosableFileFilter(newFilter);
			fc.showSaveDialog(gui);
			File img = fc.getSelectedFile();
			try {
				ImageIO.write(bi, img.getName().substring(img.getName().lastIndexOf(".") + 1, img.getName().length()), img);
			} catch (IOException ex) {
				gui.addLogEntry(ex.getMessage());
				JOptionPane.showMessageDialog(gui, ex.getMessage());
			} finally{
				fc.removeChoosableFileFilter(newFilter);
				fc.addChoosableFileFilter(oldFilter);
			}
		}
		
	}
	
	/**
	 * Inner class to handle the <i>File-&gt;Save</i> event.
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class SaveActionListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if ((gui.drawArea.getGraph() == null) || (gui.drawArea.getGraph().getVertexCount() == 0)){
				gui.addLogEntry("Nothing to save...");
				return;
			}
			if (currentFileName.equals("")){
				//save as
				gui.jmiSaveAs.doClick();
			} else {
				
				XMLIO writer = new XMLIO(currentFileName);
				gui.addLogEntry("Saving graph to " + currentFileName);
				try {
					
					//TODO remove after benchmark
					int counter = 0;
					long totalDuration = 0, min = Long.MAX_VALUE, max = Long.MIN_VALUE;
					while (counter <= 1){
						long before = System.nanoTime();

						writer.save(false, gui.drawArea.getGraph(), currentFileName);					
						
						long duration = System.nanoTime() - before;
						if (duration < min)
							min = duration;
						if (duration > max)
							max = duration;
						totalDuration += duration;
						counter++;
					}
					//gui.addLogEntry("Average time is " + totalDuration / counter + " ns = " + (totalDuration / counter) / 1000000.0 + " ms; min="+min/1000000.0+" | max="+max/1000000.0);

					
					gui.addLogEntry("Graph saved");
				} catch (XMLIOException ex) {
					gui.addLogEntry(ex.getMessage());
					JOptionPane.showMessageDialog(gui, ex.getMessage());
				}
			}

		}

	}

	/**
	 * Inner class to handle the <i>Operation-&gt;HighlightSCC</i> event.
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */	
	public class HighlightSCC implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (gui.drawArea.getGraph() == null){
				gui.addLogEntry("Can't highlight the Strongly Connected Components: no graph");
				return;
			}
			if (showSp)
				gui.jmiShortestPath.doClick();
			
			showScc = gui.jcmiHighlightSCC.isSelected();
			if (showScc){
				
				if (gui.drawArea.getGraph().getVertexCount() == 0){
					gui.addLogEntry("Can't highlight the Strongly Connected Components: the graph is empty");
					gui.jcmiHighlightSCC.setSelected(false);
					return;
				}

				gui.addLogEntry("Highlighting the Strongly Connected Components");
			}
			else{
				gui.addLogEntry("Strongly Connected Components are not highlighted anymore");
			}
		}

	}

	/**
	 * Inner class to handle the <i>Add</i>Add Edge</i> event
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class AddEdgeListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			showPanel(gui.edgePanel, true);
		}

	}
	
	/**
	 * Inner class to handle the <i>Add</i>Add Vertex</i> event
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class AddVertexListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			showPanel(gui.vertexPanel, true);
		}

	}

	/**
	 * Inner class to handle the key press events.
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */	
	public class DrawAreaKeyListener implements KeyListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void keyTyped(KeyEvent e) {
			System.out.println();
			char key = Character.toUpperCase(e.getKeyChar());
			switch (key){
			case 'H':
				gui.jcmiHighlightSCC.doClick();
				break;
			case ' ':
				gui.jmiShortestPath.doClick();
				break;
			case 'E':
				gui.jmiAddEdge.doClick();
				break;
			case 'V':
				gui.jmiAddVertex.doClick();
				break;
			default:
				gui.addLogEntry("No mapping is assigned for key `" + key + "'");
				break;
			}
		}

		@Override
		public void keyPressed(KeyEvent e) {
			// nothing to  do
		}

		@Override
		public void keyReleased(KeyEvent e) {
			// nothing to do
		}

	}


	/**
	 * Inner class to handle the <i>Operation-&gt;Shortest Path</i> event.
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */	
	public class ShortestPathListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			showSp = (!(showSp));
			if (showSp){
				if (showScc)
					gui.jcmiHighlightSCC.doClick();
				spStartId = -1;
				spEndId = -1;
				if ((gui.drawArea.getGraph() == null) || (gui.drawArea.getGraph().getVertexCount() == 0)){
					gui.addLogEntry("No shortest path calculation: the graph is empty");
					showSp = false;
				}else
					gui.addLogEntry("Calculating Shortest Path: select the starting vertex");
				
			}else if ((showSp) && (spStartId != -1) && (spEndId != -1)){
				showSp = false;
			}else if ((!showSp) && (spStartId != -1) && (spEndId != -1)){
				spStartId = -1;
				spEndId = -1;
				gui.drawArea.getGraph().deHighlightEdges();
				gui.drawArea.getGraph().deHighlightVertices();
				prevSp = null;
				repaintCanvas();
				gui.addLogEntry("Path is deselected");
			}else{
			//	gui.addLogEntry("To display(or hide) a shortest path select Operation->Shortest Path, then two vertices.");
			}
		}

	}

	/**
	 * Inner class to handle the color picking event on the vertex panel
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class VertexColorPickListener implements ActionListener{
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			Color color = JColorChooser.showDialog(gui, "Choose the color of the Vertex", Color.WHITE);
			if (color != null)
				gui.btnVertexColor.setBackground(color);
		}

	}
	
	/**
	 * Inner class to handle the highlight color picking event on the vertex panel
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class VertexHLColorPickListener implements ActionListener{
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			Color color = JColorChooser.showDialog(gui, "Choose the highlight color of the Vertex", Color.YELLOW);
			if (color != null)
				gui.btnVertexHLColor.setBackground(color);
		}

	}
	/**
	 * Inner class to handle the combobox changes on the vertex panel
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class VertexShapeListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			vs = (VertexShape) gui.cmbVertexShape.getSelectedItem();
			switch (vs){
			case CIRCLE:
				setERCEnabled(false);
				break;
			case ELLIPSE:
				setERCEnabled(true);
				break;
			case RECTANGLE:
				setERCEnabled(true);
				break;
			default:
				gui.addLogEntry("You're not supposed to do that :)");
				break;
			}
		}

		public void setERCEnabled(boolean state){
			gui.slVertexWidth.setEnabled(state);
			gui.slVertexHeight.setEnabled(state);
			gui.slVertexRadius.setEnabled(!state);
		}

	}
	
	/**
	 * Inner class to handle the "OK" button events on the vertex panel(add/modify)
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class VertexOkListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if ((gui.tfVertexText.getText().equals(null)) || (gui.tfVertexText.getText().equals(""))){
				gui.addLogEntry("Warning: vertex has no text!");
			}

			//assign id
			int id = -1;
			if (gui.drawArea.getGraph() == null)
				id = 0;
			else
				id = gui.drawArea.getGraph().getVertexCount();
			String text = gui.tfVertexText.getText();
			Color clr = gui.btnVertexColor.getBackground();
			Color hlClr = gui.btnVertexHLColor.getBackground();
			int width = gui.slVertexWidth.getValue();
			int height = gui.slVertexHeight.getValue();
			float radius = gui.slVertexRadius.getValue();
			Vertex v = null;
			
			//check if has to be modified or created
			
			if (gui.btnVertexDelete.isEnabled()){
				//modify
				v = (Vertex) selected;
				v.setText(text);
				v.setColor(clr);
				v.setHighlightColor(hlClr);
				switch(vs){
					case ELLIPSE:
						Ellipse el = null;
						el = (Ellipse) v;
						el.setWidth(width);
						el.setHeight(height);
						break;
					case RECTANGLE:
						Rectangle r = (Rectangle) v;
						r.setWidth(width);
						r.setHeight(height);
						break;
					case CIRCLE:
						Circle c = (Circle) v;
						c.setRadius(radius);
						break;
					default:
						gui.addLogEntry("Can't modify vertex to unkown type!");
						return;
				}
				gui.addLogEntry("Vertex updated");

			}else{
				//create
				switch (vs){
					case ELLIPSE:
						v = new Ellipse(id, text, clr, hlClr, gui.drawArea.getX() + lastVertexPos.getX(), gui.drawArea.getY() + lastVertexPos.getY(), id, width, height);
						break;
					case CIRCLE:
						v = new Circle(id, text, clr, hlClr, gui.drawArea.getX() + lastVertexPos.getX(), gui.drawArea.getY() + lastVertexPos.getY(), id, radius);
						break;
					case RECTANGLE:
						v = new Rectangle(id, text, clr, hlClr, gui.drawArea.getX() + lastVertexPos.getX(), gui.drawArea.getY() + lastVertexPos.getY(), id, width, height);
						break;
					default:
						gui.addLogEntry("Can't create vertex of unkown type!");
						return;
				}
				lastClr = clr;
				lastHLClr = hlClr;
				lastWidth = width;
				lastHeight = height;
				lastRadius = (int) radius;
				lastVertexShape = vs;
				lastVertexPos.setX(0);
				lastVertexPos.setY(0);
				try {
					if (gui.drawArea.getGraph() == null)
						gui.jmiNew.doClick();
	
					int z = zoomFixPrepare();
					gui.drawArea.getGraph().addVertex(v);
					zoomFixFinish(z);
				} catch (MyException e1) {
					gui.addLogEntry(e1.getMessage());
					return;
				}
				gui.addLogEntry("Vertex added");
			}
			repaintCanvas();
			updateOptionsPanelState();
		}

	}

	/**
	 * Inner class to handle the "Delete" button event on the vertex panel
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */	
	public class VertexDeleteListener implements ActionListener{
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (selected == null){
				gui.addLogEntry("Can't delete vertex because it wasn't selected");
				return;
			}
			if (gui.drawArea.getGraph() == null){
				gui.jmiNew.doClick();
			}
			if (gui.drawArea.getGraph().getVertexCount() == 0){
				gui.addLogEntry("Can't delete vertex! No vertices in graph");
				return;
			}
			String vertexAsStr = "";
			if (selected instanceof Vertex){
				Vertex v = (Vertex) selected;
				vertexAsStr = v.toString();
				
				try {
					gui.drawArea.getGraph().removeVertex(v);
				} catch (MyException e1) {
					gui.addLogEntry(e1.getMessage());
					selected = null;
					return;
				}

			}else{
				gui.addLogEntry("Can't delete vertex because it wasn't selected");
				return;
			}
			gui.addLogEntry("Vertex `" + vertexAsStr + "' deleted. Graph has " + gui.drawArea.getGraph().getVertexCount() + " vertex(ices) and " + 
					gui.drawArea.getGraph().getEdgeCount() + " edge(s)");
			selected = null;
			updateOptionsPanelState();
			repaintCanvas();
		}
		
	}
	
	/**
	 * Inner class to handle the "Remove edge from vertex" button event on the vertex panel
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */	
	public class VertexRemoveEdgeListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (selected instanceof Vertex){
				Edge edge = (Edge) gui.cmbVertexEdgeCount.getSelectedItem();
				try {
					gui.drawArea.getGraph().removeEdge(edge);
				} catch (MyException e1) {
					gui.addLogEntry(e1.getMessage());
					return;
				}
				
				gui.cmbVertexEdgeCount.removeAllItems();
				
				Vertex v = (Vertex) selected;
				Vector<Edge> out = v.out;
	
				gui.lblVertexEdgeCount.setText("Edges (" + out.size() +"):");
	
				for (int i = 0; i < out.size(); i++)
					gui.cmbVertexEdgeCount.addItem(out.get(i));
				repaintCanvas();
			}else{
				gui.addLogEntry("Can't remove the edge of an unkown vertex");
			}
		}
		
	}
	
	/**
	 * Inner class to handle the color picking event on the edge panel
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class EdgeColorPickListener implements ActionListener{
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			Color color = JColorChooser.showDialog(gui, "Choose the color of the Edge" , Color.decode("#ABCDEF"));
			if (color != null)
				gui.btnEdgeColor.setBackground(color);
		}

	}
	
	
	/**
	 * Inner class to handle the highlight color picking event on the edge panel
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class EdgeHLColorPickListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			Color color = JColorChooser.showDialog(gui, "Choose the highlight color of the Edge", Color.RED);
			if (color != null)
				gui.btnEdgeHLColor.setBackground(color);
		}
	}
	
	/**
	 * Inner class to handle the "OK" button events on the edge panel(add/modify)
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class EdgeOkListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (gui.drawArea.getGraph() == null){
				gui.jmiNew.doClick();
			}
			if (gui.drawArea.getGraph().getVertexCount() == 0){
				gui.addLogEntry("Can't add edge! No vertices in graph");
				return;
			}
			if ((gui.tfEdgeCost.getText().equals("")) && (gui.chbEdgeReal.isSelected() == false)){
				gui.addLogEntry("No cost specified!");
				return;
			}
			Vertex start = (Vertex) gui.cmbEdgeStarts.getSelectedItem();
			Vertex end = (Vertex) gui.cmbEdgeEnds.getSelectedItem();
			float cost = 0.0f;
			String label = "";
			
			try{
				cost = Float.parseFloat(gui.tfEdgeCost.getText());
			}catch (NumberFormatException nfe){
				label = gui.tfEdgeCost.getText();
			}

			Color clr = gui.btnEdgeColor.getBackground();
			Color hlClr = gui.btnEdgeHLColor.getBackground();
			int thickness = gui.slEdgeThickness.getValue();
			int pointCount = gui.slEdgePointCount.getValue();
			boolean useRealDistance = gui.chbEdgeReal.isSelected();
			
			

			//calculate the positions of the custom points
			Vector<Vector2D> points = new Vector<Vector2D>();

			Vector2D s = start.getCenter();
			Vector2D d = end.getCenter();
			Vector2D aux = null;
			
			if (s.getX() > d.getX()){
				aux = s;
				s = d;
				d = aux;
			}
			
			//decide which formula to use
			
			float step = 0.0f; 

			if (d.getX() - s.getX() == 0){
				if (d.getY() - s.getY() == 0){
					//they are on the same position => can't calculate the position of the points=>
					//=>place them in the top-left corner
					for (int i = 0; i < pointCount; i++)
						points.add(new Vector2D(i*10, 10));
				}else{
					step = (Math.abs(d.getY() - s.getY())) / (pointCount + 1);
					float prev = s.getY();
					for (int i = 1; i < pointCount; i++){
						float Y = prev + step ;
						
						float a = d.getX() - s.getX();
						float b = d.getY() - s.getY();
						float AperB = a / b;
						float c = Y - s.getY();
						float AperBszorC = AperB * c;
						
						float X = AperBszorC + s.getX();
						//float X = (d.getX()-s.getX()) / (d.getY()-s.getY()) * (Y - s.getY()) + s.getX();
						points.add(new Vector2D((int)X, (int)Y));
					}
				}
			}else{
				step = (Math.abs(d.getX() - s.getX())) / (pointCount + 1);
				float prev = s.getX();
				for (int i = 0; i < pointCount; i++){
					float X = prev + step;
					
					float a = d.getY() - s.getY();
					float b = d.getX() - s.getX();
					float aPerb = a/b;
					float c = X - s.getX();
					float AperBszorC = aPerb*c;
					
					float Y = AperBszorC + s.getY();
					
					points.add(new Vector2D((int)X, (int)Y));
					prev = X;
				}
			}

			Edge edge = null;
			if (gui.btnEdgeDelete.isEnabled()){
				if (selected instanceof Edge){
					edge = (Edge) selected;
					if ((!label.equals("")) && (!useRealDistance))
						edge.setLabel(label);
					else
						edge.setCost(cost);
					edge.setColor(clr); 
					edge.setHighlightColor(hlClr);
					edge.setStart(start);
					edge.setEnd(end);
					edge.setThickness(thickness);
					edge.setRealDistances(useRealDistance);
					if (edge.getPointsSize() != points.size())
					//	edge.setPoints(points);
						edge.straightenPoints(pointCount);
					
					gui.addLogEntry("Edge updated");
				}else{
					gui.addLogEntry("Can't update edge because it wasn't selected");
					return;
				}
			}else{
				if ((edgeCreation) && (spStartId != -1) && (spEndId != -1)){
					edge = new Edge(cost, clr, hlClr, start, end, thickness, intermediaryPoints, useRealDistance, gui.drawArea.getZoomPercent());
					if ((!label.equals("")) && (!useRealDistance))
						edge.setLabel(label);
					else
						edge.setCost(cost);
					gui.jcmiPopupAddEdge.doClick();
				}
				else{
					edge = new Edge(cost, clr, hlClr, start, end, thickness, null, useRealDistance, gui.drawArea.getZoomPercent());
					edge.straightenPoints(pointCount);
				}
//FIXED maye break here		//edge = new Edge(cost, clr, hlClr, start, end, thickness, points, useRealDistance);
				lastCost = cost;
				lastEClr = clr;
				lastEHLClr = hlClr;
				lastThickness = thickness;
				lastCustomPointCount = pointCount;
				lastRealDistance = useRealDistance;
				lastLabel = label;
				
				try {
					gui.drawArea.getGraph().addEdge(edge);
				} catch (MyException e1) {
					gui.addLogEntry(e1.getMessage());
					return;
				}
				gui.addLogEntry("New Edge added");
			}
			updateOptionsPanelState();
			repaintCanvas();
			//update the cost textfield if the edge was updated from real costs to real distances
			if (!label.equals(""))
				gui.tfEdgeCost.setText(edge.getLabel());
			else
				gui.tfEdgeCost.setText(edge.getCost()+"");
		}

	}
	 
	/**
	 * Inner class to handle the "Delete" button event on the edge panel
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class EdgeDeleteListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			if (selected == null){
				gui.addLogEntry("Can't delete edge because it wasn't selected");
				return;
			}
			if (gui.drawArea.getGraph() == null){
				gui.jmiNew.doClick();
			}
			if ((gui.drawArea.getGraph().getVertexCount() == 0) || (gui.drawArea.getGraph().getEdgeCount() == 0)){
				gui.addLogEntry("No edge to delete!");
				selected = null;
				return;
			}
			String edgeAsStr = "";
			if (selected instanceof Edge){
				Edge edge = (Edge) selected;
				edgeAsStr = edge.toString();
				
				try {
					gui.drawArea.getGraph().removeEdge(edge);
				} catch (MyException e1) {
					gui.addLogEntry(e1.getMessage());
					selected = null;
					return;
				}
			}else{
				gui.addLogEntry("Can't delete edge because it wasn't selected");
				return;
			}
			gui.addLogEntry(edgeAsStr + " deleted");
			selected = null;
			updateOptionsPanelState();
			repaintCanvas();
		}
		
	}
	
	/**
	 * Inner class to handle the "start" combobox selection changes on the edge panel,to highlight a vertex
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class EdgeStartsListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			/*
			if (gui.cmbEdgeStarts.getItemCount() < 1) 
				return;
			Vertex v = (Vertex) gui.cmbEdgeStarts.getSelectedItem();
			repaintCanvas();
			v.setHighLight(true);
			v.draw(gui.drawArea.getGraphics());
			v.setHighLight(false);
			*/
		}
		
	}
	
	/**
	 * Inner class to handle the "end" combobox selection changes on the edge panel,to highlight a vertex
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */
	public class EdgeEndsListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
				
			/*
			if (gui.cmbEdgeEnds.getItemCount() < 1)
				return;
			Vertex v = (Vertex) gui.cmbEdgeEnds.getSelectedItem();
			repaintCanvas();
			v.setHighLight(true);
			v.draw(gui.drawArea.getGraphics());
			v.setHighLight(false);
			*/
		}
		
	}
	
	/**
	 * Inner class to handle the real distance checkbox
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */	
	public class EdgeRealDistanceListener implements ActionListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			gui.tfEdgeCost.setEnabled(!gui.chbEdgeReal.isSelected());
			gui.tfEdgeCost.setEditable(!gui.chbEdgeReal.isSelected());
		}
		
	}
	
	/**
	 * Inner class to handle the the scrollpane changes in the drawArea
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */	
	public class DrawAreaHorizontalScrollListener implements AdjustmentListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void adjustmentValueChanged(AdjustmentEvent e) {
	        // Determine which scrollbar fired the event

			Adjustable source = e.getAdjustable();
			int orient = source.getOrientation();
			int value = e.getValue();


	        if (orient == Adjustable.HORIZONTAL) {
	        	gui.drawArea.setX(-value);
	        } else {
	        	gui.drawArea.setY(-value);
	        }
	        
	        repaintCanvas();
	        gui.drawArea.setLocation(0, 0);
		}
		
	}
	
	/**
	 * Inner class to handle the the mouse scroll changes in the drawArea for scaling/zooming
	 * @author Daroczi Krisztian-Zoltan
	 * @version 1.0
	 */	
	public class DrawAreaMouseWheelListener implements MouseWheelListener {
		/**
		 * Method called when the event is fired.
		 * @since Version 1.0
		 */
		@Override
		public void mouseWheelMoved(MouseWheelEvent e) {
			int scrollAmount = e.getWheelRotation() * 10;	//1 click => 10% zoom
			gui.drawArea.setZoomPercent(scrollAmount);
			repaintCanvas();
		}
		
	}
	
	public class AddVertexPopupActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			lastVertexPos.setX(lastPopupPos.getX());
			lastVertexPos.setY(lastPopupPos.getY());
			gui.jmiAddVertex.doClick();
			
			if (e.getSource() == gui.jmiPopupAddVertex1)
				gui.btnVertexOk.doClick();
			repaintCanvas();
			
		}
	}
	
	public class AddEdgePopupActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			edgeCreation = !edgeCreation;
			if (edgeCreation){
				if (showSp)
					gui.jmiShortestPath.doClick();
				gui.addLogEntry("Select the source vertex of the edge");
			}else{
				spStartId = -1;
				spEndId = -1;
				intermediaryPoints = null;
				gui.addLogEntry("Edge creation ended");
			}
			repaintCanvas();
		}
	}
	
	public class AddHighlightPopupListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			//gui.jcmiHighLight.setSelected(gui.jcmiHighLight.isSelected());

			if (selected instanceof Edge){
				Edge edge = (Edge) selected;
				edge.setHighLight(!edge.isHighLight());
			} else if ((selected instanceof Circle) || (selected instanceof Rectangle) || (selected instanceof Ellipse)){
				Vertex v = (Vertex) selected;
				v.setHighLight(!v.getHighLight());
			}
			
			repaintCanvas();
		}
		
	}
	
	public class AddGridLayoutKeepAspectRatioListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			//er = new EdgeRouter(gui.drawArea.getGraph());

			ILayout layout = new GridLayout(true);
			
			try{
				layout.setGraph(gui.drawArea.getGraph());
				layout.setDrawAreaProperties(new Vector2D(gui.drawArea.getWidth(), gui.drawArea.getHeight()), gui.drawArea.getZoomPercent(), false);
				layout.applyLayout();
				//if (!er.applyLayout())
				//	JOptionPane.showMessageDialog(gui, "Can't do an edge layout!");
				//gui.jmiScrollToCenter.doClick();
			}catch (IllegalArgumentException ex){
				gui.addLogEntry(ex.getMessage());
				JOptionPane.showMessageDialog(gui, ex.getMessage());
			}
			if (!debug)
				er = null;
			repaintCanvas();

		}		
	}
	
	public class AddGridLayoutSquareLayoutListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			//er = new EdgeRouter(gui.drawArea.getGraph());
			ILayout layout = new GridLayout(false);
			try{
				layout.setGraph(gui.drawArea.getGraph());
				layout.setDrawAreaProperties(new Vector2D(gui.drawArea.getWidth(), gui.drawArea.getHeight()), gui.drawArea.getZoomPercent(), false);
				//layout.applyLayout();
				
				
				//TODO remove after benchmark
				int counter = 0;
				long totalDuration = 0, min = Long.MAX_VALUE, max = Long.MIN_VALUE;
				while (counter <= 1){
					long before = System.nanoTime();
					layout.applyLayout();
					long duration = System.nanoTime() - before;
					if (duration < min)
						min = duration;
					if (duration > max)
						max = duration;
					totalDuration += duration;
					counter++;
				}
				//gui.addLogEntry("Average time is " + totalDuration / counter + " ns = " + (totalDuration / counter) / 1000000.0 + " ms; min="+min/1000000.0+" | max="+max/1000000.0);

				
				
				//if (!er.applyLayout())
				//	JOptionPane.showMessageDialog(gui, "Can't do an edge layout!");
				//gui.jmiScrollToCenter.doClick();
				
			}catch (IllegalArgumentException ex){
				gui.addLogEntry(ex.getMessage());
				JOptionPane.showMessageDialog(gui, ex.getMessage());
			}
			if (!debug)
				er = null;
			repaintCanvas();
		}
		
	}
	
	public class AddRadialLayoutListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			ILayout layout = new RadialLayout();
			//er = new EdgeRouter(gui.drawArea.getGraph());
			try{
				layout.setGraph(gui.drawArea.getGraph());
				layout.setDrawAreaProperties(new Vector2D(gui.drawArea.getWidth(), gui.drawArea.getHeight()), gui.drawArea.getZoomPercent(), false);
				
				//TODO remove after benchmark
				int counter = 0;
				long totalDuration = 0, min = Long.MAX_VALUE, max = Long.MIN_VALUE;
				while (counter < 1){
					long before = System.nanoTime();
					layout.applyLayout();
					long duration = System.nanoTime() - before;
					if (duration < min)
						min = duration;
					if (duration > max)
						max = duration;
					totalDuration += duration;
					counter++;
				}
				//gui.addLogEntry("Average time is " + totalDuration / counter + " ns = " + (totalDuration / counter) / 1000000.0 + " ms; min="+min/1000000.0+" | max="+max/1000000.0);

				
				
				//if (!er.applyLayout())
				//	JOptionPane.showMessageDialog(gui, "Can't do an edge layout!");
				if (!debug)
					gui.jmiScrollToCenter.doClick();
				
			}catch (IllegalArgumentException ex){
				gui.addLogEntry(ex.getMessage());
				JOptionPane.showMessageDialog(gui, ex.getMessage());
			}
			if (!debug)
				er = null;
			repaintCanvas();
		}	
	}
	
	public class AddCircularLayoutListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			ILayout layout = new CircularLayout();
			//er = new EdgeRouter(gui.drawArea.getGraph());
			try{
				layout.setGraph(gui.drawArea.getGraph());
				layout.setDrawAreaProperties(new Vector2D(gui.drawArea.getWidth(), gui.drawArea.getHeight()), gui.drawArea.getZoomPercent(), false);
				{
					//TODO remove after benchmark
					int counter = 0;
					long totalDuration = 0, min = Long.MAX_VALUE, max = Long.MIN_VALUE;
					while (counter <= 1){
						long before = System.nanoTime();
						int z = zoomFixPrepare();
						layout.applyLayout();
						zoomFixFinish(z);
						gui.drawArea.scrollToCenter();
						long duration = System.nanoTime() - before;
						if (duration < min)
							min = duration;
						if (duration > max)
							max = duration;
						totalDuration += duration;
						counter++;
					}
					//gui.addLogEntry("Average time is " + totalDuration / counter + " ns = " + (totalDuration / counter) / 1000000.0 + " ms; min="+min/1000000.0+" | max="+max/1000000.0);
					}
				{
					//TODO remove after benchmark
					int counter = 0;
					long totalDuration = 0, min = Long.MAX_VALUE, max = Long.MIN_VALUE;
					while (counter <= 1){
						long before = System.nanoTime();
						//if (!er.applyLayout())
						//	JOptionPane.showMessageDialog(gui, "Can't do an edge layout!");
						if (!debug)
							gui.jmiScrollToCenter.doClick();
	
						long duration = System.nanoTime() - before;
						if (duration < min)
							min = duration;
						if (duration > max)
							max = duration;
						totalDuration += duration;
						counter++;
					}
					//gui.addLogEntry(">>>Edge routing: Average time is " + totalDuration / counter + " ns = " + (totalDuration / counter) / 1000000.0 + " ms; min="+min/1000000.0+" | max="+max/1000000.0);
				}
				
				
			}catch (IllegalArgumentException ex){
				gui.addLogEntry(ex.getMessage());
				JOptionPane.showMessageDialog(gui, ex.getMessage());
			}
			if (!debug)
				er = null;
			repaintCanvas();
		}
		
	}
	
	public class AddTreeLayoutListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			ILayout layout = new TopologicalLayout();
			//er = new EdgeRouter(gui.drawArea.getGraph());
			try{
				layout.setGraph(gui.drawArea.getGraph());
				layout.setDrawAreaProperties(new Vector2D(gui.drawArea.getWidth(), gui.drawArea.getHeight()), gui.drawArea.getZoomPercent(), false);
				

				
				//TODO remove after benchmark
				int counter = 0;
				int resp = 0;
				long totalDuration = 0, min = Long.MAX_VALUE, max = Long.MIN_VALUE;
				while (counter <= 3){
					long before = System.nanoTime();
					resp = layout.applyLayout();
					long duration = System.nanoTime() - before;
					if (duration < min)
						min = duration;
					if (duration > max)
						max = duration;
					totalDuration += duration;
					counter++;
				}
				//gui.addLogEntry("Average time is " + totalDuration / counter + " ns = " + (totalDuration / counter) / 1000000.0 + " ms; min="+min/1000000.0+" | max="+max/1000000.0);

				
				if (resp == -1){
					String[] answers = {"Grid Layout", "Radial Layout", "Circular Layout", "None"};
					
					int answ = JOptionPane.showOptionDialog(
							null,
							"Consider applying another layout:",
							"Can't apply the Topological Layout",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.PLAIN_MESSAGE,
							null,
							answers,
							"None"
							);
					switch (answ){
						case 0:
							gui.jmiGridKeepAspectRatio.doClick();
							break;
						case 1:
							gui.jmiRadialLayout.doClick();
							break;
						case 2:
							gui.jmiCircularLayout.doClick();
							break;
						case -1:
							break;
						default:
							break;
					}
					repaintCanvas();
					return;
				}
				//if (!er.applyLayout())
				//	JOptionPane.showMessageDialog(gui, "Can't do an edge layout!");
				//gui.jmiScrollToCenter.doClick();
				
			}catch (IllegalArgumentException ex){
				gui.addLogEntry(ex.getMessage());
				JOptionPane.showMessageDialog(gui, ex.getMessage());
			}
			if (!debug)
				er = null;
			repaintCanvas();
		}
		
	}
	
	public class AddRandomLayout implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			ILayout layout = new RandomLayout();
			try{
				layout.setGraph(gui.drawArea.getGraph());
				layout.setDrawAreaProperties(new Vector2D(gui.drawArea.getWidth(), gui.drawArea.getHeight()), gui.drawArea.getZoomPercent(), false);
					//TODO remove after benchmark
					int counter = 0;
					long totalDuration = 0, min = Long.MAX_VALUE, max = Long.MIN_VALUE;
					while (counter <= 1){
						long before = System.nanoTime();

						layout.applyLayout();
						long duration = System.nanoTime() - before;
						if (duration < min)
							min = duration;
						if (duration > max)
							max = duration;
						totalDuration += duration;
						counter++;
					}
					//gui.addLogEntry("Average time is " + totalDuration / counter + " ns = " + (totalDuration / counter) / 1000000.0 + " ms; min="+min/1000000.0+" | max="+max/1000000.0);
			}catch (IllegalArgumentException ex){
				gui.addLogEntry(ex.getMessage());
				JOptionPane.showMessageDialog(gui, ex.getMessage());
			}
			if (!debug)
				er = null;
			repaintCanvas();
		}
		
	}
	
	public class AddSpringLayoutListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			ILayout layout = new ForceDirectedLayout();
			try{
				layout.setGraph(gui.drawArea.getGraph());
				layout.setDrawAreaProperties(new Vector2D(gui.drawArea.getWidth(), gui.drawArea.getHeight()), gui.drawArea.getZoomPercent(), false);
					//TODO remove after benchmark
					int counter = 0;
					long totalDuration = 0, min = Long.MAX_VALUE, max = Long.MIN_VALUE;
					while (counter < 10){
						long before = System.nanoTime();

						layout.applyLayout();
						long duration = System.nanoTime() - before;
						if (duration < min)
							min = duration;
						if (duration > max)
							max = duration;
						totalDuration += duration;
						counter++;
						//FIX ME remove this!!!!! -- done
						//gui.jmiRandomLayout.doClick();
					}
					//gui.addLogEntry("Average time is " + totalDuration / counter + " ns = " + (totalDuration / counter) / 1000000.0 + " ms; min="+min/1000000.0+" | max="+max/1000000.0);
			}catch (IllegalArgumentException ex){
				gui.addLogEntry(ex.getMessage());
				JOptionPane.showMessageDialog(gui, ex.getMessage());
			}
			if (!debug)
				er = null;
			repaintCanvas();
		}
		
	}
	
	public class AddEdgeRoutingListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			try{

				int z = zoomFixPrepare();
				er = new EdgeRouter(gui.drawArea.getGraph(), false);
				if (!er.applyLayout())
					JOptionPane.showMessageDialog(gui, "Can't do an edge layout!");
				zoomFixFinish(z);

			}catch (IllegalArgumentException ex){
				gui.addLogEntry(ex.getMessage());
				JOptionPane.showMessageDialog(gui, ex.getMessage());
			}
			if (!debug)
				er = null;
			repaintCanvas();
		}
		
	}
	
	public int zoomFixPrepare(){
		int zoom = 100 - gui.drawArea.getZoomPercent();
		gui.drawArea.setZoomPercent(-zoom);
		return zoom;
	}
	
	public void zoomFixFinish(int z){
		gui.drawArea.setZoomPercent(z);
	}
}

