import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

import ee.ioc.cs.vsle.api.Subtask;


public class Optimizer implements Comparator<double[]>{
	/*@ specification Optimizer {
	
	///////////////////////////////////////////////////////////////////////////////
	// goal of this class:											             //
	// * provide the graded security model functions (u,i,c,l)                   //
	// * initialization of the graded security model parameters                  //
	// * initialization of the evolutionary algorithm parameters                 //
	// * launch the optimization                                                 //
	// * collect the input to draw curves later                                  //
	///////////////////////////////////////////////////////////////////////////////
	
    // ********************************* VARIABLES ********************************
       
	// control variables
	void optParamsReady, reliabCalcParamsReady, secClassParamsReady, done;
	
	// ********** Graded Security Model **********
	int        iMinBudget, iMaxBudget, iStep, iNrOfYears, iBudget, iYear;
	int[]      viSitLvls, viReqLvls, viNbrLvls;
	double     dBudget, dMinBudget, dBudgetEvo;
    double[][] mdEffect, mdInvest, mdMaintn, mdLosses;
    
    // **************** Presentation **************
    boolean bCubicSpline, bBezierSpline;
    
    // ****************** Results *****************
	int[]						viOptProtProf, viCurLvls;
	int[][][]					D3iOptProtProf;
	double[][] 					mdBudget, mdOptEffect, mdOptInvest, mdOptLosses, mdOptMitRat, mdOptMaintn;
	String						sBudget,  sEffect,     sInvest,     sLosses,     sMitRat,     sMaintn;
	sBudget="Budget"; sEffect="Effect"; sInvest="Invest"; sLosses="Losses"; sMitRat="MitRat"; sMaintn="Maintn"; 
	
	alias (double[][]) results = (mdBudget, mdOptEffect, mdOptInvest, mdOptLosses, mdOptMitRat, mdOptMaintn);
	
    alias 			   budget  = (sBudget, mdBudget);
    alias 			   effect  = (sEffect, mdOptEffect);
    alias 			   invest  = (sInvest, mdOptInvest);
    alias 			   losses  = (sLosses, mdOptLosses);
    alias              mitrat  = (sMitRat, mdOptMitRat);
    alias 			   maintn  = (sMaintn, mdOptMaintn);
    
    alias portSuper         = (reliabCalcParamsReady, viSitLvls, viNbrLvls, mdEffect, mdInvest, mdMaintn, D3iOptProtProf);
    alias portSecurityClass = (secClassParamsReady, viReqLvls, mdLosses);
    
    // ******************************* DEPENDENCIES *******************************	
	mdEffect, mdInvest, mdMaintn, mdLosses, viSitLvls, viReqLvls, viNbrLvls, iNrOfYears, dBudgetEvo, bCubicSpline, bBezierSpline, iMinBudget, iMaxBudget, iStep -> optParamsReady{initOptParams};
	reliabCalcParamsReady, optParamsReady, iBudget, iYear, viCurLvls                                                            								-> viOptProtProf {runEvo};
	[iBudget, iYear, viCurLvls -> viOptProtProf], reliabCalcParamsReady, secClassParamsReady     							        							-> results, done {optimize};
	done																																						-> D3iOptProtProf{getD3ProtProf};

	// *********************************** GOALS **********************************
	-> done;

	}@*/
	
	/** cache reusable variables */
	// these variables don't change during the entire program
		// Evolutionary Algorithm parameters
		protected static final int                 _iCrossover    = 0;                                     // Offspring generation methods 
		protected static final int                 _iMutate       = 1;
	    protected static final int                 _iSwap         = 2;
	    protected static final int                 _iInversion    = 3;
	    protected static final int                 _iInsertion    = 4;
	    protected static final int                 _iDisplacement = 5; 
	    protected static final float[]             _vfVar         = {0.9f, 0.8f, 0.6f, 0.1f, 0.07f, 0.11f};// Probabilities of each method
	    
	    protected static       int                 _iPop;                                                  // Size of population
	    protected static       int                 _iTourn;                                                // Size of tournament
	    protected static       int                 _iGen;                                                  // Number of generations
	    
	    private   static       boolean             _bEvoIsInit;									           // Are EvoParams Initialized?
	    
