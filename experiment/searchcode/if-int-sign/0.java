int c = a -  b;

int signA = sign(a);
int signB = sign(b);
int signC = sign(c);

// if a and b have different signs, then k = sign(a)
int useSignA = signA ^ signB;

// if a and b have the same sign, the k = sign(a-b)
int useSignC = flip(signA ^ signB);

