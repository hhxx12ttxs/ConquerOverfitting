boolean phase1Opt(int shape, int prunvalue, int maxl, int depth, int lm) {
if (maxl == 0) {
return isSolvedInPhase1();
return false;
}

int count = 0;
Square sq = new Square();

boolean isSolvedInPhase1() {

