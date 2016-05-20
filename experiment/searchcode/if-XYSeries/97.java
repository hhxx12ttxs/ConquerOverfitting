import java.util.*;
import java.awt.Dimension;
import java.io.*;

import javax.swing.JFrame;

import org.jfree.chart.*;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.*;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.jfree.data.xy.*;

class EcoSim extends ApplicationFrame
{
	static int hour;
	static int day;
	
	// Data collection
	static ArrayList<Integer> structureData = new ArrayList<Integer>();
	static DefaultCategoryDataset structureDataset = new DefaultCategoryDataset();
//	static XYSeriesCollection structureDataset = new XYSeriesCollection();
	static ArrayList<Integer> dataFilter = new ArrayList<Integer>();
	static ArrayList<Integer> resourceData = new ArrayList<Integer>();
	static XYSeriesCollection resourceDataset = new XYSeriesCollection();
	static XYSeriesCollection structureXYDataset = new XYSeriesCollection();

	// Resource collecting structures
	static ArrayList<Integer> farms = new ArrayList<Integer>();
	static ArrayList<Integer> mines = new ArrayList<Integer>();
	static ArrayList<Integer> mills = new ArrayList<Integer>();
	static ArrayList<Integer> henges = new ArrayList<Integer>();
	
	// static ArrayList<Integer> resStructures = new ArrayList<Integer>();
	
	// Other structures
	static int treeLevel;
	static ArrayList<Integer> structures = new ArrayList<Integer>();
	
	// Vaults
	static ArrayList<Integer> vaults = new ArrayList<Integer>();
	static int[] storage = new int[7];
	
	// Accumulation rates
	static final int[] FARM_RATE = {60,90,150,300,600,1200,1800,3000};
	static final int[] MINE_RATE = {60,90,150,300,600,1200,1800,3000};
	static final int[] MILL_RATE = {60,90,150,300,600,1200,1800,3000};
	static final int[] HENGE_RATE = {60,90,150,300,600,1200,1800,3000};
	
	// Storage caps
	static final int[] VAULT_CAP = {100,500,1000,7500,50000,500000,1500000,2500000};
	static final int[] FARM_CAP = {60,200,500,1500,4600,15000,30000,60000};
	static final int[] MINE_CAP = {60,200,500,1500,4600,15000,30000,60000};
	static final int[] MILL_CAP = {60,200,500,1500,4600,15000,30000,60000};
	static final int[] HENGE_CAP = {60,200,500,1500,4600,15000,30000,60000};
	
	// Resource constants
	static final int FOOD = 0;
	static final int IRON = 1;
	static final int LUMBER = 2;
	static final int MANA = 3;
	static final int ORBS = 4;
	static final int SPLINTERS = 5;
	
	// Resource names
	static final String[] RES_NAMES = {	"FOOD",
										"IRON",
										"LUMBER",
										"MANA",
										"ORBS",
										"SPLINTERS"
	};
	
	// Structure constants
	static final int FARM = 0;
	static final int MINE = 1;
	static final int MILL = 2;
	static final int HENGE = 3;
	static final int VAULT = 4;
	static final int HOUSE = 5;
	static final int BARRACKS = 6;
	static final int TEMPLE = 7;
	static final int TOWER = 8;
	static final int TREE = 9;
	
	// Structure names
	static final String[] STR_NAMES = {	"Farm",
										"Mine",
										"Mill",
										"Henge",
										"Vault",
										"House",
										"Barracks",
										"Temple",
										"Tower",
										"Tree"};
	
	// Tree level requirements
	static final int[] treeReq = {1,3,0,0,0,1,1,2,3,0};
	
