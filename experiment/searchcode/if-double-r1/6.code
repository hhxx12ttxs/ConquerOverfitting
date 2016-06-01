public class BasicAnalysing implements Analyse {

@Override
public boolean withIn(Result r1, double within) {

if(r1.isMin()){

double value = (double)r1.getScore() / (double)r1.getMax();
return within < (1 - value);

