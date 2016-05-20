package stimulusgen;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import javax.imageio.ImageIO;

import perspectives.Property;
import perspectives.Viewer2D;
import properties.PColor;
import properties.PInteger;
import properties.PPercent;
import properties.PropertyType;

public class SineWaveStimulusViewer extends Viewer2D{
	public static final String PROPERTY_NAME_MIN_DISTANCE = "Distance.Minimum Distance";
	public static final String PROPERTY_NAME_MAX_DISTANCE = "Distance.Maximum Distance";
	
	public static final String PROPERTY_NAME_SIZE = "Size";
	public static final String PROPERTY_NAME_FORECOLOR = "Forecolor";
	public static final String PROPERTY_NAME_COLOR_DIFFERENCE = "ColorDifference";	
	public static final String PROPERTY_NAME_OBJECT_COUNT = "Object Count";
	
	
	private LabColorGenerator[] labs ;
	
	int taskCurve = 0;
	
	int colorDif = 0;

	public SineWaveStimulusViewer(String name) {
		super(name);
		
		this.labs = LabColorGenerator.generate(8);
		this.loadProperties();
		
		
	}

	private void loadProperties()
	{
		try {
			Property<PInteger> minDistance = new Property<PInteger>(PROPERTY_NAME_MIN_DISTANCE, new PInteger(200));
			this.addProperty(minDistance);
			
			Property<PInteger> maxDistance = new Property<PInteger>(PROPERTY_NAME_MAX_DISTANCE, new PInteger(700));
			this.addProperty(maxDistance);
			
			
			Property<PPercent> amplitude = new Property<PPercent>("Amplitude", new PPercent(1.));
			this.addProperty(amplitude);
			
			Property<PPercent> freq = new Property<PPercent>("Freq", new PPercent(1.));
			this.addProperty(freq);
			
		
		
			Property<PInteger> colorDifference = new Property<PInteger>(PROPERTY_NAME_COLOR_DIFFERENCE, new PInteger(7));
			this.addProperty(colorDifference);
			
			
			Property<PInteger> objectCount = new Property<PInteger>(PROPERTY_NAME_OBJECT_COUNT,new PInteger(10));
			this.addProperty(objectCount);
			
			Property<PInteger> sel = new Property<PInteger>("SEL",new PInteger(1));
			this.addProperty(sel);
			
			Property<PInteger> save = new Property<PInteger>("SaveStimulus", new PInteger(0));
			this.addProperty(save);
			
			
		}
		catch (Exception e) {		
			e.printStackTrace();
		}
	}
	
	private int getPropertyIntValue(String name)
	{
		Property<PInteger> prop = this.getProperty(name);
		return prop.getValue().intValue();
	}
	
	private double getPropertyPercentValue(String name)
	{
		Property<PPercent> prop = this.getProperty(name);
		return prop.getValue().getRatio();
	}

