//Copyright (C) 2008 United States Government as represented by the
//Administrator of the National Aeronautics and Space Administration
//(NASA).  All Rights Reserved.

//This software is distributed under the NASA Open Source Agreement
//(NOSA), version 1.3.  The NOSA has been approved by the Open Source
//Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
//directory tree for the complete NOSA document.

//THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
//KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
//LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
//SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
//A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
//THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
//DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.

package gov.nasa.jpf.tools.visualize;

import gov.nasa.jpf.sc.State;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.Timer;

import javax.swing.JPanel;

/**
 * The VisualDiagram is a graphical representation of a statechart.  It is
 * powered by GraphViz's dot language and requires that dot be installed and
 * accessible from the command line.
 * 
 * @author Carl Albach
 *
 */

public class VisualDiagram extends StateMachineDiagram {

	private static final String TITLE = "Visual State Machine Diagram";
	private static final int CONVERSION_ERROR = 10; // buffer space in pixels
	private static final boolean FILL_ARROWHEAD = true;
	private static final double ARROW_LENGTH = 10; //in pixels
	private static final int LABEL_SHIFT = 2; //distance from edge in pixels 

	private static double DPI;
	private static final int ORIGINAL_FONT_SIZE = 14;
	private static final int MIN_FONT_SIZE = 10;

	private static final Color PATHFINDER_COLOR = Color.RED;
	private static final Color NODE_OUTLINE_COLOR = Color.BLACK;
	private static final Color NODE_FILL_COLOR = new Color(242, 229, 201);
	private static final Color NODE_LABEL_COLOR = Color.BLACK;
	private static final Color EDGE_COLOR = Color.BLACK;
	private static final Color EDGE_LABEL_COLOR = Color.BLACK;
	private static final Color EXTERNAL_NODE_FILL = new Color(146, 201, 173);

	private static final int PATHFINDER_HEIGHT = 40; //in pixels
	private static final int PATHFINDER_WIDTH = 40;
	private static final Stroke PATHFINDER_STROKE = new BasicStroke(5);
	private static final double FPS = 30.0; //frames per second

	private static final String NEW_LINE = System.getProperty("line.separator");

	private JPanel drawingPanel;

	private int centerShiftX, centerShiftY;
	private int width, height;

	private final Set<Pathfinder> pathfinders = new HashSet<Pathfinder>();
	private final Set<DiagramNode> nodes = new HashSet<DiagramNode>();
	private final Set<DiagramEdge> edges = new HashSet<DiagramEdge>();

	private ProcessBuilder dot;
	private File dotIn, dotOut;



	public VisualDiagram(NotificationConsole noteConsole) {
		super(noteConsole);

		Process p = null;

		//Check to make sure dot is installed.  If its not, error msg and exit.
		try {
			p = new ProcessBuilder("dot", "-Tplain").start();

		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Graphviz's dot command is not accessible. " +
					"See ReadMe for instructions on installing dot.", "Error!", JOptionPane.ERROR_MESSAGE);
			System.exit(0);

		} finally {
			if(p!=null)
				p.destroy();
		}


		drawingPanel = new DrawingPanel(); // inner class: JPanel with overridden draw()

		drawingPanel.setBackground(Color.WHITE);
		DPI = drawingPanel.getToolkit().getScreenResolution();

		try {
			dotIn = File.createTempFile("scdiagram", "dot");
			dotOut = File.createTempFile("scdiagram", "plain");

		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		dotIn.deleteOnExit();
		dotOut.deleteOnExit();

		dot = new ProcessBuilder("dot", "-Tplain", 
				"-o" + dotOut.getPath(), dotIn.getPath());

	}

	protected synchronized void clearDiagram() {

		for(Pathfinder pf : pathfinders) {
			if(pf.inTransit) {
				pf.transitTimer.stop();
			}
		}
		pathfinders.clear();
		nodes.clear();
		edges.clear();

		drawingPanel.repaint();

	}

	protected Component getDiagram() {
		return drawingPanel;
	}

	public String getName() {
		return TITLE;		
	}

	public void dispose() {
		dotIn.delete();
		dotOut.delete();
	}

