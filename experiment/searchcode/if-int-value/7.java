Rotation(final int _intValue) {
intValue = _intValue;
}

public int getIntValue() {
return intValue;
}

public static Rotation valueOf(final int _intValue) {
for (Rotation r : values()) {
if (r.getIntValue() == _intValue) {

