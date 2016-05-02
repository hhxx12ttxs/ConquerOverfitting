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

import com.mycila.math.prime.Sieve;

import static java.lang.System.*;

/**
 * http://projecteuler.net/index.php?section=problems&id=50
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
class Problem050 {
    public static void main(String[] args) throws Exception {
        long time = currentTimeMillis();
        final Sieve sieve = Sieve.to(999999);
        int maxSum = 0, maxStart = 0, maxEnd = 0, maxTerms = 0;
        main:
        for (int start = 0, max = sieve.size() - 22; start < max; start++) {
            int sum = sieve.get(start), end = start + 1;
            for (; end < max; end++) {
                int s = sum + sieve.get(end);
                // if the sum overflows, it means that starting
                // with all numbers >= start, we will always
                // get too big sums
                if (s <= sum) break main;
                sum = s;
                final int terms = end - start + 1;
                if (maxTerms < terms && sieve.contains(sum)) {
                    maxStart = sieve.get(start);
                    maxEnd = sieve.get(end);
                    maxSum = sum;
                    maxTerms = terms;
                    out.println("S(" + maxStart + "," + maxEnd + ")=" + maxSum + " (" + maxTerms + " terms) in " + (currentTimeMillis() - time) + "ms");
                }
            }
        }
        out.println("=> MAX: S(" + maxStart + "," + maxEnd + ")=" + maxSum + " (" + maxTerms + " terms) in " + (currentTimeMillis() - time) + "ms");
    }

}
/*
S(2,3)=5 (2 terms) in 46ms
S(2,7)=17 (4 terms) in 46ms
S(2,13)=41 (6 terms) in 46ms
S(2,37)=197 (12 terms) in 46ms
S(2,43)=281 (14 terms) in 46ms
S(2,281)=7699 (60 terms) in 46ms
S(2,311)=8893 (64 terms) in 46ms
S(2,503)=22039 (96 terms) in 46ms
S(2,541)=24133 (100 terms) in 46ms
S(2,557)=25237 (102 terms) in 46ms
S(2,593)=28697 (108 terms) in 46ms
S(2,619)=32353 (114 terms) in 46ms
S(2,673)=37561 (122 terms) in 46ms
S(2,683)=38921 (124 terms) in 46ms
S(2,733)=43201 (130 terms) in 46ms
S(2,743)=44683 (132 terms) in 46ms
S(2,839)=55837 (146 terms) in 46ms
S(2,881)=61027 (152 terms) in 46ms
S(2,929)=66463 (158 terms) in 46ms
S(2,953)=70241 (162 terms) in 46ms
S(2,1061)=86453 (178 terms) in 46ms
S(2,1163)=102001 (192 terms) in 46ms
S(2,1213)=109147 (198 terms) in 46ms
S(2,1249)=116533 (204 terms) in 46ms
S(2,1277)=119069 (206 terms) in 46ms
S(2,1283)=121631 (208 terms) in 46ms
S(2,1307)=129419 (214 terms) in 46ms
S(2,1321)=132059 (216 terms) in 46ms
S(2,1949)=263171 (296 terms) in 46ms
S(2,2029)=287137 (308 terms) in 46ms
S(2,2161)=325019 (326 terms) in 46ms
S(2,2203)=329401 (328 terms) in 46ms
S(2,2213)=333821 (330 terms) in 46ms
S(2,2237)=338279 (332 terms) in 46ms
S(2,2243)=342761 (334 terms) in 46ms
S(2,2297)=360979 (342 terms) in 46ms
S(2,2357)=379667 (350 terms) in 46ms
S(2,2393)=393961 (356 terms) in 46ms
S(2,2411)=398771 (358 terms) in 46ms
S(2,2957)=581921 (426 terms) in 46ms
S(2,3137)=642869 (446 terms) in 46ms
S(2,3251)=681257 (458 terms) in 46ms
S(2,3257)=687767 (460 terms) in 46ms
S(2,3301)=700897 (464 terms) in 46ms
S(2,3413)=754573 (480 terms) in 46ms
S(2,3461)=768373 (484 terms) in 46ms
S(2,3491)=782263 (488 terms) in 46ms
S(2,3671)=868151 (512 terms) in 46ms
S(2,3821)=935507 (530 terms) in 46ms
S(2,3863)=958577 (536 terms) in 46ms
S(5,3889)=970219 (537 terms) in 78ms
S(5,3911)=978037 (539 terms) in 78ms
S(7,3931)=997651 (543 terms) in 78ms
=> MAX: S(7,3931)=997651 (543 terms) in 34453ms

*/
