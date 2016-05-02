<<<<<<< HEAD
package org.cpsolver.ifs.criteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.context.AssignmentConstraintContext;
import org.cpsolver.ifs.assignment.context.AssignmentContext;
import org.cpsolver.ifs.assignment.context.AssignmentContextReference;
import org.cpsolver.ifs.assignment.context.CanHoldContext;
import org.cpsolver.ifs.assignment.context.ConstraintWithContext;
import org.cpsolver.ifs.assignment.context.HasAssignmentContext;
import org.cpsolver.ifs.model.Constraint;
import org.cpsolver.ifs.model.Model;
import org.cpsolver.ifs.model.Value;
import org.cpsolver.ifs.model.Variable;
import org.cpsolver.ifs.solver.Solver;
import org.cpsolver.ifs.util.DataProperties;


/**
 * Abstract Criterion. <br>
 * <br>
 * An optimization objective can be split into several (optimization) criteria
 * and modeled as a weighted sum of these. This makes the implementation of a particular problem
 * more versatile as it allows for an easier modification of the optimization objective.
 * <br>
 * This class implements most of the {@link Criterion} except of the {@link Criterion#getValue(Assignment, Value, Set)}.
 * 
 * @version IFS 1.3 (Iterative Forward Search)<br>
 *          Copyright (C) 2006 - 2014 Tomas Muller<br>
 *          <a href="mailto:muller@unitime.org">muller@unitime.org</a><br>
 *          <a href="http://muller.unitime.org">http://muller.unitime.org</a><br>
 * <br>
 *          This library is free software; you can redistribute it and/or modify
 *          it under the terms of the GNU Lesser General Public License as
 *          published by the Free Software Foundation; either version 3 of the
 *          License, or (at your option) any later version. <br>
 * <br>
 *          This library is distributed in the hope that it will be useful, but
 *          WITHOUT ANY WARRANTY; without even the implied warranty of
 *          MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *          Lesser General Public License for more details. <br>
 * <br>
 *          You should have received a copy of the GNU Lesser General Public
 *          License along with this library; if not see
 *          <a href='http://www.gnu.org/licenses/'>http://www.gnu.org/licenses/</a>.
 * @param <V> Variable
 * @param <T> Value
 */
public abstract class AbstractCriterion<V extends Variable<V, T>, T extends Value<V, T>> implements Criterion<V, T>, HasAssignmentContext<V, T, AbstractCriterion<V,T>.ValueContext>, CanHoldContext {
    private Model<V, T> iModel;
    protected double iBest = 0.0, iWeight = 0.0;
    protected static java.text.DecimalFormat sDoubleFormat = new java.text.DecimalFormat("0.##",
            new java.text.DecimalFormatSymbols(Locale.US));
    protected static java.text.DecimalFormat sPercentFormat = new java.text.DecimalFormat("0.##",
            new java.text.DecimalFormatSymbols(Locale.US));
    protected boolean iDebug = false;
    
    private AssignmentContextReference<V, T, ValueContext> iContextReference = null;
    private AssignmentContext[] iContext = null;
    private int iLastCacheId = 0;

    
    /**
     * Defines how the overall value of the criterion should be automatically updated (using {@link Criterion#getValue(Value, Set)}).
     */
    @SuppressWarnings("javadoc")
    protected static enum ValueUpdateType {
        /** Update is done before an unassignment (decrement) and before an assignment (increment). */
        BeforeUnassignedBeforeAssigned,
        /** Update is done after an unassignment (decrement) and before an assignment (increment). */
        AfterUnassignedBeforeAssigned,
        /** Update is done before an unassignment (decrement) and after an assignment (increment). */
        BeforeUnassignedAfterAssigned,
        /** Update is done after an unassignment (decrement) and after an assignment (increment). This is the default. */
        AfterUnassignedAfterAssigned,
        /** Criterion is to be updated manually (e.g., using {@link Criterion#inc(Assignment, double)}). */
        NoUpdate
    }
    protected ValueUpdateType iValueUpdateType = ValueUpdateType.BeforeUnassignedBeforeAssigned;