	public Component getLegend() {
		return DiagramLegend.SINGLETON;
	}


	protected synchronized void setDiagram(List<State> visibleStates) throws IOException {

		clearDiagram();

		width = drawingPanel.getWidth();
		height = drawingPanel.getHeight();

		if(visibleStates == null || visibleStates.isEmpty())
			return;

		boolean portrait = width <= height;
		generateDotFile(visibleStates, dotIn, portrait); //create the dot file

		Process dotRun = dot.start(); //run dot on the file

		try {
			dotRun.waitFor(); //wait for dot to finish

		} catch (InterruptedException e) {
			System.err.println(e);
		}


		Scanner s = new Scanner(new FileReader(dotOut));
		parsePlainText(s, visibleStates); //parse dot's output

		drawingPanel.repaint();

	}


	protected synchronized void setActives(List<State> activeStates) {

		pathfinders.clear();
		for(State s : activeStates) {
			pathfinders.add(new Pathfinder(findNode(s)));
		}


	}


	protected Timer animate(State src, State tgt, int animationSpeed) {


		//look for a pathfinder which is currently at this state
		Pathfinder match = null;
		for(Pathfinder pf : pathfinders) {
			if(src.equals(pf.currentNode.getState())) {
				match = pf;
				break;
			}
		}

		if(match == null) //node not found
			throw new RuntimeException("Pathfinder for animation not found.");

		//animate to the respective node
		return match.animateTo(findNode(tgt), animationSpeed); 
	}

	protected synchronized Timer start(State s, int animationSpeed) {

		DiagramNode tgt = findNode(s);
		String srcName = "START-" + tgt.getName();
		for(DiagramNode node : nodes) {

			if(node.getName().equals(srcName)) {
				Pathfinder pf = new Pathfinder(node);
				pathfinders.add(pf);
				return pf.animateTo(tgt, animationSpeed);
			}

		}

		throw new RuntimeException("Couldn't start state " + s);

	}

	protected synchronized Timer kill(State s, int animationSpeed) {

		Pathfinder pf = forState(s);
		String tgtName = "END-" + pf.currentNode.getName();
		for(DiagramNode node : nodes) {

			if(node.getName().equals(tgtName)) {
				return pf.animateTo(node, animationSpeed);
			}

		}

		throw new RuntimeException("Couldn't kill state " + s);

	}

	protected synchronized Timer enter(State ext, State tgt, int animationSpeed) {

		DiagramNode srcNode = findExternalNode(ext);
		Pathfinder pf = new Pathfinder(srcNode);
		pathfinders.add(pf);

		return pf.animateTo(findNode(tgt), animationSpeed);
	}

	protected synchronized Timer exit(State src, State ext, int animationSpeed) {

		DiagramNode tgtNode = findExternalNode(ext);
		Pathfinder pf = forState(src);

		return pf.animateTo(tgtNode, animationSpeed);

	}

	private boolean isInitial(State state) {

		for(State s = state.getSuperState(); s != null; s = s.getSuperState()) {

			for(State o : s.getInitStates()) {
				if(state.equals(o))
					return true;
			}

		}

		return false;

	}

