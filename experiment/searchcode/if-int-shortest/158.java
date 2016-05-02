/**
 * CSCI 311: Graph.java
 * The implementation of the graph which is defined as a set
 * of vertices and edges.
 *
 * Date created: 11/10/2012 19:54
 * @author Jonathan Como, Lucas Bohidar
 */

import java.util.Scanner;
import java.io.*;
import java.util.LinkedList;

public class Graph {
  private static Vertex[] vertices;
  private static int count;

  //default constructor
  public Graph() { vertices = null; count = 0; }

  /**
   * Constructs a graph with a given number of vertices.
   * 
   * @param numVertices number of vertices in graph
   */
  public Graph(int numVertices) {
      vertices = new Vertex[numVertices];
  }

  // getter method for Graph
  public Vertex[] getVertices() { return vertices; }

  /**
   * Searches for a vertex in the graph by utilizing a
   * simple linear search.
   *
   * @param check the vertex to check for
   *
   * @return Vertex if found, null otherwise
   */
  public Vertex findVertex(String check) {
    for (Vertex v : vertices) {
      if (check.equals(v.getWord()))
        return v;
    }
    return null;
  }

  /**
   * Adds a vertex to the graph. First adds it to the array
   * of vertices, and then establishes the neighbors of said
   * vertex by checking down the other vertices in the array
   *
   * @param v vertex to add
   */
  public void addVertex(Vertex v) {
    vertices[count] = v;
    for(int i = count; i >= 0 ; i--) {
      v.checkIsNeighbor(vertices[i]);
    }
    count++;
  }

  /**
   * Dijkstra's algorithm. Uses a source vertex and calculates
   * the shortest distance to any other vertex in the graph. End
   * result is a minimum spanning tree at the source vertex
   *
   * @param source the source vertex -- this is the destintation
   *               vertex (2nd input word) when playing the word game
   * @param search the word of the destination vertex
   */
  private void dijkstra(Vertex source, String destWord) {
    initializePFS(source);
    boolean done = false;

    // PQ = G.V -- insert all vertices into heap
    Heap pq = new Heap();
    for (Vertex v : vertices)
      pq.insert(v);

    // variables for easier readilibity
    Vertex v, u;
    int vDist, uDist;

    while (pq.getHeapsize() > 0 && !done) {
      u = (Vertex) pq.removeMin();
      uDist = (Integer) u.record;

      for (Edge e : u.neighbors) {
        v = e.dest;
        vDist = (Integer) v.record;

        if (uDist + e.weight < vDist) {
          v.pred = u;
          v.record = uDist + e.weight;
          pq.heapifyUp(v.handle); // update PQ
        }
      }

      if (u.getWord().equals(destWord))
        done = true;
    }
  }

  /**
   * Helper method for Dijkstra's algorithm. Initializes/Re-initializes
   * the graph's metadata each time it searches for the shortest path
   * between two words
   *
   * @param source the source vertex
   */
  private void initializePFS(Vertex source) {
    for (Vertex u : vertices) {
      u.record = Integer.MAX_VALUE;
      u.pred = null;
    }
    source.record = 0;
  }

  /**
   * Returns the shortest path between the source and destination
   * vertices. Calls dijkstra's algorithm and then reads backwards
   * from the source each predecessor until it reaches the destination
   * vertex.
   *
   * @param source the source vertex
   * @param dest the destination vertex
   */
  public void printShortestPath(Vertex source, Vertex dest) {
    dijkstra(dest, source.getWord());

    // temp vertex used for traversing
    Vertex temp = source;
    String destWord = dest.getWord();

    System.out.printf("     ");
    while (!temp.getWord().equals(destWord)) {
      System.out.printf("%s   ", temp.getWord());
      temp = temp.pred;
    }
    System.out.printf("%s\n", temp.getWord());
  }
}

