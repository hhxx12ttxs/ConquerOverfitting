package perspectives.graph;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import javax.swing.SwingWorker;

import perspectives.base.ViewerContainer;

import perspectives.util.QuadTree;
import perspectives.util.Util;

public class BarnesHutGraphDrawer extends GraphDrawer{

	double k_rep, k_att,  max_step;
	
	double theta;
	
	private double spring_length, spring_length_square;
	
	int[] nodeDegrees;
	double[] rootNodeDegrees;
	
	QuadTree qt = null;
	
	
	int[][] components;
	int[] sortedComponents;
	
	int[] offx = new int[]{1,-1,0,0};
	int[] offy = new int[]{0,0,-1,1};
	int offcnt = 0;
	
	
	public BarnesHutGraphDrawer(Graph g) {
		super(g);
		
		this.computeConnectedComponents();
		
		
		
		k_rep = 100;
		k_att = 100.0;		
		max_step = 100;		
		spring_length = 30;	
		spring_length_square = 900;
		theta = 0.9;
		nodeDegrees = null;
		
		cacheNodeDegrees();			
		this.initBarnesHut();
		
	}
	
	public void setSpringLength(double sl )
	{
		spring_length = sl;
		spring_length_square = sl*sl;
	}

	@Override
	public void iteration() {
		
	//	System.out.println(spring_length );
		
		ArrayList<String> nodes = graph.getNodes();
		
		double[] fx = new double[nodes.size()];
		double[] fy = new double[nodes.size()];		
		for (int i=0; i<fx.length; i++)
		{
			fx[i] = 0; fy[i] = 0;			
		}	
		
		double[] posx = new double[nodes.size()];
		double[] posy = new double[nodes.size()];
		for (int i=0; i<posx.length; i++)
		{
			posx[i] = getX(i);
			posy[i] = getY(i);
		}
		
		long t1 = new Date().getTime();
		for (int c=0; c<components.length; c++)
		{
			if (components[c].length <= 3)
				continue;
			
			QuadTree tree = generateQuadTree(posx,posy, components[c]);
			qt = tree;
			
			if (components[c].length < 400)
			{
	 	    	for (int i=0; i<components[c].length; i++)
	 	    		calculateRepulsion(fx, fy, components[c][i], tree); 
	 	    	continue;
			}
			
			MySwingWorker w1 = new MySwingWorker(0,components[c].length/4, fx, fy, tree, components[c]);	w1.execute();
			MySwingWorker w2 = new MySwingWorker(components[c].length/4, components[c].length/2, fx, fy, tree, components[c]); w2.execute();
			MySwingWorker w3 = new MySwingWorker(components[c].length/2, 3*components[c].length/4, fx, fy, tree, components[c]); w3.execute();
			MySwingWorker w4 = new MySwingWorker(3*components[c].length/4, components[c].length, fx, fy, tree, components[c]); w4.execute();
			
			while (!w1.isDone || !w2.isDone || !w3.isDone || !w4.isDone)
				try {
					//thread.slee(10-ct+lastTime);
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
			
		long t4 = new Date().getTime();
	
			
			//compute spring forces
		ArrayList<Integer> e1 = new ArrayList<Integer>();
		ArrayList<Integer> e2 = new ArrayList<Integer>();
		graph.getEdgesAsIndeces(e1, e2);
			
		for (int i=0; i<e1.size(); i++)
		{
	
				int id1 = e1.get(i);
				double x1 = getX(id1);
				double y1 = getY(id1);	
				int id2 = e2.get(i);
				double x2 = getX(id2);
				double y2 = getY(id2);
				
				double d = Util.distanceBetweenPoints(x1, y1, x2, y2);
				
				if (d == 0) continue;
				
				double vx = x2-x1;
				double vy = y2-y1;
				double vl = Math.sqrt(vx*vx + vy*vy);
				vx = vx/vl;
				vy = vy/vl;
				
				double d1 = nodeDegrees[e1.get(i)];
				double d2 = nodeDegrees[e2.get(i)];		
				double degreeFactor = (Math.min(d1,d2));
				if (degreeFactor == 0) degreeFactor = 1;
				double adjusted_spring_length = spring_length * degreeFactor;
				
				//if (d < adjusted_spring_length)
				//	continue;
				
				//double mag = Math.signum(d-spring_length) * k_att*Math.pow((d-spring_length),1);
				double mag = d*d/adjusted_spring_length;
				
				//mag /= (degreeFactor);
				
				fx[id1] += vx *mag;
				fy[id1] += vy *mag;
				fx[id2] -= vx *mag;
				fy[id2] -= vy *mag;				
				
		}
				
		long t5 = new Date().getTime();
			
		//add forces to positions
		double cx = 0;
		double cy = 0;
		double cmy = 0;
		for (int k = 0; k<sortedComponents.length; k++)
		{
			double mnx = Double.MAX_VALUE;
			double mny = Double.MAX_VALUE;
			double mxx = Double.MIN_VALUE;
			double mxy = Double.MIN_VALUE;
			
			int c = sortedComponents[k];
			
			if (components[c].length <= 3)
			{
				setX(components[c][0], 0);
				setY(components[c][0], 0);
				mnx = -30; mny = -30;
				mxx = 30; mxy = 30;
				
				if (components[c].length > 1)
				{
					setX(components[c][1], spring_length);
					setY(components[c][1], 0);
					mnx = -30; mny = -30;
					mxx = spring_length  + 30; mxy = 30;
					if (components[c].length > 2)
					{
						setX(components[c][2], spring_length/2);
						setY(components[c][2], spring_length);
						mnx = -30; mny = -30;
						mxx = spring_length+30; mxy = spring_length+30;
					}
				}
			}
			
			
			for (int i=0; components[c].length > 3 && i<components[c].length; i++)
			{
				
				// getting the position from the position register
				double x = getX(components[c][i]);
				double y = getY(components[c][i]);
					
				double fl = Math.sqrt(fx[components[c][i]]*fx[components[c][i]] + fy[components[c][i]]*fy[components[c][i]]);
				if (fl > max_step)
				{
					fx[components[c][i]] = max_step * (fx[components[c][i]]/fl);
					fy[components[c][i]] = max_step * (fy[components[c][i]]/fl);
				}
				
				x = x+ fx[components[c][i]];
				y = y +fy[components[c][i]];
					
				setX(components[c][i],x);
				setY(components[c][i],y);	
				
				if (x < mnx) mnx = x; if (y<mny) mny = y;
				if (x > mxx) mxx = x; if (y>mxy) mxy = y;
			}
			if (components[c].length > 3)
			{
				mnx -= 30;
				mny -= 30;
				mxx += 30;
				mxy += 30;
			}
			
			for (int i=0; components[c].length > 3 && i<components[c].length; i++)
			{
				double x = getX(components[c][i]);
				double y = getY(components[c][i]);
				setX(components[c][i],x-mnx);
				setY(components[c][i],y-mny);	
			}
			
			for (int i=0; i<components[c].length; i++)
			{
				double x = getX(components[c][i]);
				double y = getY(components[c][i]);
				setX(components[c][i],x+cx);
				setY(components[c][i],y+cy);	
			}
			
			if (mxy-mny > cmy) cmy = mxy-mny;
			
			if (cx + (mxx-mnx) > spring_length * 20)
			{
				cx = 0;
				cy = cy + cmy;
				cmy = 0;
			}
			else
				cx = cx + (mxx-mnx);
			
			
			
			
			
			
		}
			
		long t6 = new Date().getTime();		
	
		System.out.println("drawer times: " + (t4-t1) + " " + (t5-t4) + " " + (t6-t5));
		
	//	max_step = Math.max(1,max_step * 0.95);
		max_step *= 0.99;
		
	//	qt = generateQuadTree(posx,posy);
		
	}
	
	
	
	
	
	
	
	/**
	 * Method which is called recursively to calculate repulsion between a vertex (Graph Node) and the quad tree (Tree Node).
	 * @param fxDouble Non Primitive data array for X 
	 * @param fyDouble Non Primitive data array for Y
	 * @param comparingNodeIndex The index of the Graph node which will be compared with the quad tree.
	 * @param quadTreeNode The current node of the tree. At first the node would be the root.
	 */
	private void calculateRepulsion(double[] fx, double[] fy, int nodeIndex, QuadTree tree)
	{
		// Getting the position of the current Graph node 
		double x1 = getX(nodeIndex);
		double y1 = getY(nodeIndex);
		
		// Degree of the Graph node
		double deg1 = rootNodeDegrees[nodeIndex];
		
		// Distance between Graph node and tree node. It is said as Radius
		double r = Util.distanceBetweenPoints(x1, y1, tree.getCenterX(), tree.getCenterY());
		
		// The diameter of the current quad in the quad tree. 
		double s = tree.getDiameter();
		
		int treeIndex = tree.getIndex();
		
		// Case -1: The case for the last lowest height of the tree
		if( 
			tree.isLeaf() // the tree node is the leaf node of the tree 
			&& treeIndex != nodeIndex // The tree node is not the current graph node 
			&& treeIndex >= 0 // The tree node is a valid Graph node. (If it is not a valid graph node then the index would be -1)
		)
		{
			
			
			//Getting the position of the current Tree node 
			double x2 = tree.getCenterX();
			double y2 = tree.getCenterY();
			
			double vx = x2-x1;
			double vy = y2-y1;
			//Getting the distance. If it is too low then keeping it distant.
			double dsq = vx*vx + vy*vy;
			double d = Math.sqrt(dsq);
			
			while (d < 1)
			{
				x2 = x2 + Math.random();
				y2 = y2 + Math.random();
				vx = x2-x1;
				vy = y2-y1;				
				dsq = vx*vx + vy*vy;
				d = Math.sqrt(dsq);
			}			
			vx /= d;
			vy /= d;
		
			//calculation DEL x and DEL y

			
			// calculation of repulsion
			double mag = -this.spring_length_square/dsq;
			double deg2 = rootNodeDegrees[treeIndex];
		
			// The cluster bug arises for the case if the graph node does not have any edges or Degree = 0. Then mag is always 0.
			// Thats why the vertices are static.
			mag = mag * Math.min(deg1, deg2);
		
			if (deg1 == 1 || deg2 == 1)
				mag = mag/10;			
			
			
			fx[nodeIndex] += vx *mag;
			fy[nodeIndex] += vy *mag;
		}
		//Case-2: case for the no need of going more deeper in the tree.
		else if(
				s/r < theta // If S< R * THETA then the quad is little enough so need to go deeper
				|| ( tree.isLeaf() &&  treeIndex < 0 )// If tree node is leaf but not a graph node.
				)
		{
			//Getting the position of the current Tree node 
			double x2 = tree.getCenterX();
			double y2 = tree.getCenterY();
			
			
			double vx = x2-x1;
			double vy = y2-y1;
			//Getting the distance. If it is too low then keeping it distant.
			double dsq = vx*vx + vy*vy;
			double d = Math.sqrt(dsq);
			while (d < 1)
			{
				x2 = x2 + Math.random();
				y2 = y2 + Math.random();
				vx = x2-x1;
				vy = y2-y1;				
				dsq = vx*vx + vy*vy;
				d = Math.sqrt(dsq);
			}			
			vx /= d;
			vy /= d;
			
			double mag = -this.spring_length_square/dsq;
			
			mag = mag * tree.getCenterMass();
			
			
			
			// As deg2 is not present so it is assumed that Min(deg2,deg1) = deg1
			mag = mag *deg1;
			
			if (deg1 == 1 )
				mag = mag/10;
			
			fx[nodeIndex] += vx *mag;
			fy[nodeIndex] += vy *mag;
		}
		//Case-3: The tree node has children. Go more deeper.
		else
		{	
			LinkedList<QuadTree> children =tree.getChildrenList(); 
			//Iterating the children list. 
			for(QuadTree child: children)
				// Calling with tree node child
				calculateRepulsion(fx, fy, nodeIndex,child);
		}
		 
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public QuadTree generateQuadTree(double[] posx, double[] posy, int[] indeces)
	{	
		double maxX = Integer.MIN_VALUE;
		double minX = Integer.MAX_VALUE;
		double maxY = Integer.MIN_VALUE;
		double minY = Integer.MAX_VALUE;
		
		for(int i =0; i< indeces.length;i++)
		{			
			if(posx[indeces[i]] > maxX)	maxX = posx[indeces[i]];
			if(posy[indeces[i]] > maxY)	maxY = posy[indeces[i]];
			if(posx[indeces[i]] < minX)	minX = posx[indeces[i]];
			if(posy[indeces[i]] < minY)	minY = posy[indeces[i]];
		}
		// Keeping the bounds a little bigger so that they dont collide with any vertices
		maxX++;
		maxY++;		
		minX--;
		minY--;			
		
		//calculate the max allowable tree height
		int maxTreeHeight =(int) Math.ceil( Math.log(posx.length) / Math.log(2));
		
		
		//create the tree recursively
		return createTreeNode(minX, minY, maxX, maxY, indeces,indeces.length, 0, maxTreeHeight);
		
	}
	

	private QuadTree createTreeNode(double minX, double minY, double maxX, double maxY, int[] indeces, int nodeCount, int height, int maxTreeHeight)
	{
		//Case-1: if only 1 node not remaining
		if(nodeCount > 1)
		{				
			double centerX  = 0;
			double centerY  = 0;
			
			int index = -1;
			double x = -1;
			double y = -1;
			
			double midX = (minX+ maxX) /2;
			double midY = (minY+maxY) /2;
			
			// Create node list for the newly created sub rectangles
			int[] quad1 = new int[nodeCount]; int cnt1 = 0;
			int[] quad2 = new int[nodeCount]; int cnt2 = 0;
			int[] quad3 = new int[nodeCount]; int cnt3 = 0;
			int[] quad4 = new int[nodeCount]; int cnt4 = 0;
			
			for(int i=0; i<nodeCount; i++)
			{				
				x = this.getX(indeces[i]);
				y = this.getY(indeces[i]);
				centerX+= x;
				centerY+= y;
				// put the node in the rectangle they belong
				if(x <= midX && y <= midY)
					quad1[cnt1++] = indeces[i];
				else if( x > midX && y <= midY)
					quad2[cnt2++] = indeces[i];
				else if( x <= midX && y > midY)
					quad3[cnt3++] = indeces[i];
				else
					quad4[cnt4++] = indeces[i];
				
				
			}
			//Calculate center of gravity
			centerX = centerX / nodeCount;
			centerY = centerY / nodeCount;
			
			// intialize a tree node
			QuadTree node = new QuadTree(-1, maxX, minX, maxY, minY, nodeCount, centerX, centerY, nodeCount) ;
			
			
			// If the height is less than the max height then its okay to add more children 
			if( height <= maxTreeHeight)
			{
				QuadTree node1 = null;
				QuadTree node2 = null;
				QuadTree node3 = null;
				QuadTree node4 = null;
				
				//Create tree node for each sub rectangle and add as a child
				if(cnt1 > 0)
				{				
					node1 = this.createTreeNode(minX, minY, midX, midY, quad1,cnt1, height+1, maxTreeHeight);
					node.addChild(node1);			
				}
				
				if(cnt2 > 0)
				{				
					node2 = this.createTreeNode(midX, minY, maxX, midY,  quad2,cnt2, height+1, maxTreeHeight);
					node.addChild(node2);
				}
				
				if(cnt3 > 0)
				{				
					node3 = this.createTreeNode(minX, midY, midX, maxY,  quad3,cnt3, height+1, maxTreeHeight);
					node.addChild(node3);
				}
				
				if(cnt4 > 0)
				{				
					node4 = this.createTreeNode(midX, midY, maxX, maxY,  quad4,cnt4, height+1, maxTreeHeight);
					node.addChild(node4);
				}
			}		
			return node;
		}
		//Case-2: Only 1 node available in the rectangle
		else
		{
			// get the index , x and y
			double x = getX(indeces[0]);
			double y = getY(indeces[0]);
			// create a tree node. The center mass is being 1 now.
			QuadTree node = new QuadTree(indeces[0], maxX, minX, maxY, minY, 1, x, y, 1);			
			return node;
		}
		
	}
	
	
	
	
	private void cacheNodeDegrees()
	{
		ArrayList<String> nodes = graph.getNodes();
		nodeDegrees = new int[nodes.size()];
		rootNodeDegrees = new double[nodes.size()];
		
	
		for (int i=0; i<nodes.size(); i++)
		{
			ArrayList<String> n = graph.neighbors(nodes.get(i));
			nodeDegrees[i] = n.size();
			if (n.indexOf(nodes.get(i)) >= 0)
				nodeDegrees[i]--;
			
			rootNodeDegrees[i] = Math.sqrt(nodeDegrees[i]);
		}
		
	/*	HashSet<String> deleted = new HashSet<String>();
		for (int k=0; k<10; k++)
		{
			System.out.println("deleted: " + deleted.size());
			//sw = false;
			for (int i=0; i<nodes.size(); i++)
				if (nodeDegrees[i] == 1)
					deleted.add(nodes.get(i));
			
			for (int i=0; i<nodes.size(); i++)
			{
				ArrayList<String> n = graph.neighbors(nodes.get(i));
				nodeDegrees[i] = n.size();
				for (int j=0; j<n.size(); j++)
					if (deleted.contains(n.get(j)))
					{
						nodeDegrees[i]--;
					}
			}
					
		}*/
	}
	
	/**
	 * Initializer method for BarnesHut implementation
	 */
	private void initBarnesHut()
	{
		this.createRandomPosition(); // create Random position to the graph nodes		
	}
	
	
	/**
	 * At first, all the graph nodes are put to (0,0). So it will be a bad quadTree for that. Thats why initially a randomize 
	 * Position is set to the graph nodes. 
	 * This method is called only once.
	 */
	private void createRandomPosition()
	{
		
		HashMap<String, Integer> nodeHashMap = new HashMap<String, Integer>();
		
		ArrayList<String> nodes = graph.getNodes(); // getting the nodes
		
		int totalNodes = nodes.size();
		Random rand = new Random();
		for(int i=0;i< totalNodes;i++)
		{
			double x = getX(i);
			double y= getY(i);
			String positionId = x+"-"+y; // keeping track of the position as x-y format.
			
			//Checks whether the position is assigned before or not
			if(nodeHashMap.containsKey(positionId))
			{
				//Checks whether the position is assigned before or not
				while (nodeHashMap.containsKey(positionId))
				{					
					x = x + Math.abs(rand.nextInt()) %2;
					y = y + Math.abs(rand.nextInt()) %2;
					positionId = x+"-"+y;
				}
				
				setX(i, x);
				setY(i, y);
				nodeHashMap.put(positionId, new Integer(i)); // keeping track of the newly assigned position
			}
			else
			{
				nodeHashMap.put(positionId, new Integer(i));
			}
		}
		
	}
	
	
	public void computeConnectedComponents()
	{
		ArrayList<String> nodes = graph.getNodes();
		int[] con = new int[nodes.size()];
		for (int i=0; i<con.length; i++)
			con[i] = 0;
		
		ArrayList<Integer> e1 = new ArrayList<Integer>();
		ArrayList<Integer> e2 = new ArrayList<Integer>();		
		graph.getEdgesAsIndeces(e1, e2);
		
		int cnt = 0;
		while (true)
		{
			int n = -1;
			for (int i=0; i<con.length; i++)
			{
				if (con[i] == 0) 
				{
					n = i;
					break;
				}
			}
			
			if (n < 0) break;
			
			cnt++;
			con[n] = cnt;
			int prevfound = 0;
			while (true)
			{
				int found = 0;
				for (int i=0; i<e1.size(); i++)
				{
					if (con[e1.get(i)] == cnt || con[e2.get(i)] == cnt)
					{
						con[e1.get(i)] = con[n];
						con[e2.get(i)] = con[n];
						found++;
					}
				}
				if (found == prevfound) break;
				prevfound = found;
			}
		}
		
		this.components = new int[cnt][];
		for (int i=0; i<cnt; i++)
		{
			int c = 0;
			for (int j=0; j<con.length; j++)
				if (con[j]-1 == i)
					c++;
			components[i] = new int[c];
			c = 0;
			for (int j=0; j<con.length; j++)
				if (con[j]-1 == i)
				{
					components[i][c] = j;
					c++;
				}							
		}
		
		sortedComponents = new int[components.length];
		for (int i=0; i<sortedComponents.length; i++)
			sortedComponents[i] = i;
		
		while (true)
		{
			boolean sw = false;
			for (int i=0; i<sortedComponents.length-1; i++)
			{
				if (components[sortedComponents[i]].length < components[sortedComponents[i+1]].length)
				{
					int aux = sortedComponents[i];
					sortedComponents[i] = sortedComponents[i+1];
					sortedComponents[i+1] = aux;
					sw=true;
				}
			}
			if (!sw) break;
		}
			
	}
	
	
	
	
	//swingworker class doing simulation and rendering in the background for some classes of viewers
	class MySwingWorker extends SwingWorker<Integer, Void>
	{		
		int startIndex, endIndex;
		double[] fx,fy;
		QuadTree tree;
		
		boolean isDone = false;
		
		int[] indeces;
		
		public MySwingWorker(int si, int ei, double[] fx, double[] fy, QuadTree tree, int[] ind)
		{
			startIndex = si;
			endIndex = ei;
			this.fx = fx;
			this.fy = fy;
			this.tree = tree;
			isDone = false;
			indeces = ind;
		}		
		 
 	    public Integer doInBackground() {
 	    	
 	    	for (int i=startIndex; i<endIndex; i++)
 	    		calculateRepulsion(fx, fy, indeces[i], tree); 
 	 	  	            
 	        return new Integer(0);
 	    }

 	    @Override
 	    public void done() {
 	    	isDone = true;
 	    	
 	    }
		
	}

	
	
	
	
	
	
	
	

}

