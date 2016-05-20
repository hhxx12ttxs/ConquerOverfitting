package ms.jasim.console.gui.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import ms.jacrim.framework.MessageOutput;
import ms.jacrim.pddl.PddlProblem;
import ms.jasim.console.gui.component.MP_SimulationChart.MyXYItemLabelGenerator;
import ms.jasim.framework.EventSimulation;
import ms.jasim.framework.IEventInstanceProvider;
import ms.jasim.framework.IEventTypeProvider;
import ms.jasim.framework.JEventInstance;
import ms.jasim.framework.JEventType;
import ms.jasim.framework.PddlModel;
import ms.jasim.framework.PddlModelListener;
import ms.jasim.framework.PddlSolutionProvider;
import ms.jasim.framework.EventSimulation.SimulationEvent;
import ms.jasim.framework.EventSimulation.SimulationProgressArg;
import ms.jasim.framework.EventSimulation.Solution;
import ms.jasim.framework.PddlModel.Actor;
import ms.jasim.framework.PddlModel.Goal;
import ms.spm.IAppContext;
import ms.utils.Event;
import ms.utils.EventList;
import ms.utils.EventListImpl;
import ms.utils.ParamaterizedRunnable;
import ms.utils.TreeNodeData;
import ms.utils.NamedList.ItemChangedArg;

