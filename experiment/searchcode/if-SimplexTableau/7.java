return new PrimalSimplex(objectiveFunction, restrictions);
}

private PrimalSimplex(int[] objectiveFunction, int[][] restrictions)
{
if (objectiveFunction == null || restrictions == null
for (int i = 0; i < aBasis.length; i++)
{
aBasis[i] = i + restrictions_[0].length - 1;
}

SimplexTableau aProblem = SimplexTableau.create(aObjectiveFunction,

