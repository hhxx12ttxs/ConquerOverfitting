public static int[] calculateMinSize(int minWidth,int minHeight,int width,int height){
double ratio=(double)width/height;
double minRatio=(double)minWidth/minHeight;
int[] result=new int[2];
if(ratio>minRatio){
result[1]=minHeight;
result[0]=(int) Math.ceil(minHeight*ratio);

