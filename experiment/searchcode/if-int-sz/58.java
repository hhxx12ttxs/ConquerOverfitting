public static int Id;
private static int sz = Box.BoxSize;

private static void getId() {

for (int i = 0; i < BoxList.size(); i++) {
//190 170
if (sz >= Math.abs((Player.x - sz / 2) - (BoxList.get(i).x + sz / 2)) || sz >= Math.abs((Player.x + sz / 2) - (BoxList.get(i).x - sz / 2))) {

