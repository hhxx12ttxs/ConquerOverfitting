<<<<<<< HEAD
/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2012 Eric Lafortune (eric@graphics.cornell.edu)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */
package proguard.evaluation.value;

/**
 * This abstract class represents a partially evaluated value.
 *
 * @author Eric Lafortune
 */
public abstract class Value
{
    public static final int NEVER  = -1;
    public static final int MAYBE  = 0;
    public static final int ALWAYS = 1;

    public static final int TYPE_INTEGER            = 1;
    public static final int TYPE_LONG               = 2;
    public static final int TYPE_FLOAT              = 3;
    public static final int TYPE_DOUBLE             = 4;
    public static final int TYPE_REFERENCE          = 5;
    public static final int TYPE_INSTRUCTION_OFFSET = 6;
    public static final int TYPE_TOP                = 7;


    /**
     * Returns this Value as a Category1Value.
     */
    public Category1Value category1Value()
    {
        throw new IllegalArgumentException("Value is not a Category 1 value [" + this.getClass().getName() + "]");
    }

    /**
     * Returns this Value as a Category2Value.
     */
    public Category2Value category2Value()
    {
        throw new IllegalArgumentException("Value is not a Category 2 value [" + this.getClass().getName() + "]");
    }


    /**
     * Returns this Value as an IntegerValue.
     */
    public IntegerValue integerValue()
    {
        throw new IllegalArgumentException("Value is not an integer value [" + this.getClass().getName() + "]");
    }

    /**
     * Returns this Value as a LongValue.
     */
    public LongValue longValue()
    {
        throw new IllegalArgumentException("Value is not a long value [" + this.getClass().getName() + "]");
    }

    /**
     * Returns this Value as a FloatValue.
     */
    public FloatValue floatValue()
    {
        throw new IllegalArgumentException("Value is not a float value [" + this.getClass().getName() + "]");
    }

    /**
     * Returns this Value as a DoubleValue.
     */
    public DoubleValue doubleValue()
    {
        throw new IllegalArgumentException("Value is not a double value [" + this.getClass().getName() + "]");
    }

    /**
     * Returns this Value as a ReferenceValue.
     */
    public ReferenceValue referenceValue()
    {
        throw new IllegalArgumentException("Value is not a reference value [" + this.getClass().getName() + "]");
    }

    /**
     * Returns this Value as an InstructionOffsetValue.
     */
    public InstructionOffsetValue instructionOffsetValue()
    {
        throw new IllegalArgumentException("Value is not an instruction offset value [" + this.getClass().getName() + "]");
    }


    /**
     * Returns whether this Value represents a single specific (but possibly
     * unknown) value.
     */
    public boolean isSpecific()
    {
        return false;
    }


    /**
     * Returns whether this Value represents a single particular (known)
     * value.
     */
    public boolean isParticular()
    {
        return false;
    }


    /**
     * Returns the generalization of this Value and the given other Value.
     */
    public abstract Value generalize(Value other);


    /**
     * Returns whether the computational type of this Value is a category 2 type.
     * This means that it takes up the space of two category 1 types on the
     * stack, for instance.
     */
    public abstract boolean isCategory2();


    /**
     * Returns the computational type of this Value.
     * @return <code>TYPE_INTEGER</code>,
     *         <code>TYPE_LONG</code>,
     *         <code>TYPE_FLOAT</code>,
     *         <code>TYPE_DOUBLE</code>,
     *         <code>TYPE_REFERENCE</code>, or
     *         <code>TYPE_INSTRUCTION_OFFSET</code>.
     */
    public abstract int computationalType();


    /**
     * Returns the internal type of this Value.
     * @return <code>ClassConstants.INTERNAL_TYPE_BOOLEAN</code>,
     *         <code>ClassConstants.INTERNAL_TYPE_BYTE</code>,
     *         <code>ClassConstants.INTERNAL_TYPE_CHAR</code>,
     *         <code>ClassConstants.INTERNAL_TYPE_SHORT</code>,
     *         <code>ClassConstants.INTERNAL_TYPE_INT</code>,
     *         <code>ClassConstants.INTERNAL_TYPE_LONG</code>,
     *         <code>ClassConstants.INTERNAL_TYPE_FLOAT</code>,
     *         <code>ClassConstants.INTERNAL_TYPE_DOUBLE</code>,
     *         <code>ClassConstants.INTERNAL_TYPE_CLASS_START ... ClassConstants.INTERNAL_TYPE_CLASS_END</code>, or
     *         an array type containing any of these types (always as String).
     */
    public abstract String internalType();
}

=======
package org.cpsolver.ifs.criteria;

import java.util.Collection;
import java.util.Set;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.model.ExtendedInfoProvider;
import org.cpsolver.ifs.model.Model;
import org.cpsolver.ifs.model.ModelListener;
import org.cpsolver.ifs.model.Value;
import org.cpsolver.ifs.model.Variable;


