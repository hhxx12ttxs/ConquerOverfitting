/*
Copyright (C) 2009  Diego Darriba

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package es.uvigo.darwin.prottest.model;

import java.io.PrintWriter;
import java.io.Serializable;

import pal.alignment.Alignment;
import pal.tree.Tree;
import pal.tree.TreeUtils;
import es.uvigo.darwin.prottest.model.state.ModelEmptyLkState;
import es.uvigo.darwin.prottest.model.state.ModelFilledLkState;
import es.uvigo.darwin.prottest.model.state.ModelLkState;
import es.uvigo.darwin.prottest.util.exception.ProtTestInternalException;
import es.uvigo.darwin.prottest.util.printer.ProtTestFormattedOutput;

import static es.uvigo.darwin.prottest.util.logging.ProtTestLogger.*;

import java.io.StringWriter;

/**
 * Substitution model.
 */
public abstract class Model implements Serializable {

    /** The serialVersionUID. */
    private static final long serialVersionUID = 20090804L;

    // distributions
    /** Useful constant for uniform distribution. */
    public static final int DISTRIBUTION_UNIFORM = 0;
    /** Useful constant for distribution with a proportion of invariable sites. */
    public static final int DISTRIBUTION_INVARIABLE = 1;
    /** Useful constant for gamma distribution. */
    public static final int DISTRIBUTION_GAMMA = 2;
    /** Useful constant for gamma distribution with a proportion of invariable sites. */
    public static final int DISTRIBUTION_GAMMA_INV = 3;
    
    // frequencies distribution
    /** The value of Uniform Frequencies Distribution. */
    static final int FREQ_DISTRIBUTION_UNIFORM = 1;
    /** The value of Empirical Frequencies Distribution. */
    static final int FREQ_DISTRIBUTION_EMPIRICAL = 2;
    /** The value of Maximum Likelihood Frequencies Distribution. */
    static final int FREQ_DISTRIBUTION_MAXIMUM_LIKELIHOOD = 3;
    /** The value of any other frequencies distribution. */
    static final int FREQ_DISTRIBUTION_OTHER = 4;
    
    /** Useful constant for consider observed frequencies. */
    public static final String PROP_PLUS_F = "plusF";
    /** The matrix name. */
    private String matrix;
    /** The distribution. */
    private int distribution;
    /** Consider observed frequencies. */
    private boolean plusF;
    /** The frequencies distribution. */
    protected int frequenciesDistribution;
    /** The alignment hashcode. */
    private int alignment;
    /** The number of sequences. */
    private int numberOfSequences;
    /** The tree. */
    private Tree tree;
    /** The number of model parameters. */
//	private  int numModelParameters;
    /** The number of transition categories. */
    private int numOfTransCategories;
    /** The likelihood calculation state. */
    private ModelLkState lkState;
    /** The external executor command line. */
    private String[] commandLine;

    /**
     * Gets the likelihood estimated value.
     * 
     * @return the likelihood estimated value
     */
    public double getLk() {
        return lkState.getLk();
    }

    /**
     * Checks if is computed.
     * 
     * @return true, if is computed
     */
    public boolean isComputed() {
        return (lkState instanceof ModelFilledLkState);
    }

    /**
     * Sets the likelihood estimated value.
     * 
     * @param lk the new likelihood estimated value
     */
    public void setLk(double lk) {
        lkState = lkState.setLk(lk);
    }

    /**
     * Gets the alpha estimated value.
     * 
     * @return the alpha estimated value
     */
    public double getAlpha() {
        return lkState.getAlpha();
    }

    /**
     * Sets the alpha estimated value.
     * 
     * @param alpha the new alpha estimated value
     */
    public void setAlpha(double alpha) {
        lkState = lkState.setAlpha(alpha);
    }

    /**
     * Gets the proportion of invariant sites.
     * 
     * @return the proportion of invariant sites
     */
    public double getInv() {
        return lkState.getInv();
    }

    /**
     * Sets the proportion of invariant sites.
     * 
     * @param inv the new proportion of invariant sites
     */
    public void setInv(double inv) {
        lkState = lkState.setInv(inv);
    }

    /**
     * Gets the alignment hashcode.
     * 
     * @return the alignment hashcode
     */
    public int getAlignment() {
        return alignment;
    }

    /**
     * Sets the alignment only if there wasn't set or it is a different instance of the same object.
     * 
     * @param alignment the new alignment
     */
    public void setAlignment(Alignment alignment) {
        if (this.alignment != 0 && this.alignment != alignment.toString().hashCode()) {
            throw new ProtTestInternalException("cannot set a different alignment");
        }
        this.alignment = alignment.toString().hashCode();
        this.numberOfSequences = alignment.getSequenceCount();
    }

    /**
     * Checks if an alignment matches the internal model alignment
     * 
     * @return alignment equality
     */
    public boolean checkAlignment(Alignment alignment) {
        if (alignment == null) {
            return false;
        }
        return (this.alignment == alignment.toString().hashCode() &&
                this.numberOfSequences == alignment.getSequenceCount());
    }

    /**
     * Gets the tree.
     * 
     * @return the tree
     */
    public Tree getTree() {
        return tree;
    }

    /**
     * Sets the tree.
     * 
     * @param tree the new tree
     */
    public void setTree(Tree tree) {
        this.tree = tree;
    }

    /**
     * Gets the command line.
     * 
     * @return the command line
     */
    public String[] getCommandLine() {
        return commandLine;
    }

    /**
     * Sets the command line.
     * 
     * @param commandLine the new command line
     */
    public void setCommandLine(String[] commandLine) {
        this.commandLine = commandLine;
    }

