public class RouletteEvolution extends Evolution {

public RouletteEvolution() {
super();
}

public RouletteEvolution(int populationSize){
for (int i = 0; i < populationSize; i++) {
for (int j = 0; j < populationSize; j++) {
if (randTable[i] <= qTable[j]) {

