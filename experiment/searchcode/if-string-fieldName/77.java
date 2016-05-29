private String fieldName, fieldNameFeature;
private boolean prefix; // valid if dynamicField == true
// false: *_s, true: s_*

public MapField(String fieldName, String fieldNameFeature){
this.fieldName = fieldName;

