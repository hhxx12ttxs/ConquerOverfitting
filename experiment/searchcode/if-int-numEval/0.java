public class CubedRoot {
public static void main (String[] args){
int numEval = 18; // found using lg((b-a)*10^4) with b-a = 25; didn&#39;t subtract one because best estimate one behind
double midVal = 0; // initiailizing middle value
double pCube;
double error = .0001;




for (int i=0; i<numEval; i++){

