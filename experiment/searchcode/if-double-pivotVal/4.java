// input[lo] = pivotVal
// terminates whhen processIndex > hi
while (processIndex <= hi) {
if (input[processIndex] ==  pivotVal) {
input[partEndIndex + 1] =  pivotVal;
partEndIndex++;
} else if (input[processIndex] < input[partStartIndex]) {

