package scpsolver.lpsolver;

import java.util.ArrayList;
import java.util.Iterator;

import lpsolve.LpSolve;
import lpsolve.LpSolveException;

import scpsolver.constraints.Constraint;
import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearConstraint;
import scpsolver.constraints.LinearEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.problems.LinearProgram;
import scpsolver.util.SparseVector;

/**
 * lp_solve wrapper for the SCPSolver API.
 * 
 * @author schober
 *
 */
public class LPSOLVESolver implements LinearProgramSolver {
	
	private LpSolve solver = null;
	private long timeconstraint = 0;
	

	/*
	 * Not needed, because the SolverFactory will take care of this automatically
	static {
		// System.loadLibrary("lpsolve55");
		System.loadLibrary("lpsolve55j");
	}
	*/
	
	/**
	 * Transfers the usual index counting to the lp_solve index counting
	 * 
	 * @param index The array of indices with non-zero elements
	 * @parum used The number of non-zero elements
	 * @return The shifted array
	 */
	private int[] transferIndexArray (int[] index, int used) {
		int[] newIndex = new int[used];
		for (int i = 0; i < used; i++)
			newIndex[i] = index[i] + 1;
		return newIndex;
	}
	
	/**
	 * Shrinks the array to the matching size
	 * 
	 * @param data The array of multiplication factors
	 * @param used The number of non-zero elements
	 * @return the shrunk array
	 */
	// TODO Either remove this comment or the whole function after testing
	@SuppressWarnings("unused")
	private double[] transferDataArray(double[] data, int used) {
		double[] newData = new double[used];
		for (int i = 0; i < used; i++)
			newData[i] = data[i];
		return newData;
	}
	
	private void addLinearConstraint (LinearConstraint c, int constrType) {
		
		if (solver == null) {
			System.err.println("WARNING: Adding constraint to a non-existing problem");
			try {
				solver = LpSolve.makeLp(0, c.getC().length);
			} catch (LpSolveException e) {
				System.err.println("Can't instantiate solver:");
				System.err.println("Maybe some libraries are missing?");
				System.err.println(e.getMessage());
				throw new RuntimeException("Could not instantiate LPSOLVESolver");
			}
		}
		
		SparseVector row = c.getCSparse();
		try {
			// solver.addConstraintex(row.getUsed(), transferDataArray(row.getData(),row.getUsed()), 
			// 				   	   transferIndexArray(row.getIndex(),row.getUsed()), constrType, c.getT());
			// TODO Ein wenig mehr testen, ob es gut funktioniert. Falls ja: obere Zeile und Funktion 'transferDataArray' l??schen!
			solver.addConstraintex(row.getUsed(), row.getData(), 
				   	   transferIndexArray(row.getIndex(),row.getUsed()), constrType, c.getT());
		} catch (LpSolveException e) {
			System.err.println("Couldn't add constraint");
			System.err.println(e.getMessage());
			throw new RuntimeException("Could not add constraint");
		}
	}
	
	

	public void addEqualsConstraint(LinearEqualsConstraint c) {
		this.addLinearConstraint(c, LpSolve.EQ);
	}

	public void addLinearBiggerThanEqualsConstraint(
			LinearBiggerThanEqualsConstraint c) {
		this.addLinearConstraint(c, LpSolve.GE);
	}

	public void addLinearSmallerThanEqualsConstraint(
			LinearSmallerThanEqualsConstraint c) {
		this.addLinearConstraint(c, LpSolve.LE);
	}

	/**
	 * @param timeconstraint the timeconstraint to set
	 */
	public void setTimeconstraint(long timeconstraint) {
		this.timeconstraint = timeconstraint;
	}

	/**
	 * @return the timeconstraint
	 */
	public long getTimeconstraint() {
		return timeconstraint;
	}

	public String[] getLibraryNames() {
		return new String[]{"lpsolve55j"};
	}

