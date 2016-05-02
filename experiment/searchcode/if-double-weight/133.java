import java.util.*;
import java.io.*;
/**
 * NerdBook makes use of Graph class to build a social networking application. The class is
 * dependent on nodes.txt and edges.txt being in same directory as application. Four different problems
 * can be solved with NerdBook. message, interview, recommender, and cohesion. Entering one of these four
 * as the parameter for NerdBook will solve that problem. Message finds the shortest path from each person
 * in the NerdBook to each other person. Interview finds the smallest path of disagreement from each
 * person to each other person in the graph. Recommender recommends the top 5 possible friends 
 * (who are not already friends) with the lowest disagreements for each person in NerdBook. Cohesion 
 * finds the total disagreement among all people in NerdBook.
 *
 * @author  Ryan Fisher
 **/
public class NerdBook
{
	Graph<String> nerdBook;

	/**
	 * Default Constructor instantiates a Graph
	 **/
	public NerdBook() {
		nerdBook = new Graph<String>();
	}

	/**
	 * Constructor
	 * @param  nodes  a file location for nodes to be entered into graph
	 * @param  edges  a file location for edges to be entered into graph
	 * @throws IOException  if file is corrupt
	 * @throws FileNotFoundException  if file nodes or edges not found
	 **/
	public NerdBook(String nodes, String edges) throws IOException, FileNotFoundException {
		nerdBook = new Graph<String>();
		BufferedReader in = null;

		in = new BufferedReader(new FileReader(new File(nodes)));
	
		ArrayList<String> names = new ArrayList<String>();
		boolean EOF = false;
		String name = in.readLine();

		while (name != null) {
			nerdBook.addVertex(name);
			names.add(name);

			name = in.readLine();
		}

		in = new BufferedReader(new FileReader(new File(edges)));

		String line = in.readLine();
		int vertex1;
		int vertex2;
		double weight;
		String number;

		while (line != null) {
			vertex1 = Integer.parseInt(line.substring(0, line.indexOf(" ")));
			line = line.substring(line.indexOf(" ") + 1);
			vertex2 = Integer.parseInt(line.substring(0, line.indexOf(" ")));
			line = line.substring(line.indexOf(" ") + 1);
			weight = Double.parseDouble(line);

			nerdBook.addEdge(names.get(vertex1), names.get(vertex2), weight);

			line = in.readLine();
		}
	}

	/**
	 * Returns a collection of people in instance of NerdBook
	 *
	 * @return  a collection of people in NerdBook
	 **/
	public Collection<String> getPeople() {
		return nerdBook.getVertices();
	}

	/**
	 * Prints shortest sequeces of all people in NerdBook from the specified person
	 *
	 * @param  person  person to print sequences from
	 **/
	public void shortestSequences(String person) {
		Collection<String> people = nerdBook.getVertices();
		Iterator<String> peopleIter = people.iterator();
		String person2;
		while (peopleIter.hasNext()) {
			person2 = peopleIter.next();
			if (!person.equals(person2)) {
				LinkedList<String> path = nerdBook.shortestUnweightedPath(person, person2);
				Iterator iter = path.iterator();
				System.out.print(iter.next());
				while (iter.hasNext()) System.out.print(" -> " + iter.next());
				System.out.println();
			}
		}
	}

	/**
	 * Prints shortest paths of disagreement to all people in NerdBook from specified person
	 *
	 * @param  person  person to print sequences from
	 **/
	public void shortestDisagreement(String person) {
		Collection<String> people = nerdBook.getVertices();
		Iterator<String> peopleIter = people.iterator();
		String person2;
		while (peopleIter.hasNext()) {
			person2 = peopleIter.next();
			if (!person.equals(person2)) {
				LinkedList<String> path = nerdBook.shortestWeightedPath(person, person2);
				Iterator iter = path.iterator();
				System.out.print(iter.next());
				while (iter.hasNext()) System.out.print(" -> " + iter.next());
				System.out.println();
			}
		}
	}

