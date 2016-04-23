/* InterpreterTests.java
 * Copyright (C) 2012 Alex "HolyCause" Mair (holy.cause@gmail.com)
 * Copyright (C) 2012 Ivan Vendrov (ivendrov@gmail.com)
 * Copyright (C) 2012 Joey Eremondi (jse313@mail.usask.ca)
 * Copyright (C) 2012 Joanne Traves (jet971@mail.usask.ca)
 * Copyright (C) 2012 Logan Cool (coollogan88@gmail.com)
 * 
 * This file is a part of Giraffe.
 * 
 * Giraffe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Giraffe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Giraffe.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.usask.cs.giraffe.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import ca.usask.cs.giraffe.compiler.ConcreteInterpreter;
import ca.usask.cs.giraffe.core.GiraffeLogger;
import ca.usask.cs.giraffe.core.GiraffeLogger.LEVEL;
import ca.usask.cs.giraffe.exception.ExecutionErrorException;
import ca.usask.cs.giraffe.exception.ParseErrorException;

/**
 * Set of tests to verify the interpreter
 * 
 * @author Joey Eremondi
 */
@SuppressWarnings("javadoc")
public class InterpreterTests {
	
	protected String run(String code) {
		GiraffeLogger.setLogLevel(LEVEL.DEBUG);
		ConcreteInterpreter c = new ConcreteInterpreter(null);
		try {
			return c.compileString(code).toString();
		} catch (ExecutionErrorException e) {
			e.printStackTrace();
			fail("Execution error on string: " + code + "\n" + e.getMessage());
			return "";
		} catch (ParseErrorException e) {
			e.printStackTrace();
			fail(e.getMessage());
			return "";
		}
	}
	
	protected void except(String code) {
		ConcreteInterpreter c = new ConcreteInterpreter(null);
		try {
			c.compileString(code).toString();
			fail("Missed expected exception");
		} catch (ExecutionErrorException e) {
			assertTrue(true);
		} catch (ParseErrorException e) {
			e.printStackTrace();
			fail("Parse error on string: " + code + "\n" + e.getMessage());
		}
	}
	
	protected void doFail(String s) {
		fail(s);
	}
	
	@Test
	public void intType() {
		assertEquals("0", this.run("int main(){" + "int x; x = 0;"
				+ "return x;}"));
		assertEquals("-3", this.run("int main(){" + "int x; x = -3;"
				+ "return x;}"));
		except("int main() { int x = true; return 0;}");
		except("int main() { int x = 3; return x[3];}");
		
		// Type conversion tests
		assertEquals("0", this.run("int main(){" + "int x; x = 0;"
				+ "bool y = x; return x;}"));
		assertEquals("0", this.run("int main(){" + "int x; x = 0;"
				+ "float y = x; return x;}"));
		assertEquals("0", this.run("int main(){" + "int x; x = 0;"
				+ "char y = x; return x;}"));
	}
	
	@Test
	public void charType() {
		assertEquals("0", this.run("int main(){" + "char x; x = 'a';"
				+ "return 0;}"));
		except("int main() { char x = true; return 0;}");
		
		// Type conversion tests
		assertEquals("0", this.run("int main(){" + "char x; x = 0;"
				+ "bool y = x; return x;}"));
		assertEquals("0", this.run("int main(){" + "char x; x = 0;"
				+ "float y = x; return x;}"));
		assertEquals("0", this.run("int main(){" + "char x; x = 0;"
				+ "int y = x; return x;}"));
	}
	
	@Test
	public void floatType() {
		assertEquals("0", this.run("int main(){" + "float x; x = 0.5;"
				+ "return 0;}"));
		except("int main() { float x = true; return 0;}");
		
		except("int main(){" + "return 3.5 && 4.2;}");
		except("int main(){" + "return 3.5 || 4.2;}");
		except("int main(){" + "return !4.5;}");
		
		except("int main(){" + "float x = 3.5; x[5] = 3;}");
		except("int main(){" + "float x = 3.5; return x[5];}");
		
		// Type conversion tests
		except("int main(){" + "float x; x = 3.5;" + "bool y = x; return x;}");
		assertEquals("3", this.run("int main(){" + "float x; x = 3.5;"
				+ "int y = x; return x;}"));
		assertEquals("3", this.run("int main(){" + "float x; x = 3.5;"
				+ "char y = x; return x;}"));
	}
	