    /**
     * Gets the number of transition categories.
     * 
     * @return the number of transition categories
     */
    public int getNumberOfTransitionCategories() {
        return numOfTransCategories;
    }

    /**
     * Instantiates a new substitution model.
     * 
     * @param matrix the matrix name
     * @param distribution the distribution value
     * @param plusF consider observed frequencies
     * @param alignment the alignment
     * @param tree the tree
     * @param ncat the ncat
     */
    public Model(String matrix, int distribution, boolean plusF, Alignment alignment, Tree tree, int ncat) {

        if (distribution < 0 || distribution > 3) {
            throw new ProtTestInternalException("Distribution not supported " + distribution);
        }

        if (alignment == null) {
            throw new ProtTestInternalException("Null alignment");
        }

        this.matrix = matrix;
        this.distribution = distribution;
        this.plusF = plusF;
        this.alignment = alignment.toString().hashCode();
        this.numberOfSequences = alignment.getSequenceCount();
        this.tree = tree;
        this.lkState = new ModelEmptyLkState();

//		numBranches = 2*alignment.getSequenceCount() - 3;
        switch (distribution) {
            case DISTRIBUTION_UNIFORM:
                numOfTransCategories = 1;
                break;
            case DISTRIBUTION_INVARIABLE:
                numOfTransCategories = 2;
                break;
            case DISTRIBUTION_GAMMA:
                numOfTransCategories = ncat;
                break;
            case DISTRIBUTION_GAMMA_INV:
                numOfTransCategories = ncat;
                break;
            }

    }

    /**
     * Gets the number of parameters according with the distribution.
     * 
     * @return the distribution parameters according with the distribution
     */
    public int getDistributionParameters() {
        int value = -1;
        switch (distribution) {
            case DISTRIBUTION_UNIFORM:
                value = 0;
                break;
            case DISTRIBUTION_INVARIABLE:
                value = 1;
                break;
            case DISTRIBUTION_GAMMA:
                value = 1;
                break;
            case DISTRIBUTION_GAMMA_INV:
                value = 2;
                break;
            }
        return value;
    }

    /**
     * Gets the number of model parameters.
     * 
     * @return the number of model parameters
     */
    public abstract int getNumberOfModelParameters();

    /**
     * Gets the matrix name.
     * 
     * @return the matrix name
     */
    public String getMatrix() {
        return matrix;
    }

    /**
     * Gets the distribution.
     * 
     * @return the distribution
     */
    public int getDistribution() {
        return distribution;
    }

    /**
     * Checks if observed frequencies are considered.
     * 
     * @return true, if the model considers observed frequencies
     */
    public boolean isPlusF() {
        return plusF;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + distribution;
        result = prime * result + ((matrix == null) ? 0 : matrix.hashCode());
        result = prime * result + (isPlusF() ? 1231 : 1237);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Model other = (Model) obj;
        if (distribution != other.distribution) {
            return false;
        }
        if (matrix == null) {
            if (other.matrix != null) {
                return false;
            }
        } else if (!matrix.equals(other.matrix)) {
            return false;
        }
        if (isPlusF() != other.isPlusF()) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Model [distribution=" + distribution + ", matrix=" + matrix + ", plusF=" + isPlusF();
//		+ ", weight=" + weight + "]";
    }

    /**
     * Gets the complete model name.
     * 
     * @return the model name
     */
    public abstract String getModelName();

    /**
     * Gets the number of branches.
     * 
     * @return the number of branches
     */
    public int getNumBranches() {
        return 2 * numberOfSequences - 3;
    }

    /**
     * Checks if distribution is gamma.
     * 
     * @return true, if is gamma
     */
    public boolean isGamma() {
        return (distribution == DISTRIBUTION_GAMMA ||
                distribution == DISTRIBUTION_GAMMA_INV);
    }

    /**
     * Checks if a proportion of invariant sites are considered.
     * 
     * @return true, if considers a proportion of invariant sites
     */
    public boolean isInv() {
        return (distribution == DISTRIBUTION_INVARIABLE ||
                distribution == DISTRIBUTION_GAMMA_INV);
    }

    /**
     * Prints the model status report.
     */
    public void printReport() {

        println("Model................................ : " + getModelName());
        print("  Number of parameters............... : " + getNumberOfModelParameters());
        println(" (" + (getNumberOfModelParameters() - getNumBranches()) + " + " +
                getNumBranches() + " branch length estimates)");

        if (isComputed()) {
            if (isGamma()) {
                println("    gamma shape (" + getNumberOfTransitionCategories() + " rate categories).. = " + getAlpha());
            }
            if (isInv()) {
                println("    proportion of invariable sites... = " + getInv());
            }
            if (isPlusF()) {
                println("    aminoacid frequencies............ = observed (see above)");
            }
            print(" -lnL................................ = " + ProtTestFormattedOutput.getDecimalString((-1 * getLk()), 2));
            println("");

            verboseln("The tree:");
            verboseln("---------");
            StringWriter ascciiSw = new StringWriter();
            TreeUtils.report(getTree(), new PrintWriter(ascciiSw));
            ascciiSw.flush();
            verboseln(ascciiSw.toString());
            StringWriter newickSw = new StringWriter();
            verboseln("---------");
            TreeUtils.printNH(getTree(), new PrintWriter(newickSw));
            newickSw.flush();
            verboseln(newickSw.toString());
            verboseln("");
        }
        flush(Model.class);
    }

    private void print(String message) {
        info(message, Model.class);
    }

    private void println(String message) {
        infoln(message, Model.class);
    }

    private void verbose(String message) {
        finer(message, Model.class);
    }

    private void verboseln(String message) {
        finerln(message, Model.class);
    }
}

