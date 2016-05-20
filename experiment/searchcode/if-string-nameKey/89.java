/*
<h2>Copyright</h2>
Copyright (c) 2002-2010 Interworld Transport.  All rights reserved.<br>
--------------------------------------------------------------------------------
<br>
---com.interworldtransport.clados.Nyad<br>
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
---com.interworldtransport.clados.Nyad<br>
--------------------------------------------------------------------------------
*/

package com.interworldtransport.clados;
import java.util.*;

/** Multivector Lists over Reals   {Cl(p,q)x...xCl(p,q) over R}.
 * <p>
 * Nyads encapsulate related multivectors from Clifford Algebras and the rules
 * relating them.  This class keeps a list of Monads and definitions for legal
 * mathematical operations upon them.
 * <p>
 * Proper use of this class is accomplished when one views a physical property
 * as a self-contained entity and a list of properties as a description of a
 * real physical object.  An electron's motion demonstrates a need for a multivector
 * list because the properties of the electron can be listed independent of its
 * motion.  Enclosing related properties in a list enables a complete instance
 * representing an observable and prevents one from making programmatic errors
 * allowed by the programming language but disallowed by the physics.
 * <p>
 * Nyad objects must be declared with at least one Monad that has at least
 * two generators of geometry.  Each Monad on the list must have the same Foot.
 * At present, the Nyad permits the encapsulated Monads to be members of
 * geometric algebras of different metric and dimensionality.
 * <p>
 * @version 0.80, $Date: 2010/09/07 04:22:49 $
 * @author Dr Alfred W Differ
 */

public class Nyad extends CladosObject
{

/**
 * This integer keeps track of the number of Monads in this Nyad
 */
    protected	int				Order=1;
/**
 * This array is the list of Monads that makes up the Nyad.  It will be tied to
 * the Foot members of each Monad as keys.
 */
    protected 	ArrayList		MList;
/**
 * This array is the list of names for the Monads that make up the Nyad.
 */
    private 	String[]		NameKey;
/**
 * This array is the list of names for the Monads that make up the Nyad.
 */
    private 	String[]		AlgebraKey;
/**
 * This array is the list of frame names for the Monads that make up the Nyad.
 */
    private 	String[]		FrameKey;



/**
 * Simple copy constructor of a Nyad.  The passed Nyad will be copied
 * in detail.  This contructor is used most often to get around operations that
 * alter one of the nyads when the developer does not wish it to be altered.
 * @param pN			Nyad
 */
    public Nyad(	Nyad pN)
    {
    	this(pN.getName(), pN);
    }

/**
 * A simple copy constructor of a Nyad.  The passed Nyad will be
 * copied without the name.  This contructor is used most often to clone other
 * objects in every way except name.
 * @param pName			String
 * @param pN			Nyad
 */
    public Nyad(	String pName,
    				Nyad pN)
    {
    	this.setName(pName);
    	this.setFootName(pN.getFootName());
    	this.setAlgebraName(pN.getAlgebraName());
    	ArrayList temp = pN.getMList();
    	this.Order=temp.size();
    	this.MList=new ArrayList(Order);
    	
    	for (int j=0; j<Order; j++)
    	{
    		MList.add(new Monad((Monad)temp.get(j)));
    	}
    	this.setNameKey();
    	this.setFrameKey();
    	this.setAlgebraKey();
    	
    	
    }

/**
 * A basic constructor of a Nyad that starts with a Monad.  The Monad wil be
 * copied and placed at the top of the list.
 * @param pName			String
 * @param pM			Monad
 */
    public Nyad(	String pName,
    				Monad pM)
    {
    	setName(pName);
    	setFootName(pM.getFootName());
    	this.setAlgebraName(pM.getAlgebraName());
    	Order=1;
    	MList=new ArrayList(Order);
   		MList.add(new Monad(pM));

    	this.setNameKey();
    	this.setFrameKey();
    	this.setAlgebraKey();
    }

/**
 * Default contructor for the Nyad.  All members are left at null.
 */
 	public Nyad()
 	{
 		this.setName("None");
    	this.setFootName("None");
    	this.Order=0;
 		this.MList=new ArrayList(1);
 		this.NameKey=new String[1];
 		this.FrameKey=new String[1];
 		this.AlgebraKey=new String[1];
	}

/**
 * Return the name of this Nyad
 * @return int
 */
    public int getOrder()
    {
    	return this.Order;
    }

    
/**
 * Return the array of Monads
 * @return ArrayList (of Monads)
 */
    public ArrayList getMList()
    {
    	return this.MList;
    }

