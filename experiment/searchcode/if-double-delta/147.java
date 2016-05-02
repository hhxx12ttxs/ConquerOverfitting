/**
    * Copyright (c) 2009, 5AM Solutions, Inc.
    * All rights reserved.
      *
    * Redistribution and use in source and binary forms, with or without
    * modification, are permitted provided that the following conditions are met:
      *
    * - Redistributions of source code must retain the above copyright notice,
    * this list of conditions and the following disclaimer.
    *
    * - Redistributions in binary form must reproduce the above copyright notice,
    * this list of conditions and the following disclaimer in the documentation
    * and/or other materials provided with the distribution.
    *
    * - Neither the name of the author nor the names of its contributors may be
    * used to endorse or promote products derived from this software without
    * specific prior written permission.
    *
    * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
    * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    * POSSIBILITY OF SUCH DAMAGE.
*/

package com.fiveamsolutions.tissuelocator.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

import org.junit.Test;

import com.fiveamsolutions.tissuelocator.data.QuantityUnits;
import com.fiveamsolutions.tissuelocator.data.TimeUnits;
import com.fiveamsolutions.tissuelocator.util.QuantityConverter;
import com.fiveamsolutions.tissuelocator.util.TimeConverter;

/**
 * Tests the conversion of values between different units.
 * @author gvaughn
 *
 */
public class UnitConversionTest {

    private static final int MAX_TIME = 90;
    private static final BigDecimal MAX_QUANTITY = new BigDecimal(99999999999999999L);
    private static final double MONTHS_IN_YEAR = 12;
    private static final double DAYS_IN_MONTH = 30;
    private static final double DAYS_IN_YEAR = 365;
    private static final double HOURS_IN_DAY = 24;
    private static final BigDecimal MASS_CONVERSION_FACTOR = new BigDecimal(1000);
    private static final BigDecimal MASS_CONVERSION_FACTOR_SQ = new BigDecimal(1000000);
    private static final BigDecimal OZ_ML_CONVERSION_FACTOR = new BigDecimal("29.5735296");
    private static final int SCALE = 1000;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_DOWN;
    private static final double DELTA = .01;
    
    /**
     * Tests time conversion.
     */
    @Test
    public void testTimeConversion() {
        int timeValue = 2;
        for (TimeUnits currentUnits : TimeUnits.values()) {
            assertEquals(timeValue, TimeConverter.convert(timeValue, currentUnits, currentUnits), DELTA);
        }
        timeValue = 0;
        for (TimeUnits currentUnits : TimeUnits.values()) {
            for (TimeUnits targetUnits : TimeUnits.values()) {
                assertEquals(0, TimeConverter.convert(timeValue, currentUnits, targetUnits), DELTA);
            }
        }
        
        verifyAll(MAX_TIME);
        
        //CHECKSTYLE:OFF - magic numbers
        // hardcoded sanity checks
        
        // 45 days
        timeValue = 1080;
        Map<TimeUnits, Double> conversions = TimeConverter.getAllValues(timeValue, TimeUnits.HOURS);
        assertEquals(45, TimeConverter.convert(timeValue, TimeUnits.HOURS, TimeUnits.DAYS), DELTA);
        assertEquals(45, conversions.get(TimeUnits.DAYS), DELTA);
        assertEquals(1.5, TimeConverter.convert(timeValue, TimeUnits.HOURS, TimeUnits.MONTHS), DELTA);
        assertEquals(1.5, conversions.get(TimeUnits.MONTHS), DELTA);
        assertEquals(.12, TimeConverter.convert(timeValue, TimeUnits.HOURS, TimeUnits.YEARS), DELTA);
        assertEquals(.12, conversions.get(TimeUnits.YEARS), DELTA);
        verifyAll(timeValue);
        
        timeValue = 90;
        conversions = TimeConverter.getAllValues(timeValue, TimeUnits.DAYS);
        assertEquals(3, TimeConverter.convert(timeValue, TimeUnits.DAYS, TimeUnits.MONTHS), DELTA);
        assertEquals(3, conversions.get(TimeUnits.MONTHS), DELTA);
        assertEquals(.25, TimeConverter.convert(timeValue, TimeUnits.DAYS, TimeUnits.YEARS), DELTA);
        assertEquals(.25, conversions.get(TimeUnits.YEARS), DELTA);
        assertEquals(2160, TimeConverter.convert(timeValue, TimeUnits.DAYS, TimeUnits.HOURS), DELTA);
        assertEquals(2160, conversions.get(TimeUnits.HOURS), DELTA);
        verifyAll(timeValue);
        
        timeValue = 21;
        conversions = TimeConverter.getAllValues(timeValue, TimeUnits.MONTHS);
        assertEquals(1.75, TimeConverter.convert(timeValue, TimeUnits.MONTHS, TimeUnits.YEARS), DELTA);
        assertEquals(1.75, conversions.get(TimeUnits.YEARS), DELTA);
        assertEquals(630, TimeConverter.convert(timeValue, TimeUnits.MONTHS, TimeUnits.DAYS), DELTA);
        assertEquals(630, conversions.get(TimeUnits.DAYS), DELTA);
        assertEquals(15120, TimeConverter.convert(timeValue, TimeUnits.MONTHS, TimeUnits.HOURS), DELTA);
        assertEquals(15120, conversions.get(TimeUnits.HOURS), DELTA);
        verifyAll(timeValue);
        
        timeValue = 3;
        conversions = TimeConverter.getAllValues(timeValue, TimeUnits.YEARS);
        assertEquals(36, TimeConverter.convert(timeValue, TimeUnits.YEARS, TimeUnits.MONTHS), DELTA);
        assertEquals(36, conversions.get(TimeUnits.MONTHS), DELTA);
        assertEquals(1095, TimeConverter.convert(timeValue, TimeUnits.YEARS, TimeUnits.DAYS), DELTA);
        assertEquals(1095, conversions.get(TimeUnits.DAYS), DELTA);
        assertEquals(26280, TimeConverter.convert(timeValue, TimeUnits.YEARS, TimeUnits.HOURS), DELTA);
        assertEquals(26280, conversions.get(TimeUnits.HOURS), DELTA);
        verifyAll(timeValue);
        //CHECKSTYLE:ON
    }
    
