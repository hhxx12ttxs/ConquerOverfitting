private int chromeLength = 30;

/**
* 种群大小
*/
private int populationSize = 5;

/**
* 进化代数
*/
private int maxGeneration = Integer.MAX_VALUE;
double p = Math.random();
int selectIndex = -1;
for (int j = 0; j < populationSize; j++) {
if (p < fitnesss[j]) {

