private List<Individual> pop = new ArrayList<Individual>();
private static final int populationSize = 25;
private static final int sampleSize = 1;
private int[] score = new int[populationSize];
for(int count=0; count < this.populationSize ; count++){ pop.add(new Individual(&quot;allZeros&quot;)); }
}
else if (&quot;allOnes&quot;.equals(type)){
for(int count=0; count < this.populationSize ; count++){ pop.add(new Individual(&quot;allOnes&quot;)); }

