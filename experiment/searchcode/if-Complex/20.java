package Math_Formulae;

import java.util.Arrays;


import Math_Formulae.Series1;

//Author Bernard(Elazar) Gingold based on "Handbook of Mathematical Functions"
public class Complex extends Object {
	
	protected  double Im;
	protected  double Re;
	
	public Complex(double re, double im){ //constructor
		
		Re = re;
		Im = im;
	}
	
	
	
	
	
    public void setImRe( double val1,double val2){
    	
    	Re = val1;
    	Im = val2;
    	
    }
    
    public double getIm(){
    	
    	return Im;
    }
    
    public double getRe(){
    	
    	return Re;
    }
    
    public String toString(){ // USE THIS METHOD WHEN WORKING WITH OBJECTS
    	
    	return Re + " +  " + Im+"i";
    }
    
    public Complex complexAdd(Complex c ){
    	
    	Complex a = this;
    	
    	double real = a.Re + c.Re;
    	double imaginary       = a.Im + c.Im;
    	
    	return new Complex(real,imaginary);
    	
    	
    }
	/** complex numbers public class with instance methods
	 * @param args
	 */
    
    
    public Complex complexSub(Complex c){
    	
    	Complex a = this;
    	
    	double real = a.Re - c.Re;
    	double imaginary = a.Im - c.Im;
    	
    	return new Complex(real,imaginary);
    }
    
    public Complex complexMul(Complex c){
    	
    	Complex a = this;
    	
    	double real= a.Re * c.Re - a.Im*c.Im;
    	double imaginary = a.Re * c.Im + a.Im*c.Re;
    	
    	return new Complex(real,imaginary);
    }
    
    public Complex complexDiv(Complex c){
    	
    	Complex a = this;
    	double pow1 = Math.pow(c.Re, 2);
    	double pow2 = Math.pow(c.Im, 2);
    	double real = a.Re * c.Re + a.Im*c.Im;
    	real = real/pow1;
    	double imaginary = a.Im *c.Im - a.Re*c.Im;
    	imaginary = imaginary/pow2;
    	
    	return new Complex(real,imaginary);
    }
    
    public double magnitude (){
    	
    	return Math.hypot(Re, Im);
    }
    
    public Complex complexConjugate(){
    	
    	return new Complex(Re,-Im);
    }
    
    
    public Complex complexScale(double x,double y){
    	
    	double real = this.Re*x;
    	double imaginary = this.Im*y;
    	
    	return new Complex(real,imaginary);
    }
    
    public double complexPhase(){
    	
    	return Math.atan2(getRe(), getIm());
    }
    
    public Complex complexInverse(){
    	
    	return new Complex(1/Re,1/Im);
    }
    
    public Complex polarForm(){
    	Complex a = this;
    	double radius = a.magnitude();
    	double phase =   a.complexPhase();
    	
    	return new Complex(radius*Math.cos(phase),radius*Math.sin(phase));
    	
    	
    }
    
    public Complex complexSine(){
    	
    	return new Complex(Math.sin(getRe())*Math.cosh(Im), Math.cos(getRe())*Math.sinh(getIm()));
    }
    
    public Complex polarMul(Complex c){
    	
    	Complex a = this;
    	double radius = this.magnitude();
    	double phase =  this.complexPhase();
    	double radius2 = c.magnitude();
    	double phase2  = c.complexPhase();
    	double radiusmul = radius*radius2;
    	double phaseadd = phase+phase2;
    	
    	return new Complex(radiusmul*Math.cos(phaseadd),radiusmul*Math.sin(phaseadd));
    	
    	
    }
    
    public Complex polarDiv(Complex c){
    	
    	Complex a = this;
    	double radius = this.magnitude();
    	double phase  = this.complexPhase();
    	double radius2 = c.magnitude();
    	double phase2  =  c.complexPhase();
    	double radiusdiv = radius/radius2;
    	double phasesub  = phase-phase2;
    	
    	return new Complex(radiusdiv*Math.cos(phasesub),radiusdiv*Math.sin(phasesub));
    	
    }
    
    public Complex reciprocalComplex(){
    	
    	double divisor = Re*Re+Im*Im;
    	
    	return new Complex(Re/divisor,-Im/divisor);
    }
    
    public Complex complexPolarPow(int n){
    	
    	if (n == 0 || n < 0)
    		
    		return null;
    	
    	double radius = this.magnitude();
    	double phase  = this.complexPhase();
    	double power = Math.pow(radius, n);
    	double sin2x = (2*Math.sin(phase))*Math.cos(phase);
    	double cos2x = Math.pow(Math.cos(phase), n)+Math.pow(Math.cos(phase), n)-1;
    	double sin3x = (3*Math.sin(phase))-(4*Math.pow(Math.sin(phase), n));
    	double cos3x = (4*Math.pow(Math.cos(phase), n))-(3*Math.cos(phase));
    	Complex a = this;
    	switch(n){
    	
    	case 1 : if(n == 1); a = new Complex(power*Math.cos(n*phase),power*Math.sin(n*phase));
    	
                             break;
                             
    	case 2 : if( n == 2); a = new Complex(power*cos2x,power*sin2x);
    	
    	                        break;
    	
    	case 3  : if( n == 3); a = new Complex( power*cos3x,power*sin3x);
    	                       
    	                      break;
    	
    	}
    	return a;
    }
    
    public Complex complexPolarRoot(Complex c,int n){
    	
    	if (n == 0 || n < 0)
    		return null;
    	
    	Complex temp;
    	temp = c.complexPolarPow(n);
    	double radius = temp.Re;
    	radius = Math.pow(radius, n);
    	double phase = c.complexPhase();
    	double k = n/2*Math.PI;
    	double term = phase+(2*k*Math.PI);
    	double term2 = phase+(2*k*Math.PI);
    	radius = Series1.nthRoot(radius, n);
    	
    	return new Complex(radius*Math.cos(term/n),radius*Math.sin(term2/n));
    	
    }
    
    public Complex complexExponential(){
    	
    	return new Complex(Math.exp(getRe())*Math.cos(getIm()),Math.exp(getRe())*Math.sin(getIm()));
    }
    
    public Complex complexCosine(){
    	
    	return new Complex(Math.cos(getRe())*Math.cosh(getIm()),-Math.sin(getRe())*Math.sinh(getIm()));
    }
    
    public Complex complexTan(){
    	
    	return complexSine().complexDiv(complexCosine());//Can be done because return value of complexCosine is object
    }
    
    public Complex complexCot(){
    	
    	return complexCosine().complexDiv(complexSine());
    }
    
