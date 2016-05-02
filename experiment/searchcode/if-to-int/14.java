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
package euler;

import com.mycila.math.Digits;
import com.mycila.math.distribution.Distribution;
import com.mycila.math.distribution.Item;
import com.mycila.math.distribution.Maximum;
import com.mycila.math.list.IntSequence;
import com.mycila.math.prime.Sieve;
import static org.junit.Assert.*;

import static java.lang.System.*;
import java.util.HashMap;
import java.util.Map;

/**
 * http://projecteuler.net/index.php?section=problems&id=51
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class Problem051 {
    public static void main(String[] args) throws Exception {
        long time = currentTimeMillis();

        final int length = 6;
        final int sameDigits = 3;

        final Digits digits = Digits.base(10);

        // create a prime list containing only primes having N digits
        final Sieve primes = Sieve.to((int) Math.pow(10, length) - 1);
        final IntSequence range = primes.asSequence((int) Math.pow(10, length - 1), primes.sieveEnd());
        System.out.println(range);

        // build a list of patterns having same length
        final Distribution<String> patterns = Distribution.of(String.class);
        for (int i = 0, max = range.size(); i < max; i++) {
            final int prime = range.get(i);
            for (Integer item : digits.map(prime).itemsHavingCount(sameDigits)) {
                patterns.add(Integer.toString(prime).replaceAll(item.toString(), "x"));
            }
        }
        System.out.println(patterns);

        Maximum<String> maximum = Maximum.of(patterns);
        System.out.println(maximum);

        Map<String, IntSequence> map = new HashMap<String, IntSequence>(maximum.itemCount());
        for (Item<String> item : maximum) {
            IntSequence seq = new IntSequence(maximum.value());
            map.put(item.value(), seq);
            for (int digit = 0; digit <= 10; digit++) {
                final int number = Integer.parseInt(item.value().replaceAll("x", "" + digit));
                if (range.contains(number)) seq.add(number);
            }
            seq.sort();
        }

        IntSequence maxList = null;
        for (Map.Entry<String, IntSequence> entry : map.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
            if(maxList == null || maxList.size() < entry.getValue().size())
                maxList = entry.getValue();
        }

        out.println(maxList.first() + " in " + (currentTimeMillis() - time) + "ms");
        assertEquals(121313, maxList.first());
    }

}

/*
=> Outputs for length = 5 and sameDigits = 2:

MAXIMUM:
56xx3: 56993 56773 56663 56443 56333 56113 56003
in 930ms

=> Outputs for length = 5 and sameDigits = 3:

MAXIMUM:
9xxx7: 98887 97777 96667 94447 93337 92227 90007
in 504ms

=> Outputs for length = 6 and sameDigits = 2:

MAXIMUM:
7x99x1: 799991 779971 769961 749941 739931 719911 709901
2x52x1: 295291 285281 265261 255251 235231 225221 205201
6x50x9: 695099 675079 665069 645049 635039 615019 605009
in 3746ms

=> Outputs for length = 6 and sameDigits = 3:

MAXIMUM:
x2x3x3: 929393 828383 626363 525353 424343 323333 222323 121313
in 1846ms

*/

