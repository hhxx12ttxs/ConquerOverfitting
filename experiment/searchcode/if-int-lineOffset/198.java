package com.zakula;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * A few tests that can be run from a normal JRE.
 * TODO: find a JUnit-like way
 * @author Marc Guillemot
 * @version $Revision:  $
 */
public class TestRhino {
    private Scriptable topScope;

    void allTests() throws Exception {
        testStringMatch();
        testSimple();
        testJSFunctionAdd();
        testCustomJSObject_property();
        testCustomJSFunction();
        testException();
        testSimple_Add();
        testSimple_Date();
        testMath_round();
        testMath_random();
//        testSimple_Math();
        testSimple_String();
        testArray();
        testGlobal();
        testEvalWrongJSON();
    }

    private void testArray() {
        testException("var a = []; a.length = 5.4;", "Inappropriate array length. (<test>#1)");
        testScript("var a = []; a[3.5] = false; a.length", new Double(0));
    }

    private void testGlobal() {
        testScript("parseInt('12', 8.5)", new Double(10));
        
    }

    private void testSimple_String() {
        testScript("'hello'.lastIndexOf('l') == 3", Boolean.TRUE);
        
    }

    private void testMath_round() {
        testScript("Math.round(1.2)", new Double(1));
        testScript("Math.round(0)", new Double(0));
        testScript("Math.round(0.2)", new Double(0));
        testScript("Math.round(-0.2)", new Double(-0.0));
        testScript("Math.round(0.5)", new Double(1));
        testScript("Math.round(-0.5)", new Double(-0.0));
        testScript("Math.round(-0.6)", new Double(-1));
        testScript("Math.round(NaN)", new Double(Double.NaN));
        testScript("Math.round(-Infinity)", new Double(Double.NEGATIVE_INFINITY));
        testScript("Math.round(+Infinity)", new Double(Double.POSITIVE_INFINITY));
    }

	private void testMath_random() {
        testScript("var r = Math.random(); r >= 0 && r < 1", Boolean.TRUE);
	}

	private void testSimple_Math() {
        testScript("Math.cos(Math.PI)", new Double(-1));
        testScript("Math.PI == Math.acos(-1)", Boolean.TRUE);
//        atan, atan2, acos, asin, exp, log, pow, random, round 
        
    }


	private void testEvalWrongJSON() {
        final String script = "function f() {\n"
        	+ "  try {\n"
        	+ "    return eval('({\"myvalue\":\"test\")}}})');\n"
        	+ "  }\n"
        	+ "  catch (e) {\n"
        	+ "    return 'failure';\n"
        	+ "  }\n"
        	+ "};\n"
        	+ "f();";
		testScript(script, "failure");
	}

	void testStringMatch() {
        testScript("'foo'.match(/^https?:\\/\\/\\w/)", null);
        testScript("'http://www.expensify.com/about'.match(/^https?:\\/\\/\\w/) != null", Boolean.TRUE);

        testScript("' foo  '.replace(/^\\s+|\\s+$/g, '')", "foo");
        
    }
    
    void testSimple_Add() {
        testScript("1 + 2", new Double(3.0));
    }

    void testSimple_Date() {
        // this one is not working on BB: not possible to access Timezone code
//        testScript("var d = new Date(); d.setTime(1300434057095); d.toString()", "Fri Mar 18 2011 08:40:57 GMT+0100 (CET)");
    }

    void testSimple() {
        final String script = "var hello = 3; hello";
        testScript(script, new Double(3.0));
    }

    void testJSFunctionAdd() {
        final String script = "var hello = 3;\n"
            + "function foo(x) { return x + hello;}\n"
            + "foo(8)";
        testScript(script, new Double(11.0));
    }

    void testException() {
        testException("null.foo", "Cannot read property \"foo\" from null");
    }

    void testException(final String throwingScript, final String expected) {
        final String script = "function foo() { try { " + throwingScript + "\n } catch(e) { return e.message }}\n"
            + "foo()";
        testScript(script, expected);
    }

    void testCustomJSObject_property() {
        final String script = "myTest.testIntProperty = 12; myTest.testIntPropertyX2";
        final Object insertObj = new JS_MyTestObject();
        
        final ContextAction action = new ContextAction() {
            public Object run(final Context cx) {
                ScriptableObject.putProperty(topScope, "myTest", insertObj);
                Object result = cx.evaluateString(topScope, script, "<test>", 1, null);
                assertEquals(new Double(24.0), result);
                return null;
            }
        };
        
        runSubAction(action);
    }

    void testCustomJSObject_function() {
//      final String script = "myTest.doSomething('foo')";
        
        // TODO
    }

    void testCustomJSFunction() {
        testCustomJSFunction("myTestFunction()", "myTestFunction()");
        testCustomJSFunction("myTestFunction(0)", "myTestFunction(java.lang.Double: 0.0)");
        testCustomJSFunction("myTestFunction('hello', true, 12)", 
                "myTestFunction(java.lang.String: hello, java.lang.Boolean: true, java.lang.Double: 12.0)");
    }

    private void testCustomJSFunction(final String script, final Object expected) {
        final Object insertObj = new JS_MyTestFunction();
        
        final ContextAction action = new ContextAction() {
            public Object run(final Context cx) {
                ScriptableObject.putProperty(topScope, "myTestFunction", insertObj);
                Object result = cx.evaluateString(topScope, script, "<test>", 1, null);
                assertEquals(expected, result);
                return null;
            }
        };
        
        runSubAction(action);
    }

    private void testScript(final String script, final Object expected) {
        final ContextAction action = new ContextAction() {
            public Object run(final Context cx) {
                final Object result = cx.evaluateString(topScope, script, "<test>", 1, null);
                assertEquals(expected, result);
                return null;
            }
        };
        
        runSubAction(action);
    }


    private void runSubAction(final ContextAction subAction) {
        final ContextFactory factory = new ContextFactory() {
            protected boolean hasFeature(Context cx, int featureIndex) {
                if (Context.FEATURE_E4X == featureIndex) {
                    return false;
                }
                return super.hasFeature(cx, featureIndex);
            }
        };
        
        final ContextAction action = new ContextAction() {
            public Object run(final Context cx) {
                topScope = cx.initStandardObjects();
                cx.setOptimizationLevel(-1);
//                cx.setErrorReporter(new ErrorReporter() {
//                    
//                    public void warning(String message, String sourceName, int line,
//                            String lineSource, int lineOffset) {
//                        System.err.println(sourceName + ":" + line + " " + message);
//                        throw new RuntimeException(message);
//                    }
//                    
//                    public EvaluatorException runtimeError(String message, String sourceName,
//                            int line, String lineSource, int lineOffset) {
//                        System.err.println(sourceName + ":" + line + " " + message);
//                        throw new RuntimeException(message);
//                    }
//                    
//                    public void error(String message, String sourceName, int line,
//                            String lineSource, int lineOffset) {
//                        System.err.println(sourceName + ":" + line + " " + message);
//                    }
//                });
                
                subAction.run(cx);
                return null;
            }
        };

        factory.call(action);
    }

    private void assertEquals(final Object expected, final Object actual) {
        if (expected == null) {
            if (actual != null) {
                throw new RuntimeException("Expected >" + expected + "< got >" + actual + "<");
            }
        }
        else if (!expected.equals(actual)) {
            throw new RuntimeException("Expected >" + expected + "< (" + expected.getClass().getName() + ") "
                    + "got >" + actual + "< (" + actual.getClass().getName() + ")");
        }
    }
}