    public Complex[] createArray1D(double step_re ,double step_im,int n) throws RuntimeException{
    	
    	if (n == 0 || n < 0)
    		
    		throw new RuntimeException("argument n must be greater than 0");
    	
    	int i;
    	Complex[] array = new Complex[n];
    	
    	
    	for(i = 0;i<array.length;i++){
    		Re += step_re;
    	    Im += step_im;
    		
    		
    		array[i] = new Complex(Re,Im);
    	}
    	
    	return array;
    }
    
    public Complex[][] createArray2D(double step_re,double step_im,int n,int m) throws RuntimeException{
    	
    	if(n == 0 || n < 0)
    		
    		throw new RuntimeException("array's size n must be greater than 0");
    	
    	if(m == 0 || m < 0)
    		
    		throw new RuntimeException("array's size m must be greater than 0");
    	
    	int i,j;
    	
    	Complex[][] array = new Complex[n][m];
    	
    	for(i = 0;i<array.length;i++){
    		for(j = 0;j<array[i].length;j++){
    			
    			Re += step_re;
    			Im += step_im;
    			
    			array[i][j] = new Complex(Re,Im);
    		}
    	}
    	
    	return array;
    }
    
    public Complex complexLog(){
    	
    	double radius = this.magnitude();
    	double phase  = this.complexPhase();
    	
    	return new Complex (Math.log(radius),phase);
    	
    	
    }
    
    public Complex[] complexArray1DSin(int n,double re_step,double im_step) throws RuntimeException{
    	
    	if(n == 0 || n < 0)
    		
    		throw new RuntimeException("argument n must be greater than 0");
    	
    	int i;
    	Complex[] array = new Complex[n];
    	
    	for(i = 0;i < array.length;i++){
    		
    		Re += re_step;
    		Im += im_step;
    		
    		array[i] = this.complexSine();
    		System.out.println("array \t "+ array[i]);
    	}
    	
    	return array;
    	
    }
    
    public Complex func(Complex c){//for testing not sure to fully implement complex functions 
    	Complex a = this;
    	
    	  for(int i =0;i<50;i++){
  	    	Re +=0.01;
  	    	Im +=0.01;                   //Proper way for evaluating Complex objects with loop
    		  a.complexCosine();
    		  c.complexCot();
    		
    		 a.complexAdd(c);
    		
  	    	
    	  }
    	 return a;
    	 
    }
    
    public Complex complexHyperbolicCos(){
    	
    	return new Complex(Math.cosh(Re)*Math.cos(Im),Math.sinh(Re)*Math.sin(Im));
    }
    
    public Complex complexHyperbolicSin(){
    	
    	return new Complex(Math.sinh(Re)*Math.cos(Im),Math.cosh(Re)*Math.sin(Im));
    }
    
    public Complex[][] complexArray2DSin(double re_step,double im_step ,int n ,int m) throws IllegalArgumentException{
    	
    	if(n == 0 || n < 0)
    		
    		throw new IllegalArgumentException("argument n must be greater than 0");
    	
    	if(m == 0 || m < 0)
    	
    		throw new IllegalArgumentException("argument m must be greater than 0");
    	
    	Complex[][]array = new Complex[n][m];
    	int i;
    	int j;
    	
    	for(i = 0;i<array.length;i++){
    		
    		for(j = 0; j<array[i].length;j++){
    			
    			Re += re_step;
    			Im += im_step;
    			
    			array[i][j] = this.complexSine();
    		}
    	}
    	
    	return array;
    }
    
    public Complex[] complexArray1DCos(double re_step,double im_step, int n) throws IllegalArgumentException{
    	
    	if( n == 0 || n < 0)
    		
    		throw new IllegalArgumentException("argument n must be greater than 0");
    	
    		if(re_step >= 1.0 || im_step >= 1.0)
    		
    		if(n >= 7090)
    			
    			throw new IllegalArgumentException("argument n must be less than 7090 overflow will occur");
    	
    		
    		int i;
    		Complex[] array = new Complex[n];
    		
    		for(i = 0; i<array.length;i++){
    			
    			Re += re_step;
    			Im += im_step;
    			
    			array[i] = this.complexCosine();
    		}
    		
    		
    	
    	
    	return array;
    }
    
    public Complex[][] complexArray2DCos(double re_step,double im_step,int m,int n) throws IllegalArgumentException{
    	
    	if( m == 0 || m < 0)
    		
    		throw new IllegalArgumentException("argument m must be greater than 0");
    	
    	if(n == 0 || n < 0)
    		
    		throw new IllegalArgumentException("argument n must be greater than 0");
    	
    	if (re_step >= 1.0 || im_step >= 1.0)
    		
    		if(n >= 7090 || m >= 7090)
    			
    			throw new IllegalArgumentException("arguments n or m can not be greater or equal to 7090 overflow will occur");
    	
    	int i;
    	int j;
    	Complex[][] array = new Complex[m][n];
    	
    	for(i = 0;i < array.length;i++){
    		
    		for(j = 0; j < array[i].length;j++){
    			
    			Re += re_step;
    			Im += im_step;
    			
    			array[i][j] = this.complexCosine();
    		}
    	}
    	
    	return array;
    }
      
    public Complex complexSQRT(){
    	
    	double radius = this.magnitude();
    	double sqrt = Math.sqrt(radius);
    	double phase = this.complexPhase();
    	double half_phase = phase/2;
    	
    	return new Complex(sqrt*Math.cos(half_phase),sqrt*Math.sin(half_phase));
    }
    
    public Complex complexSpaceRotation(double theta) throws IllegalArgumentException{
    	
    	if (theta == 0)
    		
    		throw new IllegalArgumentException("angle theta must be greater than 0");
    	
    	double x_rot = this.Re*Math.cos(theta) - this.Im*Math.sin(theta);
    	double y_rot = this.Re*Math.sin(theta) + this.Im*Math.cos(theta);
    	
    	return new Complex(x_rot,y_rot);
    }
    
    public Complex complexLogAdd(Complex c){
    	
    	Complex a = this;
    	Complex temp;
    	a = complexLog();
      temp =   c.complexLog();
        
        double re = a.Re + temp.Re;
        double im = a.Im + temp.Im;
        
        return new Complex(re,im);
    }
    
  
   public  Complex complexLogDiv(Complex c){
	   
	  Complex a = this;
	   Complex temp;
	   a = complexLog();
    	temp =   c.complexLog();//Proper Way to Use passed Reference To an Object must use temporary reference to store returned values
	   System.out.println("Re "+a.Re + " Re " + temp.Re);
	   
	   double real = a.Re - temp.Re;
	   double imaginary = a.Im - temp.Im;
	   
	   return new Complex(real,imaginary);
   }
   
