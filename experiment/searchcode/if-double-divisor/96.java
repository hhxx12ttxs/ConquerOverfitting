// <XMP>
// Self-Initializing Quadratic Sieve (SIQS) Factorization Method
//
// Written by Dario Alejandro Alpern (Buenos Aires - Argentina)
// Last updated March 28th, 2009. See http://www.alpertron.com.ar/ECM.HTM
//
// No part of this code can be used for commercial purposes without
// the written consent from the author. Otherwise it can be used freely
// except that you have to write somewhere in the code this header.
// 
import java.math.*;

final class Siqs implements Runnable
{
  ecm ecmApplet;
  private String SIQSInfoText;
  private int numberThreads;
  int NumberLength;
  long trialDivisions;
  long smoothsFound;
  long totalPartials;
  long partialsFound;
  long ValuesSieved;
  int nbrPrimes;
  int congruencesFound;
  long polynomialsSieved;
  int nbrPartials;
  private int multiplier;
  private int nbrFactorsA;
  private long afact[];
  private long startTime;
  long TestNbr[] = new long[20];
  long TestNbr2[] = new long[20];
  long biQuadrCoeff[] = new long[20];
  long biLinearDelta[][] = new long[20][20];
  private long largePrimeUpperBound;
  private byte log2;
  private int aindex[];
  private Thread threadArray[];
  private PrimeSieveData primeSieveData[];
  private PrimeTrialDivisionData primeTrialDivisionData[];
  private int span;
  private int indexMinFactorA;
  private int threadNumber;
  private int nbrThreadFinishedPolySet;
  private long oldSeed;
  private long newSeed = 0;
  private int NbrPolynomials;
  private int SieveLimit;
  private long vectLeftHandSide[][];
  private int matrixPartial[][];
  private int matrixPartialHashIndex[];
  private int matrixB[][];
  private int amodq[];
  private byte threshold;
  private int smallPrimeUpperLimit;
  private int firstLimit;
  private int secondLimit;
  private int thirdLimit;
  private int vectExpParity[];
  private int nbrPrimes2;
  private BigInteger factorSiqs;
  PrimeSieveData firstPrimeSieveData[];
  static final long DosALa32 = (long) 1 << 32;
  static final long DosALa31 = (long) 1 << 31;
  static final long DosALa31_1 = DosALa31 - 1;
  static final double dDosALa31 = (double) DosALa31;
  static final double dDosALa62 = dDosALa31 * dDosALa31;
  static final long Mi = 1000000000;

  Siqs(ecm ecmApplet)
  {
    this.ecmApplet = ecmApplet;
    this.NumberLength = ecmApplet.NumberLength;
    this.numberThreads = ecmApplet.numberThreads;
  }

  private final void PerformSiqsSieveStage(PrimeSieveData primeSieveData[],
                             byte SieveArray[],
                             int nbrPrimes, int nbrPrimes2,
                             int firstLimit, int secondLimit, int thirdLimit,
                             int smallPrimeUpperLimit, byte threshold,
                             int multiplier, int SieveLimit, int amodq[],
                             int PolynomialIndex, int aindex[],
                             int nbrFactorsA, byte log2, long biLinearCoeff[],
                             int NumberLength)
  {
    byte logprime;
    int currentPrime, F1, F2, F3, F4, X1, X2;
    int index, index2, indexFactorA;
    int mask;
    boolean polyadd;
    int S1, S2, G0, G1, G2, G3;
    long D, E, Q;
    PrimeSieveData rowPrimeSieveData;

    F1 = PolynomialIndex;
    indexFactorA = 0;
    while ((F1 & 1) == 0)
    {
      F1 >>= 1;
      indexFactorA++;
    }
    if (polyadd = ((F1 & 2) != 0))   // Adjust value of B as appropriate
    {                                // according to the Gray code.
      ecm.AddBigNbr(biLinearCoeff, biLinearDelta[indexFactorA], biLinearCoeff,
                    NumberLength);
      ecm.AddBigNbr(biLinearCoeff, biLinearDelta[indexFactorA], biLinearCoeff,
                    NumberLength);
    }
    else
    {
      ecm.SubtractBigNbr(biLinearCoeff, biLinearDelta[indexFactorA],
                         biLinearCoeff, NumberLength);
      ecm.SubtractBigNbr(biLinearCoeff, biLinearDelta[indexFactorA],
                         biLinearCoeff, NumberLength);
    }
    indexFactorA--;
    // Compute solutions for divisors of the leading coefficients
    for (index2 = 0; index2 < nbrFactorsA; index2++)
    {
      rowPrimeSieveData = primeSieveData[aindex[index2]];
      E = rowPrimeSieveData.value;
      D = ecm.RemDivBigNbrByLong(TestNbr, E * E, NumberLength);
      Q = ecm.RemDivBigNbrByLong(biLinearCoeff, E * E, NumberLength);
      rowPrimeSieveData.soln1 =
            (int) ((((D - Q * Q) / E * modInv(amodq[index2] * Q % E, E)
              + SieveLimit)
              % E + E)% E);    
      rowPrimeSieveData.difsoln = -1;   // Only one solution.
    }
    X1 = SieveLimit << 1;
    rowPrimeSieveData = primeSieveData[1];
    F1 = polyadd ? -rowPrimeSieveData.Bainv2[indexFactorA]:
                    rowPrimeSieveData.Bainv2[indexFactorA];
    if (((rowPrimeSieveData.soln1 += F1) & 1) == 0)
    {
      SieveArray[0] = (byte) (log2 - threshold);
      SieveArray[1] = (byte) (-threshold);
    }
    else
    {
      SieveArray[0] = (byte) (-threshold);
      SieveArray[1] = (byte) (log2 - threshold);
    }
    F2 = 2;
    index = 2;
    for (;;)
    {
      rowPrimeSieveData = primeSieveData[index];
      currentPrime = rowPrimeSieveData.value;
      F3 = F2 * currentPrime;
      if (X1 + 1 < F3)
      {
        F3 = X1 + 1;
      }
      F4 = F2;
      while (F4 * 2 <= F3)
      {
        System.arraycopy(SieveArray, 0, SieveArray, F4, F4);
        F4 *= 2;
      }
      System.arraycopy(SieveArray, 0, SieveArray, F4, F3 - F4);
      if (F3 == X1 + 1)
      {
        break;
      }
      F1 = currentPrime;
      logprime = 1;
      while (F1 >= 5)
      {
        F1 /= 3;
        logprime++;
      }
      F1 = polyadd ? -rowPrimeSieveData.Bainv2[indexFactorA]:
                      rowPrimeSieveData.Bainv2[indexFactorA];
      index2 = (rowPrimeSieveData.soln1 + F1)%currentPrime;
      if (index2 < 0)
      {
        index2 += currentPrime;
      }
      rowPrimeSieveData.soln1 = index2;
      for (; index2 < F3; index2 += currentPrime)
      {
        SieveArray[index2] += logprime;
      }
      if (currentPrime != multiplier)
      {
        for (index2 = (rowPrimeSieveData.soln1 + currentPrime -
                        rowPrimeSieveData.difsoln) % currentPrime;
             index2 < F3;
             index2 += currentPrime)
        {
          SieveArray[index2] += logprime;
        }
      }
      index++;
      F2 *= currentPrime;
    }

    F1 = primeSieveData[smallPrimeUpperLimit].value;
    logprime = 1;
    mask = 5;
    while (F1 >= 5)
    {
      F1 /= 3;
      logprime++;
      mask *= 3;
    }
    if (polyadd)
    {
      for (; index < smallPrimeUpperLimit; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        if ((S1 = rowPrimeSieveData.soln1 -
                  rowPrimeSieveData.Bainv2[indexFactorA]) < 0)
        { 
          S1 += currentPrime;
        }
        rowPrimeSieveData.soln1 = S1;
      }
      for (index = smallPrimeUpperLimit; index < firstLimit; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F2 = currentPrime + currentPrime;
        F3 = F2 + currentPrime;
        F4 = F3 + currentPrime;
        S1 = rowPrimeSieveData.soln1 -
                  rowPrimeSieveData.Bainv2[indexFactorA];
        S1 += (S1 >>> 31) * currentPrime;
        rowPrimeSieveData.soln1 = S1;
        S2 = S1 - rowPrimeSieveData.difsoln;
        S2 += (S2 >>> 31) * currentPrime;
        if ((G0 = S2 - S1) < 0)
        {
          S1 = S2;
          G0 = -G0;
        }
        G1 = G0 + currentPrime;
        G2 = G1 + currentPrime;
        G3 = G2 + currentPrime;
        index2 = X1 / F4 * F4 + S1;
        do
        {
          SieveArray[index2] += logprime;
          SieveArray[index2 + currentPrime] += logprime;
          SieveArray[index2 + F2] += logprime;
          SieveArray[index2 + F3] += logprime;
          SieveArray[index2 + G0] += logprime;
          SieveArray[index2 + G1] += logprime;
          SieveArray[index2 + G2] += logprime;
          SieveArray[index2 + G3] += logprime;
        }
        while ((index2 -= F4) >= 0);
      }
      for (; index < secondLimit; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        F2 = currentPrime + currentPrime;
        F3 = F2 + currentPrime;
        F4 = F2 + F2;
        X2 = X1 - F4;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F1 = rowPrimeSieveData.soln1 -
                  rowPrimeSieveData.Bainv2[indexFactorA];
        F1 += (F1 >>> 31) * currentPrime;
        index2 = (rowPrimeSieveData.soln1 = F1);
        do
        {
          SieveArray[index2] += logprime;
          SieveArray[index2 + currentPrime] += logprime;
          SieveArray[index2 + F2] += logprime;
          SieveArray[index2 + F3] += logprime;
        } while ((index2 += F4) <= X2);
        for (; index2 <= X1; index2 += currentPrime)
        {
          SieveArray[index2] += logprime;
        }
        if (rowPrimeSieveData.difsoln >= 0)
        {
          index2 = F1 - rowPrimeSieveData.difsoln;
          index2 += (index2 >>> 31) * currentPrime;
          do
          {
            SieveArray[index2] += logprime;
            SieveArray[index2 + currentPrime] += logprime;
            SieveArray[index2 + F2] += logprime;
            SieveArray[index2 + F3] += logprime;
          } while ((index2 += F4) <= X2);
          for (; index2 <= X1; index2 += currentPrime)
          {
            SieveArray[index2] += logprime;
          }
        }
      }
      for (; index < thirdLimit; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F2 = rowPrimeSieveData.soln1 -
                  rowPrimeSieveData.Bainv2[indexFactorA];
        F2 += currentPrime * (F2 >>> 31);
        index2 = (rowPrimeSieveData.soln1 = F2);
        do
        {
          SieveArray[index2] += logprime;
        } while ((index2 += currentPrime) <= X1);
        F2 -= rowPrimeSieveData.difsoln;
        F2 += currentPrime * (F2 >>> 31);
        do
        {
          SieveArray[F2] += logprime;
        } while ((F2 += currentPrime) <= X1);
      }
      for (; index < nbrPrimes2; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F2 = rowPrimeSieveData.soln1 -
                  rowPrimeSieveData.Bainv2[indexFactorA];
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
        rowPrimeSieveData = primeSieveData[++index];
        currentPrime = rowPrimeSieveData.value;
        F2 = rowPrimeSieveData.soln1 -
                  rowPrimeSieveData.Bainv2[indexFactorA];
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
        rowPrimeSieveData = primeSieveData[++index];
        currentPrime = rowPrimeSieveData.value;
        F2 = rowPrimeSieveData.soln1 -
                  rowPrimeSieveData.Bainv2[indexFactorA];
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
        rowPrimeSieveData = primeSieveData[++index];
        currentPrime = rowPrimeSieveData.value;
        F2 = rowPrimeSieveData.soln1 - rowPrimeSieveData.Bainv2[indexFactorA];
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
      }
      for (; index < nbrPrimes; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F2 = rowPrimeSieveData.soln1 -
                  rowPrimeSieveData.Bainv2[indexFactorA];
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
      }
    }
    else
    {
      for (; index < smallPrimeUpperLimit; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        S1 = rowPrimeSieveData.soln1 +
                  rowPrimeSieveData.Bainv2[indexFactorA] - currentPrime;
        S1 += currentPrime * (S1 >>> 31);
        rowPrimeSieveData.soln1 = S1;
      }
      for (index = smallPrimeUpperLimit; index < firstLimit; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F2 = currentPrime + currentPrime;
        F3 = F2 + currentPrime;
        F4 = F3 + currentPrime;
        S1 = rowPrimeSieveData.soln1 +
                  rowPrimeSieveData.Bainv2[indexFactorA] - currentPrime;
        S1 += currentPrime * (S1 >>> 31);
        rowPrimeSieveData.soln1 = S1;
        S2 = S1 - rowPrimeSieveData.difsoln;
        S2 += currentPrime * (S2 >>> 31);
        if ((G0 = S2 - S1) < 0)
        {
          S1 = S2;
          G0 = -G0;
        }
        G1 = G0 + currentPrime;
        G2 = G1 + currentPrime;
        G3 = G2 + currentPrime;
        index2 = X1 / F4 * F4 + S1;
        do
        {
          SieveArray[index2] += logprime;
          SieveArray[index2 + currentPrime] += logprime;
          SieveArray[index2 + F2] += logprime;
          SieveArray[index2 + F3] += logprime;
          SieveArray[index2 + G0] += logprime;
          SieveArray[index2 + G1] += logprime;
          SieveArray[index2 + G2] += logprime;
          SieveArray[index2 + G3] += logprime;
        }
        while ((index2 -= F4) >= 0);
      }
      for (; index < secondLimit; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        F2 = currentPrime + currentPrime;
        F3 = F2 + currentPrime;
        F4 = F2 + F2;
        X2 = X1 - F4;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F1 = rowPrimeSieveData.soln1 +
                  rowPrimeSieveData.Bainv2[indexFactorA] - currentPrime;
        F1 += currentPrime * (F1 >>> 31);
        index2 = (rowPrimeSieveData.soln1 = F1);
        do
        {
          SieveArray[index2] += logprime;
          SieveArray[index2 + currentPrime] += logprime;
          SieveArray[index2 + F2] += logprime;
          SieveArray[index2 + F3] += logprime;
        } while ((index2 += F4) <= X2);
        for (; index2 <= X1; index2 += currentPrime)
        {
          SieveArray[index2] += logprime;
        }
        if (rowPrimeSieveData.difsoln >= 0)
        {
          index2 = F1 - rowPrimeSieveData.difsoln;
          index2 += currentPrime * (index2 >>> 31);
          do
          {
            SieveArray[index2] += logprime;
            SieveArray[index2 + currentPrime] += logprime;
            SieveArray[index2 + F2] += logprime;
            SieveArray[index2 + F3] += logprime;
          } while ((index2 += F4) <= X2);
          for (; index2 <= X1; index2 += currentPrime)
          {
            SieveArray[index2] += logprime;
          }
        }
      }
      for (; index < thirdLimit; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F2 = rowPrimeSieveData.soln1 +
                  rowPrimeSieveData.Bainv2[indexFactorA] - currentPrime;
        index2 = (rowPrimeSieveData.soln1 = (F2 += currentPrime * (F2 >>> 31)));
        do
        {
          SieveArray[index2] += logprime;
        } while ((index2 += currentPrime) <= X1);
        F2 -= rowPrimeSieveData.difsoln;
        F2 += currentPrime * (F2 >>> 31);
        do
        {
          SieveArray[F2] += logprime;
        } while ((F2 += currentPrime) <= X1);
      }
      for (; index < nbrPrimes2; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F2 = rowPrimeSieveData.soln1 +
                  rowPrimeSieveData.Bainv2[indexFactorA] - currentPrime;
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
        rowPrimeSieveData = primeSieveData[++index];
        currentPrime = rowPrimeSieveData.value;
        F2 = rowPrimeSieveData.soln1 +
                  rowPrimeSieveData.Bainv2[indexFactorA] - currentPrime;
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
        rowPrimeSieveData = primeSieveData[++index];
        currentPrime = rowPrimeSieveData.value;
        F2 = rowPrimeSieveData.soln1 +
                  rowPrimeSieveData.Bainv2[indexFactorA] - currentPrime;
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
        rowPrimeSieveData = primeSieveData[++index];
        currentPrime = rowPrimeSieveData.value;
        F2 = rowPrimeSieveData.soln1 +
                  rowPrimeSieveData.Bainv2[indexFactorA] - currentPrime;
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
      }
      for (; index < nbrPrimes; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F2 = rowPrimeSieveData.soln1 +
                  rowPrimeSieveData.Bainv2[indexFactorA] - currentPrime;
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
      }
    }
  }

