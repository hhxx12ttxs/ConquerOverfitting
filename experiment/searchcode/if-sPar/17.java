package VriNettbasert;

import java.io.Serializable;

public class Kort implements Serializable{
public enum Kortfarge {Kloever, Hjerter, Ruter, Spar};

private int verdi;
private Kortfarge farge;

public Kort(Kortfarge farge, int verdi) 
{
	this.farge = farge;
	if(verdi <1 || verdi > 13) 
		throw new IllegalArgumentException("Kort m vžre mellom 1 (ess) og 13 (konge)");
	this.verdi = verdi;
} //end constructor Kort

public int getVerdi() {
	return verdi;
} //end method getVerdi

public Kortfarge getFarge() {
	return farge;
} //end method getFarge
public void setFarge(int f){
	if(f==1)
		farge = Kortfarge.Kloever;
	if(f==2)
		farge = Kortfarge.Hjerter;
	if(f==3)
		farge = Kortfarge.Ruter;
	if(f==4)
		farge = Kortfarge.Spar;
		
}

public static String fargeStreng(Kortfarge fargen)
{
	if (fargen == Kortfarge.Kloever) 	return "Kloever";
	if (fargen == Kortfarge.Hjerter)	return "Hjerter";
	if (fargen == Kortfarge.Ruter) 		return "Ruter";
	if (fargen == Kortfarge.Spar)		return "Spar";
	return "Feil";
} //end method fargeStreng

public static String verdiStreng (int verdi)
{
	if (verdi == 1)		return "Ess";
	if (verdi == 11)	return "Knekt";
	if (verdi == 12)	return "Dame";
	if (verdi == 13)	return "Konge";
	if (verdi > 13 || verdi < 1) return "Feil";
	return Integer.toString(verdi);
} //end method verdiString

public String toString() {
	return fargeStreng(farge) + " " + verdiStreng(verdi);
}


} //end class Kort


