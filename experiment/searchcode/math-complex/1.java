/*
 * Complex.java
 *   Implementation of complex numbers, for use in FFT etc.
 *   This uses integers to store the real and imaginary components.
 *   Scale arguments to the constructor appropriately.
 *   All operations are done in-place so, e.g., x.div(y) modifies x.
 *
 * Created on October 29, 2007, 12:53 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 *
 * Copyright 2007 by Jon A. Webb
 *     This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Lesser General Public License as published by
 *    the Free Software Foundation, either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Lesser General Public License for more details.
 *
 *    You should have received a copy of the Lesser GNU General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jjil.core;

/**
 * A simple implementation of complex numbers for use in FFT, etc.
 * @author webb
 */
public class Complex {
    public static final Complex ZERO = new Complex(0, 0);
}

