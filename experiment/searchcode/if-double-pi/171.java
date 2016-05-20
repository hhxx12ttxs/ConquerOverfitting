package btMath;


/***
 * @author James Birchfield
 *
 *Bad Theta
 */

public class LinMath {

	public static final double PI = 3.141592653589793;
	public static final double e= 2.71828182845904523;
	public static final int i=-1;
	public static final double phi=1.61803398874989482;  
	public static final int c= 299792458;
	public static final double h = 0.000000000000000000000000000000000626068;
	public static final double gamma = .577215664901;
	
	
	/*************Vector Math***************/
	 /*
     * returns a vector of length 1 in the direction of this vector
     */
    public Vector2D unitvec(Vector2D vec) {
        return new Vector2D(vec.getX() / vec.getMag(), vec.getY() / vec.getMag());
    }
    public Vector3D unitvec(Vector3D vec) {
    	 return new Vector3D(vec.getX() / vec.getMag(), vec.getY() / vec.getMag(), vec.getZ()/vec.getMag());
    }
    
    /**
     * Finds the amount of the vector prog in the direction of vec
     * @param vec The vector being projected on to
     * @param prog The vector being projected
     * @return The Vector representing the amount of prog in the direction of vec
     */
    public Vector2D findProjection(Vector2D vec, Vector2D prog) {
        double coeff = (dot(vec,prog) / vec.getMag());
        Vector2D ret = unitvec(vec);
        scalarMult(coeff, ret);
        return ret;
    }
    /**
     * Finds the amount of the vector prog in the direction of vec
     * @param vec The vector being projected on to
     * @param prog The vector being projected
     * @return The Vector representing the amount of prog in the direction of vec
     */
    public Vector3D findProjection(Vector3D vec, Vector3D prog) {
    	//return ScalarMult(dot(vec, prog) / dot(vec, vec), vec);
      double coeff = (dot(prog,vec) / vec.getMag());
      Vector3D ret = unitvec(vec);
      scalarMult(coeff, ret);
        return ret;
    }
    
    /**
     * Finds the Vector that is at a right angle to Vector vec
     * @param vec The direction that will be removed
     * @param orthog The vector being made orthogonal
     * @return The amount of vector orthog that is orthogonal to vec 
     */
    public Vector2D findOrthog(Vector2D vec, Vector2D orthog) {
        Vector2D proj = new Vector2D(findProjection(vec, orthog));
        Vector2D ret = new Vector2D(orthog);
        subtract(ret, proj);
        return ret;
    }
    /**
     * Finds the Vector that is at a right angle to Vector vec
     * @param vec The direction that will be removed
     * @param orthog The vector being made orthogonal
     * @return The amount of vector orthog that is orthogonal to vec 
     */
    public Vector3D findOrthog(Vector3D vec, Vector3D orthog) {
        Vector3D proj = new Vector3D(findProjection(vec, orthog));
        Vector3D ret = new Vector3D(orthog);
       // if(!proj.equals(Vector3D.Origin))//prevents accidental subtraction of -0
        	subtract(ret, proj);
        return ret;
    }
    /**
     * finds the crosss product of vector a and b
     * @param a First Vector
     * @param b Second Vector
     * @return The cross product of a and b
     */
    public Vector3D cross(Vector3D a, Vector3D b){
    	return new Vector3D( (a.getY()*b.getZ())-(a.getZ()*b.getY()),  (a.getZ()*b.getX())-(a.getX()*b.getZ()),  (a.getX()*b.getY())-(a.getY()*b.getX()) );}
    
   /**
    * Returns the smallest angle between a and b
    * @param a First vector
    * @param b Second vector
    * @return The inside angle of vector a and b
    */
    public double angleBetween(Vector2D a, Vector2D b) {
        return Math.acos(dot(a,b) / (a.getMag() * b.getMag()));
    }
    /**
     * Returns the smallest angle between a and b
     * @param a First vector
     * @param b Second vector
     * @return The inside angle of vector a and b
     */
    public double angleBetween(Vector3D a, Vector3D b){
    	return Math.acos(dot(a,b)/(a.getMag()*b.getMag()));
    }
    
