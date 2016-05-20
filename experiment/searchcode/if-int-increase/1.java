package uk.ac.lkl.migen.mockup.polydials.expression;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

//import uk.ac.lkl.common.util.event.UpdateEvent;
//import uk.ac.lkl.common.util.event.UpdateListener;
import uk.ac.lkl.common.util.value.IntegerValue;
import uk.ac.lkl.common.util.value.VectorValue;
import uk.ac.lkl.migen.mockup.polydials.model.ModuloCounter;
//import uk.ac.lkl.migen.mockup.polydials.model.StepwiseAdjustable;

public class CounterIncreaseSet extends
	VectorValue<IntegerValue, CounterIncreaseSet> {

    public static final CounterIncreaseSet ZERO = new CounterIncreaseSet();

    private HashMap<ModuloCounter, Integer> increaseMap;

//    private UpdateListener<StepwiseAdjustable> counterListener =
//	    new UpdateListener<StepwiseAdjustable>() {
//		public void objectUpdated(UpdateEvent<StepwiseAdjustable> e) {
//		    processCounterUpdated();
//		}
//	    };

    public CounterIncreaseSet(ModuloCounter counter) {
	this(new CounterIncrease(counter, 1));
    }

    public CounterIncreaseSet(CounterIncrease... counterIncreases) {
	increaseMap = new HashMap<ModuloCounter, Integer>();
	for (CounterIncrease counterIncrease : counterIncreases) {
	    ModuloCounter counter = counterIncrease.getCounter();
	    int increase = counterIncrease.getIncrease();
	    setIncrease(counter, increase);

	}
    }

    public CounterIncreaseSet(CounterIncreaseSet counterIncreaseSet) {
	increaseMap = new HashMap<ModuloCounter, Integer>();
	for (Map.Entry<ModuloCounter, Integer> entry : counterIncreaseSet.increaseMap
		.entrySet()) {
	    ModuloCounter counter = entry.getKey();
	    int increase = entry.getValue();
	    setIncrease(counter, increase);
	}
    }

//    private void processCounterUpdated() {
//	fireObjectUpdated();
//    }
    
    // better name than 'increaseIncrease'!
    private void adjustIncrease(ModuloCounter counter, int adjustment) {
	int currentIncrease = getIncrease(counter);
	int newIncrease = currentIncrease + adjustment;
	setIncrease(counter, newIncrease);
    }

    // never stores 0 as the increase so that getCounters always returns those
    // counters with a non-zero coefficient
    private void setIncrease(ModuloCounter counter, int increase) {
	increase %= counter.getModulus();
	if (increase == 0)
	    increaseMap.remove(counter);
	else
	    increaseMap.put(counter, increase);
    }

    public void apply() {
	for (Map.Entry<ModuloCounter, Integer> entry : increaseMap.entrySet()) {
	    ModuloCounter counter = entry.getKey();
	    int increase = entry.getValue();
	    counter.increaseValue(increase);
	}
    }

    public int getIncrease(ModuloCounter counter) {
	Integer increase = increaseMap.get(counter);
	if (increase == null)
	    return 0;
	else
	    return increase;
    }

    // to satisfy Value interface. Needs clean up.
    public final boolean isValid() {
	return true;
    }

    public Class<? extends CounterIncreaseSet> getValueClass() {
	return this.getClass();
    }

    // returns ONLY counters with non-zero increases
    public Set<ModuloCounter> getCounters() {
	return increaseMap.keySet();
    }

    public final boolean isEqualTo(CounterIncreaseSet other) {
	Collection<ModuloCounter> thisCounters = this.getCounters();
	Collection<ModuloCounter> otherCounters = other.getCounters();
	if (thisCounters.size() != otherCounters.size())
	    return false;

	thisCounters.removeAll(otherCounters);
	return thisCounters.size() == 0;
    }

    public final CounterIncreaseSet add(CounterIncreaseSet other) {
	CounterIncreaseSet result = new CounterIncreaseSet(this);
	Set<ModuloCounter> otherCounters = other.getCounters();
	for (ModuloCounter counter : otherCounters) {
	    int adjustment = other.getIncrease(counter);
	    result.adjustIncrease(counter, adjustment);
	}
	return result;
    }

    public final CounterIncreaseSet negate() {
	CounterIncreaseSet result = new CounterIncreaseSet(this);
	Set<ModuloCounter> counters = result.getCounters();
	for (ModuloCounter counter : counters) {
	    int increase = result.getIncrease(counter);
	    result.setIncrease(counter, -increase);
	}
	return result;
    }

    public final CounterIncreaseSet subtract(CounterIncreaseSet other) {
	CounterIncreaseSet result = new CounterIncreaseSet(this);
	CounterIncreaseSet negatedOther = other.negate();
	return result.add(negatedOther);
    }

    public CounterIncreaseSet getZero() {
	return ZERO;
    }

    public CounterIncreaseSet scale(IntegerValue scalar) {
	CounterIncreaseSet result = new CounterIncreaseSet(this);
	// note: important to use 'this.getCounters()' since, as a result of the
	// setting of the increase, it may be deleted from the map if it is 0
	Set<ModuloCounter> counters = this.getCounters();
	for (ModuloCounter counter : counters) {
	    int currentIncrease = result.getIncrease(counter);
	    int newIncrease = scalar.getInt() * currentIncrease;
	    result.setIncrease(counter, newIncrease);
	}
	return result;
    }

    public CounterIncreaseSet absoluteValue() {
	CounterIncreaseSet result = new CounterIncreaseSet(this);
	Set<ModuloCounter> counters = result.getCounters();
	for (ModuloCounter counter : counters) {
	    int currentIncrease = result.getIncrease(counter);
	    int newIncrease = Math.abs(currentIncrease);
	    result.setIncrease(counter, newIncrease);
	}
	return result;
    }

    // fundamentally relies on only storing non-zero increases
    public boolean isZero() {
	Set<ModuloCounter> counters = getCounters();
	return counters.size() == 0;
    }

    public CounterIncreaseSet createCopy() {
	CounterIncreaseSet result = new CounterIncreaseSet(this);
	return result;
    }

    public String toString() {
	String result = "";
	for (Map.Entry<ModuloCounter, Integer> entry : increaseMap.entrySet()) {
	    ModuloCounter counter = entry.getKey();
	    int increase = entry.getValue();
	    result += counter.getId() + " --> " + increase;
	}
	return result;
    }
}

