if(A == null)   return 0;
int len = A.length;
if(len <= 2)    return len;

int i = 2;
for(int j = i; j < len; j++) {
if(A[i - 2] != A[j]) {
A[i] = A[j];
i++;
}
}
return i;
}
}

