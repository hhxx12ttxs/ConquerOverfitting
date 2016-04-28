package schemamatchings.topk.algorithms;

import java.util.Iterator;
import java.util.Stack;

import schemamatchings.topk.graphs.BipartiteGraph;
import schemamatchings.topk.graphs.Edge;
import schemamatchings.topk.graphs.EdgesSet;
import schemamatchings.topk.graphs.Graph;
import schemamatchings.topk.graphs.Vertex;
import schemamatchings.topk.graphs.util.EdgeArray;
import schemamatchings.topk.graphs.util.GraphUtilities;
import schemamatchings.topk.graphs.util.VertexArray;
import schemamatchings.topk.graphs.util.VertexPQ;

/**
 * <p>Title: MaxWeightBipartiteMatchingAlgorithm</p>
 * <p>Description: Represents the Max Weighted Bipartite Graph Algorithm ("A-Best")</p>
 * <p>Implemenetation Reference: This algorithm was based on LEDA C++ implementation<br>
 *    taken from LEDA book: "A platform for combinatorial and geometric computing"<br>
 *    authors: K.Mehlhorn,S.Naher , CAMBRIDGE UNIVERSITY PRESS<br></p>
 * <p>The use of this implementation is only for ACADEMIC RESEARCH,and by that<br>
 *    all other use of this implementation is on the user responsibility<br></p>
 * @author Haggai Roitman
 * @version 1.1
 */

 public final class MaxWeightBipartiteMatchingAlgorithm implements SchemaMatchingsAlgorithm{

  public static final int NAIVE_HEURISTIC = 0;
  public static final int SIMPLE_HEURISTIC = 1;
  public static final int REFINED_HEURISTIC = 2;

  private BipartiteGraph g;
  private EdgeArray c;
  private VertexArray pot;
  private int heuristic = REFINED_HEURISTIC;
  private double oneOverS;
  private int exp;

  public MaxWeightBipartiteMatchingAlgorithm(BipartiteGraph g,EdgeArray ct,VertexArray pot){
    this.g = g;
    this.pot = pot;
    this.c = new EdgeArray(g);
    boolean scalling = scaleWeights(g,ct,c,3.0);
  }

  public void setHeuristic(int heuristic){
    this.heuristic = heuristic;
  }



  public EdgesSet runAlgorithm(){
    EdgesSet result = new EdgesSet(g.getEdgesSet().getVc());
    //for all nodes(v,G) pot[v] = 0;
    //this will be done by giving pot:= VertexArray(g,new Double(0))
    if (g.getEdgesSet().isEmpty()) return new EdgesSet(g.getEdgesSet().getVc());
    //check that all edges are directed from A to B
    //this should be a precondition also
    VertexArray free = new VertexArray(g,new Boolean(true));
    VertexArray pred = new VertexArray(g,null);
    VertexArray dist = new VertexArray(g,new Double(0));
    VertexPQ PQ = new VertexPQ(g);

    switch(heuristic){
      case(NAIVE_HEURISTIC):
        Double C = new Double(0);
        Iterator edgesIterator = g.getEdgesIterator();
        while (edgesIterator.hasNext()){
          Edge e = (Edge)edgesIterator.next();
          Double edgeC = (Double)c.getEdgeProperty(e);
          if (edgeC.compareTo(C) == 1)//bigger than...
            C = edgeC;
        }
        Iterator leftVertexesIterator = g.getLeftVertexSetIterator();
        while (leftVertexesIterator.hasNext()){
         pot.setVertexProperty((Vertex)leftVertexesIterator.next(),C);
        }
        break;
      case(SIMPLE_HEURISTIC):
        Iterator leftVertexesIterator1 = g.getLeftVertexSetIterator();
        while (leftVertexesIterator1.hasNext()){
          Vertex a = (Vertex)leftVertexesIterator1.next();
          Edge eMax = null;
          double C_max = 0;
          Iterator vertexAdjEdgesIterator = GraphUtilities.getVertexAdjEdges(g,a).getMembers().iterator();
          while (vertexAdjEdgesIterator.hasNext()){
            Edge e = (Edge)vertexAdjEdgesIterator.next();
            if (((Double)c.getEdgeProperty(e)).doubleValue() > C_max){
              eMax = e;
              C_max = ((Double)c.getEdgeProperty(e)).doubleValue();
            }
          }
          pot.setVertexProperty(a,new Double(C_max));
          Vertex b;
          if (eMax != null && ((Boolean)free.getVertexProperty((b = GraphUtilities.getEdgeTargetVertex(g,eMax)))).booleanValue()){
            eMax.turnOverEdgeDirection();
            free.setVertexProperty(a,new Boolean(false));
            free.setVertexProperty(b,new Boolean(false));
          }
        }
        break;
      default:
        //REFINED_HEURISTIC
        mwbmHeuristic(g,c,pot,free);
        break;
    }
    Iterator leftVertexesIterator = g.getLeftVertexSetIterator();
    while (leftVertexesIterator.hasNext()){
      Vertex a = (Vertex)leftVertexesIterator.next();
       if (((Boolean)free.getVertexProperty(a)).booleanValue())
         agument(g,a,c,pot,free,pred,dist,PQ);
    }
    Iterator rightVertexesIterator = g.getRightVertexSetIterator();
    while (rightVertexesIterator.hasNext()){
      Vertex b = (Vertex)rightVertexesIterator.next();
      Iterator outEdgesIterator = GraphUtilities.getVertexOutEdges(g,b).getMembers().iterator();
      while (outEdgesIterator.hasNext()){
        result.addMember(outEdgesIterator.next());
      }
    }
    result.turnOverEdges(false);
    return result;
  }


  private void agument(BipartiteGraph g,Vertex a,final EdgeArray c,VertexArray pot,VertexArray free,VertexArray pred,VertexArray dist,VertexPQ PQ){
    //agument:initialization
    dist.setVertexProperty(a,new Double(0));
    Vertex bestVertexInA = a;
    double minA = ((Double)pot.getVertexProperty(a)).doubleValue();
    double Delta;
    Stack RA = new Stack(),RB = new Stack();
    RA.push(a);
    Vertex a1 = a;
    Edge e;
    //relax all edges out of a1
    Iterator a1AdjEdgesIterator = GraphUtilities.getVertexAdjEdges(g,a1).getMembers().iterator();
    while (a1AdjEdgesIterator.hasNext()){
      e = (Edge)a1AdjEdgesIterator.next();
      Vertex b = GraphUtilities.getEdgeTargetVertex(g,e);
      double db = ((Double)dist.getVertexProperty(a1)).doubleValue() + (((Double)pot.getVertexProperty(a1)).doubleValue() + ((Double)pot.getVertexProperty(b)).doubleValue() - ((Double)c.getEdgeProperty(e)).doubleValue());
      if (pred.getVertexProperty(b) == null){
        dist.setVertexProperty(b,new Double(db));
        pred.setVertexProperty(b,e);
        RB.push(b);
        PQ.insert(b,new Double(db));//PQ.insert(b,db)
      }
      else if (db < ((Double)dist.getVertexProperty(b)).doubleValue()){
        dist.setVertexProperty(b,new Double(db));
        pred.setVertexProperty(b,e);
        PQ.decreaseP(b,new Double(db));
      }
    }
    while (true){
      //select from PQ the node b with minimal distance db
      Vertex b;
      double db = 0;
      if (PQ.isEmpty())
         b = null;
      else{
         b = (Vertex)PQ.deleteMin();
         db = ((Double)dist.getVertexProperty(b)).doubleValue();
      }
      //distinguish three cases
      if (b == null || db >= minA){
        Delta = minA;
        //agumentation by path to best node in A
        agumentPathTo(g,bestVertexInA,pred);
        free.setVertexProperty(a,new Boolean(false));
        free.setVertexProperty(bestVertexInA,new Boolean(true));
        break;
      }
      else if (((Boolean)free.getVertexProperty(b)).booleanValue()){
        Delta = db;
        //agumentation by path to b
        agumentPathTo(g,b,pred);
        free.setVertexProperty(a,new Boolean(false));
        free.setVertexProperty(b,new Boolean(false));
        break;
      }
      else{
        //continue shortest-path computation
        e = GraphUtilities.getVertexFirstAdjEdge(g,b);
        Vertex a11 = GraphUtilities.getEdgeTargetVertex(g,e);
        pred.setVertexProperty(a11,e);
        RA.push(a11);
        dist.setVertexProperty(a11,new Double(db));
        if (db + ((Double)pot.getVertexProperty(a11)).doubleValue() < minA){
          bestVertexInA = a11;
          minA = db + ((Double)pot.getVertexProperty(a11)).doubleValue();
        }
        //relax all edges out of a11
        Iterator a11AdjEdgesIterator = GraphUtilities.getVertexAdjEdges(g,a11).getMembers().iterator();
        while (a11AdjEdgesIterator.hasNext()){
          e = (Edge)a11AdjEdgesIterator.next();
          Vertex b1 = GraphUtilities.getEdgeTargetVertex(g,e);
          double db1 = ((Double)dist.getVertexProperty(a11)).doubleValue() + (((Double)pot.getVertexProperty(a11)).doubleValue() +((Double)pot.getVertexProperty(b1)).doubleValue() - ((Double)c.getEdgeProperty(e)).doubleValue());
          if (pred.getVertexProperty(b1) == null){
            dist.setVertexProperty(b1,new Double(db1));
            pred.setVertexProperty(b1,e);
            RB.push(b1);
            PQ.insert(b1,new Double(db1));//PQ.insert(b1,db1)
          }
          else if (db1 < ((Double)dist.getVertexProperty(b1)).doubleValue()){
            dist.setVertexProperty(b1,new Double(db1));
            pred.setVertexProperty(b1,e);
            PQ.decreaseP(b1,new Double(db1));
          }
        }
      }
    }
    //agument:potential update and reinitialization
    while (!RA.isEmpty()){
      Vertex a12 = (Vertex)RA.pop();
      pred.setVertexProperty(a12,null);
      double potChange = Delta - ((Double)dist.getVertexProperty(a12)).doubleValue();
      if (potChange <= 0) continue;
      pot.setVertexProperty(a12,new Double(((Double)pot.getVertexProperty(a12)).doubleValue() - potChange));
    }
    while (!RB.isEmpty()){
      Vertex b12 = (Vertex)RB.pop();
      pred.setVertexProperty(b12,null);
      if (PQ.member(b12))
        PQ.delete(b12);
      double potChange = Delta - ((Double)dist.getVertexProperty(b12)).doubleValue();
      if (potChange <= 0) continue;
      pot.setVertexProperty(b12,new Double(((Double)pot.getVertexProperty(b12)).doubleValue() + potChange));
    }
  }

  private void agumentPathTo(Graph g,Vertex v,final VertexArray pred){
    Edge e = (Edge)pred.getVertexProperty(v);
    while (e != null){
      e.turnOverEdgeDirection();
      e = (Edge)pred.getVertexProperty(GraphUtilities.getEdgeTargetVertex(g,e));
    }
  }


  private void mwbmHeuristic(BipartiteGraph g,EdgeArray c,VertexArray pot,VertexArray free){
    Vertex a,b;
    Edge e,e2,eb;
    VertexArray secEdge = new VertexArray(g,null);
    Iterator leftVertexSetIterator = g.getLeftVertexSetIterator();
    while (leftVertexSetIterator.hasNext()){
      a = (Vertex)leftVertexSetIterator.next();
      double max2 = 0,max =0; eb = null; e2 = null;
      //compute edges with largest and second largest slack
      Iterator aAdjEdgesIterator = GraphUtilities.getVertexAdjEdges(g,a).getMembers().iterator();
      while (aAdjEdgesIterator.hasNext()){
        e = (Edge) aAdjEdgesIterator.next();
        double we = ((Double)c.getEdgeProperty(e)).doubleValue() - ((Double)pot.getVertexProperty(GraphUtilities.getEdgeTargetVertex(g,e))).doubleValue();
        if (we >= max2){
          if (we >= max){
            max2 = max; e2 = eb;
            max = we; eb = e;
          }
          else{
            max2 = we; e2 = e;
          }
        }
      }
      if (eb != null){
        b = GraphUtilities.getEdgeTargetVertex(g,eb);
        if (((Boolean)free.getVertexProperty(b)).booleanValue()){
          //match eb and change pot[] to make slack of e2 zero
          secEdge.setVertexProperty(a,e2);
          pot.setVertexProperty(a,new Double(max2));
          pot.setVertexProperty(b,new Double(max - max2));
          eb.turnOverEdgeDirection();
          free.setVertexProperty(a,new Boolean(false));
          free.setVertexProperty(b,new Boolean(false));
        }
        else{
          //try to augment matching along
          //path of length 3 given by sec_edge[]
          pot.setVertexProperty(a,new Double(max));
          e2 = GraphUtilities.getVertexFirstAdjEdge(g,b);
          e = (Edge)secEdge.getVertexProperty(GraphUtilities.getEdgeTargetVertex(g,e2));
          if (e != null && GraphUtilities.getVertexOutDeg(g,GraphUtilities.getEdgeTargetVertex(g,e)) == 0){
            free.setVertexProperty(a,new Boolean(false));
            free.setVertexProperty(GraphUtilities.getEdgeTargetVertex(g,e),new Boolean(false));
            e.turnOverEdgeDirection();
            e2.turnOverEdgeDirection();
            eb.turnOverEdgeDirection();
          }
        }
      }
      else{
        pot.setVertexProperty(a,new Double(0));
      }
    }
  }


  private boolean scaleWeights(final BipartiteGraph g,final EdgeArray c2,EdgeArray c1,double f){
    Edge e;
    double C = 0;
    Iterator edgesIterator = g.getEdgesIterator();
    while (edgesIterator.hasNext()){
      e = (Edge)edgesIterator.next();
      C = Math.max(C,fabs((((Double)c2.getEdgeProperty(e)).doubleValue())));
    }
    double S = computeS(f,C);
    boolean noScalling = true;
    edgesIterator = g.getEdgesIterator();
    while (edgesIterator.hasNext()){
      e = (Edge)edgesIterator.next();
      c1.setEdgeProperty(e,new Double(scaleWeight(((Double)c2.getEdgeProperty(e)).doubleValue(),S)));
      if (((Double)c2.getEdgeProperty(e)).doubleValue() != ((Double)c1.getEdgeProperty(e)).doubleValue())
        noScalling = false;
    }
    return noScalling;
  }

  private double computeS(double f,double C){
    double x = frexp(f*C);
    oneOverS = ldexp(1,exp - 53);
    return ldexp(1,53 - exp);
  }

  private double scaleWeight(double w,double S){
    if (w == 0) return 0;
    int signW = +1;
    if (w < 0){
      signW = -1;
      w = -w;
    }
    return (signW * Math.floor(w * S) * oneOverS);
  }



  private double frexp(double x){
    boolean neg;
    int i;
    if (neg = (x < 0.0))
      x = -x;
    else if (x == 0.0){
      exp = 0;
      return 0.0;
    }
    if (x < 0.5)
       for (i = 0;x < 0.5;i--)
         x *= 2.0;
    else
      for (i = 0;x >= 1.0;i++)
         x *= 0.5;
    exp = i;
    return (neg ? -x : x);
  }


  private double ldexp(double x,int exp){
    return (x * Math.pow(2,exp));
  }


  private double fabs(double arg){
    if (arg < 0.0)
      arg = -arg;
    return (arg);
  }


  /**
   * release resources
   */
  public void nullify(){
    try{
       g = null;
       c.nullify();
       c = null;
       pot.nullify();
       pot = null;
    }catch(NullPointerException e){}
  }

  public String getAlgorithmName(){
    return MWBM_ALGORITHM;
  }

}