 /**
 * Return the element of the array of Monads at the jth index.
 * @param pj	int
 * @return Monads
 */
    public Monad getMList(int pj)
    {
    	return (Monad)MList.get(pj);
    }
    
/**
 * Return the array of Monad names
 * @return String[]
 */
    public String[] getNameKey()
    {
    	return this.NameKey;
    }
    
/**
 * Return the array of Algebra names
 * @return String[]
 */
    public String[] getAlgebraKey()
    {
    	return this.AlgebraKey;
    }
/**
 * Return the array of Monad frame names
 * @return String[]
 */
    public String[] getFrameKey()
    {
    	return this.FrameKey;
    }
    
/**
 * Set the name of this Nyad
 * @param String NewMonadName
 */
    private void setOrder()
    {
    	this.Order=this.MList.size();
    }
    
/**
 * Set the Name array of this Nyad.  The Monad List must be set first.
 */
    private void setNameKey()
    {
    	if (MList==null) return;
    	//now fill the foot array so the Monads may be keyed for later operations.
    	this.NameKey = new String[MList.size()];
    	for (int j=0; j<MList.size(); j++)
    	{
    		Monad temp=(Monad)MList.get(j);
    		this.NameKey[j] = temp.getName();
    	}
    }
    
/**
 * Set the Name array of this Nyad.  The Monad List must be set first.
 */
    private void setAlgebraKey()
    {
    	if (MList==null) return;
    
    	this.AlgebraKey = new String[MList.size()];
    	for (int j=0; j<MList.size(); j++)
    	{
    		Monad temp=(Monad)MList.get(j);
    		this.AlgebraKey[j] = temp.getAlgebraName();
    	}
    }
    

/**
 * Set the Frame name array of this Nyad.  The Monad List must be set first.
 */
    private void setFrameKey()
    {
    	if (MList==null) return;
    	//now fill the foot array so the Monads may be keyed for later operations.
    	this.FrameKey = new String[MList.size()];
    	for (int i=0; i<MList.size(); i++)
    	{
    		Monad temp=(Monad)MList.get(i);
    		FrameKey[i] = new String(temp.getFrameName());
    	}
    }
/**
 * Return with the integer position for the monad that belongs to the algebra
 * named in the parameter.  Return with -1 if the Monad cannot be found.
 * @param pAlg String
 * @return int
 */
    private int findPosition(String pAlg)
    {
    	for(int j=0; j<AlgebraKey.length; j++)
    	{
    		if (pAlg.equals(AlgebraKey[j]))
    		{
    			return j;
    		}
    	}
    return -1;
    }
    
/**
 * Add another Monad to the list of monads in this nyad.  This method creates
 * a new copy of the Monad offered as a parameter, so the Nyad does not wind
 * up referencing the passed Monad.
 * @param pM				Monad
 */
    public void addMonad(Monad pM)
    throws ListAppendException
    {
    	//A check should be made to ensure pM is OK to use to make a Nyad.
    	if (pM.getFootName().equals(getFootName()))
    	{
    		int testit=findPosition(pM.getAlgebraName());
    		if (testit>=0)			//No algebra names should repeat.
    		{
    			throw new ListAppendException(this, "Nyads should have unique algebra names", pM);
    		}
    		else
    		{
    			//Add Monad to the ArrayList
    			this.MList.ensureCapacity(MList.size()+1);
    			boolean test2=MList.add(new Monad(pM));
    			if (test2)
    			{
    				this.setOrder();
    				this.setNameKey();
    				this.setFrameKey();
    				this.setAlgebraKey();
    				this.setAlgebraName(this.getAlgebraName()+"|"+pM.getAlgebraName());
    			}
    		}
    	}
    	else
    	{
    		throw new ListAppendException(this, "Nyads should not have foot name mismatch", pM);
    	}
    }
    
/**
 * Remove a Monad on the list of monads in this nyad.
 * @param pM				Monad
 */
    public void removeMonad(Monad pM)
    throws ListRemoveException
    {
    	int testfind=findPosition(pM.getAlgebraName());
    	if (testfind<0)
    	{
    		throw new ListRemoveException(this, "Can't find the Monad to remove.", pM);
    	}
    	else
    	{
    		this.removeMonad(testfind);
    	}
    }
    
/**
 * Remove a Monad on the list of monads in this nyad.
 * @param pthisone			int
 */
    public void removeMonad(int pthisone)
    {
    	if (pthisone<MList.size())
    	{
    		try 
    		{
    			Monad test=(Monad)MList.remove(pthisone);
            	if (test!=null)
            	{
            		this.setOrder();
            		this.setNameKey();
            		this.setFrameKey();
            		this.setAlgebraKey();
            		StringBuffer tempName=new StringBuffer("|");
            		for (int j=0; j<AlgebraKey.length; j++)
            		{
            			tempName.append(AlgebraKey[j]);
            		}
            		this.setAlgebraName(tempName.toString());
            	}
    		}
    		catch (IndexOutOfBoundsException e)
    		{
    			//Don't act at all.
    		}
    		
    	}
    }
    
/**
 * This method takes the Monad at the k'th position in the list and swaps it for 
 * the one in the k+1 position there is one there.
 * @param key				int
 */
    public void push(int key)
    {
    	int limit=MList.size();
    	if (key>=0 && key<limit-1)
    	{
    		Monad temp=(Monad)MList.remove(key);
    		MList.add(key+1, temp);
    		setNameKey();
    		setFrameKey();
    	}
    	else
    	{
    		return;	//Monad already at the end.
    	}
    }
    
/**
 * This method takes the Monad at the k'th position in the list and swaps it for 
 * the one in the k-1 position if there is one there.
 * @param key				int
 */
    public void pop(int key)
    {
    	int limit=MList.size();
    	if (key>0 && key<limit)
    	{
    		Monad temp=(Monad)MList.remove(key-1);
    		MList.add(key, temp);
    		setNameKey();
    		setFrameKey();
    	}
    	else
    	{
    		return;	//Monad already at the beginning.
    	}
    }
    
/**
 * This method performs a strong test for a reference match.  All properties of 
 * the Nyads must match except for the Nyad names.  The monads within the Nyad
 * must also be reference matches for pairs from the same algebra.  There must
 * also be NO unpaired monads, so the algebra keys have to be identical to
 * within sorting.  
 * Only monads sharing the same Algebra name need to be checked against each 
 * other for reference matches.  For those in the same algebra, we make use of 
 * the isRefereceMatch method and compare the two.
 */

