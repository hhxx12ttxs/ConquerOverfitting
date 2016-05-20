/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 * TreeVisualizer.java Copyright (C) 1999 University of Waikato, Hamilton, New
 * Zealand
 */

package weka.gui.treevisualizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import weka.core.Instances;
import weka.core.Utils;
import weka.gui.visualize.PrintablePanel;
import weka.gui.visualize.VisualizePanel;
import weka.gui.visualize.VisualizeUtils;

/**
 * Class for displaying a Node structure in Swing.
 * <p>
 * 
 * To work this class simply create an instance of it.
 * <p>
 * 
 * Assign it to a window or other such object.
 * <p>
 * 
 * Resize it to the desired size.
 * <p>
 * 
 * 
 * When using the Displayer hold the left mouse button to drag the tree around.
 * <p>
 * 
 * Click the left mouse button with ctrl to shrink the size of the tree by half.
 * <p>
 * 
 * Click and drag with the left mouse button and shift to draw a box, when the left mouse button is released the contents of the box will be magnified to fill the screen.
 * <p>
 * <p>
 * 
 * Click the right mouse button to bring up a menu.
 * <p>
 * Most options are self explanatory.
 * <p>
 * 
 * Select Auto Scale to set the tree to it's optimal display size.
 * 
 * @author Malcolm Ware (mfw4@cs.waikato.ac.nz)
 * @version $Revision: 7386 $
 */
