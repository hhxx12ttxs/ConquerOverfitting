public static List<Integer> Sort(List<Integer> myList) {
int listSize = myList.size();
int tmp = 0;
for (int i = 0; i < listSize; i++) {
for (int j = (listSize - 1); j >= (i + 1); j--) {
if (myList.get(j) < myList.get(j - 1)) {

