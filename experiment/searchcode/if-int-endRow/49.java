public class RESTTransportContainer {

public int status;
public int totalRows;
public int startRow;
public int endRow;
public ArrayList<String> errors;
@SuppressWarnings(&quot;rawtypes&quot;)
public RESTTransportContainer(final int totalRows, final int startRow,
final int endRow, final int status, final ArrayList data) {