	@Test
	public void doubleType() {
		assertEquals("0", this.run("int main(){" + "double x; x = 0.5;"
				+ "return 0;}"));
		except("int main() { double x = true; return 0;}");
		
	}
	
	@Test
	public void boolType() {
		assertEquals("0", this.run("int main(){" + "bool x; x = true;"
				+ "bool y; y = false;" + "return 0;}"));
		except("int main() { bool x = 3.0; return 0;}");
		
		// Type conversion tests
		except("int main(){" + "bool x; x = true;" + "int y = x; return x;}");
		except("int main(){" + "bool x; x = true;" + "float y = x; return x;}");
		except("int main(){" + "bool x; x = true;" + "char y = x; return x;}");
	}
	
	@Test
	public void stringType() {
		assertEquals("0", this.run("int main(){" + "char x[6]; x = \"hello\";"
				+ "return 0;}"));
		
		except("int main() { char x[6] = true; return 0;}");
		except("int main() { char x[6]; x = true; return 0;}");
		
		except("int main() { char x[6] = \"hello\"; char y[6] = \"hello\"; char z[12] = x + y; return 0;}");
		except("int main() { char x[6] = \"hello\"; char y[6] = \"hello\"; char z[12] = x - y; return 0;}");
		except("int main() { char x[6] = \"hello\"; char y[6] = \"hello\"; char z[12] = x * y; return 0;}");
		except("int main() { char x[6] = \"hello\"; char y[6] = \"hello\"; char z[12] = x / y; return 0;}");
		except("int main() { char x[6] = \"hello\"; char y[6] = \"hello\"; char z[12] = x % y; return 0;}");
		except("int main() { char x[6] = \"hello\"; char y[6] = \"hello\"; char z[12] = x && y; return 0;}");
		except("int main() { char x[6] = \"hello\"; char y[6] = \"hello\"; char z[12] = x || y; return 0;}");
		except("int main() { char x[6] = \"hello\"; char y[6] = \"hello\"; char z[12] = !x; return 0;}");
	}
	
	@Test
	public void array() {
		assertEquals("0", this.run("int main(){" + "int x[6];" + "return 0;}"));
		assertEquals("3", this.run("int main(){" + "int x[5];"
				+ "for (int i = 0; i < 5; i++) x[i] = i; return x[3];}"));
		assertEquals("4", this.run("int main(){" + "int x[5];"
				+ "for (int i = 0; i < 5; i++) x[i] = i; return x[4];}"));
		assertEquals("0", this.run("int main(){" + "int x[5];"
				+ "for (int i = 0; i < 5; i++) x[i] = i; return x[0];}"));
		
		assertEquals(
				"2",
				this.run("int main(){"
						+ "double x[3];"
						+ "x[0] = 1.5; x[2] = 2.5; x[1] = 3.5; if (x[1] < x[2]) return 1; else return 2;}"));
		
		except("int main(){" + "int x[4];"
				+ "for (int i = 0; i < 5; i++) x[i] = i; return x[5];}");
		except("int main(){" + "int x[4];"
				+ "for (int i = 0; i < 5; i++) x[i] = i; return x[-1];}");
		except("int main(){" + "int x[5]; return x[4];}");
		except("int main(){" + "int x; return x[4];}");
		except("int main(){" + "int x; x[3] = 4; return 0;}");
		
	}
	
	@Test
	public void add() {
		assertEquals("8", this.run("int main(){" + "return 3 + 5;}"));
		except("int main(){" + "return 3 + true;}");
		except("int main(){" + "return \"hello\" + 3;}");
	}
	
	@Test
	public void sub() {
		assertEquals("-2", this.run("int main(){" + "return 3 - 5;}"));
		except("int main(){" + "return 3 - true;}");
		except("int main(){" + "return \"hello\" - 3;}");
	}
	
	@Test
	public void mult() {
		assertEquals("15", this.run("int main(){" + "return 3 * 5;}"));
		except("int main(){" + "return 3 * true;}");
		except("int main(){" + "return \"hello\" * 3;}");
	}
	
