private static final double TOP_RIGHT = Math.PI / 4.;

private final int spriteRow;

Direction(int spriteRow) {
this.spriteRow = spriteRow;
}

public static Direction fromAngle(double direction) {
Direction result;

