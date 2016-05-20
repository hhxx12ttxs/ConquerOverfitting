/*
  $Id: AbstractPKCSKeyGenerator.java 1818 2011-02-08 19:19:09Z dfisher $

  Copyright (C) 2007-2011 Virginia Tech.
  All rights reserved.

  SEE LICENSE FOR MORE INFORMATION

  Author:  Middleware Services
  Email:   middleware@vt.edu
  Version: $Revision: 1818 $
  Updated: $Date: 2011-02-08 20:19:09 +0100 (Tue, 08 Feb 2011) $
*/
package edu.vt.middleware.crypt.pbe;

import org.bouncycastle.crypto.PBEParametersGenerator;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * Base class for all PKCS key generators that use a key derivation function
 * that performs iterative hashing operations on a salted password.
 *
 * <p>For a key derivation function <em>f</em>, the following formula applies:
 * <br>
 * <code>derivedKey = f(password, salt, iterations)</code></p>
 *
 * <p>Classes derived from this class are <em>NOT</em> thread safe.</p>
 *
 * @author  Middleware Services
 * @version  $Revision: 1818 $
 */
public abstract class AbstractPKCSKeyGenerator implements KeyGenerator
{

  /** Default number of iterations taken from examples in PKCS#5v2. */
  public static final int DEFAULT_ITERATION_COUNT = 1000;

  /** Key derifcation function salt. */
  protected byte[] salt;

  /** Key derifcation function iteration count. */
  protected int iterationCount = DEFAULT_ITERATION_COUNT;


  /** {@inheritDoc} */
  public byte[] generate(final char[] password, final int size)
  {
    if (size < 1) {
      throw new IllegalArgumentException("Size must be positive integer.");
    }

    final PBEParametersGenerator generator = newParamGenerator();
    generator.init(toBytes(password), salt, iterationCount);

    final KeyParameter p = (KeyParameter) generator.generateDerivedParameters(
      size);
    return p.getKey();
  }


  /**
   * Gets the key derivation function iteration count.
   *
   * @param  count  Iteration count. MUST be positive integer.
   */
  protected void setIterationCount(final int count)
  {
    if (count < 1) {
      throw new IllegalArgumentException("Count must be positive integer.");
    }
    this.iterationCount = count;
  }


  /**
   * Creates a new BC parameter generator instance.
   *
   * @return  New parameter generator.
   */
  protected abstract PBEParametersGenerator newParamGenerator();


  /**
   * Converts password characters to bytes in implementation-dependent fashion.
   *
   * @param  password  Password to convert.
   *
   * @return  Password bytes.
   */
  protected abstract byte[] toBytes(char[] password);
}

