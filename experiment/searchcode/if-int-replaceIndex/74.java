@SuppressWarnings(&quot;serial&quot;)
abstract class BaseIndexedVector extends ASizedVector {
protected final int[] indexes;

protected BaseIndexedVector(int length) {
protected abstract BaseIndexedVector replaceIndex(int[] newIndices);

@Override
public boolean isView() {
return true;

