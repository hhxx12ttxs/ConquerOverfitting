// delta = dnrm2_(&amp;n, g, &amp;inc);
double gnorm1 = delta;
double gnorm = gnorm1;

if ( gnorm <= eps * gnorm1 ) search = 0;
info(&quot;iter %2d act %5.3e pre %5.3e delta %5.3e f %5.3e |g| %5.3e CG %3d&quot; + NL, iter, actred, prered, delta, f, gnorm, cg_iter);

if ( actred > eta0 * prered ) {
iter++;

