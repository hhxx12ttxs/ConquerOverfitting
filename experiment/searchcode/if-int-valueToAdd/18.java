protected WhereClause wclause = null;
//List order_by; // ORDER BY order_by
//int limit[]; // 2 max that will be offset, count
public void addToColumn(String column, int valueToAdd) {
addColumnNoAlter(column, column + &quot; + (&quot; + valueToAdd + &quot;)&quot;);