   public Complex complexLogToPow(int n) throws IllegalArgumentException{
	   
	  
		   
		   if (n == 0)
			   
			   throw new IllegalArgumentException("argument n cannot be 0");
			   
			   if (n < Integer.MIN_VALUE || n > Integer.MAX_VALUE)
				   
				   throw new IllegalArgumentException(" argument n must be an integer");
			   Complex temp;
			  temp = this.complexLog();
			  double re = n*temp.Re;
			  double im = n*temp.Im;
			   
			 return new Complex(re,im);
          
           
        	   
        	
           
           
           
   }
   
   public Complex complexLogFromBaseDec(){
	   
	  Complex temp;
	  
	  temp = this.complexLog();
	  double constant = 0.4342944819;
	  double re = constant*temp.Re;
	  double im = constant*temp.Im;
	  
	  return new Complex(re,im);
   }
   
   public Complex complexLogToBaseDec(){
	   
	   Complex temp;
	   
	   temp = this.complexLog();
	   double constant = 2.3025850929;
	   double re = constant*temp.Re;
	   double im = constant*temp.Im;
	   
	   return new Complex(re,im);
   }
   
   public Complex complexScaleDiv(double n, double m) throws IllegalArgumentException{
	   
	   if (n == 0)
		   
		   throw new IllegalArgumentException("argument n cannot be 0");
	   
	   if (m == 0)
		   
		   throw new IllegalArgumentException("argument m cannot be 0");
	   
	   double real = this.Re/n;
	   double imaginary = this.Im/m;
	   
	   return new Complex(real,imaginary);
	   
   }
    
   public Complex complexLnExpansion() throws IllegalArgumentException{
	   
	   if ((this.Re > 1 || this.Re < 0) && (this.Im > 1 || this.Im <0))
		   
		   throw new IllegalArgumentException("Re and Im must be <_ 1");
	   
	   Complex a = this;
	   Complex temp;
	   Complex[]array = new Complex[25];
	   int i; 
	  
	   double re = 0;
	   double im = 0;
	   double real = 0;
	   double imaginary = 0;
	   
	   for(i = 2;  i < 25 ; i++){
		   
		   temp = this.complexPolarPow(i);
			  
		  array[i] = temp;
		  array[i] = array[i].complexScaleDiv(i, i);
		  
		 
		
		  re += array[i].getRe();
		  im += array[i].getIm();
		  
		   real = this.Re - re;
		  imaginary = this.Im - im;
		
		  System.out.println("array" + array[i] + " ");
	   }
	   
	 System.out.println("real " + real + " imaginare " + imaginary);
	   
	   return new Complex(real,imaginary);
	   
   }
   
   public Complex complexLogDerivative(){
	   
	   Complex temp;
	    temp = this.complexInverse();
	    double real = temp.Re;
	    double imaginary = temp.Im;
	    
	    return new Complex(real,imaginary);
   }
   
   public Complex complexLogIntegral(Complex c){
	   
	   if (c == null)
		   
		   return null;
	   
	  
		 
	   
	   Complex a = this;
	   
	   if ((c.Re < a.Re) && (c.Im < a.Im))
		   
		  return null;
	   
	  
	   Complex temp;
	   a =  complexLog();
	   temp = c.complexLog();
	   double real = temp.Re - a.Re;
	   double imaginary = temp.Im - a.Im;
	   
	   return new Complex(real,imaginary);
	  
   }
   
   public Complex complexCsc(){
	   
	   Complex temp;
	   temp = this.complexSine();
	   double real = temp.Re;
	   double imaginary = temp.Im;
	   real = 1/real;
	   imaginary = 1/imaginary;
	   
	   return new Complex(real,imaginary);
   }
   
   public Complex complexSec(){
	   
	   Complex temp;
	   temp = this.complexCosine();
	   double real = temp.Re;
	   double imaginary = temp.Im;
	   real = 1/real;
	   imaginary = 1/imaginary;
	   
	   return new Complex(real,imaginary);
   }
   
   public Complex complexSinAddition(Complex c){
	   
	   if ( c == null)
		   
		   return null;
	   
	   Complex a = this;
	   Complex b = this;
	   Complex temp;
	   Complex temp2;
	   a = a.complexSine();
	   b = b.complexCosine();
	   
	   temp = c.complexCosine();
	   temp2 = c.complexSine();
	   
	   a.complexMul(temp);
	   b.complexMul(temp2);
	   
	return  a.complexAdd(b);
   }
   
   public Complex complexCosAddition(Complex c){
	   
	   if(c == null)
		   
		   return null;
	   
	   Complex a = this;
	   Complex b = this;
	   Complex temp;
	   Complex temp2;
	   
	   a = a.complexCosine();
	   b = b.complexSine();
	   temp = c.complexCosine();
	   temp2 = c.complexSine();
	   
	   a.complexMul(temp);
	   b.complexMul(temp2);
	   
	   return a.complexAdd(b);
	   
   }
   
   public Complex complexTanAddition(Complex c){
	   
	   if (c == null)
		   
		   return null;
	   
	   Complex a = this;
	   Complex temp;
	   
	   a = a.complexTan();
	   Complex b =a;
	   temp = c.complexTan();
	   Complex temp_add = a.complexAdd(temp);
	   double real = b.Re;
	   double imaginary = b.Im;//calculating 1-tan(z)
	   real = 1-real;
	   imaginary = 1-imaginary;
	   b = new Complex(real,imaginary);//reassignment
	   Complex temp_div = b.complexMul(temp);
	   
	   return temp_add.complexDiv(temp_div);
   }
   
   public Complex complexCotAddition(Complex c){
	   
	   if ( c == null)
		   
		   return null;
	   
	   Complex a = this;
	   Complex temp;
	   
	   a = a.complexCot();
	   temp = c.complexCot();//cotz2
	   Complex temp2 = temp;//cotz2-1
	   double real = temp2.Re;
	   double imaginary = temp2.Im;
	   real = real-1;
	   imaginary = imaginary-1;
	   temp2 = new Complex(real,imaginary);//reassignment cotz2-1
	   Complex temp_mul = a.complexMul(temp2);
	   Complex temp_add = temp.complexAdd(a);
	   
	   return temp_mul.complexDiv(temp_add);
	   
   }
   
   public Complex complexSinHalfAngle(){
	   
	   Complex temp;
	  temp = this.complexCosine();
	  double real = temp.Re;
	  double imaginary = temp.Im;
	  real = (1-real)/2; 
	  imaginary = (1-imaginary)/2;
	  Complex temp2;
	  temp2 = new Complex(real,imaginary);
	  
	  return temp2.complexSQRT();
	   
   }
   
