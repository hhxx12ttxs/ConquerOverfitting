int nrow = 13;
int nvars = 5;

public RandomPlugin(String fileName, String modeName) {
System.err.println(&quot;[RandomPlugin] &quot; + fileName + &quot;  (&quot; + modeName +&quot;)&quot;);
public int getNumRecords() {
return(nrow);
}

public int getNumVariables() {
return(nvars);
}

public String[] getVariableNames() {

