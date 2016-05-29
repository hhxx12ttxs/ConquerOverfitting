double [] variance= new double[30];
double [] SD= new double[30];
double max= 0;
public CalculateSD(List<List<String>> SDlistOfLists, int[] size){
return SD;
}

public int MaxSD(){
int index=0;
for(int i=0;i<30;i++){
if(SD[i]>max){
max=SD[i];
index = i;
}
}
return index;
}
}