    /** Defines weight name (to be used to get the criterion weight from the configuration). 
     * @return name of the weight associated with this criterion
     **/
    public String getWeightName() {
        return "Weight." + getClass().getName().substring(1 + getClass().getName().lastIndexOf('.'));
    }
    
    /** Defines default weight (when {@link AbstractCriterion#getWeightName()} parameter is not present in the criterion).
     * @param config solver configuration
     * @return default criterion weight value
     **/
    public double getWeightDefault(DataProperties config) {
        return 0.0;
    }
    
    @Override
    public void setModel(Model<V,T> model) {
        iModel = model;
        if (model != null)
            iContextReference = model.createReference(this);
    }

    @Override
    public boolean init(Solver<V, T> solver) {
        iWeight = solver.getProperties().getPropertyDouble(getWeightName(), getWeightDefault(solver.getProperties()));
        iDebug = solver.getProperties().getPropertyBoolean(
                "Debug." + getClass().getName().substring(1 + getClass().getName().lastIndexOf('.')),
                solver.getProperties().getPropertyBoolean("Debug.Criterion", false));
        return true;
    }
    
    /**
     * Returns current model
     * @return problem model
     **/
    public Model<V, T> getModel() { return iModel; }
    
    /**
     * Returns an assignment context associated with this criterion. If there is no 
     * assignment context associated with this criterion yet, one is created using the
     * {@link ConstraintWithContext#createAssignmentContext(Assignment)} method. From that time on,
     * this context is kept with the assignment and automatically updated by calling the
     * {@link AssignmentConstraintContext#assigned(Assignment, Value)} and {@link AssignmentConstraintContext#unassigned(Assignment, Value)}
     * whenever a variable is changed as given by the {@link ValueUpdateType}.
     * @param assignment given assignment
     * @return assignment context associated with this constraint and the given assignment
     */
    @SuppressWarnings("unchecked")
    @Override
    public ValueContext getContext(Assignment<V, T> assignment) {
        if (iContext != null && assignment.getIndex() >= 0 && assignment.getIndex() < iContext.length) {
            AssignmentContext c = iContext[assignment.getIndex()];
            if (c != null) return (ValueContext) c;
        }
        return assignment.getAssignmentContext(getAssignmentContextReference());
    }
    
    @Override
    public ValueContext createAssignmentContext(Assignment<V,T> assignment) {
        return new ValueContext(assignment);
    }

    @Override
    public AssignmentContextReference<V, T, ValueContext> getAssignmentContextReference() { return iContextReference; }

    @Override
    public void setAssignmentContextReference(AssignmentContextReference<V, T, ValueContext> reference) { iContextReference = reference; }

    @Override
    public AssignmentContext[] getContext() {
        return iContext;
    }

    @Override
    public void setContext(AssignmentContext[] context) {
        iContext = context;
    }
    
    @Override
    public double getValue(Assignment<V, T> assignment) {
        return getContext(assignment).getTotal();
    }
    
    @Override
    public double getBest() {
        return iBest;
    }
    
    @Override
    public double getValue(Assignment<V, T> assignment, Collection<V> variables) {
        double ret = 0;
        for (V v: variables) {
            T t = assignment.getValue(v);
            if (t != null) ret += getValue(assignment, t, null);
        }
        return ret;
    }

    
    @Override
    public double getWeight() {
        return iWeight;
    }
    
    @Override
    public double getWeightedBest() {
        return getWeight() == 0.0 ? 0.0 : getWeight() * getBest();
    }
    
    @Override
    public double getWeightedValue(Assignment<V, T> assignment) {
        return (getWeight() == 0.0 ? 0.0 : getWeight() * getValue(assignment));
    }
    