  private final void PerformSiqsSieveStage_0(PrimeSieveData primeSieveData[],
                             byte SieveArray[],
                             int nbrPrimes, int nbrPrimes2,
                             int firstLimit, int secondLimit, int thirdLimit,
                             int smallPrimeUpperLimit, byte threshold,
                             int multiplier, int SieveLimit, int amodq[],
                             int PolynomialIndex, int aindex[],
                             int nbrFactorsA, byte log2, long biLinearCoeff[],
                             int NumberLength)
  {
    byte logprime;
    int currentPrime, F1, F2, F3, F4, X1, X2;
    int index, index2;
    int mask;
    boolean polyadd;
    int S1, S2, G0, G1, G2, G3;
    long D, E, Q;
    PrimeSieveData rowPrimeSieveData;

    F1 = PolynomialIndex;
    if (polyadd = ((F1 & 2) != 0))    // Adjust value of B as appropriate
    {                                 // according to the Gray code.
      ecm.AddBigNbr(biLinearCoeff, biLinearDelta[0], biLinearCoeff, NumberLength);
      ecm.AddBigNbr(biLinearCoeff, biLinearDelta[0], biLinearCoeff, NumberLength);
    }
    else
    {
      ecm.SubtractBigNbr(biLinearCoeff, biLinearDelta[0], biLinearCoeff, NumberLength);
      ecm.SubtractBigNbr(biLinearCoeff, biLinearDelta[0], biLinearCoeff, NumberLength);
    }
    // Compute solutions for divisors of the leading coefficients
    for (index2 = 0; index2 < nbrFactorsA; index2++)
    {
      rowPrimeSieveData = primeSieveData[aindex[index2]];
      E = rowPrimeSieveData.value;
      D = ecm.RemDivBigNbrByLong(TestNbr, E * E, NumberLength);
      Q = ecm.RemDivBigNbrByLong(biLinearCoeff, E * E, NumberLength);
      rowPrimeSieveData.soln1 =
            (int) ((((D - Q * Q) / E * modInv(amodq[index2] * Q % E, E)
              + SieveLimit)
              % E + E)% E);    
      rowPrimeSieveData.difsoln = -1;   // Only one solution.
    }
    X1 = SieveLimit << 1;
    rowPrimeSieveData = primeSieveData[1];
    F1 = polyadd ? -rowPrimeSieveData.Bainv2_0:
                    rowPrimeSieveData.Bainv2_0;
    if (((rowPrimeSieveData.soln1 += F1) & 1) == 0)
    {
      SieveArray[0] = (byte) (log2 - threshold);
      SieveArray[1] = (byte) (-threshold);
    }
    else
    {
      SieveArray[0] = (byte) (-threshold);
      SieveArray[1] = (byte) (log2 - threshold);
    }
    F2 = 2;
    index = 2;
    for (;;)
    {
      rowPrimeSieveData = primeSieveData[index];
      currentPrime = rowPrimeSieveData.value;
      F3 = F2 * currentPrime;
      if (X1 + 1 < F3)
      {
        F3 = X1 + 1;
      }
      F4 = F2;
      while (F4 * 2 <= F3)
      {
        System.arraycopy(SieveArray, 0, SieveArray, F4, F4);
        F4 *= 2;
      }
      System.arraycopy(SieveArray, 0, SieveArray, F4, F3 - F4);
      if (F3 == X1 + 1)
      {
        break;
      }
      F1 = currentPrime;
      logprime = 1;
      while (F1 >= 5)
      {
        F1 /= 3;
        logprime++;
      }
      F1 = polyadd ? -rowPrimeSieveData.Bainv2_0:
                      rowPrimeSieveData.Bainv2_0;
      index2 = (rowPrimeSieveData.soln1 + F1)%currentPrime;
      if (index2 < 0)
      {
        index2 += currentPrime;
      }
      rowPrimeSieveData.soln1 = index2;
      for (; index2 < F3; index2 += currentPrime)
      {
        SieveArray[index2] += logprime;
      }
      if (currentPrime != multiplier)
      {
        for (index2 = (rowPrimeSieveData.soln1 + currentPrime -
                       rowPrimeSieveData.difsoln) % currentPrime;
             index2 < F3;
             index2 += currentPrime)
        {
          SieveArray[index2] += logprime;
        }
      }
      index++;
      F2 *= currentPrime;
    }

    F1 = primeSieveData[smallPrimeUpperLimit].value;
    logprime = 1;
    mask = 5;
    while (F1 >= 5)
    {
      F1 /= 3;
      logprime++;
      mask *= 3;
    }
    if (polyadd)
    {
      for (; index < smallPrimeUpperLimit; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        S1 = rowPrimeSieveData.soln1 - rowPrimeSieveData.Bainv2_0;
        S1 += (S1 >>> 31) * currentPrime;
        rowPrimeSieveData.soln1 = S1;
      }
      for (index = smallPrimeUpperLimit; index < firstLimit; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F2 = currentPrime + currentPrime;
        F3 = F2 + currentPrime;
        F4 = F3 + currentPrime;
        S1 = rowPrimeSieveData.soln1 - rowPrimeSieveData.Bainv2_0;
        S1 += (S1 >>> 31) * currentPrime;
        rowPrimeSieveData.soln1 = S1;
        S2 = S1 - rowPrimeSieveData.difsoln;
        S2 += (S2 >>> 31) * currentPrime;
        if ((G0 = S2 - S1) < 0)
        {
          S1 = S2;
          G0 = -G0;
        }
        G1 = G0 + currentPrime;
        G2 = G1 + currentPrime;
        G3 = G2 + currentPrime;
        index2 = X1 / F4 * F4 + S1;
        do
        {
          SieveArray[index2] += logprime;
          SieveArray[index2 + currentPrime] += logprime;
          SieveArray[index2 + F2] += logprime;
          SieveArray[index2 + F3] += logprime;
          SieveArray[index2 + G0] += logprime;
          SieveArray[index2 + G1] += logprime;
          SieveArray[index2 + G2] += logprime;
          SieveArray[index2 + G3] += logprime;
        }
        while ((index2 -= F4) >= 0);
      }
      for (; index < secondLimit; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        F2 = currentPrime + currentPrime;
        F3 = F2 + currentPrime;
        F4 = F2 + F2;
        X2 = X1 - F4;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F1 = rowPrimeSieveData.soln1 - rowPrimeSieveData.Bainv2_0;
        F1 += (F1 >>> 31) * currentPrime;
        index2 = (rowPrimeSieveData.soln1 = F1);
        do
        {
          SieveArray[index2] += logprime;
          SieveArray[index2 + currentPrime] += logprime;
          SieveArray[index2 + F2] += logprime;
          SieveArray[index2 + F3] += logprime;
        } while ((index2 += F4) <= X2);
        for (; index2 <= X1; index2 += currentPrime)
        {
          SieveArray[index2] += logprime;
        }
        if (rowPrimeSieveData.difsoln >= 0)
        {
          index2 = F1 - rowPrimeSieveData.difsoln;
          index2 += (index2 >>> 31) * currentPrime;
          do
          {
            SieveArray[index2] += logprime;
            SieveArray[index2 + currentPrime] += logprime;
            SieveArray[index2 + F2] += logprime;
            SieveArray[index2 + F3] += logprime;
          } while ((index2 += F4) <= X2);
          for (; index2 <= X1; index2 += currentPrime)
          {
            SieveArray[index2] += logprime;
          }
        }
      }
      for (; index < thirdLimit; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F2 = rowPrimeSieveData.soln1 - rowPrimeSieveData.Bainv2_0;
        index2 = (rowPrimeSieveData.soln1 = (F2 += currentPrime * (F2 >>> 31)));
        do
        {
          SieveArray[index2] += logprime;
        } while ((index2 += currentPrime) <= X1);
        F2 -= rowPrimeSieveData.difsoln;
        F2 += currentPrime * (F2 >>> 31);
        do
        {
          SieveArray[F2] += logprime;
        } while ((F2 += currentPrime) <= X1);
      }
      for (; index < nbrPrimes2; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F2 = rowPrimeSieveData.soln1 - rowPrimeSieveData.Bainv2_0;
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
        rowPrimeSieveData = primeSieveData[++index];
        currentPrime = rowPrimeSieveData.value;
        F2 = rowPrimeSieveData.soln1 - rowPrimeSieveData.Bainv2_0;
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
        rowPrimeSieveData = primeSieveData[++index];
        currentPrime = rowPrimeSieveData.value;
        F2 = rowPrimeSieveData.soln1 - rowPrimeSieveData.Bainv2_0;
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
        rowPrimeSieveData = primeSieveData[++index];
        currentPrime = rowPrimeSieveData.value;
        F2 = rowPrimeSieveData.soln1 - rowPrimeSieveData.Bainv2_0;
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
      }
      for (; index < nbrPrimes; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F2 = rowPrimeSieveData.soln1 - rowPrimeSieveData.Bainv2_0;
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
      }
    }
    else
    {
      for (; index < smallPrimeUpperLimit; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        S1 = rowPrimeSieveData.soln1 +
                  rowPrimeSieveData.Bainv2_0 - currentPrime;
        S1 += currentPrime * (S1 >>> 31);
        rowPrimeSieveData.soln1 = S1;
      }
      for (index = smallPrimeUpperLimit; index < firstLimit; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F2 = currentPrime + currentPrime;
        F3 = F2 + currentPrime;
        F4 = F3 + currentPrime;
        S1 = rowPrimeSieveData.soln1 +
                  rowPrimeSieveData.Bainv2_0 - currentPrime;
        S1 += currentPrime * (S1 >>> 31);
        rowPrimeSieveData.soln1 = S1;
        S2 = S1 - rowPrimeSieveData.difsoln;
        S2 += currentPrime * (S2 >>> 31);
        if ((G0 = S2 - S1) < 0)
        {
          S1 = S2;
          G0 = -G0;
        }
        G1 = G0 + currentPrime;
        G2 = G1 + currentPrime;
        G3 = G2 + currentPrime;
        index2 = X1 / F4 * F4 + S1;
        do
        {
          SieveArray[index2] += logprime;
          SieveArray[index2 + currentPrime] += logprime;
          SieveArray[index2 + F2] += logprime;
          SieveArray[index2 + F3] += logprime;
          SieveArray[index2 + G0] += logprime;
          SieveArray[index2 + G1] += logprime;
          SieveArray[index2 + G2] += logprime;
          SieveArray[index2 + G3] += logprime;
        }
        while ((index2 -= F4) >= 0);
      }
      for (; index < secondLimit; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        F2 = currentPrime + currentPrime;
        F3 = F2 + currentPrime;
        F4 = F2 + F2;
        X2 = X1 - F4;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F1 = rowPrimeSieveData.soln1 +
                  rowPrimeSieveData.Bainv2_0 - currentPrime;
        F1 += currentPrime * (F1 >>> 31);
        index2 = (rowPrimeSieveData.soln1 = F1);
        do
        {
          SieveArray[index2] += logprime;
          SieveArray[index2 + currentPrime] += logprime;
          SieveArray[index2 + F2] += logprime;
          SieveArray[index2 + F3] += logprime;
        } while ((index2 += F4) <= X2);
        for (; index2 <= X1; index2 += currentPrime)
        {
          SieveArray[index2] += logprime;
        }
        if (rowPrimeSieveData.difsoln >= 0)
        {
          index2 = F1 - rowPrimeSieveData.difsoln;
          index2 += currentPrime * (index2 >>> 31);
          do
          {
            SieveArray[index2] += logprime;
            SieveArray[index2 + currentPrime] += logprime;
            SieveArray[index2 + F2] += logprime;
            SieveArray[index2 + F3] += logprime;
          } while ((index2 += F4) <= X2);
          for (; index2 <= X1; index2 += currentPrime)
          {
            SieveArray[index2] += logprime;
          }
        }
      }
      for (; index < thirdLimit; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F2 = rowPrimeSieveData.soln1 +
                  rowPrimeSieveData.Bainv2_0 - currentPrime;
        index2 = (rowPrimeSieveData.soln1 = (F2 += currentPrime * (F2 >>> 31)));
        do
        {
          SieveArray[index2] += logprime;
        } while ((index2 += currentPrime) <= X1);
        F2 -= rowPrimeSieveData.difsoln;
        F2 += currentPrime * (F2 >>> 31);
        do
        {
          SieveArray[F2] += logprime;
        } while ((F2 += currentPrime) <= X1);
      }
      for (; index < nbrPrimes2; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F2 = rowPrimeSieveData.soln1 +
                  rowPrimeSieveData.Bainv2_0 - currentPrime;
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
        rowPrimeSieveData = primeSieveData[++index];
        currentPrime = rowPrimeSieveData.value;
        F2 = rowPrimeSieveData.soln1 +
                  rowPrimeSieveData.Bainv2_0 - currentPrime;
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
        rowPrimeSieveData = primeSieveData[++index];
        currentPrime = rowPrimeSieveData.value;
        F2 = rowPrimeSieveData.soln1 +
                  rowPrimeSieveData.Bainv2_0 - currentPrime;
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
        rowPrimeSieveData = primeSieveData[++index];
        currentPrime = rowPrimeSieveData.value;
        F2 = rowPrimeSieveData.soln1 +
                  rowPrimeSieveData.Bainv2_0 - currentPrime;
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
      }
      for (; index < nbrPrimes; index++)
      {
        rowPrimeSieveData = primeSieveData[index];
        currentPrime = rowPrimeSieveData.value;
        if (currentPrime >= mask)
        {
          mask *= 3;
          logprime++;
        }
        F2 = rowPrimeSieveData.soln1 +
                  rowPrimeSieveData.Bainv2_0 - currentPrime;
        if ((rowPrimeSieveData.soln1=(F2 += currentPrime * (F2 >>> 31))) < X1)
        {
          SieveArray[F2] += logprime;
        }
        F2 -= rowPrimeSieveData.difsoln;
        if ((F2 += currentPrime * (F2 >>> 31)) < X1)
        {
          SieveArray[F2] += logprime;
        }
      }
    }
  }

