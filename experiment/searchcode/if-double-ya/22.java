public Direction dir;
protected int health;

public void move(double xa, double ya) {
if (xa != 0 &amp;&amp; ya != 0) {
this.y += ya;
}
ya = 0;
}
}

}

private int abs(double value) {
if (value < 0) return -1;