	@Test
	public void div() {
		assertEquals("8", this.run("int main(){" + "return 24 / 3;}"));
		assertEquals("9", this.run("int main(){" + "return 28 / 3;}"));
		except("int main(){" + "return 3 / 0;}");
		except("int main(){" + "return 3/ true;}");
		except("int main(){" + "return \"hello\" / 3;}");
	}
	
	@Test
	public void mod() {
		assertEquals("0", this.run("int main(){" + "return 24 % 3;}"));
		assertEquals("1", this.run("int main(){" + "return 28 % 3;}"));
		except("int main(){" + "return 3 % 0;}");
		except("int main(){" + "return 3 % true;}");
		except("int main(){" + "return \"hello\" % 3;}");
	}
	
	@Test
	public void preMM() {
		assertEquals("2", this.run("int main(){"
				+ "int x; x = 3; --x; return x;}"));
		assertEquals("2", this.run("int main(){"
				+ "int x; x = 3;  return --x;}"));
		
		assertEquals("97", this.run("int main(){"
				+ "char x; x = 'b'; --x; return x;}"));
		assertEquals("97", this.run("int main(){"
				+ "char x; x = 'b';  return --x;}"));
		
		assertEquals("2", this.run("int main(){"
				+ "float x; x = 3.5; --x; return x;}"));
		assertEquals("2", this.run("int main(){"
				+ "float x; x = 3.5;  return --x;}"));
		except("int main(){" + "return --true;}");
	}
	
	@Test
	public void prePP() {
		assertEquals("4", this.run("int main(){"
				+ "int x; x = 3; ++x; return x;}"));
		assertEquals("4", this.run("int main(){"
				+ "int x; x = 3;  return ++x;}"));
		
		assertEquals("99", this.run("int main(){"
				+ "char x; x = 'b'; ++x; return x;}"));
		assertEquals("99", this.run("int main(){"
				+ "char x; x = 'b';  return ++x;}"));
		
		assertEquals("4", this.run("int main(){"
				+ "float x; x = 3.5; ++x; return x;}"));
		assertEquals("4", this.run("int main(){"
				+ "float x; x = 3.5;  return ++x;}"));
		except("int main(){" + "return ++true;}");
	}
	
	@Test
	public void postMM() {
		assertEquals("2", this.run("int main(){"
				+ "int x; x = 3; x--; return x;}"));
		assertEquals("3", this.run("int main(){"
				+ "int x; x = 3;  return x--;}"));
		
		assertEquals("97", this.run("int main(){"
				+ "char x; x = 'b'; x--; return x;}"));
		assertEquals("98", this.run("int main(){"
				+ "char x; x = 'b';  return x--;}"));
		
		assertEquals("2", this.run("int main(){"
				+ "float x; x = 3.5; x--; return x;}"));
		assertEquals("3", this.run("int main(){"
				+ "float x; x = 3.5;  return x--;}"));
		except("int main(){" + "return true--;}");
	}
	
	@Test
	public void postPP() {
		assertEquals("4", this.run("int main(){"
				+ "int x; x = 3; x++; return x;}"));
		assertEquals("3", this.run("int main(){"
				+ "int x; x = 3;  return x++;}"));
		
		assertEquals("99", this.run("int main(){"
				+ "char x; x = 'b'; x++; return x;}"));
		assertEquals("98", this.run("int main(){"
				+ "char x; x = 'b';  return x++;}"));
		
		assertEquals("4", this.run("int main(){"
				+ "float x; x = 3.5; x++; return x;}"));
		assertEquals("3", this.run("int main(){"
				+ "float x; x = 3.5;  return x++;}"));
		except("int main(){" + "return true++;}");
	}
	
	@Test
	public void addChar() {
		assertEquals("97", this.run("int main(){" + "return 'a' + '\0';}"));
		except("int main(){" + "return 'b' + true;}");
		except("int main(){" + "return \"hello\" + 'c';}");
	}
	
	@Test
	public void subChar() {
		assertEquals("2", this.run("int main(){" + "return 'c' -'a';}"));
		except("int main(){" + "return 'a' - true;}");
		except("int main(){" + "return \"hello\" - 'a';}");
	}
	