import org.jfree.chart.JFreeChart;
import org.jfree.data.general.Dataset;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class MP_EventPanel extends JPanel {

	private JTextArea txtSolPddlText;
	private JTabbedPane tabbedPane;
	private JLabel progressLabel;
	private JTextArea txtLpgLog;
	private JProgressBar progressBar;
	private SpringLayout springLayout_5;
	private JPanel eventPanel;
	private JButton cmdStartStop;
	private SpringLayout springLayout_1;
	private JSplitPane eventTypeSplitPane;
	private JButton cmdRemoveEvent;
	private JButton cmdAddEvent;
	private PropertySheetPanel psProp;
	private JList lbEventType;
	private SpringLayout springLayout_4;
	private JTree tvEvent;
	private SpringLayout springLayout_3;
	private JTextArea txtEventDescription;
	private SpringLayout springLayout_2;
	private JTextArea textArea;
	private SpringLayout springLayout;

	private static final long serialVersionUID = 1L;

	public final static Cursor busyCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
	public final static Cursor defaultCursor = Cursor.getDefaultCursor();
	public static final ImageIcon eventIcon = new ImageIcon("resources/event16x16.png");
	public static final ImageIcon solutionIcon = new ImageIcon("resources/solution_16x16.png");

	public IAppContext Context;
	private DefaultListModel lbEventTypeModel;
	private DefaultTreeModel tvEventModel, tvSolutionModel;
	private DefaultMutableTreeNode eventRoot, tvSolutionRoot;
	private ModelListener modelListener;
	private boolean simulating;
	private boolean loading = true;
	private Thread simulThread;
	private Map<EventSimulation.Solution, TreeNode> mapNode;
	private EventSimulation simul;
	private JTree tvSolution;
	private Dataset solutionChartDataset;
	private JFreeChart solutionChart;
	private MyXYItemLabelGenerator chartItemLabel;
	private MP_SimulationChart simulationChart;

	class EventInstanceProvider implements IEventInstanceProvider {

		@Override
		public JEventInstance getEvent(String instanceID) {
			return null;
		}

		@Override
		public int getEventCount() {
			return eventRoot.getChildCount();
		}

		@Override
		public List<JEventInstance> getEvents() {
			ArrayList<JEventInstance> ins = new ArrayList<JEventInstance>();
			for (int i = 0; i < eventRoot.getChildCount(); i++)
				ins.add(((EventTreeNode) eventRoot.getChildAt(i)).getEventInstance());
			return ins;
		}
	}

	class ModelListener implements PddlModelListener {

		public final EventListImpl<ItemChangedArg<Actor>> actorListener = new EventListImpl<ItemChangedArg<Actor>>();
		public final EventListImpl<ItemChangedArg<Goal>> goalListener = new EventListImpl<ItemChangedArg<Goal>>();

		Event<ItemChangedArg<Actor>> actorAdapter;
		Event<ItemChangedArg<Goal>> goalAdapter;
		PddlModel model;

		public ModelListener(PddlModel model) {
			this.model = model;
			model.Actors.addItemChangedListener(actorAdapter = new Event<ItemChangedArg<Actor>>() {
				@Override
				public void run(Object sender, ItemChangedArg<Actor> arg) {
					actorListener.invoke(sender, arg);
				}
			});

			model.Goals.addItemChangedListener(goalAdapter = new Event<ItemChangedArg<Goal>>() {

				@Override
				public void run(Object sender, ItemChangedArg<Goal> arg) {
					goalListener.invoke(sender, arg);
				}

			});
		}

		@Override
		public EventList<ItemChangedArg<Actor>> getActorListener() {
			return actorListener;
		}

		@Override
		public EventList<ItemChangedArg<Goal>> getGoalListener() {
			return goalListener;
		}

		public void unregisterListener() {
			model.Actors.removeItemChangedListener(actorAdapter);
			model.Goals.removeItemChangedListener(goalAdapter);
		}
	}

	class EventTreeNode extends DefaultMutableTreeNode {
		private static final long serialVersionUID = 1L;

		class EventTreeParam {
			private JEventType.Parameter param;

			public EventTreeParam(JEventType.Parameter param) {
				this.param = param;
			}

			@Override
			public String toString() {
				JEventInstance instance = getEventInstance();
				return String.format("%s: %s", param.getText(), param.getType().convertToString(Context, instance.getParamValue(param.getName())));
			}
		}

		public EventTreeNode(JEventInstance instance) {
			super(instance);
			for (JEventType.Parameter param : instance.Type.Parameters) {
				DefaultMutableTreeNode p = new DefaultMutableTreeNode();
				p.setUserObject(new EventTreeParam(param));
				this.add(p);
			}
		}

		public JEventInstance getEventInstance() {
			return (JEventInstance) this.getUserObject();
		}

		@Override
		public boolean isLeaf() {
			return false;
		}
	}

	class CustomIconRenderer extends DefaultTreeCellRenderer {

		private static final long serialVersionUID = 1L;

		public CustomIconRenderer() {

		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

			Object nodeObj = ((DefaultMutableTreeNode) value).getUserObject();

			if (nodeObj instanceof JEventInstance)
				setIcon(eventIcon);
			else if (nodeObj instanceof TreeNodeData) {
				TreeNodeData data = (TreeNodeData) nodeObj;
				if (data.getIcon() != null)
					setIcon(data.getIcon());
			}

			return this;
		}
	}

	class SolutionTreeNode extends DefaultMutableTreeNode {

		private static final long serialVersionUID = 1L;
		private TreeNodeData data;

		public SolutionTreeNode(EventSimulation.Solution solution) {
			super(new TreeNodeData(null, solution));
			setData((TreeNodeData) this.getUserObject());
			data.setIcon(solutionIcon);
		}

		private void setData(TreeNodeData data) {
			this.data = data;
		}

		public TreeNodeData getData() {
			return data;
		}
	}

	
	/**
	 * Create the panel
	 */
	public MP_EventPanel() {
		super();
		springLayout = new SpringLayout();
		setLayout(springLayout);
		setSize(622, 443);

		textArea = new JTextArea();
		textArea.setBackground(UIManager.getColor("Button.background"));
		textArea.setBorder(new EmptyBorder(0, 0, 0, 0));
		textArea.setFont(new Font("Tahoma", Font.PLAIN, 11));
		textArea.setOpaque(true);
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		textArea.setText("Event simulation provides an advance solution evaluation to user. This feature pr" + "ovides user an ability to see the adaptation of solutions with respect to a fixe" + "d set of runtime events.");
		add(textArea);
		springLayout.putConstraint(SpringLayout.EAST, textArea, -7, SpringLayout.EAST, this);
		springLayout.putConstraint(SpringLayout.WEST, textArea, 5, SpringLayout.WEST, this);

		tabbedPane = new JTabbedPane();
		add(tabbedPane);
		springLayout.putConstraint(SpringLayout.SOUTH, textArea, -5, SpringLayout.NORTH, tabbedPane);
		springLayout.putConstraint(SpringLayout.NORTH, textArea, 5, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.SOUTH, tabbedPane, -38, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.NORTH, tabbedPane, 50, SpringLayout.NORTH, this);
		springLayout.putConstraint(SpringLayout.EAST, tabbedPane, -7, SpringLayout.EAST, this);
		springLayout.putConstraint(SpringLayout.WEST, tabbedPane, 5, SpringLayout.WEST, this);

		eventPanel = new JPanel();
		eventPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		eventPanel.setLayout(new BorderLayout());
		tabbedPane.addTab("Events", null, eventPanel, null);

		final JSplitPane splitPane = new JSplitPane();
		splitPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setDividerSize(4);
		splitPane.setDividerLocation(200);
		eventPanel.add(splitPane);

		final JPanel panel_7 = new JPanel();
		springLayout_4 = new SpringLayout();
		panel_7.setLayout(springLayout_4);
		splitPane.setLeftComponent(panel_7);

		eventTypeSplitPane = new JSplitPane();
		eventTypeSplitPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		eventTypeSplitPane.setResizeWeight(1);
		eventTypeSplitPane.setDividerLocation(200);
		eventTypeSplitPane.setDividerSize(5);
		eventTypeSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		panel_7.add(eventTypeSplitPane);
		springLayout_4.putConstraint(SpringLayout.EAST, eventTypeSplitPane, -5, SpringLayout.EAST, panel_7);
		springLayout_4.putConstraint(SpringLayout.WEST, eventTypeSplitPane, 5, SpringLayout.WEST, panel_7);
		springLayout_4.putConstraint(SpringLayout.SOUTH, eventTypeSplitPane, -12, SpringLayout.SOUTH, panel_7);
		springLayout_4.putConstraint(SpringLayout.NORTH, eventTypeSplitPane, 30, SpringLayout.NORTH, panel_7);

		final JPanel panel_2 = new JPanel();
		panel_2.setLayout(new BorderLayout());
		panel_2.setBorder(new EmptyBorder(0, 0, 0, 0));
		eventTypeSplitPane.setLeftComponent(panel_2);

		final JScrollPane scrollPane = new JScrollPane();
		panel_2.add(scrollPane, BorderLayout.CENTER);

		lbEventType = new JList();
		lbEventType.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(final ListSelectionEvent e) {
				updateEventTypeSelection();
			}
		});
		lbEventTypeModel = new DefaultListModel();
		lbEventType.setModel(lbEventTypeModel);
		scrollPane.setViewportView(lbEventType);

		final JPanel panel_3 = new JPanel();
		springLayout_2 = new SpringLayout();
		panel_3.setLayout(springLayout_2);
		panel_3.setBorder(new EmptyBorder(0, 0, 0, 0));
		eventTypeSplitPane.setRightComponent(panel_3);

		final JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBorder(new EmptyBorder(0, 0, 0, 0));
		panel_3.add(scrollPane_1);
		springLayout_2.putConstraint(SpringLayout.EAST, scrollPane_1, -5, SpringLayout.EAST, panel_3);
		springLayout_2.putConstraint(SpringLayout.WEST, scrollPane_1, 5, SpringLayout.WEST, panel_3);
		springLayout_2.putConstraint(SpringLayout.SOUTH, scrollPane_1, -5, SpringLayout.SOUTH, panel_3);
		springLayout_2.putConstraint(SpringLayout.NORTH, scrollPane_1, 5, SpringLayout.NORTH, panel_3);

		txtEventDescription = new JTextArea();
		txtEventDescription.setText("");
		txtEventDescription.setFont(new Font("Tahoma", Font.PLAIN, 11));
		txtEventDescription.setEditable(false);
		txtEventDescription.setLineWrap(true);
		txtEventDescription.setWrapStyleWord(true);
		txtEventDescription.setOpaque(false);
		scrollPane_1.setViewportView(txtEventDescription);

		final JLabel availableEventsLabel_1_1 = new JLabel();
		availableEventsLabel_1_1.setDisplayedMnemonic(KeyEvent.VK_A);
		availableEventsLabel_1_1.setText("Available Event");
		panel_7.add(availableEventsLabel_1_1);
		springLayout_4.putConstraint(SpringLayout.NORTH, availableEventsLabel_1_1, -21, SpringLayout.NORTH, eventTypeSplitPane);
		springLayout_4.putConstraint(SpringLayout.WEST, availableEventsLabel_1_1, 0, SpringLayout.WEST, eventTypeSplitPane);

		final JPanel panel_4 = new JPanel();
		springLayout_3 = new SpringLayout();
		panel_4.setLayout(springLayout_3);
		splitPane.setRightComponent(panel_4);

		cmdAddEvent = new JButton();
		cmdAddEvent.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				addEventInstance();
			}
		});
		cmdAddEvent.setMargin(new Insets(2, 2, 2, 2));
		cmdAddEvent.setText(">");
		panel_4.add(cmdAddEvent);
		springLayout_3.putConstraint(SpringLayout.SOUTH, cmdAddEvent, 55, SpringLayout.NORTH, panel_4);
		springLayout_3.putConstraint(SpringLayout.NORTH, cmdAddEvent, 29, SpringLayout.NORTH, panel_4);
		springLayout_3.putConstraint(SpringLayout.EAST, cmdAddEvent, 60, SpringLayout.WEST, panel_4);
		springLayout_3.putConstraint(SpringLayout.WEST, cmdAddEvent, 5, SpringLayout.WEST, panel_4);

		cmdRemoveEvent = new JButton();
		cmdRemoveEvent.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				removeSelectedEvent();
			}
		});
		cmdRemoveEvent.setMargin(new Insets(2, 2, 2, 2));
		cmdRemoveEvent.setText("<");
		panel_4.add(cmdRemoveEvent);
		springLayout_3.putConstraint(SpringLayout.EAST, cmdRemoveEvent, 60, SpringLayout.WEST, panel_4);
		springLayout_3.putConstraint(SpringLayout.WEST, cmdRemoveEvent, 0, SpringLayout.WEST, cmdAddEvent);
		springLayout_3.putConstraint(SpringLayout.SOUTH, cmdRemoveEvent, 31, SpringLayout.SOUTH, cmdAddEvent);
		springLayout_3.putConstraint(SpringLayout.NORTH, cmdRemoveEvent, 5, SpringLayout.SOUTH, cmdAddEvent);

		final JLabel availableEventsLabel_1 = new JLabel();
		availableEventsLabel_1.setDisplayedMnemonic(KeyEvent.VK_S);
		availableEventsLabel_1.setText("Selected Events");
		panel_4.add(availableEventsLabel_1);
		springLayout_3.putConstraint(SpringLayout.SOUTH, availableEventsLabel_1, 26, SpringLayout.NORTH, panel_4);
		springLayout_3.putConstraint(SpringLayout.NORTH, availableEventsLabel_1, 10, SpringLayout.NORTH, panel_4);
		springLayout_3.putConstraint(SpringLayout.EAST, availableEventsLabel_1, 162, SpringLayout.WEST, panel_4);
		springLayout_3.putConstraint(SpringLayout.WEST, availableEventsLabel_1, 70, SpringLayout.WEST, panel_4);

		final JSplitPane splitPane_2 = new JSplitPane();
		splitPane_2.setDividerSize(4);
		splitPane_2.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		splitPane_2.setDividerLocation(150);
		splitPane_2.setResizeWeight(.5);
		panel_4.add(splitPane_2);
		springLayout_3.putConstraint(SpringLayout.EAST, splitPane_2, -11, SpringLayout.EAST, panel_4);
		springLayout_3.putConstraint(SpringLayout.WEST, splitPane_2, 5, SpringLayout.WEST, availableEventsLabel_1);
		springLayout_3.putConstraint(SpringLayout.SOUTH, splitPane_2, -12, SpringLayout.SOUTH, panel_4);
		springLayout_3.putConstraint(SpringLayout.NORTH, splitPane_2, 5, SpringLayout.SOUTH, availableEventsLabel_1);

		eventRoot = new DefaultMutableTreeNode("Root");
		tvEvent = new JTree(eventRoot);
		tvEvent.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(final TreeSelectionEvent e) {
				updateEventTreeSelection();
			}
		});
		tvEvent.setRootVisible(true);
		tvEvent.setCellRenderer(new CustomIconRenderer());
		splitPane_2.setLeftComponent(tvEvent);

		final JPanel panel_5 = new JPanel();
		panel_5.setLayout(new BorderLayout());
		splitPane_2.setRightComponent(panel_5);

		psProp = new PropertySheetPanel();
		psProp.addPropertySheetChangeListener(new PropertyChangeListener() {
			public void propertyChange(final PropertyChangeEvent evt) {
				updatePropertyPage(evt);
			}
		});
		psProp.setSorting(false);
		psProp.setToolTipText("");
		psProp.setName("");
		psProp.setMode(1);
		psProp.setSortingCategories(true);
		psProp.setSortingProperties(false);
		psProp.setRestoreToggleStates(true);
		panel_5.add(psProp, BorderLayout.CENTER);

		final JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BorderLayout());
		tabbedPane.addTab("Simulation Result", null, resultPanel, null);

		final JSplitPane splitPane_1 = new JSplitPane();
		splitPane_1.setDividerSize(4);
		splitPane_1.setDividerLocation(200);
		splitPane_1.setResizeWeight(0);
		resultPanel.add(splitPane_1);

		final JPanel panel_6 = new JPanel();
		springLayout_1 = new SpringLayout();
		panel_6.setLayout(springLayout_1);
		splitPane_1.setLeftComponent(panel_6);

		final JLabel solutionTreeLabel = new JLabel();
		solutionTreeLabel.setText("Solution Tree");
		panel_6.add(solutionTreeLabel);
		springLayout_1.putConstraint(SpringLayout.NORTH, solutionTreeLabel, 5, SpringLayout.NORTH, panel_6);
		springLayout_1.putConstraint(SpringLayout.WEST, solutionTreeLabel, 5, SpringLayout.WEST, panel_6);

		final JScrollPane tvSolutionScrollPane = new JScrollPane();

		tvSolution = new JTree();
		tvSolution.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(final TreeSelectionEvent e) {
				updateSolutionTree(e);
			}
		});
		tvSolution.setModel(tvSolutionModel = new DefaultTreeModel(tvSolutionRoot = new DefaultMutableTreeNode("Branches")));
		tvSolution.setBorder(new LineBorder(SystemColor.activeCaption, 1, false));
		tvSolution.setCellRenderer(tvEvent.getCellRenderer());
		tvSolutionScrollPane.setViewportView(tvSolution);
		panel_6.add(tvSolutionScrollPane);

		springLayout_1.putConstraint(SpringLayout.SOUTH, tvSolutionScrollPane, -5, SpringLayout.SOUTH, panel_6);
		springLayout_1.putConstraint(SpringLayout.NORTH, tvSolutionScrollPane, 5, SpringLayout.SOUTH, solutionTreeLabel);
		springLayout_1.putConstraint(SpringLayout.EAST, tvSolutionScrollPane, -5, SpringLayout.EAST, panel_6);
		springLayout_1.putConstraint(SpringLayout.WEST, tvSolutionScrollPane, 0, SpringLayout.WEST, solutionTreeLabel);

		final JTabbedPane tabbedPane_1 = new JTabbedPane();
		tabbedPane_1.setTabPlacement(SwingConstants.BOTTOM);
		splitPane_1.setRightComponent(tabbedPane_1);

		final JPanel panel = new JPanel();
		springLayout_5 = new SpringLayout();
		panel.setLayout(springLayout_5);
		tabbedPane_1.addTab("Message Log", null, panel, null);

		final JLabel currentOperationLabel = new JLabel();
		currentOperationLabel.setText("Current progress:");
		panel.add(currentOperationLabel);
		springLayout_5.putConstraint(SpringLayout.SOUTH, currentOperationLabel, 30, SpringLayout.NORTH, panel);
		springLayout_5.putConstraint(SpringLayout.WEST, currentOperationLabel, 5, SpringLayout.WEST, panel);

		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setToolTipText("");
		progressBar.setValue(0);
		panel.add(progressBar);
		springLayout_5.putConstraint(SpringLayout.SOUTH, progressBar, 60, SpringLayout.NORTH, panel);
		springLayout_5.putConstraint(SpringLayout.NORTH, progressBar, 5, SpringLayout.SOUTH, currentOperationLabel);
		springLayout_5.putConstraint(SpringLayout.EAST, progressBar, -10, SpringLayout.EAST, panel);
		springLayout_5.putConstraint(SpringLayout.WEST, progressBar, 5, SpringLayout.WEST, panel);

		final JScrollPane scrollPane_2 = new JScrollPane();
		panel.add(scrollPane_2);
		springLayout_5.putConstraint(SpringLayout.EAST, scrollPane_2, -10, SpringLayout.EAST, panel);
		springLayout_5.putConstraint(SpringLayout.WEST, scrollPane_2, 5, SpringLayout.WEST, panel);
		springLayout_5.putConstraint(SpringLayout.SOUTH, scrollPane_2, -7, SpringLayout.SOUTH, panel);
		springLayout_5.putConstraint(SpringLayout.NORTH, scrollPane_2, 5, SpringLayout.SOUTH, progressBar);

		txtLpgLog = new JTextArea();
		txtLpgLog.setEditable(false);
		scrollPane_2.setViewportView(txtLpgLog);

		progressLabel = new JLabel();
		progressLabel.setText("");
		panel.add(progressLabel);
		springLayout_5.putConstraint(SpringLayout.EAST, progressLabel, 276, SpringLayout.EAST, currentOperationLabel);
		springLayout_5.putConstraint(SpringLayout.WEST, progressLabel, 5, SpringLayout.EAST, currentOperationLabel);
		springLayout_5.putConstraint(SpringLayout.SOUTH, progressLabel, 30, SpringLayout.NORTH, panel);
		springLayout_5.putConstraint(SpringLayout.NORTH, progressLabel, 14, SpringLayout.NORTH, panel);

		final JPanel panelPDDL = new JPanel();
		panelPDDL.setLayout(new BorderLayout());
		tabbedPane_1.addTab("PDDL & Solution", null, panelPDDL, null);

		final JScrollPane scrollPane_3 = new JScrollPane();
		panelPDDL.add(scrollPane_3, BorderLayout.CENTER);

		txtSolPddlText = new JTextArea();
		txtSolPddlText.setMargin(new Insets(2, 2, 2, 2));
		txtSolPddlText.setText("test");
		txtSolPddlText.setFont(new Font("Courier New", Font.PLAIN, 12));
		txtSolPddlText.setEditable(false);
		scrollPane_3.setViewportView(txtSolPddlText);

		simulationChart = new MP_SimulationChart();
		tabbedPane_1.addTab("Chart", null, simulationChart, null);
		//
		updateEventTypeSelection();

		cmdStartStop = new JButton();
		cmdStartStop.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				setSimulating(!simulating);
			}
		});
		cmdStartStop.setText("Start Simulation");
		add(cmdStartStop);
		springLayout.putConstraint(SpringLayout.SOUTH, cmdStartStop, -5, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.NORTH, cmdStartStop, -33, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, cmdStartStop, 145, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.WEST, cmdStartStop, 5, SpringLayout.WEST, this);

		loading = false;
		updateButtonState();
	}

	@SuppressWarnings("deprecation")
	public void setupContext(IAppContext context) {
		eventTypeSplitPane.setDividerLocation(eventTypeSplitPane.getHeight() - 200);

		Context = context.createChildContext("MP_EventPanel");
		Context.addService(new EventInstanceProvider());
		Context.addService(new MessageOutput() {
			@Override
			public void write(final String category, String message) {
				SwingUtilities.invokeLater(new ParamaterizedRunnable<String>(message) {
					@Override
					public void run() {
						// if (txtLpgLog.getText().length() > 100)
						if (category != null)
							txtLpgLog.setText(txtLpgLog.getText() + parameter);
					}
				});

			}

			@Override
			public void write(String category, String format, Object... args) {
				write(category, String.format(format, args));
			}
		});

		
		simulationChart.setContext(Context);
		simulationChart.setSolutionTree(tvSolutionRoot);
		
		PddlModel model = context.getService(PddlModel.class);
		if (model != null) {
			modelListener = new ModelListener(model);
			Context.addService(modelListener);
		}

		buildEventList();

		tvEventModel = (DefaultTreeModel) tvEvent.getModel();

		PropertyEditorRegistry editorRegistry = psProp.getEditorRegistry();

		editorRegistry.setContext(Context);

		editorRegistry.registerEditor(PddlModel.Goal.class, new JEventType.NamedListPropertyEditor<PddlModel.Goal>(model.Goals));
		editorRegistry.registerEditor(PddlModel.Actor.class, new JEventType.NamedListPropertyEditor<PddlModel.Actor>(model.Actors));
		editorRegistry.registerEditor(JEventType.EventTimeAction.class, new JEventType.EnumPropertyEditor(JEventType.EventTimeAction.class, JEventType.EventTimeAction.values()));
		editorRegistry.registerEditor(JEventType.EventTimeType.class, new JEventType.EnumPropertyEditor(JEventType.EventTimeType.class, JEventType.EventTimeType.values()));

	}

	protected void updateButtonState() {
		if (loading)
			return;
		cmdAddEvent.setEnabled(!simulating && lbEventType.getSelectedValue() != null);
		cmdRemoveEvent.setEnabled(!simulating && getSelectedEventInstance() != null);
		cmdStartStop.setText(simulating ? "Stop Simulation" : "Start Simulation");
	}

	public void dispose() {
		modelListener.unregisterListener();
	}

	private void buildEventList() {
		IEventTypeProvider provider = Context.getService(IEventTypeProvider.class);
		lbEventTypeModel.clear();
		for (JEventType ev : provider.getEventTypes()) {
			lbEventTypeModel.addElement(ev);
		}
	}

	protected void updateEventTypeSelection() {
		JEventType sel = (JEventType) lbEventType.getSelectedValue();
		if (sel != null) {
			txtEventDescription.setText(sel.getDescription());
		} else {
			txtEventDescription.setText("");
		}
		updateButtonState();
	}

	// khi nhan phim Add
	protected void addEventInstance() {
		JEventType evType = (JEventType) lbEventType.getSelectedValue();
		if (evType != null) {
			JEventInstance evt = new JEventInstance(evType);
			EventTreeNode child = new EventTreeNode(evt);
			if (eventRoot.getChildCount() > 0) {
				EventTreeNode lastChild = (EventTreeNode) eventRoot.getLastChild();
				evt.setTime(lastChild.getEventInstance().getTime() + 1);
			} else
				evt.setTime(1);
			tvEventModel.insertNodeInto(child, eventRoot, eventRoot.getChildCount());
			// eventRoot.add(child);
			// tvEventModel.reload(eventRoot);
		}
	}

	protected void removeSelectedEvent() {
		TreePath path = tvEvent.getSelectionPath();
		if (path != null && path.getPathCount() > 1) {
			EventTreeNode node = (EventTreeNode) path.getPathComponent(1);
			tvEventModel.removeNodeFromParent(node);
		}
	}

	// goi khi 1 even tren tree duoc chon
	// event ben panel trai, duoc dat len PropertyGrid o ben phai
	protected void updateEventTreeSelection() {
		JEventInstance ins = getSelectedEventInstance();
		
		// props: chua tat ca cac property cua event ma se hien thi len grid
		ArrayList<Property> props = new ArrayList<Property>();
		if (ins != null) {
			for (JEventType.Parameter param : ins.Type.Parameters) {
				// Property p = new
				// JEventInstance.JEventInstanceProperty(param);

				// goi ham nay de tao property tuong ung cho cai param do
				Property p = param.createProperty(ins);

				p.setValue(ins.getParamValue(param.getName()));
				props.add(p);
			}
		}
		psProp.setProperties(props.toArray(new Property[0]));
		updateButtonState();
	}

	protected JEventInstance getSelectedEventInstance() {
		TreePath path = tvEvent.getSelectionPath();
		if (path != null && path.getPathCount() > 1) {
			EventTreeNode node = (EventTreeNode) path.getPathComponent(1);
			return node.getEventInstance();
		}
		return null;
	}

	protected Solution getSelectedSolution() {
		TreePath path = tvSolution.getSelectionPath();
		if (path != null) {
			Object comp = path.getPathComponent(path.getPathCount() - 1);
			if (comp instanceof SolutionTreeNode) {
				SolutionTreeNode node = (SolutionTreeNode) comp;
				return (Solution) node.getData().getData();
			}
		}
		return null;
	}

	protected void updatePropertyPage(PropertyChangeEvent evt) {
		Property prop = (Property) evt.getSource();
		try {
			JEventInstance ins = getSelectedEventInstance();
			if (ins != null) {
				if (!simulating) {
					prop.writeToObject(ins);
					if (prop.getName().equalsIgnoreCase(JEventType.TIME)) {

					} else {
						tvEvent.invalidate();
						TreePath path = tvEvent.getSelectionPath();
						tvEventModel.reload((TreeNode) path.getLastPathComponent());
					}
				} else {
					// UIManager.getLookAndFeel().provideErrorFeedback(psProp);
					// prop.setValue(evt.getOldValue());
				}
			}
		} catch (RuntimeException e) {
			// handle PropertyVetoException and restore previous value
			if (e.getCause() instanceof PropertyVetoException) {
				UIManager.getLookAndFeel().provideErrorFeedback(psProp);
				prop.setValue(evt.getOldValue());
			}
		}
	}

	protected void setSimulating(boolean value) {
		if (value) {
			setCursor(busyCursor);
			tabbedPane.setSelectedIndex(1);
			tvSolutionRoot.removeAllChildren();
			tvSolutionModel.reload(tvSolutionRoot);
			txtLpgLog.setText("");
			if (mapNode != null)
				mapNode.clear();
			if (simulThread == null) {
				PddlSolutionProvider solProv = Context.getService(PddlSolutionProvider.class);
				IEventInstanceProvider eventProv = Context.getService(IEventInstanceProvider.class);
				PddlModel model = Context.getService(PddlModel.class);

				if (solProv != null && eventProv != null && model != null && solProv.getSolutions() != null) {
					simul = new EventSimulation(Context, model, eventProv.getEvents(), solProv.getSolutions());
					simul.addProgressListener(new Event<SimulationProgressArg>() {
						@Override
						public void run(Object sender, SimulationProgressArg arg) {
							updateSimulationProgress(arg);
						}
					});
					simul.start();
				}
			}
		} else {
			if (simul != null)
				simul.stop();
			setCursor(defaultCursor);
		}
		simulating = value;
		updateButtonState();

	}

	protected void updateSimulationProgress(SimulationProgressArg arg) {
		try {
			createSolutionTreeNode(arg);
			if (arg.isCompleted() && arg.getEvent() == SimulationEvent.NA) {
				simul = null;
				setSimulating(false);
				simulationChart.createChart();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void createSolutionTreeNode(SimulationProgressArg arg) throws Exception {
		if (mapNode == null)
			mapNode = new HashMap<EventSimulation.Solution, TreeNode>();
		EventSimulation.Solution sol = arg.getSolution();

		if (sol != null) {
			switch (arg.getEvent()) {
			case EVENT_TRIGGERED:
				TreeNode pNode = mapNode.get(sol);
				TreeNodeData data = new TreeNodeData(arg.getMessage(), sol, eventIcon);
				data.setData(arg.getSolution().getModel().generatePddl());
				DefaultMutableTreeNode eventNode = new DefaultMutableTreeNode(data);
				tvSolutionModel.insertNodeInto(eventNode, (MutableTreeNode) pNode, pNode.getChildCount());
				break;
			case VISITED_NODE:
			case VISITING_NODE:
				if (!mapNode.containsKey(sol)) {
					pNode = sol.getParent() != null ? mapNode.get(sol.getParent()) : tvSolutionRoot;
					if (pNode != tvSolutionRoot)
						pNode = pNode.getChildAt(pNode.getChildCount() - 1);
					SolutionTreeNode node = new SolutionTreeNode(sol);
					tvSolutionModel.insertNodeInto(node, (MutableTreeNode) pNode, pNode.getChildCount());
					mapNode.put(sol, node);
				}

			}
		}
		progressBar.setMaximum(arg.getTotal());
		progressBar.setValue(arg.getProgress());
		progressLabel.setText(arg.getMessage());

	}

	protected void updateSolutionTree(TreeSelectionEvent e) {
		TreePath path = tvSolution.getSelectionPath();
		txtSolPddlText.setText("");

		if (path != null) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getPathComponent(path.getPathCount() - 1);
			Object data = node.getUserObject();
			if (data instanceof TreeNodeData) {
				if (((TreeNodeData) data).getData() instanceof PddlProblem) {
					PddlProblem prob = (PddlProblem) ((TreeNodeData) data).getData();
					txtSolPddlText.setText(prob.toString());
				} else if (((TreeNodeData) data).getData() instanceof Solution) {
					Solution sol = (Solution) ((TreeNodeData) data).getData();
					txtSolPddlText.setText(sol.getPddlSolution().toString());
				}
			}
		}
	}

}

