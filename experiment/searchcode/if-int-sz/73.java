public class QuickUW{
private int[] id;
private int[] sz;
public QuickUW(int N){
id=new int[N];
sz=new int[N];
for(int i=0;i<N;i++){
public void union(int p,int q){
int i = root(p);
int j = root(q);
if(sz[i]<sz[j]){
id[i]=j;
sz[j]+=sz[i];
}
else{
id[j]=i;
sz[i]+=sz[j];
}
}
}

