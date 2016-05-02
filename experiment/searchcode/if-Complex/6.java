<<<<<<< HEAD
package com.apress.springrecipes.calculator;

import java.util.Collections;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class ComplexCachingAspect {

    private Map<String, Complex> cache;

    public void setCache(Map<String, Complex> cache) {
        this.cache = Collections.synchronizedMap(cache);
    }

    @Around("call(public Complex.new(int, int)) && args(a,b)")
    public Object cacheAround(ProceedingJoinPoint joinPoint, int a, int b)
            throws Throwable {
        String key = a + "," + b;
        Complex complex = cache.get(key);
        if (complex == null) {
            System.out.println("Cache MISS for (" + key + ")");
            complex = (Complex) joinPoint.proceed();
            cache.put(key, complex);
        }
        else {
            System.out.println("Cache HIT for (" + key + ")");
        }
        return complex;
    }
}
=======
package problems;

public class Complex {
	private Double real;
	private Double imaginary;
	
	public Complex(Double real, Double imaginary) {
		this.real = real;
		this.imaginary = imaginary;
	}
	
	public boolean equals (Object o) {
		if (this == o) return true;
		if ( (o == null) || (getClass() != o.getClass()) ) return false;
	
		Complex complex = (Complex) o;
		if ((Double.compare(this.real, complex.real)) != 0 || 
				(Double.compare(this.real, complex.real) != 0)) return false;
		return true;
	}
	
	public Complex add(Complex obj) {
		Complex complex = new Complex(obj.real, obj.imaginary);
		complex.real += this.real;
		complex.imaginary += this.imaginary;
		return complex;
	}
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

