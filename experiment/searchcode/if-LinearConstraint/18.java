/**
 * 
 */
package scpsolver.lpsolver;


import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.DoubleParam;
import ilog.cplex.IloCplex.IntParam;

import java.util.ArrayList;
import java.util.Iterator;


import scpsolver.constraints.Constraint;
import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearConstraint;
import scpsolver.constraints.LinearEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.constraints.QuadraticSmallerThanEqualsContraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.problems.LinearProgram;
import scpsolver.problems.MathematicalProgram;
import scpsolver.qpsolver.QuadraticProgram;
import scpsolver.qpsolver.QuadraticProgramSolver;
import scpsolver.util.Matrix;
import scpsolver.util.NonZeroElementIterator;
import scpsolver.util.SparseVector;

/**
 * @author hannes
 *
 */
public class CPLEXSolver implements LinearProgramSolver,QuadraticProgramSolver {


	IloCplex model;
	IloNumVar[] x;
	Thread  cplexthread;
	int timeconstraint = -1;
	//String licensefile = "ILOG_LICENSE_FILE=/Users/hannes/access.ilm";
	String lasterror = "";


	public CPLEXSolver() {
	}
	
	public IloNumExpr getCplexScalProd(SparseVector c) {
		NonZeroElementIterator nze = c.getNonZeroElementIterator();
		ArrayList<IloNumExpr> qexpr = new ArrayList<IloNumExpr>();

		while (nze.hasNext()) {

			Double cval = (Double) nze.next();

			try {
				IloNumExpr expr;
				expr = model.prod(cval, x[nze.getActualj()]);
				qexpr.add(expr);
			} catch (IloException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}

		}

		IloNumExpr[] qexprarr = new IloNumExpr[qexpr.size()];
		int i = 0;


		for (Iterator<IloNumExpr> iterator = qexpr.iterator(); iterator.hasNext();) {
			qexprarr[i++] = (IloNumExpr) iterator.next();

		}

		IloNumExpr qsum = null;
		try {
			qsum = model.sum(qexprarr);
		} catch (IloException e) {
			e.printStackTrace();
		}
		return qsum;
	}

	public IloNumExpr getCplexQuadraticTerm(Matrix c) {
		NonZeroElementIterator nze = c.getNonZeroElementIterator();
		ArrayList<IloNumExpr> qexpr = new ArrayList<IloNumExpr>();

		while (nze.hasNext()) {

			Double cval = (Double) nze.next();
			try {
				IloNumExpr expr;
				expr = model.prod(cval, x[nze.getActuali()],x[nze.getActualj()]);
				qexpr.add(expr);
			} catch (IloException e) {
				e.printStackTrace();
			}

		}

		IloNumExpr[] qexprarr = new IloNumExpr[qexpr.size()];
		int i = 0;

		for (Iterator<IloNumExpr> iterator = qexpr.iterator(); iterator.hasNext();) {
			qexprarr[i++] = (IloNumExpr) iterator.next();

		}

		IloNumExpr qsum = null;
		try {
			qsum = model.sum(qexprarr);
		} catch (IloException e) {
			lasterror = e.getMessage();
			e.printStackTrace();
		}
		return qsum;
	}

	public String getLasterror() {
		return lasterror;
	}

	@Deprecated
	public String getException() {
		return lasterror;
	}

/*	public String getLicensefile() {
		return licensefile;
	}

	public void setLicensefile(String licensefile) {
		this.licensefile = licensefile;
	}*/

	/* (non-Javadoc)
	 * @see nmi.lpsolver.LinearProgramSolver#addEqualsConstraint(nmi.constraints.LinearEqualsConstraint)
	 */
	public void addEqualsConstraint(LinearEqualsConstraint c) {
		// TODO Auto-generated method stub
		try {



			IloNumExpr qsum = getCplexScalProd(c.getCSparse());

			model.addEq(qsum, c.getT());
		} catch (Exception e) {
			System.out.println("CPLEX: Couldnt add equals constraint");
			e.printStackTrace();
		}

	}

