static double simulate(int L) {
double current = generate(L);
double previous = -1;
double next = current + generate(L);
while (current - previous < 1 || next - current < 1) {

