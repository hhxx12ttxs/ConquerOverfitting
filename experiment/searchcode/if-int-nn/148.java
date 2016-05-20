/*
 * Created by Toomas Kirt
 * Last change 25.05.2010 15:59
 * 
 * Modified by Geert Alberghs
 * - Cosmetics (preserve variable name logic with regard to rest of code))
 * - Modified GenerateIndividual (create more valid individuals by using viReqLvls & viSitLvls)
 * - Modified ValidateData (avoid presence of vPop={0})
 * - Modified stopcriterium (_iGen)
 * - Compare budget with maximum cost (faster solution)
 * - Include update cost in costFn method
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class EvoOptAlgo extends Optimizer{
	
    ///////////////////////////////////////////////////////
	// goal of this class:								 //
	// * find optimum ProtProf for a certain budgetlevel //
	// * keep statistics of the optimization process     //
	///////////////////////////////////////////////////////
	
	/** **********  singleton design pattern  ********** */
	
	/** cache reusable variables */
	// these variables don't change during the entire program
		// Evolutionary Algorithm parameters
		// -> see superclass
	
	    // Optimization problem parameters
		// -> see superclass
	
	// these variables are different for each generation
	 // Evolutionary Algorithm parameters
	    private static double[][] mdStat;       // Collected statistics (time and fitness value) for each generation
	    
	    private int[][]      miPop;             // Population matrix
	    private double[]     vdFitness;         // Vector of fitness values of each individual in a population 
	    private int[][]      miPopNew;          // Temporary population matrix for generating offspring
	    private double[]     vdFitnessNew;      // Temporary fitness vector for generating offspring
	    
	// these variables might be different for each round
	  // optimization problem parameters
	    private double       dBudget;           // Available budget for a certain year
	    private double	     dMinBudget;        // Smallest future budget available (last year or next year budget, depending on sign _dBudgetEvo)
	    private double       dReqCostI;         // Cost for achieving required measure group levels
	    private double       dReqCostU;		    // Cost for maintaining required measure group levels
	    
	    private int[]        viCurLvls;		    // Measure group levels at a certain year
	    private int[]        viMinLvls;		    // Minimum levels for each measure group
        private int[]        viMaxLvls;         // Maximum levels for each measure group
	
	/** create singleton */
	private static EvoOptAlgo singletonEOA;
	
	/** replace the default constructor with a private one */
	private EvoOptAlgo(){	
	}
	/** method to get a reference to the Singleton Object */
	// synchronization is needed in case two or more threads would try to access the singleton at the same time
	public static synchronized EvoOptAlgo getSingletonObject(){
		if (singletonEOA==null) singletonEOA = new EvoOptAlgo();
		return singletonEOA;
	}
	/** overwrite clone method to avoid duplicates */
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
	/** methods to set all singleton attributes */
	/** initialization method */
	public void initSingletonEOA(double dInitBudget, double dInitMinBudget, int iInitYear, int[] viInitCurLvls){
		
		System.out.print("Initializing Evolutionary Algorithm ... ");
		System.out.print(" Budget = " + dInitBudget);
		
		// Debugging Info
		mdStat = new double[_iGen + 1][2]; mdStat[0][0] = System.currentTimeMillis();
        // Optimization data
        dBudget    = dInitBudget;
        dMinBudget = dInitMinBudget;
        
        viCurLvls  = viInitCurLvls;
        
        dReqCostI  = CostFn(_viReqLvls, viCurLvls);
        dReqCostU  = CostFn(_viReqLvls, _viReqLvls);        
        
        viMinLvls  = new int[_iNrOfMGs];
        viMaxLvls  = new int[_iNrOfMGs];
        
        if ( !(dBudget > dReqCostI && dMinBudget > dReqCostU)) {
    		System.arraycopy(viCurLvls, 0, viMinLvls, 0, _iNrOfMGs);
    		// initial situations can be higher than the required levels
    		for (int i=0;i<_iNrOfMGs;i++) viMaxLvls[i]=(viCurLvls[i]>_viReqLvls[i])?viCurLvls[i]:_viReqLvls[i];
    	}
    	else{
    		// initial situations can be higher than the required levels
    		for (int i=0;i<_iNrOfMGs;i++) viMinLvls[i]=(viCurLvls[i]>_viReqLvls[i])?viCurLvls[i]:_viReqLvls[i];
    		System.arraycopy(_viMaxLvls, 0, viMaxLvls, 0, _iNrOfMGs);
    	}
        
        // Optimization variables
        miPopNew = new int[_iPop][_iNrOfMGs]; vdFitnessNew = new double[_iPop];
        miPop    = new int[_iPop][_iNrOfMGs]; vdFitness    = new double[_iPop];
        // if in first budget point --> initPop()
        // else miPop and vdFitness will contain previous budget point's results for the same year
        if (Arrays.deepEquals(_3DPrvPop[iInitYear], _miAllZeros)) initPop();
        else miPop = _3DPrvPop[iInitYear].clone();
        
        statEvo(0);
        
		System.out.println(" ... done");
	}
	
	// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

	/** **********  Evolutionary Algorithm methods  ********** */
	/** Initialize population by randomly creating individuals */
	public void initPop() {
    	ReliabCalc singletonRC = ReliabCalc.getSingletonObject();
        for (int i=0; i<_iPop; i++) {
            miPop[i]     = GenerateIndividual(miPop[i]);
            miPop[i]     = ValidateData(miPop[i]);
            vdFitness[i] = singletonRC.calcEffect(miPop[i]);
        }
	}
	/** Collects statistics: array of (time, fitness value) couples */
	public void statEvo(int iCount) {
	    // Collects statistics - time and fitness value
	    mdStat[iCount][0] = System.currentTimeMillis() - mdStat[0][0];
	    double nMax = vdFitness[0];
	    for (int i=1; i<_iPop; i++) {
	        if ( vdFitness[i] > nMax ) nMax = vdFitness[i];
	    }
	    mdStat[iCount][1] = nMax;
	}

    // -------------------------------------------------------------------------------------------------------------------------------------------------------
    /** Random initialization of an individual */
    public int[] GenerateIndividual(int[] vPop) {
    	int iI = 0;
        int nN = 0;
    	
        int[] vPopA       = new int[_iNrOfMGs];
        int[] vPerm       = permutation(_iNrOfMGs);
        
        double nCostI = CostFn(vPopA, viCurLvls); // Total Cost (Invest + Update) must be below budget
        double nCostU = CostFn(vPopA, vPopA);     // Update Cost for future years must be below budget
    	
    	while (nCostI <= dBudget && nCostU <= dMinBudget && iI < _iNrOfMGs) { // repeat until available budget level
    		
    		System.arraycopy(vPopA, 0, vPop, 0, _iNrOfMGs);
            nN = vPerm[iI];
            vPopA[nN] = random_integers(viMinLvls[nN], viMaxLvls[nN]);
            
            nCostI = CostFn(vPopA,viCurLvls);
            nCostU = CostFn(vPopA,vPopA);
            
            iI++;
        }
        return vPop;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------------
    /** Validation of an individual */
    public int[] ValidateData(int[] vPop) {
        for (int iR = 0; iR < _iNrOfMGs; iR++) {
            if (vPop[iR] < viMinLvls[iR]) vPop[iR] = viMinLvls[iR];
            if (vPop[iR] > viMaxLvls[iR]) vPop[iR] = viMaxLvls[iR];
        }
        // If an individual is inadequate
        if (CostFn(vPop,viCurLvls) > dBudget || CostFn(vPop,vPop) > dMinBudget) System.arraycopy(viMinLvls, 0, vPop, 0, _iNrOfMGs);
        return vPop;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------------
    /** Function to generate a vector of nRange permutated integer numbers */
    public int[] permutation(int nRange) {
        Random random = new Random();
        boolean[] iInd = new boolean[nRange];
        int[] vPerm = new int[nRange];
        for (int iI = 0; iI < nRange; iI++) {
            int nRandInt = random.nextInt(nRange);
            while (iInd[nRandInt]) {
                if (nRandInt == nRange - 1)
                    nRandInt = 0;
                else
                    nRandInt++;
            }
            vPerm[iI] = nRandInt;
            iInd[nRandInt] = true;
        }
        return vPerm;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------------
    /** Generates an integer range min to max */
    public int random_integers(int nMin, int nMax) {
        int value = (int) Math.round((Math.random() * (nMax - nMin)) + nMin);
        return value;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------------
    /** Sorts a vector in a descending order and gives out index of reordered values */
    public int[] sortInd(double[] vVect) {
        int[] vInd = new int[vVect.length];
        for (int iI = 0; iI < vInd.length; iI++) {
            vInd[iI] = iI;
        }
        int iMax;
        for (int iI = 0; iI < vVect.length; iI++) {
            iMax = vInd[iI];
            for (int iJ = iI; iJ < vVect.length; iJ++) {
                if ( vVect[vInd[iI]] < vVect[vInd[iJ]] ) {
                    vInd[iI] = vInd[iJ];
                    vInd[iJ] = iMax;
                    iMax = vInd[iI];
                }
            }
        }
        return vInd;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------------
    /** Variation operators */
    public int[] Crossover(int[] vPop, int iPos) {
        // Crossover variation operator
        int nParent = random_integers(0, _iPop - 1); // Selects a parent
        int[] vRange = new int[2];
        if (iPos != nParent) {
            vRange[0] = random_integers(0, _iNrOfMGs - 1); // Selects beginning of a segment
            vRange[1] = random_integers(0, _iNrOfMGs - 1); // Selects ending of a segment
            Arrays.sort(vRange);
            for (int iJ = vRange[0]; iJ <= vRange[1]; iJ++)
                vPop[iJ] = this.miPop[nParent][iJ]; // Changes the segment
        }
        return vPop;
    }

    public int[] Mutate(int[] vPop) {
        int nRand = random_integers(0, _iNrOfMGs - 1); // Selects a gene for mutation
        vPop[nRand] = random_integers(viMinLvls[nRand], viMaxLvls[nRand]);
        return vPop;
    }

    public int[] Swap(int[] vPop) {
        // Swaps two genes
        int[] vRand = new int[2];
        vRand[0] = random_integers(0, _iNrOfMGs - 1); // Select points to swap
        vRand[1] = random_integers(0, _iNrOfMGs - 1);
        Arrays.sort(vRand);
        int nA = vPop[vRand[0]];
        vPop[vRand[0]] = vPop[vRand[1]];
        vPop[vRand[1]] = nA;
        return vPop;
    }

    public int[] Inversion(int[] vPop) {
        // inverses the order selected genes 
        int[] vPopNew = new int[_iNrOfMGs];
        for (int iR = 0; iR < _iNrOfMGs; iR++)
            vPopNew[iR] = vPop[iR];
        int[] vRange = new int[2];
        vRange[0] = random_integers(0, _iNrOfMGs - 1); // Selects beginning of a segment
        vRange[1] = random_integers(0, _iNrOfMGs - 1); // selects end of a segment
        Arrays.sort(vRange);
        for (int iI = 0; iI < (vRange[1] - vRange[0] + 1); iI++)
            vPopNew[vRange[0] + iI] = vPop[vRange[1] - iI];
        return vPopNew;
    }

    public int[] Insertion(int[] vPop) {
        // inserts a gene in the other place
        int[] vPopNew = new int[_iNrOfMGs]; // A copy of genes
        for (int iR = 0; iR < _iNrOfMGs; iR++)
            vPopNew[iR] = vPop[iR];
        int nWhere = random_integers(0, _iNrOfMGs - 1); // Where to insert
        int[] vWhat = new int[2]; // What to insert
        vWhat[0] = random_integers(0, _iNrOfMGs - 1); // Selects the beginning of a segment what to move
        vWhat[1] = vWhat[0];
        if (nWhere < vWhat[0] || nWhere > vWhat[1]) {
            int[] vCoord = Coordinates(nWhere, vWhat);
            for (int iR = 0; iR < _iNrOfMGs; iR++)
                vPopNew[iR] = vPop[vCoord[iR]];
        }
        return vPopNew;
    }

    public int[] Displacement(int[] vPop) {
        // Displaces a segment of genes in the other place 
        int[] vPopNew = new int[_iNrOfMGs];
        for (int iR = 0; iR < _iNrOfMGs; iR++)
            vPopNew[iR] = vPop[iR];
        int nWhere = random_integers(0, _iNrOfMGs - 1); // Where to insert
        int[] vWhat = new int[2]; // What to insert
        vWhat[0] = random_integers(0, _iNrOfMGs - 1); // Selects the beginning of a segment what to move
        vWhat[1] = random_integers(0, _iNrOfMGs - 1);
        Arrays.sort(vWhat);
        if (nWhere < vWhat[0] || nWhere > vWhat[1]) {
            int[] vCoord = Coordinates(nWhere, vWhat);
            for (int iR = 0; iR < _iNrOfMGs; iR++)
                vPopNew[iR] = vPop[vCoord[iR]];
        }
        return vPopNew;
    }

    public int[] Coordinates(int nWhere, int[] vWhat) {
        // auxiliary function for insertion and displacement operators
        int[] vCoord = new int[_iNrOfMGs];
        int[][] vInd = new int[4][2];
        
        if (nWhere < vWhat[0]) {
            vInd[0][0] = 0;
            vInd[0][1] = nWhere; // range(0,vRand)
            vInd[1][0] = vWhat[0];
            vInd[1][1] = vWhat[1] + 1; // range(vRange[0],vRange[1]+1)
            vInd[2][0] = nWhere;
            vInd[2][1] = vWhat[0]; // range(vRand,vRange[0])
            vInd[3][0] = vWhat[1] + 1;
            vInd[3][1] = _iNrOfMGs; // range(vRange[1]+1,nR)
        }
        else {
            vInd[0][0] = 0;
            vInd[0][1] = vWhat[0]; // range(0,vRange[0])
            vInd[1][0] = vWhat[1] + 1;
            vInd[1][1] = nWhere + 1; // range(vRange[1]+1,vRand])
            vInd[2][0] = vWhat[0];
            vInd[2][1] = vWhat[1] + 1; // range(vRange[0],vRange[1]+1)
            vInd[3][0] = nWhere + 1;
            vInd[3][1] = _iNrOfMGs; // range(vRand,nR)
        }
        int nI = 0;
        int nC = 0;
        for (int iJ = 0; iJ < vInd.length; iJ++) {
            nC = vInd[iJ][0];
            while (nC < vInd[iJ][1]) {
                vCoord[nI] = nC;
                nC++;
                nI++;
            }
        }
        return vCoord;
    }

    public int[] Variate(int iPos) {
        int[] vPop = new int[_iNrOfMGs];
        for (int iJ = 0; iJ < _iNrOfMGs; iJ++)
            vPop[iJ] = this.miPop[iPos][iJ];

        if (Math.random() < _vfVar[_iCrossover])    vPop = Crossover(vPop, iPos);
        if (Math.random() < _vfVar[_iMutate])       vPop = Mutate(vPop);
        if (Math.random() < _vfVar[_iSwap])         vPop = Swap(vPop);
        if (Math.random() < _vfVar[_iInversion])    vPop = Inversion(vPop);
        if (Math.random() < _vfVar[_iInsertion])    vPop = Insertion(vPop);
        if (Math.random() < _vfVar[_iDisplacement]) vPop = Displacement(vPop);
        return vPop;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------------
    /** Main functions of evolutionary algorithm */
    
    public void RunEOA() {
        // Run evolutionary algorithm
        int nGenCount = 1;
        ReliabCalc singletonRC = ReliabCalc.getSingletonObject();
        while (nGenCount <= _iGen) {
            for (int iP = 0; iP < _iPop; iP++) {
                this.miPopNew    [iP] = Variate(iP);
                this.miPopNew    [iP] = ValidateData(this.miPopNew[iP]);
                this.vdFitnessNew[iP] = singletonRC.calcEffect(this.miPopNew[iP]);
            }
            Selection();
            statEvo(nGenCount);
            nGenCount++;
        }
    }

    public void Selection() {
        // Temporary matrixes
        double[] vFitnessTmp = new double[_iTourn * 2];
        int[][]      mPopTmp     = new int[_iTourn * 2][_iNrOfMGs];
        // No of tournaments
        int nTournNo = (int) Math.ceil((double) _iPop / _iTourn);
      
        int iTBeg; // index of the beginning of the tournament
        int iTEnd; // index of the end of the tournament
        for (int iT = 0; iT < nTournNo; iT++) {
            iTBeg = iT * _iTourn; // Beginning of the tournament 
            iTEnd = iTBeg + _iTourn; // End of the tournament 
            if (iTEnd > _iPop) // If the end of the tournament is greater than the number of individuals
                iTEnd = _iPop;
            // Initialize vFitnessTmp
            for (int iP = 0; iP < (_iTourn * 2); iP++) {
                vFitnessTmp[iP] = 0;
                for (int iR = 0; iR < _iNrOfMGs; iR++) {
                    mPopTmp[iP][iR] = 0;
                }
            }
            // Copy offspring values
            for (int iI = 0; iI < (iTEnd - iTBeg); iI++) {
                vFitnessTmp[iI] = this.vdFitness[iTBeg + iI];
                vFitnessTmp[(iTEnd - iTBeg) + iI] = this.vdFitnessNew[iTBeg + iI];
                for (int iR = 0; iR < _iNrOfMGs; iR++) {
                    mPopTmp[iI][iR] = this.miPop[iTBeg + iI][iR];
                    mPopTmp[(iTEnd - iTBeg) + iI][iR] = this.miPopNew[iTBeg + iI][iR];
                }
            }
            // Sort the fitness values
            int[] vIndNew = sortInd(vFitnessTmp);
         
            for (int iI = 0; iI < (iTEnd - iTBeg); iI++) {
                // Select the best for the next generation 
                this.vdFitness[iI + iTBeg] = vFitnessTmp[vIndNew[iI]];
                for (int iR = 0; iR < _iNrOfMGs; iR++) {
                    this.miPop[iI + iTBeg][iR] = mPopTmp[vIndNew[iI]][iR];
                }
            }
        }
    } 

    // -------------------------------------------------------------------------------------------------------------------------------------------------------
    public int[] getBestState() {
        int iI = 0;
       
        while (mdStat[_iGen][1] != this.vdFitness[iI] && iI < _iPop) {
            iI++;
        }
        System.out.println(Arrays.toString(this.miPop[iI]));
        return this.miPop[iI];
    }

    // -------------------------------------------------------------------------------------------------------------------------------------------------------
    public ArrayList<int[]> getPopulation() {
        ArrayList<int[]> al = new ArrayList<int[]>();
        for (int i = 0; i < this.miPop.length; i++) {            
            al.add(this.miPop[i]);
        }
        return al;
    }
    
}
