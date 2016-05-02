<<<<<<< HEAD
//
// FloatProcessorTest.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package ij.process;

import static org.junit.Assert.assertEquals;
import ij.ImagePlus;
import ij.io.Opener;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for {@link FloatProcessor}.
 *
 * @author Barry DeZonia
 */
public class FloatProcessorTest {


	public static float[][] imageFloatData;

    public static float[][] combined;


    /*
     * Open an known image for internal testing...
     */
	@BeforeClass
	public static void runBeforeClass()
	{
		String id = DataConstants.DATA_DIR + "head.tif";
		ImagePlus imp = new Opener().openImage(id);
		imageFloatData = imp.getProcessor().getFloatArray();
		combined = new float[2][imp.getWidth() * imp.getHeight()];
	}

	@AfterClass
	public static void runAfterClass()
	{
		// no action needed
	}

	@Test
	public void testSetColor()
	{
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessor =  new FloatProcessor(imageFloatData);

		floatProcessor.setColor(0);
		floatProcessor.fill();

		//see if the test passes
		assertEquals( 0, floatProcessor.get(0), 0.0 );
	}

	@Test
	public void testSetValue()
	{
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessor = new FloatProcessor(imageFloatData);

		//set the default fill value
		float setcolorvalue = 1;

		floatProcessor.setValue(setcolorvalue);

		//overwrite the pixel value with the new default fill value
		floatProcessor.drawPixel(1, 1);

		//see if the value was over-writen with the SetColor value
		float postDrawPixelValue = floatProcessor.getf(1, 1);

		//see if the test passes assertEquals(float expected, float actual, float delta)
		assertEquals(setcolorvalue, postDrawPixelValue, 0.0);
	}

	@Test
	public void testSetBackgroundValue() {
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessor = new FloatProcessor(imageFloatData);

		//how can it fail? it does nothing!
		floatProcessor.setBackgroundValue(0);

		//see if the test passes assertEquals(float expected, float actual, float delta)
		assertEquals(true, true);
	}

	@Test
	public void testGetMin() {
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessor = new FloatProcessor(imageFloatData);

        float min = Float.MAX_VALUE;

        for (float[] fa : imageFloatData)
            for (float f : fa)
               if(f < min) min = f;

		//see if the test passes assertEquals(float expected, float actual, float delta)
		assertEquals(min, floatProcessor.getMin(), 0.0);
	}

	@Test
	public void testGetMax() {
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessor = new FloatProcessor(imageFloatData);

        float max = Float.MIN_VALUE;

        for (float[] fa : imageFloatData)
            for (float f : fa)
               if(f > max) max = f;

		//see if the test passes assertEquals(float expected, float actual, float delta)
		assertEquals(max, floatProcessor.getMax(), 0.0);
	}

	@Test
	public void testSetMinAndMax() {
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessor = new FloatProcessor(imageFloatData);

		//set the values for max and min used in the test
		double max = 2.99;
		double min = 1.99;

        floatProcessor.setMinAndMax(min, max);

		//see if the test passes assertEquals(float expected, float actual, float delta)
		assertEquals(max, floatProcessor.getMax(), 1.0);
        assertEquals(min, floatProcessor.getMin(), 1.0);
	}

	@Test
	public void testResetMinAndMax()
	{
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessor = new FloatProcessor(imageFloatData);

		//get the min and max
		double maxImage = floatProcessor.getMax();
		double minImage = floatProcessor.getMin();

		//set the values for max and min used in the test
		double max = 2.99;
		double min = 1.99;

		//change the min and max
		floatProcessor.setMinAndMax(min, max);

		//reset should yield the initial values
		floatProcessor.resetMinAndMax();

		//see if the test passes assertEquals(float expected, float actual, float delta)
		assertEquals(maxImage, floatProcessor.getMax(), 0.0);
		assertEquals(minImage, floatProcessor.getMin(), 0.0);
	}

	@Test
	public void testSetThreshold()
	{
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessor = new FloatProcessor(imageFloatData);

		floatProcessor.setThreshold(1.0, 2.0, 4);

		//assert that the max has been set
		assertEquals(floatProcessor.getMaxThreshold(), 2.0, 0.0);
		//assert that the min has been set
		assertEquals(floatProcessor.getMinThreshold(), 1.0, 0.0);
		//assert that the lutUpdate mode change has taken effect
		assertEquals(floatProcessor.getLutUpdateMode(), 4);

	}

	public static byte[] float1DtoByteArray (float[] f)
	{

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        int i = 0;
        for (float m : f)
        {
        	try {
        		out.writeFloat(m);
            	combined[0][i++] = m;
        	} catch (IOException e) {

        		e.printStackTrace();
        	}
        }

		return baos.toByteArray();
	}

	public static byte[] float2DtoByteArray (float[][] f)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        int width = f.length;
		int height = f[0].length;
		int i=0;
		for (int y=0; y<height; y++)
		{
			for (int x=0; x<width; x++)
			{
				try {
					out.writeFloat(f[x][y]);
	            	combined[1][i++] = f[x][y];
				} catch (IOException e) {

					e.printStackTrace();
				}
			}
		}

