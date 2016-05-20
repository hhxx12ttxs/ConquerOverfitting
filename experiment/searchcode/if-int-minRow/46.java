package ecf3;

import java.text.*; // NumberFormat

/**
 * An equation system consists of a square matrix of size cdim*cdim<br>
 * and a right member = a column vector of size cdim*1. <br>
 * E.g. &nbsp 1.0 1.0 2.0 | 4.0<br> &nbsp &nbsp &nbsp &nbsp &nbsp 2.0 1.0 1.0 |
 * 5.0<br> &nbsp &nbsp &nbsp &nbsp &nbsp 0.0 3.0 4.0 | 8.0<br>
 * <br>
 * Example code:<br>
 * <code>
        LinEquSystem eqsys = new LinEquSystem(3);<br>
        System.out.println(eqsys);<br>
        LinearEquation eq;<br>
        try {<br>
            eq = new LinearEquation(3);<br>
            eq.setCoefficient(0, );<br>
            eq.setCoefficient(1, 1);<br>
            eq.setCoefficient(2, 2);<br>
            eq.setRightMember(4);<br>
            eqsys.addEquation(eq);<br>
            <br>
            eq = new LinearEquation(3);<br>
            eq.setCoefficient(0, 2);<br>
            eq.setCoefficient(1, 1);<br>
            eq.setCoefficient(2, 1);<br>
            eq.setRightMember(5);<br>
            eqsys.addEquation(eq);<br>
            <br>
            eq = new LinearEquation(3);<br>
            eq.setCoefficient(0, 0);<br>
            eq.setCoefficient(1, 3);<br>
            eq.setCoefficient(2, 4);<br>
            eq.setRightMember(8);<br>
            eqsys.addEquation(eq);<br><br>
            System.out.println(eqsys);<br>
            <br>
            double[] solution = eqsys.getSolution();<br>
            System.out.println("Solution: ");<br>
            for (int i=0; i&lt;solution.length; i++)<br>
                System.out.println("i=" + i + "  X=" + 0.001*Math.round(1000*solution[i]));<br>
            <br>
        } catch (Exception e) {<br>
            System.out.println("LinEquSystem.main: Could not create equations system\n"
                    + e);<br>
        }</code> <br><br>
 * Solution:<br>
 * <code>
      i=0  X=1.2<br>
      i=1  X=2.4<br>
      i=2  X=0.2<br>
 * </code> <br>
 * @author Lars Olert
 */
public class LinEquSystem {

    double[][] equsys;
    int cdim; // Number of variables of the equation system
    int crow; // Number of equations added to the system
    double maxAllowedConditionNumber = 2000; // ... for the system matrix

    /**
     * Makes an equation system consists of a square matrix of size
     * cdim*cdim<br> 
     * and a right member = a column vector of size cdim*1 with
     * zero elements. <br> 
     * E.g. &nbsp 0.0 0.0 0.0 | 0.0<br> 
     * &nbsp &nbsp &nbsp &nbsp &nbsp 0.0 0.0 0.0 | 0.0<br> 
     * &nbsp &nbsp &nbsp &nbsp &nbsp 0.0 0.0 0.0 | 0.0<br>
     *
     * @param cdim = number of equations = number of unknowns
     */
    public LinEquSystem(int cdim) {
        crow = 0;
        if (cdim <= 0) {
            this.cdim = 0;
            equsys = null;
        }
        // Make an equation system with only zero (0.0) elements
        this.cdim = cdim;
        equsys = new double[cdim][cdim + 1];
        for (int irow = 0; irow < cdim; irow++) {
            for (int icol = 0; icol < cdim + 1; icol++) {
                equsys[irow][icol] = 0;
            }
        }
    } // LinEquSystem

    public void addEquation(double[] vrelem) throws EquationSystemException {
        // If vrelem is a row vector of size 1*cdim+1 add this equation
        // to the last empty row of the equation system
        // else if vrelem has a different length does nothing
        if (vrelem.length == (cdim + 1) && crow < cdim) {
            System.arraycopy(vrelem, 0, equsys[crow], 0, cdim + 1);
            crow++;
        } // if
        else {
            throw new EquationSystemException("Equation " + vrelem + " could not be added");
        }
    } // addEquation

    public void addEquation(LinearEquation equation) throws EquationSystemException {
        // If equation is of size cdim add this equation
        // to the last empty row of the equation system
        // else if equation has a different length does nothing
        if (equation.getNoVar() == cdim && crow < cdim) {
            double[] vrelem = equation.getCoefficients();
            System.arraycopy(vrelem, 0, equsys[crow], 0, cdim + 1);
            crow++;
        } // if
        else {
            throw new EquationSystemException(equation + " could not be added");
        }
    } // addEquation

