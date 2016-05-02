package stimulusgen;

import java.util.ArrayList;

import util.Vector3D;

public class CurveDist {

	public static boolean regularDistance = true;
	/*************************************************************************
	 * Function Name: ICurveDist::NEAREST_LINE_PT
	 * Parameters: const Wpt &pt, const Wpt &s, const Wpt &e
	 * Returns: Wpt
	 * Effects: returns the nearest point on the line defined by (s,e)
	 * to the point pt by projecting pt onto the line.
	 *************************************************************************/
	public static Vector3D nearestLinePoint(Vector3D pt, Vector3D s, Vector3D e)
	{
	  
	  Vector3D eMinusS = e.minus(s);
	  Vector3D ptMinusS = pt.minus(s).div(eMinusS.length()); //(pt-s)/(e-s).length()
	  Vector3D v2 =eMinusS.cross( eMinusS.cross(ptMinusS)); //  (e-s)*(((e-s)*(pt-s))/(e-s).length()));
	  Vector3D result = s.plus(v2);
	  
	  return result;
	}
	
	/*************************************************************************
	 * Function Name: ICurveDist::PT_TO_LINE_SEG_DIST
	 * Parameters: const Wpt &pt, const Wpt &s, const Wpt &e
	 * Returns: double
	 * Effects: returns the distance from the nearest point on the line seg
	 * defined by (s,e) to the given point pt.
	 *************************************************************************/
	public static double pointToLineSegmentDistance(Vector3D pt, Vector3D s,Vector3D e)
	{
		Vector3D nearestPoint = nearestLinePoint(pt, s, e);
		double ret =0;
		Vector3D v = null;
		
		if(nearestPoint.minus(s).dot(nearestPoint.minus(e)) < 0)
		{
			ret = pt.minus(nearestPoint).length();
			v = nearestPoint;
		}

		
		if(pt.minus(s).length() < pt.minus(e).length())
		{
			ret = pt.minus(s).length();
			v = s;
		}
		else
		{
			ret = pt.minus(e).length();
			v = e;
		}
		
		return ret;
		
	}


	/*************************************************************************
	 * Function Name: ICurveDist::CURVE_TO_CURVE_DIST
	 * Parameters: Array<double>& c1, Array<double>& c2, double threshold
	 * Returns: double
	 * Effects: integral distance between the curves.
	 *************************************************************************/
	public static double curveToCurveDistance(ArrayList<Double> c1, ArrayList<Double>c2, double threshold)
	{
		 //num of pts having distances above 'threshold'
		  double cnt=0;
		  double d=0.0;
		  int n1 = (int)(c1.size()/3);
		  int n2 = (int)(c2.size()/3); 
		  
		  for(int i=0; i< n1; i++)
		  {
			  	
			  double minD = Double.MAX_VALUE;
			  double minD2 = Double.MAX_VALUE;

			  int bi = i*3;
			  
			  Vector3D pt = new Vector3D(
					  (float)c1.get(bi).doubleValue()
					  ,(float) c1.get(bi+1).doubleValue()
					  ,(float)c1.get(bi+2).doubleValue()
					  );
			  boolean found = false;
		
			  for(int j=0; j< n2-1; j++)
			  {
		
			      int bj_s= j*3;
			      int bj_e= (j+1)*3;

			      Vector3D s = new Vector3D(
			    		  (float) c2.get(bj_s).doubleValue(),
			    		  (float) c2.get(bj_s+1).doubleValue(),
			    		  (float) c2.get(bj_s+2).doubleValue());
			      Vector3D e = new Vector3D(
			    		  (float) c2.get(bj_e).doubleValue(),
			    		  (float) c2.get(bj_e+1).doubleValue(),
			    		  (float) c2.get(bj_e+2).doubleValue());
			      
			      if (s.minus(e).length()> 100) continue;
			      

			      double t = pointToLineSegmentDistance(pt, s, e);
			      
			      found = true;
			      minD = (t< minD)?t:minD;
			      
			    }
			  
			    if (found)
			    {
			    	if(minD >= threshold)
			        {
			        	//d=(double)(d+minD); 
			        	//cnt+=1;	
			        	
			        	//minD /= 5.;	        	
			        	if (minD < 1)
			        		minD = 1;
			        	
			        	d += 1;			        	
			        	cnt += 1./(minD);
			        	
			        	//System.out.println("  " + i + " " +  1./(minD*minD) + " " + cnt);	
			        	
			        }
			    }
			}
		  if (regularDistance)
		      return ((cnt==0)?0:(d/(double)cnt));
		  else
		      return ((cnt==0)?Double.MAX_VALUE:(d/(double)cnt));  
		  
	}
	
