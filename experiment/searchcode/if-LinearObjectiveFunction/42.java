public void testMath781() {
LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] { 2, 6, 7 }, 0);
Assert.assertEquals(2.0d, solution.getValue(), epsilon);
}

@Test
public void testMath713NegativeVariable() {
LinearObjectiveFunction f = new LinearObjectiveFunction(new double[] {1.0, 1.0}, 0.0d);

