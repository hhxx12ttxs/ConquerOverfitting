package edu.bu;

/**
 * @author Abhinay
 *
 */
public class ScoreEvaluator {

    NeighborsGenerator nbgen = new NeighborsGenerator();
	// Computes the score of a move by evaluating the best possible 
    // consecutive move that can be made by the competitor. 
    //
    // This function returns an average score if the competitor can play anywhere. 
    //
    // Since it is the best possible score for the competitor the scores are always
    // negative and a score of 0 implies defeat for the competitor. 
    public int evaluate(int[] neighbors, int height, int leftDistance, AtroposState currentstate) {
                
		int free   = 0;
        int score  = 0;
               
        // Counts the number of freenodes.
        for (int i = 0; i < neighbors.length; i++) {
     
            if (neighbors[i] == 0) 
                free++;

        }
               
        // If the current move offers the competitor the choice
        // of coloring anywhere on the board then return a 
        // decent score as he/it has a lesser chance of setting
        // a trap.
        if (free == 0)
            return -2;

        // Evaluate the scores of the neighbors of the 
        // last move.
        for (int j = 0; j < neighbors.length; j++) {
                    
            int currentscore = 0;
            int[] nofn       = {0, 0, 0, 0, 0, 0};       
                    
            if (neighbors[j] == 0) {
                        
                if (j == 0)
                    nofn = nbgen.generate(height  , leftDistance-1, currentstate);
                if (j == 1)
                    nofn = nbgen.generate(height+1, leftDistance-1, currentstate);
                if (j == 2)
                    nofn = nbgen.generate(height+1, leftDistance  , currentstate);
                if (j == 3)
                    nofn = nbgen.generate(height  , leftDistance+1, currentstate);
                if (j == 4)
                    nofn = nbgen.generate(height-1, leftDistance+1, currentstate);
                if (j == 5)
                    nofn = nbgen.generate(height-1, leftDistance  , currentstate);
                        
                // Replaces the color value for green with 5
                // and counts the number of uncolored neighbors.
                for (int i = 0; i < neighbors.length; i++) {
                    
                    if (neighbors[i] == 3)
                        neighbors[i] =  5;
                }
                        
                        
                for (int k = 0; k < nofn.length; k++) {
                            
                    // Integer array to store possible
                    // colors for the current circle.
                    //
                    // Index 1 for Red, 2 fpr Blue 
                    // and 5 for Green (replaced value).
                    // Indexes 0, 3 and 4 are not used.
                    // 
                    // Value 1 at Index indicates that the color is 
                    // possible and value 0 indicates 
                    // otherwise.
                    int[] temp = {0, 1, 1, 0, 0, 1};
                           
                    if (nofn[(k)%nofn.length]   != nofn[(k+1)%nofn.length] 
                            && nofn[(k)%nofn.length] != 0 && nofn[(k+1)%nofn.length] != 0)
                        temp[Math.abs(nofn[(k)%nofn.length] + nofn[(k+1)%nofn.length] - 8)] = 0;

                    for (int l = 0; l < temp.length; l++) {
                        if (temp[l] != 0)
                              currentscore = currentscore - l;                            
                    }
                }
            }
            score = Math.min(score, currentscore);                     
        }
        return score;
    }
}