    public boolean isReferenceMatch(Nyad pML) 
    {
    	//Check first to see if the Nyads are of the same order.  Return false
    	//if they are not.
    	if (this.getOrder() != pML.getOrder()) return false;
    	
    	//Check to see if the foot names match
    	if (this.getFootName() != pML.getFootName()) return false;
    	
    	//Check to see if the Nyad algebra names match up.
    	if (this.getAlgebraName() != pML.getAlgebraName()) return false;
    	
    	//Now we start into the Monad lists.  Find a monad from this and its 
    	//counterpart in other.  If they are a reference match, move on.  If not
    	//return a false result.
    	//Because the nyads must be of the same order at this point, sifting 
    	//through one of them will detect unmatched monads.
    	
    	String[] oAlgKey=pML.getAlgebraKey();
    	boolean check=false;
    	for (int j=0; j<AlgebraKey.length; j++)
    	{
    		check=false;
    		for (int k=0; k<oAlgKey.length; k++)
    		{
    			if (AlgebraKey[j].equals(oAlgKey[k]))
    			{
    				check=true;
    				Monad testthis=(Monad)MList.get(j);
    				Monad testother=(Monad)pML.getMList(k);
    				if (!testthis.isReferenceMatch(testother)) return false;
    				break;
    			}
    		}
    		//if check is true a match was found
    		if (!check) return false;
    	}
    	
    	//Making it this far implies that all tests have passed.  pML is a
    	//strong reference match for this Nyad.
    	return true;
    }

/**
 * This method performs a weak test for a reference match.  All properties of 
 * the Nyads must match except for the Nyad names and orders.  
 * The monads within the Nyad must also be reference matches for pairs from the 
 * same algebra.  It is NOT required that monads from this nyad have 
 * counterparts in the other nyad, so the passed Nyad may have a different order 
 * than this one.  Unpaired monads are counted as matches against scalars from
 * the field.
 * Only monads sharing the same Algebra name need to be checked against each 
 * other for reference matches.  For those in the same algebra, we make use of 
 * the isRefereceMatch method and compare the two.
 */

