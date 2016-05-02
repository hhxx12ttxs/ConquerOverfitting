/*
 * Copyright Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the authors tag. All rights reserved.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License version 2.
 * 
 * This particular file is subject to the "Classpath" exception as provided in the 
 * LICENSE file that accompanied this code.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package com.redhat.ceylon.compiler.java.test.interop;

public class JavaWithOverloadedMembers {
    
    public JavaWithOverloadedMembers(){}
    public JavaWithOverloadedMembers(long param){}
    
    public void method(){}
    public void method(long param){}
    public void method(long param, long param2){}
    
    public void topMethod(){}
    
    public void variadic(){}
    public void variadic(long p1){}
    public void variadic(long p1, long p2){}
    public void variadic(long... params){}
    public void variadic(double p1){}
    public void variadic(double p1, double p2){}
    public void variadic(double... params){}
    public void variadic(Object param){}
    public void variadic(Object... params){}
    public void variadic(String... params){}
}

