// private boolean[][] seen;

/**最初に作ったへぼい視野計算システム*/
public void calNewSeenIfUnitAdd(Unit unit) {
int view = unit.getViewRange();
boolean[][] seen = devil.getSeen();

for (int y = unit.getY() - view; y <= unit.getY() + view; y++) {

