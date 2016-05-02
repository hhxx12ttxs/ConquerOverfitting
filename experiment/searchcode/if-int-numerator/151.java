<<<<<<< HEAD
/*
 * --------- BEGIN COPYRIGHT NOTICE ---------
 * Copyright 2002-2012 Extentech Inc.
 * Copyright 2013 Infoteria America Corp.
 * 
 * This file is part of OpenXLS.
 * 
 * OpenXLS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * OpenXLS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with OpenXLS.  If not, see
 * <http://www.gnu.org/licenses/>.
 * ---------- END COPYRIGHT NOTICE ----------
 */
package com.extentech.formats.XLS;

import com.extentech.toolkit.ByteTools;
import com.extentech.toolkit.Logger;


/** <b>Scl: Sheet Zoom (A0h)</b><br>

   Scl stores the zoom magnification for the sheet
   
   <p><pre>
    offset  name            size    contents
    ---
    4       num             2       = Numerator of the view magnification fraction (num)  
	6		denum			2		= Denumerator of the view magnification fraction (den)
    </p></pre>
*/

public final class Scl extends com.extentech.formats.XLS.XLSRecord 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -4595833226859365049L;
//	int num = 100; 20081231 KSC: default val is 1, making the calc (num/denum)*100
	int num= 1;
	int denum = 1;
    
    /** default constructor
    */
    Scl(){
        super();
        byte[] bs = new byte[4];
        bs[0] = 1;
        bs[1] = 0;
        bs[2] = 1;
        bs[3] = 0;
        setOpcode(SCL);
        setLength((short)4);
        if(DEBUGLEVEL > DEBUG_LOW)
        	Logger.logInfo("Scl.init()" + String.valueOf(this.offset));
        this.setData(bs);
        this.originalsize = 4;
    }
    
    /** sets the zoom as a percentage for this sheet
     * 
     * @param b
     */
    public void setZoom(float b){
        byte[] data = this.getData();

/* 20081231 KSC:  appears that zooming is such that 1/1=100%         
        // set our scale to 1000
        denum = 1000;
        byte[] denmbd= ByteTools.shortToLEBytes((short) denum);
        System.arraycopy(denmbd, 0, data, 2, 2);

        // take something like .2345 and come up with 24 & 100
        float nx = b * denum; // get denum
        // get the num
        num = (int)nx;

        if((denum % b)>0){
        	if(b>999) // only 2 precision places for zoom... out a warn
        		Logger.logWarn("Cannot set zoom to : " +b + " rounding to nearest valid zoom setting.");
        }
*/
        // 20081231 KSC: Convert double to fraction and set num/denum to results
        int[] n= gcd((int)(b*100), 100);
        num= n[0];
        denum= n[1];
        byte[] nmbd= ByteTools.shortToLEBytes((short) num);
        System.arraycopy(nmbd, 0, data, 0, 2);
        nmbd= ByteTools.shortToLEBytes((short) denum);
        System.arraycopy(nmbd, 0, data, 2, 2);
        
        this.setData(data);        
    }
    
    /** gets the zoom as a percentage for this sheet
     * 
     * @return
     */
    public float getZoom(){
        return ((float)num/(float)denum);	
    }
    
	public void init(){
        super.init();       
        num = (int) ByteTools.readShort(this.getByteAt(0),this.getByteAt(1));
        denum = (int) ByteTools.readShort(this.getByteAt(2),this.getByteAt(3));        
        if((DEBUGLEVEL > DEBUG_LOW))
        	Logger.logInfo("Scl.init() sheet zoom:" + getZoom());
    }
	
	
	private int[] gcd (int numerator, int denominator)
	{
		int highest;
		int n= 1;
		int d= 1;

		if (denominator>numerator)
			highest=denominator;
		else
			highest=numerator;

		for(int x = highest;x>0;x--)
		{
			if (denominator%x==0 && numerator%x==0)
			{
				n=numerator/x;
				d=denominator/x;
				break;
			}	
		}
		return new int[] { n, d};
	}
}
=======
/*
 * Copyright 2012 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.pdf417.decoder.ec;

import com.google.zxing.ChecksumException;

/**
 * <p>PDF417 error correction implementation.</p>
 *
 * <p>This <a href="http://en.wikipedia.org/wiki/Reed%E2%80%93Solomon_error_correction#Example">example</a>
 * is quite useful in understanding the algorithm.</p>
 *
 * @author Sean Owen
 * @see com.google.zxing.common.reedsolomon.ReedSolomonDecoder
 */
public final class ErrorCorrection {

  private final ModulusGF field;

  public ErrorCorrection() {
    this.field = ModulusGF.PDF417_GF;
  }

