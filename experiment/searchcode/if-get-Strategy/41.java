WindowsUpdateStrategyBO oldStrategy = findStrategy(strategy.getIp(), strategy.getSensorId(), false);
if (oldStrategy != null) {
oldStrategy.setCovered(1);
strategyDao.updateStrategy(oldStrategy);
}

if (strategy.getCreateTime() == null) {
strategy.setCreateTime(new Date());

