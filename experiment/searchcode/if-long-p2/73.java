costoPm=6;

}

public boolean attiva(Personaggio p1, Personaggio p2){
if(p1.getPm()<costoPm) return false;
p1.setPm(p1.getPm() - costoPm);
if(precisione(p1,p2)){
if(RoutineSystem.prob(30)){
//TODO

