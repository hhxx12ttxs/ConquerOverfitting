} else if (!cause.equals(other.cause))
return false;
return true;
}

public Raise createCopy() {
return createCopy(true);
}

public Raise createCopy(boolean copyComments) {
Raise temp = new Raise(type != null ? (exprType) type.createCopy(copyComments) : null,

