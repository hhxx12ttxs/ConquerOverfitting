public Population (int populationSize, boolean initialize) {
routes = new Route [populationSize];

if (initialize) {
for (int i = 0; i < populationSize(); i++) {
Route fittest = routes[0];

for (int i = 1; i<populationSize(); i++) {
if (fittest.getFitness() <= getRoute(i).getFitness()) {

