package ims.acs.algorithm;

import java.io.*;

public class GrafGradova implements Serializable
{
   
	private static final long serialVersionUID = 5622556804457348814L;
	
	private double[][] deltaFactor;
    private double[][] tauFactor;
    private int cityNumber;
    private double deltaTau0;
    
    public GrafGradova(int nNodes, double[][] delta, double[][] tau)
    {
        if(delta.length != nNodes)
            throw new IllegalArgumentException();
        
        cityNumber = nNodes;
        this.deltaFactor = delta;
        tauFactor   = tau;
    }
    
    public GrafGradova(int nodes, double[][] delta)
    {
        this(nodes, delta, new double[nodes][nodes]);
        
        setTau();
    }
    
    public synchronized double delta(int r, int s)
    {
        return deltaFactor[r][s];
    }
    
    public synchronized double tau(int r, int s)
    {
        return tauFactor[r][s];
    }
    
    public synchronized double etha(int r, int s)
    {
        return ((double)1) / delta(r, s);
    }
    
    public synchronized int nodes()
    {
        return cityNumber;
    }
    
    public synchronized double tau0()
    {
        return deltaTau0;
    }
    
    public synchronized void updateTau(int r, int s, double value)
    {
        tauFactor[r][s] = value;
    }
    
    public void setTau()
    {
        double deltaAverage = averageDelta();
        
        deltaTau0 = (double)1 / ((double)cityNumber * (0.5 * deltaAverage));
        
        for(int r = 0; r < nodes(); r++)
        {
            for(int s = 0; s < nodes(); s++)
            {
                tauFactor[r][s] = deltaTau0;
            }
        }
    }
    
    public double averageDelta()
    {
        return average(deltaFactor);
    }
    
    public double averageTau()
    {
        return average(tauFactor);
    }
    
    private double average(double matrix[][])
    {
        double distanceSum = 0;
        for(int r = 0; r < cityNumber; r++)
        {
            for(int s = 0; s < cityNumber; s++)
            {
                distanceSum += matrix[r][s];
            }
        }
        
        double distanceAverage = distanceSum / (double)(cityNumber * cityNumber);
        
        return distanceAverage;
    }
}


