// X^(-n) = X^(n + 1) * X
// X^n = 1/(x^(-n))
if (n < 0) {
double ret = x * pow(x, -(n + 1));
return (double)1/ret;
}

// å°†æ±‚powå¯¹åŠåˆ†ã?‚å†å°†ç»“æžœç›¸ä¹?

