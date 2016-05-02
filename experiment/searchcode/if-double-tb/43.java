package strategy.movement;

import java.util.ArrayList;

import world.state.WorldInterface;

import comms.control.ServerInterface;
import geometry.Vector;

public class GoToPoint {
	
	private static double usX, usY, themX, themY, ballX, ballY, destX, destY;
	private static double dxUsNode, dyUsNode, dxUsDest, dyUsDest, dxDestNode, dyDestNode,
							dxo1o2, dxo1Node, dyo1Node, dxo2Node, dyo2Node;
	private static double distUsNode, distUsDest, distDestNode, distO1Node, distO2Node;
	
	private static double[][] obst1Circle = new double[16][2];
	private static double[][] obst2Circle = new double[16][2];
	
	private static final double enemySafeDist = 60;//0.4;
	private static final double ballSafeDist = 62;//0.2;
	private static final double edgeSafeDist = 20;//0.05;
	private static final double epsilon = 5;//0.02;
	
	private static int nodeClosestToUs =0;
	private static int nodeClosestToDest =0;
	private static int nodeClosestToO1 =0;
	private static int nodeClosestToO2 =0;
	
	private static double nodeSize = 2.0*Math.PI*enemySafeDist/16.0;
	
	public static final float pitchL = 600;// 625;//600;
    public static final float pitchW = 330; // 315;//330;
	
	//private static double[][] path = new double[18][2];
	
	//the real mod 16, always returning a positive number
	public static int mod16(int x)
	{
		while(x < 0)
			x+=16;
		while(x>=16)
			x-=16;
		return x;
	}
	
	//it will depend on the coord system
	public static boolean tooCloseToEdge(double x, double y)
	{
		//for side pitch: margin = (37,25)
		
		if(Math.abs(39 - x) < edgeSafeDist)
			return true;
		if(Math.abs(pitchL + 39 - x) < edgeSafeDist)
			return true;
		if(Math.abs(46 -y) < edgeSafeDist)
			return true;
		if(Math.abs(pitchW + 46 - y) < edgeSafeDist)
			return true;
		
		return false;
	}
	
