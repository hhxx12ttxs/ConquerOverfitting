makeAssertions(genomeLength, populationSize);

}

@Test public void shouldGenerateMinimalPopulation(){
int genomeLength = 10;
int populationSize = 2;
initPopulation(genomeLength, populationSize);
makeAssertions(genomeLength, populationSize);

