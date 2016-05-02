package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import utils.TextTools;

/**
 * 
 * @author Samir Ahmed
 *
 * HMM is a wrapper class that contains all information about an Hidden Markov Model
 * A : the State Transition Matrix
 * B : the Output Likelihood Matrix
 * PI: the Initial State Likelihood Vector
 * 
 */
public class HMM {

	// Mutable Data
	private  ArrayList<ArrayList<Double>> Bmat;
	private  ArrayList<ArrayList<Double>> Amat;
	private  ArrayList<Double> PI;
	public	 ArrayList<String> vocabulary;
	public	 ArrayList<String> structure;

	/// Immutable data members
	public final int stateCount;
	public final int observationCount;
	public final int length;

	/**
	 * Construct an empty HMM with three factors,
	 * @param numStates			The number of states
	 * @param numObservations	The number of Observations
	 * @param length			The length of Observations
	 */
	public HMM( int numStates, int numObservations, int length ){

		// Load the values passed that represent the HMM
		this.observationCount = numObservations;
		this.stateCount = numStates;
		this.length = length;

		// Create our A and B matrices based on the constructor values
		this.Amat = new ArrayList<ArrayList<Double>>( this.stateCount);

		// Initialize 2D Matrix
		for (int ii =0; ii< this.stateCount ; ii++ ){
			this.Amat.add( ii, new ArrayList<Double>() );
			for ( int jj = 0; jj < this.stateCount ; jj++){ this.Amat.get(ii).add(jj, 0.0); }
		}

		// Initialize 2D Matrix
		this.Bmat = new ArrayList<ArrayList<Double>>( this.stateCount);		
		for (int ii =0; ii< this.stateCount ; ii++ ){			
			this.Bmat.add( ii, new ArrayList<Double>() );
			for ( int jj = 0; jj < this.observationCount ; jj++){ this.Bmat.get(ii).add( jj,0.0); }
		}

		// Setup the remaining vectors/ lists
		this.PI	  = new ArrayList<Double> (this.stateCount);
		this.structure = new ArrayList<String> ();
		this.vocabulary =  new ArrayList<String>();
	}

	/**
	 * Set the PI Vector
	 * @param initialValues A Double array of initial values
	 */
	public void addPi(Double [] initialValues){
		Collections.addAll( this.PI, initialValues);
	}
	
	/**
	 * Set a value in Pi vector
	 * @param state			The state in question 
	 * @param probability	The new probability for the given state
	 */
	public void Piset(int state ,Double probability){
		this.PI.set(state, probability);
	}

	/**
	 * Add the sentence structure to the HMM
	 * @param words  An ordered string array that represents the syntax structure
	 */
	public void addStructure(String [] words)
	{
		Collections.addAll( this.structure,words);
	}

	/**
	 * Add words to the vocabulary
	 * @param words  Words to be added the HMM's vocab
	 */
	public void addVocabulary(String [] words)
	{
		Collections.addAll( this.vocabulary, words);
	}

	/**
	 * Set the values on in the Transition Matrix A
	 * @param startState	The starting State
	 * @param endState		The Ending State
	 * @param probability	The Probability of the transition from Start to End State
	 */
	public void Aset(int startState, int endState, Double probability)
	{
		this.Amat.get(startState).set(endState, probability);
	}

	/**
	 * Get the Values in the Transition Matrix A
	 * @param startState	The starting state	
	 * @param endState		The Ending State
	 * @return				The Probability of the transitions from Start to End State
	 */
	public Double A(int startState, int endState)
	{
		return Amat.get(startState).get(endState);
	}

	/**
	 * Set the values in the Output Probability Matrix
	 * 
	 * @param state			The state in question
	 * @param outputNo		The desired output No
	 * @param probability	The probablity of the output at the given state
	 */
	public void Bset(int state, int outputNo, Double probability)
	{
		this.Bmat.get(state).set( outputNo , probability);
	}

	/**
	 * Get the Probabity associated with a given State and Output
	 * @param state		The State in question
	 * @param outputNo	The output number in question
	 * @return			A probability 0-1
	 */
	public Double B(int state, int outputNo)
	{
		return Bmat.get(state).get(outputNo);
	}

	/**
	 * Get the probability of starting in a given state
	 * @param state State number
	 * @return		The probability of starting in that state
	 */
	public Double Pi(int state)
	{
		return this.PI.get(state);
	}

