/*
<h2>Copyright</h2>
Copyright (c) 2003-2005 Interworld Transport.  All rights reserved.<br>
--------------------------------------------------------------------------------
<br>
---com.interworldtransport.clados.ProductTable<br>
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
---com.interworldtransport.clados.ProductTable<br>
--------------------------------------------------------------------------------
 */
//}
package com.interworldtransport.clados;

/** 
 * Multiplication table for a Monad and associated methods.
 * <p>
 * ProductTables encapsulate Clifford Algebra multiplication tables.  ProductTables
 * are kept in a separate class so that flat Monads may share them in the future.
 * Only Monads should need to construct and own a ProductTable.  A Monad should 
 * be able to rely upon a ProductTable to handle the minutia of multiplication.
 * <p>
 * Operations in the Product Table should return logicals for tests the table
 * can answer.  They should alter inbound objects for complex requests.  At no
 * point should a Product Table have to make a copy of itself or inbound objects
 * except for private use.
 * <p>
 * A Product table will actually assume it is OK to perform the requested
 * operations and throw an exception if it discovers later that it isn't.  This
 * discovery isn't likely to be very good, so it should not be expected to work
 * well.  Only checks against primitive elements can be done.  Physical sense
 * checks need to be performed at the Monad level.
 * <p>
 * At present, each Monad will maintain a copy of its own product table.  In the
 * not too distant future, the product table should be made static or shareable
 * by all Monads using the same signature, reference frame and foot.
 * <p>
 * @version 0.80, $Date: 2005/09/29 08:36:20 $
 * @author Dr Alfred W Differ
 */