	@Test
	public void multChar() {
		assertEquals("0", this.run("int main(){" + "return '3' * '\0';}"));
		except("int main(){" + "return 3 * true;}");
		except("int main(){" + "return \"hello\" * 'a';}");
	}
	
	@Test
	public void divChar() {
		assertEquals("49", this.run("int main(){" + "return  'b'/ 2;}"));
		assertEquals("48", this.run("int main(){" + "return  'a'/ 2;}"));
		except("int main(){" + "return 'a' / 0;}");
		
	}
	
	@Test
	public void modChar() {
		assertEquals("1", this.run("int main(){" + "return 'a' % 2;}"));
		assertEquals("0", this.run("int main(){" + "return 97 % 'a';}"));
		except("int main(){" + "return 'a' % 0;}");
		except("int main(){" + "return 'a' % true;}");
		except("int main(){" + "return \"hello\" % 'a';}");
	}
	
	@Test
	public void ifStatements() {
		assertEquals("0", this.run("int main(){"
				+ "if (true) return 0; else return 1;}"));
		assertEquals("0", this.run("int main(){"
				+ "if (true) {return 0;} else {return 1;}}"));
		assertEquals("0", this.run("int main(){"
				+ "if (false) return 1; else return 0;}"));
		assertEquals("0", this.run("int main(){"
				+ "if (false) {return 1;} else {return 0;}}"));
		// Test with non-constant condition
		assertEquals("4", this.run("int main(){"
				+ "if (3 < 4) return 4; else return 1;}"));
		assertEquals("3", this.run("int main(){"
				+ "if (3 > 4) return 1; else return 3;}"));
		
		// Sequence of ifs
		assertEquals(
				"0",
				this.run("int main(){"
						+ "if (false) {return 1;} else if (false) {return 2;} else if (false) {return 23;} else {return 0;}}"));
		
		// If no else
		assertEquals("0", this.run("int main(){"
				+ "if (false) {return 1;} return 0;}"));
		
	}
	
	@Test
	public void fnCall() {
		assertEquals("7", this.run("int three(){ return 3;}"
				+ "int four(){ return 4;}" + "int main(){"
				+ "return three() + four();}"));
		
		assertEquals("3", this.run("int three(){ return 3;}"
				+ "int four(){ return 4;}" + "int main(){"
				+ "four(); return three();}"));
		
		// Function, 1 arg
		assertEquals("7", this.run("int plusThree(int x){ return 3 + x;}"
				+ "int four(){ return 4;}" + "int main(){"
				+ "four(); return plusThree(four());}"));
		
		// Function, 2 args
		assertEquals("7", this.run("int plus(int x, int y){ return y + x;}"
				+ "int four(){ return 4;}" + "int main(){"
				+ "int x = 3; int y = four(); return plus(x,y);}"));
		
		// Recursion
		assertEquals(
				"120",
				this.run("int factorial(int x){ if (x == 0) return 1; else return x* factorial(x-1);}"
						+ "int main(){" + "return factorial(5);}"));
		
		// Bad argument types
		this.except("int plus(int x, int y){ return y + x;}" + "int main(){"
				+ "bool x = true; float y = 3.0; return plus(x,y);}");
		
		// Bad argument number
		this.except("int plus(int x, int y){ return y + x;}" + "int main(){"
				+ "bool x = true; float y = 3.0; return plus(x,y, x);}");
	}
	
	@Test
	public void addFloat() {
		assertEquals("8", this.run("int main(){"
				+ "if (3.5 + 4.5 >= 7) return 8; return 7;}"));
		except("int main(){" + "return 3.5 + true;}");
		except("int main(){" + "return \"hello\" + 3.5;}");
		
	}
	
	@Test
	public void subFloat() {
		assertEquals("-2", this.run("int main(){"
				+ "if (3.5 - 4.5 < 0) return -2; return 0;}"));
		except("int main(){" + "return 3.5 - true;}");
		except("int main(){" + "return \"hello\" - 3.5;}");
	}
	
	@Test
	public void multFloat() {
		assertEquals("10", this.run("int main(){"
				+ "if (0.5 + 20 < 9) return 9; return 10;}"));
		except("int main(){" + "return 3.5 * true;}");
		except("int main(){" + "return \"hello\" * 3.5;}");
	}
	
