sb.append(TdsConstants.URL_CHAR_DELIMETER_FOR_PARAMS_WITH_HTML_REQUEST + field.getName() + &quot;=&quot;);
sb.append(value);

}else if(field.get(object) instanceof Double){
Double value = (Double)field.get(object);

