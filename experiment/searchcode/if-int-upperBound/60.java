return 0;
}

if(x < 2) {
return 1;
}

int upperbound = x;
int candidate = x / 2;
while(!isSQRT(candidate, x)) {
int sq = safeSq(candidate);
if(sq > x || sq < 0) {
upperbound = candidate;