	/* (non-Javadoc)
	 * @see nmi.lpsolver.LinearProgramSolver#addLinearBiggerThanEqualsConstraint(nmi.constraints.LinearBiggerThanEqualsConstraint)
	 */
	public void addLinearBiggerThanEqualsConstraint(
			LinearBiggerThanEqualsConstraint c) {
		try {
			IloNumExpr qsum = getCplexScalProd(c.getCSparse());
			model.addGe(qsum, c.getT());

		} catch (Exception e) {
			System.out.println("CPLEX: Couldnt add bigger constraint");
			e.printStackTrace();
		}
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see nmi.lpsolver.LinearProgramSolver#addLinearSmallerThanEqualsConstraint(nmi.constraints.LinearSmallerThanEqualsConstraint)
	 */
	public void addLinearSmallerThanEqualsConstraint(
			LinearSmallerThanEqualsConstraint c) {
		try {

			IloNumExpr qsum = getCplexScalProd(c.getCSparse());

			model.addLe(qsum, c.getT());
			//model.addLe(model.scalProd(c.getC(), x) , c.getT());	
		} catch (Exception e) {
			System.out.println("CPLEX: Couldnt add smaller constraint");
			e.printStackTrace();
		}


	}


	private void implementModel(LinearProgram lp)  {
		try {
			model = initModel();

			/* add variables */
			/* add bounds */					
			//	System.out.println("Dimension:" + lp.getDimension());

			if (!lp.isMIP()) {
				if (lp.hasBounds()) {
					x = model.numVarArray(lp.getDimension(), lp.getLowerbound(),lp.getUpperbound());
				} else {
					x = model.numVarArray(lp.getDimension(), MathematicalProgram.makeDoubleArray(lp.getDimension(), Double.NEGATIVE_INFINITY), MathematicalProgram.makeDoubleArray(lp.getDimension(), Double.POSITIVE_INFINITY));
					
				}
				//	System.out.println("Dimension:" + lp.getDimension());
			} else {
				/* set to MIP if necessary */

				IloNumVarType[] typearr = new IloNumVarType[lp.getDimension()];
				for (int i = 0; i < typearr.length; i++) {
					typearr[i] = (lp.getIsinteger()[i])? IloNumVarType.Int:IloNumVarType.Float;
					if (lp.getIsboolean()[i]) typearr[i] = IloNumVarType.Bool;
				}


				if (lp.hasBounds()) {
					x = model.numVarArray(lp.getDimension(), lp.getLowerbound(),lp.getUpperbound(),typearr);
				} else {
					x = model.numVarArray(lp.getDimension(), MathematicalProgram.makeDoubleArray(lp.getDimension(), Double.NEGATIVE_INFINITY), MathematicalProgram.makeDoubleArray(lp.getDimension(), Double.POSITIVE_INFINITY),typearr);					
				}

			}


			if (lp.isMinProblem()) {
				model.addMinimize(model.scalProd(x, lp.getC()));
			} else {
				model.addMaximize(model.scalProd(x, lp.getC()));				
			}			

			/* add constraints */

			ArrayList<Constraint> constraints = lp.getConstraints();

			for (Constraint constraint : constraints) {
				((LinearConstraint) constraint).addToLinearProgramSolver(this);
			}
			if (timeconstraint > 0) {				
				System.out.println("Setting time constraint to:" + timeconstraint + " seconds");
				model.setParam(DoubleParam.TiLim , (double) timeconstraint);
			}
		} catch (Exception e) {
			System.out.println("Model implementation exception: "+e.getMessage());
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see nmi.lpsolver.LinearProgramSolver#solve(nmi.lpsolver.LinearProgram)
	 */
	public double[] solve(LinearProgram lp) {
		try {
			
			implementModel(lp);
			model.setParam(IntParam.Threads, 1);
			model.solve();
			return model.getValues(x);


		} catch (Exception e) {
			lasterror = e.getMessage();
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		endModel(model);
		return null;
	}

	/* (non-Javadoc)
	 * @see nmi.lpsolver.LinearProgramSolver#solve(nmi.lpsolver.LinearProgram)
	 */
	public double[] solveWarmStart(LinearProgram lp, double[] s) {
		try {
			implementModel(lp);
			setStartSolution(s);
			model.solve();
			return model.getValues(x);


		} catch (Exception e) {
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * @param args
	 */


	public void addQuadraticSmallerThanEqualsContraint(
			QuadraticSmallerThanEqualsContraint c) {
		// TODO Auto-generated method stub
		try {
			IloNumExpr qsum = getCplexQuadraticTerm(c.getQ());
			IloNumExpr csum = getCplexScalProd(c.getCSparse());
			model.addLe(model.sum(qsum, csum), c.getT());						
		} catch (Exception e) {
			System.out.println("CPLEX: Couldnt add quadratic contraints smaller constraint");
			e.printStackTrace();
		}
	}

	/**
	 * @return the timeconstraint
	 */
	public int getTimeconstraint() {
		return timeconstraint;
	}

	/**
	 * @param timeconstraint the timeconstraint to set
	 */
	public void setTimeconstraint(int timeconstraint) {
		this.timeconstraint = timeconstraint;
	}


	public double[] solve(QuadraticProgram qp) {
		try {

			model = initModel();

			/* add variables */
			/* add bounds */					

			if (!qp.isMIP()) {

				if (qp.hasBounds()) {
					x = model.numVarArray(qp.getDimension(), qp.getLowerbound(),qp.getUpperbound());
				} else {
					x = model.numVarArray(qp.getDimension(), MathematicalProgram.makeDoubleArray(qp.getDimension(), Double.MIN_VALUE), MathematicalProgram.makeDoubleArray(qp.getDimension(), Double.MAX_VALUE));
				}
			} else {
				/* set to MIP if necessary */

				IloNumVarType[] typearr = new IloNumVarType[qp.getDimension()];
				for (int i = 0; i < typearr.length; i++) {
					typearr[i] = (qp.getIsinteger()[i])? IloNumVarType.Int:IloNumVarType.Float;
				}
				


				if (qp.hasBounds()) {
					x = model.numVarArray(qp.getDimension(), qp.getLowerbound(),qp.getUpperbound(),typearr);
				} else {
					x = model.numVarArray(qp.getDimension(), MathematicalProgram.makeDoubleArray(qp.getDimension(), Double.MIN_VALUE), MathematicalProgram.makeDoubleArray(qp.getDimension(), Double.MAX_VALUE),typearr);					
				}

			}


			/* create Q */

			IloNumExpr qsum = model.prod(0.5,this.getCplexQuadraticTerm(qp.getQ()));

			if (qp.isMinProblem()) {
				model.addMinimize(model.sum(qsum,model.scalProd(x, qp.getC())));
			} else {

				model.add(model.maximize(model.sum(qsum,model.scalProd(x, qp.getC()))));				
			}			

			/* add constraints */

			ArrayList<Constraint> constraints = qp.getConstraints();

			for (Constraint constraint : constraints) {
				((LinearConstraint) constraint).addToLinearProgramSolver(this);
			}
			if (timeconstraint > 0) {				
				System.out.println("Setting time constraint to:" + timeconstraint + " seconds");
				model.setParam(DoubleParam.TiLim , (double) timeconstraint);
			}
			model.solve();

			return model.getValues(x);


		} catch (Exception e) {
			e.printStackTrace();
		}
		endModel(model);
		// TODO Auto-generated method stub
		return null;
	}



	public void setStartSolution(double[] s) {
		// TODO write the codo, hai!
		try {
			model.setVectors(s, null, x, null, null, null);
		} catch (IloException e) {
			System.out.println("CPLEX solver: Could not set initial solution");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * for threading, "new IloCplex()" shouldn't be tried in parallel!
	 * 
	 * @param model
	 * @throws IloException 
	 */
	synchronized static IloCplex initModel() throws IloException
	{
		IloCplex mod = new IloCplex();
		//System.out.println("-->MODEL:" + mod);
		return mod;
	}


	/**
	 * the model must be ended to free the memory.
	 * for threading, this shouldn't be tried in parallel!
	 * 
	 * @param model
	 */
	synchronized static void endModel(IloCplex mod)
	{
		if (mod != null)
			mod.end();
	}

	
	public String[] getLibraryNames() {
		return new String[]{"cplex125"};
	}
	
	public String getName() {
		return "CPLEX";
	}

}

