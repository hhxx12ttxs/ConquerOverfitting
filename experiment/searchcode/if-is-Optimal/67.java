public class StochasticStrategy {
boolean isOptimal;

IDWithSVNodes id;

Hashtable<FiniteStates,PotentialTable> policies;
public void calculateOptimalStrategy(int algor){
DeterministicPotentialTable auxDetPot;


if (!isOptimal){


//Take the policies

