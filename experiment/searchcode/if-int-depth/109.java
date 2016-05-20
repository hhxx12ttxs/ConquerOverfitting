package panels;

import java.awt.Dimension;
import java.awt.Graphics;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTable;

import calculations.GeologyCalculator;
import dataInterfaces.Soil;
import dataInterfaces.SoilInstance;



//TODO
/*
 * Add a zero in every soil for the PI test A, B = 0, C, D
 * 
 * Add a box to show where the test is being drawn from (ex. a box from 5-6 ft depth)
 */





/**
 * This panel contains the boring log, the associated short information, and the
 * tests performed on the soils on this boring.
 */
public class GeoTestPanel extends JPanel implements Serializable
{
	private static final long serialVersionUID = 1L;
	public static final int X_MARGIN = 30; //Pixels from the horizontal border on the left and Text
	public static final int Y_MARGIN = 20; //Pixels from the vertical border on the top
	private BoringLogDisplay theBoring;
	//private static int maxDepth;
	private int finalDepth;
	private int depthIncrement;
	private int testsToPerform[][];
	private int dataRows;
	private int xCoord;
	private int yCoord;
	private int dataCols;
	private JPanel testPanel;
	private SitePanel.SiteType siteType;
	private final ArrayList<SoilInstance> soilInstanceList;
	private boolean tested = false;
	private JTable testTable = null;
	
	
	//TODO
	/*
	 * The chart is not getting updated at the correct times
	 * currently the screen needs to be refreshed before
	 * the chart gets updated with the correct tests
	 */

	GeoTestPanel(BoringLogDisplay bLog, SitePanel.SiteType type)
	{
		siteType = type;
		theBoring = bLog;
		soilInstanceList = theBoring.getTheBoring().getSoilInstanceList();
		xCoord = theBoring.getxCoord();
		yCoord = theBoring.getyCoord();
		
		finalDepth = theBoring.getFinalDepth();
		//TODO read in the proper depth Increment for each site
		depthIncrement = 5;
		dataRows = finalDepth / depthIncrement;
		if(siteType == SitePanel.SiteType.HYDRO)
		{
			dataCols = 6;
		}
		else if(siteType == SitePanel.SiteType.GEOTECH)
		{
			dataCols = 8;
		}
		//int depth = 0;
		setLayout(null);
		
		//System.out.println("geotestPanel -> dataRows = " + dataRows);
		testsToPerform = new int[dataRows][dataCols];
		createChart();
		this.setMinimumSize(new Dimension(1000, 1000));
		
	}
	
	public int[][] getTestsToPerform(){
		return testsToPerform;
	}
	
	@Override
	public void paintComponent(Graphics g)
	{
		add(theBoring.getLabelWithDepthAndLocation());
		theBoring.createLog(g, BoringLogDisplay.InfoType.SHORT);

		createChart();
	}
	
