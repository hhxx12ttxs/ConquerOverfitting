public abstract class DataStructure implements IDataStructure {
private String splitter=&quot;_&quot;;
@Override
public String genId(String oid) {
String id =oid;
int idi = 1;
@Override
public String splitId(String id) {
int spaceIndex = id.indexOf(splitter);
if (spaceIndex != -1)

