* @throws EmitterException If the two angles are not legal.
*/
public StarSparkEmitter(double initialXPos, double initialYPos, double initialXV, double initialYV,
* @return An ArrayList of StarSpark objects.
*/
public ArrayList<Spark> launch(double time) {
double angle, vXInitial, vYInitial;
double[] position = getPosition();

