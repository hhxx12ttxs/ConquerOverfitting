package stimulusgen;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import util.Vector3D;

public class SineWaveShape{
	public int width =1200;
	public double amplitude;
	public double frequency;
	public double rotation;
	
	public int x;
	public int y;
	public Color color;
	
	public double start;

	//private int sizeFactor;
	public SineWaveShape(int x, int y,Color color,  double amplitude, double frequency, double rotation, double start) {
		this.x = x; 
		this.y = y;
		this.color = color;
		this.amplitude = amplitude;
		this.frequency = frequency;
		this.rotation = rotation;
		this.start = start;
	}
	
	
	private ArrayList<Double> createApproximation()
	{
		ArrayList<Double> approx = new ArrayList<Double>();
		
		double stepX =  Math.PI;
		double x=0;
		double y =0;
		int totalDegree =(int)( this.width / stepX);
		for(int i= 1;i<= totalDegree ;i++)
		{
			x = i* stepX;
			y = this.y + this.amplitude* Math.sin(this.frequency* x + start);
			double radianAngle = this.rotation * Math.PI / 180;
			int transformedX = (int)x-width/2;
			int transformedY = (int)y;
			approx.add(new Double(transformedX));
			approx.add(new Double(transformedY));
			approx.add(0.);
		}
		
		return approx;
	}
	
	ArrayList<Double> approx;
	public ArrayList<Double> getApproximation()
	{
		if (approx == null) approx = createApproximation();
		return approx;
	}
	
	public double distance(SineWaveShape other)
	{
		return CurveDist.distance(this.getApproximation(), other.getApproximation(), 0,"AVG_MEANDIST", "EXACT", 0,0);
	}
	

	void drawApprox(Graphics2D g)
	{
		g.setColor(color);
		ArrayList<Double> c2 = getApproximation();
		
		int n = approx.size()/3;
		
		for (int i=0; i<n-1; i++)
		{
		      int bj_s= i*3;
		      int bj_e= (i+1)*3;

		      Vector3D s = new Vector3D(
		    		  (float) c2.get(bj_s).doubleValue(),
		    		  (float) c2.get(bj_s+1).doubleValue(),
		    		  (float) c2.get(bj_s+2).doubleValue());
		      Vector3D e = new Vector3D(
		    		  (float) c2.get(bj_e).doubleValue(),
		    		  (float) c2.get(bj_e+1).doubleValue(),
		    		  (float) c2.get(bj_e+2).doubleValue());
		      
		      g.drawLine((int)s.x, (int)s.y, (int)e.x, (int)e.y);
		}
	}
	
	public double[] firstPoint()
	{
		double stepX =  Math.PI / 1;
		double x=0;
		double y =0;
		
			x = 1* stepX;
			y = this.y+ this.amplitude* Math.sin(this.frequency* x + start);
			double radianAngle = this.rotation * Math.PI / 180;
			int transformedX = (int)x-width/2;
			int transformedY = (int)y;

			return new double[]{transformedX, transformedY};
		
	}
	
	public double[] lastPoint()
	{
		double stepX =  Math.PI / 1;
		double x=0;
		double y =0;
		
			x = (int)( this.width / stepX)* stepX;
			y = this.y+this.amplitude* Math.sin(this.frequency* x + start);
			double radianAngle = this.rotation * Math.PI / 180;
			int transformedX = (int)x-width/2;
			int transformedY = (int)y;

			return new double[]{transformedX, transformedY};
		
	}
	
	void draw(Graphics2D g)
	{
		g.setColor(color);
		double stepX =  Math.PI / 1;
		int lastX = this.x- this.width/2;
		int lastY = this.y;//- this.width/2;
		double x=0;
		double y =0;
		int totalDegree =(int)( this.width / stepX);
		for(int i= 1;i<= totalDegree ;i++)
		{
			x = i* stepX;
			y = this.y + this.amplitude* Math.sin(this.frequency* x + start);
			double radianAngle = this.rotation * Math.PI / 180;
			
			int transformedX = (int)x-width/2;
			int transformedY = (int)y;
			
			//int transformedX =(int)( (double)this.x+ x *Math.cos(radianAngle)-  y * Math.sin(radianAngle) ) - this.width/2;
			//int transformedY =(int)( (double)this.y+ x *Math.sin(radianAngle)+  y * Math.cos(radianAngle) )- this.width/2;
			if (i != 1)
				g.drawLine(lastX, lastY, transformedX, transformedY);
			lastX = transformedX;
			lastY = transformedY;
		}
		
	}
	
}

