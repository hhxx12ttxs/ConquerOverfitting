package library;

import java.io.Serializable;

/**
 * This class implements complex numbers z = (a+bi).
 * Methods with "_inp" in its name can be used for faster calculations due to not creating a new object (inplace).
 * 
 * @author Marc Sladek
 * @version Feb, 2012
 */

public class Complex extends Object implements Serializable{
	
	private static final long serialVersionUID = 2702201204192L;
	
	private double 
		/** Real part */
		a,
		/** Imaginary part */
		b;
	
	public static final Complex
		/** (0+0i) */
		C_ZERO = new Complex(0,0),
		/** (1+0i) */
		C_ONE_REAL = new Complex(1,0),
		/** (-1+0i) */
		C_ONE_REAL_M = new Complex(-1,0),
		/** (0+1i) */
		C_ONE_IMAG = new Complex(0,1),
		/** (0-1i) */
		C_ONE_IMAG_M = new Complex(0,-1),
		/** (1+1i) */
		C_ONE_BOTH = new Complex(1,1),
		/** (-1-1i) */
		C_ONE_BOTH_M = new Complex(-1,-1);
	
	/** Constructs the new complex number (0+0i) */
	public Complex(){ this(0, 0, false); }
	/** Constructs the new complex number (a+0i) */
	public Complex(double a){ this(a, 0, false); }
	/** Constructs the new complex number (a+bi) */
	public Complex(double a, double b){ this(a, b, false); }
	/** Constructs the new complex number in polar form if isPolar==true */
	public Complex(double r, double phi, boolean isPolar){
		if(isPolar){
			this.a = r*Math.cos(phi);
			this.b = r*Math.sin(phi);
		} else{
			this.a = r;
			this.b = phi;
		}
	}
	
	/** Return the String representation of this complex number rounded to scale 1000 */
	public String toString(){
		return this.toString(1000);
	}
	
	/** Return the String representation of this complex number rounded to given scale 
	 * @param scale - rounds number to scale
	 * */
	public String toString(int scale){
		return this.round(scale).toStringEx();
	}
	
	/** Return this complex number rounded to given scale 
	 * @param scale - rounds number to scale
	 * */
	public Complex round(int scale){
		return new Complex(
				Math.round(this.getReal()*scale)/(1.0*scale),
				Math.round(this.getImag()*scale)/(1.0*scale)
		);
	}
	
	/** Return the String representation of this complex number exactly */
	public String toStringEx(){
		return new StringBuffer(
			"(" ).append(
			this.a ).append(
			b<0?" - i":" + i" ).append(
			Math.abs(this.b) ).append(
			")"
		).toString();
	}
	
	/** Returns the real part */
	public double getReal(){ return this.a; }
	/** Sets the real part
	 * @param a - new real part */
	public void setReal(double a){ this.a = a; }
	
	/** Returns the imaginary part */
	public double getImag(){ return this.b; }
	/** Sets the imaginary part
	 * @param b - new imaginary part */
	public void setImag(double b){ this.b = b; }
	
	public Complex toPolar(){
		double r = this.a;
		this.a = r*Math.cos(b);
		this.b = r*Math.sin(b);
		return this;
	}
	
	/** returns the radius of the polar form **/
	public double getRad(){
		return this.abs();
	}
	
	public double getPhi(){ return getPhi(Double.MAX_VALUE); } 
	public double getPhi(double scale){
		if(Math.round(this.a*scale)/scale==0)
			if(Math.round(this.b*scale)/scale==0)
								return Double.NaN;
			else if(this.b>0) 	return Math.PI/2;
			else				return -Math.PI/2;
		else if(this.a<0)
			if(this.b>=0)		return Math.atan(this.b/this.a)+Math.PI;
			else				return Math.atan(this.b/this.a)-Math.PI;
		else
								return Math.atan(this.b/this.a);
	}
	
	public Complex copy(){ return new Complex(this.a, this.b); }
	public Complex copy_inp(Complex in){
		if(in==null) this.a = this.b = 0;
		this.a = in.getReal();
		this.b = in.getImag();
		return this;
	}
		
	public boolean equals(Object obj) {
	    try {
	      Complex in = (Complex)obj;
	      return in.a == this.a && in.b == this.b;
	    } catch(Exception e){ return false; }
	}
		
	/* conj(a+bi) = (a-bi) */
	public Complex conj() {
	    return new Complex(this.a, -this.b);
	}
	
	public Complex conj_inp() {
	    this.b *= -1;
	    return this;
	}
	
