 * games with a fair bit of chance involved).
 * @param style false play simple round robin tournament, true play complex one.
 * @throws InvalidRequestException if the request is made in between generations or is out of the acceptable range
 */
throws InvalidRequestException {
if (safeToInterrupt()) {
this.tournamentStyle = style;
Collections.shuffle(thePopulation, GAFrame.rnd);
if (!complex) // O(n) games
{
running = false;
if (game != null)
game.interrupt();
if (!safeToInterrupt()) {
output.append(\"\\n\\nGA INTERRUPTED!\");

