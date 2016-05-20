/*
  $Id: PBEParameter.java 1818 2011-02-08 19:19:09Z dfisher $

  Copyright (C) 2007-2011 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 1818 $
  Updated: $Date: 2011-02-08 20:19:09 +0100 (Tue, 08 Feb 2011) $
*/
package edu.vt.middleware.crypt.pkcs;

import edu.vt.middleware.crypt.util.DERHelper;
import org.bouncycastle.asn1.DERSequence;

/**
 * Describes the PBEParameter type defined in PKCS#5. It can also be used to
 * model the pkcs-12PbeParams type defined in section B.4 of PKCS#12.
 *
 * @author  Middleware Services
 * @version  $Revision: 1818 $
 */
public class PBEParameter
{

  /** Digest salt value. */
  protected byte[] salt;

  /** Number of iterations of mixing function. */
  protected int iterationCount;


  /**
   * Creates a new PBE parameter with given values.
   *
   * @param  saltBytes  Bytes of digest salt.
   * @param  iterations  Number of iterations of mixing function.
   */
  public PBEParameter(final byte[] saltBytes, final int iterations)
  {
    if (iterations < 1) {
      throw new IllegalArgumentException("Iterations must be greater than 0.");
    }
    this.salt = saltBytes;
    this.iterationCount = iterations;
  }


  /**
   * Decodes a DER sequence of PBE parameters into an instance of this class.
   *
   * @param  params  PBE parameters as a DER sequence.
   *
   * @return  Equivalent instance of {@link PBEParameter}.
   */
  public static PBEParameter decode(final DERSequence params)
  {
    return
      new PBEParameter(
        DERHelper.asOctets(params.getObjectAt(0)),
        DERHelper.asInt(params.getObjectAt(1)));
  }


  /** @return  the salt */
  public byte[] getSalt()
  {
    return salt;
  }


  /** @return  the iterationCount */
  public int getIterationCount()
  {
    return iterationCount;
  }


}

