/**
 * 
 */
package net.cellingo.sequence_tools.alignment;

import java.util.List;

import net.cellingo.sequence_tools.annotation.SequenceCoordinates;
import net.cellingo.sequence_tools.annotation.SequenceElement;
import net.cellingo.sequence_tools.sequences.BiologicalSequence;
import net.cellingo.sequence_tools.sequences.NucleicAcidSequence;
import net.cellingo.sequence_tools.sequences.SequenceType;

/**
 * instances of this class perform intermolecular alignments
 * @author Michiel Noback (michiel@cellingo.net)
 * @version 1.0
 */
public class IntermolecularAligner extends Aligner {
	private SmithWatermanMatrix swMatrix;
	private AlignmentScoringMatrix scoringMatrix;
	private AlignmentCharacterMatrix alignmentCharacterMatrix;
	private List<Alignment> alignmentList;
	private BiologicalSequence currentSequenceOne;
	private BiologicalSequence currentSequenceTwo;
	//private int currentSequenceOneLength;
	//private int currentSequenceTwoLength;
	private char[] charArrayOne;
	private char[] charArrayTwo;
	private int arrayOneLength;
	private int arrayTwoLength;
	private boolean currentStrandIsForward = true;
	boolean sequenceTwoReversed = false;

	public IntermolecularAligner( AlignmentStrategy strategy, AlignmentAlgorithm algorithm, AlignmentOptions options ){
		super(strategy, algorithm, options );
		this.scoringMatrix = options.getAlignmentScoringMatrix();
		this.alignmentCharacterMatrix = options.getAlignmentCharacterMatrix();
		alignmentList = getAlignmentList();
	}
	
	/**
	 * overrides the void method of the Aligner base type
	 */
	public void doAlignment(){
		/*first determine maxuimum size in sequence list*/
		int maximumLength = findMaximumLength();
		/*create the matrix to store scores and paths in*/
		
		swMatrix = new SmithWatermanMatrix(maximumLength);
		int listSize = getSequenceList().size();
		if( this.getStrategy() == AlignmentStrategy.INTERMOLECULAR_FIRST_TO_ALL){
			currentSequenceOne = getSequenceList().get(0);
			
			/*now loop the rest of the sequence collection and align*/
			for(int count=1; count<listSize; count++){
				currentSequenceTwo = getSequenceList().get(count);
				doSingleAlignment();
			}
			
			if( getOptions().alsoComplementStrand() ){
				SequenceType sequenceType = currentSequenceOne.getSequenceType();
				if(sequenceType == SequenceType.DNA || sequenceType == SequenceType.RNA){
					//cast to DNA to be able to reverse complement
					NucleicAcidSequence sequenceOneDna = (NucleicAcidSequence) currentSequenceOne;
					sequenceOneDna.reverseComplement();
					currentStrandIsForward = false;

					/*now loop the rest of the sequence collection again and align*/
					for(int count=1; count<listSize; count++){
						currentSequenceTwo = getSequenceList().get(count);
						doSingleAlignment();
					}

					//reset everything to original status
					sequenceOneDna.reverseComplement();
					currentStrandIsForward = true;
				}
			}


		}
		else if( this.getStrategy() == AlignmentStrategy.INTERMOLECULAR_ALL_TO_ALL ){
			/*now loop the sequence collection and align*/
			for(int i=0; i<listSize; i++){
				currentSequenceOne = getSequenceList().get(i);
				/*now loop the rest of the sequence collection and align*/
				for(int j=i+1; j<listSize; j++){
					currentSequenceTwo = getSequenceList().get(j);
					doSingleAlignment();
				}//countSecond
				
				
				if( getOptions().alsoComplementStrand() ){
					SequenceType sequenceType = currentSequenceOne.getSequenceType();
					if(sequenceType == SequenceType.DNA || sequenceType == SequenceType.RNA){
						//cast to DNA to be able to reverse complement
						NucleicAcidSequence sequenceOneDna = (NucleicAcidSequence) currentSequenceOne;
						sequenceOneDna.reverseComplement();
						currentStrandIsForward = false;

						/*now loop the rest of the sequence collection again and align*/
						for(int j=i+1; j<listSize; j++){
							currentSequenceTwo = getSequenceList().get(j);
							doSingleAlignment();
						}//countSecond

						//reset everything to original status
						sequenceOneDna.reverseComplement();
						currentStrandIsForward = true;
					}
				}
			}//countFirst
		}
			
			
	}

