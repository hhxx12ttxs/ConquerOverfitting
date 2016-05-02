package perspectives.graph;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.lang.reflect.Array;
import java.util.ArrayList;

import perspectives.util.DistanceMatrix2;

public class ForceDirectedEdgeBundler extends EdgeBundler{

	int P;
	double S;
	int I;
	
	double[][] segX;
	double[][] segY;
	
	DistanceMatrix2 compat;
	
	double[] edgeLength;
	
	int cnt = 0;
	
	ArrayList<ArrayList<Point2D.Double>> edgeSegments = null;
	public ForceDirectedEdgeBundler(GraphViewer g) {
		super(g);
		
		P = 1;
		S = 2;
		I = 50;
		
		segX = null;
		segY = null;
	
	}

	
	@Override
	public void compute() {
		
		if (segX == null || cnt == I)
		{
			if (segX==null)
			{
				P = 1;
				S = 4;
				I = 100;
				cnt=0;
			}
			else
			{
				P *= 2;
				S /= 2;
				I = (int)(0.80 * I);
				cnt=0;
			}
			if (I == 0) return;
			
			//segment some more
			//if no segments yet at all
			if (segX == null && segY == null)
			{
				ArrayList<Integer> e1 =new ArrayList<Integer>();
				ArrayList<Integer> e2 =new ArrayList<Integer>();
				graphViewer.graph.getEdgesAsIndeces(e1, e2);
				segX = new double[e1.size()][];
				segY = new double[e2.size()][];
				edgeLength = new double[e1.size()];
				
				for (int i=0; i<segX.length; i++)
				{
					segX[i] = new double[2];
					segY[i] = new double[2];
					segX[i][0] = graphViewer.drawer.getX(e1.get(i));
					segX[i][1] = graphViewer.drawer.getX(e2.get(i));
					segY[i][0] = graphViewer.drawer.getY(e1.get(i));
					segY[i][1] = graphViewer.drawer.getY(e2.get(i));
					
					edgeLength[i] = Math.sqrt((segX[i][0]-segX[i][1])*(segX[i][0]-segX[i][1]) + (segY[i][0]-segY[i][1])*(segY[i][0]-segY[i][1]));
				}
				
				this.computeCompatibilities();
			}
			
			double[][] newSegX = new double[segX.length][];
			double[][] newSegY = new double[segY.length][];
			for (int i=0; i<segX.length; i++)
			{
				if (edgeLength[i]/segX[i].length < 10)
				{
					newSegX[i] = segX[i];
					newSegY[i] = segY[i];
					continue;
				}
				newSegX[i] = new double[2*segX[i].length-1];
				newSegY[i] = new double[2*segY[i].length-1];
				for (int j=0; j<segX[i].length-1; j++)
				{
					newSegX[i][j*2] = segX[i][j];
					newSegX[i][j*2+2] = segX[i][j+1];
					newSegX[i][j*2+1] = (segX[i][j]+segX[i][j+1])/2.;
					
					newSegY[i][j*2] = segY[i][j];
					newSegY[i][j*2+2] = segY[i][j+1];
					newSegY[i][j*2+1] = (segY[i][j]+segY[i][j+1])/2.;
				}
			}
			
			segX = newSegX;
			segY = newSegY;
			
		}
		
		System.out.println(cnt + "/" + I);
		cnt++;
		
		//Forces are initialized here
		double[][] fx = new double[segX.length][];
		double[][] fy = new double[segX.length][];
		for(int i=0;i<fx.length;i++)
		{
			fx[i] = new double[segX[i].length];
			fy[i] = new double[segY[i].length];
			for(int j=0;j<fx[i].length;j++)
			{
				fx[i][j] =0.0;
				fy[i][j] =0.0;
			}
			
		}
		
		double K = 1;
		
		//calculate forces
		for(int i=0;i<segX.length;i++)
		{
			//Forces FS are calculated here
			if (edgeLength[i]<0.1) continue;
			
			double kp = K/edgeLength[i];// * segX[i].length;
			
			for(int j=1;j<segX[i].length-1;j++)
			{
				double vx1 = segX[i][j] - segX[i][j-1];
				double vy1 = segY[i][j] - segY[i][j-1];				
				
				fx[i][j] -= Math.signum(vx1)*kp*vx1*vx1;
				fy[i][j] -= Math.signum(vy1)*kp*vy1*vy1;
				
				double vx2 = segX[i][j] - segX[i][j+1];
				double vy2 = segY[i][j] - segY[i][j+1];				
				
				fx[i][j] -= Math.signum(vx2)*kp*vx2*vx2;
				fy[i][j] -= Math.signum(vy2)*kp*vy2*vy2;
				
				//FE
				for(int k=0;k< segX.length; k++)
				{
					if (i == k) continue;
					
					double c = compat.getDistance(i, k);
					
					if (c == 0) continue;
					
					/*int coresp1 = j;
					int coresp2 = segX[i].length-j-1;
					if (segX[i].length != segX[k].length)
					{
						double f = (segX[k].length-1)/(segX[i].length-1);
						coresp1 = (int)(coresp1*f);
						coresp2 = (int)(coresp2*f);
						
						if (coresp1 >= segX[k].length || coresp2 >= segX[k].length)
							System.out.println("-- " + segX[i].length + " " + j + " " +  segX[k].length + " " + coresp1 + " " + coresp2);
					}*/
					
					int coresp1 = this.closestPoint(segX[i][j], segY[i][j], segX[k], segY[k]);
					
					if (coresp1<0) continue;
					
					
					
				
					
					double v1x = segX[k][coresp1] - segX[i][j];
					double v1y = segY[k][coresp1] - segY[i][j];					
					double d1 = Math.sqrt(v1x*v1x + v1y*v1y);				
					
					double d = d1;
					double vx = v1x;
					double vy = v1y;
					
					/*double v2x = segX[k][coresp2] - segX[i][j];
					double v2y = segY[k][coresp2] - segY[i][j];					
					double d2 = Math.sqrt(v2x*v2x + v2y*v2y);	
					if (d2 < d1)
					{
						d = d2;
						vx = v2x;
						vy = v2y;
					}*/
					
					
					if (d == 0) continue;	
						
					vx /= d;
					vy /= d;				
						
					double mag = 100/d;
						
					if (mag>5) mag = 5;
					mag = c*mag;
					
				//	if (c == 0) mag = -100/(d*d);
					
					fx[i][j] += mag*vx;
					fy[i][j] += mag*vy;
					
				}
				
			}
		}
		
		//apply
		for(int i=0;i<segX.length;i++)
		{
			for(int j=1;j< segX[i].length-1;j++)
			{
				double fd = Math.sqrt(fx[i][j]*fx[i][j] + fy[i][j] * fy[i][j]);
				if (fd > S)
				{
					fx[i][j] = S* fx[i][j]/fd;
					fy[i][j] = S* fy[i][j]/fd;
				}
				segX[i][j] += fx[i][j];
				segY[i][j] += fy[i][j];
			}
		}
		
		/*for (int ii=0; ii<1; ii++)
		for(int i=0;i<this.edgePoly.length;i++){
			Point2D.Double[] polyline = this.getEdgePolyline(i);
			this.smoothRenderEdge(polyline);
		}*/
		
	}
		
		
	public void computeCompatibilities()
	{
		ArrayList<Integer> e1 =new ArrayList<Integer>();
		ArrayList<Integer> e2 =new ArrayList<Integer>();
		graphViewer.graph.getEdgesAsIndeces(e1, e2);
		
		compat = new DistanceMatrix2(segX.length);
		
		EdgeCompatibility ec = new EdgeCompatibility();
		
		for (int i=0; i<segX.length-1; i++)
			for (int j=i+1; j<segX.length; j++)
			{
				Point2D.Double p1 = new Point2D.Double(segX[i][0],segY[i][0]);
				Point2D.Double p2 =  new Point2D.Double(segX[i][segX[i].length-1],segY[i][segY[i].length-1]);
				
				Point2D.Double q1 = new Point2D.Double(segX[j][0],segY[j][0]);
				Point2D.Double q2 =  new Point2D.Double(segX[j][segX[j].length-1],segY[j][segY[j].length-1]);
				
				double c = ec.getCompatibility(p1,p2, q1, q2, false);
				
				//if (!(e1.get(i).intValue() == e1.get(j).intValue() || e1.get(i).intValue() == e2.get(j).intValue() || e2.get(i).intValue() == e1.get(j).intValue() || e2.get(i).intValue() == e2.get(j).intValue()))
				//	c = 0;
				
				
				compat.setDistance(i, j, (float)c);
			
			}
		
	}
	
