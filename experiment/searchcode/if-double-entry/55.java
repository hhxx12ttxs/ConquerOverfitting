robots.put(r.nextDouble(), (double)0);
}


int id = 1;
for(Map.Entry<Double, Double> i : robots.entrySet()) {
int steps = 0;
boolean stop = false;

while(!stop) {
psi = 0;
for(Map.Entry<Double, Double> i : robots.entrySet()) {

