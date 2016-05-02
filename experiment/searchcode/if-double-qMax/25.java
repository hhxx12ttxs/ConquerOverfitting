package core;

import java.util.ArrayList;
import java.util.List;

public class ViterbiAlgorithm {

	public static List<String> evaluate( Observation oo, HMM hmm){

		// Initialize the DELTA matrix, a member of oo
		createDeltaMatrix(oo,hmm);
		createPsiMatrix(oo,hmm);
		// --------------------------
		// STEP 1: Problem Initialize
		// --------------------------	

		// For all states, at t=0 we have a fixed probability of seen the output 
		for ( int state = 0; state< hmm.stateCount ;state++ )
		{
			oo.DELTA[state][0]	= hmm.Pi(state)*hmm.B(state, oo.at(0) );
			oo.PSI[state][0]	= 0;
		}

		// ----------------------------------------------------
		// STEP 2: Recursive Relation : Induction
		// ----------------------------------------------------

		// For every time step, from second to the last
		for ( int time = 1; time< oo.length() ; time++ )
		{
			// We look at every tranisition TO state
			for ( int jj =0; jj< hmm.stateCount ; jj++)
			{
				// Calculate the probability of transitioning to this state from the previous step in time
				Double maxProb 	= 0.0;
				int maxIndex 	= 0;
				for( int ii=0; ii< hmm.stateCount ; ii++)
				{
					Double prob = oo.ALPHA[ii][time-1]* hmm.A(ii,jj);

					// If we have a maximum, capture the max value and its index
					if (prob > maxProb)
					{
						maxProb	= prob;
						maxIndex= ii;
					}
				}

				// Calculate B_j(O_t+1) as the output Probability
				Double Bj = hmm.B(jj, oo.at(time) );

				// Store index of that corresponds to the maximum probability.
				// Multiply my Bj and Maximum Probabilities and store in the current state and time
				oo.DELTA[jj][time] = maxProb * Bj ;
				oo.PSI[jj][time] = maxIndex;
			}
		}

		// ----------------------------------
		// STEP 3: Termination : Calculation
		// -----------------------------------	

		// Get index of the LAST time step
		int LAST = oo.length()-1;

		// Give our HMM, we can sum the probaility of every state
		Double pMax = 0.0;
		int qMax	= 0;

		for ( int ii=0; ii< hmm.stateCount ; ii++ )
		{
			// Get the delta associated with ii at LAST time step
			Double pp = oo.DELTA[ii][ LAST ];

			// If we have a new max, keep track of its value and index
			if ( pp > pMax )
			{
				pMax = pp;
				qMax = ii;
			}
		}
		
		// Store our PMAX QMAX
		oo.PMAX = pMax;
		oo.PATH[LAST] = qMax;
		
		// --------------------------------------
		// STEP 4: BACKTRACK: Path Determination
		// --------------------------------------

		// Back track your path from then to the start
		for ( int time=(LAST-1) ; time >= 0; time-- )
		{
			oo.PATH[time] = oo.PSI[ oo.PATH[time+1] ][time+1];
		}
		
		// Extract the State Path
		ArrayList<String> statepath = new ArrayList<String> ( oo.PATH.length );
		
		// For every state in the path
		for ( int kk=0; kk < oo.PATH.length ;kk++)
		{
			// Get the corresponding Syntax Term and add it to a list
			String stateString = hmm.structure.get( oo.PATH[kk] );
			statepath.add(stateString);
		}
		
		// Return the state path
		return statepath;
	}

	/**
	 * Creates a PSI Matrix as a property of an Observation
	 * Initializes everything to zeros
	 * @param oo	Observation
	 * @param hmm	HMM
	 */
	private static void createPsiMatrix( Observation oo, HMM hmm ){
		oo.PSI = new int[hmm.stateCount][ oo.length() ];

		// Initialize the ALPHA matrix to zeros;
		for (int state = 0; state < hmm.stateCount ; state++){
			for (int tt=0; tt< oo.length() ; tt++){
				oo.PSI[state][tt]= 0;
			}
		}
	}

	/**
	 * Creates a DELTA Matrix as a property of an Observation
	 * Initializes everything to zeros
	 * @param oo	Observation
	 * @param hmm	HMM
	 */
	private static void createDeltaMatrix( Observation oo, HMM hmm ){
		oo.DELTA = new Double[hmm.stateCount][ oo.length() ];

		// Initialize the ALPHA matrix to zeros;
		for (int state = 0; state < hmm.stateCount ; state++){
			for (int tt=0; tt< oo.length() ; tt++){
				oo.DELTA[state][tt]= 0.0;
			}
		}
	}
}

