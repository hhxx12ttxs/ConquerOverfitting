this.tsGroupid = tsGroupid;
}

@Override
public int hashCode() {
int hash = 0;
hash += (tsId != null ? tsId.hashCode() : 0);
public boolean equals(Object object) {
if (!(object instanceof TsMembers)) {
return false;

