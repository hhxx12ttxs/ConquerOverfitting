StrategyAlgorithm strategyAlgorithm;

public StrategyAlgorithm getStrategyAlgorithm() {
return strategyAlgorithm;
}

public void setStrategyAlgorithm(StrategyAlgorithm strategyAlgorithm) {
this.strategyAlgorithm = strategyAlgorithm;
}

public void executeAlgorithm()
{
if(strategyAlgorithm!=null)
strategyAlgorithm.executeAlgorithm();

