/*
 * Created on Apr 8, 2005
 */
package com.objectwave.tools;

import java.io.LineNumberReader;
import java.io.PrintStream;
import java.io.StringReader;
import com.objectwave.tools.MethodBuilder;;
/**
 * @author dave_hoag
 * @version $Date: 2005/04/09 12:35:45 $ $Revision: 1.1 $
 */
public class MethodBuilderTest extends com.objectwave.test.UnitTestBaseImpl
{
	MethodBuilderTest aVar;
	MethodBuilder builder;
	String name;
	String str;
	String[] strArray;
	//Used for testing
	int testVariable = 30;

	public static void main(String[] args)
	{
		com.objectwave.test.TestRunner.run(new MethodBuilderTest(), args);
	}
	/**
	 *  Constructor for the Test object
	 */
	public MethodBuilderTest() { }
	/**
	 *  Constructor for the Test object
	 *
	 * @param  string
	 */
	public MethodBuilderTest(String string)
	{
		super(string);
		name = string;
	}
	/**
	 * @param  one
	 * @param  two
	 * @return  Description of the Return Value
	 */
	public String doThis(String one, String two)
	{
		return (one + " " + two);
	}
	//
	int getInt()
	{
		return testVariable;
	}
	public String name()
	{
		return name;
	}
	//
	int printInt(int val)
	{
		return val;
	}
	/**
	 *  Print Str used for testing.
	 *
	 * @param  tst
	 * @return  Description of the Return Value
	 */
	public String printStr(MethodBuilderTest tst)
	{
		return tst.str;
	}
	/**
	 * @param  testName The new up value
	 * @param  context The new up value
	 */
	public void setUp(String testName, com.objectwave.test.TestContext context)
	{
		if(builder != null)
		{
			return;
		}
		str = "com.objectwave.tools.MethodBuilder$Test.printInt(int)\n";
		str += "com.objectwave.tools.MethodBuilder$Test.getInt()\n";
//        str += "com.objectwave.tools.MethodBuilder$Test.defineMethods(java.io.LineNumberReader):defineMethodsKey\n";
		str += "java.lang.Integer.new(int)\n";
		str += "com.objectwave.tools.MethodBuilder$Test.new(java.lang.String)\n";
		str += "com.objectwave.tools.MethodBuilder$Test.doThis(java.lang.String ,java.lang.String)\n";
		builder = new MethodBuilder();
	}
	/**
	 * @param  input
	 * @return
	 */
	public String show(String input)
	{
		return input;
	}
	public void testArrays()
	{
		String commands = "show(aVar.strArray[0])\n";
		commands += "aVar.strArray[0] = \"asdf\"\n";
		commands += "show(aVar.strArray[0])\n";
		commands += "show(aVar2[0])\n";
		commands += "aVar3 = new java.lang.String[10]\n";
		commands += "aVar3[0] = \"ThirdTest\"\n";
		commands += "aVar2[0] =\"Argg\"\n";
		commands += "show(aVar2[0])\n";
		commands += "show(aVar3[0])\n";

		Object res;
		builder.variables.put("aVar", this);
		strArray = new String[]{"aMessage"};
		builder.variables.put("aVar2", strArray);
		try
		{
			LineNumberReader rdr = new LineNumberReader(new StringReader(commands));
			res = builder.invokeMethod(this, rdr);
			testContext.assertEquals("aMessage", res.toString());
			res = builder.invokeMethod(this, rdr);
			testContext.assertEquals("asdf", res.toString());
			res = builder.invokeMethod(this, rdr);
			testContext.assertEquals("asdf", res.toString());
			res = builder.invokeMethod(this, rdr);
			testContext.assertEquals("Argg", res.toString());
			res = builder.invokeMethod(this, rdr);
			testContext.assertEquals("ThirdTest", res.toString());
		}
//	    catch (RuntimeException ex) { throw ex; }
		catch(Exception e)
		{
			testContext.assertTrue(e.toString(), false);
			return;
		}

	}
	/**
	 */
	public void testDefineMethods()
	{
		try
		{
			LineNumberReader rdr = new LineNumberReader(new StringReader(str));
			builder.defineMethods(rdr);
		}
		catch(RuntimeException ex)
		{
			throw ex;
		}
		catch(Exception e)
		{
			testContext.assertTrue(e.toString(), false);
		}
	}
	public void testLitteral()
	{
		String commands = "printInt(30)\n";

		Object res;
		try
		{
			LineNumberReader rdr = new LineNumberReader(new StringReader(commands));
			res = builder.invokeMethod(this, rdr);
		}
		catch(RuntimeException ex)
		{
			throw ex;
		}
		catch(Exception e)
		{
			testContext.assertTrue(e.toString(), false);
			return;
		}
		testContext.assertTrue(res instanceof Integer);
		testContext.assertEquals(30, ((Integer) res).intValue());
	}
	public void testMultipleArgs()
	{
		String commands = "doThis(  one, two)\n";

		Object res;
		try
		{
			LineNumberReader rdr = new LineNumberReader(new StringReader(commands));
			res = builder.invokeMethod(this, rdr);
		}
		catch(RuntimeException ex)
		{
			throw ex;
		}
		catch(Exception e)
		{
			testContext.assertTrue(e.toString(), false);
			return;
		}
		testContext.assertEquals("one two", res.toString());
	}
	public void testNestedInvocation()
	{
		String commands = "printInt(this.getInt())";

		Object res;
		try
		{
			LineNumberReader rdr = new LineNumberReader(new StringReader(commands));
			res = builder.invokeMethod(this, rdr);
		}
		catch(RuntimeException ex)
		{
			throw ex;
		}
		catch(Exception e)
		{
			testContext.assertTrue(e.toString(), false);
			return;
		}
		testContext.assertTrue(res instanceof Integer);
		testContext.assertEquals(30, ((Integer) res).intValue());
	}
	public void testNestedVariableManipulation()
	{
		String commands = "aVar = new com.objectwave.tools.MethodBuilder$Test(aTest)\n";
		commands += "aVar.aVar = new com.objectwave.tools.MethodBuilder$Test(aTestTwo)\n";
		commands += "aVar.aVar.aVar = new com.objectwave.tools.MethodBuilder$Test(aTestThree)\n";
		commands += "name()\n";
		commands += "show(aVar.name())\n";
		commands += "show(aVar.aVar.name())\n";

		MethodBuilderTest b = new MethodBuilderTest("first");
		Object res = null;
		try
		{
			LineNumberReader rdr = new LineNumberReader(new StringReader(commands));

			res = builder.invokeMethod(b, rdr);
			testContext.assertEquals("first", res);
			res = builder.invokeMethod(b, rdr);
			testContext.assertEquals("aTest", res);
			res = builder.invokeMethod(b, rdr);
			testContext.assertEquals("aTestTwo", res);
		}
		catch(RuntimeException ex)
		{
			throw ex;
		}
		catch(Exception e)
		{
			testContext.assertTrue(e.toString(), false);
			return;
		}
	}
	public void testQuotes()
	{
		String commands = "doThis( \"one.two\", \"three\")\n";
		commands += "aVar = \"more\"\n";
		commands += "doThis(aVar, \"\")\n";
		commands += "doThis(\"aVar\", \"\")\n";
		commands += "aVar = new com.objectwave.tools.MethodBuilder$Test(aTest)\n";
		commands += "aVar.str = \"more\"\n";
		commands += "printStr(aVar)\n";
		Object res;
		try
		{
			LineNumberReader rdr = new LineNumberReader(new StringReader(commands));
			res = builder.invokeMethod(this, rdr);
			testContext.assertEquals("one.two three", res.toString());
			res = builder.invokeMethod(this, rdr);
			testContext.assertEquals("more ", res.toString());
			res = builder.invokeMethod(this, rdr);
			testContext.assertEquals("aVar ", res.toString());
			res = builder.invokeMethod(this, rdr);
			testContext.assertEquals("more", res.toString());
		}
		catch(RuntimeException ex)
		{
			throw ex;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			testContext.assertTrue(e.toString(), false);
		}

	}
	public void testReturnType()
	{
		String commands = "toString()\n";

		MethodBuilder b = new MethodBuilder();
		Object res;
		try
		{
			LineNumberReader rdr = new LineNumberReader(new StringReader(commands));
			res = builder.invokeMethod(b, rdr);
		}
		catch(RuntimeException ex)
		{
			throw ex;
		}
		catch(Exception e)
		{
			testContext.assertTrue(e.toString(), false);
			return;
		}
		testContext.assertTrue(res instanceof String);
		testContext.assertTrue(((String) res).startsWith("com.objectwave.tools"));
	}
	public void testSimpleMath()
	{
		String commands = "printInt(10 + 20)\n";

		Object res;
		try
		{
			LineNumberReader rdr = new LineNumberReader(new StringReader(commands));
			res = builder.invokeMethod(this, rdr);
		}
		catch(RuntimeException ex)
		{
			throw ex;
		}
		catch(Exception e)
		{
			testContext.assertTrue(e.toString(), false);
			return;
		}
		testContext.assertTrue(res instanceof Integer);
		testContext.assertEquals(30, ((Integer) res).intValue());
	}
	/**
	 *  Due to the fact that PrintStream has multiple println methods, we must
	 *  declare the method we are to use!
	 */
	public void testSystemOut()
	{
		String commands = "System.out.println(Invoke from script!)\n";
		commands += "name()\n";

		//Define the println method we are going to use in this test.
		String str = "java.io.PrintStream.println(java.lang.String)\n";
		try
		{
			LineNumberReader rdr = new LineNumberReader(new StringReader(str));
			builder.defineMethods(rdr);
		}
		catch(RuntimeException ex)
		{
			throw ex;
		}
		catch(Exception e)
		{
			testContext.assertTrue(e.toString(), false);
		}

		MethodBuilderTest b = new MethodBuilderTest();
		Object res = null;
		java.io.PrintStream orig = System.out;
		java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
		System.setOut(new PrintStream(bos));
		try
		{
			LineNumberReader rdr = new LineNumberReader(new StringReader(commands));
			res = builder.invokeMethod(b, rdr);
			String result = new String(bos.toByteArray()).trim();
			testContext.assertEquals("Invoke from script!", result);
		}
		catch(RuntimeException ex)
		{
			throw ex;
		}
		catch(Exception e)
		{
			testContext.assertTrue(e.toString(), false);
			return;
		}
		finally
		{
			System.setOut(orig);
		}
	}
	/**
	 */
	public void testSystemOutTwo()
	{
		String commands = "System.out.println(\"Invoke from script!\")\n";
		commands += "name()\n";

		//Define the println method we are going to use in this test.
		String str = "java.io.PrintStream.println(java.lang.String)\n";
		try
		{
			LineNumberReader rdr = new LineNumberReader(new StringReader(str));
			builder.defineMethods(rdr);
		}
		catch(RuntimeException ex)
		{
			throw ex;
		}
		catch(Exception e)
		{
			testContext.assertTrue(e.toString(), false);
		}

		MethodBuilderTest b = new MethodBuilderTest();
		Object res = null;
		java.io.PrintStream orig = System.out;
		java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
		System.setOut(new PrintStream(bos));
		try
		{
			LineNumberReader rdr = new LineNumberReader(new StringReader(commands));
			res = builder.invokeMethod(b, rdr);
			String result = new String(bos.toByteArray()).trim();
			testContext.assertEquals("Invoke from script!", result);
		}
		catch(RuntimeException ex)
		{
			throw ex;
		}
		catch(Exception e)
		{
			testContext.assertTrue(e.toString(), false);
			return;
		}
		finally
		{
			System.setOut(orig);
		}
	}
	public void testVarAssignment()
	{
		String commands = "aVar = getInt()\n";
		commands += "X = 1\n";
		commands += "Y = X\n";
		commands += "printInt(aVar)\n";

		Object res;
		try
		{
			LineNumberReader rdr = new LineNumberReader(new StringReader(commands));
			res = builder.invokeMethod(this, rdr);
		}
		catch(RuntimeException ex)
		{
			throw ex;
		}
		catch(Exception e)
		{
			testContext.assertTrue(e.toString(), false);
			return;
		}
		testContext.assertTrue(res instanceof Integer);
		testContext.assertEquals(30, ((Integer) res).intValue());
		testContext.assertEquals("1", (String) (builder.variables.get("X")));
		testContext.assertEquals("1", (String) (builder.variables.get("Y")));
//System.out.println(builder.variables.get("Y"));
	}
	public void testVariableManipulation()
	{
		String commands = "this.testVariable = 20\n";
		commands += "getInt()\n";

		Object res;
		try
		{
			LineNumberReader rdr = new LineNumberReader(new StringReader(commands));
			res = builder.invokeMethod(this, rdr);
		}
		catch(RuntimeException ex)
		{
			throw ex;
		}
		catch(Exception e)
		{
			testContext.assertTrue(e.toString(), false);
			return;
		}
		testContext.assertTrue(res instanceof Integer);
		testContext.assertEquals(20, ((Integer) res).intValue());
	}
	public void testVariableMath()
	{
		String commands = "aVar = 10\n";
		commands += "printInt(aVar + 20)\n";

		Object res;
		try
		{
			LineNumberReader rdr = new LineNumberReader(new StringReader(commands));
			res = builder.invokeMethod(this, rdr);
		}
		catch(RuntimeException ex)
		{
			throw ex;
		}
		catch(Exception e)
		{
			testContext.assertTrue(e.toString(), false);
			return;
		}
		testContext.assertTrue(res instanceof Integer);
		testContext.assertEquals(30, ((Integer) res).intValue());
	}
	public void testVars()
	{
		String commands = "aVar = 1\n";
		commands += "aVar = aVar + 1\n";
		commands += "show(aVar)\n";

		MethodBuilderTest b = new MethodBuilderTest();
		Object res = null;
		try
		{
			LineNumberReader rdr = new LineNumberReader(new StringReader(commands));

			res = builder.invokeMethod(b, rdr);
			testContext.assertEquals("2.0", res);
		}
		catch(RuntimeException ex)
		{
			throw ex;
		}
		catch(Exception e)
		{
			testContext.assertTrue(e.toString(), false);
			return;
		}
	}
}