	/*************************************************************************
	 * Function Name: ICurveDist::CURVE_TO_CURVE_APPROX_DIST
	 * Parameters: Array<double>& c1,
	 * Array<double>& c2, double threshold
	 * Returns: double
	 * Effects: approximate distance; almost the same as chamfer distance
	 *************************************************************************/
	public static double curveToCurveApproximateDistance(ArrayList<Double> c1, ArrayList<Double>c2, double threshold)
	{
	  //num of pts having distances above 'threshold'
	  int cnt=0;

	  double d =0;
	  int n1 = (int)(c1.size()/3);
	  int n2 = (int)(c2.size()/3);

	  for(int i=0; i< n1; i++)
	  {
		  double minDPoint = Double.MAX_VALUE;

	      int indx=-1;
	      int bi= i*3;
	      Vector3D ptC1 = new Vector3D(
	    		(float) c1.get(bi).doubleValue(),
	    		(float) c1.get(bi+1).doubleValue(),
	    		(float) c1.get(bi+2).doubleValue());

	      for(int j=0; j< n2; j++)
	      {
	    	  int bj= j*3;
	    	  Vector3D ptC2 = new Vector3D(
		    		(float) c2.get(bj).doubleValue(),
		    		(float) c2.get(bj+1).doubleValue(),
		    		(float) c2.get(bj+2).doubleValue());

		      double t = ptC1.minus(ptC2).length();
		      if( t< minDPoint )
		      {
		    	  minDPoint = t;
		          indx = j;
		      }
	       }

	    double minD = 0;

	    if(indx == 0)
	    {
	    	minD = pointToLineSegmentDistance(ptC1,
	    			new Vector3D(
	    					(float) c2.get(3* indx).doubleValue(),
	    					(float) c2.get(3* indx+1).doubleValue(),
	    					(float) c2.get(3* indx+2).doubleValue()),
					new Vector3D(
	    					(float) c2.get(3* (indx+1)).doubleValue(),
	    					(float) c2.get(3* (indx+1)+1).doubleValue(),
	    					(float) c2.get(3* (indx+1)+2).doubleValue()) 
	    			);

	    }
	    else if(indx == (n2-1))
	    {

	    	minD = pointToLineSegmentDistance(ptC1,
	    			new Vector3D(
	    					(float) c2.get(3* indx).doubleValue(),
	    					(float) c2.get(3* indx+1).doubleValue(),
	    					(float) c2.get(3* indx+2).doubleValue()),
					new Vector3D(
	    					(float) c2.get(3* (indx-1)).doubleValue(),
	    					(float) c2.get(3* (indx-1)+1).doubleValue(),
	    					(float) c2.get(3* (indx-1)+2).doubleValue()) 
	    			);
	    }
	    else
	    {
	    	double minD1 = pointToLineSegmentDistance(ptC1,
	    			new Vector3D(
	    					(float) c2.get(3* indx).doubleValue(),
	    					(float) c2.get(3* indx+1).doubleValue(),
	    					(float) c2.get(3* indx+2).doubleValue()),
					new Vector3D(
	    					(float) c2.get(3* (indx-1)).doubleValue(),
	    					(float) c2.get(3* (indx-1)+1).doubleValue(),
	    					(float) c2.get(3* (indx-1)+2).doubleValue()) 
	    			);

	    	double minD2 = pointToLineSegmentDistance(ptC1,
	    			new Vector3D(
	    					(float) c2.get(3* indx).doubleValue(),
	    					(float) c2.get(3* indx+1).doubleValue(),
	    					(float) c2.get(3* indx+2).doubleValue()),
					new Vector3D(
	    					(float) c2.get(3* (indx+1)).doubleValue(),
	    					(float) c2.get(3* (indx+1)+1).doubleValue(),
	    					(float) c2.get(3* (indx+1)+2).doubleValue()) 
	    			);
	      minD  = (minD1<minD2)?minD1:minD2;
	    }

	    // when double is defined to be  float the following
	    // double typecasting increases numerical accuracy by
	    // limiting  the numerical error to float truncation
	    if(minD >= threshold)
	    {
	    	d=(double)(d+minD); 
	    	cnt++;
	    }

	  }

	  return ((cnt==0)?0:(d/(double)cnt));
	}
	
	/*************************************************************************
	 * Function Name: ICurveDist::DIST
	 * Parameters: Array<double>& c1, Array<double>& c2, double threshold, int distm, int symmtype, double sigmasqr
	 * Returns: double
	 * Effects:
	 *************************************************************************/
//	double
//	BrainData::DIST(Array<double>& c1,
//	    Array<double>& c2,
//	    double threshold,
//	    std::string symmtype,
//	    std::string distm,
//	    int wflag,
//	    double sigmasqr)
//	 {
	public static double distance
			(
					ArrayList<Double> c1,
					ArrayList<Double> c2,
					double threshold,
					String symmType,
					String distm,
					int wflag,
					double sigmasqr
			)
	{
			double d = 0;
			double dIJ=0;
			double dJI=0;

			if(distm.equalsIgnoreCase("EXACT")){

				dIJ = curveToCurveDistance(c1,c2,threshold);
			//	dJI = curveToCurveDistance(c2,c1,threshold);

		  }

		  if(symmType.equalsIgnoreCase("MAX_MEANDIST")){
		    d = ((dIJ > dJI) ? dIJ : dJI);
		  } else if(symmType.equalsIgnoreCase("AVG_MEANDIST")){
		    d =  (dIJ + dJI)*0.5f;
		  } else {
		    d = ((dIJ < dJI) ? dIJ : dJI);
		  }
		  return d;
		
	}

	  
	
}

