/**
 * 
 */
package net.cellingo.sequence_tools.alignment;

import java.util.List;

import net.cellingo.sequence_tools.annotation.SequenceCoordinates;
import net.cellingo.sequence_tools.annotation.SequenceElement;
import net.cellingo.sequence_tools.sequences.BiologicalSequence;
import net.cellingo.sequence_tools.sequences.IllegalSequenceOperation;
import net.cellingo.sequence_tools.sequences.NucleicAcidSequence;
import net.cellingo.sequence_tools.sequences.SequenceType;

/**
 * instances of this class do intramolecular alignments
 * @author Michiel Noback (www.cellingo.net, michiel@cellingo.net)
 * @version 1.0
 */
public class IntramolecularAligner extends Aligner {
	private SmithWatermanMatrix swMatrix;
	private AlignmentScoringMatrix scoringMatrix;
	private AlignmentCharacterMatrix alignmentCharacterMatrix;
	private List<Alignment> alignmentList;
	private BiologicalSequence currentSequence;
	private int currentSequenceLength;
	private char[] charArray;
	private int arrayLength;
	private boolean currentStrandIsForward;

	public IntramolecularAligner(AlignmentStrategy strategy, AlignmentAlgorithm algorithm, AlignmentOptions options ) {
		super(strategy, algorithm, options);
		this.currentStrandIsForward = true;
		this.scoringMatrix = getScoringMatrix();
		this.alignmentCharacterMatrix = getAlignmentCharacterMatrix();
		this.alignmentList = getAlignmentList();
	}

	
	/**
	 * overrides the void method of the Aligner base type
	 */
	public void doAlignment(){
		
		/*sliding window alignment*/
		for( BiologicalSequence sequence : getSequenceList() ){
			this.currentSequence = sequence;
			this.currentSequenceLength = currentSequence.getSequenceLength();
			if( this.getStrategy() == AlignmentStrategy.INTRAMOLECULAR_SLIDING_WINDOW){
				try {
					doSlidingWindow();
					
					if( getOptions().alsoComplementStrand() ){
						SequenceType sequenceType = currentSequence.getSequenceType();
						if(sequenceType == SequenceType.DNA || sequenceType == SequenceType.RNA){
							/*cast to DNA to be able to reverse complement*/
							NucleicAcidSequence sequenceDna = (NucleicAcidSequence) currentSequence;
							sequenceDna.reverseComplement();
							currentStrandIsForward = false;
							doSlidingWindow();
							/*reset everything to original status*/
							sequenceDna.reverseComplement();
							currentStrandIsForward = true;
						}
					}
					
				} catch (IllegalSequenceOperation e) {
					e.printStackTrace();
				}
			}
			else if( this.getStrategy() == AlignmentStrategy.INTRAMOLECULAR ){
				try {
					doCompleteAlignment();
				} catch (AlignmentException e) {
					System.out.println( e.getMessage() );
					//e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * complete alignment of all sequences
	 * @throws AlignmentException 
	 */
	private void doCompleteAlignment() throws AlignmentException{
		int minimumScore = (int)getOptions().getAlignmentPropertiesMinimumValues().getPropertyValue(AlignmentProperty.SW_SCORE);
		//System.out.println(" minimum score:" + minimumScore);
		
		charArray = currentSequence.getSequenceString().toCharArray();
		arrayLength = charArray.length;

		try{
			swMatrix = new SmithWatermanMatrix(arrayLength);
		}
		catch (Exception e) {
			throw new AlignmentException( "sequence too long for complete alignment: " + arrayLength );
		}
		
		//now calculate the scores and store in swMatrix
		int[] returnData = calculateScores( );
		int windowBestScore = returnData[0];
		int bestX = returnData[1];
		int bestY = returnData[2];
		if(windowBestScore>=minimumScore){	//only process with minimum score
			//check for duplicates in subsequent windows
			doAlignmentTraceback(bestX,bestY,0);
		}//end if >=minimum score
	}
	
	/**
	 * sliding window analysis
	 * @throws IllegalSequenceOperation
	 */
	private void doSlidingWindow() throws IllegalSequenceOperation{
		
		int minimumScore = (int)getOptions().getAlignmentPropertiesMinimumValues().getPropertyValue(AlignmentProperty.SW_SCORE);
		//System.out.println(" minimum score:" + minimumScore);
		int windowLength = getOptions().getWindowLength();
		int windowStep = getOptions().getWindowStep();
		
		swMatrix = new SmithWatermanMatrix(windowLength);

		//int subsequenceLength;				//length of the sliding window subsequence
		int position;						//start position of subSequence
		int previousBestX = 0;				//used to filter for duplicates in subsequent windows
		int previousWindowBestScore = 0;	//used to filter for duplicates in subsequent windows
		
		/*do the "sliding window" */
		for(position=0; position<(currentSequenceLength-windowStep); position+=windowStep){
			if((position+windowLength)<=currentSequenceLength){
				charArray = currentSequence.getSubSequence(position, (position+windowLength)).getSequenceString().toCharArray();
				arrayLength = charArray.length;
			}
			else{
				charArray = currentSequence.getSubSequence(position, currentSequenceLength-1).getSequenceString().toCharArray();
				arrayLength = charArray.length;
			}
			//System.out.println("arrayLength: " + arrayLength);
			
			//now calculate the scores and store in swMatrix
			int[] returnData = calculateScores( );
			int windowBestScore = returnData[0];
			int bestX = returnData[1];
			int bestY = returnData[2];
			if(windowBestScore>=minimumScore){	//only process with minimum score
				//check for duplicates in subsequent windows
				if(windowBestScore!=previousWindowBestScore && bestX!=(previousBestX-windowStep)){	
					//no duplicate; this new hairpin will be processed
					//do trace-back through swMatrix to obtain the alignment
					doAlignmentTraceback(bestX,bestY,position);
				}
				//reassign the "previous" values
				previousWindowBestScore = windowBestScore;
				previousBestX = bestX;
			}//end if >=minimum score
		}//end sliding this sequence
	}

	
	/**
	 * calculate the scores of the alignment. If seqTwoLengt == 0, a single-sequence intramolecular 
	 * alignment is assumed
	 * @return
	 */
	private int[] calculateScores( ){
		int gapOpen = getOptions().getGapOpenPenalty();
		int gapExtension = getOptions().getGapExtensionPenalty();
		int minimumAlignmentLength = (int)getOptions().getAlignmentPropertiesMinimumValues().getPropertyValue(AlignmentProperty.ALIGNMENT_LENGTH);
		AlignmentAlgorithm algorithm = getAlgorithm();
//		AlignmentScoringMatrix scoringMatrix = getScoringMatrix();
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
		
		for(int x=1; x<=(arrayLength-minimumAlignmentLength); x++){		//looping the forward strand: vertical
			for(int y=1; (x+y)<arrayLength; y++){	//looping the reverse strand: horizontal
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
				baseX = charArray[x-1];
				baseY = charArray[arrayLength-y];
				
				diagMoveScore = swMatrix.getScore((x-1),(y-1)) + scoringMatrix.getAlignmentScore(baseX,baseY);
//					System.out.println("baseX " + baseX + " baseY " + baseY + " alignmentScore " + scoringMatrix.getAlignmentScore(baseX,baseY) + " prevDiagMoveScore " + swMatrix.getScore((x-1),(y-1)) );
				
				//determine which of the three gives the highest score
				if(diagMoveScore>=horMoveScore && diagMoveScore>=vertMoveScore){ //diagonal move yields highest score
					currentBestScore = diagMoveScore;
					currentBestMove = 3;
				}
				else if(horMoveScore>=vertMoveScore){	//diagonal is NOT highest, therefore horizontal or vertical is
					currentBestScore = horMoveScore;
					currentBestMove = 2;	//ALERT!!! SOMEWHERE SOMETHING WRONG WITH MIX-UP OF HOR/VERT
				}
				else{	//vertical move is left as best score
					currentBestScore = vertMoveScore;
					currentBestMove = 1;	//ALERT!!! SOMEWHERE SOMETHING WRONG WITH MIX-UP OF HOR/VERT
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
		
		//obtain data of alignment starting position
		int endPositionForwardStrand = position + x;
		int endPositionReverseStrand = position + (arrayLength - y);
		alignmentScore = swMatrix.getScore(x,y);
		
		while(swMatrix.getScore(x,y)>0){	//do traceback while score is above zero
			localPath = swMatrix.getPath(x,y);
			
			if(localPath==3){	//diagonal path
				forwardBase = charArray[x-1];
				reverseBase = charArray[arrayLength-y];
				//get midLine character
				midLineCharacter = alignmentCharacterMatrix.getAlignmentCharacter(forwardBase, reverseBase);
				x = x-1;
				y = y-1;
			}
			else if(localPath == 2){	//vertical path
				reverseBase = charArray[arrayLength-y];
				forwardBase = '-';
				midLineCharacter = ' ';
				y = y - 1;
			}
			else{	//(localPath == 1 || localPath == 0) horizontal path
				reverseBase = '-';
				forwardBase = charArray[x-1];
				midLineCharacter = ' ';
				x = x - 1;
			}
			//add to alignment lines
			forwardStrand.append(forwardBase);
			reverseStrand.append(reverseBase);
			midLine.append(midLineCharacter);
		}
		//get start positions of both strands
		startPositionForwardStrand = position + x;
		startPositionReverseStrand = position + (arrayLength - y);
		
		alignmentLength = forwardStrand.length();

		//get relative score
		double relScore = (double) alignmentScore/alignmentLength;
		
		if(relScore>=minimumRelativeScore && alignmentLength>=minimumAlignmentLength){//only process if relative score greater than minimum

			int loopSequenceStart = endPositionForwardStrand-1;
			int loopSequenceEnd = endPositionReverseStrand-1;
			
			/*get positions right*/
//			startPositionForwardStrand+=1;
			endPositionForwardStrand-=1;
			startPositionReverseStrand-=1;
			
			if(! currentStrandIsForward){//reverse strand analysis with sliding window
				startPositionForwardStrand = currentSequenceLength - startPositionForwardStrand;
				endPositionForwardStrand = currentSequenceLength - endPositionForwardStrand;
				startPositionReverseStrand = currentSequenceLength - startPositionReverseStrand;
				endPositionReverseStrand = currentSequenceLength - endPositionReverseStrand;
			}
			
			/*create Alignment object*/
			HairpinAlignment alignment = new HairpinAlignment();
			alignment.setOnComplement(!currentStrandIsForward);
			alignment.setAligmentType( Alignment.ALIGNMENT_TYPE_INTRAMOLECULAR );
			SequenceElement topParent = new SequenceElement( );
			topParent.setParentSequence(currentSequence);
			SequenceCoordinates top = new SequenceCoordinates(startPositionForwardStrand, endPositionForwardStrand, (!currentStrandIsForward), true );
			topParent.addCoordinates(top);
/*			topParent.setParentStart(startPositionForwardStrand);
			topParent.setParentStop(endPositionForwardStrand);
*/			alignment.setTopParent( topParent );

			SequenceElement bottomParent = new SequenceElement( );
			bottomParent.setParentSequence(currentSequence);
			SequenceCoordinates bott = new SequenceCoordinates(startPositionReverseStrand, endPositionReverseStrand, (!currentStrandIsForward), true );
			bottomParent.addCoordinates(bott);
/*			bottomParent.setParentStart(startPositionReverseStrand);
			bottomParent.setParentStop(endPositionReverseStrand);
*/			alignment.setBottomParent( bottomParent );

			alignment.setTopStrand( forwardStrand.reverse() );
			alignment.setBottomStrand( reverseStrand.reverse() );
			alignment.setMiddleLine( midLine.reverse() );

			alignment.setAlignmentScore( alignmentScore );
			
			SequenceElement loop = new SequenceElement();
			loop.setParentSequence(currentSequence);
			SequenceCoordinates lc = new SequenceCoordinates(loopSequenceStart+1, loopSequenceEnd, (!currentStrandIsForward), true );
			loop.addCoordinates(lc);
/*			loop.setParentStart(loopSequenceStart+1);
			loop.setParentStop(loopSequenceEnd);
*/			alignment.setHairpinLoop(loop);
			
			/*add to alignment list*/
			alignmentList.add( alignment );
			
		}//end if >=minimumRelativeScore and if >=minimum length
	}//end alignment traceback

	
	
}