	/**
	 * Get the probability of seeing a specific output, at a certain state 
	 * @param state				The state in question
	 * @param ObservedOutput	The Output observation as a string
	 * @return					Probability of observing this output.
	 */
	public Double B(int state, String ObservedOutput )
	{
		// Since the vocab is small, i can't be bothered to use an other datastructure
		// Just find the corresponding observation and use that to index the arraylist matrix.
		int outputNo=-1;
		for ( int output =0; output< this.observationCount; output++ )
		{
			if ( this.vocabulary.get(output).equals(ObservedOutput))
			{
				outputNo= output;
				break;
			}
		}
		
		return this.Bmat.get(state).get(outputNo);
		
	}
	
	/**
	 * Returns a string that is a snapshot of the HMM, A, B, pi etc.. 
	 * @return 	Lolz
	 */
	public String toString()
	{

		// Create A string builder
		StringBuilder sb = new StringBuilder();

		// Append State Count, Observation Symbol Count, Length of Ob Sequences
		sb.append(this.stateCount+" "+this.observationCount+" "+this.length+"\n");
		
		// Append the structure syntax terms
		for (String ss: this.structure) { sb.append(ss+" "); }
		sb.append("\n");
		
		// Append the vocab
		for (String ss: this.vocabulary) { sb.append(ss+" "); }
		sb.append("\n");
		
		// Append formated transition matrix
		sb.append("a:\n");
		for ( int ii = 0; ii< this.Amat.size(); ii++)
		{
			List<Double> aa = Amat.get(ii);
			for (int jj=0; jj< aa.size() ; jj++) { sb.append(String.format("%.5f ", aa.get(jj)));}
			sb.append("\n");
		}
		
		// Append the B matrix
		sb.append("b:\n");		
		for ( int ii = 0; ii< this.Bmat.size(); ii++)
		{
			List<Double> bb = Bmat.get(ii);
			for (int jj=0; jj< bb.size() ; jj++) { sb.append(String.format("%.5f ", bb.get(jj)));}
			sb.append("\n");
		}

		// Append the PI
		sb.append("pi:\n");
		for ( Double dd: this.PI) sb.append(String.format("%.5f ", dd ));
		sb.append("\n");


		return sb.toString();
	}

	/**
	 * HMM parsing function from the .hmm file
	 * @param file
	 * @return A new HMM object with the parameters indicated in the file
	 * @throws Exception - In the event that the file passed to it is not formatted correctly 
	 */
	public static HMM parseHMM(String[] file) throws Exception 
	{
		/// set LINE=0
		int line = 0;

		// Use the first line to setup the HMM
		int [] parameters = TextTools.readIntegers(file[line]);
		HMM hmm = new HMM( parameters[0], parameters[1], parameters[2]);

		/// set LINE=1
		line++;	

		// Read the Syntatic Structure Words from the second Line
		hmm.addStructure( TextTools.readString(file[line]) );

		/// set LINE=2
		line++; 

		// Read the Vocabulary List from third line of file
		hmm.addVocabulary( TextTools.readString(file[line]));

		/// set LINE=3
		line++;	

		// Read the Matrix from the fourth line into the hmm's Amat
		if (file[line].contains("a:"))
		{
			line++;	// LINE = 4

			int start = 0;
			while(!file[line].contains("b:"))
			{	
				Double [] nums = TextTools.readDoubles( file[line] );
				for ( int end = 0; end < hmm.stateCount ; end++ )
				{
					hmm.Aset(start, end, nums[end]);
				}

				// Increment the start State index
				start++;
				line++;	// INCREMENT LINE
			}
		}
		else
		{
			throw new Exception("Couldn't find 'a:' on Line 4");
		}

		// We should we at the line "b:"
		if (file[line].contains("b:"))
		{
			line++;	// LINE = Starting Line of B matrix
			int start = 0;
			while(!file[line].contains("pi:"))
			{	
				Double [] nums = TextTools.readDoubles( file[line] );
				for ( int end = 0; end < hmm.observationCount ; end++ )
				{
					hmm.Bset(start, end, nums[end]);
				}

				// Increment the start State index
				start++;
				line++;	// INCREMENT LINE
			}
		}

		if (file[line].contains("pi:"))
		{
			line++; // We should be on the last line
			hmm.addPi( TextTools.readDoubles( file[line] ));
		}


		return hmm;
	}

}