   public Complex complexCosHalfAngle(){
	   
	   Complex temp;
	   temp = this.complexCosine();
	  
	   double real = temp.Re;
	   double imaginary = temp.Im;
	   real = (1+real)/2;
	   imaginary = (1+imaginary)/2;
	   temp = new Complex(real,imaginary);
	   
	   
	   return temp.complexSQRT();
	   
	   
	   
   }
   
   public Complex complexTanHalfAngle(){
	   
	   if (this == null)
		   
		   return null;
	   
	   Complex temp;
	   Complex temp2;
	   temp = this.complexSine();
	   temp2 = this.complexCosine();
	   double real = temp2.Re;
	   double imaginary = temp2.Im;
	   real = 1+real;
	   imaginary = 1+imaginary;
	   temp2 = new Complex(real,imaginary);//new Complex.
	   
	   return temp.complexDiv(temp2);
   }
   
   public Complex complexSinMultiAngle(){
	   
	   if (this == null)
		   
		   return null;
	   
	   Complex temp;
	   Complex temp2;
	   Complex temp3;
	   temp = this.complexTan();
	  
	   temp2 = temp.complexAdd(temp);
	   
	   temp3 = temp.complexMul(temp);
	   
	   double real = temp3.Re;
	   double imaginary = temp3.Im;
	   real = 1+real;
	   imaginary = 1+imaginary;
	 Complex  temp4 = new Complex(real,imaginary);
	
	   
	   return temp2.complexDiv(temp4);
	   
   }
   
   public Complex complexCosMultiAngle(){
	   
	   if(this == null)
		   
		   return null;
	   
	   Complex temp;
	   Complex temp2;
	   Complex temp3;
	   
	   temp = this.complexTan();
	   temp2 = temp.complexMul(temp);
	   temp3 = temp.complexMul(temp);
	   double real = temp2.Re;
	   double imaginary = temp.Im;
	   real = 1-real;
	   imaginary = 1-imaginary;
	   Complex temp4 = new Complex(real,imaginary);
	   double real2 = temp3.Re;
	   double imaginary2 = temp3.Im;
	   real2 = 1+real2;
	   imaginary2 = 1+imaginary2;
	   Complex temp5 = new Complex(real2,imaginary2);
	   
	   return temp4.complexDiv(temp5);
	   
   }
   
   public Complex complexTanMulti2Angle(){
	   
	   if(this == null)
		   
		   return null;
	   
	   Complex temp;
	   Complex temp2;
	   Complex temp3;
	   
	   temp = this.complexCot();
	   temp2 = this.complexTan();
	   temp3 = temp.complexSub(temp2);
	   double real = temp3.Re;
	   double imaginary = temp3.Im;
	   real = 2/real;
	   imaginary = 2/imaginary;
	   
	   return new Complex(real,imaginary);
		   
   }
   
   public Complex complexSinMulti3Angle(){
	   
	   if(this == null)
		   
		   return null;
	   
	   Complex temp;
	   Complex temp2;
	   Complex temp3;
	   
	   temp = this.complexSine();
	   temp2 = this.complexSine();
	   temp3 = temp.complexMul(temp2);//sin^2z
	   temp3 = temp3.complexMul(temp);//sin^3z
	   double real = temp.Re;
	   double imaginary = temp.Im;
	   real = 3*real;
	   imaginary = 3*imaginary;
	   double real2 = temp3.Re;
	   double imaginary2 = temp3.Im;
	   real2 = 4*real2;
	   imaginary2 = 4*imaginary2;
	   
	   Complex temp4 = new Complex(real,imaginary);
	   Complex temp5 = new Complex(real2,imaginary2);
	   
	   return temp4.complexSub(temp5);
	   
   }
   
   public Complex complexCosMulti3Angle(){
	   
	   if( this == null )
		   
		   return null;
	   
	   Complex temp;
	   Complex temp2;
	   Complex temp3;
	   
	   temp = this.complexCosine();
	   temp2 = this.complexCosine();
	   temp3 = temp.complexMul(temp2);
	   temp3 = temp3.complexMul(temp);
	   double real = temp.Re;
	   double imaginary = temp.Im;
	   real = -3*real;
	   imaginary = -3*imaginary;
	   double real2 = temp3.Re;
	   double imaginary2 = temp3.Im;
	   real2 = 4*real2;
	   imaginary2 = 4*imaginary2;
	   
	   Complex temp4 = new Complex(real,imaginary);
	   Complex temp5 = new Complex(real2,imaginary2);
	   
	   return temp4.complexAdd(temp5);
   }
   
   public Complex complexSinMulti4Angle(){
	   
	   if(this == null)
		   
		   return null;
	   
	   Complex temp;
	   Complex temp1;
	   Complex temp2;
	   Complex temp3;
	   
	   temp = this.complexCosine();
	   temp1 = this.complexCosine();
	   temp2 = this.complexSine();
	   temp3 = temp.complexMul(temp1);
	   temp3 = temp3.complexMul(temp);
	   Complex temp4 = temp3.complexMul(temp2);
	   double real = temp4.Re;
	   double imaginary = temp4.Im;
	   real = 8*real;
	   imaginary = 8*imaginary;
	   Complex temp5 = new Complex(real,imaginary);
	   Complex temp6 = temp.complexMul(temp2);
	   double real2 = temp6.Re;
	   double imaginary2 = temp6.Im;
	   real2 = 4*real2;
	   imaginary2 = 4*imaginary2;
	   Complex temp7 = new Complex(real2,imaginary2);
	   
	   return temp5.complexSub(temp7);
   }
   
   public Complex complexCosMulti4Angle(){
	   
	   if(this == null)
		   
		   return null;
	   
	   Complex temp;
	   Complex temp1;
	   Complex temp2;
	   Complex temp3;
	   
	   temp = this.complexCosine();
	   temp1 = this.complexCosine();
	   temp2 = temp.complexMul(temp1);
	   temp3 = temp2.complexMul(temp);
	   Complex temp4 = temp3.complexMul(temp);
	   double real = temp4.Re;
	   double imaginary = temp4.Im;
	   real = 8*real;
	   imaginary = 8*imaginary;
	   Complex temp6 = new Complex(real,imaginary);//8*cos^4z
	   Complex temp7 = temp.complexMul(temp);
	   double real2 = temp7.Re;
	   double imaginary2 = temp7.Im;
	   real2 = (8*real)+1;
	   imaginary2 =( 8*imaginary2)+1;
	   Complex temp8 = new Complex(real2,imaginary2);
	   
	   return temp6.complexSub(temp8);
	   
	   
   }
   