    @Override
    public double getWeightedValue(Assignment<V, T> assignment, T value, Set<T> conflicts) {
        return (getWeight() == 0.0 ? 0.0 : getWeight() * getValue(assignment, value, conflicts));
    }
    
    @Override
    public double getWeightedValue(Assignment<V, T> assignment, Collection<V> variables) {
        return (getWeight() == 0.0 ? 0.0 : getWeight() * getValue(assignment, variables));
    }

    /** Compute bounds (bounds are being cached by default). 
     * @param assignment current assignment
     * @return minimum and maximum of this criterion's value
     **/
    protected double[] computeBounds(Assignment<V, T> assignment) {
        return getBounds(assignment, new ArrayList<V>(getModel().variables()));
    }

    @Override
    public double[] getBounds(Assignment<V, T> assignment) {
        return getContext(assignment).getBounds(assignment);
    }

    @Override
    public double[] getBounds(Assignment<V, T> assignment, Collection<V> variables) {
        double[] bounds = new double[] { 0.0, 0.0 };
        for (V v: variables) {
            Double min = null, max = null;
            for (T t: v.values()) {
                double value = getValue(assignment, t, null);
                if (min == null) { min = value; max = value; continue; }
                min = Math.min(min, value);
                max = Math.max(max, value);
            }
            if (min != null) {
                bounds[0] += min;
                bounds[1] += max;
            }
        }
        return bounds;
    }

    @Override
    public void beforeAssigned(Assignment<V, T> assignment, long iteration, T value) {
        switch (iValueUpdateType) {
            case AfterUnassignedBeforeAssigned:
            case BeforeUnassignedBeforeAssigned:
                getContext(assignment).assigned(assignment, value);
        }
    }

    @Override
    public void afterAssigned(Assignment<V, T> assignment, long iteration, T value) {
        switch (iValueUpdateType) {
            case AfterUnassignedAfterAssigned:
            case BeforeUnassignedAfterAssigned:
                getContext(assignment).assigned(assignment, value);
        }
    }

    @Override
    public void beforeUnassigned(Assignment<V, T> assignment, long iteration, T value) {
        switch (iValueUpdateType) {
            case BeforeUnassignedAfterAssigned:
            case BeforeUnassignedBeforeAssigned:
                getContext(assignment).unassigned(assignment, value);
        }
    }

    @Override
    public void afterUnassigned(Assignment<V, T> assignment, long iteration, T value) {
        switch (iValueUpdateType) {
            case AfterUnassignedAfterAssigned:
            case AfterUnassignedBeforeAssigned:
                getContext(assignment).unassigned(assignment, value);
        }
    }

    @Override
    public void bestSaved(Assignment<V, T> assignment) {
        iBest = getContext(assignment).getTotal();
    }

    @Override
    public void bestRestored(Assignment<V, T> assignment) {
        getContext(assignment).setTotal(iBest);
    }
    
    @Override
    public void inc(Assignment<V, T> assignment, double value) {
        getContext(assignment).inc(value);
    }   

    @Override
    public String getName() {
        return getClass().getName().substring(1 + getClass().getName().lastIndexOf('.')).replaceAll("(?<=[^A-Z])([A-Z])"," $1");
    }
    
    /** Clear bounds cache */
    protected void clearCache() {
        iLastCacheId++;
    }
    
    @Override
    public void variableAdded(V variable) {
        clearCache();
    }
    
    @Override
    public void variableRemoved(V variable) {
        clearCache();
    }
    
    @Override
    public void constraintAdded(Constraint<V, T> constraint) {
        clearCache();
    }
    
    @Override
    public void constraintRemoved(Constraint<V, T> constraint) {
        clearCache();
    }
    
    protected String getPerc(double value, double min, double max) {
        if (max == min)
            return sPercentFormat.format(100.0);
        return sPercentFormat.format(100.0 - 100.0 * (value - min) / (max - min));
    }

