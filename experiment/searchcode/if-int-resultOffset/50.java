return _equals;
}

private boolean __hashCodeCalc = false;
public synchronized int hashCode() {
if (__hashCodeCalc) {
_hashCode += getSessionId().hashCode();
}
if (getFilterOptions() != null) {
for (int i=0;