	private void doSingleAlignment(){//int sequenceOneLength, int sequenceTwoLength
		int minimumScore = (int)getOptions().getAlignmentPropertiesMinimumValues().getPropertyValue(AlignmentProperty.SW_SCORE);
		
		//currentSequenceOneLength = currentSequenceOne.getSequenceLength();
		charArrayOne = currentSequenceOne.getSequenceString().toCharArray();
		arrayOneLength = charArrayOne.length;
		
		//currentSequenceTwoLength = currentSequenceTwo.getSequenceLength();
		charArrayTwo = currentSequenceTwo.getSequenceString().toCharArray();
		arrayTwoLength = charArrayTwo.length;
		
		if( (scoringMatrix.getMatrixType() == AlignmentMatrixType.RNA_STRUCTURE_ALIGNMENT) || (scoringMatrix.getMatrixType() == AlignmentMatrixType.STRUCTURE_WEIGHTED_ALIGNMENT)){
			//do reversal of nucleic acid sequence
			currentSequenceTwo.reverse();

			//TODO correct the positions after the alignment
			sequenceTwoReversed = true;
		}


		//now calculate the scores and store in swMatrix
		int[] returnData = calculateScores( );
		int bestScore = returnData[0];
		int bestX = returnData[1];
		int bestY = returnData[2];
//			System.out.println("wbs: " + windowBestScore + " x: " + bestX + " y: " + bestY);
		if(bestScore >= minimumScore){	//only process with minimum score
			doAlignmentTraceback(bestX,bestY,0);
		}
		
		if( sequenceTwoReversed ){//reverse back to normal
			currentSequenceTwo.reverse();
		}
		
	}

	private int[] calculateScores( ){
		int gapOpen = getOptions().getGapOpenPenalty();
		int gapExtension = getOptions().getGapExtensionPenalty();
		int minimumAlignmentLength = (int)getOptions().getAlignmentPropertiesMinimumValues().getPropertyValue(AlignmentProperty.ALIGNMENT_LENGTH);
		AlignmentAlgorithm algorithm = getAlgorithm();
		int[] best = new int[3];
		char baseX;	
		char baseY;
		int currentBestMove; 	//current alignment move: 0=border; 1=horizontal; 2=vertical; 3=diagonal
		int horMoveScore;		//score for horizontal move
		int vertMoveScore;		//score for vertical move
		int diagMoveScore;		//score for diagonal move
		int currentBestScore;	//best score of current alignment position
		int previousHorMove;	//move value in cell to the left 
		int previousVertMove;	//move value in cell above 
		int bestX = 0;			//x-index for best score in current sequence
		int bestY = 0;			//y-index for best score in current sequence
		int windowBestScore=0;	//best score in current analysis window
		
		/*two-sequence alignments*/
		for(int x=1; x<arrayOneLength-minimumAlignmentLength; x++){		//looping sequence one
			for(int y=1; y<arrayTwoLength; y++){	//looping sequence two
				//first determine move value in cell left and top
				previousHorMove = swMatrix.getPath(x, (y-1));
				previousVertMove = swMatrix.getPath((x-1), y);
				
				//determine score for horizontal move, depending on previous
				if(previousHorMove==0 || previousHorMove==1){
					horMoveScore = swMatrix.getScore(x,(y-1)) + gapExtension;
				}
				else{	//previousHorMove==2 || previousHorMove==3 
					horMoveScore = swMatrix.getScore(x,(y-1)) + gapOpen;
				}

				//determine score for vertical move, depending on previous
				if(previousVertMove==0 || previousVertMove==2){
					vertMoveScore = swMatrix.getScore((x-1),y) + gapExtension;
				}
				else{	//previousVertMove==1 || previousVertMove==3
					vertMoveScore = swMatrix.getScore((x-1),y) + gapOpen;
				}
				
				//determine score for diagonal move
				baseX = charArrayOne[x-1];
				baseY = charArrayTwo[y-1];
				diagMoveScore = swMatrix.getScore((x-1),(y-1)) + scoringMatrix.getAlignmentScore(baseX,baseY);
				
				//determine which of the three gives the highest score
				if(diagMoveScore>=horMoveScore && diagMoveScore>=vertMoveScore){ //diagonal move yields highest score
					currentBestScore = diagMoveScore;
					currentBestMove = 3;
				}
				else if(horMoveScore>=vertMoveScore){	//diagonal is NOT highest, therefore horizontal or vertical is
					currentBestScore = horMoveScore;
					currentBestMove = 2;
				}
				else{	//vertical move is left as best score
					currentBestScore = vertMoveScore;
					currentBestMove = 1;
				}
								
				/*decide on global/local alignments*/
				if(currentBestScore<0 && (algorithm==AlignmentAlgorithm.LOCAL)){	//check for scores below 0 and correct if necessary
					currentBestScore = 0;
				}
				
				//now write best score and move to sw matrix
				swMatrix.setScore(x, y, currentBestScore);
				swMatrix.setPath(x, y, currentBestMove);
				
				//obtain highest score for window
				if(currentBestScore>windowBestScore){
					windowBestScore = currentBestScore;
					bestX = x;
					bestY = y;
				}
			}//end reverse strand
		}//end forward strand

		//return main values
		best[0] = windowBestScore;
		best[1] = bestX;
		best[2] = bestY;
		return best;
	}//end calculateScores