	private StimulusGenPlotter getPlotter(int objectCount, int minDistance, int maxDistance)
	{
		StimulusGenPlotter plotter = new RadialDistancePlotter(objectCount, minDistance, maxDistance);
		
		return plotter;
	}
	public <T extends PropertyType> void propertyUpdated(Property p, T newvalue)
	{
		if (p.getName().equals("SEL"))
		{
			for (int i=1; i<curves.size(); i++)
				curves.get(i).color = Color.black;
			int sc = ((PInteger)newvalue).intValue();
			if (sc < curves.size())
			{
				curves.get(sc).color = Color.blue;
				System.out.println(curves.get(0).distance(curves.get(sc)));
			}
		}
		else if (p.getName().equals("SaveStimulus"))
		{
			this.saveStimulus();
		}
		else if (p.getName().equals(this.PROPERTY_NAME_COLOR_DIFFERENCE))
		{
			colorDif = ((PInteger)newvalue).intValue();
		}
		else
		{
			if (p.getName().equals("Amplitude"))
				this.createStimulus(10, 
						((PPercent)newvalue).getRatio()*350.,
						((PPercent)getProperty("Freq").getValue()).getRatio()*.01, 
						((PInteger)getProperty(PROPERTY_NAME_MIN_DISTANCE).getValue()).intValue(), 
						((PInteger)getProperty(PROPERTY_NAME_MAX_DISTANCE).getValue()).intValue());
			if (p.getName().equals("Freq"))
				this.createStimulus(10, 
						((PPercent)getProperty("Amplitude").getValue()).getRatio()*350.,
						((PPercent)newvalue).getRatio()*.01, 
						((PInteger)getProperty(PROPERTY_NAME_MIN_DISTANCE).getValue()).intValue(), 
						((PInteger)getProperty(PROPERTY_NAME_MAX_DISTANCE).getValue()).intValue());
			if (p.getName().equals(PROPERTY_NAME_MIN_DISTANCE))
				this.createStimulus(10, 
						((PPercent)getProperty("Amplitude").getValue()).getRatio()*350.,
						((PPercent)getProperty("Freq").getValue()).getRatio()*.01, 
						((PInteger)newvalue).intValue(), 
						((PInteger)getProperty(PROPERTY_NAME_MAX_DISTANCE).getValue()).intValue());
			if (p.getName().equals(PROPERTY_NAME_MAX_DISTANCE))
				this.createStimulus(10, 
						((PPercent)getProperty("Amplitude").getValue()).getRatio()*350.,
						((PPercent)getProperty("Freq").getValue()).getRatio()*.01, 
						((PInteger)getProperty(PROPERTY_NAME_MIN_DISTANCE).getValue()).intValue(), 
						((PInteger)newvalue).intValue());
		}
	}

	@Override
	public void render(Graphics2D g) {
		// TODO Auto-generated method stub
		
		g.setColor(Color.white);
		g.fillRect(-1000, -1000, 2000,2000);
		
		int cdif = 6-colorDif;
		if (cdif < 0) cdif = 0;
		if (cdif >= labs.length) cdif = labs.length-1;
		Color col = labs[cdif].getColor();
		
		
		
		
		if (curves.size() == 0)
			return;
		
		double[] fp = curves.get(taskCurve).firstPoint();
		
		g.setFont(g.getFont().deriveFont(25f));
						
		for (int i=0; i<curves.size(); i++)
		{
			if (i != taskCurve)
				curves.get(i).color = col;
			else curves.get(i).color = Color.black;
			
			curves.get(i).draw(g);
			double[] lp = curves.get(i).lastPoint();
			
			g.setColor(Color.DARK_GRAY);
			g.fillOval((int)lp[0]-10, (int)lp[1]-10, 30, 30);
			g.setColor(Color.white);
			g.drawString(""+i, (int)lp[0]-2, (int)lp[1]+13);
		}
		
		
		g.setColor(Color.DARK_GRAY);
		g.fillOval((int)fp[0]-10, (int)fp[1]-10, 30, 30);
		g.setColor(Color.white);
		g.drawString("A", (int)fp[0]-3, (int)fp[1]+13);
		
		g.setColor(Color.red);
		g.drawRect(-625, -475, 1250, 950);
		
		
		

	}

	@Override
	public void simulate() {
		// TODO Auto-generated method stub
		
	}
	