	/**
	 * Prints top five recommended friends based on smallest cumulative disagreement
	 * Does not include anyone that is already friend
	 *
	 * @param  person  person to offer recommendations for
	 **/
	public void recommend(String person) {
		Collection<String> people = nerdBook.getVertices();
		Iterator<String> peopleIter = people.iterator();
		String person2;
		PriorityQueue<RecommendPath> recommendList = new PriorityQueue<RecommendPath>();
		LinkedList<String> path;
		while (peopleIter.hasNext()) {
			person2 = peopleIter.next();
			path = nerdBook.shortestWeightedPath(person, person2);
			if (path.size() > 2) {
				Iterator<String> iter = path.iterator();
				double disagreement = 0;
				String first = iter.next();
				while (iter.hasNext()) {
					String second = iter.next();
					disagreement = disagreement + nerdBook.getEdgeWeight(first, second);
					first = second;
				}
				recommendList.add(new RecommendPath(path, disagreement));
			}
		}
		int count = 0;
		System.out.print("Recommended friends for " + person + ": ");
		while (!recommendList.isEmpty() && count < 5) {
			path = recommendList.poll().getPath();
			System.out.print(path.removeLast());
			if (count < 4) System.out.print(", ");
			else System.out.println();
			count++;
		}	
	}

	/**
	 * Gets the cohesion of the group as a whole
	 *
	 * @return cohesion of group
	 **/
	public double getCohesion() {
		Graph<String> mst = nerdBook.minimumSpanningTree();
		Iterator<String> iter = mst.getVertices().iterator();
		Iterator<String> iter2;
	
		String vertex, vertex2;
		double cohesion = 0;
		double weight;
		int count = 0;
		int count2;
		while (iter.hasNext()) {
			iter2 = mst.getVertices().iterator();
			vertex = iter.next();
			count2 = 0;
			while (iter2.hasNext()) {
				vertex2 = iter2.next();
				if (count < count2) { //makes sure edge is only checked once; once first vertex iterates it is not checked again
					weight = mst.getEdgeWeight(vertex, vertex2);
					//if an edge exists
					if (weight < Double.POSITIVE_INFINITY) cohesion += weight; //if edge exist, weight is added
				}
				++count2;
			}
			++count;
		}

		return cohesion;
	}
	
	/**
	 * RecommendPath describes a path between people keeping track of total disagreement
	 **/
	public class RecommendPath implements Comparable
	{
		LinkedList<String> path;
		double disagreement;

		/**
		 * Constructor for RecommendPath
		 **/
		public RecommendPath(LinkedList<String> p, double d) {
			path = p;
			disagreement = d;
		}

		/**
		 * Gets the path
		 *
		 * @return  the path
		 **/
		public LinkedList<String> getPath() {
			return path;
		}
		
		/**
		 * Gets the disagreement over the path
		 *
		 * @return  disagreement over path
		 **/
		public double getDisagreement() {
			return disagreement;
		}
	
		/**
		 * compareTo method implemented as described in Comparable interface
		 * Makes use of disagreement for sorting
		 * 
		 * @param  o1  object to compare, must be another RecommendPath
		 * @return negative, postive, or 0 if disagreement is less than, greater than, or equal to specified object
		 **/
		public int compareTo(Object o1) {
			RecommendPath p = (RecommendPath)o1;
			int retVal;
			
			if (disagreement < p.getDisagreement()) retVal = -1;
			else if (disagreement > p.getDisagreement()) retVal = 1;
			else retVal = 0;

			return retVal;
		}
	}

	public static void main(String[] args) {
		String problem = "Valid parameter not entered";
		try {
			problem = args[0];
		}
		catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("No parameter entered.");
			System.exit(-1);
		}
		NerdBook book = null;

		try {
			book = new NerdBook("nodes.txt", "edges.txt");
		}
		catch (IOException e) {
			System.out.println("File is corrupt");
		}

		Collection<String> people = book.getPeople();

		Iterator<String> iter = people.iterator();

		if (problem.equals("message")) {
			//book.shortestSequences("Samuel Baden");
			while (iter.hasNext()) book.shortestSequences(iter.next());
		}
		else if (problem.equals("interview")) {
			//book.shortestDisagreement("Samuel Baden");
			while (iter.hasNext()) book.shortestDisagreement(iter.next());
		}
		else if (problem.equals("recommender")) {
			//book.recommend("Samuel Baden");
			while (iter.hasNext()) book.recommend(iter.next());
		}
		else if (problem.equals("cohesion")) System.out.println("The total amount of disagreement is " + book.getCohesion());
		else System.out.println("Valid parameter not entered.");

	}
}