   public Complex complexSinTwoFuncAdd(Complex c){
	   
	   if(c == null || this == null)
		   
		   return null;
	   
	   Complex a = this;
	   Complex temp = c;
	   a = a.complexSine();
	   temp = c.complexSine();
	   Complex temp2;
	   Complex temp3;
	   Complex temp4;
	   double real = a.Re+temp.Re;
	   double imaginary = a.Im+temp.Im;
	   real =( 2*real)/2;
	   imaginary = (2*imaginary)/2;
	   temp2 = new Complex(real,imaginary);
	   temp3 = a.complexCosine();
	   temp4 = c.complexCosine();
	   double real2 = temp3.Re + temp4.Re;
	   double imaginary2 = temp3.Im+temp4.Im;
	   real2 = real2/2;
	   imaginary2 = imaginary2/2;
	   Complex temp5 = new Complex(real2,imaginary2);
	   
	   return temp2.complexMul(temp5);
   }
   
   public Complex complexSinTwoFuncSub(Complex c){
	   
	   if(c == null || this == null)
		   
		   return null;
	   
	   Complex a = this;
	   Complex temp = c;
	   a = a.complexSine();
	   temp = c.complexSine();
	   Complex temp2;
	   Complex temp3;
	  
	   double real = a.Re+c.Re;
	   double imaginary = a.Im+c.Im;
	   real = (2*real)/2;
	   imaginary = (2*imaginary)/2;
	   temp2 = new Complex(real,imaginary);
	   temp2.complexCosine();  
	   double real2 = a.Re+temp.Re;
	   double imaginary2 = a.Im+temp.Im;
	   real2 = real2/2;
	   imaginary2 = imaginary2/2;
	   temp3 = new Complex(real2,imaginary2);
	   temp3.complexSine();  
	   
	   return temp2.complexMul(temp3);
	   
	   
   }
   
   public Complex complexCosTwoFuncAdd(Complex c){
	   
	   if(c == null || this == null)
		   
		   return null;
	   
	   Complex a = this;
	   Complex temp = c;
	   a = a.complexCosine();
	   temp = c.complexCosine();
	   Complex temp2;
	   Complex temp3;
	   double real = a.Re+temp.Re;
	   double imaginary = a.Im+temp.Im;
	   real = (2*real)/2;
	   imaginary = (2*imaginary)/2;
	    temp2 = new Complex(real,imaginary);
	    temp2.complexCosine();
	    double real2 = a.Re-temp.Re;
	    double imaginary2 = a.Im-temp.Im;
	    real2 = real2/2;
	    imaginary = imaginary2/2;
	    temp3 = new Complex(real2,imaginary2);
	    temp3.complexCosine();
	    
	    return temp2.complexMul(temp3);
	  
   }
   
   public Complex complexCosTwoFuncSub(Complex c){
	   
	   if(this == null || c == null)
		   
		   return null;
	   
	   Complex a = this;
	   Complex temp = c;
	   a = a.complexCosine();
	   temp = c.complexCosine();
	   Complex temp2;
	   Complex temp3;
	   double real = a.Re+temp.Re;
	   double imaginary = a.Im+temp.Im;
	   real = (-2*real)/2;
	   imaginary = (-2*imaginary)/2;
	   temp2 = new Complex(real,imaginary);
	   temp2.complexSine();
	   double real2 = a.Re-temp.Re;
	   double imaginary2 = a.Im-temp.Im;
	   real2 = real2/2;
	   imaginary2 = imaginary2/2;
	   temp3 = new Complex(real2,imaginary2);
	   temp3.complexSine();
	   
	   return temp2.complexMul(temp3);
	   
   }
   
   public Complex complexTanTwoFuncAdd(Complex c){
	   
	   if(this == null || c == null)
		   
		   return null;
	   
	   Complex a = this;
	   Complex temp = c;
	   a.complexSinAddition(temp);
	   Complex temp2;
	   Complex temp3;
	   temp2 = a.complexCosine();
	   System.out.println("value \t" + temp2);
	   temp3 = temp.complexCosine();
	   Complex temp4 = temp2.complexMul(temp3);
	   
	   return a.complexDiv(temp4);
   }
   
   public Complex complexHyperbolicTan(){
	   
	   if(this == null)
		   
		   return null;
	   
	   Complex a = this;
	   Complex temp = this;
	   a= this.complexHyperbolicSin();
	   temp = this.complexHyperbolicCos();
	   
	   return a.complexDiv(temp);
   }
   
   public Complex complexHyperbolicCsc(){
	   
	   if (this == null)
		   
		   return null;
	   
	   Complex a = this;
	   a = this.complexHyperbolicSin();
	   double real = a.Re;
	   double imaginary = a.Im;
	   real = 1/real;
	   imaginary = 1/imaginary;
	   
	   return new Complex(real,imaginary);
   }
   
   public Complex complexHyperbolicSec(){
	   
	   if(this  == null)
		   
		   return null;
	   
	   Complex a = this;
	   a = this.complexHyperbolicCos();
	   double real = a.Re;
	   double imaginary = a.Im;
	   real = 1/real;
	   imaginary = 1/imaginary;
	   
	   return new Complex(real,imaginary);
   }
   
   public Complex complexHyperbolicCot(){
	   
	   if(this == null)
		   
		   return null;
	   
	   Complex a = this;
	   a = this.complexHyperbolicTan();
	   double real = a.Re;
	   double imaginary = a.Im;
	   real = 1/real;
	   imaginary = 1/imaginary;
	   
	   return new Complex(real,imaginary);
   }
   
   public Complex[] complexIntegralSin(int n,double re_step,double im_step){
	   
	   if(this == null)
		   return null;
	   if(n == 0)
		   return null;
	   if(n < 0)
		   return null;
	   if(re_step > 7096)
		   return null;
	   if(im_step > 7096)
		   return null;
	   
	   
	   int Len = n;
	   Complex[] array = new Complex[Len];
	   int i;
	  
	   for(i = 0;i<array.length;i++){
		   
		 
		  Re += re_step;
		  Im += im_step;
		 
		  
		 
		  array[i] = this.complexCosine();
		 
		  System.out.println(array[i] + "  ");
	   }
	   return array;
   }
   
