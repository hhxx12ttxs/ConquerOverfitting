/*
    This file is part of JSMAA.
    JSMAA is distributed from http://smaa.fi/.

    (c) Tommi Tervonen, 2009-2010.
    (c) Tommi Tervonen, Gert van Valkenhoef 2011.
    (c) Tommi Tervonen, Gert van Valkenhoef, Joel Kuiper, Daan Reid 2012.
    (c) Tommi Tervonen, Gert van Valkenhoef, Joel Kuiper, Daan Reid, Raymond Vermaas 2013.

    JSMAA is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    JSMAA is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with JSMAA.  If not, see <http://www.gnu.org/licenses/>.
*/
package fi.smaa.jsmaa.model;

import javolution.xml.XMLFormat;
import javolution.xml.stream.XMLStreamException;

import org.apache.commons.math3.distribution.BetaDistribution;

import fi.smaa.common.RandomUtil;

public class BetaMeasurement extends CardinalMeasurement {

	private static final long serialVersionUID = -7078702411067171274L;
	
	public final static String PROPERTY_ALPHA = "alpha";
	public final static String PROPERTY_BETA = "beta";
	public final static String PROPERTY_MIN = "min";
	public final static String PROPERTY_MAX = "max";
	
	private Double alpha;
	private Double beta;
	private Double min;
	private Double max;
		
	public BetaMeasurement(double alpha, double beta, double min, double max) {
		assert(alpha > 0.0);
		assert(beta > 0.0);
		assert(min <= max);
		
		this.alpha = alpha;
		this.beta = beta;
		this.min = min;
		this.max = max;
	}

	/**
	 * Creates a beta beta with a = 2, b = 2, min = 0, max = 1
	 */
	public BetaMeasurement() {
		alpha = 2.0;
		beta = 2.0;
		min = 0.0;
		max = 1.0;
	}

	public Double getMax() {
		return max;
	}

	public void setMax(Double max) {
		assert (max >= min);
		
		Object oldval = this.max;
		this.max = max;
		firePropertyChange(PROPERTY_MAX, oldval, this.max);		
	}
	
	public Double getMin() {
		return min;
	}

	public void setMin(Double min) {
		assert (min <= max);
		Object oldval = this.min;
		this.min = min;
		firePropertyChange(PROPERTY_MIN, oldval, this.min);				
	}
	
	public Double getAlpha() {
		return alpha;
	}	

	public void setAlpha(Double alpha) {
		assert (alpha > 0.0);
		
		Object oldval = this.alpha;
		this.alpha = alpha;
		firePropertyChange(PROPERTY_ALPHA, oldval, this.alpha);				
	}

	public Double getBeta() {
		return beta;
	}

	public void setBeta(Double beta) {
		assert(beta > 0.0);
		
		Object oldval = this.beta;
		this.beta = beta;
		firePropertyChange(PROPERTY_BETA, oldval, this.beta);				
	}	
	
	@Override
	public Interval getRange() {
		BetaDistribution dist = new BetaDistribution(alpha, beta);
		double lowEnd = convertToRange(dist.inverseCumulativeProbability(0.025));
		double highEnd = convertToRange(dist.inverseCumulativeProbability(0.975));
		return new Interval(lowEnd, highEnd);			
	}
	
	private double convertToRange(double val) {
		return (val + min) * (max - min);
	}

	@Override
	public double sample(RandomUtil random) {
		return random.createBeta(min, max, alpha, beta);
	}
	
	public BetaMeasurement deepCopy() {
		return new BetaMeasurement(alpha, beta, min, max);
	}
	
	@Override
	public String toString() {
		return "\u03B1: " + alpha + " \u03B2: " + beta + " min: " + min + " max: " + max; 
	}	

	@Override
	public boolean equals(Object other) {
		if (!(other.getClass().equals(BetaMeasurement.class))) {
			return false;
		}
		BetaMeasurement bo = (BetaMeasurement) other;
		return this.min.equals(bo.min) && this.max.equals(bo.max) && this.alpha.equals(bo.alpha) && this.beta.equals(bo.beta);
	}

	protected static final XMLFormat<BetaMeasurement> XML = new XMLFormat<BetaMeasurement>(BetaMeasurement.class) {
		@Override
		public BetaMeasurement newInstance(Class<BetaMeasurement> cls, InputElement ie) throws XMLStreamException {
			return new BetaMeasurement(ie.getAttribute("alpha").toDouble(),
					ie.getAttribute("beta").toDouble(),
					ie.getAttribute("min").toDouble(),
					ie.getAttribute("max").toDouble());
		}				
		@Override
		public boolean isReferenceable() {
			return false;
		}
		@Override
		public void read(InputElement ie, BetaMeasurement meas) throws XMLStreamException {
			meas.alpha = ie.getAttribute("alpha").toDouble();
			meas.beta = ie.getAttribute("beta").toDouble();
			meas.min = ie.getAttribute("min").toDouble();
			meas.max = ie.getAttribute("max").toDouble();
		}
		@Override
		public void write(BetaMeasurement meas, OutputElement oe) throws XMLStreamException {
			oe.setAttribute("alpha", meas.alpha);
			oe.setAttribute("beta", meas.beta);
			oe.setAttribute("min", meas.min);
			oe.setAttribute("max", meas.max);
		}
	};
}

