/*************************************************************************
 *  Compilation:  javac Complex.java
 *  Execution:    java Complex
 *
 *  Data type for complex numbers.
 *
 *  The data type is \"immutable\" so once you create and initialize
 *  a Complex object, you cannot change it. The \"final\" keyword
 *  when declaring re and im enforces this rule, making it a
 *
 *  % java Complex
 *  a            = 5.0 + 6.0i
public class Complex {
    private final double re;   // the real part

