public double evaluatePlayerAction(int player, GameState gs, PlayerAction playerAction, int numEval) throws Exception {
double evalMean = 0;

for (int step = 0; step < numEval; step++) {
GameState gs2 = gs.cloneIssue(playerAction);

