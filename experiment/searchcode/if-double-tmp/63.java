public static double cycleRange(double R) {
double tmp = F(0.25,R);
for(int i=1 ; i<200000 ; i++) {
tmp = F(tmp,R);
}
//200,001
tmp = F(tmp,R);

double highest = tmp;
double lowest =  tmp;

for(int i=1 ; i<1000 ; i++) {

