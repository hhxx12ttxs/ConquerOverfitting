while(i == k || j == k) {
k = rand.nextInt(populationSize);
}

if(population[i].fitness > population[j].fitness) {
population[i] = child;
}

MatrixSolution solution = population[0];

for(int i = 1; i < populationSize; i++) {
if(population[i].compareTo(solution) > 0) {

