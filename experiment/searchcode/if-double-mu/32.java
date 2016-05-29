import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;

public class Constants {

private double mu;

public Constants() throws OrekitException {
public void setMu(double mu) throws OrekitException {
if (mu != this.mu) {
this.mu = mu;
}
}

/**
*
* @return mu.
*/
public double getMu() {
return this.mu;
}
}