public class TreeVisualizer extends PrintablePanel implements MouseMotionListener, MouseListener, ActionListener,
		ItemListener{

	/**
	 * Internal Class for containing display information about an Edge.
	 */
	protected class EdgeInfo{
		// this class contains a pointer to the edge along with all the other
		// extra info about the edge

		/** The child subscript (for a Node). */
		int		m_child;

		/** The Edge itself. */
		Edge	m_edge;

		/** The height of the text. */
		int		m_height;

		/** The parent subscript (for a Node). */
		int		m_parent;	// array indexs for its two connections

		/** The distance from the center of the text to either side. */
		int		m_side;	// these are used to describe the dimensions of the
		// text

		/** The distance from the center of the text to top or bottom. */
		int		m_tb;		// tb stands for top , bottom, this is simply the
		// distance from the middle to top bottom

		/** The width of the text. */
		int		m_width;
	}

	/**
	 * Internal Class for containing display information about a Node.
	 */
	protected class NodeInfo{
		// this class contains a pointer to the node itself along with extra
		// information
		// about the node used by the Displayer class

		/** The x pos of the node on screen. */
		int			m_center;			// these coords will probably change
										// each
										// refresh

		/**
		 * True if the node is at the start (left) of a new level (not sibling
		 * group).
		 */
		boolean		m_change;			// this is quickly used to identify
										// whether
										// the node
		// has chenged height from the
		// previous one to help speed up the calculation of what row it lies in

		// and are the positioning coords
		// which the rest of the offsets use

		/** The height of the node. */
		int			m_height;

		/** The Node itself. */
		public Node	m_node;

		/** The subscript number of the Nodes parent. */
		int			m_parent;			// this is the index of the nodes parent
										// edge in an array

		/** The rough position of the node relative to the screen. */
		int			m_quad;			// what of nine quadrants is it in

		/** The offset to get to the left or right of the node. */
		int			m_side;			// these are the screen offset for the
										// dimensions of

		/** The y pos of the node on screen. */
		int			m_top	= 32000;	// the main node coords calculated out

		/*
		 * 12 10 9 20 18 17 //18 being the screen 36 34 33 //this arrangement
		 * uses 6 bits, each bit represents a row or column
		 */

		// the node relative to the nodes
		// internal top and center values (after they have been converted to
		// screen coords
		/** The width of the node. */
		int			m_width;
	}

	/** the props file. */
	public final static String	PROPERTIES_FILE		= "weka/gui/treevisualizer/TreeVisualizer.props";

	/** for serialization */
	private static final long	serialVersionUID	= -8668637962504080749L;

	/**
	 * Main method for testing this class.
	 * 
	 * @param args
	 *            first argument should be the name of a file that contains a
	 *            tree discription in dot format.
	 */
	public static void main(String[] args) {
		try {
			weka.core.logging.Logger.log(weka.core.logging.Logger.Level.INFO,
					Messages.getInstance().getString("TreeVisualizer_Main_Logger_Text"));
			// put in the random data generator right here
			// this call with import java.lang gives me between 0 and 1
			// Math.random
			TreeBuild builder = new TreeBuild();
			Node top = null;
			NodePlace arrange = new PlaceNode2();
			// top = builder.create(new
			// StringReader("digraph atree { top [label=\"the top\"] a [label=\"the first node\"] b [label=\"the second nodes\"] c [label=\"comes off of first\"] top->a top->b b->c }"));
			top = builder.create(new FileReader(args[0]));

			int num = Node.getCount(top, 0);
			// System.out.println("counter counted " + num + " nodes");
			// System.out.println("there are " + num + " nodes");
			TreeVisualizer a = new TreeVisualizer(null, top, arrange);
			a.setSize(800, 600);
			// a.setTree(top);
			JFrame f;
			f = new JFrame();
			// a.addMouseMotionListener(a);
			// a.addMouseListener(a);
			// f.add(a);
			Container contentPane = f.getContentPane();
			contentPane.add(a);
			f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			f.setSize(800, 600);
			f.setVisible(true);
			// f.
			// find_prop(top);
			// a.setTree(top,arrange);//,(num + 1000), num / 2 + 1000);
		} catch (IOException e) {
			// ignored
		}
	}

	protected Color						m_aboveColor		= TreeConstants.ABOVE_COLOR;

	/** An option on the win menu. */
	private final JMenuItem				m_accept;

	/**
	 * An add children to Node choice, This is only available if the tree
	 * display has a treedisplay listerner added to it.
	 */
	private JMenuItem					m_addChildren;

	/** An option on the win_menu */
	protected JMenuItem					m_autoScale;

	/** the background color. */
	protected Color						m_BackgroundColor	= Color.white;

	protected Color						m_belowColor		= TreeConstants.BELOW_COLOR;

	private JRadioButton				m_caseSen;

	/** Use this to have J48 classify this node. */
	private JMenuItem					m_classifyChild;

	/**
	 * A variable used to determine for the clicked method if any other mouse
	 * state has already taken place.
	 */
	protected boolean					m_clickAvailable;

	/** The font used to display the tree. */
	protected Font						m_currentFont;

	/**
	 * An array with the Edges sorted into it and display information about the
	 * Edges.
	 */
	protected EdgeInfo[]				m_edges;

	/** An option on the win_menu */
	protected JMenuItem					m_fitToScreen;

	/**
	 * The subscript for the currently selected node (this is an internal thing,
	 * so the user is unaware of this).
	 */
	protected int						m_focusNode;

	/** the font color. */
	protected Color						m_FontColor			= null;

	/** The size information for the current font. */
	protected FontMetrics				m_fontSize;

	/** A timer to keep the frame rate constant. */
	protected Timer						m_frameLimiter;

	/**
	 * The Node the user is currently focused on , this is similar to focus node
	 * except that it is used by other classes rather than this one.
	 */
	protected int						m_highlightNode;

	/** the line color. */
	protected Color						m_LineColor			= null;

	/* A pointer to this tree's classifier if a classifier is using it. */
	// private UserClassifier classer;
	private final TreeDisplayListener	m_listener;

	/** Describes the action the user is performing. */
	protected int						m_mouseState;

	/** A variable used to tag the most current point of a user action. */
	protected Dimension					m_newMousePos;

	/** the node color. */
	protected Color						m_NodeColor			= null;

	/** A right or middle click popup menu for nodes. */
	private final JPopupMenu			m_nodeMenu;

	/**
	 * An array with the Nodes sorted into it and display information about the
	 * Nodes.
	 */
	protected NodeInfo[]				m_nodes;

	/** The number of levels in the tree. */
	protected int						m_numLevels;

	/** The number of Nodes in the tree. */
	protected int						m_numNodes;

	/** A variable used to remember the desired view pos. */
	protected Dimension					m_nViewPos;

	/** A variable used to remember the desired tree size. */
	protected Dimension					m_nViewSize;

	/** A variable used to tag the start pos of a user action. */
	protected Dimension					m_oldMousePos;

	/** The placement algorithm for the Node structure. */
	protected NodePlace					m_placer;

	/** Similar to add children but now it removes children. */
	private JMenuItem					m_remChildren;

	/** The number of frames left to calculate. */
	protected int						m_scaling;

	private JTextField					m_searchString;

	private JDialog						m_searchWin;

	/** A sub group on the win_menu */
	private final JMenu					m_selectFont;

	/** A grouping for the font choices */
	private final ButtonGroup			m_selectFontGroup;

	/** Use this to dump the instances from this node to the vis panel. */
	private JMenuItem					m_sendInstances;

	/** whether to show the border or not. */
	protected boolean					m_ShowBorder		= true;

	/** A font choice. */
	private final JRadioButtonMenuItem	m_size1;

	/** A font choice. */
	private final JRadioButtonMenuItem	m_size10;

	/** A font choice. */
	private final JRadioButtonMenuItem	m_size12;

	/** A font choice. */
	private final JRadioButtonMenuItem	m_size14;

	/** A font choice. */
	private final JRadioButtonMenuItem	m_size16;

	/** A font choice. */
	private final JRadioButtonMenuItem	m_size18;

	/** A font choice. */
	private final JRadioButtonMenuItem	m_size2;
	/** A font choice. */
	private final JRadioButtonMenuItem	m_size20;
	/** A font choice. */
	private final JRadioButtonMenuItem	m_size22;

	/** A font choice. */
	private final JRadioButtonMenuItem	m_size24;

	/** A font choice. */
	private final JRadioButtonMenuItem	m_size4;

	/** A font choice. */
	private final JRadioButtonMenuItem	m_size6;

	/** A font choice. */
	private final JRadioButtonMenuItem	m_size8;
	/** An option on the win_menu */
	protected JMenuItem					m_topN;

	/** The top Node. */
	protected Node						m_topNode;

	/** The postion of the view relative to the tree. */
	protected Dimension					m_viewPos;

	/** The size of the tree in pixels. */
	protected Dimension					m_viewSize;

	/** A visualize choice for the node, may not be available. */
	private final JMenuItem				m_visualise;

	// /////////////////

	// this is the event fireing stuff

	/** A right (or middle) click popup menu. */
	protected JPopupMenu				m_winMenu;

	/** the color of the zoombox. */
	protected Color						m_ZoomBoxColor		= null;

	/** the XOR color of the zoombox. */
	protected Color						m_ZoomBoxXORColor	= null;

	/**
	 * Constructs Displayer with the specified Node as the top of the tree, and
	 * uses the NodePlacer to place the Nodes.
	 * 
	 * @param tdl
	 *            listener.
	 * @param n
	 *            the top Node of the tree to be displayed.
	 * @param p
	 *            the algorithm to be used to position the nodes.
	 */
	public TreeVisualizer(TreeDisplayListener tdl, Node n, NodePlace p) {
		super();

		this.initialize();

		// if the size needs to be automatically alocated I will do it here
		if (this.m_ShowBorder) {
			this.setBorder(BorderFactory.createTitledBorder(Messages.getInstance().getString(
					"TreeVisualizer_BorderFactoryCreateTitledBorder_Text_Second")));
		}
		this.m_listener = tdl;
		this.m_topNode = n;
		this.m_placer = p;
		this.m_placer.place(this.m_topNode);
		this.m_viewPos = new Dimension(0, 0); // will be adjusted
		this.m_viewSize = new Dimension(800, 600); // I allocate this now so
													// that
		// the tree will be visible
		// when the panel is enlarged

		this.m_nViewPos = new Dimension(0, 0);
		this.m_nViewSize = new Dimension(800, 600);

		this.m_scaling = 0;

		this.m_numNodes = Node.getCount(this.m_topNode, 0); // note the second
		// argument
		// must be a zero, this is a
		// recursive function

		this.m_numLevels = Node.getHeight(this.m_topNode, 0);

		this.m_nodes = new NodeInfo[this.m_numNodes];
		this.m_edges = new EdgeInfo[this.m_numNodes - 1];

		this.arrayFill(this.m_topNode, this.m_nodes, this.m_edges);

		this.changeFontSize(12);

		this.m_mouseState = 0;
		this.m_oldMousePos = new Dimension(0, 0);
		this.m_newMousePos = new Dimension(0, 0);
		this.m_frameLimiter = new Timer(120, this);

		this.m_winMenu = new JPopupMenu();
		Messages.getInstance();
		this.m_topN = new JMenuItem(Messages.getString("TreeVisualizer_TopN_JMenuItem_Text_Second")); // note
																										// to
																										// change
		// language change this line
		this.m_topN.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_TopN_JMenuItem_SetActionCommand_Text_Second")); // but
		Messages.getInstance();
		// not
		// this
		// one, same for all menu items
		this.m_fitToScreen = new JMenuItem(Messages.getString("TreeVisualizer_FitToScreen_JMenuItem_Text_Second"));
		this.m_fitToScreen.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_FitToScreen_JMenuItem_SetActionCommand_Text_Second"));
		Messages.getInstance();
		// unhide = new JMenuItem("Unhide all Nodes");
		this.m_selectFont = new JMenu(Messages.getString("TreeVisualizer_SelectFont_JMenu_Text_Second"));
		this.m_selectFont.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_SelectFont_JMenu_SetActionCommand_Text_Second"));
		Messages.getInstance();
		this.m_autoScale = new JMenuItem(Messages.getString("TreeVisualizer_AutoScale_JMenuItem_Text_Second"));
		this.m_autoScale.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_AutoScale_JMenuItem_SetActionCommand_Text_Second"));
		this.m_selectFontGroup = new ButtonGroup();

		Messages.getInstance();
		this.m_accept = new JMenuItem(Messages.getString("TreeVisualizer_Accept_JMenuItem_Text_Second"));
		this.m_accept.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Accept_JMenuItem_SetActionCommand_Text_Second"));

		this.m_winMenu.add(this.m_topN);
		this.m_winMenu.addSeparator();
		this.m_winMenu.add(this.m_fitToScreen);
		this.m_winMenu.add(this.m_autoScale);
		this.m_winMenu.addSeparator();
		// m_winMenu.add(unhide);
		this.m_winMenu.addSeparator();
		this.m_winMenu.add(this.m_selectFont);
		this.m_winMenu.addSeparator();

		if (this.m_listener != null) {
			this.m_winMenu.add(this.m_accept);
		}

		this.m_topN.addActionListener(this);
		this.m_fitToScreen.addActionListener(this);
		// unhide.addActionListener(this);
		this.m_autoScale.addActionListener(this);
		this.m_accept.addActionListener(this);

		Messages.getInstance();
		this.m_size24 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size24_Text_Second"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size22 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size22_Text_Second"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size20 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size20_Text_Second"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size18 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size18_Text_Second"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size16 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size16_Text_Second"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size14 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size14_Text_Second"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size12 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size12_Text_Second"), true);// ,select_font_group);
		Messages.getInstance();
		this.m_size10 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size10_Text_Second"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size8 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size8_Text_Second"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size6 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size6_Text_Second"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size4 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size4_Text_Second"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size2 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size2_Text_Second"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size1 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size1_Text_Second"), false);// ,select_font_group);

		this.m_size24.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size24_Text_Second"));// ,select_font_group);
		this.m_size22.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size22_Text_Second"));// ,select_font_group);
		this.m_size20.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size20_Text_Second"));// ,select_font_group);
		this.m_size18.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size18_Text_Second"));// ,select_font_group);
		this.m_size16.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size16_Text_Second"));// ,select_font_group);
		this.m_size14.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size14_Text_Second"));// ,select_font_group);
		this.m_size12.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size12_Text_Second"));// ,select_font_group);
		this.m_size10.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size10_Text_Second"));// ,select_font_group);
		this.m_size8.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size8_Text_Second"));// ,select_font_group);
		this.m_size6.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size6_Text_Second"));// ,select_font_group);
		this.m_size4.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size4_Text_Second"));// ,select_font_group);
		this.m_size2.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size2_Text_Second"));// ,select_font_group);
		this.m_size1.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size1_Text_Second"));// ,select_font_group);

		this.m_selectFontGroup.add(this.m_size24);
		this.m_selectFontGroup.add(this.m_size22);
		this.m_selectFontGroup.add(this.m_size20);
		this.m_selectFontGroup.add(this.m_size18);
		this.m_selectFontGroup.add(this.m_size16);
		this.m_selectFontGroup.add(this.m_size14);
		this.m_selectFontGroup.add(this.m_size12);
		this.m_selectFontGroup.add(this.m_size10);
		this.m_selectFontGroup.add(this.m_size8);
		this.m_selectFontGroup.add(this.m_size6);
		this.m_selectFontGroup.add(this.m_size4);
		this.m_selectFontGroup.add(this.m_size2);
		this.m_selectFontGroup.add(this.m_size1);

		this.m_selectFont.add(this.m_size24);
		this.m_selectFont.add(this.m_size22);
		this.m_selectFont.add(this.m_size20);
		this.m_selectFont.add(this.m_size18);
		this.m_selectFont.add(this.m_size16);
		this.m_selectFont.add(this.m_size14);
		this.m_selectFont.add(this.m_size12);
		this.m_selectFont.add(this.m_size10);
		this.m_selectFont.add(this.m_size8);
		this.m_selectFont.add(this.m_size6);
		this.m_selectFont.add(this.m_size4);
		this.m_selectFont.add(this.m_size2);
		this.m_selectFont.add(this.m_size1);

		this.m_size24.addItemListener(this);
		this.m_size22.addItemListener(this);
		this.m_size20.addItemListener(this);
		this.m_size18.addItemListener(this);
		this.m_size16.addItemListener(this);
		this.m_size14.addItemListener(this);
		this.m_size12.addItemListener(this);
		this.m_size10.addItemListener(this);
		this.m_size8.addItemListener(this);
		this.m_size6.addItemListener(this);
		this.m_size4.addItemListener(this);
		this.m_size2.addItemListener(this);
		this.m_size1.addItemListener(this);

		/*
		 * search_string = new JTextField(22); search_win = new JDialog();
		 * case_sen = new JRadioButton("Case Sensitive");
		 * search_win.getContentPane().setLayout(null); search_win.setSize(300,
		 * 200); search_win.getContentPane().add(search_string);
		 * search_win.getContentPane().add(case_sen);
		 * search_string.setLocation(50, 70); case_sen.setLocation(50, 120);
		 * case_sen.setSize(100, 24); search_string.setSize(100, 24);
		 * //search_string.setVisible(true); //case_sen.setVisible(true);
		 * search_win.setVisible(true);
		 */

		this.m_nodeMenu = new JPopupMenu();
		Messages.getInstance();
		/* A visualize choice for the node, may not be available. */
		this.m_visualise = new JMenuItem(Messages.getString("TreeVisualizer_Visualise_JMenuItem_Text_Second"));
		this.m_visualise.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Visualise_JMenuItem_SetActionCommand_Text_Second"));
		this.m_visualise.addActionListener(this);
		this.m_nodeMenu.add(this.m_visualise);

		if (this.m_listener != null) {
			Messages.getInstance();
			this.m_remChildren = new JMenuItem(Messages.getString("TreeVisualizer_RemChildren_JMenuItem_Text_Second"));
			this.m_remChildren.setActionCommand(Messages.getInstance().getString(
					"TreeVisualizer_RemChildren_JMenuItem_SetActionCommand_Text_Second"));
			this.m_remChildren.addActionListener(this);
			this.m_nodeMenu.add(this.m_remChildren);

			Messages.getInstance();
			this.m_classifyChild = new JMenuItem(
					Messages.getString("TreeVisualizer_ClassifyChild_JMenuItem_Text_Second"));
			this.m_classifyChild.setActionCommand(Messages.getInstance().getString(
					"TreeVisualizer_ClassifyChild_JMenuItem_SetActionCommand_Text_Second"));
			this.m_classifyChild.addActionListener(this);
			this.m_nodeMenu.add(this.m_classifyChild);

			Messages.getInstance();
			this.m_sendInstances = new JMenuItem(Messages.getString("TreeVisualizer_SendInstances_JMenuItem_Text"));
			this.m_sendInstances.setActionCommand(Messages.getInstance().getString(
					"TreeVisualizer_SendInstances_JMenuItem_SetActionCommand_Text"));
			this.m_sendInstances.addActionListener(this);
			this.m_nodeMenu.add(this.m_sendInstances);

		}

		this.m_focusNode = -1;
		this.m_highlightNode = -1;

		this.addMouseMotionListener(this);
		this.addMouseListener(this);

		// repaint();

		// frame_limiter.setInitialDelay();
		this.m_frameLimiter.setRepeats(false);
		this.m_frameLimiter.start();
	}

	/**
	 * Constructs Displayer to display a tree provided in a dot format. Uses the
	 * NodePlacer to place the Nodes.
	 * 
	 * @param tdl
	 *            listener
	 * @param dot
	 *            string containing the dot representation of the tree to
	 *            display
	 * @param p
	 *            the algorithm to be used to position the nodes.
	 */
	public TreeVisualizer(TreeDisplayListener tdl, String dot, NodePlace p) {
		super();

		this.initialize();

		// generate the node structure in here
		if (this.m_ShowBorder) {
			this.setBorder(BorderFactory.createTitledBorder(Messages.getInstance().getString(
					"TreeVisualizer_BorderFactoryCreateTitledBorder_Text_First")));
		}
		this.m_listener = tdl;

		TreeBuild builder = new TreeBuild();

		Node n = null;
		NodePlace arrange = new PlaceNode2();
		n = builder.create(new StringReader(dot));
		// System.out.println(n.getCount(n, 0));
		// if the size needs to be automatically alocated I will do it here
		this.m_highlightNode = 5;
		this.m_topNode = n;
		this.m_placer = p;
		this.m_placer.place(this.m_topNode);
		this.m_viewPos = new Dimension(0, 0); // will be adjusted
		this.m_viewSize = new Dimension(800, 600); // I allocate this now so
													// that
		// the tree will be visible
		// when the panel is enlarged

		this.m_nViewPos = new Dimension(0, 0);
		this.m_nViewSize = new Dimension(800, 600);

		this.m_scaling = 0;

		this.m_numNodes = Node.getCount(this.m_topNode, 0); // note the second
		// argument must be a zero, this is a
		// recursive function

		this.m_numLevels = Node.getHeight(this.m_topNode, 0);

		this.m_nodes = new NodeInfo[this.m_numNodes];
		this.m_edges = new EdgeInfo[this.m_numNodes - 1];

		this.arrayFill(this.m_topNode, this.m_nodes, this.m_edges);

		this.changeFontSize(12);

		this.m_mouseState = 0;
		this.m_oldMousePos = new Dimension(0, 0);
		this.m_newMousePos = new Dimension(0, 0);
		this.m_frameLimiter = new Timer(120, this);

		this.m_winMenu = new JPopupMenu();
		Messages.getInstance();
		this.m_topN = new JMenuItem(Messages.getString("TreeVisualizer_TopN_JMenuItem_Text_First")); // note
																										// to
																										// change
		// language change this line
		this.m_topN.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_TopN_JMenuItem_SetActionCommand_Text_First")); // but
		Messages.getInstance();
		// not
		// this
		// one,
		// same for all menu items
		this.m_fitToScreen = new JMenuItem(Messages.getString("TreeVisualizer_FitToScreen_JMenuItem_Text_First"));
		this.m_fitToScreen.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_FitToScreen_JMenuItem_SetActionCommand_Text_First"));
		Messages.getInstance();
		// unhide = new JMenuItem("Unhide all Nodes");
		this.m_selectFont = new JMenu(Messages.getString("TreeVisualizer_SelectFont_JMenu_Text_First"));
		this.m_selectFont.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_SelectFont_JMenu_SetActionCommand_Text_First"));
		Messages.getInstance();
		this.m_autoScale = new JMenuItem(Messages.getString("TreeVisualizer_AutoScale_JMenuItem_Text_First"));
		this.m_autoScale.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_AutoScale_JMenuItem_SetActionCommand_Text_First"));
		this.m_selectFontGroup = new ButtonGroup();

		Messages.getInstance();
		this.m_accept = new JMenuItem(Messages.getString("TreeVisualizer_Accept_JMenuItem_Text_First"));
		this.m_accept.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Accept_JMenuItem_SetActionCommand_Text_First"));

		this.m_winMenu.add(this.m_topN);
		this.m_winMenu.addSeparator();
		this.m_winMenu.add(this.m_fitToScreen);
		this.m_winMenu.add(this.m_autoScale);
		// m_winMenu.addSeparator();
		// m_winMenu.add(unhide);
		this.m_winMenu.addSeparator();
		this.m_winMenu.add(this.m_selectFont);

		if (this.m_listener != null) {
			this.m_winMenu.addSeparator();
			this.m_winMenu.add(this.m_accept);
		}

		this.m_topN.addActionListener(this);
		this.m_fitToScreen.addActionListener(this);
		// unhide.addActionListener(this);
		this.m_autoScale.addActionListener(this);
		this.m_accept.addActionListener(this);

		Messages.getInstance();
		this.m_size24 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size24_JRadioButtonMenuItem_Size24_Text_First"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size22 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size22_JRadioButtonMenuItem_Size22_Text_First"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size20 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size20_JRadioButtonMenuItem_Size20_Text_First"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size18 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size18_JRadioButtonMenuItem_Size18_Text_First"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size16 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size16_JRadioButtonMenuItem_Size16_Text_First"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size14 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size14_JRadioButtonMenuItem_Size14_Text_First"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size12 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size12_JRadioButtonMenuItem_Size12_Text_First"), true);// ,select_font_group);
		Messages.getInstance();
		this.m_size10 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size10_JRadioButtonMenuItem_Size10_Text_First"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size8 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size8_JRadioButtonMenuItem_Size8_Text_First"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size6 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size6_JRadioButtonMenuItem_Size6_Text_First"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size4 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size4_JRadioButtonMenuItem_Size4_Text_First"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size2 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size2_JRadioButtonMenuItem_Size2_Text_First"), false);// ,select_font_group);
		Messages.getInstance();
		this.m_size1 = new JRadioButtonMenuItem(
				Messages.getString("TreeVisualizer_Size1_JRadioButtonMenuItem_Size1_Text_First"), false);// ,select_font_group);

		this.m_size24.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size24_JRadioButtonMenuItem_SetActionCommand_Size24_Text_First"));// ,select_font_group);
		this.m_size22.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size22_JRadioButtonMenuItem_SetActionCommand_Size22_Text_First"));// ,select_font_group);
		this.m_size20.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size20_JRadioButtonMenuItem_SetActionCommand_Size20_Text_First"));// ,select_font_group);
		this.m_size18.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size18_JRadioButtonMenuItem_SetActionCommand_Size18_Text_First"));// ,select_font_group);
		this.m_size16.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size16_JRadioButtonMenuItem_SetActionCommand_Size16_Text_First"));// ,select_font_group);
		this.m_size14.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size14_JRadioButtonMenuItem_SetActionCommand_Size14_Text_First"));// ,select_font_group);
		this.m_size12.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size12_JRadioButtonMenuItem_SetActionCommand_Size12_Text_First"));// ,select_font_group);
		this.m_size10.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size10_JRadioButtonMenuItem_SetActionCommand_Size10_Text_First"));// ,select_font_group);
		this.m_size8.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size8_JRadioButtonMenuItem_SetActionCommand_Size8_Text_First"));// ,select_font_group);
		this.m_size6.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size6_JRadioButtonMenuItem_SetActionCommand_Size6_Text_First"));// ,select_font_group);
		this.m_size4.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size4_JRadioButtonMenuItem_SetActionCommand_Size4_Text_First"));// ,select_font_group);
		this.m_size2.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size2_JRadioButtonMenuItem_SetActionCommand_Size2_Text_First"));// ,select_font_group);
		this.m_size1.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Size1_JRadioButtonMenuItem_SetActionCommand_Size1_Text_First"));// ,select_font_group);

		this.m_selectFontGroup.add(this.m_size24);
		this.m_selectFontGroup.add(this.m_size22);
		this.m_selectFontGroup.add(this.m_size20);
		this.m_selectFontGroup.add(this.m_size18);
		this.m_selectFontGroup.add(this.m_size16);
		this.m_selectFontGroup.add(this.m_size14);
		this.m_selectFontGroup.add(this.m_size12);
		this.m_selectFontGroup.add(this.m_size10);
		this.m_selectFontGroup.add(this.m_size8);
		this.m_selectFontGroup.add(this.m_size6);
		this.m_selectFontGroup.add(this.m_size4);
		this.m_selectFontGroup.add(this.m_size2);
		this.m_selectFontGroup.add(this.m_size1);

		this.m_selectFont.add(this.m_size24);
		this.m_selectFont.add(this.m_size22);
		this.m_selectFont.add(this.m_size20);
		this.m_selectFont.add(this.m_size18);
		this.m_selectFont.add(this.m_size16);
		this.m_selectFont.add(this.m_size14);
		this.m_selectFont.add(this.m_size12);
		this.m_selectFont.add(this.m_size10);
		this.m_selectFont.add(this.m_size8);
		this.m_selectFont.add(this.m_size6);
		this.m_selectFont.add(this.m_size4);
		this.m_selectFont.add(this.m_size2);
		this.m_selectFont.add(this.m_size1);

		this.m_size24.addItemListener(this);
		this.m_size22.addItemListener(this);
		this.m_size20.addItemListener(this);
		this.m_size18.addItemListener(this);
		this.m_size16.addItemListener(this);
		this.m_size14.addItemListener(this);
		this.m_size12.addItemListener(this);
		this.m_size10.addItemListener(this);
		this.m_size8.addItemListener(this);
		this.m_size6.addItemListener(this);
		this.m_size4.addItemListener(this);
		this.m_size2.addItemListener(this);
		this.m_size1.addItemListener(this);

		/*
		 * search_string = new JTextField(22); search_win = new JDialog();
		 * case_sen = new JRadioButton("Case Sensitive");
		 * search_win.getContentPane().setLayout(null); search_win.setSize(300,
		 * 200); search_win.getContentPane().add(search_string);
		 * search_win.getContentPane().add(case_sen);
		 * search_string.setLocation(50, 70); case_sen.setLocation(50, 120);
		 * case_sen.setSize(100, 24); search_string.setSize(100, 24);
		 * //search_string.setVisible(true); //case_sen.setVisible(true);
		 * //search_win.setVisible(true);
		 */

		this.m_nodeMenu = new JPopupMenu();
		Messages.getInstance();
		/* A visualize choice for the node, may not be available. */
		this.m_visualise = new JMenuItem(Messages.getString("TreeVisualizer_Visualise_JMenuItem_Text_First"));
		this.m_visualise.setActionCommand(Messages.getInstance().getString(
				"TreeVisualizer_Visualise_JMenuItem_SetActionCommand_Text_First"));
		this.m_visualise.addActionListener(this);
		this.m_nodeMenu.add(this.m_visualise);

		if (this.m_listener != null) {
			Messages.getInstance();
			this.m_remChildren = new JMenuItem(Messages.getString("TreeVisualizer_RemChildren_JMenuItem_Text_First"));
			this.m_remChildren.setActionCommand(Messages.getInstance().getString(
					"TreeVisualizer_RemChildren_JMenuItem_SetActionCommand_Text_First"));
			this.m_remChildren.addActionListener(this);
			this.m_nodeMenu.add(this.m_remChildren);

			Messages.getInstance();
			this.m_classifyChild = new JMenuItem(
					Messages.getString("TreeVisualizer_ClassifyChild_JMenuItem_Text_First"));
			this.m_classifyChild.setActionCommand(Messages.getInstance().getString(
					"TreeVisualizer_ClassifyChild_JMenuItem_SetActionCommand_Text_First"));
			this.m_classifyChild.addActionListener(this);
			this.m_nodeMenu.add(this.m_classifyChild);

			/*
			 * m_sendInstances = new JMenuItem("Add Instances To Viewer");
			 * m_sendInstances.setActionCommand("send_instances");
			 * m_sendInstances.addActionListener(this);
			 * m_nodeMenu.add(m_sendInstances);
			 */

		}

		this.m_focusNode = -1;
		this.m_highlightNode = -1;

		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		// repaint();
		// frame_limiter.setInitialDelay();
		this.m_frameLimiter.setRepeats(false);
		this.m_frameLimiter.start();
	}

	/**
	 * Performs the action associated with the ActionEvent.
	 * 
	 * @param e
	 *            the action event.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		// JMenuItem m = (JMenuItem)e.getSource();
		if (e.getActionCommand() == null) {
			if (this.m_scaling == 0) {
				this.repaint();
			} else {
				this.animateScaling(this.m_nViewPos, this.m_nViewSize, this.m_scaling);
			}
		} else if (e.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ActionPerformed_FitToScreen_Text"))) {

			Dimension np = new Dimension();
			Dimension ns = new Dimension();

			this.getScreenFit(np, ns);

			this.animateScaling(np, ns, 10);

		} else if (e.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ActionPerformed_CenterOnTopNode_Text"))) {
			this.centerTopNode();
		} else if (e.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_AutoScale_JMenuItem_SetActionCommand_Text_Second"))) {
			this.autoScale(); // this will figure the best scale value
			// keep the focus on the middle of the screen and call animate
		} else if (e.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ActionPerformed_VisualizeTheNode_Text"))) {
			// send the node data to the visualizer
			if (this.m_focusNode >= 0) {
				Instances inst;
				if ((inst = this.m_nodes[this.m_focusNode].m_node.getInstances()) != null) {
					VisualizePanel pan = new VisualizePanel();
					pan.setInstances(inst);
					JFrame nf = new JFrame();
					nf.setSize(400, 300);
					nf.getContentPane().add(pan);
					nf.setVisible(true);
				} else {
					JOptionPane.showMessageDialog(
							this,
							Messages.getInstance().getString(
									"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_First"),
							Messages.getInstance().getString(
									"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Second"),
							JOptionPane.WARNING_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(
						this,
						Messages.getInstance().getString(
								"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Third"),
						Messages.getInstance().getString(
								"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Fourth"),
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ActionPerformed_CreateChildNodes_Text"))) {
			if (this.m_focusNode >= 0) {
				if (this.m_listener != null) {
					// then send message to the listener
					this.m_listener.userCommand(new TreeDisplayEvent(TreeDisplayEvent.ADD_CHILDREN,
							this.m_nodes[this.m_focusNode].m_node.getRefer()));
				} else {
					JOptionPane.showMessageDialog(
							this,
							Messages.getInstance().getString(
									"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Sixth"),
							Messages.getInstance().getString(
									"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Seventh"),
							JOptionPane.WARNING_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(
						this,
						Messages.getInstance().getString(
								"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Eighth"),
						Messages.getInstance().getString(
								"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Eighth"),
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ActionPerformed_RemoveChildNodes_Text"))) {
			if (this.m_focusNode >= 0) {
				if (this.m_listener != null) {
					// then send message to the listener
					this.m_listener.userCommand(new TreeDisplayEvent(TreeDisplayEvent.REMOVE_CHILDREN,
							this.m_nodes[this.m_focusNode].m_node.getRefer()));
				} else {
					JOptionPane.showMessageDialog(
							this,
							Messages.getInstance().getString(
									"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Nineth"),
							Messages.getInstance().getString(
									"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Tenth"),
							JOptionPane.WARNING_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(
						this,
						Messages.getInstance().getString(
								"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Eleventh"),
						Messages.getInstance().getString(
								"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Twelveth"),
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ActionPerformed_Classify_Child_Text"))) {
			if (this.m_focusNode >= 0) {
				if (this.m_listener != null) {
					// then send message to the listener
					this.m_listener.userCommand(new TreeDisplayEvent(TreeDisplayEvent.CLASSIFY_CHILD,
							this.m_nodes[this.m_focusNode].m_node.getRefer()));
				} else {
					JOptionPane.showMessageDialog(
							this,
							Messages.getInstance().getString(
									"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Thirteenth"),
							Messages.getInstance().getString(
									"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Fourteenth"),
							JOptionPane.WARNING_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(
						this,
						Messages.getInstance().getString(
								"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Fifteenth"),
						"			"
								+ Messages.getInstance().getString(
										"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Sixteenth"),
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ActionPerformed_Send_Instances_Text"))) {
			if (this.m_focusNode >= 0) {
				if (this.m_listener != null) {
					// then send message to the listener
					this.m_listener.userCommand(new TreeDisplayEvent(TreeDisplayEvent.SEND_INSTANCES,
							this.m_nodes[this.m_focusNode].m_node.getRefer()));
				} else {
					JOptionPane.showMessageDialog(
							this,
							Messages.getInstance().getString(
									"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Seventeenth"),
							Messages.getInstance().getString(
									"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Eighteenth"),
							JOptionPane.WARNING_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(
						this,
						Messages.getInstance().getString(
								"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Nineteenth"),
						Messages.getInstance().getString(
								"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_Twentyth"),
						JOptionPane.ERROR_MESSAGE);
			}
		} else if (e.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ActionPerformed_AcceptTheTree_Text"))) {
			if (this.m_listener != null) {
				// then send message to the listener saying that the tree is
				// done
				this.m_listener.userCommand(new TreeDisplayEvent(TreeDisplayEvent.ACCEPT, null));
			} else {
				JOptionPane.showMessageDialog(
						this,
						Messages.getInstance().getString(
								"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_TwentyFirst"),
						Messages.getInstance().getString(
								"TreeVisualizer_ActionPerformed_JOptionPaneShowMessageDialog_Text_TwentySecond"),
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * This will increment the size and position of the tree towards the desired
	 * size and position a little (depending on the value of <i>frames</i>)
	 * everytime it is called.
	 * 
	 * @param n_pos
	 *            The final position of the tree wanted.
	 * @param n_size
	 *            The final size of the tree wanted.
	 * @param frames
	 *            The number of frames that shall occur before the final size
	 *            and pos is reached.
	 */
	private void animateScaling(Dimension n_pos, Dimension n_size, int frames) {
		// this function will take new size and position coords , and
		// incrementally
		// scale the view to these
		// since I will be tying it in with the framelimiter I will simply call
		// this function and increment it once
		// I will have to use a global variable since I am doing it
		// proportionally

		if (frames == 0) {
			System.out.println(Messages.getInstance().getString("TreeVisualizer_AnimateScaling_Text"));
			this.m_scaling = 0;
		} else {
			if (this.m_scaling == 0) {
				// new animate session
				// start timer and set scaling
				this.m_frameLimiter.start();
				this.m_nViewPos.width = n_pos.width;
				this.m_nViewPos.height = n_pos.height;
				this.m_nViewSize.width = n_size.width;
				this.m_nViewSize.height = n_size.height;

				this.m_scaling = frames;
			}

			int s_w = (n_size.width - this.m_viewSize.width) / frames;
			int s_h = (n_size.height - this.m_viewSize.height) / frames;
			int p_w = (n_pos.width - this.m_viewPos.width) / frames;
			int p_h = (n_pos.height - this.m_viewPos.height) / frames;

			this.m_viewSize.width += s_w;
			this.m_viewSize.height += s_h;

			this.m_viewPos.width += p_w;
			this.m_viewPos.height += p_h;

			this.repaint();

			this.m_scaling--;
			if (this.m_scaling == 0) {
				// all done
				this.m_frameLimiter.stop();
			}
		}
	}

	/**
	 * This will fill two arrays with the Nodes and Edges from the tree into a
	 * particular order.
	 * 
	 * @param t
	 *            The top Node of the tree.
	 * @param l
	 *            An array that has already been allocated, to be filled.
	 * @param k
	 *            An array that has already been allocated, to be filled.
	 */
	private void arrayFill(Node t, NodeInfo[] l, EdgeInfo[] k) {

		// this will take the top node and the array to fill
		// it will go through the tree structure and and fill the array with the
		// nodes
		// from top to bottom left to right

		// note I do not believe this function will be able to deal with
		// multiple
		// parents

		if (t == null || l == null) {
			System.exit(1); // this is just a preliminary safety check
			// (i shouldn' need it)
		}

		Edge e;
		Node r, s;
		l[0] = new NodeInfo();
		l[0].m_node = t;
		l[0].m_parent = -1;
		l[0].m_change = true;

		int floater; // this will point at a node that has previously been
		// put in the list
		// all of the children that this node has shall be put in the list ,
		// once this is done the floater shall point at the next node in the
		// list
		// this will allow the nodes to be put into order from closest to top
		// node
		// to furtherest from top node

		int free_space = 1; // the next empty array position

		double height = t.getTop(); // this will be used to determine if the
									// node
		// has a
		// new height compared to the
		// previous one

		for (floater = 0; floater < free_space; floater++) {
			r = l[floater].m_node;
			for (int noa = 0; (e = r.getChild(noa)) != null; noa++) {
				// this loop pulls out each child of r

				// e points to a child edge, getTarget will return that edges
				// child node
				s = e.getTarget();
				l[free_space] = new NodeInfo();
				l[free_space].m_node = s;
				l[free_space].m_parent = free_space - 1;

				k[free_space - 1] = new EdgeInfo();
				k[free_space - 1].m_edge = e;
				k[free_space - 1].m_parent = floater;
				k[free_space - 1].m_child = free_space; // note although it's
														// child
				// will always have a subscript
				// of 1 more , I may not nessecarily have access to that
				// and it will need the subscr.. for multiple parents

				// determine if level of node has changed from previous one
				if (height != s.getTop()) {
					l[free_space].m_change = true;
					height = s.getTop();
				} else {
					l[free_space].m_change = false;
				}
				free_space++;
			}
		}
	}

	/**
	 * This Calculates the minimum size of the tree which will prevent any text
	 * overlapping and make it readable, and then set the size of the tree to
	 * this.
	 */
	public void autoScale() {
		// this function will determine the smallest scale value that keeps the
		// text
		// from overlapping
		// it will leave the view centered

		int dist;
		Node ln, rn;
		Dimension temp = new Dimension(10, 10);

		if (this.m_numNodes <= 1) {
			return;
		}

		// calc height needed by first node
		dist = (this.m_nodes[0].m_height + 40) * this.m_numLevels;
		if (dist > temp.height) {
			temp.height = dist;
		}

		for (int noa = 0; noa < this.m_numNodes - 1; noa++) {
			this.calcScreenCoords(noa);
			this.calcScreenCoords(noa + 1);
			if (this.m_nodes[noa + 1].m_change) {
				// then on a new level so don't check width this time round
			} else {

				dist = this.m_nodes[noa + 1].m_center - this.m_nodes[noa].m_center;
				// the distance between the node centers, along horiz
				if (dist <= 0) {
					dist = 1;
				}
				dist = (6 + this.m_nodes[noa].m_side + this.m_nodes[noa + 1].m_side) * this.m_viewSize.width / dist; // calc
				// optimal
				// size
				// for
				// width

				if (dist > temp.width) {

					temp.width = dist;
				}
			}
			// now calc.. minimun hieght needed by nodes

			dist = (this.m_nodes[noa + 1].m_height + 40) * this.m_numLevels;
			if (dist > temp.height) {

				temp.height = dist;
			}
		}

		int y1, y2, xa, xb;

		y1 = this.m_nodes[this.m_edges[0].m_parent].m_top;
		y2 = this.m_nodes[this.m_edges[0].m_child].m_top;

		dist = y2 - y1;
		if (dist <= 0) {
			dist = 1;
		}
		dist = (60 + this.m_edges[0].m_height + this.m_nodes[this.m_edges[0].m_parent].m_height)
				* this.m_viewSize.height / dist;
		if (dist > temp.height) {

			temp.height = dist;
		}

		for (int noa = 0; noa < this.m_numNodes - 2; noa++) {
			// check the edges now
			if (this.m_nodes[this.m_edges[noa + 1].m_child].m_change) {
				// then edge is on a different level , so skip this one
			} else {
				// calc the width requirements of this pair of edges

				xa = this.m_nodes[this.m_edges[noa].m_child].m_center
						- this.m_nodes[this.m_edges[noa].m_parent].m_center;
				xa /= 2;
				xa += this.m_nodes[this.m_edges[noa].m_parent].m_center;

				xb = this.m_nodes[this.m_edges[noa + 1].m_child].m_center
						- this.m_nodes[this.m_edges[noa + 1].m_parent].m_center;
				xb /= 2;
				xb += this.m_nodes[this.m_edges[noa + 1].m_parent].m_center;

				dist = xb - xa;
				if (dist <= 0) {
					dist = 1;
				}
				dist = (12 + this.m_edges[noa].m_side + this.m_edges[noa + 1].m_side) * this.m_viewSize.width / dist;
				if (dist > temp.width) {

					temp.width = dist;
				}
			}
			// now calc height need by the edges
			y1 = this.m_nodes[this.m_edges[noa + 1].m_parent].m_top;
			y2 = this.m_nodes[this.m_edges[noa + 1].m_child].m_top;

			dist = y2 - y1;
			if (dist <= 0) {

				dist = 1;
			}
			dist = (60 + this.m_edges[noa + 1].m_height + this.m_nodes[this.m_edges[noa + 1].m_parent].m_height)
					* this.m_viewSize.height / dist;

			if (dist > temp.height) {

				temp.height = dist;
			}
		}

		Dimension e = this.getSize();

		Dimension np = new Dimension();
		np.width = (int) (e.width / 2 - ((double) e.width / 2 - this.m_viewPos.width) / this.m_viewSize.width
				* temp.width);
		np.height = (int) (e.height / 2 - ((double) e.height / 2 - this.m_viewPos.height) / this.m_viewSize.height
				* temp.height);
		// animate_scaling(c_size,c_pos,25);

		for (int noa = 0; noa < this.m_numNodes; noa++) {
			// this resets the coords so that next time a refresh occurs they
			// don't
			// accidentally get used
			// I will use 32000 to signify that they are invalid, even if this
			// coordinate occurs it doesn't
			// matter as it is only for the sake of the caching

			this.m_nodes[noa].m_top = 32000;

		}
		this.animateScaling(np, temp, 10);
	}

	/**
	 * Converts the internal coordinates of the node found from <i>n</i> and
	 * converts them to the actual screen coordinates.
	 * 
	 * @param n
	 *            A subscript identifying the Node.
	 */
	private void calcScreenCoords(int n) {
		// this converts the coordinate system the Node uses into screen
		// coordinates
		// System.out.println(n + " " + view_pos.height + " " +
		// nodes[n].node.getCenter());
		if (this.m_nodes[n].m_top == 32000) {
			this.m_nodes[n].m_top = (int) (this.m_nodes[n].m_node.getTop() * this.m_viewSize.height)
					+ this.m_viewPos.height;
			this.m_nodes[n].m_center = (int) (this.m_nodes[n].m_node.getCenter() * this.m_viewSize.width)
					+ this.m_viewPos.width;
		}
	}

	public void centerTopNode() {
		int tpx = (int) (this.m_topNode.getCenter() * this.m_viewSize.width); // calculate
		// the top nodes postion but don't adjust for where
		int tpy = (int) (this.m_topNode.getTop() * this.m_viewSize.height); // view
																			// is

		Dimension np = new Dimension(this.getSize().width / 2 - tpx, this.getSize().width / 6 - tpy);

		this.animateScaling(np, this.m_viewSize, 10);
	}

	/**
	 * This will change the font size for displaying the tree to the one
	 * specified.
	 * 
	 * @param s
	 *            The new pointsize of the font.
	 */
	private void changeFontSize(int s) {
		// this will set up the new font that shall be used
		// it will also recalculate the size of the nodes as these will change
		// as
		// a result of
		// the new font size
		this.setFont(this.m_currentFont = new Font("A Name", 0, s));

		this.m_fontSize = this.getFontMetrics(this.getFont());

		Dimension d;

		for (int noa = 0; noa < this.m_numNodes; noa++) {
			// this will set the size info for each node and edge

			d = this.m_nodes[noa].m_node.stringSize(this.m_fontSize);

			if (this.m_nodes[noa].m_node.getShape() == 1) {
				this.m_nodes[noa].m_height = d.height + 10;
				this.m_nodes[noa].m_width = d.width + 8;
				this.m_nodes[noa].m_side = this.m_nodes[noa].m_width / 2;
			} else if (this.m_nodes[noa].m_node.getShape() == 2) {
				this.m_nodes[noa].m_height = (int) ((d.height + 2) * 1.6);
				this.m_nodes[noa].m_width = (int) ((d.width + 2) * 1.6);
				this.m_nodes[noa].m_side = this.m_nodes[noa].m_width / 2;
			}

			if (noa < this.m_numNodes - 1) {
				// this will do the same for edges

				d = this.m_edges[noa].m_edge.stringSize(this.m_fontSize);

				this.m_edges[noa].m_height = d.height + 8;
				this.m_edges[noa].m_width = d.width + 8;
				this.m_edges[noa].m_side = this.m_edges[noa].m_width / 2;
				this.m_edges[noa].m_tb = this.m_edges[noa].m_height / 2;
			}
		}
	}

	/**
	 * Determines the attributes of the edge and draws it.
	 * 
	 * @param e
	 *            A subscript identifying the edge in <i>edges</i> array.
	 * @param g
	 *            The drawing surface.
	 */
	private void drawLine(int e, Graphics g) {
		// this will draw a line taking in the edge number and then getting
		// the nodes subscript for the parent and child entries

		// this will draw a line that has been broken in the middle
		// for the edge text to be displayed
		// if applicable

		// first convert both parent and child node coords to screen coords
		int p = this.m_edges[e].m_parent;
		int c = this.m_edges[e].m_child;
		this.calcScreenCoords(c);
		this.calcScreenCoords(p);

		Graphics2D g2 = (Graphics2D) g;

		if (this.m_edges[e].m_edge.m_lead_to_selection) {
			g2.setColor(TreeConstants.EDGE_HIGHLIGHTED_COLOR);
			g2.setStroke(new BasicStroke(TreeConstants.EDGE_STROKE_SIZE));
		} else if (this.m_LineColor == null) {
			g2.setColor(Color.black);
			g2.setStroke(new BasicStroke());
		} else {
			g2.setColor(this.m_LineColor);
			g2.setStroke(new BasicStroke());
		}
		g.setPaintMode();

		if (this.m_currentFont.getSize() < 2) {
			// text to small to bother cutting the edge
			g2.drawLine(this.m_nodes[p].m_center, this.m_nodes[p].m_top + this.m_nodes[p].m_height,
					this.m_nodes[c].m_center, this.m_nodes[c].m_top);

		} else {
			// find where to cut the edge to insert text
			int e_width = this.m_nodes[c].m_center - this.m_nodes[p].m_center;
			int e_height = this.m_nodes[c].m_top - (this.m_nodes[p].m_top + this.m_nodes[p].m_height);
			int e_width2 = e_width / 2;
			int e_height2 = e_height / 2;
			int e_centerx = this.m_nodes[p].m_center + e_width2;
			int e_centery = this.m_nodes[p].m_top + this.m_nodes[p].m_height + e_height2;
			int e_offset = this.m_edges[e].m_tb;

			int tmp = (int) ((double) e_width / e_height * (e_height2 - e_offset)) + this.m_nodes[p].m_center;
			// System.out.println(edges[e].m_height);

			// draw text now

			this.drawText(e_centerx - this.m_edges[e].m_side, e_centery - e_offset, e, true, g);

			if (tmp > e_centerx - this.m_edges[e].m_side && tmp < e_centerx + this.m_edges[e].m_side) {
				// then cut line on top and bottom of text
				g2.drawLine(this.m_nodes[p].m_center, this.m_nodes[p].m_top + this.m_nodes[p].m_height, tmp, e_centery
						- e_offset); // first
				// segment
				g2.drawLine(e_centerx * 2 - tmp, e_centery + e_offset, this.m_nodes[c].m_center, this.m_nodes[c].m_top); // second
				// segment
			} else {
				e_offset = this.m_edges[e].m_side;
				if (e_width < 0) {
					e_offset *= -1; // adjusting for direction which could
									// otherwise
					// screw up the calculation
				}
				tmp = (int) ((double) e_height / e_width * (e_width2 - e_offset)) + this.m_nodes[p].m_top
						+ this.m_nodes[p].m_height;

				g2.drawLine(this.m_nodes[p].m_center, this.m_nodes[p].m_top + this.m_nodes[p].m_height, e_centerx
						- e_offset, tmp); // first
				// segment
				g2.drawLine(e_centerx + e_offset, e_centery * 2 - tmp, this.m_nodes[c].m_center, this.m_nodes[c].m_top); // second
				// segment

			}
		}
		// System.out.println("here" + nodes[p].center);
	}

	/**
	 * Determines the attributes of the node and draws it.
	 * 
	 * @param n
	 *            A subscript identifying the node in <i>nodes</i> array
	 * @param g
	 *            The drawing surface
	 */
	private void drawNode(int n, Graphics g) {
		// this will draw a node and then print text on it
		if (this.m_NodeColor == null) {
			g.setColor(this.m_nodes[n].m_node.getColor());
		} else {
			g.setColor(this.m_NodeColor);
		}

		g.setPaintMode();
		this.calcScreenCoords(n);
		int x = this.m_nodes[n].m_center - this.m_nodes[n].m_side;
		int y = this.m_nodes[n].m_top;

		if (this.m_nodes[n].m_node.getShape() == 1) {// leaf node -> rect
			String st;
			Node node = this.m_nodes[n].m_node;
			// determine it's a above or below node
			for (int noa = 0; (st = node.getLine(noa)) != null; noa++) {
				if (st.startsWith(TreeConstants.BELOW_STRING)) {
					g.setColor(this.m_belowColor);
				} else if (st.startsWith(TreeConstants.ABOVE_STRING)) {
					g.setColor(this.m_aboveColor);
				} else {
				}
			}
			//int coefficient = this.getLogSizeByInstanceNumber(this.m_nodes[n].m_node.getInstances().numInstances());
			g.fill3DRect(x, y, this.m_nodes[n].m_width, this.m_nodes[n].m_height, true);
			this.drawText(x, y, n, false, g);
			if (this.m_nodes[n].m_node.m_contains_selection) {
				g.setColor(TreeConstants.NODE_HIGHLIGHTED_COLOR);
				g.drawRect(this.m_nodes[n].m_center - this.m_nodes[n].m_side, this.m_nodes[n].m_top,
						this.m_nodes[n].m_width, this.m_nodes[n].m_height);

				g.drawRect(this.m_nodes[n].m_center - this.m_nodes[n].m_side + 1, this.m_nodes[n].m_top + 1,
						this.m_nodes[n].m_width - 2, this.m_nodes[n].m_height - 2);
			}
		} else if (this.m_nodes[n].m_node.getShape() == 2) {// non-leaf node ->
															// oval
			g.setColor(TreeConstants.OVAL_COLOR);
			g.fillOval(x, y, this.m_nodes[n].m_width, this.m_nodes[n].m_height);
			this.drawText(x, y + (int) (this.m_nodes[n].m_height * .15), n, false, g);
			if (this.m_nodes[n].m_node.m_contains_selection) {
				g.setColor(TreeConstants.NODE_HIGHLIGHTED_COLOR);
				g.drawOval(this.m_nodes[n].m_center - this.m_nodes[n].m_side, this.m_nodes[n].m_top,
						this.m_nodes[n].m_width, this.m_nodes[n].m_height);

				g.drawOval(this.m_nodes[n].m_center - this.m_nodes[n].m_side + 1, this.m_nodes[n].m_top + 1,
						this.m_nodes[n].m_width - 2, this.m_nodes[n].m_height - 2);
			}
		}
	}

	/**
	 * Draws the text for either an Edge or a Node.
	 * 
	 * @param x1
	 *            the left side of the text area.
	 * @param y1
	 *            the top of the text area.
	 * @param s
	 *            A subscript identifying either a Node or Edge.
	 * @param e_or_n
	 *            Distinguishes whether it is a node or edge.
	 * @param g
	 *            The drawing surface.
	 */
	private void drawText(int x1, int y1, int s, boolean e_or_n, Graphics g) {
		// this function will take in the rectangle that the text should be
		// drawn in as well as the subscript
		// for either the edge or node and a boolean variable to tell which

		// backup color
		Color oldColor = g.getColor();

		g.setPaintMode();
		if (this.m_FontColor == null) {
			g.setColor(Color.black);
		} else {
			g.setColor(this.m_FontColor);
		}
		String st;
		if (e_or_n) {
			// then paint for edge
			Edge e = this.m_edges[s].m_edge;
			for (int noa = 0; (st = e.getLine(noa)) != null; noa++) {
				g.drawString(st, (this.m_edges[s].m_width - this.m_fontSize.stringWidth(st)) / 2 + x1, y1 + (noa + 1)
						* this.m_fontSize.getHeight());
			}
		} else {
			// then paint for node
			Node e = this.m_nodes[s].m_node;
			for (int noa = 0; (st = e.getLine(noa)) != null; noa++) {
				g.drawString(st, (this.m_nodes[s].m_width - this.m_fontSize.stringWidth(st)) / 2 + x1, y1 + (noa + 1)
						* this.m_fontSize.getHeight());
			}
		}

		// restore color
		g.setColor(oldColor);
	}

	/**
	 * Fits the tree to the current screen size. Call this after window has been
	 * created to get the entrire tree to be in view upon launch.
	 */
	public void fitToScreen() {

		this.getScreenFit(this.m_viewPos, this.m_viewSize);
		this.repaint();
	}

	/**
	 * Processes the color string. Returns null if empty.
	 * 
	 * @param colorStr
	 *            the string to process
	 * @return the processed color or null
	 */
	protected Color getColor(String colorStr) {
		Color result;

		result = null;

		if (colorStr != null && colorStr.length() > 0) {
			result = VisualizeUtils.processColour(colorStr, result);
		}

		return result;
	}

	protected int getLogSizeByInstanceNumber(int num) {
		return (int) Math.pow(num, 0.2);
	}

	/**
	 * Calculates the dimensions needed to fit the entire tree into view.
	 */
	private void getScreenFit(Dimension np, Dimension ns) {

		int leftmost = 1000000, rightmost = -1000000;
		int leftCenter = 1000000, rightCenter = -1000000, rightNode = 0;
		int highest = -1000000, highTop = -1000000;
		for (int noa = 0; noa < this.m_numNodes; noa++) {
			this.calcScreenCoords(noa);
			if (this.m_nodes[noa].m_center - this.m_nodes[noa].m_side < leftmost) {
				leftmost = this.m_nodes[noa].m_center - this.m_nodes[noa].m_side;
			}
			if (this.m_nodes[noa].m_center < leftCenter) {
				leftCenter = this.m_nodes[noa].m_center;
			}

			if (this.m_nodes[noa].m_center + this.m_nodes[noa].m_side > rightmost) {
				rightmost = this.m_nodes[noa].m_center + this.m_nodes[noa].m_side;
			}
			if (this.m_nodes[noa].m_center > rightCenter) {
				rightCenter = this.m_nodes[noa].m_center;
				rightNode = noa;
			}
			if (this.m_nodes[noa].m_top + this.m_nodes[noa].m_height > highest) {
				highest = this.m_nodes[noa].m_top + this.m_nodes[noa].m_height;
			}
			if (this.m_nodes[noa].m_top > highTop) {
				highTop = this.m_nodes[noa].m_top;
			}
		}

		ns.width = this.getWidth();
		ns.width -= leftCenter - leftmost + rightmost - rightCenter + 30;
		ns.height = this.getHeight() - highest + highTop - 40;

		if (this.m_nodes[rightNode].m_node.getCenter() != 0 && leftCenter != rightCenter) {
			ns.width /= this.m_nodes[rightNode].m_node.getCenter();
		}
		if (ns.width < 10) {
			ns.width = 10;
		}
		if (ns.height < 10) {
			ns.height = 10;
		}

		np.width = (leftCenter - leftmost + rightmost - rightCenter) / 2 + 15;
		np.height = (highest - highTop) / 2 + 20;
	}

	/**
	 * Performs some initialization.
	 */
	protected void initialize() {
		Properties props;

		try {
			props = Utils.readProperties(TreeVisualizer.PROPERTIES_FILE);
		} catch (Exception e) {
			e.printStackTrace();
			props = new Properties();
		}

		this.m_FontColor = this.getColor(props.getProperty("FontColor", ""));
		// this.m_BackgroundColor =
		// getColor(props.getProperty("BackgroundColor", ""));
		this.m_NodeColor = this.getColor(props.getProperty("NodeColor", ""));
		this.m_LineColor = this.getColor(props.getProperty("LineColor", ""));
		this.m_ZoomBoxColor = this.getColor(props.getProperty("ZoomBoxColor", ""));
		this.m_ZoomBoxXORColor = this.getColor(props.getProperty("ZoomBoxXORColor", ""));
		this.m_ShowBorder = Boolean.parseBoolean(props.getProperty("ShowBorder", "true"));
	}

	/**
	 * Performs the action associated with the ItemEvent.
	 * 
	 * @param e
	 *            the item event.
	 */
	@Override
	public void itemStateChanged(ItemEvent e) {
		JRadioButtonMenuItem c = (JRadioButtonMenuItem) e.getSource();
		if (c.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size24_Text"))) {
			this.changeFontSize(24);
		} else if (c.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size22_Text"))) {
			this.changeFontSize(22);
		} else if (c.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size20_Text"))) {
			this.changeFontSize(20);
		} else if (c.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size18_Text"))) {
			this.changeFontSize(18);
		} else if (c.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size16_Text"))) {
			this.changeFontSize(16);
		} else if (c.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size14_Text"))) {
			this.changeFontSize(14);
		} else if (c.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size12_Text"))) {
			this.changeFontSize(12);
		} else if (c.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size10_Text"))) {
			this.changeFontSize(10);
		} else if (c.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size8_Text"))) {
			this.changeFontSize(8);
		} else if (c.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size6_Text"))) {
			this.changeFontSize(6);
		} else if (c.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size4_Text"))) {
			this.changeFontSize(4);
		} else if (c.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size2_Text"))) {
			this.changeFontSize(2);
		} else if (c.getActionCommand().equals(
				Messages.getInstance().getString("TreeVisualizer_ItemStateChanged_GetActionCommand_Size1_Text"))) {
			this.changeFontSize(1);
		} else if (c.getActionCommand().equals(
				Messages.getInstance().getString(
						"TreeVisualizer_ItemStateChanged_GetActionCommand_HideDescendants_Text"))) {
			// focus_node.setCVisible(!c.isSelected());
			// no longer used...
		}
	}

	/**
	 * Does nothing.
	 * 
	 * @param e
	 *            the mouse event.
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		// if the mouse was left clicked on
		// the node then
		if (this.m_clickAvailable) {
			// determine if the click was on a node or not
			int s = -1;

			for (int noa = 0; noa < this.m_numNodes; noa++) {
				if (this.m_nodes[noa].m_quad == 18) {
					// then is on the screen 
					this.calcScreenCoords(noa);
					if (e.getX() <= this.m_nodes[noa].m_center + this.m_nodes[noa].m_side
							&& e.getX() >= this.m_nodes[noa].m_center - this.m_nodes[noa].m_side
							&& e.getY() >= this.m_nodes[noa].m_top
							&& e.getY() <= this.m_nodes[noa].m_top + this.m_nodes[noa].m_height) {
						// then it is this node that the mouse was clicked on
						s = noa;
					}
					this.m_nodes[noa].m_top = 32000;
				}
			}
			this.m_focusNode = s;

			if (this.m_focusNode != -1) {
				if (this.m_listener != null) {
					// then set this to be the selected node for editing
					this.actionPerformed(new ActionEvent(this, 32000, Messages.getInstance().getString(
							"TreeVisualizer_ItemStateChanged_GetActionCommand_ActionPerformed_Text_First")));
				} else {
					// then open a visualize to display this nodes instances if
					// possible
					// actionPerformed(new ActionEvent(this, 32000,
					// Messages.getInstance().getString(
					// "TreeVisualizer_ItemStateChanged_GetActionCommand_ActionPerformed_Text_Second")));
					this.m_highlightNode = this.m_focusNode;
					// m_nodes[m_highlightNode].m_node.getInstances();
				}
				this.repaint();
			}
		}
	}

	/**
	 * Performs intermediate updates to what the user wishes to do.
	 * 
	 * @param e
	 *            the mouse event.
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		// use mouse state to determine what to do to the view of the tree

		if (this.m_mouseState == 1) {
			// then dragging view
			this.m_oldMousePos.width = this.m_newMousePos.width;
			this.m_oldMousePos.height = this.m_newMousePos.height;
			this.m_newMousePos.width = e.getX();
			this.m_newMousePos.height = e.getY();
			this.m_viewPos.width += this.m_newMousePos.width - this.m_oldMousePos.width;
			this.m_viewPos.height += this.m_newMousePos.height - this.m_oldMousePos.height;

		} else if (this.m_mouseState == 3) {
			// then zoom box being created
			// redraw the zoom box
			Graphics g = this.getGraphics();
			if (this.m_ZoomBoxColor == null) {
				g.setColor(Color.black);
			} else {
				g.setColor(this.m_ZoomBoxColor);
			}
			if (this.m_ZoomBoxXORColor == null) {
				g.setXORMode(Color.white);
			} else {
				g.setXORMode(this.m_ZoomBoxXORColor);
			}
			g.drawRect(this.m_oldMousePos.width, this.m_oldMousePos.height, this.m_newMousePos.width
					- this.m_oldMousePos.width, this.m_newMousePos.height - this.m_oldMousePos.height);

			this.m_newMousePos.width = e.getX();
			this.m_newMousePos.height = e.getY();

			g.drawRect(this.m_oldMousePos.width, this.m_oldMousePos.height, this.m_newMousePos.width
					- this.m_oldMousePos.width, this.m_newMousePos.height - this.m_oldMousePos.height);
			g.dispose();
		}

	}

	/**
	 * Does nothing.
	 * 
	 * @param e
	 *            the mouse event.
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
	}

	/**
	 * Does nothing.
	 * 
	 * @param e
	 *            the mouse event.
	 */
	@Override
	public void mouseExited(MouseEvent e) {
	}

	/**
	 * Checks to see if the coordinates of the mouse lie on this JPanel.
	 * 
	 * @param e
	 *            the mouse event.
	 * @return true if the mouse lies on this JPanel.
	 */
	private boolean mouseInBounds(MouseEvent e) {
		// this returns true if the mouse is currently over the canvas otherwise
		// false

		if (e.getX() < 0 || e.getY() < 0 || e.getX() > this.getSize().width || e.getY() > this.getSize().height) {
			return false;
		}
		return true;
	}

	/**
	 * Does nothing.
	 * 
	 * @param e
	 *            the mouse event.
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
	}

	/**
	 * Determines what action the user wants to perform.
	 * 
	 * @param e
	 *            the mouse event.
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		this.m_frameLimiter.setRepeats(true);
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) != 0 && !e.isAltDown() && this.m_mouseState == 0
				&& this.m_scaling == 0) {
			// then the left mouse button has been pressed
			// check for modifiers

			if ((e.getModifiers() & InputEvent.CTRL_MASK) != 0 && (e.getModifiers() & InputEvent.SHIFT_MASK) == 0) {
				// then is in zoom out mode
				this.m_mouseState = 2;
			} else if ((e.getModifiers() & InputEvent.SHIFT_MASK) != 0
					&& (e.getModifiers() & InputEvent.CTRL_MASK) == 0) {
				// then is in zoom mode
				// note if both are pressed default action is to zoom out
				this.m_oldMousePos.width = e.getX();
				this.m_oldMousePos.height = e.getY();
				this.m_newMousePos.width = e.getX();
				this.m_newMousePos.height = e.getY();
				this.m_mouseState = 3;

				Graphics g = this.getGraphics();
				if (this.m_ZoomBoxColor == null) {
					g.setColor(Color.black);
				} else {
					g.setColor(this.m_ZoomBoxColor);
				}
				if (this.m_ZoomBoxXORColor == null) {
					g.setXORMode(Color.white);
				} else {
					g.setXORMode(this.m_ZoomBoxXORColor);
				}
				g.drawRect(this.m_oldMousePos.width, this.m_oldMousePos.height, this.m_newMousePos.width
						- this.m_oldMousePos.width, this.m_newMousePos.height - this.m_oldMousePos.height);
				g.dispose();
			} else {
				// no modifiers drag area around
				this.m_oldMousePos.width = e.getX();
				this.m_oldMousePos.height = e.getY();
				this.m_newMousePos.width = e.getX();
				this.m_newMousePos.height = e.getY();
				this.m_mouseState = 1;
				this.m_frameLimiter.start();
			}

		}
		// pop up save dialog explicitly (is somehow overridden...)
		else if (e.getButton() == MouseEvent.BUTTON1 && e.isAltDown() && e.isShiftDown() && !e.isControlDown()) {
			this.saveComponent();
		} else if (this.m_mouseState == 0 && this.m_scaling == 0) {
			// either middle or right mouse button pushed
			// determine menu to use
		}
	}

	/**
	 * Performs the final stages of what the user wants to perform.
	 * 
	 * @param e
	 *            the mouse event.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if (this.m_mouseState == 1) {
			// this is used by mouseClicked to determine if it is alright to do
			// something
			this.m_clickAvailable = true;
			// note that a standard click with the left mouse is pretty much the
			// only safe input left to be assigned anything.
		} else {
			this.m_clickAvailable = false;
		}
		if (this.m_mouseState == 2 && this.mouseInBounds(e)) {
			// then zoom out;
			this.m_mouseState = 0;
			Dimension ns = new Dimension(this.m_viewSize.width / 2, this.m_viewSize.height / 2);
			if (ns.width < 10) {
				ns.width = 10;
			}
			if (ns.height < 10) {
				ns.height = 10;
			}

			Dimension d = this.getSize();
			Dimension np = new Dimension((int) (d.width / 2 - ((double) d.width / 2 - this.m_viewPos.width) / 2),
					(int) (d.height / 2 - ((double) d.height / 2 - this.m_viewPos.height) / 2));

			this.animateScaling(np, ns, 10);

			// view_pos.width += view_size.width / 2;
			// view_pos.height += view_size.height / 2;

		} else if (this.m_mouseState == 3) {
			// then zoom in
			this.m_mouseState = 0;
			Graphics g = this.getGraphics();
			if (this.m_ZoomBoxColor == null) {
				g.setColor(Color.black);
			} else {
				g.setColor(this.m_ZoomBoxColor);
			}
			if (this.m_ZoomBoxXORColor == null) {
				g.setXORMode(Color.white);
			} else {
				g.setXORMode(this.m_ZoomBoxXORColor);
			}
			g.drawRect(this.m_oldMousePos.width, this.m_oldMousePos.height, this.m_newMousePos.width
					- this.m_oldMousePos.width, this.m_newMousePos.height - this.m_oldMousePos.height);
			g.dispose();

			int cw = this.m_newMousePos.width - this.m_oldMousePos.width;
			int ch = this.m_newMousePos.height - this.m_oldMousePos.height;
			if (cw >= 1 && ch >= 1) {
				if (this.mouseInBounds(e) && this.getSize().width / cw <= 6 && this.getSize().height / ch <= 6) {

					// now calculate new position and size
					Dimension ns = new Dimension();
					Dimension np = new Dimension();
					double nvsw = this.getSize().width / (double) cw;
					double nvsh = this.getSize().height / (double) ch;
					np.width = (int) ((this.m_oldMousePos.width - this.m_viewPos.width) * -nvsw);
					np.height = (int) ((this.m_oldMousePos.height - this.m_viewPos.height) * -nvsh);
					ns.width = (int) (this.m_viewSize.width * nvsw);
					ns.height = (int) (this.m_viewSize.height * nvsh);

					this.animateScaling(np, ns, 10);

				}
			}
		} else if (this.m_mouseState == 0 && this.m_scaling == 0) {
			// menu
			this.m_mouseState = 0;
			this.setFont(new Font("A Name", 0, 12));
			// determine if the click was on a node or not
			int s = -1;

			for (int noa = 0; noa < this.m_numNodes; noa++) {
				if (this.m_nodes[noa].m_quad == 18) {
					// then is on the screen
					this.calcScreenCoords(noa);
					if (e.getX() <= this.m_nodes[noa].m_center + this.m_nodes[noa].m_side
							&& e.getX() >= this.m_nodes[noa].m_center - this.m_nodes[noa].m_side
							&& e.getY() >= this.m_nodes[noa].m_top
							&& e.getY() <= this.m_nodes[noa].m_top + this.m_nodes[noa].m_height) {
						// then it is this node that the mouse was clicked on
						s = noa;
					}
					this.m_nodes[noa].m_top = 32000;
				}
			}
			if (s == -1) {
				// the mouse wasn't clicked on a node
				this.m_winMenu.show(this, e.getX(), e.getY());
			} else {
				// the mouse was clicked on a node
				this.m_focusNode = s;
				this.m_nodeMenu.show(this, e.getX(), e.getY());

			}
			this.setFont(this.m_currentFont);
		} else if (this.m_mouseState == 1) {
			// dragging
			this.m_mouseState = 0;
			this.m_frameLimiter.stop();
			this.repaint();
		}

	}

	/**
	 * Updates the screen contents.
	 * 
	 * @param g
	 *            the drawing surface.
	 */
	@Override
	public void paintComponent(Graphics g) {
		Color oldBackground = ((Graphics2D) g).getBackground();
		if (this.m_BackgroundColor != null) {
			((Graphics2D) g).setBackground(this.m_BackgroundColor);
		}
		g.clearRect(0, 0, this.getSize().width, this.getSize().height);
		((Graphics2D) g).setBackground(oldBackground);
		g.setClip(3, 7, this.getWidth() - 6, this.getHeight() - 10);
		this.painter(g);
		g.setClip(0, 0, this.getWidth(), this.getHeight());

	}

	/**
	 * Draws the tree to the graphics context
	 * 
	 * @param g
	 *            the drawing surface.
	 */
	private void painter(Graphics g) {
		// I have moved what would normally be in the paintComponent
		// function to here
		// for now so that if I do in fact need to do double
		// buffering or the like it will be easier

		// this will go through the table of edges and draw the edge if it deems
		// the
		// two nodes attached to it could cause it to cut the screen or be on
		// it.

		// in the process flagging all nodes so that they can quickly be put to
		// the
		// screen if they lie on it

		// I do it in this order because in some circumstances I have seen a
		// line
		// cut through a node , to make things look better the line will
		// be drawn under the node

		// converting the screen edges to the node scale so that they
		// can be positioned relative to the screen
		// note I give a buffer around the edges of the screen.

		// when seeing
		// if a node is on screen I only bother to check the nodes top centre
		// if it has large enough size it may still fall onto the screen
		double left_clip = (double) (-this.m_viewPos.width - 50) / this.m_viewSize.width;
		double right_clip = (double) (this.getSize().width - this.m_viewPos.width + 50) / this.m_viewSize.width;
		double top_clip = (double) (-this.m_viewPos.height - 50) / this.m_viewSize.height;
		double bottom_clip = (double) (this.getSize().height - this.m_viewPos.height + 50) / this.m_viewSize.height;

		// 12 10 9 //the quadrants
		// 20 18 17
		// 36 34 33

		// first the edges must be rendered

		Edge e;
		Node r, s;
		double ncent, ntop;

		int row = 0, col = 0, pq, cq;
		for (int noa = 0; noa < this.m_numNodes; noa++) {
			r = this.m_nodes[noa].m_node;
			if (this.m_nodes[noa].m_change) {
				// then recalc row component of quadrant
				ntop = r.getTop();
				if (ntop < top_clip) {
					row = 8;
				} else if (ntop > bottom_clip) {
					row = 32;
				} else {
					row = 16;
				}
			}

			// calc the column the node falls in for the quadrant
			ncent = r.getCenter();
			if (ncent < left_clip) {
				col = 4;
			} else if (ncent > right_clip) {
				col = 1;
			} else {
				col = 2;
			}

			this.m_nodes[noa].m_quad = row | col;

			if (this.m_nodes[noa].m_parent >= 0) {
				// this will draw the edge if it should be drawn
				// It will do this by eliminating all edges that definitely
				// won't enter
				// the screen and then draw the rest

				pq = this.m_nodes[this.m_edges[this.m_nodes[noa].m_parent].m_parent].m_quad;
				cq = this.m_nodes[noa].m_quad;

				// note that this will need to be altered if more than 1 parent
				// exists
				if ((cq & 8) == 8) {
					// then child exists above screen
				} else if ((pq & 32) == 32) {
					// then parent exists below screen
				} else if ((cq & 4) == 4 && (pq & 4) == 4) {
					// then both child and parent exist to the left of the
					// screen
				} else if ((cq & 1) == 1 && (pq & 1) == 1) {
					// then both child and parent exist to the right of the
					// screen
				} else {
					// then draw the line
					this.drawLine(this.m_nodes[noa].m_parent, g);
				}
			}

			// now draw the nodes
		}

		for (int noa = 0; noa < this.m_numNodes; noa++) {
			if (this.m_nodes[noa].m_quad == 18) {
				// then the node is on the screen , draw it
				this.drawNode(noa, g);
			}
		}

		// Draw the boundary for hightlight node
		if (this.m_highlightNode >= 0 && this.m_highlightNode < this.m_numNodes) {
			// then draw outline
			if (this.m_nodes[this.m_highlightNode].m_quad == 18) {
				Color acol;
				if (this.m_NodeColor == null) {
					acol = this.m_nodes[this.m_highlightNode].m_node.getColor();
				} else {
					acol = this.m_NodeColor;
				}
				// g.setColor(new Color((acol.getRed() + 125) % 256,
				// (acol.getGreen() + 125) % 256,
				// (acol.getBlue() + 125) % 256));
				g.setColor(Color.blue);
				// g.setXORMode(Color.white);
				if (this.m_nodes[this.m_highlightNode].m_node.getShape() == 1) {
					g.drawRect(this.m_nodes[this.m_highlightNode].m_center - this.m_nodes[this.m_highlightNode].m_side,
							this.m_nodes[this.m_highlightNode].m_top, this.m_nodes[this.m_highlightNode].m_width,
							this.m_nodes[this.m_highlightNode].m_height);

					g.drawRect(this.m_nodes[this.m_highlightNode].m_center - this.m_nodes[this.m_highlightNode].m_side
							+ 1, this.m_nodes[this.m_highlightNode].m_top + 1,
							this.m_nodes[this.m_highlightNode].m_width - 2,
							this.m_nodes[this.m_highlightNode].m_height - 2);
				} else if (this.m_nodes[this.m_highlightNode].m_node.getShape() == 2) {
					g.drawOval(this.m_nodes[this.m_highlightNode].m_center - this.m_nodes[this.m_highlightNode].m_side,
							this.m_nodes[this.m_highlightNode].m_top, this.m_nodes[this.m_highlightNode].m_width,
							this.m_nodes[this.m_highlightNode].m_height);

					g.drawOval(this.m_nodes[this.m_highlightNode].m_center - this.m_nodes[this.m_highlightNode].m_side
							+ 1, this.m_nodes[this.m_highlightNode].m_top + 1,
							this.m_nodes[this.m_highlightNode].m_width - 2,
							this.m_nodes[this.m_highlightNode].m_height - 2);
				}
			}
		}

		for (int noa = 0; noa < this.m_numNodes; noa++) {
			// this resets the coords so that next time a refresh occurs
			// they don't accidentally get used
			// I will use 32000 to signify that they are invalid, even if this
			// coordinate occurs it doesn't
			// matter as it is only for the sake of the caching

			this.m_nodes[noa].m_top = 32000;
		}
	}

	/**
	 * Set the highlight for the node with the given id
	 * 
	 * @param id
	 *            the id of the node to set the highlight for
	 */
	public void setHighlight(String id) {
		// set the highlight for the node with the given id
		for (int noa = 0; noa < this.m_numNodes; noa++) {
			if (id.equals(this.m_nodes[noa].m_node.getRefer())) {
				// then highlight this node
				this.m_highlightNode = noa;
			}
		}
		// System.out.println("ahuh " + highlight_node + " " +
		// nodes[0].node.getRefer());
		this.repaint();
	}
}