  private long PerformTrialDivision(PrimeSieveData primeSieveData[],
                            PrimeTrialDivisionData primeTrialDivisionData[],
                            int nbrPrimes, int rowMatrixBbeforeMerge[],
                            int aindex[], int nbrFactorsA, int index2,
                            long biDividend[], int rowSquares[],
                            int NumberLength, long biT[])
  {
    long biR0 = 0, biR1 = 0, biR2 = 0, biR3 = 0, biR4 = 0, biR5 = 0;
    long biR6 = 0;
    boolean cond = false;
    boolean testFactorA;
    long Divid, Divisor;
    int nbrSquares = rowSquares[0];
    int divis, iRem;
    int index;
    int newFactorAIndex;
    long Rem;
    int expParity;
    int left, right, median, nbr;
    int indexFactorA=0;
    int nbrColumns = rowMatrixBbeforeMerge[0];
    boolean fullRemainder;
    PrimeSieveData rowPrimeSieveData;
    PrimeTrialDivisionData rowPrimeTrialDivisionData;
    switch (NumberLength)
    {
      case 7 :
        biR6 = biDividend[6];
      case 6 :
        biR5 = biDividend[5];
      case 5 :
        biR4 = biDividend[4];
      case 4 :
        biR3 = biDividend[3];
      case 3 :
        biR2 = biDividend[2];
      case 1 :
      case 2 :
        biR1 = biDividend[1];
        biR0 = biDividend[0];
    }
    expParity = 0;
    Divid = (biR1 << 31) + biR0;
    if (NumberLength <= 2)
    {
      for (index = 1; index < nbrPrimes; index++)
      {
        Divisor = primeTrialDivisionData[index].value;
        while (Divid % Divisor == 0)
        {
          Divid /= Divisor;
          expParity = 1 - expParity;
          if (expParity == 0)
          {
            rowSquares[nbrSquares++] = (int)Divisor;
          }
        }
        if (expParity != 0)
        {
          rowMatrixBbeforeMerge[nbrColumns++] = index;
          expParity = 0;
        }
      }
    }
    else
    {
      testFactorA = true;
      newFactorAIndex = aindex[0];
      for (index = 1; testFactorA; index++)
      {
        fullRemainder = false;
        if (index < 3)
        {
          fullRemainder = true;
        }
        else if (index == newFactorAIndex)
        {
          fullRemainder = true;
          if (++indexFactorA == nbrFactorsA)
          {
            testFactorA = false;   // All factors of A were tested.
          }
          else
          {
            newFactorAIndex = aindex[indexFactorA];
          }
        }
        for (;;)
        {
          if (fullRemainder == false)
          {
            rowPrimeSieveData = primeSieveData[index];
            Divisor = rowPrimeSieveData.value;
            divis = (int)Divisor;
            iRem = index2 - rowPrimeSieveData.soln1;
            if (iRem >= divis)
            {
              if ((iRem -= divis) >= divis)
              {
                iRem %= divis;
              }
            }
            else
            {
              iRem += (iRem >>> 31)*divis;
            }
            if (iRem != 0 && iRem != divis - rowPrimeSieveData.difsoln)
            {
              if (expParity != 0)
              {
                rowMatrixBbeforeMerge[nbrColumns++] = index;
                expParity = 0;
              }
              break;              // Process next prime.
            }
            fullRemainder = true;
          }
          else
          {
            rowPrimeTrialDivisionData = primeTrialDivisionData[index];
            Divisor = rowPrimeTrialDivisionData.value;
            divis = (int)Divisor;
            switch (NumberLength)
            {
              case 7 :
                Rem = biR6*rowPrimeTrialDivisionData.exp6 +
                      biR5*rowPrimeTrialDivisionData.exp5 + 
                      biR4*rowPrimeTrialDivisionData.exp4 + 
                      biR3*rowPrimeTrialDivisionData.exp3 + 
                      biR2*rowPrimeTrialDivisionData.exp2 + Divid;
                break;
              case 6 :
                Rem = biR5*rowPrimeTrialDivisionData.exp5 + 
                      biR4*rowPrimeTrialDivisionData.exp4 + 
                      biR3*rowPrimeTrialDivisionData.exp3 + 
                      biR2*rowPrimeTrialDivisionData.exp2 + Divid;
                break;
              case 5 :
                Rem = biR4*rowPrimeTrialDivisionData.exp4 + 
                      biR3*rowPrimeTrialDivisionData.exp3 + 
                      biR2*rowPrimeTrialDivisionData.exp2 + Divid;
                break;
              case 4 :
                Rem = biR3*rowPrimeTrialDivisionData.exp3 + 
                      biR2*rowPrimeTrialDivisionData.exp2 + Divid;
                break;
              default :
                Rem = biR2*rowPrimeTrialDivisionData.exp2 + Divid;
                break;
            }
            if (Rem%divis != 0)
            {                     // Number is not a multiple of prime.
              if (expParity != 0)
              {
                rowMatrixBbeforeMerge[nbrColumns++] = index;
                expParity = 0;
              }
              break;              // Process next prime.
            }
          }
          expParity = 1 - expParity;
          if (expParity == 0)
          {
            rowSquares[nbrSquares++] = (int)Divisor;
          }
          Rem = 0;
          switch (NumberLength)
          {
            case 7 :
              Divid = biR6 + (Rem << 31);
              Rem = Divid - (biR6 = Divid / Divisor) * Divisor;
            case 6 :
              Divid = biR5 + (Rem << 31);
              Rem = Divid - (biR5 = Divid / Divisor) * Divisor;
            case 5 :
              Divid = biR4 + (Rem << 31);
              Rem = Divid - (biR4 = Divid / Divisor) * Divisor;
            case 4 :
              Divid = biR3 + (Rem << 31);
              Rem = Divid - (biR3 = Divid / Divisor) * Divisor;
            case 3 :
              Divid = biR2 + (Rem << 31);
              Rem = Divid - (biR2 = Divid / Divisor) * Divisor;
              Divid = biR1 + (Rem << 31);
              biR1 = Divid / Divisor;
              biR0 = (biR0 + ((Divid - Divisor*biR1) << 31)) / Divisor;
          }
          switch (NumberLength)
          {
            case 7 :
              cond = (biR6 == 0 && biR5 < 0x40000000);
              break;
            case 6 :
              cond = (biR5 == 0 && biR4 < 0x40000000);
              break;
            case 5 :
              cond = (biR4 == 0 && biR3 < 0x40000000);
              break;
            case 4 :
              cond = (biR3 == 0 && biR2 < 0x40000000);
              break;
            case 3 :
              cond = (biR2 == 0 && biR1 < 0x40000000);
              break;
          }
          Divid = (biR1 << 31) + biR0;
          if (cond)
          {
            NumberLength--;
            if (NumberLength == 2)
            {
              int sqrtDivid = (int)Math.sqrt(Divid) + 1;
              fullRemainder = true;
              for (; index < nbrPrimes; index++)
              {
                rowPrimeSieveData = primeSieveData[index];
                Divisor = rowPrimeSieveData.value;
                if (testFactorA && index == newFactorAIndex)
                {
                  fullRemainder = true;
                  if (++indexFactorA == nbrFactorsA)
                  {
                    testFactorA = false;   // All factors of A were tested.
                  }
                  else
                  {
                    newFactorAIndex = aindex[indexFactorA];
                  }
                }
                for (;;)
                {
                  if (fullRemainder == false)
                  {
                    divis = (int)Divisor;
                    iRem = index2 - rowPrimeSieveData.soln1;
                    if (iRem >= divis)
                    {
                      if ((iRem -= divis) >= divis)
                      {
                        iRem %= divis;
                      }
                    }
                    else
                    {
                      iRem += (iRem >>> 31)*divis;
                    }
                    if (iRem != 0 && iRem != divis - rowPrimeSieveData.difsoln)
                    {
                      break;
                    }
                    fullRemainder = true;
                  }
                  else if (Divid % Divisor != 0)
                  {
                    break;
                  }
                  Divid /= Divisor;
                  sqrtDivid = (int)Math.sqrt(Divid) + 1;
                  expParity = 1 - expParity;
                  if (expParity == 0)
                  {
                    rowSquares[nbrSquares++] = (int)Divisor;
                  }
                }
                if (expParity != 0)
                {
                  rowMatrixBbeforeMerge[nbrColumns++] = index;
                  expParity = 0;
                }
                if (Divisor > sqrtDivid)
                {                     // End of trial division.
                  rowSquares[0] = nbrSquares;
                  index = nbrPrimes-1;
                  if (Divid <= primeTrialDivisionData[index].value &&
                      Divid > 1)
                  {          // Perform binary search to find the index.
                    left = -1;
                    median = right = nbrPrimes;
                    while (left != right)
                    {
                      median = ((right-left) >> 1) + left;
                      nbr = primeTrialDivisionData[median].value;
                      if (nbr < Divid)
                      {
                        if (median == left &&
                            congruencesFound >= matrixB.length)
                        {
                          return 0;
                        }
                        left = median;                       
                      }
                      else if (nbr > Divid)
                      {
                        right = median;
                      }
                      else
                      {
                    	break;
                      }
                    }
                    rowMatrixBbeforeMerge[nbrColumns++] = median;
                    rowMatrixBbeforeMerge[0] = nbrColumns;
                    return 1;
                  }
                  rowMatrixBbeforeMerge[0] = nbrColumns;
                  return Divid;
                }
                fullRemainder = false;
              }
              break;
            }
          }
        }             /* end while */
      }               /* end for */
      for (; index < nbrPrimes; index++)
      {
        fullRemainder = false;
        for (;;)
        {
          if (fullRemainder == false)
          {
            rowPrimeSieveData = primeSieveData[index];
            Divisor = rowPrimeSieveData.value;
            divis = (int)Divisor;
            iRem = index2 - rowPrimeSieveData.soln1;
            if (iRem >= divis)
            {
              if ((iRem -= divis) >= divis)
              {
                iRem %= divis;
              }
            }
            else
            {
              iRem += (iRem >>> 31) * divis;
            }
            if (iRem != 0 && iRem != divis - rowPrimeSieveData.difsoln)
            {
              if (expParity != 0)
              {
                rowMatrixBbeforeMerge[nbrColumns++] = index;
                expParity = 0;
              }
              break;              // Process next prime.
            }
            fullRemainder = true;
          }
          else
          {
            rowPrimeTrialDivisionData = primeTrialDivisionData[index];
            Divisor = rowPrimeTrialDivisionData.value;
            divis = (int)Divisor;
            switch (NumberLength)
            {
              case 7 :
                Rem = biR6*rowPrimeTrialDivisionData.exp6 +
                      biR5*rowPrimeTrialDivisionData.exp5 + 
                      biR4*rowPrimeTrialDivisionData.exp4 + 
                      biR3*rowPrimeTrialDivisionData.exp3 + 
                      biR2*rowPrimeTrialDivisionData.exp2 + Divid;
                break;
              case 6 :
                Rem = biR5*rowPrimeTrialDivisionData.exp5 + 
                      biR4*rowPrimeTrialDivisionData.exp4 + 
                      biR3*rowPrimeTrialDivisionData.exp3 + 
                      biR2*rowPrimeTrialDivisionData.exp2 + Divid;
                break;
              case 5 :
                Rem = biR4*rowPrimeTrialDivisionData.exp4 + 
                      biR3*rowPrimeTrialDivisionData.exp3 + 
                      biR2*rowPrimeTrialDivisionData.exp2 + Divid;
                break;
              case 4 :
                Rem = biR3*rowPrimeTrialDivisionData.exp3 + 
                      biR2*rowPrimeTrialDivisionData.exp2 + Divid;
                break;
              default :
                Rem = biR2*rowPrimeTrialDivisionData.exp2 + Divid;
                break;
            }
            if (Rem%divis != 0)
            {                     // Number is not a multiple of prime.
              if (expParity != 0)
              {
                rowMatrixBbeforeMerge[nbrColumns++] = index;
                expParity = 0;
              }
              break;              // Process next prime.
            }
          }
          expParity = 1 - expParity;
          if (expParity == 0)
          {
            rowSquares[nbrSquares++] = (int)Divisor;
          }
          Rem = 0;
          switch (NumberLength)
          {
            case 7 :
              Divid = biR6 + (Rem << 31);
              Rem = Divid - (biR6 = Divid / Divisor) * Divisor;
            case 6 :
              Divid = biR5 + (Rem << 31);
              Rem = Divid - (biR5 = Divid / Divisor) * Divisor;
            case 5 :
              Divid = biR4 + (Rem << 31);
              Rem = Divid - (biR4 = Divid / Divisor) * Divisor;
            case 4 :
              Divid = biR3 + (Rem << 31);
              Rem = Divid - (biR3 = Divid / Divisor) * Divisor;
            case 3 :
              Divid = biR2 + (Rem << 31);
              Rem = Divid - (biR2 = Divid / Divisor) * Divisor;
              Divid = biR1 + (Rem << 31);
              biR1 = Divid / Divisor;
              biR0 = (biR0 + ((Divid - Divisor*biR1) << 31)) / Divisor;
          }
          switch (NumberLength)
          {
            case 7 :
              cond = (biR6 == 0 && biR5 < 0x40000000);
              break;
            case 6 :
              cond = (biR5 == 0 && biR4 < 0x40000000);
              break;
            case 5 :
              cond = (biR4 == 0 && biR3 < 0x40000000);
              break;
            case 4 :
              cond = (biR3 == 0 && biR2 < 0x40000000);
              break;
            case 3 :
              cond = (biR2 == 0 && biR1 < 0x40000000);
              break;
          }
          Divid = (biR1 << 31) + biR0;
          if (cond)
          {
            NumberLength--;
            if (NumberLength == 2)
            {
              int sqrtDivid = (int)Math.sqrt(Divid) + 1;
              fullRemainder = true;
              for (; index < nbrPrimes; index++)
              {
                rowPrimeSieveData = primeSieveData[index];
                Divisor = rowPrimeSieveData.value;
                if (testFactorA && index == newFactorAIndex)
                {
                  fullRemainder = true;
                  if (++indexFactorA == nbrFactorsA)
                  {
                    testFactorA = false;   // All factors of A were tested.
                  }
                  else
                  {
                    newFactorAIndex = aindex[indexFactorA];
                  }
                }
                for (;;)
                {
                  if (fullRemainder == false)
                  {
                    divis = (int)Divisor;
                    iRem = index2 - rowPrimeSieveData.soln1;
                    if (iRem >= divis)
                    {
                      if ((iRem -= divis) >= divis)
                      {
                        iRem %= divis;
                      }
                    }
                    else
                    {
                      iRem += (iRem >>> 31) * divis;
                    }
                    if (iRem != 0 && iRem != divis - rowPrimeSieveData.difsoln)
                    {
                      break;
                    }
                    fullRemainder = true;
                  }
                  else if (Divid % Divisor != 0)
                  {
                    break;
                  }
                  Divid /= Divisor;
                  sqrtDivid = (int)Math.sqrt(Divid) + 1;
                  expParity = 1 - expParity;
                  if (expParity == 0)
                  {
                    rowSquares[nbrSquares++] = (int)Divisor;
                  }
                }
                if (expParity != 0)
                {
                  rowMatrixBbeforeMerge[nbrColumns++] = index;
                  expParity = 0;
                }
                if (Divisor > sqrtDivid)
                {                     // End of trial division.
                  rowSquares[0] = nbrSquares;
                  index = nbrPrimes-1;
                  if (Divid <= primeTrialDivisionData[index].value &&
                      Divid > 1)
                  {          // Perform binary search to find the index.
                    left = -1;
                    median = right = nbrPrimes;
                    while (left != right)
                    {
                      median = ((right-left) >> 1) + left;
                      nbr = primeTrialDivisionData[median].value;
                      if (nbr < Divid)
                      {
                        if (median == left &&
                            congruencesFound >= matrixB.length)
                        {
                          return 0;
                        }
                        left = median;
                      }
                      else if (nbr > Divid)
                      {
                        right = median;
                      }
                      else
                      {
                    	break;
                      }
                    }
                    rowMatrixBbeforeMerge[nbrColumns++] = median;
                    rowMatrixBbeforeMerge[0] = nbrColumns;
                    return 1;
                  }
                  rowMatrixBbeforeMerge[0] = nbrColumns;
                  return Divid;
                }
                fullRemainder = false;
              }
              break;
            }
          }
        }             /* end while */
      }               /* end for */
    }
    rowSquares[0] = nbrSquares;
    rowMatrixBbeforeMerge[0] = nbrColumns;
    if (NumberLength > 2)
    {
      return 0;           // Very large quotient.
    }
    return Divid;
  }

  private void mergeArrays(int aindex[], int nbrFactorsA,
                           int rowMatrixB[], int rowMatrixBeforeMerge[],
                           PrimeTrialDivisionData primeTrialDivisionData[],
                           int rowSquares[])
  {
    int indexAindex = 0;
    int indexRMBBM = 1;
    int indexRMB = 1;
    int nbrColumns = rowMatrixBeforeMerge[0];

    while (indexAindex < nbrFactorsA && indexRMBBM < nbrColumns)
    {
      if (aindex[indexAindex] < rowMatrixBeforeMerge[indexRMBBM])
      {
        rowMatrixB[indexRMB++] = aindex[indexAindex++];
      }
      else if (aindex[indexAindex] > rowMatrixBeforeMerge[indexRMBBM])
      {
        rowMatrixB[indexRMB++] = rowMatrixBeforeMerge[indexRMBBM++];
      }
      else
      {
        rowSquares[rowSquares[0]++] =
                   primeTrialDivisionData[aindex[indexAindex++]].value;
        indexRMBBM++;
      }
    }
    while (indexAindex < nbrFactorsA)
    {
      rowMatrixB[indexRMB++] = aindex[indexAindex++];
    }
    while (indexRMBBM < nbrColumns)
    {
      rowMatrixB[indexRMB++] = rowMatrixBeforeMerge[indexRMBBM++];
    }
    rowMatrixB[0] = indexRMB;
  }

  private void SmoothRelationFound(
                           boolean positive, int matrixB[][],
                           int rowMatrixB[], int rowMatrixBbeforeMerge[],
                           int SieveLimit, int vectExpParity[],
                           long vectLeftHandSide[][], int index2,
                           PrimeTrialDivisionData primeTrialDivisionData[],
                           long startTime, int aindex[], int nbrFactorsA,
                           int rowSquares[], long biLinearCoeff[],
                           int NumberLength, long biT[], long biU[], long biR[])
  {
    int index;
    long D;
    int nbrSquares;
    if (congruencesFound == matrixB.length)
    {
      return;            // All congruences already found.
    }
    // Add all elements of aindex array to the rowMatrixB array discarding
    // duplicates.
    mergeArrays(aindex, nbrFactorsA, rowMatrixB, rowMatrixBbeforeMerge,
                primeTrialDivisionData, rowSquares);
    nbrSquares = rowSquares[0];
    ecm.LongToBigNbr(1, biR, NumberLength);
    ecm.LongToBigNbr(positive? 1: -1, biT, NumberLength);
    ecm.MultBigNbrByLong(biQuadrCoeff, index2 - SieveLimit, biU,
                               NumberLength);                     // Ax
    ecm.AddBigNbr(biU, biLinearCoeff, biU, NumberLength);         // Ax+B
    if ((biU[NumberLength - 1] & 0x40000000L) != 0)
    {                                        // If number is negative
      ecm.ChSignBigNbr(biU, NumberLength);   // make it positive.
    }
    for (index=1; index < nbrSquares; index++)
    {
      D = rowSquares[index];
      if (D == multiplier)
      {
      	ecm.AddBigNbr(biU, TestNbr, biU, NumberLength);
        ecm.DivBigNbrByLong(biU, D, biU, NumberLength);
      }
      else
      {
        ecm.MultBigNbrByLong(biR, D, biR, NumberLength);
      }
    }
    if (InsertNewRelation(matrixB, rowMatrixB, vectLeftHandSide, biT,
                          biU, biR, NumberLength))
    {
      smoothsFound++;
      ShowSIQSStatus(matrixB.length, startTime);
    }
    return;
  }

