throw new IllegalArgumentException(&quot;Wrapped field&#39;s minumum value must be zero&quot;);
}
}

public long add(long instant, int value) {
return getWrappedField().add(instant, value);
}
public long add(long instant, long value) {

