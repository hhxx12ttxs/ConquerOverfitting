/* John Stockwell
 * 5/10/2011
 * PURPOSE: 
 * the purpose of this program is to allow a programmer to interact with a graph and 
 * perform basic operations on it.  
 * 
 * CHANGES:
 * the only changes that were made were to the path() method in the graph class, and the 
 * main method for the app. basically what i did was make nested for loops to output a 
 * matrix showing the values of all the edges on the graph and which nodes were connected 
 * by those edges. the other thing i did was change the method itself so that, rather than
 * make comparisons and decisions assuming that the user wanted the shortest path from the
 * first node to the last node, i made it so that the nodes to be traversed could selected 
 * by passing method parameters. outside of that, the method was left unchanged. the output 
 * for the display method now shows the shortest path ONLY between the nodes that were 
 * selected. there is still, however, some issue with the getMin method, to which it only 
 * gets the shortest distances from the first node to all the other nodes individually. 
 * which can be seen in the output... i didn't really feel like fixing it since i didn't 
 * totally understand where the values were going to and coming from...and because the two 
 * classmates i sit next to said it didn't matter...*/

////////////////////////////////////////////////////////////////

class DistPar // distance and parent
{ // items stored in sPath array
	public int distance; // distance from start to this vertex
	public int parentVert; // current parent of this vertex

	// -------------------------------------------------------------

	public DistPar(int pv, int d) // constructor
	{
		distance = d;
		parentVert = pv;
	}
	// -------------------------------------------------------------
} // end class DistPar
// /////////////////////////////////////////////////////////////

class Vertex {
	public char label; // label (e.g. 'A')
	public boolean isInTree;

	// -------------------------------------------------------------
	public Vertex(char lab) // constructor
	{
		label = lab;
		isInTree = false;
	}
	// -------------------------------------------------------------
} // end class Vertex
// //////////////////////////////////////////////////////////////

class Graph {
	private final int MAX_VERTS = 20;
	private final int INFINITY = -1;
	private Vertex vertexList[]; // list of vertices
	private int adjMat[][]; // adjacency matrix
	private int nVerts; // current number of vertices
	private int nTree; // number of verts in tree
	private DistPar sPath[]; // array for shortest-path data
	private int currentVert; // current vertex
	private int startToCurrent; // distance to currentVert
	private int arrayElemDist;
	private int arrayElemDistStart;

	// -------------------------------------------------------------
	public Graph() // constructor
	{
		vertexList = new Vertex[MAX_VERTS];
		// adjacency matrix
		adjMat = new int[MAX_VERTS][MAX_VERTS];
		nVerts = 0;
		nTree = 0;
		for (int j = 0; j < MAX_VERTS; j++)
			// set adjacency
			for (int k = 0; k < MAX_VERTS; k++)
				// matrix
				adjMat[j][k] = INFINITY; // to infinity
		sPath = new DistPar[MAX_VERTS]; // shortest paths
	} // end constructor

	// -------------------------------------------------------------

	public void addVertex(char lab) {
		vertexList[nVerts++] = new Vertex(lab);
	}

	// -------------------------------------------------------------
	public void addEdge(int start, int end, int weight) {
		adjMat[start][end] = weight; // (directed)
	}

	// CHANGED FOR CHAPTER14 PROGRAM 1------------------------------
	public void path(int startTree, int endTree) // find all shortest paths
	{
		// int startTree = 0; // start at vertex 0
		arrayElemDist = endTree;
		arrayElemDistStart = startTree;
		vertexList[startTree].isInTree = true;
		nTree = 1; // put it in tree

		// transfer row of distances from adjMat to sPath
		for (int j = 0; j <= endTree; j++) {
			int tempDist = adjMat[startTree][j];
			sPath[j] = new DistPar(startTree, tempDist);
		}
		
///////////////////////////////////////////////
		int arnold;
		char charley;
		for(int i=0; i<=4; i++)
		{
			arnold=(65+i);
			charley=(char)arnold;
			System.out.print("  "+charley);
		}
		System.out.println();
		for(int i=0; i<=4; i++)
		{arnold=(65+i);
		charley=(char)arnold;
			for(int h=0; h<=4; h++)
			{
				System.out.print(" "+adjMat[h][i]);
			}
			System.out.print(" "+charley);
			System.out.println();
		}
/////////////////////////////////////////////////////

		// until all vertices are in the tree
		while (nTree <= endTree) {
			int indexMin = getMin(); // get minimum from sPath
			int minDist = sPath[indexMin].distance;

			if (minDist == INFINITY) // if all infinite
			{ // or in tree,
				System.out.println("There are unreachable vertices");
				break; // sPath is complete
			} else { // reset currentVert
				currentVert = indexMin; // to closest vert
				startToCurrent = sPath[indexMin].distance;
				// minimum distance from startTree is
				// to currentVert, and is startToCurrent
			}
			// put current vertex in tree
			vertexList[currentVert].isInTree = true;
			nTree++;
			adjust_sPath(); // update sPath[] array
		} // end while(nTree<nVerts)

		displayPaths(); // display sPath[] contents

		nTree = 0; // clear tree
		for (int j = 0; j < nVerts; j++)
			vertexList[j].isInTree = false;
	} // end path()

