//do a depth search through all phase1 positions at depth phase1len.
// return false if no solution, or break out with true if solved.
int m, nxt;
if (sollen >= phase1len) {
// if have phase1 solution at end of phase1, then try alternative phase2 solution first

