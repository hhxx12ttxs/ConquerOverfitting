this.dernierResultat = dernierResultat;
}


@Override
public double add(double c1, double c2)
public double sub(double c1, double c2)
{
this.setDernierResultat(c1-c2);
return this.getDernierResultat();