    @Override
    public Object clone() {
        // cdim = Number of variables of the equation system
        LinEquSystem clone = new LinEquSystem(cdim);
        clone.crow = crow;  // Number of equations added to the system
        for (int irow = 0; irow < crow; irow++) {
            System.arraycopy(equsys[irow], 0, clone.equsys[irow], 0, cdim + 1);
        }
        return clone;
    } // clone

    /**
     * Condition number based on maximum norm of the system matrix.
     *
     * @return The condition number of the system matrix.
     */
    public double cond() {
        double maxRow = 0;
        double minRow = Double.POSITIVE_INFINITY;
        double condNum = 0;
        // Find max row norm
        for (int irow = 0; irow < cdim; irow++) {
            double sumRow = 0;
            for (int icol = 0; icol < cdim; icol++) {
                sumRow += Math.abs(equsys[irow][icol]);
            } // for icol
            if (sumRow > maxRow) {
                maxRow = sumRow;
            }
            if (sumRow < minRow) {
                minRow = sumRow;
            }
        } // for irow
        condNum = maxRow / minRow;
        return condNum;
    } // cond

    public void replaceEquation(LinearEquation equation, int iflow) throws EquationSystemException {
        // If equation is of size cdim add this equation
        // to the last empty row of the equation system
        // else if equation has a different length does nothing
        if (equation.getNoVar() == cdim && iflow < cdim) {
            double[] vrelem = equation.getCoefficients();
            System.arraycopy(vrelem, 0, equsys[iflow], 0, cdim + 1);
        } // if
        else {
            throw new EquationSystemException(equation + " could not be replaced");
        }
    } // addEquation

    /**
     * Solve the equation system. Throw EquationSystemException exception if
     * equation system is badly conditioned.
     */
    public double[] getSolution() throws EquationSystemException {
        // Check max norm of system matrix
        if (cond() > maxAllowedConditionNumber) {
            throw new EquationSystemException("\nThe equation system is badly conditioned.\n"
                    + "Check the Max values in the Units dialog (Edit menu) and set them closer to\n"
                    + "max expected flow values.",
                    EquationSystemException.BADLY_CONDITIONED);
        }
        // Solve equation system
        triangulate();
        double[] vrsolution = new double[cdim];
        int irow = cdim - 1;
        while (irow >= 0) {
            double rightmember = equsys[irow][cdim];  // right member as it is
            // Move known results to right member side
            for (int icol = irow + 1; icol < cdim; icol++) {
                rightmember -= equsys[irow][icol] * vrsolution[icol];
            } // for
            vrsolution[irow] = rightmember / equsys[irow][irow];
            irow--;
        } // for
        return vrsolution;
    } // getSolution

    public void triangulate() throws EquationSystemException {
        // Rotate rows until all elements below the system diagonal are zero.
        if (crow < cdim) {
            throw new EquationSystemException("Too few equations, " + crow + " out of " + cdim);
        }
        for (int icol = 0; icol < cdim - 1; icol++) {
            int irow1 = icol;
            for (int irow2 = cdim - 1; irow2 > irow1; irow2--) {
                rotate(irow1, irow2);
            }
        } // for
        for (int irow = 0; irow < cdim; irow++) {
            if (Math.abs(equsys[irow][irow]) < 0.001) {
                throw new EquationSystemException("Equation system is singular (has no solution)");
            }
        }
    } // triangulate

