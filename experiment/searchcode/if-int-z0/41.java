public boolean obligatorio(String dato){
return dato != null &amp;&amp; dato.trim().length() > 0;

}

public boolean maxLength(String dato, int max){
return dato.length() <= max;

}

public boolean minLength(String dato, int min){