/**
 * Criterion. <br>
 * <br>
 * An optimization objective can be split into several (optimization) criteria
 * and modeled as a weighted sum of these. This makes the implementation of a particular problem
 * more versatile as it allows for an easier modification of the optimization objective.
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
public interface Criterion<V extends Variable<V, T>, T extends Value<V, T>> extends ModelListener<V, T>, ExtendedInfoProvider<V, T> {
    
    /** called when the criterion is added to a model
     * @param model problem model
     **/
    public void setModel(Model<V,T> model);
    
    /** Current value of the criterion (optimization objective)
     * @param assignment current assignment
     * @return value of this criterion
     **/
    public double getValue(Assignment<V, T> assignment);
    
    /**
     * Weighted value of the objectives.
     * Use {@link Criterion#getWeightedValue(Assignment)} instead.
     * @return weighted value of this criterion
     **/
    @Deprecated
    public double getWeightedValue();

    /** Weighted value of the objectives 
     * @param assignment current assignment 
     * @return weighted value of this criterion
     **/
    public double getWeightedValue(Assignment<V, T> assignment);
    
    /**
     * Bounds (minimum and maximum) estimate for the value.
     * Use {@link Criterion#getBounds(Assignment)} instead.
     * @return minimum and maximum of the criterion value
     **/
    @Deprecated
    public double[] getBounds();

    /** Bounds (minimum and maximum) estimate for the value 
     * @param assignment current assignment
     * @return minimum and maximum of the criterion value
     **/
    public double[] getBounds(Assignment<V, T> assignment);
    
    /** Weighted best value of the objective (value in the best solution). 
     * @return weighted value of this criterion in the best solution
     **/
    public double getWeightedBest();

    /** Best value (value of the criterion in the best solution)
     * @return value of this criterion in the best solution
     **/
    public double getBest();
    
    /** Weight of the criterion
     * @return criterion weight
     **/
    public double getWeight();
    
    /**
     * Weighted value of a proposed assignment (including hard conflicts).
     * Use {@link Criterion#getWeightedValue(Assignment, Value, Set)} instead.
     * @param value given value
     * @param conflicts values conflicting with the given value
     * @return weighted change in this criterion value when assigned
     **/
    @Deprecated
    public double getWeightedValue(T value, Set<T> conflicts);

    /** Weighted value of a proposed assignment (including hard conflicts)
     * @param assignment current assignment
     * @param value given value
     * @param conflicts values conflicting with the given value
     * @return weighted change in this criterion value when assigned
     **/
    public double getWeightedValue(Assignment<V, T> assignment, T value, Set<T> conflicts);
    
    /**
     * Value of a proposed assignment (including hard conflicts).
     * Use {@link Criterion#getValue(Assignment, Value, Set)} instead.
     * @param value given value
     * @param conflicts values conflicting with the given value
     * @return change in this criterion value when assigned
     **/
    @Deprecated
    public double getValue(T value, Set<T> conflicts);

    /** Value of a proposed assignment (including hard conflicts)
     * @param assignment current assignment
     * @param value given value
     * @param conflicts values conflicting with the given value
     * @return change in this criterion value when assigned
     **/
    public double getValue(Assignment<V, T> assignment, T value, Set<T> conflicts);
    
    /**
     * Weighted value of a part of the problem (given by the collection of variables)
     * Use {@link Criterion#getWeightedValue(Assignment, Collection)} instead.
     * @param variables list of problem variables
     * @return weighted value of the given variables
     **/
    @Deprecated
    public double getWeightedValue(Collection<V> variables);

    /** Weighted value of a part of the problem (given by the collection of variables)
     * @param assignment current assignment
     * @param variables list of problem variables
     * @return weighted value of the given variables
     **/
    public double getWeightedValue(Assignment<V, T> assignment, Collection<V> variables);
    
    /**
     * Value of a part of the problem (given by the collection of variables).
     * Use {@link Criterion#getValue(Assignment, Collection)} instead.
     * @param variables list of problem variables
     * @return value of the given variables
     **/
    @Deprecated
    public double getValue(Collection<V> variables);

    /** Value of a part of the problem (given by the collection of variables)
     * @param assignment current assignment
     * @param variables list of problem variables
     * @return value of the given variables
     **/
    public double getValue(Assignment<V, T> assignment, Collection<V> variables);
    
    /**
     * Value bounds (minimum and maximum) of the criterion on a part of the problem.
     * Use {@link Criterion#getBounds(Assignment, Collection)} instead.
     * @param variables list of problem variables
     * @return minimum and maximum of this criterion for the given sub-problem
     **/
    @Deprecated
    public double[] getBounds(Collection<V> variables);

    /** Value bounds (minimum and maximum) of the criterion on a part of the problem
     * @param assignment current assignment
     * @param variables list of problem variables
     * @return minimum and maximum of this criterion for the given sub-problem
     **/
    public double[] getBounds(Assignment<V, T> assignment, Collection<V> variables);
    
    /** Criterion name
     * @return name
     **/
    public String getName();
    
    /**
     * Outside update of the criterion (usefull when the criterion is driven by a set of constraints).
     * Use {@link Criterion#inc(Assignment, double)} instead.
     * @param value increment criterion by this value
     **/
    @Deprecated
    public void inc(double value);

    /** Outside update of the criterion (usefull when the criterion is driven by a set of constraints).
     * @param assignment current assignment
     * @param value increment criterion by this value
     **/
    public void inc(Assignment<V, T> assignment, double value);
    
    /** Notification that the current solution has been saved to the best.
     * @param assignment current assignment
     **/
    public void bestSaved(Assignment<V, T> assignment);

    /** Notification that the current solution has been restored from the best.
     * @param assignment current assignment
     **/
    public void bestRestored(Assignment<V, T> assignment);
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
