public static void main(String args[])
{
int A[] = new int[]{40,50,11,32,55,68,75};
int B[] = new int[7];
System.arraycopy( A, 0, B, 0, A.length );
ts.add(40); ts.add(50); ts.add(11); ts.add(32); ts.add(55);
ts.add(68); ts.add(75);
int size = ts.size();
ts.pollFirst();

for(int i=1;i<size; i++)

