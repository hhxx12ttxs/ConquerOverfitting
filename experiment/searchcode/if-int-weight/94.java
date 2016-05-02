package graph;

import generator.Generator;
import generator.Graph;
import items.ConnItem;
import items.Item;
import items.ItemsFactory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import algorithms.Alg1;
import algorithms.Alg14;
import algorithms.Alg4;
import algorithms.Alg8;

public class GraphFrame extends JFrame {
	public static final int WIDTH = 800;

	public static final int HEIGHT = 600;

	private JButton blockButton;
	private JButton connButton;
	private JButton procButton;
	private JButton cycleButton;
	private JButton connectivityButton;

	JTabbedPane tabbedPane = new JTabbedPane();

	protected int countOfItems;
	protected int minItem;
	protected int maxItem;
	protected int minConn;
	protected int maxConn;
	protected double koef;

	private JCheckBoxMenuItem isDuplexItem;
	private JCheckBoxMenuItem isAutonomous;

	public void generate() {
		try {
			countOfItems = Integer.parseInt(JOptionPane
					.showInputDialog("Input count of Items"));
			minItem = Integer.parseInt(JOptionPane
					.showInputDialog("min item weight"));
			maxItem = Integer.parseInt(JOptionPane
					.showInputDialog("max item weight"));
			minConn = Integer.parseInt(JOptionPane
					.showInputDialog("min conn weight"));
			maxConn = Integer.parseInt(JOptionPane
					.showInputDialog("max conn weight"));
			koef = Double.parseDouble(JOptionPane.showInputDialog("koef"));
			Graph graph = Generator.generateGraph(countOfItems, minItem,
					maxItem, minConn, maxConn, koef);
			taskCanvas.clearAll();
			taskCanvas.connectionsMap = graph.connectionsMap;
			taskCanvas.items = graph.items;
			taskCanvas.connections.clear();
			for (ConnItem cItem : graph.connections) {
				taskCanvas.addConnection(cItem);
			}
			taskCanvas.repaint();

		} catch (Exception e) {
		}
	}

	public List<system.Task> getOrder(int alg) {
		if (alg == 1) {
//			Alg1 alg1 = new Alg1();
			return Alg1.execute(taskCanvas.items,
					taskCanvas.getConnectivityMatrix2(),
					taskCanvas.getConnectivityMatrix());
		}
		if (alg == 14) {
//			Alg14 alg14 = new Alg14();
			return Alg14.execute(taskCanvas.items);
		}
		if (alg == 8) {
//			Alg8 alg8 = new Alg8();
			return Alg8.execute(taskCanvas.items,
					taskCanvas.getConnectivityMatrix2(),
					taskCanvas.getConnectivityMatrix());
		}
		return null;
	}