	public String getName() {
		return "LPSOLVE";
	}

	public double[] solve(LinearProgram lp) {
		
		try {
			if (solver != null) {
				System.err.println("WARNING: overriding existing problem ...");
				solver.deleteLp();
				solver = null;
			}
			
			solver = LpSolve.makeLp(0, lp.getDimension());
			
			if (lp.isMinProblem())
				solver.setMinim();
			else
				solver.setMaxim();
			
			// Warnings and errors are reported, but not more
			// TODO Change back to LpSolve.IMPORTANT after debugging
			solver.setVerbose(LpSolve.IMPORTANT);
			
			// Builds the model faster row-wise
			solver.setAddRowmode(true);
			
			double[] c = new double[lp.getC().length + 1];
			System.arraycopy(lp.getC(), 0, c, 1, lp.getC().length);
			solver.setObjFn(c);
			
			for (Constraint constr : lp.getConstraints()) {
				if (constr instanceof LinearEqualsConstraint) {
					this.addEqualsConstraint((LinearEqualsConstraint) constr);
				}
				else if (constr instanceof LinearBiggerThanEqualsConstraint) {
					this.addLinearBiggerThanEqualsConstraint((LinearBiggerThanEqualsConstraint) constr);
				}
				else if (constr instanceof LinearSmallerThanEqualsConstraint) {
					this.addLinearSmallerThanEqualsConstraint((LinearSmallerThanEqualsConstraint) constr);
				}
				else
					throw new RuntimeException("Unexpected constraint type" + constr.getName());
			}
			
			// Need to unset rowmode after adding objective and constraints -- see API
			solver.setAddRowmode(false);
			
			if (!lp.hasBounds()) {
				for (int i = 0; i < lp.getDimension(); i++)
					solver.setUnbounded(i+1);
			}
			else {
				for (int i = 0; i < lp.getDimension(); i++)
					solver.setBounds(i+1, lp.getLowerbound()[i], lp.getUpperbound()[i]);
			}
			
			boolean[] integers = lp.getIsinteger();
			for (int i = 0; i < integers.length; i++)
				solver.setInt(i+1, integers[i]);
			
			boolean[] booleans = lp.getIsboolean();
			for (int i = 0; i < booleans.length; i++)
				solver.setBinary(i+1, booleans[i]);
			
			if (timeconstraint > 0) {				
				System.out.println("Setting time constraint to:" + timeconstraint + " seconds");
				solver.setTimeout(timeconstraint);
			}
			
			/*
			if (solver.solve() != LpSolve.OPTIMAL) {
				System.err.println("WARNING: solving failed or is suboptimal");
			}
			*/
			
			switch (solver.solve()) {
			case LpSolve.OPTIMAL:
				break;
			case LpSolve.NOMEMORY:
				System.err.println("WARNING: ran out of memory during solving");
				break;
			case LpSolve.SUBOPTIMAL:
				System.err.println("WARNING: solution is suboptimal");
				break;
			case LpSolve.INFEASIBLE:
				System.err.println("WARNING: model is infeasible");
				break;
			case LpSolve.UNBOUNDED:
				System.err.println("WARNING: model is unbounded");
				break;
			case LpSolve.DEGENERATE:
				System.err.println("WARNING: model is degenerate");
				break;
			case LpSolve.NUMFAILURE:
				System.err.println("WARNING: numerical failure happened");
				break;
			case LpSolve.USERABORT:
				System.err.println("WARNING: solving aborted by user");
				break;
			case LpSolve.TIMEOUT:
				System.err.println("WARNING: timeout occured");
				break;
			case LpSolve.PRESOLVED:
				System.err.println("WARNING: model was already solved by presolving");
				break;
			case LpSolve.PROCFAIL:
				System.err.println("WARNING: branch and bound method failed");
				break;
			case LpSolve.PROCBREAK:
				System.err.println("WARNING: branch and bound stopped because of a break-at-first or a break-at-value");
				break;
			case LpSolve.FEASFOUND:
				System.err.println("WARNING: a feasible branch and bound solution was found");
				break;
			case LpSolve.NOFEASFOUND:
				System.err.println("WARNING: no feasible branch and bound solution was found");
				break;
			default:
				System.err.println("WARNING: something unexpected happened while trying to solve the problem");
				break;
			}
			
			double[] result = solver.getPtrVariables();
			solver.deleteLp();
			solver = null;
			return result;
			
		} catch (LpSolveException e) {
			System.err.println("Can't instantiate solver:");
			System.err.println("Maybe some libraries are missing?");
			System.err.println(e.getMessage());
			throw new RuntimeException("Could not instantiate LPSOLVESolver");
		}
	}
	
