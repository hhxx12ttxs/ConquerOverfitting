/*
<h2>Copyright</h2>
Copyright (c) 1998-2010 Interworld Transport.  All rights reserved.<br>
--------------------------------------------------------------------------------
<br>
---com.interworldtransport.clados.Monad<br>
--------------------------------------------------------------------------------
<p>
Interworld Transport grants you ("Licensee") a license to this software
under the terms of the GNU General Public License.<br>
A full copy of the license can be found bundled with this package or code file.
<p>
If the license file has become separated from the package, code file, or binary
executable, the Licensee is still expected to read about the license at the
following URL before accepting this material.
<blockquote><code>http://www.opensource.org/gpl-license.html</code></blockquote>
<p>
Use of this code or executable objects derived from it by the Licensee states
their willingness to accept the terms of the license.
<p>
A prospective Licensee unable to find a copy of the license terms should contact
Interworld Transport for a free copy.
<p>
--------------------------------------------------------------------------------
<br>
---com.interworldtransport.clados.Monad<br>
--------------------------------------------------------------------------------
*/

package com.interworldtransport.clados;

/** Multivector over Reals {Cl(p,q) x R}.
 * <p>
 * Monads encapsulate Clifford Algebra multivectors and the rules defining them.  
 * The coefficients, reference frame, multiplication rules, and a few names are 
 * contained to assist in the proper definition of physical attributes.
 * <p>
 * Proper use of this class is accomplished when one views a physical property
 * as a self-contained entity.  A planet's angular momentum is an example of a
 * property that can be fully defined by a Monad if one properly encloses the
 * coefficients, geometric rules, and reference frame within the definition.
 * Enclosing properties in this manner prevents one from making programmatic
 * errors allowed by the programming language but disallowed by the physics.
 * An example of such an error would be multiplying two vectors that are defined
 * on different reference systems or in spaces with different signatures.
 * <p>
 * Operations on the Monads are designed to alter the Monad defining the
 * operation.  The definitions of addition, multiplication, and all other
 * basic operations are carried within each instance of a Monad. Doing this
 * allows the defintions to vary from one physical object to another. Physical
 * theories requiring manifold curvature will demand this feature. The data
 * members named FtName and FrameFoot are initial attempts to support curvature.
 * <p>
 * Monad objects must be declared with at least two generators of geometry.
 * Properties not requiring a generator of geometry may be adequately defined on
 * Fields and are not intended to be covered in the clados package.
 * @version 0.80, $Date: 2010/09/07 04:22:49 $
 * @author Dr Alfred W Differ
 */
