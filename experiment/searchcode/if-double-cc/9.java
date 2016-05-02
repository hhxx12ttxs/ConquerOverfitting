package database2;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import dataInterfaces.Soil;

public class SoilInstanceDAO implements dataInterfaces.SoilInstance{
	
	public static final int UNDECLARED = -9999;
	
	private int id = UNDECLARED;
	private final double PI;
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
	private final int startDepth;
	private final int endDepth;
	private final SoilDAO soil;
	private final int xPos;
	private final int yPos;
	private final int siteId;
	
	private SoilInstanceDAO(SoilInstanceDAO.Builder build){
		id = build.id;
		PI = build.PI;
		LL = build.LL;
		Dd = build.Dd;
		E = build.E;
		W = build.W;
		Tf = build.Tf;
		Cc = build.Cc;
		K = build.K;
		TOC = build.TOC;
		Sy = build.Sy;
		SPT = build.SPT;
		rock = build.rock;
		grad = build.grad;
		startDepth = build.startDepth;
		System.out.println("start in DAO: " + startDepth);
		endDepth = build.endDepth;
		xPos = build.xPos;
		yPos = build.yPos;
		siteId = build.siteId;
		soil = build.soil;
	}
	
	
	
	public SoilInstanceDAO(int id) throws SQLException {
		
		switch(Database.getDatabaseType()){
		case SQLITETEST:
		case SQLITE:
			ResultSet rs = Database.executeQuery("SELECT * FROM soil_instance WHERE id="+id+";");
			
			this.id=(rs.getInt(1));
			siteId=(rs.getInt(2));
			int soilId=(rs.getInt(3));
			Dd=(rs.getDouble(4));
			E=(rs.getDouble(5));
			W=(rs.getDouble(6));
			Tf=(rs.getDouble(7));
			Cc=(rs.getDouble(8));
			K=(rs.getDouble(9));
			TOC=(rs.getDouble(10));
			Sy=(rs.getDouble(11));
			SPT=(rs.getDouble(12));
			rock=(rs.getBoolean(13));
			grad=(rs.getInt(14));
			PI=(rs.getDouble(15));
			LL=(rs.getDouble(16));
			startDepth=(rs.getInt(17));
			System.out.println("start in sqlite: " + startDepth);
			endDepth=(rs.getInt(18));
			xPos=(rs.getInt(19));
			yPos=(rs.getInt(20));
			
			rs.close();
			
			soil = new SoilDAO(soilId);
			break;
			
		default:
			throw new SQLException("Database type not supported");
		}
	}
	
	
	@Override
	public int getId() {
		return id;
	}


	//@Override
	public double getPI() {
		return PI;
	}


	//@Override
	public double getLL() {
		return LL;
	}


	//@Override
	public double getDd() {
		return Dd;
	}


	//@Override
	public double getE() {
		return E;
	}


	//@Override
	public double getW() {
		return W;
	}


	//@Override
	public double getTf() {
		return Tf;
	}


	//@Override
	public double getCc() {
		return Cc;
	}


	//@Override
	public double getK() {
		return K;
	}


	//@Override
	public double getTOC() {
		return TOC;
	}


	//@Override
	public double getSy() {
		return Sy;
	}


	//@Override
	public double getSPT() {
		return SPT;
	}


	//@Override
	public Boolean getRock() {
		return rock;
	}


	//@Override
	public int getGrad() {
		return grad;
	}



	@Override
	public int getStartDepth() {
		return startDepth;
	}


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


	@Override
	public int getSiteId() {
		return siteId;
	}



	public static class Builder {
		//required parameters
		private int id = UNDECLARED;
		private int siteId = UNDECLARED;
		private double PI = UNDECLARED;
		private double LL = UNDECLARED;
		private double Dd = UNDECLARED;
		private double E = UNDECLARED;
		private double W = UNDECLARED;
		private double Tf = UNDECLARED;
		private double  Cc = UNDECLARED;
		private double  K = UNDECLARED;
		private double TOC = UNDECLARED;
		private double Sy = UNDECLARED;
		private double SPT = UNDECLARED;
		private Boolean rock = null;
		private int grad = UNDECLARED;
		private int startDepth = UNDECLARED;
		private int endDepth = UNDECLARED;
		private int xPos = UNDECLARED;
		private int yPos =UNDECLARED;
		private SoilDAO soil = null;
		