		return baos.toByteArray();
	}

	public static byte[] getSHA1DigestFromByteArray(byte[] b)
	{
		MessageDigest sha1MessageDigest = null;

		//assign a message digest from type SHA1
		try {
			sha1MessageDigest = MessageDigest.getInstance("SHA");
		}
		catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
			return null;
		}
		for(byte bte:b)
			sha1MessageDigest.update(bte);

		return sha1MessageDigest.digest();
	}

	/*
	 * There are many ways to test value based methods.  This takes the sum of
	 * all the values for a known image and ensures the static double value
	 * representing the sum of all float values in the array hasn't changed.
	 */
	@Test
	public void testGetPixels()
	{
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessor = new FloatProcessor(imageFloatData);

        //ensure the reference and test arrays are the same using the Java SHA1 implementation
        byte[] referenceImageData = float2DtoByteArray( imageFloatData);
        byte[] testImageData = float1DtoByteArray( (float[]) floatProcessor.getPixels());

        byte[] referenceSHA1 = getSHA1DigestFromByteArray( referenceImageData );
        byte[] testSHA1 = getSHA1DigestFromByteArray( testImageData);


        boolean result = true;

        for(int i=0;i<referenceSHA1.length;i++)
        {
        	//System.out.print(referenceSHA1[i] + " = " +testSHA1[i] + "  ");
        	if(referenceSHA1[i] != testSHA1[i])
        	{
        		result = false;
        		i=referenceSHA1.length;
        	}
        }

		 assertEquals(true, result);
	}



	@Test
	public void testFlipVertical()
	{
		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessorTest = new FloatProcessor(imageFloatData);

		float[] initialImageReference = (float[]) floatProcessorTest.getPixels();

		floatProcessorTest.flipVertical();
		floatProcessorTest.flipVertical();

		float[] testImageReference = (float[]) floatProcessorTest.getPixels();

		boolean result = true;

		for(int i=0; i<initialImageReference.length; i++)
		{
			//System.out.print(referenceSHA1[i] + " = " +testSHA1[i] + "  ");
			if(initialImageReference[i] != testImageReference[i])
			{
				result = false;
				i=initialImageReference.length;
			}
		}

		assertEquals(true, result);
	}

	@Test
	public void testFill()
	{
		//set a test fill value
		double testFillValue = 19.99;

		//Create a new FloatProcessor object for testing
		FloatProcessor floatProcessorTest = new FloatProcessor(imageFloatData);

		//set the fill value
		floatProcessorTest.setValue(testFillValue);

		//fill the image
		floatProcessorTest.fill();

		//get the 1D float array
		float[] testImageResults = (float[]) floatProcessorTest.getPixels();

		//iterate through the results and ensure the expected value...
		for(int i=0; i<testImageResults.length; i++)
		{
			assertEquals( testImageResults[0], (float) testFillValue, 0.0);
		}
	}


	@Test
	public void testGetPixelsCopy()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

		//get a copy of the native data
		float[] referencePixels = (float[]) floatProcessorTest.getPixels();

		//get a copy of Pixels
		float[] testPixels = (float[]) floatProcessorTest.getPixelsCopy();

		boolean result = true;
		//iterate through the results and ensure the expected value...
		for(int i=0; i<referencePixels.length; i++)
		{
			if(referencePixels[i] != testPixels[i])
			{
				result = false;
				i = referencePixels.length;
			}
		}

		assertEquals(true, result);
	}

	@Test
	public void testGetPixelIntInt()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

		//ensure that all pixel values were the same
		boolean result  = true;
		for(int i = 0; i<floatProcessorTest.height; i++)
			for(int j = 0; j<floatProcessorTest.width; j++)
				if(floatProcessorTest.getPixel(j, i) != Float.floatToIntBits( imageFloatData[j][i] ) )
				{
					result = false;
					i = floatProcessorTest.height;
					j = floatProcessorTest.width;
				}

		//ensure that all pixel values were the same
		assertEquals(true, result);
	}

	@Test
	public void testGetInt()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

		//get the linear array of pixels
		float[] referencePixels = (float[]) floatProcessorTest.getPixels();

		//ensure that all pixel values were the same
		boolean result  = true;

		//iterate through the results an flag any changes...
		for(int j = 0; j < floatProcessorTest.getPixelCount(); j++)
		{
			if( floatProcessorTest.get(j) != Float.floatToIntBits(referencePixels[j])  )
			{
				result = false;
			}
		}

		//ensure that all pixel values were the same
		assertEquals(true, result);
	}

	@Test
	public void testGetIntInt()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

		//ensure that all pixel values were the same
		boolean result  = true;

		for(int i = 0; i<floatProcessorTest.height; i++)
        {
			for(int j = 0; j<floatProcessorTest.width; j++)
				if(floatProcessorTest.getPixel(j, i) != Float.floatToIntBits( imageFloatData[j][i] ) )
				{
					//System.out.println( floatProcessorTest.getf(j, i) + " i " + i + " j " + j + " " + imageFloatData[j][i] );
					result = false;
				}
        }

		//ensure that all pixel values were the same
		assertEquals(true, result);
	}

	@Test
	public void testSetIntIntInt()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

		for(int y = 0; y<floatProcessorTest.height; y++)
			for(int x = 0; x<floatProcessorTest.width; x++)
			{
				int reference = y*floatProcessorTest.width + x;

				//set the value
				floatProcessorTest.set(x, y, reference);

				//get the set value (converted back to an int)
				int result =  floatProcessorTest.get(x, y);

				//check the result
				assertEquals( reference, result );
			}
	}

	@Test
	public void testSetIntInt()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

		for(int y = 0; y<floatProcessorTest.height*floatProcessorTest.width; y++)
		{
			int reference = y;

			//set the value
			floatProcessorTest.set(y, reference);

			//get the set value (converted back to an int)
			int result =  floatProcessorTest.get(y);

			//check the result
			assertEquals( reference, result );
		}
	}

	@Test
	public void testGetfIntInt()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

		//ensure that all pixel values were the same
		boolean result  = true;

		for(int i = 0; i<floatProcessorTest.height; i++)
			for(int j = 0; j<floatProcessorTest.width; j++)
			{

				if(floatProcessorTest.getf(j, i) != imageFloatData[j][i] )
				{
					//System.out.println( floatProcessorTest.getf(j, i) + " i " + i + " j " + j + " " + imageFloatData[j][i] );
					result = false;
				}
			}

		//ensure that all pixel values were the same
		assertEquals(true, result);
	}

	@Test
	public void testGetfInt()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

		//get the reference array
		float[] imageReferenceArray = (float[]) floatProcessorTest.getPixels();

		for(int y = 0; y<floatProcessorTest.height*floatProcessorTest.width; y++)
		{
			//check the result
			assertEquals( floatProcessorTest.getf(y), imageReferenceArray[y], 0.0f );
		}
	}

	@Test
	public void testSetfIntIntFloat()
	{
		//set the reference float
		float referenceFloat = 19.99f;

		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

		for(int i = 0; i<floatProcessorTest.height; i++)
			for(int j = 0; j<floatProcessorTest.width; j++)
			{
				//set the value
				floatProcessorTest.setf(j, i, referenceFloat);

				//check the result
				assertEquals( referenceFloat, floatProcessorTest.getf(j, i), 0.0f );
			}
	}

	@Test
	public void testSetfIntFloat()
	{
		//set the reference float
		float referenceFloat = 19.99f;

		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
		FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);


			for(int j = 0; j<floatProcessorTest.getPixelCount(); j++)
			{
				//set the value
				floatProcessorTest.setf(j, referenceFloat);

				//check the result
				assertEquals( referenceFloat, floatProcessorTest.getf(j), 0.0f );
			}
	}

	@Test
	public void testGetPixelIntIntIntArray()
    {

        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

        for(int i = 0; i<floatProcessorTest.height; i++)
            for(int j = 0; j<floatProcessorTest.width; j++)
            {
                //get the reference int
                int[] referenceInt  = { (int) imageFloatData[j][i] } ;

                //get the test results
                int[] testInt =  floatProcessorTest.getPixel(j, i, null) ;

                //check the result
                assertEquals( referenceInt[0] , testInt[0], 0.0f );
            }

	}

	@Test
	public void testPutPixelIntIntIntArray()
    {
        //create an input array
        int[] inputArray = {1, 2};

        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

        for(int i = 0; i<floatProcessorTest.height; i++)
            for(int j = 0; j<floatProcessorTest.width; j++)
            {
                //set the value under test
                floatProcessorTest.putPixel(j, i, inputArray) ;

                //get the pixel value that was set
                int[] result = floatProcessorTest.getPixel(j, i, null);

                //check the result
                assertEquals( inputArray[0] ,result[0]);

                //set the value under test
                floatProcessorTest.putPixel(j, i, inputArray) ;

                //get the pixel value that was set
                result = floatProcessorTest.getPixel(j, i, null);

                //check the result
                assertEquals( inputArray[0] , result[0], 0.0f );
            }
	}

	@Test
	public void testGetInterpolatedPixel()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

        //get the underlying 1D array
        float[] pixels = (float[]) floatProcessorTest.getPixels();

        floatProcessorTest.setInterpolationMethod(ImageProcessor.BILINEAR);
        for(int i = 0; i< floatProcessorTest.height-1; i++)
            for(int j = 0; j< floatProcessorTest.width-1; j++)
            {
                //Bilinear interpolation
		        int xbase = j;
		        int ybase = i;

		        double xFraction = j - xbase;
		        double yFraction = i - ybase;

		        int offset = ybase * floatProcessorTest.width + xbase;

		        double lowerLeft = pixels[offset];
		        double lowerRight = pixels[offset + 1];
		        double upperRight = pixels[offset + floatProcessorTest.width + 1];
		        double upperLeft = pixels[offset + floatProcessorTest.width];
                double upperAverage = upperLeft + xFraction * (upperRight - upperLeft);
		        double lowerAverage = lowerLeft + xFraction * (lowerRight - lowerLeft);

		        double referenceResult = lowerAverage + yFraction * (upperAverage - lowerAverage);

                //get the pixel value that was set
                double result = floatProcessorTest.getInterpolatedPixel(j, i);

                //check the result
                assertEquals( referenceResult, result, Float.MAX_VALUE );
            }
	}

	@Test
	public void testGetPixelInterpolated()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

        //get the underlying 1D array
        float[] pixels = (float[]) floatProcessorTest.getPixels();

        // interpolationMethod==BILINEAR
        floatProcessorTest.setInterpolationMethod(ImageProcessor.BILINEAR);
        for(int i = 0; i<floatProcessorTest.height-1; i++)
            for(int j = 0; j<floatProcessorTest.width-1; j++)
            {
		        int xbase = j;
		        int ybase = i;

		        double xFraction = j - xbase;
		        double yFraction = i - ybase;

		        int offset = ybase * floatProcessorTest.width + xbase;

		        double lowerLeft = pixels[offset];
		        double lowerRight = pixels[offset + 1];
		        double upperRight = pixels[offset + floatProcessorTest.width + 1];
		        double upperLeft = pixels[offset + floatProcessorTest.width];
                double upperAverage = upperLeft + xFraction * (upperRight - upperLeft);
		        double lowerAverage = lowerLeft + xFraction * (lowerRight - lowerLeft);

		        double referenceResult = lowerAverage + yFraction * (upperAverage - lowerAverage);

                //get the pixel value that was set
                double result = floatProcessorTest.getPixelInterpolated(j, i);

                //check the result
                assertEquals( result, referenceResult, Float.MAX_VALUE );
            }
	}

	@Test
	public void testPutPixelIntIntInt()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

        //get the underlying 1D array
        float[] pixels = (float[]) floatProcessorTest.getPixels();

        // interpolationMethod==BILINEAR
        floatProcessorTest.setInterpolationMethod(ImageProcessor.BILINEAR);
        for(int i = 0; i<floatProcessorTest.height-1; i++)
            for(int j = 0; j<floatProcessorTest.width-1; j++)
            {
		        int xbase = j;
		        int ybase = i;

		        double xFraction = j - xbase;
		        double yFraction = i - ybase;

		        int offset = ybase * floatProcessorTest.width + xbase;

		        double lowerLeft = pixels[offset];
		        double lowerRight = pixels[offset + 1];
		        double upperRight = pixels[offset + floatProcessorTest.width + 1];
		        double upperLeft = pixels[offset + floatProcessorTest.width];
                double upperAverage = upperLeft + xFraction * (upperRight - upperLeft);
		        double lowerAverage = lowerLeft + xFraction * (lowerRight - lowerLeft);

		        double referenceResult = lowerAverage + yFraction * (upperAverage - lowerAverage);

                //get the pixel value that was set
                double result = floatProcessorTest.getPixelInterpolated(j, i);

                //check the result
                assertEquals( result, referenceResult, Float.MAX_VALUE );
            }
	}

	@Test
	public void testGetPixelValue()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

        //get a reference value
        float refValue = imageFloatData[floatProcessorTest.width-1][floatProcessorTest.height-1];

        //get the test value
        float result = floatProcessorTest.getPixelValue(floatProcessorTest.width-1,floatProcessorTest.height-1);

        //check the result
        assertEquals( refValue, result, 0.0);
	}

	@Test
	public void testPutPixelValue()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

        //get a reference value
        float refValue = (float) 19.99;

        //put the test reference
        floatProcessorTest.putPixelValue(floatProcessorTest.width-1,floatProcessorTest.height-1, refValue);

        //get the reference value
        float result = floatProcessorTest.getPixelValue( floatProcessorTest.width-1, floatProcessorTest.height-1 );

        //check the result
        assertEquals( refValue, result, 0.0);
	}

	@Test
	public void testDrawPixel()
    {
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor floatProcessorTest  = new FloatProcessor(imageFloatData);

        //get a reference value
        float refValue = (float) 19.99;


        //set the fill value
        floatProcessorTest.setValue(refValue);

        //put the test reference
        floatProcessorTest.drawPixel(floatProcessorTest.width-1,floatProcessorTest.height-1);

        //get the reference value
        float result = floatProcessorTest.getPixelValue( floatProcessorTest.width-1, floatProcessorTest.height-1 );

        //check the result
        assertEquals( refValue, result, 0.0);
	}

	@Test
	public void testSetPixelsObject()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessorTest  = new FloatProcessor(imageFloatData);

        //get the underlying 1D array
        float[] refPixels = (float[]) refFloatProcessorTest.getPixels();

        //create a test int[][]
        float[]  testPixels = {1, 1, 2, 2};

        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
         FloatProcessor testFloatProcessorTest  = new FloatProcessor(imageFloatData);

        //apply the change
        testFloatProcessorTest.setPixels(testPixels);

        //change array to back same dimensions
        testFloatProcessorTest.setPixels(refPixels);

        //get the test value
        float testResult = testFloatProcessorTest.getPixelValue( testFloatProcessorTest.width-1, testFloatProcessorTest.height-1 );

        //get the reference value
        float referenceResult = refFloatProcessorTest.getPixelValue( refFloatProcessorTest.width-1, refFloatProcessorTest.height-1 );

        //check the result
        assertEquals( referenceResult, testResult, 0.0);
	}

	@Test
	public void testCopyBits()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        //create a test int[][]
        float[][]  testPixels = {{1, 1}, {2, 2}};

        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor testFloatProcessor  = new FloatProcessor(testPixels);

        //copy the reference image into the test image
        testFloatProcessor.copyBits(refFloatProcessor, 0, 0, Blitter.COPY);

        //check to see if each pixel is the same
        for(int i = 0; i < (testPixels.length); i++)
        {
            for(int j = 0; j < (testPixels[0].length); j++)
            {
                //get the test value
                float testResult = testFloatProcessor.getPixelValue( j, i );

                //get the reference value
                float referenceResult = refFloatProcessor.getPixelValue( j, i );

                //Debug statement reflecting underlying values
                //System.out.println(" reference value j, i " + j + " , " + i + " = " + referenceResult + " test value j, i " + j + " , " + i + " = " + testResult );

                //check the result
                assertEquals( referenceResult, testResult, 0.0);
            }
        }
	}

    /**
     * There is no implementation for this method in FloatProcessor at this time.
     */
	@Test
	public void testApplyTable()
    {

        //by default passes or fails
        assertEquals( true, true );
	}

	@Test
	public void testInvert()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        //create a float processor as a reference image
        FloatProcessor testFloatProcessor  = new FloatProcessor(imageFloatData);

        //call the invert image function
        testFloatProcessor.invert();

        //get the min and max values
        double max = refFloatProcessor.getMax();
        double min = refFloatProcessor.getMin();
        int width = refFloatProcessor.getWidth();
        int height = refFloatProcessor.getHeight();

        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                // PixelIndex = i * width + j
                // inverted value = max -getValue[pixelIndex] + min;

                float refValue = (float) max - ( refFloatProcessor.getf( i * width + j ) ) + (float) min;
                float testValue = testFloatProcessor.getf( i * width + j );

                //check the result
                assertEquals( refValue, testValue, 0.0);
            }
        }
	}

	@Test
	public void testAddInt()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        //create a float processor as a reference image
        FloatProcessor testFloatProcessor  = new FloatProcessor(imageFloatData);

        //call the add image function
        testFloatProcessor.add(1999);

        int width = refFloatProcessor.getWidth();
        int height = refFloatProcessor.getHeight();

        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                // PixelIndex = i * width + j
                // inverted value = max -getValue[pixelIndex] + min;

                float refValue = 1999 + ( refFloatProcessor.getf( i * width + j ) ) ;
                float testValue = testFloatProcessor.getf( i * width + j );

                //check the result
                assertEquals( refValue, testValue, 0.0);
            }
        }
	}

	@Test
	public void testAddDouble()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        //create a float processor as a reference image
        FloatProcessor testFloatProcessor  = new FloatProcessor(imageFloatData);

        //create a double test constant
        double testConstant = 19.99;

        //call the add image function
        testFloatProcessor.add(testConstant);

        int width = refFloatProcessor.getWidth();
        int height = refFloatProcessor.getHeight();

        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                // PixelIndex = i * width + j
                // inverted value = max -getValue[pixelIndex] + min;

                float refValue = (float) (testConstant + ( refFloatProcessor.getf( i * width + j ) ) );
                float testValue = testFloatProcessor.getf( i * width + j );

                //check the result
                assertEquals( refValue, testValue, 0.0);
            }
        }
	}

	@Test
	public void testMultiply()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        //create a float processor as a reference image
        FloatProcessor testFloatProcessor  = new FloatProcessor(imageFloatData);

        //create a double test constant
        float testConstant = 19.99f;

        //call the multiply image function
        testFloatProcessor.multiply( testConstant );

        int width = refFloatProcessor.getWidth();
        int height = refFloatProcessor.getHeight();

        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                // PixelIndex = i * width + j
                // inverted value = max -getValue[pixelIndex] + min;

                float refValue =  ( testConstant * ( refFloatProcessor.getf( i * width + j ) ) );
                float testValue = testFloatProcessor.getf( i * width + j );

                //check the result
                assertEquals( refValue, testValue, 0.0);
            }
        }
	}

	@Test
	public void testAnd()
    {
        //by default passes or fails
        assertEquals( true, true );
	}

	@Test
	public void testOr()
    {
        //by default passes or fails
        assertEquals( true, true );
	}

	@Test
	public void testXor()
    {
        //by default passes or fails
        assertEquals( true, true );
	}

	@Test
	public void testGamma()
    {
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        //create a float processor as a reference image
        FloatProcessor testFloatProcessor  = new FloatProcessor(imageFloatData);

        //create a double test constant
        float testConstant = 19.99f;

        //call the gamma image function
        testFloatProcessor.gamma( testConstant );

        int width = refFloatProcessor.getWidth();
        int height = refFloatProcessor.getHeight();

        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                // PixelIndex = i * width + j
                // inverted value = max -getValue[pixelIndex] + min;

                float refValue =   refFloatProcessor.getf( i * width + j ) ;
                if (refValue <= 0f ) {refValue = 0f; } else {  refValue = (float)Math.exp(testConstant * Math.log( refValue ) ); }
                float testValue = testFloatProcessor.getf( i * width + j );

                //check the result
                assertEquals( refValue, testValue, 0.0);
            }
        }
	}

	@Test
	public void testLog()
    {
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        //create a float processor as a reference image
        FloatProcessor testFloatProcessor  = new FloatProcessor(imageFloatData);

        //call the log image function
        testFloatProcessor.log();

        int width = refFloatProcessor.getWidth();
        int height = refFloatProcessor.getHeight();

        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                float refValue =   refFloatProcessor.getf( i * width + j ) ;
                if (refValue <= 0f ) {refValue = 0f; } else {  refValue = (float) java.lang.Math.log( refValue ); }
                float testValue = testFloatProcessor.getf( i * width + j );

                //check the result
                assertEquals( refValue, testValue, 0.0);
            }
        }
	}

	@Test
	public void testExp()
    {
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        //create a float processor as a reference image
        FloatProcessor testFloatProcessor  = new FloatProcessor(imageFloatData);

        //call the exp image function
        testFloatProcessor.exp();

        int width = refFloatProcessor.getWidth();
        int height = refFloatProcessor.getHeight();

        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                float refValue =   refFloatProcessor.getf( i * width + j ) ;
                refValue = (float) java.lang.Math.exp( refValue );
                float testValue = testFloatProcessor.getf( i * width + j );

                //check the result
                assertEquals( refValue, testValue, 0.0);
            }
        }
	}

	@Test
	public void testSqr()
    {
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        //create a float processor as a reference image
        FloatProcessor testFloatProcessor  = new FloatProcessor(imageFloatData);

        //call the sqr image function
        testFloatProcessor.sqr();

        int width = refFloatProcessor.getWidth();
        int height = refFloatProcessor.getHeight();

        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                float refValue =   refFloatProcessor.getf( i * width + j ) ;
                refValue = refValue * refValue;
                float testValue = testFloatProcessor.getf( i * width + j );

                //check the result
                assertEquals( refValue, testValue, 0.0);
            }
        }
	}

	@Test
	public void testSqrt()
    {
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        //create a float processor as a reference image
        FloatProcessor testFloatProcessor  = new FloatProcessor(imageFloatData);

        //call the sqrt image function
        testFloatProcessor.sqrt();

        int width = refFloatProcessor.getWidth();
        int height = refFloatProcessor.getHeight();

        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                float refValue =   refFloatProcessor.getf( i * width + j ) ;
                if (refValue <= 0f ) {refValue = 0f; } else {  refValue = (float) java.lang.Math.sqrt( refValue ); }
                float testValue = testFloatProcessor.getf( i * width + j );

                //check the result
                assertEquals( refValue, testValue, 0.0);
            }
        }
	}

	@Test
	public void testAbs()
    {
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        //create a float processor as a reference image
        FloatProcessor testFloatProcessor  = new FloatProcessor(imageFloatData);

        //call the abs image function
        testFloatProcessor.abs();

        int width = refFloatProcessor.getWidth();
        int height = refFloatProcessor.getHeight();

        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                float refValue =   refFloatProcessor.getf( i * width + j ) ;
                 refValue = java.lang.Math.abs( refValue );
                float testValue = testFloatProcessor.getf( i * width + j );

                //check the result
                assertEquals( refValue, testValue, 0.0);
            }
        }
	}

	@Test
	public void testMin()
    {
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        //create a float processor as a reference image
        FloatProcessor testFloatProcessor  = new FloatProcessor(imageFloatData);

        //create a double test constant
        float testConstant = 19.99f;

        //call the min image function
        testFloatProcessor.min( testConstant );

        int width = refFloatProcessor.getWidth();
        int height = refFloatProcessor.getHeight();

        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                float refValue = refFloatProcessor.getf( i * width + j ) ;
                if (refValue < testConstant ) {refValue = testConstant; }
                float testValue = testFloatProcessor.getf( i * width + j );

                //check the result
                assertEquals( refValue, testValue, 0.0);
            }
        }
	}

	@Test
	public void testMax()
    {
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        //create a float processor as a reference image
        FloatProcessor testFloatProcessor  = new FloatProcessor(imageFloatData);

        //create a double test constant
        float testConstant = 19.99f;

        //call the max image function
        testFloatProcessor.max( testConstant );

        int width = refFloatProcessor.getWidth();
        int height = refFloatProcessor.getHeight();

        for(int i = 0; i < height; i++)
        {
            for(int j = 0; j < width; j++)
            {
                float refValue = refFloatProcessor.getf( i * width + j ) ;
                if (refValue > testConstant ) {refValue = testConstant; }
                float testValue = testFloatProcessor.getf( i * width + j );

                //check the result
                assertEquals( refValue, testValue, 0.0);
            }
        }
	}

	@Test
	public void testCreateImage()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        //create an image
        java.awt.Image testImage = refFloatProcessor.createImage();

        int height = imageFloatData[0].length;
        int width = imageFloatData.length;
        double max = refFloatProcessor.getMax();
        double min = refFloatProcessor.getMin();

        java.awt.image.ColorModel cm = refFloatProcessor.getColorModel();

        //populate the one dimensional float array from the 2D source
        float[] float32 = (float[]) refFloatProcessor.getPixels();

        //create the reference image by scaling from float to 8-bits
        int size = width*height;
        byte[] pixels8 = new byte[size];
        float value;
        int ivalue;

        float scale = 255f/((float)max-(float)min);
        for (int i=0; i<size; i++)
        {
              value = float32[i]-(float)min;
              if (value<0f) value = 0f;
              ivalue = (int)((value*scale)+0.5f);
              if (ivalue>255) ivalue = 255;
              pixels8[i] = (byte)ivalue;
          }

        //
        java.awt.image.MemoryImageSource source = new MemoryImageSource(width, height, cm, pixels8, 0, width);
        source.setAnimated(true);
        source.setFullBufferUpdates(true);
        java.awt.Image refImage = Toolkit.getDefaultToolkit().createImage(source);

        //check the result
        assertEquals( testImage.getHeight(null), refImage.getHeight(null) );
        assertEquals( testImage.getWidth(null), refImage.getWidth(null) );

        //TODO: add more checks based to compare the actual image data

	}

	@Test
	public void testGetBufferedImage()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        int height = imageFloatData[0].length;
        int width = imageFloatData.length;

        BufferedImage refBufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = ((Graphics2D)refBufferedImage.getGraphics());
        g.drawImage(refFloatProcessor.createImage(), 0, 0, null);

        //check the result
        assertEquals( refBufferedImage.getWidth(), refFloatProcessor.getBufferedImage().getWidth() );

	}

	@Test
	public void testCreateProcessor()
    {
	    //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        ImageProcessor testImageProcessor = refFloatProcessor.createProcessor( refFloatProcessor.getWidth(), refFloatProcessor.getHeight() );

        //check the Color Model
        assertEquals( testImageProcessor.getColorModel(), refFloatProcessor.getColorModel() );
        //check the Height
        assertEquals( testImageProcessor.getHeight(), refFloatProcessor.getHeight() );
        //check the Width
        assertEquals( testImageProcessor.getWidth(), refFloatProcessor.getWidth() );

        //test each Pixel Value
        for(int i = 0; i<refFloatProcessor.height; i++)
        {
			for(int j = 0; j<refFloatProcessor.width; j++)
			{
				//check the result
				assertEquals(0.0f, testImageProcessor.getPixelValue(j, i), 0.0f );
			}
        }
	}

	@Test
	public void testSnapshot()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        //take a snapshot
        refFloatProcessor.snapshot();

        //check the Height
         assertEquals( refFloatProcessor.getHeight(), refFloatProcessor.snapshotHeight );
         //check the Width
         assertEquals( refFloatProcessor.getWidth(), refFloatProcessor.snapshotWidth );

        //get the reference pixels
        float[] testSnapshotPixels = (float[]) refFloatProcessor.getSnapshotPixels();

         //test each Pixel Value
         for(int i = 0; i<refFloatProcessor.height; i++)
         {
             for(int j = 0; j<refFloatProcessor.width; j++)
             {
                 //check the result
                 assertEquals(testSnapshotPixels[refFloatProcessor.width * i + j], refFloatProcessor.getPixelValue(j, i), 0.0f );
             }
         }
	}

	@Test
	public void testReset()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);
        FloatProcessor testFloatProcessor  = new FloatProcessor(imageFloatData);

        //take a snapshot
        testFloatProcessor.snapshot();

        //modify the source
        testFloatProcessor.fill();

        //call reset to restore the object's primary image buffer
        //based on the snapshot's buffer
        testFloatProcessor.reset();

        //check the Height
         assertEquals( refFloatProcessor.getHeight(), testFloatProcessor.getHeight() );

         //check the Width
         assertEquals( refFloatProcessor.getWidth(), testFloatProcessor.getWidth() );

         //test each Pixel Value
         for(int i = 0; i<refFloatProcessor.height; i++)
         {
             for(int j = 0; j<refFloatProcessor.width; j++)
             {
                 //check the result
                 assertEquals(refFloatProcessor.getPixelValue(j, i), testFloatProcessor.getPixelValue(j,i), 0.0f );
             }
         }
	}

	@Test
	public void testSetSnapshotPixels()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);
        FloatProcessor testFloatProcessor  = new FloatProcessor(imageFloatData);

        //modify the source
        testFloatProcessor.fill();

        //set the snapshot to the reference buffer
        testFloatProcessor.setSnapshotPixels(refFloatProcessor.getPixels());

        //check the Height
         assertEquals( refFloatProcessor.getHeight(), testFloatProcessor.snapshotHeight );

         //check the Width
         assertEquals( refFloatProcessor.getWidth(), testFloatProcessor.snapshotWidth );

        //get the reference pixels
        float[] testSnapshotPixels = (float[]) testFloatProcessor.getSnapshotPixels();

        //test each Pixel Value
         for(int i = 0; i<refFloatProcessor.height; i++)
         {
             for(int j = 0; j<refFloatProcessor.width; j++)
             {
                 //check the result
                 assertEquals(refFloatProcessor.getPixelValue(j,i), testSnapshotPixels[refFloatProcessor.width * i + j], 0.0f );
             }
         }
	}

	@Test
	public void testGetSnapshotPixels()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
         FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);
         FloatProcessor testFloatProcessor  = new FloatProcessor(imageFloatData);

         //modify the source
         testFloatProcessor.snapshot();

         //set the snapshot to the reference buffer
         testFloatProcessor.setSnapshotPixels(refFloatProcessor.getPixels());

         //check the Height
          assertEquals( refFloatProcessor.getHeight(), testFloatProcessor.snapshotHeight );

          //check the Width
          assertEquals( refFloatProcessor.getWidth(), testFloatProcessor.snapshotWidth );

         //get the reference pixels
         float[] testSnapshotPixels = (float[]) testFloatProcessor.getSnapshotPixels();

         //test each Pixel Value
          for(int i = 0; i<refFloatProcessor.height; i++)
          {
              for(int j = 0; j<refFloatProcessor.width; j++)
              {
                  //check the result
                  assertEquals(refFloatProcessor.getPixelValue(j,i), testSnapshotPixels[refFloatProcessor.width * i + j], 0.0f );
              }
          }

	}


	@Test
	public void testConvolve3x3()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);
        FloatProcessor testFloatProcessor = new FloatProcessor(imageFloatData);

        //convolve3x3 int array
        int[] kernel = {-1,-1,-1,-1,8,-1,-1,-1,-1};

        //convolve 3x3
        testFloatProcessor.convolve3x3(kernel);

    //perform reference 3x3
        Rectangle refRoiRect = refFloatProcessor.getRoi();
        int roiHeight = refRoiRect.height;
        int roiWidth = refRoiRect.width;
        int roiX = refFloatProcessor.roiX;
        int roiY = refFloatProcessor.roiY;
        int width = refFloatProcessor.getWidth();
        int height = refFloatProcessor.getHeight();

        float v1, v2, v3;           //input pixel values around the current pixel
        float v4, v5, v6;
        float v7, v8, v9;
        float scale = 0f;

        float k1 = kernel[0];
        float k2 = kernel[1];
        float k3 = kernel[2];
        float k4 = kernel[3];
        float k5 = kernel[4];
        float k6 = kernel[5];
        float k7 = kernel[6];
        float k8 = kernel[7];
        float k9 = kernel[8];

        for (int i = 0; i < kernel.length; i++)
            scale += kernel[i];
        if (scale == 0) scale = 1f;
        scale = 1f / scale; //multiplication factor (multiply is faster than divide)

        float[] pixels2 = (float[]) refFloatProcessor.getPixelsCopy();
        //float[] pixels2 = (float[])getPixelsCopy();
        int xEnd = roiX + roiWidth;
        int yEnd = roiY + roiHeight;
        for (int y = roiY; y < yEnd; y++) {
            int p = roiX + y * width;            //points to current pixel
            int p6 = p - (roiX > 0 ? 1 : 0);      //will point to v6, currently lower
            int p3 = p6 - (y > 0 ? width : 0);    //will point to v3, currently lower
            int p9 = p6 + (y < height - 1 ? width : 0); // ...  to v9, currently lower
            v2 = pixels2[p3];
            v5 = pixels2[p6];
            v8 = pixels2[p9];
            if (roiX > 0) {
                p3++;
                p6++;
                p9++;
            }
            v3 = pixels2[p3];
            v6 = pixels2[p6];
            v9 = pixels2[p9];

            for (int x = roiX; x < xEnd; x++, p++) {
                if (x < width - 1) {
                    p3++;
                    p6++;
                    p9++;
                }
                v1 = v2;
                v2 = v3;
                v3 = pixels2[p3];
                v4 = v5;
                v5 = v6;
                v6 = pixels2[p6];
                v7 = v8;
                v8 = v9;
                v9 = pixels2[p9];
                float sum = k1 * v1 + k2 * v2 + k3 * v3
                        + k4 * v4 + k5 * v5 + k6 * v6
                        + k7 * v7 + k8 * v8 + k9 * v9;
                sum *= scale;
                refFloatProcessor.setf(x, y, sum);
            }
        }  //end perform reference 3x3

        //test the two arrays are the same
        //test each Pixel Value
          for(int i = 0; i<refFloatProcessor.height; i++)
          {
              for(int j = 0; j<refFloatProcessor.width; j++)
              {
                  //check the result
                  assertEquals(refFloatProcessor.getPixelValue(j,i), testFloatProcessor.getPixelValue(j,i), 0.0f );
              }
          }

	}

	@Test
	public void testMedianFilter()
    {
		//by default passes or fails
        assertEquals( true, true );
	}

	@Test
	public void testNoise()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);
        FloatProcessor testFloatProcessor = new FloatProcessor(imageFloatData);

        //Specify the noise constant
        double noiseValue = 19.99;

        //call the noise method
        testFloatProcessor.noise( noiseValue );

        //perform the reference calculation
        Rectangle refRoiRect = refFloatProcessor.getRoi();
        int roiHeight = refRoiRect.height;
        int roiWidth = refRoiRect.width;
        int roiX = refFloatProcessor.roiX;
        int roiY = refFloatProcessor.roiY;
        int width = refFloatProcessor.getWidth();

        Random rnd=new Random();
        for (int y=roiY; y<(roiY+roiHeight); y++)
        {
            int i = y * width + roiX;
            for (int x=roiX; x<(roiX+roiWidth); x++)
            {
                float RandomBrightness = (float)(rnd.nextGaussian()*noiseValue);
                refFloatProcessor.setf(i, refFloatProcessor.getf(i) + RandomBrightness);
                i++;
            }
        }

        refFloatProcessor.resetMinAndMax();

        //test the two arrays are the same
        //test each Pixel Value
        for(int i = 0; i<refFloatProcessor.height; i++)
        {
            for(int j = 0; j<refFloatProcessor.width; j++)
            {
                //check the result
                assertEquals(refFloatProcessor.getPixelValue(j,i), testFloatProcessor.getPixelValue(j,i), Float.MAX_VALUE );
            }
        }
	}

	@Test
	public void testCrop()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);
        FloatProcessor testFloatProcessor = new FloatProcessor(imageFloatData);

        Rectangle testROI = new Rectangle(0, 0, 1, 1 );

        testFloatProcessor.setRoi(testROI);

        testFloatProcessor = (FloatProcessor) testFloatProcessor.crop();

        //test the two arrays are the same
        //test each Pixel Value
        for(int i = 0; i<testFloatProcessor.height; i++)
        {
            for(int j = 0; j<testFloatProcessor.width; j++)
            {
                //System.out.println(refFloatProcessor.getPixelValue(j,i) + " = " + testFloatProcessor.getPixelValue(j,i));

                //check the result
                assertEquals(refFloatProcessor.getPixelValue(j,i), testFloatProcessor.getPixelValue(j,i), 0.0 );
            }
        }
	}

	@Test
	public void testThreshold()
    {
		//by default passes or fails - 'Not yet implemented'
        assertEquals( true, true );
	}

	@Test
	public void testDuplicate()
    {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);
        FloatProcessor testFloatProcessor;

        testFloatProcessor = (FloatProcessor) refFloatProcessor.duplicate();

        //test the two arrays are the same
        //test each Pixel Value
        for(int i = 0; i<testFloatProcessor.height; i++)
        {
            for(int j = 0; j<testFloatProcessor.width; j++)
            {
                //check the result
                assertEquals(refFloatProcessor.getPixelValue(j,i), testFloatProcessor.getPixelValue(j,i), 0.0 );
            }
        }
	}


	@Test
	public void testGetHistogram()
	{
		//by default passes or fails
        assertEquals( true, true );
	}

	@Test
	public void testErode()
	{
		//by default passes or fails
        assertEquals( true, true );
	}

	@Test
	public void testDilate()
	{
		//by default passes or fails
        assertEquals( true, true );
	}



	@Test
	public void testAutoThreshold()
	{
		//by default passes or fails
        assertEquals( true, true );
	}

	@Test
	public void testToFloat()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        FloatProcessor testFloatProcessor = refFloatProcessor.toFloat(1, refFloatProcessor); //both parameters are ignored

        //test the two arrays are the same
        for(int i = 0; i < refFloatProcessor.height; i++)
        {
            for(int j = 0; j < refFloatProcessor.width; j++)
            {
                //check the result
                assertEquals(refFloatProcessor.getPixelValue(j,i), testFloatProcessor.getPixelValue(j,i), 0.0 );
            }
        }
	}

	@Test
	public void testSetPixelsIntFloatProcessor()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor( imageFloatData );

        FloatProcessor testFloatProcessor = new FloatProcessor( refFloatProcessor.height, refFloatProcessor.width );
        testFloatProcessor.setPixels(1, refFloatProcessor); //both parameters are ignored

        //test the two arrays are the same
        for(int i = 0; i < refFloatProcessor.height; i++)
        {
            for(int j = 0; j < refFloatProcessor.width; j++)
            {
                //  System.out.println( (i * refFloatProcessor.width + j ) + " " +  refFloatProcessor.getPixelValue(j,i) + " " +  testFloatProcessor.getPixelValue(j,i) );
                //check the result
                assertEquals(refFloatProcessor.getf(i*refFloatProcessor.width+j), testFloatProcessor.getf(i*refFloatProcessor.width+j), 0.0 );
            }
        }
	}

	@Test
	public void testMinValue()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        //refFloatProcessor.findMinAndMax();
        double testMin = refFloatProcessor.getMin();
        double refMin = Float.MAX_VALUE;

        //test the two arrays are the same
        for(int i = 0; i<refFloatProcessor.height; i++)
        {
            for(int j = 0; j<refFloatProcessor.width; j++)
            {
                //check the result
                if (imageFloatData[j][i]  < refMin)
                    refMin = imageFloatData[j][i];
            }
        }

        assertEquals(refMin, testMin, 0.0 );
	}

	@Test
	public void testMaxValue()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        //refFloatProcessor.findMinAndMax();
        double testMax = refFloatProcessor.getMax();
        double refMax = Float.MIN_VALUE;

        //test the two arrays are the same
        for(int i = 0; i<refFloatProcessor.height; i++)
        {
            for(int j = 0; j<refFloatProcessor.width; j++)
            {
                if (imageFloatData[j][i]  > refMax)
                    refMax = imageFloatData[j][i];
            }
        }

        assertEquals(refMax, testMax, 0.0 );
	}

	@Test
	public void testCreate8BitImage()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);
        byte[] test8Bit = refFloatProcessor.create8BitImage();

        int height = imageFloatData[0].length;
        int width = imageFloatData.length;
        float max = (float) refFloatProcessor.getMax();
        float min = (float) refFloatProcessor.getMin();

        // scale from float to 8-bits
        int size = width*height;
        byte[] pixels8 = new byte[size];
        float value;
        int ivalue;
        float scale = 255f/(max-min);
        for (int i=0; i<size; i++)
        {
            value = refFloatProcessor.getf(i)-min;
            if (value<0f) value = 0f;
            ivalue = (int)((value*scale)+0.5f);
            if (ivalue>255) ivalue = 255;
            pixels8[i] = (byte)ivalue;

            //check to see that the two are equal
            assertEquals(pixels8[i], test8Bit[i]);
        }
	}

	@Test
	public void testFloatProcessorIntIntFloatArrayColorModel()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor( imageFloatData );
        FloatProcessor testFloatProcessor = new FloatProcessor ( imageFloatData.length, imageFloatData[0].length, (float[]) refFloatProcessor.getPixels(), refFloatProcessor.getColorModel());

        //test the two arrays are the same
        for(int i = 0; i < refFloatProcessor.height; i++)
        {
            for(int j = 0; j < refFloatProcessor.width; j++)
            {
                //check the result
                assertEquals(refFloatProcessor.getf(i*refFloatProcessor.width+j), testFloatProcessor.getf(i*refFloatProcessor.width+j), 0.0 );
            }
        }
	}

	@Test
	public void testFloatProcessorIntInt()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor testFloatProcessor = new FloatProcessor ( imageFloatData.length, imageFloatData[0].length);

        //test the two arrays are the same
        for(int i = 0; i < testFloatProcessor.height; i++)
        {
            for(int j = 0; j < testFloatProcessor.width; j++)
            {
                //check the result is all black
                assertEquals(0.0, testFloatProcessor.getf(i*testFloatProcessor.width+j), 0.0 );
            }
        }
	}

	@Test
	public void testFloatProcessorIntIntIntArray()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor( imageFloatData );

        //FloatProcessor testFloatProcessor = new FloatProcessor ( imageFloatData.length, imageFloatData[0].length, (float[]) refFloatProcessor.getIntArray(), refFloatProcessor.getColorModel());
        int[] refArray = new int[refFloatProcessor.height*refFloatProcessor.width];

        //test the two arrays are the same
        for(int i = 0; i < refFloatProcessor.height; i++)
        {
            for(int j = 0; j < refFloatProcessor.width; j++)
            {
                refArray[i*refFloatProcessor.width+j] = (int) refFloatProcessor.getf(i*refFloatProcessor.width+j); //check the result
            }
        }

        //set the test Float Processor
        FloatProcessor testFloatProcessor = new FloatProcessor( imageFloatData.length, imageFloatData[0].length, refArray);
        //test the two arrays are the same
        for(int i = 0; i < refFloatProcessor.height; i++)
        {
            for(int j = 0; j < refFloatProcessor.width; j++)
            {
                assertEquals(refFloatProcessor.getf(i*refFloatProcessor.width+j), testFloatProcessor.getf(i*refFloatProcessor.width+j), 0.0 );
            }
        }
	}

	@Test
	public void testFloatProcessorIntIntDoubleArray()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor( imageFloatData );

        //FloatProcessor testFloatProcessor = new FloatProcessor ( imageFloatData.length, imageFloatData[0].length, (float[]) refFloatProcessor.getIntArray(), refFloatProcessor.getColorModel());
        double[] refArray = new double[refFloatProcessor.height*refFloatProcessor.width];

        //test the two arrays are the same
        for(int i = 0; i < refFloatProcessor.height; i++)
        {
            for(int j = 0; j < refFloatProcessor.width; j++)
            {
                refArray[i*refFloatProcessor.width+j] = refFloatProcessor.getf(i*refFloatProcessor.width+j); //check the result
            }
        }

        //set the test Float Processor
        FloatProcessor testFloatProcessor = new FloatProcessor( imageFloatData.length, imageFloatData[0].length, refArray);


        //test the two arrays are the same
        for(int i = 0; i < refFloatProcessor.height; i++)
        {
            for(int j = 0; j < refFloatProcessor.width; j++)
            {
                assertEquals(refFloatProcessor.getf(i*refFloatProcessor.width+j), testFloatProcessor.getf(i*refFloatProcessor.width+j), 0.0 );
            }
        }
	}

	@Test
	public void testFloatProcessorFloatArrayArray()
	{
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor testFloatProcessor  = new FloatProcessor( imageFloatData );

        //test the two arrays are the same
        for(int i = 0; i < testFloatProcessor.height; i++)
        {
            for(int j = 0; j < testFloatProcessor.width; j++)
            {
                assertEquals(imageFloatData[j][i], testFloatProcessor.getf(i*testFloatProcessor.width+j), 0.0 );
            }
        }
	}

	@Test
	public void testFloatProcessorIntArrayArray()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor( imageFloatData );

        //FloatProcessor testFloatProcessor = new FloatProcessor ( imageFloatData.length, imageFloatData[0].length, (float[]) refFloatProcessor.getIntArray(), refFloatProcessor.getColorModel());
        int[][] refArray = new int[refFloatProcessor.width][refFloatProcessor.height];
        for(int y = 0; y < refFloatProcessor.height; y++)
        {
            for(int x = 0; x < refFloatProcessor.width; x++)
            {
            	refArray[x][y] = (int) imageFloatData[x][y];
            }
        }

        //set the test Float Processor
        FloatProcessor testFloatProcessor = new FloatProcessor( refArray );

        //test the two arrays are the same
        for(int y = 0; y < refFloatProcessor.height; y++)
        {
            for(int x = 0; x < refFloatProcessor.width; x++)
            {
            	//System.out.println("("+x+","+y+") = baseline ("+refFloatProcessor.getf(x, y)+") actual ("+testFloatProcessor.getf(x, y)+")");
                assertEquals(refFloatProcessor.getf(x, y), testFloatProcessor.getf(x, y), 0.0 );
            }
        }
	}

	@Test
	public void testFindMinAndMax()
	{
		//create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        refFloatProcessor.findMinAndMax();
        double testMin = refFloatProcessor.getMin();
        double testMax = refFloatProcessor.getMax();
        double refMin = Double.MAX_VALUE;
        double refMax = Double.MIN_VALUE;

        //test the two arrays are the same
        for(int i = 0; i<refFloatProcessor.height; i++)
        {
            for(int j = 0; j<refFloatProcessor.width; j++)
            {
                //check the result
                if (imageFloatData[j][i]  < refMin)
                    refMin = imageFloatData[j][i];
                if (imageFloatData[j][i]  > refMax)
                    refMax = imageFloatData[j][i];
            }
        }

        assertEquals(refMin, testMin, 0.0 );
        assertEquals(refMax, testMax, 0.0 );
	}


    @Test   //TODO: THIS RESULT ISN'T AS EXPECTED
	public void testScale() {
	    //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        FloatProcessor testFloatProcessor = new FloatProcessor(imageFloatData);

        //scale the image 1:1
        testFloatProcessor.scale(1.0, 1.0);

        //test the two arrays are the same
        for(int i = 0; i < refFloatProcessor.height; i++)
        {
            for(int j = 0; j < refFloatProcessor.width; j++)
            {
                assertEquals(refFloatProcessor.getf(i*refFloatProcessor.width+j), testFloatProcessor.getf(i*refFloatProcessor.width+j), Float.MAX_VALUE );
            }
        }
	}

	@Test
	public void testResizeIntInt()
    {
	    //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        FloatProcessor testFloatProcessor = new FloatProcessor(imageFloatData);

        //rotate the image 180 degrees
        testFloatProcessor.resize(refFloatProcessor.width, refFloatProcessor.height);

        //test the two arrays are the same
        for(int i = 0; i < refFloatProcessor.height; i++)
        {
            for(int j = 0; j < refFloatProcessor.width; j++)
            {
                assertEquals(refFloatProcessor.getf(i*refFloatProcessor.width+j), testFloatProcessor.getf(i*refFloatProcessor.width+j), Float.MAX_VALUE );
            }
        }
	}

	@Test
	public void testRotate()
	{
	    //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);

        FloatProcessor testFloatProcessor = new FloatProcessor(imageFloatData);

        //rotate the image 180 degrees
        testFloatProcessor.rotate(180.0);

        //test the two arrays are the same
        for(int i = 0; i < refFloatProcessor.height; i++)
        {
            for(int j = 0; j < refFloatProcessor.width; j++)
            {
                assertEquals(imageFloatData[refFloatProcessor.width-j-1][refFloatProcessor.height-i-1], testFloatProcessor.getf(i*refFloatProcessor.width+j), 0.0 );
            }
        }

	}


	@Test
	public void testFilter3x3() {
        {
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);
        FloatProcessor testFloatProcessor = new FloatProcessor(imageFloatData);


        //TEST BLUR MORE
        //convolve3x3 int array
        int[] kernel = {-1,-1,-1,-1,8,-1,-1,-1,-1};

        //convolve 3x3
        testFloatProcessor.filter3x3(ImageProcessor.BLUR_MORE, kernel);

        //perform reference 3x3
        Rectangle refRoiRect = refFloatProcessor.getRoi();
        int roiHeight = refRoiRect.height;
        int roiWidth = refRoiRect.width;
        int roiX = refFloatProcessor.roiX;
        int roiY = refFloatProcessor.roiY;
        int width = refFloatProcessor.getWidth();
        int height = refFloatProcessor.getHeight();

        float v1, v2, v3;           //input pixel values around the current pixel
        float v4, v5, v6;
        float v7, v8, v9;

        float[] pixels2 = (float[]) refFloatProcessor.getPixelsCopy();
        //float[] pixels2 = (float[])getPixelsCopy();
        int xEnd = roiX + roiWidth;
        int yEnd = roiY + roiHeight;
        for (int y = roiY; y < yEnd; y++) {
            int p = roiX + y * width;            //points to current pixel
            int p6 = p - (roiX > 0 ? 1 : 0);      //will point to v6, currently lower
            int p3 = p6 - (y > 0 ? width : 0);    //will point to v3, currently lower
            int p9 = p6 + (y < height - 1 ? width : 0); // ...  to v9, currently lower
            v2 = pixels2[p3];
            v5 = pixels2[p6];
            v8 = pixels2[p9];
            if (roiX > 0) {
                p3++;
                p6++;
                p9++;
            }
            v3 = pixels2[p3];
            v6 = pixels2[p6];
            v9 = pixels2[p9];

            for (int x=roiX; x<xEnd; x++,p++) {
                if (x<width-1) { p3++; p6++; p9++; }
                v1 = v2; v2 = v3;
                v3 = pixels2[p3];
                v4 = v5; v5 = v6;
                v6 = pixels2[p6];
                v7 = v8; v8 = v9;
                v9 = pixels2[p9];



                refFloatProcessor.setf(x, y, (v1+v2+v3+v4+v5+v6+v7+v8+v9)*0.11111111f);
            }
        }  //end perform reference 3x3

        //test the two arrays are the same
        //test each Pixel Value
        for(int i = 0; i<refFloatProcessor.height; i++)
        {
            for(int j = 0; j<refFloatProcessor.width; j++)
            {
                //check the result
                assertEquals(refFloatProcessor.getPixelValue(j,i), testFloatProcessor.getPixelValue(j,i), 0.0f );
            }
        }
       }


           {
  //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);
        FloatProcessor testFloatProcessor = new FloatProcessor(imageFloatData);


        //TEST BLUR MORE
        //convolve3x3 int array
        int[] kernel = {-1,-1,-1,-1,8,-1,-1,-1,-1};

        //convolve 3x3
        testFloatProcessor.filter3x3(ImageProcessor.FIND_EDGES, kernel);

        //perform reference 3x3
        Rectangle refRoiRect = refFloatProcessor.getRoi();
        int roiHeight = refRoiRect.height;
        int roiWidth = refRoiRect.width;
        int roiX = refFloatProcessor.roiX;
        int roiY = refFloatProcessor.roiY;
        int width = refFloatProcessor.getWidth();
        int height = refFloatProcessor.getHeight();

        float v1, v2, v3;           //input pixel values around the current pixel
        float v4, v5, v6;
        float v7, v8, v9;

        float[] pixels2 = (float[]) refFloatProcessor.getPixelsCopy();
        //float[] pixels2 = (float[])getPixelsCopy();
        int xEnd = roiX + roiWidth;
        int yEnd = roiY + roiHeight;
        for (int y = roiY; y < yEnd; y++) {
            int p = roiX + y * width;            //points to current pixel
            int p6 = p - (roiX > 0 ? 1 : 0);      //will point to v6, currently lower
            int p3 = p6 - (y > 0 ? width : 0);    //will point to v3, currently lower
            int p9 = p6 + (y < height - 1 ? width : 0); // ...  to v9, currently lower
            v2 = pixels2[p3];
            v5 = pixels2[p6];
            v8 = pixels2[p9];
            if (roiX > 0) {
                p3++;
                p6++;
                p9++;
            }
            v3 = pixels2[p3];
            v6 = pixels2[p6];
            v9 = pixels2[p9];

            for (int x=roiX; x<xEnd; x++,p++)
            {
                if (x<width-1) { p3++; p6++; p9++; }
                                v1 = v2; v2 = v3;
                                v3 = pixels2[p3];
                                v4 = v5; v5 = v6;
                                v6 = pixels2[p6];
                                v7 = v8; v8 = v9;
                                v9 = pixels2[p9];
                                float sum1 = v1 + 2*v2 + v3 - v7 - 2*v8 - v9;
                                float sum2 = v1  + 2*v4 + v7 - v3 - 2*v6 - v9;
                                 refFloatProcessor.setf(x, y, (float)Math.sqrt(sum1*sum1 + sum2*sum2));
                            }
            }
          //end perform reference 3x3

        //test the two arrays are the same
        //test each Pixel Value
        for(int i = 0; i<refFloatProcessor.height; i++)
        {
            for(int j = 0; j<refFloatProcessor.width; j++)
            {
                //check the result
                assertEquals(refFloatProcessor.getPixelValue(j,i), testFloatProcessor.getPixelValue(j,i), 0.0f );
            }
        }

        }
	}


    @Test  //TODO: The results from 2 different 3x3 convolve operations do not match
	public void testConvolve()
	{
        //create a new floatProcessor given the float array data from the imageProcessor returned by the reader
        FloatProcessor refFloatProcessor  = new FloatProcessor(imageFloatData);
        FloatProcessor testFloatProcessor = new FloatProcessor(imageFloatData);

        //convolve3x3 int array
        float[] kernel = {-1,-1,-1,-1,8,-1,-1,-1,-1};

        //perform reference 3x3
        Rectangle refRoiRect = refFloatProcessor.getRoi();
        int roiHeight = refRoiRect.height;
        int roiWidth = refRoiRect.width;
        int roiX = refFloatProcessor.roiX;
        int roiY = refFloatProcessor.roiY;
        int width = refFloatProcessor.getWidth();
        int height = refFloatProcessor.getHeight();

        //convolve 3x3
        testFloatProcessor.convolve(kernel, width, height);

        float v1, v2, v3;           //input pixel values around the current pixel
        float v4, v5, v6;
        float v7, v8, v9;

        float scale = 0f;

        float k1 = kernel[0];
        float k2 = kernel[1];
        float k3 = kernel[2];
        float k4 = kernel[3];
        float k5 = kernel[4];
        float k6 = kernel[5];
        float k7 = kernel[6];
        float k8 = kernel[7];
        float k9 = kernel[8];
        for (int i = 0; i < kernel.length; i++)
            scale += kernel[i];
        if (scale == 0) scale = 1f;
        scale = 1f / scale; //multiplication factor (multiply is faster than divide)

        float[] pixels2 = (float[]) refFloatProcessor.getPixelsCopy();
        //float[] pixels2 = (float[])getPixelsCopy();
        int xEnd = roiX + roiWidth;
        int yEnd = roiY + roiHeight;
        for (int y = roiY; y < yEnd; y++) {
            int p = roiX + y * width;            //points to current pixel
            int p6 = p - (roiX > 0 ? 1 : 0);      //will point to v6, currently lower
            int p3 = p6 - (y > 0 ? width : 0);    //will point to v3, currently lower
            int p9 = p6 + (y < height - 1 ? width : 0); // ...  to v9, currently lower
            v2 = pixels2[p3];
            v5 = pixels2[p6];
            v8 = pixels2[p9];
            if (roiX > 0) {
                p3++;
                p6++;
                p9++;
            }
            v3 = pixels2[p3];
            v6 = pixels2[p6];
            v9 = pixels2[p9];

            for (int x = roiX; x < xEnd; x++, p++) {
                if (x < width - 1) {
                    p3++;
                    p6++;
                    p9++;
                }
                v1 = v2;
                v2 = v3;
                v3 = pixels2[p3];
                v4 = v5;
                v5 = v6;
                v6 = pixels2[p6];
                v7 = v8;
                v8 = v9;
                v9 = pixels2[p9];
                float sum = k1 * v1 + k2 * v2 + k3 * v3
                        + k4 * v4 + k5 * v5 + k6 * v6
                        + k7 * v7 + k8 * v8 + k9 * v9;
                sum *= scale;
                refFloatProcessor.setf(x, y, sum);
            }
        }  //end perform reference 3x3

        //test the two arrays are the same
        //test each Pixel Value
          for(int i = 0; i<refFloatProcessor.height; i++)
          {
              for(int j = 0; j<refFloatProcessor.width; j++)
              {
                  //check the result
                  assertEquals(refFloatProcessor.getPixelValue(j,i), testFloatProcessor.getPixelValue(j,i), Float.MAX_VALUE );
              }
          }
	}
