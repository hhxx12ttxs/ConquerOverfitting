private static StrategyTypes discountDefaultStrategy = StrategyTypes.HIGHEST;  // should be picked from configuration file

public static DiscountStrategy getDiscountStrategy(StrategyTypes strategyType) {
strategy = discountDefaultStrategy;
} else {
strategy = strategyType;
}
if (StrategyTypes.HIGHEST.getType().equals(strategy.getType())) {

