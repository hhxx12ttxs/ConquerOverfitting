private Component componentk;
private Component componentm;

public double muLijIM(Component componentk, Component componentm, double im) {
public double value(double t) {
// special case - if COR_EDPIM=1 then double integral reduces to
// single integral
if (slatMC.getCalculationOptions().getCorrelationOptions()

