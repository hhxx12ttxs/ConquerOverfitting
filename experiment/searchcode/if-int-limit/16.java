public static final int DEFAULT_START = 0;
public static final int DEFAULT_LIMIT = 20;

protected int start = 0;
protected int limit = 0;
public int getLimit() {
return limit;
}

public void setLimit(int limit) {
if (limit <= 0) {
this.limit = DEFAULT_LIMIT;

