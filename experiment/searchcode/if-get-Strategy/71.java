public void addStrategy(Strategy strategy) {
if(strategyMap.containsKey(strategy.getName())){
return;
}
//TODO:Currently only HitBattleShipStrategy supported.
public boolean executeStrategy(String strategyType, Target target) throws NoSuchStrategyException {
Strategy strategy = strategyMap.get(strategyType);
if(strategy == null){

