public abstract class Item implements Drawable,Collisionable {

private Bitmap bitmap;
protected int x;
protected int y;
private int quadrantX;
private int quadrantY;

public Item(Bitmap bitmap, int x, int y, int quadrantX, int quadrantY) {
this.bitmap = bitmap;