	public static void main(String[] args) {
		
		LinearProgram lp = new LinearProgram(new double[]{10.0, 6.0, 4.0});
		lp.addConstraint(new LinearSmallerThanEqualsConstraint(new double[]{1.0,1.0,1.0}, 320,"p"));
		lp.addConstraint(new LinearSmallerThanEqualsConstraint(new double[]{10.0,4.0,5.0}, 650,"q"));
		lp.addConstraint(new LinearBiggerThanEqualsConstraint(new double[]{2.0,2.0,6.0}, 100,"r1"));

		lp.setLowerbound(new double[]{30.0,0.0,0.0});

		//lp.addConstraint(new LinearEqualsConstraint(new double[]{1.0,1.0,1.0}, 100,"t"));

		lp.setInteger(0);
		lp.setInteger(1);
		lp.setInteger(2);

		LPSOLVESolver solver = (LPSOLVESolver) SolverFactory.getSolver("LPSOLVE");
		// GLPKSolver solver = (GLPKSolver) SolverFactory.getSolver("GLPK");

		System.out.println(solver.solve(lp)[0]);
		double[] sol = solver.solve(lp);
		ArrayList<Constraint> constraints = lp.getConstraints();
		for (Iterator<Constraint> iterator = constraints.iterator(); iterator.hasNext();) {
			Constraint constraint = (Constraint) iterator.next();
			if (constraint.isSatisfiedBy(sol)) System.out.println(constraint.getName() + " satisfied");
		}
		
		lp = new LinearProgram(new double[]{2., 0., 1., 0.5, 0.5});
		lp.setMinProblem(Boolean.TRUE);
		
		lp.setLowerbound(new double[]{1., 1., 1., 1., 1.});
		
		lp.addConstraint(new LinearBiggerThanEqualsConstraint
							(new double[]{0.5, 0., 0., 2., 0.}, 2., "x_1 + x_4 >= 2"));
		lp.addConstraint(new LinearSmallerThanEqualsConstraint
							(new double[]{0., 2., 0., 2., 1.}, 5., "2 + x_4 + 0.5*x_5 <= 5"));
		lp.addConstraint(new LinearBiggerThanEqualsConstraint
							(new double[]{0., 0., 3., 0., 10.}, 16., "3*x_3 + 5*x_5 >= 16"));
		
		for (int i = 0; i < lp.getDimension(); i++)
			lp.setInteger(i);
		
		solver.setTimeconstraint(20);
		
		// solver = new GLPKSolver();
		
		double[] solution = solver.solve(lp);
		double[] expected = new double[]{1., 1., 2., 1., 1.};
		
		for (int i = 0; i < lp.getDimension() || i < expected.length; i++) {
			if (Math.abs(solution[i] - expected[i]) < 0.01)
				System.out.println("Variable " + i + " erfolgreich gefunden!");
			else
				System.out.println("ACHTUNG: Variable " + i + " nicht erfolgreich gefunden!");
		}

	}

	@Override
	public void setTimeconstraint(int t) {
		System.out.println("setTimeconstraint not yet functional for LPSOLVE!");
		
	}
}

