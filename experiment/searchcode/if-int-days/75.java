int N = Integer.parseInt(args[0]);
int p = 0;
for (int i = 0; i<N; i++) {
boolean[] days = new boolean[365];
int n = (int)(Math.random()*days.length);
if (!days[n]) {
days[n] = true;

