public RegexMatcher(String patternStr) {
this.patternStr = patternStr;
if(!VarBindingList.containsVar(patternStr))
this.pattern = convert(patternStr);
}
public boolean matches(String s, VarBindingList bindings) {