public class ProductTable extends CladosObject
{

/** 
 * This string holds the parent Monad for this product table.
 */
    private CladosObject			Parent;
/** This string holds the signature information describing the squares of all
 *  geometry generators present on the multiplication table.
 */
    private String					Signature;
    private int[]					intSignature;
/**
 * This basis holds a representation of all the elements that can be built from
 * the generators that space the algebra's vector space. 
 */
    private Basis					ABasis;
/** This array holds the geometric multiplication table for the Monad.
 *  The array contains numbers that represent the row of the Eddington
 *  Basis one would produce with a product of elements represented by
 *  the row and column of ProductResult.
 */
    private int[][]					ProductResult;
/** This integer is the number of linearly independent basis elements in the algebra.
 *  It is a count of the number of Eddington Basis elements and is used often
 *  enough to be worth keeping around.
 */
    private int						linearDim;
/** This integer is the number of independent blades in the algebra.
 *  It is equal to FrPosSig+FrNegSig+1 and is used often enough to be worth keeping.
 */
    private int						GradeCount;
/** This array is used for keeping track of where grades start and stop in the
 *  EddingtonBasis
 *  GradeRange[j][0] is the first postion for a coefficient for grade j.
 *  GradeRange[j][1] is the last postion for a coefficient for grade j.
 */
    private  int[][]				GradeRange;


/** 
 * Main constructor of ProductTable with signature information passed in.
 * It figures out the rest of what it needs.
 * @param pParent			Monad
 * @param pSig				String
 */
    public ProductTable(CladosObject pParent,	
    					String pSig)
    	throws 			BadSignatureException
	{
		//Validate pSig to ensure it has only the information we want.  Then
		//save it internally
		boolean check=this.validateSignature(pSig);
		if (check)
		{
			//Figure out linear dimension and grade count.  Both are needed often.
			this.Parent=pParent;
			this.setLinearDimension();
			this.setGradeCount();
			this.setGradeRange();
			this.ABasis=new Basis(	Parent.getName(), 
									Parent.getAlgebraName(), 
									Parent.getFootName(), 
									getGradeCount()-1);

			//Fill the ProductResult array with integers representing Eddington
			//Basis elements that show the product of two other such
			//elements.
			this.fillProductResult();
		}
		else
		{
			throw new BadSignatureException(pParent, "Valid signature was expected.");
		}
		//Fill in any other helpful things to be kept here.
	}

/** 
 * Return the signature of the generator geometry. This lists the squares of
 * the generators in their numeric order.
 * @return String
 */
    public String 	getSignature()
    {
    	return this.Signature;
    }
    
/** 
 * Get the linear dimension variable.
 * @return int
 */
    public int		getLinearDimension()
    {
    	return this.linearDim;
    }
    
/** 
 * Get the grade count variable.
 * @return int
 */
    public int		getGradeCount()
    {
    	return this.GradeCount;
    }
  
/** 
 * Return an element of the array holding the geometric multiplication rules.
 * @param	pj				int
 * @param	pk				int
 * @return int
 */
    public int	 	getProductResult(int pj, int pk)
    {
    	return this.ProductResult[pj][pk];
    }
    
/** 
 * Get start index from the GradeRange array
 * GradeRange[j][0] is the first postion for a coefficient for grade j.
 * GradeRange[j][1] is the last postion for a coefficient for grade j.
 * @param	pGrade			int
 * @return int
 */
    public int 		getGradeRangeF(int pGrade)
    {
    	return GradeRange[pGrade][0];
    }

/** 
 * Get Final index from the GradeRange array
 * GradeRange[j][0] is the first postion for a coefficient for grade j.
 * GradeRange[j][1] is the last postion for a coefficient for grade j.
 * @param	pGrade			int
 * @return int
 */
    public int 		getGradeRangeB(int pGrade)
    {
    	return GradeRange[pGrade][1];
    }

/** 
 * Set the linear dimension variable using the length of a valid Signature
 * string.
 */
    private void	setLinearDimension()
    {
    	this.linearDim=(int) Math.pow(2,Signature.length());
    }
    
/** 
 * Set the grade count variable using the length of a valid Signature 
 * string.
 */
    private void	setGradeCount()
    {
    	this.GradeCount=Signature.length()+1; //Strings start at 0
    }

/** 
 * Set the array used for keeping track of where grades start and stop in the
 * Coefficient array for this Monad.
 * GradeRange[j][0] is the first postion for a coefficient for grade j.
 * GradeRange[j][1] is the last postion for a coefficient for grade j.
 */
    private void setGradeRange()
    {
    	this.GradeRange=new int[this.GradeCount][2];

    	this.GradeRange[0][0]=1;
    	this.GradeRange[0][1]=1;

    	for (int j=1; j<this.GradeCount; j++)
    	{
    		this.GradeRange[j][0]=this.GradeRange[j-1][1]+1;
    		this.GradeRange[j][1]=this.GradeRange[j][0]+(factorial(this.GradeCount-1)/(factorial(j)*factorial(this.GradeCount-1-j)))-1;
    	}
    }

/** 
 * Set the array used for representing the geometric multiplication rules for
 * this Monad.  This method takes pairs of Eddington Basis elements, multiplies
 * them and figures out which other basis element is the result.  Standard
 * index commutation is performed while using the generator signatures to
 * eliminate pairs of indecies.  This method should only be called once when
 * the Monad is initialized.
 */
    private void fillProductResult()
    {
	this.ProductResult=new int[this.linearDim+1][this.linearDim+1];

	for (int j=1; j<this.linearDim+1;j++)
	{
		this.ProductResult[1][j]=j;
		this.ProductResult[j][1]=j;
	}        //Scalar section finished

	int[] doubleSort=new int[2*this.GradeCount-1];
	int permuteCounter=0;
	int doubleKey=0;
	int j=2;
	int k=2;
	int m=1;
	int n=1;
	int tempSort=0;
	for (j=2; j<this.linearDim+1; j++)
	{//counter for row element
		for (k=2; k<this.linearDim+1; k++)
		{//counter for column element
			permuteCounter=0;
			doubleKey=0;
			//Set up row with all generators for each basis element j and k
			for (m=1; m<this.GradeCount; m++)
			{
				//Copy EddingtonBasis' into doubleSort to find new element
				doubleSort[m]=this.ABasis.getBasis(j, m);
				doubleSort[m+this.GradeCount-1]=this.ABasis.getBasis(k, m);
			}
			m=1;
			for (m=1; m<2*this.GradeCount-1; m++)
			{
				for (n=1; n<2*this.GradeCount-2; n++)
				{
					//Swap on doubleSort
					if (doubleSort[n]>doubleSort[n+1])
					{
						tempSort=doubleSort[n];
						doubleSort[n]=doubleSort[n+1];
						doubleSort[n+1]=tempSort;
						if (!(doubleSort[n]==0 || doubleSort[n+1]==0))
						{
							permuteCounter += 1;
					 	}
					}
				}
				n=1;
			}  //end of doubleSort sort
			m=1;
			permuteCounter=permuteCounter%2;  //commutation sign tracking is being done.

			//Now we need to remove generator pairs and track signs.
			for (m=1; m<2*this.GradeCount-2; m++)
			{
				if (doubleSort[m]==0) continue;
				if (doubleSort[m]==doubleSort[m+1])
				{
					tempSort=doubleSort[m];
					doubleSort[m]=0;
					doubleSort[m+1]=0;
					m += 1;
					permuteCounter += intSignature[tempSort-1];
					//flip sign again if generator has negative square.
				}
		 	}
			m=1;
			permuteCounter=permuteCounter%2;  //commutation sign tracking is being done.

			//Now sort again.
			for (m=1; m<2*this.GradeCount-1; m++)
			{
				for (n=1; n<2*this.GradeCount-2; n++)
				{
					//Swap on doubleSort
					if (doubleSort[n]>doubleSort[n+1])
					{
						tempSort=doubleSort[n];
						doubleSort[n]=doubleSort[n+1];
						doubleSort[n+1]=tempSort;
						if (!(doubleSort[n]==0 || doubleSort[n+1]==0))
						{
							permuteCounter += 1;
						}
					}
				}//end of inside doublesort pass
				n=1;
				//*/
			}  //end of outside doubleSort sort
			m=1;
			permuteCounter=permuteCounter%2;  //commutation sign tracking is being done.

			//At this point doubleSort should be fully sorted and have no
			//duplicate generators.  Now we need to Key the basis element in
			//doubleSort to identify it
			for (m=1; m<2*this.GradeCount-1; m++)
			{//temporary counter
				doubleKey += (int) doubleSort[m]*Math.pow(this.GradeCount,2*this.GradeCount-2-m);
			}//Base (p+q) representation of Eddington Number is now in doubleKey
			m=1;
			//Compare doubleKey against EddingtonKey to find match
			this.ProductResult[j][k]=0;
			for (m=1; m<this.linearDim+1; m++)
			{//temporary counter
				if (doubleKey==this.ABasis.getBasisKey(m))
				{  //We have a match!
					this.ProductResult[j][k]= m * (int)Math.pow(-1.0,permuteCounter);
					break;        //Good enough.  Go on to next ProductResult piece
				}
			}
			m=1;
				//Found ProductResult[j][k] and sign successfully

		}//end of column k
		k=2;
	}//end of row j
    }

/** 
 * Return a measure of the validitity of the Signature string.  A string with
 * +'s and -'s will pass.  No other one should.
 * @param pSg				String
 */
    private boolean validateSignature(String pSg)
    {
	    intSignature=new int[pSg.length()];
	    for (int j=0; j<pSg.length(); j++)
	    {
	    	if (pSg.substring(j,j+1).equals("+"))
	    	{
	    		intSignature[j]=0;
	    	}
	    	else
	    	{
	    		if (pSg.substring(j,j+1).equals("-"))
	    		{
	    			intSignature[j]=1;
	    		}
	    		else
	    		{
	    			return false;
	    		}
	    	}
	    }
	    this.Signature=pSg;
	    return true;
    }

/**
 * Private factorial function.  Couldn't find one in the JDK when this package
 * first needed it.
 * @param p					int
 */
    public static final int factorial(int p)
    {
    	if (p<=0) return 1;
    	int temp=1;
    	for (int k=1; k<p+1; k++) 
    		{
    			temp *= k;
    		}
    	return temp;
    }
}

