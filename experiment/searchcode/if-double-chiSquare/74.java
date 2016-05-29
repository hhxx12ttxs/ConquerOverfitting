// PART B: Calculate chi-square - this approach is in Sedgewick
double n_r = (double) randomNums.size() / r;
double chiSquare = 0;

for (int v : ht.values()) {
double f = v - n_r;
chiSquare += f * f;
}
chiSquare /= n_r;

// PART C: According to Swdgewick: &quot;The statistic should be within

