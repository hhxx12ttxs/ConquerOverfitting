public ArrayList<Integer> findMinimumSpanningTree(){

int bestSolutionNSteps = Integer.MAX_VALUE;
for(int startingIndex = 0; startingIndex < graph.length; startingIndex++) {
int nSteps = 0;
boolean isSolvable = true;

stepIndex = startingIndex;

