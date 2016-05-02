package flatFileData;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import dataInterfaces.Boring;
import dataInterfaces.Contaminant;
import dataInterfaces.EarthModel;
import dataInterfaces.Sample;
import dataInterfaces.Site;
import dataInterfaces.SoilInstance;
import dataInterfaces.WaterTable;

public class BoringFF implements Boring, Serializable {

	private final int id;
	private final int siteInstanceId;
	private final int xPos;
	private final int yPos;
	private final int depth;
	private int wellStartDepth;
	private int wellEndDepth;
	public static final HashMap<String, SoilFF> soilList = new HashMap<String, SoilFF>();
	// private transient Scanner scan;
	// private transient Scanner soilScan;
	// private final ArrayList<Sample> testList;
	// private final ArrayList<Contaminant> contaminantList;
	// private final ArrayList<WaterTable> waterTableList;
	private final ArrayList<SoilInstance> soilInstanceList;

	/*
	 * public BoringFF(int siteInstanceId, int xPos, int yPos, int depth, int
	 * wellStartDepth, int wellEndDepth, EarthModelFF earthModel,
	 * ArrayList<SampleFF> testList){ this.siteInstanceId = siteInstanceId;
	 * /*this.waterTableList = earthModel.getWaterTableList(xPos, yPos);
	 * this.contaminantList = earthModel.getContaminantList(xPos, yPos);
	 * this.soilInstanceList = earthModel.getSoilInstanceList(xPos, yPos,
	 * depth); this.id = -9999; this.xPos = xPos; this.yPos = yPos; this.depth =
	 * depth; this.wellStartDepth = wellStartDepth; this.wellEndDepth =
	 * wellEndDepth; //this.testList = testList; }
	 */
	/*
	 * public BoringFF(int id, int xPos, int yPos, int depth, EarthModel
	 * earthModel) { this.id = id; this.xPos = xPos; this.yPos = yPos;
	 * this.soilInstanceList = ((EarthModelFF)
	 * earthModel).getSoilInstanceList(xPos, yPos, depth);
	 * //this.soilInstanceList = ((SiteFF) site).getSoilInstanceList(xPos, yPos,
	 * depth); this.depth = depth; this.siteInstanceId = 0; this.wellStartDepth
	 * = 0; this.wellEndDepth = 0; }
	 */

