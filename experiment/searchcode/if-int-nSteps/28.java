* less than or equal to nSteps steps. */
public boolean winInNumSteps(GameState curr, int nSteps) {
assert(nSteps >= 0 &amp;&amp; nSteps <= NUM_STEPS_IN_MOVE);
private boolean test_goal_squares(GameState game, int nSteps) {
// check first if rabbit can run to the end unassisted!
if (can_rabbit_run(game, nSteps)) return true;

