getInvertedCoeffiecientSum(objectiveCoefficients));
}

// initialize the constraint rows
int slackVar = 0;
int artificialVar = 0;
for (int i = 0; i < constraints.size(); i++) {
for (int i = getNumObjectiveFunctions(); i < getArtificialVariableOffset(); i++) {
if (MathUtils.compareTo(tableau.getEntry(0, i), 0, epsilon) > 0) {

