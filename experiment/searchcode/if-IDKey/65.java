public boolean equals(final Object other) {
if (!(other instanceof IDKey)) {
return false;
}
final IDKey idKey = (IDKey) other;
if (id != idKey.id) {

