final List check = reduceCollisionSet(reducer, motions);
int suspectedSize = check.size();
if (immortal.ImmortalEntry.recordedRuns < immortal.ImmortalEntry.maxDetectorRuns) {
String prefix = debugPrefix + frameno + &quot; &quot;;
int offset = 0;
for (int i=0;i<currentFrame.planeCnt;i++) {
int cslen = currentFrame.lengths[i];