  private void PartialRelationFound(
                           boolean positive, int matrixB[][],
                           int rowMatrixB[], int rowMatrixBbeforeMerge[],
                           int SieveLimit, int vectExpParity[],
                           long vectLeftHandSide[][], int index2,
                           PrimeTrialDivisionData primeTrialDivisionData[],
                           long startTime, int aindex[], int nbrFactorsA,
                           long Divid, int rowPartials[],
                           int matrixPartial[][], int matrixPartialHashIndex[],
                           int indexMinFactorA, int rowSquares[],
                           long biLinearCoeff[], int NumberLength, long biT[],
                           long biR[], long biU[], long biV[],
                           int indexFactorsA[])
  {
    int index;
    int expParity;
    long D, Rem, Divisor;
    int nbrFactorsPartial;
    int prev;
    long seed;
    int hashIndex;
    int rowPartial[];
    int newDivid = (int)Divid;    // This number is greater than zero.
    int indexFactorA = 0;
    int oldDivid;
    int nbrSquares;
    int NumberLengthDivid;
    long DividLSDW;
    long biT2, biT3, biT4, biT5, biT6;
    int squareRootSize = NumberLength/2+1;
    int nbrColumns;
    PrimeTrialDivisionData rowPrimeTrialDivisionData;

    if (congruencesFound == matrixB.length)
    {
      return;
    }
    // Partial relation found.
    totalPartials++;
    // Check if there is already another relation with the same
    // factor outside the prime base.
    // Calculate hash index
    hashIndex = matrixPartialHashIndex[(int)(Divid & 0xFFE) >> 1];
    prev = -1;
    while (hashIndex >= 0)
    {
      rowPartial = matrixPartial[hashIndex];
      oldDivid = rowPartial[0];
      if (newDivid == oldDivid || newDivid == -oldDivid)
      {   // Match of partials.
        for (index = 0; index < squareRootSize; index++)
        {
          biV[index] = rowPartial[index + 2];
        }                           // biV = Old positive square root (Ax+B).
        for (; index < NumberLength; index++)
        {
          biV[index] = 0;
        }
        seed = rowPartial[squareRootSize + 2];
        getFactorsOfA(seed, nbrFactorsA, indexFactorsA, indexMinFactorA);
        ecm.LongToBigNbr(newDivid, biR, NumberLength);
        nbrFactorsPartial = 0;
                                              // biT = old (Ax+B)^2.
        ecm.MultBigNbr(biV, biV, biT, NumberLength);
                                              // biT = old (Ax+B)^2 - N.
        ecm.SubtractBigNbr(biT, TestNbr, biT, NumberLength);
        if (oldDivid < 0)
        {
          rowPartials[nbrFactorsPartial++] = 0; // Insert -1 as a factor.
        }
        if (biT[NumberLength-1] >= 0x40000000)
        {
          ecm.ChSignBigNbr(biT, NumberLength);   // Make it positive.
        }
        NumberLengthDivid = NumberLength;
           // The number is multiple of the big prime, so divide by it.
        ecm.DivBigNbrByLong(biT, newDivid, biT, NumberLengthDivid);
        if (biT[NumberLengthDivid-1] == 0 &&
            biT[NumberLengthDivid-2] < 0x40000000)
        {
          NumberLengthDivid--;
        }
        for (index=0; index < nbrFactorsA; index++)
        {
          ecm.DivBigNbrByLong(biT,
                 primeTrialDivisionData[indexFactorsA[index]].value, biT,
                 NumberLengthDivid);
          if (biT[NumberLengthDivid-1] == 0 &&
              biT[NumberLengthDivid-2] < 0x40000000)
          {
            NumberLengthDivid--;
          }
        }
        DividLSDW = (biT[1] << 31) + biT[0];
        biT2 = biT[2];
        biT3 = biT[3];
        biT4 = biT[4];
        biT5 = biT[5];
        biT6 = biT[6];
        for (index = 1; index < nbrPrimes; index++)
        {
          expParity = 0;
          if (index >= indexMinFactorA && indexFactorA < nbrFactorsA)
          {
            if (index == indexFactorsA[indexFactorA])
            {
              expParity = 1;
              indexFactorA++;
            }
          }
          rowPrimeTrialDivisionData = primeTrialDivisionData[index];
          Divisor = rowPrimeTrialDivisionData.value;
          for (;;)
          {
            switch (NumberLengthDivid)
            {
              case 7 :
                Rem = biT6*rowPrimeTrialDivisionData.exp6 +
                      biT5*rowPrimeTrialDivisionData.exp5 + 
                      biT4*rowPrimeTrialDivisionData.exp4 + 
                      biT3*rowPrimeTrialDivisionData.exp3 + 
                      biT2*rowPrimeTrialDivisionData.exp2 + DividLSDW;
                break;
              case 6 :
                Rem = biT5*rowPrimeTrialDivisionData.exp5 + 
                      biT4*rowPrimeTrialDivisionData.exp4 + 
                      biT3*rowPrimeTrialDivisionData.exp3 + 
                      biT2*rowPrimeTrialDivisionData.exp2 + DividLSDW;
                break;
              case 5 :
                Rem = biT4*rowPrimeTrialDivisionData.exp4 + 
                      biT3*rowPrimeTrialDivisionData.exp3 + 
                      biT2*rowPrimeTrialDivisionData.exp2 + DividLSDW;
                break;
              case 4 :
                Rem = biT3*rowPrimeTrialDivisionData.exp3 + 
                      biT2*rowPrimeTrialDivisionData.exp2 + DividLSDW;
                break;
              case 3 :
                Rem = biT2*rowPrimeTrialDivisionData.exp2 + DividLSDW;
                break;
              default :
                Rem = DividLSDW;
                break;
            }
            if (Rem%Divisor != 0)
            {
              break;
            }
            expParity = 1 - expParity;
            ecm.DivBigNbrByLong(biT, Divisor, biT, NumberLengthDivid);
            DividLSDW = (biT[1] << 31) + biT[0];
            biT2 = biT[2];
            biT3 = biT[3];
            biT4 = biT[4];
            biT5 = biT[5];
            biT6 = biT[6];
            if (expParity == 0)
            {
           	  rowSquares[rowSquares[0]++] = (int)Divisor;
            }
            if (NumberLengthDivid <= 2)
            {
              if (DividLSDW == 1)
              {               // Division has ended.
                break;
              }
            }
            else if (biT[NumberLengthDivid-1] == 0 &&
                     biT[NumberLengthDivid-2] < 0x40000000)
            {
              NumberLengthDivid--;
            }
          }
          if (expParity != 0)
          {
            rowPartials[nbrFactorsPartial++] = index;
          }
        }
        ecm.MultBigNbrByLong(biQuadrCoeff, index2 - SieveLimit, biT,
                             NumberLength);
        ecm.AddBigNbr(biT, biLinearCoeff, biT, NumberLength); // biT = Ax+B
        if ((biT[NumberLength - 1] & 0x40000000L) != 0)
        {                                        // If number is negative
          ecm.ChSignBigNbr(biT, NumberLength);   // make it positive.
        }
               // biU = Product of old Ax+B times new Ax+B
        ecm.MultBigNbrModN(biV, biT, biU, TestNbr, NumberLength);
        // Add all elements of aindex array to the rowMatrixB array discarding
        // duplicates.
        mergeArrays(aindex, nbrFactorsA, rowMatrixB, rowMatrixBbeforeMerge,
                    primeTrialDivisionData, rowSquares);
        rowMatrixBbeforeMerge[0] = nbrColumns = rowMatrixB[0];
        System.arraycopy(rowMatrixB, 1,                      // Source
                         rowMatrixBbeforeMerge, 1,           // Destination
                         nbrColumns);                        // Length
        mergeArrays(rowPartials, nbrFactorsPartial,
                    rowMatrixB, rowMatrixBbeforeMerge, primeTrialDivisionData,
                    rowSquares);
        nbrSquares = rowSquares[0];
        for (index=1; index < nbrSquares; index++)
        {
          D = rowSquares[index];
          if (D != multiplier)
          {
            ecm.MultBigNbrByLong(biR, D, biR, NumberLength);
          }
          else
          {
            ecm.AddBigNbr(biU, TestNbr, biU, NumberLength);
            ecm.DivBigNbrByLong(biU, multiplier, biU, NumberLength);
          }
        }
        if (rowMatrixB[0] > 1 &&
            InsertNewRelation(matrixB, rowMatrixB, vectLeftHandSide, biT,
                                biU, biR, NumberLength))
        {
          partialsFound++;
          ShowSIQSStatus(matrixB.length, startTime);
        }
        return;
      }
      else
      {
        prev = hashIndex;
        hashIndex = rowPartial[1]; // Get next index for same hash.
      }
    } /* end while */
    synchronized(firstPrimeSieveData)
    {
      if (hashIndex == -1 && nbrPartials < matrixPartial.length)
      { // No match and partials table is not full.
        // Add partial to table of partials.
        if (prev >= 0)
        {
          matrixPartial[prev][1] = nbrPartials;
        }
        else
        {
          matrixPartialHashIndex[(newDivid & 0xFFE) >> 1] = nbrPartials;
        }
        rowPartial = matrixPartial[nbrPartials];
        // Add all elements of aindex array to the rowMatrixB array discarding
        // duplicates.
        mergeArrays(aindex, nbrFactorsA, rowMatrixB, rowMatrixBbeforeMerge,
                    primeTrialDivisionData, rowSquares);
        ecm.LongToBigNbr(Divid, biR, NumberLength);
        nbrSquares = rowSquares[0];
        for (index=1; index < nbrSquares; index++)
        {
          D = rowSquares[index];
          ecm.MultBigNbrByLongModN(biR, D, biR, TestNbr, NumberLength);
          if (D == multiplier)
          {
            ecm.DivBigNbrByLong(biU, D, biU, NumberLength);
          }
        }
        rowPartial[0] = (positive?newDivid:-newDivid);
                   // Indicate last index with this hash.
        rowPartial[1] = -1; 
        ecm.MultBigNbrByLong(biQuadrCoeff, index2 - SieveLimit, biT,
                             NumberLength);
        ecm.AddBigNbr(biT, biLinearCoeff, biT, NumberLength); // biT = Ax+B
        if ((biT[NumberLength-1] & 0x4000000) != 0)
        {                      // If square root is negative convert to positive.
          ecm.ChSignBigNbr(biT, NumberLength);
        }
        for (index = 0; index < squareRootSize; index++)
        { 
          rowPartial[index + 2] = (int) biT[index];
        }
        rowPartial[squareRootSize + 2] = (int)oldSeed;
        nbrPartials++;
      }
    }               // End synchronized block.
    return;
  }

  private void SieveLocationHit(int matrixB[][],
                           int rowMatrixB[], int rowMatrixBbeforeMerge[],
                           int SieveLimit, int vectExpParity[],
                           long vectLeftHandSide[][], int index2,
                           PrimeSieveData primeSieveData[],
                           PrimeTrialDivisionData primeTrialDivisionData[],
                           long startTime, int aindex[],
                           int nbrFactorsA, int rowPartials[],
                           int matrixPartial[][], int matrixPartialHashIndex[],
                           long afact[],
                           long largePrimeUpperBound, int indexMinFactorA,
                           int multiplier, int rowSquares[], long biDividend[],
                           int NumberLength, long biT[], long biLinearCoeff[],
                           long biR[], long biU[], long biV[],
                           int indexFactorsA[])
  {
    boolean positive;
    int NumberLengthDivid;
    int index;
    long Divid;
    int nbrColumns;

    trialDivisions++;
    ecm.MultBigNbrByLong(biQuadrCoeff, index2 - SieveLimit, biT,
                         NumberLength);                       // Ax
    ecm.AddBigNbr(biT, biLinearCoeff, biT, NumberLength);     // Ax+B
    ecm.MultBigNbr(biT, biT, biDividend, NumberLength);       // (Ax+B)^2
    ecm.SubtractBigNbr(biDividend, TestNbr, biDividend, NumberLength); // To factor: (Ax+B)^2-N
            /* factor biDividend */

//    if (multiplier > 1)
//    {
//      while (ecm.RemDivBigNbrByLong(biDividend, multiplier * multiplier,
//                                          NumberLength) == 0)
//      {
//        ecm.DivBigNbrByLong(biDividend, multiplier * multiplier,
//                                  biDividend, NumberLength);
//      }
//    }
    NumberLengthDivid = NumberLength; /* Number length for dividend */
    positive = true;
    if (biDividend[NumberLengthDivid - 1] >= 0x40000000)
    { /* Negative */
      positive = false;
      ecm.ChSignBigNbr(biDividend, NumberLengthDivid); // Convert to positive
    }
    rowSquares[0] = 1;
    for (index = 0; index < nbrFactorsA; index++)
    {
      ecm.DivBigNbrByLong(biDividend, afact[index], biDividend,
                                NumberLengthDivid);
      if ((biDividend[NumberLengthDivid - 1] == 0
          && biDividend[NumberLengthDivid - 2] < 0x40000000L))
      {
        NumberLengthDivid--;
      }
    }
    nbrColumns = 1;
    if (!positive)
    {                                  // Insert -1 as a factor.
      rowMatrixBbeforeMerge[nbrColumns++] = 0;
    }
    rowMatrixBbeforeMerge[0] = nbrColumns;
    Divid = PerformTrialDivision(primeSieveData, primeTrialDivisionData,
                                 nbrPrimes, rowMatrixBbeforeMerge,
                                 aindex, nbrFactorsA, index2, biDividend,
                                 rowSquares, NumberLengthDivid, biT);
    if (Divid == 1)
    { // Smooth relation found.
      SmoothRelationFound(positive, matrixB, rowMatrixB,
                                   rowMatrixBbeforeMerge,
                                   SieveLimit, vectExpParity, vectLeftHandSide,
                                   index2, primeTrialDivisionData, startTime,
                                   aindex, nbrFactorsA, rowSquares,
                                   biLinearCoeff, NumberLength, biT, biU, biR);
    }
    else
    {
      if (Divid > 0 && Divid < largePrimeUpperBound)
      {
        PartialRelationFound(positive, matrixB, rowMatrixB,
                                 rowMatrixBbeforeMerge,
                                 SieveLimit, vectExpParity, vectLeftHandSide,
                                 index2, primeTrialDivisionData, startTime,
                                 aindex, nbrFactorsA, Divid, rowPartials,
                                 matrixPartial, matrixPartialHashIndex,
                                 indexMinFactorA, rowSquares, biLinearCoeff,
                                 NumberLength, biT, biR, biU, biV,
                                 indexFactorsA);
      }
    }
    return;
  }

  private long getFactorsOfA(long seed, int nbrFactorsA, int aindex[],
                             int indexMinFactorA)
  {
    int index, index2, i, tmp;
    for (index = 0; index < nbrFactorsA; index++)
    {
      do
      {
        seed = (1141592621 * seed + 321435) & 0xFFFFFFFFL;
        i = (int) (((seed * span) >> 32) + indexMinFactorA);
        for (index2 = 0; index2 < index; index2++)
        {
          if (aindex[index2] == i || aindex[index2] == i + 1)
          {
            break;
          }
        }
      }
      while (index2 < index);
      aindex[index] = i;
    }
    for (index=0; index<nbrFactorsA; index++)    // Sort factors of A.
    {
      for (index2=index+1; index2<nbrFactorsA; index2++)
      {
        if (aindex[index] > aindex[index2])
        {
          tmp = aindex[index];
          aindex[index] = aindex[index2];
          aindex[index2] = tmp;
        }
      }
    }
    return seed;
  }