    protected String getPercRev(double value, double min, double max) {
        if (max == min)
            return sPercentFormat.format(0.0);
        return sPercentFormat.format(100.0 * (value - min) / (max - min));
    }

    @Override
    public void getInfo(Assignment<V, T> assignment, Map<String, String> info) {
    }
    
    @Override
    public void getInfo(Assignment<V, T> assignment, Map<String, String> info, Collection<V> variables) {
    }
    
    @Override
    public void getExtendedInfo(Assignment<V, T> assignment, Map<String, String> info) {
        if (iDebug) {
            double val = getValue(assignment), w = getWeightedValue(assignment), prec = getValue(assignment, getModel().variables());
            double[] bounds = getBounds(assignment);
            if (bounds[0] <= val && val <= bounds[1] && bounds[0] < bounds[1])
                info.put("[C] " + getName(),
                        getPerc(val, bounds[0], bounds[1]) + "% (value: " + sDoubleFormat.format(val) +
                        (Math.abs(prec - val) > 0.0001 ? ", precise:" + sDoubleFormat.format(prec) : "") +
                        ", weighted:" + sDoubleFormat.format(w) +
                        ", bounds: " + sDoubleFormat.format(bounds[0]) + ".." + sDoubleFormat.format(bounds[1]) + ")");
            else if (bounds[1] <= val && val <= bounds[0] && bounds[1] < bounds[0])
                info.put("[C] " + getName(),
                        getPercRev(val, bounds[1], bounds[0]) + "% (value: " + sDoubleFormat.format(val) +
                        (Math.abs(prec - val) > 0.0001 ? ", precise:" + sDoubleFormat.format(prec) : "") +
                        ", weighted:" + sDoubleFormat.format(w) +
                        ", bounds: " + sDoubleFormat.format(bounds[1]) + ".." + sDoubleFormat.format(bounds[0]) + ")");
            else if (bounds[0] != val || val != bounds[1])
                info.put("[C] " + getName(),
                        sDoubleFormat.format(val) + " (" +
                        (Math.abs(prec - val) > 0.0001 ? "precise:" + sDoubleFormat.format(prec) + ", ": "") +
                        "weighted:" + sDoubleFormat.format(w) +
                        (bounds[0] != bounds[1] ? ", bounds: " + sDoubleFormat.format(bounds[0]) + ".." + sDoubleFormat.format(bounds[1]) : "") +
                        ")");
        }        
    }
    
    /**
     * Assignment context holding current value and the cached bounds.
     */
    public class ValueContext implements AssignmentContext {
        protected double iTotal = 0.0;
        private double[] iBounds = null;
        private int iCacheId = -1;

        /** Create from an assignment 
         * @param assignment current assignment
         **/
        protected ValueContext(Assignment<V, T> assignment) {
            if (iValueUpdateType != ValueUpdateType.NoUpdate)
                iTotal = AbstractCriterion.this.getValue(assignment, getModel().variables());
        }
        
        protected ValueContext() {}
        
        /** Update value when unassigned
         * @param assignment current assignment
         * @param value recently unassigned value
         **/
        protected void unassigned(Assignment<V, T> assignment, T value) {
            iTotal -= getValue(assignment, value, null);
        }
        
        /** Update value when assigned 
         * @param assignment current assignment
         * @param value recently assigned value
         **/
        protected void assigned(Assignment<V, T> assignment, T value) {
            iTotal += getValue(assignment, value, null);
        }

        /** Return value 
         * @return current value of the criterion
         **/
        public double getTotal() { return iTotal; }
        
        /** Set value
         * @param value current value of the criterion
         **/
        public void setTotal(double value) { iTotal = value; }
        
        /** Increment value
         * @param value increment
         **/
        public void inc(double value) { iTotal += value; }
        
