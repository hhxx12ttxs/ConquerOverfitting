private String fieldBoolean = &quot;fieldBoolean&quot;;

private String fieldInt = &quot;fieldInt&quot;;

private String fieldString = &quot;fieldString&quot;;

private int intValue = 1;
public void testDataSourceObject() {

String[] columns = new String[] { fieldString, fieldInt, fieldBoolean };
MatrixCursor matrixCursor = new MatrixCursor(columns);

