package twentyFour.terms;

public class ConstantTerm extends Term {

private double value;

public ConstantTerm(double value) {
this.value = value;
}

@Override
public double getValue() {

