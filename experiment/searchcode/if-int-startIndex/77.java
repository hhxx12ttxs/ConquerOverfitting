int[] array = {3, 12, 8};
permute(array, 0);
}

public static void permute(int[] array, int startIndex) {
if (array.length == startIndex) {
printVariations(array);
} else {
for (int i = startIndex; i < array.length; i++) {

