
//Test cases would be:
/*
1, Illegal input: -1
2, Lower edge cases: 0, 1
3, Normal input: 5, 6
4, Upper edge cases: 13, 14
*/
//Factorial class
/*
public class Factorial {

	private static Scanner input;
	public static void main(String[] args) throws Exception {
		input = new Scanner(System.in);
		int n = input.nextInt();
		Factorial myFactorial = new Factorial();
		System.out.println(myFactorial.factorial(n));
	}
	public int factorial(int n) throws Exception{
		if(n < 0){
			throw new IllegalArgumentException("Illegal input.");
		}
		if(n == 0){
		  return 1;
		}
	
		return n * factorial(n - 1);
	}
}
*/
//1, Illegal input: -1
public class TestException {
	@Rule
	public ExpectedException exception = ExpectedException.none(); 
	@Test //(expected = IllegalArgumentException.class)
	public void test() throws Exception {
		Factorial myFactorial = new Factorial();
		exception.expect(IllegalArgumentException.class);
		myFactorial.factorial(-1);
	}
}
//2, Lower edge cases: 0, 1
public class TestLowerBound {
		@Test
		public void test0() throws Exception {
			Factorial myFactorial = new Factorial();
			int result = myFactorial.factorial(0);
			assertEquals(result, 1);
		}
		@Test
		public void test1() throws Exception{
			Factorial myFactorial = new Factorial();
			int result = myFactorial.factorial(1);
			assertEquals(result, 1);
		}
	}
	//3, Normal input: 5, 6
	public class TestNormalInput {

	@Test
	public void test5() throws Exception {
		Factorial myFactorial = new Factorial();
		int result = myFactorial.factorial(5);
		assertEquals(result, 120);
	}
	@Test
	public void test6() throws Exception{
		Factorial myFactorial = new Factorial();
		int result = myFactorial.factorial(6);
		assertEquals(result, 720);
	}
}
//4, Upper edge cases: 13, 14
public class TestUpperBound {

	@Test
	public void test13() throws Exception {
		Factorial myFactorial = new Factorial();
		int result = myFactorial.factorial(13);
		int pre = myFactorial.factorial(12);
		assertFalse(result < pre);
	}
	@Test
	public void test14() throws Exception {
		Factorial myFactorial = new Factorial();
		int result = myFactorial.factorial(14);
		int pre = myFactorial.factorial(13);
		assertFalse(result < pre);
	}
}

