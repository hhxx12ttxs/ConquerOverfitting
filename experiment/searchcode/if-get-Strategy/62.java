public static StrategyData getInstance() {
if (StrategyData.instance == null) {
synchronized (StrategyData.class) {
if (StrategyData.instance == null) {
StrategyData.instance = new StrategyData();