	private void generateDotFile(List<State> visibleStates, File dotIn, boolean portrait) throws IOException {

		FileWriter fw = new FileWriter(dotIn);

		double graphWidth = ((double)width - CONVERSION_ERROR) / DPI;
		double graphHeight = ((double)height - CONVERSION_ERROR) / DPI;

		fw.append("digraph States { " + NEW_LINE);
		fw.append("size = \"" + graphWidth + "," + 
				graphHeight + "\";" + NEW_LINE);

		if(!portrait) {
			fw.append("orientation=landscape;" + NEW_LINE);
		}



		//Add all visible states, external states, pseudo states and edges
		for(State state: visibleStates) {

			//checking for composite
			String shape = state.getSubStates() != null ? "box":"ellipse"; 

			String name = state.getClass().getSimpleName();

			fw.append("\"" + state.getName() + "\"[label=\"" + name + 
					"\", shape=\"" + shape + "\"];");
			fw.append(NEW_LINE);

			//if we need a START pseudo state...
			if(isInitial(state)) {
				String startName = "START-" + state.getName();
				fw.append("\"" + startName + "\"[label=\"\", shape=\"circle\"];");
				fw.append(NEW_LINE);
				fw.append("\"" + startName + "\"->\"" + state.getName() + "\";");
				fw.append(NEW_LINE);
			}

			Map<State, Set<String>> edgeMap = EdgeUtility.getEdges(state);

			//Add all of the neighboring edges
			for(State nextState : edgeMap.keySet()) {

				String nextName = "";

				//this is an end pseudo state, need to initialize the node
				if(nextState == null) {
					nextName = "END-" + state.getName();
					fw.append("\"" + nextName + "\"[label=\"\", shape=\"circle\"];");

				} else {
					nextName = nextState.getName();
				}

				fw.append("\"" + state.getName() + "\" -> ");							
				fw.append("\"" + nextName + "\" [label=\"");

				boolean first = true;
				for(String label : edgeMap.get(nextState)) {

					if(!first) {
						fw.append(",");
					} else {
						first = false;
					}
					fw.append(label);

				}
				fw.append("\"];");
				fw.append(NEW_LINE);
			}


			//Then add all edges  and external nodes
			Map<String, Set<String>> externals = EdgeUtility.getExternalEdges(state);

			for(String s : externals.keySet()) {

				fw.append("\"" + s + "\"[label=\"" + trimExternal(s) + 
				"\", shape=\"box\"];");
				fw.append(NEW_LINE);

				if(s.endsWith("-TO")) {
					fw.append("\"" + state.getName() + "\" -> ");							
					fw.append("\"" + s + "\" [label=\"");

				} else {
					fw.append("\"" + s + "\" -> ");							
					fw.append("\"" + state.getName() + "\" [label=\"");
				}

				boolean first = true;
				for(String label : externals.get(s)) {

					if(!first) {
						fw.append(", ");
					} else {
						first = false;
					}
					fw.append(label);

				}
				fw.append("\"];");
				fw.append(NEW_LINE);
			}
		}


		fw.append("}");

		fw.close();
	}

	private String trimExternal(String name) {

		int firstDash = name.indexOf('-');
		int lastDash = name.lastIndexOf('-');

		String typeName = name.substring(firstDash + 1, lastDash);
		String[] pieces = typeName.split("\\.");

		if(pieces.length > 2) {
			return pieces[pieces.length-2] + '.' + pieces[pieces.length-1]; 

		} else {
			return typeName;
		}

	}

	private double scaleFactor;

	private void parsePlainText(Scanner s, List<State> visibleStates) throws IOException {

		String line;

		while( s.hasNextLine()) {

			line = s.nextLine();

			if(line.startsWith("graph")) {

				scaleFactor = initGraph(line);

			} else if(line.startsWith("node")) {

				//determine the type of node to spawn
				if(line.startsWith("END", 6) ||line.startsWith("START", 6)) {
					nodes.add(new PseudoNode(line, visibleStates));

				} else if(line.startsWith("EXT", 6)) {
					nodes.add(new ExternalNode(line, visibleStates));

				} else {
					nodes.add(new NeighborNode(line, visibleStates));
				}



			} else if (line.startsWith("edge")) {
				edges.add(new DiagramEdge(line, visibleStates));


			} else if(line.startsWith("stop")) {
				break;

			}
		}
	}


	private double initGraph(String line) {

		String[] graphConfig = line.split(" +");
		double scaleFactor = Double.parseDouble(graphConfig[1]);

		int graphWidth = (int)(Double.parseDouble(graphConfig[2]) * scaleFactor * DPI);
		int graphHeight = (int)(Double.parseDouble(graphConfig[3]) * scaleFactor * DPI);

		centerShiftX = width/2 - graphWidth/2;
		centerShiftY = height/2 - graphHeight/2;

		return scaleFactor;
	}

	public boolean isEdgeToSelf(DiagramNode node) {

		try {
			findEdge(node,node);
			return true;


		} catch (RuntimeException e) {
			return false;
		}

	}

	public DiagramEdge findEdge(DiagramNode fromNode, DiagramNode toNode) {

		for(DiagramEdge edge : edges) {
			if(fromNode == edge.getTail() && toNode == edge.getHead()) {
				return edge;
			}
		}

		throw new RuntimeException("Edge not found for animation.");

	}


