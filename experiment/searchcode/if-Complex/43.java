// ------------
// Complex.java
// ------------

class MyComplex {
private int _r;
private int _i;
public boolean equals (Object rhs) {
if (!(rhs instanceof MyComplex))
return false;

