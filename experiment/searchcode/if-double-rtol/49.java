implements BinaryRelation<T,U>
{
private final double rTol;
private final double iTol;

public ComplexCartesianNear() {
rTol = 0.000001;
iTol = 0.000001;
}

public ComplexCartesianNear(double rTol, double iTol) {