    public boolean isWeakReferenceMatch(Nyad pML) 
    {     	
    	//Check to see if the foot names match
    	if (this.getFootName() != pML.getFootName()) return false;
    	
    	//Check to see if the Nyad algebra names match up.
    	if (this.getAlgebraName() != pML.getAlgebraName()) return false;
    	
    	//Now we start into the Monad lists.  Find a monad from this and its 
    	//counterpart in other.  If they are a reference match, move on.  If 
    	//not return a false result.        	
    	String[] oAlgKey=pML.getAlgebraKey();
    	for (int j=0; j<AlgebraKey.length; j++)
    	{
    		
    		for (int k=0; k<oAlgKey.length; k++)
    		{
    			if (AlgebraKey[j].equals(oAlgKey[k]))
    			{
    				Monad testthis=(Monad)MList.get(j);
    				Monad testother=(Monad)pML.getMList(k);
    				if (!testthis.isReferenceMatch(testother)) return false;
    				break;
    			}
    		}
    	}
    	
    	//Making it this far implies that all tests have passed.  pML is a
    	//weak reference match for this Nyad.
    	return true;
    }
    
/**
 * Return true if the Monads in the list are identical to the passed Monads
 * in the comparison list.  Only monads sharing the same Algebra name need to be
 * checked against each other.  For those pairs, a check is made using the same 
 * equality check used by the Monads themselves.  No check is to be made for 
 * equality between MNames.
 * This method is needed to compare Nyads since comparing instances via
 * their variable names only checks to see if both variables refernce the same
 * place in memory.  It is possible for two Nyads to  be equal without
 * occupying the same place in memory.
 */
    public boolean isEqual(Nyad pML) 
    {
    	//Check first to see if the Nyads are a reference match.  Return 
    	//false if they are not because they can't be equal in this case.
    	if (!this.isReferenceMatch(pML)) return false;
    	
    	//If they are a reference match, then we have only to step through 
    	//the Monads in one list, find their counterparts in the second, and 
    	//test for equality.
    	
    	String[] oAlgKey=pML.getAlgebraKey();
    	boolean check=false;
    	for (int j=0; j<AlgebraKey.length; j++)
    	{
    		check=false;
    		for (int k=0; k<oAlgKey.length; k++)
    		{
    			if (AlgebraKey[j].equals(oAlgKey[k]))
    			{
    				check=true;
    				Monad testthis=(Monad)MList.get(j);
    				Monad testother=(Monad)pML.getMList(k);
    				if (!testthis.isEqual(testother)) return false;
    				break;
    			}
    		}
    		//if check is true a match was found
    		//if check is false, we have a dangling monad, so they can't
    		//be equal.
    		if (!check) return false;
    	}
    	//To get this far, all Monads in one list must pass the equality 
    	//test for their counterparts in the other list.
    	return true;
    }

/**
 * Nyad Scaling:  Pick a monad and scale it by the magnitude provided.
 * Only one monad can be scaled within a nyad at a time.
 * Note that a request to scale a monad that cannot be found in the list results 
 * in no action and no exception.  The scaling is effectively performed against 
 * a 'zero' monad for the algebra not represented in the list since much monads
 * can be appended to the list without really changing the nature of the nyad.
 * @param pWhichOne			int
 * @param pMag				double
 */
    public void Scale(int pWhichOne, double pMag) 
    {
    	if (pWhichOne<AlgebraKey.length)
    	{
    		Monad target=(Monad)MList.get(pWhichOne);
    		target.Scale(pMag);
    	}  	
    }

/**
 * Nyad Scaling:  Pick a monad and scale it by the magnitude provided.
 * Only one monad can be scaled within a nyad at a time.
 * Note that a request to scale a monad that cannot be found in the list results 
 * in no action and no exception.  The scaling is effectively performed against 
 * a 'zero' monad for the algebra not represented in the list since much monads
 * can be appended to the list without really changing the nature of the nyad.
 * @param pWhichAlg			String
 * @param pMag				double
 */
    public void Scale(String pWhichAlg, double pMag) 
    {
    	int testfind=findPosition(pWhichAlg);
    	Monad target=(Monad)MList.get(testfind);
    	target.Scale(pMag);		
    }

/**
 * Each of the Monads is turned into its Dual
 */
    public void LocalDual() 
    throws CladosException
    {
    	for (int j=0; j<AlgebraKey.length; j++)
    	{
    		Monad one=(Monad)MList.get(j);
    		one.LocalDual();
    	}
    }
        
/**
 * Dyad symmetric compression:  1/2 (left right + right left)
 * Monads are placed in the same algebra and symmetrically multiplied to each
 * other.  A reference match test must pass for both after the algebra names
 * have been changed. 
 */
    public void SymmCompress(String pInto, String pFrom)
    throws CladosException
    {
    	//The strings refer to particular algebras.  Find them in the AlgebraKey
    	//to know which two monads are being compress.  Once that is done, do
    	//the operation.
    	int tempInto=findPosition(pInto);
    	int tempFrom=findPosition(pFrom);
    	if (tempInto+tempFrom<0)
    	{
    		//One or more of the monads could not be found
    		throw new CladosException(this, "Could not find a monad for compression.");
    	}
    	else
    	{
    		SymmCompress(tempInto, tempFrom);
    	}
    }
    
/**
 * Dyad symmetric compression:  1/2 (left right + right left)
 * Monads are placed in the same algebra and symmetrically multiplied to each
 * other.  A reference match test must pass for both after the algebra names
 * have been changed. 
 */
    public void SymmCompress(int pInto, int pFrom)
    throws CladosException
    {
    	Monad tempLeft = (Monad)MList.get(pInto);
    	Monad tempLeft2 = new Monad(tempLeft);
    	
    	Monad tempRight = (Monad)MList.get(pFrom);
    	String tempRAlg=tempRight.getAlgebraName();
    	tempRight.setAlgebraName(tempLeft.getAlgebraName());
    	
    	try
    	{
    		tempLeft.RightMultiply(tempRight);
    		tempLeft2.LeftMultiply(tempRight);
    		tempLeft.Add(tempLeft2);
    		tempLeft.Scale(0.5);
    		
    		tempRight = (Monad)MList.remove(pFrom);
    		setOrder();
    		setNameKey();
    		setAlgebraKey();
    		setFrameKey();
    	}
    	catch (NoReferenceMatchException e)
    	{
    		tempRight.setAlgebraName(tempRAlg);
    		throw new CladosException(this, "Reference Match failed.  Operation rolled back.");
    	}    	
    }

/**
 * Dyad anymmetric compression:  1/2 (left right - right left)
 * Monads are placed in the same algebra and antisymmetrically multiplied to 
 * eachother.  A reference match test must pass for both after the algebra names
 * have been changed. 
 */
    public void AntiSymmCompress(String pInto, String pFrom)
    throws CladosException
    {
//    	The strings refer to particular algebras.  Find them in the AlgebraKey
    	//to know which two monads are being compress.  Once that is done, do
    	//the operation.
    	int tempInto=findPosition(pInto);
    	int tempFrom=findPosition(pFrom);
    	if (tempInto+tempFrom<0)
    	{
    		//One or more of the monads could not be found
    		throw new CladosException(this, "Could not find a monad for compression.");
    	}
    	else
    	{
    		AntiSymmCompress(tempInto, tempFrom);
    	}
    }
    
/**
 * Dyad anymmetric compression:  1/2 (left right - right left)
 * Monads are placed in the same algebra and antisymmetrically multiplied to 
 * eachother.  A reference match test must pass for both after the algebra names
 * have been changed. 
 */
    public void AntiSymmCompress(int pInto, int pFrom)
    throws CladosException
    {
    	Monad tempLeft = (Monad)MList.get(pInto);
    	Monad tempLeft2 = new Monad(tempLeft);
    	
    	Monad tempRight = (Monad)MList.get(pFrom);
    	String tempRAlg=tempRight.getAlgebraName();
    	tempRight.setAlgebraName(tempLeft.getAlgebraName());
    	
    	try
    	{
    		tempLeft.RightMultiply(tempRight);
    		tempLeft2.LeftMultiply(tempRight);
    		tempLeft.Subtract(tempLeft2);
    		tempLeft.Scale(0.5);
    		
    		tempRight = (Monad)MList.remove(pFrom);
    		setOrder();
    		setNameKey();
    		setAlgebraKey();
    		setFrameKey();
    	}
    	catch (NoReferenceMatchException e)
    	{
    		tempRight.setAlgebraName(tempRAlg);
    		throw new CladosException(this, "Reference Match failed.  Operation rolled back.");
    	}    	
    }

/**
 * Cast the Nyad as a higher dimension, single algebra object using pSig as the
 * generator's signature.
 */
    public Monad Regenerate(String pSig) 
    {
    	return null;
    }

/**
 * Decompose the Nyad into lower dimensions using pSig1 and pSig2 as the
 * generator's signatures.
 */
    public ArrayList Decompose(String pSig1, String pSig2) 
    {
    	return null;
    }
    
}

