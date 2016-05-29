* The only thing this does is remove any CustomPattern
* propertyChangeListener.
*/
public void clear() {
if (pattern instanceof CustomPattern) {
public synchronized void setPattern(Pattern p) {
if (pattern instanceof CustomPattern) {   // quit listening to the old pattern