    private void verifyAll(int timeValue) {
        verifyHours(timeValue);
        verifyDays(timeValue);
        verifyMonths(timeValue);
        verifyYears(timeValue);
    }
    
    private void verifyHours(int timeValue) {
        Map<TimeUnits, Double> conversions = TimeConverter.getAllValues(timeValue, TimeUnits.HOURS);
        assertEquals(timeValue, conversions.get(TimeUnits.HOURS), DELTA);
        assertEquals(timeValue / HOURS_IN_DAY, TimeConverter.convert(timeValue, 
                TimeUnits.HOURS, TimeUnits.DAYS), DELTA);
        assertEquals(timeValue / HOURS_IN_DAY, conversions.get(TimeUnits.DAYS), DELTA);
        assertEquals(timeValue / (HOURS_IN_DAY * DAYS_IN_MONTH), 
                TimeConverter.convert(timeValue, TimeUnits.HOURS, TimeUnits.MONTHS), DELTA);
        assertEquals(timeValue / (HOURS_IN_DAY * DAYS_IN_MONTH), conversions.get(TimeUnits.MONTHS), DELTA);
        assertEquals(timeValue / (HOURS_IN_DAY * DAYS_IN_YEAR), 
                TimeConverter.convert(timeValue, TimeUnits.HOURS, TimeUnits.YEARS), DELTA);
        assertEquals(timeValue / (HOURS_IN_DAY * DAYS_IN_YEAR), conversions.get(TimeUnits.YEARS), DELTA);
    }
    
    private void verifyDays(int timeValue) {
        Map<TimeUnits, Double> conversions = TimeConverter.getAllValues(timeValue, TimeUnits.DAYS);
        assertEquals(timeValue, conversions.get(TimeUnits.DAYS), DELTA);
        assertEquals(timeValue / DAYS_IN_MONTH, TimeConverter.convert(timeValue, TimeUnits.DAYS, 
                TimeUnits.MONTHS), DELTA);
        assertEquals(timeValue / DAYS_IN_MONTH, conversions.get(TimeUnits.MONTHS), DELTA);
        assertEquals(timeValue / DAYS_IN_YEAR, TimeConverter.convert(timeValue, TimeUnits.DAYS, 
                TimeUnits.YEARS), DELTA);
        assertEquals(timeValue / DAYS_IN_YEAR, conversions.get(TimeUnits.YEARS), DELTA);
        assertEquals(timeValue * HOURS_IN_DAY, TimeConverter.convert(timeValue, TimeUnits.DAYS, 
                TimeUnits.HOURS), DELTA);
        assertEquals(timeValue * HOURS_IN_DAY, conversions.get(TimeUnits.HOURS), DELTA); 
    }
    
