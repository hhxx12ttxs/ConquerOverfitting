for(int j = i+1; j < N; j++) {
if(count[j] == 0) continue;
double[] Qlij = new double[N], Qijm = new double[N]; // q(.,i+j) and q(i+j,.)
double Qijij = nlogn(pijij, pij, pij); // q(i+j, i+j)
double Lij = -Q[i+N*i] -Q[j+N*j] + Q[i+N*j] + Q[j+N*i] + Qijij;
for(int u = 0; u < N; u++) {

