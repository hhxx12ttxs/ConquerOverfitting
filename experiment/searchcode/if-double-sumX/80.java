package perspectives.util;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class BubbleSets {

	private Rectangle2D.Double[] points = null;
	
	private Rectangle[] pointsD = null; //discrete points - on the grid
	
	private int[][] sets = null;
	
	public double minX, maxX, minY, maxY;
	public double[][] grid = null;
	
	public double[][] gridMinus = null;
	
	double R1 = 100;
	double R0 = 50;

	
	private double cellSize;
	
	private ArrayList< ArrayList<Point2D.Double>> paths;
	
	private Point2D.Double centroid = null;
	
	private int[] sortedS = null;
	
	private int set = -1;
	
	private boolean[] pointsIn = null;
	
	
	
	//works for rectangular shapes; so input is rectangles
	public BubbleSets(Rectangle2D.Double[] points, int[][] sets)
	{
		this.points = points;
		this.sets = sets;
	}
	
	public Path2D[] computeAll(double cellSize, int R1, int R0, double contThresh)
	{
		Path2D[] result = new Path2D[sets.length];
		for (int i=0; i<sets.length; i++)
		{
			System.out.println("compute " + i);
			result[i] = computeOne(i, cellSize, R1, R0, contThresh);
		}
		return result;
	}
	
	public Path2D computeOne(int set, double cellSize, int R1, int R0, double contThresh)
	{		
		computeContour(cellSize, R1, R0, set);
		
		Point2D.Double[][] lin = Util.marchingSquares(grid, cellSize, new Point2D.Double(minX, minY), contThresh);
		
	   Path2D.Double path = new Path2D.Double();
	   for(int i=0;i<lin.length;i++)
	   {	
		   if (lin[i].length > 0) path.moveTo((int)lin[i][0].x,(int)lin[i][0].y); 
		   for (int k=1; k<lin[i].length; k++)	
		   {
			   Point2D.Double p1 = lin[i][k-1];
			   Point2D.Double p2 = lin[i][k];
			   path.lineTo((int)lin[i][k].x,(int)lin[i][k].y);
		   }
		   path.closePath();
	    }
	   
	   return path;
	}
	
	
	
	private void computeContour(double cellSize, int R1, int R0, int set)
	{
		
		paths = new ArrayList< ArrayList<Point2D.Double> >();
		
		this.cellSize = cellSize;
		this.set = set;
		this.R0 = R0;
		this.R1 = R1;
		
		grid = createGrid(cellSize);
		
		//pointsD now creates int rectangles on the grid
		int[] sortedSet = sortSet(set);	
			
			sortedS = sortedSet;
			
			for (int i=0; i<sortedSet.length; i++)
			{	
				int p = sortedSet[i];
				
				addPointEnergy(p, 1.);				
				
				int[][] path = connect(set,sortedSet, i);
				
				if (path == null) continue;	
						
				//add energy to the path
				for (int x=0; x < grid.length; x++)
					for (int y=0; y<grid[x].length; y++)
					{
						double dmin = Double.MAX_VALUE;
						
						for (int k=1; k<path.length; k++)
						{
							double d = (new Line2D.Double(path[k][0],path[k][1], path[k-1][0], path[k-1][1])).ptSegDist(x, y);
							if (d < dmin) dmin = d;
						}
						
						if (x >=0 && x < grid.length && y>=0 && y<grid[x].length)
						{
							dmin *= cellSize;
							if (R1-dmin <= 0) continue;
							double en = 1.2*((R1 - dmin)*(R1-dmin))/((R1-R0)*(R1-R0));
							grid[x][y] = Math.max(grid[x][y],en);
						}
					}
			}
			
			//substract some energies from nonset members (just from the point shapes)
			
			for (int i=0; i<sets.length; i++)
			{
				if (i == set) continue;
				
				for (int j=0; j<sets[i].length; j++)
				{
					if (pointsIn[sets[i][j]])
						addPointEnergy(sets[i][j], -0.8);
				}
			}
			
			for (int i=0; i<grid.length; i++)
				for (int j=0; j<grid[i].length; j++)
					grid[i][j] = grid[i][j] + gridMinus[i][j];
			

	}
	
	private void addPointEnergy(int p, double w) {
		//start adding energy
		int xs = pointsD[p].x;
		int xe = pointsD[p].x + pointsD[p].width;
		int ys = pointsD[p].y;
		int ye = pointsD[p].y + pointsD[p].height;
		
		//1. add 1 to inside of the shape
		for (int x = xs; x<xe; x++)
			for (int y = ys; y<ye; y++)
				if (x >= 0 && x <grid.length && y>=0 && y<grid[x].length)
				{
					if (w > 0)
							grid[x][y] = Math.max(grid[x][y], w);
					else
							gridMinus[x][y] = Math.min(gridMinus[x][y], w);
					
				}
		
		//2. add energy around it
		//add borders around the rectangle of I until you don't add anything to pixels anymore
		int b = 1;
		double maxEn = 1;
		while (maxEn > 0)
		{
			maxEn = 0;
			int sx = xs-b;
			int sy = ys-b;
			int dirx = 1;
			int diry = 0;
			
			int x = sx;
			int y = sy;
			
			
			while (true)
			{
				if (x>=0 && x<grid.length && y>=0 && y<grid[x].length)
				{
					double d = 0;
					
					boolean yes = true;
					
					//compute distance from rectangle
					if (x < xs && y < ys) d = Math.sqrt((x-xs)*(x-xs) + (y-ys)*(y-ys));
					else if (x >= xs && x < xe && y < ys) d = ys - y;
					else if (x >= xe && y < ys) d = Math.sqrt((x-xe+1)*(x-xe+1) + (y-ys)*(y-ys));
					else if (x < xs && y >= ys && y < ye) d = xs - x;
					else if (x >= xe && y >= ys && y < ye) d = x - xe+1;
					else if (x < xs && y >= ye) d = Math.sqrt((x-xs)*(x-xs) + (y-ye+1)*(y-ye+1));
					else if (x >= xs && x < xe && y >= ye) d = y - ye+1;
					else if (x >= xe && y >= ye) d = Math.sqrt((x-xe+1)*(x-xe+1) + (y-ye+1)*(y-ye+1));
					else yes = false;
					
					if (yes)
					{
						d = d*cellSize;
						
						double delta = R1-d; 
						
						if (delta > 0)
						{						
						double en = w*(delta*delta)/((R1-R0)*(R1-R0));
						if (Math.abs(en) > maxEn) maxEn = Math.abs(en);
						
						if (w > 0)
							grid[x][y] = Math.max(grid[x][y], en);
					else
							gridMinus[x][y] = Math.min(gridMinus[x][y], en);
						}
					}
				}
				
				x = x+dirx;
				y = y+diry;
				
				if (x == sx && y == sy)
					break;
				
				if (x == xe+b-1 && y==sy)
				{
					dirx = 0;
					diry = 1;
				}
				else if (x == xe+b-1 && y==ye+b-1)
				{
					dirx = -1;
					diry=0;
				}
				else if (x == sx && y== ye+b-1)
				{
					diry = -1;
					dirx = 0;
				}
			}
			
		
			if (maxEn == 0) break;	
			b++;
		}
		
	}


	public double[][] createGrid(double cellSize)
	{
		minX = Float.POSITIVE_INFINITY;
		minY = Float.POSITIVE_INFINITY;
		maxX = Float.NEGATIVE_INFINITY;
		maxY = Float.NEGATIVE_INFINITY;
		
		for (int j=0; j<sets[set].length; j++)
		{
			int i = sets[set][j];
			if (points[i].getX() < minX) minX = points[i].getX();
			if (points[i].getY() < minY) minY = points[i].getY();
			if (points[i].getX()+points[i].getWidth() > maxX) maxX = points[i].getX()+points[i].getWidth();
			if (points[i].getY()+points[i].getHeight() > maxY) maxY = points[i].getY()+points[i].getHeight();				
		}
		
		minX = minX - 2*R1;
		minY = minY - 2*R1;
		maxX = maxX + 2*R1;
		maxY = maxY + 2*R1;			
		
		int gridWidth = (int)Math.ceil((maxX-minX)/cellSize);
		maxX = minX + cellSize*gridWidth;
		int gridHeight = (int)Math.ceil((maxY-minY)/cellSize);
		maxY = minY + cellSize*gridHeight;
		
		grid = new double[gridWidth][];
		for (int i=0; i<gridWidth; i++)
		{
			grid[i] = new double[gridHeight];
			for (int j=0; j<grid[i].length; j++)
				grid[i][j] = 0;
		}
		
		Rectangle2D.Double rect = new Rectangle2D.Double(minX, minY, maxX-minX, maxY-minY);
		pointsIn = new boolean[points.length];
		
		for (int i=0; i<points.length; i++)
			if (rect.intersects(points[i]))
				pointsIn[i] = true;
			else pointsIn[i] = false;
		
		pointsD = new Rectangle[points.length];
		for (int i=0; i<points.length; i++)
		{
			int x = (int)Math.floor((points[i].getX() - minX)/cellSize);
			int y = (int)Math.floor((points[i].getY() - minY)/cellSize);
			int w = (int)Math.ceil(points[i].getWidth()/cellSize);
			int h = (int)Math.ceil(points[i].getHeight()/cellSize);
			pointsD[i] = new Rectangle(x,y,w,h);
		}	
		
		gridMinus = new double[grid.length][];
		for (int i=0; i<gridMinus.length; i++)
		{
			gridMinus[i] = new double[grid[i].length];
			for (int j=0; j<gridMinus[i].length; j++)
				gridMinus[i][j] = 0;
		}
	
		return grid;
	}
	
	private int[] findCentroid(int[] set)
	{
		int sumX = 0;
		int sumY = 0;
		for (int i=0; i<set.length; i++)
		{
			sumX += (pointsD[set[i]].x + pointsD[set[i]].width/2);
			sumY += (pointsD[set[i]].y + pointsD[set[i]].height/2);
		}
		sumX /= set.length;
		sumY /= set.length;
		
		int[] ret = new int[2];
		ret[0] = sumX; ret[1] = sumY;
		
		centroid = new Point2D.Double(sumX, sumY);
		
		return ret;
	}
	
	private int[] sortSet(int i)
	{
		int[] sortedSet = new int[sets[i].length];
		boolean[] moved = new boolean[sets[i].length];
		for (int j=0; j<moved.length; j++)
			moved[j] = false;
		
		int[] c= findCentroid(sets[i]);
		
		int mc = 0;
		while (mc < sets[i].length)
		{
			double mind = Double.MAX_VALUE;
			int index = -1;
			
			for (int j=0; j<sets[i].length; j++)
			{
				if (moved[j]) continue;
				
				double d = Math.sqrt((pointsD[sets[i][j]].getCenterX() - c[0])*(pointsD[sets[i][j]].getCenterX() - c[0]) +  
						(pointsD[sets[i][j]].getCenterY() - c[1])*(pointsD[sets[i][j]].getCenterY() - c[1]));
				if (!moved[j] && d < mind)
				{
					mind  = d;
					index = j;					
				}
			}
			
			sortedSet[mc] = sets[i][index];
			moved[index] = true;
			mc++;
		}
		
		
		return sortedSet;
	}
	
	private int[][] connect(int set, int[] sortedSet, int j)
	{
		//if (true)
		//return null;
		
		int opt = -1;
		
		double score = Double.MAX_VALUE;
		
		double x = points[sortedSet[j]].getCenterX();
		double y = points[sortedSet[j]].getCenterY();
		
		for (int i=0; i<j; i++)
		{
			double xi = points[sortedSet[i]].getCenterX();
			double yi = points[sortedSet[i]].getCenterY();
			
			int intersections = getNumberOfIntersections(set, sortedSet[j], sortedSet[i]) + 1;
			
			double dist = Math.sqrt((x-xi)*(x-xi) + (y-yi)*(y-yi));
			
			double scorei = dist * intersections;	
			
			if (scorei < score)
			{
				score = scorei;
				opt = i;
			}
		}
		
		if (opt < 0) return null;
		
		ArrayList<Point2D.Double> path = new ArrayList<Point2D.Double>();
		path.add(new Point2D.Double(points[sortedSet[j]].x + points[sortedSet[j]].width/2,points[sortedSet[j]].y + points[sortedSet[j]].height/2));
		path.add(new Point2D.Double(points[sortedSet[opt]].x + points[sortedSet[opt]].width/2,points[sortedSet[opt]].y + points[sortedSet[opt]].height/2));
		
		boolean intersected = true;
		int iterationct = 0;
		double buffer = 2*R0;
		while (intersected && iterationct<100 )
		{
			iterationct++;
			int object = -1;
			intersected = false;
			//look for intersections
			
			for (int i=0; i<sets.length; i++)
			{
				if (i == set) continue;
				
				for (int k=0; k<sets[i].length; k++)
				{
					if (!pointsIn[sets[i][k]])
						continue;
					
					for (int l=1; l<path.size(); l++)
					{
						if (points[sets[i][k]].intersectsLine(path.get(l).x, path.get(l).y, path.get(l-1).x, path.get(l-1).y))
						{
							//found an intersection
							Point2D.Double p = null;
							boolean swap = false;
							buffer = 2*R0;
							intersected = true;
							
							int intersectct = 0;
							while (p == null && buffer > R0 && intersectct<100)
							{
								intersectct++;
							
								Rectangle2D.Double r = points[sets[i][k]];
								Point2D.Double pr1 = new Point2D.Double(r.x, r.y);
								Point2D.Double pr2 = new Point2D.Double(r.x+r.width, r.y);
								Point2D.Double pr3 = new Point2D.Double(r.x+r.width, r.y+r.height);
								Point2D.Double pr4 = new Point2D.Double(r.x, r.y+r.height);
							
								//adjacent edge
								Line2D.Double l1 = new Line2D.Double(r.x, r.y, r.x + r.width, r.y);
								Line2D.Double l2 = new Line2D.Double(r.x+r.width, r.y, r.x + r.width, r.y+r.height);
								Line2D.Double l3 = new Line2D.Double(r.x+r.width, r.y+r.height, r.x, r.y+r.height);
								Line2D.Double l4 = new Line2D.Double(r.x, r.y+r.height, r.x, r.y);
							
								Line2D.Double line = new Line2D.Double(path.get(l).x, path.get(l).y, path.get(l-1).x, path.get(l-1).y);
							
								Point2D i1 = Util.getLineLineIntersection(l1, line); if (i1!=null && (i1.getX() < r.x  || i1.getX() > r.x+r.width)) i1 = null; 
								Point2D i2 = Util.getLineLineIntersection(l2, line); if (i2!=null && (i2.getY() < r.y || i2.getY() > r.y + r.height)) i2 = null;
								Point2D i3 = Util.getLineLineIntersection(l3, line); if (i3!=null && (i3.getX() < r.x  || i3.getX() > r.x+r.width)) i3 = null;
								Point2D i4 = Util.getLineLineIntersection(l4, line); if (i4!=null && (i4.getY() < r.y || i4.getY() > r.y + r.height)) i4 = null;
							
								if (i4 != null && i1 != null)
									p = new Point2D.Double(r.x - buffer, r.y - buffer);
								else if (i1 != null && i2 != null)
									p = new Point2D.Double(r.x + r.width + buffer, r.y - buffer);
								else if (i2 != null && i3 != null)
									p = new Point2D.Double(r.x + r.width + buffer, r.y + r.height + buffer);
								else if (i3 != null && i4 != null)
									p = new Point2D.Double(r.x - buffer, r.y + r.height + buffer);
								else if (i1 != null && i3 != null)
									p = routeOption(line, i1, i3,pr2, pr3, pr4, pr1, buffer, swap);
								else if (i2 != null && i4 != null )
									p = routeOption(line, i2, i4,pr3, pr4, pr1, pr2, buffer, swap); 
																
								if (swap) buffer = buffer*0.9;
								swap = !swap;								
								
								//is p in an obstacle?
								for (int q=0; q<sets[i].length; q++)
								{
									if (points[sets[i][q]].contains(p))
									{
										p = null;
										break;
									}
								}								
							}							
							if (p != null)
							{
								path.add(l,p);
								break;
							}
							buffer = 2*R0;
						}
					}
				}
			}
		}
		
		paths.add(path);
		
		int[][] ret = new int[path.size()][];
		for (int i=0; i<path.size(); i++)
		{
			ret[i] = new int[2];
			ret[i][0] = (int)Math.floor((path.get(i).getX() - minX)/cellSize);
			ret[i][1] = (int)Math.floor((path.get(i).getY() - minY)/cellSize);
		}		
		
		return ret;
	}
	
	private Point2D.Double routeOption(Line2D.Double line, Point2D i, Point2D j, Point2D c1, Point2D c2, Point2D c3, Point2D c4, double buffer, boolean swap)
	{
		boolean ismj = false;		
		if (i.distance(line.getP1()) <= j.distance(line.getP1()))
			ismj = true;		
		
		double A = (i.distance(c1) + j.distance(c2));
		double B = (i.distance(c4) + j.distance(c3));
		
		if (A > B)
		{
			if (!ismj)
			{
				if (swap) return new Point2D.Double(c1.getX() - Math.signum(c3.getX()-c1.getX())*buffer, c1.getY() - Math.signum(c3.getY()-c1.getY())*buffer);
				else return new Point2D.Double(c3.getX() - Math.signum(c1.getX()-c3.getX())*buffer, c3.getY() - Math.signum(c1.getY()-c3.getY())*buffer);
			}
			else 
			{
				if (swap) return new Point2D.Double(c2.getX() - Math.signum(c4.getX()-c2.getX())*buffer, c2.getY() - Math.signum(c4.getY()-c2.getY())*buffer);
				else return new Point2D.Double(c4.getX() - Math.signum(c2.getX()-c4.getX())*buffer, c4.getY() - Math.signum(c2.getY()-c4.getY())*buffer);
			}
		}
		else
		{
			if (!ismj)
			{
				if (swap) return new Point2D.Double(c3.getX() - Math.signum(c1.getX()-c3.getX())*buffer, c3.getY() - Math.signum(c1.getY()-c3.getY())*buffer);
				else return new Point2D.Double(c1.getX() - Math.signum(c3.getX()-c1.getX())*buffer, c1.getY() - Math.signum(c3.getY()-c1.getY())*buffer);
			}
			else 
			{
				if (swap) return new Point2D.Double(c4.getX() - Math.signum(c2.getX()-c4.getX())*buffer, c4.getY() - Math.signum(c2.getY()-c4.getY())*buffer);
				else return new Point2D.Double(c2.getX() - Math.signum(c4.getX()-c2.getX())*buffer, c2.getY() - Math.signum(c4.getY()-c2.getY())*buffer);
			}
		}
		
	}


	private int getNumberOfIntersections(int set, int p1, int p2) {
		int ct = 0;
		
		double x1 = points[p1].getCenterX();
		double y1 = points[p1].getCenterY();
		
		double x2 = points[p2].getCenterX();
		double y2 = points[p2].getCenterY();
		
		for (int i=0; i<sets.length; i++)
		{
			if (i == set) continue;
			
			for (int j=0; j<sets[i].length; j++)
			{
				if (points[sets[i][j]].intersectsLine(x1, y1, x2, y2))
					ct++;
			}
		}
		
		return ct;
	}
	
}