	// Build costs
	static final int[][][] BUILD_COST = {  {{ 0, 0, 10, 0, 0, 0},  // Farm
											{ 0, 0, 15, 0, 0, 0},
											{ 0, 0, 85, 0, 0, 0},
											{ 0, 0, 600, 0, 0, 0},
											{ 0, 1000, 30000, 0, 0, 0},
											{ 0, 10000, 300000, 0, 0, 0},
											{ 0, 20000, 600000, 0, 0, 0},
											{ 0, 400000, 1200000, 0, 0, 0}},
											
										   {{ 0, 0, 75, 0, 0, 0},  // Mine
											{ 0, 0, 150, 0, 0, 0},
											{ 0, 50, 250, 0, 0, 0},
											{ 0, 200, 1000, 0, 0, 0},
											{ 0, 10000, 50000, 0, 0, 0},
											{ 0, 100000, 500000, 0, 0, 0},
											{ 0, 200000, 1000000, 0, 0, 0},
											{ 0, 400000, 2000000, 0, 0, 0}},
										  
										   {{ 0, 0, 10, 0, 0, 0},  // Mill
											{ 0, 0, 15, 0, 0, 0},
											{ 0, 0, 85, 0, 0, 0},
											{ 0, 0, 600, 0, 0, 0},
											{ 0, 1000, 30000, 0, 0, 0},
											{ 0, 10000, 300000, 0, 0, 0},
											{ 0, 20000, 600000, 0, 0, 0},
											{ 0, 400000, 1200000, 0, 0, 0}},
											
										   {{ 0, 0, 10, 0, 0, 0},  // Henge
											{ 0, 0, 15, 0, 0, 0},
											{ 0, 0, 85, 0, 0, 0},
											{ 0, 0, 600, 0, 0, 0},
											{ 0, 1000, 30000, 0, 0, 0},
											{ 0, 10000, 300000, 0, 0, 0},
											{ 0, 20000, 600000, 0, 0, 0},
											{ 0, 400000, 1200000, 0, 0, 0}},
										
										   {{ 0, 0, 20, 0, 0, 0},  // Vault
											{ 0, 0, 50, 0, 0, 0},
											{ 0, 0, 250, 0, 0, 0},
											{ 0, 250, 750, 0, 0, 0},
											{ 0, 1500, 25000, 0, 0, 0},
											{ 0, 15000, 500000, 0, 0, 0},
											{ 0, 30000, 750000, 0, 0, 0},
											{ 0, 500000, 1500000, 0, 0, 0}},
											
										   {{ 0, 0, 25, 0, 0, 0},  // House
											{ 0, 0, 100, 0, 0, 0},
											{ 0, 0, 500, 0, 0, 0},
											{ 0, 300, 1000, 0, 0, 0},
											{ 0, 2000, 50000, 0, 0, 0},
											{ 0, 20000, 500000, 0, 0, 0},
											{ 0, 40000, 1000000, 0, 0, 0},
											{ 0, 750000, 2000000, 0, 0, 0}},
											
										   {{ 0, 0, 40, 0, 0, 0},  // Barracks
											{ 0, 0, 150, 0, 0, 0},
											{ 0, 0, 750, 0, 0, 0},
											{ 0, 500, 10000, 0, 0, 0},
											{ 0, 5000, 50000, 0, 0, 0},
											{ 0, 50000, 750000, 0, 0, 0},
											{ 0, 250000, 1500000, 0, 0, 0},
											{ 0, 1000000, 5000000, 0, 0, 0}},
											
										   {{ 0, 0, 100, 0, 0, 0},  // Temple
											{ 0, 0, 250, 0, 0, 0},
											{ 0, 0, 1000, 0, 0, 0},
											{ 0, 750, 12500, 0, 0, 0},
											{ 0, 7500, 75000, 0, 0, 0},
											{ 0, 75000, 1000000, 0, 0, 0},
											{ 0, 300000, 2000000, 0, 0, 0},
											{ 0, 1500000, 6000000, 0, 0, 0}},
											
										   {{ 50, 25, 50, 50, 0, 0},  // Tower
											{ 100, 50, 100, 100, 0, 0},
											{ 200, 100, 200, 200, 0, 0},
											{ 1000, 500, 1000, 1000, 0, 0},
											{ 10000, 5000, 10000, 10000, 0, 0},
											{ 100000, 50000, 100000, 100000, 0, 0},
											{ 750000, 500000, 750000, 750000, 0, 0},
											{ 1500000, 750000, 1500000, 1500000, 0, 0}},
											
										   {{ 0, 0, 0, 0, 0, 0},  // Tree
											{ 0, 0, 10, 10, 0, 0},
											{ 25, 0, 50, 50, 0, 0},
											{ 250, 0, 500, 500, 0, 0},
											{ 15000, 1000, 17500, 17500, 0, 0},
											{ 200000, 75000, 200000, 200000, 0, 0},
											{ 1000000, 500000, 1000000, 1000000, 0, 0},
											{ 2000000, 1000000, 2000000, 2000000, 0, 0}}};
										