	public int getCrytPathLength() {
		int[][] D = taskCanvas.getConnectivityMatrix2();
		int[][] Dm = D;
		int length = D.length;
		for (int m = 0; m < length - 1; m++) {
			int[][] Dtemp = new int[length][length];
			for (int i = 0; i < length; i++) {
				for (int j = 0; j < length; j++) {
					if (i != j) {
						Dtemp[i][j] = max(Dm[i][j],
								sum(Dm[i][m + 1], Dm[m + 1][j]));
					}
				}
			}
			Dm = Dtemp;
		}
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
				if (Dm[i][j] != Integer.MAX_VALUE) {
					Dm[i][j] += taskCanvas.items.get(i).getWeight();
				}
			}
		}

		int lengthOfCryPath = 0;
		for (int i = 0; i < length; i++) {
			if (lengthOfCryPath < getCritPath(Dm, i)) {
				lengthOfCryPath = (int) getCritPath(Dm, i);
			}
		}

		return lengthOfCryPath;
	}

	private static int getCritPath(int[][] dm, int i) {
		int length = dm.length;
		int max = 0;
		for (int j = 0; j < length; j++) {
			if (dm[i][j] != Integer.MAX_VALUE) {
				max = max(max, dm[i][j]);
			}
		}
		return max;
	}

	private static double[][] normalize(int[][] dm) {
		int length = dm.length;
		int max = 0;
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
				if (dm[i][j] != Integer.MAX_VALUE) {
					max = max(max, dm[i][j]);
				}
			}
		}
		double[][] DM = new double[length][length];
		for (int i = 0; i < length; i++) {
			for (int j = 0; j < length; j++) {
				if (dm[i][j] != Integer.MAX_VALUE) {
					DM[i][j] = (dm[i][j] + 0.0) / max;
				} else {
					DM[i][j] = Integer.MAX_VALUE;
				}
			}
		}
		return DM;
	}

	public static int max(int a, int b) {
		if (a == Integer.MAX_VALUE) {
			return b;
		}
		if (b == Integer.MAX_VALUE) {
			return a;
		}
		return a < b ? b : a;
	}

	public static double max(double a, double b) {
		if (a == Integer.MAX_VALUE) {
			return b;
		}
		if (b == Integer.MAX_VALUE) {
			return a;
		}
		return a < b ? b : a;
	}

	public static int sum(int a, int b) {
		if (a == Integer.MAX_VALUE || b == Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		return a + b;
	}

	public void emb5(List<system.Task> tasks, int f, int chanelsCount) {

		// Connectivity Matrix
		int[][] ta = taskCanvas.getConnectivityMatrixForSystem();
		int[][] ts = systemCanvas.getConnectivityMatrixForSystem();
		int[] iw = new int[taskCanvas.items.size()];
		for (int i = 0; i < iw.length; i++) {
			iw[i] = taskCanvas.items.get(i).getWeight();
		}

		int[] pl = new int[systemCanvas.items.size()];
		for (int i = 0; i < pl.length; i++) {
			pl[i] = chanelsCount;// systemCanvas.items.get(i).getWeight();
		}
		system.System system = new system.System(ta, iw, ts, pl, 5,
				isDuplexItem.isSelected(), isAutonomous.isSelected());
		JPanel[] panels = system.executeTasks5(tasks);
		tabbedPane.add("Processors load", panels[0]);
		tabbedPane.add("Transactions", panels[1]);

		int totalWeight = 0;
		for (Item item : taskCanvas.items) {
			totalWeight += item.getWeight();
		}

		double ky = (totalWeight + 0.0) / system.getLoadLength();
		double ke = ky / systemCanvas.items.size();

		double kea = getCrytPathLength() / (system.getLoadLength() + 0.0);
		String format1 = "0.000";

		DecimalFormat fm1 = new DecimalFormat(format1,
				new DecimalFormatSymbols(Locale.US));

		operations.Operations.log(
				"" + totalWeight + '\t' + system.getLoadLength() + '\t'
						+ getCrytPathLength() + '\t' + fm1.format(ky) + '\t'
						+ fm1.format(ke) + '\t' + fm1.format(kea), f);
	}

	public void emb5(List<system.Task> tasks, int f) {

		// Connectivity Matrix
		int[][] ta = taskCanvas.getConnectivityMatrixForSystem();
		int[][] ts = systemCanvas.getConnectivityMatrixForSystem();
		int[] iw = new int[taskCanvas.items.size()];
		for (int i = 0; i < iw.length; i++) {
			iw[i] = taskCanvas.items.get(i).getWeight();
		}

		int[] pl = new int[systemCanvas.items.size()];
		for (int i = 0; i < pl.length; i++) {
			pl[i] = systemCanvas.items.get(i).getWeight();
		}
		system.System system = new system.System(ta, iw, ts, pl, 5,
				isDuplexItem.isSelected(), isAutonomous.isSelected());
		JPanel[] panels = system.executeTasks5(tasks);
		tabbedPane.add("Processors load", panels[0]);
		tabbedPane.add("Transactions", panels[1]);

		int totalWeight = 0;
		for (Item item : taskCanvas.items) {
			totalWeight += item.getWeight();
		}

		double ky = (totalWeight + 0.0) / system.getLoadLength();
		double ke = ky / systemCanvas.items.size();

		double kea = getCrytPathLength() / (system.getLoadLength() + 0.0);
		String format1 = "0.000";

		DecimalFormat fm1 = new DecimalFormat(format1,
				new DecimalFormatSymbols(Locale.US));

		operations.Operations.log(
				"" + totalWeight + '\t' + system.getLoadLength() + '\t'
						+ getCrytPathLength() + '\t' + fm1.format(ky) + '\t'
						+ fm1.format(ke) + '\t' + fm1.format(kea), f);
	}
	
	public void emb4(List<system.Task> tasks, int f, int chanelsCount) {
		// Connectivity Matrix
		int[][] ta = taskCanvas.getConnectivityMatrixForSystem();
		int[][] ts = systemCanvas.getConnectivityMatrixForSystem();
		int[] iw = new int[taskCanvas.items.size()];
		for (int i = 0; i < iw.length; i++) {
			iw[i] = taskCanvas.items.get(i).getWeight();
		}

		int[] pl = new int[systemCanvas.items.size()];
		for (int i = 0; i < pl.length; i++) {
			pl[i] = chanelsCount;// systemCanvas.items.get(i).getWeight();
		}
		system.System system = new system.System(ta, iw, ts, pl, 4,
				isDuplexItem.isSelected(), isAutonomous.isSelected());
		JPanel[] panels = system.executeTasks4(tasks);
		tabbedPane.add("Processors load", panels[0]);
		tabbedPane.add("Transactions", panels[1]);
		System.out.println("********** " + getCrytPathLength());

		int totalWeight = 0;
		for (Item item : taskCanvas.items) {
			totalWeight += item.getWeight();
		}

		double ky = (totalWeight + 0.0) / system.getLoadLength();
		double ke = ky / systemCanvas.items.size();

		double kea = getCrytPathLength() / (system.getLoadLength() + 0.0);
		String format1 = "0.000";

		DecimalFormat fm1 = new DecimalFormat(format1,
				new DecimalFormatSymbols(Locale.US));
		operations.Operations.log(
				"" + totalWeight + '\t' + system.getLoadLength() + '\t'
						+ getCrytPathLength() + '\t' + fm1.format(ky) + '\t'
						+ fm1.format(ke) + '\t' + fm1.format(kea), f);
	}

	public void emb4(List<system.Task> tasks, int f) {
		// Connectivity Matrix
		int[][] ta = taskCanvas.getConnectivityMatrixForSystem();
		int[][] ts = systemCanvas.getConnectivityMatrixForSystem();
		int[] iw = new int[taskCanvas.items.size()];
		for (int i = 0; i < iw.length; i++) {
			iw[i] = taskCanvas.items.get(i).getWeight();
		}

		int[] pl = new int[systemCanvas.items.size()];
		for (int i = 0; i < pl.length; i++) {
			pl[i] = systemCanvas.items.get(i).getWeight();
		}
		system.System system = new system.System(ta, iw, ts, pl, 4,
				isDuplexItem.isSelected(), isAutonomous.isSelected());
		JPanel[] panels = system.executeTasks4(tasks);
		tabbedPane.add("Processors load", panels[0]);
		tabbedPane.add("Transactions", panels[1]);
		System.out.println("********** " + getCrytPathLength());

		int totalWeight = 0;
		for (Item item : taskCanvas.items) {
			totalWeight += item.getWeight();
		}

		double ky = (totalWeight + 0.0) / system.getLoadLength();
		double ke = ky / systemCanvas.items.size();

		double kea = getCrytPathLength() / (system.getLoadLength() + 0.0);
		String format1 = "0.000";

		DecimalFormat fm1 = new DecimalFormat(format1,
				new DecimalFormatSymbols(Locale.US));
		operations.Operations.log(
				"" + totalWeight + '\t' + system.getLoadLength() + '\t'
						+ getCrytPathLength() + '\t' + fm1.format(ky) + '\t'
						+ fm1.format(ke) + '\t' + fm1.format(kea), f);
	}

	public GraphFrame() {
		setSize(WIDTH, HEIGHT);
		setLayout(new BorderLayout());
		setTitle("ПЗКС");

		// Initlize Menu
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		menuBar.add(fileMenu);

		JMenuItem newMenuItem = new JMenuItem("New");
		newMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				taskCanvas.clearAll();
				systemCanvas.clearAll();

				taskCanvas.repaint();
				systemCanvas.repaint();
			}
		});
		fileMenu.add(newMenuItem);

		JMenuItem openMenuItem = new JMenuItem("Open");
		openMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				open();

			}
		});
		fileMenu.add(openMenuItem);

		JMenuItem saveMenuItem = new JMenuItem("Save");
		saveMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				save();

			}
		});
		fileMenu.add(saveMenuItem);

		fileMenu.addSeparator();

		JMenuItem exitMenuItem = new JMenuItem("Quit");
		exitMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);
			}
		});
		fileMenu.add(exitMenuItem);

		JMenu operationsMenu = new JMenu("Test");
		//menuBar.add(operationsMenu);

		JMenuItem checkCyclesItem = new JMenuItem("Cycles");
		checkCyclesItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String mes = operations.Operations.checkForCycles2(taskCanvas
						.getConnactionsArray()) ? "Has cycle(s)"
						: "Doesn't have cycles";
				JOptionPane.showMessageDialog(null, mes);
			}
		});
		operationsMenu.add(checkCyclesItem);

		JMenuItem checkForConnectivity = new JMenuItem("Connectivity");
		checkForConnectivity.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String mes = operations.Operations
						.checkForConnectivity(systemCanvas
								.getConnactionsArray()) ? "Connected"
						: "Not connected";
				JOptionPane.showMessageDialog(null, mes);
			}
		});
		operationsMenu.add(checkForConnectivity);

		JMenu algMenu = new JMenu("Алгоритм");
		//menuBar.add(algMenu);

		JMenuItem alg1Item = new JMenuItem("Алгоритм №1");
		alg1Item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				Alg1 alg1 = new Alg1();
				Alg1.execute(taskCanvas.items,
						taskCanvas.getConnectivityMatrix2(),
						taskCanvas.getConnectivityMatrix());

			}
		});
		algMenu.add(alg1Item);

		JMenuItem alg8Item = new JMenuItem("Алгоритм №8");
		alg8Item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Alg8 alg8 = new Alg8();
				alg8.execute(taskCanvas.items,
						taskCanvas.getConnectivityMatrix2(),
						taskCanvas.getConnectivityMatrix());

			}
		});
		algMenu.add(alg8Item);

		//
		// JMenuItem alg4Item = new JMenuItem("Algorithm 4");
		// alg4Item.addActionListener(new ActionListener() {
		//
		// public void actionPerformed(ActionEvent arg0) {
		// Alg4 alg4 = new Alg4();
		// alg4.execute(taskCanvas.items, taskCanvas
		// .getConnectivityMatrix2(), taskCanvas
		// .getConnectivityMatrix());
		//
		// }
		// });
		// algMenu.add(alg4Item);
		//

		JMenuItem alg14Item = new JMenuItem("Алгоритм №14");
		alg14Item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				Alg14 alg14 = new Alg14();
				alg14.execute(taskCanvas.items);

			}
		});
		algMenu.add(alg14Item);

		JMenu generatorMenu = new JMenu("Генератор");
		//menuBar.add(generatorMenu);

		JMenuItem generateItem = new JMenuItem("Згенерувати");
		generateItem.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				generate();
			}
		});
		generatorMenu.add(generateItem);

		JMenu embeddingMenu = new JMenu("Назначення");
		//menuBar.add(embeddingMenu);

		JMenuItem embedding4Item = new JMenuItem("Алгоритм №4");

		embeddingMenu.add(embedding4Item);

		JMenuItem embedding5Item = new JMenuItem("Алгоритм №5");

		JMenu settingsMenu = new JMenu("Settings");
		menuBar.add(settingsMenu);

		isDuplexItem = new JCheckBoxMenuItem("Duplex");
		settingsMenu.add(isDuplexItem);

		isAutonomous = new JCheckBoxMenuItem("Autonomus");
		settingsMenu.add(isAutonomous);

		setJMenuBar(menuBar);

		embedding5Item.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
