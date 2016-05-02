package com.objectwave.utility;

import java.util.Vector;
import java.util.Enumeration;

/**
 * @author Dave Hoag
 * @version $Date: 2005/04/04 02:27:39 $ $Revision: 1.2 $
 */
public class ThreadPoolManagerTest extends com.objectwave.test.UnitTestBaseImpl
{
	public ThreadPoolManagerTest(){}
	public ThreadPoolManagerTest(String test){
		super(test);
	}
    /**
     *  The main program for the Test class
     *
     * @param  args The command line arguments
     */
    public static void main(String[] args)
    {
        com.objectwave.test.TestRunner.run(new ThreadPoolManagerTest(), args);
    }
    /**
     *  Gets the Runnable attribute of the Test object
     *
     * @param  str
     * @param  v
     * @return  The Runnable value
     */
    public Runnable getRunnable(final String str, final Vector v)
    {
        return
            new Runnable()
            {
                /**
                 *  Main processing method for the Test object
                 */
                public void run()
                {
                    v.addElement(str);
                    Enumeration e = v.elements();
                    String string = "DoSomethingToTakeTime";
                    while(e.hasMoreElements())
                    {
                        Object obj = e.nextElement();
                        string += obj.toString() + "more";
                        string += new StringBuffer(obj.toString()).toString();
                    }
                }
            };
    }
    /**
     *  A unit test for JUnit
     */
    public synchronized void testSomeExecute()
    {
        ThreadPoolManager mgr = new ThreadPoolManager(1);
        Runnable[] runs = new Runnable[2230];
        Vector vector = new Vector();
        for(int i = 0; i < runs.length; ++i)
        {
            runs[i] = getRunnable("aTest" + i, vector);
            mgr.start(runs[i]);
        }
        try
        {
            wait(10);
        }
        catch(InterruptedException ex)
        {
        }
        int count = mgr.shutdown().length;
        testContext.assertTrue("All threads executed despite shutdown!", count > 0);
        testContext.assertEquals("Queue information not accurate!", vector.size() + count, runs.length);
    }
    /**
     *  A unit test for JUnit
     */
    public synchronized void testSomeExecute2()
    {
        ThreadPoolManager mgr = new ThreadPoolManager(20);
        Runnable[] runs = new Runnable[3992];
        Vector vector = new Vector();
        for(int i = 0; i < runs.length; ++i)
        {
            runs[i] = getRunnable("aTest" + i, vector);
            mgr.start(runs[i]);
        }
        try
        {
            wait(10);
        }
        catch(InterruptedException ex)
        {
        }
        int count = mgr.shutdown().length;
        testContext.assertTrue("All threads executed despite shutdown!", count > 0);
        testContext.assertEquals("Queue information not accurate!", runs.length, vector.size() + count);
    }
    /**
     *  A unit test for JUnit
     */
    public synchronized void testAllExecute()
    {
        ThreadPoolManager mgr = new ThreadPoolManager(1);
        Runnable[] runs = new Runnable[30];
        Vector vector = new Vector();
        for(int i = 0; i < runs.length; ++i)
        {
            runs[i] = getRunnable("aTest" + i, vector);
            mgr.start(runs[i]);
        }
        int count = 0;
        while(vector.size() < 30)
        {
            try
            {
                count++;
                wait(50);
            }
            catch(InterruptedException ex)
            {
            }
            if(count > 1000)
            {
                throw new IllegalThreadStateException("Pool seems to have not executed everything!");
            }
        }
        mgr.shutdown();
        mgr.shutdown();
    }
}