	/**
	 * This method takes care of creating the alignment 
	 * of two pairing stretches of nucleic acid
	 * */
	private void doAlignmentTraceback(int bestX, int bestY, int position) {
		double minimumRelativeScore = getOptions().getAlignmentPropertiesMinimumValues().getPropertyValue(AlignmentProperty.RELATIVE_SCORE);
		int minimumAlignmentLength = (int)getOptions().getAlignmentPropertiesMinimumValues().getPropertyValue(AlignmentProperty.ALIGNMENT_LENGTH);
		int x = bestX;
		int y = bestY;
		int endPositionForwardStrand = x;
		int endPositionReverseStrand = y;
		int startPositionForwardStrand;
		int startPositionReverseStrand;
		
		int alignmentScore;
		char forwardBase;
		char reverseBase;
		char midLineCharacter;
		int localPath;
		int alignmentLength;

		StringBuilder forwardStrand = new StringBuilder();
		StringBuilder reverseStrand = new StringBuilder();
		StringBuilder midLine = new StringBuilder();
		
		alignmentScore = swMatrix.getScore(x,y);	

		while(swMatrix.getScore(x,y)>0){	//do traceback while score is above zero
			//get new localScore
			localPath = swMatrix.getPath(x,y);
			
			if(localPath==3){	//diagonal path
				forwardBase = charArrayOne[x];
				reverseBase = charArrayTwo[y];
				//get midLine character
				midLineCharacter = alignmentCharacterMatrix.getAlignmentCharacter(forwardBase, reverseBase);
				x = x-1;
				y = y-1;
			}
			else if(localPath == 2){	//vertical path
				reverseBase = charArrayTwo[y];
				forwardBase = '-';
				midLineCharacter = ' ';
				y = y - 1;
			}
			else{	//(localPath == 1 || localPath == 0) horizontal path
				reverseBase = '-';
				forwardBase = charArrayOne[x];
				midLineCharacter = ' ';
				x = x - 1;
			}
			//add to alignment lines
			forwardStrand.append(forwardBase);
			reverseStrand.append(reverseBase);
			midLine.append(midLineCharacter);

		}
		//get start positions of both strands
		startPositionForwardStrand = x;
		startPositionReverseStrand = y;

		//convert ArrayLists to Strings
		alignmentLength = forwardStrand.length();
		
		//get relative score
		double relScore = (double) alignmentScore/alignmentLength;
		
		if(relScore>=minimumRelativeScore && alignmentLength>=minimumAlignmentLength){//only process if relative score greater than minimum

			/*get positions right*/
			startPositionForwardStrand+=1;
			startPositionReverseStrand+=1;
			
			if( (getStrategy() == AlignmentStrategy.INTRAMOLECULAR_SLIDING_WINDOW) || (getStrategy() == AlignmentStrategy.INTRAMOLECULAR) ){
				if(! currentStrandIsForward){//reverse strand analysis with sliding window
					startPositionForwardStrand = arrayOneLength - startPositionForwardStrand;
					endPositionForwardStrand = arrayOneLength - endPositionForwardStrand;
					startPositionReverseStrand = arrayOneLength - startPositionReverseStrand;
					endPositionReverseStrand = arrayOneLength - endPositionReverseStrand;
				}
			}
			if(sequenceTwoReversed){
				startPositionReverseStrand = arrayTwoLength - startPositionReverseStrand;
				endPositionReverseStrand = arrayTwoLength - endPositionReverseStrand;
			}
			

			/*create Alignment object*/
			Alignment alignment = new Alignment();
			
			alignment.setAligmentType( Alignment.ALIGNMENT_TYPE_INTERMOLECULAR );
			SequenceElement topParent = new SequenceElement( );
			topParent.setParentSequence( currentSequenceOne );
			SequenceCoordinates top = new SequenceCoordinates(startPositionForwardStrand, endPositionForwardStrand, (!currentStrandIsForward), true );
			topParent.addCoordinates(top);
/*			topParent.setParentStart(startPositionForwardStrand);
			topParent.setParentStop(endPositionForwardStrand);
*/			alignment.setTopParent( topParent );

			SequenceElement bottomParent = new SequenceElement( );
			bottomParent.setParentSequence( currentSequenceTwo );
			SequenceCoordinates bott = new SequenceCoordinates(startPositionReverseStrand, endPositionReverseStrand, (!currentStrandIsForward), true );
			bottomParent.addCoordinates(bott);
/*			bottomParent.setParentStart(startPositionReverseStrand);
			bottomParent.setParentStop(endPositionReverseStrand);
*/			alignment.setBottomParent( bottomParent );

			alignment.setTopStrand( forwardStrand.reverse() );
			alignment.setBottomStrand( reverseStrand.reverse() );
			alignment.setMiddleLine( midLine.reverse() );

			alignment.setAlignmentScore( alignmentScore );
			
			/*add to alignment list*/
			alignmentList.add( alignment );

		}//end if >=minimumRelativeScore and if >=minimum length
	}//end alignment traceback

	/**
	 * find the maximum length in the sequence collection
	 * @return
	 */
	private int findMaximumLength(){
		int maximumLength = 0;
		int length = 0;
		for(BiologicalSequence sequence: getSequenceList() ){
			length = sequence.getSequenceLength();
			if(length > maximumLength){
				maximumLength = length;
			}
		}
		return maximumLength;
	}

}

