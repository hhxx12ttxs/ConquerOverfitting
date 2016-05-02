/*******************************************************************************
 * Copyright (c) 2012 Danilo Pianini.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 ******************************************************************************/
/**
 * 
 */
package it.unibo.alchemist.model.implementations.reactions;

import it.unibo.alchemist.core.implementations.Simulation;
import it.unibo.alchemist.model.implementations.times.DoubleTime;
import it.unibo.alchemist.model.interfaces.Context;
import it.unibo.alchemist.model.interfaces.IEnvironment;
import it.unibo.alchemist.model.interfaces.INode;
import it.unibo.alchemist.model.interfaces.IReaction;
import it.unibo.alchemist.model.interfaces.ITime;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Danilo Pianini
 * @version 20110608
 * @param <T>
 *            The type which describes the concentration of a molecule
 * 
 *            This class offers a partial implementation of IReaction. In
 *            particular, it allows to write new reaction specifying only which
 *            distribution time to adopt
 * 
 */
public abstract class AReaction<T> implements IReaction<T> {

	private static final long serialVersionUID = 6454665278161217867L;
	private static final int CENTER = 0;
	private static final int MAX = 1073741824;
	private static final int MIN = -MAX;
	private static final AtomicInteger ID_GEN = new AtomicInteger();
	private static final AtomicInteger ODD = new AtomicInteger(1);
	private static final AtomicBoolean POSITIVE = new AtomicBoolean(true);
	private static final AtomicInteger POW = new AtomicInteger(1);
	private final int hash;
	private final ITime startTime;
	private Context incontext = Context.LOCAL, outcontext = Context.LOCAL;
	private double oldPropensity = -1;
	private boolean notSchedulable = true;
	private ITime tau = new DoubleTime();

	/**
	 * Builds a new reaction, starting at time t.
	 * 
	 * @param t
	 *            the time since this reaction can be scheduled
	 */
	public AReaction(final ITime t) {
		final int id = ID_GEN.getAndIncrement();
		if (id == 0) {
			hash = CENTER;
		} else {
			final boolean positive = POSITIVE.get();
			final int val = positive ? MAX : MIN;
			final int pow = POW.get();
			final int odd = ODD.get();
			hash = val / pow * odd;
			if (!positive) {
				if (odd + 2 > pow) {
					POW.set(pow * 2);
					ODD.set(1);
				} else {
					ODD.set(odd + 2);
				}
			}
			POSITIVE.set(!positive);
		}
		startTime = t;
		tau = t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(final IReaction<T> o) {
		return tau.compareTo(o.getTau());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(final Object o) {
		if (o instanceof AReaction) {
			return ((AReaction<?>) o).hash == hash;
		}
		return false;
	}

	/**
	 * @param propensity
	 *            the current propensity for the reaction
	 * @return the next occurrence time for the reaction, in case this is the
	 *         reaction which have been executed.
	 */
	protected abstract ITime genTime(double propensity);

	/*
	 * (non-Javadoc)
	 * 
	 * @see alice.alchemist.model.interfaces.IReaction#getInputContext()
	 */
	@Override
	public Context getInputContext() {
		return incontext;
	}

	/**
	 * @return the previous propensity value
	 */
	protected double getOldPropensity() {
		return oldPropensity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see alice.alchemist.model.interfaces.IReaction#getOutputContext()
	 */
	@Override
	public Context getOutputContext() {
		return outcontext;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see alice.kemio.model.interfaces.IReaction#getTau()
	 */
	@Override
	public ITime getTau() {
		return tau;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode() {
		return hash;
	}

	/**
	 * Removes this reaction from the environment and the Simulation.
	 * 
	 * @param env
	 *            the current environment
	 * @param node
	 *            the node in which this reaction is
	 */
	protected void removeReaction(final IEnvironment<?, ?, T> env, final INode<T> node) {
		node.removeReaction(this);
		Simulation.removeReaction(env, this);
	}

	/**
	 * Used by sublcasses to set their input context.
	 * 
	 * @param c
	 *            the new input context
	 */
	protected void setInputContext(final Context c) {
		incontext = c;
	}

	/**
	 * Allows subclasses to set the old propensity value. Use with care.
	 * 
	 * @param oldProp
	 *            the new value for the old propensity
	 */
	protected void setOldPropensity(final double oldProp) {
		oldPropensity = oldProp;
	}

	/**
	 * Used by sublcasses to set their output context.
	 * 
	 * @param c
	 *            the new input context
	 */
	protected void setOutputContext(final Context c) {
		outcontext = c;
	}

	/**
	 * Allows subclasses to set the next putative time. Use with care.
	 * 
	 * @param t
	 *            the new time
	 */
	protected void setTau(final ITime t) {
		this.tau = t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see alice.alchemist.model.interfaces.IReaction#update(boolean,
	 * alice.alchemist.model.interfaces.ITime)
	 */
	@Override
	public void update(final boolean isMu, final ITime curTime) {
		final double newpropensity = getPropensity();
		if (oldPropensity == 0 && newpropensity != 0) {
			update(newpropensity, true, curTime);
		} else if (oldPropensity != 0 && newpropensity != 0) {
			update(newpropensity, isMu, curTime);
		} else if (oldPropensity != 0 && newpropensity == 0) {
			setTau(DoubleTime.INFINITE_TIME);
		}
		oldPropensity = newpropensity;
	}

	private void update(final double newpropensity, final boolean isMu, final ITime curTime) {
		if (notSchedulable && curTime.compareTo(startTime) > 0) {
			/*
			 * If the simulation time is beyond the startTime for this reaction,
			 * it can start being scheduled normally.
			 */
			notSchedulable = false;
		}
		/*
		 * If the current time is not past the starting time for this reaction,
		 * it should not be used.
		 */
		final ITime actualCurrentTime = notSchedulable ? startTime : curTime;
		if (isMu) {
			final ITime dt = genTime(newpropensity);
			setTau(actualCurrentTime.sum(dt));
		} else {
			if (oldPropensity != newpropensity) {
				final ITime sub = tau.subtract(actualCurrentTime);
				final ITime mul = sub.multiply(oldPropensity / newpropensity);
				setTau(mul.sum(actualCurrentTime));
			}
		}
	}

}

