Random random = new Random();
double randomValue = random.nextDouble();
if (randomValue < solutions[0].getDistributionFunction()) {
for (int i = 1; i < solutions.length; i++) {

if (solutions[i - 1].getDistributionFunction() < randomValue