//				emb5(getOrder(14), 5);				
				emb5(getOrder(1), 5, 2);

			}
		});
		embedding4Item.addActionListener(new ActionListener() {
			//
			public void actionPerformed(ActionEvent arg0) {
//				emb4(getOrder(14), 5);
				
				emb4(getOrder(1), 4, 1);
			}
		});
		embeddingMenu.add(embedding5Item);
//
//		JMenuItem embeddingAutoItem = new JMenuItem("embedding auto");
//
//		embeddingMenu.add(embeddingAutoItem);
//
//		embeddingAutoItem.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent arg0) {
//				System.out.println("Auto loading");
//				try {
//					// Open the file that is the first
//					// command line parameter
//					FileInputStream fstream = new FileInputStream("E:\\inputs");
//					// Get the object of DataInputStream
//					DataInputStream in = new DataInputStream(fstream);
//					BufferedReader br = new BufferedReader(
//							new InputStreamReader(in));
//					String strLine;
//					// Read File Line By Line
//					while ((strLine = br.readLine()) != null) {
//						// Print the content on the console
//						System.out.println(strLine);
//						StringTokenizer t = new StringTokenizer(strLine, "\t");
//
//						countOfItems = Integer.parseInt(t.nextToken());
//						minItem = Integer.parseInt(t.nextToken());
//						maxItem = Integer.parseInt(t.nextToken());
//						minConn = Integer.parseInt(t.nextToken());
//						maxConn = Integer.parseInt(t.nextToken());
//						koef = Double.parseDouble(t.nextToken());
//
//						System.out.println(countOfItems);
//						System.out.println(minItem);
//						System.out.println(maxItem);
//						System.out.println(minConn);
//						System.out.println(maxConn);
//						System.out.println(koef);
//
//						Graph graph = Generator.generateGraph(countOfItems,
//								minItem, maxItem, minConn, maxConn, koef);
//						taskCanvas.clearAll();
//						taskCanvas.connectionsMap = graph.connectionsMap;
//						taskCanvas.items = graph.items;
//						taskCanvas.connections.clear();
//						for (ConnItem cItem : graph.connections) {
//							taskCanvas.addConnection(cItem);
//						}
//						taskCanvas.repaint();
//						ArrayList<Integer> list = new ArrayList<Integer>();
//						list.add(1);
//						list.add(4);
//						list.add(14);
//
//						for (int a : list) {
//							isDuplexItem.setSelected(false);
//							isAutonomous.setSelected(false);
//							emb4(getOrder(a), a * 10 + 4);
//
//							isDuplexItem.setSelected(true);
//							isAutonomous.setSelected(false);
//							emb5(getOrder(a), a * 10 + 5);
//
//						}
//					}
//					// Close the input stream
//					in.close();
//				} catch (Exception e) {// Catch exception if any
//					System.err.println("Error: " + e.getMessage());
//				}
//
//			}
//		});
//		JMenuItem embeddingAutoItem2 = new JMenuItem("embedding auto2");
//
//		embeddingMenu.add(embeddingAutoItem2);
//
//		embeddingAutoItem2.addActionListener(new ActionListener() {
//
//			public void actionPerformed(ActionEvent arg0) {
//				System.out.println("Auto loading2");
//				try {
//					// Open the file that is the first
//					// command line parameter
//					FileInputStream fstream = new FileInputStream(
//							"D:\\inputs_c");
//					// Get the object of DataInputStream
//					DataInputStream in = new DataInputStream(fstream);
//					BufferedReader br = new BufferedReader(
//							new InputStreamReader(in));
//					String strLine;
//					// Read File Line By Line
//					while ((strLine = br.readLine()) != null) {
//						// Print the content on the console
//						System.out.println(strLine);
//						StringTokenizer t = new StringTokenizer(strLine, "\t");
//
//						countOfItems = Integer.parseInt(t.nextToken());
//						minItem = Integer.parseInt(t.nextToken());
//						maxItem = Integer.parseInt(t.nextToken());
//						minConn = Integer.parseInt(t.nextToken());
//						maxConn = Integer.parseInt(t.nextToken());
//						koef = Double.parseDouble(t.nextToken());
//
//						System.out.println(countOfItems);
//						System.out.println(minItem);
//						System.out.println(maxItem);
//						System.out.println(minConn);
//						System.out.println(maxConn);
//						System.out.println(koef);
//
//						Graph graph = Generator.generateGraph(countOfItems,
//								minItem, maxItem, minConn, maxConn, koef);
//						taskCanvas.clearAll();
//						taskCanvas.connectionsMap = graph.connectionsMap;
//						taskCanvas.items = graph.items;
//						taskCanvas.connections.clear();
//						for (ConnItem cItem : graph.connections) {
//							taskCanvas.addConnection(cItem);
//						}
//						taskCanvas.repaint();
//
//						isDuplexItem.setSelected(false);
//						isAutonomous.setSelected(false);
//						emb5(getOrder(14), 1000, 1);
//
//						isDuplexItem.setSelected(true);
//						isAutonomous.setSelected(false);
//						emb5(getOrder(14), 2000, 1);
//
//						isDuplexItem.setSelected(false);
//						isAutonomous.setSelected(true);
//						emb5(getOrder(14), 3000, 1);
//
//						isDuplexItem.setSelected(true);
//						isAutonomous.setSelected(true);
//						emb5(getOrder(14), 4000, 1);
//
//					}
//					// Close the input stream
//					in.close();
//				} catch (Exception e) {// Catch exception if any
//					System.err.println("Error: " + e.getMessage());
//				}
//
//			}
//		});

		// Init toolbar
		ToolBarActionListener actionListener = new ToolBarActionListener();

		JToolBar toolBar = new JToolBar(JToolBar.VERTICAL);

		JButton cursorButton = new JButton(new javax.swing.ImageIcon(
				"src/resources/cursor.jpg"));
		cursorButton.setActionCommand(null);
		cursorButton.addActionListener(actionListener);
		toolBar.add(cursorButton);

		connButton = new JButton(new javax.swing.ImageIcon("src/resources/conn.jpg"));
		connButton.setActionCommand(Item.CONN_ITEM);
		connButton.addActionListener(actionListener);
		toolBar.add(connButton);

		blockButton = new JButton(new javax.swing.ImageIcon(
				"src/resources/block.jpg"));
		blockButton.setActionCommand(Item.BLOCK_ITEM);
		blockButton.addActionListener(actionListener);
		toolBar.add(blockButton);

		procButton = new JButton(new javax.swing.ImageIcon(
				"src/resources/proc.jpg"));
		procButton.setActionCommand(Item.PROC_ITEM);
		procButton.addActionListener(actionListener);
		toolBar.add(procButton);
		
		cycleButton = new JButton(new javax.swing.ImageIcon("src/resources/cycles.jpg"));
		connectivityButton = new JButton(new javax.swing.ImageIcon("src/resources/connection.jpg"));
		
		toolBar.add(cycleButton);
		toolBar.add(connectivityButton);
		
		cycleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String mes;
				if (tabbedPane.getSelectedIndex()==0)
					mes = operations.Operations.checkForCycles2(taskCanvas
						.getConnactionsArray()) ? "Has cycle(s)"
						: "Doesn't have cycles";
				else
					mes = operations.Operations.checkForCycles2(systemCanvas.getConnactionsArray()) ? "Has cycle(s)"
							: "Doesn't have cycles";
				JOptionPane.showMessageDialog(null, mes);
			}
		});
		

		
		connectivityButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String mes;
				if (tabbedPane.getSelectedIndex()==0)
					mes = operations.Operations.checkForConnectivity(taskCanvas.getConnactionsArray()) ? "Connected"
						: "Not connected";
				else
					mes = operations.Operations.checkForConnectivity(systemCanvas.getConnactionsArray()) ? "Connected"
							: "Not connected";
				JOptionPane.showMessageDialog(null, mes);
			}
		});
		
		
		procButton.setVisible(false);
		  blockButton.setVisible(true);
		  connectivityButton.setVisible(false);
		  cycleButton.setVisible(true);

		add(toolBar, BorderLayout.WEST);

		tabbedPane.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent event) {
				int currentTab = ((JTabbedPane) event.getSource())
						.getSelectedIndex();
				checkButtons(currentTab);
			}
		});

		// Init Panel
		taskCanvas = new TaskCanvas(this);
		MouseAdapter mouseAdapter = new TaskCanvasMouseListener(this);
		taskCanvas.addMouseListener(mouseAdapter);
		taskCanvas.addMouseMotionListener(mouseAdapter);
		taskCanvas.setBackground(Color.WHITE);

		// Init Panel
		systemCanvas = new SystemCanvas(this);
		MouseAdapter sMouseAdapter = new SystemCanvasMouseListener(this);
		systemCanvas.setBackground(Color.WHITE);
		systemCanvas.addMouseListener(sMouseAdapter);
		systemCanvas.addMouseMotionListener(sMouseAdapter);

		tabbedPane.add("Graph", taskCanvas);
		tabbedPane.add("System", systemCanvas);
		
		
		tabbedPane.addChangeListener(new ChangeListener() {
		      public void stateChanged(ChangeEvent e) {
		    	  if (tabbedPane.getSelectedIndex()==0){
		    		  
		    		  procButton.setVisible(false);
		    		  blockButton.setVisible(true);
		    		  connectivityButton.setVisible(false);
		    		  cycleButton.setVisible(true);
		    	  }
		    	  else if (tabbedPane.getSelectedIndex()==1){
		    		  procButton.setVisible(true);
		    		  blockButton.setVisible(false);
		    		  cycleButton.setVisible(false);
		    		  connectivityButton.setVisible(true);
		    	  }
		    		 
		        }
		});

		add(tabbedPane, BorderLayout.CENTER);

	}

	protected void open() {
		taskCanvas.clearAll();
		systemCanvas.clearAll();
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("src/Files"));
		if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File f = chooser.getSelectedFile();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(f);

			Element task = (Element) doc.getElementsByTagName("task").item(0);
			Element system = (Element) doc.getElementsByTagName("system").item(
					0);

			Map<Integer, Item> taskItems = new HashMap<Integer, Item>();
			NodeList itemsList = task.getElementsByTagName("item");
			for (int i = 0; i < itemsList.getLength(); i++) {
				Element itemElement = (Element) itemsList.item(i);
				int id = Integer.parseInt(itemElement.getAttribute("id"));
				String type = itemElement.getAttribute("type");
				int x = Integer.parseInt(itemElement.getAttribute("x"));
				int y = Integer.parseInt(itemElement.getAttribute("y"));
				int weight = Integer.parseInt(itemElement
						.getAttribute("weight"));

				Item item = ItemsFactory.newItem(type);
				item.setId(id);
				item.setX(x);
				item.setY(y);
				item.setWeight(weight);

				taskItems.put(id, item);
				taskCanvas.items.add(item);

			}

			for (int i = 0; i < itemsList.getLength(); i++) {
				Element itemElement = (Element) itemsList.item(i);
				int id = Integer.parseInt(itemElement.getAttribute("id"));
				Item item = taskItems.get(id);
				NodeList connections = itemElement
						.getElementsByTagName("connection");
				for (int j = 0; j < connections.getLength(); j++) {
					Element connElement = (Element) connections.item(j);
					int connId = Integer.parseInt(connElement
							.getAttribute("elementId"));
					int weight = Integer.parseInt(connElement
							.getAttribute("weight"));

					Item connItem = taskItems.get(connId);

					if (taskCanvas.connectionsMap.get(item) == null) {
						ArrayList<Item> items = new ArrayList<Item>();
						taskCanvas.connectionsMap.put(item, items);
					}
					ArrayList<Item> items = taskCanvas.connectionsMap.get(item);
					items.add(connItem);

					ConnItem connection = new ConnItem();
					connection.setStartItem(item);
					connection.setEndItem(connItem);
					connection.setWeight(weight);

					taskCanvas.addConnection(connection);
				}

			}

			for (Item item : taskItems.values()) {
				if (ItemsFactory.sequenceMap.get(item.getType()) == null) {
					Integer seq = new Integer(0);
					ItemsFactory.sequenceMap.put(item.getType(), seq);
				}
				Integer seq = ItemsFactory.sequenceMap.get(item.getType());
				if (seq < item.getId())
					seq = item.getId();
				ItemsFactory.sequenceMap.put(item.getType(), seq);
			}

			Map<Integer, Item> systemItems = new HashMap<Integer, Item>();
			itemsList = system.getElementsByTagName("item");
			for (int i = 0; i < itemsList.getLength(); i++) {
				Element itemElement = (Element) itemsList.item(i);
				int id = Integer.parseInt(itemElement.getAttribute("id"));
				String type = itemElement.getAttribute("type");
				int x = Integer.parseInt(itemElement.getAttribute("x"));
				int y = Integer.parseInt(itemElement.getAttribute("y"));
				int weight = Integer.parseInt(itemElement
						.getAttribute("weight"));

				Item item = ItemsFactory.newItem(type);
				item.setId(id);
				item.setX(x);
				item.setY(y);
				item.setWeight(weight);

				systemItems.put(id, item);
				systemCanvas.items.add(item);

			}

			for (int i = 0; i < itemsList.getLength(); i++) {
				Element itemElement = (Element) itemsList.item(i);
				int id = Integer.parseInt(itemElement.getAttribute("id"));
				Item item = systemItems.get(id);
				NodeList connections = itemElement
						.getElementsByTagName("connection");
				for (int j = 0; j < connections.getLength(); j++) {
					Element connElement = (Element) connections.item(j);
					int connId = Integer.parseInt(connElement
							.getAttribute("elementId"));
					int weight = Integer.parseInt(connElement
							.getAttribute("weight"));
					Item connItem = systemItems.get(connId);

					if (systemCanvas.connectionsMap.get(item) == null) {
						ArrayList<Item> items = new ArrayList<Item>();
						systemCanvas.connectionsMap.put(item, items);
					}
					ArrayList<Item> items = systemCanvas.connectionsMap
							.get(item);
					items.add(connItem);
					ConnItem connection = new ConnItem();
					connection.setStartItem(item);
					connection.setEndItem(connItem);
					connection.setWeight(weight);

					systemCanvas.addConnection(connection);
				}
			}

			for (Item item : taskItems.values()) {
				if (ItemsFactory.sequenceMap.get(item.getType()) == null) {
					Integer seq = new Integer(0);
					ItemsFactory.sequenceMap.put(item.getType(), seq);
				}
				Integer seq = ItemsFactory.sequenceMap.get(item.getType());
				if (seq < item.getId())
					seq = item.getId();
				ItemsFactory.sequenceMap.put(item.getType(), seq);
			}
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		taskCanvas.repaint();
		systemCanvas.repaint();
	}

	protected void save() {

		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new java.io.File("src/Files"));
		if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		File f = chooser.getSelectedFile();
		Transformer t = null;
		try {
			t = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformerFactoryConfigurationError e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		try {
			builder = factory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document doc = builder.newDocument();
		Element root = doc.createElement("config");
		doc.appendChild(root);

		Element task = doc.createElement("task");
		Element system = doc.createElement("system");

		root.appendChild(task);
		root.appendChild(system);

		for (Item item : taskCanvas.items) {
			Element itemElement = doc.createElement("item");
			itemElement.setAttribute("type", item.getType());
			itemElement.setAttribute("x", item.getX() + "");
			itemElement.setAttribute("y", item.getY() + "");
			itemElement.setAttribute("id", item.getId() + "");
			itemElement.setAttribute("weight", item.getWeight() + "");
			ArrayList<Item> endItems = taskCanvas.connectionsMap.get(item);
			if (endItems != null)
				for (Item endItem : endItems) {
					Element connectionElement = doc.createElement("connection");
					connectionElement.setAttribute("elementId", endItem.getId()
							+ "");
					Item connection = taskCanvas.connectivityMap.get(item
							.getId() + "_" + endItem.getId());
					connectionElement.setAttribute("weight",
							connection.getWeight() + "");
					itemElement.appendChild(connectionElement);
				}
			task.appendChild(itemElement);
		}

		for (Item item : systemCanvas.items) {
			Element itemElement = doc.createElement("item");
			itemElement.setAttribute("type", item.getType());
			itemElement.setAttribute("x", item.getX() + "");
			itemElement.setAttribute("y", item.getY() + "");
			itemElement.setAttribute("id", item.getId() + "");
			itemElement.setAttribute("weight", item.getWeight() + "");
			ArrayList<Item> endItems = systemCanvas.connectionsMap.get(item);
			if (endItems != null)
				for (Item endItem : endItems) {
					Element connectionElement = doc.createElement("connection");
					connectionElement.setAttribute("elementId", endItem.getId()
							+ "");
					Item connection = systemCanvas.connectivityMap.get(item
							.getId() + "_" + endItem.getId());
					connectionElement.setAttribute("weight",
							connection.getWeight() + "");
					itemElement.appendChild(connectionElement);
				}
			system.appendChild(itemElement);
		}

		try {
			t.transform(new DOMSource(doc), new StreamResult(
					new FileOutputStream(f)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	protected void checkButtons(int currentTab) {
		if (currentTab == 0) {
			blockButton.setEnabled(true);
			procButton.setEnabled(false);
			currentAction = null;
		} else if (currentTab == 1) {
			blockButton.setEnabled(false);
			procButton.setEnabled(true);
			currentAction = null;
		}

	}

	private class ToolBarActionListener implements ActionListener {

		public void actionPerformed(ActionEvent event) {
			currentAction = event.getActionCommand();
			taskCanvas.clear();
			systemCanvas.clear();
			taskCanvas.currentConnection = null;
			systemCanvas.currentConnection = null;

		}
	}

	/**
	 * These vars are operating with taskCanvas
	 */
	public String currentAction = null;

	TaskCanvas taskCanvas = null;

	SystemCanvas systemCanvas = null;

	/**
	 * Selected Item (like conn, block)
	 */
	public Item currentItem = null;

	public void repaintTaskCanvas() {
		taskCanvas.repaint();

	}

	public void repaintSystemCanvas() {
		systemCanvas.repaint();

	}

	public void clearCanvas() {
		taskCanvas.clear();
		systemCanvas.clear();

	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame =  new GraphFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

}