  /**
   * @param received received codewords
   * @param numECCodewords number of those codewords used for EC
   * @param erasures location of erasures
   * @return number of errors
   * @throws ChecksumException if errors cannot be corrected, maybe because of too many errors
   */
  public int decode(int[] received,
                    int numECCodewords,
                    int[] erasures) throws ChecksumException {

    ModulusPoly poly = new ModulusPoly(field, received);
    int[] S = new int[numECCodewords];
    boolean error = false;
    for (int i = numECCodewords; i > 0; i--) {
      int eval = poly.evaluateAt(field.exp(i));
      S[numECCodewords - i] = eval;
      if (eval != 0) {
        error = true;
      }
    }

    if (!error) {
      return 0;
    }

    ModulusPoly knownErrors = field.getOne();
    if (erasures != null) {
      for (int erasure : erasures) {
        int b = field.exp(received.length - 1 - erasure);
        // Add (1 - bx) term:
        ModulusPoly term = new ModulusPoly(field, new int[]{field.subtract(0, b), 1});
        knownErrors = knownErrors.multiply(term);
      }
    }

    ModulusPoly syndrome = new ModulusPoly(field, S);
    //syndrome = syndrome.multiply(knownErrors);

    ModulusPoly[] sigmaOmega =
        runEuclideanAlgorithm(field.buildMonomial(numECCodewords, 1), syndrome, numECCodewords);
    ModulusPoly sigma = sigmaOmega[0];
    ModulusPoly omega = sigmaOmega[1];

    //sigma = sigma.multiply(knownErrors);

    int[] errorLocations = findErrorLocations(sigma);
    int[] errorMagnitudes = findErrorMagnitudes(omega, sigma, errorLocations);

    for (int i = 0; i < errorLocations.length; i++) {
      int position = received.length - 1 - field.log(errorLocations[i]);
      if (position < 0) {
        throw ChecksumException.getChecksumInstance();
      }
      received[position] = field.subtract(received[position], errorMagnitudes[i]);
    }
    return errorLocations.length;
  }

  private ModulusPoly[] runEuclideanAlgorithm(ModulusPoly a, ModulusPoly b, int R)
      throws ChecksumException {
    // Assume a's degree is >= b's
    if (a.getDegree() < b.getDegree()) {
      ModulusPoly temp = a;
      a = b;
      b = temp;
    }

    ModulusPoly rLast = a;
    ModulusPoly r = b;
    ModulusPoly tLast = field.getZero();
    ModulusPoly t = field.getOne();

    // Run Euclidean algorithm until r's degree is less than R/2
    while (r.getDegree() >= R / 2) {
      ModulusPoly rLastLast = rLast;
      ModulusPoly tLastLast = tLast;
      rLast = r;
      tLast = t;

      // Divide rLastLast by rLast, with quotient in q and remainder in r
      if (rLast.isZero()) {
        // Oops, Euclidean algorithm already terminated?
        throw ChecksumException.getChecksumInstance();
      }
      r = rLastLast;
      ModulusPoly q = field.getZero();
      int denominatorLeadingTerm = rLast.getCoefficient(rLast.getDegree());
      int dltInverse = field.inverse(denominatorLeadingTerm);
      while (r.getDegree() >= rLast.getDegree() && !r.isZero()) {
        int degreeDiff = r.getDegree() - rLast.getDegree();
        int scale = field.multiply(r.getCoefficient(r.getDegree()), dltInverse);
        q = q.add(field.buildMonomial(degreeDiff, scale));
        r = r.subtract(rLast.multiplyByMonomial(degreeDiff, scale));
      }

      t = q.multiply(tLast).subtract(tLastLast).negative();
    }

    int sigmaTildeAtZero = t.getCoefficient(0);
    if (sigmaTildeAtZero == 0) {
      throw ChecksumException.getChecksumInstance();
    }

    int inverse = field.inverse(sigmaTildeAtZero);
    ModulusPoly sigma = t.multiply(inverse);
    ModulusPoly omega = r.multiply(inverse);
    return new ModulusPoly[]{sigma, omega};
  }

  private int[] findErrorLocations(ModulusPoly errorLocator) throws ChecksumException {
    // This is a direct application of Chien's search
    int numErrors = errorLocator.getDegree();
    int[] result = new int[numErrors];
    int e = 0;
    for (int i = 1; i < field.getSize() && e < numErrors; i++) {
      if (errorLocator.evaluateAt(i) == 0) {
        result[e] = field.inverse(i);
        e++;
      }
    }
    if (e != numErrors) {
      throw ChecksumException.getChecksumInstance();
    }
    return result;
  }

  private int[] findErrorMagnitudes(ModulusPoly errorEvaluator,
                                    ModulusPoly errorLocator,
                                    int[] errorLocations) {
    int errorLocatorDegree = errorLocator.getDegree();
    int[] formalDerivativeCoefficients = new int[errorLocatorDegree];
    for (int i = 1; i <= errorLocatorDegree; i++) {
      formalDerivativeCoefficients[errorLocatorDegree - i] =
          field.multiply(i, errorLocator.getCoefficient(i));
    }
    ModulusPoly formalDerivative = new ModulusPoly(field, formalDerivativeCoefficients);

    // This is directly applying Forney's Formula
    int s = errorLocations.length;
    int[] result = new int[s];
    for (int i = 0; i < s; i++) {
      int xiInverse = field.inverse(errorLocations[i]);
      int numerator = field.subtract(0, errorEvaluator.evaluateAt(xiInverse));
      int denominator = field.inverse(formalDerivative.evaluateAt(xiInverse));
      result[i] = field.multiply(numerator, denominator);
    }
    return result;
  }
}

>>>>>>> 76aa07461566a5976980e6696204781271955163
