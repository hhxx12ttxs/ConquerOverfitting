public Dimension(Dimension dimension) {
this(dimension.width, dimension.height);
}

public Dimension(int i, int j) {
public boolean equals(Object obj) {
if (obj instanceof Dimension) {
Dimension dimension = (Dimension) obj;