    /**
     * Adds Vector b to Vector a, equivalent to a+=b
     * @param a
     * @param b
     */
    public void add(Vector2D a, Vector2D b) {
    	a.addX(b.getX());
    	a.addY(b.getY());
    	a.findQuad();
    }// end add(Vector)
   /**
    *  Adds Vector b to Vector a, equivalent to a+=b
    * @param a
    * @param b
    */
    public void add(Vector3D a, Vector2D b) {
    	a.addX(b.getX());
    	a.addY(b.getY());
    	a.findThetaquad();
    }// end add(Vector)
    /**
     *  Adds Vector b to Vector a, equivalent to a+=b
     * @param a
     * @param b
     */
    public void add(Vector3D a, Vector3D b) {
    	a.addX(b.getX());
    	a.addY(b.getY());
    	a.addZ(b.getZ());
    	a.findThetaquad();
    }// end add(Vector)
    /**
     * Adds Vector b to vector a
     * @param a
     * @param b
     * @return The resultant vector of a and b
     */
    public Vector3D Add(Vector3D a, Vector3D b) {
    	Vector3D ret = new Vector3D(
    	a.getX()+b.getX(),
    	a.getY()+b.getY(),
    	a.getZ()+b.getZ()
    	);
    	ret.findThetaquad();
    	return ret;
    }// end add(Vector)
    
    /*
     * subtracts Vector2D b from Vector2D a  equivalent to a-=b
     */
    public void subtract(Vector2D a, Vector2D b) {
    	a.addX(b.getX()*-1);
    	a.addY(b.getY()*-1);
    	a.findQuad();
    }
    public void subtract(Vector3D a, Vector2D b) {
    	a.addX(b.getX()*-1);
    	a.addY(b.getY()*-1);
    	a.findThetaquad();
    }
    public void subtract(Vector3D a, Vector3D b) {
    	a.addX(b.getX()*-1);
    	a.addY(b.getY()*-1);
    	a.addZ(b.getZ()*-1);
    	a.findThetaquad();
    }
    public Vector3D Subtract(Vector3D a, Vector3D b){
    	Vector3D ret = new Vector3D(
    	    	a.getX()-b.getX(),
    	    	a.getY()-b.getY(),
    	    	a.getZ()-b.getZ()
    	    	);
    	    	ret.findThetaquad();
    	    	return ret;
    }
    
    /*
     * multiplies the magnitude by the number s by Vector2D vec
     */
    public void scalarMult(double s, Vector2D vec) {
    	vec.multX(s);
    	vec.multY(s);
        vec.findQuad();      
    }// end scalarMult
    public void scalarMult(double s, Vector3D vec) {
    	vec.multX(s);
    	vec.multY(s);
    	vec.multZ(s);
        vec.findThetaquad();      
    }// end scalarMult
    public Vector3D ScalarMult(double s, Vector3D vec) {
    	Vector3D ret = new Vector3D(
    	vec.getX()*s,
    	vec.getY()*s,
    	vec.getZ()*s
    	);
        ret.findThetaquad();
        return ret;
    }// end scalarMult
    
    
    
    /*
     * returns the dot product of Vector a and Vector b
     */
    public double dot(Vector2D a, Vector2D b) {
        return ((a.getX() * b.getX()) + (a.getY() * b.getY()));
    }//returns the dot product of this vector and the given vector a
    public double dot(Vector3D a, Vector3D b) {
    return (a.getX()*b.getX())+(a.getY()*b.getY())+(a.getZ()*b.getZ());
    }
    
    /*
     * Changes the direction of the magnitude of xcomp 180 degrees or pi radians
     */
    public void reflectX(Vector2D vec) {
        vec.multX(-1);
        vec.findQuad();
    }
    public void reflectX(Vector3D vec) {
        vec.multX(-1);
        vec.findThetaquad();
    }

    /*
     * Changes the direction of the magnitude of ycomp 180 degrees or pi radians
     */
    public void reflectY(Vector2D vec) {
        vec.multY(-1);
       vec.findQuad();
    }
    public void reflectY(Vector3D vec) {
        vec.multY(-1);
        vec.findThetaquad();
    }