	    // Optimization problem parameters
	    protected static       double              _dSitCost;                                              // Budget needed for maintaining current security levels
	    protected static       double              _dReqCost;                                              // Budget needed for implementing required security levels
	    protected static       double	           _dMaxCost;		                                       // Budget needed for implementing maximum security levels
	    
	    protected static       double[][]          _mdEffect;                                              // Matrix of effectiveness levels
	    protected static       double[][]          _mdInvest;                                              // Matrix of investment levels
	    protected static       double[][]          _mdMaintn;                                              // Matrix of maintenance levels
	    protected static       double[][]          _mdConLos;                                              // Matrix of effectiveness/losses values as calculated by SecurityClass.java
	    
	    protected static       int                 _iNrOfMGs;                                              // The number of genes ( = Number of MeasureGroups)
	    protected static       int                 _iNrOfYears;											   // The number of years
	    protected static       double              _dBudgetEvo;											   // Expressed as a percentage (increase/decrease) of the budget
	    
	    private   static       int[]               _viSitLvls;		                                       // Original value of each gene (imposed by initial security situation)
	    protected static       int[]               _viReqLvls;		                                       // Required value of each gene (imposed by SecClass)
	    protected static       int[]               _viMaxLvls;                                             // Maximum  value of each gene (number of security levels per measuregroup)
	    	    
	    private   static       boolean             _bOptIsInit;											   // Are OptParams Initialized?
	    private   static       boolean             _bBezierSpline;										   // Use bezier splines for interpolation of losses-curve
	    private   static       boolean             _bCubicSpline;										   // Use cubic  splines for interpolation of losses-curve
	    
	 // these variables will contain all state parameters
	    // state "=" budget-level
	    private   static       int[][][]           _3DiOptProtProf;
	    protected static       int[][][]           _3DPrvPop;    										   // Previous budget point's population for each year
	    protected static       int[][]			   _miAllZeros;
	    
	    private   static       double[][]          _mdBudget;
	    private   static	   double[][]		   _mdOptEffect;
	    private   static       double[][]          _mdOptInvest;
	    private   static       double[][]          _mdOptMaintn;
	    private   static       double[][]	       _mdOptMitRat;
	    // state "=" spline approximation with effectiveness values as abscissa
	    private   static       double[][]          _mdLosses;											   // Interpolated spline values for losses
	    private   static       double[][]          _mdLossesSpline;										   // Contains spline coefficients
	    
	    private   static       double[][][]        _3DdResults;
  
	/** **********  general purpose methods  ********** */
	/** define a new comparator */
	public int compare(double[] vdCostEffect, double[] vdCC){
		if(vdCostEffect[1] < vdCC[1]){
			return -1;
		}
		else if(vdCostEffect[1] > vdCC[1]){
			return 1;
		}
		else return 0;
	}
	
	/** **********  methods for optimization ********** */
	public void initOptParams(double[][] mdInitEffect, double[][] mdInitInvest, double[][] mdInitMaintn, double[][] mdInitLosses, 
								int[] viInitSitLvls, int[] viInitReqLvls, int[] viInitNbrLvls, 
								int iNrOfYears, double dBudgetEvo, 
								boolean bCubicSpline, boolean bBezierSpline, 
								int iMinBudget, int iMaxBudget, int iStep){
		if(!_bOptIsInit){
			System.out.println("Initializing Optimizer ... ");
			// import expert data
			_mdEffect  = mdInitEffect; _mdInvest  = mdInitInvest; _mdMaintn  = mdInitMaintn; _mdConLos = mdInitLosses;
			// determine number of measuregroups
			_iNrOfMGs  = _mdEffect.length;
			// Set all levels (Sit, Req, Max)
			_viSitLvls = viInitSitLvls; _viReqLvls = viInitReqLvls; 
			_viMaxLvls = new int[_iNrOfMGs];
		        for (int iR = 0; iR < _iNrOfMGs; iR++){
		        	_viMaxLvls[iR] = viInitNbrLvls[iR]-1;
		        } 
			// Information for multi-year approach
	        _iNrOfYears = iNrOfYears;
	        _dBudgetEvo = dBudgetEvo;
			// Add total cost for all levels
	        _dSitCost = calcInvest(_viSitLvls,_viSitLvls) + calcMaintn(_viSitLvls,_viSitLvls);
	        _dReqCost = calcInvest(_viReqLvls,_viSitLvls) + calcMaintn(_viReqLvls,_viSitLvls);
	        _dMaxCost = calcInvest(_viMaxLvls,_viSitLvls) + calcMaintn(_viMaxLvls,_viSitLvls);
	        
			_bBezierSpline = bBezierSpline; _bCubicSpline = bCubicSpline; _bOptIsInit = true;
			
			initArrays(iMinBudget, iMaxBudget, iStep);
			initLossesSpline();
			initEvoParams();
			
			System.out.println("Current   MG Levels: " + Arrays.toString(_viSitLvls));
			System.out.println("Required MG Levels: " + Arrays.toString(_viReqLvls));
			System.out.println("Maximum MG Levels: " + Arrays.toString(_viMaxLvls));
			
			System.out.println("related costs: " + _dSitCost + " " + _dReqCost + " " + _dMaxCost);
			
			System.out.println("... done");
		}
	}
	
