int bestSeed = -1;
double bestAccuracy = -1;
for (int i = 0; i < 5; i++) {
double lr = (int) (Math.random() * 1000);
Configuration c = new Configuration();
MLP mlp = c.readInputFile(inputFile);
double accuracy = mlp.train();