	// Structure build times (in hours)
	static final int[][] BUILD_TIME = {	{ 0, 0, 0, 3, 12, 24, 48, 72}, // Farm
										{ 0, 0, 0, 3, 12, 24, 48, 72}, // Mine
										{ 0, 0, 0, 3, 12, 24, 48, 72}, // Mill
										{ 0, 0, 0, 3, 12, 24, 48, 72}, // Henge
										{ 0, 0, 1, 3, 12, 24, 48, 72}, // Vault
										{ 0, 0, 1, 3, 12, 24, 48, 72}, // House
										{ 0, 0, 3, 7, 12, 24, 72, 120}, // Barracks
										{ 0, 0, 3, 7, 12, 24, 72, 120}, // Temple
										{ 0, 0, 3, 7, 12, 24, 120, 168}, // Tower
										{ 0, 0, 0, 1, 12, 72, 120, 168}}; // Tree
	
	// Structure space costs
	static final int[] BUILD_SPACE = {	12,	// Farm
										12,	// Mine
										12,	// Mill
										12,	// Henge
										9,	// Vault
										9,	// House
										16,	// Barracks
										16,	// Temple
										16,	// Tower
										25};	// Tree	
	static int space = 144;
	static final int CLEAR_SPACE = 9;
	static final int CLEAR_TIME = 2;
	static int clearable = 72;
	static ArrayList<Integer> clearQ = new ArrayList<Integer>();
										
	static ArrayList<Integer> buildQ = new ArrayList<Integer>();
	
	static int collections = 0;
	static int[] spent = {0,0,0,0,0,0};
	
	// Player parameters
	static int activity = 7; // Expected number of logons per week (must be <= 112)
	
	static boolean test = false;
	static int dayLogons = 0;
	static int maxLogons = 0;
	
	public EcoSim(String title) {
		super(title);
//		JFreeChart resChart = outputResourceData();
		CombinedDomainXYPlot plot = new CombinedDomainXYPlot(new NumberAxis("Hour"));
		plot.setGap(20.0);
		XYPlot subplot1 = outputResourceData();
		subplot1.setRangeAxisLocation(AxisLocation.TOP_OR_LEFT);
		XYItemRenderer sub1Renderer = plot.getRenderer();
		if (sub1Renderer instanceof XYLineAndShapeRenderer) {
			XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) sub1Renderer;
			renderer.setBaseShapesVisible(true);
			renderer.setBaseShapesFilled(true);
		}
		XYPlot subplot2 = outputXYStructureData();
		XYItemRenderer sub2Renderer = subplot2.getRenderer();
		subplot2.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
		plot.add(subplot1, 1);
		plot.add(subplot2, 1);
		plot.setOrientation(PlotOrientation.VERTICAL);
		
		JFreeChart resChart = new JFreeChart("CombinedXY",JFreeChart.DEFAULT_TITLE_FONT,plot,true);
		
		ChartPanel resChartPanel = new ChartPanel(resChart);
		resChartPanel.setPreferredSize(new Dimension(500,270));
		setContentPane(resChartPanel);
		