	public static void goToPoint(WorldInterface world, ServerInterface rc, Vector point, AvoidanceStrategy mode)
	{
		double x = point.getX();
		double y = point.getY();
		//mode: 0-defensive 1-retrieval 2-offensive
		usX = world.getOurRobot().x;
		usY = world.getOurRobot().y;
		themX = world.getTheirRobot().x;
		themY = world.getTheirRobot().y;
		ballX = world.getBall().x;
		ballY = world.getBall().y;
		destX = x;
		destY = y;
		
		double o1x=0, o1y=0, o2x=0, o2y=0, r1=0, r2=0;
		
		double angle = 0.0;
		//initialize distUsNode to "infinity"
		distUsNode = 1000.0;
		distDestNode = 1000.0;
		distO1Node = 1000.0;
		distO2Node = 1000.0;
		
		boolean twoObstacles = false;
		
		ArrayList<double[]> path = new ArrayList<double[]>();
		
		//first determine which object is closer to our robot
		if(mode != AvoidanceStrategy.AvoidingBall)
		{
			o1x = themX;
			o1y = themY;
			r1 = enemySafeDist;
		}
		else
		{
			//l: p = a + t*(b-a)
			double ax = usX;
			double ay = usY;
			
			double bx = destX;
			double by = destY;
			
			double ox = themX;
			double oy = themY;
			
			double tE = ((ox-ax)*(bx-ax) + (oy-ay)*(by-ay)) / ((bx-ax)*(bx-ax) + (by-ay)*(by-ay));
			
			double distSqrE;
			
			double px = ax + tE*(bx-ax);
			double py = ay + tE*(by-ay);
			
			if((tE>=0)&&(tE<=1))
			{
				distSqrE = (px-ox)*(px-ox) + (py-oy)*(py-oy);
			}
			else
			{
				double distSqrA = (ax-ox)*(ax-ox) + (ay-oy)*(ay-oy);
				double distSqrB = (bx-ox)*(bx-ox) + (by-oy)*(by-oy);
				distSqrE = Math.min(distSqrA, distSqrB);
			}
			
			double distSqrBl;
			
			ox = ballX;
			oy = ballY;
			
			double tB = ((ox-ax)*(bx-ax) + (oy-ay)*(by-ay)) / ((bx-ax)*(bx-ax) + (by-ay)*(by-ay));
			
			px = ax + tE*(bx-ax);
			py = ay + tE*(by-ay);
			
			if((tE>=0)&&(tE<=1))
			{
				distSqrBl = (px-ox)*(px-ox) + (py-oy)*(py-oy);
			}
			else
			{
				double distSqrA = (ax-ox)*(ax-ox) + (ay-oy)*(ay-oy);
				double distSqrB = (bx-ox)*(bx-ox) + (by-oy)*(by-oy);
				distSqrBl = Math.min(distSqrA, distSqrB);
			}
			
			if((tE>=0)&&(tE<=1)&&(tB>=0)&&(tB<=1))
			{
				//No time for that now- use the greedy approach.
				//twoObstacles = true;
				
				
				
				
				if((tE < tB)&&(distSqrE < enemySafeDist*enemySafeDist))
				{
					//System.out.println("First enemy then ball");
					//enemy is the first obstacle
					o1x = themX;
					o1y = themY;
					r1 = enemySafeDist;
					
					o2x = ballX;
					o2y = ballY;
					r2 = ballSafeDist;
				}
				else// if((distSqrBl < ballSafeDist*ballSafeDist))
				{
					//System.out.println("First ball then enemy");
					//ball is the first obstacle
					o2x = themX;
					o2y = themY;
					r2 = enemySafeDist;
					
					o1x = ballX;
					o1y = ballY;
					r1 = ballSafeDist;
				}
				/*
				else
				{
					double vectX = destX - usX;
					double vectY = destY - usY;
					FollowVector.followVector(world, rc, vectX, vectY);
					return;
				}*/
			}
			else
			{
				twoObstacles = false;
				if((tE>=0)&&(tE<=1))
				{
					//System.out.println("Only enemy");
					//enemy is the only obstacle
					o1x = themX;
					o1y = themY;
					r1 = enemySafeDist;
				}
				else if((tB>=0)&&(tB<=1))
				{
					//System.out.println("Only ball");
					//ball is the only obstacle
					o1x = ballX;
					o1y = ballY;
					r1 = ballSafeDist;
				}
				else
				{
					//System.out.println("Nada");
					double vectX = destX - usX;
					double vectY = destY - usY;
					FollowVector.followVector(world, rc, vectX, vectY);
					return;
				}
				
				
				
			}
			
			
		}
		
		
		/*
		if(twoObstacles)
		{
			for(int i =0; i<16; i++)
			{
				obst1Circle[i][0] = o1x + r1*Math.cos(angle);
				obst1Circle[i][1] = o1y + r1*Math.sin(angle);
				
				dxUsNode = obst1Circle[i][0] - usX;
				dyUsNode = obst1Circle[i][1] - usY;
				
				double newDistUsNode = Math.sqrt(dxUsNode*dxUsNode + dyUsNode*dyUsNode);
				if(newDistUsNode < distUsNode)
				{
					distUsNode = newDistUsNode;
					nodeClosestToUs = i;
				}
				
				dxo2Node = obst1Circle[i][0] - o2x;
				dyo2Node = obst1Circle[i][1] - o2y;
				
				double newDistO2Node = Math.sqrt(dxo2Node*dxo2Node + dyo2Node*dyo2Node);
				if(newDistO2Node < distO2Node)
				{
					distO2Node = newDistO2Node;
					nodeClosestToO2 = i;
				}
				
				angle += (2.0*Math.PI)/16.0;
			}
			
			angle = 0;
			for(int i =0; i<16; i++)
			{
				obst2Circle[i][0] = o2x + r2*Math.cos(angle);
				obst2Circle[i][1] = o2y + r2*Math.sin(angle);
				
				dxo1Node = obst2Circle[i][0] - o1x;
				dyo1Node = obst2Circle[i][1] - o1y;
				
				double newDistO1Node = Math.sqrt(dxo1Node*dxo1Node + dyo1Node*dyo1Node);
				if(newDistO1Node < distO1Node)
				{
					distO1Node = newDistO1Node;
					nodeClosestToO1 = i;
				}
				
				dxDestNode = obst1Circle[i][0] - destX;
				dyDestNode = obst1Circle[i][1] - destY;
				
				double newDistDestNode = Math.sqrt(dxDestNode*dxDestNode + dyDestNode*dyDestNode);
				if(newDistDestNode < distDestNode)
				{
					distDestNode = newDistDestNode;
					nodeClosestToDest = i;
				}
				
				angle += (2.0*Math.PI)/16.0;
			}
			
			
			
			
			int dir1 = 1;
			if(mod16(nodeClosestToO2 - nodeClosestToUs) > 8)
				dir1 = -1;
			
			for(int i = mod16(nodeClosestToUs + dir1); i != nodeClosestToO2 ; i = mod16(i+dir1))
			{
				if(tooCloseToEdge(obst1Circle[i][0], obst1Circle[i][1]))
				{
					dir1 *= -1;
					break;
				}
				
			}
			
			int dir2 = 1;
			if(mod16(nodeClosestToDest - nodeClosestToO1) > 8)
				dir2 = -1;
			
			for(int i = mod16(nodeClosestToO1 + dir2); i != nodeClosestToDest ; i = mod16(i+dir2))
			{
				if(tooCloseToEdge(obst2Circle[i][0], obst2Circle[i][1]))
				{
					dir2 *= -1;
					break;
				}
				
			}
			
			System.out.println("dirs: " + dir1 + "\t" + dir2);
			
			//System.out.println("diff= " + mod16(nodeClosestToO2 - nodeClosestToUs));
			
			//and now add it all to the path
			
			path.add(new double[]{usX, usY});
			
			for(int i = nodeClosestToUs; ; i = mod16(i+dir1))
			{
				//System.out.println("i= " + mod16(i+dir));
				path.add(obst1Circle[i]);
				if(i == nodeClosestToO2)
					break;
			}
			
			for(int i = nodeClosestToO1; ; i = mod16(i+dir2))
			{
				//System.out.println("i= " + mod16(i+dir));
				path.add(obst1Circle[i]);
				if(i == nodeClosestToDest)
					break;
			}
			
			path.add(new double[]{destX, destY});
			
			//string pulling
			
			int from = 0;
			//for(int i = 0; i < path.size() - 1; i++)
			//while(from < path.size() - 2)
			{
				//for(int to = from + 2; to < path.size(); to++)
				for(int to = path.size() - 1; to > from + 1; to--)
				{
					//l: p = a + t*(b-a)
					double ax = path.get(from)[0];
					double ay = path.get(from)[1];
					
					double bx = path.get(to)[0];
					double by = path.get(to)[1];
					
					double ox = o1x;
					double oy = o1y;
					
					double t = ((ox-ax)*(bx-ax) + (oy-ay)*(by-ay)) / ((bx-ax)*(bx-ax) + (by-ay)*(by-ay));
					
					double distSqr1 = 0;
					
					double px = ax + t*(bx-ax);
					double py = ay + t*(by-ay);
					
					if((t>=0)&&(t<=1))
					{
						distSqr1 = (px-ox)*(px-ox) + (py-oy)*(py-oy);
					}
					else
					{
						double distSqrA = (ax-ox)*(ax-ox) + (ay-oy)*(ay-oy);
						double distSqrB = (bx-ox)*(bx-ox) + (by-oy)*(by-oy);
						distSqr1 = Math.min(distSqrA, distSqrB);
					}
					
					ox = o2x;
					oy = o2y;
					
					t = ((ox-ax)*(bx-ax) + (oy-ay)*(by-ay)) / ((bx-ax)*(bx-ax) + (by-ay)*(by-ay));
					
					double distSqr2 = 0;
					
					px = ax + t*(bx-ax);
					py = ay + t*(by-ay);
					
					if((t>=0)&&(t<=1))
					{
						distSqr2 = (px-ox)*(px-ox) + (py-oy)*(py-oy);
					}
					else
					{
						double distSqrA = (ax-ox)*(ax-ox) + (ay-oy)*(ay-oy);
						double distSqrB = (bx-ox)*(bx-ox) + (by-oy)*(by-oy);
						distSqr2 = Math.min(distSqrA, distSqrB);
					}
					
					if((distSqr1 >= r1*r1 - epsilon)&&
							(distSqr2 >= r2*r2 - epsilon))
					{
						//System.out.println("yes");
						//int iter = 0;
						//for(int k = from + 1; k < to; k++)
						while(from + 1 < to)
						{
							path.remove(from + 1);
							to--;
						}
						break;
					}
					else
					{
					}
					
					//else
						//from++;
				}
				//from++;
			}
			
			while(!path.isEmpty())
			{
				double dx = path.get(0)[0] - usX;
				double dy = path.get(0)[1] - usY;
				if(dx*dx + dy*dy < nodeSize*nodeSize)
					path.remove(0);
				else
					break;
			}
			
			//System.out.println("dir: " + dir);
			
			if(!path.isEmpty())
			{
			
				double vectX = path.get(0)[0] - usX;
				double vectY = path.get(0)[1] - usY;
				
				FollowVector.followVector(world, rc, vectX, vectY);
			}
			
		}
		
		else
		*/
		{
			//In our system, positive angles are to the right, so the nodes are ordered clockwise
			for(int i =0; i<16; i++)
			{
				obst1Circle[i][0] = o1x + r1*Math.cos(angle);
				obst1Circle[i][1] = o1y + r1*Math.sin(angle);
				
				dxUsNode = obst1Circle[i][0] - usX;
				dyUsNode = obst1Circle[i][1] - usY;
				
				double newDistUsNode = Math.sqrt(dxUsNode*dxUsNode + dyUsNode*dyUsNode);
				if(newDistUsNode < distUsNode)
				{
					distUsNode = newDistUsNode;
					nodeClosestToUs = i;
				}
				
				dxDestNode = obst1Circle[i][0] - destX;
				dyDestNode = obst1Circle[i][1] - destY;
				
				double newDistDestNode = Math.sqrt(dxDestNode*dxDestNode + dyDestNode*dyDestNode);
				if(newDistDestNode < distDestNode)
				{
					distDestNode = newDistDestNode;
					nodeClosestToDest = i;
				}
				
				angle += (2.0*Math.PI)/16.0;
			}
			
			path.add(new double[]{usX, usY});
			
			//determine direction
			int dir = 1;
			if(mod16(nodeClosestToDest - nodeClosestToUs) > 8)
				dir = -1;
			
			//check if wont crash into a wall
			for(int i = mod16(nodeClosestToUs + dir); i != nodeClosestToDest ; i = mod16(i+dir))
			{
				if(tooCloseToEdge(obst1Circle[i][0], obst1Circle[i][1]))
				{
					dir *= -1;
					break;
				}
				
			}
			
			
			//System.out.println("diff= " + mod16(nodeClosestToDest - nodeClosestToUs));
			//System.out.println("clDest= " + mod16(nodeClosestToDest));
			//System.out.println("clUs= " + mod16(nodeClosestToUs));
			for(int i = nodeClosestToUs; ; i = mod16(i+dir))
			{
				//System.out.println("i= " + mod16(i+dir));
				path.add(obst1Circle[i]);
				if(i == nodeClosestToDest)
					break;
			}
			path.add(new double[]{destX, destY});
			
			
			//System.out.println("before: " + path.size());
			
			//string pulling
			int from = 0;
			//for(int i = 0; i < path.size() - 1; i++)
			//while(from < path.size() - 2)
			{
				//for(int to = from + 2; to < path.size(); to++)
				for(int to = path.size() - 1; to > from + 1; to--)
				{
					//l: p = a + t*(b-a)
					double ax = path.get(from)[0];
					double ay = path.get(from)[1];
					
					double bx = path.get(to)[0];
					double by = path.get(to)[1];
					
					double ox = o1x;
					double oy = o1y;
					
					double t = ((ox-ax)*(bx-ax) + (oy-ay)*(by-ay)) / ((bx-ax)*(bx-ax) + (by-ay)*(by-ay));
					
					double distSqr = 0;
					
					double px = ax + t*(bx-ax);
					double py = ay + t*(by-ay);
					
					if((t>=0)&&(t<=1))
					{
						distSqr = (px-ox)*(px-ox) + (py-oy)*(py-oy);
					}
					else
					{
						double distSqrA = (ax-ox)*(ax-ox) + (ay-oy)*(ay-oy);
						double distSqrB = (bx-ox)*(bx-ox) + (by-oy)*(by-oy);
						distSqr = Math.min(distSqrA, distSqrB);
					}
					
					if(distSqr >= r1*r1 - epsilon)
					{
						//System.out.println("yes");
						//int iter = 0;
						//for(int k = from + 1; k < to; k++)
						while(from + 1 < to)
						{
							path.remove(from + 1);
							to--;
						}
						break;
					}
					else
					{
					}
					
					//else
						//from++;
				}
				//from++;
			}
			
			while(!path.isEmpty())
			{
				double dx = path.get(0)[0] - usX;
				double dy = path.get(0)[1] - usY;
				if(dx*dx + dy*dy < nodeSize*nodeSize)
					path.remove(0);
				else
					break;
			}
			
			//System.out.println("dir: " + dir);
			
			if(!path.isEmpty())
			{
				distUsDest = Math.sqrt((usX-destX)*(usX-destX) + (usY-destY)*(usY-destY));
			
				double vectX = path.get(0)[0] - usX;
				double vectY = path.get(0)[1] - usY;
				if(mode == AvoidanceStrategy.Aggressive)
					FollowVector.followVectorNoRotate(world, rc, vectX, vectY, distUsDest);
				else if(mode == AvoidanceStrategy.VariableRadius)
					FollowVector.arcToPoint(world, rc, new Vector(destX, destY));
				else
					FollowVector.followVector(world, rc, vectX, vectY);
			}
		}
	}

}

