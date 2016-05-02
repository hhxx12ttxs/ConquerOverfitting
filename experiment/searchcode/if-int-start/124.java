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
package com.mycila.math;

import com.mycila.math.list.LongSequence;

/**
 * @author Mathieu Carbou
 */
public final class Sequence {

    private Sequence() {
    }

    /**
     * Summarize all numbers from start to end
     *
     * @param start Starting number
     * @param end   Ending number
     * @return The sum from start to end
     */
    public static int sum(int start, int end) {
        return ((end - start + 1) * (start + end)) >>> 1;
    }

    /**
     * Computes the <a href="http://en.wikipedia.org/wiki/Collatz_conjecture">Collatz sequence</a>
     * starting from a given number
     *
     * @param number The starting number
     * @return The sequence
     */
    public static LongSequence collatz(long number) {
        final LongSequence sequence = new LongSequence();
        if (number > 0) {
            for (; number > 1; sequence.add(number), number = (number & 1) == 0 ?
                    number >>> 1 :
                    (number << 1) + number + 1)
                ;
            sequence.add(1L);
        }
        return sequence;
    }

}

