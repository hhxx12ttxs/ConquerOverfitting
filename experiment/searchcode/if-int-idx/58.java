public static int getIndexOfLargestElementLesserThanOrEqualTo(int[] array,int ele) {
int startIdx = 0;
int endIdx = array.length-1;;
int midIdx = (startIdx + endIdx)/2;
while(startIdx < endIdx) {

