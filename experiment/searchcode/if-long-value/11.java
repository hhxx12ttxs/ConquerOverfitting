return (int)value;
}

public boolean equals(Object o) {
if (o instanceof Long) {
return ((Long)o).value == value;
}
if (o instanceof Double) {
return ((Double)o).value == value;

