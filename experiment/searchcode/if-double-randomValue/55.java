public ArrayList<Individual> crossover(ArrayList<ArrayList<Individual>> parents, double crossoverRate) {
ArrayList<Individual> offspring = new ArrayList<Individual>();
double randomValue = rand.nextDouble();
if (randomValue < crossoverRate) {
offspring.addAll(crossoverCouple(couple));

