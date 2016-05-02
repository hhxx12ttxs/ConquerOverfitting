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

/**
 * A special wrapper that efficiently store a result of a "fill" method call.
 */
class FillWrapper<T> extends AbstractWrapper<T> {

    private final T value;
    private final int size;

    public FillWrapper(T value, int size) {
        if (size < 0) {
            size = 0;
        }

        this.value = value;
        this.size = size;
    }

    @Override
    public Wrapper<T> copy() {
        return new FillWrapper<T>(value, size);
    }

    private static class FillIterator<T> implements Iterator<T> {

        private final T value;
        private int size;

        public FillIterator(T value, int size) {
            this.value = value;
            this.size = size;
        }

        @Override
        public boolean hasNext() {
            return size > 0;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            size--;
            return value;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("remove() not supported for FillWrapper");
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new FillIterator<T>(value, size);
    }

    @Override
    public Wrapper<T> repeat(int n) {
        if (n < 0) {
            n = 0;
        }

        return new FillWrapper<T>(value, size * n);
    }

    @Override
    public Wrapper<T> reverse() {
        return this;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public Wrapper<T> sort() {
        return this;
    }

    @Override
    public Wrapper<T> shuffle(Random random) {
        return this;
    }

    @Override
    public Wrapper<T> slice(int i, int j) {
        if (i >= 0 && j <= size) {
            return new FillWrapper<T>(value, j - i);
        } else {
            return this;
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object object) {
        if (object instanceof FillWrapper) {
            FillWrapper fillWrapper = (FillWrapper) object;
            return value.equals(fillWrapper.value) && size == fillWrapper.size;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash *= 41 + value.hashCode();
        hash *= 41 + size;
        return hash;
    }
}

