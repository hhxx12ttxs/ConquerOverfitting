getInvertedCoeffiecientSum(objectiveCoefficients);
}

// initialize the constraint rows
int slackVar = 0;
matrix[row][getSlackVariableOffset() + slackVar++] = 1;  // slack
} else if (constraint.getRelationship() == Relationship.GEQ) {

