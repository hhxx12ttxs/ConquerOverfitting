* Loads the next pruning table if and only if it is <i>null</i>.<br>
* Equivalent to <code>loadNext(false);</code>
*/
public void loadNext() { loadNext(false); }
* @param force if true override it even if it already exists; if false only load when it is <i>null</i>
*/
public void loadNext(boolean force) {