    public void rotate(int irow1, int irow2) {
        // Multiply the equation system matrix, including the right member
        // by a Givens matrix, e.g.:
        // ( 1  0  0  0 )  ( a11  a12  a13  a14 | r1 )   ( a11  a12  a13  a14 | r1 )
        // | 0  c  0  s |  | a21  a22  a23  a24 | r2 | = | b21  b22  b23  b24 | t2 |
        // | 0  0  1  0 |  | a31  a32  a33  a34 | r3 |   | a31  a32  a33  a34 | r3 |
        // ( 0 -s  0  c )  ( a41  a42  a43  a44 | r4 )   ( b41    0  b43  b44 | t4 )
        // which is a unitary matrix except for four elements
        // c in position (irow1,irow1), s in position (irow1,irow2)
        // -s in position (irow2,irow1) and c in position (irow2,irow2),
        // c = cos(alfa) and s = sin(alfa) where alfa is the rotation angle.
        // The result is that rows irow1 and irow2 are combined by linear addition
        // to form two new rows (equations). The angle alfa is chosen so that the
        // first element of the second row becomes zero (=0).
        // Let a1 be the element (irow1,irow1) of the first row irow1 and
        // a2 be the element (iro12,irow1) of the second row irow2.
        // Then let denom = sqrt(a1*a1+a2*a2) and c = a1/denom and s = a2/denom.
        // The new elements b[irow1,icol] = c*a[irow1,icol] + s*a[irow2,icol]
        //                  b[irow2,icol] = -s*a[irow1,icol] + c*a[irow2,icol]
        // Column index runs from 0 to cdim-1.
        // Start the process by first setting element a41 to zero then a31 and
        // then a21, the equation system becomes:
        // ( a11  a12  a13  a14 | r1 )
        // |   0  a22  a23  a24 | r2 |
        // |   0  a32  a33  a34 | r3 |
        // (   0  a42  a43  a44 | r4 ), all elements aij and ri having new values.
        // Continue by setting elements a42 and a32 to zero, the equation system becomes:
        // ( a11  a12  a13  a14 | r1 )
        // |   0  a22  a23  a24 | r2 |
        // |   0    0  a33  a34 | r3 |
        // (   0    0  a43  a44 | r4 ), elements aij and ri of rows 1-3 having new values.
        // and finally setting element a43 to zero, the equation system becomes:
        // ( a11  a12  a13  a14 | r1 )
        // |   0  a22  a23  a24 | r2 |
        // |   0    0  a33  a34 | r3 |
        // (   0    0    0  a44 | r4 ), elements aij and ri of rows 2-3 having new values.
        //
        int icol = irow1; // Column index to be rotated
        double a1 = equsys[irow1][icol];
        double a2 = equsys[irow2][icol];
        if (a2 == 0) {
            return; // There is already a zero in that position
        }
        double denom = Math.sqrt(a1 * a1 + a2 * a2);
        double c = a1 / denom;
        double s = a2 / denom;
        equsys[irow1][icol] = c * a1 + s * a2;
        equsys[irow2][icol] = 0;
        icol++;
        while (icol < cdim + 1) {
            a1 = equsys[irow1][icol];
            a2 = equsys[irow2][icol];
            equsys[irow1][icol] = c * a1 + s * a2;
            equsys[irow2][icol] = -s * a1 + c * a2;
            icol++;
        }
    } // rotate

    protected void printEquation(double[] vrelem) {
        int length = 8;
        String sz = "Equation: ";
        for (int ielem = 0; ielem < vrelem.length - 1; ielem++) {
            sz += padToLength(vrelem[ielem], length) + " ";
        }
        sz += "| " + padToLength(vrelem[vrelem.length - 1], length);
        System.out.println(sz);
    } // printEquation

    @Override
    public String toString() {
        int length = 6;
        String sz = "Equation system:\n";
        for (int irow = 0; irow < cdim; irow++) {
            for (int icol = 0; icol < cdim; icol++) {
                sz += padToLength(equsys[irow][icol], length) + " ";
            }
            sz += "| ";
            sz += padToLength(equsys[irow][cdim], length) + "\n";
        } // for irow
        return sz;
    } // toString

    /**
     * Print a compact picture of the equation system. Coefficient are marked
     * with cross(x), zeros with period(.).
     */
    public String toStringCompact() {
        return toStringCompact("", 0);
    } // toStringCompact

    /**
     * Print a compact picture of the equation system. Coefficient are marked
     * with cross(x), zeros with period(.).
     *
     * @param commentSign Comment sign for the specific file format.
     * @param indexOffset Index offset for vectors (0 or 1):
     */
    public String toStringCompact(String commentSign, int indexOffset) {
        int length = 6;
        String sz = commentSign + "Compact view of the equation system:\n";
        for (int irow = 0; irow < cdim; irow++) {
            sz += commentSign + padToLength(irow + indexOffset, 2) + ": ";
            for (int icol = 0; icol < cdim; icol++) {
                double x = equsys[irow][icol];
                if (x == 0) {
                    sz += ".";
                } else {
                    sz += "x";
                }
            } // for icol
            sz += "| ";
            sz += padToLength(equsys[irow][cdim], length) + "\n";
        } // for irow
        return sz;
    } // toString

