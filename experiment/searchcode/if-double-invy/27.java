ArrayList<Invazia> invy = S.invPodlaCasu.get(i);
if (invy != null) { // z neznameho dovodu tu mozu byt nullptr? asi nieco paralelne
for (int j=0; j<invy.size(); j++) {
int vlastnik = invy.get(j).vlastnik;
if (vlastnik<0 || vlastnik>=staty.length) {