	/**
	 * Creates the chart that is displayed on the GeoTestPanel
	 * Currently it's not updating at the correct times, the screen needs to be 
	 * refreshed before the chart gets updated.
	 */
	private void createChart()
	{
		
//		System.out.println("createChart CALLED");
//		int testRrows = 0;
//		int testRcols = 0;
//		for(int[] row :testsToPerform){
//			testRcols = 0;
//			for(int i : row){
//				if(i == 1){
//					System.out.println("test found at: " + testRrows + "," + testRcols);
//				}
//				testRcols++;
//			}
//			testRrows++;
//		}
//		System.out.println("testsToPerform is " + testRrows + "by" + testRcols);
//		for(int i = 0; i < dataRows; i++){
//			for(int j = 0; j < dataCols; j++){
//				if(testsToPerform[i][j] == 1){
//					System.out.println("testsToPerform[" + i + "][" + j + "]: " + testsToPerform[i][j]);
//				}
//			}
//		}
		//Creates the row of names of the columns of tests
		String[] testNames = new String[dataCols];
		if(siteType == SitePanel.SiteType.HYDRO){
			testNames[0] = "Depth";
			testNames[1] = "K";
			testNames[2] = "Sy";
			testNames[3] = "OC";
			testNames[4] = "ne";
			testNames[5] = "Pb";
		}else if(siteType == SitePanel.SiteType.GEOTECH){
			testNames[0] = "Depth";
			testNames[1] = "Dd";
			testNames[2] = "w";
			testNames[3] = "Tf";
			testNames[4] = "Cc";
			testNames[5] = "e";
			testNames[6] = "Pi";
			testNames[7] = "LL";
		}
		
		//The object that holds the data (names and values) for the table
		Object[][] data = new Object[dataRows + 1][dataCols];

		//If the site is a Hydro site
		if(siteType == SitePanel.SiteType.HYDRO)
		{
			data[0] = testNames;
			//XXX Mark Shivers 
			//System.out.println("depthIncrement" + depthIncrement);
			for(int i = 1; i < (dataRows+1); i++)
			{
				String currentDepth = (i-1)*depthIncrement + " - " + ((i-1)*depthIncrement+1);
				data[i][0] = currentDepth;
				int intDepth = (i-1)*depthIncrement;
//				System.out.println("intDepth: " + intDepth);
				Soil currentSoil = getSoilFromDepth(intDepth);
				if(currentSoil == null)
				{
					System.out.println("currentSoil was NULL");
				}
				
				
				
				//for(int j = 1; j < dataCols; j++)
				//{
					
					if(testsToPerform[i-1][0] != 0)
					{
						if(currentSoil.getK() != null)
						{
							data[i][1] = GeologyCalculator.performKCalc(currentSoil.getK(), intDepth, xCoord, yCoord);
						}
						else
						{
							data[i][1] = "N/A";
						}

					}
					if(testsToPerform[i-1][1] != 0)
					{
						if(currentSoil.getSy() != null)
						{
							data[i][2] = GeologyCalculator.performCalc(currentSoil.getSy(), intDepth, xCoord, yCoord);
						}
						else
						{
							data[i][2] = "N/A";
						}

					}
					if(testsToPerform[i-1][2] != 0)
					{
						if(currentSoil.getOC() != null)
						{
							data[i][3] = GeologyCalculator.performCalc(currentSoil.getOC(), intDepth, xCoord, yCoord);
						}
						else
						{
							data[i][3] = "N/A";
						}

					}
					if(testsToPerform[i-1][3] != 0)
					{
						if(currentSoil.getSPT() != null)
						{
							data[i][4] = GeologyCalculator.performCalc(currentSoil.getSPT(), intDepth, xCoord, yCoord);
						}
						else
						{
							data[i][4] = "N/A";
						}

					}
					if(testsToPerform[i-1][4] != 0)
					{
						if(currentSoil.getDd() != null)
						{
							data[i][5] = GeologyCalculator.performCalc(currentSoil.getDd(), intDepth, xCoord, yCoord);
						}
						else
						{
							data[i][5] = "N/A";
						}

					}
				//}
			}
		}
		if(siteType == SitePanel.SiteType.GEOTECH)
		{
			data[0] = testNames;
			for(int i = 1; i < (dataRows+1); i++)
			{
				String currentDepth = (i-1)*depthIncrement + " - " + ((i-1)*depthIncrement+1);
				data[i][0] = currentDepth;
				//starts here
				int intDepth = (i-1)*depthIncrement;
				/*
				for(int j = 1; j < dataCols; j++)
				{
					//data[i][j] = j;
					if(testsToPerform[i-1][j-1] != 0)
					{
						data[i][j] = testsToPerform[i-1][j-1];
					}
				}
				*/
				Soil currentSoil = getSoilFromDepth(intDepth);
				if(currentSoil == null)
				{
					System.out.println("currentSoil was NULL");
				}
				if(testsToPerform[i-1][0] != 0)
				{
					//data[i][1] = 1; //Dd 
					//System.out.println(intDepth + " " + xCoord + " " + yCoord);
					if(currentSoil.getDd() != null)
					{
						data[i][1] = GeologyCalculator.performCalc(currentSoil.getDd(), intDepth, xCoord, yCoord);
					}
					else
					{
						data[i][1] = "N/A";
					}
					//data[i][2] = GeologyCalculator.performCalc(currentSoil.getW(), intDepth, xCoord, yCoord); //W
					if(currentSoil.getW() != null)
					{
						data[i][2] = GeologyCalculator.performCalc(currentSoil.getW(), intDepth, xCoord, yCoord);
					}
					else
					{
						data[i][2] = "N/A";
					}
				}
				if(testsToPerform[i-1][1] != 0)
				{
					//data[i][3] = GeologyCalculator.performCalc(currentSoil.getTf(), intDepth, xCoord, yCoord); //Tf
					if(currentSoil.getTf() != null)
					{
						data[i][3] = GeologyCalculator.performCalc(currentSoil.getTf(), intDepth, xCoord, yCoord);
					}
					else
					{
						data[i][3] = "N/A";
					}
				}
				if(testsToPerform[i-1][2] != 0)
				{
					//data[i][4] = GeologyCalculator.performCalc(currentSoil.getCc(), intDepth, xCoord, yCoord); //CC
					if(currentSoil.getCc() != null)
					{
						data[i][4] = GeologyCalculator.performCalc(currentSoil.getCc(), intDepth, xCoord, yCoord);
					}
					else
					{
						data[i][4] = "N/A";
					}
					//data[i][5] = GeologyCalculator.performCalc(currentSoil.getE(), intDepth, xCoord, yCoord); //E
					if(currentSoil.getE() != null)
					{
						data[i][5] = GeologyCalculator.performCalc(currentSoil.getE(), intDepth, xCoord, yCoord);
					}
					else
					{
						data[i][5] = "N/A";
					}
				}
				if(testsToPerform[i-1][3] != 0)
				{
				
					//data[i][6] = GeologyCalculator.performCalc(currentSoil.getPI(), intDepth, xCoord, yCoord); //PI
					if(currentSoil.getPI() != null)
					{
						data[i][6] = GeologyCalculator.performCalc(currentSoil.getPI(), intDepth, xCoord, yCoord);
					}
					else
					{
						data[i][6] = "N/A";
					}
					//data[i][7] = GeologyCalculator.performCalc(currentSoil.getLL(), intDepth, xCoord, yCoord); //LL
					if(currentSoil.getLL() != null)
					{
						data[i][7] = GeologyCalculator.performCalc(currentSoil.getLL(), intDepth, xCoord, yCoord);
					}
					else
					{
						data[i][7] = "N/A";
					}
				}
			}
		}
		if(testTable==null){
			testTable = new JTable(data, testNames){
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;
	
				@Override
				public boolean isCellEditable(int row, int column){
					this.setFocusable(false);
					this.setRowSelectionAllowed(false);
	
					return false;
				}
			};
			testTable.setRowHeight(BoringLogDisplay.VERTICAL_INCREMENT*5);
			testTable.setRowHeight(0, BoringLogDisplay.Y_MARGIN - 5);
			
		
			//testTable.setDoubleBuffered(true);
			//testTable.setBounds(x,y,width,height);
			//testTable.setBounds(330, 5, 310, 800);
			//testTable.setBounds(330, 5, theBoring.getWidth()-360, 800);
			//add(testTable);
			testPanel = new JPanel();
			//testPanel.add(testTable);
			testPanel.add(testTable, JPanel.LEFT_ALIGNMENT);
			//System.out.println(theBoring.getWidth());
			testPanel.setBounds(370, 5, 700, 2000);
			add(testPanel);
		}
		else{
			for(int i=0;i<data.length;i++){
				for(int j=0;j<data[i].length;j++){
						testTable.getModel().setValueAt(data[i][j], i, j);
				}
			}
		}
		
		
		testPanel.revalidate();
		testPanel.repaint();
	}
	
