/**
 * AmbientTalk/2 Project
 * VisualDataflowEditor.java
 * (c) Programming Technology Lab, 2006 - 2007
 * Authors: Tom Van Cutsem & Stijn Mostinckx
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package at.visualdataflow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jgraph.JGraph;
import org.jgraph.event.GraphModelEvent;
import org.jgraph.event.GraphModelListener;
import org.jgraph.event.GraphSelectionEvent;
import org.jgraph.event.GraphSelectionListener;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultCellViewFactory;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphCell;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphLayoutCache;
import org.jgraph.graph.GraphModel;

import edu.vub.at.exceptions.InterpreterException;
import edu.vub.at.objects.ATTypeTag;
import edu.vub.at.objects.natives.NATTypeTag;

/**
 * Visual Dataflow Editor based on the reactive version of AmbientTalk.
 * Makes use of ambient behaviors which in their turn use ambient references
 * to handle the event propagation.
 *
 * @author alombide
 */

public class VisualDataflowEditor 
	extends 
		JFrame 
	implements 
		ActionListener, 
		GraphSelectionListener,
		GraphModelListener,
		Printable {
	
	private GraphModel graphModel_ = new DefaultGraphModel();
	private JGraph graph_ = new JGraph(graphModel_);
	private Vector cells_ = new Vector();
	private VisualDataflowEngine engine_;
	private VisualDataflowNode startNode_;
	
	private HashMap idCellMap_ = new HashMap();
	private HashMap cellIdMap_ = new HashMap();
	private HashMap nodeIdMap_ = new HashMap();
	
	private JToolBar toolbar_ = new JToolBar("Tools");
	private JButton saveGraphButton_ = new JButton("Save");
	private JButton savePartialGraphButton_ = new JButton("Save selected");
	private JButton openGraphButton_ = new JButton("Open");
	private JButton openPartialGraphButton_ = new JButton("Import graph");
	private JButton printButton_ = new JButton("Print");
	private JButton newNodeButton_ = new JButton("New node");
	private JButton removeNodeButton_ = new JButton("Remove node");
	private JButton addEdgeButton_ = new JButton("Add edge");
	private JButton addFixedEdgeButton_ = new JButton("Add fixed edge");
	private JButton removeEdgeButton_ = new JButton("Remove edge");
	private JButton zoomInButton_ = new JButton("Zoom in");
	private JButton zoomOutButton_ = new JButton("Zoom out");
	
	private JButton deployButton_ = new JButton("Deploy!");
	
	private JButton errorCheckingButton_ = new JButton("Disable error checking");
	
	private JButton[] allButtons_ = { 
			saveGraphButton_,
			savePartialGraphButton_,
			openGraphButton_,
			openPartialGraphButton_,
			printButton_,
			newNodeButton_, 
			removeNodeButton_, 
			addEdgeButton_,
			addFixedEdgeButton_,
			removeEdgeButton_,
			deployButton_,
			zoomInButton_,
			zoomOutButton_,
			errorCheckingButton_};
	
	private boolean isErrorCheckingEnabled_ = true;
	
	private int edgeCount_ = 0;
	private int nodeCount_ = 0;
	
	
	public VisualDataflowEditor(VisualDataflowEngine app) {
		super("AmbientTalk Visual Dataflow Editor");
		
		/*try {
			UIManager.setLookAndFeel(
	            UIManager.getCrossPlatformLookAndFeelClassName());
		} catch(UnsupportedLookAndFeelException e) {
		       // handle exception
	    } catch (ClassNotFoundException e) {
		       // handle exception
		} catch (InstantiationException e) {
		       // handle exception
		} catch (IllegalAccessException e) {
		       // handle exception
		}*/
		
		// Switch off D3D because of Sun XOR painting bug
		// See http://www.jgraph.com/forum/viewtopic.php?t=4066
		System.setProperty("sun.java2d.d3d", "false");
		
		engine_ = app;
		
		add(toolbar_, BorderLayout.PAGE_START);
		toolbar_.add(saveGraphButton_);
		saveGraphButton_.setActionCommand("Save");
		saveGraphButton_.addActionListener(this);
		toolbar_.add(savePartialGraphButton_);
		savePartialGraphButton_.setActionCommand("SavePartial");
		savePartialGraphButton_.addActionListener(this);
		toolbar_.add(openGraphButton_);
		openGraphButton_.setActionCommand("Open");
		openGraphButton_.addActionListener(this);
		toolbar_.add(openPartialGraphButton_);
		openPartialGraphButton_.setActionCommand("OpenPartial");
		openPartialGraphButton_.addActionListener(this);
		toolbar_.add(printButton_);
		printButton_.setActionCommand("Print");
		printButton_.addActionListener(this);
		toolbar_.addSeparator();
		toolbar_.add(newNodeButton_);
		newNodeButton_.setActionCommand("NewNode");
		newNodeButton_.addActionListener(this);
		toolbar_.add(removeNodeButton_);
		removeNodeButton_.setActionCommand("RemoveNode");
		removeNodeButton_.addActionListener(this);
		toolbar_.addSeparator();
		toolbar_.add(addEdgeButton_);
		addEdgeButton_.setActionCommand("AddEdge");
		addEdgeButton_.addActionListener(this);
		toolbar_.add(addFixedEdgeButton_);
		addFixedEdgeButton_.setActionCommand("AddFixedEdge");
		addFixedEdgeButton_.addActionListener(this);
		toolbar_.add(removeEdgeButton_);
		removeEdgeButton_.setActionCommand("RemoveEdge");
		removeEdgeButton_.addActionListener(this);
		toolbar_.addSeparator();
		toolbar_.add(zoomInButton_);
		zoomInButton_.setActionCommand("ZoomIn");
		zoomInButton_.addActionListener(this);
		toolbar_.add(zoomOutButton_);
		zoomOutButton_.setActionCommand("ZoomOut");
		zoomOutButton_.addActionListener(this);
		
		toolbar_.addSeparator();
		toolbar_.add(deployButton_);
		deployButton_.setActionCommand("Deploy");
		deployButton_.addActionListener(this);
		
		toolbar_.addSeparator();
		toolbar_.add(errorCheckingButton_);
		errorCheckingButton_.setActionCommand("SwitchErrorChecking");
		errorCheckingButton_.addActionListener(this);
		
		// Control-drag should clone selection
		//graph_.setCloneable(true);
		// Enable edit without final RETURN keystroke
		graph_.setInvokesStopCellEditing(true);
		
		graph_.addGraphSelectionListener(this);
		this.valueChanged(null);
		graphModel_.addGraphModelListener(this);
		
		getContentPane().add(new JScrollPane(graph_));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
		
	}
	
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "Save") {
			saveGraph();
			return;
		}
		if (e.getActionCommand() == "Open") {
			openGraph();
			return;
		}
		if (e.getActionCommand() == "SavePartial") {
			saveSelectedCells();
			return;
		}
		if (e.getActionCommand() == "OpenPartial") {
			openPartialGraph();
			return;
		}
		if (e.getActionCommand() == "Print") {
			print();
			return;
		}
		if (e.getActionCommand() == "NewNode") {
			addNewNode();
			return;
		}
		if (e.getActionCommand() == "RemoveNode") {
			removeNode();
			return;
		}
		if (e.getActionCommand() == "AddEdge") {
			addEdge(true);
			return;
		}
		if (e.getActionCommand() == "AddFixedEdge") {
			addEdge(false);
			return;
		}
		if (e.getActionCommand() == "RemoveEdge") {
			removeEdge();
			return;
		}
		if (e.getActionCommand() == "ZoomIn") {
			graph_.setScale((graph_.getScale()) + 0.1);
			return;
		}
		if (e.getActionCommand() == "ZoomOut") {
			graph_.setScale((graph_.getScale()) - 0.1);
			return;
		}
		if (e.getActionCommand() == "SwitchErrorChecking") {
			isErrorCheckingEnabled_ = !isErrorCheckingEnabled_;
			if (isErrorCheckingEnabled_) {
				errorCheckingButton_.setText("Disable error checking");
			} else {
				errorCheckingButton_.setText("Enable error checking");
			}
			return;
		}
		if (e.getActionCommand() == "Deploy") {
			if (engine_.isRunning()) {
				deployButton_.setText("Deploy!");
				engine_.cancelExecution();
			} else {
				if (checkDuplicateTypetags()) {
					setEdgeArities();
					engine_.deploy();
				}
				deployButton_.setText("STOP");
			}
			return;
		}
	}
	
	private void print() {
		PrinterJob printJob = PrinterJob.getPrinterJob(); 
		PageFormat pageFormat = printJob.pageDialog(printJob.defaultPage());
		printJob.setPrintable(this, pageFormat);
		if (printJob.printDialog()) {
			try {
				printJob.print();
			} catch(PrinterException pexc) {
				pexc.printStackTrace();
			}
		} 
	}
	
	
	public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException { 
		Graphics2D g2 = (Graphics2D) g;

		// for faster printing, turn off double buffering 
		 //RepaintManager.currentManager(this).setDoubleBufferingEnabled(false); 

		JGraph printGraph = getPrintGraph();
		Dimension d = printGraph.getSize(); // get size of document 
		double graphWidth = d.width; // width in pixels 
		double graphHeight = d.height; // height in pixels 
		double pageHeight = pf.getImageableHeight(); // height of printer page
		double pageWidth = pf.getImageableWidth(); // width of printer page 

		double scale = Math.min((pageWidth / graphWidth), (pageHeight / graphHeight)); 
		int totalNumPages = 1;
		if (pf.getOrientation() == PageFormat.LANDSCAPE) {
			totalNumPages = (int) Math.ceil(scale * graphWidth / pageWidth); 
		} else {
			totalNumPages = (int) Math.ceil(scale * graphHeight / pageHeight);
		}
		
		// make sure not print empty pages 
		if (pageIndex >= totalNumPages) { 
			return Printable.NO_SUCH_PAGE; 
		} 
		// shift Graphic to line up with beginning of print-imageable region 
		g2.translate(pf.getImageableX(), pf.getImageableY()); 
		// shift Graphic to line up with beginning of next page to print 
		g2.translate(0f, -pageIndex * pageHeight); 
		// scale the page so the width fits... 
		g2.scale(scale, scale); 
		printGraph.paint(g2); // repaint the page for printing 
		return Printable.PAGE_EXISTS; 
	}
 
	private JGraph getPrintGraph(){
		GraphModel model = graphModel_;   //replace with your own graph instance
		GraphLayoutCache view = new GraphLayoutCache(model, new DefaultCellViewFactory());
		view.setSelectsAllInsertedCells(false); //avoid that added cells are selected
		JGraph printGraph = new JGraph(model, view);

		printGraph.setScale(1);
		printGraph.setGridVisible(false);
		printGraph.clearSelection();
		printGraph.setBackground(Color.white);
	
		JPanel panel = new JPanel();
		panel.setDoubleBuffered(false);
		panel.add(printGraph);
		panel.setVisible(true);
		panel.setEnabled(true);
		panel.addNotify();
		panel.validate();
	   
		return printGraph;
	}

	
	private void setEdgeArities() {
		Iterator it = cells_.iterator();
		while (it.hasNext()) {
			Object cell = it.next();
			if (cell instanceof DefaultEdge) {
				Object[] arityLabels = GraphConstants.getExtraLabels(((DefaultEdge)cell).getAttributes());
				VisualDataflowEdge edge = (VisualDataflowEdge)((VisualDataflowElementWrapper)((DefaultEdge)cell).getUserObject()).getElement();
				if ((arityLabels[0].equals("*")) || (arityLabels[1].equals("*"))) {
					edge.setIsBroadcastEdge(true);
				} else {
					edge.setIsBroadcastEdge(false);
				}
			}
		}
	}
	
	public void valueChanged(GraphSelectionEvent event) {
		for (int i = 0; i < allButtons_.length; i++) {
			allButtons_[i].setEnabled(true);
		}
		
		Object[] selected = graph_.getSelectionCells();
		
		if (selected.length == 0) {
			removeNodeButton_.setEnabled(false);
			removeEdgeButton_.setEnabled(false);
			removeEdgeButton_.setEnabled(false);
		}
		boolean allNodes = true;
		boolean allEdges = true;
		boolean startNodeSelected = false;
		VisualDataflowElement current;
		for (int i = 0; i < selected.length; i++) {
			Object userObject = ((DefaultGraphCell)selected[i]).getUserObject();
			try {
			current = ((VisualDataflowElementWrapper)userObject).getElement();
			if (current.isEdge()) {
				allNodes = false;
			}
			if (current.isNode()) {
				allEdges = false;
			}
			if (current == startNode_) {
				startNodeSelected = true;
			}
			} catch(ClassCastException exc) {
				if (userObject instanceof String) {
					// Could not create VisualDataflowElement AmbientTalk object
					break;
				}
				// Should not happen
				System.out.println("Could not convert user object!");
				throw exc;
			}
		}
		if (!allNodes || startNodeSelected) {
			removeNodeButton_.setEnabled(false);
		}
		if (!allEdges || selected.length != 1) {
			removeEdgeButton_.setEnabled(false);
		}
		if (!allNodes || selected.length != 2) {
			addEdgeButton_.setEnabled(false);
		}
		// Check if edge already exists
		if (allNodes && (selected.length == 2)) {
			VisualDataflowNode source = (VisualDataflowNode)((VisualDataflowElementWrapper)((DefaultGraphCell)selected[0]).getUserObject()).getElement();
			VisualDataflowNode target = (VisualDataflowNode)((VisualDataflowElementWrapper)((DefaultGraphCell)selected[1]).getUserObject()).getElement();
			VisualDataflowEdge[] outgoingEdges = source.getOutgoingEdges();
			for (int i = 0; i < outgoingEdges.length; i ++) {
				if (outgoingEdges[i].getTargetNode() == target) {
					addEdgeButton_.setEnabled(false);
				}
			}
		}
	}
	
	public void graphChanged(GraphModelEvent e) {
		// Apparently getAttributes returns the old attributes
		// and getPreviousAttributes the changed ones.
		Map old = e.getChange().getAttributes();
		Map changed = e.getChange().getPreviousAttributes();
		if ((old == null) || (changed == null)) {
			return;
		}
		Object[] changedCells = (Object[])e.getChange().getChanged();
		for (int i = 0; i < changedCells.length; i++) {
			GraphCell cell = (GraphCell)changedCells[i];
			Map oldCellAttributes = (Map)old.get(cell);
			Map newCellAttributes = (Map)changed.get(cell);
			Object oldValue = GraphConstants.getValue(oldCellAttributes);
			if (oldValue == null) {
				return;
			}
			try {
				VisualDataflowElement oldElement = ((VisualDataflowElementWrapper)oldValue).getElement();
				Object newValue = GraphConstants.getValue(newCellAttributes);
				if ((newValue == null) || (newValue == oldValue)) {
					// New cell added, no changed attributes
					// OR: what was changed was not the value
					return;
				}
				String newValueString = newValue.toString();
				AttributeMap allCellAttributes = cell.getAttributes();
				if(oldCellAttributes != null)
					allCellAttributes.putAll(oldCellAttributes);
				cell.setAttributes(allCellAttributes);
			
				if (oldElement.isNode()) {
					try {
						DefaultGraphCell node = (DefaultGraphCell)cell;
						((VisualDataflowNode)oldElement).setOperatorHostType(NATTypeTag.atValue(engine_.getOperatorHostTypeStringFromNodeString(newValueString)));
						((VisualDataflowNode)oldElement).setOperatorString(engine_.getOperatorStringFromNodeString(newValueString));
						node.setUserObject(allCellAttributes.get("value"));
						((VisualDataflowNode)oldElement).setContainsError(false);
					} catch(InterpreterException exc) {
						DefaultGraphCell node = (DefaultGraphCell)cell;
						node.setUserObject(allCellAttributes.get("value"));
						if (isErrorCheckingEnabled_) {
							((VisualDataflowNode)oldElement).setContainsError(true);
							new InvalidAmbientTalkCodeDialog(exc./*getCause().*/getMessage());
						}
					}
				}
				if (oldElement.isEdge()) {
					DefaultEdge edge = (DefaultEdge)cell;
					((VisualDataflowEdge)oldElement).setTypetag(NATTypeTag.atValue(newValueString));
					edge.setUserObject(allCellAttributes.get("value"));
				}
			} catch(ClassCastException exc) {
				if (oldValue instanceof String) {
					// Could not create VisualDataflowElement AmbientTalk object
					break;
				}
				// Should not happen
				System.out.println("Could not convert user object!");
				throw exc;
			}
		}
	}
	
	
	private boolean checkDuplicateTypetags() {
		Iterator it = cells_.iterator();
		Vector foundTags = new Vector();
		boolean noDuplicates = true;
		while (it.hasNext()) {
			VisualDataflowElementWrapper elementWrapper = (VisualDataflowElementWrapper)graphModel_.getValue(it.next());
			VisualDataflowElement element = elementWrapper.getElement();
			ATTypeTag tag;
			if (element.isNode()) {
				tag = ((VisualDataflowNode)element).getOperatorHostType();
			} else {
				tag = ((VisualDataflowEdge)element).getTypetag();
			}
			if (foundTags.contains(tag)) {
				new DuplicateTypetagDialog(tag);
				noDuplicates = false;
			}
			foundTags.add(tag);
		}
		return noDuplicates;
	}
	
	
	public void addNewNode() {
		nodeCount_++;
		VisualDataflowNode node = engine_.makeNewNode(NATTypeTag.atValue("node" + nodeCount_));
		try {
			node.setOperatorString("nil");
		} catch(InterpreterException exc) {
			// Should never happen
		}
		DefaultGraphCell cell = new DefaultGraphCell(new VisualDataflowElementWrapper(node));
		
		GraphConstants.setBounds(cell.getAttributes(), new Rectangle2D.Double(140, 140, 175, 150));
		GraphConstants.setBorder(cell.getAttributes(), BorderFactory.createLineBorder(Color.black));
		GraphConstants.setFont(cell.getAttributes(), new Font("Courier", Font.PLAIN, 12));
		
		cells_.add(cell);
		graph_.getGraphLayoutCache().insert(cell);
		
		engine_.addNode(node);
		Integer nodeId = new Integer(nodeCount_);
		cellIdMap_.put(cell, nodeId);
		nodeIdMap_.put(node, nodeId);
		idCellMap_.put(nodeId, cell);
	}
	
	public VisualDataflowNode makeStartNode() {
		VisualDataflowNode node = engine_.makeNewNode(NATTypeTag.atValue("node" + nodeCount_));
		try {
			node.setOperatorString("nil");
		} catch(InterpreterException exc) {
			// Should never happen.
		}
		DefaultGraphCell cell = new DefaultGraphCell(new VisualDataflowElementWrapper(node));
		
		GraphConstants.setBounds(cell.getAttributes(), new Rectangle2D.Double(140, 140, 175, 150));
		GraphConstants.setBorder(cell.getAttributes(), BorderFactory.createLineBorder(Color.BLUE));
		GraphConstants.setFont(cell.getAttributes(), new Font("Courier", Font.PLAIN, 12));
		
		cells_.add(cell);
		graph_.getGraphLayoutCache().insert(cell);
		Integer nodeId = new Integer(nodeCount_);
		cellIdMap_.put(cell, nodeId);
		nodeIdMap_.put(node, nodeId);
		idCellMap_.put(nodeId, cell);
		startNode_ = node;
		node.setIsStartNode(true);
		return node;
	}
	
	public VisualDataflowNode getStartNode() {
		return startNode_;
	}
	
	public void removeNode() {
		Object[] selected = graph_.getSelectionCells();
		for (int i = 0; i < selected.length; i++) {
			DefaultGraphCell current = (DefaultGraphCell)selected[i];
			removeEdges(((VisualDataflowElementWrapper)current.getUserObject()).getEdges());
			cells_.remove(current);
			idCellMap_.remove(cellIdMap_.get(current));
			cellIdMap_.remove(current);
			nodeIdMap_.remove(((VisualDataflowElementWrapper)current.getUserObject()).getElement());
			engine_.removeNode((VisualDataflowNode)((VisualDataflowElementWrapper)current.getUserObject()).getElement());
		}
		graphModel_.remove(selected);
	}
	
	public void addEdge(boolean isRebinding) {
		edgeCount_++;
		Object[] selected = graph_.getSelectionCells();
		DefaultGraphCell sourceCell = (DefaultGraphCell)selected[0];
		DefaultGraphCell targetCell = (DefaultGraphCell)selected[1];
		VisualDataflowElementWrapper sourceWrapper = (VisualDataflowElementWrapper)sourceCell.getUserObject();
		VisualDataflowElementWrapper targetWrapper = (VisualDataflowElementWrapper)targetCell.getUserObject();
		VisualDataflowNode source = (VisualDataflowNode)sourceWrapper.getElement();
		VisualDataflowNode target = (VisualDataflowNode)targetWrapper.getElement();
		
		VisualDataflowEdge edge = engine_.makeEdge(NATTypeTag.atValue("connection" + edgeCount_), source, target, isRebinding);
		
		DefaultEdge cell = new DefaultEdge(new VisualDataflowElementWrapper(edge));
		DefaultPort sourcePort = (DefaultPort)sourceCell.addPort();
		DefaultPort targetPort = (DefaultPort)targetCell.addPort();
		GraphConstants.setDisconnectable(sourcePort.getAttributes(), false);
		GraphConstants.setDisconnectable(targetPort.getAttributes(), false);
		cell.setSource(sourcePort);		
		cell.setTarget(targetPort);
		sourceWrapper.addEdge(cell);
		targetWrapper.addEdge(cell);
		
		if (isRebinding) {
			float[] dashPattern = { 5, 5 };
			GraphConstants.setDashPattern(cell.getAttributes(), dashPattern);
		}
		GraphConstants.setLineStyle(cell.getAttributes(), GraphConstants.STYLE_BEZIER);
		GraphConstants.setRouting(cell.getAttributes(), GraphConstants.ROUTING_DEFAULT);
		GraphConstants.setLabelPosition(cell.getAttributes(), new Point2D.Double(GraphConstants.PERMILLE/2, 0));
		
		Object[] labels = { new String("1"), new String("1") }; 
		Point2D[] labelPositions = { 
				new Point2D.Double (GraphConstants.PERMILLE*7/8, -20), 
				new Point2D.Double (GraphConstants.PERMILLE/8, -20) }; 
		GraphConstants.setExtraLabelPositions(cell.getAttributes(), labelPositions); 
		GraphConstants.setExtraLabels(cell.getAttributes(), labels);
		
		GraphConstants.setLineEnd(cell.getAttributes(), GraphConstants.ARROW_CLASSIC);
		GraphConstants.setFont(cell.getAttributes(), new Font("Courier", Font.PLAIN, 12));
		GraphConstants.setEndFill(cell.getAttributes(), true);
		GraphConstants.setDisconnectable(cell.getAttributes(), false);
		
		GraphConstants.setLabelAlongEdge(cell.getAttributes(), true);
		
		cells_.add(cell);
		graph_.getGraphLayoutCache().insertEdge(cell, sourceCell, targetCell);
	}
	
	public void removeEdge() {
		removeEdges(graph_.getSelectionCells());
	}
	
	public void removeEdges(Object[] toRemove) {
		for (int i = 0; i < toRemove.length; i++) {
			DefaultEdge edgeCell = (DefaultEdge)toRemove[i];
			VisualDataflowEdge edge = (VisualDataflowEdge)((VisualDataflowElementWrapper)edgeCell.getUserObject()).getElement();
			// Source and target ports might have been removed by removing the node first
			if ((edgeCell.getSource()) != null && (edgeCell.getTarget() != null)) {
				VisualDataflowElementWrapper sourceWrapper = (VisualDataflowElementWrapper)((DefaultGraphCell)((DefaultPort)edgeCell.getSource()).getParent()).getUserObject();
				VisualDataflowElementWrapper targetWrapper = (VisualDataflowElementWrapper)((DefaultGraphCell)((DefaultPort)edgeCell.getTarget()).getParent()).getUserObject();
				VisualDataflowNode source = (VisualDataflowNode)sourceWrapper.getElement();
				VisualDataflowNode target = (VisualDataflowNode)targetWrapper.getElement();
				source.removeOutgoingEdge(edge);
				target.removeIncomingEdge(edge);
				sourceWrapper.removeEdge(edgeCell);
				targetWrapper.removeEdge(edgeCell);
				cells_.remove(edgeCell);
				try {
					target.setOperatorString(target.getOperatorString());
				} catch(InterpreterException exc) {
					new InvalidAmbientTalkCodeDialog(exc./*getCause().*/getMessage());
				}
			}
			graphModel_.remove(toRemove);
		}
	}
	
	public void saveGraph() {
		JFileChooser chooser = new JFileChooser();
	    int returnVal = chooser.showSaveDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
	    	String filename = chooser.getSelectedFile().getPath();
	    	try {
	    		XMLEncoder enc = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(filename)));
	    		enc.setExceptionListener(new ExceptionListener() { 
	    			public void exceptionThrown(Exception e) { 
	    				e.printStackTrace(); 
	    			}});
	    		Iterator it = cells_.iterator();
	    		while (it.hasNext()) {
	    			saveCell(enc, (GraphCell)it.next());
	    		}
	    		enc.close();
	    	} catch(FileNotFoundException exc) {
	    		new FileNotFoundDialog(filename);
	    	}
	    }
	}
	
	public void saveSelectedCells() {
		JFileChooser chooser = new JFileChooser();
	    int returnVal = chooser.showSaveDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
	    	String filename = chooser.getSelectedFile().getPath();
	    	try {
	    		XMLEncoder enc = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(filename)));
	    		enc.setExceptionListener(new ExceptionListener() { 
	    			public void exceptionThrown(Exception e) { 
	    				e.printStackTrace(); 
	    			}});
	    		
	    		Object[] selected = graph_.getSelectionCells();
	    		HashSet cellsToSave = new HashSet();
	    		VisualDataflowElement current;
	    		for (int i = 0; i < selected.length; i++) {
	    			cellsToSave.add(selected[i]);
	    			DefaultGraphCell currentCell = (DefaultGraphCell)selected[i];
	    			VisualDataflowElementWrapper wrapper = (VisualDataflowElementWrapper)currentCell.getUserObject();
	    			current = wrapper.getElement();
	    			if (current.isEdge()) {
	    				cellsToSave.add(((DefaultEdge)currentCell).getSource());
	    				cellsToSave.add(((DefaultEdge)currentCell).getTarget());
	    			}
	    		}
	    		Iterator it = cellsToSave.iterator();
	    		while (it.hasNext()) {
	    			saveCell(enc, (GraphCell)it.next());
	    		}
	    			
	    		enc.close();
	    	} catch(FileNotFoundException exc) {
	    		new FileNotFoundDialog(filename);
	    	}
	    }
	}
	
	private void saveCell(XMLEncoder encoder, GraphCell cell) {
		AttributeMap allAttributes = cell.getAttributes();
		AttributeMap attributesToSave = new AttributeMap();
		//VisualDataflowElementWrapper elementWrapper = (VisualDataflowElementWrapper)GraphConstants.getValue(allAttributes);
		VisualDataflowElementWrapper elementWrapper = (VisualDataflowElementWrapper)((DefaultMutableTreeNode)cell).getUserObject();
		if (elementWrapper == null) {
			return;
		}
		String atElementValue = elementWrapper.toString();
		boolean isNode = elementWrapper.getElement().isNode();
		attributesToSave.put("isNode", new Boolean(isNode));
		attributesToSave.put("ATElementValue", atElementValue);
		if (isNode) {
			GraphConstants.setBounds(attributesToSave, GraphConstants.getBounds(allAttributes));
			attributesToSave.put("nodeId", cellIdMap_.get(cell));
			if (elementWrapper.getElement() == startNode_) {
				attributesToSave.put("isStartNode", new Boolean(true));
			} else {
				attributesToSave.put("isStartNode", new Boolean(false));
			}
		} else {
			VisualDataflowEdge edge = (VisualDataflowEdge)elementWrapper.getElement();
			attributesToSave.put("sourceId", nodeIdMap_.get(edge.getSourceNode()));
			attributesToSave.put("targetId", nodeIdMap_.get(edge.getTargetNode()));
			Object[] arityLabels = GraphConstants.getExtraLabels(((DefaultEdge)cell).getAttributes());
			attributesToSave.put("sourceArity", arityLabels[0]);
			attributesToSave.put("targetArity", arityLabels[1]);
			attributesToSave.put("isRebindingEdge", edge.isRebindingEdge());
		}
		encoder.writeObject(attributesToSave);
	}
	
	public void readCells(XMLDecoder decoder, int startNodeCount) {
		Vector nodeAttributes = new Vector();
		Vector edgeAttributes = new Vector();
		try {
			while (true) {
				AttributeMap attributes = (AttributeMap)decoder.readObject();
				if ((Boolean)attributes.get("isNode")) {
					nodeAttributes.add(attributes);
				} else {
					edgeAttributes.add(attributes);
				}
			}
		} catch(ArrayIndexOutOfBoundsException exc) {
			// First create node cells and store the nodes and their operator code
			Iterator it = nodeAttributes.iterator();
			Vector nodesAndOperatorCode = new Vector();
			while (it.hasNext()) {
				nodesAndOperatorCode.add(createNodeCellFromAttributeMap((AttributeMap)it.next(), startNodeCount));
			}
			// ... then the edges can be added to connect the nodes...
			Iterator edgeIt = edgeAttributes.iterator();
			while (edgeIt.hasNext()) {
				createEdgeCellFromAttributeMap((AttributeMap)edgeIt.next(), startNodeCount);
			}
			// ... and finally (now that we have the edges and the scopes in which the
			// the operator code should be evaluated in each node), the operator code
			// can be checked and set.
			Iterator nodesAndOperatorCodeIt = nodesAndOperatorCode.iterator();
			while (nodesAndOperatorCodeIt.hasNext()) {
				Object[] nodeAndOperatorCode = (Object[])nodesAndOperatorCodeIt.next();
				VisualDataflowNode node = (VisualDataflowNode)((VisualDataflowElementWrapper)((DefaultGraphCell)nodeAndOperatorCode[0]).getUserObject()).getElement();
				try {
					node.setOperatorString((String)nodeAndOperatorCode[1]);
				} catch(InterpreterException intExc) {
					DefaultGraphCell cell = (DefaultGraphCell)nodeAndOperatorCode[0];
					//cell.setUserObject(cell.getUserObject() + (String)nodeAndOperatorCode[1]);
					new InvalidAmbientTalkCodeDialog(intExc./*getCause().*/getMessage());
				}
			}
		}
	}
	
	private Object[] createNodeCellFromAttributeMap(AttributeMap attributes, int startNodeCount) {
		nodeCount_++;
		VisualDataflowNode node = engine_.makeNewNode(NATTypeTag.atValue("node" + nodeCount_));
		((VisualDataflowNode)node).setOperatorHostType(NATTypeTag.atValue(engine_.getOperatorHostTypeStringFromNodeString((String)attributes.get("ATElementValue"))));
		//((VisualDataflowNode)node).setOperatorString(engine_.getOperatorStringFromNodeString((String)attributes.get("ATElementValue")));
		DefaultGraphCell cell = new DefaultGraphCell(new VisualDataflowElementWrapper(node));
		
		GraphConstants.setBounds(cell.getAttributes(), GraphConstants.getBounds(attributes));
		GraphConstants.setFont(cell.getAttributes(), new Font("Courier", Font.PLAIN, 12));
		if ((Boolean)attributes.get("isStartNode")) {
			GraphConstants.setBorder(cell.getAttributes(), BorderFactory.createLineBorder(Color.BLUE));
			startNode_ = node;
			engine_.setStartNode(node);
			node.setIsStartNode(true);
		} else {
			GraphConstants.setBorder(cell.getAttributes(), BorderFactory.createLineBorder(Color.black));
		};
		
		cells_.add(cell);
		graph_.getGraphLayoutCache().insert(cell);
		
		engine_.addNode(node);
		Integer newNodeId = (Integer)attributes.get("nodeId") + startNodeCount;
		cellIdMap_.put(cell, newNodeId);
		nodeIdMap_.put(node, newNodeId);
		idCellMap_.put(newNodeId, cell);
		Object[] result = { cell, engine_.getOperatorStringFromNodeString((String)attributes.get("ATElementValue")) };
		return result;
	}
	
	private void createEdgeCellFromAttributeMap(AttributeMap attributes, int startNodeCount) {
		edgeCount_++;
		DefaultGraphCell sourceCell = (DefaultGraphCell)idCellMap_.get((Integer)attributes.get("sourceId") + startNodeCount);
		DefaultGraphCell targetCell = (DefaultGraphCell)idCellMap_.get((Integer)attributes.get("targetId") + startNodeCount);
		VisualDataflowElementWrapper sourceWrapper = (VisualDataflowElementWrapper)sourceCell.getUserObject();
		VisualDataflowElementWrapper targetWrapper = (VisualDataflowElementWrapper)targetCell.getUserObject();
		VisualDataflowNode sourceNode = (VisualDataflowNode)sourceWrapper.getElement();
		VisualDataflowNode targetNode = (VisualDataflowNode)targetWrapper.getElement();
		
		VisualDataflowEdge edge = engine_.makeEdge(NATTypeTag.atValue(
				(String)attributes.get("ATElementValue")), 
				sourceNode, 
				targetNode, 
				(Boolean)attributes.get("isRebindingEdge"));
		
		DefaultEdge cell = new DefaultEdge(new VisualDataflowElementWrapper(edge));
		DefaultPort sourcePort = (DefaultPort)sourceCell.addPort();
		DefaultPort targetPort = (DefaultPort)targetCell.addPort();
		GraphConstants.setDisconnectable(sourcePort.getAttributes(), false);
		GraphConstants.setDisconnectable(targetPort.getAttributes(), false);
		cell.setSource(sourcePort);		
		cell.setTarget(targetPort);
		sourceWrapper.addEdge(cell);
		targetWrapper.addEdge(cell);
		
		if (edge.isRebindingEdge()) {
			float[] dashPattern = { 5, 5 };
			GraphConstants.setDashPattern(cell.getAttributes(), dashPattern);
		}
		GraphConstants.setLineStyle(cell.getAttributes(), GraphConstants.STYLE_BEZIER);
		GraphConstants.setRouting(cell.getAttributes(), GraphConstants.ROUTING_DEFAULT);
		GraphConstants.setLabelPosition(cell.getAttributes(), new Point2D.Double(GraphConstants.PERMILLE/2, 0));
		
		Object[] labels = { attributes.get("sourceArity"), attributes.get("targetArity") }; 
		Point2D[] labelPositions = { 
				new Point2D.Double (GraphConstants.PERMILLE*7/8, -10), 
				new Point2D.Double (GraphConstants.PERMILLE/8, -10) }; 
		GraphConstants.setExtraLabelPositions(cell.getAttributes(), labelPositions); 
		GraphConstants.setExtraLabels(cell.getAttributes(), labels);
		
		GraphConstants.setLineEnd(cell.getAttributes(), GraphConstants.ARROW_CLASSIC);
		GraphConstants.setFont(cell.getAttributes(), new Font("Courier", Font.PLAIN, 12));
		GraphConstants.setEndFill(cell.getAttributes(), true);
		GraphConstants.setDisconnectable(cell.getAttributes(), false);
		
		GraphConstants.setLabelAlongEdge(cell.getAttributes(), true);
		
		cells_.add(cell);
		graph_.getGraphLayoutCache().insertEdge(cell, sourceCell, targetCell);
	}
	
	
	public void openGraph() {
		cellIdMap_ = new HashMap();
		nodeIdMap_ = new HashMap();
		idCellMap_ = new HashMap();
		JFileChooser chooser = new JFileChooser();
	    int returnVal = chooser.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
	    	String filename = chooser.getSelectedFile().getPath();
	    	try {
	    		XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(filename)));
	    		
	    		engine_.reset();
	    		VisualDataflowEditor editor = new VisualDataflowEditor(engine_);
	    		engine_.setEditor(editor);
	    		editor.readCells(decoder, 0);
	    		
	    		this.dispose();
	    		
	    		decoder.close();
	    	} catch(FileNotFoundException exc) {
	    		new FileNotFoundDialog(filename);
	    	}
	    }
	}
	
	public void openPartialGraph() {
		JFileChooser chooser = new JFileChooser();
	    int returnVal = chooser.showOpenDialog(this);
	    if (returnVal == JFileChooser.APPROVE_OPTION) {
	    	String filename = chooser.getSelectedFile().getPath();
	    	try {
	    		XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(filename)));
	    		readCells(decoder, nodeCount_);
	    		decoder.close();
	    	} catch(FileNotFoundException exc) {
	    		new FileNotFoundDialog(filename);
	    	}
	    }
	}
	
	
	private class InvalidAmbientTalkCodeDialog extends Frame implements ActionListener {
		
		public InvalidAmbientTalkCodeDialog(String message) {
			
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(new JLabel("Failed to accept AmbientTalk code."));
			add(new JLabel("CAUSE:"));
			add(new JLabel(message));
			JButton okButton = new JButton("Ok");
			okButton.addActionListener(this);
			okButton.setActionCommand("ok");
			add(okButton);
		
			pack();
			setVisible(true);
		}
		
		public void actionPerformed(ActionEvent ae) {
			this.dispose();
		}
	}

	
private class DuplicateTypetagDialog extends Frame implements ActionListener {
		
		public DuplicateTypetagDialog(ATTypeTag typetag) {
			
			this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(new JLabel("WARNING: Found duplicate node or edge name:"));
			add(new JLabel(typetag.toString()));
			JButton okButton = new JButton("Ok");
			okButton.addActionListener(this);
			okButton.setActionCommand("ok");
			add(okButton);
		
			pack();
			setVisible(true);
		}
		
		public void actionPerformed(ActionEvent ae) {
			this.dispose();
		}
	}


private class FileNotFoundDialog extends Frame implements ActionListener {
	
	public FileNotFoundDialog(String filepath) {
		
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(new JLabel("File not found!"));
		add(new JLabel(filepath));
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(this);
		okButton.setActionCommand("ok");
		add(okButton);
	
		pack();
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent ae) {
		this.dispose();
	}
}
}
