fireForces.add(fireForce);
}

public long solve(int strikeLength) {
int numHeads = fireForces.size();
if(numHeads <= strikeLength * 2)
List<Long> maxStarts = maxStart(fireSums);
long maxDamage = 0;
int numFireSums = fireSums.size();
for(int i = 0; i < numFireSums - strikeLength; i++) {

