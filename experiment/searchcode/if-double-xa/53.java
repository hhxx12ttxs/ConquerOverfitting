this.sprite = sprite;
}

public abstract void render(Screen screen);

public void move(double xa, double ya) {
if (xa != 0 &amp;&amp; ya != 0) {
move(xa, 0);
move(0, ya);
return;
}

if (map.getTile((int) x >> 4, (int) y >> 4) == Tile.web) {

