// initialize the objective function rows
if (getNumObjectiveFunctions() == 2) {
matrix[0][0] = -1;
}
int zIndex = (getNumObjectiveFunctions() == 1) ? 0 : 1;
// initialize the constraint rows
int slackVar = 0;
int artificialVar = 0;