	ArrayList<SineWaveShape> curves = new ArrayList<SineWaveShape>();
	public void createStimulus(int nrcurves, double amplitude, double freq, double minDist, double maxDist)
	{
		curves.clear();
		
	
		while (true)
		{
		while (curves.size() < nrcurves)
		{
			//pick a candidate set of params
			double dy = Math.random();
			double damp = Math.random();
			double dfreq = Math.random();
			double dang = Math.random();
			
			//freq is between 0 and 0.01
			//damp is between 0 and 400
			
		//	double dist = Math.sqrt(dy*dy + dang*dang + (dfreq-nfreq)*(freq-nfreq) + (damp-namp)*(damp-namp));	
			
			
			SineWaveShape sine = new SineWaveShape(0, (int)(dy*150)-100,Color.black, 0.5*(damp+1)*350, 0.5*(dfreq+1)*0.01, dang*Math.PI/2, Math.PI*Math.random());
			
			
			if (curves.size() == 0)
			{
				sine = new SineWaveShape(0, (int)(dy*150)-100,Color.black, amplitude, freq, dang*Math.PI/2, Math.PI*Math.random());				
				curves.add(sine);
			}
			else
			{
				//make sure endpoints are not very close to any other endpoints
				double[] sp = sine.firstPoint();
				double[] ep = sine.lastPoint();
				boolean tooClose = false;
				for (int j=0; j<curves.size(); j++)
				{
					double[] sp2 = curves.get(j).firstPoint();
					double[] ep2 = curves.get(j).lastPoint();
					
					double d1 = Math.sqrt((sp[0]-sp2[0])*(sp[0]-sp2[0]) + (sp[1]-sp2[1])*(sp[1]-sp2[1]));
					double d2 = Math.sqrt((ep[0]-ep2[0])*(ep[0]-ep2[0]) + (ep[1]-ep2[1])*(ep[1]-ep2[1]));
					
					if (d1 < 20 || d2 < 20)
					{
						tooClose = true;
						break;
					}
				}
				if (tooClose) continue;
				
				double d = curves.get(0).distance(sine);
				
				//if (d > minDist && d<maxDist)
				//{		
						curves.add(sine);
						System.out.println("d=" + d);
				//}
			}
		}
		
		ArrayList<Double> allOthers = new ArrayList<Double>();
		for (int i=1; i<curves.size(); i++)
		{
			ArrayList<Double> a = curves.get(i).getApproximation();
			for (int j=0; j<a.size(); j++)
				allOthers.add(a.get(j));
		}
		
		double d = CurveDist.curveToCurveDistance(curves.get(0).getApproximation(), allOthers, 0);
		System.out.println("all curves dist = " + d);
		
		if (d < minDist || d> maxDist)
			curves.clear();
		else
			break;
		}
		
		
		taskCurve = (int)(Math.random()*(curves.size()-1));
		SineWaveShape c = curves.get(0);
		curves.remove(0);
		curves.add(taskCurve,c);
		
		System.out.println("found config");
		
		//
	}
	
	
	
	public void saveStimulus()
	{		
		BufferedImage bim = new BufferedImage(1240,940, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = bim.createGraphics();
		
		g.translate(620, 470);
		
		this.render(g);
		
		Calendar c = Calendar.getInstance();
		String filename ="" + c.get(c.DAY_OF_YEAR) + c.get(c.HOUR) + c.get(c.MINUTE) + c.get(c.SECOND);
		
		try {
			ImageIO.write(bim, "PNG", new File("c:\\work\\curve_stim_" + filename + ".PNG"));
			saveData("c:\\work\\curve_stim_" + filename + ".tex");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveData(String filename)
	{
		 PrintWriter bw;
		try {
			bw = new PrintWriter(new FileWriter(new File(filename)));
		
		 
		bw.write("NUMOBJECTS:"+curves.size());
		bw.println();
		
		bw.write("CURVES:");
		for (int i=0; i<curves.size(); i++)
				bw.write(curves.get(i).amplitude + "\t" +  curves.get(i).frequency + "\t" + curves.get(i).x + "\t" + curves.get(i).y + "\t" + curves.get(i).width + "\t" + curves.get(i).start);
		
		bw.println();
		
		bw.println("MINDIST:" +  this.getPropertyIntValue(PROPERTY_NAME_MIN_DISTANCE));
		bw.println("MAXDIST:" +  this.getPropertyIntValue(PROPERTY_NAME_MAX_DISTANCE));
		bw.println("AMPLITUDE:" +  this.getPropertyPercentValue("Amplitude"));
		bw.println("FREQ:" +  this.getPropertyPercentValue("Freq"));
		bw.println("COLORDIF:" +  this.getPropertyIntValue(PROPERTY_NAME_COLOR_DIFFERENCE) + "," + this.labs.length);
			
		 bw.close();
		 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

