int period = periods.get(i) ;
ArrayList<Double> mvg = mvgs.get(i) ;

if(mvg.size()<period){
mvg.add(price) ;
public void checkState(){ // check the state of the mvgs, to decide whether or not to buy/sell
int index = raw.size()-1 ;
if(raw.size()>=2){
double slope = mvgs.get(2).get(index) - mvgs.get(2).get(index-1) ;

