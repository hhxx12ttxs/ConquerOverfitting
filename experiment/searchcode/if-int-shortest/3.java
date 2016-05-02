package com.cloudera.training.SSSP2;

import java.io.*;
import java.lang.*;
import java.util.ArrayList;

import org.apache.hadoop.io.*;

public class Node implements Writable {
  public ArrayList<ArrayList<Integer>> neighbours;
  public int shortest;
  public ArrayList<Integer> shortestPath;
  public int fastest;
  public ArrayList<Integer> fastestPath;

  public void set(ArrayList<ArrayList<Integer>> neighbours, int shortest, ArrayList<Integer> shortestPath, int fastest, ArrayList<Integer> fastestPath) {
    this.neighbours = neighbours;
    this.shortest = shortest;
    this.shortestPath = shortestPath;
    this.fastest = fastest;
    this.fastestPath = fastestPath;
  }

  public void set(ArrayList<ArrayList<Integer>> neighbours) {
    this.set(neighbours, 99999, new ArrayList<Integer>(), 99999, new ArrayList<Integer>());
  }

  public void write(DataOutput out) throws IOException {
    out.writeInt(neighbours.size());
System.out.println(neighbours.size());
    for (ArrayList<Integer> neighbour : neighbours) {
      out.writeInt(neighbour.get(0));
      out.writeInt(neighbour.get(1));
      out.writeInt(neighbour.get(2));
    }
    out.writeInt(shortest);
    out.writeInt(shortestPath.size());
    for (int n : shortestPath) {
      out.writeInt(n);
    }
    out.writeInt(fastest);
    out.writeInt(fastestPath.size());
    for (int n : fastestPath) {
      out.writeInt(n);
    }
  }

  public void readFields(DataInput in) throws IOException {
    ArrayList<Integer> ndt = new ArrayList<Integer>();
    int size = in.readInt();
    for (int i=0; i<size; i++) {
      ndt.set(0, in.readInt());
      ndt.set(1, in.readInt());
      ndt.set(2, in.readInt());
      neighbours.add(ndt);
    }
    shortest = in.readInt();
    size = in.readInt();
    for (int i=0; i<size; i++) {
      shortestPath.add(in.readInt());
    }
    fastest = in.readInt();
    size = in.readInt();
    for (int i=0; i<size; i++) {
      fastestPath.add(in.readInt());
    }
  }

  public String toString() {
    String res = "Neighbours: (";
    int first;
    first=1;
    for (ArrayList<Integer> neighbour : neighbours) {
      if (first!=1) {
        res += ", ";
      }
      first = 0;
      // res += "("+neighbours[i][0]+","+neighbours[i][1]+","+neighbours[i][2]+")";
    }
    res += "), ";
    res += "Shortest Path: "+shortest+" (";
    first=1;
    for (int n : shortestPath) {
      if (first!=1) {
        res += ", ";
      }
      first=1;
      res += n;
    }
    res += "), ";
    res += "Fastest Path: "+fastest+" (";
    first=1;
    for (int n : fastestPath) {
      if (first!=1) {
        res += ", ";
      }
      first=0;
      res += n;
    }
    res += ")";
    return(res);
  }
}