	//this function is for smoothing edges
	private void smoothRenderEdge(Point2D.Double[] polyline){
		
		for (int i=1; i<polyline.length-1; i++)
		{
			double cnt = 1;
			
			double f = 1;
			
			for (int k=1; k<10; k++)
			{
				f = f *0.7;
				if (i-k >= 0)
				{
					polyline[i].x += f*polyline[i-k].x;				
					polyline[i].y += f*polyline[i-k].y;	
					cnt+= f;
				}
				if (i+k < polyline.length)
				{
					polyline[i].x += f*polyline[i+k].x;				
					polyline[i].y += f*polyline[i+k].y;	
					cnt+= f;
				}				
			}			
			polyline[i].x /= cnt;
			polyline[i].y /= cnt;
		
		}
	}
	
	public void smoothEdges(){
		
		for (int i=0; i<segX.length; i++)
		{
		for (int j=1; j<segX[i].length-1; j++)
		{
			double midx = 0.5*(segX[i][j-1] + segX[i][j+1]);	
			double midy = 0.5*(segY[i][j-1] + segY[i][j+1]);	
			
			segX[i][j] = (6*segX[i][j] + 4*midx)/10;
			segY[i][j] = (6*segY[i][j] + 4*midy)/10;
		}
		}
	}

	

	
	@Override
	public Point2D.Double[] getEdgePolyline(int e) {
		if (segX == null)
			return null;
		
		Point2D.Double[] ep = new Point2D.Double[segX[e].length];
		for (int i=0; i<ep.length; i++)
			ep[i] = new Point2D.Double(segX[e][i],segY[e][i]);
		
		//this.smoothRenderEdge2(ep,8);
		return ep;
		
	}
	
	public int closestPoint(double x, double y, double[] xx, double[] yy)
	{
		double mind = java.lang.Double.MAX_VALUE;
		int index = -1;
		for (int i=1; i<xx.length-1; i++)
		{
			double d = (xx[i]-x)*(xx[i]-x) + (yy[i]-y)*(yy[i]-y);
			if (d <mind)
			{
				mind = d;
				index = i;
			}
		}
		if (index <0) System.out.println("why?");
		return index;
	}

}
