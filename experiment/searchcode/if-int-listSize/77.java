int listSize = random.nextInt(100);
System.out.println(&quot;List size: &quot;+listSize);
for (int i=0;i<listSize;i++){
System.out.print(list.get(i));
}

/*
* Solution
*/
int totalSteps = 0;
for (int i=0;i<listSize-1;i++){
totalSteps++;
if (list.get(i)==1){

