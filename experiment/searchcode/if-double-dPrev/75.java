ArrayList<Double> mvg = mvgs.get(i) ;

if(mvg.size()<period){
mvg.add(price) ;
}
else if(mvg.size()==period){
int index = raw.size()-1 ;
if(raw.size()>=2){
double slope = mvgs.get(2).get(index) - mvgs.get(2).get(index-1) ;

