/** retourne le ratio de consommation de nourriture */
public static double ratioCentrale(double conso_ene, double prod_ene){
if(prod_ene == 0){
public static double ratioUsine(double morts, double cap_usine){
if(cap_usine == 0){
cap_usine = 1.0;
}
double ratio = morts/cap_usine;
return ratio;

