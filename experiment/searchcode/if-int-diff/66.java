private static final long serialVersionUID = -2942226928212019162L;

private final int xDiff;
private final int yDiff;

public Direction(int xDiff, int yDiff) {
this.xDiff = xDiff > 0 ? 1 : xDiff < 0 ? -1 : 0;