  /************************************************************************/
  /* Multithread procedure:                                               */
  /*                                                                      */
  /* 1) Main thread generates factor base and other parameters.           */
  /* 2) Start M threads where the number M is specified by the user in a  */
  /*    box beneath the applet.                                           */
  /* 3) For each polynomial:                                              */
  /*    3a) Main thread generates the data for the set of 2^n polynomials.*/
  /*    3b) Each child thread computes a range of polynomials             */
  /*        (u*2^n/M to (u+1)*2^n/M exclusive).                           */
  /* Partial and full relation routines must be synchronized.             */
  /************************************************************************/
  BigInteger FactoringSIQS(BigInteger NbrToFactor)
  {
    long FactorBase;
    int currentPrime;
    int NbrMod;
    PrimeSieveData rowPrimeSieveData;
    PrimeTrialDivisionData rowPrimeTrialDivisionData;
    long Power2, SqrRootMod, fact;
    int index;
    long D, E, Q, V, W, X, Y, Z, T1, V1, W1, Y1;
    double Temp, Prod;
    double bestadjust;
    int i, j, multiplier;
    int arrmult[] = { 1, 2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43,
                      47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97};
    double adjustment[] = new double[arrmult.length];
    int halfCurrentPrime;
    double dNumberToFactor;
    ValuesSieved = 0;
    int NumberLength = this.NumberLength;

    nbrThreadFinishedPolySet = 0;
    factorSiqs = null;
    threadArray = new Thread[numberThreads];
    nbrPartials = 0;
    congruencesFound = 0;
    Temp = Math.log(NbrToFactor.doubleValue());
    nbrPrimes = (int) Math.exp(Math.sqrt(Temp * Math.log(Temp)) * 0.318);
    SieveLimit = (int) Math.exp(8.5 + 0.015 * Temp) & 0xFFFFFFF8;
    nbrFactorsA = NbrToFactor.bitLength() / 28 + 1;
    primeSieveData = new PrimeSieveData[nbrPrimes + 3];
    primeTrialDivisionData = new PrimeTrialDivisionData[nbrPrimes + 3];
    afact = new long[nbrFactorsA];
    aindex = new int[nbrFactorsA];
    amodq = new int[nbrFactorsA];
    NbrPolynomials = (1 << (nbrFactorsA - 1)) - 1;
    NumberLength = ecm.BigNbrToBigInt(NbrToFactor, TestNbr);
    TestNbr[NumberLength++] = 0;
    System.arraycopy(TestNbr, 0,          // Source
                     TestNbr2, 0,         // Destination
                     NumberLength);       // Length
    matrixPartialHashIndex = new int[2048];
    for (i = matrixPartialHashIndex.length - 1; i >= 0; i--)
    {
      matrixPartialHashIndex[i] = -1;
    }
    ecmApplet.textAreaContents = "";
    ecmApplet.StringToLabel = "Factoring ";
    ecmApplet.insertBigNbr(NbrToFactor);
    ecmApplet.addStringToLabel("(" + NbrToFactor.toString().length() +
                               " digits)");
    SIQSInfoText =
      ecmApplet.textAreaContents
        + ecmApplet.StringToLabel
        + "\n\nSIQS parameters: "
        + nbrPrimes
        + " primes, sieve limit: "
        + SieveLimit;
    ecmApplet.lowerTextArea.setText(
      SIQSInfoText + "\nSearching for Knuth-Schroeppel multiplier...");

    /************************/
    /* Compute startup data */
    /************************/

    for (index=primeSieveData.length-1; index>=0; index--)
    {
      primeSieveData[index] = new PrimeSieveData();
    }
    for (index=primeSieveData.length-1; index>=0; index--)
    {
      primeSieveData[index].Bainv2 = new int[nbrFactorsA-1];
    }
    for (index=primeTrialDivisionData.length-1; index>=0; index--)
    {
      primeTrialDivisionData[index] = new PrimeTrialDivisionData();
    }
    /* search for best Knuth-Schroeppel multiplier */
    bestadjust = -10.0e0;
    primeSieveData[0].value = 1;
    primeTrialDivisionData[0].value = 1;
    rowPrimeSieveData = primeSieveData[1];
    rowPrimeTrialDivisionData = primeTrialDivisionData[1];
    rowPrimeSieveData.value = 2;
    rowPrimeTrialDivisionData.value = 2;
       // (2^31)^(j+1) mod 2
    rowPrimeTrialDivisionData.exp2 =
        rowPrimeTrialDivisionData.exp3 = rowPrimeTrialDivisionData.exp4 =
        rowPrimeTrialDivisionData.exp5 = rowPrimeTrialDivisionData.exp6 = 0;

    NbrMod = NbrToFactor.and(BigInteger.valueOf(7)).intValue();
    for (j=0; j<arrmult.length; j++)
    {
      int mod = NbrMod * arrmult[j] % 8;
      adjustment[j] = 0.34657359; /*  (ln 2)/2  */
      if (mod == 1)
        adjustment[j] *= (4.0e0);
      if (mod == 5)
        adjustment[j] *= (2.0e0);
      adjustment[j] -= Math.log((double) arrmult[j]) / (2.0e0);
    }
    currentPrime = 3;
    while (currentPrime < 10000)
    { 
      NbrMod = (int) ecm.RemDivBigNbrByLong(TestNbr, currentPrime,
                              NumberLength);
      halfCurrentPrime = (currentPrime - 1) / 2;
      int jacobi = (int)modPow(NbrMod, halfCurrentPrime, currentPrime);
      double dp = (double) currentPrime;
      double logp = Math.log(dp) / dp;
      for (j=0; j<arrmult.length; j++)
      {
        if (arrmult[j] == currentPrime)
        {
          adjustment[j] += logp;
        }
        else if (jacobi * (int)modPow(arrmult[j], halfCurrentPrime,
                 currentPrime)%currentPrime == 1)
        {
          adjustment[j] += 2 * logp;
        }
      }
     calculate_new_prime1 :
      for (;;)
      {
        currentPrime += 2;
        for (Q = 3; Q * Q <= currentPrime; Q += 2)
        { /* Check if currentPrime is prime */
          if (currentPrime % Q == 0)
          {
            continue calculate_new_prime1;
          }
        }
        break; /* Prime found */
      }
    }  /* end while */
    multiplier = 1;
    for (j=0; j<arrmult.length; j++)
    {
      if (adjustment[j] > bestadjust)
      { /* find biggest adjustment */
        bestadjust = adjustment[j];
        multiplier = arrmult[j];
      }
    } /* end while */
//multiplier=2;    //DEBUG BORRAR
    ecm.MultBigNbrByLong(TestNbr2, multiplier, TestNbr, NumberLength);
    matrixPartial = new int[nbrPrimes * 8][NumberLength/2 + 4];
    FactorBase = currentPrime;
    matrixB = new int[nbrPrimes + 50][];
    vectLeftHandSide = new long[matrixB.length][];
    vectExpParity = new int[matrixB.length];
    rowPrimeSieveData.modsqrt = NbrToFactor.testBit(0) ? 1 : 0;
    switch ((int)TestNbr[0] & 0x07)
    {
      case 1:
        log2 = 3;
        break;
      case 5:
        log2 = 1;
        break;
      default:
        log2 = 1;
        break;
    }
    if (multiplier != 1 && multiplier != 2)
    {
      rowPrimeSieveData = primeSieveData[2];
      rowPrimeTrialDivisionData = primeTrialDivisionData[2];
      rowPrimeSieveData.value = multiplier;
      rowPrimeTrialDivisionData.value = multiplier;
      rowPrimeSieveData.modsqrt = 0;
      D = (1L << 62) % multiplier;
      rowPrimeTrialDivisionData.exp2 = (int)D;  // (2^31)^2 mod multiplier
      D = (D << 31) % multiplier;
      rowPrimeTrialDivisionData.exp3 = (int)D;  // (2^31)^3 mod multiplier
      D = (D << 31) % multiplier;
      rowPrimeTrialDivisionData.exp4 = (int)D;  // (2^31)^4 mod multiplier
      D = (D << 31) % multiplier;
      rowPrimeTrialDivisionData.exp5 = (int)D;  // (2^31)^5 mod multiplier
      D = (D << 31) % multiplier;
      rowPrimeTrialDivisionData.exp6 = (int)D;  // (2^31)^6 mod multiplier
      j = 3;
    }
    else
    {
      j = 2;
    }
    currentPrime = 3;
    while (j < nbrPrimes)
    { /* select small primes */
      NbrMod = (int) ecm.RemDivBigNbrByLong(TestNbr, currentPrime,
                                NumberLength);
      if (currentPrime != multiplier &&
          modPow(NbrMod, (currentPrime - 1) / 2, currentPrime) == 1)
      {
        /* use only if Jacobi symbol = 0 or 1 */
        rowPrimeSieveData = primeSieveData[j];
        rowPrimeTrialDivisionData = primeTrialDivisionData[j];
        rowPrimeSieveData.value = (int)currentPrime;
        rowPrimeTrialDivisionData.value = (int)currentPrime;
        D = (1L << 62) % currentPrime;
        rowPrimeTrialDivisionData.exp2 = (int)D; // (2^31)^2 mod currentPrime
        D = (D << 31) % currentPrime;
        rowPrimeTrialDivisionData.exp3 = (int)D; // (2^31)^3 mod currentPrime
        D = (D << 31) % currentPrime;
        rowPrimeTrialDivisionData.exp4 = (int)D; // (2^31)^4 mod currentPrime
        D = (D << 31) % currentPrime;
        rowPrimeTrialDivisionData.exp5 = (int)D; // (2^31)^5 mod currentPrime
        D = (D << 31) % currentPrime;
        rowPrimeTrialDivisionData.exp6 = (int)D; // (2^31)^6 mod currentPrime
        if (currentPrime % 4 == 3)
        {
          SqrRootMod = modPow(NbrMod, (currentPrime + 1) / 4, currentPrime);
        }
        else
        {
          if (currentPrime % 8 == 5)
          {
            SqrRootMod =
              modPow(NbrMod * 2, (currentPrime - 5) / 8, currentPrime);
            SqrRootMod =
              ((((2 * NbrMod * SqrRootMod % currentPrime) * SqrRootMod - 1)
                % currentPrime)
                * NbrMod
                % currentPrime)
                * SqrRootMod
                % currentPrime;
          }
          else
          { /* p = 1 (mod 8) */
            Q = currentPrime - 1;
            E = 0;
            Power2 = 1;
            do
            {
              E++;
              Q /= 2;
              Power2 *= 2;
            }
            while ((Q & 1) == 0); /* E >= 3 */
            Power2 /= 2;
            X = 1;
            do
            {
              X++;
              Z = modPow(X, Q, currentPrime);
            }
            while (modPow(Z, Power2, currentPrime) == 1);
            Y = Z;
            X = modPow(NbrMod, (Q - 1) / 2, currentPrime);
            V = NbrMod * X % currentPrime;
            W = V * X % currentPrime;
            while (W != 1)
            {
              T1 = 0;
              D = W;
              while (D != 1)
              {
                D = D * D % currentPrime;
                T1++;
              }
              D = modPow(Y, 1 << (E - T1 - 1), currentPrime);
              Y1 = D * D % currentPrime;
              E = T1;
              V1 = V * D % currentPrime;
              W1 = W * Y1 % currentPrime;
              Y = Y1;
              V = V1;
              W = W1;
            } /* end while */
            SqrRootMod = V;
          } /* end if */
        } /* end if */
        rowPrimeSieveData.modsqrt = (int)SqrRootMod;
        j++;
      } /* end while */
      calculate_new_prime2 : for (;;)
      {
        currentPrime += 2;
        for (Q = 3; Q * Q <= currentPrime; Q += 2)
        { /* Check if currentPrime is prime */
          if (currentPrime % Q == 0)
          {
            continue calculate_new_prime2;
          }
        }
        break; /* Prime found */
      }
    } /* End while */

    FactorBase = currentPrime;
    largePrimeUpperBound = 100 * FactorBase;
    dNumberToFactor = NbrToFactor.doubleValue();
    SIQSInfoText += "\nMultiplier: " + multiplier + ", factor base: " + FactorBase;
    ecmApplet.lowerTextArea.setText(SIQSInfoText);
    firstLimit = 2;
    for (j = 2; j < nbrPrimes; j++)
    {
      firstLimit *= (int) (primeSieveData[j].value);
      if (firstLimit > 2 * SieveLimit)
      {
        break;
      }
    }
    smallPrimeUpperLimit = j + 1;
    threshold =
      (byte) (Math
        .log(
          Math.sqrt(dNumberToFactor) * SieveLimit / (FactorBase * 64) / 
            primeSieveData[j+1].value)
        / Math.log(3)+1);
    firstLimit = (int) (Math.log(dNumberToFactor) / 3);
    for (secondLimit = firstLimit; secondLimit < nbrPrimes; secondLimit++)
    {
      if (primeSieveData[secondLimit].value * 2 > SieveLimit)
      {
        break;
      }
    }
    for (thirdLimit = secondLimit; thirdLimit < nbrPrimes; thirdLimit++)
    {
      if (primeSieveData[thirdLimit].value > 2 * SieveLimit)
      {
        break;
      }
    }
    nbrPrimes2 = nbrPrimes - 4;
    startTime = System.currentTimeMillis();
    // Sieve start time in milliseconds.
    Prod = Math.sqrt(2 * dNumberToFactor) / (double) SieveLimit;
    fact = (long) Math.pow(Prod, 1 / (float) nbrFactorsA);
    for (i = 2;; i++)
    {
      if (primeSieveData[i].value > fact)
      {
        break;
      }
    }
    span = nbrPrimes / (2*nbrFactorsA*nbrFactorsA);
    if (nbrPrimes < 500)
    {
      span *= 2;
    }
    indexMinFactorA = i - span / 2;
    this.multiplier = multiplier;
      /*********************************************/
      /* Generate sieve threads                    */
      /*********************************************/
    for (threadNumber=0; threadNumber<numberThreads; threadNumber++)
    {
      new Thread(this).start();                // Start new thread.
      synchronized(amodq)
      {
        while (threadArray[threadNumber] == null &&
               ecmApplet.TerminateThread == false)
        {
          try
          {
            amodq.wait();
          } catch (InterruptedException ie) {}
        }
      }
    }
    synchronized(matrixB)
    {
      while (factorSiqs == null && ecmApplet.TerminateThread == false)
      {
        try
        {
          matrixB.wait();
        } catch(InterruptedException ie) {}
      }
    }
    if (ecmApplet.TerminateThread || factorSiqs.signum() == 0)
    {
      throw new ArithmeticException();
    }
    for (threadNumber=0; threadNumber<numberThreads; threadNumber++)
    {                 // Wake up all sieve threads so they can terminate.
      if (threadArray[threadNumber].isAlive())
      {
        try
        {
          threadArray[threadNumber].interrupt();
        } catch (Exception e) {}
      }
    }
    synchronized(this)
    {
      ecmApplet.polynomialsSieved = polynomialsSieved;
      ecmApplet.trialDivisions = trialDivisions;
      ecmApplet.smoothsFound = smoothsFound;
      ecmApplet.totalPartials = totalPartials;
      ecmApplet.partialsFound = partialsFound;
      ecmApplet.ValuesSieved = ValuesSieved;
    }
    return factorSiqs;
  }

  void ShowSIQSStatus(int matrixBLength, long startTime)
  {
    long New, u;
    long oldTime = ecmApplet.OldTimeElapsed;

    if (ecmApplet.TerminateThread)
    {
      throw new ArithmeticException();
    }
//    Thread.yield();
    New = System.currentTimeMillis();
    if (oldTime >= 0
      && oldTime / 1000 != (oldTime + New - ecmApplet.Old) / 1000)
    {
      oldTime += New - ecmApplet.Old;
      ecmApplet.OldTimeElapsed = oldTime;
      ecmApplet.Old = New;
      long t = oldTime / 1000;
      if (New - startTime > 5 && congruencesFound > 10)
      {
        u = (New - startTime) * (matrixBLength - congruencesFound) /
                                congruencesFound / 1000;
        ecmApplet.labelStatus.setText(
            ecmApplet.GetDHMS(t)
            + "     Congruences found: "
            + congruencesFound
            + " ("
            + (int) ((float) (congruencesFound * 100) / (float) matrixBLength)
            + "%)   End sieve in "
            + ecmApplet.GetDHMS(u/2));
      }
      else
      {
        ecmApplet.labelStatus.setText(
            ecmApplet.GetDHMS(t)
            + "     Congruences found: "
            + congruencesFound
            + " ("
            + (int) ((float) (congruencesFound * 100) / (float) matrixBLength)
            + "%)");
      }
    }
  }

  private int EraseSingletons(
    int nbrPrimes,
    int[][] matrixB,
    long[][] vectLeftHandSide,
    int[] vectExpParity,
    PrimeTrialDivisionData[] primeTrialDivisionData)
  {
    int row, column, delta;
    int[] rowMatrixB;
    int matrixBlength = matrixB.length;
    int[] newColumns = new int[matrixBlength];
    // Find singletons in matrixB storing in array vectExpParity the number
    // of primes in each column.
    do
    {   // The singleton removal phase must run until there are no more
        // singletons to erase.
      for (column = nbrPrimes - 1; column >= 0; column--)
      {                  // Initialize number of primes per column to zero.
        vectExpParity[column] = 0;
      }
      for (row = matrixBlength - 1; row >= 0; row--)
      {                  // Traverse all rows of the matrix.
        rowMatrixB = matrixB[row];
        for (column = rowMatrixB.length - 1; column >= 0; column--)
        {                // A prime appeared in that column.
          vectExpParity[rowMatrixB[column]]++;
        }
      }
      row = 0;
      for (column=0; column<nbrPrimes; column++)
      {
        if (vectExpParity[column] > 1)
        {                // Useful column found with at least 2 primes.
          newColumns[column] = row;
          primeTrialDivisionData[row++].value =
                              primeTrialDivisionData[column].value;
        }
      }
      nbrPrimes = row;
      delta = 0;
      // Erase singletons from matrixB. The rows to be erased are those where the
      // the corresponding element of the array vectExpParity equals 1.
      for (row = 0; row < matrixBlength; row++)
      {                  // Traverse all rows of the matrix.
        rowMatrixB = matrixB[row];
        for (column = rowMatrixB.length - 1; column >= 0; column--)
        {                // Traverse all columns.
          if (vectExpParity[rowMatrixB[column]] == 1)
          {              // Singleton found: erase this row.
            delta++;
            break;
          }
        }
        if (column < 0)
        {                // Singleton not found: move row upwards.
          matrixB[row - delta] = matrixB[row];
          vectLeftHandSide[row - delta] = vectLeftHandSide[row];
        }
      }
      matrixBlength -= delta;      // Update number of rows of the matrix.
      for (row = 0; row < matrixBlength; row++)
      {                  // Traverse all rows of the matrix.
        rowMatrixB = matrixB[row];
        for (column = rowMatrixB.length - 1; column >= 0; column--)
        {                // Change all column indexes in this row.
          rowMatrixB[column] = newColumns[rowMatrixB[column]];
        }
      }
    } while (delta > 0);           // End loop if number of rows did not
                                   // change.
    primeTrialDivisionData[0].exp2 = nbrPrimes;
    return matrixBlength;
  }

