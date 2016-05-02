package flatFileData;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import windows.MapWindow;
import dataInterfaces.Soil;
import dataInterfaces.SoilInstance;

public class SoilInstanceFF implements SoilInstance,Serializable{
	
	private final int id;
	/*
	 
	private final double LL;
	private final double Dd;
	private final double E;
	private final double W;
	private final double Tf;
	private final double  Cc;
	private final double  K;
	private final double TOC;
	private final double Sy;
	private final double SPT;
	private final Boolean rock;
	private final int grad;
	*/
	
	private int startDepth;
	private int endDepth;
	private SoilFF soil;
	private final int xPos;
	private final int yPos;
	//private final int earthModelId;
	//private ArrayList<Image> images;
	public static int currDepth = 1;
	//private static Color lastColor = null;
	//private static int airDepth = 0;
	
	/*
	public SoilInstanceFF(int id, double pI, double lL, double dd, double e,
			double w, double tf, double cc, double k, double tOC, double sy,
			double sPT, Boolean rock, int grad, int startDepth, int endDepth,
			SoilFF soil, int xPos, int yPos, int earthModelId) {
		super();
		this.id = id;
		PI = pI;
		LL = lL;
		Dd = dd;
		E = e;
		W = w;
		Tf = tf;
		Cc = cc;
		K = k;
		TOC = tOC;
		Sy = sy;
		SPT = sPT;
		this.rock = rock;
		this.grad = grad;
		this.startDepth = startDepth;
		this.endDepth = endDepth;
		this.soil = soil;
		this.xPos = xPos;
		this.yPos = yPos;
		this.earthModelId = earthModelId;
	}
	*/


	public SoilInstanceFF(int xPos, int yPos, int startDepth, int endDepth /*SoilFF soil XXX Mark commenting this out till I can figure out what it is*/)
	{
		super();
		this.id = 0;
		this.xPos = xPos;
		this.yPos = yPos;
		this.startDepth = startDepth;
		this.endDepth = endDepth;
		//this.soil = soil;
	}
	public SoilInstanceFF(int xPos, int yPos, int endDepth /*SoilFF soil XXX Mark commenting this out till I can figure out what it is*/)
	{
		super();
		this.id = 0;
		this.xPos = xPos;
		this.yPos = yPos;
		this.endDepth = endDepth;
		//this.soil = soil;
	}
	public SoilInstanceFF(int xPos, int yPos, int startDepth, int endDepth, SoilFF soil)
	{
		super();
		this.id = 0;
		this.xPos = xPos;
		this.yPos = yPos;
		this.startDepth = startDepth;
		this.endDepth = endDepth;
		this.soil = soil;
	}

	/*
	private String setupLayerFileName (int depth)
	{
		String temp = "";
		String depthString = depth +"";
		if(depthString.length()<2)
			depthString = "0"+depth;
		if(SiteFF.siteFileExtension.contains("Blytheville"))
		{
			temp = "layer" + depthString;
		}
		else if (SiteFF.siteFileExtension.contains("Fort"))
		{
			temp = depthString;
		}
		else if (SiteFF.siteFileExtension.contains("Airport"))
		{
			temp = "Slaps" + depthString;
		}
		else if (SiteFF.siteFileExtension.contains("Springfield"))
		{
			temp = "layer" + depthString;
		}
		else if (SiteFF.siteFileExtension.contains("Weldon"))
		{
			temp = "layer" + depthString;
		}
		return temp;
	}*/
	
	@Override
	public int getId() {
		return id;
	}


/*
	@Override
	public double getPI() {
		return PI;
	}



	@Override
	public double getLL() {
		return LL;
	}



	@Override
	public double getDd() {
		return Dd;
	}



	@Override
	public double getE() {
		return E;
	}



	@Override
	public double getW() {
		return W;
	}



	@Override
	public double getTf() {
		return Tf;
	}



	@Override
	public double getCc() {
		return Cc;
	}



	@Override
	public double getK() {
		return K;
	}



	@Override
	public double getTOC() {
		return TOC;
	}



	@Override
	public double getSy() {
		return Sy;
	}



	@Override
	public double getSPT() {
		return SPT;
	}



	@Override
	public Boolean getRock() {
		return rock;
	}



	@Override
	public int getGrad() {
		return grad;
	}

*/
/*
	@Override
	public int getStartDepth() {
		return startDepth;
	}
*/


	@Override
	public int getEndDepth() {
		return endDepth;
	}



	@Override
	public Soil getSoil() {
		return soil;
	}



	@Override
	public int getxPos() {
		return xPos;
	}



	@Override
	public int getyPos() {
		return yPos;
	}


/*
	public int getEarthModelId() {
		return earthModelId;
	}
*/

	@Override
	public int getSiteId() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int getStartDepth() {
		// TODO Auto-generated method stub
		return startDepth;
	}

/*
	public void setImages(ArrayList<Image> images) {
		this.images = images;
	}


	public ArrayList<Image> getImages() {
		return images;
	}
	*/


}
