double tmp1 = score[b]     + scoreA;
double tmp2 = score_min[b] + scoreA;
double tmp3 = score[b-1]     * scoreB;
double tmp4 = score_min[b-1] * scoreB;

score[b]     = tmp1;
score_min[b] = tmp1;