        /** Return bounds 
         * @param assignment current assignment 
         * @return minimum and maximum of this criterion's value
         **/
        protected double[] getBounds(Assignment<V, T> assignment) {
            if (iBounds == null || iCacheId < iLastCacheId) {
                iCacheId = iLastCacheId;
                iBounds = computeBounds(assignment);
            }
            return (iBounds == null ? new double[] {0.0, 0.0} : iBounds);
        }
        
        /** Set bounds 
         * @param bounds bounds to cache
         **/
        protected void setBounds(double[] bounds) {
            iBounds = bounds;
        }
    }

    @Override
    @Deprecated
    public double getWeightedValue() {
        return getWeightedValue(getModel().getDefaultAssignment());
    }

    @Override
    @Deprecated
    public double[] getBounds() {
        return getBounds(getModel().getDefaultAssignment());
    }

    @Override
    @Deprecated
    public double getWeightedValue(T value, Set<T> conflicts) {
        return getWeightedValue(getModel().getDefaultAssignment(), value, conflicts);
    }
    
    @Override
    @Deprecated
    public double getValue(T value, Set<T> conflicts) {
        return getValue(getModel().getDefaultAssignment(), value, conflicts);
    }

    @Override
    @Deprecated
    public double getWeightedValue(Collection<V> variables) {
        return getWeightedValue(getModel().getDefaultAssignment(), variables);
    }

    @Override
    @Deprecated
    public double getValue(Collection<V> variables) {
        return getValue(getModel().getDefaultAssignment(), variables);
    }

    @Override
    @Deprecated
    public double[] getBounds(Collection<V> variables) {
        return getBounds(getModel().getDefaultAssignment(), variables);
    }
    
    @Override
    @Deprecated
    public void inc(double value) {
        inc(getModel().getDefaultAssignment(), value);
    }
=======
/*
 * $RCSfile$
 *
 * Copyright 1998-2008 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 * $Revision: 127 $
 * $Date: 2008-02-28 15:18:51 -0500 (Thu, 28 Feb 2008) $
 * $State$
 */

package javax.vecmath;

import java.lang.Math;

/**
 * A generic 2-element tuple that is represented by double-precision  
 * floating point x,y coordinates.
 *
 */
public abstract class Tuple2d implements java.io.Serializable, Cloneable {

    static final long serialVersionUID = 6205762482756093838L;

    /**
     * The x coordinate.
     */
    public	double	x;

    /**
     * The y coordinate.
     */
    public	double	y;


    /**
     * Constructs and initializes a Tuple2d from the specified xy coordinates.
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Tuple2d(double x, double y)
    {
	this.x = x;
	this.y = y;
    }


    /**
     * Constructs and initializes a Tuple2d from the specified array.
     * @param t the array of length 2 containing xy in order
     */
    public Tuple2d(double[] t)
    {
	this.x = t[0];
	this.y = t[1];
    }


    /**
     * Constructs and initializes a Tuple2d from the specified Tuple2d.
     * @param t1 the Tuple2d containing the initialization x y data
     */
    public Tuple2d(Tuple2d t1)
    {
	this.x = t1.x;
	this.y = t1.y;
    }


    /**
     * Constructs and initializes a Tuple2d from the specified Tuple2f.
     * @param t1 the Tuple2f containing the initialization x y data
     */
    public Tuple2d(Tuple2f t1)
    {
	this.x = (double) t1.x;
	this.y = (double) t1.y;
    }

    /**
     * Constructs and initializes a Tuple2d to (0,0).
     */
    public Tuple2d()
    {
	this.x = 0.0;
	this.y = 0.0;
    }


    /**
     * Sets the value of this tuple to the specified xy coordinates.
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public final void set(double x, double y)
    {
	this.x = x;
	this.y = y;
    }


    /**
     * Sets the value of this tuple from the 2 values specified in 
     * the array.
     * @param t the array of length 2 containing xy in order
     */
    public final void set(double[] t)
    {
	this.x = t[0];
	this.y = t[1];
    }