    private void verifyMonths(int timeValue) {
        Map<TimeUnits, Double> conversions = TimeConverter.getAllValues(timeValue, TimeUnits.MONTHS);
        assertEquals(timeValue, conversions.get(TimeUnits.MONTHS), DELTA);
        assertEquals(timeValue / MONTHS_IN_YEAR, 
                TimeConverter.convert(timeValue, TimeUnits.MONTHS, TimeUnits.YEARS), DELTA);
        assertEquals(timeValue / MONTHS_IN_YEAR, conversions.get(TimeUnits.YEARS), DELTA);
        assertEquals(timeValue * DAYS_IN_MONTH, 
                TimeConverter.convert(timeValue, TimeUnits.MONTHS, TimeUnits.DAYS), DELTA);
        assertEquals(timeValue * DAYS_IN_MONTH, conversions.get(TimeUnits.DAYS), DELTA);
        assertEquals(timeValue * DAYS_IN_MONTH * HOURS_IN_DAY, 
                TimeConverter.convert(timeValue, TimeUnits.MONTHS, TimeUnits.HOURS), DELTA);
        assertEquals(timeValue * DAYS_IN_MONTH * HOURS_IN_DAY, conversions.get(TimeUnits.HOURS), DELTA);
    }
    
    private void verifyYears(int timeValue) {
        Map<TimeUnits, Double> conversions = TimeConverter.getAllValues(timeValue, TimeUnits.YEARS);
        assertEquals(timeValue, conversions.get(TimeUnits.YEARS), DELTA);
        assertEquals(timeValue * MONTHS_IN_YEAR, TimeConverter.convert(timeValue, 
                TimeUnits.YEARS, TimeUnits.MONTHS), DELTA);
        assertEquals(timeValue * MONTHS_IN_YEAR, conversions.get(TimeUnits.MONTHS), DELTA);
        assertEquals(timeValue * DAYS_IN_YEAR, TimeConverter.convert(timeValue, TimeUnits.YEARS, 
                TimeUnits.DAYS), DELTA);
        assertEquals(timeValue * DAYS_IN_YEAR, conversions.get(TimeUnits.DAYS), DELTA);
        assertEquals(timeValue * DAYS_IN_YEAR * HOURS_IN_DAY, 
                TimeConverter.convert(timeValue, TimeUnits.YEARS, TimeUnits.HOURS), DELTA);
        assertEquals(timeValue * DAYS_IN_YEAR * HOURS_IN_DAY, conversions.get(TimeUnits.HOURS), DELTA);
    }
    
    /**
     * Test quantity unit conversion.
     */
    @Test
    public void verifyQuantityConversion() {
        BigDecimal quantityValue = new BigDecimal(2);
        for (QuantityUnits currentUnits : QuantityUnits.values()) {
            assertEquals(quantityValue, QuantityConverter.convert(quantityValue, currentUnits, currentUnits));
        }
        
        QuantityUnits[] nonConvertable = new QuantityUnits[]{QuantityUnits.CELL_COUNT,
                QuantityUnits.CELLS, QuantityUnits.COUNT};
        
        for (QuantityUnits currentUnits : nonConvertable) {
            for (QuantityUnits targetUnits : QuantityUnits.values()) {
                if (!currentUnits.equals(targetUnits)) {
                    assertNull(QuantityConverter.convert(quantityValue, currentUnits, targetUnits)); 
                }             
            }
        }
        
        for (QuantityUnits targetUnits : QuantityUnits.values()) {
            for (QuantityUnits currentUnits : nonConvertable) {
                if (!currentUnits.equals(targetUnits)) {
                    assertNull(QuantityConverter.convert(quantityValue, currentUnits, targetUnits)); 
                }
            }
        }
        
        quantityValue = new BigDecimal(2);
        Map<QuantityUnits, BigDecimal> conversions;
        for (QuantityUnits currentUnits : nonConvertable) {
            conversions = QuantityConverter.getAllValues(quantityValue, currentUnits);
            assertEquals(1, conversions.size());
            assertEquals(quantityValue, conversions.get(currentUnits));
        }
        
        //CHECKSTYLE:OFF - magic numbers
        BigDecimal[] values = new BigDecimal[]{
                new BigDecimal(-23569.87654),
                BigDecimal.TEN,
                new BigDecimal(34598743.345353),
                MAX_QUANTITY,
                BigDecimal.ZERO
        };
        for (BigDecimal value : values) {
            verifyMilligrams(value);
            verifyMicrograms(value);
            verifyGrams(value);
            verifyMilliliters(value);
            verifyOunces(value);
        }
    }
    
