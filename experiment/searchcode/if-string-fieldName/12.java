public static String getCamelCaseUpper(String fieldName){
if(fieldName==null || fieldName.length()==0) return &quot;&quot;;
String newfieldName = null;
newfieldName = fieldName.substring(0,1).toUpperCase()+fieldName.substring(1);
}
return newfieldName;
}

public static String getCamelCaseLower(String fieldName){
if(fieldName==null || fieldName.length()==0) return &quot;&quot;;

