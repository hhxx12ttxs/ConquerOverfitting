@Override
public long convertLocalToUTC(long instantLocal, boolean strict) {
return delegate().convertLocalToUTC(instantLocal, strict);
}

@Override
public long convertUTCToLocal(long instantUTC) {

