public class PiecewiseFunction extends FunctionModel {

public FunctionModel f1, f2;
public Double split;

public PiecewiseFunction(FunctionModel f1, Double split, FunctionModel f2) {
this.f1 = f1; this.f2 = f2;

