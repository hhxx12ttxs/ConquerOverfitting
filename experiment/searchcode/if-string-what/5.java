int line = 0;
public DMIException(String[] descriptor, int line, String what) {
super(what);
desc = descriptor;
this.line = line;
}
public DMIException(String what) {

