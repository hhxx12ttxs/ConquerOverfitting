protected AbstractBlock calculatedValue;
public Parameter(String xpath) {
super(xpath);
}

public void childDone(Instruction child) {
if (calculatedValue != null) {
// TODO think about auto array for parameter

