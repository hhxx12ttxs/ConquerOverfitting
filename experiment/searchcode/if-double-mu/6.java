private final double mu; // Poisson Ratio (axial to traverse strain ratio)

public Material(double young_mod, double mu_ratio)
{
E = young_mod;
mu = mu_ratio;
}

public double G()
{ // return the shear modulus G

