help(A, i);
}

}

public void help(int[] A, int idx){
int leftIdx = idx*2+1;
int rightIdx =  idx*2+2;
if(rightIdx > A.length-1) rightIdx = leftIdx;

int minIdx = A[leftIdx] <= A[rightIdx] ? leftIdx: rightIdx;

if(A[idx] > A[minIdx]){

