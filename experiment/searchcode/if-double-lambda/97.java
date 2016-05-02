package stream.data.graph.factor;

import java.io.Serializable;
import java.util.Random;
import java.util.Vector;

public class TableFactor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8585018904984571688L;

	private int MAX_DIM;

	private DiscreteDomain _args;
	public double[] _data;

	private static Random rng = new Random();

	/** Construct an empty table factor */
	public TableFactor(int MAX_DIM) {
		this.MAX_DIM = MAX_DIM;
		_data = new double[1];
	}

	/** Construct a table factor over the given domain */
	public TableFactor(int MAX_DIM, DiscreteDomain dom) {
		this.MAX_DIM = MAX_DIM;

		_args = new DiscreteDomain(MAX_DIM, dom);
		_data = new double[dom.size()];
	}

	/** Construct a copy */
	public TableFactor(int MAX_DIM, TableFactor other) {
		this.MAX_DIM = MAX_DIM;

		_args = new DiscreteDomain(MAX_DIM, other.getArgs());
		_data = new double[other._data.length];
		System.arraycopy(other._data, 0, _data, 0, _data.length);
	}

	public void setArgs(DiscreteDomain args) {
		_args = new DiscreteDomain(MAX_DIM, args);

		if (_data.length < args.size()) {
			double[] temp = new double[args.size()];
			System.arraycopy(_data, 0, temp, 0, _data.length);
			_data = temp;
		}
	}

	public DiscreteDomain getArgs() {
		return _args;
	}

	public double logP(int index) {
		// ASSERT_LT(index, _data.length);
		return _data[index];
	}

	public void setLogP(int index, double val) {
		// ASSERT_LT(index, _data.length); throw exception
		// while(index > _data.length+1) _data.add(0.0);
		_data[index] = val;
	}

	public void setLogP(DiscreteAssignment asg, double val) {
		if (asg.getArgs() == getArgs()) {
			int index = asg.getLinearIndex();
			// ASSERT_LT(index, _data.length); throw exception
			_data[index] = val;
		} else {
			DiscreteAssignment sub_asg = asg.restrict(_args);
			int index = sub_asg.getLinearIndex();
			// ASSERT_LT(index, _data.length); throw exception
			_data[index] = val;
		}
	}

	public double logP(DiscreteAssignment asg) {
		if (asg.getArgs() == getArgs()) {
			int index = asg.getLinearIndex();
			// ASSERT_LT(index, _data.length); throw exception
			return _data[index];
		} else {
			DiscreteAssignment sub_asg = asg.restrict(_args);
			int index = sub_asg.getLinearIndex();
			// ASSERT_LT(index, _data.length); throw exception
			return _data[index];
		}
	}

	public int size() {
		return _args.size();
	}

	public int getNumVars() {
		return _args.getNumVars();
	}

	public void zero() {
		java.util.Arrays.fill(_data, 0.0);
	}

	public void uniform() {
		java.util.Arrays.fill(_data, Math.log(1.0 / _data.length));
	}

	public void uniform(double value) {
		java.util.Arrays.fill(_data, value);
	}

	// ! ensure that sum_x this(x) = 1
	public void normalize() {
		// Compute the max value
		double max_value = logP(0);
		for (int i = 0; i < _data.length; ++i)
			max_value = Math.max(max_value, _data[i]);
		// scale and compute normalizing constant
		double Z = 0.0;
		for (int i = 0; i < _data.length; ++i) {
			_data[i] -= max_value;
			Z += Math.exp(_data[i]);
		}
		// //ASSERT( !std::isinf(Z) );
		// //ASSERT( !std::isnan(Z) );
		// //ASSERT( Z > 0.0);
		double logZ = Math.log(Z);
		// ASSERT_FALSE( std::isinf(logZ) );
		// ASSERT_FALSE( std::isnan(logZ) );
		// Normalize
		for (int i = 0; i < _data.length; ++i)
			_data[i] -= logZ;
	} // End of normalize

	/**
	 * Ensure that the largest value in log form is zero. This prevents
	 * overflows on normalization.
	 */
	public void shift_normalize() {
		// Compute the max value
		double max_value = logP(0);
		for (int i = 0; i < _data.length; ++i)
			max_value = Math.max(max_value, _data[i]);
		for (int i = 0; i < _data.length; ++i)
			_data[i] -= max_value;
	}

	/**
	 * Return false if any of the entries are not finite
	 */
	public boolean is_finite() {
		for (int i = 0; i < _data.length; ++i) {
			boolean is_inf = (_data[i] == Double.POSITIVE_INFINITY);
			boolean is_nan = (_data[i] == Double.NaN);
			if (is_inf || is_nan)
				return false;
		}
		return true;
	}

	// ! this(x) += other(x);
	public TableFactor add(TableFactor other) {
		if (getArgs() == other.getArgs()) {
			// ASSERT_EQ(_data.length, other._data.length);
			// More verctorizable version
			for (int i = 0; i < _data.length; ++i)
				_data[i] = Math.log(Math.exp(_data[i])
						+ Math.exp(other._data[i]));
		} else {
			for (DiscreteAssignment asg = getArgs().begin(); asg.lt(getArgs()
					.end()); asg.increment()) {
				setLogP(asg.getLinearIndex(),
						Math.log(Math.exp(logP(asg.getLinearIndex()))
								+ Math.exp(other.logP(asg))));
				// //ASSERT_FALSE(std::isinf( logP(asg.getLinearIndex()) ));
				// //ASSERT_FALSE(std::isnan( logP(asg.getLinearIndex()) ));
			}
		}
		return this;
	}

	// ! this(x) *= other(x);
	public TableFactor mult(TableFactor other) {
		if (getArgs() == other.getArgs()) {
			// ASSERT_EQ(_data.length, other._data.length);
			// More verctorizable version
			for (int i = 0; i < _data.length; ++i)
				_data[i] += other._data[i];
		} else {
			for (DiscreteAssignment asg = getArgs().begin(); asg.lt(getArgs()
					.end()); asg.increment()) {
				setLogP(asg.getLinearIndex(), logP(asg.getLinearIndex())
						+ other.logP(asg));
				// //ASSERT_FALSE(std::isinf( logP(asg.getLinearIndex()) ));
				// //ASSERT_FALSE(std::isnan( logP(asg.getLinearIndex()) ));
			}
		}
		return this;
	}

	// ! this(x) /= other(x);
	public TableFactor div(TableFactor other) {
		if (getArgs() == other.getArgs()) {
			// ASSERT_EQ(_data.length, other._data.length);
			// More verctorizable version
			for (int i = 0; i < _data.length; ++i)
				_data[i] -= other._data[i];
		} else {
			for (DiscreteAssignment asg = getArgs().begin(); asg.lt(getArgs()
					.end()); asg.increment()) {
				setLogP(asg.getLinearIndex(), logP(asg.getLinearIndex())
						- other.logP(asg));
				// //ASSERT_FALSE(std::isinf( logP(asg.getLinearIndex()) ));
				// //ASSERT_FALSE(std::isnan( logP(asg.getLinearIndex()) ));
			}
		}
		return this;
	}

	// Currently unused
	// ! this(x) = sum_y joint(x,y) * other(y)
	public void convolve(TableFactor joint, TableFactor other) {
		// ensure that both factors have the same domain
		// ASSERT_EQ(getArgs() + other.getArgs(), joint.getArgs()); throw
		// incompatible domains exception
		// Initialize the factor to zero so we can use it as an
		// accumulator
		uniform(0);
		for (DiscreteAssignment asg = joint.getArgs().begin(); asg.lt(joint
				.getArgs().end()); asg.increment()) {
			double value = Math.exp(joint.logP(asg.getLinearIndex())
					+ other.logP(asg));
			setLogP(asg, logP(asg) + value);
		}

		for (int i = 0; i < _data.length; ++i) {
			double sum = _data[i];
			// ASSERT_GE(sum, 0.0);
			if (sum == 0)
				sum = -Double.MAX_VALUE;
			else
				sum = Math.log(sum);
			_data[i] = sum;
		}
	}

	// ! this(x) = other(x, y = asg)
	public void condition(TableFactor other, DiscreteAssignment asg) {
		// ASSERT_EQ(getArgs(), other.getArgs() - asg.getArgs());

		// create a fast assignment starting from the '0' assignment
		// of getArgs() and the conditioning assignment of asg
		FastDiscreteAssignment fastyasg = new FastDiscreteAssignment(MAX_DIM,
				getArgs().begin().union(asg));
		// transpose the remaining assignments to the start
		fastyasg.transposeToStart(getArgs());

		for (DiscreteAssignment xasg = getArgs().begin(); xasg.lt(getArgs()
				.end()); xasg.increment()) {
			setLogP(xasg.getLinearIndex(),
					other.logP(fastyasg.getLinearIndex()));
			fastyasg.increment();
		}
	}

	// ! this(x) = this(x) other(x, y = asg)
	public void timesCondition(TableFactor other, DiscreteAssignment asg) {
		// //ASSERT(getArgs() == other.getArgs() - asg.getArgs());

		// create a fast assignment starting from the '0' assignment
		// of getArgs() and the conditioning assignment of asg
		FastDiscreteAssignment fastyasg = new FastDiscreteAssignment(MAX_DIM,
				getArgs().begin().union(asg));
		// transpose the remaining assignments to the start
		fastyasg.transposeToStart(getArgs());
		if (asg.getNumVars() == 0) {
			this.mult(other);
		} else {
			for (DiscreteAssignment xasg = getArgs().begin(); xasg.lt(getArgs()
					.end()); xasg.increment()) {
				setLogP(xasg.getLinearIndex(), logP(xasg.getLinearIndex())
						+ other.logP(fastyasg.getLinearIndex()));
				fastyasg.increment();
			}
		}
	}

	// ! this(x) = sum_y joint(x,y)
	public TableFactor marginalize(TableFactor joint) {
		// No need to marginalize
		if (getArgs() == joint.getArgs()) {
			// Just copy and return
			return new TableFactor(MAX_DIM, joint);
		}
		// Compute the domain to remove
		DiscreteDomain ydom = new DiscreteDomain(MAX_DIM, joint.getArgs())
				.sub(getArgs());
		// ASSERT_GT(ydom.getNumVars(), 0);

		FastDiscreteAssignment fastyasg = new FastDiscreteAssignment(MAX_DIM,
				joint.getArgs());
		fastyasg.transposeToStart(ydom);
		// count the number of elements in ydom
		int numel = 1;
		for (int i = 0; i < ydom.getNumVars(); ++i) {
			numel *= ydom.var(i).size();
		}
		// Loop over x
		for (DiscreteAssignment xasg = getArgs().begin(); xasg.lt(getArgs()
				.end()); xasg.increment()) {
			double sum = 0;
			for (int i = 0; i < numel; ++i) {
				sum += Math.exp(joint.logP(fastyasg.getLinearIndex()));
				fastyasg.increment();
			}
			// ASSERT_FALSE( std::isinf(sum) );
			// ASSERT_FALSE( std::isnan(sum) );
			// ASSERT_GE(sum, 0.0);
			if (sum == 0)
				setLogP(xasg.getLinearIndex(), -Double.MAX_VALUE);
			else
				setLogP(xasg.getLinearIndex(), Math.log(sum));
		}
		return this;
	}

	// ! This = other * damping + this * (1-damping)
	public void damp(TableFactor other, double damping) {
		// This factor must be over the same dimensions as the other
		// factor
		if (damping == 0)
			return;
		// ASSERT_EQ(getArgs(), other.getArgs());
		// ASSERT_GT(damping, 0.0);
		// ASSERT_LT(damping, 1.0);
		for (int i = 0; i < getArgs().size(); ++i) {
			double val = damping * Math.exp(other.logP(i)) + (1 - damping)
					* Math.exp(logP(i));
			// ASSERT_GE(val, 0);
			if (val == 0)
				setLogP(i, -Double.MAX_VALUE);
			else
				setLogP(i, Math.log(val));
			// ASSERT_FALSE( std::isinf(logP(i)) );
			// ASSERT_FALSE( std::isnan(logP(i)) );
		}
	}

	// ! compute the average l1 norm between to factors
	public double l1_diff(TableFactor other) {
		// This factor must be over the same dimensions as the other
		// factor
		// ASSERT_EQ(getArgs(), other.getArgs());
		double sum = 0;
		for (int i = 0; i < getArgs().size(); ++i) {
			sum += Math.abs(Math.exp(other.logP(i)) - Math.exp(logP(i)));
		}
		return sum / getArgs().size();
	}

	// ! compute the l1 norm in log space
	public double l1_logdiff(TableFactor other) {
		// ASSERT_EQ(getArgs(), other.getArgs());
		double sum = 0;
		for (int i = 0; i < getArgs().size(); ++i) {
			sum += Math.abs(other.logP(i) - logP(i));
		}
		return sum / getArgs().size();
	}

	public DiscreteAssignment max_asg() {
		DiscreteAssignment max_asg = getArgs().begin();
		double max_value = logP(max_asg.getLinearIndex());
		for (DiscreteAssignment asg = getArgs().begin(); asg
				.lt(getArgs().end()); asg.increment()) {
			if (logP(asg.getLinearIndex()) > max_value) {
				max_value = logP(asg.getLinearIndex());
				max_asg = new DiscreteAssignment(MAX_DIM, asg);
			}
		}
		return max_asg;
	}

	/**
	 * Compute the expectation of the table factor
	 */
	public void expectation(Vector<Double> values) {
		values.clear();
		values.ensureCapacity(getNumVars());
		double sum = 0;
		for (DiscreteAssignment asg = getArgs().begin(); asg
				.lt(getArgs().end()); asg.increment()) {
			double scale = Math.exp(logP(asg.getLinearIndex()));
			sum += scale;
			for (int i = 0; i < getNumVars(); ++i) {
				values.set(i, values.get(i) + asg.getAsgAt(i) * scale);
			}
		}
		// Rescale for normalization
		for (int i = 0; i < getNumVars(); ++i)
			values.set(i, values.get(i) / sum);
	} // end of expectation

	/**
	 * Draw a sample from the table factor
	 */
	public DiscreteAssignment sample() {
		// ASSERT_GT(size(), 0);
		// This factor must be normalized
		double t = rng.nextDouble();
		// ASSERT_GE( t, 0 );
		// ASSERT_LT( t, 1 );
		double sum = 0;
		for (int i = 0; i < _data.length; ++i) {
			sum += Math.exp(logP(i));
			if (t <= sum)
				return new DiscreteAssignment(MAX_DIM, getArgs(), i);
			// ASSERT_LT(sum, 1);
		}
		// Unreachable
		System.out.println("Invalid state reached in sample()");
		return new DiscreteAssignment(MAX_DIM);
	} // end of sample

	/**
	 * Construct a binary agreement factor
	 */
	void set_as_agreement(double lambda) {
		// ASSERT_EQ(getNumVars(), 2);
		for (DiscreteAssignment asg = getArgs().begin(); asg
				.lt(getArgs().end()); asg.increment()) {
			int diff = Math.abs(asg.asg(0) - asg.asg(1));
			if (diff > 0)
				setLogP(asg.getLinearIndex(), -lambda);
			else
				setLogP(asg.getLinearIndex(), 0);
		}
	} // end of set_as_agreement

	void set_as_laplace(double lambda) {
		// ASSERT_EQ(getNumVars(), 2);
		for (DiscreteAssignment asg = getArgs().begin(); asg
				.lt(getArgs().end()); asg.increment()) {
			int diff = Math.abs(asg.getAsgAt(0) - asg.getAsgAt(1));
			setLogP(asg.getLinearIndex(), -diff * lambda);
		}
	} // end of set_as_laplace

	public String toString() {
		String out = new String("Table Factor: ");
		out += getArgs() + "{\n";
		for (DiscreteAssignment asg = getArgs().begin(); asg
				.lt(getArgs().end()); asg.increment()) {
			out += "\tLogP(" + asg.toString() + ")="
					+ (new Double(logP(asg))).toString() + "\n";
		}
		return out + "}";
	}
}