	@Test
	public void divFloat() {
		assertEquals("8", this.run("int main(){"
				+ "if (24.0 / 3 <= 7) return 7; return 8;}"));
		assertEquals("9", this.run("int main(){"
				+ "if (28.0 / 3 > 9) return 9; return 8;}"));
		// except("int main(){" + "return 3.0 / 0;}");
		except("int main(){" + "return 3.0/ true;}");
		except("int main(){" + "return \"hello\" / 3.1;}");
	}
	
	@Test
	public void and() {
		assertEquals("1", this.run("int main(){"
				+ "if (true && true) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (true && false) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (false && true) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (false && false) return 1; return 2;}"));
		
		assertEquals("1", this.run("int main(){"
				+ "if (1 && 1) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (1 && 0) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (0 && 1) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (0 && 0) return 1; return 2;}"));
		
		assertEquals("1", this.run("int main(){"
				+ "if ('a' && 'a') return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if ('a' && '\0') return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if ('\0' && 'a') return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if ('\0' && '\0') return 1; return 2;}"));
	}
	
	@Test
	public void or() {
		assertEquals("1", this.run("int main(){"
				+ "if (true || true) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (true || false) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (false || true) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (false || false) return 1; return 2;}"));
		
		assertEquals("1", this.run("int main(){"
				+ "if (1 || 1) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (1 || 0) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (0 || 1) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (0 || 0) return 1; return 2;}"));
		
		assertEquals("1", this.run("int main(){"
				+ "if ('a' || 'a') return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if ('a' || '\0') return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if ('\0' || 'a') return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if ('\0' || '\0') return 1; return 2;}"));
	}
	
	@Test
	public void not() {
		assertEquals("2", this.run("int main(){"
				+ "if (!true) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (!false) return 1; return 2;}"));
		
		assertEquals("2", this.run("int main(){"
				+ "if (!1) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (!0) return 1; return 2;}"));
		
		assertEquals("2", this.run("int main(){"
				+ "if (!'a') return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (!'\0') return 1; return 2;}"));
		
	}
	
	@Test
	public void less() {
		assertEquals("1", this.run("int main(){"
				+ "if (3 < 4) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (5 < 4) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (5 < 5) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (3.1 < 4) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (5 < 4.1) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (3.1 < 4.2) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (5.1 < 4.2) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (4.2 < 4.2) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if ('a' < 'b') return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if ('c' < 'b') return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if ('c' < 'c') return 1; return 2;}"));
		except("int main(){" + "if (true < 4) return 1; return 2;}");
		except("int main(){" + "if (3 < \"hello\") return 1; return 2;}");
	}
	
	@Test
	public void leq() {
		assertEquals("1", this.run("int main(){"
				+ "if (3 <= 4) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (5 <= 4) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (5 <= 5) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (3.1 <= 4) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (5 <= 4.1) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (3.1 <= 4.2) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (5.1 <= 4.2) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (4.2 <= 4.2) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if ('a' <= 'b') return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if ('c' <= 'b') return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if ('c' <= 'c') return 1; return 2;}"));
		except("int main(){" + "if (true <= 4) return 1; return 2;}");
		except("int main(){" + "if (3 <= \"hello\") return 1; return 2;}");
	}
	
	@Test
	public void greater() {
		assertEquals("2", this.run("int main(){"
				+ "if (3 > 4) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (5 > 4) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (5 > 5) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (3.1 > 4) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (5 > 4.1) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (3.1 > 4.2) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (5.1 > 4.2) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (4.2 > 4.2) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if ('a' > 'b') return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if ('c' > 'b') return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if ('c' > 'c') return 1; return 2;}"));
		except("int main(){" + "if (true > 4) return 1; return 2;}");
		except("int main(){" + "if (3 > \"hello\") return 1; return 2;}");
	}
	