    /*
     * Changes the direction of the magnitude of z 180 degrees or pi radians
     */
    public void reflectZ(Vector3D vec){
    	vec.multZ(-1);
    	vec.findThetaquad();
    }
    
    /*
     * Reflects the vector 180 degrees
     */
    public void reflect(Vector2D vec) {
    	vec.multX(-1);
    	vec.multY(-1);
    	vec.findQuad();
    }
    public void reflect(Vector3D vec) {
    	vec.multX(-1);
    	vec.multY(-1);
    	vec.multZ(-1);

    	vec.findThetaquad();
    }
    
    public Vector2D rotate(Vector2D vec, double angle){
    	float px= (float) ((vec.getX()*Math.cos(angle)) - (vec.getY()*Math.sin(angle)));
    	float py= (float) ((vec.getX()*Math.sin(angle)) + (vec.getY()*Math.cos(angle)));
    	return new Vector2D(px,py);
    }
    
    public Vector2D rotate(Vector2D vec, double angle, Vector2D pt){
    	vec.addX(-pt.getX());
    	vec.addY(-pt.getY());
    	Vector2D ret = rotate(vec, angle);
    	vec.addX(pt.getX());
    	vec.addY(pt.getY());
    	ret.addX(pt.getX());
    	ret.addY(pt.getY());
    	return ret;
    }
    
    public Vector3D rotate(Vector3D Start_norm, Vector3D End_norm, double angle, Vector3D End_v){
    	
    	Vector3D n = unitvec(new Vector3D(Start_norm,End_norm)); 
    	Vector3D v = new Vector3D(Start_norm,End_v); 
    	Vector3D rot = new Vector3D(		
    	Add(
    		Add( ScalarMult( Math.cos(angle), new Vector3D(v)),  ScalarMult( (1-Math.cos(angle))*(dot(n,v)), new Vector3D(n))),
    		ScalarMult(Math.sin(angle), cross(n,v)))
    		);
    	add(Start_norm, rot);
    	return Start_norm;
    	   	
    }//end rotate
    
    
    public double distance(Vector2D a ,Vector2D b){return   Math.sqrt( ((a.getX()-b.getX())*(a.getX()-b.getX())) + ((a.getY()-b.getY())*(a.getY()-b.getY())) );}
    public double toDegrees(double rads){return rads*(180/PI);}
	public double toRadians(double degs){return degs*(PI/180);}
    
    /***********Quaternion Math*****************/
    

 /**
  *Normalizes the Quaternion q 
  * @param q Quaternion to be normalized
  * @return The normalized version of q
  */
 public Quaternion Quatnorm(Quaternion q) {
	 double mag= Math.sqrt((q.getW()*q.getW()) + (q.getX()*q.getX()) + (q.getY()*q.getY()) + (q.getZ()*q.getZ()));
     return new Quaternion( q.getW()/mag, q.getX()/mag, q.getY()/mag, q.getZ()/mag);   
    }
    
 /**
  * Returns the complex conjugate of the q
  * @param q Quaternion to get the conjugate of
  * @return The conjugate of q
  */
 public Quaternion Quatconjugate(Quaternion q) {
        return new Quaternion(q.getW(), -q.getX(), -q.getY(), -q.getZ());
}
    
