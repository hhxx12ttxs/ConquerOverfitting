int minPos = i;
for (int j = i; j < vector.length; j++) {
if (vector[j] < vector[minPos]) {
minPos = j;
}
}
int temp = vector[i];
vector[i] = vector[minPos];
vector[minPos] = temp;
}
}
}