	public DiagramNode findNode(State state) {

		for(DiagramNode node : nodes) {
			if(state.equals(node.getState()))
				return node;
		}

		return null;

	}

	private DiagramNode findExternalNode(State state) {
		DiagramNode match = null;

		for(State s = state; s != null; s = s.getSuperState()) {
			for(DiagramNode node : nodes) {
				if(node instanceof ExternalNode  && s == node.getState()) {
					match = node;
				}
			}
		}

		if(match == null)
			throw new RuntimeException("External node " + state + " not found.");

		return match;
	}

	private Pathfinder forState(State s) {

		for(Pathfinder pf : pathfinders) {
			if(s.equals(pf.currentNode.state))
				return pf;
		}

		throw new RuntimeException("Pathfinder could not be found for state " + s);
	}



	/**
	 * A JPanel with a paint override.
	 *
	 */

	private class DrawingPanel extends JPanel{

		public void paintComponent(Graphics g) {
			super.paintComponent(g);

			int size = (int)Math.max((ORIGINAL_FONT_SIZE * scaleFactor), MIN_FONT_SIZE);
			g.setFont(new Font("Times New Roman", Font.PLAIN, size));

			synchronized(VisualDiagram.this) {
				g.translate(centerShiftX, centerShiftY);

				for(DiagramNode node : nodes) {
					node.draw(g);
				}

				for(DiagramEdge edge : edges) {
					edge.draw(g);
				}

				for(Pathfinder pf : pathfinders) {
					pf.draw(g);
				}

			}
		}
	}

	/**
	 * See through pane explaining the symbols on the diagram.
	 */
	private static class DiagramLegend extends JPanel {

		public static final DiagramLegend SINGLETON = new DiagramLegend();
		private static final int ICON_SIZE = 20;
		private static final int CLOSE_SIZE = 2*ICON_SIZE/3;

		private DiagramLegend() {

			setBackground(new Color(184,207,229,150));
			setForeground(null);

			Box b = Box.createVerticalBox();
			b.add(createCompositeDef());
			b.add(createLeafDef());
			b.add(createExternalDef());
			b.add(createPseudoDef());

			add(b);

			setAlignmentX(Component.RIGHT_ALIGNMENT);
			setAlignmentY(Component.BOTTOM_ALIGNMENT);
		}

