package solver.simplex.logic;

import java.util.Formatter;

public final class SimplexTableau
public static SimplexTableau create(int[] objectiveFunction, int[] basis,
int[][] restrictions)
{
Fraction[][] z = new Fraction[restrictions.length + 1][restrictions[0].length];

