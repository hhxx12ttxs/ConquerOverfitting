for (int n = 0; n < this.q1; n++) {
if (this.rnd.nextDouble() < replanningProbability) {
deltaQ1--;
for (int n = this.q1; n < this.demand; n++) {
if (this.rnd.nextDouble() < replanningProbability) {

