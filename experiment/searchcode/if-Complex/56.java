package complex;

import org.junit.Assert;
import org.junit.Test;
public class ComplexTest {

	@Test
	public void TestCreation() {
		Complex Z = new Complex(4,5);
		Complex Z2 = new Complex(Z);
		Assert.assertEquals("Value-constructor failed!", Z.getReal(), 4.0d, 10);
		Assert.assertEquals("Value-constructor failed!", Z.getImaginary(), 5.0d, 10);
		Assert.assertEquals("Copy-constructor failed!", Z2.getReal(), 4.0d, 10);
		Assert.assertEquals("Copy-constructor failed!", Z2.getImaginary(), 5.0d, 10);
	}
	
	@Test
	public void TestEquals() {
		Complex Z = new Complex(4,5);
		Complex Z2 = new Complex(Z);
		Complex Z3 = new Complex(5,4);
		Assert.assertTrue("Equality function failed!.", Z.equals(Z2) && Z.equals((Object)Z2) && !(Z.equals(Z3)));
	}
	
	@Test
	public void TestConjugate() {
		Complex Z = new Complex(4,5);
		Complex Z2 = Complex.Conjugate(Z);
		Complex Z3 = Complex.Conjugate(Z2);
		Complex Z4 = Complex.Conjugate(Z3);
		Assert.assertTrue("Conjugate function failed!", Z.equals(Z3) && Z2.equals(Z4) && !(Z.equals(Z2)));
		Assert.assertEquals("Conjugate function failed!", Z2.getImaginary(), -5.0d, 10);
		Assert.assertEquals("Conjugate function failed!", Z2.getReal(), 4.0d, 10);
	}
	
	@Test
	public void TestAddition() {
		Complex Z   = new Complex(5,4);
		Complex Z2  = new Complex(5,4);
		Complex Z3  = new Complex(-5,-4);
		Complex Z4  = Complex.Add(Z, Z2);
		Complex Z5  = Complex.Add(Z, Z3);
		Complex ZR1 = new Complex(10.0d,8.0d);
		Complex ZR2 = new Complex(0.0d,0.0d);
		Assert.assertTrue("Addition failed!", Z4.equals(ZR1) && Z5.equals(ZR2));
	}
	
	@Test
	public void TestSubtraction() {
		Complex Z   = new Complex(5,4);
		Complex Z2  = new Complex(5,4);
		Complex Z3  = new Complex(-5,-4);
		Complex Z4  = Complex.Sub(Z, Z2);
		Complex Z5  = Complex.Sub(Z, Z3);
		Complex ZR1 = new Complex(0.0d,0.0d);
		Complex ZR2 = new Complex(10.0d,8.0d);
		Assert.assertTrue("Subtraction failed!", Z4.equals(ZR1) && Z5.equals(ZR2));
	}
	
	@Test
	public void TestMultiplication() {
		Complex Z = new Complex(2,5);
		Complex Z2 = new Complex(7,8);
		Complex Z3 = Complex.Mul(Z, Z2);
		Complex ZR = new Complex(-26, 51);
		Assert.assertTrue("Multiplication failed!", ZR.equals(Z3));
	}
	
	@Test
	public void TestDivision() {
		Complex Z = new Complex(2,5);
		Complex Z2 = new Complex(7,8);
		Complex Z3 = Complex.Div(Z, Z2);
		Complex ZR = new Complex(54.0d/113.d, 19.0d/113.0d);
		Assert.assertTrue("Division Failed!", Z3.equals(ZR));
	}
	
	@Test
	public void TestMagnitude() {
		Complex Z = new Complex(1,1);
		double a = Math.sqrt(2.0);
		Assert.assertEquals("Absolute function failed!", a, Complex.Abs(Z), 10);
		double a2 = 2.0;
		Assert.assertEquals("Absolute-squared function failed!", a2, Complex.Abs2(Z), 10);
	}
	
	@Test
	public void TestSqrt() {
		Complex Z = new Complex(1,1);
		Complex Z2 = Complex.Sqrt(Z);
		Complex ZR = new Complex(1.0986841134678099660398011952d, 0.45508986056222734130435778d);
		Assert.assertEquals("Square-root function failed!", ZR.getReal(), Z2.getReal(), 10);
		Assert.assertEquals("Square-root function failed!", ZR.getImaginary(), Z2.getImaginary(), 10);
	}
	
	@Test
	public void TestPow() {
		Complex Z = new Complex(1,1);
		Complex Z2 = Complex.Pow(Z, 2);
		Complex Z3 = Complex.Pow(Z, -1);
		Complex Z4 = Complex.Mul(Z, Z);
		Complex ZR = new Complex(0, 2);
		Complex ZR2 = new Complex(0.5, -0.5);
		Assert.assertTrue("Power-of function failed!", Z2.equals(ZR));
		Assert.assertTrue("Power-of function failed!", Complex.Sqrt(Z2).equals(Z));
		Assert.assertTrue("Power-of function failed!", Z3.equals(ZR2));
		Assert.assertTrue("Power-of function failed!", Z4.equals(Z2));
	}
	
	@Test
	public void TestArg() {
		Complex Z = new Complex(1,1);
		double a = Complex.Arg(Z);
		double r = Math.PI/4;
		Assert.assertEquals("Argument function failed!", r, a, 10);
	}	
	
	@Test
	public void TestException() {
		// Catch thrown exceptions.
		try	{
			Complex Z = null;
			double a = Complex.Arg(Z);
			Complex Z2 = new Complex(1, 1);
			Complex Z3 = new Complex(0, 0);
			Complex Z4 = Complex.Div(Z2, Z3); 	// This will not run, since the above null-pointer exception thrown
												// halts execution.
											  	// But if it it was to run, IllegalArgumentException (divide by 0) 
			  									// would be thrown.
		} catch(NullPointerException e) {
			System.out.println("Null pointer exception!");
		} catch(IllegalArgumentException e) {
			System.out.println(e); // Catch divide by 0.
		}/*finally {
			If there was something that needed to be cleaned up regardless of if an exception is caught or not,
			put it in here.
		}*/
	}
}

