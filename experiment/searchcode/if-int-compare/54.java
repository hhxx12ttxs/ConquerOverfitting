int[] sorted = sortDie(die);
int counter = 0;
int anotherCounter = 0;
int compare = sorted[0];
int sum = 0;
for(int i = 0; i < sorted.length-1; i++){
if(compare == sorted[i])
counter++;
}

for(int i = sorted.length-1; i > 0; i--){

