int opt_threshold;
int ih, it;
int first_bin;
int last_bin;
int tmp_var;
int t_star1, t_star2, t_star3;
double[] P1 = new double[256]; /* cumulative normalized histogram */
double[] P2 = new double[256];
int total = 0;
for (ih = 0; ih < 256; ih++)
total += data[ih];

