public static DataGenerator instantiate(String dataGeneratorName,
long randomSeed,
int maxEvaluations) {
long randomSeed,
int maxEvaluations,
Schema schema) {
Class<DataGeneratorFactory> c = DataGeneratorFactory.class;

