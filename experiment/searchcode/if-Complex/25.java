/*
* Created on Feb 23, 2006
*/
package de.torstennahm.math;


public class Complex {
public boolean equals(Object o) {
if (o instanceof Complex) {
Complex c = (Complex) o;
return c.r == r &amp;&amp; c.i == i;
} else {
return false;
}
}
}

