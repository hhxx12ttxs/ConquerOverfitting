public void setXmType(Short xmType) {
this.xmType = xmType;
}

public boolean equals(Object other) {
if ((this == other))
public int hashCode() {
int result = 17;

result = 37 * result
+ (getXmId() == null ? 0 : this.getXmId().hashCode());

