public int minimize(String A, String B){
int lenA = A.length();
int lenB = B.length();
int diff;
int min_diff = lenB;
for(int it=0;it<=lenB-lenA;it++){
diff = 0;
for(int ind=0;ind<lenA;ind++){