 /**
  * Adds b to a
  * @param a Quaternion to be added to
  * @param b Quaternion to add
  * @return The sum of a and b
  */
 public Quaternion Quatplus(Quaternion a,Quaternion b) {
        return new Quaternion(a.getW()+b.getW(), a.getX()+b.getX(), a.getY()+b.getY(), a.getZ()+b.getZ());
    }
   
 
 /**
  * Applies the rotation represented by a to the point vec   
  * @param a The Quaternion representation of a rotation
  * @param vec The point to be rotated
  * @return The new point
  */
 public Vector3D Quatrotate(Quaternion a, Vector3D vec) {
	 if(vec.equals(new Vector3D(0,0,0)))
		 return vec;
	 
	 double len = vec.getMag();
     // p'= qpq* 
     // p is quaternion [0,(x,y,z)]
     // p' is the rotatet result
     // q  is the unit quaternion representing a rotation
     // q* is the conjugated q
     Quaternion vq = new Quaternion(0.0f, vec.getX() ,vec.getY() ,vec.getZ() );
     Quaternion rotated = Quattimes( Quattimes( a , vq ) , Quatconjugate(a) );
     
     return ScalarMult(len, unitvec(new Vector3D( rotated.getX(), rotated.getY(), rotated.getZ())));
    
 }
 /**
  * Applies the rotation represented by a to the point vec around the point 
  * @param a The Quaternion representation of a rotation
  * @param vec The point to be rotated
  * @param point The point to rotate around
  * @return The point rotated by a around point
  */
 public Vector3D Quatrotate(Quaternion a, Vector3D vec, Vector3D point) {
	 if(vec.equals(point))
		 return vec;
	 Vector3D ret = Subtract(vec, point);
	 ret = Quatrotate(a, ret);
	 add(ret, point);
	 return ret;
 }
 
 /**
  * Multiplies a by b and combines rotations represented by a and b
  * @param a First Quaternion
  * @param b Second Quaternion
  * @return The product of a and b
  */
 public Quaternion Quattimes(Quaternion a, Quaternion b) {
     
     double w = a.getW()*b.getW() - a.getX()*b.getX() - a.getY()*b.getY() - a.getZ()*b.getZ();
     double x = a.getW()*b.getX() + a.getX()*b.getW() + a.getY()*b.getZ() - a.getZ()*b.getY();
     double y = a.getW()*b.getY() - a.getX()*b.getZ() + a.getY()*b.getW() + a.getZ()*b.getX();
     double z = a.getW()*b.getZ() + a.getX()*b.getY() - a.getY()*b.getX() + a.getZ()*b.getW();
     return new Quaternion(w, x, y, z);
 }
    
 /**
  * Gets the inverse of q
  * @param q The Quaternion whose inverse you need
  * @return The inverse of q
  */
 public Quaternion Quatinverse(Quaternion q) {
	 Quaternion con = this.Quatconjugate(q);
     double d = q.getW()*q.getW() + q.getX()*q.getX() + q.getY()*q.getY() + q.getZ()*q.getZ();
     return new Quaternion(con.getW()/d, -con.getX()/d, -con.getY()/d, -con.getZ()/d);
 }
 /**
  * Divides a by b
  * @param a First Quaternion
  * @param b Second Quaternion
  * @return The quotient of a and b
  */
 public Quaternion Quatdivide(Quaternion a, Quaternion b) {
     return Quattimes( Quatinverse(a), b);
 }
 /**
  * Returns the Matrix representation of the Quaternion
  * @param q Quaternion to be converted to a matrix
  * @return The Matrix representation of q
  */
 public Matrix getRotationMatrix(Quaternion q)
 {
 	float[] mat = new float[16];

 	float xx      = (float) (q.getX() * q.getX());
 	float xy      = (float) (q.getX() * q.getY());
 	float xz      = (float) (q.getX() * q.getZ());
 	float xw      = (float) (q.getX() * q.getW());
 	float yy      = (float) (q.getY() * q.getY());
 	float yz      = (float) (q.getY() * q.getZ());
 	float yw      = (float) (q.getY() * q.getW());
 	float zz      = (float) (q.getZ() * q.getZ());
 	float zw      = (float) (q.getZ() * q.getW());
     mat[0]  = 1 - 2 * ( yy + zz );
     mat[1]  =     2 * ( xy - zw );
     mat[2]  =     2 * ( xz + yw );
     mat[4]  =     2 * ( xy + zw );
     mat[5]  = 1 - 2 * ( xx + zz );
     mat[6]  =     2 * ( yz - xw );
     mat[8]  =     2 * ( xz - yw );
     mat[9]  =     2 * ( yz + xw );
     mat[10] = 1 - 2 * ( xx + yy );
     mat[3]  = mat[7] = mat[11] = mat[12] = mat[13] = mat[14] = 0;
     mat[15] = 1;

     Matrix ret = new Matrix(mat, 4, 4);
     
 	return ret;
 }
 
