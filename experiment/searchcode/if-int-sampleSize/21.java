public static void main(String[] args) {

}


public int calcSampleSize(Options options, int reqWidth, int reqHeight){
int sampleSize = 1;
int width = options.outWidth;
int height = options.outHeight;

if(reqWidth > width || reqHeight > height){

