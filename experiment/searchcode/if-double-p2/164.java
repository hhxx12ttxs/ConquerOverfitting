package perspectives.multidimensional;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;

import perspectives.util.DistancedPoints;

public class SpringEmbedder extends Embedder2D {

	
	private ArrayList< ArrayList<Integer> > vSet = new ArrayList< ArrayList<Integer> >();
	private double[] maxDist = new double[0];
	
	int smax = 20;
	int vmax = 10;
	
	double maxStep = 5;
	
	double k = 0.01;
	
	DistancedPoints points;
	
	private double maxRealDist;
	
	public SpringEmbedder(DistancedPoints t) {
		super(t);
		points = t;
		
		maxRealDist = 1;
	}
	
	public void basicStep()
	{
		//init forces
		double[] fx = new double[points.getCount()];
		double[] fy = new double[points.getCount()];		
		for (int i=0; i<fx.length; i++)
		{
			fx[i] = 0; fy[i] = 0;
		} 
		
		if (maxDist.length < points.getCount())
		{
			maxDist = new double[points.getCount()];
			for (int i=0; i<maxDist.length; i++)
				maxDist[i] = Double.MAX_VALUE;			
		}
		
		for (int i=0; i<fx.length-1; i++)
		{
			for (int j=i+1; j<fx.length; j++)
			{			

				Point2D.Double p1 = new Point2D.Double(getX(i), getY(i));
				Point2D.Double p2 = new Point2D.Double(getX(j), getY(j));
				
		        double realDistance = p1.distance(p2);

		        while (realDistance == 0)
		        {	
		        	double r1 = Math.random()/1000;
		        	double r2 = Math.random()/1000;
		        	p1 = new Point2D.Double(p1.getX() + r1, p1.getY() + r2);
		        	realDistance = p1.distance(p2);
		        }

		        double expectedDistance = points.getDistance(i,j);
	            if (expectedDistance > maxRealDist) maxRealDist = expectedDistance;		            
	            expectedDistance = expectedDistance/maxRealDist;
		        
		        double forceDelta = Math.abs(realDistance - expectedDistance);
		        double forceSign = -Math.signum(realDistance - expectedDistance);		           

		        double force = forceSign * k * forceDelta;
		        
		        force *= (1-expectedDistance);
		 
		        fx[i] += (p1.getX()-p2.getX())/realDistance * force;
		        fy[i] += (p1.getY()-p2.getY())/realDistance * force;
		        
		        fx[j] -= (p1.getX()-p2.getX())/realDistance * force;
		        fy[j] -= (p1.getY()-p2.getY())/realDistance * force;
		     }

		}
		    
		//add forces to positions
		for (int i=0; i<fx.length; i++)
		{
			double x = getX(i);
			double y = getY(i);
			
			double fl = Math.sqrt(fx[i]*fx[i] + fy[i]*fy[i]);
			if (fl > maxStep)
			{
				fx[i] = maxStep * (fx[i]/fl);
				fy[i] = maxStep * (fy[i]/fl);
			}
			setX(i,x + fx[i]);
			setY(i,y + fy[i]);
		}
			
		maxStep = maxStep * 0.995;		
	}
	
	public void iteration()
	{
		if (points.getCount() < 500)
		{
			basicStep();
			return;
		}
		
		//otherwise, do an optimized embedding;
		//init forces
		double[] fx = new double[points.getCount()];
		double[] fy = new double[points.getCount()];		
		for (int i=0; i<fx.length; i++)
		{
			fx[i] = 0; fy[i] = 0;
		} 
		
		while (vSet.size() < points.getCount())
			vSet.add(new ArrayList<Integer>());

		if (maxDist.length < points.getCount())
		{
			maxDist = new double[points.getCount()];
			for (int i=0; i<maxDist.length; i++)
				maxDist[i] = Double.MAX_VALUE;			
		}
		
		for (int i=0; i<fx.length; i++)
		{			
			ArrayList<Integer> sSet = new ArrayList<Integer>();
			
			while(sSet.size() < smax)
			{
		            //pick random j != i, and j not in vset_i
		            int j;
		            
		            while(true)
		            {		            	
		                    j= (int)((points.getCount()-1)*Math.random());
		                    if (i != j && vSet.get(i).indexOf(new Integer(j))<0) break;		         
		            }		          
		           
		            double dist = points.getDistance(i,j);
		      

		            if (vSet.get(i).size()==0)
		            {
		            	vSet.get(i).add(new Integer(j));
		                maxDist[i] = dist;
		            }
		            else if (dist < maxDist[i])
		            {
		                for (int k=0; k<vSet.get(i).size(); k++)
		                {
		                    if (dist <= points.getDistance(i,vSet.get(i).get(k)))
		                    {
		                        vSet.get(i).add(k,j);
		                        break;
		                    }
		                }
		                if (vSet.get(i).size() > vmax)
		                {
		                    vSet.get(i).remove(vSet.get(i).size()-1);
		                    maxDist[i] = points.getDistance(i,vSet.get(i).get(vSet.get(i).size()-1).intValue());
		                }
		            }
		            else
		                sSet.add(j);
		        }


		        //now we have the v and s sets and we can compute the forces
			int[] combined = new int[vSet.get(i).size() + sSet.size()];
			for (int j=0; j<vSet.get(i).size(); j++)
				combined[j] = vSet.get(i).get(j);
			for (int j=0; j<sSet.size(); j++)
				combined[vSet.get(i).size() + j] = sSet.get(j);
			
			for (int j=0; j<combined.length; j++)
			{
				Point2D.Double p1 = new Point2D.Double(getX(i), getY(i));
				Point2D.Double p2 = new Point2D.Double(getX(combined[j]), getY(combined[j]));
				
		        double realDistance = p1.distance(p2);

		        
		        while (realDistance == 0)
		        {	
		        	double r1 = Math.random()/1000;
		        	double r2 = Math.random()/1000;
		        	p1 = new Point2D.Double(p1.getX() + r1, p1.getY() + r2);
		        	realDistance = p1.distance(p2);
		        }

		        double expectedDistance = points.getDistance(i,combined[j]);
	            if (expectedDistance > maxRealDist) maxRealDist = expectedDistance;		            
	            expectedDistance = expectedDistance/maxRealDist;
		        
		        double forceDelta = Math.abs(realDistance - expectedDistance);
		        double forceSign = -Math.signum(realDistance - expectedDistance);		           

		        double force = forceSign * k * forceDelta;
		 
		       // force = force * (1-expectedDistance);
		        
		        fx[i] += (p1.getX()-p2.getX())/realDistance * force;
		        fy[i] += (p1.getY()-p2.getY())/realDistance * force;
		        
		        fx[combined[j]] -= (p1.getX()-p2.getX())/realDistance * force;
		        fy[combined[j]] -= (p1.getY()-p2.getY())/realDistance * force;
		     }

		}
		    
		//add forces to positions
		for (int i=0; i<fx.length; i++)
		{
			double x = getX(i);
			double y = getY(i);
			
			double fl = Math.sqrt(fx[i]*fx[i] + fy[i]*fy[i]);
			if (fl > maxStep)
			{
				fx[i] = maxStep * (fx[i]/fl);
				fy[i] = maxStep * (fy[i]/fl);
			}
			setX(i,x + fx[i]);
			setY(i,y + fy[i]);
		}
			
		maxStep = maxStep * 0.995;

		   
	}

}
