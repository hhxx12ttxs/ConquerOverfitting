public synchronized <T extends Enum<T>> int getInt(T keyIndex) {

if (Config.isDebug()) {
argumentCheck(keyIndex);
public synchronized <T extends Enum<T>> void setInt(T keyIndex, int value) {

if (Config.isDebug()) {
argumentCheck(keyIndex);

