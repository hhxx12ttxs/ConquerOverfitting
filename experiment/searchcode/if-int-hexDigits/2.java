public static void convert (int decNumber) {
int a[] = new int[32]; //  массив разрядов двоичного числа
int[] hexDigits = new int[8];// массив разрядов шестнадцатиричного числа
for (int q = 0; q < 8; q++) {
for (int j = 0 ; j < 4 ; j++) {
if (a[j + 4 * q] == 1) {