	// -------------------------------------------------------------

	public int getMin() // get entry from sPath
	{ // with minimum distance
		int minDist = INFINITY; // assume minimum
		int indexMin = 0;
		for (int j = 1; j < arrayElemDist; j++) // for each vertex,
		{ // if it's in tree and
			if (vertexList[j].isInTree && sPath[j].distance < minDist) 
			{
				minDist = sPath[j].distance;
				indexMin = j; // update minimum
			}
		} // end for
		return indexMin; // return index of minimum
	} // end getMin()

	// -------------------------------------------------------------

	public void adjust_sPath() {
		// adjust values in shortest-path array sPath
		int column = 1; // skip starting vertex
		while (column <= arrayElemDist) // go across columns
		{
			// if this column's vertex already in tree, skip it
			if (vertexList[column].isInTree) {
				column++;
				continue;
			}
			// calculate distance for one sPath entry
			// get edge from currentVert to column
			int currentToFringe = adjMat[currentVert][column];
			// add distance from start
			int startToFringe = startToCurrent + currentToFringe;
			// get distance of current sPath entry
			int sPathDist = sPath[column].distance;

			// compare distance from start with sPath entry
			if (startToFringe < sPathDist) // if shorter,
			{ // update sPath
				sPath[column].parentVert = currentVert;
				sPath[column].distance = startToFringe;
			}
			column++;
		} // end while(column < nVerts)
	} // end adjust_sPath()

	// -------------------------------------------------------------

	public void displayPaths() {
		for (int j = arrayElemDistStart; j <= arrayElemDist; j++) // display
		// contents
		// of
		// sPath[]
		{
			System.out.print(vertexList[j].label + "="); // B=
			if (sPath[j].distance == INFINITY)
				System.out.print("inf"); // inf
			else
				System.out.print(sPath[j].distance); // 50
			char parent = vertexList[sPath[j].parentVert].label;
			System.out.print("(" + parent + ") "); // (A)
		}
		System.out.println("");
	}
	// -------------------------------------------------------------
} // end class Graph
// //////////////////////////////////////////////////////////////

class PathApp {
	public static void main(String[] args) {
		Graph theGraph = new Graph();
		theGraph.addVertex('A'); // 0 (start)
		theGraph.addVertex('B'); // 1
		theGraph.addVertex('C'); // 2
		theGraph.addVertex('D'); // 3
		theGraph.addVertex('E'); // 4

		theGraph.addEdge(0, 1, 50); // AB 50
		theGraph.addEdge(0, 3, 80); // AD 80
		theGraph.addEdge(1, 2, 60); // BC 60
		theGraph.addEdge(1, 3, 90); // BD 90
		theGraph.addEdge(2, 4, 20); // CE 40
		theGraph.addEdge(3, 2, 20); // DC 20
		theGraph.addEdge(3, 4, 70); // DE 70
		theGraph.addEdge(4, 1, 50); // EB 50
		theGraph.addEdge(2, 3, 20); // CD 20

		System.out.println("Shortest paths: ");
		theGraph.path(1, 4); // shortest paths
		System.out.println();
	} // end main()
} // end class PathApp
//OUTPUT--------------------------------------------------

/*
 * DESCRIPTION:
 * notice that the values output in the matrix follow those that are shown above in the main method.
 * the values are arranged so that the values on the top mark where the edge starts, while the values
 * on the right mark where the edge stops. also note that the output from the display method at the 
 * bottom shows the starting node, it's shortest edge to it's parent, and the parent node; and, since
 * B is not connected to B, the value is -1 and therefore considered infinite by the program. also 
 * note that E is not connected to B, and is also considered infinite. 

Shortest paths: 
  A  B  C  D  E
 -1 -1 -1 -1 -1 A
 50 -1 -1 -1 50 B
 -1 60 -1 20 -1 C
 80 90 20 -1 -1 D
 -1 -1 20 70 -1 E
There are unreachable vertices
B=inf(B) C=60(B) D=90(B) E=inf(B) 

*/