		private Component createCompositeDef() {
			Box b = Box.createHorizontalBox();
			b.add(new JLabel(new Icon() {
				public int getIconHeight() {
					return ICON_SIZE;
				}
				public int getIconWidth() {
					return ICON_SIZE;
				}
				public void paintIcon(Component c, Graphics g, int x, int y) {
					g.translate(ICON_SIZE/6, ICON_SIZE/6);
					g.setColor(NODE_FILL_COLOR);
					g.fillRect(x, y, 2*ICON_SIZE/3, 2*ICON_SIZE/3);
					g.setColor(Color.BLACK);
					g.drawRect(x, y, 2*ICON_SIZE/3, 2*ICON_SIZE/3);
				}
			}));

			b.add(new JLabel("Composite State"));
			Icon icon = new Icon() {

				public int getIconHeight() {
					return CLOSE_SIZE;
				}

				public int getIconWidth() {
					return CLOSE_SIZE;
				}

				public void paintIcon(Component c, Graphics g, int x, int y) {

					int frac = 5;

					g.translate(CLOSE_SIZE/frac, CLOSE_SIZE/frac);
					g.drawLine(0, 0, (frac - 2)*CLOSE_SIZE/frac, (frac - 2)*CLOSE_SIZE/frac);
					g.drawLine(0, (frac - 2)*CLOSE_SIZE/frac, (frac - 2)*CLOSE_SIZE/frac, 0);

				}

			};

			JButton closingButton = new JButton(icon);
			closingButton.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					DiagramLegend.SINGLETON.setVisible(false);
				}

			});
			Dimension d = new Dimension(CLOSE_SIZE, CLOSE_SIZE);
			closingButton.setPreferredSize(d);
			closingButton.setMinimumSize(d);
			closingButton.setMaximumSize(d);
			//closingButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);

			b.add(Box.createHorizontalStrut(18));
			b.add(closingButton);
			b.add(Box.createHorizontalGlue());
			return b;
		}

		private Component createLeafDef() {
			Box b = Box.createHorizontalBox();
			b.add(new JLabel(new Icon() {
				public int getIconHeight() {
					return ICON_SIZE;
				}
				public int getIconWidth() {
					return ICON_SIZE;
				}
				public void paintIcon(Component c, Graphics g, int x, int y) {
					g.translate(ICON_SIZE/6, ICON_SIZE/6);
					g.setColor(NODE_FILL_COLOR);
					g.fillOval(x, y, 2*ICON_SIZE/3, 2*ICON_SIZE/3);
					g.setColor(Color.BLACK);
					g.drawOval(x, y, 2*ICON_SIZE/3, 2*ICON_SIZE/3);
				}
			}));
			b.add(new JLabel("Non-composite State"));
			b.add(Box.createHorizontalGlue());
			return b;
		}

		private Component createExternalDef() {

			Box b = Box.createHorizontalBox();
			b.add(new JLabel(new Icon() {
				public int getIconHeight() {
					return ICON_SIZE;
				}
				public int getIconWidth() {
					return ICON_SIZE;
				}
				public void paintIcon(Component c, Graphics g, int x, int y) {
					g.translate(ICON_SIZE/6, ICON_SIZE/6);
					g.setColor(EXTERNAL_NODE_FILL);
					g.fillRoundRect(x, y, 2*ICON_SIZE/3, 2*ICON_SIZE/3, ICON_SIZE/10, ICON_SIZE/10);
					g.setColor(Color.BLACK);
					g.drawRoundRect(x, y, 2*ICON_SIZE/3, 2*ICON_SIZE/3, ICON_SIZE/10, ICON_SIZE/10);
				}
			}));
			b.add(new JLabel("External State"));
			b.add(Box.createHorizontalGlue());

			return b;

		}

		private Component createPseudoDef() {
			Box b = Box.createHorizontalBox();
			b.add(new JLabel(new Icon() {
				public int getIconHeight() {
					return ICON_SIZE;
				}
				public int getIconWidth() {
					return ICON_SIZE;
				}
				public void paintIcon(Component c, Graphics g, int x, int y) {
					g.translate(ICON_SIZE/6, ICON_SIZE/6);
					g.fillOval(x, y, 2*ICON_SIZE/3, 2*ICON_SIZE/3);
				}
			}));
			b.add(new JLabel("Start/End Node"));
			b.add(Box.createHorizontalGlue());
			return b;
		}



	}

	/**
	 * Represents a node upon the diagram. 
	 *
	 */


	private abstract class DiagramNode {

		protected int x, y, width, height;

		protected String name;
		protected State state;


		/**
		 * @param dotLine should be in this format:
		 * 
		 *  	node name x y xsize ysize label style shape color fillcolor
		 */

		DiagramNode(String dotLine, List<State> visibleStates) {

			String[] nodeConfig = dotLine.split(" +");
			name = nodeConfig[1].replaceAll("\"", "");

			width = (int)(Double.parseDouble(nodeConfig[4]) * DPI * scaleFactor);
			height = (int)(Double.parseDouble(nodeConfig[5]) * DPI * scaleFactor);


			x =(int)(Double.parseDouble(nodeConfig[2]) * DPI * scaleFactor);
			y = (int)(Double.parseDouble(nodeConfig[3]) * DPI * scaleFactor);

			//dot gives us the center of the node... need to shift to top left corner
			x -= width/2;
			y -= height/2;

		}

		public int hashCode() {
			int hash = 3*x + 2*y;
			if(state != null)
				hash += state.hashCode();

			return hash;
		}

		abstract void draw(Graphics g);

		String getName() {
			return name;
		}

		State getState(){
			return state;
		}

		Point getCenter() {
			return new Point(x + width/2, y + height/2);
		}

	}

	/**
	 * This node simply represents a visible state.  The state represented can 
	 * be either composite or non-composite.  If the state is composite, the
	 * node will be drawn as a rectangle.  Otherwise, the node will be drawn as
	 * an oval.
	 */
	private class NeighborNode extends DiagramNode {

		boolean composite;
		private int labelX, labelY;
		private String label;

		public NeighborNode(String dotLine, List<State> visibleStates) {
			super(dotLine, visibleStates);

			String[] nodeConfig = dotLine.split(" +");
			state = EdgeUtility.forStateName(visibleStates, name);

			label = nodeConfig[6];
			labelX = x + width/2; //set label to the center for now
			labelY = y + height/2;

			composite = nodeConfig[8].equals("box");

		}

		public void draw(Graphics g) {
			g.setColor(NODE_FILL_COLOR);   

			if(composite) {
				g.fillRect(x, y, width, height);
			} else {
				g.fillOval(x, y, width, height);
			}

			g.setColor(NODE_OUTLINE_COLOR);

			if(composite) {
				g.drawRect(x, y, width, height);
			} else {
				g.drawOval(x, y, width, height);
			}

			g.setColor(NODE_LABEL_COLOR);
			FontMetrics metrics = g.getFontMetrics();
			g.drawString(label, labelX - metrics.stringWidth(label)/2, labelY + metrics.getHeight()/4);
		}
	}

	/**
	 * This represents either an end or start pseudo state.  If a new state
	 * may be initialized from a call to makeInitial(), a start pseudo state will
	 * be present, and animation originate from this node when makeInitial() is called.
	 * 
	 * If a state may be killed by either a call to setEndState() or the end of execution,
	 * there will be a pseudo end state represented by this node.
	 * 
	 * Drawn as a circle.  PseudoNodes are unlabeled and do not refer to a specific state.
	 * The node name will reflect the type of node.  Either END-statename or START-statename
	 * where statename refers to the state which the pseudo state is associated with
	 */
	private class PseudoNode extends DiagramNode {

		public PseudoNode(String dotLine, List<State> visibleStates) {
			super(dotLine, visibleStates);
		}

		public void draw(Graphics g) {
			g.fillOval(x, y, width, height);
		}
	}
	
	
