gladiator.setRandomWalk(false);

int[] selectedLoc = null;
double selectedDistance = 0.0D;
for(int[] outloc : OUTROOM_LOCATIONS)
{
if((selectedLoc == null) || (selectedDistance > PositionUtils.calculateDistance(currentLoc.getX(), currentLoc.getY(), 0, outloc[0], outloc[1], 0, false)))

