public void addDependencyStrategy(StrategyUserHelper<?> helper) {
for (S strategy : strategies) {
if (strategy instanceof DependencyStrategy) {
checkDependencyStrategy();
}
}

public void checkDependencyStrategy() {
Strategy strategy = getCurrentStrategy();
if (strategy instanceof DependencyStrategy) {

