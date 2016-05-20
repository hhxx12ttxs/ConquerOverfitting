package saa.collsionavoidance.mdp360;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class ValueIteration
{
	private ACASXMDP mdp;

	private final int numStates=(2*ACASXMDP.nh+1)*(2*ACASXMDP.noV+1)*(2*ACASXMDP.niV+1)*(ACASXMDP.nt+1)*(ACASXMDP.nra);
	
	private double[] U= new double[numStates];

	/**
	 * Constructor.
	 * 
	 * @param gamma
	 *            discount &gamma; to be used.
	 */
	public ValueIteration(ACASXMDP mdp, double gamma,double epsilon) 
	{
		this.mdp=mdp;
		if (gamma > 1.0 || gamma <= 0.0) 
		{
			throw new IllegalArgumentException("Gamma must be > 0 and <= 1.0");
		}
		
		if (epsilon < 0.0) 
		{
			throw new IllegalArgumentException("epsilon must be >= 0");
		}

		double[] Udelta= new double[numStates];
		double delta;

		ACASXState[]states=mdp.states();
		// repeat
		int iteration=0;
		do 
		{
//			U= Arrays.copyOf(Udelta, numStates);
			U=Udelta.clone();
			delta = 0;			
			for (int i=0; i<numStates;i++)
			{
				ACASXState s=states[i];
				if(s.getOrder()!=i)
				{
					System.err.println("error happens in valueIteration() + s.getOrder()!=i");
				}
				ArrayList<Integer> actions = mdp.actions(s);
				double aMax = Double.NEGATIVE_INFINITY;

				for (Integer a : actions) 
				{
//					System.out.println(s+"   "+a+"-------------------------");
					double aSum = 0;
					Map<ACASXState,Double> TransitionStatesAndProbs= mdp.getTransitionStatesAndProbs(s, a);
					Set<Entry<ACASXState,Double>> entrySet = TransitionStatesAndProbs.entrySet();
							
					for (Entry<ACASXState,Double> entry : entrySet) 
					{						
						ACASXState nextState = entry.getKey();
						int nextStateOrder = nextState.getOrder();
						aSum += entry.getValue() * gamma *  U[nextStateOrder];
//						System.out.println(nextState+"   "+entry.getValue());
					}
					aSum += mdp.reward(s, a);
	
					if (aSum > aMax) 
					{
						aMax = aSum;
					}
				}

				Udelta[i]=aMax;
				
				double aDiff = Math.abs(aMax - U[i]);
				if (aDiff > delta) 
				{
					delta = aDiff;
				}
				
			}
			System.out.println(++iteration);
		} while (delta > epsilon);		
//		U= Arrays.copyOf(Udelta, numStates);
		U=Udelta.clone();
	}
		

	public double getQValue(ACASXState state, int action) 
	{	
		double QValue = 0;
		Map<ACASXState,Double> TransitionStatesAndProbs= mdp.getTransitionStatesAndProbs(state, action);
		Set<Entry<ACASXState,Double>> entrySet = TransitionStatesAndProbs.entrySet();

		for (Entry<ACASXState,Double> entry : entrySet) 
		{
			if(entry.getValue()>1)
			{
				System.err.println(entry.getValue()+"greater than 1");
			}
			ACASXState nextState = entry.getKey();
			int nextStateOrder=nextState.getOrder();
			QValue += entry.getValue()*  U[nextStateOrder];
		
		}
		return QValue;		
	}
	
	public void storeQValues() 
	{	
		FileWriter indexFileWriter = null;
		FileWriter costFileWriter = null;
		FileWriter actionFileWriter = null;
	
		try 
        {
            indexFileWriter = new FileWriter("indexFile",false);
            costFileWriter = new FileWriter("costFile",false);
            actionFileWriter = new FileWriter("actionFile",false);
      
            int index=0;
            ACASXState[]states=mdp.states();
			for (int i=0; i<numStates;i++)
			{
				ACASXState s=states[i];
				ArrayList<Integer> actions =mdp.actions(s);
    			indexFileWriter.write(index+"\n");//+s.toString()
    			index+=actions.size();
    			for (int a :actions ) 
    			{
    				actionFileWriter.write(a+"\n");    				
    				double QValue = getQValue(s, a);
    				costFileWriter.write(QValue+"\n");
    			}			

			}
			indexFileWriter.write(index+"\n");//+s.toString()
    		
        }
        catch(Exception e) 
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally 
        {
            try 
            {
	                if(indexFileWriter!=null)
	                {
	                	indexFileWriter.close();
	                }
	                if(costFileWriter!=null)
	                {
	                	costFileWriter.close();
	                }
	                if(actionFileWriter!=null)
	                {
	                	actionFileWriter.close();
	                }
            } 
            catch (IOException e) 
            {
                e.printStackTrace();
            }
        }
				
	}	
	
}