 /**********Matrix Math*********************/
 
 public Matrix transposeMatrix(float[] mat)
 {
 	float[] tran = new float[16];

 	tran[0] = mat[0];
 	tran[1] = mat[4];
 	tran[2] = mat[8];
 	tran[3] = mat[12];
 	tran[4] = mat[1];
 	tran[5] = mat[5];
 	tran[6] = mat[9];
 	tran[7] = mat[13];
 	tran[8] = mat[2];
 	tran[9] = mat[6];
 	tran[10] = mat[10];
 	tran[11] = mat[14];
 	tran[12] = mat[3];
 	tran[13] = mat[7];
 	tran[14] = mat[11];
 	tran[15] = mat[15];
 	
 	Matrix ret = new Matrix( tran , 4 , 4 );
 	
 	return ret;
 }
 
 public float[] multiplyVector(float[] m1, float[] m2)
 	{
	    	if (m1.length != m2.length)
	            throw new IllegalArgumentException("Vectors need to have the same length");
	        float[][] m = new float[m1.length][m1.length];
	        for (int i=0; i<m1.length; i++)
	            for (int j=0; j<m1.length; j++)
	                m[i][j] = (m1[i]*m2[j]);
	 
	        int id = 0;
	 
	        float[] newArray = new float[16];
	 
	        for (int j=0; j<4; j++)
	        {
	        	newArray[id] = m[0][j];
	        	id++;
	        }
	 
	        for (int j=0; j<4; j++)
	        {
	        	newArray[id] = m[1][j];
	        	id++;
	        }
	 
	        for (int j=0; j<4; j++)
	        {
	        	newArray[id] = m[2][j];
	        	id++;
	        }
	 
	        for (int j=0; j<4; j++)
	        {
	        	newArray[id] = m[3][j];
	        	id++;
	        }
	 
	        return newArray;
	    }
	
	 /*Pre: mat has the same row and col values as this Matrix
		 * Post:adds each element of mat to this Matrix
		 */
 public void Add(Matrix mat1, Matrix mat2){
			if(mat1.getNumCol() == mat2.getNumCol() && mat1.getNumRow() == mat2.getNumRow()){
			for(int x=0; x<mat1.getNumCol(); x++){
				for(int y=0; y<mat1.getNumRow(); y++){
					mat1.chgElement(x, y, mat1.getElement(x, y)+mat2.getElement(x, y));
				}
			}
			}
		}//end Add
		
		/*Pre: mat has the same row and col values as this Matrix
		 * Post:subtracts each element of mat from this Matrix
		 */
 public void Subtract(Matrix mat1,Matrix mat2 ){
			if(mat1.getNumCol() == mat2.getNumCol() && mat1.getNumRow() == mat2.getNumRow()){
				for(int x=0; x<mat1.getNumCol(); x++){
					for(int y=0; y<mat1.getNumRow(); y++){
						mat1.chgElement(x, y, mat1.getElement(x, y)-mat2.getElement(x, y));
					}
				}
				}
		}//end Subtract
		
 public Matrix times(Matrix mat1, Matrix mat2){
			if(mat1.getNumCol() != mat2.getNumRow()){throw new IllegalArgumentException("Check rows and Coloumns");}
			
			Matrix res= new Matrix(mat2.getNumRow(), mat1.getNumCol());
				
			for(int i=0; i<mat2.getNumRow(); i++){
				for(int j=0; j<mat1.getNumCol(); j++){
					for(int k=0; k<res.getNumCol(); k++){
						res.chgElement(i, j, mat1.getElement(i,k) * mat2.getElement(k, j));
					}//end k
				}//end j
			}//end i
			
			return res;
		}//end times
		
 public Matrix transpose(Matrix m) {
			Matrix mat = new Matrix(m.getNumRow(), m.getNumCol());
			for (int x = 0; x < m.getNumCol(); x++) {
				for (int y = 0; y < m.getNumRow(); y++) {
					mat.chgElement(y, x, m.getElement(x, y));
				}
			}
			return mat;
		}
		
