Point3D action = this.actionList.getAction(actionIndex);
double nextX = positionX + action.getX();
double nextY = positionY + action.getY();
double oldQValue = this.getQValue(positionX, positionY, positionZ, actionIndex);

double qValue = oldQValue + STUDY *
(reward.getReward(nextX, nextY, nextZ) + DISCOUNT * this.searchMaxQValue(nextX, nextY, nextZ)- oldQValue);

