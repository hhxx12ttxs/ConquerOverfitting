public void firstPlayer(MoveChannel<Move> c) {
c.comment(board + &quot;\nThe optimal outcome is &quot; + optimalOutcome);

if (isLeaf()) {
c.comment(board + &quot;\nThe optimal outcome is &quot; + optimalOutcome);
if (isLeaf()) {
assert(optimalOutcome == board.value());

