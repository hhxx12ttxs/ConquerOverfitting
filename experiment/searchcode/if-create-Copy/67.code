} else if (!orelse.equals(other.orelse))
return false;
return true;
}

public IfExp createCopy() {
return createCopy(true);
}

public IfExp createCopy(boolean copyComments) {
IfExp temp = new IfExp(test != null ? (exprType) test.createCopy(copyComments) : null,