    /**
     * Print Maple format of the equation system.
     *
     * @param szCommandStart Command start prompt.
     * @return A string with each line of the eqation system separated by line
     * feed.
     */
    public String toStringMaple(String commentSign, String szCommandStart) {
        return toStringExport(commentSign, szCommandStart,
                "Maple", ":=",
                "Matrix(" + cdim + "," + cdim + ",[[  ",
                "]  ,[",
                "]]);",
                "Vector(" + cdim + ",[",
                "]);\n");
    } // toStringMaple

    /**
     * Print Maple format of the equation system.
     *
     * @return A string with each line of the eqation system separated by line
     * feed.
     */
    public String toStringMatlab(String commentSign) {
        return toStringExport(commentSign, "", "Matlab", "=",
                "[", "; ...\n   ", "];\n",
                "[", "];\n");
    } // toStringMatlab

    /**
     * Print general format of the equation system.
     *
     * @return A string with each line of the eqation system separated by line
     * feed.
     */
    public String toStringExport(String commentSign, String szCommandStart,
            String szType, String szEquals,
            String szMatrixStart, String szNewRow, String szMatrixEnd,
            String szVectorStart, String szVectorEnd) {
        String sz = commentSign + szType + " view of the equation system,\n"
                + commentSign + "mxaSys = system matrix, vrbRM = right member:\n";
        if (cdim == 0) {
            return "No equation system was defined.";
        }

        // System matrix
        sz += szCommandStart + "mxaSys" + szEquals + szMatrixStart;

        if (cdim == 1) {
            // Only one row and one unknown
            sz += equsys[0][0] + szMatrixEnd;
        } else {
            // Two or more rows and unknowns
            // First data row
            int irow = 0;
            for (int icol = 0; icol < cdim - 1; icol++) {
                double x = equsys[irow][icol];
                sz += x + ",";
            } // for icol
            sz += equsys[irow][cdim - 1] + szNewRow;

            // Data rows: #2 -> second last
            irow = 1;
            while (irow < cdim - 1) {
                for (int icol = 0; icol < cdim - 1; icol++) {
                    double x = equsys[irow][icol];
                    sz += x + ",";
                } // for icol
                sz += equsys[irow][cdim - 1] + szNewRow;
                irow++;
            } // while

            // Last data row
            for (int icol = 0; icol < cdim - 1; icol++) {
                double x = equsys[irow][icol];
                sz += x + ",";
            } // for icol
            sz += equsys[irow][cdim - 1] + szMatrixEnd;

        } // if/else

        // Right member
        int icol = cdim;
        sz += "\n" + szCommandStart + "vrbRM" + szEquals + szVectorStart;
        for (int irow = 0; irow < cdim - 1; irow++) {
            double x = equsys[irow][icol];
            sz += x + ",";
        } // for icol
        sz += equsys[cdim - 1][cdim] + szVectorEnd;

        return sz;
    } // toString

    protected String padToLength(double elem, int length) {
        NumberFormat numform = NumberFormat.getInstance();
        numform.setMaximumFractionDigits(2);
        String sz = numform.format(elem);
        while (sz.length() < length) {
            sz = " " + sz;
        }
        return sz;
    } // padToLength
    
    /**
     * Test of LinEquSystem
     * 
     * @param args Not used
     */
    public static void main(String[] args) {
        LinEquSystem eqsys = new LinEquSystem(3);
        System.out.println(eqsys);
        LinearEquation eq;
        try {
            eq = new LinearEquation(3);
            eq.setCoefficient(0, 1);
            eq.setCoefficient(1, 1);
            eq.setCoefficient(2, 2);
            eq.setRightMember(4);
            System.out.println("Add " + eq);
            eqsys.addEquation(eq);
            
            eq = new LinearEquation(3);
            eq.setCoefficient(0, 2);
            eq.setCoefficient(1, 1);
            eq.setCoefficient(2, 1);
            eq.setRightMember(5);
            System.out.println("Add " + eq);
            eqsys.addEquation(eq);
            
            eq = new LinearEquation(3);
            eq.setCoefficient(0, 0);
            eq.setCoefficient(1, 3);
            eq.setCoefficient(2, 4);
            eq.setRightMember(8);
            System.out.println("Add " + eq);
            eqsys.addEquation(eq);
            
            System.out.println(eqsys);
            
            double[] solution = eqsys.getSolution();
            System.out.println("Solution: ");
            for (int i=0; i<solution.length; i++)
                System.out.println("i=" + i + "  X=" + 0.001*Math.round(1000*solution[i]));
            
        } catch (Exception e) {
            System.out.println("LinEquSystem.main: Could not create equations system\n"
                    + e);
        }
        
    }
    
} // LinEquSystem

