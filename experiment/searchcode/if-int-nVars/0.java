class TruthTable implements Stateful {
public int nvars;
public long tt;

public int hashCode() {
return nvars+(int)tt;
}

public boolean equals(Object that) {
if(!(that instanceof TruthTable)) {