	/* (a+bi)+r = (a+r)+bi */
	public Complex add(double in){
		return new Complex( this.a + in , this.b );
	}
	
	/* (a+bi)+(c+di) = (a+c)+(b+d)i */
	public Complex add(Complex in){
		return new Complex( this.a + in.a , this.b +  in.b );
	}
	
	public Complex add_inp(double in){
		this.a += in;
		return this;
	}

	public Complex add_inp(Complex in){
		this.a += in.a;
		this.b += in.b;
		return this;
	}
	
	/* (a+bi)+r = (a+r)+bi */
	public Complex sub(double in){
		return new Complex( this.a - in , this.b );
	}
	
	/* (a+bi)+(c+di) = (a-c)+(b-d)i */
	public Complex sub(Complex in){
		return new Complex(this.a - in.a , this.b - in.b );
	}
	
	public Complex sub_inp(double in){
		this.a -= in;
		return this;
	}

	public Complex sub_inp(Complex in){
		this.a -= in.a;
		this.b -= in.b;
		return this;
	}
	
	/* (a+bi)*r = (ar)+(br)i  */
	public Complex mult(double in){
		return new Complex( in*this.a , in*this.b );
	}

	/* (a+bi)(c+di) = (ac-bd)+(ad+bc)i  */
	public Complex mult(Complex in){
		return new Complex( in.a*this.a - in.b*this.b , in.a*this.b + in.b*this.a );
	}
	
	public Complex mult_inp(double in){
		this.a *= in;
		this.b *= in;
		return this;
	}

	public Complex mult_inp(Complex in){
		double 
			this_a_tmp = this.a,
			in_a_tmp = in.a;
		this.a = in.a*this.a - in.b*this.b;
		this.b = in_a_tmp*this.b + in.b*this_a_tmp;
		return this;
	}
	
	/* (a+bi)/(c+di) = (ac+bd)/(c^2+d^2)+(bc-ad)/(c^2+d^2)i  */
	public Complex div(Complex in){
		double denom = in.a*in.a + in.b*in.b;
		if(denom==0) return new Complex(Double.NaN,Double.NaN);
		else return new Complex( (in.a*this.a + in.b*this.b) / denom , (this.b*in.a - this.a*in.b) / denom );
	}
	
	/* c1.div_op(c2) == c2.div(c1) */
	public Complex div_op(Complex in){
		double denom = this.a*this.a + this.b*this.b;
		if(denom==0) return new Complex(Double.NaN,Double.NaN);
		return new Complex( (in.a*this.a + in.b*this.b) / denom , (in.b*this.a - in.a*this.b) / denom );
	}
	
	public Complex div_inp(Complex in){
		double 
			denom = in.a*in.a + in.b*in.b,
			this_a_tmp = this.a,
			in_a_tmp = in.a;
		if(denom==0) this.a = this.b = Double.NaN;
		else{
			this.a = (in.a*this.a + in.b*this.b) / denom;
			this.b = (this.b*in_a_tmp - this_a_tmp*in.b) / denom;
		}
		return this;
	}
	
	public Complex div_op_inp(Complex in){
		double 
			denom = this.a*this.a + this.b*this.b,
			this_a_tmp = this.a,
			in_a_tmp = in.a;
		if(denom==0) this.a = this.b = Double.NaN;
		else{
			this.a = (in.a*this.a + in.b*this.b) / denom;
			this.b = (in.b*this_a_tmp - in_a_tmp*this.b) / denom;
		}
		return this;
	}
	
	/* (a+bi)^p
	 * pow_polar is faster than pow_mult for in > 151 and <-167
	 */
	public Complex pow(int in){
		if(in==0)
			return new Complex(1);
		else if(in==1)
			return this.copy();
		else if(in==2)
			return this.sqr();
		else if(-167<in || in<152)
			return this.pow_mult(in);
		else
			return this.pow_polar(in);
	}
	
	public Complex pow_inp(int in){
		if(in==0){
			this.a = 1; this.b = 0;
			return this;
		}
		else if(in==1)
			return this;
		else if(in==2)
			return this.sqr_inp();
		else if(-167<in || in<152)
			return this.pow_mult_inp(in);
		else
			return this.pow_polar_inp(in);
	}
	
	private Complex pow_mult(int in){
		Complex ret = this.copy();
		for(int i=0;i<Math.abs(in)-1;i++)
			ret.mult_inp(this);
		if(in<0)
			ret.div_op_inp(Complex.C_ONE_REAL);
		return ret;
	}
	
