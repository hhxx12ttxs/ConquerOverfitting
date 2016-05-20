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

import com.mycila.math.Divisors;
import static org.junit.Assert.*;

/**
 * http://projecteuler.net/index.php?section=problems&id=33
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class Problem033 {
    public static void main(String[] args) throws Exception {
        final long time = System.currentTimeMillis();
        int N = 1;
        int D = 1;
        for (float n = 11; n <= 98; n++) {
            float n0 = n % 10;
            if (n0 != 0) {
                float n1 = (n - n0) / 10;
                for (float d = n + 1; d <= 99; d++) {
                    float d0 = d % 10;
                    if (d0 % 10 != 0) {
                        float d1 = (d - d0) / 10f;
                        if (n0 == d0 && n1 != d1 && n1 / d1 == n / d) {
                            System.out.println(n + "/" + d + "=" + n1 + "/" + d1);
                            N *= n1;
                            D *= d1;
                        } else if (n1 == d0 && n0 != d1 && n0 / d1 == n / d) {
                            System.out.println(n + "/" + d + "=" + n0 + "/" + d1);
                            N *= n0;
                            D *= d1;
                        } else if (n1 == d1 && n0 != d0 && n0 / d0 == n / d) {
                            System.out.println(n + "/" + d + "=" + n0 + "/" + d0);
                            N *= n0;
                            D *= d0;
                        } else if (n0 == d1 && n1 != d0 && n1 / d0 == n / d) {
                            System.out.println(n + "/" + d + "=" + n1 + "/" + d0);
                            N *= n1;
                            D *= d0;
                        }
                    }
                }
            }
        }
        int gcd = Divisors.gcd(N, D);
        System.out.println((N / gcd) + "/" + (D / gcd) + " in " + (System.currentTimeMillis() - time) + "ms");
        assertEquals(1, N / gcd);
        assertEquals(100, D / gcd);
    }
}

/*

The numerator n=N1N0 and denominator d=D1D0 have 2 digits, and the value is less than 1. So:

10 <= n <= 98
n+1 <= d <= 99

We then must find when n and d have one digit in common, except when they are both multiples of 10 (this is the get case).

10/10, 10/20, ...

We also can remove all cases when at least one is a multiple of 10.
Because if n is a multiple of 10 and we find a case where d has a common digit, it will be either 0 (both multiples of 10), or 1 and in this case the value is 0. So:

11 <= n <= 98
n+1 <= d <= 99

When we found a case where there is a common digit, such as 49/98, we simply check if the fraction without the common digit gives the same value.

*/

