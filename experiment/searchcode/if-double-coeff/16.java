public class Polynome {
protected ArrayList<Double> coeff =new ArrayList<Double>();
int degre;
private int[] vec;
public Polynome (int taille){
public Polynome (int taille,double [] d){

for(int i=0;i<taille;i++)
coeff.add(d[i]);
this.degre=degre();

}
public Polynome (int taille,ArrayList<Double> d){

