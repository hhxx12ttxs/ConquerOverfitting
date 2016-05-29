Object[] r1 = new Object[] { Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, null, &quot;AAAAAAAAAAAAAA&quot;, &quot;   &quot;, null, Long.valueOf(-100L), Long.valueOf(0L), Long.valueOf(212L), null, new Double(-100.2),
new Double(0.0), new Double(212.23), null };
*  Check the 2 lists comparing the rows in order.
*  If they are not the same fail the test.
*/
public void checkRows(List<RowMetaAndData> rows1, List<RowMetaAndData> rows2)