	private Complex pow_mult_inp(int in){
		Complex c = this.copy();
		for(int i=0;i<Math.abs(in)-1;i++)
			this.mult_inp(c);
		if(in<0)
			this.div_op_inp(Complex.C_ONE_REAL);
		return this;
	}
	
	private Complex pow_polar(int in){
		double 
			r = Math.pow(this.getRad(), in),
			phi_this = this.getPhi();
		return new Complex(
				r * Math.cos(in*phi_this),
				r * Math.sin(in*phi_this)
		);
	}
	
	private Complex pow_polar_inp(int in){
		double 
			r = Math.pow(this.getRad(), in),
			phi_this = this.getPhi();
		this.a = r * Math.cos(in*phi_this);
		this.b = r * Math.sin(in*phi_this);
		return this;
	}
	
	public Complex[] root(int in){
		if(in==0) return new Complex[] {new Complex(1,0)};
		else if(in<0) return this.pow(new Frac(-1,Math.abs(in)));
		Complex[] ret = new Complex[in];
		double 
			r = Math.pow(this.getRad(), 1d/in),
			phi,
			phi_this = this.getPhi();
		
		for(int i=0; i<=in-1; i++){
			phi = (phi_this+i*2*Math.PI)/in;
			ret[i] = new Complex(r, phi, true);
		}
		return ret;
	}
	
	public Complex root_f(int in){
		if(in==0) return new Complex(1,0);
		else if(in<0) return this.pow_f(new Frac(-1,Math.abs(in)));
		double 
			r = Math.pow(this.getRad(), 1d/in),
			phi = this.getPhi()/in;
		return new Complex(r, phi, true);
	}
	
	public Complex root_f_inp(int in){
		if(in==0){ this.a = 1; this.b = 0; return this; }
		else if(in<0) return this.pow_f_inp(new Frac(-1,Math.abs(in)));
		double 
			r = Math.pow(this.getRad(), 1d/in),
			phi = this.getPhi()/in;
		this.a = r;
		this.b = phi;
		return this.toPolar();
	}
	
	public Complex[] pow(Frac in){
		Complex[] ret = this.root(in.getDen());
		for(Complex c : ret)
			c.pow_inp(in.getNum());
		return ret;
	}
	
	public Complex pow_f(Frac in){
		if(in.getDen()==1) return this.pow(in.getNum());
		else if(in.getNum()==1) return this.root_f(in.getDen());
		else return this.root_f(in.getDen()).pow_inp(in.getNum());
	}
	
	public Complex pow_f_inp(Frac in){
		if(in.getDen()==1) return this.pow_inp(in.getNum());
		else if(in.getNum()==1) return this.root_f_inp(in.getDen());
		else return this.root_f_inp(in.getDen()).pow_inp(in.getNum());
	}
	
	public Complex[] pow(double in){
		return this.pow(new Frac(in));
	}
	
	public Complex pow_f(Double in){
		return this.pow_f(new Frac(in));
	}
	
	public Complex pow_f_inp(Double in){
		return this.pow_f_inp(new Frac(in));
	}
	
	//(a+bi)^2
	public Complex sqr(){
		return this.mult(this);
	}

	public Complex sqr_inp(){
		return this.mult_inp(this);
	}
	
	/* |(a+bi)| = sqrt(a^2+b^2) */
	public double abs(){
		return Math.sqrt(this.a*this.a + this.b*this.b);
	}
	
	/* |(a+bi)|^2 = a^2+b^2 */
	public double abs_sqr(){
		return this.a*this.a + this.b*this.b;
	}
	
	/* (|a|+|b|i) = (sqrt(a^2)+sqrt(b^2)i) */
	public Complex abs_sep(){
		this.a = Math.abs(this.a);
		this.b = Math.abs(this.b);
		return this;
	}
	
	public Complex sin(){
		return new Complex(Math.sin(this.a)*Math.cosh(this.b) , Math.cos(this.a)+Math.sinh(this.b));
	}
	
	public Complex sin_inp(){
		double a_tmp = this.a;
		this.a = Math.sin(this.a)*Math.cosh(this.b);
		this.b = Math.cos(a_tmp)+Math.sinh(this.b);
		return this;
	}
	
	public Complex cos(){
		return new Complex(Math.cos(this.a)*Math.cosh(this.b) , -Math.sin(this.a)+Math.sinh(this.b));
	}
	
	public Complex cos_inp(){
		double a_tmp = this.a;
		this.a = Math.cos(this.a)*Math.cosh(this.b);
		this.b = Math.sin(a_tmp)+Math.sinh(this.b);
		return this;
	}
	
}

