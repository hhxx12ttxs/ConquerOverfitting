/*
 * Copyright 2009 Yannick Stucki (yannickstucki.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yannickstucki.android.musicqueue.old;

import java.util.Random;

/**
 * A utility class for generating random sequences.
 * 
 * @author Yannick Stucki (yannickstucki@gmail.com)
 * 
 */
public final class RandomSequence {

  /**
   * Utility class cannot be instantiated.
   */
  private RandomSequence() {
  }

  /**
   * An array of prime numbers. We need a generator for every possible finite
   * group. Any prime number is coprime and thus a generator, except when the
   * group's cardinality is divided by the prime number. For that case we have
   * an array of prime numbers, so one of them surely won't divide the group's
   * cardinality. The only problem would occur if someone had the same amount of
   * songs as a multiple of the product of all those prime numbers which is of
   * course too big to be even an integer.
   */
  private static final int[] X = new int[] { 19, 23, 29, 31, 37, 41, 43, 47,
      53, 59, 61, 67, 71, 73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127,
      131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197,
      199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277,
      281, 283, 293, 307, 311, 313, 317, 331, 337, 347, 349, 353, 359, 367,
      373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 439, 443, 449,
      457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541 };

  /**
   * Creates a (semi) random sequence of length n containing all the integers
   * from 0 to n - 1. Not every sequence is possible, but since there's many
   * prime numbers as potential generators, the variety will be big enough to
   * appear random. This way we can generate a random sequence in O(n) instead
   * of O(n^2).
   */
  public static int[] get(int n) {
    Random r = new Random();
    r.setSeed(System.currentTimeMillis());
    int index = r.nextInt(X.length);
    int x = X[index];
    while (n % x == 0) {
      index = (index + 1) % X.length;
      x = X[index];
    }
    int y = r.nextInt(X.length);
    int[] randomSequence = new int[n];
    for (int i = 0; i < n; i++) {
      randomSequence[i] = (x * i + y) % n;
    }
    return randomSequence;
  }
}