	@Test
	public void geq() {
		assertEquals("2", this.run("int main(){"
				+ "if (3 >= 4) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (5 >= 4) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (5 >= 5) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (3.1 >= 4) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (5 >= 4.1) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if (3.1 >= 4.2) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (5.1 >= 4.2) return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if (4.2 >= 4.2) return 1; return 2;}"));
		assertEquals("2", this.run("int main(){"
				+ "if ('a' >= 'b') return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if ('c' >= 'b') return 1; return 2;}"));
		assertEquals("1", this.run("int main(){"
				+ "if ('c' >= 'c') return 1; return 2;}"));
		except("int main(){" + "if (true >= 4) return 1; return 2;}");
		except("int main(){" + "if (3 >= \"hello\") return 1; return 2;}");
	}
	
	@Test
	public void vars() {
		// declare and assign
		assertEquals("10", this.run("int main(){" + "int x = 10; return x;}"));
		// separate assign
		assertEquals("10",
				this.run("int main(){" + "int x; x = 10; return x;}"));
		// different scopes
		assertEquals("0", this.run("int main(){"
				+ "int x = 0; if (true){ int x; x = 10;}; return x;}"));
		assertEquals(
				"8",
				this.run("int main(){"
						+ "int x = 0; int y; if (true){int x = 5; y = 3 + x;}; return y;}"));
		assertEquals("2", this.run("int main(){"
				+ "int x = 0; if (true){ x = 2;}; return x;}"));
		assertEquals("3", this.run("int main(){"
				+ "int x = 0; int y; if (true){ y = 3 + x;}; return y;}"));
		// duplicate name
		except("int main(){" + "int x; int x; x = 3; return x;}");
		// duplicate name, different type
		except("int main(){" + "int x; bool x; x = 3; return x;}");
		// get var undeclared
		except("int main(){" + "int y = 3 + x; return 0;}");
		// set var undeclared
		except("int main(){" + "x = 3; return x;}");
		// out of scope
		except("int main(){" + "if (true) {int x = 3;} return x;}");
	}
	
	@Test
	public void whileLoop() {
		assertEquals(
				"10",
				this.run("int main(){"
						+ "int x = 0; int i = 0; while (i < 5) {x += 2; i++;} return x;}"));
		assertEquals(
				"0",
				this.run("int main(){"
						+ "int x = 0; int i = 0; while (false) {x += 2; i++;} return x;}"));
		assertEquals(
				"10",
				this.run("int main(){"
						+ "int x = 0; int y = 0; int i = 0; while (i < 5 && y < 20) { int y; y = 2000; x += 2; i++;} return x;}"));
		assertEquals("30", this.run("int main(){" + "int x = 0;" + "int i = 0;"
				+ "while (i < 5) {" + "cout << \"Outer loop\"; int z = 0; "
				+ "while (z < 3) {"
				+ "cout <<\"Inner loop\" << x; x = x + 2; z++;}" + "i++;} "
				+ "return x;}"));
		// break from infinite loop
		assertEquals("0", this.run("int main(){"
				+ "while(true) {return 0;} return 1;}"));
	}
	
	@Test
	public void forLoop() {
		assertEquals(
				"10",
				this.run("int main(){"
						+ "int x = 0; for (int i = 0; i < 5; i++) { x = x + 2; cout << x;} return x;}"));
		assertEquals(
				"10",
				this.run("int main(){"
						+ "int x = 0; int i; for (i = 0; i < 5; i+= 1) {x += 2;} return x;}"));
		assertEquals("0", this.run("int main(){"
				+ "for ( ; ; ) {return 0;} return 1;}"));
	}
	
