package checker.model;

public class FieldName {

String fieldName;

public FieldName(String className) {
this.fieldName = className.intern();
}

public String getFieldName() {
return this.fieldName;