	private void initArrays(int iMinBudget, int iMaxBudget, int iStep){
		
		int iMax = ((iMaxBudget - iMinBudget)/iStep);
		if (iMax <= 0) throw new RuntimeException("iMaxBudget < iMinBudget");
		if ((iMaxBudget - iMinBudget)%iStep != 0) iMax++;
		
		_3DiOptProtProf = new int   [iMax+1][_iNrOfYears][_iNrOfMGs];
		
		_mdBudget       = new double	[iMax+1][_iNrOfYears];
		_mdOptEffect    = new double    [iMax+1][_iNrOfYears];
		_mdOptInvest    = new double	[iMax+1][_iNrOfYears];
		_mdLosses       = new double	[iMax+1][_iNrOfYears];
		_mdOptMitRat    = new double	[iMax+1][_iNrOfYears];
		_mdOptMaintn    = new double	[iMax+1][_iNrOfYears];
		
		_3DdResults     = new double[6][iMax+1][_iNrOfYears];
		
		for (int iR = 0; iR < iMax; iR++ ){
			for (int iS = 0; iS < _iNrOfYears; iS++){
				_mdBudget[iR][iS] = (iMinBudget + iR*iStep)*java.lang.Math.pow((1+_dBudgetEvo),iS);
			}
		}
		
		for (int iS = 0; iS < _iNrOfYears; iS++){
			_mdBudget[iMax][iS] = iMaxBudget*java.lang.Math.pow((1+_dBudgetEvo),iS);
		}
	}
	
	private void initLossesSpline(){
		double[] vdX = new double[_mdConLos.length];
		double[] vdY = new double[_mdConLos.length];
		
		for (int i=0;i<_mdConLos.length;i++){
			vdX[i] = _mdConLos[i][0];
			vdY[i] = _mdConLos[i][1];
		}
		InterPolation singletonIP = InterPolation.getSingletonObject();
		_mdLossesSpline = singletonIP.calcSpline(_bBezierSpline, _bCubicSpline, vdX, vdY);
	}
	
	private void initEvoParams(){
		if(_bOptIsInit && !_bEvoIsInit){
			int iNrOfLvls = 0, iInitTourn = 0;
			System.out.println(Arrays.toString(_viMaxLvls));
			for (int iI=0; iI<_iNrOfMGs; iI++){
				iNrOfLvls += (_viMaxLvls[iI]+1);
			}
			iInitTourn = (iNrOfLvls>100)?50:iNrOfLvls/2; // remember for eventual later changes: if(_iTourn == 1) _iTourn = _iPop;
			
			_iPop   = iNrOfLvls;
			_iTourn = iInitTourn;
			_iGen   = iNrOfLvls;
			
			// initialize 3DPrvPop with zeros
			_3DPrvPop   = new int[_iNrOfYears][_iPop][_iNrOfMGs];
			_miAllZeros = new int[_iPop][_iNrOfMGs];
			for (int iI=0; iI<_iPop; iI++){
				Arrays.fill(_miAllZeros[iI], 0);
			}
			for (int iI=0; iI<_iNrOfYears; iI++){
				_3DPrvPop[iI] = _miAllZeros.clone();
			}
			
			_bEvoIsInit = true;
		}
	}
	