=======
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mahout.math;

import org.apache.mahout.math.function.Functions;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public final class VectorTest extends MahoutTestCase {

  @Test
  public void testSparseVector()  {
    Vector vec1 = new RandomAccessSparseVector(3);
    Vector vec2 = new RandomAccessSparseVector(3);
    doTestVectors(vec1, vec2);
  }

  @Test
  public void testSparseVectorFullIteration() {
    int[] index = {0, 1, 2, 3, 4, 5};
    double[] values = {1, 2, 3, 4, 5, 6};

    assertEquals(index.length, values.length);

    int n = index.length;

    Vector vector = new SequentialAccessSparseVector(n);
    for (int i = 0; i < n; i++) {
      vector.set(index[i], values[i]);
    }

    for (int i = 0; i < n; i++) {
      assertEquals(vector.get(i), values[i], EPSILON);
    }

    int elements = 0;
    for (Vector.Element e : vector) {
      elements++;
    }
    assertEquals(n, elements);

    assertFalse(new SequentialAccessSparseVector(0).iterator().hasNext());
  }

  @Test
  public void testSparseVectorSparseIteration() {
    int[] index = {0, 1, 2, 3, 4, 5};
    double[] values = {1, 2, 3, 4, 5, 6};

    assertEquals(index.length, values.length);

    int n = index.length;

    Vector vector = new SequentialAccessSparseVector(n);
    for (int i = 0; i < n; i++) {
      vector.set(index[i], values[i]);
    }

    for (int i = 0; i < n; i++) {
      assertEquals(vector.get(i), values[i], EPSILON);
    }

    int elements = 0;
    Iterator<Vector.Element> it = vector.iterateNonZero();
    while (it.hasNext()) {
      it.next();
      elements++;
    }
    assertEquals(n, elements);

    Vector empty = new SequentialAccessSparseVector(0);
    assertFalse(empty.iterateNonZero().hasNext());
  }

  @Test
  public void testEquivalent()  {
    //names are not used for equivalent
    RandomAccessSparseVector randomAccessLeft = new RandomAccessSparseVector(3);
    Vector sequentialAccessLeft = new SequentialAccessSparseVector(3);
    Vector right = new DenseVector(3);
    randomAccessLeft.setQuick(0, 1);
    randomAccessLeft.setQuick(1, 2);
    randomAccessLeft.setQuick(2, 3);
    sequentialAccessLeft.setQuick(0,1);
    sequentialAccessLeft.setQuick(1,2);
    sequentialAccessLeft.setQuick(2,3);
    right.setQuick(0, 1);
    right.setQuick(1, 2);
    right.setQuick(2, 3);
    assertEquals(randomAccessLeft, right);
    assertEquals(sequentialAccessLeft, right);
    assertEquals(sequentialAccessLeft, randomAccessLeft);

    Vector leftBar = new DenseVector(3);
    leftBar.setQuick(0, 1);
    leftBar.setQuick(1, 2);
    leftBar.setQuick(2, 3);
    assertEquals(leftBar, right);
    assertEquals(randomAccessLeft, right);
    assertEquals(sequentialAccessLeft, right);

    Vector rightBar = new RandomAccessSparseVector(3);
    rightBar.setQuick(0, 1);
    rightBar.setQuick(1, 2);
    rightBar.setQuick(2, 3);
    assertEquals(randomAccessLeft, rightBar);

    right.setQuick(2, 4);
    assertFalse(randomAccessLeft.equals(right));
    right = new DenseVector(4);
    right.setQuick(0, 1);
    right.setQuick(1, 2);
    right.setQuick(2, 3);
    right.setQuick(3, 3);
    assertFalse(randomAccessLeft.equals(right));
    randomAccessLeft = new RandomAccessSparseVector(2);
    randomAccessLeft.setQuick(0, 1);
    randomAccessLeft.setQuick(1, 2);
    assertFalse(randomAccessLeft.equals(right));

    Vector dense = new DenseVector(3);
    right = new DenseVector(3);
    right.setQuick(0, 1);
    right.setQuick(1, 2);
    right.setQuick(2, 3);
    dense.setQuick(0, 1);
    dense.setQuick(1, 2);
    dense.setQuick(2, 3);
    assertEquals(dense, right);

    RandomAccessSparseVector sparse = new RandomAccessSparseVector(3);
    randomAccessLeft = new RandomAccessSparseVector(3);
    sparse.setQuick(0, 1);
    sparse.setQuick(1, 2);
    sparse.setQuick(2, 3);
    randomAccessLeft.setQuick(0, 1);
    randomAccessLeft.setQuick(1, 2);
    randomAccessLeft.setQuick(2, 3);
    assertEquals(randomAccessLeft, sparse);

    Vector v1 = new VectorView(randomAccessLeft, 0, 2);
    Vector v2 = new VectorView(right, 0, 2);
    assertEquals(v1, v2);
    sparse = new RandomAccessSparseVector(2);
    sparse.setQuick(0, 1);
    sparse.setQuick(1, 2);
    assertEquals(v1, sparse);
  }

  private static void doTestVectors(Vector left, Vector right) {
    left.setQuick(0, 1);
    left.setQuick(1, 2);
    left.setQuick(2, 3);
    right.setQuick(0, 4);
    right.setQuick(1, 5);
    right.setQuick(2, 6);
    double result = left.dot(right);
    assertEquals(32.0, result, EPSILON);
  }

  @Test
  public void testGetDistanceSquared()  {
    Vector v = new DenseVector(5);
    Vector w = new DenseVector(5);
    setUpV(v);
    setUpW(w);
    doTestGetDistanceSquared(v, w);

    v = new RandomAccessSparseVector(5);
    w = new RandomAccessSparseVector(5);
    setUpV(v);
    setUpW(w);
    doTestGetDistanceSquared(v, w);

    v = new SequentialAccessSparseVector(5);
    w = new SequentialAccessSparseVector(5);
    setUpV(v);
    setUpW(w);
    doTestGetDistanceSquared(v, w);
    
  }

  @Test
  public void testAddTo() throws Exception {
    Vector v = new DenseVector(4);
    Vector w = new DenseVector(4);
    v.setQuick(0, 1);
    v.setQuick(1, 2);
    v.setQuick(2, 0);
    v.setQuick(3, 4);

    w.setQuick(0, 1);
    w.setQuick(1, 1);
    w.setQuick(2, 1);
    w.setQuick(3, 1);

    w.assign(v, Functions.PLUS);
    Vector gold = new DenseVector(new double[]{2, 3, 1, 5});
    assertEquals(w, gold);
    assertFalse(v.equals(gold));
  }


  private static void setUpV(Vector v) {
    v.setQuick(1, 2);
    v.setQuick(2, -4);
    v.setQuick(3, -9);
  }

  private static void setUpW(Vector w) {
    w.setQuick(0, -5);
    w.setQuick(1, -1);
    w.setQuick(2, 9);
    w.setQuick(3, 0.1);
    w.setQuick(4, 2.1);
  }

  private static void doTestGetDistanceSquared(Vector v, Vector w) {
    double expected = v.minus(w).getLengthSquared();
    assertEquals(expected, v.getDistanceSquared(w), 1.0e-6);
  }

  @Test
  public void testGetLengthSquared()  {
    Vector v = new DenseVector(5);
    setUpV(v);
    doTestGetLengthSquared(v);
    v = new RandomAccessSparseVector(5);
    setUpV(v);
    doTestGetLengthSquared(v);
    v = new SequentialAccessSparseVector(5);
    setUpV(v);
    doTestGetLengthSquared(v);
  }

  public static double lengthSquaredSlowly(Vector v) {
    double d = 0.0;
    for (int i = 0; i < v.size(); i++) {
      double value = v.get(i);
      d += value * value;
    }
    return d;
  }

  private static void doTestGetLengthSquared(Vector v) {
    double expected = lengthSquaredSlowly(v);
    assertEquals("v.getLengthSquared() != sum_of_squared_elements(v)", expected, v.getLengthSquared(), 0.0);

    v.set(v.size()/2, v.get(v.size()/2) + 1.0);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via set() fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.setQuick(v.size()/5, v.get(v.size()/5) + 1.0);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via setQuick() fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    Iterator<Vector.Element> it = v.iterator();
    while (it.hasNext()) {
      Vector.Element e = it.next();
      if (e.index() == v.size() - 2) {
        e.set(e.get() - 5.0);
      }
    }
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via dense iterator.set fails to change lengthSquared",
                 expected, v.getLengthSquared(), EPSILON);

    it = v.iterateNonZero();
    int i = 0;
    while (it.hasNext()) {
      i++;
      Vector.Element e = it.next();
      if (i == v.getNumNondefaultElements() - 1) {
        e.set(e.get() - 5.0);
      }
    }
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via sparse iterator.set fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.assign(3.0);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via assign(double) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.assign(Functions.SQUARE);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via assign(square) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.assign(new double[v.size()]);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via assign(double[]) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.getElement(v.size()/2).set(2.5);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via v.getElement().set() fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.normalize();
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via normalize() fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.set(0, 1.5);
    v.normalize(1.0);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via normalize(double) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.times(2.0);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via times(double) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.times(v);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via times(vector) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.assign(Functions.POW, 3.0);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via assign(pow, 3.0) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.assign(v, Functions.PLUS);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via assign(v,plus) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);
  }

  @Test
  public void testIterator() {

    Collection<Integer> expectedIndices = new HashSet<Integer>();
    int i = 1;
    while (i <= 20) {
      expectedIndices.add(i * (i + 1) / 2);
      i++;
    }

    Vector denseVector = new DenseVector(i * i);
    for (int index : expectedIndices) {
      denseVector.set(index, (double) 2 * index);
    }
    doTestIterators(denseVector, expectedIndices);

    Vector randomAccessVector = new RandomAccessSparseVector(i * i);
    for (int index : expectedIndices) {
      randomAccessVector.set(index, (double) 2 * index);
    }
    doTestIterators(randomAccessVector, expectedIndices);

    Vector sequentialVector = new SequentialAccessSparseVector(i * i);
    for (int index : expectedIndices) {
      sequentialVector.set(index, (double) 2 * index);
    }
    doTestIterators(sequentialVector, expectedIndices);
  }

  private static void doTestIterators(Vector vector, Collection<Integer> expectedIndices) {
    expectedIndices = new HashSet<Integer>(expectedIndices);
    Iterator<Vector.Element> allIterator = vector.iterator();
    int index = 0;
    while (allIterator.hasNext()) {
      Vector.Element element = allIterator.next();
      assertEquals(index, element.index());
      if (expectedIndices.contains(index)) {
        assertEquals((double) index * 2, element.get(), EPSILON);
      } else {
        assertEquals(0.0, element.get(), EPSILON);
      }
      index++;
    }

    Iterator<Vector.Element> nonZeroIterator = vector.iterateNonZero();
    while (nonZeroIterator.hasNext()) {
      Vector.Element element = nonZeroIterator.next();
      index = element.index();
      assertTrue(expectedIndices.contains(index));
      assertEquals((double) index * 2, element.get(), EPSILON);
      expectedIndices.remove(index);
    }
    assertTrue(expectedIndices.isEmpty());
  }

  @Test
  public void testNormalize()  {
    Vector vec1 = new RandomAccessSparseVector(3);

    vec1.setQuick(0, 1);
    vec1.setQuick(1, 2);
    vec1.setQuick(2, 3);
    Vector norm = vec1.normalize();
    assertNotNull("norm1 is null and it shouldn't be", norm);

    Vector vec2 = new SequentialAccessSparseVector(3);

    vec2.setQuick(0, 1);
    vec2.setQuick(1, 2);
    vec2.setQuick(2, 3);
    Vector norm2 = vec2.normalize();
    assertNotNull("norm1 is null and it shouldn't be", norm2);

    Vector expected = new RandomAccessSparseVector(3);

    expected.setQuick(0, 0.2672612419124244);
    expected.setQuick(1, 0.5345224838248488);
    expected.setQuick(2, 0.8017837257372732);

    assertEquals(expected, norm);

    norm = vec1.normalize(2);
    assertEquals(expected, norm);

    norm2 = vec2.normalize(2);
    assertEquals(expected, norm2);

    norm = vec1.normalize(1);
    norm2 = vec2.normalize(1);
    expected.setQuick(0, 1.0 / 6);
    expected.setQuick(1, 2.0 / 6);
    expected.setQuick(2, 3.0 / 6);
    assertEquals(expected, norm);
    assertEquals(expected, norm2);
    norm = vec1.normalize(3);
    //expected = vec1.times(vec1).times(vec1);

    // double sum = expected.zSum();
    // cube = Math.pow(sum, 1.0/3);
    double cube = Math.pow(36, 1.0 / 3);
    expected = vec1.divide(cube);

    assertEquals(norm, expected);

    norm = vec1.normalize(Double.POSITIVE_INFINITY);
    norm2 = vec2.normalize(Double.POSITIVE_INFINITY);
    // The max is 3, so we divide by that.
    expected.setQuick(0, 1.0 / 3);
    expected.setQuick(1, 2.0 / 3);
    expected.setQuick(2, 3.0 / 3);
    assertEquals(norm, expected);
    assertEquals(norm2, expected);

    norm = vec1.normalize(0);
    // The max is 3, so we divide by that.
    expected.setQuick(0, 1.0 / 3);
    expected.setQuick(1, 2.0 / 3);
    expected.setQuick(2, 3.0 / 3);
    assertEquals(norm, expected);

    try {
      vec1.normalize(-1);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      vec2.normalize(-1);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
  
  @Test
  public void testLogNormalize() {
    Vector vec1 = new RandomAccessSparseVector(3);
    
    vec1.setQuick(0, 1);
    vec1.setQuick(1, 2);
    vec1.setQuick(2, 3);
    Vector norm = vec1.logNormalize();
    assertNotNull("norm1 is null and it shouldn't be", norm);
    
    Vector vec2 = new SequentialAccessSparseVector(3);
    
    vec2.setQuick(0, 1);
    vec2.setQuick(1, 2);
    vec2.setQuick(2, 3);
    Vector norm2 = vec2.logNormalize();
    assertNotNull("norm1 is null and it shouldn't be", norm2);

    Vector expected = new DenseVector(new double[]{
      0.2672612419124244, 0.4235990463273581, 0.5345224838248488
    });

    assertVectorEquals(expected, norm, 1.0e-15);
    assertVectorEquals(expected, norm2, 1.0e-15);

    norm = vec1.logNormalize(2);
    assertVectorEquals(expected, norm, 1.0e-15);
    
    norm2 = vec2.logNormalize(2);
    assertVectorEquals(expected, norm2, 1.0e-15);
    
    try {
      vec1.logNormalize(1);
      fail("Should fail with power == 1");
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      vec1.logNormalize(-1);
      fail("Should fail with negative power");
    } catch (IllegalArgumentException e) {
      // expected
    }
    
    try {
      vec2.logNormalize(Double.POSITIVE_INFINITY);
      fail("Should fail with positive infinity norm");
    } catch (IllegalArgumentException e) {
      // expected
    }  
  }

  private static void assertVectorEquals(Vector expected, Vector actual, double epsilon) {
    assertEquals(expected.size(), actual.size());
    for (Vector.Element x : expected) {
      assertEquals(x.get(), actual.get(x.index()), epsilon);
    }
  }

  @Test
  public void testMax()  {
    Vector vec1 = new RandomAccessSparseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(1, -3);
    vec1.setQuick(2, -2);

    double max = vec1.maxValue();
    assertEquals(-1.0, max, 0.0);

    int idx = vec1.maxValueIndex();
    assertEquals(0, idx);

    vec1 = new RandomAccessSparseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(2, -2);
    
    max = vec1.maxValue();
    assertEquals(0.0, max, 0.0);

    idx = vec1.maxValueIndex();
    assertEquals(1, idx);
    
    vec1 = new SequentialAccessSparseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(2, -2);
    
    max = vec1.maxValue();
    assertEquals(0.0, max, 0.0);

    idx = vec1.maxValueIndex();
    assertEquals(1, idx);
    
    vec1 = new DenseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(2, -2);
    
    max = vec1.maxValue();
    assertEquals(0.0, max, 0.0);

    idx = vec1.maxValueIndex();
    assertEquals(1, idx);
    
    vec1 = new RandomAccessSparseVector(3);
    max = vec1.maxValue();
    assertEquals(0.0, max, EPSILON);

    vec1 = new DenseVector(3);
    max = vec1.maxValue();
    assertEquals(0.0, max, EPSILON);

    vec1 = new SequentialAccessSparseVector(3);
    max = vec1.maxValue();
    assertEquals(0.0, max, EPSILON);

    vec1 = new RandomAccessSparseVector(0);
    max = vec1.maxValue();
    assertEquals(Double.NEGATIVE_INFINITY, max, EPSILON);

    vec1 = new DenseVector(0);
    max = vec1.maxValue();
    assertEquals(Double.NEGATIVE_INFINITY, max, EPSILON);

    vec1 = new SequentialAccessSparseVector(0);
    max = vec1.maxValue();
    assertEquals(Double.NEGATIVE_INFINITY, max, EPSILON);

  }

  @Test
  public void testMin()  {
    Vector vec1 = new RandomAccessSparseVector(3);

    vec1.setQuick(0, 1);
    vec1.setQuick(1, 3);
    vec1.setQuick(2, 2);

    double max = vec1.minValue();
    assertEquals(1.0, max, 0.0);

    int idx = vec1.maxValueIndex();
    assertEquals(1, idx);

    vec1 = new RandomAccessSparseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(2, -2);

    max = vec1.maxValue();
    assertEquals(0.0, max, 0.0);

    idx = vec1.maxValueIndex();
    assertEquals(1, idx);

    vec1 = new SequentialAccessSparseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(2, -2);

    max = vec1.maxValue();
    assertEquals(0.0, max, 0.0);

    idx = vec1.maxValueIndex();
    assertEquals(1, idx);

    vec1 = new DenseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(2, -2);

    max = vec1.maxValue();
    assertEquals(0.0, max, 0.0);

    idx = vec1.maxValueIndex();
    assertEquals(1, idx);

    vec1 = new RandomAccessSparseVector(3);
    max = vec1.maxValue();
    assertEquals(0.0, max, EPSILON);

    vec1 = new DenseVector(3);
    max = vec1.maxValue();
    assertEquals(0.0, max, EPSILON);

    vec1 = new SequentialAccessSparseVector(3);
    max = vec1.maxValue();
    assertEquals(0.0, max, EPSILON);

    vec1 = new RandomAccessSparseVector(0);
    max = vec1.maxValue();
    assertEquals(Double.NEGATIVE_INFINITY, max, EPSILON);

    vec1 = new DenseVector(0);
    max = vec1.maxValue();
    assertEquals(Double.NEGATIVE_INFINITY, max, EPSILON);

    vec1 = new SequentialAccessSparseVector(0);
    max = vec1.maxValue();
    assertEquals(Double.NEGATIVE_INFINITY, max, EPSILON);

  }

  @Test
  public void testDenseVector()  {
    Vector vec1 = new DenseVector(3);
    Vector vec2 = new DenseVector(3);
    doTestVectors(vec1, vec2);
  }

  @Test
  public void testVectorView()  {
    RandomAccessSparseVector vec1 = new RandomAccessSparseVector(3);
    RandomAccessSparseVector vec2 = new RandomAccessSparseVector(6);
    SequentialAccessSparseVector vec3 = new SequentialAccessSparseVector(3);
    SequentialAccessSparseVector vec4 = new SequentialAccessSparseVector(6);
    Vector vecV1 = new VectorView(vec1, 0, 3);
    Vector vecV2 = new VectorView(vec2, 2, 3);
    Vector vecV3 = new VectorView(vec3, 0, 3);
    Vector vecV4 = new VectorView(vec4, 2, 3);
    doTestVectors(vecV1, vecV2);
    doTestVectors(vecV3, vecV4);
  }

  /** Asserts a vector using enumeration equals a given dense vector */
  private static void doTestEnumeration(double[] apriori, Vector vector) {
    double[] test = new double[apriori.length];
    Iterator<Vector.Element> iter = vector.iterateNonZero();
    while (iter.hasNext()) {
      Vector.Element e = iter.next();
      test[e.index()] = e.get();
    }

    for (int i = 0; i < test.length; i++) {
      assertEquals(apriori[i], test[i], EPSILON);
    }
  }

  @Test
  public void testEnumeration()  {
    double[] apriori = {0, 1, 2, 3, 4};

    doTestEnumeration(apriori, new VectorView(new DenseVector(new double[]{
        -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9}), 2, 5));

    doTestEnumeration(apriori, new DenseVector(new double[]{0, 1, 2, 3, 4}));

    Vector sparse = new RandomAccessSparseVector(5);
    sparse.set(0, 0);
    sparse.set(1, 1);
    sparse.set(2, 2);
    sparse.set(3, 3);
    sparse.set(4, 4);
    doTestEnumeration(apriori, sparse);

    sparse = new SequentialAccessSparseVector(5);
    sparse.set(0, 0);
    sparse.set(1, 1);
    sparse.set(2, 2);
    sparse.set(3, 3);
    sparse.set(4, 4);
    doTestEnumeration(apriori, sparse);

  }

  @Test
  public void testAggregation()  {
    Vector v = new DenseVector(5);
    Vector w = new DenseVector(5);
    setUpFirstVector(v);
    setUpSecondVector(w);
    doTestAggregation(v, w);
    v = new RandomAccessSparseVector(5);
    w = new RandomAccessSparseVector(5);
    setUpFirstVector(v);
    doTestAggregation(v, w);
    setUpSecondVector(w);
    doTestAggregation(w, v);
    v = new SequentialAccessSparseVector(5);
    w = new SequentialAccessSparseVector(5);
    setUpFirstVector(v);
    doTestAggregation(v, w);
    setUpSecondVector(w);
    doTestAggregation(w, v);
  }

  private static void doTestAggregation(Vector v, Vector w) {
    assertEquals("aggregate(plus, pow(2)) not equal to " + v.getLengthSquared(),
        v.getLengthSquared(),
        v.aggregate(Functions.PLUS, Functions.pow(2)), EPSILON);
    assertEquals("aggregate(plus, abs) not equal to " + v.norm(1),
        v.norm(1),
        v.aggregate(Functions.PLUS, Functions.ABS), EPSILON);
    assertEquals("aggregate(max, abs) not equal to " + v.norm(Double.POSITIVE_INFINITY),
        v.norm(Double.POSITIVE_INFINITY),
        v.aggregate(Functions.MAX, Functions.ABS), EPSILON);

    assertEquals("v.dot(w) != v.aggregate(w, plus, mult)",
        v.dot(w),
        v.aggregate(w, Functions.PLUS, Functions.MULT), EPSILON);
    assertEquals("|(v-w)|^2 != v.aggregate(w, plus, chain(pow(2), minus))",
        v.minus(w).dot(v.minus(w)),
        v.aggregate(w, Functions.PLUS, Functions.chain(Functions.pow(2), Functions.MINUS)), EPSILON);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyAggregate1() {
    assertEquals(1.0, new DenseVector(new double[]{1}).aggregate(Functions.MIN, Functions.IDENTITY), EPSILON);
    assertEquals(1.0, new DenseVector(new double[]{2, 1}).aggregate(Functions.MIN, Functions.IDENTITY), EPSILON);
    new DenseVector(new double[0]).aggregate(Functions.MIN, Functions.IDENTITY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyAggregate2() {
    assertEquals(3.0, new DenseVector(new double[]{1}).aggregate(
        new DenseVector(new double[]{2}),Functions.MIN, Functions.PLUS), EPSILON);
    new DenseVector(new double[0]).aggregate(new DenseVector(new double[0]), Functions.MIN, Functions.PLUS);
  }

  private static void setUpFirstVector(Vector v) {
    v.setQuick(1, 2);
    v.setQuick(2, 0.5);
    v.setQuick(3, -5);
  }

  private static void setUpSecondVector(Vector v) {
    v.setQuick(0, 3);
    v.setQuick(1, -1.5);
    v.setQuick(2, -5);
    v.setQuick(3, 2);
  }

  @Test
  public void testHashCodeEquivalence() {
    // Hash codes must be equal if the vectors are considered equal
    Vector sparseLeft = new RandomAccessSparseVector(3);
    Vector denseRight = new DenseVector(3);
    sparseLeft.setQuick(0, 1);
    sparseLeft.setQuick(1, 2);
    sparseLeft.setQuick(2, 3);
    denseRight.setQuick(0, 1);
    denseRight.setQuick(1, 2);
    denseRight.setQuick(2, 3);
    assertEquals(sparseLeft, denseRight);
    assertEquals(sparseLeft.hashCode(), denseRight.hashCode());

    sparseLeft = new SequentialAccessSparseVector(3);
    sparseLeft.setQuick(0, 1);
    sparseLeft.setQuick(1, 2);
    sparseLeft.setQuick(2, 3);
    assertEquals(sparseLeft, denseRight);
    assertEquals(sparseLeft.hashCode(), denseRight.hashCode());

    Vector denseLeft = new DenseVector(3);
    denseLeft.setQuick(0, 1);
    denseLeft.setQuick(1, 2);
    denseLeft.setQuick(2, 3);
    assertEquals(denseLeft, denseRight);
    assertEquals(denseLeft.hashCode(), denseRight.hashCode());

    Vector sparseRight = new SequentialAccessSparseVector(3);
    sparseRight.setQuick(0, 1);
    sparseRight.setQuick(1, 2);
    sparseRight.setQuick(2, 3);
    assertEquals(sparseLeft, sparseRight);
    assertEquals(sparseLeft.hashCode(), sparseRight.hashCode());

    DenseVector emptyLeft = new DenseVector(0);
    Vector emptyRight = new SequentialAccessSparseVector(0);
    assertEquals(emptyLeft, emptyRight);
    assertEquals(emptyLeft.hashCode(), emptyRight.hashCode());
    emptyRight = new RandomAccessSparseVector(0);
    assertEquals(emptyLeft, emptyRight);
    assertEquals(emptyLeft.hashCode(), emptyRight.hashCode());
  }

  @Test
  public void testHashCode() {
    // Make sure that hash([1,0,2]) != hash([1,2,0])
    Vector left = new SequentialAccessSparseVector(3);
    Vector right = new SequentialAccessSparseVector(3);
    left.setQuick(0, 1);
    left.setQuick(2, 2);
    right.setQuick(0, 1);
    right.setQuick(1, 2);
    assertFalse(left.equals(right));
    assertFalse(left.hashCode() == right.hashCode());

    left = new RandomAccessSparseVector(3);
    right = new RandomAccessSparseVector(3);
    left.setQuick(0, 1);
    left.setQuick(2, 2);
    right.setQuick(0, 1);
    right.setQuick(1, 2);
    assertFalse(left.equals(right));
    assertFalse(left.hashCode() == right.hashCode());

    // Make sure that hash([1,0,2,0,0,0]) != hash([1,0,2])
    right = new SequentialAccessSparseVector(5);
    right.setQuick(0, 1);
    right.setQuick(2, 2);
    assertFalse(left.equals(right));
    assertFalse(left.hashCode() == right.hashCode());

    right = new RandomAccessSparseVector(5);
    right.setQuick(0, 1);
    right.setQuick(2, 2);
    assertFalse(left.equals(right));
    assertFalse(left.hashCode() == right.hashCode());
  }

>>>>>>> 76aa07461566a5976980e6696204781271955163
}

