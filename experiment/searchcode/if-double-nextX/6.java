* Created by Sergey on 20.02.2015.
*/
public class JacobiSolver implements Solver {

public final double q;
public final int iterationsLimit;
x = nextX;
nextX = b.multiply(x).add(g);
iterations++;
if (nextX.hasNaN() || iterations > iterationsLimit)