    /**
     * Sets the value of this tuple to the value of the Tuple2d argument.
     * @param t1 the tuple to be copied
     */
    public final void set(Tuple2d t1)
    {
	this.x = t1.x;
	this.y = t1.y;
    }
 

    /**
     * Sets the value of this tuple to the value of Tuple2f t1.
     * @param t1 the tuple to be copied
     */
    public final void set(Tuple2f t1)
    {
	this.x = (double) t1.x;
	this.y = (double) t1.y;
    }

   /**
    *  Copies the value of the elements of this tuple into the array t.
    *  @param t the array that will contain the values of the vector
    */
   public final void get(double[] t)
    {
        t[0] = this.x;
        t[1] = this.y;
    }


    /**
     * Sets the value of this tuple to the vector sum of tuples t1 and t2.
     * @param t1 the first tuple
     * @param t2 the second tuple
     */
    public final void add(Tuple2d t1, Tuple2d t2)
    {
	this.x = t1.x + t2.x;
	this.y = t1.y + t2.y;
    }


    /**
     * Sets the value of this tuple to the vector sum of itself and tuple t1.
     * @param t1 the other tuple
     */  
    public final void add(Tuple2d t1)
    {
        this.x += t1.x;
        this.y += t1.y;
    }


    /**
     * Sets the value of this tuple to the vector difference of 
     * tuple t1 and t2 (this = t1 - t2).    
     * @param t1 the first tuple
     * @param t2 the second tuple
     */  
    public final void sub(Tuple2d t1, Tuple2d t2)
    {
        this.x = t1.x - t2.x;
        this.y = t1.y - t2.y;
    }  


    /**
     * Sets the value of this tuple to the vector difference of
     * itself and tuple t1 (this = this - t1).
     * @param t1 the other vector
     */  
    public final void sub(Tuple2d t1)
    {
        this.x -= t1.x;
        this.y -= t1.y;
    }


    /**
     * Sets the value of this tuple to the negation of tuple t1.
     * @param t1 the source vector
     */
    public final void negate(Tuple2d t1)
    {
	this.x = -t1.x;
	this.y = -t1.y;
    }


    /**
     * Negates the value of this vector in place.
     */
    public final void negate()
    {
	this.x = -this.x;
	this.y = -this.y;
    }


    /**
     * Sets the value of this tuple to the scalar multiplication
     * of tuple t1.
     * @param s the scalar value
     * @param t1 the source tuple
     */
    public final void scale(double s, Tuple2d t1)
    {
	this.x = s*t1.x;
	this.y = s*t1.y;
    }


    /**
     * Sets the value of this tuple to the scalar multiplication
     * of itself.
     * @param s the scalar value
     */
    public final void scale(double s)
    {
	this.x *= s;
	this.y *= s;
    }


    /**
     * Sets the value of this tuple to the scalar multiplication
     * of tuple t1 and then adds tuple t2 (this = s*t1 + t2).
     * @param s the scalar value
     * @param t1 the tuple to be multipled
     * @param t2 the tuple to be added
     */  
    public final void scaleAdd(double s, Tuple2d t1, Tuple2d t2)
    {
        this.x = s*t1.x + t2.x; 
        this.y = s*t1.y + t2.y; 
    } 
 

    /**
     * Sets the value of this tuple to the scalar multiplication
     * of itself and then adds tuple t1 (this = s*this + t1).
     * @param s the scalar value
     * @param t1 the tuple to be added
     */
    public final void scaleAdd(double s, Tuple2d t1)
    {
        this.x = s*this.x + t1.x;
        this.y = s*this.y + t1.y;
    }