	public int[] runEvo(int iBudget, int iYear, int[] viCurLvls){
		
		double dBudget           = _mdBudget[iBudget][iYear];
		double dMinBudget        = (_dBudgetEvo<0)?_mdBudget[iBudget][_iNrOfYears-1]*(1+_dBudgetEvo):_mdBudget[iBudget][iYear]*(1+_dBudgetEvo);
		
		double dCurCost          = CostFn( viCurLvls, viCurLvls);
		double dMaxCostI         = CostFn(_viMaxLvls, viCurLvls);
		double dMaxCostU         = CostFn(_viMaxLvls,_viMaxLvls);
		
		int[]  viOptProtProf     = new int[_iNrOfMGs];
			
		if (dBudget < dCurCost){
			System.out.println("Budget (" + dBudget + ") < dCurCost: " + dCurCost + "  ==> error ");
			Arrays.fill(viOptProtProf, -1);
			_3DPrvPop[iYear] = _miAllZeros.clone();
		}
		else if (dBudget >= dMaxCostI && dMinBudget >= dMaxCostU){
			System.out.println("Budget (" + dBudget + ") >= dMaxCost: " + _dMaxCost + " ==> all genes set at maximum level");
			System.arraycopy(_viMaxLvls, 0, viOptProtProf, 0, _iNrOfMGs);
			_3DPrvPop[iYear] = _miAllZeros.clone();
		}
		else{
			EvoOptAlgo singletonEOA = EvoOptAlgo.getSingletonObject();
			singletonEOA.initSingletonEOA(dBudget, dMinBudget, iYear, viCurLvls);
			singletonEOA.RunEOA();			
			System.arraycopy(singletonEOA.getBestState(), 0, viOptProtProf, 0, _iNrOfMGs);
			_3DPrvPop[iYear] = (int[][])singletonEOA.getPopulation().toArray(new int[_iPop][_iNrOfMGs]);
		}
		return viOptProtProf;
	}
	
	public double[][][] optimize (Subtask stRunEvo){	
		
		int[] viCurLvls = new int[_iNrOfMGs];
		
		int iMax = _mdBudget.length-1;
		
		for (int iR = 0; iR < iMax; iR++ ){
			System.arraycopy(_viSitLvls, 0, viCurLvls, 0, _iNrOfMGs);
			
			for (int iS = 0; iS < _iNrOfYears; iS++){
				Object[]   stRunEvoIn = new Object[3];
				Object[]   stRunEvoOut;
				
				stRunEvoIn[0] = iR; stRunEvoIn[1] = iS; stRunEvoIn[2] = viCurLvls;
				stRunEvoOut = stRunEvo.run(stRunEvoIn);
				
				_3DiOptProtProf[iR][iS] = (int[])(stRunEvoOut[0]);
				_mdOptEffect   [iR][iS] = calcEffect(_3DiOptProtProf[iR][iS]);
				_mdOptInvest   [iR][iS] = calcInvest(_3DiOptProtProf[iR][iS],viCurLvls);
				_mdLosses      [iR][iS] = calcLosses((_mdOptEffect[iR][iS]));
				_mdOptMitRat   [iR][iS] = 1/(1-_mdOptEffect[iR][iS]);
				_mdOptMaintn   [iR][iS] = calcMaintn(_3DiOptProtProf[iR][iS],viCurLvls);
				System.arraycopy(_3DiOptProtProf[iR][iS], 0, viCurLvls, 0, _iNrOfMGs);
				System.out.println(_mdOptEffect[iR][iS]);
			}
		}
		// The last point is always iMaxBudget, even if (iMaxBudget-iMinBudget) is no multiple of iStep
		System.arraycopy(_viSitLvls, 0, viCurLvls, 0, _iNrOfMGs);
		for (int iS = 0; iS < _iNrOfYears; iS++){
			Object[]   stRunEvoIn = new Object[3];
			Object[]   stRunEvoOut;
			
			stRunEvoIn[0] = iMax; stRunEvoIn[1] = iS; stRunEvoIn[2] = viCurLvls;
			stRunEvoOut = stRunEvo.run(stRunEvoIn);
			
			_3DiOptProtProf[iMax][iS] = (int[])(stRunEvoOut[0]);
			_mdOptEffect   [iMax][iS] = calcEffect(_3DiOptProtProf[iMax][iS]);
			_mdOptInvest   [iMax][iS] = calcInvest(_3DiOptProtProf[iMax][iS],viCurLvls);
			_mdLosses      [iMax][iS] = calcLosses(_mdOptEffect[iMax][iS]);
			_mdOptMitRat   [iMax][iS] = 1/(1-_mdOptEffect[iMax][iS]);;
			_mdOptMaintn   [iMax][iS] = calcMaintn(_3DiOptProtProf[iMax][iS],viCurLvls);
			System.arraycopy(_3DiOptProtProf[iMax][iS], 0, viCurLvls, 0, _iNrOfMGs);
		}
		
		_3DdResults[0] = _mdBudget;
		_3DdResults[1] = _mdOptEffect;
		_3DdResults[2] = _mdOptInvest;
		_3DdResults[3] = _mdLosses;
		_3DdResults[4] = _mdOptMitRat;
		_3DdResults[5] = _mdOptMaintn;
		
		consoleOutputByBudget(iMax);
		// consoleOutputByYear(iMax);
		fileOutput(iMax);
		
		return _3DdResults;
	}
	
