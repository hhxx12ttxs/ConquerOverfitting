final List collisions = lookForCollisions(reducer, createMotions());

int numberOfCollisions = collisions.size();
if (ImmortalEntry.recordedRuns < ImmortalEntry.maxDetectorRuns) {
for (int i=0;i<currentFrame.planeCnt;i++) {

int cslen = currentFrame.lengths[i];
System.out.println(prefix+new String( currentFrame.callsigns, offset, cslen )+&quot; &quot;+

