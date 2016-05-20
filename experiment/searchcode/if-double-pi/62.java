package flatFileData;

import java.io.Serializable;

import dataInterfaces.Soil;

public class SoilFF implements Soil,Serializable {
	
	private final int id;
	//private final int earthModelId;
	private final String name;
	private final String USCS;
	private final String description;
	private final String shortDescription;
	private final int pattern;
	private final int color;
	//Not sure why but where every other variable has 5 inputs, PI has only 4
	private final double[] PI; //Plasticity Index (geoTech)
	private final double[] LL; //Liquid Limit (geoTech)
	private final double[] Dd; //Dry Density (geoTech)  (Dry bulk density [Pb] in hydro)
	private final double[] E; //Void Ratio (geoTech)
	private final double[] W; //Water content (geoTech)
	private final double[] Tf; //Shear strength (geoTech)
	private final double[] Cc; //Coefficient of Consolidation (geoTech)
	private final double[] K; //Hydraulic Conductivity (hydro)
	private final double[] OC; //Organic Carbon (hydro)
	private final double[] Sy; //Specific Yield  (hydro)
	private final double[] SPT; //Effective Porosity (hydro)
	private boolean isRock;

	
	public SoilFF(String name, String USCS, String description, String shortDescription, int pattern, int color)
	{
		id = 0;
		this.pattern = pattern;
		this.name = name;
		this.USCS = USCS;
		this.description = description;
		this.shortDescription = shortDescription;
		pattern = 0;
		this.color = color;
		
		PI = new double[] {0,0,0,0};
		LL = new double[] {0,0,0,0,0};
		Dd = new double[] {0,0,0,0,0};
		E = new double[] {0,0,0,0,0};
		W = new double[] {0,0,0,0,0};
		Tf = new double[] {0,0,0,0,0};
		Cc = new double[] {0,0,0,0,0};
		K = new double[] {0,0,0,0,0};
		OC = new double[] {0,0,0,0,0};
		Sy = new double[] {0,0,0,0,0};
		SPT = new double[] {0,0,0,0,0};
	}
	
	public SoilFF() {
		id = -1;
		name = "null";
		USCS = "null";
		description = "null";
		shortDescription = "null";
		pattern = -1;
		color = -1;
		
		PI = new double[] {0,0,0,0};
		LL = new double[] {0,0,0,0,0};
		Dd = new double[] {0,0,0,0,0};
		E = new double[] {0,0,0,0,0};
		W = new double[] {0,0,0,0,0};
		Tf = new double[] {0,0,0,0,0};
		Cc = new double[] {0,0,0,0,0};
		K = new double[] {0,0,0,0,0};
		OC = new double[] {0,0,0,0,0};
		Sy = new double[] {0,0,0,0,0};
		SPT = new double[] {0,0,0,0,0};
	}

	public SoilFF(String name, String USCS, String description, String shortDescription, int pattern, int color,
			double[] SPT, double[] Dd, double[] W, double[] Tf, double[] Cc, double[] E, double[] LL, double[] PI,
			double[] K, double[] Sy, double[] OC)
	{
		//currently using this constructor
		id = 0;
		this.pattern = pattern;
		this.name = name;
		this.USCS = USCS;
		this.description = description;
		this.shortDescription = shortDescription;
		pattern = 0;
		this.color = color;
		this.SPT = SPT;
		this.Dd = Dd;
		this.W = W;
		this.Tf = Tf;
		this.Cc = Cc;
		this.E = E;
		this.LL = LL;
		this.PI = PI;
		this.K = K;
		this.Sy = Sy;
		this.OC = OC;
		if(description.contains("shale") || description.contains("siltstone") || description.contains("limestone"))
		{
			this.isRock = true;
		}
		
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
	public String getUSCS() {
		return USCS;
	}



	@Override
	public String getDescription() {
		return description;
	}



	@Override
	public String getShortDescription() {
		return shortDescription;
	}



	@Override
	public int getPattern() {
		return pattern;
	}



	@Override
	public int getColor() {
		return color;
	}

	public double[] getPI() {
		return PI;
	}

	public double[] getLL() {
		return LL;
	}

	public double[] getDd() {
		return Dd;
	}

	public double[] getE() {
		return E;
	}

	public double[] getW() {
		return W;
	}

	public double[] getTf() {
		return Tf;
	}

	public double[] getCc() {
		return Cc;
	}

	public double[] getK() {
		return K;
	}

	public double[] getOC() {
		return OC;
	}

	public double[] getSy() {
		return Sy;
	}

	public double[] getSPT() {
		return SPT;
	}
	public Boolean isRock()
	{
		return isRock;
	}


	
}