	public BoringFF(int id, int xPos, int yPos, int depth, Site site) {
		// This one being used
		// soilList = new HashMap<Color,SoilFF>();

		String fileExtension = ((SiteFF) site).getSiteFileExtension();

		try {
			createSoilList(fileExtension);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.id = id;
		this.xPos = xPos;
		this.yPos = yPos;
		// this.soilInstanceList = ((EarthModelFF)
		// earthModel).getSoilInstanceList(xPos, yPos, depth);
		this.soilInstanceList = ((SiteFF) site).getSoilInstanceList(xPos, yPos,
				depth);
		this.depth = depth;
		this.siteInstanceId = 0;
		this.wellStartDepth = 0;
		this.wellEndDepth = 0;

	}

	private void createSoilList(String siteFileExtension)
			throws FileNotFoundException {
		//XXX edited By Mark Shivers on 3/3/13
		//InputStream readStream = getClass().getResourceAsStream(siteFileExtension + "/SOILS.DEF");
		//BufferedReader read = new BufferedReader(new InputStreamReader(readStream));
		
		FileReader read = new FileReader(siteFileExtension + "/SOILS.DEF");
//		System.out.println(siteFileExtension);
		Scanner soilScan = new Scanner(read);
		String temp;
//		System.out.println("testing line 97 boringff");
		while (soilScan.hasNextLine())  /*(temp = read.readLine()) != null)*/ {
			// System.out.println("TEST");
			temp = soilScan.nextLine();
//			System.out.println(temp);
			if (temp.length() < 2){
				continue;
			}
			String name = temp;
			String USCS = extractVariableValueFromStringAndAdvanceReader(soilScan);
			String desc = extractVariableValueFromStringAndAdvanceReader(soilScan);
			String sdesc = extractVariableValueFromStringAndAdvanceReader(soilScan);
			Color color = convertStringToColor(extractVariableValueFromStringAndAdvanceReader(soilScan));
			if (color == null)
				System.out.println("Failed to grab color from def");
			int pattern = Integer
					.parseInt(extractVariableValueFromStringAndAdvanceReader(soilScan));
			// soilList.put(color, new SoilFF(name, USCS, desc, sdesc, pattern,
			// color.getRGB()));

			String grad = extractVariableValueFromStringAndAdvanceReader(soilScan); // Not
																					// used
																					// in
																					// the
																					// new
																					// version
			String rock = extractVariableValueFromStringAndAdvanceReader(soilScan); // Not
																					// used
																					// in
																					// the
																					// new
																					// version
			double[] spt = extractTestValuesFromStringAndAdvanceReader(soilScan);
			double[] dd = extractTestValuesFromStringAndAdvanceReader(soilScan);
			double[] w = extractTestValuesFromStringAndAdvanceReader(soilScan);
			double[] tf = extractTestValuesFromStringAndAdvanceReader(soilScan);
			double[] cc = extractTestValuesFromStringAndAdvanceReader(soilScan);
			double[] e = extractTestValuesFromStringAndAdvanceReader(soilScan);
			double[] LL = extractTestValuesFromStringAndAdvanceReader(soilScan);
			double[] PI = extractTestValuesFromStringAndAdvanceReader(soilScan);
			double[] K = extractTestValuesFromStringAndAdvanceReader(soilScan);
			double[] sy = extractTestValuesFromStringAndAdvanceReader(soilScan);
			double[] OC = extractTestValuesFromStringAndAdvanceReader(soilScan);
			soilList.put(siteFileExtension + color.getRGB(), new SoilFF(name,
					USCS, desc, sdesc, pattern, color.getRGB(), spt, dd, w, tf,
					cc, e, LL, PI, K, sy, OC));
//			System.out.println(color.getRed() + " " + color.getGreen() + " "
//					+ color.getBlue());

		}
	}

	private Color convertStringToColor(String input) {
		int indexOfStartComma = 0, indexOfEndComma = input.indexOf(',');
		int c1 = Integer.parseInt(input.substring(indexOfStartComma,
				indexOfEndComma));
		input = input.substring(indexOfEndComma + 1, input.length());
		indexOfEndComma = input.indexOf(',');
		int c2 = Integer.parseInt(input.substring(indexOfStartComma,
				indexOfEndComma));
		input = input.substring(indexOfEndComma + 1, input.length());
		int c3 = Integer.parseInt(input.substring(indexOfStartComma,
				input.length()));
		return new Color(c1, c2, c3);
	}

	private String extractVariableValueFromStringAndAdvanceReader(
			Scanner soilScan) {
		String temp = soilScan.nextLine();
		String returnString = temp.substring(temp.indexOf('=') + 2,
				temp.length());
		return returnString;
	}
	//XXX remaking the file input system
	private String extractVariableValueFromStringAndAdvanceReader(BufferedReader soilReader){
		String temp;
		String returnString = null;
		try {
			temp = soilReader.readLine();
			returnString = temp.substring(temp.indexOf('=') + 2,
					temp.length());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return returnString;
	}

	private double[] extractTestValuesFromStringAndAdvanceReader(
			Scanner soilScan) {
		String line = soilScan.nextLine();
		// System.out.println("nextLine =     " + line);
		String returnString = line.substring(line.indexOf('=') + 2,
				line.length());
		// System.out.println(returnString);

		if (returnString.contains("N/A")) {
			double[] returnSet = null;
			return returnSet;
		} else {
			Scanner lineScanner = new Scanner(returnString);
			lineScanner.useDelimiter(", ");
			double[] returnSet = { -10000, -10000, -10000, -10000, -10000 };

			int index = 0;
			while (lineScanner.hasNext()) {

				String nextValue = lineScanner.next();
				// System.out.println(nextValue);
				returnSet[index] = Double.parseDouble(nextValue);
				index++;
				// System.out.println(nextValue);
			}
			// MS Closing the Scanner may fix some stability issues
			lineScanner.close();
			return returnSet;
		}
	}
	
	private double[] extractTestValuesFromStringAndAdvanceReader(
			BufferedReader soilReader) {
		String line = null;
		try {
			line = soilReader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println("nextLine =     " + line);
		String returnString = line.substring(line.indexOf('=') + 2,
				line.length());
		// System.out.println(returnString);

		if (returnString.contains("N/A")) {
			double[] returnSet = null;
			return returnSet;
		} else {
			Scanner lineScanner = new Scanner(returnString);
			lineScanner.useDelimiter(", ");
			double[] returnSet = { -10000, -10000, -10000, -10000, -10000 };

			int index = 0;
			while (lineScanner.hasNext()) {

				String nextValue = lineScanner.next();
				// System.out.println(nextValue);
				returnSet[index] = Double.parseDouble(nextValue);
				index++;
				// System.out.println(nextValue);
			}
			// MS Closing the Scanner may fix some stability issues
			lineScanner.close();
			return returnSet;
		}
	}

	public BoringFF(int id, int siteInstanceId, int xPos, int yPos, int depth,
			int wellStartDepth, int wellEndDepth, EarthModelFF earthModel,
			ArrayList<SampleFF> testList) {
		this.id = id;
		this.siteInstanceId = siteInstanceId;
		/*
		 * this.waterTableList = earthModel.getWaterTableList(xPos, yPos);
		 * this.contaminantList = earthModel.getContaminantList(xPos, yPos);
		 */
		this.soilInstanceList = earthModel.getSoilInstanceList(xPos, yPos,
				depth);
		this.xPos = xPos;
		this.yPos = yPos;
		this.depth = depth;
		this.wellStartDepth = wellStartDepth;
		this.wellEndDepth = wellEndDepth;
		// this.testList = testList;
	}

	public BoringFF() {
		xPos = 1;
		yPos = 1;
		siteInstanceId = -1;
		soilInstanceList = null;
		id = -9999;
		depth = 10;
		wellStartDepth = -1;
		wellEndDepth = -1;
	}

	@Override
	public int getId() {
		return id;
	}

	public int getSiteInstanceId() {
		return siteInstanceId;
	}

	@Override
	public int getxPos() {
		return xPos;
	}

	public int getYpos() {
		return yPos;
	}

	@Override
	public int getDepth() {
		return depth;
	}

	@Override
	public int getWellStartDepth() {
		return wellStartDepth;
	}

	@Override
	public int getWellEndDepth() {
		return wellEndDepth;
	}

	/*
	 * public ArrayList<Sample> getTestList() { ArrayList<Sample> newList = new
	 * ArrayList<Sample>(); for(Sample t:testList){ newList.add(t);//immutable }
	 * return newList; }
	 */

	/*
	 * public void addTest(Sample sample){ testList.add(sample); }
	 */

	/*
	 * public ArrayList<Contaminant> getContaminantList() {
	 * ArrayList<Contaminant> cList = new ArrayList<Contaminant>();
	 * 
	 * for(Contaminant c : contaminantList) cList.add(c); return cList; }
	 */

	/*
	 * public ArrayList<WaterTable> getWaterTableList() { ArrayList<WaterTable>
	 * cList = new ArrayList<WaterTable>();
	 * 
	 * for(WaterTable c : waterTableList) cList.add(c); return cList; }
	 */

	@Override
	public ArrayList<SoilInstance> getSoilInstanceList() {
		return soilInstanceList;
	}

	@Override
	public void setWellStartDepth(int wellStartDepth) {
		this.wellStartDepth = wellStartDepth;
	}

	@Override
	public void setWellEndDepth(int wellEndDepth) {
		this.wellEndDepth = wellEndDepth;
	}

	@Override
	public int getBoringNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ArrayList<Contaminant> getContaminantList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBoringNumber(int number) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getSiteId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getyPos() {
		return yPos;
	}

	@Override
	public ArrayList<Sample> getSampleList() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addSample(Sample sample) {
		// TODO Auto-generated method stub

	}

	@Override
	public int write() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void delete() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public int getWorkingSiteId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public ArrayList<WaterTable> getWaterTableList() {
		// TODO Auto-generated method stub
		return null;
	}
}

