public class QuadraticProbing extends ProbeSequence {
private double c1;
private double c2;
public QuadraticProbing(HashFunction hf, double c1, double c2){
super(hf);
this.c1 = c1;
this.c2 = c2;

