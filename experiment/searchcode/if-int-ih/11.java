int ih, it;
int first_bin;
int last_bin;
double tot_ent;  /* total entropy */
double max_ent;  /* max entropy */
double [] P1 = new double[256]; /* cumulative normalized histogram */
double [] P2 = new double[256];

int total =0;
for (ih = 0; ih < 256; ih++ )

