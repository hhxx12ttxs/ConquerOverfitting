private final int[] _array;
private int         _offset;


public void setOffset(int offset) {
if (offset < 0 || offset >= _array.length) throw new IllegalArgumentException(&quot;offset must be between 0 and the length of the array&quot;);
_offset = offset;
}

public IntArrayPointer( final int[] array, final int offset ) {