  /************************/
  /* Linear algebra phase */
  /************************/
  private boolean LinearAlgebraPhase(
    int nbrPrimes,
    int[][] matrixB,
    PrimeTrialDivisionData[] primeTrialDivisionData,
    int[] vectExpParity,
    long[][] vectLeftHandSide, long biT[], long biR[], long biU[],
    int NumberLength)
  {
    int mask, row, j, index;
    int[] rowMatrixB;
    int primeIndex;
    int NumberLengthBak;
       // Get new number of rows after erasing singletons.
    int matrixBlength = EraseSingletons(nbrPrimes, matrixB, vectLeftHandSide,
                                  vectExpParity, primeTrialDivisionData);
    ecmApplet.lowerTextArea.setText(
             SIQSInfoText +"\nSolving "+matrixBlength+"x"+
             primeTrialDivisionData[0].exp2+
             " congruence matrix using Block Lanczos algorithm");
    primeTrialDivisionData[0].exp2 = 0;         // Restore correct value.
    int[] matrixV = BlockLanczos(matrixB, matrixBlength);
      // The rows of matrixV indicate which rows must be multiplied so no
      // primes are multiplied an odd number of times.
    for (mask = 1; mask != 0; mask *= 2)
    {
      ecm.LongToBigNbr(1, biT, NumberLength);
      ecm.LongToBigNbr(1, biR, NumberLength);    
      for (row = matrixBlength - 1; row >= 0; row--)
      {
        vectExpParity[row] = 0;
      }
      NumberLengthBak = NumberLength;
      if (TestNbr[NumberLength-1] == 0 && TestNbr[NumberLength-2] < 0x40000000)
      {
        NumberLength--;
      }  
      for (row = matrixBlength - 1; row >= 0; row--)
      {
        if ((matrixV[row] & mask) != 0)
        {
          ecm.MultBigNbrModN(vectLeftHandSide[row], biR, biU, TestNbr,
                         NumberLength);
          for (j = 0; j <= NumberLength; j++)
          {
            biR[j] = biU[j];
          } 
          rowMatrixB = matrixB[row];
          for (j = rowMatrixB.length - 1; j >= 0; j--)
          {
            primeIndex = rowMatrixB[j];
            vectExpParity[primeIndex] ^= 1;
            if (vectExpParity[primeIndex] == 0)
            {
              if (primeIndex == 0)
              {
                ecm.SubtractBigNbr(TestNbr, biT, biT, NumberLength); // Multiply biT by -1.
              }
              else
              {
                ecm.MultBigNbrByLongModN(biT,
                        primeTrialDivisionData[primeIndex].value, biT,
                        TestNbr, NumberLength);
              }
            }
          }
        }
      }
      NumberLength = NumberLengthBak;
      ecm.SubtractBigNbrModN(biR, biT, biR, TestNbr, NumberLength);
      ecmApplet.GcdBigNbr(biR, TestNbr2, biT, NumberLength);
      index = 0;
      if (biT[0] == 1)
      {
        for (index = 1; index < NumberLength; index++)
        {
          if (biT[index] != 0)
          {
            break;
          }
        }
      }
      if (index < NumberLength)
      { /* GCD is not 1 */
        for (index = 0; index < NumberLength; index++)
        {
          if (biT[index] != TestNbr2[index])
          {
            break;
          }
        }
        if (index < NumberLength)
        { /* GCD is not 1 */
          return true;
        }
      }
    }
    return false;
  }

  private long modPow(long NbrMod, long Expon, long currentPrime)
  {
    long Power = 1;
    long Square = NbrMod;
    while (Expon != 0)
    {
      if ((Expon & 1) == 1)
      {
        Power = (Power * Square) % currentPrime;
      }
      Square = (Square * Square) % currentPrime;
      Expon /= 2;
    }
    return Power;
  }
  private synchronized boolean InsertNewRelation(
    int[][] matrixB,
    int[] rowMatrixB,
    long[][] vectLeftHandSide, long biT[], long biU[], long biR[],
    int NumberLength)
  {
    int i, k;
    int nbrColumns = rowMatrixB[0]-1;
    int curRowMatrixB[];
    // Insert it only if it is different from previous relations.
    if (congruencesFound >= matrixB.length)
    {                   // Discard excess congruences.
      return true;
    }
    for (i = 0; i < congruencesFound; i++)
    {
      curRowMatrixB = matrixB[i];
      if (nbrColumns > curRowMatrixB.length)
      {
        continue;
      }
      if (nbrColumns == curRowMatrixB.length)
      {
        for (k = 1; k <= nbrColumns; k++)
        {
          if (rowMatrixB[k] != curRowMatrixB[k-1])
          {
            break;
          }
        }
        if (k > nbrColumns)
        {
          return false; // Do not insert same relation.
        }
        if (rowMatrixB[k] > curRowMatrixB[k-1])
        {
          continue;
        }
      }
      for (k = congruencesFound - 1; k >= i; k--)
      {
        matrixB[k + 1] = matrixB[k];
        vectLeftHandSide[k + 1] = vectLeftHandSide[k];
      }
      break;
    }
    /* Convert negative numbers to the range 0 <= n < TestNbr */
    if ((TestNbr[0] & 1) == 0)
    {
      ecm.DivBigNbrByLong(TestNbr, 2, TestNbr2, NumberLength);
             // If biR >= TestNbr perform biR = biR - TestNbr.
      for (k = 0; k < NumberLength; k++)
      {
        biT[k] = 0;
      }
      ecm.AddBigNbrModN(biR, biT, biR, TestNbr2, NumberLength);
      ecmApplet.ModInvBigNbr(biR, biT, TestNbr2, NumberLength);
    }
    else
    {
      ecmApplet.ModInvBigNbr(biR, biT, TestNbr, NumberLength);
    }
    if ((biU[NumberLength - 1] & 0x40000000L) != 0)
    {
      ecm.AddBigNbr(biU, TestNbr, biU, NumberLength);
    }

    // Compute biU / biR  (mod TestNbr)

    ecm.MultBigNbrModN(biU, biT, biR, TestNbr, NumberLength);

    matrixB[i] = new int[nbrColumns];        // Add relation to matrix B.
    System.arraycopy(rowMatrixB, 1,          // Source
                     matrixB[i], 0,          // Destination
                     nbrColumns);            // Number of elements to copy.
    vectLeftHandSide[i] = new long[NumberLength];
    System.arraycopy(biR, 0,                 // Source
                     vectLeftHandSide[i], 0, // Destination
                     NumberLength);          // Number of elements to copy.
    congruencesFound++;
    return true;
  }

  private static long modInv(long NbrMod, long currentPrime)
  {
    long QQ, T1, T3;
    long U1 = 1;
    long U3 = NbrMod;
    long V1 = 0;
    long V3 = currentPrime;
    while (V3 != 0)
    {
      QQ = U3 / V3;
      T1 = U1 - V1 * QQ;
      T3 = U3 - V3 * QQ;
      U1 = V1;
      U3 = V3;
      V1 = T1;
      V3 = T3;
    }
    return (U1 + currentPrime) % currentPrime;
  }

  private static int modInv(int NbrMod, int currentPrime)
  {
    int QQ, T1, T3;
    int U1 = 1;
    int U3 = NbrMod;
    int V1 = 0;
    int V3 = currentPrime;
    while (V3 != 0)
    {
      QQ = U3 / V3;
      T1 = U1 - V1 * QQ;
      T3 = U3 - V3 * QQ;
      U1 = V1;
      U3 = V3;
      V1 = T1;
      V3 = T3;
    }
    return (U1 + currentPrime) % currentPrime;
  }

  /* Multiply binary matrices of length m x 32 by 32 x 32 */
  /* The product matrix has size m x 32. Then add it to a m x 32 matrix. */
  private void MatrixMultAdd(int[] LeftMatr, int[] RightMatr, int[] ProdMatr)
  {
    int leftMatr;
    int matrLength = LeftMatr.length;
    int prodMatr;
    int row, col;
    for (row = 0; row < matrLength; row++)
    {
      prodMatr = ProdMatr[row];
      leftMatr = LeftMatr[row];
      col = 0;
      while (leftMatr != 0)
      {
        if (leftMatr < 0)
        {
          prodMatr ^= RightMatr[col];
        }
        leftMatr *= 2;
        col++;
      }
      ProdMatr[row] = prodMatr;
    }
  }
  /* Multiply binary matrices of length m x 32 by 32 x 32 */
  /* The product matrix has size m x 32 */
  private void MatrixMultiplication(int[] LeftMatr, int[] RightMatr, int[] ProdMatr)
  {
    int leftMatr;
    int matrLength = LeftMatr.length;
    int prodMatr;
    int row, col;
    for (row = 0; row < matrLength; row++)
    {
      prodMatr = 0;
      leftMatr = LeftMatr[row];
      col = 0;
      while (leftMatr != 0)
      {
        if (leftMatr < 0)
        {
          prodMatr ^= RightMatr[col];
        }
        leftMatr *= 2;
        col++;
      }
      ProdMatr[row] = prodMatr;
    }
  }

  /* Multiply the transpose of a binary matrix of length n x 32 by */
  /* another binary matrix of length n x 32 */
  /* The product matrix has size 32 x 32 */
  private void MatrTranspMult(int[] LeftMatr, int[] RightMatr, int[] ProdMatr)
  {
    int prodMatr;
    int matrLength = LeftMatr.length;
    int row, col;
    int iMask = 1;
    for (col = 31; col >= 0; col--)
    {
      prodMatr = 0;
      for (row = 0; row < matrLength; row++)
      {
        if ((LeftMatr[row] & iMask) != 0)
        {
          prodMatr ^= RightMatr[row];
        }
      }
      ProdMatr[col] = prodMatr;
      iMask *= 2;
    }
  }

  private void MatrixAddition(int[] leftMatr, int[] rightMatr, int[] sumMatr)
  {
    for (int row = leftMatr.length - 1; row >= 0; row--)
    {
      sumMatr[row] = leftMatr[row] ^ rightMatr[row];
    }
  }

  private void MatrMultBySSt(int[] Matr, int diagS, int[] Prod)
  {
    for (int row = Matr.length - 1; row >= 0; row--)
    {
      Prod[row] = diagS & Matr[row];
    }
  }

  /* Compute Bt * B * input matrix where B is the matrix that holds the */
  /* factorization relations */
  private void MultiplyAByMatrix(
    int[][] matrixB,
    int[] Matr,
    int[] TempMatr,
    int[] ProdMatr,
    int matrixBlength)
  {
    int index;
    int prodMatr;
    int row;
    int[] rowMatrixB;

    /* Compute TempMatr = B * Matr */
    for (row = matrixBlength - 1; row >= 0; row--)
    {
      TempMatr[row] = 0;
    }
    for (row = matrixBlength - 1; row >= 0; row--)
    {
      rowMatrixB = matrixB[row];
      for (index = rowMatrixB.length - 1; index >= 0; index--)
      {
        TempMatr[rowMatrixB[index]] ^= Matr[row];
      }
    }

    /* Compute ProdMatr = Bt * TempMatr */
    for (row = matrixBlength - 1; row >= 0; row--)
    {
      prodMatr = 0;
      rowMatrixB = matrixB[row];
      for (index = rowMatrixB.length - 1; index >= 0; index--)
      {
        prodMatr ^= TempMatr[rowMatrixB[index]];
      }
      ProdMatr[row] = prodMatr;
    }
  }

  private void colexchange(int[] XmY, int[] V, int[] V1, int[] V2,
                           int col1, int col2)
  {
    int row;
    int mask1, mask2;
    int[] matr1, matr2;

    if (col1 == col2)
    {          // Cannot exchange the same column.
      return;
    }          // Exchange columns col1 and col2 of V1:V2
    mask1 = 0x80000000 >>> (col1 & 31);
    mask2 = 0x80000000 >>> (col2 & 31);
    matr1 = (col1 >= 32 ? V1 : V2);
    matr2 = (col2 >= 32 ? V1 : V2);
    for (row = V.length - 1; row >= 0; row--)
    {             // If both bits are different toggle them.
      if (((matr1[row] & mask1) == 0) != ((matr2[row] & mask2) == 0))
      {           // If both bits are different toggle them.
        matr1[row] ^= mask1;
        matr2[row] ^= mask2;
      }
    }
               // Exchange columns col1 and col2 of XmY:V
    matr1 = (col1 >= 32 ? XmY : V);
    matr2 = (col2 >= 32 ? XmY : V);
    for (row = V.length - 1; row >= 0; row--)
    {             // If both bits are different toggle them.
      if (((matr1[row] & mask1) == 0) != ((matr2[row] & mask2) == 0))
      {
        matr1[row] ^= mask1;
        matr2[row] ^= mask2;
      }
    }
  }

  private void coladd(int[] XmY, int[] V, int[] V1, int[] V2,
                      int col1, int col2)
  {
    int row;
    int mask1, mask2;
    int[] matr1, matr2;

    if (col1 == col2)
    {
      return;
    }               // Add column col1 to column col2 of V1:V2
    mask1 = 0x80000000 >>> (col1 & 31);
    mask2 = 0x80000000 >>> (col2 & 31);
    matr1 = (col1 >= 32 ? V1 : V2);
    matr2 = (col2 >= 32 ? V1 : V2);
    for (row = V.length - 1; row >= 0; row--)
    {              // If bit to add is '1'...
      if ((matr1[row] & mask1) != 0)
      {            // Toggle bit in destination.
        matr2[row] ^= mask2;
      }
    }
                   // Add column col1 to column col2 of XmY:V
    matr1 = (col1 >= 32 ? XmY : V);
    matr2 = (col2 >= 32 ? XmY : V);
    for (row = V.length - 1; row >= 0; row--)
    {              // If bit to add is '1'...
      if ((matr1[row] & mask1) != 0)
      {            // Toggle bit in destination.
        matr2[row] ^= mask2;
      }
    }
  }