	@Test
	public void unInit() {
		except(" int main(){" + "int x = 3; int y; return x+y;}");
		except(" int main(){" + "int x; int y = 3; return x+y;}");
		
		except(" int main(){" + "int x = 3; int y; return x*y;}");
		except(" int main(){" + "int x; int y = 3; return x*y;}");
		
		except(" int main(){" + "int x = 3; int y; return x/y;}");
		except(" int main(){" + "int x; int y = 3; return x/y;}");
		
		except(" int main(){" + "int x = 3; int y; return x-y;}");
		except(" int main(){" + "int x; int y = 3; return x-y;}");
		
		except(" int main(){" + "int x = 3; int y; return x%y;}");
		except(" int main(){" + "int x; int y = 3; return x%y;}");
		
		except(" int main(){" + "int x = 3; int y; bool z =  x<y; return 0;}");
		except(" int main(){" + "int x; int y = 3; bool z =  x<y; return 0;}");
		
		except(" int main(){" + "int x = 3; int y; bool z =  x>y; return 0;}");
		except(" int main(){" + "int x; int y = 3; bool z =  x>y; return 0;}");
		
		except(" int main(){" + "int x = 3; int y; bool z =  x<=y; return 0;}");
		except(" int main(){" + "int x; int y = 3; bool z =  x<=y; return 0;}");
		
		except(" int main(){" + "int x = 3; int y; bool z =  x>=y; return 0;}");
		except(" int main(){" + "int x; int y = 3; bool z =  x>=y; return 0;}");
		
		except(" int main(){" + "int x = 3; int y; bool z =  x==y; return 0;}");
		except(" int main(){" + "int x; int y = 3; bool z =  x==y; return 0;}");
		
		except(" int main(){" + "int x = 3; int y; bool z =  x!=y; return 0;}");
		except(" int main(){" + "int x; int y = 3; bool z =  x!=y; return 0;}");
		
		except(" int main(){" + "bool x = true; bool y; return x&&y;}");
		except(" int main(){" + "bool x; bool y = true; return x&&y;}");
		
		except(" int main(){" + "bool x = true; bool y; return x||y;}");
		except(" int main(){" + "bool x; bool y = true; return x||y;}");
		
		except(" int main(){" + " int y; return !y;}");
		except(" int main(){" + " int y; return ++y;}");
		except(" int main(){" + " int y; return --y;}");
		except(" int main(){" + " int y; return y++;}");
		except(" int main(){" + " int y; return y--;}");
		
	}
	
	@Test
	public void globals() {
		assertEquals("3", this.run("int x = 3; int main(){" + "return x;}"));
		assertEquals("4", this.run("int x = 4; int main(){"
				+ "int x = 4; return x;}"));
	}
	
	@Test
	public void unsupported() {
		
		except(" int main(){" + "while (true) break; return 0;}");
		except(" int main(){" + "while (true) continue; return 0;}");
		except(" int main(){" + "int x; goto foo; return 0;}");
		
		except(" int main(){"
				+ "int x = 0; switch(x)  case 0: return 0; default: break;  return 0;}");
		except(" int main(){" + "case 3 : break;  return 0;}");
		except(" int main(){" + "default : break;  return 0;}");
		
		except(" int main(){" + "int x = (int) 3.5;  return 0;}");
		except("typedef int foo; int main(){" + "return 0;}");
		
		// except(" int main(){"struct foo {int x; int y;} myfoo; +
		// "return 0;}");
		
		// except("int main(){" + "int x; x.foo; return 0;}");
		
		// except("enum FOO { BAR, BAZ}; int main(){" + "return 0;}");
		except(" int main(){" + "static int x;  return 0;}");
		except(" int main(){" + "register int x;  return 0;}");
		except(" int main(){" + "auto int x;  return 0;}");
		except(" int main(){" + "volatile int x;  return 0;}");
		except(" int main(){" + "const int x;  return 0;}");
		except(" int main(){" + "static volatile int x;  return 0;}");
		except(" int main(){" + "extern int x;  return 0;}");
		except(" int main(){" + "long int x;  return 0;}");
		
		except(" int main(){" + "int* x;  return 0;}");
		except(" int main(){" + "int y; &y;  return 0;}");
		except(" int main(){" + "int y; *y;  return 0;}");
		
		except(" int main(){" + "do {  return 0;} while (true);}");
		
		except(" int main(){" + "return sizeof(int);}");
		except(" int main(){" + "int x; x->y; return 0;}");
		
		except(" int main(){" + "int x = 3; int y = 4;  return x & y;}");
		except(" int main(){" + "int x = 3; int y = 4;  return x | y;}");
		except(" int main(){" + "int x = 3; int y = 4;  return x ^ y;}");
		// except(" int main(){" + "int x = 3; int y = 4;  return x << y;}");
		// except(" int main(){" + "int x = 3; int y = 4;  return x >> y;}");
		except(" int main(){" + "int y = ~4;  return y ;}");
		
		except(" int main(){" + "return true ? 0 : 1;}");
		except(" int main(){" + "int y; foo: y = 0; return 0;}");
	}
	
}

