package com.objectwave.utility;

import java.io.File;
import java.util.Random;

/**
 * @author Dave Hoag
 * @version $Date: 2005/04/04 02:27:39 $ $Revision: 1.2 $
 */
public class CachedRandomAccessFileTest extends com.objectwave.test.UnitTestBaseImpl
{
	public CachedRandomAccessFileTest(){}
	public CachedRandomAccessFileTest(String test){
		super(test);
	}
    /**
     *  A unit test for JUnit
     *
     * @exception  Exception Description of Exception
     */
    public void test1() throws Exception
    {
        File testFile = File.createTempFile("test", ".tmp");
        CachedRandomAccessFile craf = new CachedRandomAccessFile(testFile, "rw", 8
        /*
         * currBuf.bytes size
         */
, false);

        //
        // Test int read() and void write(int), and basic seek(long) operations.
        //

        testContext.assertTrue("Expected length 0", craf.length() == 0);

        char[] firstChars = {'s', 'o', 'm', 'e', ' ', 't', 'e', 's', 't', ' ', 'd', 'a', 't', 'a'};
        for(int i = 0; i < firstChars.length; i++)
        {
            craf.write(firstChars[i]);
        }

        testContext.assertTrue("Expected length " + firstChars.length, craf.length() == firstChars.length);

        craf.seek(0);
        for(int i = 0; i < firstChars.length; i++)
        {
            char c = (char) craf.read();
            testContext.assertTrue("Reading char[" + i + "] failed: " + c + "!=" + firstChars[i], firstChars[i] == c);
        }

        craf.seek(firstChars.length / 3);
        for(int i = firstChars.length / 3; i < firstChars.length; i++)
        {
            char c = (char) craf.read();
            testContext.assertTrue("Reading char[" + i + "] failed: " + c + "!=" + firstChars[i], firstChars[i] == c);
        }

        //
        // Test read(byte[]), write(byte[]), seek(int), and length()
        //

        String testData = "MORE TEST DATA TO WRITE TO THE TEMPORARY FILE";

        craf.seek(craf.length());
        // append to end of file

        craf.write(testData.getBytes());

        long len = craf.length();
        testContext.assertTrue("Expected file length " + (firstChars.length + testData.length()) + ", got " + len, len == (firstChars.length + testData.length()));

        craf.seek(0);
        byte[] confirmFirstChars = new byte[firstChars.length];
        craf.read(confirmFirstChars);
        for(int i = 0; i < confirmFirstChars.length; i++)
        {
            testContext.assertTrue("Expected chars[" + i + "] = " + firstChars[i] + ", not " + (char) confirmFirstChars[i], firstChars[i] == confirmFirstChars[i]);
        }

        byte[] confirmTestData = new byte[testData.length()];
        craf.read(confirmTestData);
        for(int i = 0; i < confirmTestData.length; i++)
        {
            testContext.assertTrue("Expected testData[" + i + "] = " + testData.charAt(i) + ", not " + (char) confirmTestData[i], testData.charAt(i) == confirmTestData[i]);
        }

        craf.close();
        testFile.delete();
    }

    /**
     *  A unit test for JUnit
     *
     * @exception  Exception Description of Exception
     */
    public void test2() throws Exception
    {
        File testFile = File.createTempFile("test", ".tmp");
        CachedRandomAccessFile craf = new CachedRandomAccessFile(testFile, "rw", 8, false);

        //                      1         2         3         4         5         6         7
        //            0123456789012345678901234567890123456789012345678901234567890123456789012*  file len = 73
        String txt = "This is a sentence which will be stored in the cached random access file.";
        craf.write(txt.getBytes());

        byte[] all = new byte[txt.length()];
        craf.seek(0);
        int allLen = craf.read(all);
        testContext.assertTrue("Expected to read " + txt.length() + " bytes, not " + allLen, txt.length() == allLen);
        for(int i = 0; i < allLen; i++)
        {
            testContext.assertTrue("[test all bytes] Expected byte " + txt.charAt(i) + " at pos " + i + ", not " + (char) all[i] + " (int " + all[i] + ")", all[i] == txt.charAt(i));
        }

        Random rnd = new Random(System.currentTimeMillis());
        for(int j = 0; j < 1000; j++)
        {
            int pos = Math.abs(rnd.nextInt()) % (txt.length());
            int len = Math.abs(rnd.nextInt()) % (txt.length() - pos);
            craf.seek(pos);
            if(rnd.nextInt() % 2 == 1)
            {
                byte[] b = new byte[len];
                int readLen = craf.read(b);
                testContext.assertTrue("Expected to read " + len + " bytes, not " + readLen, len == readLen);
                for(int i = 0; i < len; i++)
                {
                    testContext.assertTrue("Expected, for read(b[]) from pos " + pos + " b[" + i + "] == " + txt.charAt(pos + i) + ", not " + b[i], txt.charAt(pos + i) == b[i]);
                }
            }
            else
            {
                for(int i = 0; i < len; i++)
                {
                    int b = craf.read();
                    testContext.assertTrue("Expected, for read() from pos " + (pos + i) + " byte " + txt.charAt(pos + i) + ", not " + b, txt.charAt(pos + i) == b);
                }
            }
            //System.out.println("Read " + len + " bytes from pos " + pos + " ok.");
        }
    }

    /**
     *  The main program for the CachedRandomAccessFile class
     *
     * @param  args The command line arguments
     */
    public static void main(String[] args)
    {
        com.objectwave.test.TestRunner.run(new CachedRandomAccessFileTest(), args);
    }

}
