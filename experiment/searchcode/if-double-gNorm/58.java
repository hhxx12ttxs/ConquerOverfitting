fun_obj.grad(w, g);
delta = euclideanNorm(g);
double gnorm1 = delta;
double gnorm = gnorm1;

if (gnorm <= eps * gnorm1) search = 0;

iter = 1;

while (iter <= max_iter &amp;&amp; search != 0) {