	/**
	 * Sets the tests that are going to be performed and displayed in the GeoTestPanel
	 * will only add new tests, tests that have already been performed will not be taken away.
	 * @param newTests - 2D array of integers, non-zero means the test will be performed.
	 */
	public void setTestsToPerform(int newTests[][])
	{
		System.out.println("setTestsToPerform CALLED");
		tested = true;
		testsToPerform = newTests;
		createChart();
//		repaint();
	}
	
	public boolean hasBeenTested(){
		return tested;
	}
	
	/**
	 * Gets the appropriate soil from a given depth within a boring
	 */
	public Soil getSoilFromDepth(int depth)
	{
		Soil someSoil = null;
		//System.out.println("depth = " + depth);
		for(SoilInstance someSoilInstance : soilInstanceList)
		{
			//System.out.println("startDepth = " + someSoilInstance.getStartDepth() + "  endDepth = " + someSoilInstance.getEndDepth());
			if(depth >= (someSoilInstance.getStartDepth()-1) && depth <= (someSoilInstance.getEndDepth()-1))
			{
				//System.out.println("start = " + someSoil.getStartDepth() + "  end = " + someSoil.getEndDepth());
				//System.out.println("A soil was found");
				someSoil = someSoilInstance.getSoil();
			}
			
		}
		if(someSoil == null)
		{
			System.out.println("somesoil was found to be null....");
		}
		return someSoil;
	}
	
	

}