    /**
     * Returns a hash code value based on the data values in this
     * object.  Two different Tuple2d objects with identical data values
     * (i.e., Tuple2d.equals returns true) will return the same hash
     * code value.  Two objects with different data members may return the
     * same hash value, although this is not likely.
     * @return the integer hash code value
     */  
    public int hashCode() {
	long bits = 1L;
	bits = 31L * bits + VecMathUtil.doubleToLongBits(x);
	bits = 31L * bits + VecMathUtil.doubleToLongBits(y);
	return (int) (bits ^ (bits >> 32));
    }


   /**   
     * Returns true if all of the data members of Tuple2d t1 are
     * equal to the corresponding data members in this Tuple2d.
     * @param t1  the vector with which the comparison is made
     * @return  true or false
     */  
    public boolean equals(Tuple2d t1)
    {
        try {
           return(this.x == t1.x && this.y == t1.y);
        }
        catch (NullPointerException e2) {return false;}

    }

   /**   
     * Returns true if the Object t1 is of type Tuple2d and all of the
     * data members of t1 are equal to the corresponding data members in
     * this Tuple2d.
     * @param t1  the object with which the comparison is made
     * @return  true or false
     */  
    public boolean equals(Object t1)
    {
        try {
           Tuple2d t2 = (Tuple2d) t1;
           return(this.x == t2.x && this.y == t2.y);
        }
        catch (NullPointerException e2) {return false;}
        catch (ClassCastException   e1) {return false;}

    }

   /**
     * Returns true if the L-infinite distance between this tuple
     * and tuple t1 is less than or equal to the epsilon parameter, 
     * otherwise returns false.  The L-infinite
     * distance is equal to MAX[abs(x1-x2), abs(y1-y2)]. 
     * @param t1  the tuple to be compared to this tuple
     * @param epsilon  the threshold value  
     * @return  true or false
     */
    public boolean epsilonEquals(Tuple2d t1, double epsilon)
    {
       double diff;

       diff = x - t1.x;
       if(Double.isNaN(diff)) return false;
       if((diff<0?-diff:diff) > epsilon) return false;

       diff = y - t1.y;
       if(Double.isNaN(diff)) return false;
       if((diff<0?-diff:diff) > epsilon) return false;

       return true;
    }

   /**
     * Returns a string that contains the values of this Tuple2d.
     * The form is (x,y).
     * @return the String representation
     */  
   public String toString()
   {
        return("(" + this.x + ", " + this.y + ")");
   }


  /**
    *  Clamps the tuple parameter to the range [low, high] and 
    *  places the values into this tuple.  
    *  @param min   the lowest value in the tuple after clamping
    *  @param max  the highest value in the tuple after clamping 
    *  @param t   the source tuple, which will not be modified
    */
   public final void clamp(double min, double max, Tuple2d t)
   {
        if( t.x > max ) { 
          x = max;
        } else if( t.x < min ){
          x = min;
        } else {
          x = t.x;
        }

        if( t.y > max ) { 
          y = max;
        } else if( t.y < min ){
          y = min;
        } else {
          y = t.y;
        }

   }


  /** 
    *  Clamps the minimum value of the tuple parameter to the min 
    *  parameter and places the values into this tuple.
    *  @param min   the lowest value in the tuple after clamping 
    *  @param t   the source tuple, which will not be modified
    */   
   public final void clampMin(double min, Tuple2d t) 
   { 
        if( t.x < min ) { 
          x = min;
        } else {
          x = t.x;
        }

        if( t.y < min ) { 
          y = min;
        } else {
          y = t.y;
        }

   } 


  /**  
    *  Clamps the maximum value of the tuple parameter to the max 
    *  parameter and places the values into this tuple.
    *  @param max   the highest value in the tuple after clamping  
    *  @param t   the source tuple, which will not be modified
    */    
   public final void clampMax(double max, Tuple2d t)  
   {  
        if( t.x > max ) { 
          x = max;
        } else { 
          x = t.x;
        }
 
        if( t.y > max ) {
          y = max;
        } else {
          y = t.y;
        }

   } 


