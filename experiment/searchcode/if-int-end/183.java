/**
 * Copyright (C) 2009 Mathieu Carbou <mathieu.carbou@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mycila.math.prime;

import com.mycila.math.list.IntSequence;
import com.mycila.math.list.ReadOnlySequenceIterator;
import com.mycila.math.number.BigInt;
import static com.mycila.math.number.BigInt.*;
import com.mycila.math.range.IntRange;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Iterator;

/**
 * @author Mathieu Carbou
 */
public final class Sieve {

    private static SoftReference<Sieve> CACHED_SIEVE = new SoftReference<Sieve>(Sieve.toInternal(10000));

    private final int[] primes;
    private final int sieveEnd;
    private final int sieveLength;
    private final Iterable<Integer> iterable = new Iterable<Integer>() {
        @Override
        public Iterator<Integer> iterator() {
            return ReadOnlySequenceIterator.on(IntRange.range(0, sieveLength - 1), primes);
        }
    };

    private Sieve(int sieveEnd, int[] primes, int sieveLength) {
        this.sieveLength = sieveLength;
        this.sieveEnd = sieveEnd;
        this.primes = primes;
    }

    /**
     * Grow the sieve up to given number
     *
     * @param number The number to extend the sieve to
     * @return A new extended sieve
     */
    public Sieve growTo(int number) {
        if ((number & 1) == 0) number--;
        if (sieveEnd >= number) return this;
        int maxNum = limit();
        if (number < maxNum) maxNum = number;
        int[] newPrimes = new int[Primes.getPiHighBound(number) - sieveLength];
        int newPrimesPos = 0;
        int p = primes[sieveLength - 1] + 2;
        for (; p <= maxNum; p += 2)
            if (PrimaltyTest.isPrime(p, primes, sieveLength))
                newPrimes[newPrimesPos++] = p;
        for (; p <= number; p += 2)
            if (PrimaltyTest.isPrime(p, primes, sieveLength)
                    && PrimaltyTest.isPrime(p, newPrimes, newPrimesPos))
                newPrimes[newPrimesPos++] = p;
        final int[] array = Arrays.copyOf(primes, sieveLength + newPrimesPos);
        System.arraycopy(newPrimes, 0, array, sieveLength, newPrimesPos);
        newPrimes = null; // free mem
        return new Sieve(number, array, array.length);
    }

    /**
     * Extend this Sieve to add more primes into.
     *
     * @param numberOfPrimesToAdd Number of primes to add to this sieve
     * @return The new extended sieve.
     */
    public Sieve grow(int numberOfPrimesToAdd) {
        int pos = sieveLength;
        final int max = pos + numberOfPrimesToAdd;
        final int[] newPrimes = Arrays.copyOf(primes, max);
        final int maxNum = limit();
        int p = primes[sieveLength - 1] + 2;
        for (; p < maxNum && pos < max; p += 2)
            if (PrimaltyTest.isPrime(p, primes, sieveLength))
                newPrimes[pos++] = p;
        for (p = maxNum + 2; pos < max; p += 2)
            if (PrimaltyTest.isPrime(p, newPrimes, pos))
                newPrimes[pos++] = p;
        return new Sieve(newPrimes[pos - 1], newPrimes, newPrimes.length);
    }

    private int limit() {
        final long tmp = (long) primes[sieveLength - 1] * (long) primes[sieveLength - 1];
        return tmp > Integer.MAX_VALUE || tmp < 0 ? Integer.MAX_VALUE : (int) tmp;
    }

    /**
     * Check wheter the number is prime.
     * <p/>
     * Implementation note:<br>
     * - if the number is contained in the sieve range, check if the sieve contains the number.<br>
     * - If number <= last()^2, check is some primes of this sieve divides the number
     * - Otherwise, if the number is greater than last()^2, the Miller-Rabin test is executed
     *
     * @param number Numberto check
     * @return True if it is prime
     */
    public boolean isPrime(int number) {
        if (number <= sieveEnd)
            return Arrays.binarySearch(primes, number) >= 0;
        if (number <= limit())
            return PrimaltyTest.isPrime(number, primes, sieveLength);
        return PrimaltyTest.millerRabin(number);
    }

