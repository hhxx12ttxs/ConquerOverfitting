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
package com.mycila;

import com.mycila.math.triplet.IntTriplet;

/**
 * @author Mathieu Carbou
 */
public final class Factor {
    private final int p;
    private int exp;

    private Factor(int p, int times) {
        this.p = p;
        this.exp = times;
    }

    public int prime() {
        return p;
    }

    public int exponent() {
        return exp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntTriplet triplet = (IntTriplet) o;
        return p == triplet.a && exp == triplet.b;
    }

    @Override
    public int hashCode() {
        int result = p;
        result = 31 * result + exp;
        return result;
    }

    @Override
    public String toString() {
        return p + "^" + exp;
    }

    public Factor incrementExponent() {
        this.exp++;
        return this;
    }

    public int value() {
        return (int) Math.pow(p, exp);
    }

    public static Factor valueOf(int p) {
        return new Factor(p, 1);
    }

    public static Factor valueOf(int p, int times) {
        return new Factor(p, times);
    }
}
