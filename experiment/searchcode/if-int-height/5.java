public int trap(int[] height) {
if(height==null||height.length==0)return 0;
// 找到最高值
int maxid=0;
for(int i=0;i<height.length;i++){
if(height[i]>height[maxid]){
maxid=i;
}
}
// 已经找到最高值
int water=0;

