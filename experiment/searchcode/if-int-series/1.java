Scanner scan = new Scanner(System.in);
String[] line ;
int[] series = new int[5];
while (scan.hasNext()) {
line = scan.nextLine().split(&quot; &quot;);
if ( line.length != 4)
continue;
for(int i=0; i< line.length ; i++) {