/**
 * This node is not actually "visible" on this level of hierarchy.  We keep
 * a marker for it and display it on the diagram though, because some transition
 * either originates or terminates at this node.
 * 
 * The node name will read either EXT-statename-TO or EXT-statename-FROM, where
 * statename is the state with which this state has an edge to.
 * 
 * Drawn with rounded corners.
 *
 */
	private class ExternalNode extends DiagramNode {

		private int labelX, labelY;
		private String label;

		public ExternalNode(String dotLine, List<State> visibleStates) {
			super(dotLine, visibleStates);

			String[] nodeConfig = dotLine.split(" +");

			int firstDash = name.indexOf('-');
			int lastDash = name.lastIndexOf('-');
			String typeName = name.substring(firstDash + 1, lastDash);

			state = EdgeUtility.findExternalState(visibleStates.get(0).getMasterState(), typeName);

			label = nodeConfig[6].replace("\"", "");
			labelX = x + width/2; //set label to the center for now
			labelY = y + height/2;

		}

		public void draw(Graphics g) {

			g.setColor(EXTERNAL_NODE_FILL);
			g.fillRoundRect(x, y, width, height, width/10, width/10);
			g.setColor(NODE_OUTLINE_COLOR);
			g.drawRoundRect(x, y, width, height, width/10, width/10);

			g.setColor(NODE_LABEL_COLOR);
			Font f = g.getFont();
			g.setFont(new Font("Times New Roman", Font.ITALIC, f.getSize()));
			FontMetrics metrics = g.getFontMetrics();
			g.drawString(label, labelX - metrics.stringWidth(label)/2, labelY + metrics.getHeight()/4);
			g.setFont(f);
		}
	}


	/**
	 * 
	 * The DiagramEdge class represents an edge connecting two nodes,
	 * a head and a tail, upon the diagram.  It can be query'd to find
	 * Points at specified fractions of its length.  It is drawn with
	 * and arrow at the head (adjustable in VisualDiagram constants)
	 *
	 */


	private class DiagramEdge {

		private int[] x, y;
		private int rightArrowX, leftArrowX, rightArrowY, leftArrowY;

		private int labelX, labelY;
		private String label;

		private double edgeLength;
		private double[] distances;

		private DiagramNode head, tail;



		/**
		 * @param line should be in this format:
		 * 		
		 * 		edge tail head n x1 y1 x2 y2 . . . xn yn [ label lx ly ] style color
		 */

		DiagramEdge(String dotLine, List<State> visibleStates) {

			String[] edgeConfig = dotLine.split(" +");

			int numPoints = Integer.parseInt(edgeConfig[3]);

			x = new int[numPoints];
			y = new int[numPoints];
			distances = new double[numPoints]; 

			for(int i = 0; i < numPoints; i++) {

				int xArgPos = 4 + 2*i;

				x[i] = (int)(Double.parseDouble(edgeConfig[xArgPos]) * DPI * scaleFactor);
				y[i] = (int)(Double.parseDouble(edgeConfig[xArgPos + 1]) * DPI * scaleFactor);

				if(i > 0) {

					//distance formula
					distances[i-1] = Math.sqrt(Math.pow(x[i] - x[i-1], 2) + Math.pow(y[i] - y[i-1], 2));
					edgeLength += distances[i-1];

				}

			}

			String headName = edgeConfig[2].replaceAll("\"", "");
			String tailName = edgeConfig[1].replaceAll("\"", "");

			for(DiagramNode node : nodes) {
				if(node.getName().equals(tailName))
					tail = node;
				if(node.getName().equals(headName))
					head = node;
			}

			int labelPosition = 4 + 2*numPoints;

			//if there is a label...
			if(edgeConfig.length - labelPosition > 3  ) {

				label = edgeConfig[labelPosition];
				label = label.replaceAll("\"", ""); //strip the "'s

				labelX = (int)(Double.parseDouble(edgeConfig[labelPosition + 1]) * DPI * scaleFactor ) + LABEL_SHIFT;
				labelY = (int)(Double.parseDouble(edgeConfig[labelPosition + 2]) * DPI * scaleFactor );


			}

			setArrow();


		}



		private void setArrow() {

			Point p1 = new Point(x[x.length - 2], y[y.length - 2]);
			Point p0 = new Point(x[x.length - 1], y[y.length - 1]);

			int deltaX = p1.x - p0.x;
			int deltaY = p1.y - p0.y;
			double frac = 0.4;
			double scale =  ARROW_LENGTH / p0.distance(p1);


			rightArrowX = p0.x + (int)(((1-frac)*deltaX + frac*deltaY)*scale);
			rightArrowY	=  p0.y + (int)(((1-frac)*deltaY - frac*deltaX)*scale);

			leftArrowX = p0.x + (int)(((1-frac)*deltaX - frac*deltaY)*scale);
			leftArrowY = p0.y + (int)(((1-frac)*deltaY + frac*deltaX)*scale);

		}



		Point getPosition(double progress) {

			Point p0 = null, p1 = null;

			Point startingPoint = tail.getCenter();
			Point endingPoint = head.getCenter();

			double firstTransit = startingPoint.distance(new Point(x[0], y[0]));
			double lastTransit = endingPoint.distance(new Point(x[x.length-1], y[y.length-1]));

			double desiredDistance = (firstTransit + edgeLength + lastTransit)* progress;
			double holder = 0, breakingIncrement = 0;

			if(desiredDistance <= firstTransit) {

				p0 = startingPoint;
				p1 = new Point(x[0], y[0]);
				holder = firstTransit;
				breakingIncrement = firstTransit;

			} else if (desiredDistance < edgeLength + firstTransit) {

				holder += firstTransit;

				for(int i = 0; i < distances.length; i++) {

					holder += distances[i];

					if(holder >= desiredDistance) {
						p0 = new Point(x[i], y[i]);
						p1 = new Point(x[i+1], y[i+1]);
						breakingIncrement = distances[i];
						break;
					}
				}

			} else {

				p0 = new Point(x[x.length-1], y[y.length-1]);
				p1 = endingPoint;
				breakingIncrement = lastTransit;
				holder = firstTransit + edgeLength + lastTransit;

			}


			double frac = (breakingIncrement - (holder - desiredDistance)) / breakingIncrement;


			return new Point(p0.x + (int)((p1.x - p0.x) * frac), 
					p0.y + (int)((p1.y - p0.y) * frac));

		}

		public int hashCode() {
			int hash = 2*rightArrowX + 3*leftArrowY;
			hash += head.hashCode();

			return hash;
		}

		DiagramNode getHead() { 
			return head; 
		}

		DiagramNode getTail() {
			return tail;
		}



		void draw(Graphics g) {

			g.setColor(EDGE_COLOR);
			g.drawPolyline(x, y, x.length);

			if(FILL_ARROWHEAD) {

				int triX[] = {rightArrowX, leftArrowX, x[x.length-1]};
				int triY[] = {rightArrowY, leftArrowY, y[y.length-1]};
				g.fillPolygon(triX, triY, 3);

			} else {

				g.drawLine(rightArrowX, rightArrowY, x[x.length-1], y[y.length-1]);
				g.drawLine(leftArrowX, leftArrowY, x[x.length-1], y[y.length-1]);

			}

			if(label != null) {
				g.setColor(EDGE_LABEL_COLOR);
				FontMetrics metrics = g.getFontMetrics();
				g.drawString(label, labelX - metrics.stringWidth(label)/2, labelY + metrics.getHeight()/4);
			}
		}

	}


	/**
	 * This is the vehicle used for animation along the graph.  The position is changed
	 * by use of the animateTo() method.  Which will animate to the given node. Travels
	 * along DiagramEdges, from DiagramNode to DiagramNode.  The locations of all 3 of
	 * these parts are used to determine animation.
	 * 
	 */

	private class Pathfinder {

		private boolean inTransit;
		private DiagramNode currentNode;
		private DiagramEdge currentEdge;

		double progressNum;
		double progressDenom;
		Timer transitTimer;

		private int x,y;

		public Pathfinder(DiagramNode startNode) {
			currentNode = startNode;
			setPos(startNode.getCenter());
		}


		public void clear() {

			if(transitTimer != null){
				transitTimer.stop();
			}

			currentNode = null;
			inTransit = false;
		}


		public Timer animateTo(DiagramNode nextNode, int animationSpeed) {

			if(currentNode == nextNode && !isEdgeToSelf(currentNode))
				throw new RuntimeException(nextNode + "has no edge to itself.");

			if (inTransit){

				transitTimer.stop();
				Thread.yield();  //Let transitTimer finish its work.
				setPos(nextNode.getCenter());

			}

			currentEdge = findEdge(currentNode, nextNode);
			progressNum = 1;
			progressDenom = (((double)animationSpeed)* FPS) / 1000 ;

			transitTimer = new Timer((int)progressDenom, new ActionListener() {

				public void actionPerformed(ActionEvent e) { 

					if(currentEdge == null)
						return;

					Point p = currentEdge.getPosition(progressNum / progressDenom);

					setPos(p);

					drawingPanel.repaint();

					if(progressNum++ >= progressDenom){

						inTransit = false;
						currentEdge = null;
						synchronized(transitTimer) {
							transitTimer.notify();
							transitTimer.stop();
						}
					}
				}
			});

			inTransit = true;
			transitTimer.start();
			currentNode = nextNode;
			return transitTimer;

		}

		public int hashCode() {
			return currentNode.hashCode() + 3*x - y;
		}

		private void setPos(Point p) {
			x = p.x;
			y = p.y;
		}

		public void draw(Graphics g) {

			g.setColor(PATHFINDER_COLOR);

			if(g instanceof Graphics2D) {
				((Graphics2D)g).setStroke(PATHFINDER_STROKE);
			}

			int width = (int)(PATHFINDER_WIDTH * scaleFactor);
			int height = (int)(PATHFINDER_HEIGHT * scaleFactor);

			g.drawLine(x - width/2, y - height/2,
					x + width, y + height/2);
			g.drawLine(x - width/2, y + height/2,
					x + width/2, y - height/2);

			if(g instanceof Graphics2D) {
				((Graphics2D)g).setStroke(new BasicStroke());
			}

		}
	}
}