	public int[][][] getD3ProtProf(){
		return _3DiOptProtProf;
	}
	
	private void fileOutput(int iMax){
		// create CSV file
		try{
			File file = new File("combinations.txt");
			if (!file.exists()) file.createNewFile();
			
			BufferedWriter bwComb = new BufferedWriter (new FileWriter (file));
			int[]  viCurProtprof = new int[_iNrOfMGs];
			
			for (int iR = 0; iR < iMax; iR++ ){
				bwComb.write("Initial Budget, " + Double.toString(_mdBudget[iR][0]) + "\n");
				bwComb.write("Yearly Budget Evolution, " + _dBudgetEvo + "\n");
				for (int i=0;i<(_iNrOfMGs+2);i++) bwComb.write(",");
				bwComb.write("budget, Year, Effect, MitRat, Maintn, Invest, Losses \n");
				System.arraycopy(_viSitLvls, 0, viCurProtprof, 0, _iNrOfMGs);
				for (int iS = 0; iS < _iNrOfYears; iS++){
					int []     viOptProtProf = _3DiOptProtProf[iR][iS];
					double dEffect       = calcEffect(viOptProtProf);
					String     sOptProtProf  = Arrays.toString(viOptProtProf);
					
					bwComb.write( "," + "," + sOptProtProf.substring(1, sOptProtProf.length()-1)
								      + "," + _mdBudget[iR][iS]
								      + "," + iS
								      + "," + dEffect
								      + "," + 1/(1-dEffect)
								      + "," + calcMaintn(viOptProtProf, viCurProtprof)
								      + "," + calcInvest(viOptProtProf, viCurProtprof)
								      + "," + calcLosses(dEffect));
					bwComb.write("\n");
					System.arraycopy(_3DiOptProtProf[iR][iS], 0, viCurProtprof, 0, _iNrOfMGs);
				}
			}
			bwComb.close();
		}
		catch (IOException IOE){ System.err.println(IOE.getMessage()); };
	}
	
	private void consoleOutputByBudget(int iMax){
		for (int iR = 0; iR <= iMax; iR++ ){
			for (int iS = 0; iS < _iNrOfYears; iS++){
				System.out.print(_3DdResults[0][iR][iS] + " ");
				System.out.print(_3DdResults[1][iR][iS] + " ");
				System.out.println(_3DdResults[4][iR][iS]);
			}
			
//			System.out.println("Budget: " + _3DdResults[0][iR][0]);
//			System.out.print("Effect: [ ");
//			for (int iS = 0; iS < _iNrOfYears; iS++){
//				System.out.print(_3DdResults[1][iR][iS] + " ");
//			}
//			System.out.println("]");
//			System.out.print("Invest: [ ");
//			for (int iS = 0; iS < _iNrOfYears; iS++){
//				System.out.print(_3DdResults[2][iR][iS] + " ");
//			}
//			System.out.println("]");
//			System.out.print("Losses: [ ");
//			for (int iS = 0; iS < _iNrOfYears; iS++){
//				System.out.print(_3DdResults[3][iR][iS] + " ");
//			}
//			System.out.println("]");
//			System.out.print("MitRat: [ ");
//			for (int iS = 0; iS < _iNrOfYears; iS++){
//				System.out.print(_3DdResults[4][iR][iS] + " ");
//			}
//			System.out.println("]");
//			System.out.print("Maintn: [ ");
//			for (int iS = 0; iS < _iNrOfYears; iS++){
//				System.out.print(_3DdResults[5][iR][iS] + " ");
//			}
//			System.out.println("]");
		}
	}
	
