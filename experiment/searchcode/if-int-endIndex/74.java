public static void permutate(int[] A, int startIndex, int endIndex){
if (startIndex==endIndex){
for (int i=0;i<A.length;i++){
System.out.print(A[i]);
}
System.out.println();
}else{
for (int i=startIndex;i<=endIndex;i++){