  private int[] BlockLanczos(int[][] matrixB, int matrixBlength)
  {
    int i, j, k;
    int oldDiagonalSSt, newDiagonalSSt;
    int index, indexC, mask;
    int[] matrixD = new int[32];
    int[] matrixE = new int[32];
    int[] matrixF = new int[32];
    int[] matrixWinv = new int[32];
    int[] matrixWinv1 = new int[32];
    int[] matrixWinv2 = new int[32];
    int[] matrixVtV0 = new int[32];
    int[] matrixVt1V0 = new int[32];
    int[] matrixVt2V0 = new int[32];
    int[] matrixVtAV = new int[32];
    int[] matrixVt1AV1 = new int[32];
    int[] matrixAV = new int[matrixBlength];
    int[] matrixCalcParenD = new int[32];
    int[] vectorIndex = new int[64];
    int[] matrixV = new int[matrixBlength];
    int[] matrixV1 = new int[matrixBlength];
    int[] matrixV2 = new int[matrixBlength];
    int[] matrixXmY = new int[matrixBlength];
                                     // Matrix that holds temporary data
    int[] matrixCalc3 = new int[matrixBlength];
    int[] matrixTemp;
    int[] matrixCalc1 = new int[32]; // Matrix that holds temporary data
    int[] matrixCalc2 = new int[32]; // Matrix that holds temporary data
    int[] matr;
    int rowMatrixV;
    int rowMatrixXmY;
    long seed;
    int Temp, Temp1;
    int stepNbr = 0;
    int currentOrder, currentMask;
    int row, col;
    int leftCol, rightCol;
    int minind, min, minanswer;
    int[] rowMatrixB;

    newDiagonalSSt = oldDiagonalSSt = -1;

    /* Initialize matrix X-Y and matrix V_0 with random data */
    seed = 123456789L;
    for (i = matrixXmY.length - 1; i >= 0; i--)
    {
      matrixXmY[i] = (int) seed;
      seed = (seed * 62089911L + 54325442L) % DosALa31_1;
      matrixXmY[i] += (int) (seed * 6543265L);
      seed = (seed * 62089911L + 54325442L) % DosALa31_1;
      matrixV[i] = (int) seed;
      seed = (seed * 62089911L + 54325442L) % DosALa31_1;
      matrixV[i] += (int) (seed * 6543265L);
      seed = (seed * 62089911L + 54325442L) % DosALa31_1;
    }
    // Compute matrix Vt(0) * V(0)
    MatrTranspMult(matrixV, matrixV, matrixVtV0);
    for (;;)
    {
      if (ecmApplet.TerminateThread)
      {
        throw new ArithmeticException();
      }
      oldDiagonalSSt = newDiagonalSSt;
      stepNbr++;
      // Compute matrix A * V(i)
      MultiplyAByMatrix(matrixB, matrixV, matrixCalc3, matrixAV, matrixBlength);
      // Compute matrix Vt(i) * A * V(i)
      MatrTranspMult(matrixV, matrixAV, matrixVtAV);

      /* If Vt(i) * A * V(i) = 0, end of loop */
      for (i = matrixVtAV.length - 1; i >= 0; i--)
      {
        if (matrixVtAV[i] != 0)
        {
          break;
        }
      }
      if (i < 0)
      {
        break;
      } /* End X-Y calculation loop */

      /* Selection of S(i) and W(i) */

      matrixTemp = matrixWinv2;
      matrixWinv2 = matrixWinv1;
      matrixWinv1 = matrixWinv;
      matrixWinv = matrixTemp;

      mask = 1;
      for (j = 31; j >= 0; j--)
      {
        matrixD[j] = matrixVtAV[j]; /*  D = VtAV    */
        matrixWinv[j] = mask; /*  Winv = I    */
        mask *= 2;
      }

      index = 31;
      indexC = 31;
      for (mask = 1; mask != 0; mask *= 2)
      {
        if ((oldDiagonalSSt & mask) != 0)
        {
          matrixE[index] = indexC;
          matrixF[index] = mask;
          index--;
        }
        indexC--;
      }
      indexC = 31;
      for (mask = 1; mask != 0; mask *= 2)
      {
        if ((oldDiagonalSSt & mask) == 0)
        {
          matrixE[index] = indexC;
          matrixF[index] = mask;
          index--;
        }
        indexC--;
      }
      newDiagonalSSt = 0;
      for (j = 0; j < 32; j++)
      {
        currentOrder = matrixE[j];
        currentMask = matrixF[j];
        for (k = j; k < 32; k++)
        {
          if ((matrixD[matrixE[k]] & currentMask) != 0)
          {
            break;
          }
        }
        if (k < 32)
        {
          i = matrixE[k];
          Temp = matrixWinv[i];
          matrixWinv[i] = matrixWinv[currentOrder];
          matrixWinv[currentOrder] = Temp;
          Temp1 = matrixD[i];
          matrixD[i] = matrixD[currentOrder];
          matrixD[currentOrder] = Temp1;
          newDiagonalSSt |= currentMask;
          for (k = 31; k >= 0; k--)
          {
            if (k != currentOrder && ((matrixD[k] & currentMask) != 0))
            {
              matrixWinv[k] ^= Temp;
              matrixD[k] ^= Temp1;
            }
          } /* end for k */
        }
        else
        {
          for (k = j; k < 32; k++)
          {
            if ((matrixWinv[matrixE[k]] & currentMask) != 0)
            {
              break;
            }
          }
          i = matrixE[k];
          Temp = matrixWinv[i];
          matrixWinv[i] = matrixWinv[currentOrder];
          matrixWinv[currentOrder] = Temp;
          Temp1 = matrixD[i];
          matrixD[i] = matrixD[currentOrder];
          matrixD[currentOrder] = Temp1;
          for (k = 31; k >= 0; k--)
          {
            if ((matrixWinv[k] & currentMask) != 0)
            {
              matrixWinv[k] ^= Temp;
              matrixD[k] ^= Temp1;
            }
          } /* end for k */
        } /* end if */
      } /* end for j */
      /* Compute D(i), E(i) and F(i) */
      if (stepNbr >= 3)
      {
        // F = -Winv(i-2) * (I - Vt(i-1)*A*V(i-1)*Winv(i-1)) * ParenD * S*St
        MatrixMultiplication(matrixVt1AV1, matrixWinv1, matrixCalc2);
        index = 31; /* Add identity matrix */
        for (mask = 1; mask != 0; mask *= 2)
        {
          matrixCalc2[index] ^= mask;
          index--;
        }
        MatrixMultiplication(matrixWinv2, matrixCalc2, matrixCalc1);
        MatrixMultiplication(matrixCalc1, matrixCalcParenD, matrixF);
        MatrMultBySSt(matrixF, newDiagonalSSt, matrixF);
      }
      // E = -Winv(i-1) * Vt(i)*A*V(i) * S*St
      if (stepNbr >= 2)
      {
        MatrixMultiplication(matrixWinv1, matrixVtAV, matrixE);
        MatrMultBySSt(matrixE, newDiagonalSSt, matrixE);
      }
      // ParenD = Vt(i)*A*A*V(i) * S*St + Vt(i)*A*V(i)
      // D = I - Winv(i) * ParenD
      MatrTranspMult(matrixAV, matrixAV, matrixCalc1); // Vt(i)*A*A*V(i)
      MatrMultBySSt(matrixCalc1, newDiagonalSSt, matrixCalc1);
      MatrixAddition(matrixCalc1, matrixVtAV, matrixCalcParenD);
      MatrixMultiplication(matrixWinv, matrixCalcParenD, matrixD);
      index = 31; /* Add identity matrix */
      for (mask = 1; mask != 0; mask *= 2)
      {
        matrixD[index] ^= mask;
        index--;
      }

      /* Update value of X - Y */
      MatrixMultiplication(matrixWinv, matrixVtV0, matrixCalc1);
      MatrixMultAdd(matrixV, matrixCalc1, matrixXmY);

      /* Compute value of new matrix V(i) */
      // V(i+1) = A * V(i) * S * St + V(i) * D + V(i-1) * E + V(i-2) * F
      MatrMultBySSt(matrixAV, newDiagonalSSt, matrixCalc3);
      MatrixMultAdd(matrixV, matrixD, matrixCalc3);
      if (stepNbr >= 2)
      {
        MatrixMultAdd(matrixV1, matrixE, matrixCalc3);
        if (stepNbr >= 3)
        {
          MatrixMultAdd(matrixV2, matrixF, matrixCalc3);
        }
      }
      /* Compute value of new matrix Vt(i)V0 */
        // Vt(i+1)V(0) = Dt * Vt(i)V(0) + Et * Vt(i-1)V(0) + Ft * Vt(i-2)V(0)
      MatrTranspMult(matrixD, matrixVtV0, matrixCalc2);
      if (stepNbr >= 2)
      {
        MatrTranspMult(matrixE, matrixVt1V0, matrixCalc1);
        MatrixAddition(matrixCalc1, matrixCalc2, matrixCalc2);
        if (stepNbr >= 3)
        {
          MatrTranspMult(matrixF, matrixVt2V0, matrixCalc1);
          MatrixAddition(matrixCalc1, matrixCalc2, matrixCalc2);
        }
      }
      matrixTemp = matrixV2;
      matrixV2 = matrixV1;
      matrixV1 = matrixV;
      matrixV = matrixCalc3;
      matrixCalc3 = matrixTemp;
      matrixTemp = matrixVt2V0;
      matrixVt2V0 = matrixVt1V0;
      matrixVt1V0 = matrixVtV0;
      matrixVtV0 = matrixCalc2;
      matrixCalc2 = matrixTemp;
      matrixTemp = matrixVt1AV1;
      matrixVt1AV1 = matrixVtAV;
      matrixVtAV = matrixTemp;
    } /* end while */

    /* Find matrix V1:V2 = B * (X-Y:V) */
    for (row = matrixBlength - 1; row >= 0; row--)
    {
      matrixV1[row] = matrixV2[row] = 0;
    }
    for (row = matrixBlength - 1; row >= 0; row--)
    {
      rowMatrixB = matrixB[row];
      rowMatrixXmY = matrixXmY[row];
      rowMatrixV = matrixV[row];
       // The vector rowMatrixB includes the indexes of the columns set to '1'.
      for (index = rowMatrixB.length - 1; index >= 0; index--)
      {
        col = rowMatrixB[index];
        matrixV1[col] ^= rowMatrixXmY;
        matrixV2[col] ^= rowMatrixV;
      }
    }
    rightCol = 64;
    leftCol = 0;
    while (leftCol < rightCol)
    {
      for (col = leftCol; col < rightCol; col++)
      {       // For each column find the first row which has a '1'.
              // Columns outside this range must have '0' in all rows.
        matr = (col >= 32 ? matrixV1 : matrixV2);
        mask = 0x80000000 >>> (col & 31);
        vectorIndex[col] = -1;    // indicate all rows in zero in advance.
        for (row = 0; row < matr.length; row++)
        {
          if ((matr[row] & mask) != 0)
          {               // First row for this mask is found. Store it.
            vectorIndex[col] = row;
            break;
          }
        }
      }
      for (col = leftCol; col < rightCol; col++)
      {
        if (vectorIndex[col] < 0)
        {  // If all zeros in col 'col', exchange it with first column with
           // data different from zero (leftCol).
          colexchange(matrixXmY, matrixV, matrixV1, matrixV2, leftCol, col);
          vectorIndex[col] = vectorIndex[leftCol];
          vectorIndex[leftCol] = -1;  // This column now has zeros.
          leftCol++;                  // Update leftCol to exclude that column.
        }
      }
      if (leftCol == rightCol)
      {
        break;
      }
        // At this moment all columns from leftCol to rightCol are non-zero.
        // Get the first row that includes a '1'.
      min = vectorIndex[leftCol];
      minind = leftCol;
      for (col = leftCol+1; col < rightCol; col++)
      {
        if (vectorIndex[col] < min)
        {
          min = vectorIndex[col];
          minind = col;
        }
      }
      minanswer = 0;
      for (col = leftCol; col < rightCol; col++)
      {
        if (vectorIndex[col] == min)
        {
          minanswer++;
        }
      }
      if (minanswer > 1)
      {            // Two columns with the same first row to '1'.
        for (col = minind + 1; col < rightCol; col++)
        {
          if (vectorIndex[col] == min)
          {        // Add first column which has '1' in the same row to
                   // the other columns so they have '0' in this row after
                   // this operation.
            coladd(matrixXmY, matrixV, matrixV1, matrixV2, minind, col);
          }
        }
      }
      else
      {
        rightCol--;
        colexchange(matrixXmY, matrixV, matrixV1, matrixV2, minind, rightCol);
      }
    }
    leftCol = 0; /* find linear independent solutions */
    while (leftCol < rightCol)
    {
      for (col = leftCol; col < rightCol; col++)
      {         // For each column find the first row which has a '1'.
        matr = (col >= 32 ? matrixXmY : matrixV);
        mask = 0x80000000 >>> (col & 31);
        vectorIndex[col] = -1;    // indicate all rows in zero in advance.
        for (row = 0; row < matrixV1.length; row++)
        {
          if ((matr[row] & mask) != 0)
          {         // First row for this mask is found. Store it.
            vectorIndex[col] = row;
            break;
          }
        }
      }
      for (col = leftCol; col < rightCol; col++)
      {  // If all zeros in col 'col', exchange it with last column with
         // data different from zero (rightCol).
        if (vectorIndex[col] < 0)
        {
          rightCol--;                 // Update rightCol to exclude that column.
          colexchange(matrixXmY, matrixV, matrixV1, matrixV2, rightCol, col);
          vectorIndex[col] = vectorIndex[rightCol];
          vectorIndex[rightCol] = -1; // This column now has zeros.
        }
      }
      if (leftCol == rightCol)
      {
        break;
      }
        // At this moment all columns from leftCol to rightCol are non-zero.
        // Get the first row that includes a '1'.
      min = vectorIndex[leftCol];
      minind = leftCol;
      for (col = leftCol + 1; col < rightCol; col++)
      {
        if (vectorIndex[col] < min)
        {
          min = vectorIndex[col];
          minind = col;
        }
      }
      minanswer = 0;
      for (col = leftCol; col < rightCol; col++)
      {
        if (vectorIndex[col] == min)
        {
          minanswer++;
        }
      }
      if (minanswer > 1)
      {            // At least two columns with the same first row to '1'.
        for (col = minind + 1; col < rightCol; col++)
        {
          if (vectorIndex[col] == min)
          {        // Add first column which has '1' in the same row to
                   // the other columns so they have '0' in this row after
                   // this operation.
            coladd(matrixXmY, matrixV, matrixV1, matrixV2, minind, col);
          }
        }
      }
      else
      {
        colexchange(matrixXmY, matrixV, matrixV1, matrixV2, minind, leftCol);
        leftCol++;
      }
    }
    return matrixV;
  }

