3:    minpos = j;
4:    for i = j + 1,...,n {
5:        if A[i].key < A[minpos].key then {
6:            minpos = i;
7:        }
8:    }
9:    if minpos > j then {
10:       Vertausche A[minpos] mit A[j];
11:   }

