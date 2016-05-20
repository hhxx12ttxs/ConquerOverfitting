package perspectives.util;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;


public class Util {
	
	public static Point2D getLineLineIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
	      double det1And2 = det(x1, y1, x2, y2);
	      double det3And4 = det(x3, y3, x4, y4);
	      double x1LessX2 = x1 - x2;
	      double y1LessY2 = y1 - y2;
	      double x3LessX4 = x3 - x4;
	      double y3LessY4 = y3 - y4;
	      double det1Less2And3Less4 = det(x1LessX2, y1LessY2, x3LessX4, y3LessY4);
	      if (det1Less2And3Less4 == 0){
	         // the denominator is zero so the lines are parallel and there's either no solution (or multiple solutions if the lines overlap) so return null.
	         return null;
	      }
	      double x = (det(det1And2, x1LessX2,
	            det3And4, x3LessX4) /
	            det1Less2And3Less4);
	      double y = (det(det1And2, y1LessY2,
	            det3And4, y3LessY4) /
	            det1Less2And3Less4);
	      return new Point2D.Double(x, y);  
	   }
	
	public static Point2D getLineLineIntersection(Line2D.Double l1, Line2D.Double l2)
	{
		return getLineLineIntersection(l1.getX1(), l1.getY1(), l1.getX2(), l1.getY2(),l2.getX1(), l2.getY1(), l2.getX2(), l2.getY2());	
	}
	
	
	
	   protected static double det(double a, double b, double c, double d) {
	      return a * d - b * c;
	   }
	   
	   public static boolean linesIntersect(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4){
		   
		   Line2D l1 = new Line2D.Double(x1,y1,x2,y2);
		   Line2D l2 = new Line2D.Double(x3,y3,x4,y4);
		   
		   return l1.intersectsLine(l2);
		   
		   
		   }
	   
	   
       static public void drawArrow(Graphics g1, int x1, int y1, int x2, int y2, boolean doubleHead, int arrSize, int offset) {
           Graphics2D g = (Graphics2D) g1.create();
           
          
           double dx = x2 - x1, dy = y2 - y1;
           double angle = Math.atan2(dy, dx);
           int len = (int) Math.sqrt(dx*dx + dy*dy);
           AffineTransform at = AffineTransform.getTranslateInstance(x1, y1);
           at.concatenate(AffineTransform.getRotateInstance(angle));
           g.transform(at);

           // Draw horizontal arrow starting in (0, 0)
           if (doubleHead)
        	   g.drawLine(offset+arrSize, 0, len-arrSize-offset, 0);
           else
        	   g.drawLine(0, 0, len-arrSize-offset, 0);
           
           g.fillPolygon(new int[] {len-offset+1, len-arrSize-offset, len-arrSize-offset, len-offset+1},
                         new int[] {0, -arrSize/2, arrSize/2, 0}, 4);
           
           if (doubleHead)
               g.fillPolygon(new int[] {offset, arrSize+offset, arrSize+offset, offset},
                       new int[] {0, -arrSize/2, arrSize/2, 0}, 4);        	   
       }
       
       static public void drawArrow(Graphics g1, int x1, int y1, int x2, int y2, boolean doubleHead, int arrSize) {
    	   drawArrow(g1,x1,y1,x2,y2,doubleHead,arrSize,0);
       }
       
       static public double distanceBetweenPoints(double x1, double y1, double x2, double y2)
       {
    	   return Math.sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2));
       }
       
       static public double[] rgbToLab(Color rgb)
       {
    	   double[] f1 = {0.412424, 0.212656, 0.0193324};
    	   double[] f2 = {0.357579, 0.715158,  0.119193};
    	   double[] f3 = {0.180464, 0.0721856, 0.950444};

           double r,g,b;
           
           double rgbR = rgb.getRed()/255.;
           double rgbG = rgb.getGreen()/255.;
           double rgbB = rgb.getBlue()/255.;

           if (rgbR <= 0.04045)
               r = rgbR/12.92;
           else
               r = Math.pow((rgbR + 0.055)/1.055,2.4);

           if (rgbG <= 0.04045)
               g = rgbG/12.92;
           else
               g = Math.pow((rgbG + 0.055)/1.055,2.4);

           if (rgbB <= 0.04045)
               b = rgbB/12.92;
           else
               b = Math.pow((rgbB + 0.055)/1.055,2.4);
           
           double x = r*f1[0] + g*f1[1] + b*f1[1];
           double y = r*f2[0] + g*f2[1] + b*f2[1];
           double z = r*f3[0] + g*f3[1] + b*f3[1];

           double e = 0.008856;
           double k = 903.3;

           double fx,fy,fz;
           
           double Xr = 0.95047; double Yr =    1.00000; double Zr =    1.08883;
           double xr = x / Xr;
           double yr = y / Yr;
           double zr = z / Zr;

           if (xr>e)
               fx = Math.pow(xr,1/3.);
           else
               fx = (k*xr + 16)/116;

           if (yr>e)
               fy = Math.pow(yr,1/3.);
           else
               fy = (k*yr + 16)/116;

           if (zr>e)
               fz = Math.pow(zr,1/3.);
           else
               fz = (k*zr + 16)/116;

           double L = 116*fy -16;
           double A = 500*(fx-fy);
           double B= 200*(fy-fz);
           
           double[] rt = {L,A,B};

           return rt;
       }
       
       
       static public Color labToRgb(double l,double a, double b)
       {
           double Xr = 0.95047; double Yr =    1.00000; double Zr =    1.08883;

           double epsilon = 0.008856;
           double kappa = 903.3;

           double yr;
           if (l > kappa*epsilon)
               yr = Math.pow((l+16)/116,3);
           else
               yr = l/kappa;
               

           double fy;
           if (yr>epsilon)
               fy = (l+16)/116;
           else
               fy = (kappa*yr+16)/116;
           double fx = a/500 + fy;
           double fz = fy - b/200;

           double xr;
           if (fx*fx*fx > epsilon)
               xr = fx*fx*fx;
           else
               xr = (116*fx - 16)/kappa;

           double zr;
           if (fz*fz*fz > epsilon)
               zr = fz*fz*fz;
           else
               zr = (116*fz - 16)/kappa;

           double x = xr*Xr;
           double y = yr*Yr;
           double z = zr*Zr;

           //double f1[] = {3.24071,   -0.969258, 0.0556352};
         //  double f2[] = {-1.53726,  1.87599,   -0.203996};
          // double f3[] = {-0.498571, 0.0415557, 1.05707};
           
           double f1[] = {3.24071,   -1.53726, -0.498571};
           double f2[] = {-0.969258,  1.87599,   0.0415557};
           double f3[] = {0.0556352, -0.203996,1.05707};

           
           double r = x*f1[0] + y*f1[1] + z*f1[2];
           double g = x*f2[0] + y*f2[1] + z*f2[2];
           b = x*f3[0] + y*f3[1] + z*f3[2];

           double R;
           if (r<=0.0031308)
               R = 12.92*r;
           else
               R = 1.055*Math.pow(r,1.0/2.4)-0.055;

           double G;
           if (g<=0.0031308)
               G = 12.92*g;
           else
               G = 1.055*Math.pow(g,1.0/2.4)-0.055;

           double B;
           if (b<=0.0031308)
               B = 12.92*b;
           else
               B = 1.055*Math.pow(b,1.0/2.4)-0.055;
           
           if (R<0) R = 0; if (R>1) R = 1;
           if (G<0) G = 0; if (G>1) G = 1;
           if (B<0) B = 0; if (B>1) B = 1;

           return new Color((float)R,(float)G,(float)B);
       }
       
       
       
       
       public static Point2D.Double[][] marchingSquares(double[][] pGrid,double cellSize,Point2D.Double min, double threshold)
       {
           int [][] gridThreshHold;
           
           ArrayList<Line2D> retValue = new ArrayList<Line2D>();
           gridThreshHold= new int[pGrid.length][];
           for(int i=0;i<gridThreshHold.length;i++)
           {
               gridThreshHold[i]= new int[pGrid[i].length];
           }
           for(int i=0;i<pGrid.length;i++)
           {
               for(int j=0;j<pGrid[i].length;j++)
               {
                   double degd = pGrid[i][j];
                   if (degd < threshold) 
                   {
                       gridThreshHold[i][j]=1;
                   }
                   else if(degd >= threshold)
                   {
                       gridThreshHold[i][j] = 0;
                   }
                   
               }
           }
           int intValue;
           double dx = cellSize;
           double dy = cellSize;
         //  dx = (max.x-min.)/cellSize;
         //  dy = (minMax[3]-minMax[1])/cellSize;
           double x1,y1,x2,y2,x3,y3,x4,y4;
           Line2D l1;
           for(int i=0;i<gridThreshHold.length-1;i++)
           {
               for(int j=0;j<gridThreshHold[i].length-1;j++)
               {
                   x1=min.x + i*dx;
                   y1=min.y + j*dy;
                   x2=min.x + (i+1)*dx;
                   y2=min.y + j*dy;
                   x3=min.x + (i+1)*dx;
                   y3=min.y + (j+1)*dy;
                   x4=min.x + i*dx;
                   y4=min.y + (j+1)*dy;
                   String binaSt="";
                   binaSt +=gridThreshHold[i][j];
                   binaSt +=gridThreshHold[i+1][j];
                   binaSt +=gridThreshHold[i+1][j+1];
                   binaSt +=gridThreshHold[i][j+1];
                   intValue= Integer.parseInt(binaSt,2);
                   switch(intValue)
                   {
                       case 0:
                       break;
                       case 1:
                           l1 = new Line2D.Double(x1, (y1+y4)/2, (x3+x4)/2, y4);
                           retValue.add(l1);
                           break;
                       case 2:
                           l1 = new Line2D.Double(x2, (y2+y3)/2, (x3+x4)/2, y4);
                           retValue.add(l1);
                           break;
                       case 3:
                           l1 = new Line2D.Double(x1, (y1+y4)/2, x3, (y1+y4)/2);
                           retValue.add(l1);
                           break;
                       case 4:
                           l1 = new Line2D.Double((x1+x2)/2, y1, x2, (y2+y3)/2);
                           retValue.add(l1);
                           break;
                       case 5:
                           l1 = new Line2D.Double(x1, (y1+y4)/2, (x1+x2)/2, y1);
                           retValue.add(l1);
                           l1 = new Line2D.Double(x3, (y2+y3)/2, (x3+x4)/2, y4);
                           retValue.add(l1);
                           break;
                       case 6:
                           l1 = new Line2D.Double((x1+x2)/2, y1, (x1+x2)/2, y4);
                           retValue.add(l1);
                           break;
                       case 7:
                           //Smae as fist line of case 5
                           l1 = new Line2D.Double(x1, (y1+y4)/2, (x1+x2)/2, y1);
                           retValue.add(l1);
                           break;
                       case 8:
                           //Smae as fist line of case 5
                           l1 = new Line2D.Double(x1, (y1+y4)/2, (x1+x2)/2, y1);
                           retValue.add(l1);
                           break;
                       case 9:
                           l1 = new Line2D.Double((x1+x2)/2, y1, (x1+x2)/2, y4);
                           retValue.add(l1);
                           break;
                       case 10:
                            l1 = new Line2D.Double((x1+x2)/2, y1, x2, (y1+y4)/2);
                           retValue.add(l1);
                           l1 = new Line2D.Double(x1, (y1+y4)/2, (x3+x4)/2, y4);
                           retValue.add(l1);
                           break;
                       case 11:
                           l1 = new Line2D.Double((x1+x2)/2, y1, x3, (y2+y3)/2);
                           retValue.add(l1);
                           break;
                       case 12:
                           l1 = new Line2D.Double(x1, (y1+y4)/2, x3, (y1+y4)/2);
                           retValue.add(l1);
                           break;
                       case 13:
                           l1 = new Line2D.Double(x3, (y2+y3)/2, (x3+x4)/2, y4);
                           retValue.add(l1);
                           break;
                       case 14:
                           l1 = new Line2D.Double(x1, (y1+y4)/2, (x3+x4)/2, y4);
                           retValue.add(l1);
                           break;
                       case 15:
                           break;
                   }
               }
           }
           
           
           //order them
           ArrayList< ArrayList<Point2D.Double> > polylines = new ArrayList< ArrayList<Point2D.Double> >();
           polylines.add(new ArrayList<Point2D.Double>());
           polylines.get(0).add((Point2D.Double) retValue.get(0).getP1());
           polylines.get(0).add((Point2D.Double) retValue.get(0).getP2());
           retValue.remove(0);
           int adding = 0;
           while (retValue.size() > 0)
           {
           	boolean found = false;
           	Point2D.Double lastp = polylines.get(adding).get(polylines.get(adding).size()-1);
           	
           	   for (int i=0; i<retValue.size(); i++)
                  {
                  	Point2D.Double p1 = (Point2D.Double) retValue.get(i).getP1();
                  	Point2D.Double p2 = (Point2D.Double) retValue.get(i).getP2();
                  	
                  		if (lastp.distance(p1) < cellSize/5)
                  		{
                  			polylines.get(adding).add(p2);
                  			retValue.remove(i);
                  			lastp = p2;
                  			found = true;
                  			i--;
                  			
                  		}
                  		else if (lastp.distance(p2) < cellSize/5)
                  		{
                  			polylines.get(adding).add(p1);
                  			retValue.remove(i);
                  			lastp = p1;
                  			found = true;
                  			i--;
                  			
                  		}
                  }
           	   if (found || retValue.size() <= 0) continue;
           	   
           	   polylines.add(new ArrayList<Point2D.Double>());
           	   adding++;
                  polylines.get(adding).add((Point2D.Double) retValue.get(0).getP1());
                  polylines.get(adding).add((Point2D.Double) retValue.get(0).getP2());	 
                  retValue.remove(0);
           }
           
           Point2D.Double[][] ret = new Point2D.Double[polylines.size()][];
           for (int i=0; i<ret.length; i++)
           {
           	ret[i] = new Point2D.Double[polylines.get(i).size()];
           	for (int j=0; j<ret[i].length; j++)
           		ret[i][j] = polylines.get(i).get(j);
           }
           
           
           return ret;
       }


}

