Quadrant opponentQuadrant = getOpponentLocation();

if (opponentQuadrant.equals(bulletQuadrant)) {
bullet.destroy();
FieldObject object = battleField.scan(bulletQuadrant);
if (object != null) {
battleField.update(bulletQuadrant, null);
System.out.println(&quot;QUADRANT CLEANED, vertical: &quot; + bulletQuadrant.v + &quot;, horizontal: &quot; + bulletQuadrant.h);

