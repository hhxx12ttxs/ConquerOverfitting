IQuadrant quadrant = (IQuadrant) board;
int quadrantSize = quadrant.getQuadrantSize();

if (rotation.equals(IPentagoRotation.LEFT_ROTATION))
returnValue = rotateLeft(board, quadrantSize);
}
else if (rotation.equals(IPentagoRotation.RIGHT_ROTATION))

