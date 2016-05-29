return false;
return true;
}

public If createCopy() {
return createCopy(true);
}

public If createCopy(boolean copyComments) {
stmtType[] new0;
if (this.body != null) {

