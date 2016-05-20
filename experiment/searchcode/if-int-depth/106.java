package flatFileData;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import calculations.BalanceCalculator;
import dataInterfaces.EarthModel;
import dataInterfaces.Site;
import dataInterfaces.SoilInstance;

import panels.ButtonPanel;
import panels.SitePanel;

import windows.MapWindow;

public class SiteFF implements Site,Serializable{
	
	private String name;
	private final int id;
	private int numWaterTables;
	private int numContaminants;
	private String description;
	private final int xScale;
	private final int yScale;
	private final int zScale;
	private final int testPrice;
	private final int drillPrice;
	//private final EarthModelFF earthModel;
	private int MSL;
	private int budget;
	private double xDim;
	private double yDim;
	private int bottomElevation;
	private SitePanel.SiteType typeOfSite;
	public static int zDim;
	private BufferedImage map;
	//XXX MS 
	//private ImageIcon map;
	private final BufferedImage legend;
	private String siteFileExtension;
	private String siteMapLocationName;
	private String defFileLocationName;
	private InputStream iOStream;
	
	public int getNumWaterTables() {
		return numWaterTables;
	}

	public int getNumContaminants() {
		return numContaminants;
	}

	public int getBottomElevation() {
		return bottomElevation;
	}

	public SitePanel.SiteType getTypeOfSite() {
		return typeOfSite;
	}

	public String getSiteFileExtension() {
		return siteFileExtension;
	}

	public String getDefFileLocationName() {
		return defFileLocationName;
	}

	public static double getSCALE_TO_PIXEL_X_MULTIPLIER() {
		return SCALE_TO_PIXEL_X_MULTIPLIER;
	}

	public static double getSCALE_TO_PIXEL_Y_MULTIPLIER() {
		return SCALE_TO_PIXEL_Y_MULTIPLIER;
	}

	public Scanner getSoilScan() {
		return soilScan;
	}

	public static HashMap<Color, SoilFF> getSoilList() {
		return soilList;
	}

	public ArrayList<BufferedImage> getImages() {
		return images;
	}

	public static double SCALE_TO_PIXEL_X_MULTIPLIER;
	public static double SCALE_TO_PIXEL_Y_MULTIPLIER;
	private Scanner soilScan;
	public static HashMap<Color,SoilFF> soilList = new HashMap<Color,SoilFF>();
	private ArrayList<BufferedImage> images = new ArrayList<BufferedImage>();
	
	private SiteFF(Builder build){
		id = build.id;
		name = build.name;
		description = build.description;
		xScale = build.xScale;
		yScale = build.yScale;
		zScale = build.zScale;
		testPrice = build.testPrice;
		drillPrice = build.drillPrice;
		//earthModel = build.earthModel;
		MSL = build.MSL;
		budget = build.budget;
		xDim = build.xDim;
		yDim = build.yDim;
		zDim = build.zDim;
		map = build.map;
		legend = build.legend;
		
		
	}
	