 public Matrix SubMatrix(Matrix mat,int startRow, int startCol, int width, int length){
	 
	 float[] retmat = new float[width*length];
	 int place=0;
	 for(; startRow<width; startRow++){
			for(; startCol<length; startCol++){
				
					retmat[place] = mat.getElement(startRow, startCol);
				
				place++;
			}
		}
	 return new Matrix(retmat, width, length);
 }
		
		
public static Matrix createIdentityMatrix(int size) {
	Matrix mat = new Matrix(size, size);
	for (int i = 0; i < size; i++) {
		mat.chgElement(i, i, 1);
	}
	return mat;
}
		
/**************************Complex Numbers******************************/		
/**
 * Adds two complex numbers together
 * @param a First Complex Number
 * @param b Second Complex Number
 * @return The sum of a and b
 */
public Complex Add(Complex a, Complex b){
	return new Complex( a.getReal()+b.getReal() , a.getImaginary()+b.getImaginary() );
}
/**
 * Adds b to a: a+=b
 * @param a First Complex Number
 * @param b Second Complex Number
 */
public void add(Complex a, Complex b){
	a.setReal( a.getReal()+b.getReal() ); 
	a.setImaginary( a.getImaginary()+b.getImaginary() );
}		

/**
 * Subtracts two complex numbers together
 * @param a First Complex Number
 * @param b Second Complex Number
 * @return The result of a and b
 */
public Complex Subtract(Complex a, Complex b){
	return new Complex( a.getReal()-b.getReal() , a.getImaginary()-b.getImaginary() );
}
/**
 * Subtracts b from a: a-=b
 * @param a First Complex Number
 * @param b Second Complex Number
 */
public void subtract(Complex a, Complex b){
	a.setReal( a.getReal()-b.getReal() ); 
	a.setImaginary( a.getImaginary()-b.getImaginary() );
}		

/**
 * Multiplies two Complex numbers
 * @param a First Complex number
 * @param b Second Complex number
 * @return The product of a and b
 */
public Complex Multiply(Complex a, Complex b){
	double real = (a.getReal()*b.getReal()) - (a.getImaginary()*b.getImaginary());
	double imaginary = (a.getReal()*b.getImaginary()) + (a.getImaginary()*b.getReal());
	return new Complex(real, imaginary);
}
/**
 * Multiplies a by b: a*=b;
 * @param a First Complex number
 * @param b Second Complex number
 */
public void multiply(Complex a, Complex b){
	double real = (a.getReal()*b.getReal()) - (a.getImaginary()*b.getImaginary());
	double imaginary = (a.getReal()*b.getImaginary()) + (a.getImaginary()*b.getReal());
	a.setReal(real);
	a.setImaginary(imaginary);
}

/**
 * Divides a by b
 * @param a Numerator
 * @param b Denominator
 * @return The quotient of a and b
 */
public Complex Divide(Complex a, Complex b){
	double div = ComplexSquare(b);
	Complex ret = Multiply( a , conjugate(b) );
	ret.setReal( ret.getReal()/div );
	ret.setImaginary( ret.getImaginary()/div );
	return ret;
}
/**
 * Divides a by b: a/=b
 * @param a Numerator
 * @param b Denominator
 */
public void divide(Complex a, Complex b){
	double div = ComplexSquare(b);
	a = Multiply( a, conjugate(b) );
	a.setReal( a.getReal()/div );
	a.setImaginary( a.getImaginary()/div );
}

/**
 * Finds the complex conjugate of the complex number
 * @param con The number to conjugate
 * @return The complex conjugate of con
 */
public Complex conjugate(Complex con){
	return new Complex( con.getReal(), -con.getImaginary());
}
/**
 * Squares a Complex number 
 * @param c The complex number to be squared
 * @return The square of c
 */
public double ComplexSquare(Complex c){
	return (c.getReal()*c.getReal()) + (c.getImaginary()*c.getImaginary());
}

}//end LinMath

