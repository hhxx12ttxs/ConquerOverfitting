package res;

import java.math.BigDecimal;
import java.math.BigInteger;
import res.SumLibrary;

/**
 *
 * @author Devin
 */
public class BigRational
{
   private BigInteger mNumerator;
   private BigInteger mDenominator;

   public BigRational(int pNumerator, int pDenominator)
   {
      mNumerator = new BigDecimal(pNumerator).toBigInteger();
      mDenominator = new BigDecimal(pDenominator).toBigInteger();
   }

   public BigRational(BigInteger pNumerator, BigInteger pDenominator)
   {
      mNumerator = pNumerator;
      mDenominator = pDenominator;
   }

   public void simplify()
   {
      BigInteger gcd = mNumerator.gcd(mDenominator);
      if (gcd.compareTo(new BigInteger("0")) != 0)
      {
         mNumerator = mNumerator.divide(gcd);
         mDenominator = mDenominator.divide(gcd);
      }

      if (SumLibrary.isNegative(mDenominator))
      {
         mNumerator = mNumerator.negate();
         mDenominator = mDenominator.negate();
      }
   }

   public BigDecimal toBigDecimal()
   {
      simplify();
      BigDecimal num = new BigDecimal(mNumerator);
      BigDecimal den = new BigDecimal(mDenominator);

      return SumLibrary.bigDivide(num, den);
   }

   /**
    * (ad + bc) / bd = (a/b) + (c/d)
    *
    * @param pAugend
    * @return
    */
   public BigRational add(BigRational pAugend)
   {
      BigInteger ad = mNumerator.multiply(pAugend.getDenominator());
      BigInteger bc = mDenominator.multiply(pAugend.getNumerator());
      BigInteger bd = mDenominator.multiply(pAugend.getDenominator());

      return new BigRational(ad.add(bc), bd);
   }

   public String toFraction()
   {
      simplify();
      return mNumerator.toString() + "/" + mDenominator.toString();
   }

   public String toLatex()
   {
      simplify();
      String latex = " {" + mNumerator.toString() + "\\over " + mDenominator.
         toString() + "} ";

      if (SumLibrary.isNegative(mNumerator))
      {
         latex = "\\left(- {" + mNumerator.abs().toString() + "\\over"
            + mDenominator.toString() + "}\\right)";
      }

      return latex;
   }

   public BigInteger getNumerator()
   {
      return mNumerator;
   }

   public BigInteger getDenominator()
   {
      return mDenominator;
   }

   public void setNumerator(BigInteger pNumerator)
   {
      mNumerator = pNumerator;
   }

   public void setDenominator(BigInteger pDenominator)
   {
      mDenominator = pDenominator;
   }
}

