/*      */ import java.awt.Color;
/*      */ import java.awt.Font;
/*      */ import java.awt.FontMetrics;
/*      */ import java.awt.Graphics;
/*      */ import java.awt.print.PageFormat;
/*      */ import java.awt.print.Printable;
/*      */ import java.awt.print.PrinterException;
/*      */ import java.io.FileOutputStream;
/*      */ import java.io.PrintStream;
/*      */ import java.io.PrintWriter;
/*      */ import java.io.Serializable;
/*      */ import java.text.DecimalFormat;
/*      */ import java.util.Vector;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public abstract class ORSolverBase
/*      */   implements Serializable, Printable
/*      */ {
/*      */   protected int ApplicationNumber;
/*      */   protected int CommandNumber;
/*      */   protected String errInfo;
/*      */   
/*      */   public abstract boolean doWork(IOROperation paramIOROperation);
/*      */   
/*      */   public void reset() {}
/*      */   
/*      */   public void setBookmark() {}
/*      */   
/*      */   public String getErrInfo()
/*      */   {
/*   53 */     return this.errInfo;
/*      */   }
/*      */   
/*      */   protected void arraycopy1(double[] src, double[] des) {
/*   57 */     System.arraycopy(src, 0, des, 0, des.length);
/*      */   }
/*      */   
/*      */   protected void arraycopy1(int[] src, int[] des) {
/*   61 */     System.arraycopy(src, 0, des, 0, des.length);
/*      */   }
/*      */   
/*      */   protected void arraycopy1(boolean[] src, boolean[] des) {
/*   65 */     System.arraycopy(src, 0, des, 0, des.length);
/*      */   }
/*      */   
/*      */   protected void arraycopy2(double[][] src, double[][] des)
/*      */   {
/*   70 */     for (int i = 0; i < des.length; i++) {
/*   71 */       System.arraycopy(src[i], 0, des[i], 0, des[i].length);
/*      */     }
/*      */   }
/*      */   
/*      */   protected void arraycopy2(int[][] src, int[][] des) {
/*   76 */     for (int i = 0; i < des.length; i++) {
/*   77 */       System.arraycopy(src[i], 0, des[i], 0, des[i].length);
/*      */     }
/*      */   }
/*      */   
/*      */   protected void arraycopy2(boolean[][] src, boolean[][] des) {
/*   82 */     for (int i = 0; i < des.length; i++)
/*   83 */       System.arraycopy(src[i], 0, des[i], 0, des[i].length);
/*      */   }
/*      */   
/*      */   protected double RealBound(double a, double lb, double ub) {
/*   87 */     return Math.max(Math.min(a, ub), lb);
/*      */   }
/*      */   
/*      */ 
/*      */   protected boolean SolveSystem(double[][] A, double[] b, int n, double[] Solution)
/*      */   {
/*   93 */     int[] c = new int[11];
/*      */     
/*   95 */     double[][] CopyA = new double[11][11];
/*   96 */     double[] Copyb = new double[11];
/*      */     
/*      */ 
/*   99 */     arraycopy2(A, CopyA);
/*  100 */     arraycopy1(b, Copyb);
/*      */     
/*  102 */     double d = Decomp(n, CopyA, c);
/*  103 */     boolean Singular; boolean Singular; if (Math.abs(d) < 1.0E-5D) {
/*  104 */       Singular = true;
/*      */     } else {
/*  106 */       Solve(n, CopyA, c, Copyb);
/*  107 */       for (int i = 1; i <= n; i++)
/*  108 */         Solution[i] = Copyb[i];
/*  109 */       Singular = false;
/*      */     }
/*  111 */     return Singular;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */   private double Decomp(int n, double[][] A, int[] c)
/*      */   {
/*  119 */     double d = 1.0D;
/*  120 */     c[n] = 1;
/*  121 */     for (int k = 1; k <= n - 1; k++) {
/*  122 */       int m = k;
/*  123 */       for (int i = k + 1; i <= n; i++) {
/*  124 */         if (Math.abs(A[i][k]) > Math.abs(A[m][k]))
/*  125 */           m = i;
/*      */       }
/*  127 */       c[k] = m;
/*  128 */       if (m != k)
/*  129 */         c[n] = (-c[n]);
/*  130 */       double p = A[m][k];
/*  131 */       A[m][k] = A[k][k];
/*  132 */       A[k][k] = p;
/*  133 */       d *= p;
/*  134 */       if (p != 0) {
/*  135 */         for (i = k + 1; i <= n; i++)
/*  136 */           A[i][k] = (-A[i][k] / p);
/*  137 */         for (int j = k + 1; j <= n; j++) {
/*  138 */           double t = A[m][j];
/*  139 */           A[m][j] = A[k][j];
/*  140 */           A[k][j] = t;
/*  141 */           if (t != 0) {
/*  142 */             for (i = k + 1; i <= n; i++)
/*  143 */               A[i][j] += A[i][k] * t;
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*  148 */     d = d * A[n][n] * c[n];
/*      */     
/*  150 */     return d;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */   private void Solve(int n, double[][] A, int[] c, double[] b)
/*      */   {
/*  157 */     if (n == 1) {
/*  158 */       b[1] /= A[1][1];
/*      */     } else {
/*  160 */       for (int k = 1; k <= n - 1; k++) {
/*  161 */         int m = c[k];
/*  162 */         double s = b[m];
/*  163 */         b[m] = b[k];
/*  164 */         b[k] = s;
/*  165 */         for (int i = k + 1; i <= n; i++)
/*  166 */           b[i] += A[i][k] * s;
/*      */       }
/*  168 */       for (int j = 1; j <= n - 1; j++) {
/*  169 */         k = n - j + 1;
/*  170 */         b[k] /= A[k][k];
/*  171 */         double s = -b[k];
/*  172 */         for (int i = 1; i <= n - j; i++)
/*  173 */           b[i] += A[i][k] * s;
/*      */       }
/*  175 */       b[1] /= A[1][1];
/*      */     }
/*      */   }
/*      */   
/*      */   protected double exp1(double x) { double t;
/*      */     double t;
/*  181 */     if (x < -60) {
/*  182 */       t = 0.0D; } else { double t;
/*  183 */       if (x > 60) {
/*  184 */         t = 1.0E10D;
/*      */       } else
/*  186 */         t = Math.exp(x); }
/*  187 */     return t;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */   protected void MultiplyMatrices(double[][] LeftMatrix, double[][] RightMatrix, int LeftNR, int LeftNC, int RightNC, double[][] ResultingMatrix)
/*      */   {
/*  195 */     double[][] tempLeftMatrix = new double[11][11];
/*      */     
/*  197 */     double[][] tempRightMatrix = new double[11][11];
/*      */     
/*      */ 
/*  200 */     arraycopy2(LeftMatrix, tempLeftMatrix);
/*  201 */     arraycopy2(RightMatrix, tempRightMatrix);
/*      */     
/*  203 */     for (int Row = 1; Row <= LeftNR; Row++) {
/*  204 */       for (int Column = 1; Column <= RightNC; Column++) {
/*  205 */         ResultingMatrix[Row][Column] = 0.0D;
/*  206 */         for (int ColRow = 1; ColRow <= LeftNC; ColRow++) {
/*  207 */           ResultingMatrix[Row][Column] += tempLeftMatrix[Row][ColRow] * tempRightMatrix[ColRow][Column];
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*  215 */   protected static final int MaxConstraints = 10;
/*  216 */   protected static final int MaxVariables = 10;
/*  217 */   protected static final int SimplexAutoMaxConstraints = 50;
/*  218 */   protected static final int SimplexModelMaxVariables = 50;
/*  219 */   protected static final int SimplexAutoMaxVariables = 150;
/*  220 */   protected static final int MaxIntegerVariables = 30;
/*  221 */   protected static final int MaxIPIterations = 15;
/*  222 */   protected static final int MaxBasicVariables = 30;
/*  223 */   protected static final int MaxSources = 15;
/*  224 */   protected static final int MaxDestinations = 15;
/*  225 */   protected static final int MaxNodes = 16;
/*  226 */   protected static final int MaxInteractiveNodes = 11;
/*  227 */   protected static final int MaxTerms = 10;
/*  228 */   protected static final int MaxMSMVariables = 10;
/*      */   
/*  230 */   protected static final int SimplexApplication = 1;
/*  231 */   protected static final int SimplexModifyCommand = 1;
/*  232 */   protected static final int SimplexSolveCommand = 2;
/*  233 */   protected static final int SimplexSolveIPCommand = 3;
/*  234 */   protected static final int SimplexSolveGraphicallyCommand = 4;
/*  235 */   protected static final int SimplexConvertToEqualityFormCommand = 5;
/*  236 */   protected static final int SimplexSolveInteractivelyCommand = 6;
/*  237 */   protected static final int SimplexSensitivityAnalysisCommand = 7;
/*  238 */   protected static final int SimplexModifiedSimplexCommand = 8;
/*  239 */   protected static final int NumSimplexProcedures = 8;
/*  240 */   protected static final int TransportationApplication = 2;
/*  241 */   protected static final int TransportationModifyCommand = 1;
/*  242 */   protected static final int TransportationSolveCommand = 2;
/*  243 */   protected static final int TransportationFindIBFSCommand = 3;
/*  244 */   protected static final int TransportationSolveInteractivelyCommand = 4;
/*  245 */   protected static final int TransportationAssignmentModifyCommand = 5;
/*  246 */   protected static final int TransportationAssignmentSolveCommand = 6;
/*  247 */   protected static final int NumTransportationProcedures = 6;
/*  248 */   protected static final int NetworkApplication = 3;
/*  249 */   protected static final int NetworkSolveAutomaticallyCommand = 1;
/*  250 */   protected static final int NetworkSolveInteractivelyCommand = 2;
/*  251 */   protected static final int NetworkCPMCommand = 3;
/*  252 */   protected static final int NumNetworkProcedures = 3;
/*  253 */   protected static final int DynamicApplication = 4;
/*  254 */   protected static final int DynamicSolveInteractivelyCommand = 1;
/*  255 */   protected static final int NumDynamicProcedures = 1;
/*  256 */   protected static final int IntegerApplication = 5;
/*  257 */   protected static final int IntegerEnterModelCommand = 1;
/*  258 */   protected static final int IntegerSolveBIPAutomaticallyCommand = 2;
/*  259 */   protected static final int IntegerSolveBIPInteractivelyCommand = 3;
/*  260 */   protected static final int IntegerSolveMIPAutomaticallyCommand = 4;
/*  261 */   protected static final int IntegerSolveMIPInteractivelyCommand = 5;
/*  262 */   protected static final int NumIntegerProcedures = 5;
/*  263 */   protected static final int NonlinearApplication = 6;
/*  264 */   protected static final int NonlinearSolveOneDAutomaticallyCommand = 1;
/*  265 */   protected static final int NonlinearSolveOneDInteractivelyCommand = 2;
/*  266 */   protected static final int NonlinearSolveGradientAutomaticallyCommand = 3;
/*  267 */   protected static final int NonlinearSolveGradientInteractivelyCommand = 4;
/*  268 */   protected static final int NonlinearEnterModifiedSimplexCommand = 5;
/*      */   
/*  270 */   protected static final int NonlinearSolveModifiedSimplexAutomaticallyCommand = 6;
/*  271 */   protected static final int NonlinearModifiedSimplexCommand = 7;
/*  272 */   protected static final int NonlinearModifiedSimplexMenuCommand = 6;
/*      */   
/*  274 */   protected static final int NonlinearEnterFrankWolfeCommand = 8;
/*  275 */   protected static final int NonlinearFrankWolfeMenuCommand = 7;
/*  276 */   protected static final int NonlinearSolveFrankWolfeInteractivelyCommand = 9;
/*      */   
/*      */ 
/*      */ 
/*  280 */   protected static final int NonlinearSUMTCommand = 10;
/*  281 */   protected static final int NonlinearSUMTMenuCommand = 8;
/*  282 */   protected static final int NumNonlinearProcedures = 8;
/*      */   
/*  284 */   protected static final int MaxJacksonSize = 6;
/*  285 */   protected static final int MaxPQSize = 8;
/*  286 */   protected static final int MarkovMaxStates = 4;
/*  287 */   protected static final int MarkovMaxDecisions = 5;
/*      */   
/*  289 */   protected static final int Exponential = 0;
/*  290 */   protected static final int Uniform = 1;
/*      */   
/*  292 */   protected static final int SinglePeriodNoSetupCommand = 1;
/*  293 */   protected static final int SinglePeriodWithSetupCommand = 2;
/*  294 */   protected static final int TwoPeriodNoSetupCommand = 3;
/*  295 */   protected static final int InfinitePeriodNoSetupCommand = 4;
/*  296 */   protected static final int ContinuousReviewFixedLagCommand = 5;
/*      */   
/*  298 */   protected static final int SimulationMaxServersInteractive = 2;
/*  299 */   protected static final int SimulationMaxServersAutomatic = 5;
/*  300 */   protected static final int SimulationMaxPriorityInteractive = 2;
/*  301 */   protected static final int SimulationMaxPriorityAutomatic = 3;
/*  302 */   protected static final int SimulationMaxInLine = 199;
/*      */   
/*  304 */   protected static final int Constant = 1;
/*  305 */   protected static final int Erlang = 2;
/*  306 */   protected static final int Expon = 3;
/*  307 */   protected static final int TranslatedExponential = 4;
/*  308 */   protected static final int Unif = 5;
/*      */   
/*      */ 
/*  311 */   protected static final int MaxZ = 0;
/*  312 */   protected static final int MinZ = 1;
/*      */   
/*  314 */   protected static final int LessT = 0;
/*  315 */   protected static final int EqualT = 1;
/*  316 */   protected static final int GreaterT = 2;
/*      */   
/*  318 */   protected static final int NorthwestCorner = 0;
/*  319 */   protected static final int Vogels = 1;
/*  320 */   protected static final int Russels = 2;
/*      */   protected Vector steps;
/*      */   
/*      */   protected class SimplexModelType
/*      */     implements Cloneable, Serializable
/*      */   {
/*      */     int NumVariables;
/*      */     int NumConstraints;
/*      */     int Objective;
/*      */     int[] Annealling;
/*      */     float[] initSolution;
/*  331 */     double[] ObjectiveFunction = new double[51];
/*  332 */     double[] ObjectiveFunctionM = new double[51];
/*  333 */     double[][] A = new double[51][51];
/*      */     
/*  335 */     double[] RightHandSide = new double[51];
/*  336 */     int[] Constraint = new int[51];
/*  337 */     boolean[] Nonnegativity = new boolean[''];
/*  338 */     double[] LowerBound = new double[51];
/*  339 */     double[] UpperBound = new double[51];
/*  340 */     boolean[] AnUpperBound = new boolean[51];
/*      */     
/*      */     protected SimplexModelType() {}
/*      */     
/*  344 */     public Object clone() { SimplexModelType localSimplexModelType1; try { SimplexModelType s = (SimplexModelType)super.clone();
/*      */         
/*  346 */         s.ObjectiveFunction = new double[51];
/*  347 */         s.ObjectiveFunctionM = new double[51];
/*  348 */         s.A = new double[51][51];
/*      */         
/*  350 */         s.RightHandSide = new double[51];
/*  351 */         s.Constraint = new int[51];
/*  352 */         s.Nonnegativity = new boolean[''];
/*  353 */         s.LowerBound = new double[51];
/*  354 */         s.UpperBound = new double[51];
/*  355 */         s.AnUpperBound = new boolean[51];
/*      */         
/*  357 */         ORSolverBase.this.arraycopy1(this.ObjectiveFunction, s.ObjectiveFunction);
/*  358 */         ORSolverBase.this.arraycopy1(this.ObjectiveFunctionM, s.ObjectiveFunctionM);
/*  359 */         ORSolverBase.this.arraycopy2(this.A, s.A);
/*  360 */         ORSolverBase.this.arraycopy1(this.RightHandSide, s.RightHandSide);
/*  361 */         ORSolverBase.this.arraycopy1(this.Constraint, s.Constraint);
/*  362 */         ORSolverBase.this.arraycopy1(this.Nonnegativity, s.Nonnegativity);
/*  363 */         ORSolverBase.this.arraycopy1(this.LowerBound, s.LowerBound);
/*  364 */         ORSolverBase.this.arraycopy1(this.UpperBound, s.UpperBound);
/*  365 */         ORSolverBase.this.arraycopy1(this.AnUpperBound, s.AnUpperBound);
/*  366 */         return s;
/*      */       }
/*      */       catch (CloneNotSupportedException e) {
/*  369 */         localSimplexModelType1 = null; } return localSimplexModelType1;
/*      */     }
/*      */   }
/*      */   
/*      */   protected class SimplexTableauType
/*      */     implements Cloneable, Serializable
/*      */   {
/*  376 */     double[] Multiple = new double[51];
/*      */     double MultipleM;
/*      */     int EnteringBasicVariable;
/*      */     int LeavingBasicVariableEquation;
/*      */     int Objective;
/*  381 */     double[] ObjectiveFunction = new double[''];
/*  382 */     double[] ObjectiveFunctionM = new double[''];
/*  383 */     boolean[] AnArtificial = new boolean[''];
/*      */     double Z;
/*      */     double ZM;
/*  386 */     double[][] T = new double[51][''];
/*      */     
/*  388 */     double[] RightHandSide = new double[51];
/*  389 */     double[] BasicVariable = new double[51];
/*  390 */     boolean[] Nonnegativity = new boolean[''];
/*  391 */     double[] LowerBound = new double[''];
/*  392 */     double[] UpperBound = new double[''];
/*  393 */     boolean[] AnUpperBound = new boolean[''];
/*  394 */     boolean[] Reversed = new boolean[''];
/*      */     int NumConstraints;
/*      */     int NumVariables;
/*      */     boolean ContainsArtificials;
/*      */     boolean ContainsConstant;
/*      */     double Constant;
/*      */     int VariableWithConstant;
/*  401 */     int[] Constraint = new int[51];
/*  402 */     int[] VariableLocation = new int[''];
/*      */     boolean Optimal;
/*      */     boolean Unbounded;
/*      */     boolean Feasible;
/*  406 */     boolean[] ABasicVariable = new boolean[''];
/*      */     int NumBasicArtificials;
/*      */     
/*      */     protected SimplexTableauType() {}
/*      */     
/*  411 */     public Object clone() { SimplexTableauType localSimplexTableauType1; try { SimplexTableauType s = (SimplexTableauType)super.clone();
/*      */         
/*  413 */         s.Multiple = new double[51];
/*  414 */         s.ObjectiveFunction = new double[''];
/*  415 */         s.ObjectiveFunctionM = new double[''];
/*  416 */         s.AnArtificial = new boolean[''];
/*  417 */         s.T = new double[51][''];
/*      */         
/*  419 */         s.RightHandSide = new double[51];
/*  420 */         s.BasicVariable = new double[51];
/*  421 */         s.Nonnegativity = new boolean[''];
/*  422 */         s.LowerBound = new double[''];
/*  423 */         s.UpperBound = new double[''];
/*  424 */         s.AnUpperBound = new boolean[''];
/*  425 */         s.Reversed = new boolean[''];
/*  426 */         s.Constraint = new int[51];
/*  427 */         s.VariableLocation = new int[''];
/*  428 */         s.ABasicVariable = new boolean[''];
/*      */         
/*  430 */         ORSolverBase.this.arraycopy1(this.Multiple, s.Multiple);
/*  431 */         ORSolverBase.this.arraycopy1(this.ObjectiveFunction, s.ObjectiveFunction);
/*  432 */         ORSolverBase.this.arraycopy1(this.ObjectiveFunctionM, s.ObjectiveFunctionM);
/*  433 */         ORSolverBase.this.arraycopy1(this.AnArtificial, s.AnArtificial);
/*  434 */         ORSolverBase.this.arraycopy2(this.T, s.T);
/*  435 */         ORSolverBase.this.arraycopy1(this.RightHandSide, s.RightHandSide);
/*  436 */         ORSolverBase.this.arraycopy1(this.BasicVariable, s.BasicVariable);
/*  437 */         ORSolverBase.this.arraycopy1(this.Nonnegativity, s.Nonnegativity);
/*  438 */         ORSolverBase.this.arraycopy1(this.LowerBound, s.LowerBound);
/*  439 */         ORSolverBase.this.arraycopy1(this.UpperBound, s.UpperBound);
/*  440 */         ORSolverBase.this.arraycopy1(this.AnUpperBound, s.AnUpperBound);
/*  441 */         ORSolverBase.this.arraycopy1(this.Reversed, s.Reversed);
/*  442 */         ORSolverBase.this.arraycopy1(this.Constraint, s.Constraint);
/*  443 */         ORSolverBase.this.arraycopy1(this.VariableLocation, s.VariableLocation);
/*  444 */         ORSolverBase.this.arraycopy1(this.ABasicVariable, s.ABasicVariable);
/*  445 */         return s;
/*      */       }
/*      */       catch (CloneNotSupportedException e) {
/*  448 */         localSimplexTableauType1 = null; } return localSimplexTableauType1;
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */   protected class LPSolutionType
/*      */     implements Serializable
/*      */   {
/*  456 */     double[] Solution = new double[51];
/*  457 */     double[] MinObjective = new double[51];
/*  458 */     double[] MaxObjective = new double[51];
/*  459 */     double[] ShadowPrice = new double[51];
/*  460 */     double[] Slack = new double[51];
/*  461 */     double[] MinRHS = new double[51];
/*  462 */     double[] MaxRHS = new double[51];
/*      */     
/*      */     protected LPSolutionType() {}
/*      */   }
/*      */   
/*      */   protected class DakinsType implements Cloneable, Serializable {
/*      */     int IncumbentNumber;
/*  469 */     double[] Solution = new double[31];
/*      */     double Z;
/*      */     int Variable;
/*      */     double LessThanWhat;
/*      */     double GreaterThanWhat;
/*      */     boolean Feasible;
/*      */     boolean Fathomed;
/*      */     int WhereX;
/*      */     int WhereY;
/*      */     DakinsType Parent;
/*      */     DakinsType RightChild;
/*      */     DakinsType LeftChild;
/*      */     
/*      */     protected DakinsType() {}
/*      */     
/*  484 */     public Object clone() { DakinsType localDakinsType1; try { DakinsType d = (DakinsType)super.clone();
/*  485 */         d.Solution = new double[31];
/*  486 */         ORSolverBase.this.arraycopy1(this.Solution, d.Solution);
/*  487 */         return d;
/*      */       }
/*      */       catch (CloneNotSupportedException e) {
/*  490 */         localDakinsType1 = null; } return localDakinsType1;
/*      */     }
/*      */   }
/*      */   
/*      */   protected class DakinsPivotType implements Serializable
/*      */   {
/*      */     ORSolverBase.DakinsType Node;
/*      */     DakinsPivotType Next;
/*      */     DakinsPivotType Last;
/*      */     
/*      */     protected DakinsPivotType() {}
/*      */   }
/*      */   
/*      */   protected class InteriorPointType implements Serializable
/*      */   {
/*  505 */     double[][] A = new double[11][11];
/*  506 */     double[][] AT = new double[11][11];
/*  507 */     double[][] ATilde = new double[11][11];
/*  508 */     double[][] D = new double[11][11];
/*  509 */     double[][] P = new double[11][11];
/*  510 */     double[] RightHandSide = new double[11];
/*  511 */     double[] x = new double[11];
/*  512 */     double[] xTilde = new double[11];
/*  513 */     double[] c = new double[11];
/*  514 */     double[] cTilde = new double[11];
/*  515 */     double[] cp = new double[11];
/*  516 */     double[][] Solution = new double[16][11];
/*      */     int It;
/*      */     int NumVariables;
/*      */     int NumConstraints;
/*      */     
/*      */     protected InteriorPointType() {}
/*      */   }
/*      */   
/*      */   protected class TransportationModelType implements Serializable {
/*      */     int NumSources;
/*      */     int NumDestinations;
/*  527 */     double[][] Cost = new double[16][16];
/*  528 */     double[][] CostM = new double[16][16];
/*  529 */     int[] Supply = new int[16];
/*  530 */     int[] Demand = new int[16];
/*      */     int NumBasicVariables;
/*      */     int Width;
/*      */     int Spacing;
/*      */     
/*      */     protected TransportationModelType() {}
/*      */   }
/*      */   
/*      */   protected class PathType implements Cloneable, Serializable { int Number;
/*  539 */     int[] Source = new int[31];
/*  540 */     int[] Destination = new int[31];
/*      */     
/*      */     protected PathType() {}
/*      */     
/*  544 */     public Object clone() { PathType localPathType1; try { PathType p = (PathType)super.clone();
/*  545 */         p.Source = new int[31];
/*  546 */         p.Destination = new int[31];
/*  547 */         ORSolverBase.this.arraycopy1(this.Source, p.Source);
/*  548 */         ORSolverBase.this.arraycopy1(this.Destination, p.Destination);
/*  549 */         return p;
/*      */       }
/*      */       catch (CloneNotSupportedException e) {
/*  552 */         localPathType1 = null; } return localPathType1;
/*      */     }
/*      */   }
/*      */   
/*      */   protected class TransportationTableauType
/*      */     implements Cloneable, Serializable
/*      */   {
/*  559 */     double[][] Cell = new double[16][16];
/*  560 */     double[][] CellM = new double[16][16];
/*  561 */     double[] U = new double[16];
/*  562 */     double[] UM = new double[16];
/*  563 */     double[] V = new double[16];
/*  564 */     double[] VM = new double[16];
/*  565 */     int[][] BasicVariable = new int[31][3];
/*  566 */     boolean[][] ABasicVariable = new boolean[16][16];
/*      */     
/*  568 */     int[] EnteringBasicVariable = new int[3];
/*  569 */     int[] LeavingBasicVariable = new int[3];
/*  570 */     int[] Marker = new int[3];
/*  571 */     boolean[] EliminatedSource = new boolean[16];
/*  572 */     boolean[] EliminatedDestination = new boolean[16];
/*  573 */     ORSolverBase.PathType Path = new ORSolverBase.PathType(ORSolverBase.this);
/*      */     int Crunch;
/*      */     int Spacing;
/*      */     int Y;
/*      */     TransportationTableauType NextTableau;
/*      */     TransportationTableauType LastTableau;
/*      */     
/*      */     protected TransportationTableauType() {}
/*      */     
/*  582 */     public Object clone() { TransportationTableauType localTransportationTableauType1; try { TransportationTableauType t = (TransportationTableauType)super.clone();
/*      */         
/*      */ 
/*  585 */         t.Cell = new double[16][16];
/*  586 */         t.CellM = new double[16][16];
/*  587 */         t.U = new double[16];
/*  588 */         t.UM = new double[16];
/*  589 */         t.V = new double[16];
/*  590 */         t.VM = new double[16];
/*  591 */         t.BasicVariable = new int[31][3];
/*  592 */         t.ABasicVariable = new boolean[16][16];
/*      */         
/*  594 */         t.EnteringBasicVariable = new int[3];
/*  595 */         t.LeavingBasicVariable = new int[3];
/*  596 */         t.Marker = new int[3];
/*  597 */         t.EliminatedSource = new boolean[16];
/*  598 */         t.EliminatedDestination = new boolean[16];
/*  599 */         t.Path = ((ORSolverBase.PathType)this.Path.clone());
/*      */         
/*  601 */         ORSolverBase.this.arraycopy2(this.Cell, t.Cell);
/*  602 */         ORSolverBase.this.arraycopy2(this.CellM, t.CellM);
/*  603 */         ORSolverBase.this.arraycopy1(this.U, t.U);
/*  604 */         ORSolverBase.this.arraycopy1(this.UM, t.UM);
/*  605 */         ORSolverBase.this.arraycopy1(this.V, t.V);
/*  606 */         ORSolverBase.this.arraycopy1(this.VM, t.VM);
/*  607 */         ORSolverBase.this.arraycopy2(this.BasicVariable, t.BasicVariable);
/*  608 */         ORSolverBase.this.arraycopy2(this.ABasicVariable, t.ABasicVariable);
/*  609 */         ORSolverBase.this.arraycopy1(this.EnteringBasicVariable, t.EnteringBasicVariable);
/*  610 */         ORSolverBase.this.arraycopy1(this.LeavingBasicVariable, t.LeavingBasicVariable);
/*  611 */         ORSolverBase.this.arraycopy1(this.Marker, t.Marker);
/*  612 */         ORSolverBase.this.arraycopy1(this.EliminatedSource, t.EliminatedSource);
/*  613 */         ORSolverBase.this.arraycopy1(this.EliminatedDestination, t.EliminatedDestination);
/*  614 */         return t;
/*      */       }
/*      */       catch (CloneNotSupportedException e) {
/*  617 */         localTransportationTableauType1 = null; } return localTransportationTableauType1;
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */   protected class NetworkType
/*      */     implements Cloneable, Serializable
/*      */   {
/*      */     int NumArcs;
/*      */     
/*  628 */     int CurrentCycle = 0;
/*      */     
/*      */     int NumNodes;
/*  631 */     double[][] Cost = new double[17][17];
/*  632 */     double[][] OriginalCost = new double[17][17];
/*  633 */     double[][] Flow = new double[17][17];
/*  634 */     double[][] Capacity = new double[17][17];
/*  635 */     boolean[][] Reversed = new boolean[17][17];
/*  636 */     double[] NetFlow = new double[17];
/*  637 */     double[] NodeNumber = new double[17];
/*  638 */     boolean[] NodeNumbered = new boolean[17];
/*      */     boolean NetworkCapacitated;
/*  640 */     boolean[][] AnArc = new boolean[17][17];
/*  641 */     boolean[][] Basic = new boolean[17][17];
/*  642 */     boolean[][] Capacitated = new boolean[17][17];
/*  643 */     int[] EnteringArc = new int[3];
/*  644 */     int[] LeavingArc = new int[3];
/*  645 */     double[][] FlowMatrix = new double[11][11];
/*  646 */     double[] FlowVector = new double[11];
/*      */     double EnteringArcFlow;
/*      */     int Width;
/*      */     int WhichProblem;
/*      */     int KeyY;
/*      */     int CapacityY;
/*  652 */     int[][] WhichBasic = new int[12][3];
/*  653 */     int[][] WhichReversed = new int[12][3];
/*      */     int HowManyReversed;
/*  655 */     int[][] WhichFlow = new int[12][3];
/*      */     double Z;
/*      */     boolean Feasible;
/*      */     boolean Unbounded;
/*      */     boolean Optimal;
/*      */     
/*      */     protected NetworkType() {}
/*      */     
/*  663 */     public Object clone() { NetworkType localNetworkType1; try { NetworkType n = (NetworkType)super.clone();
/*      */         
/*  665 */         n.Cost = new double[17][17];
/*  666 */         n.OriginalCost = new double[17][17];
/*  667 */         n.Flow = new double[17][17];
/*  668 */         n.Capacity = new double[17][17];
/*  669 */         n.Reversed = new boolean[17][17];
/*  670 */         n.NetFlow = new double[17];
/*  671 */         n.NodeNumber = new double[17];
/*  672 */         n.NodeNumbered = new boolean[17];
/*  673 */         n.AnArc = new boolean[17][17];
/*  674 */         n.Basic = new boolean[17][17];
/*  675 */         n.Capacitated = new boolean[17][17];
/*  676 */         n.EnteringArc = new int[3];
/*  677 */         n.LeavingArc = new int[3];
/*  678 */         n.FlowMatrix = new double[11][11];
/*  679 */         n.FlowVector = new double[11];
/*  680 */         n.WhichBasic = new int[12][3];
/*  681 */         n.WhichReversed = new int[12][3];
/*  682 */         n.WhichFlow = new int[12][3];
/*      */         
/*  684 */         ORSolverBase.this.arraycopy2(this.Cost, n.Cost);
/*  685 */         ORSolverBase.this.arraycopy2(this.OriginalCost, n.OriginalCost);
/*  686 */         ORSolverBase.this.arraycopy2(this.Flow, n.Flow);
/*  687 */         ORSolverBase.this.arraycopy2(this.Capacity, n.Capacity);
/*  688 */         ORSolverBase.this.arraycopy2(this.Reversed, n.Reversed);
/*  689 */         ORSolverBase.this.arraycopy1(this.NetFlow, n.NetFlow);
/*  690 */         ORSolverBase.this.arraycopy1(this.NodeNumber, n.NodeNumber);
/*  691 */         ORSolverBase.this.arraycopy1(this.NodeNumbered, n.NodeNumbered);
/*  692 */         ORSolverBase.this.arraycopy2(this.AnArc, n.AnArc);
/*  693 */         ORSolverBase.this.arraycopy2(this.Basic, n.Basic);
/*  694 */         ORSolverBase.this.arraycopy2(this.Capacitated, n.Capacitated);
/*  695 */         ORSolverBase.this.arraycopy1(this.EnteringArc, n.EnteringArc);
/*  696 */         ORSolverBase.this.arraycopy1(this.LeavingArc, n.LeavingArc);
/*  697 */         ORSolverBase.this.arraycopy2(this.FlowMatrix, n.FlowMatrix);
/*  698 */         ORSolverBase.this.arraycopy1(this.FlowVector, n.FlowVector);
/*  699 */         ORSolverBase.this.arraycopy2(this.WhichBasic, n.WhichBasic);
/*  700 */         ORSolverBase.this.arraycopy2(this.WhichReversed, n.WhichReversed);
/*  701 */         ORSolverBase.this.arraycopy2(this.WhichFlow, n.WhichFlow);
/*  702 */         return n;
/*      */       }
/*      */       catch (CloneNotSupportedException e) {
/*  705 */         localNetworkType1 = null; } return localNetworkType1;
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */   public class PolynomialType
/*      */     implements Cloneable, Serializable
/*      */   {
/*      */     int NumTerms;
/*      */     int NumVariables;
/*  715 */     double[] Coefficient = new double[11];
/*  716 */     int[][] Exponent = new int[11][4];
/*      */     
/*      */     public PolynomialType() {}
/*      */     
/*  720 */     public Object clone() { PolynomialType localPolynomialType1; try { PolynomialType p = (PolynomialType)super.clone();
/*      */         
/*  722 */         p.Coefficient = new double[11];
/*  723 */         p.Exponent = new int[11][4];
/*      */         
/*  725 */         ORSolverBase.this.arraycopy1(this.Coefficient, p.Coefficient);
/*  726 */         ORSolverBase.this.arraycopy2(this.Exponent, p.Exponent);
/*  727 */         return p;
/*      */       }
/*      */       catch (CloneNotSupportedException e) {
/*  730 */         localPolynomialType1 = null; } return localPolynomialType1;
/*      */     }
/*      */     
/*      */     public void ZeroAll()
/*      */     {
/*  735 */       this.NumTerms = 0;
/*  736 */       this.NumVariables = 0;
/*  737 */       this.Coefficient = new double[11];
/*  738 */       this.Exponent = new int[11][4];
/*      */     }
/*      */   }
/*      */   
/*      */   protected class OneDSearchType implements Serializable {
/*      */     double Derivative;
/*      */     double UpperBound;
/*      */     double LowerBound;
/*      */     double NewBound;
/*      */     double Value;
/*      */     
/*      */     protected OneDSearchType() {}
/*      */   }
/*      */   
/*      */   protected class GradientSearchType implements Serializable {
/*  753 */     double[] InitialSolution = new double[11];
/*  754 */     double[] Gradient = new double[11];
/*  755 */     double[] Difference = new double[11];
/*  756 */     double[] NewSolution = new double[11];
/*  757 */     double[] a = new double[11];
/*  758 */     double[] b = new double[11];
/*      */     double t;
/*      */     int Method;
/*  761 */     ORSolverBase.PolynomialType Polynomial = new ORSolverBase.PolynomialType(ORSolverBase.this);
/*      */     
/*      */     protected GradientSearchType() {}
/*      */   }
/*      */   
/*  766 */   protected class SimplexSensitivityType implements Serializable { int ChangeRow = 0;
/*  767 */     int ChangeColumn = 1;
/*  768 */     int MatrixRow = 0;
/*  769 */     int MatrixColumn = 1;
/*  770 */     int VectorRow = 0;
/*  771 */     int VectorColumn = 1;
/*  772 */     int ConstantRow = 0;
/*  773 */     int ConstantColumn = 1;
/*  774 */     double Change = 0.0D;
/*      */     
/*      */     protected SimplexSensitivityType() {}
/*      */   }
/*      */   
/*  779 */   protected class InteractiveNetworkType implements Cloneable, Serializable { int[][] Path = new int[12][12];
/*  780 */     double[] PathZ = new double[12];
/*      */     boolean Optimal;
/*      */     int EnteringArc;
/*      */     int LeavingArc;
/*      */     boolean ReverseLeavingArc;
/*      */     double EnteringArcFlow;
/*      */     
/*      */     protected InteractiveNetworkType() {}
/*      */     
/*  789 */     public Object clone() { InteractiveNetworkType localInteractiveNetworkType1; try { InteractiveNetworkType n = (InteractiveNetworkType)super.clone();
/*      */         
/*  791 */         n.Path = new int[12][12];
/*      */         
/*  793 */         n.PathZ = new double[12];
/*      */         
/*  795 */         ORSolverBase.this.arraycopy2(this.Path, n.Path);
/*  796 */         ORSolverBase.this.arraycopy1(this.PathZ, n.PathZ);
/*  797 */         return n;
/*      */       }
/*      */       catch (CloneNotSupportedException e) {
/*  800 */         localInteractiveNetworkType1 = null; } return localInteractiveNetworkType1;
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */   protected class TransitionMatrixType
/*      */     implements Serializable
/*      */   {
/*      */     int Size;
/*  809 */     double[][] Prob = new double[11][11];
/*      */     
/*      */     protected TransitionMatrixType() {}
/*      */   }
/*      */   
/*      */   protected class MarkovPIType implements Serializable { protected MarkovPIType() {}
/*      */     
/*  816 */     double[][] A = new double[11][11];
/*  817 */     double[][] ACompute = new double[11][11];
/*  818 */     double[] bCompute = new double[11];
/*  819 */     double[] cCompute = new double[11];
/*  820 */     double[][] A2 = new double[11][11];
/*  821 */     double[][] A2Old = new double[11][11];
/*  822 */     double[][] Value = new double[11][11];
/*      */     int CurrentState;
/*  824 */     int[] OldPolicy = new int[5];
/*  825 */     int[] NewPolicy = new int[5];
/*      */     
/*      */      }
/*      */   
/*      */   protected class EstimatorsType implements Serializable { double Y;
/*      */     double Z;
/*      */     double SumY;
/*      */     double SumZ;
/*  833 */     double SumY2; double SumZ2; double SumYZ; double PointEstimate; double[] ConfidenceInterval = new double[3];
/*      */     
/*      */     protected EstimatorsType() {}
/*      */   }
/*      */   
/*      */   protected class SimulationType implements Serializable {
/*      */     double CurrentTime;
/*      */     double NumArrivals;
/*      */     double NumCycles;
/*      */     double MaxTime;
/*      */     double MaxArrivals;
/*      */     double MaxCycles;
/*  845 */     double[] NextArrival = new double[4];
/*  846 */     double[][] WhenArrived = new double[4]['È'];
/*      */     
/*  848 */     int[] NextInLine = new int[4];
/*  849 */     int[] LastInLine = new int[4];
/*  850 */     int[] Serving = new int[6];
/*  851 */     double[] NextServiceCompletion = new double[6];
/*      */     
/*  853 */     int[] NumCustomers = new int[4];
/*  854 */     int[] NumInLine = new int[4];
/*  855 */     ORSolverBase.EstimatorsType[] L = new ORSolverBase.EstimatorsType[4];
/*      */     
/*  857 */     ORSolverBase.EstimatorsType[] Lq = new ORSolverBase.EstimatorsType[4];
/*      */     
/*  859 */     ORSolverBase.EstimatorsType[] W = new ORSolverBase.EstimatorsType[4];
/*      */     
/*  861 */     ORSolverBase.EstimatorsType[] Wq = new ORSolverBase.EstimatorsType[4];
/*      */     
/*  863 */     ORSolverBase.EstimatorsType[][] P = new ORSolverBase.EstimatorsType[4][11];
/*      */     
/*      */ 
/*      */     public SimulationType()
/*      */     {
/*  868 */       for (int i = 0; i <= 3; i++) {
/*  869 */         ORSolverBase tmp127_126 = this$0;tmp127_126.getClass();this.L[i] = new ORSolverBase.EstimatorsType(tmp127_126); ORSolverBase 
/*  870 */           tmp146_145 = this$0;tmp146_145.getClass();this.Lq[i] = new ORSolverBase.EstimatorsType(tmp146_145); ORSolverBase 
/*  871 */           tmp165_164 = this$0;tmp165_164.getClass();this.W[i] = new ORSolverBase.EstimatorsType(tmp165_164); ORSolverBase 
/*  872 */           tmp184_183 = this$0;tmp184_183.getClass();this.Wq[i] = new ORSolverBase.EstimatorsType(tmp184_183);
/*  873 */         for (int j = 0; j <= 10; j++) {
/*  874 */           ORSolverBase tmp213_212 = this$0;tmp213_212.getClass();this.P[i][j] = new ORSolverBase.EstimatorsType(tmp213_212);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */   protected class TSPInputModelType
/*      */     implements Serializable
/*      */   {
/*  884 */     public int num_city = 7;
/*  885 */     public double[][] matrix = { { 0.0D, 12.0D, 10.0D, 0.0D, 0.0D, 0.0D, 12.0D }, { 12.0D, 0.0D, 8.0D, 12.0D, 0.0D, 0.0D, 0.0D }, { 10.0D, 8.0D, 0.0D, 11.0D, 3.0D, 0.0D, 9.0D }, { 0.0D, 12.0D, 11.0D, 0.0D, 11.0D, 10.0D, 0.0D }, { 0.0D, 0.0D, 8.0D, 11.0D, 0.0D, 6.0D, 7.0D }, { 0.0D, 0.0D, 0.0D, 10.0D, 6.0D, 0.0D, 9.0D }, { 12.0D, 0.0D, 9.0D, 0.0D, 7.0D, 9.0D, 0.0D } };
/*      */     
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*  901 */     public int[] initSolution = { 1, 2, 3, 4, 5, 6, 7 };
/*      */     
/*      */     public String[][] SAPrintArray;
/*      */     
/*      */     public String[][] SA_Next_PrintData;
/*      */     
/*      */     public Vector SA_Auto_PrintData;
/*      */     
/*      */     public String SA_BestSolutionInfoLab;
/*      */     
/*      */     public String[][] GA_BestSolution_Data;
/*      */     
/*      */     public Vector GA_Print_Pop_Data;
/*      */     
/*      */     public Vector GA_Print_Child_Data;
/*      */     public Vector GA_Print_P_Connection_C_Data;
/*      */     public Vector GA_Print_ChildNum;
/*  918 */     public Vector TB_Print_TableV = null;
/*      */     
/*      */     public String[][] TB_Auto_Print_Table;
/*      */     
/*      */     public double MinDistance;
/*      */     public String bestSolution;
/*  924 */     public String isAuto = null;
/*      */     int Width;
/*      */     int Spacing;
/*      */     
/*      */     protected TSPInputModelType() {}
/*      */   }
/*      */   
/*      */   protected class SimulationDistributionType implements Serializable
/*      */   {
/*      */     int Distribution;
/*      */     double MinValue;
/*      */     double MaxValue;
/*      */     double Mean;
/*      */     double StandardDeviation;
/*      */     int k;
/*      */     
/*      */     protected SimulationDistributionType() {}
/*      */   }
/*      */   
/*      */   protected class InteractiveSimulation implements Cloneable, Serializable {
/*      */     int Iteration;
/*  945 */     boolean[] Answer1 = new boolean[5];
/*      */     int Answer2;
/*      */     int Answer3;
/*  948 */     int Answer4; double CurrentTime; int[] NumberOfCustomers = new int[3];
/*  949 */     int[] ClassBeingServed = new int[3];
/*  950 */     double[] NextArrival = new double[3];
/*  951 */     double[] NextService = new double[3];
/*      */     double WhenArrived;
/*      */     int LastCurrentEditRow;
/*      */     InteractiveSimulation Last;
/*      */     InteractiveSimulation Next;
/*      */     
/*      */     protected InteractiveSimulation() {}
/*      */     
/*  959 */     public Object clone() { InteractiveSimulation localInteractiveSimulation1; try { InteractiveSimulation i = (InteractiveSimulation)super.clone();
/*      */         
/*      */ 
/*  962 */         i.Answer1 = new boolean[5];
/*  963 */         i.NumberOfCustomers = new int[3];
/*  964 */         i.ClassBeingServed = new int[3];
/*  965 */         i.NextArrival = new double[3];
/*  966 */         i.NextService = new double[3];
/*      */         
/*      */ 
/*  969 */         ORSolverBase.this.arraycopy1(this.Answer1, i.Answer1);
/*  970 */         ORSolverBase.this.arraycopy1(this.NumberOfCustomers, i.NumberOfCustomers);
/*  971 */         ORSolverBase.this.arraycopy1(this.ClassBeingServed, i.ClassBeingServed);
/*  972 */         ORSolverBase.this.arraycopy1(this.NextArrival, i.NextArrival);
/*  973 */         ORSolverBase.this.arraycopy1(this.NextService, i.NextService);
/*  974 */         return i;
/*      */       }
/*      */       catch (CloneNotSupportedException e) {
/*  977 */         localInteractiveSimulation1 = null; } return localInteractiveSimulation1;
/*      */     }
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */   protected String ConvertDoubleToFixString(double a, int width, int dec, int justification, boolean sig)
/*      */   {
/*  985 */     if (Math.abs(a) < 1.0E-10D) {
/*  986 */       return ConvertIntToFixString(0, width, justification);
/*      */     }
/*      */     
/*  989 */     if (Math.abs(a - Math.round(a)) < 1.0E-10D) {
/*  990 */       return ConvertIntToFixString((int)Math.round(a), width, justification);
/*      */     }
/*      */     
/*  993 */     String s = Double.toString(a);
/*      */     
/*  995 */     int i = s.indexOf(".");
/*  996 */     if ((i != -1) && (s.length() > i + dec + 1)) {
/*  997 */       int j = s.indexOf("E");
/*  998 */       if (j != -1) {
/*  999 */         s = String.valueOf(String.valueOf(s.substring(0, i + dec))).concat(String.valueOf(String.valueOf(s.substring(j, s.length()))));
/*      */       } else
/* 1001 */         s = s.substring(0, i + dec);
/*      */     }
/* 1003 */     return ConvertStringToFixString(s, width, justification);
/*      */   }
/*      */   
/*      */   protected String ConvertIntToFixString(int a, int width, int justification)
/*      */   {
/* 1008 */     return ConvertStringToFixString(Integer.toString(a), width, justification);
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */   protected String ConvertStringToFixString(String a, int width, int justification)
/*      */   {
/* 1015 */     StringBuffer sb = new StringBuffer(a);
/*      */     
/* 1017 */     int len = sb.length();
/* 1018 */     if (justification == 1) {
/* 1019 */       for (int i = len; i <= width; i++) {
/* 1020 */         sb.append(' ');
/*      */       }
/*      */     }
/* 1023 */     if (len < width) {
/* 1024 */       for (int i = 0; i < width - len; i++) {
/* 1025 */         sb.insert(0, ' ');
/*      */       }
/*      */     }
/* 1028 */     return new String(sb);
/*      */   }
/*      */   
/*      */ 
/*      */ 
/* 1033 */   protected boolean bPrintToFile = false;
/* 1034 */   protected PrintWriter out = null;
/* 1035 */   private int PageCount = 0;
/*      */   protected int procedure;
/* 1037 */   protected String[] strPages = new String[20];
/*      */   
/* 1039 */   protected boolean BigMFlag = false;
/*      */   
/* 1041 */   protected static final int Normal = 0;
/* 1042 */   protected static final int Sign = 1;
/* 1043 */   protected static final int NoSign = 2;
/* 1044 */   protected static final int SignClose = 3;
/* 1045 */   protected static final int Left = 0;
/* 1046 */   protected static final int Right = 1;
/* 1047 */   protected static final int EquationForm = 0;
/* 1048 */   protected static final int TableauForm = 1;
/* 1049 */   protected static final int BigMMethod = 0;
/* 1050 */   protected static final int TwoPhaseMethod = 1;
/*      */   
/*      */   public void setStepVector(Vector s)
/*      */   {
/* 1054 */     this.steps = s;
/*      */   }
/*      */   
/*      */   protected abstract void ORPrint();
/*      */   
/*      */   public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
/* 1060 */     if (pi == 0) {
/* 1061 */       this.bPrintToFile = false;
/* 1062 */       InitilizaPrintVariables();
/* 1063 */       ORPrint();
/*      */     }
/*      */     else {
/* 1066 */       if (pi >= this.PageCount)
/* 1067 */         return 1;
/* 1068 */       if (this.strPages[pi].length() == 0) {
/* 1069 */         return 1;
/*      */       }
/*      */     }
/* 1072 */     g.setColor(Color.black);
/* 1073 */     g.translate((int)pf.getImageableX(), (int)pf.getImageableY());
/*      */     
/* 1075 */     Font f = new Font("Courier", 0, 9);
/* 1076 */     g.setFont(f);
/* 1077 */     drawStringWithReturn(g, this.strPages[pi]);
/*      */     
/* 1079 */     g.dispose();
/* 1080 */     return 0;
/*      */   }
/*      */   
/*      */   private void drawStringWithReturn(Graphics g, String s) {
/* 1084 */     if (s == null) {
/* 1085 */       return;
/*      */     }
/* 1087 */     int index1 = 0;
/*      */     
/* 1089 */     int LineHeight = g.getFontMetrics().getHeight();
/* 1090 */     int y = 2;
/* 1091 */     while (index1 < s.length()) {
/* 1092 */       int index2 = s.indexOf('\n', index1);
/* 1093 */       if (index2 == -1) {
/* 1094 */         g.drawString(s.substring(index1), 0, y * LineHeight);
/* 1095 */         return;
/*      */       }
/* 1097 */       if (index1 == index2) {
/* 1098 */         y++;
/*      */       }
/*      */       else {
/* 1101 */         g.drawString(s.substring(index1, index2), 0, y * LineHeight);
/* 1102 */         y++;
/*      */       }
/* 1104 */       index1 = index2 + 1;
/*      */     }
/*      */   }
/*      */   
/*      */   public void setPrintProcedure(int p)
/*      */   {
/* 1110 */     this.procedure = p;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   public boolean PrintToFile(String fileName, int p)
/*      */   {
/* 1119 */     setPrintProcedure(p);
/* 1120 */     return PrintToFile(fileName);
/*      */   }
/*      */   
/*      */   private boolean PrintToFile(String fileName0)
/*      */   {
/* 1125 */     String fileName = fileName0;
/* 1126 */     if (!fileName.endsWith(".txt")) {
/* 1127 */       fileName = String.valueOf(String.valueOf(fileName)).concat(".txt");
/*      */     }
/* 1129 */     System.out.println("fileName is :".concat(String.valueOf(String.valueOf(fileName))));
/* 1130 */     this.bPrintToFile = true;
/*      */     boolean bool;
/* 1132 */     try { this.out = new PrintWriter(new FileOutputStream(fileName));
/*      */     }
/*      */     catch (Exception e) {
/* 1135 */       return false;
/*      */     }
/* 1137 */     if (this.out == null) {
/* 1138 */       return false;
/*      */     }
/*      */     
/* 1141 */     InitilizaPrintVariables();
/* 1142 */     ORPrint();
/*      */     
/*      */     try
/*      */     {
/* 1146 */       this.out.close();
/*      */     }
/*      */     catch (Exception e) {
/* 1149 */       return false;
/*      */     }
/*      */     
/* 1152 */     this.out = null;
/* 1153 */     return true;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */   protected String IntegerToString(int x, int Space)
/*      */   {
/* 1161 */     if (x < 0)
/* 1162 */       Space -= 1;
/* 1163 */     int ND = NumDigits(x);
/* 1164 */     String AString; String AString; if (ND <= Space) {
/* 1165 */       AString = String.valueOf(Math.abs(x));
/*      */     } else {
/* 1167 */       Space -= 2;
/* 1168 */       if (ND > 10)
/* 1169 */         Space -= 1;
/* 1170 */       AString = String.valueOf(Math.abs(x));
/* 1171 */       AString = AString.substring(0, Space);
/* 1172 */       String Exponent = String.valueOf(ND - Space);
/* 1173 */       AString = String.valueOf(String.valueOf(new StringBuffer(String.valueOf(String.valueOf(AString))).append("e").append(Exponent)));
/*      */     }
/* 1175 */     if (x < 0)
/* 1176 */       AString = "-".concat(String.valueOf(String.valueOf(AString)));
/* 1177 */     return AString;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */   protected String RealToString(double x, int Space)
/*      */   {
/* 1185 */     DecimalFormat df = new DecimalFormat();
/*      */     
/* 1187 */     df.setGroupingUsed(false);
/* 1188 */     int OrigLength = Space;
/* 1189 */     if (Math.abs(x) < 1.0E-6D)
/* 1190 */       x = 0.0D;
/* 1191 */     if (x < 0)
/* 1192 */       Space -= 1;
/* 1193 */     int ND = RealNumDigits(x);
/* 1194 */     String AString; if ((Math.abs(x) > 0) && (Math.abs(x) < 1)) {
/* 1195 */       if (ND <= Space) {
/* 1196 */         int NumDecimals = Space - 2;
/*      */         String AString;
/*      */         do {
/* 1199 */           df.setMaximumFractionDigits(NumDecimals);
/* 1200 */           AString = df.format(Math.abs(x));
/* 1201 */           Space -= 1;
/* 1202 */           NumDecimals -= 1;
/*      */         }
/* 1204 */         while ((AString.charAt(AString.length() - 1) == '0') && (NumDecimals > 0));
/*      */         
/* 1206 */         if (x < 0) {
/* 1207 */           AString = "-".concat(String.valueOf(String.valueOf(AString)));
/*      */         }
/*      */       } else {
/* 1210 */         df.applyPattern("#.#E0");
/* 1211 */         String AString; String AString; if (Space == 4)
/*      */         {
/* 1213 */           String AString2 = df.format(Math.abs(x));
/*      */           
/* 1215 */           AString = String.valueOf(String.valueOf(new StringBuffer(String.valueOf(String.valueOf(AString2.substring(0, 1)))).append("e-").append(AString2.substring(AString2.length() - 1, AString2.length()))));
/*      */         }
/*      */         else {
/*      */           String AString;
/* 1219 */           if (Space == 5)
/*      */           {
/* 1221 */             String AString2 = df.format(Math.abs(x));
/* 1222 */             String AString; if (AString2.charAt(AString2.length() - 2) == '-') {
/* 1223 */               AString = String.valueOf(String.valueOf(new StringBuffer(String.valueOf(String.valueOf(AString2.substring(0, 1)))).append("e-").append(AString2.substring(AString2.length() - 1, AString2.length()))));
/*      */             }
/*      */             else
/*      */             {
/* 1227 */               AString = String.valueOf(String.valueOf(new StringBuffer(String.valueOf(String.valueOf(AString2.substring(0, 1)))).append("e-").append(AString2.substring(AString2.length() - 2, AString2.length()))));
/*      */             }
/*      */           } else {
/*      */             String AString;
/* 1231 */             if (Space == 6)
/*      */             {
/* 1233 */               String AString2 = df.format(Math.abs(x));
/* 1234 */               String AString; if (AString2.charAt(AString2.length() - 2) == '-') { String AString;
/* 1235 */                 if (AString2.length() >= 6) {
/* 1236 */                   AString = String.valueOf(String.valueOf(new StringBuffer(String.valueOf(String.valueOf(AString2.substring(0, 3)))).append("e-").append(AString2.substring(AString2.length() - 1, AString2.length()))));
/*      */                 }
/*      */                 else
/*      */                 {
/* 1240 */                   AString = AString2;
/*      */                 }
/*      */               } else {
/* 1243 */                 AString = String.valueOf(String.valueOf(new StringBuffer(String.valueOf(String.valueOf(AString2.substring(0, 1)))).append("e-").append(AString2.substring(AString2.length() - 2, AString2.length()))));
/*      */               }
/*      */             } else {
/*      */               String AString;
/* 1247 */               if ((Space >= 7) && (Space <= 99)) {
/* 1248 */                 if (ND <= 11)
/*      */                 {
/* 1250 */                   df.setMaximumFractionDigits(Space - 5);
/*      */                 }
/*      */                 else
/*      */                 {
/* 1254 */                   df.setMaximumFractionDigits(Space - 6);
/*      */                 }
/* 1256 */                 AString = df.format(Math.abs(x));
/*      */               }
/*      */               else {
/* 1259 */                 AString = df.format(Math.abs(x));
/*      */               } } } }
/* 1261 */         if (x < 0) {
/* 1262 */           AString = "-".concat(String.valueOf(String.valueOf(AString)));
/*      */         }
/*      */       }
/*      */     } else {
/* 1266 */       int NumDecimals = Space - ND - 1;
/* 1267 */       if (NumDecimals < 0) {
/* 1268 */         NumDecimals = 0;
/*      */       }
/*      */       do {
/* 1271 */         df.setMaximumFractionDigits(NumDecimals);
/* 1272 */         AString = df.format(Math.abs(x));
/* 1273 */         NumDecimals -= 1;
/*      */       }
/* 1275 */       while ((NumDecimals >= 0) && (AString.charAt(AString.length() - 1) == '0'));
/*      */       
/* 1277 */       AString = AString.trim();
/* 1278 */       if (x < 0)
/* 1279 */         AString = "-".concat(String.valueOf(String.valueOf(AString)));
/* 1280 */       if (AString.length() > OrigLength) {
/* 1281 */         df.applyPattern("#.#E0");
/* 1282 */         if (ND < 10) {
/* 1283 */           if ((Space >= 1) && (Space <= 4))
/*      */           {
/* 1285 */             String AString2 = df.format(Math.abs(x));
/* 1286 */             AString = String.valueOf(String.valueOf(new StringBuffer(String.valueOf(String.valueOf(AString2.substring(0, 1)))).append("e").append(AString2.substring(AString2.length() - 1, AString2.length()))));
/*      */ 
/*      */ 
/*      */           }
/* 1290 */           else if ((Space >= 5) && (Space <= 99))
/*      */           {
/* 1292 */             df.setMaximumFractionDigits(Space - 5);
/* 1293 */             AString = df.format(Math.abs(x));
/*      */           }
/*      */           
/*      */         }
/* 1297 */         else if ((Space >= 1) && (Space <= 5))
/*      */         {
/* 1299 */           String AString2 = df.format(Math.abs(x));
/* 1300 */           AString = String.valueOf(String.valueOf(new StringBuffer(String.valueOf(String.valueOf(AString2.substring(0, 1)))).append("e").append(AString2.substring(AString2.length() - 2, AString2.length()))));
/*      */ 
/*      */ 
/*      */         }
/* 1304 */         else if ((Space >= 6) && (Space <= 99))
/*      */         {
/* 1306 */           df.setMaximumFractionDigits(Space - 6);
/* 1307 */           AString = df.format(Math.abs(x));
/*      */         }
/*      */         
/* 1310 */         if (x < 0)
/* 1311 */           AString = "-".concat(String.valueOf(String.valueOf(AString)));
/*      */       }
/*      */     }
/* 1314 */     return AString;
/*      */   }
/*      */   
/*      */   private int NumDigits(int x)
/*      */   {
/* 1319 */     x = Math.abs(x);
/* 1320 */     int Len; int Len; if (x < 10) {
/* 1321 */       Len = 1; } else { int Len;
/* 1322 */       if (x < 100) {
/* 1323 */         Len = 2; } else { int Len;
/* 1324 */         if (x < 1000) {
/* 1325 */           Len = 3;
/*      */         } else
/* 1327 */           Len = (int)(Math.log(x) / Math.log(10.0D)) + 1; } }
/* 1328 */     return Len;
/*      */   }
/*      */   
/*      */   private int RealNumDigits(double x)
/*      */   {
/* 1333 */     x = Math.abs(x);
/* 1334 */     int Len; int Len; if ((x > 0) && (x < 1)) {
/* 1335 */       Len = (int)(0.999999999999D - Math.log(x) / Math.log(10.0D)) + 2; } else { int Len;
/* 1336 */       if (x < 10) {
/* 1337 */         Len = 1; } else { int Len;
/* 1338 */         if (x < 100) {
/* 1339 */           Len = 2; } else { int Len;
/* 1340 */           if (x < 'Ϩ') {
/* 1341 */             Len = 3;
/*      */           } else
/* 1343 */             Len = (int)(Math.log(x) / Math.log(10.0D)) + 1; } } }
/* 1344 */     return Len;
/*      */   }
/*      */   
/*      */   private String getBlankString(int Len)
/*      */   {
/* 1349 */     String BlankString = "";
/* 1350 */     for (int i = 0; i < Len; i++)
/* 1351 */       BlankString = String.valueOf(String.valueOf(BlankString)).concat(" ");
/* 1352 */     return BlankString;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */   protected String FormatInteger(int Data, int Len, int WriteSign, int Justification)
/*      */   {
/* 1360 */     int OrigLength = Len;
/* 1361 */     if ((WriteSign == 1) || ((WriteSign == 0) && (Data < 0)) || (WriteSign == 3))
/*      */     {
/* 1363 */       Len -= 1; }
/* 1364 */     String AString = IntegerToString(Math.abs(Data), Len);
/* 1365 */     switch (WriteSign) {
/*      */     case 0: 
/* 1367 */       if (Data < 0)
/* 1368 */         AString = "-".concat(String.valueOf(String.valueOf(AString)));
/*      */       break;
/*      */     case 3: 
/* 1371 */       if (Data < 0) {
/* 1372 */         AString = "-".concat(String.valueOf(String.valueOf(AString)));
/*      */       } else
/* 1374 */         AString = "+".concat(String.valueOf(String.valueOf(AString)));
/* 1375 */       break;
/*      */     }
/* 1377 */     if (WriteSign == 1)
/* 1378 */       OrigLength -= 1;
/* 1379 */     if (AString.length() < OrigLength) {
/* 1380 */       if (Justification == 0) {
/* 1381 */         AString = String.valueOf(String.valueOf(AString)).concat(String.valueOf(String.valueOf(getBlankString(OrigLength - AString.length()))));
/*      */       } else {
/* 1383 */         AString = String.valueOf(String.valueOf(getBlankString(OrigLength - AString.length()))).concat(String.valueOf(String.valueOf(AString)));
/*      */       }
/*      */     }
/* 1386 */     if (WriteSign == 1) {
/* 1387 */       if (Data < 0) {
/* 1388 */         AString = "-".concat(String.valueOf(String.valueOf(AString)));
/*      */       } else
/* 1390 */         AString = "+".concat(String.valueOf(String.valueOf(AString)));
/* 1391 */       OrigLength += 1;
/*      */     }
/* 1393 */     return AString.substring(0, OrigLength);
/*      */   }
/*      */   
/*      */   protected String FormatString(String Data, int Len, int Justification) { String s;
/*      */     String s;
/* 1398 */     if (Data.length() < Len) { String s;
/* 1399 */       if (Justification == 0) {
/* 1400 */         s = String.valueOf(String.valueOf(Data)).concat(String.valueOf(String.valueOf(getBlankString(Len - Data.length()))));
/*      */       } else {
/* 1402 */         s = String.valueOf(String.valueOf(getBlankString(Len - Data.length()))).concat(String.valueOf(String.valueOf(Data)));
/*      */       }
/*      */     } else {
/* 1405 */       s = Data.substring(0, Len); }
/* 1406 */     return s;
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */ 
/*      */   protected String FormatReal(double Data, int Len, int WriteSign, int Justification)
/*      */   {
/* 1414 */     int OrigLength = Len;
/* 1415 */     if ((WriteSign == 1) || ((WriteSign == 0) && (Data < 0)) || (WriteSign == 3))
/*      */     {
/* 1417 */       Len -= 1; }
/* 1418 */     if (this.BigMFlag)
/* 1419 */       Len -= 1;
/* 1420 */     String AString = RealToString(Math.abs(Data), Len);
/* 1421 */     switch (WriteSign) {
/*      */     case 0: 
/* 1423 */       if (Data < 0)
/* 1424 */         AString = "-".concat(String.valueOf(String.valueOf(AString)));
/* 1425 */       break;
/*      */     case 3: 
/* 1427 */       if (Data < 0) {
/* 1428 */         AString = "-".concat(String.valueOf(String.valueOf(AString)));
/*      */       } else
/* 1430 */         AString = "+".concat(String.valueOf(String.valueOf(AString)));
/*      */       break;
/*      */     }
/* 1433 */     if (this.BigMFlag)
/* 1434 */       AString = String.valueOf(String.valueOf(AString)).concat("M");
/* 1435 */     if (WriteSign == 1)
/* 1436 */       OrigLength -= 1;
/* 1437 */     if (AString.length() < OrigLength) {
/* 1438 */       if (Justification == 0) {
/* 1439 */         AString = String.valueOf(String.valueOf(AString)).concat(String.valueOf(String.valueOf(getBlankString(OrigLength - AString.length()))));
/*      */       } else {
/* 1441 */         AString = String.valueOf(String.valueOf(getBlankString(OrigLength - AString.length()))).concat(String.valueOf(String.valueOf(AString)));
/*      */       }
/*      */     }
/* 1444 */     if (WriteSign == 1) {
/* 1445 */       if (Data < 0) {
/* 1446 */         AString = "-".concat(String.valueOf(String.valueOf(AString)));
/*      */       } else
/* 1448 */         AString = "+".concat(String.valueOf(String.valueOf(AString)));
/* 1449 */       OrigLength += 1;
/*      */     }
/* 1451 */     return AString.substring(0, OrigLength);
/*      */   }
/*      */   
/*      */ 
/* 1455 */   private boolean TopMargin = false;
/* 1456 */   private boolean RightMargin = false;
/* 1457 */   protected int LineNumber = 1;
/* 1458 */   protected int PrinterX = 1;
/* 1459 */   private int PageNumber = 0;
/*      */   
/*      */   private void InitilizaPrintVariables() {
/* 1462 */     this.TopMargin = false;
/* 1463 */     this.RightMargin = false;
/* 1464 */     this.LineNumber = 1;
/* 1465 */     this.PrinterX = 1;
/* 1466 */     this.BigMFlag = false;
/*      */     
/* 1468 */     this.PageCount = 1;
/* 1469 */     this.PageNumber = 0;
/* 1470 */     this.strPages = new String[20];
/* 1471 */     for (int i = 0; i < 20; i++)
/* 1472 */       this.strPages[i] = "";
/*      */   }
/*      */   
/*      */   protected void CheckTopMargin() {
/* 1476 */     if (this.bPrintToFile == false)
/* 1477 */       return;
/* 1478 */     if ((this.LineNumber == 1) && (this.PrinterX == 1) && (!this.TopMargin)) {
/* 1479 */       this.out.println();
/* 1480 */       this.out.println();
/* 1481 */       this.out.println();
/* 1482 */       this.TopMargin = true;
/*      */     }
/*      */   }
/*      */   
/*      */   protected void CheckRightMargin() {
/* 1487 */     if (this.bPrintToFile == false)
/* 1488 */       return;
/* 1489 */     if ((this.PrinterX == 1) && (!this.RightMargin)) {
/* 1490 */       this.out.print("  ");
/* 1491 */       this.RightMargin = true;
/*      */     }
/*      */   }
/*      */   
/*      */   protected void Print(String s) {
/* 1496 */     if (this.bPrintToFile) {
/* 1497 */       CheckTopMargin();
/* 1498 */       CheckRightMargin();
/* 1499 */       this.out.print(s);
/* 1500 */       this.PrinterX += s.length();
/*      */     }
/*      */     else {
/* 1503 */       int tmp47_44 = this.PageNumber; String[] tmp47_40 = this.strPages;tmp47_40[tmp47_44] = String.valueOf(String.valueOf(tmp47_40[tmp47_44])).concat(String.valueOf(String.valueOf(s)));
/* 1504 */       this.PrinterX += s.length();
/*      */     }
/*      */   }
/*      */   
/*      */   protected void PrintPage() {
/* 1509 */     if (this.bPrintToFile) {
/* 1510 */       char[] c = new char[1];
/* 1511 */       c[0] = '\f';
/* 1512 */       this.out.write(c);
/* 1513 */       this.PrinterX = 1;
/* 1514 */       this.LineNumber = 1;
/* 1515 */       this.TopMargin = false;
/* 1516 */       this.RightMargin = false;
/* 1517 */       CheckTopMargin();
/* 1518 */       CheckRightMargin();
/*      */     }
/*      */     else {
/* 1521 */       this.PageNumber += 1;
/* 1522 */       this.PageCount += 1;
/* 1523 */       this.PrinterX = 1;
/* 1524 */       this.LineNumber = 1;
/*      */     }
/*      */   }
/*      */   
/*      */   protected void PrintLine(String StringToPrint) {
/* 1529 */     if (this.bPrintToFile) {
/* 1530 */       CheckTopMargin();
/* 1531 */       CheckRightMargin();
/* 1532 */       this.out.println(StringToPrint);
/* 1533 */       this.LineNumber += 1;
/* 1534 */       if (this.LineNumber > 56) {
/* 1535 */         PrintPage();
/*      */       } else {
/* 1537 */         this.PrinterX = 1;
/* 1538 */         this.RightMargin = false;
/* 1539 */         CheckRightMargin();
/*      */       }
/*      */     }
/*      */     else {
/* 1543 */       int tmp74_71 = this.PageNumber; String[] tmp74_67 = this.strPages;tmp74_67[tmp74_71] = String.valueOf(String.valueOf(tmp74_67[tmp74_71])).concat(String.valueOf(String.valueOf(String.valueOf(String.valueOf(StringToPrint)).concat("\n"))));
/* 1544 */       this.LineNumber += 1;
/* 1545 */       if (this.LineNumber > 56)
/* 1546 */         PrintPage();
/* 1547 */       this.PrinterX = 1;
/*      */     }
/*      */   }
/*      */   
/*      */   protected void SkipLine() {
/* 1552 */     if (this.bPrintToFile) {
/* 1553 */       if (this.LineNumber == 56) {
/* 1554 */         PrintPage();
/*      */       } else {
/* 1556 */         this.out.println();
/* 1557 */         this.LineNumber += 1;
/* 1558 */         this.PrinterX = 1;
/* 1559 */         this.RightMargin = false;
/* 1560 */         CheckRightMargin();
/*      */       }
/*      */     }
/*      */     else {
/* 1564 */       if (this.LineNumber == 56) {
/* 1565 */         PrintPage();
/*      */       } else {
/* 1567 */         int tmp81_78 = this.PageNumber; String[] tmp81_74 = this.strPages;tmp81_74[tmp81_78] = String.valueOf(String.valueOf(tmp81_74[tmp81_78])).concat("\n");
/* 1568 */         this.LineNumber += 1;
/*      */       }
/* 1570 */       this.PrinterX = 1;
/*      */     }
/*      */   }
/*      */   
/*      */   protected void Skip(int NumLines)
/*      */   {
/* 1576 */     if (this.bPrintToFile) {
/* 1577 */       this.LineNumber += NumLines;
/* 1578 */       if (this.LineNumber > 56) {
/* 1579 */         PrintPage();
/*      */       } else {
/* 1581 */         for (int i = 1; i <= NumLines; i++)
/* 1582 */           this.out.println();
/* 1583 */         this.PrinterX = 1;
/* 1584 */         this.RightMargin = false;
/* 1585 */         CheckRightMargin();
/*      */       }
/*      */     }
/*      */     else {
/* 1589 */       for (int i = 1; i <= NumLines; i++) {
/* 1590 */         int tmp85_82 = this.PageNumber; String[] tmp85_78 = this.strPages;tmp85_78[tmp85_82] = String.valueOf(String.valueOf(tmp85_78[tmp85_82])).concat("\n"); }
/* 1591 */       this.LineNumber += NumLines;
/* 1592 */       if (this.LineNumber > 56)
/* 1593 */         PrintPage();
/* 1594 */       this.PrinterX = 1;
/*      */     }
/*      */   }
/*      */   
/*      */   protected void PrintLines(int NumLines) {
/* 1599 */     if (this.bPrintToFile) {
/* 1600 */       if (this.LineNumber + NumLines > 56) {
/* 1601 */         PrintPage();
/*      */       }
/*      */     }
/* 1604 */     else if (this.LineNumber + NumLines > 56) {
/* 1605 */       PrintPage();
/*      */     }
/*      */   }
/*      */   
/*      */   protected void PrintSpace(int SpaceNeeded, int NewLineStart)
/*      */   {
/* 1611 */     CheckTopMargin();
/* 1612 */     CheckRightMargin();
/* 1613 */     if (this.PrinterX + SpaceNeeded > 81) {
/* 1614 */       this.PrinterX = 1;
/* 1615 */       SkipLine();
/* 1616 */       for (int i = 1; i <= NewLineStart - 1; i++) {
/* 1617 */         Print(" ");
/*      */       }
/*      */     }
/*      */   }
/*      */   
/*      */   protected void TabPrint(int Space, String StringToPrint) {
/* 1623 */     CheckTopMargin();
/* 1624 */     CheckRightMargin();
/* 1625 */     for (int i = this.PrinterX; i <= Space - 1; i++)
/* 1626 */       Print(" ");
/* 1627 */     Print(StringToPrint);
/*      */   }
/*      */   
/*      */   protected void TabPrintLine(int Space, String StringToPrint) {
/* 1631 */     CheckTopMargin();
/* 1632 */     CheckRightMargin();
/* 1633 */     TabPrint(Space, StringToPrint);
/* 1634 */     SkipLine();
/*      */   }
/*      */   
/*      */ 
/*      */ 
/*      */   protected void PrintMatrix(double[][] Matrix, int Rows, int Columns, int Space)
/*      */   {
/* 1641 */     PrintLines(Rows + 1);
/* 1642 */     Print(" _");
/* 1643 */     TabPrintLine((Space + 2) * Columns + 3, "_");
/* 1644 */     for (int i = 1; i <= Rows; i++) {
/* 1645 */       if (i == Rows) {
/* 1646 */         Print("|_");
/*      */       } else
/* 1648 */         Print("|");
/* 1649 */       for (int j = 1; j <= Columns; j++) {
/* 1650 */         if (Matrix[i][j] == 'ϧ') {
/* 1651 */           TabPrint((Space + 2) * j - 4, "----");
/*      */         } else {
/* 1653 */           TabPrint((Space + 2) * (j - 1) + 4, FormatReal(Matrix[i][j], Space, 0, 0));
/*      */         }
/*      */       }
/* 1656 */       if (i == Rows) {
/* 1657 */         TabPrintLine((Space + 2) * Columns + 3, "_|");
/*      */       } else {
/* 1659 */         TabPrintLine((Space + 2) * Columns + 4, "|");
/*      */       }
/*      */     }
/*      */   }
/*      */ }


/* Location:              C:\Program Files (x86)\Accelet\IORTutorial\IORTutorial.jar!\ORSolverBase.class
 * Java compiler version: 2 (46.0)
 * JD-Core Version:       0.7.1
 */
