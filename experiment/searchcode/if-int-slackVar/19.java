// initialize the constraint rows
int slackVar = 0;
int artificialVar = 0;
for (int i = 0; i < constraints.size(); i++) {
matrix.setEntry(row, getSlackVariableOffset() + slackVar++, 1);  // slack
} else if (constraint.getRelationship() == Relationship.GEQ) {

