for(OptimalCombination combinationTemp : optimalCombinationsTemp)
{
boolean isMoreOptimal = false;
for(OptimalCombination combination : optimalCombinations)
if (combinationTemp.getWeight() < combination.getWeight()) {
isMoreOptimal = true;
//TODO break???
}
}
if (isMoreOptimal || optimalCombinations.size() < maxOptimalCombinations)

