public int FindMinValue(int[] A) {
// TODO Auto-generated method stub
if (A == null) {
return -1;
}
int startIndex = 0;
int endIndex = A.length-1;
// TODO Auto-generated method stub
int key = a[startIndex];
for (int i = 1; i <= endIndex; i++) {
if (a[i] < key) {
key = a[i];
}
}
return key;
}

}

