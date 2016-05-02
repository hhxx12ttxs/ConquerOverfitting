/**
 * CSCI 311: WordGame.java
 * The implementation of the WordGame which finds the shortest
 * amount of moves (based on cost) between two 5-letter words
 *
 * Date created: 11/10/2012 16:23
 * @author Jonathan Como, Lucas Bohidar
 */
import java.util.Scanner;
import java.io.*;
import java.util.LinkedList;

public class WordGame {
  private Graph map;
  private Scanner file;

  // default constructor
  public WordGame() { file = null; }

  /**
   * Constructs a WordGame object. Takes an input files and
   * constructs a proper graph which is used for playing the
   * game. If the file cannot be read in correctly, the program
   * exits.
   *
   * @param _file the input filename
   */
  public WordGame(String _file) {
    try {
      // count number of vertices
      int count = 0;
      file = new Scanner(new File(_file));
      while (file.hasNext()) {
        count++;
        file.next();
      }
      // reopen to construct graph
      file = new Scanner(new File(_file));
      map = constructGraph(count);
    } catch (FileNotFoundException e) {
      System.out.println("Error: file not found. Please specify a valid file.");
      System.exit(0);
    }
  }

  /**
   * Helper method for the constructor. Constructs the graph
   * by creating vertices with each vertex corresponding to
   * a word in the input list.
   *
   * @param numVertices number of vertices in the graph.
   *                    found by an initial pass through list
   */
  private Graph constructGraph(int numVertices) {
    String word;
    Graph g = new Graph(numVertices);

    // read through the file, handle each token
    while (file.hasNext()) {
      word = file.next();
      g.addVertex(new Vertex(word));
    }

    return g;
  }

  /**
   * Prints the neighbors of the source vertex. Checks to make
   * sure that the vertex exists in the graph before printing
   *
   * @param inWord source vertex to print neighbors of
   */
  public void printNeighbors(String inWord) {

    /* BEGIN: check for vertex */
    if (inWord.length() != 5)
      System.out.println("Please enter a 5-letter word...try again.");

    inWord = inWord.toUpperCase();
    Vertex v = map.findVertex(inWord);
    /* END: check for vertex */

    if (v == null)
      System.out.println(inWord + " not found.");
    else {
      System.out.println("Neighbors of " + inWord + " are:");
      System.out.println(v.listNeighbors());
    }
  }

  /**
   * Finds the shortest path between two vertices as a result
   * of running Dijkstra's algorithm. Finds the shortest path
   * by following the predecessor trail of the source vertex
   * until it finds the destination. It recognizes when it
   * has arrived at the destination by checking the word
   * associated with the vertex.
   *
   * @param word1 the source vertex
   * @param word2 the destination
   *
   * @return prints the shortest path
   */
  public void findShortestPath(String word1, String word2) {
    // find both words
    Vertex v1 = map.findVertex(word1.toUpperCase());
    Vertex v2 = map.findVertex(word2.toUpperCase());

    // if either word not found, exit
    if (v1 == null || v2 == null) {
      System.err.println("One or both words not found. Exiting...");
      System.exit(0);
    }

    map.printShortestPath(v1, v2);
    System.out.printf("The best score for %s to %s is %d points.\n",
        v1.getWord(), v2.getWord(), (Integer) v1.record);
  }

  /**
   * Checks to see if the user is done with trails. Loops until
   * a valid response is acquired.
   *
   * Valid responses: 'y', 'yes', 'n', 'no' (not case-sensitive)
   *
   * @param input the scanner used for user input
   */
  private static boolean checkForDone(Scanner input) {
    while (true) {
      System.out.print("Continue (y/n)? ");
      String line = input.nextLine();
      if (line.equalsIgnoreCase("y") || line.equalsIgnoreCase("yes"))
        return false;
      else if (line.equalsIgnoreCase("n") || line.equalsIgnoreCase("no"))
        return true;
      else
        continue;
    }
  }

  public static void main(String[] args) {
    // no filename specified
    if (args.length != 1) {
      System.err.println("usage: WordGame [dictionary]");
      System.exit(0);
    }

    WordGame game = new WordGame(args[0]);

    Scanner input = new Scanner(System.in);
    boolean done = false;

    String line;
    while (!done) {
      System.out.print("Enter a first five letter word: ");
      line = input.nextLine();
      System.out.print("Enter a second five letter word: ");
      game.findShortestPath(line, input.nextLine());
      done = checkForDone(input);
    }
  }
}

