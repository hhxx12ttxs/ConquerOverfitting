@Override
public boolean equals(Object other) {
if (!(other instanceof IDKey)) {
return false;
}
IDKey idKey = (IDKey) other;
if (id != idKey.id) {
return false;
}
// Note that identity equals is used.

