array = a;
quicksort(0,a.length-1);
}

private static void quicksort(int lower, int upper){
int index = partition(lower, upper);
if(lower < index-1)
quicksort(lower,index-1);

if(index < upper)

