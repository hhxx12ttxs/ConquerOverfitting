public void findBestSolution(Map map, int iteration, int maxIterations) {

if (map.checkForFinish()) {

if (iteration < moves) {
public void tryMap(String moves, Map map, int iterator, int maxIterations) {

iterator++;
if (map.getCompletedPoints() >= map.getPointToComplete())