    /**
     * Computes the <a href="http://en.wikipedia.org/wiki/Primorial">Primorial</a> for all primes in this sieve
     *
     * @return The product of all primes in this Sieve
     */
    public BigInt primorial() {
        return Primes.product(primes, 0, sieveLength);
    }

    /**
     * Computes the <a href="http://en.wikipedia.org/wiki/Primorial">Primorial</a> for all primes from this Sieve contained in the provided range.
     *
     * @param from The minimum number in the range
     * @param to   The maximum number in the range
     * @return The product of all primes in this Sieve matching given range
     */
    public BigInt primorial(int from, int to) {
        IntRange indexes = getIndexes(from, to);
        if (indexes.isEmpty() || indexes.length() == 0) return ONE;
        return Primes.product(primes, indexes.from, indexes.length());
    }

    /**
     * The number of prime in the sieve
     *
     * @return The number of prime in the sieve
     */
    public int size() {
        return sieveLength;
    }

    /**
     * The range of numbers used to create the sieve
     *
     * @return A range
     */
    public int sieveEnd() {
        return sieveEnd;
    }

    /**
     * Get a prime
     *
     * @param pos Prime index in the sieve
     * @return the prime
     */
    public int get(int pos) {
        return primes[pos];
    }

    /**
     * Check wheter the Sieve contains a prime
     *
     * @param prime The prime
     * @return True if the Sieve contains the prime
     */
    public boolean contains(int prime) {
        return Arrays.binarySearch(primes, 0, sieveLength, prime) >= 0;
    }

    /**
     * Get the first prime of this sieve
     *
     * @return the first prime of this sieve
     */
    public int first() {
        return primes[0];
    }

    /**
     * Get the last prime of this sieve
     *
     * @return the last prime of this sieve
     */
    public int last() {
        return primes[sieveLength - 1];
    }

    /**
     * Get the index of a prime
     *
     * @param prime Prime number
     * @return the position of the prime in the sieve, starting from 0, or -1 if not found
     */
    public int indexOf(int prime) {
        final int pos = Arrays.binarySearch(primes, 0, sieveLength, prime);
        return pos >= 0 ? pos : -1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sieve that = (Sieve) o;
        if (that.size() != this.size()) return false;
        for (int i = sieveLength; i-- > 0;)
            if (this.primes[i] != that.primes[i])
                return false;
        return true;
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (int i = sieveLength; i-- > 0;)
            h = 37 * h + 31 * primes[i];
        return h;
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder("{");
        for (int i = 0, end = sieveLength - 1; i < end; i++) {
            buf.append(primes[i]);
            buf.append(", ");
        }
        if (size() > 0) buf.append(primes[sieveLength - 1]);
        buf.append("}");
        return buf.toString();
    }

    /**
     * Returns an iterable over this prime to be used in for loops
     *
     * @return This Sieve's iterable
     */
    public Iterable<Integer> iterable() {
        return iterable;
    }

    /**
     * Returns an iterable over the primes of this Sieve which are included in the given range
     *
     * @param from The minimum number in the range
     * @param to   The maximum number in the range
     * @return An iterable to be used in for loops
     */
    public Iterable<Integer> iterable(final int from, final int to) {
        return new Iterable<Integer>() {
            @Override
            public Iterator<Integer> iterator() {
                return ReadOnlySequenceIterator.on(getIndexes(from, to), primes);
            }
        };
    }

    /**
     * Create an array from the prime numbers of this Sieve
     *
     * @return an arraycontaining all primes in order
     */
    public int[] asArray() {
        return Arrays.copyOf(primes, sieveLength);
    }

