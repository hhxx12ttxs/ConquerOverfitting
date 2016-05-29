binarySearch(num, 0, 9, key);
}

public static void binarySearch(int array[], int lowerbound, int upperbound, int key) {
int position;
int comparisonCount = 1;

position = (lowerbound + upperbound) / 2;
while ((array[position] != key) &amp;&amp; (lowerbound <= upperbound)) {