	private void consoleOutputByYear(int iMax){
		for (int iS = 0; iS < _iNrOfYears; iS++ ){
			int iYear = iS+1;
			System.out.println("Year: " + iYear );
			System.out.print("Effect: [ ");
			for (int iR = 0; iR <= iMax; iR++){
				System.out.print(_3DdResults[1][iR][iS] + " ");
			}
			System.out.println("]");
			System.out.print("Invest: [ ");
			for (int iR = 0; iR <= iMax; iR++){
				System.out.print(_3DdResults[2][iR][iS] + " ");
			}
			System.out.println("]");
			System.out.print("Losses: [ ");
			for (int iR = 0; iR <= iMax; iR++){
				System.out.print(_3DdResults[3][iR][iS] + " ");
			}
			System.out.println("]");
			System.out.print("MitRat: [ ");
			for (int iR = 0; iR <= iMax; iR++){
				System.out.print(_3DdResults[4][iR][iS] + " ");
			}
			System.out.println("]");
			System.out.print("Maintn: [ ");
			for (int iR = 0; iR <= iMax; iR++){
				System.out.print(_3DdResults[5][iR][iS] + " ");
			}
			System.out.println("]");
			System.out.println("Protection profiles: ");
			for (int iR = 0; iR <= iMax; iR++){
				System.out.print("Budget: " + _3DdResults[0][iR][iS] + " ");
				System.out.println(Arrays.toString(_3DiOptProtProf[iR][iS]));
			}
		}
	}	
	
	/** **********  methods for graded security model  ********** */
	/** u(l,g) -> calculates maintenance cost for protection profile */
	// viCurLvls represents the evolution of _viSitLvls throughout the years in the multiyear approach
	private double calcMaintn(int[] viProtProf, int[] viCurLvls){		
		double dTotalMaintnCost = 0;
		if (_mdMaintn.length != viProtProf.length) throw new RuntimeException("Protection profile length and size of maintenance cost list don�t match");
		for (int i=0;i<_iNrOfMGs;i++){
			if (viProtProf[i] >= _mdMaintn[i].length) throw new RuntimeException("In mdMaintn: Protection profile security level for group " + i + " exceeds maximum group security level");
			if (viProtProf[i] <= viCurLvls[i]) dTotalMaintnCost += _mdMaintn[i][viCurLvls[i]];
		}
		return dTotalMaintnCost;
	}
	/** i(l,g) -> calculates investment cost for protection profile */
	// viCurLvls represents the evolution of _viSitLvls throughout the years in the multiyear approach
	private double calcInvest(int[] viProtProf, int[] viCurLvls){
		double dTotalInvestCost = 0;
		if (_mdInvest.length != viProtProf.length) throw new RuntimeException("Protection profile length and size of investment cost list don�t match");
		for (int i=0;i<_iNrOfMGs;i++){
			if (viProtProf[i] >= _mdInvest[i].length) throw new RuntimeException("In mdInvest: Protection profile security level for group " + i + " exceeds maximum group security level");
			if (viProtProf[i] >  viCurLvls[i]){
				dTotalInvestCost += _mdInvest[i][viProtProf[i]];
				dTotalInvestCost -= _mdInvest[i][viCurLvls [i]];
			}
		}
		return dTotalInvestCost;
	}
	/** c(l,g) -> calculates effectiveness level for protection profile */
	public static double calcEffect(int[] viProtProf){
		ReliabCalc singletonRC = ReliabCalc.getSingletonObject();
		double dTotalEffect = singletonRC.calcEffect(viProtProf);
		return dTotalEffect;
	}
	/** l(l,g) -> calculates new losses through extrapolation */
	private double calcLosses(double dEffect){
		double dLosses = 0;
		InterPolation singletonIP = InterPolation.getSingletonObject();
		dLosses = singletonIP.interpolate(dEffect, _bBezierSpline, _bCubicSpline, _mdLossesSpline);
		return dLosses;
	}
	/** function to define the total cost of a protection profile (maintenance + invest) */
	// this function is used for the evolutionary algorithm
	protected double CostFn(int[] viProtProf, int[] viCurLvls){
		double nCost;
		nCost = calcInvest(viProtProf, viCurLvls) + calcMaintn(viProtProf, viCurLvls);
		return nCost;
	}
	
}

