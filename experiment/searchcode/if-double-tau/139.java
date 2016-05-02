package ims.acs.algorithm;

import java.util.*;

public class Ant extends Observable implements Runnable
{
    
    protected int[][] pathMatrix;
    protected int currentCity;
    protected int startingCity;
    protected double currentPathValue;
    protected Observer observer;
    protected Vector<Integer> pathVector;
    
    protected static Colony colony;
    
    public static double OverallbestPathValue = Double.MAX_VALUE;
    public static Vector<Integer> overallBestPathVect  = null;
    public static int[][] bestPath = null;
    public static int lastBestPathIterationNumber = 0;
    
    //constants
    private static final double beta = 2;
    private static final double Q0 = 0.8;
    private static final double rho = 0.1;
    
    private static final Random s_randGen = new Random(System.currentTimeMillis());
    private Hashtable<Integer,Integer> citiesNotVisited;
                        
    public static void setAntColony(Colony antColony)
    {
        colony = antColony;
    }
    
    public static void reset()
    {
        OverallbestPathValue = Double.MAX_VALUE;
        overallBestPathVect = null;
        bestPath = null;
        lastBestPathIterationNumber = 0;
    }
    
    public Ant(int startingNode, Observer observer)
    {
        this.startingCity = startingNode;
        this.observer  = observer;
    }

    public void start()
    {
        init();
        Thread thread = new Thread(this);
        thread.start();
    }

    public void run()
    {
        final GrafGradova graph = colony.getGraph();
        
        while(!citiesNotVisited.isEmpty())
        {
            int nextNode;
            
            synchronized(graph)
            {
                nextNode = nextCity(currentCity);
                currentPathValue += graph.delta(currentCity, nextNode);
            }
                        
            pathVector.addElement(new Integer(nextNode));
            pathMatrix[currentCity][nextNode] = 1;
                        
            synchronized(graph)
            {
                updatePheromones(currentCity, nextNode);
            }
            
            currentCity = nextNode;
        }
        
        synchronized(graph)
        {
            if(isBetterPath(currentPathValue, OverallbestPathValue))
            {
                OverallbestPathValue  = currentPathValue;
                bestPath = pathMatrix;
                overallBestPathVect = pathVector;
                lastBestPathIterationNumber = colony.getIterationCounter();
                
            }
        }
        
        observer.update(this, null);
    }
    
    public void init()
    {

        final GrafGradova graph = colony.getGraph();
        currentCity   = startingCity;
        
        pathMatrix = new int[graph.nodes()][graph.nodes()];
        pathVector = new Vector<Integer>(graph.nodes());
        
        pathVector.addElement(new Integer(startingCity));
        currentPathValue = 0;
        
        citiesNotVisited = new Hashtable<Integer, Integer>(graph.nodes());
        for(int i = 0; i < graph.nodes(); i++)
            citiesNotVisited.put(new Integer(i), new Integer(i));
        
        citiesNotVisited.remove(new Integer(startingCity));
    }

    public int nextCity(int currentNode)
    {
        final GrafGradova graph = colony.getGraph();
        
        double q = s_randGen.nextDouble();
        int nextBestNode = -1;
        
        if(q <= Q0) 
        {
            double dMaxVal = -1;
            double dVal;
            int nNode;
            
            Enumeration<Integer> en = citiesNotVisited.elements();
            while(en.hasMoreElements())
            {
                nNode = ((Integer)en.nextElement()).intValue();
                
                dVal = graph.tau(currentNode, nNode) * Math.pow(graph.etha(currentNode, nNode), beta);
                
                if(dVal > dMaxVal)
                {
                    dMaxVal  = dVal;
                    nextBestNode = nNode;
                }
            }
        }
        else 
        {
            double sum = 0;
            int nextNodeIndex = -1;
            
            Enumeration<Integer> en = citiesNotVisited.elements();
            while(en.hasMoreElements())
            {
                nextNodeIndex = ((Integer)en.nextElement()).intValue();
                
                sum += graph.tau(currentNode, nextNodeIndex) * Math.pow(graph.etha(currentNode, nextNodeIndex), beta);
            }
            
            double dAverage = sum / (double)citiesNotVisited.size();
            
            en = citiesNotVisited.elements();
            while(en.hasMoreElements() && nextBestNode < 0)
            {
                nextNodeIndex = ((Integer)en.nextElement()).intValue();
                
                double p = (graph.tau(currentNode, nextNodeIndex) * Math.pow(graph.etha(currentNode, nextNodeIndex), beta)) / sum;
                
                if((p*sum) > dAverage)
                {
                    nextBestNode = nextNodeIndex;
                }
            }
            
            if(nextBestNode == -1)
                nextBestNode = nextNodeIndex;
       }
        
        citiesNotVisited.remove(new Integer(nextBestNode));
        return nextBestNode;
    }
    
    public void updatePheromones(int thisNode, int nextNode)
    {
        final GrafGradova graph = colony.getGraph();
        
        double val = ((double)1 - rho) * graph.tau(thisNode, nextNode) + (rho * (graph.tau0()));
        graph.updateTau(thisNode, nextNode, val);
    }
    
    public boolean isBetterPath(double dPathValue1, double dPathValue2)
    {
        return dPathValue1 < dPathValue2;
    }
    
    public static int[] getBestPath()
    {
        int nBestPathArray[] = new int[overallBestPathVect.size()];
        
        for(int i = 0; i < overallBestPathVect.size(); i++)
        {
            nBestPathArray[i] = ((Integer)overallBestPathVect.elementAt(i)).intValue();
        }

        return nBestPathArray;
    }
}
