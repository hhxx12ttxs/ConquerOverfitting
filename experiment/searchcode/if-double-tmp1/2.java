return A[1];
}

public double deleteMin(){
double tmp1 = A[1];
A[1] = A[count];
count = count-1;
int i = 1;
if ((2*i+1 <= count) &amp;&amp; (A[2*i+1] <= A[2*i])) m = 2*i+1;
if (A[m] >= A[i]) return tmp1;
//A[m] < A[i]
double tmp = A[m];
A[m] = A[i];
A[i] = tmp;
i = m;
}
return tmp1;
}
}