	public SiteFF(String name, int id, SitePanel.SiteType typeOfSite)
	{
		//System.out.println("SiteFF - name = " + name + " id = " + id);
		this.typeOfSite = typeOfSite;
		xScale = 196/8000;
		yScale = 178/8000;
		//MS 3/20/13
		if(name.contains("Fort")){
			zScale = 5;
		}else{
			zScale = 1;
		}
		testPrice = 0;
		drillPrice = 30;
		System.out.println("name of site: " + name);
		if(name.contains("Blytheville"))
		{
			xDim = 196;
			SCALE_TO_PIXEL_X_MULTIPLIER = 196./MapWindow.MAX_COORDINATE;
			yDim = 178;
			SCALE_TO_PIXEL_Y_MULTIPLIER = 178./MapWindow.MAX_COORDINATE;
			zDim = 50;
			siteFileExtension = "BlythevilleSiteFiles";
			defFileLocationName = siteFileExtension + "/BLYTH";
			siteMapLocationName = siteFileExtension + "/BLYTH";
			bottomElevation = 243;
		}
		else if (name.contains("Fort"))
		{
			xDim = 196;
			SCALE_TO_PIXEL_X_MULTIPLIER = 196./MapWindow.MAX_COORDINATE;
			yDim = 178;
			SCALE_TO_PIXEL_Y_MULTIPLIER = 178./MapWindow.MAX_COORDINATE;
			zDim = 88;
			

			siteFileExtension = "FortOrdSiteFiles";
			defFileLocationName = siteFileExtension + "/FTORD";
			siteMapLocationName = siteFileExtension + "/FTORD";
		}
		else if (name.contains("Airport"))
		{
			
			xDim = 478;
			SCALE_TO_PIXEL_X_MULTIPLIER = 99./MapWindow.MAX_COORDINATE_SLAPS;
			yDim = 478;
			SCALE_TO_PIXEL_Y_MULTIPLIER = 99./MapWindow.MAX_COORDINATE_SLAPS;
//			System.out.println("#airportYScale: " + SCALE_TO_PIXEL_Y_MULTIPLIER);
			zDim = 112;
			siteFileExtension = "SlapsSiteFiles";
			defFileLocationName = siteFileExtension + "/SLAPS";
			siteMapLocationName = siteFileExtension + "/SLAPS";
		}
		else if (name.contains("SpringField"))
		{
			xDim = 196;
			SCALE_TO_PIXEL_X_MULTIPLIER = 196./MapWindow.MAX_COORDINATE;
			yDim = 178;
			SCALE_TO_PIXEL_Y_MULTIPLIER = 178./MapWindow.MAX_COORDINATE;
			zDim = 64;
			siteFileExtension = "SpringfieldSiteFiles";
			defFileLocationName = siteFileExtension + "/SPRNG";
			siteMapLocationName = siteFileExtension + "/SPRNG";
		}
		else if (name.contains("Weldon"))
		{
			xDim = 477;
			SCALE_TO_PIXEL_X_MULTIPLIER = 99./MapWindow.MAX_COORDINATE_WELDON;
			yDim = 477;
			SCALE_TO_PIXEL_Y_MULTIPLIER = 99./MapWindow.MAX_COORDINATE_WELDON;
			zDim = 65;
			siteFileExtension = "WeldonSpringSiteFiles";
			defFileLocationName = siteFileExtension + "/WELDN";
			siteMapLocationName = siteFileExtension + "/WELDN";
		}
		if(typeOfSite == SitePanel.SiteType.GEOTECH)
		{
			defFileLocationName = defFileLocationName + ("_G" + id + ".DEF");
			//The Blythevilles are pngs not pngfs
			if(name.contains("Blytheville")){
				siteMapLocationName = siteMapLocationName + ("_G" + id + ".png");
			}else{
				siteMapLocationName = siteMapLocationName + ("_G" + id + ".PNGf");
			}
		}
		else if(typeOfSite == SitePanel.SiteType.HYDRO)
		{
			defFileLocationName = defFileLocationName + ("_H" + id + ".DEF");
			//The blythevilles only use h1 and h2 for 1,3,5 and 2,4,6 respectively
			if(name.contains("Blytheville")){
				System.out.println("Blytheville Hydro!");
				if(id % 2 == 1){
					siteMapLocationName = siteMapLocationName + ("_H1.png");
				}else{
					siteMapLocationName = siteMapLocationName + ("_H2.png");
				}
			}else{
				siteMapLocationName = siteMapLocationName + ("_H" + id + ".PNGf");
			}
		}
		else
		{
			System.out.println("(SiteFF) Error - invalid siteType");
		}
		//System.out.println("SiteFF - defFileLocationName = " + defFileLocationName);
		try {
			System.out.println("site file extension: "+ siteFileExtension);
			//XXX MS 3/5/13
		    //map = ImageIO.read(new File(siteFileExtension + "/SITEMAP.GIF"));
			//System.out.println("siteMapLocationName: " + siteMapLocationName);
			map = ImageIO.read(new File(siteMapLocationName));
			//XXX MS 3/5/13
			//map =  new javax.swing.ImageIcon(getClass().getResource("myimage.jpeg"))
			//iOStream = this.getClass().getClassLoader().getResourceAsStream(siteMapLocationName);
			//map = ImageIO.read(iOStream);
			
		} catch (IOException e)
		{
			map = null;
			System.out.println("Could not find map image");
		}
		legend = null;
		this.id = id;
		this.name = name;
		//earthModel = new EarthModelFF();
		//XXX altered by Mark Shivers 3/3/13
		//InputStream readStream;
		//BufferedReader read;
		FileReader read;
		try {
			read = new FileReader(name);
	    }catch (FileNotFoundException e){
		   System.out.println("Error finding the site description file");
		   read = null;
		}
		Scanner scan = new Scanner(read);
		//try {
			//readStream = getClass().getResourceAsStream(name);
			//read = new BufferedReader(new InputStreamReader(readStream));
		//} catch (ResourceNotFoundException e) {
			//System.out.println("Error finding the site description file");
			//read = null;
		//}
		//Scanner scan = new Scanner(read);
		description = "";

		while(scan.hasNextLine())
		{
			description += scan.nextLine();
		}
		scan.close();
		budget = 10000;
		MSL = 0;
		getDefFileInfo();
		
		//ButtonPanel.updateBalanceField(0);
//		System.out.println("budget: " + budget);
		BalanceCalculator.setBalance(budget);
//		System.out.println("setbalance called in siteff");
		ButtonPanel.updateBalanceField(0);
		for( int i = 1 ; i < zDim ; i++){
			try{
				InputStream is = new BufferedInputStream(
						new FileInputStream(siteFileExtension + "/" + setupLayerFileName(i)+ ".gif"));
				images.add(ImageIO.read(is));
			}catch (IOException e)
			{
				System.out.println("(SoilInstanceFF) Could not find image, temp= " + setupLayerFileName(i));
			}
		}
		/*
		try {
			createSoilList();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}
	
	public SiteFF() {
		xScale = 196/8000;
		yScale = 178/8000;
		if(name.contains("Fort")){
			zScale = 5;
		} else {
			zScale = 1;
		}
		
		testPrice = 0;
		drillPrice = 30;
		xDim = 196;
		yDim = 178;
		zDim = 50;
		try {
		    map = ImageIO.read(new File("TestPics/Topo.JPG"));
		} catch (IOException e)
		{
			map = null;
			System.out.println("Could not find map image");
		}
		legend = null;
		this.id = -1;
		this.name = "null";
		//earthModel = new EarthModelFF();
		
		FileReader read;
		try {
			read = new FileReader(name);
		} catch (FileNotFoundException e) {
			System.out.println("Error finding the site description file");
			read = null;
		}
		Scanner scan = new Scanner(read);
		description = "";

		while(scan.hasNextLine())
		{
			description += scan.nextLine();
		}
		scan.close();
		budget = 10000;
		MSL = 0;
	}
	
	
	private void createSoilList() throws FileNotFoundException
	{
		//moving the soillist to siteff so that the earthmodel is no longer used
		//then each soil will be able to be assigned test values
		//and can be called from the geotestpanel
		FileReader read = new FileReader(siteFileExtension + "/SOILS.DEF");
		soilScan = new Scanner(read);
		
		while(soilScan.hasNextLine())
		{
			//System.out.println("TEST");
			String temp = soilScan.nextLine();
			if(!soilScan.hasNextLine())
				break;
			if(temp.length()<2)
				temp = soilScan.nextLine();
			String name = temp;
			String USCS = extractVariableValueFromStringAndAdvanceReader();
			String desc = extractVariableValueFromStringAndAdvanceReader();
			String sdesc  = extractVariableValueFromStringAndAdvanceReader();
			Color color = convertStringToColor(extractVariableValueFromStringAndAdvanceReader());
			int pattern = Integer.parseInt(extractVariableValueFromStringAndAdvanceReader());
			soilList.put(color, new SoilFF(name, USCS, desc, sdesc, pattern, color.getRGB()));
			/*
			String grad = extractVariableValueFromStringAndAdvanceReader();
			String rock = extractVariableValueFromStringAndAdvanceReader();
			String spt = extractVariableValueFromStringAndAdvanceReader();
			String dd = extractVariableValueFromStringAndAdvanceReader();
			String w  = extractVariableValueFromStringAndAdvanceReader();
			String tf = extractVariableValueFromStringAndAdvanceReader();
			String cc = extractVariableValueFromStringAndAdvanceReader();
			String e = extractVariableValueFromStringAndAdvanceReader();
			String LL  = extractVariableValueFromStringAndAdvanceReader();
			String PI = extractVariableValueFromStringAndAdvanceReader();
			String K = extractVariableValueFromStringAndAdvanceReader();
			String sy = extractVariableValueFromStringAndAdvanceReader();
			String OC = extractVariableValueFromStringAndAdvanceReader();
			*/
			double[] PI = extractTestValuesFromStringAndAdvanceReader();
			double[] LL = extractTestValuesFromStringAndAdvanceReader();
			double[] Dd;
			double[] E;
			double[] W;
			double[] Tf;
			double[] Cc;
			double[] K;
			double[] TOC;
			double[] Sy;
			double[] SPT;
			
		}
	}
	
	private Color convertStringToColor(
			String input) {
		int indexOfStartComma = 0, indexOfEndComma = input.indexOf(',');
		int c1 = Integer.parseInt(input.substring(indexOfStartComma,indexOfEndComma));
		input = input.substring(indexOfEndComma+1, input.length());
		indexOfEndComma = input.indexOf(',');
		int c2 = Integer.parseInt(input.substring(indexOfStartComma,indexOfEndComma));
		input = input.substring(indexOfEndComma+1, input.length());
		int c3 = Integer.parseInt(input.substring(indexOfStartComma,input.length()));
		return new Color(c1,c2,c3);
	}

	private String extractVariableValueFromStringAndAdvanceReader() {
		String temp = soilScan.nextLine();
		String returnString = temp.substring(temp.indexOf('=')+2, temp.length());
		return returnString;
	}
	
	private double[] extractTestValuesFromStringAndAdvanceReader() {
		String line = soilScan.nextLine();
		String returnString = line.substring(line.indexOf('=')+2, line.length());
//		System.out.println(returnString);
		
		
		return null;
	}
	
	public void getDefFileInfo()
	{
		FileReader reader = null;
		try {
			reader = new FileReader(defFileLocationName);
		} catch (FileNotFoundException e) {
			System.out.println("(SiteFF) Error - Cannot read def File");
			e.printStackTrace();
		}
		Scanner scanner = new Scanner(reader);
		Scanner lineScanner;
		
		while(scanner.hasNextLine())
		{
			lineScanner = new Scanner(scanner.nextLine());
			lineScanner.useDelimiter(" = ");
			if(lineScanner.hasNext())
			{
				//Gets the budget for each site
				String nextTerm = lineScanner.next();
				String nextValue = null;
				if(lineScanner.hasNext())
				{
					nextValue = lineScanner.next();
				}
				if((nextTerm.contains("costHydro") || nextTerm.contains("costGeotech")) && Integer.parseInt(nextValue) != 0)
				{
					budget = Integer.parseInt(nextValue);
				}
				if(nextTerm.contains("MSL"))
				{
					MSL = Integer.parseInt(nextValue);
				}
				
				
				//System.out.println(nextTerm);
			}
		}
		scanner.close();
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public int getxScale() {
		return xScale;
	}

	@Override
	public int getyScale() {
		return yScale;
	}

	@Override
	public int getzScale() {
		return zScale;
	}

	@Override
	public int getTestPrice() {
		return testPrice;
	}

	@Override
	public int getDrillPrice() {
		return drillPrice;
	}

	
	
	//public EarthModel getEarthModel() {
	//	return earthModel;
	//}

	@Override
	public int getMSL() {
		return MSL;
	}

	@Override
	public int getBudget() {
		return budget;
	}

	@Override
	public double getxDim() {
		return xDim;
	}

	@Override
	public double getyDim() {
		return yDim;
	}

	@Override
	public int getzDim() {
		return zDim;
	}

	@Override
	public BufferedImage getMap() {
		ColorModel cm = map.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = map.copyData(null);
		return new BufferedImage(cm, raster, isAlphaPremultiplied, null);

	}

	//TODO defensive copy
	@Override
	public BufferedImage getLegend() {
		return legend;
	}

	/**
	 * Modified Builder pattern.
	 * THis seems like a hack... refactor
	 * @author anhill
	 *
	 */
	public static class Builder {
		//required parameters
		private int id = -9999;
		private String name = null;
		private String description = null;
		private int xScale = -9999;
		private int yScale = -9999;
		private int zScale = -9999;
		private int testPrice = -9999;
		private int drillPrice = -9999;
		private EarthModelFF earthModel = null;
		private int MSL = -9999;
		private int budget = -9999;
		private int xDim = -9999;
		private int yDim = -9999;
		private int zDim = -9999;
		private BufferedImage map = null;
		private BufferedImage legend = null;
		
		//optional
		//none
		
		public Builder(){}
		
		public Builder id(int val)
		{ 
			id = val;
			return this;
			
		}
		
		public Builder name(String val)
		{ 
			//name = val;
			name = "blah";
			return this;
			
		}
		
		public Builder description(String val)
		{
			description = val;
			return this;
			
		}
		
		public Builder xScale(int val)
		{
			xScale = val;
			return this;
			
		}
		
		public Builder yScale(int val)
		{
			yScale = val;
			return this;
			
		}
		
		public Builder zScale(int val)
		{
			zScale = val;
			return this;
			
		}
		
		public Builder testPrice(int val)
		{
			testPrice = val;
			return this;
			
		}
		
		public Builder drillPrice(int val)
		{
			drillPrice = val;
			return this;
			
		}
		
		
		public Builder earthmodel(EarthModelFF val)
		{
			earthModel = val;
			return this;
			
		}
		
		public Builder MSL(int val)
		{
			MSL = val;
			return this;
			
		}
		
		public Builder budget(int val)
		{
			budget = val;
			return this;
			
		}
		
		public Builder xDim(int val)
		{
			xDim = val;
			return this;
			
		}
		
		public Builder yDim(int val)
		{
			yDim = val;
			return this;
			
		}
		
		public Builder zDim(int val)
		{
			zDim = val;
			return this;
			
		}
		
		public Builder map(BufferedImage val)
		{
			map = val;
			return this;
		}
		
		public Builder legend(BufferedImage val)
		{
			legend = val;
			return this;
			
		}
		
		public SiteFF build() throws IllegalStateException{
			/*
			if(id==-9999){
				throw new IllegalStateException("id not set");
			}*/
			if(name==null){
				throw new IllegalStateException("name not set");
			}
			if(description==null){
				throw new IllegalStateException("description not set");
			}
			if(xScale==-9999){
				throw new IllegalStateException("xScale not set");
			}
			if(yScale==-9999){
				throw new IllegalStateException("yScale not set");
			}
			if(zScale==-9999){
				throw new IllegalStateException("zScale not set");
			}
			if(testPrice==-9999){
				throw new IllegalStateException("testPrice not set");
			}
			if(drillPrice==-9999){
				throw new IllegalStateException("drillPrice not set");
			}
			
			if(earthModel==null){
				throw new IllegalStateException("earthModel not set");
			}
			if(MSL==-9999){
				throw new IllegalStateException("MSL not set");
			}
			if(budget==-9999){
				throw new IllegalStateException("budget not set");
			}
			if(xDim==-9999){
				throw new IllegalStateException("xDim not set");
			}
			if(yDim==-9999){
				throw new IllegalStateException("yDim not set");
			}
			if(zDim==-9999){
				throw new IllegalStateException("zDim not set");
			}/*
			if(map==null){
				throw new IllegalStateException("map not set");
			}
			if(legend==null){
				throw new IllegalStateException("legend not set");
			}*/
			return new SiteFF(this);
		}	
		
	}
	

	public ArrayList<SoilInstance> getSoilInstanceList(int xPos, int yPos, int finalDepth) {
		System.out.println("#getSoilInstanceList yPos: " + yPos);
		finalDepth++;
		int finalDepthInitial = finalDepth;
		ArrayList<SoilInstance> newList = new ArrayList<SoilInstance>();
		int yPixel = (int) ((MapWindow.MAX_COORDINATE-yPos)*SiteFF.SCALE_TO_PIXEL_Y_MULTIPLIER);
		int xPixel = (int)(xPos*SiteFF.SCALE_TO_PIXEL_X_MULTIPLIER);
		if(name.contains("SLAPS") || name.contains("Airport")){
			yPixel = (int) ((MapWindow.MAX_COORDINATE_SLAPS-yPos)*SiteFF.SCALE_TO_PIXEL_Y_MULTIPLIER);
		}else if(name.contains("Weldon")){
			yPixel = (int) ((MapWindow.MAX_COORDINATE_WELDON-yPos)*SiteFF.SCALE_TO_PIXEL_Y_MULTIPLIER);
		}else {
			yPixel = (int) ((MapWindow.MAX_COORDINATE-yPos)*SiteFF.SCALE_TO_PIXEL_Y_MULTIPLIER);
		}
		
		
		
		System.out.println("Drilling in image pixel "+xPixel+" "+yPixel);
		
		Color currColor = new Color(images.get(0).getRGB(xPixel,yPixel));
		Color lastColor = currColor;
		
		int startDepth = 1;
		int airDepth = 0;
		
		for(int depth = 2; depth <= finalDepth ; depth++)
		{
			
			if(depth==zDim){
				
				currColor = new Color(images.get(depth-2).getRGB(xPixel,yPixel));
				SoilFF s = BoringFF.soilList.get(siteFileExtension+lastColor.getRGB());
				newList.add(new SoilInstanceFF(xPos,yPos, startDepth - airDepth ,depth,s));
				break;
			}
			currColor = new Color(images.get(depth-1).getRGB(xPixel,yPixel));
			//System.out.println("currColor"+lastColor.getRed()+" "+lastColor.getGreen()+" "+lastColor.getBlue());
				//lastColor = currColor;
			if((!currColor.equals(lastColor)) || depth==(finalDepth))
			{
				SoilFF s = BoringFF.soilList.get(siteFileExtension+lastColor.getRGB());
				if(s==null)
				{
					System.out.println("Color not found at "+this.name+" "+lastColor.getRed()+" "+lastColor.getGreen()+" "+lastColor.getBlue());
					continue;
				}
				if(s.getShortDescription().equalsIgnoreCase("Air")){
					airDepth = depth-1;
					startDepth = depth;
					finalDepth=airDepth+finalDepthInitial;
					lastColor = currColor;
				}else{
					//if(depth<(finalDepth))
						newList.add(new SoilInstanceFF(xPos,yPos,startDepth-airDepth,depth-1-airDepth,s));
					//else
					//{
					//	newList.add(new SoilInstanceFF(xPos,yPos,startDepth-airDepth,finalDepth,s));
					//}
					startDepth = depth;
					lastColor = currColor;
				}		
			}
		}
		/*
		currColor = new Color(images.get(finalDepth).getRGB(xPixel,yPixel));
		SoilFF s = BoringFF.soilList.get(currColor);
		newList.add(new SoilInstanceFF(xPos,yPos,startDepth-airDepth,finalDepth,s));
		*/
		
		return newList;
	}
	
	private String setupLayerFileName (int depth)
	{
		String temp = "";
		String depthString = depth +"";
		if(depthString.length()<2)
			depthString = "0"+depth;
		if(siteFileExtension.contains("Blytheville"))
		{
			temp = "layer" + depthString;
		}
		else if (siteFileExtension.contains("Fort"))
		{
			temp = depthString;
		}
		else if (siteFileExtension.contains("Slaps"))
		{
			temp = "Slaps" + depthString;
		}
		else if (siteFileExtension.contains("Springfield"))
		{
			temp = "layer" + depthString;
		}
		else if (siteFileExtension.contains("Weldon"))
		{
			temp = "layer" + depthString;
		}
		return temp;
	}

}

