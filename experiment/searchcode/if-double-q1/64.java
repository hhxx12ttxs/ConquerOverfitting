double ne = s.nextDouble();
double nf = s.nextDouble();
LinearEquation Q1 =  new LinearEquation(na,nb,nc,nd,ne,nf);
if(Q1.isSolvable() == true){
return d;
}
double gete(){
return e;
}
double getf(){
return f;
}

boolean isSolvable(){
if(a*d-b*c == 0){

