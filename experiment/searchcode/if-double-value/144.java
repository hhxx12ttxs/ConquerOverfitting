/*
<<<<<<< HEAD
 * Copyright (C) 2011 Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package org.esa.beam.akh.adam.bbdr;

import java.io.IOException;
import junit.framework.TestCase;

/**
 * @author Olaf Danne
 * @version $Revision: $ $Date:  $
 */
public class MerisLutTest extends TestCase {

    public void testLutAot() throws IOException {
        AotLookupTable aotLut = N2ModisUtils.getAotLookupTable(Sensor.MERIS);
        assertNotNull(aotLut);
        LookupTable lut = aotLut.getLut();
        assertNotNull(lut);

        assertEquals(7, lut.getDimensionCount());

        final double[] parametersArray = lut.getDimension(6).getSequence();
        final int nParameters = parametersArray.length;
        assertEquals(5, nParameters);     //  Parameters
        assertEquals(1.0, parametersArray[0], 1.E-4);
        assertEquals(2.0, parametersArray[1], 1.E-4);
        assertEquals(3.0, parametersArray[2], 1.E-4);
        assertEquals(5.0, parametersArray[4], 1.E-4);

        final double[] vzaArray = lut.getDimension(5).getSequence();
        final int nVza = vzaArray.length;
        assertEquals(13, nVza);     //  VZA
        assertEquals(0.0, vzaArray[0], 1.E-4);
        assertEquals(12.76, vzaArray[3], 1.E-4);
        assertEquals(24.24, vzaArray[5], 1.E-4);
        assertEquals(35.68, vzaArray[7], 1.E-4);
        assertEquals(64.2799987, vzaArray[12], 1.E-4);

        final double[] szaArray = lut.getDimension(4).getSequence();
        final int nSza = szaArray.length;
        assertEquals(14, nSza);     //  SZA
        assertEquals(0.0, szaArray[0], 1.E-4);
        assertEquals(6.97, szaArray[2], 1.E-4);
        assertEquals(18.51, szaArray[4], 1.E-4);
        assertEquals(29.96, szaArray[6], 1.E-4);
        assertEquals(69.9899, szaArray[13], 1.E-4);

        final double[] aziArray = lut.getDimension(3).getSequence();
        final int nAzi = aziArray.length;
        assertEquals(19, nAzi);     //  AZI
        assertEquals(10.0, aziArray[1], 1.E-4);
        assertEquals(130.0, aziArray[13], 1.E-4);
        assertEquals(150.0, aziArray[15], 1.E-4);

        final double[] hsfArray = lut.getDimension(2).getSequence();
        final int nHsf = hsfArray.length;
        assertEquals(4, nHsf);     //  HSF
//        assertEquals(746.825, hsfArray[1], 1.E-3);
//        assertEquals(898.746, hsfArray[2], 1.E-3);
//        assertEquals(1013.25, hsfArray[3], 1.E-3);
        assertEquals(0.0, hsfArray[0], 1.E-3);
        assertEquals(1.0, hsfArray[1], 1.E-3);
        assertEquals(2.5, hsfArray[2], 1.E-3);
        assertEquals(8.0, hsfArray[3], 1.E-3);

        final double[] aotArray = lut.getDimension(1).getSequence();
        final int nAot = aotArray.length;
        assertEquals(9, nAot);     //  AOT
        assertEquals(0.1, aotArray[2], 1.E-3);
        assertEquals(0.2, aotArray[3], 1.E-3);
        assertEquals(1.5, aotArray[7], 1.E-3);

        final double[] wvlArray = lut.getDimension(0).getSequence();
        final int nWvl = wvlArray.length;
        assertEquals(15, nWvl);     //  AOT
        assertEquals(412.0, wvlArray[0], 1.E-3);
        assertEquals(442.0, wvlArray[1], 1.E-3);
        assertEquals(900.0, wvlArray[14], 1.E-3);

        // solar irradiances
        assertNotNull(aotLut.getSolarIrradiance());
        assertEquals(15, aotLut.getSolarIrradiance().length);
        assertEquals(1879.69f, aotLut.getSolarIrradiance()[1]);
        assertEquals(1803.06f, aotLut.getSolarIrradiance()[4]);
        assertEquals(958.059f, aotLut.getSolarIrradiance()[12]);

        // first values in LUT
        // iWvl=0, iAot0, iHsf0, iAzi=0, iSza=0, iVza=0, iParameters=0..4:
        double[] coord = new double[]{412.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        double value = lut.getValue(coord);
        assertEquals(0.03574, value, 1.E-4);

        coord = new double[]{412.0, 0.0, 0.0, 0.0, 0.0, 0.0, 2.0};
        value = lut.getValue(coord);
        assertEquals(0.74368, value, 1.E-4);

        coord = new double[]{412.0, 0.0, 0.0, 0.0, 0.0, 0.0, 3.0};
        value = lut.getValue(coord);
        assertEquals(0.21518, value, 1.E-4);

        coord = new double[]{412.0, 0.0, 0.0, 0.0, 0.0, 0.0, 4.0};
        value = lut.getValue(coord);
        assertEquals(0.84468, value, 1.E-4);

        coord = new double[]{412.0, 0.0, 0.0, 0.0, 0.0, 0.0, 5.0};
        value = lut.getValue(coord);
        assertEquals(0.84468, value, 1.E-4);

        // iWvl=0, iAot0, iHsf0, iAzi=0, iSza=0, iVza=1, iParameters=4:
        coord = new double[]{412.0, 0.0, 0.0, 0.0, 0.0, 2.56, 5.0};
        value = lut.getValue(coord);
        assertEquals(0.84454, value, 1.E-4);

        // values somewhere inside LUT:
        coord = new double[]{
                wvlArray[7], aotArray[2], hsfArray[1], aziArray[6], szaArray[9], vzaArray[11], parametersArray[3]
        };
        value = lut.getValue(coord);
        assertEquals(0.930663, value, 1.E-4);

        coord = new double[]{
                wvlArray[4], aotArray[1], hsfArray[2], aziArray[14], szaArray[5], vzaArray[3], parametersArray[2]
        };
        value = lut.getValue(coord);
        assertEquals(0.060401, value, 1.E-4);

        coord = new double[]{
                wvlArray[8], aotArray[0], hsfArray[1], aziArray[16], szaArray[3], vzaArray[10], parametersArray[1]
        };
        value = lut.getValue(coord);
        assertEquals(0.944405, value, 1.E-4);

        // last values in LUT:
        coord = new double[]{900.0, 2.0, 1013.25, 180.0, 69.989, 64.279, 3.0};
        value = lut.getValue(coord);
        assertEquals(0.192216, value, 1.E-4);

        coord = new double[]{900.0, 2.0, 1013.25, 180.0, 69.989, 64.279, 4.0};
        value = lut.getValue(coord);
        assertEquals(0.998259, value, 1.E-4);

        coord = new double[]{900.0, 2.0, 1013.25, 180.0, 69.989, 64.279, 5.0};
        value = lut.getValue(coord);
        assertEquals(0.998625, value, 1.E-4);
    }

    public void testLutAotKx() throws IOException {
        LookupTable lut = N2ModisUtils.getAotKxLookupTable(Sensor.MERIS);
        assertNotNull(lut);

        assertEquals(7, lut.getDimensionCount());

        final double[] kxArray = lut.getDimension(6).getSequence();
        final int nKx = kxArray.length;
        assertEquals(2, nKx);     //  Parameters
        assertEquals(1.0, kxArray[0], 1.E-4);
        assertEquals(2.0, kxArray[1], 1.E-4);

        final double[] vzaArray = lut.getDimension(5).getSequence();
        final int nVza = vzaArray.length;
        assertEquals(13, nVza);     //  VZA
        assertEquals(12.76, vzaArray[3], 1.E-4);
        assertEquals(24.24, vzaArray[5], 1.E-4);
        assertEquals(35.68, vzaArray[7], 1.E-4);

        final double[] szaArray = lut.getDimension(4).getSequence();
        final int nSza = szaArray.length;
        assertEquals(14, nSza);     //  SZA
        assertEquals(6.97, szaArray[2], 1.E-4);
        assertEquals(18.51, szaArray[4], 1.E-4);
        assertEquals(29.96, szaArray[6], 1.E-4);

        final double[] aziArray = lut.getDimension(3).getSequence();
        final int nAzi = aziArray.length;
        assertEquals(19, nAzi);     //  AZI
        assertEquals(10.0, aziArray[1], 1.E-4);
        assertEquals(130.0, aziArray[13], 1.E-4);
        assertEquals(150.0, aziArray[15], 1.E-4);

        final double[] hsfArray = lut.getDimension(2).getSequence();
        final int nHsf = hsfArray.length;
        assertEquals(4, nHsf);     //  HSF
        assertEquals(1.0, hsfArray[1], 1.E-3);
        assertEquals(2.5, hsfArray[2], 1.E-3);
        assertEquals(7.998, hsfArray[3], 1.E-3);

        final double[] aotArray = lut.getDimension(1).getSequence();
        final int nAot = aotArray.length;
        assertEquals(9, nAot);     //  AOT
        assertEquals(0.1, aotArray[2], 1.E-3);
        assertEquals(0.2, aotArray[3], 1.E-3);
        assertEquals(1.5, aotArray[7], 1.E-3);

        final double[] wvlArray = lut.getDimension(0).getSequence();
        final int nWvl = wvlArray.length;
        assertEquals(15, nWvl);     //  AOT
        assertEquals(412.0, wvlArray[0], 1.E-3);
        assertEquals(442.0, wvlArray[1], 1.E-3);
        assertEquals(900.0, wvlArray[14], 1.E-3);

        // first values in LUT
        // iWvl=0, iAot0, iHsf0, iAzi=0, iSza=0, iVza=0..1, iKx=0..1:
        double[] coord = new double[]{412.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0};
        double value = lut.getValue(coord);
        assertEquals(-0.06027, value, 1.E-4);

        coord = new double[]{412.0, 0.0, 0.0, 0.0, 0.0, 0.0, 2.0};
        value = lut.getValue(coord);
        assertEquals(0.056184, value, 1.E-4);

        coord = new double[]{412.0, 0.0, 0.0, 0.0, 0.0, 2.56, 1.0};
        value = lut.getValue(coord);
        assertEquals(-0.059273, value, 1.E-4);

        coord = new double[]{412.0, 0.0, 0.0, 0.0, 0.0, 2.56, 2.0};
        value = lut.getValue(coord);
        assertEquals(0.055945, value, 1.E-4);

        // values somewhere inside LUT:
        coord = new double[]{wvlArray[7], aotArray[2], hsfArray[1], aziArray[6], szaArray[9], vzaArray[11], kxArray[0]};
        value = lut.getValue(coord);
        assertEquals(-0.082877, value, 1.E-4);

        coord = new double[]{wvlArray[4], aotArray[1], hsfArray[2], aziArray[14], szaArray[5], vzaArray[3], kxArray[1]};
        value = lut.getValue(coord);
        assertEquals(-0.3205, value, 1.E-4);

        coord = new double[]{
                wvlArray[8], aotArray[0], hsfArray[1], aziArray[16], szaArray[3], vzaArray[10], kxArray[0]
        };
        value = lut.getValue(coord);
        assertEquals(-0.01571, value, 1.E-4);

        // last values in LUT:
        coord = new double[]{900.0, 2.0, 1013.25, 180.0, 69.989, 64.279, 1.0};
        value = lut.getValue(coord);
        assertEquals(-0.197412, value, 1.E-4);

        coord = new double[]{900.0, 2.0, 1013.25, 180.0, 69.989, 64.279, 2.0};
        value = lut.getValue(coord);
        assertEquals(0.007395, value, 1.E-4);
    }

    //<editor-fold defaultstate="collapsed" desc="NSky stuff: to be removed">
    /*
    public void testNskyLutDw() throws IOException {
        NskyLookupTable nskyLut = BbdrUtils.getNskyLookupTableDw(Sensor.MERIS);
        assertNotNull(nskyLut);
        LookupTable lut = nskyLut.getLut();
        assertNotNull(lut);
        
        assertEquals(5, lut.getDimensionCount());
        
        final double[] valueArray = lut.getDimension(4).getSequence();
        final int nValues = valueArray.length;
        assertEquals(2, nValues);     //  Parameters
        assertEquals(1.0, valueArray[0], 1.E-4);
        assertEquals(2.0, valueArray[1], 1.E-4);
        
        final double[] szaArray = lut.getDimension(3).getSequence();
        final int nSza = szaArray.length;
        assertEquals(17, nSza);     //  SZA
        assertEquals(6.97, szaArray[2], 1.E-4);
        assertEquals(29.96, szaArray[6], 1.E-4);
        assertEquals(75.71, szaArray[14], 1.E-4);
        
        final double[] hsfArray = lut.getDimension(2).getSequence();
        final int nHsf = hsfArray.length;
        assertEquals(4, nHsf);     //  HSF
        assertEquals(1.0, hsfArray[1], 1.E-3);
        assertEquals(2.5, hsfArray[2], 1.E-3);
        assertEquals(8.0, hsfArray[3], 1.E-3);
        
        final double[] aotArray = lut.getDimension(1).getSequence();
        final int nAot = aotArray.length;
        assertEquals(9, nAot);     //  AOT
        assertEquals(0.1, aotArray[2], 1.E-3);
        assertEquals(0.2, aotArray[3], 1.E-3);
        assertEquals(1.5, aotArray[7], 1.E-3);
        
        final double[] specArray = lut.getDimension(0).getSequence();
        final int nSpecs = specArray.length;
        assertEquals(3, nSpecs);     //  Parameters
        assertEquals(1.0, specArray[0], 1.E-4);
        assertEquals(2.0, specArray[1], 1.E-4);
        
        // Kpp coeffs:
        assertEquals(-1.289934, nskyLut.getKppGeo(), 1.E-4);
        assertEquals(0.10046, nskyLut.getKppVol(), 1.E-4);
        
        // first values in LUT
        // iSpec=0, iAot0, iHsf0, iSza=0, iValues=0..2:
        double[] coord = new double[]{0.0, 0.0, 0.0, 0.0, 1.0};
        double value = lut.getValue(coord);
        assertEquals(-0.02289, value, 1.E-4);
        
        coord = new double[]{0.0, 0.0, 0.0, 0.0, 2.0};
        value = lut.getValue(coord);
        assertEquals(-1.1780, value, 1.E-4);
        
        // iSpec=0, iAot0, iHsf0, iSza=1, iValues=0:
        coord = new double[]{0.0, 0.0, 0.0, 2.56, 1.0};
        value = lut.getValue(coord);
        assertEquals(-0.022719, value, 1.E-4);
        
        // values somewhere inside LUT:
        coord = new double[]{specArray[1], aotArray[1], hsfArray[2], szaArray[5], valueArray[1]};
        value = lut.getValue(coord);
        assertEquals(-1.49603, value, 1.E-4);
        
        coord = new double[]{specArray[2], aotArray[4], hsfArray[1], szaArray[9], valueArray[0]};
        value = lut.getValue(coord);
        assertEquals(0.038364, value, 1.E-4);
        
        // last values in LUT:
        // iSpec=0, iAot0, iHsf0, iSza=1, iValues=0:
        coord = new double[]{3.0, 2.0, 8.0, 87.14, 1.0};
        value = lut.getValue(coord);
        assertEquals(1.80323, value, 1.E-4);
        
        coord = new double[]{3.0, 2.0, 8.0, 87.14, 2.0};
        value = lut.getValue(coord);
        assertEquals(5.84008, value, 1.E-4);
    }
    
    public void testNskyLutUp() throws IOException {
        NskyLookupTable nskyLut = BbdrUtils.getNskyLookupTableUp(Sensor.MERIS);
        assertNotNull(nskyLut);
        LookupTable lut = nskyLut.getLut();
        assertNotNull(lut);
        
        assertEquals(5, lut.getDimensionCount());
        
        final double[] valueArray = lut.getDimension(4).getSequence();
        final int nValues = valueArray.length;
        assertEquals(2, nValues);     //  Parameters
        assertEquals(1.0, valueArray[0], 1.E-4);
        assertEquals(2.0, valueArray[1], 1.E-4);
        
        final double[] szaArray = lut.getDimension(3).getSequence();
        final int nSza = szaArray.length;
        assertEquals(17, nSza);     //  SZA
        assertEquals(6.97, szaArray[2], 1.E-4);
        assertEquals(29.96, szaArray[6], 1.E-4);
        assertEquals(75.71, szaArray[14], 1.E-4);
        
        final double[] hsfArray = lut.getDimension(2).getSequence();
        final int nHsf = hsfArray.length;
        assertEquals(4, nHsf);     //  HSF
        assertEquals(1.0, hsfArray[1], 1.E-3);
        assertEquals(2.5, hsfArray[2], 1.E-3);
        assertEquals(8.0, hsfArray[3], 1.E-3);
        
        final double[] aotArray = lut.getDimension(1).getSequence();
        final int nAot = aotArray.length;
        assertEquals(9, nAot);     //  AOT
        assertEquals(0.1, aotArray[2], 1.E-3);
        assertEquals(0.2, aotArray[3], 1.E-3);
        assertEquals(1.5, aotArray[7], 1.E-3);
        
        final double[] specArray = lut.getDimension(0).getSequence();
        final int nSpecs = specArray.length;
        assertEquals(3, nSpecs);     //  Parameters
        assertEquals(1.0, specArray[0], 1.E-4);
        assertEquals(2.0, specArray[1], 1.E-4);
        
        // Kpp coeffs:
        assertEquals(-1.289934, nskyLut.getKppGeo(), 1.E-4);
        assertEquals(0.10046, nskyLut.getKppVol(), 1.E-4);
        
        // first values in LUT
        // iSpec=0, iAot0, iHsf0, iSza=0, iValues=0..2:
        double[] coord = new double[]{0.0, 0.0, 0.0, 0.0, 1.0};
        double value = lut.getValue(coord);
        assertEquals(-0.0256047, value, 1.E-4);
        
        coord = new double[]{0.0, 0.0, 0.0, 0.0, 2.0};
        value = lut.getValue(coord);
        assertEquals(-0.863457, value, 1.E-4);
        
        // iSpec=0, iAot0, iHsf0, iSza=1, iValues=0:
        coord = new double[]{0.0, 0.0, 0.0, 2.56, 1.0};
        value = lut.getValue(coord);
        assertEquals(-0.025343, value, 1.E-4);
        
        // values somewhere inside LUT:
        coord = new double[]{specArray[1], aotArray[1], hsfArray[2], szaArray[5], valueArray[1]};
        value = lut.getValue(coord);
        assertEquals(-1.02195, value, 1.E-4);
        
        coord = new double[]{specArray[2], aotArray[4], hsfArray[1], szaArray[9], valueArray[0]};
        value = lut.getValue(coord);
        assertEquals(0.0856803, value, 1.E-4);
        
        // last values in LUT:
        // iSpec=0, iAot0, iHsf0, iSza=1, iValues=0:
        coord = new double[]{3.0, 2.0, 8.0, 87.14, 1.0};
        value = lut.getValue(coord);
        assertEquals(1.05181, value, 1.E-4);
        
        coord = new double[]{3.0, 2.0, 8.0, 87.14, 2.0};
        value = lut.getValue(coord);
        assertEquals(-9.34972, value, 1.E-4);
    }
    */
    //</editor-fold>

    public void testLutGas() throws IOException {
        GasLookupTable gasLookupTable = new GasLookupTable(Sensor.MERIS);
        gasLookupTable.load(null);
        assertNotNull(gasLookupTable);
        // todo test sth.
//        assertEquals(0,gasLookupTable.getAmfArray().length;
    }

    public void testLutInterpolation1D() {
        final double[] dimension = new double[]{0, 1, 2, 3, 4};
        final double[] values = new double[]{0, 2, 5, 10, 22};

        final LookupTable lut = new LookupTable(values, dimension);
        assertEquals(1, lut.getDimensionCount());

        assertEquals(0.0, lut.getDimension(0).getMin(), 0.0);
        assertEquals(4.0, lut.getDimension(0).getMax(), 0.0);

        assertEquals(0.0, lut.getValue(0.0), 0.0);
        assertEquals(2.0, lut.getValue(1.0), 0.0);
        assertEquals(5.0, lut.getValue(2.0), 0.0);
        assertEquals(7.5, lut.getValue(2.5), 0.0);
        assertEquals(0.2469, lut.getValue(0.12345), 0.0);
    }


=======
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.massivecraft.mcore.xlib.gson;

import com.massivecraft.mcore.xlib.gson.JsonElement;
import com.massivecraft.mcore.xlib.gson.JsonPrimitive;
import com.massivecraft.mcore.xlib.gson.internal.$Gson$Preconditions;
import com.massivecraft.mcore.xlib.gson.internal.LazilyParsedNumber;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A class representing a Json primitive value. A primitive value
 * is either a String, a Java primitive, or a Java primitive
 * wrapper type.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public final class JsonPrimitive extends JsonElement {

  private static final Class<?>[] PRIMITIVE_TYPES = { int.class, long.class, short.class,
      float.class, double.class, byte.class, boolean.class, char.class, Integer.class, Long.class,
      Short.class, Float.class, Double.class, Byte.class, Boolean.class, Character.class };

  private Object value;

  /**
   * Create a primitive containing a boolean value.
   *
   * @param bool the value to create the primitive with.
   */
  public JsonPrimitive(Boolean bool) {
    setValue(bool);
  }

  /**
   * Create a primitive containing a {@link Number}.
   *
   * @param number the value to create the primitive with.
   */
  public JsonPrimitive(Number number) {
    setValue(number);
  }

  /**
   * Create a primitive containing a String value.
   *
   * @param string the value to create the primitive with.
   */
  public JsonPrimitive(String string) {
    setValue(string);
  }

  /**
   * Create a primitive containing a character. The character is turned into a one character String
   * since Json only supports String.
   *
   * @param c the value to create the primitive with.
   */
  public JsonPrimitive(Character c) {
    setValue(c);
  }

  /**
   * Create a primitive using the specified Object. It must be an instance of {@link Number}, a
   * Java primitive type, or a String.
   *
   * @param primitive the value to create the primitive with.
   */
  JsonPrimitive(Object primitive) {
    setValue(primitive);
  }

  void setValue(Object primitive) {
    if (primitive instanceof Character) {
      // convert characters to strings since in JSON, characters are represented as a single
      // character string
      char c = ((Character) primitive).charValue();
      this.value = String.valueOf(c);
    } else {
      $Gson$Preconditions.checkArgument(primitive instanceof Number
              || isPrimitiveOrString(primitive));
      this.value = primitive;
    }
  }

  /**
   * Check whether this primitive contains a boolean value.
   *
   * @return true if this primitive contains a boolean value, false otherwise.
   */
  public boolean isBoolean() {
    return value instanceof Boolean;
  }

  /**
   * convenience method to get this element as a {@link Boolean}.
   *
   * @return get this element as a {@link Boolean}.
   */
  @Override
  Boolean getAsBooleanWrapper() {
    return (Boolean) value;
  }

  /**
   * convenience method to get this element as a boolean value.
   *
   * @return get this element as a primitive boolean value.
   */
  @Override
  public boolean getAsBoolean() {
    if (isBoolean()) {
      return getAsBooleanWrapper().booleanValue();
    } else {
      // Check to see if the value as a String is "true" in any case.
      return Boolean.parseBoolean(getAsString());
    }
  }

  /**
   * Check whether this primitive contains a Number.
   *
   * @return true if this primitive contains a Number, false otherwise.
   */
  public boolean isNumber() {
    return value instanceof Number;
  }

  /**
   * convenience method to get this element as a Number.
   *
   * @return get this element as a Number.
   * @throws NumberFormatException if the value contained is not a valid Number.
   */
  @Override
  public Number getAsNumber() {
    return value instanceof String ? new LazilyParsedNumber((String) value) : (Number) value;
  }

  /**
   * Check whether this primitive contains a String value.
   *
   * @return true if this primitive contains a String value, false otherwise.
   */
  public boolean isString() {
    return value instanceof String;
  }

  /**
   * convenience method to get this element as a String.
   *
   * @return get this element as a String.
   */
  @Override
  public String getAsString() {
    if (isNumber()) {
      return getAsNumber().toString();
    } else if (isBoolean()) {
      return getAsBooleanWrapper().toString();
    } else {
      return (String) value;
    }
  }

  /**
   * convenience method to get this element as a primitive double.
   *
   * @return get this element as a primitive double.
   * @throws NumberFormatException if the value contained is not a valid double.
   */
  @Override
  public double getAsDouble() {
    return isNumber() ? getAsNumber().doubleValue() : Double.parseDouble(getAsString());
  }

  /**
   * convenience method to get this element as a {@link BigDecimal}.
   *
   * @return get this element as a {@link BigDecimal}.
   * @throws NumberFormatException if the value contained is not a valid {@link BigDecimal}.
   */
  @Override
  public BigDecimal getAsBigDecimal() {
    return value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(value.toString());
  }

  /**
   * convenience method to get this element as a {@link BigInteger}.
   *
   * @return get this element as a {@link BigInteger}.
   * @throws NumberFormatException if the value contained is not a valid {@link BigInteger}.
   */
  @Override
  public BigInteger getAsBigInteger() {
    return value instanceof BigInteger ?
        (BigInteger) value : new BigInteger(value.toString());
  }

  /**
   * convenience method to get this element as a float.
   *
   * @return get this element as a float.
   * @throws NumberFormatException if the value contained is not a valid float.
   */
  @Override
  public float getAsFloat() {
    return isNumber() ? getAsNumber().floatValue() : Float.parseFloat(getAsString());
  }

  /**
   * convenience method to get this element as a primitive long.
   *
   * @return get this element as a primitive long.
   * @throws NumberFormatException if the value contained is not a valid long.
   */
  @Override
  public long getAsLong() {
    return isNumber() ? getAsNumber().longValue() : Long.parseLong(getAsString());
  }

  /**
   * convenience method to get this element as a primitive short.
   *
   * @return get this element as a primitive short.
   * @throws NumberFormatException if the value contained is not a valid short value.
   */
  @Override
  public short getAsShort() {
    return isNumber() ? getAsNumber().shortValue() : Short.parseShort(getAsString());
  }

 /**
  * convenience method to get this element as a primitive integer.
  *
  * @return get this element as a primitive integer.
  * @throws NumberFormatException if the value contained is not a valid integer.
  */
  @Override
  public int getAsInt() {
    return isNumber() ? getAsNumber().intValue() : Integer.parseInt(getAsString());
  }

  @Override
  public byte getAsByte() {
    return isNumber() ? getAsNumber().byteValue() : Byte.parseByte(getAsString());
  }

  @Override
  public char getAsCharacter() {
    return getAsString().charAt(0);
  }

  private static boolean isPrimitiveOrString(Object target) {
    if (target instanceof String) {
      return true;
    }

    Class<?> classOfPrimitive = target.getClass();
    for (Class<?> standardPrimitive : PRIMITIVE_TYPES) {
      if (standardPrimitive.isAssignableFrom(classOfPrimitive)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    if (value == null) {
      return 31;
    }
    // Using recommended hashing algorithm from Effective Java for longs and doubles
    if (isIntegral(this)) {
      long value = getAsNumber().longValue();
      return (int) (value ^ (value >>> 32));
    }
    if (value instanceof Number) {
      long value = Double.doubleToLongBits(getAsNumber().doubleValue());
      return (int) (value ^ (value >>> 32));
    }
    return value.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    JsonPrimitive other = (JsonPrimitive)obj;
    if (value == null) {
      return other.value == null;
    }
    if (isIntegral(this) && isIntegral(other)) {
      return getAsNumber().longValue() == other.getAsNumber().longValue();
    }
    if (value instanceof Number && other.value instanceof Number) {
      double a = getAsNumber().doubleValue();
      // Java standard types other than double return true for two NaN. So, need
      // special handling for double.
      double b = other.getAsNumber().doubleValue();
      return a == b || (Double.isNaN(a) && Double.isNaN(b));
    }
    return value.equals(other.value);
  }

  /**
   * Returns true if the specified number is an integral type
   * (Long, Integer, Short, Byte, BigInteger)
   */
  private static boolean isIntegral(JsonPrimitive primitive) {
    if (primitive.value instanceof Number) {
      Number number = (Number) primitive.value;
      return number instanceof BigInteger || number instanceof Long || number instanceof Integer
          || number instanceof Short || number instanceof Byte;
    }
    return false;
  }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

