@Override
public void sort(int[] aArray) {
for (int i = 0; i < aArray.length; i++) {
int minPos = i;
for (int j = i; j < aArray.length; j++) {
if(aArray[j]<aArray[minPos]){
minPos = j;
}
}
Utility.exchange(aArray, i, minPos);
}

}

}

