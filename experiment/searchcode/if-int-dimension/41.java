NETHER(-1), OVERWORLD(0), END(1);

private final int id;

Dimension(int id) {
this.id = (byte) id;
}

public int getId() {
return this.id;
}

public static Dimension getDimension(int i) {

