package app;

public class StringId implements ItemKey<String>{

private String id;

@Deprecated
private StringId() {
id = null;
}

public StringId(String id) {
if(id==null || id.isEmpty())

