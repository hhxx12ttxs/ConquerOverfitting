public void addStrategy(RegisterStrategyBO strategy) {
RegisterStrategyBO oldStrategy = findStrategy(strategy.getIp(), strategy.getSensorId(), false);
if (oldStrategy != null) {
oldStrategy.setIssued(1);
oldStrategy.setCovered(1);

