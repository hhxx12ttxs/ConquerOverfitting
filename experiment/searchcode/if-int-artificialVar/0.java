matrix.setEntry(eqNum, variableCount + eq.slackVar.getKey(), eq.slackVar.getValue());
}

if (eq.artificialVar != null) {
matrix.setEntry(eqNum, variableCount + slackVariableCount + eq.artificialVar.getKey(), eq.artificialVar.getValue());
matrix.setRowVector(pivotRowIdx, pivotRow);

for (int r = 0; r < rows; ++r)
{
if (r == pivotRowIdx)

