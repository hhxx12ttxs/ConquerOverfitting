private String fieldName = &quot;&quot;;
private int summarySize = -1;

public SearchField(String fieldName) {
@Override
public String query() {
if(summarySize == -1) {
return fieldName;

