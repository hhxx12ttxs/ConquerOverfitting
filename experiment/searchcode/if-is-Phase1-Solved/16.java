boolean phase1Opt(int shape, int prunvalue, int maxl, int depth, int lm) {
if (maxl == 0) {
return isSolvedInPhase1();
}
//try each possible move. First twist;
Square sq = new Square();

boolean isSolvedInPhase1() {
d.copy(c);
for (int i=0; i<length1; i++) {