   public Complex[] complexIntegralCos(double re_step,double im_step,int n){
	   
	   if(n == 0)
		   return null;
	   if(n < 0)
		   return null;
	   if(re_step > 7096)
		   return null;
	   if(im_step > 7096)
		   return null;
	   
	   int Len = n;
	   Complex[] array = new Complex[Len];
	   array = this.complexArray1DSin(Len, re_step, im_step);
	   
	   return array;
   }
   
   public Complex[] complexIntegralTan(double re_step,double im_step,int n){
	   
	   if(n == 0)
		   return null;
	   if(n < 0)
		   return null;
	   if(re_step > 7096)
		   return null;
	   if(im_step > 7096)
		   return null;
	   if(this == null)
		   return null;
	   
	   int Len = n;
	   Complex[] array = new Complex[Len];
	   Complex[] temp_ar = new Complex[Len];
	   
	   int i;
	   double real;
	   double imaginary;
	   
	   for(i = 0;i<array.length;i++){
		   
		   Re += re_step;
		   Im += im_step;
		   
		   array[i] = this.complexSec();
		   System.out.println("array[i] \t "+ array[i]+ "  ");
		   real = array[i].getRe();
		   imaginary = array[i].getIm();
		   real = Math.log(real);
		   imaginary = Math.log(imaginary);
		   Complex temp = new Complex(real,imaginary);
		   temp_ar[i] = temp;
		   System.out.println(temp_ar[i]+ " ");
	   }
	   
	   return temp_ar;
   }
   
   public Complex[] complexIntegralCsc(double re_step,double im_step,int n){
	   
	   if( this == null)
		   return null;
	   if(n == 0)
		   return null;
	   if(n < 0)
		   return null;
	   if(re_step > 7096)
		   return null;
	   if(im_step > 7096)
		   return null;
	   
	   int Len = n;
	   Complex[] array = new Complex[Len];
	   Complex[] temp_ar = new Complex[Len];
	   int i;
	  
	   Complex temp2;
	   Complex temp3;
	   double real;
	   double imaginary;
	   double real2;
	   double imaginary2;
	  
	   
	   for(i = 0; i<array.length;i++ ){
		   
		   Re += re_step;
		   Im += im_step;
		   
		   array[i] = this.complexCosine();
		   real = array[i].getRe();
		   imaginary = array[i].getIm();
		   real = 1-real;
		   imaginary = 1-imaginary;
		   temp2 = new Complex(real,imaginary);
		   real2 = array[i].getRe();
		   imaginary2 = array[i].getIm();
		   real2 = 1+real2;
		   imaginary2 = 1+imaginary2;
		   temp3 = new Complex(real2,imaginary2);
		   Complex temp4 = temp2.complexDiv(temp3);
		   
		  temp4 = temp4.complexLog();
		  
		   temp_ar[i] = temp4;
		   
		   
	   }
	   
	   return temp_ar;
   }
   
   public Complex[] complexIntegralCot(double re_step,double im_step,int n){
	   
	   if(this == null)
		   return null;
	   if(n == 0)
		   return null;
	   if(n < 0)
		   return null;
	   if(re_step > 7096)
		   return null;
	   if(im_step > 7096)
		   return null;
	   
	   int Len = n;
	   Complex[]array = new Complex[Len];
	   Complex[]temp_ar = new Complex[Len];
	   int i;
	   double real;
	   double imaginary;
	   
	   for(i = 0;i<array.length;i++){
		   
		   Re += re_step;
		   Im += im_step;
		   array[i] = this.complexCsc();
		   real = array[i].getRe();
		   imaginary = array[i].getIm();
		   real = -Math.log(real);
		   imaginary = -Math.log(imaginary);
		   Complex temp = new Complex(real,imaginary);
		   temp_ar[i] = temp;
	   }
	   
	   return temp_ar;
   }
   
   public Complex[] complexIntInvSineSqr(double re_step,double im_step,int n){
	   
	   if(this == null)
		   return null;
	   if(n == 0)
		   return null;
	   if(n < 0)
		   return null;
	   if(re_step > 7096)
		   return null;
	   if(im_step > 7096)
		   return null;
	   
	   int Len = n;
	   Complex[]array = new Complex[Len];
	   Complex a = this;
	   Complex b = this;
	   Complex c = this;
	   double real;
	   double imaginary;
	   Complex temp;
	   Complex[]temp_ar = new Complex[Len];
	   int i;
	   
	   for(i = 0;i<array.length;i++){
		   
		   Re += re_step;
		   Im += im_step;
		   b = b.complexCot();
		   temp = a.complexMul(b);
		   c = c.complexSine();
		   real = c.Re;
		   imaginary = c.Im;
		   real = Math.log(real);
		   imaginary = Math.log(imaginary);
		   Complex d = new Complex(real,imaginary);
		   Complex sum = temp.complexAdd(d);
		   array[i] = sum;
	  
		   
	   }
	   
	   return array;
   }
   
   public Complex complexHyperbolicSinAddition(Complex c){
	   
	   if(this == null)
		   return null;
	   if(c == null)
		   return null;
	   
	   Complex a = this;
	   Complex temp = c;
	   Complex temp2;
	   Complex temp3;
	   Complex temp4;
	   Complex temp5;
	   
	   a = this.complexHyperbolicSin();
	   temp = c.complexHyperbolicCos();
	   temp2 = this.complexHyperbolicCos();
	   temp3 = c.complexHyperbolicSin();
	   
	   temp4 = a.complexMul(temp);
	   temp5 = temp2.complexMul(temp3);
	   
	   return temp4.complexAdd(temp5);
	   
   }
   
   public Complex complexHyperbolicCosAddition(Complex c){
	   
	   if(this == null)
		   return null;
	   if(c == null)
		   return null;
	   
	   Complex a = this;
	   Complex temp = c;
	   Complex temp2;
	   Complex temp3;
	   Complex temp4;
	   Complex temp5;
	   
	   a = this.complexCosine();
	   temp = c.complexCosine();
	   temp2 = this.complexSine();
	   temp3 = c.complexSine();
	   temp4 = a.complexMul(temp);
	   temp5 = temp2.complexMul(temp3);
	   
	   return temp4.complexAdd(temp5);
	   
   }
   
   public Complex complexHyperbolicTanAddition(Complex c){
	   
	   if(this == null)
		   return null;
	   if( c == null)
		   return null;
	   
	   Complex a = this;
	   Complex temp = c;
	   Complex temp2;
	   Complex temp3;
	   Complex temp4;
	   Complex temp5;
	   
	   a = this.complexTan();
	   temp = c.complexTan();
	   temp2 = a.complexAdd(temp);
	   temp3 = this.complexTan();
	   temp4 = c.complexTan();
	   temp5 = temp3.complexMul(temp4);
	   double real = temp5.Re;
	   double imaginary = temp5.Im;
	   real = 1+real;
	   imaginary = 1+imaginary;
	   Complex temp6 = new Complex(real,imaginary);
	   return temp2.complexDiv(temp6);
   }
   