public class Monad extends CladosObject
{
/** 
 * This String is the name of the Reference Frame of the Monad
 */
    protected String		FrName;
/** 
 * This array holds the coefficients of the Monad. Support for
 * fields other than Real numbers is not handled yet.
 */
    protected double[]		Coeff;
/** 
 * This array holds the alteration information between the Reference Frame and
 * the Foot Frame.
 */
    protected double[][]	FrameFoot;
/** 
 * This class holds the ProductTable associated with the generators of the
 * geometric algebra.  It also keeps track of the signature of the algebraic
 * space and a few other odds and ends for convenience.
 */
     protected ProductTable	GProduct;


/** 
 * Simple copy constructor of Monad.
 * Passed Monad will be copied in all details. This contructor is used most 
 * often to get around operations that alter a Monad when the developer does 
 * not wish it to be altered.
 * @param pM Monad
 */
    public Monad(	Monad pM)
	{
		setName(pM.getName());
		setAlgebraName(pM.getAlgebraName());
		this.FrName = pM.getFrameName();
		setFootName(pM.getFootName());
		try 
		{
			this.GProduct = new ProductTable(this, pM.getSignature());
		}
		catch (BadSignatureException e)
		{
			; //Can't happen in a straight copy.
		}
		this.fillFrameFoot();

		double[] pC=pM.getCoeff();
		this.Coeff=new double[getLinearDimension()+1];
		if (pC.length==getLinearDimension()+1){this.setCoeff(pC);}
		else this.setCoeff(new double[getLinearDimension()+1]);
	}

/** 
 * Main copy constructor of Monad.
 * Passed Monad will be copied in all details except its name. This contructor 
 * is used most often as a starting point to generate new Monads based
 * on an old one.  Developers could just clone the old Monad and then rename it.
 * @param pName String
 * @param pM Monad
 */
    public Monad(	String pName,
    				Monad pM)
	throws 			BadSignatureException
	{
		this(	pName,
				pM.getAlgebraName(),
				pM.getFrameName(),
				pM.getFootName(),
				pM.getSignature(), pM.getCoeff());

		this.fillFrameFoot();
	}

/** 
 * Special constructor of Monad with most information passed in.  This one
 * will create the default 'Zero' Monad.
 * @param pMonadName	String
 * @param pAlgebraName	String
 * @param pFrameName	String
 * @param pFootName		String
 * @param pSig			String
 * 
 */
    public Monad(	String pMonadName,
    				String pAlgebraName,
    				String pFrameName,
    				String pFootName,
    				String pSig )
    throws 			BadSignatureException
    {
    	
    	setName(pMonadName);
    	setAlgebraName(pAlgebraName);
    	this.FrName = pFrameName;
    	setFootName(pFootName);
    	this.GProduct = new ProductTable(this, pSig);
    	
    	this.fillFrameFoot();
    	this.Coeff=new double[getLinearDimension()+1];
    }
    
/** 
 * Special constructor of Monad with most information passed in.
 * 'Special Case' strings determine the coefficients automatically.
 * 'Unit Scalar' and 'Unit PScalar' are recognized spacial cases.
 * All unrecognized strings create a 'Zero' Monad by default.
 * @param pMonadName	String
 * @param pAlgebraName	String
 * @param pFrameName	String
 * @param pFootName		String
 * @param pSig			String
 * @param pSpecial		String
 */
    public Monad(	String pMonadName,
    				String pAlgebraName,
    				String pFrameName,
    				String pFootName,
    				String pSig, 
    				String pSpecial )
	throws 			BadSignatureException
	{
		this(pMonadName, pAlgebraName, pFrameName, pFootName, pSig);
		double[] t = new double[getLinearDimension()+1];
		this.fillFrameFoot();
		if (pSpecial.equals("Unit Scalar"))
		{
			t[GProduct.getGradeRangeF(0)]=1.0;
			this.setCoeff(t);
		}
		if (pSpecial.equals("Unit PScalar"))
		{
			t[GProduct.getGradeRangeF(pSig.length()+1)]=1.0;
			this.setCoeff(t);
		}

	}

/** 
 * Main constructor of Monad with all information passed in.
 * @param pMonadName	String
 * @param pAlgebraName	String
 * @param pFrameName	String
 * @param pFootName		String
 * @param pSig			String
 * @param pC 			double[]
 */
    public Monad(	String pMonadName,
					String pAlgebraName,
					String pFrameName,
					String pFootName,
					String pSig, 
					double[] pC )
	throws 			BadSignatureException
	{

		setName(pMonadName);
		setAlgebraName(pAlgebraName);
		this.FrName = pFrameName;
		setFootName(pFootName);
		this.GProduct = new ProductTable(this, pSig);

		this.fillFrameFoot();

		this.Coeff=new double[getLinearDimension()+1];
		if (pC.length==getLinearDimension()+1){this.setCoeff(pC);}
		else this.setCoeff(new double[getLinearDimension()+1]);

	}

/** 
 * Return the name of the Reference Frame for this Monad
 * @return String
 */
    public String getFrameName()
	{
		return this.FrName;
	}

/** 
 * Return the Frame to Foot translation array for this Monad
 * This array keeps track of changes to the Reference Frame made
 * relative to the Foot of the space.
 * @return double[][]
 */
    public double[][] getFrameFoot()
	{
		return this.FrameFoot;
	}
    
/** 
 * Return the String that shows the squares of the geometric generators.
 * @return String
 */
 	public String	getSignature()
	{
		return this.GProduct.getSignature();
	}
 	
/** 
 * Return the integer that shows the grade count of the algebra.
 * @return int
 */
 	public int	getGradeCount()
	{
		return this.GProduct.getGradeCount();
	}
 	
/** 
 * The coefficients for all grades are strung together in a one dimensional 
 * array.  This method is used to know where a particular grade starts in the
 * array.  The parameter is the requested grade.  The method returns the index
 * of the first coefficient of that grade. 
 * @return pGr		int
 */
 	public int	getGradeRangeF(int pGr)
	{
		return this.GProduct.getGradeRangeF(pGr);
	}
 	
/** 
 * The coefficients for all grades are strung together in a one dimensional 
 * array.  This method is used to know where a particular grade ends in the
 * array.  The parameter is the requested grade.  The method returns the index
 * of the last coefficient of that grade. 
 * @return pGr		int
 */
 	public int	getGradeRangeB(int pGr)
	{
		return this.GProduct.getGradeRangeB(pGr);
	}
 	
/** 
 * Return the integer that shows the linear dimension of the algebra.
 * @return int
 */
 	public int	getLinearDimension()
	{
		return this.GProduct.getLinearDimension();
	}
 	
/** 
 * This method queries the product table to deliver the result of a
 * product of two basis elements.  The elements are identified by 
 * integers from zero to the number of linear dimensions.  The result 
 * is returned as a signed integer whose absolute magnitude is in the
 * same range as the inputs. 
 * @param a				int
 * @param b				int
 * @return int
 */
 	public int	getProductResult(int a, int b)
	{
		return this.GProduct.getProductResult(a, b);
	}
 	
/** 
 * Return the field Coefficients for this Monad.  
 * These coefficients are the multipliers making linear combinations of the
 * basis elements.
 * @return double[]
 */
    public double[] getCoeff()
	{
		return this.Coeff;
	}

/** 
 * Reset the name used for this Monad
 * @param String
 */
//    public void setName(String pMonadName)
//	{
//		this.Name = pMonadName;
//	}
    
/** 
 * Reset the name used for the Reference Frame for this Monad
 * This operation would take place to point out a passive rotation or translation or
 * any other alteration to the reference frame.
 * @param pFrameName		String
 */
    public void setFrameName(String pFrameName)
	{
		this.FrName = pFrameName;
	}
    
/** 
 * Reset the array used for the relation between the Reference Frame and the Foot
 * in this Monad.  Use of this method by the physical model is discouraged.
 * Copy constructors rely on this method to fully copy the details of another Monad.
 * @param pfdf				double[][]
 */
    public void setFrameFoot(double[][] pfdf)
	{int j=0;
		for(int i=0; i < getLinearDimension()+1; i++)
		{
			for(j=0; j < getLinearDimension()+1; j++)
			{
				this.FrameFoot[i][j] = pfdf[i][j];
			}
			j=0;
	 	}
	}
    
/** 
 * Reset the Coefficient array used for this Monad.
 * @param ppC				double[]
 */
    public void setCoeff(double[] ppC)
	{
		if (ppC.length == getLinearDimension()+1)
		{
			System.arraycopy(ppC, 0,this.Coeff, 0, ppC.length);
		}
		else
		{
			System.out.println("Coefficient copying failed for Monad "+getName());
			System.exit(-1);
		}
	}

/** 
 * Set the array used for initial tracking of Reference Frame variations from the
 * Foot Frame.  Alterations are tracked by how the new Reference basis elements would
 * multiply with the old set.
 */
    private void fillFrameFoot()
	{
		this.FrameFoot=new double[getLinearDimension()+1][getLinearDimension()+1];
		for (int j=0; j<getLinearDimension()+1;j++)
		{
			FrameFoot[j][j]=getProductResult(j,j);
		}
	}

//The state checking Methods.
/** 
 * Return true if more than one blade is present in the Monad.
 * @return boolean
 */
    public boolean isMultiGrade()
	{
		int sumup = 0;         //sumup is the grade count in 'this'.
		int l=0;
		for (int j=0; j<GProduct.getGradeCount(); j++)
		{
			for (l=GProduct.getGradeRangeF(j); l<GProduct.getGradeRangeB(j)+1; l++)
			{
				if (this.Coeff[l]!=0.0)
				{
					sumup++;
					break;    //Grade j found.  Move to grade j+1
				}
			}
			l=0;
		}
		if (sumup > 1.0) return true;
		return false;
	}
    
/** 
 * Return the grade number present in the coefficients of the Monad.
 * A <b>NoDefinedGradeException</b> is thrown if more than one grade is present.
 * @return int
 */
    public int isGrade() throws NoDefinedGradeException
	{

		if (this.isMultiGrade())
		{
			throw new NoDefinedGradeException(this, "Unique grade was expected.");
		}
		int l=0;
		for (int j=0; j<GProduct.getGradeCount(); j++)
		{
			for (l=GProduct.getGradeRangeF(j); l<GProduct.getGradeRangeB(j)+1; l++)
			{
				if (this.Coeff[l]!=0.0) return j;    //Grade j found.
			}
			l=0;
		}
		return 0;
	}

/** 
 * Return a boolean if the grade being checked is the grade of the Monad.
 * False is returned if  more than one grade is present.
 * @param pGrade			int
 * @return boolean
 */
    public boolean isGrade(int pGrade)
    	throws NoDefinedGradeException, BladeOutOfRangeException
	{
		if (pGrade>GProduct.getGradeCount() | pGrade<0)
		{
			throw new BladeOutOfRangeException(this, "Grade to check out of range");
		}
		if (this.isMultiGrade()) return false;
		if (this.isZero()) return true;

		for (int l=GProduct.getGradeRangeF(pGrade); l<GProduct.getGradeRangeB(pGrade)+1; l++)
		{
			if (this.Coeff[l]!=0.0) return true;
		}
		return false;
	}   //end of isGrade method

/** 
 * Return true if the Monad is nilpotent
 * @return boolean
 */
    public boolean isNilpotent()
    	throws CladosException
	{
		Monad check1 = new Monad(this);
		try
		{
			check1.LeftMultiply(this);
			if (check1.isZero()) return true;
			return false;
		}
		catch (NoReferenceMatchException e)
		{
			throw new CladosException(this, "Self reference check failure.");
		}
	}

/** 
 * Return true if the Monad an idempotent
 * @return boolean
 */
    public boolean isIdempotent() throws CladosException
	{
		Monad check1 = new Monad(this);
		try
		{
			check1.LeftMultiply(this);
			if (check1.isEqual(this)) return true;

		}
		catch (NoReferenceMatchException e)
		{
			throw new CladosException(this, "Self reference check failure.");
		}
		return false;
	}
    
/** 
 * Return true if the Monad is a multiple of an idempotent
 * @return boolean
 */
    public boolean isIdempotentMultiple() throws CladosException
	{
		if (this.isZero()) return false;
		if (this.isIdempotent()) return true;
		else
		{
			try
			{
				//What we want is to square 'this', find the first non-zero
				//coefficient, and rescale the original 'this' to see if it
				//would be an idempotent that way.  If it is, then the original
				//'this' is an idempotent multiple.

				Monad check1 = new Monad(this);
				check1.LeftMultiply(this);

				if (check1.isZero()) return false;	//this is nilpotent

				double[] temp1 = check1.getCoeff();
				double fstnzeroC=1.0;
				for(int j=1; j<getLinearDimension()+1; j++)
				{
					if (temp1[j] != 0.0)
					{
						fstnzeroC = temp1[j];	//First non-zero coefficient
						break;
					}
				}

				check1 = new Monad(this);
				check1.Scale(1.0/fstnzeroC);
				if (check1.isIdempotent()) return true;
				else return false;
			}
			catch (NoReferenceMatchException e)
			{
				throw new CladosException(this, "Self reference check failure.");
			}
		}
	}
    
/** 
 * Return true if the Monad has zeros in all coefficients.
 * @return boolean
 */
    public boolean isZero()
	{
		for (int j=1; j<getLinearDimension()+1; j++)
		{
			if (this.Coeff[j]!=0.0) return false;
		}
		return true;
	}
    
/** 
 * Return true if the Monad is identical to the passed Monad.
 * A check is made on FrameName, FootName, Signature, FrameFoot, and Coeffs
 * for equality.  No check is made for equality between MNames and the
 * ProductTables that result from signatures.
 *
 * This method is needed to compare Monads since comparing Monad instances via
 * their variable names only checks to see if both variables reference the same
 * place in memory.  It is possible for two Monads to be equal without being
 * identical.
 * @param pM				Monad
 * @return boolean
 */
    public boolean isEqual(Monad pM)
	{
		if (!this.isReferenceMatch(pM)) return false;

		double[] tempCM = pM.getCoeff();
		for(int i=1; i<getLinearDimension()+1; i++)
		{
			if (this.Coeff[i] != tempCM[i]) return false;  //Coeffs don't match
		}

		return true;
	}
    
/** 
 * Return true if the Monad shares the same Reference frame as the passed Monad.
 * A check is made on FrameName, FootName, Signature, and FrameFoot for
 * equality.  No check is made for equality between Mnames and Coeffs and the
 * Product Table
 * @param pM				Monad
 * @return boolean
 */
    public boolean isReferenceMatch(Monad pM)
	{
		double[][] tempffM = pM.getFrameFoot();

		if (!pM.getFrameName().equals(this.getFrameName())) return false;
		if (!pM.getFootName().equals(this.getFootName())) return false;
		if (!pM.getSignature().equals(this.getSignature())) return false;
		int j=0;
		for(int i=0; i<getLinearDimension()+1; i++)
		{
			for(j=0; j<getLinearDimension()+1; j++)
			{
				if (tempffM[i][j] != this.FrameFoot[i][j]) return false;
			}
			j=0;
		}
	return true;
	}

/** 
 * Return the magnitude of the Monad
 * @return double
 */
    public double MagnitudeOf()
	{
		return Math.sqrt(this.SQMagnitudeOf());
	}
    
/** 
 * Return the magnitude squared of the Monad
 */
    public double SQMagnitudeOf()
	{
		double temp=0;
		for (int j=1; j<getLinearDimension()+1; j++)
		{
			temp += this.Coeff[j]*this.Coeff[j];
		}
		return temp;
	}
    
/** 
 * Normalize the monad.
 * A <b>NoInverseException</b> is thrown if the Monad has a zero magnitude.
 */
    public void Normalize() 
    throws NoInverseException
	{
		double temp=this.SQMagnitudeOf();
		if (temp == 0.0)
		{
			throw new NoInverseException(this, "Normalizable Monads have a non-zero SQMagnitude");
		}
		else this.Scale(1/Math.sqrt(temp));
	}
    
/** 
 * The Monad is turned into its Dual
 */
    public void LocalDual()
    	throws CladosException
    {
	Monad tempM=new Monad(this);
	double[] tempC=new double[getLinearDimension()+1];
	for (int j=0; j<getLinearDimension()+1; j++)
	{
		tempC[j]=0.0;
	}
	tempC[GProduct.getGradeRangeF(getGradeCount()-1)]=1.0;
	tempM.setCoeff(tempC);      //tempM is now PScalar based on this
	try
	{
		this.LeftMultiply(tempM);
	}
	catch (NoReferenceMatchException e)
	{
		throw new CladosException(this, "Self reference check failure in local dual.");
	}
    }
    
/** 
 * The Monad is turned into its inverse if it exists.
 * <p>
 * A <b>NoInverseException</b> is thrown if the Monad is nilpotent, idempotent, or
 * an idempotent multiple.
 * <p>
 * A <b>NoInverseCalculationMethodException</b> is thrown if the Monad is multigrade.
 * Future refinements concerning when an inverse exists will lead to further refinements
 * of this method.
 * <p>
 * This method is not working as it is supposed to as of 1999/12/22.<br>
 * Some work is needed here.
 */
    public void Inverse()
    	throws NoInverseException, NoInverseCalculationMethodException, CladosException
    {

	if (this.isNilpotent()) {
		throw new NoInverseException(this, "Nilpotents have no inverse.");
		}
	if (this.isIdempotentMultiple()) {
		throw new NoInverseException(this, "Idempotent multiples have no inverse.");
		}

	Monad check1 = new Monad(this);
	try
	{
		check1.LeftMultiply(this);
		if (check1.isGrade()!= 0) throw new NoInverseCalculationMethodException(this, "Inverse on non-scalar SQ monad.");
		double[] temp = check1.getCoeff();
		if (temp[1]==0.0) throw new NoInverseCalculationMethodException(this, "Inverting a zero?");
		else temp[1] = 1 / Math.abs(temp[1]);                //The scalar coefficient is always in the [1] slot

		for(int j=1; j<getLinearDimension()+1; j++)
		{
			this.Coeff[j]=temp[1]*this.Coeff[j]*getProductResult(j,j)/Math.abs(getProductResult(j,j));
		}
	}
	catch (CladosException e)
	{
		throw new NoInverseCalculationMethodException(this, e.getSourceMessage());
	}
    } //end Inverse method

/** 
 * Mirror the sense of all geometry generators in the Monad.
 * This operation alters all grades other than scalar.  In some grades the affects cancel out.
 * Active Invert:  All odd grades switch signs, so those coefficients are altered.
 * Passive Invert:  (Not developed yet!) FrameFoot is altered to reflect refernce frame Invert
 * @param pType				String
 */
    public void Invert(String pType) {

	if (pType.equals("active")) {        //Switch coefficient signs as needed
		for (int j=1; j<GProduct.getGradeCount(); j+=2) {
			for (int l=GProduct.getGradeRangeF(j); l<GProduct.getGradeRangeB(j)+1; l++) this.Coeff[l] *= -1.0;
			}
		}
	if (pType.equals("passive")) {        //Alter FrameFoot (Refernce frame)
		System.out.println("Can't do passive Invert operations yet.");
		System.exit(-1);
		}
	}

/** 
 * Mirror the sense of all geometry generators in the Monad.
 * This operation alters all grades other than scalar.  In some grades the affects cancel out.
 * Active Invert:  All odd grades switch signs, so those coefficients are altered.
 * Passive Invert:  (Not developed yet!) FrameFoot is altered to reflect refernce frame Invert
 */
    public void Invert()
    {
	    this.Invert("active");
    }

/** 
 * Reverse the multiplicaction order of all geometry generators in the Monad.
 * Active Reversion:  Alternating pairs of grades switch signs, so those coefficients are altered.
 * Passive Reversion:  (Not developed yet!) FrameFoot is altered to reflect reference frame Reversion
 * @param pType				String
 */
    public void Reverse(String pType)
    {

	if (pType.equals("active"))
	{       //Switch coefficient signs as needed
		int k=0;
		int l=0;
		for (int j=0; j<GProduct.getGradeCount(); j++)
		{
			k=j%4;
			if (k<2) continue;
			for (l=GProduct.getGradeRangeF(j); l<GProduct.getGradeRangeB(j)+1; l++)
			{
				this.Coeff[l] *= -1.0;
			}
			l=0;
		}
	}
	if (pType.equals("passive"))
	{       //Alter FrameFoot (Refernce frame)
		System.out.println("Can't do passive Reverse operations yet.");
		System.exit(-1);
	}
    }

/** 
 * Reverse the multiplicaction order of all geometry generators in the Monad.
 * Active Reversion:  Alternating pairs of grades switch signs, so those coefficients are altered.
 * Passive Reversion:  (Not developed yet!) FrameFoot is altered to reflect reference frame Reversion
 */
    public void Reverse()
    {
	    this.Reverse("active");
    }

/** 
 * Monad Scaling:  (this * real number)
 * Only the Monad coefficients are scaled by the real number.
 * @param pScale			double
 */
    public void Scale(double pScale)
	{
		for (int j=0; j<getLinearDimension()+1; j++)
		{
			this.Coeff[j] *= pScale;
		}
	}

/** 
 * Monad Addition:  (this + pM)
 * Initial checks are made to see if both Frame and Foot names and FrameFoot match.
 * If not, a <b>NoReferenceMatchException</b> is thrown.
 * @param pM				Monad
 */
    public void Add(Monad pM) throws NoReferenceMatchException {

	if (!this.isReferenceMatch(pM)) {
		throw new NoReferenceMatchException(this, "Can't add when frames don't match.", pM);
		}
	double[] temp = pM.getCoeff();
	for(int i=1; i<getLinearDimension()+1; i++) {
		this.Coeff[i] = temp[i] + this.Coeff[i];
		}
	}

/** 
 * Monad Subtraction:  (this - pM)
 * Initial checks are made to see if both Frame and Foot names and FrameFoot match.
 * If not, a <b>NoReferenceMatchException</b> is thrown.
 * @param pM				Monad
 */
    public void Subtract(Monad pM) throws NoReferenceMatchException {

	if (!this.isReferenceMatch(pM)) {
		throw new NoReferenceMatchException(this, "Can't subtract when frames don't match.", pM);
		}
	double[] temp = pM.getCoeff();
	for(int i=1; i<getLinearDimension()+1; i++) {
		this.Coeff[i] = this.Coeff[i] - temp[i];
		}
	}

/**
 * Monad leftside multiplication:  (pM this)
 * Initial checks are made to see if both Frame and Foot names and FrameFoot match.
 * If not, a <b>NoReferenceMatchException</b> is thrown.
 * @param pM				Monad
 */
    public void LeftMultiply(Monad pM) throws NoReferenceMatchException
    {

	if (!this.isReferenceMatch(pM)) {
		throw new NoReferenceMatchException(this, "Can't left multiply when frames don't match.", pM);
		}
	int i=1;
	int j=1;
	int prd=1;
	double[] t2 = new double[getLinearDimension()+1];
	//for (j=0; j<getLinearDimension()+1; j++) t2[j]=0.0;

	double[] t1 = pM.getCoeff();
	for(i=1; i<getLinearDimension()+1; i++)
	{
		for(j=1; j<getLinearDimension()+1; j++)
		{
			prd=getProductResult(i,j);
			t2[Math.abs(prd)] += t1[i] * this.Coeff[j] * (prd/Math.abs(prd));
		}
	}//t2 now has a copy of the coefficents needed for 'this'.
	this.setCoeff(t2);
    }

/** 
 * Monad rightside multiplication:  (this pM)
 * Initial checks are made to see if both Frame and Foot names and FrameFoot match.
 * If not, a <b>NoReferenceMatchException</b> is thrown.
 * @param pM				Monad
 */
    public void RightMultiply(Monad pM) throws NoReferenceMatchException
    {
	if (!this.isReferenceMatch(pM))
	{
		throw new NoReferenceMatchException(this, "Can't right multiply when frames don't match.", pM);
	}
	int i=1;
	int j=1;
	int drp=1;
	double[] t2 = new double[getLinearDimension()+1];
	//for (int j=0; j<getLinearDimension()+1; j++) t2[j]=0.0;

	double[] t1 = pM.getCoeff();
	for(i=1; i<getLinearDimension()+1; i++)
	{
		for(j=1; j<getLinearDimension()+1; j++)
		{
			drp=getProductResult(j,i);
			t2[Math.abs(drp)] += t1[i] * this.Coeff[j] * (drp/Math.abs(drp));
		}
	}             //t2 now has a copy of the coefficents needed for 'this'.
	this.setCoeff(t2);
    }

/** 
 * Monad Grade lowering multiplication
 * Initial checks are made to see if both Frame and Foot names and FrameFoot match.
 * If not, a <b>NoReferenceMatchException</b> is thrown.
 * Dot is currently constrained to work on pure grade Monads.
 * @param pM				Monad
 */
    public void Dot(Monad pM)
	throws 	NoReferenceMatchException,
		DotDefinitionException,
		CladosException
    {

	if (!this.isReferenceMatch(pM)) {
		throw new NoReferenceMatchException(this, "Can't dot when frames don't match.", pM);
		}
	int grd=0;
	int grd2=0;
	try {
		grd = pM.isGrade();
		grd2 = this.isGrade();
		}
	catch (NoDefinedGradeException e) {
		throw new DotDefinitionException(this, "Dot defined on pure grade Monads.", pM);
		}

	Monad check1 = new Monad(pM);
	check1.LeftMultiply(this);
	this.LeftMultiply(pM);
	check1.Scale(0.5);
	this.Scale(0.5);

	if ((grd+grd2)%2 == 0) {      //this . pM is 1/2(this pM + pM this)
		this.Add(check1);
		}
	else {            //this . pM is 1/2(this pM - pM this)
		this.Subtract(check1);
		this.Scale(-1.0);
		}
    }
    
/** 
 * Monad Grade raising multiplication:  (this ^ pM)
 * Initial checks are made to see if both Frame and Foot names and FrameFoot match.
 * If not, a <b>NoReferenceMatchException</b> is thrown.
 * Wedge is currently constrained to work on pure grade Monads.
 * @param pM				Monad
 */
    public void Wedge(Monad pM)
	throws 	NoReferenceMatchException,
		WedgeDefinitionException,
		CladosException
    {

	if (!this.isReferenceMatch(pM)) {
		throw new NoReferenceMatchException(this, "Can't wedge when frames don't match.", pM);
		}
	int grd=0;
	int grd2=0;
	try {
		grd = pM.isGrade();
		grd2 = this.isGrade();
		}
	catch (NoDefinedGradeException e) {
		throw new WedgeDefinitionException(this, "Wedge defined on pure grade Monads.", pM);
		}

	Monad check1 = new Monad(pM);
	check1.LeftMultiply(this);
	this.LeftMultiply(pM);
	check1.Scale(0.5);
	this.Scale(0.5);

	if ((grd+grd2)%2 == 0) {      //this ^ pM is 1/2(this pM - pM this)
		this.Subtract(check1);
		this.Scale(-1.0);
		}
	else {            //this ^ pM is 1/2(this pM + pM this)
		this.Add(check1);
		}
    }

/** 
 * Monad Rotation:  (pM-1 this pM)
 * Initial checks are made to see if both Frame and Foot names and FrameFoot match.
 * If not, a <b>NoReferenceMatchException</b> is thrown.
 * Rotation operators are currently constrained to grade two Monads.
 * The grade two monad that is passed into this method should be of half angle magnitude.
 * Passive rotations will be supported at a later date.
 * @param pM				Monad
 */
    public void Rotate(String paction, Monad pM, double pAngle)
	throws	RotationDefinitionException,
		CantDoPassiveRotationYetException,
		CladosException
    {

	//If action is passive: Alter FrameFoot.  Alter the frame name of 'this' to match pM.
	if (!paction.equals("active")) {
		throw new CantDoPassiveRotationYetException(this, "Passive rotations aren't ready yet.");
		}
	int grade=0;
	try {
		grade = pM.isGrade();
		if ((grade == 2)) {
			Monad Unit=new Monad("Temp Unit", "test", pM.getFrameName(), pM.getFootName(), "+++", "Unit Scalar");
			Unit.Scale(Math.cos(pAngle/2));
			Monad PlaneLeft = new Monad(pM);
			PlaneLeft.Scale(Math.sin(pAngle/2));
			Monad PlaneRight = new Monad(PlaneLeft);
			PlaneLeft.Scale(-1.0);

			PlaneLeft.Add(Unit);
			PlaneRight.Add(Unit);

			this.RightMultiply(PlaneRight);
			this.LeftMultiply(PlaneLeft);
			}
		else {
			throw new RotationDefinitionException(this, "Rotation op defined on grade 2 Monads.", pM);
			}
		}
	catch (NoDefinedGradeException e) {
		throw new RotationDefinitionException(this, "Rotation op defined on pure grade 2 Monads.", pM);
		}
	catch (NoReferenceMatchException e) {
		throw new RotationDefinitionException(this, "Rotation op must share the same reference frame as this Monad.", pM);
		}
    }

/** 
 * Monad Translation:  (this > this')
 * Initial checks are made to see if both Frame and Foot names and FrameFoot match.
 * If not, a <b>NoReferenceMatchException</b> is thrown.
 * Translation operators are currently constrained to grade one Monads.
 * Operation currently alters the [0][*] and [*][0] rows and columns with action.
 * @param pM				Monad
 */
    public void Translate(Monad pM)
	throws 	NoReferenceMatchException,
		TranslationDefinitionException
    {

	if (!this.isReferenceMatch(pM)){
		throw new NoReferenceMatchException(this, "Can't translate when frames don't match.", pM);
		}
	int grade=0;
	try {
		grade = pM.isGrade();
		}
	catch (NoDefinedGradeException e) {
		throw new TranslationDefinitionException(this, "Translate op defined on grade 1 Monads.", pM);
		}
	if (!(grade == 1)) {
		throw new TranslationDefinitionException(this, "Translate op defined for grade 1 Monads.", pM);
		}
	double[] temp2 = pM.getCoeff();
	for(int i=1; i<getLinearDimension()+1; i++) {
		this.FrameFoot[0][i] = this.FrameFoot[0][i] + temp2[i];
		this.FrameFoot[i][0] = this.FrameFoot[i][0] + temp2[i];
		}
    }  //end of Translate method

/** 
 * This method suppresses all grades in the Monad not equal to the integer passed.
 * Example:  The Scalar Part operation is performed by calling GradePart(0)
 * @param pGrade			int
 */
    public void GradePart(int pGrade)
    	throws BladeOutOfRangeException
    {
	if (pGrade<0 || pGrade>GProduct.getGradeCount()) {
		throw new BladeOutOfRangeException(this, "Blade:"+pGrade+" not contained in Monad");
		}
	for (int j=0; j<GProduct.getGradeCount(); j++) {
		if (j==pGrade) continue;      //skip this grade
		for (int l=GProduct.getGradeRangeF(j); l<GProduct.getGradeRangeB(j)+1; l++) {
			this.Coeff[l]=0.0;
			}
		}
	}
    
/** 
 * This method returns the coefficients in the Monad of the grade equal to the
 * integer passed.
 * Example:  Projecting the vector part of a Monad is performed by calling
 * GradeProject(1)
 * @param pGrade			int
 * @return double[]
 */
    public double[] GradeProject(int pGrade)
    throws BladeOutOfRangeException
    {
    	if (pGrade<0 || pGrade>GProduct.getGradeCount()) 
    	{
    		throw new BladeOutOfRangeException(this, "Blade:"+pGrade+" not contained in Monad");
    	}
    	double[] temp=new double[GProduct.getGradeRangeB(pGrade)-GProduct.getGradeRangeF(pGrade)+1];
    	for (int l=GProduct.getGradeRangeF(pGrade); l<GProduct.getGradeRangeB(pGrade)+1; l++) 
    	{
    		temp[l-GProduct.getGradeRangeF(pGrade)]=this.Coeff[l];
    	}
    	return temp;
	}
    
/** 
 * This method suppresses the grade in the Monad equal to the integer passed.
 * Example:  Suppression of the bivector part of a Monad is performed by calling GradePart(2)
 * @param pGrade			int
 */
    public void GradeSupress(int pGrade)
    throws BladeOutOfRangeException
    {
    	if (pGrade<0 || pGrade>getGradeCount())
    	{
    		throw new BladeOutOfRangeException(this, "Blade:"+pGrade+" not contained in Monad");
    	}
    	for (int l=GProduct.getGradeRangeF(pGrade); l<GProduct.getGradeRangeB(pGrade)+1; l++)
    	{
    		this.Coeff[l]=0.0;
    	}
    }

}
