package behavioral.strategy;

import model.HolyKnight;

public class StrategyHolyKnight extends HolyKnight implements StrategyKnight {
public void reduceArmor(Double reduceValue) {
super.reduceArmor(reduceValue);
if (strategy != null) {
strategy.armorResponse(getArmor());
}
}
}