   public Complex complexHyperbolicCotAddition(Complex c){
	   
	   if(this == null)
		   return null;
	   if(c == null)
		   return null;
	   
	   Complex a = this;
	   Complex temp = c;
	   Complex temp2;
	   Complex temp3;
	   Complex temp4;
	   
	   a = this.complexCot();
	  temp = c.complexCot();
	  temp2 = a.complexMul(temp);
	  double real = temp2.Re;
	  double imaginary = temp2.Im;
	  real = 1+real;
	  imaginary = 1+imaginary;
	  temp3 = new Complex(real,imaginary);
	  temp4 = this.complexCot();
	  Complex temp5 = c.complexCot();
	  Complex temp6 = temp5.complexMul(temp4);
	  
	  return temp3.complexDiv(temp6);
   }
   
   public Complex complexHyperbolicSinHalfAngle(){
	   
	   if(this == null)
		   return null;
	   
	   Complex a = this;
	   Complex temp;
	   double real;
	   double imaginary;
	   a = this.complexHyperbolicCos();
	   real = a.Re;
	   imaginary = a.Im;
	   real = (real-1)/2;
	   imaginary = (imaginary-1)/2;
	   temp = new Complex(real,imaginary);
	   
	   return temp.complexSQRT();
   }
   
   public Complex complexHyperbolicCosHalfAngle(){
	   
	   if(this == null )
		   return null;
	   
	   Complex a = this;
	   Complex temp;
	   double real;
	   double imaginary;
	   
	   a = this.complexHyperbolicCos();
	   real = a.Re;
	   imaginary = a.Im;
	   real = (1+real)/2;
	   imaginary = (1+imaginary)/2;
	   temp = new Complex(real,imaginary);
	   
	   return temp.complexSQRT();
   }
   public Complex complexHyperbolicTanHalfAngle(){
	   
	   if(this == null)
		   return null;
	   
	   Complex a = this;
	   Complex temp = this;
	   Complex temp2;
	   double real;
	   double imaginary;
	   
	   a = this.complexHyperbolicSin();
	   temp = this.complexHyperbolicCos();
	   real = temp.Re;
	   imaginary = temp.Im;
	   real = 1+real;
	   imaginary = 1+imaginary;
	   temp2 = new Complex(real,imaginary);
	   
	   return a.complexDiv(temp2);
   }
   
   public Complex[] CosineIntegral(int n,double re_step,double im_step){//for testing gives wrong values
		Complex[]array ;
		
		if(this == null){
			return null;
		
		}else if(n == 0){
			throw new IllegalArgumentException("n must be greater than 0");
		}else if(n < 0){
			throw new IllegalArgumentException("n can not be less than 0");
		}else if( re_step > Math.PI){
			throw new IllegalArgumentException("re_step can not be greater than Math.Pi ");
			
		}else if(im_step > Math.PI){
			throw new IllegalArgumentException("im_step cannot be greater than Math.Pi");
		
			    
			    	
			     }else{
			    	 
			    	
			    	 int Len = n;
			    array	  = new Complex[Len];
			  
			    	 int i;
			    	 double euler_num;
			    	 
			    	 euler_num = 0.5772156649015328606065120900824024310421;
			    	 double real;
			    	
			    	 double imaginary;
			    	 
			    	 for(i = 0;i<array.length;i++){
			    		 
			    		 Re += re_step;
			    		 Im += im_step;
			    		 if(Re > Math.PI || Im > Math.PI){
			    			 break;
			    		 } if(Re > Math.PI && Im > Math.PI){
			    			 break;
			    		 } else {
			    			
			    		 
			    	Complex temp1 ;
			    	temp1 = this.complexLog();
			    	real = temp1.Re+euler_num;
			    	imaginary = temp1.Im+euler_num;
			    	Complex temp2 = new Complex(real,imaginary);
			    	Complex b = this;
			    	b = this.complexCosine();
			    	double real2 = b.Re;
			    	double imaginary2 = b.Im;
			    	real2 = real2-1;
			    	imaginary2 = imaginary2-1;
			    	b = new Complex(real2,imaginary2);
			    	Complex c = this;
			    	System.out.println("c \t"+c);
			    	c = b.complexDiv(c);
			    	Complex d;
			    	d = temp2.complexAdd(c);
			    	array[i] = d;
			    	continue;
			    	 }
			    	 }
			    	
			     }
		 return array;    
		}
		
	
	 public Complex[] SinIntegral(int n,double re_step,double im_step){
		 Complex[]array;
		 
		 if ( this == null){
			 return null;
		 }else if( n == 0){
			throw new IllegalArgumentException("n must be greater than 0");
		 }else if(n < 0){
			 throw new IllegalArgumentException(" n must be greater than 0");
		 }else{
			 
			 int Len = n;
			 array = new Complex[Len];
			 Complex a = this;
			 Complex b = this;
			 int i;
			 
			 for(i = 0;i<array.length;i++){
				 
				 Re += re_step;
				 Im += im_step;
				 
				 a = this.complexSine();
				 Complex temp;
				 temp = a.complexDiv(b);
				 array[i] = temp;
			 }
		 }
		 return array;
	 }
	 
	 public Complex SiExpansion(int c){
		 
		
		 Complex temp2;
		 if(this == null){
			 return null;
		 
		 }else if( c == 0 || c >20){
			 throw new IllegalArgumentException("upper bound c must lie beetwen 0-20");
		 }else{
			 
			
			
			 int k = 0;
			 int one = -1;
			 int exp = 1;
			 int l = 1;
			 int factorial = 1;
			 Complex a = this;
			 Complex b;
			 Complex temp;
			 double real;
			 double real2 = 0;
			 double imaginary2 = 0;
			 double imaginary;
			 double denom;
			 int counter = 0;
			 
			 do{
				 ++counter;
				 ++k;
				 exp = (2*k)+1;
				 factorial *= ++l;
				 one = (int) Math.pow(one, k);
				 
				 b = a.complexPolarPow(exp);
				 real = one*b.Re;
				 imaginary = one*b.Im;
				 temp = new Complex(real,imaginary);
				 denom = exp*factorial;
				 real2 += temp.Re/denom;
				 imaginary2 += temp.Im/denom;
				 System.out.println("real2 \t" + real2 + " imag2 \t"+ imaginary2 + "denom \t" + denom + "b \t" + b);
				 
				 temp2 = new Complex(real2,imaginary2);
				
			 }while(counter<c);
		 }
		 return temp2;
	 }
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
            