		public Builder(){}
		
		//public Builder id(int val)	{	id = val;	return this;	}
		public Builder siteId(int val)	{	siteId = val;	return this;	} 
		public Builder PI(double val)	{	PI = val;	return this;	}
		public Builder LL(double val)	{	LL = val;	return this;	}
		public Builder Dd(double val)	{	Dd = val;	return this;	}
		public Builder E(double val)	{	E = val;	return this;	}
		public Builder W(double val)	{	W = val;	return this;	}
		public Builder Tf(double val)	{	Tf = val;	return this;	}
		public Builder Cc(double val)	{	Cc = val;	return this;	}
		public Builder K(double val)	{	K = val;	return this;	}
		public Builder TOC(double val)	{	TOC = val;	return this;	}
		public Builder Sy(double val)	{	Sy = val;	return this;	}
		public Builder SPT(double val)	{	SPT = val;	return this;	}
		public Builder rock(Boolean val)	{	rock = val;	return this;	}
		public Builder grad(int val)	{	grad = val;	return this;	}
		public Builder startDepth(int val)	{	startDepth = val;	return this;	}
		public Builder endDepth(int val)	{	endDepth = val;	return this;	}
		public Builder xPos(int val)	{	xPos = val;	return this;	}
		public Builder yPos(int val)	{	yPos = val;	return this;	}
		public Builder soil(SoilDAO val)	{	soil = val;	return this;	}
		
		public SoilInstanceDAO build() throws IllegalStateException	{
			if(siteId==UNDECLARED)
				throw new IllegalStateException("earthModelId not set");
			if(PI==UNDECLARED)
				throw new IllegalStateException("PI not set");
			if(LL==UNDECLARED)
				throw new IllegalStateException("LL not set");
			if(Dd==UNDECLARED)
				throw new IllegalStateException("Dd not set");
			if(E==UNDECLARED)
				throw new IllegalStateException("E not set");
			if(W==UNDECLARED)
				throw new IllegalStateException("W not set");
			if(Tf==UNDECLARED)
				throw new IllegalStateException("Tf not set");
			if(Cc==UNDECLARED)
				throw new IllegalStateException("Cc not set");
			if(K==UNDECLARED)
				throw new IllegalStateException("K not set");
			if(TOC==UNDECLARED)
				throw new IllegalStateException("TOC not set");
			if(Sy==UNDECLARED)
				throw new IllegalStateException("Sy not set");
			if(SPT==UNDECLARED)
				throw new IllegalStateException("SPT not set");
			if(rock==null)
				throw new IllegalStateException("rock not set");
			if(grad==UNDECLARED)
				throw new IllegalStateException("grad not set");
			if(startDepth==UNDECLARED)
				throw new IllegalStateException("startDepth not set");
			if(endDepth==UNDECLARED)
				throw new IllegalStateException("endDepth not set");
			if(xPos==UNDECLARED)
				throw new IllegalStateException("grad not set");
			if(yPos==UNDECLARED)
				throw new IllegalStateException("startDepth not set");
			if(soil==null)
				throw new IllegalStateException("endDepth not set");
			
			
			return new SoilInstanceDAO(this);
		}
		
	}
	
	public static ArrayList<SoilInstanceDAO> readBySiteId(int siteId) throws SQLException{
		ArrayList<SoilInstanceDAO> soilList = new ArrayList<SoilInstanceDAO>();
		
		ResultSet rs = Database.executeQuery("SELECT id FROM soil_instance WHERE site_id="+siteId+";");
		while(rs.next()){
			soilList.add(new SoilInstanceDAO(rs.getInt(1)));
		}
		rs.close();
		
		return soilList;
	}



}

