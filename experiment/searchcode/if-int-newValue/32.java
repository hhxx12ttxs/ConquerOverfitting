public final class ValueChange<A> {
private A oldValue;
private A newValue;

public ValueChange(A oldValue, A newValue) {
this.oldValue = oldValue;
this.newValue = newValue;
}

public A getOldValue() {

