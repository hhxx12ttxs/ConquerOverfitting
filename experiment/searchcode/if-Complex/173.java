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
 *  Class based off of Princeton University's Complex.java class
 *  @author Aaron Gokaslan, Princeton University
public class Complex {
    private final double re;   // the real part