		Complex b = new Complex(10.0,10.0);
		Complex a = new Complex(0.0,0.0);
		
		System.out.println( a.complexAdd(b) );
		System.out.println(a.complexMul(b));
		System.out.println(b.magnitude());
		System.out.println(b.complexPhase());
		System.out.println(b.complexInverse());
		System.out.println(b.polarForm());
		System.out.println(b.complexSine());
		System.out.println(a.complexInverse().magnitude());
		Complex com = b.complexSine();
	System.out.println(	b.complexMul(com));
		System.out.println(b.polarMul(a));
		System.out.println(b.polarDiv(a));
		System.out.println(a.complexPolarPow(6));
		System.out.println(a.complexPolarRoot(b, 6));
		System.out.println(a.complexCosine());
		System.out.println(a.complexCot());
		System.out.println(a.complexLog());
		System.out.println(a.polarForm());
		System.out.println(a.magnitude());
		System.out.println(a.complexPhase());
		Complex[] temp = new Complex[50];
	temp =	a.complexArray1DSin(70, 0.1, 0.1);
	System.out.println(Arrays.toString(temp));//proper use of arrays must be referenced from the object
	     Complex temp2 = a.complexCosine();
	    b.complexAdd(b.complexCosine());
	    a.complexAdd(b);
	    a.complexDiv(temp2);
	  System.out.println( b.func(b));
	  System.out.println(a.complexHyperbolicCos());
	  Complex[][] temp3 = new Complex[10][10];
	  temp3 = a.complexArray2DSin(0.001,0.001, 10, 100);
	  System.out.println(Arrays.deepToString(temp3));
	  Complex[] temp4 = new Complex[50];
	  temp4 = a.complexArray1DCos(1.5, 0.1, 50);
	  System.out.println();
	  System.out.println(Arrays.toString(temp4));
	  System.out.println();
	  Complex[][] temp5 = new Complex[10][10];
	  temp5 = a.complexArray2DCos(0.1, 0.1, 10, 10);
	  System.out.println(Arrays.deepToString(temp5));
	  System.out.println(a.complexSQRT());
	  System.out.println(a.complexPhase());
	  System.out.println(a.getRe());
	  Complex d = new Complex(1.0,2.0);
	  System.out.println(d.getRe());
	  System.out.println(d.complexSQRT());
	  System.out.println(d.complexSpaceRotation(1.56));
	  System.out.println(b.complexLogAdd(d));
	 
	  Complex x = new Complex(10.5,9.0);
	  Complex y = new Complex(4.5,2.5);
	  System.out.println(x.complexLog());
	  System.out.println(y.complexLog());
	 
	  System.out.println(b.complexLogDiv(d));
	  System.out.println(x.complexLogToPow(3));
	  System.out.println(x.complexLogAdd(y));
	  System.out.println(x.complexLogFromBaseDec());
	  System.out.println(x.complexLogToBaseDec());
	  Complex z = new Complex(10.5,8.5);
	 
	  
	  System.out.println(z.complexLog());
	  System.out.println(z.complexLogDerivative());
	  Complex w = new Complex(2.5,2.0);
	  System.out.println(w.complexLogIntegral(x));
	  System.out.println(w.complexCsc());
	  System.out.println(w.complexSine());
	  System.out.println(x.complexSinAddition(z));
	  System.out.println(z.complexTanAddition(y));
	  System.out.println(z.complexCotAddition(x));
	  System.out.println(w.complexSinHalfAngle());
	  System.out.println(w.complexCosine());
	  System.out.println(w.complexSQRT());
	  w.complexCosHalfAngle();
	  System.out.println(w.complexTanHalfAngle());
	  System.out.println(z.complexSinMultiAngle());
	  System.out.println(y.complexCosMultiAngle());
	  System.out.println(z.complexTanMulti2Angle());
	  System.out.println(w.complexSinMulti3Angle());
	  System.out.println(x.complexCosMulti3Angle());
	  System.out.println(z.complexSinMulti4Angle());
	System.out.println(w.complexCosMulti4Angle());
     Complex xx = new Complex(1.1,1.5);
	System.out.println(xx.complexSinTwoFuncAdd(w));
	System.out.println(xx.complexSinTwoFuncSub(w));
	System.out.println(xx.complexCosTwoFuncSub(xx));
	System.out.println(xx.complexTanTwoFuncAdd(w));
	System.out.println(xx.complexHyperbolicTan());
	System.out.println(xx.complexHyperbolicCsc());
	System.out.println(xx.complexHyperbolicSec());
	System.out.println(xx.complexHyperbolicCos());
	Complex zz = new Complex(0.1,0.1);
	Complex[] tempor2 = new Complex[25];
	tempor2 = zz.complexIntegralSin(25,0.1,0.1);
	Complex[] tempor3 = new Complex[25];
	tempor3 = zz.complexIntegralCos(0.1, 0.1, 25);
	System.out.println(Arrays.toString(tempor3));
	Complex[] tempor4 = new Complex[25];
	 tempor4 = zz.complexIntegralTan(0.1, 0.1, 25);
	 Complex yy = new Complex(2.0,2.0);
	 Complex vv = new Complex(10.5,2.5);
	 System.out.println(vv.complexDiv(yy));
	 Complex t = yy.complexCosine();
	 System.out.println(t.complexDiv(vv.complexSine()));
	 Complex[]tempor5 = new Complex[25];
	 tempor5 = zz.complexIntegralCsc(0.1, 0.1, 25);
	 System.out.println(Arrays.toString(tempor5));
	 Complex ww = new Complex(1.0,1.0);
	 Complex[]tempor6 = new Complex[25];
	 tempor6 = ww.complexIntInvSineSqr(0.1, 0.1, 25);
	 System.out.println(Arrays.toString(tempor6));
	 System.out.println(zz.complexHyperbolicSinAddition(xx));
	Complex qq = new Complex(0.5,0.59);
	System.out.println(qq.complexHyperbolicTanAddition(qq));
	System.out.println(qq.complexHyperbolicSinHalfAngle());
	System.out.println(qq.complexHyperbolicTanHalfAngle());
	Complex[]tempor7 = new Complex[25];
	tempor7 = qq.CosineIntegral(250, 0.1, 0.1);
	System.out.println(Arrays.toString(tempor7));
	Complex zzz = new Complex(0.1,0.1);
	System.out.println(zzz.SiExpansion(8));

	  
  

	
	 
	    
	
		
		
		
		
		
		
	}

}