  /****************/
  /* Sieve thread */
  /****************/
  public void run()
  {
    int polySet;
    long biT[] = new long[20];
    long biU[] = new long[20];
    long biV[] = new long[20];
    long biR[] = new long[20];
    int multiplier = this.multiplier;
    PrimeSieveData primeSieveData[];
    PrimeSieveData rowPrimeSieveData;
    PrimeSieveData rowPrimeSieveData0;
    PrimeTrialDivisionData rowPrimeTrialDivisionData;
    byte [] SieveArray = new byte[2 * SieveLimit + 5000];
    int rowPartials[] = new int[200];
    long biLinearCoeff[] = new long[20];
    int threadNumber = this.threadNumber;
    long biDividend[] = new long[20];
    long biAbsLinearCoeff[];
    int indexFactorsA[] = new int[50];
    int rowSquares[] = new int[200];
    int polynomialsPerThread = (NbrPolynomials-1)/numberThreads;
    int firstPolynomial = threadNumber*polynomialsPerThread;
    int lastPolynomial = firstPolynomial + polynomialsPerThread;
    int grayCode = firstPolynomial ^ (firstPolynomial >> 1);
    firstPolynomial++;
    int i, PolynomialIndex, index, index2;
    int currentPrime;
    long Rem, RemB, remE, D, Q, Dividend[];
    int rowMatrixBbeforeMerge[] = new int[200];
    int rowMatrixB[] = new int[200];
    boolean positive;
    BigInteger result;
    int inverseA, twiceInverseA;
    int NumberLengthA, NumberLengthB;
    int NumberLength = this.NumberLength+1;

    synchronized(amodq)
    {
      if (threadNumber == 0)
      {
        primeSieveData = this.primeSieveData;
      }
      else
      {
        primeSieveData = new PrimeSieveData[nbrPrimes + 3];
        for (i=0; i<nbrPrimes; i++)
        {
          primeSieveData[i] = rowPrimeSieveData = new PrimeSieveData();
          rowPrimeSieveData0 = this.primeSieveData[i];
          rowPrimeSieveData.value = rowPrimeSieveData0.value;
          rowPrimeSieveData.modsqrt = rowPrimeSieveData0.modsqrt;
          rowPrimeSieveData.Bainv2 = rowPrimeSieveData0.Bainv2;
        }  
      }
      threadArray[threadNumber] = Thread.currentThread();
      amodq.notifyAll();
    }               // End synchronized block.
    try
    {
nextpoly:
      for (polySet=1;;polySet++)
      {                         // For each polynomial set...
        if (ecmApplet.TerminateThread)
        {
          throw new ArithmeticException();
        }
        synchronized(amodq)
        {
          nbrThreadFinishedPolySet++;
          if (congruencesFound >= matrixB.length || factorSiqs != null)
          {
            if (nbrThreadFinishedPolySet < polySet * numberThreads)
            {
              return;
            }
            if (factorSiqs == null)
            {
              while (!LinearAlgebraPhase(nbrPrimes,
                     matrixB,
                     primeTrialDivisionData,
                     vectExpParity,
                     vectLeftHandSide, biT, biR, biU, NumberLength));
              result = ecm.BigIntToBigNbr(biT, NumberLength);  // Factor found.
              synchronized(matrixB)
              {
                factorSiqs = result;
                matrixB.notify();
              }
            }
            else
            {
              synchronized(matrixB)
              {
                matrixB.notify();
              }
            }
            return;
          }
          if (nbrThreadFinishedPolySet == polySet * numberThreads)
          {
            /*********************************************/
            /* Initialization stage for first polynomial */
            /*********************************************/
            firstPrimeSieveData = primeSieveData;
            oldSeed = newSeed;
            newSeed = getFactorsOfA(oldSeed, nbrFactorsA, aindex,
                                    indexMinFactorA);
            for (index=0; index<nbrFactorsA; index++)
            {                        // Get the values of the factors of A.
              afact[index] = primeSieveData[aindex[index]].value;
            }
             // Compute the leading coefficient in biQuadrCoeff.
    
            ecm.LongToBigNbr(afact[0], biQuadrCoeff, NumberLength);
            for (index = 1; index < nbrFactorsA; index++)
            {
              ecm.MultBigNbrByLong(biQuadrCoeff, afact[index], biQuadrCoeff,
                                   NumberLength);
            }
            for (NumberLengthA=NumberLength; NumberLengthA>=2; NumberLengthA--)
            {
              if (biQuadrCoeff[NumberLengthA-1] != 0 ||
                  biQuadrCoeff[NumberLengthA-2] >= 0x40000000L)
              {
                break;
              }
            }
            for (index = 0; index < nbrFactorsA; index++)
            {
              currentPrime = (int)afact[index];
              D = ecm.RemDivBigNbrByLong(biQuadrCoeff,
                       currentPrime*currentPrime, NumberLengthA)/currentPrime;
              Q = (long)primeSieveData[aindex[index]].modsqrt *
                  modInv((amodq[index] = (int)D), currentPrime) % currentPrime;
              if (Q > currentPrime / 2)
              {
                Q = currentPrime - Q;
              }
              ecm.DivBigNbrByLong(biQuadrCoeff, currentPrime, biDividend,
                                  NumberLengthA);
              ecm.MultBigNbrByLong(biDividend, Q, biLinearDelta[index],
                                   NumberLengthA);
              for (index2 = NumberLengthA; index2 < NumberLength; index2++)
              {
                biLinearDelta[index][index2] = 0;
              }
            }
            for (index = 1; index < nbrPrimes; index++)
            {
              rowPrimeTrialDivisionData = primeTrialDivisionData[index];
              rowPrimeSieveData = primeSieveData[index];
                                                // Get current prime.
              currentPrime = rowPrimeTrialDivisionData.value; 
              Dividend = biQuadrCoeff;          // Get A mod current prime.
              switch (NumberLengthA)
              {
                case 7 :
                  Rem = Dividend[6]*rowPrimeTrialDivisionData.exp6 +
                        Dividend[5]*rowPrimeTrialDivisionData.exp5 + 
                        Dividend[4]*rowPrimeTrialDivisionData.exp4 + 
                        Dividend[3]*rowPrimeTrialDivisionData.exp3 + 
                        Dividend[2]*rowPrimeTrialDivisionData.exp2;
                  break;
                case 6 :
                  Rem = Dividend[5]*rowPrimeTrialDivisionData.exp5 + 
                        Dividend[4]*rowPrimeTrialDivisionData.exp4 + 
                        Dividend[3]*rowPrimeTrialDivisionData.exp3 + 
                        Dividend[2]*rowPrimeTrialDivisionData.exp2;
                  break;
                case 5 :
                  Rem = Dividend[4]*rowPrimeTrialDivisionData.exp4 + 
                        Dividend[3]*rowPrimeTrialDivisionData.exp3 + 
                        Dividend[2]*rowPrimeTrialDivisionData.exp2;
                  break;
                case 4 :
                  Rem = Dividend[3]*rowPrimeTrialDivisionData.exp3 + 
                        Dividend[2]*rowPrimeTrialDivisionData.exp2;
                  break;
                default :
                  Rem = Dividend[2]*rowPrimeTrialDivisionData.exp2;
                  break;
              }
                                                       // Get its inverse
              inverseA = modInv((int)((Rem+(Dividend[1] << 31) + Dividend[0]) %
                                currentPrime), currentPrime);
              twiceInverseA = inverseA << 1;       // and twice this value.
              rowPrimeSieveData.difsoln = (int) ((long)twiceInverseA *
                       rowPrimeSieveData.modsqrt % currentPrime);
              switch (NumberLengthA)
              {
                case 7:
                  for (index2 = nbrFactorsA-1; index2 > 0; index2--)
                  {
                    Dividend = biLinearDelta[index2];
                    remE = (Dividend[6]*rowPrimeTrialDivisionData.exp6 +
                            Dividend[5]*rowPrimeTrialDivisionData.exp5 + 
                            Dividend[4]*rowPrimeTrialDivisionData.exp4 + 
                            Dividend[3]*rowPrimeTrialDivisionData.exp3 + 
                            Dividend[2]*rowPrimeTrialDivisionData.exp2 +
                            (Dividend[1] << 31) + Dividend[0]) %
                            currentPrime;
                    rowPrimeSieveData.Bainv2[index2-1] =
                            (int) (remE * twiceInverseA % currentPrime);
                  }
                  Dividend = biLinearDelta[0];
                  remE = (Dividend[6]*rowPrimeTrialDivisionData.exp6 +
                          Dividend[5]*rowPrimeTrialDivisionData.exp5 + 
                          Dividend[4]*rowPrimeTrialDivisionData.exp4 + 
                          Dividend[3]*rowPrimeTrialDivisionData.exp3 + 
                          Dividend[2]*rowPrimeTrialDivisionData.exp2 +
                          (Dividend[1] << 31) + Dividend[0]) %
                          currentPrime;
                  break;
                case 6 :
                  for (index2 = nbrFactorsA-1; index2 > 0; index2--)
                  {
                    Dividend = biLinearDelta[index2];
                    remE = (Dividend[5]*rowPrimeTrialDivisionData.exp5 + 
                            Dividend[4]*rowPrimeTrialDivisionData.exp4 + 
                            Dividend[3]*rowPrimeTrialDivisionData.exp3 + 
                            Dividend[2]*rowPrimeTrialDivisionData.exp2 +
                            (Dividend[1] << 31) + Dividend[0]) %
                            currentPrime;
                    rowPrimeSieveData.Bainv2[index2-1] =
                            (int) (remE * twiceInverseA % currentPrime);
                  }
                  Dividend = biLinearDelta[0];
                  remE = (Dividend[5]*rowPrimeTrialDivisionData.exp5 + 
                          Dividend[4]*rowPrimeTrialDivisionData.exp4 + 
                          Dividend[3]*rowPrimeTrialDivisionData.exp3 + 
                          Dividend[2]*rowPrimeTrialDivisionData.exp2 +
                          (Dividend[1] << 31) + Dividend[0]) %
                          currentPrime;
                  break;
                case 5 :
                  for (index2 = nbrFactorsA-1; index2 > 0; index2--)
                  {
                    Dividend = biLinearDelta[index2];
                    remE = (Dividend[4]*rowPrimeTrialDivisionData.exp4 + 
                            Dividend[3]*rowPrimeTrialDivisionData.exp3 + 
                            Dividend[2]*rowPrimeTrialDivisionData.exp2 +
                            (Dividend[1] << 31) + Dividend[0]) %
                            currentPrime;
                    rowPrimeSieveData.Bainv2[index2-1] =
                            (int) (remE * twiceInverseA % currentPrime);
                  }
                  Dividend = biLinearDelta[0];
                  remE = (Dividend[4]*rowPrimeTrialDivisionData.exp4 + 
                          Dividend[3]*rowPrimeTrialDivisionData.exp3 + 
                          Dividend[2]*rowPrimeTrialDivisionData.exp2 +
                          (Dividend[1] << 31) + Dividend[0]) %
                          currentPrime;
                  break;
                case 4 :
                  for (index2 = nbrFactorsA-1; index2 > 0; index2--)
                  {
                    Dividend = biLinearDelta[index2];
                    remE = (Dividend[3]*rowPrimeTrialDivisionData.exp3 + 
                            Dividend[2]*rowPrimeTrialDivisionData.exp2 +
                            (Dividend[1] << 31) + Dividend[0]) %
                            currentPrime;
                    rowPrimeSieveData.Bainv2[index2-1] =
                            (int) (remE * twiceInverseA % currentPrime);
                  }
                  Dividend = biLinearDelta[0];
                  remE = (Dividend[3]*rowPrimeTrialDivisionData.exp3 + 
                          Dividend[2]*rowPrimeTrialDivisionData.exp2 +
                          (Dividend[1] << 31) + Dividend[0]) %
                          currentPrime;
                  break;
                default :
                  for (index2 = nbrFactorsA-1; index2 > 0; index2--)
                  {
                    Dividend = biLinearDelta[index2];
                    remE = (Dividend[2]*rowPrimeTrialDivisionData.exp2 +
                            (Dividend[1] << 31) + Dividend[0]) %
                            currentPrime;
                    rowPrimeSieveData.Bainv2[index2-1] =
                            (int) (remE * twiceInverseA % currentPrime);
                  }
                  Dividend = biLinearDelta[0];
                  remE = (Dividend[2]*rowPrimeTrialDivisionData.exp2 +
                          (Dividend[1] << 31) + Dividend[0]) %
                          currentPrime;
                  break;
              }
              rowPrimeSieveData.Bainv2_0 =
                       (int) (remE * twiceInverseA % currentPrime);
            }
            synchronized(TestNbr2)
            {
              TestNbr2.notifyAll();
            }
          }           // End initializing first polynomial
        }             // End synchronized
        synchronized(TestNbr2)
        {
          while(nbrThreadFinishedPolySet < polySet * numberThreads)
          {
            try
            {
              TestNbr2.wait();
            }
            catch(InterruptedException ie) {}
          }
        }
        if (factorSiqs != null || congruencesFound >= matrixB.length)
        {
          if (nbrThreadFinishedPolySet > numberThreads*polySet)
          {
            continue nextpoly;
          }
          synchronized(amodq)
          {
            nbrThreadFinishedPolySet++;
          }
          return;
        }
        PolynomialIndex = firstPolynomial;
                                // Compute first polynomial parameters.
        for (i=0; i<NumberLength; i++)
        {
          biLinearCoeff[i] = 0;
        }
        for (i=0; i<nbrFactorsA; i++)
        {
          if ((grayCode & (1<<i)) == 0)
          {
            ecm.AddBigNbr(biLinearCoeff, biLinearDelta[i], biLinearCoeff,
                          NumberLength);
          }
          else
          {
            ecm.SubtractBigNbr(biLinearCoeff, biLinearDelta[i], biLinearCoeff,
                               NumberLength);
          }
        }
        for (NumberLengthA = NumberLength; NumberLengthA>=2; NumberLengthA--)
        {
          if (biQuadrCoeff[NumberLengthA-1] != 0 ||
              biQuadrCoeff[NumberLengthA-2] >= 0x40000000)
          {
            break;
          }
        }
        if (biLinearCoeff[NumberLength-1] >= 0x40000000)
        {                               // Number is negative.
          positive = false;
          System.arraycopy(biLinearCoeff, 0,     // Source
                           biT, 0,               // Destination
                           NumberLength);        // Number of elements.
          ecm.ChSignBigNbr(biT, NumberLength);   // Make it positive.
          biAbsLinearCoeff = biT;
        }
        else
        {
          positive = true;                       // B is positive.
          biAbsLinearCoeff = biLinearCoeff;      // Get B mod current prime.
        }
        for (NumberLengthB = NumberLength; NumberLengthB>=2; NumberLengthB--)
        {
          if (biAbsLinearCoeff[NumberLengthB-1] != 0 ||
              biAbsLinearCoeff[NumberLengthB-2] >= 0x40000000)
          {
            break;
          }
        }
        for (i=nbrPrimes-1; i>0; i--)
        { 
          rowPrimeSieveData = primeSieveData[i];
          rowPrimeSieveData0 = firstPrimeSieveData[i];
          rowPrimeSieveData.difsoln = rowPrimeSieveData0.difsoln;
          rowPrimeSieveData.Bainv2_0 = rowPrimeSieveData0.Bainv2_0;
          rowPrimeTrialDivisionData = primeTrialDivisionData[i];
          currentPrime = rowPrimeTrialDivisionData.value; // Get current prime.
          Dividend = biQuadrCoeff;                  // Get A mod current prime.
          switch (NumberLengthA)
          {
            case 7 :
              Rem = Dividend[6]*rowPrimeTrialDivisionData.exp6 +
                    Dividend[5]*rowPrimeTrialDivisionData.exp5 + 
                    Dividend[4]*rowPrimeTrialDivisionData.exp4 + 
                    Dividend[3]*rowPrimeTrialDivisionData.exp3 + 
                    Dividend[2]*rowPrimeTrialDivisionData.exp2;
              break;
            case 6 :
              Rem = Dividend[5]*rowPrimeTrialDivisionData.exp5 + 
                    Dividend[4]*rowPrimeTrialDivisionData.exp4 + 
                    Dividend[3]*rowPrimeTrialDivisionData.exp3 + 
                    Dividend[2]*rowPrimeTrialDivisionData.exp2;
              break;
            case 5 :
              Rem = Dividend[4]*rowPrimeTrialDivisionData.exp4 + 
                    Dividend[3]*rowPrimeTrialDivisionData.exp3 + 
                    Dividend[2]*rowPrimeTrialDivisionData.exp2;
              break;
            case 4 :
              Rem = Dividend[3]*rowPrimeTrialDivisionData.exp3 + 
                    Dividend[2]*rowPrimeTrialDivisionData.exp2;
              break;
            case 3 :
              Rem = Dividend[2]*rowPrimeTrialDivisionData.exp2;
              break;
            default :
              Rem = 0;
              break;
          }
                                                   // Get its inverse
          inverseA = modInv((int)((Rem+(Dividend[1] << 31) + Dividend[0]) %
                                currentPrime), currentPrime);
          switch (NumberLengthB)
          {
            case 7 :
              Rem = biAbsLinearCoeff[6]*rowPrimeTrialDivisionData.exp6 +
                    biAbsLinearCoeff[5]*rowPrimeTrialDivisionData.exp5 + 
                    biAbsLinearCoeff[4]*rowPrimeTrialDivisionData.exp4 + 
                    biAbsLinearCoeff[3]*rowPrimeTrialDivisionData.exp3 + 
                    biAbsLinearCoeff[2]*rowPrimeTrialDivisionData.exp2;
              break;
            case 6 :
              Rem = biAbsLinearCoeff[5]*rowPrimeTrialDivisionData.exp5 + 
                    biAbsLinearCoeff[4]*rowPrimeTrialDivisionData.exp4 + 
                    biAbsLinearCoeff[3]*rowPrimeTrialDivisionData.exp3 + 
                    biAbsLinearCoeff[2]*rowPrimeTrialDivisionData.exp2;
              break;
            case 5 :
              Rem = biAbsLinearCoeff[4]*rowPrimeTrialDivisionData.exp4 + 
                    biAbsLinearCoeff[3]*rowPrimeTrialDivisionData.exp3 + 
                    biAbsLinearCoeff[2]*rowPrimeTrialDivisionData.exp2;
              break;
            case 4 :
              Rem = biAbsLinearCoeff[3]*rowPrimeTrialDivisionData.exp3 + 
                    biAbsLinearCoeff[2]*rowPrimeTrialDivisionData.exp2;
              break;
            case 3 :
              Rem = biAbsLinearCoeff[2]*rowPrimeTrialDivisionData.exp2;
              break; 
            default :
              Rem = 0;
              break;
          }
          RemB = (Rem+(biAbsLinearCoeff[1] << 31) + biAbsLinearCoeff[0]) %
                 currentPrime;
          if (positive)
          {
            RemB = currentPrime-RemB;
          }
          rowPrimeSieveData.soln1 = (int)((SieveLimit+(long)inverseA *
                  (rowPrimeSieveData0.modsqrt+RemB))%currentPrime);
        }
        do
        {                       // For each polynomial...
          if (congruencesFound >= matrixB.length || factorSiqs != null)
          {
            if (nbrThreadFinishedPolySet > numberThreads*polySet)
            {
              continue nextpoly;
            }
            synchronized(amodq)
            {
              nbrThreadFinishedPolySet++;
            }
            return;             // Another thread finished factoring.
          }
          if (ecmApplet.onlyFactoring)
          {
            polynomialsSieved++;
          }
          /***************/
          /* Sieve stage */
          /***************/
          if ((PolynomialIndex & 1) == 0)
          {
            PerformSiqsSieveStage(primeSieveData, SieveArray, nbrPrimes,
                                nbrPrimes2,
                                firstLimit, secondLimit, thirdLimit,
                                smallPrimeUpperLimit, threshold, multiplier,
                                SieveLimit, amodq, PolynomialIndex,
                                aindex, nbrFactorsA, log2, biLinearCoeff,
                                NumberLength);
          }
          else
          {
            PerformSiqsSieveStage_0(primeSieveData, SieveArray, nbrPrimes,
                                nbrPrimes2,
                                firstLimit, secondLimit, thirdLimit,
                                smallPrimeUpperLimit, threshold, multiplier,
                                SieveLimit, amodq, PolynomialIndex,
                                aindex, nbrFactorsA, log2, biLinearCoeff,
                                NumberLength);
          }
          ValuesSieved += 2*SieveLimit;
          /************************/
          /* Trial division stage */
          /************************/
          index2 = 2 * SieveLimit-1;
          do
          {
            if ((SieveArray[index2--] & SieveArray[index2--] &
                 SieveArray[index2--] & SieveArray[index2--] &
                 SieveArray[index2--] & SieveArray[index2--] &
                 SieveArray[index2--] & SieveArray[index2--] &
                 SieveArray[index2--] & SieveArray[index2--] &
                 SieveArray[index2--] & SieveArray[index2--] &
                 SieveArray[index2--] & SieveArray[index2--] &
                 SieveArray[index2--] & SieveArray[index2--]) >= 0)
            {
              for (i=16; i>0; i--)
              {
                if ((SieveArray[index2+i]) >= 0)
                {
                  if (congruencesFound >= matrixB.length)
                  {       // All congruences were found: stop sieving.
                    index2 = 0;
                    break;
                  }
                  SieveLocationHit(matrixB, rowMatrixB,
                                   rowMatrixBbeforeMerge,
                                   SieveLimit, vectExpParity, vectLeftHandSide,
                                   index2+i, primeSieveData,
                                   primeTrialDivisionData, startTime,
                                   aindex, nbrFactorsA, rowPartials,
                                   matrixPartial, matrixPartialHashIndex,
                                   afact, largePrimeUpperBound,
                                   indexMinFactorA, multiplier, rowSquares,
                                   biDividend, NumberLength, biT,
                                   biLinearCoeff, biR, biU, biV,
                                   indexFactorsA);
                  if (congruencesFound >= matrixB.length)
                  {               // All congruences were found: stop sieving.
                    index2 = 0;
                    break;
                  }
                }
              }
            }
          }
          while (index2 > 0);
          /*******************/
          /* Next polynomial */
          /*******************/
          PolynomialIndex++;
        }
        while (PolynomialIndex <= lastPolynomial &&
               congruencesFound < matrixB.length);
      }
    }
    catch (ArithmeticException ae)
    {
      synchronized(matrixB)
      {
        factorSiqs = null;
        matrixB.notify();
      }
    }
  }
}

class PrimeSieveData
{
  int value;
  int modsqrt;
  int Bainv2[];
  int Bainv2_0;
  int soln1;
  int difsoln;
}

class PrimeTrialDivisionData
{
  int value;
  int exp2;
  int exp3;
  int exp4;
  int exp5;
  int exp6;
}
// </XMP>

