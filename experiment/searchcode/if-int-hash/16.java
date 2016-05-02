package com.humaorie.dollar;

/*
 * Dollar, http://bitbucket.org/dfa/dollar
 * (c) 2010, 2011 Davide Angelocola <davide.angelocola@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
import com.humaorie.dollar.Dollar.Wrapper;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

abstract class RandomWrapper<T> extends AbstractWrapper<T> implements Dollar.RandomWrapper<T> {

    protected Random random;
    protected int samples;

    public RandomWrapper(Random random, int samples) {
        generator(random);
        samples(samples);
    }

    @Override
    public final RandomWrapper<T> samples(int newSamples) {
        if (newSamples < 1) {
            newSamples = 1;
        }

        samples = newSamples;
        return this;
    }

    @Override
    public final Dollar.RandomWrapper<T> generator(Random random) {
        if (random == null) {
            random = new Random();
        }

        this.random = random;
        return this;
    }

    @Override
    public Wrapper<T> shuffle(Random random) {
        return this;
    }

    @Override
    public Wrapper<T> reverse() {
        return this;
    }

    @Override
    public Wrapper<T> slice(int i, int j) {
        if (i >= 0 && j < samples) {
            samples = j - i;
        }

        return this;
    }

    @Override
    public int size() {
        return samples;
    }
}

abstract class RandomIterator<T> implements Iterator<T> {

    private int n;

    public RandomIterator(int samples) {
        this.n = samples;
    }

    @Override
    public boolean hasNext() {
        return n > 0;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("remove() not supported for RandomWrapper");
    }

    // template
    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        n--;
        return nextRandom();
    }

    abstract T nextRandom();
}

class RandomIntegerWrapper extends RandomWrapper<Integer> {

    private final int upTo;

    public RandomIntegerWrapper(Random random, int samples, int upTo) {
        super(random, samples);
        this.upTo = upTo;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new RandomIterator<Integer>(samples) {

            @Override
            public Integer nextRandom() {
                return random.nextInt(upTo);
            }
        };
    }

    @Override
    public Wrapper<Integer> copy() {
        return new RandomIntegerWrapper(random, samples, upTo);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof RandomIntegerWrapper) {
            RandomIntegerWrapper randomIntegerWrapper = (RandomIntegerWrapper) object;
            return random == randomIntegerWrapper.random
                    && samples == randomIntegerWrapper.samples
                    && upTo == randomIntegerWrapper.upTo;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash *= 79 + random.hashCode();
        hash *= 79 + upTo;
        hash *= 79 + samples;
        return hash;
    }
}

class RandomLongWrapper extends RandomWrapper<Long> {

    private final long upTo;

    public RandomLongWrapper(Random random, int samples, long upTo) {
        super(random, samples);
        this.upTo = upTo;
    }

    @Override
    public Iterator<Long> iterator() {
        return new RandomIterator<Long>(samples) {

            @Override
            public Long nextRandom() {
                return random.nextLong() % upTo;
            }
        };
    }

    @Override
    public Wrapper<Long> copy() {
        return new RandomLongWrapper(random, samples, upTo);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof RandomLongWrapper) {
            RandomLongWrapper randomLongWrapper = (RandomLongWrapper) object;
            return random == randomLongWrapper.random
                    && samples == randomLongWrapper.samples
                    && upTo == randomLongWrapper.upTo;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash *= 79 + random.hashCode();
        hash *= 79 + upTo;
        hash *= 79 + samples;
        return hash;
    }
}

class RandomBooleanWrapper extends RandomWrapper<Boolean> {

    public RandomBooleanWrapper(Random random, int samples) {
        super(random, samples);
    }

    @Override
    public Iterator<Boolean> iterator() {
        return new RandomIterator<Boolean>(samples) {

            @Override
            public Boolean nextRandom() {
                return random.nextBoolean();
            }
        };
    }

    @Override
    public Wrapper<Boolean> copy() {
        return new RandomBooleanWrapper(random, samples);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof RandomBooleanWrapper) {
            RandomBooleanWrapper randomBooleanWrapper = (RandomBooleanWrapper) object;
            return random == randomBooleanWrapper.random
                    && samples == randomBooleanWrapper.samples;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 19;
        hash *= 79 + random.hashCode();
        hash *= 79 + samples;
        return hash;
    }
}

class RandomFloatWrapper extends RandomWrapper<Float> {

    private final float upTo;

    public RandomFloatWrapper(Random random, int samples, float upTo) {
        super(random, samples);
        this.upTo = upTo;
    }

    @Override
    public Iterator<Float> iterator() {
        return new RandomIterator<Float>(samples) {

            @Override
            public Float nextRandom() {
                return random.nextFloat() * upTo;
            }
        };
    }

    @Override
    public Wrapper<Float> copy() {
        return new RandomFloatWrapper(random, samples, upTo);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof RandomFloatWrapper) {
            RandomFloatWrapper randomFloatWrapper = (RandomFloatWrapper) object;
            return random == randomFloatWrapper.random
                    && samples == randomFloatWrapper.samples
                    && upTo == randomFloatWrapper.upTo;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 23;
        hash *= 79 + random.hashCode();
        hash *= 79 + upTo;
        hash *= 79 + samples;
        return hash;
    }
}

class RandomDoubleWrapper extends RandomWrapper<Double> {

    private final double upTo;

    public RandomDoubleWrapper(Random random, int samples, double upTo) {
        super(random, samples);
        this.upTo = upTo;
    }

    @Override
    public Iterator<Double> iterator() {
        return new RandomIterator<Double>(samples) {

            @Override
            public Double nextRandom() {
                return random.nextDouble() * upTo;
            }
        };
    }

    @Override
    public Wrapper<Double> copy() {
        return new RandomDoubleWrapper(random, samples, upTo);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof RandomDoubleWrapper) {
            RandomDoubleWrapper randomDoubleWrapper = (RandomDoubleWrapper) object;
            return random == randomDoubleWrapper.random
                    && samples == randomDoubleWrapper.samples
                    && upTo == randomDoubleWrapper.upTo;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 29;
        hash *= 79 + random.hashCode();
        hash *= 79 + upTo;
        hash *= 79 + samples;
        return hash;
    }
}