    private void verifyMilligrams(BigDecimal quantityValue) {
        Map<QuantityUnits, BigDecimal> conversions = QuantityConverter.getAllValues(quantityValue, QuantityUnits.MG);
        assertEquals(quantityValue, conversions.get(QuantityUnits.MG));
        assertEquals(quantityValue.multiply(MASS_CONVERSION_FACTOR), conversions.get(QuantityUnits.UG));
        assertEquals(quantityValue.divide(MASS_CONVERSION_FACTOR), conversions.get(QuantityUnits.G));
        assertEquals(quantityValue.multiply(MASS_CONVERSION_FACTOR), 
                QuantityConverter.convert(quantityValue, QuantityUnits.MG, QuantityUnits.UG));
        assertEquals(quantityValue.divide(MASS_CONVERSION_FACTOR), 
                QuantityConverter.convert(quantityValue, QuantityUnits.MG, QuantityUnits.G));
    }
    
    private void verifyMicrograms(BigDecimal quantityValue) {
        Map<QuantityUnits, BigDecimal> conversions = QuantityConverter.getAllValues(quantityValue, QuantityUnits.UG);
        assertEquals(quantityValue, conversions.get(QuantityUnits.UG));
        assertEquals(quantityValue.divide(MASS_CONVERSION_FACTOR), conversions.get(QuantityUnits.MG));
        assertEquals(quantityValue.divide(MASS_CONVERSION_FACTOR_SQ), conversions.get(QuantityUnits.G));
        assertEquals(quantityValue.divide(MASS_CONVERSION_FACTOR), 
                QuantityConverter.convert(quantityValue, QuantityUnits.UG, QuantityUnits.MG));
        assertEquals(quantityValue.divide(MASS_CONVERSION_FACTOR_SQ), 
                QuantityConverter.convert(quantityValue, QuantityUnits.UG, QuantityUnits.G));
    }
    
    private void verifyGrams(BigDecimal quantityValue) {
        Map<QuantityUnits, BigDecimal> conversions = QuantityConverter.getAllValues(quantityValue, QuantityUnits.G);
        assertEquals(quantityValue, conversions.get(QuantityUnits.G));
        assertEquals(quantityValue.multiply(MASS_CONVERSION_FACTOR), conversions.get(QuantityUnits.MG));
        assertEquals(quantityValue.multiply(MASS_CONVERSION_FACTOR_SQ), conversions.get(QuantityUnits.UG));
        assertEquals(quantityValue.multiply(MASS_CONVERSION_FACTOR), 
                QuantityConverter.convert(quantityValue, QuantityUnits.G, QuantityUnits.MG));
        assertEquals(quantityValue.multiply(MASS_CONVERSION_FACTOR_SQ), 
                QuantityConverter.convert(quantityValue, QuantityUnits.G, QuantityUnits.UG));
    }
    
    private void verifyMilliliters(BigDecimal quantityValue) {
        Map<QuantityUnits, BigDecimal> conversions = QuantityConverter.getAllValues(quantityValue, QuantityUnits.ML);
        assertEquals(quantityValue, conversions.get(QuantityUnits.ML));
        assertEquals(quantityValue.divide(OZ_ML_CONVERSION_FACTOR, SCALE, ROUNDING_MODE), conversions.get(QuantityUnits.OZ));
        assertEquals(quantityValue.divide(OZ_ML_CONVERSION_FACTOR, SCALE, ROUNDING_MODE), 
                QuantityConverter.convert(quantityValue, QuantityUnits.ML, QuantityUnits.OZ));
    }
    
    private void verifyOunces(BigDecimal quantityValue) {
        Map<QuantityUnits, BigDecimal> conversions = QuantityConverter.getAllValues(quantityValue, QuantityUnits.OZ);
        assertEquals(quantityValue, conversions.get(QuantityUnits.OZ));
        assertEquals(quantityValue.multiply(OZ_ML_CONVERSION_FACTOR), conversions.get(QuantityUnits.ML));
        assertEquals(quantityValue.multiply(OZ_ML_CONVERSION_FACTOR), 
                QuantityConverter.convert(quantityValue, QuantityUnits.OZ, QuantityUnits.ML));
    }
}

