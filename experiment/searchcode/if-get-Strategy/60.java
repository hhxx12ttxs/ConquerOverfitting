public Strategy search(String selector) throws StrategyNotFoundException {

Strategy strategy = strategyMap.get(selector);

if(strategy==null){
throw new StrategyNotFoundException();