    /**
     * Create an array from the prime numbers of this Sieve which are included in given range
     *
     * @param from The minimum number in the range
     * @param to   The maximum number in the range
     * @return an array containing all primes in order which are contained in the range
     */
    public int[] asArray(int from, int to) {
        IntRange range = getIndexes(from, to);
        if (range.isEmpty()) return new int[0];
        int[] p = new int[range.length()];
        System.arraycopy(primes, range.from, p, 0, range.length());
        return p;
    }

    /**
     * Create an {@link com.mycila.math.list.IntSequence} from the prime numbers of this Sieve
     *
     * @return an {@link com.mycila.math.list.IntSequence} containing all primes in order
     */
    public IntSequence asSequence() {
        return new IntSequence(sieveLength).appendFrom(primes, 0, sieveLength);
    }

    /**
     * Create an {@link com.mycila.math.list.IntSequence} from the prime numbers of this Sieve which are included in given range
     *
     * @param from The minimum number in the range
     * @param to   The maximum number in the range
     * @return an {@link com.mycila.math.list.IntSequence} containing all primes in order which are contained in the range
     */
    public IntSequence asSequence(int from, int to) {
        IntRange range = getIndexes(from, to);
        if (range.isEmpty()) return new IntSequence(0);
        IntSequence seq = new IntSequence(range.length());
        seq.appendFrom(primes, range.from, range.length());
        return seq;
    }

    /**
     * From this sieve, create another sieve containing only primes in the range [1, max]
     *
     * @param max Maximum limit, which must be contained in this.range()
     * @return Another sieve containing primes in the range [1, max]
     */
    public Sieve subSieve(int max) {
        if (max > this.sieveEnd)
            throw new IllegalArgumentException(max + " is not included in this sieve range");
        int end = Arrays.binarySearch(primes, 0, sieveLength, max);
        if (end < 0) end = -end - 2;
        end++;
        return new Sieve(max, Arrays.copyOf(primes, end), end);
    }

    private IntRange getIndexes(int from, int to) {
        if (sieveLength == 0 || to < from) return IntRange.empty();
        int start = Arrays.binarySearch(primes, 0, sieveLength, from);
        if (start < 0) start = -start - 1;
        if (start >= sieveLength) return IntRange.range(sieveLength - 1, sieveLength - 1);
        int end = Arrays.binarySearch(primes, 0, sieveLength, to);
        if (end < 0) end = -end - 2;
        return end < start ? IntRange.empty() :
                end >= sieveLength ?
                        IntRange.range(start, sieveLength - 1) :
                        IntRange.range(start, end);
    }

    /**
     * Build a Sieve up to this number.
     *
     * @param max The maxmimu range for this sieve
     * @return The sieve
     */
    public static Sieve to(int max) {
        Sieve cached = CACHED_SIEVE.get();
        if (cached != null && max <= cached.sieveEnd())
            return cached.subSieve(max);
        if (max < 2) {
            cached = new Sieve(1, new int[0], -1);
            CACHED_SIEVE = new SoftReference<Sieve>(cached);
            return cached;
        }
        cached = toInternal(max);
        CACHED_SIEVE = new SoftReference<Sieve>(cached);
        return cached;
    }

    private static Sieve toInternal(int max) {
        BitSet composite = Primes.sieveOfEratosthenes(max);
        int approxim = Primes.getPiHighBound(max);
        // there are 105097565 primes <= Integer.MAX_VALUE (2147483647) and 2147483647 is the latest
        if (approxim > 105097565) approxim = 105097565;
        int[] primes = new int[approxim];
        boolean toggle = false;
        int p = 5, i = 0, j = 0;
        if (2 <= max) primes[j++] = 2;
        if (3 <= max) primes[j++] = 3;
        while (p > 0 && p <= max) {
            if (!composite.get(i++) && p <= max)
                primes[j++] = p;
            p += (toggle = !toggle) ? 2 : 4;
        }
        composite = null; //free mem
        return new Sieve(max, primes, j);
    }

    /**
     * Removes the cached sieve so that memory can be free by garbadge collector
     */
    public static void clearCachedSieve() {
        CACHED_SIEVE = null;
    }

}