		JFrame strFrame = new JFrame("Structures");
		JFreeChart strChart = outputStructureData();
		ChartPanel strChartPanel = new ChartPanel(strChart);
		strChartPanel.setPreferredSize(new Dimension(500,270));
		strFrame.setContentPane(strChartPanel);
		strFrame.pack();
		strFrame.setVisible(true);
		
//		JFrame strXYFrame = new JFrame("Structures XY");
//		JFreeChart strXYChart = outputXYStructureData();
//		ChartPanel strXYChartPanel = new ChartPanel(strXYChart);
//		strXYChartPanel.setPreferredSize(new Dimension(500,270));
//		strXYFrame.setContentPane(strXYChartPanel);
//		strXYFrame.pack();
//		strXYFrame.setVisible(true);
	}
	
	public static void main(String args[]) {
		int hoursToSim = 0;
		if (args.length > 0) {
			hoursToSim = Integer.parseInt(args[0]);
		}
		if (args.length > 1) {
			activity = Integer.parseInt(args[1]);
		}
		if (args.length > 2) {
			for (int i=2; i<args.length; i++) {
				dataFilter.add(Integer.parseInt(args[i]));
			}
		}
		else {
			for (int i=0; i<STR_NAMES.length; i++) {
				dataFilter.add(i);
			}
		}
		
		init();		
		
		for (int i=0; i<hoursToSim; i++) {
			hour();
		}
		
//		Scanner in = new Scanner(System.in);
//		while (true) {
//			for (int i=0; i<hoursToSim; i++) {
//				hour();
//			}
//			
//			System.out.println("Day: "+day+", Hour: "+hour);
//			System.out.println("Logons: "+collections);
//			System.out.println("Storage: "+Arrays.toString(storage));
//			System.out.println("Spent: "+Arrays.toString(spent));
//			System.out.println("Vaults: "+Arrays.toString(vaults.toArray()));
//			System.out.println("Mills: "+Arrays.toString(mills.toArray()));
//			System.out.println("Henges: "+Arrays.toString(henges.toArray()));
//			System.out.println("Farms: "+Arrays.toString(farms.toArray()));
//			System.out.println("Mines: "+Arrays.toString(mines.toArray()));
//			System.out.println("Other Structures: "+Arrays.toString(structures.toArray()));
//			System.out.println("Build Queue: "+Arrays.toString(buildQ.toArray()));
//			System.out.println("Tree: "+treeLevel);
//			System.out.println("Max logons per day: "+maxLogons);
//			System.out.println("Test: "+test);
//			
//			System.out.print("\nContinue: ");
//			hoursToSim = in.nextInt();
//			if (hoursToSim == 0) {
//				break;
//			}
//			System.out.println();
//		}
		
		EcoSim e = new EcoSim("EcoSim");
		e.pack();
		RefineryUtilities.centerFrameOnScreen(e);
		e.setVisible(true);
	}	
	
	private static XYPlot outputResourceData() {
		XYSeries resSeries[] = new XYSeries[6];
		for (int i=0; i<resSeries.length; i++) {
			resSeries[i] = new XYSeries(RES_NAMES[i]);
		}
		int[] resourceSum = new int[6];
		int hour = 0;
		for (int i=2; i<resourceData.size(); i+=3) {
			resourceSum[resourceData.get(i-2)] += resourceData.get(i-1);
			if (resourceData.get(i)>hour) {
				for (int u=0; u<resSeries.length; u++) {
					resSeries[u].add(hour,(Number)resourceSum[u]);
				}
				hour = resourceData.get(i);
			}
//			resourceSum[resourceData.get(i-2)] += resourceData.get(i-1);
//			resSeries[resourceData.get(i-2)].add((Number)resourceData.get(i), (Number)resourceSum[resourceData.get(i-2)]);
		}
		for (int i=0; i<resSeries.length; i++) {
			resourceDataset.addSeries(resSeries[i]);
		}
		
		return new XYPlot(resourceDataset, null, new NumberAxis("Resources"), new StandardXYItemRenderer());
		
//		JFreeChart chart = ChartFactory.createXYLineChart(
//				"EcoSim Resources",
//				"Hour",
//				"Level",
//				resourceDataset,
//				PlotOrientation.VERTICAL,
//				true,
//				true,
//				false
//		);
//		return chart;
	}
	
	private static JFreeChart outputStructureData() {
		for (int i=2; i<structureData.size(); i+=3) {
			structureDataset.addValue(structureData.get(i-1)+1,STR_NAMES[structureData.get(i-2)], structureData.get(i));
		}
//		XYSeries strSeries[] = new XYSeries[10];
//		for (int i=0; i<strSeries.length; i++) {
//			strSeries[i] = new XYSeries(STR_NAMES[i]);
//		}
//		for (int i=2; i<structureData.size(); i+=3) {
//			strSeries[structureData.get(i-2)].add((Number)structureData.get(i), (Number)structureData.get(i-1));
//		}
//		for (int i=0; i<strSeries.length; i++) {
//			structureDataset.addSeries(strSeries[i]);
//		}
//		
		JFreeChart chart = ChartFactory.createBarChart(
				"EcoSim Structures", 
				"Hour",
				"Level", 
				structureDataset, 
				PlotOrientation.VERTICAL, 
				true, 
				true, 
				false
		);
		
//		CategoryPlot plot = (CategoryPlot) chart.getPlot();
//		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		
		return chart;
	}
	
	private static XYPlot outputXYStructureData() {
		XYSeries strSeries[] = new XYSeries[10];
		for (int i=0; i<strSeries.length; i++) {
			strSeries[i] = new XYSeries(STR_NAMES[i]);
		}
		int[] structureSum = new int[10];
		int hour = 0;
		for (int i=2; i<structureData.size(); i+=3) {
			structureSum[structureData.get(i-2)]++;
			if (structureData.get(i)>hour) {
				for (int u=0; u<strSeries.length; u++) {
					strSeries[u].add(hour,(Number)structureSum[u]);
				}
				hour = structureData.get(i);
			}
		}
		for (int i=0; i<strSeries.length; i++) {
			structureXYDataset.addSeries(strSeries[i]);
		}
		
		return new XYPlot(structureXYDataset, null, new NumberAxis("Level"), new StandardXYItemRenderer());
	}
	
	private static void init() {
		hour = 0;
		day = 0;
		
		treeLevel = 2;
		structureData.add(TREE);
		structureData.add(2);
		structureData.add(0);
		
		mills.add(2);
		mills.add(0);
		structureData.add(MILL);
		structureData.add(2);
		structureData.add(0);
		henges.add(2);
		henges.add(0);
		structureData.add(HENGE);
		structureData.add(2);
		structureData.add(0);
		farms.add(2);
		farms.add(0);
		structureData.add(FARM);
		structureData.add(2);
		structureData.add(0);
		
		vaults.add(1);
		structureData.add(VAULT);
		structureData.add(1);
		structureData.add(0);
		updateStorage();
		
	}
	
	private static void hour() {
		hour += 1;
		if (hour%24 == 0) {
			day += 1;
			if (dayLogons > maxLogons) {
				maxLogons = dayLogons;
			}
			dayLogons = 0;
		}
		
		accumulate();
		construction();
		
		if (hour%24 < 16 && logon()) {
			dayLogons += 1;
			collect();
			action();
		}
	}
	
	private static boolean logon() {
		Random gen = new Random();
		int r = gen.nextInt(112);
		
		if (r<activity) {
			return true;
		} 
		else {
			return false;
		}
	}
	
	private static void action() {
		for (int i=0; i<4; i++) {
			if (storage[i] < treeLevel*100) {
				raise(i);
			}
		}
		
//		for (int i=5; i<7; i++) {
//			raise(i);
//		}
		
		if (treeLevel<7) {
			raise(TREE);
		}
	}
	
	private static void construction() {
		for (int i=2; i<buildQ.size(); i+=3) {
			buildQ.set(i, buildQ.get(i)+1);
			if (buildQ.get(i) >= BUILD_TIME[buildQ.get(i-2)][buildQ.get(i-1)]) {
			
				buildComplete(buildQ.get(i-2), buildQ.get(i-1));
			
				buildQ.remove(i-2);
				buildQ.remove(i-2);
				buildQ.remove(i-2);
				i -= 3;
			}
		}
		for (int i=0; i<clearQ.size(); i++) {
			clearQ.set(i, clearQ.get(i)+1);
			if (clearQ.get(i) >= CLEAR_TIME) {
				space += CLEAR_SPACE;
				clearQ.remove(i);
				i--;
			}
		}
	}
	
	private static void buildComplete(int structure, int level) {
		switch (structure) {
			case FARM:			
				if (level > 0) {
					for (int i=0; i<farms.size(); i+=2) {
						if (farms.get(i) == level*-1) {
							farms.set(i,level);
							break;
						}
					}
				}				
				else {
					farms.add(0);
					farms.add(0);
				}				
				break;
				
			case MINE:			
				if (level > 0) {
					for (int i=0; i<mines.size(); i+=2) {
						if (mines.get(i) == level*-1) {
							mines.set(i,level);
							break;
						}
					}
				}				
				else {
					mines.add(0);
					mines.add(0);				
				}
				break;
				
			case MILL:			
				if (level > 0) {
					for (int i=0; i<mills.size(); i+=2) {
						if (mills.get(i) == level*-1) {
							mills.set(i,level);
							break;
						}
					}
				}				
				else {
					mills.add(0);
					mills.add(0);				
				}
				break;
				
			case HENGE:			
				if (level > 0) {
					for (int i=0; i<henges.size(); i+=2) {
						if (henges.get(i) == level*-1) {
							henges.set(i,level);
							break;
						}
					}
				}				
				else {
					henges.add(0);
					henges.add(0);
				}
				break;
				
			case VAULT:
				if (level > 0) {
					for (int i=0; i<vaults.size(); i++) {
						if (vaults.get(i) == level*-1) {
							vaults.set(i,level);
							updateStorage();
							break;
						}
					}
				}
				else {
					vaults.add(0);
					updateStorage();
				}
				break;
			
			case TREE:
				treeLevel++;
				break;
				
			default:
				if (level > 0) {
					for (int i=0; i<structures.size(); i+=2) {
						if (structures.get(i) == structure && structures.get(i+1) == level*-1) {
							structures.set(i+1,level);
							break;
						}
					}
				}
				else {
					structures.add(structure);
					structures.add(0);
				}
				break;
		}
	}
	
	private static void accumulate() {
		for (int i=0; i<farms.size(); i+=2) {
			int level = farms.get(i);			
			if (level < 0) { continue; }
			
			int stored = farms.get(i+1);
			if (stored + FARM_RATE[level] < FARM_CAP[level]) {
				stored += FARM_RATE[level];
			}
			else {
				stored = FARM_CAP[level];
			}
			farms.set(i+1,stored);
		}
		
		for (int i=0; i<mines.size(); i+=2) {
			int level = mines.get(i);
			if (level < 0) { continue; }
			
			int stored = mines.get(i+1);
			if (level >= 0 && stored + MINE_RATE[level] < MINE_CAP[level]) {
				stored += MINE_RATE[level];
			}
			else {
				stored = MINE_CAP[level];
			}
			mines.set(i+1,stored);
		}
		
		for (int i=0; i<mills.size(); i+=2) {
			int level = mills.get(i);
			if (level < 0) { continue; }
			
			int stored = mills.get(i+1);
			if (level >= 0 && stored + MILL_RATE[level] < MILL_CAP[level]) {
				stored += MILL_RATE[level];
			}
			else {
				stored = MILL_CAP[level];
			}
			mills.set(i+1,stored);
		}
		
		for (int i=0; i<henges.size(); i+=2) {
			int level = henges.get(i);
			if (level < 0) { continue; }
			
			int stored = henges.get(i+1);
			if (level >= 0 && stored + HENGE_RATE[level] < HENGE_CAP[level]) {
				stored += HENGE_RATE[level];
			}
			else {
				stored = HENGE_CAP[level];
			}
			henges.set(i+1,stored);
		}
	}
	
	private static void collect() {
		collections += 1;
		
		updateStorage();
		
		boolean needVault = false;
		
		for (int i=1; i<farms.size(); i+=2) {
			int stored = farms.get(i);
			if (storage[FOOD] + stored < storage[storage.length-1]) {
				storage[FOOD] += stored;
				farms.set(i,0);
				resourceData.add(FOOD);
				resourceData.add(stored);
				resourceData.add(hour);
			}
			else {
				farms.set(i,storage[storage.length-1]-storage[FOOD]);
				storage[FOOD] = storage[storage.length-1];
				resourceData.add(FOOD);
				resourceData.add(storage[storage.length-1]);
				resourceData.add(hour);
				needVault = true;
			}
		}
		
		for (int i=1; i<mills.size(); i+=2) {
			int stored = mills.get(i);
			if (storage[LUMBER] + stored < storage[storage.length-1]) {
				storage[LUMBER] += stored;
				mills.set(i,0);
				resourceData.add(LUMBER);
				resourceData.add(stored);
				resourceData.add(hour);
			}
			else {
				mills.set(i,storage[storage.length-1]-storage[LUMBER]);
				storage[LUMBER] = storage[storage.length-1];
				resourceData.add(LUMBER);
				resourceData.add(storage[storage.length-1]);
				resourceData.add(hour);
				needVault = true;
			}
		}
		
		for (int i=1; i<mines.size(); i+=2) {
			int stored = mines.get(i);
			if (storage[IRON] + stored < storage[storage.length-1]) {
				storage[IRON] += stored;
				mines.set(i,0);
				resourceData.add(IRON);
				resourceData.add(stored);
				resourceData.add(hour);
			}
			else {
				mines.set(i,storage[storage.length-1]-storage[IRON]);
				storage[IRON] = storage[storage.length-1];
				resourceData.add(IRON);
				resourceData.add(storage[storage.length-1]);
				resourceData.add(hour);
				needVault = true;
			}
		}
		
		for (int i=1; i<henges.size(); i+=2) {
			int stored = henges.get(i);
			if (storage[MANA] + stored < storage[storage.length-1]) {
				storage[MANA] += stored;
				henges.set(i,0);
				resourceData.add(MANA);
				resourceData.add(stored);
				resourceData.add(hour);
			}
			else {
				henges.set(i,storage[storage.length-1]-storage[MANA]);
				storage[MANA] = storage[storage.length-1];
				resourceData.add(MANA);
				resourceData.add(storage[storage.length-1]);
				resourceData.add(hour);
				needVault = true;
			}
		}
		
		if (needVault) {
			raise(VAULT);
		}
	}
	
	private static void raise(int structure) {	
		if (treeReq[structure] > treeLevel) {
			return;
		}
	
		int upgradeLevel = 0;
		ArrayList<Integer> levelArray = new ArrayList<Integer>();
		int upgradeIndex = 0;
		
		if (structure == TREE) {
			if (treeLevel < 7)
				upgradeLevel = treeLevel+1;
		}
		else {
			int interval = 2;
			int start = 0;
			
			switch (structure) {
				case FARM:
					levelArray = farms;
					break;
				case MINE:
					levelArray = mines;
					break;
				case MILL:
					levelArray = mills;
					break;
				case HENGE:
					levelArray = henges;
					break;
				case VAULT:
					levelArray = vaults;
					interval = 1;
					break;
				default:
					levelArray = structures;
					start = 1;
					break;
			}
			
			for (int i=start; i<levelArray.size(); i+=interval) {
				int level = levelArray.get(i)+1;
				if (level <= treeLevel && level > upgradeLevel && checkResources(BUILD_COST[structure][level])) {
					upgradeLevel = level;
					upgradeIndex = i;
				}
			}
		}
		
		if (checkResources(BUILD_COST[structure][upgradeLevel])) {
			if (upgradeLevel == 0) {
				if (!checkSpace(structure)) {
					clearLand();
					return;
				}
				space -= BUILD_SPACE[structure];
			}
			
			buildQ.add(structure);
			buildQ.add(upgradeLevel);
			buildQ.add(0);
			
			if (dataFilter.indexOf(structure) >= 0) {
				structureData.add(structure);
				structureData.add(upgradeLevel);
				structureData.add(hour);
			}
			
			if (structure != TREE && upgradeLevel > 0) {
				levelArray.set(upgradeIndex,upgradeLevel*-1);
			}
		}
	}
	
	private static void clearLand() {
		if (clearable > 0) {
			clearQ.add(0);
			clearable--;
		}
	}
	
	private static boolean checkSpace(int structure) {
		if (BUILD_SPACE[structure] <= space) {
			return true;
		}
		return false;
	}
	
	private static boolean checkResources(int[] cost) {		
		for (int i=0; i<cost.length && i<storage.length-1; i++) {
			if (cost[i] > storage[i]) {
				return false;
			}
		}
		
		return true;
	}
	
	private static boolean buy(int[] cost) {
		if (checkResources(cost)) {
			for (int i=0; i<cost.length && i<storage.length-1; i++) {
				storage[i] -= cost[i];
				resourceData.add(i);
				resourceData.add(-1*cost[i]);
				resourceData.add(hour);
				spent[i] += cost[i];
			}
			return true;
		}	
		
		return false;
	}
	
	private static int checkUpgrade(int structure) {
		if (structure == TREE) {
			return treeLevel+1;
		}
		ArrayList<Integer> levelArray;
		int interval = 2;
		
		switch (structure) {
			case FARM:
				levelArray = farms;			
				break;
			case MINE:
				levelArray = mines;
				break;
			case MILL:
				levelArray = mills;
				break;
			case HENGE:
				levelArray = henges;
				break;
			case VAULT:
				levelArray = vaults;
				interval = 1;
				break;
			default:
				return -1;
		}
		
		int upgradeLevel = 0;
		for (int i=0; i<levelArray.size(); i+=interval) {
			int level = levelArray.get(i)+1;
			if (level <= treeLevel && level > upgradeLevel && checkResources(BUILD_COST[structure][level])) {
				upgradeLevel = level;
				test = true;
			}
		}
		
		return upgradeLevel;
	}
	
	private static void updateStorage() {
		int cap = 0;
		for (int i=0; i<vaults.size(); i++) {
			cap += VAULT_CAP[Math.abs(vaults.get(i))];
		}
		storage[storage.length-1] = cap;
	}
}