  /**  
    *  Sets each component of the tuple parameter to its absolute 
    *  value and places the modified values into this tuple.
    *  @param t   the source tuple, which will not be modified
    */    
  public final void absolute(Tuple2d t)
  {
       x = Math.abs(t.x);
       y = Math.abs(t.y);
  } 



  /**
    *  Clamps this tuple to the range [low, high].
    *  @param min  the lowest value in this tuple after clamping
    *  @param max  the highest value in this tuple after clamping
    */
   public final void clamp(double min, double max)
   {
        if( x > max ) {
          x = max;
        } else if( x < min ){
          x = min;
        }
 
        if( y > max ) {
          y = max;
        } else if( y < min ){
          y = min;
        }

   }

 
  /**
    *  Clamps the minimum value of this tuple to the min parameter.
    *  @param min   the lowest value in this tuple after clamping
    */
   public final void clampMin(double min)
   { 
      if( x < min ) x=min;
      if( y < min ) y=min;
   } 
 
 
  /**
    *  Clamps the maximum value of this tuple to the max parameter.
    *  @param max   the highest value in the tuple after clamping
    */
   public final void clampMax(double max)
   { 
      if( x > max ) x=max;
      if( y > max ) y=max;
   }


  /**
    *  Sets each component of this tuple to its absolute value.
    */
  public final void absolute()
  {
     x = Math.abs(x);
     y = Math.abs(y);
  }


  /** 
    *  Linearly interpolates between tuples t1 and t2 and places the 
    *  result into this tuple:  this = (1-alpha)*t1 + alpha*t2.
    *  @param t1  the first tuple
    *  @param t2  the second tuple
    *  @param alpha  the alpha interpolation parameter
    */
  public final void interpolate(Tuple2d t1, Tuple2d t2, double alpha)
  {
       this.x = (1-alpha)*t1.x + alpha*t2.x;
       this.y = (1-alpha)*t1.y + alpha*t2.y;
  }


  /**  
    *  Linearly interpolates between this tuple and tuple t1 and 
    *  places the result into this tuple:  this = (1-alpha)*this + alpha*t1.
    *  @param t1  the first tuple
    *  @param alpha  the alpha interpolation parameter  
    */   
  public final void interpolate(Tuple2d t1, double alpha) 
  { 
       this.x = (1-alpha)*this.x + alpha*t1.x;
       this.y = (1-alpha)*this.y + alpha*t1.y;

  } 

    /**
     * Creates a new object of the same class as this object.
     *
     * @return a clone of this instance.
     * @exception OutOfMemoryError if there is not enough memory.
     * @see java.lang.Cloneable
     * @since vecmath 1.3
     */
    public Object clone() {
	// Since there are no arrays we can just use Object.clone()
	try {
	    return super.clone();
	} catch (CloneNotSupportedException e) {
	    // this shouldn't happen, since we are Cloneable
	    throw new InternalError();
	}
    }


	/**
	 * Get the <i>x</i> coordinate.
	 * 
	 * @return the <i>x</i> coordinate.
	 * 
	 * @since vecmath 1.5
	 */
	public final double getX() {
		return x;
	}


	/**
	 * Set the <i>x</i> coordinate.
	 * 
	 * @param x  value to <i>x</i> coordinate.
	 * 
	 * @since vecmath 1.5
	 */
	public final void setX(double x) {
		this.x = x;
	}


	/**
	 * Get the <i>y</i> coordinate.
	 * 
	 * @return the <i>y</i> coordinate.
	 * 
	 * @since vecmath 1.5
	 */
	public final double getY() {
		return y;
	}


	/**
	 * Set the <i>y</i> coordinate.
	 * 
	 * @param y value to <i>y</i> coordinate.
	 * 
	 * @since vecmath 1.5
	 */
	public final void setY(double y) {
		this.y = y;
	}

>>>>>>> 76aa07461566a5976980e6696204781271955163
}

