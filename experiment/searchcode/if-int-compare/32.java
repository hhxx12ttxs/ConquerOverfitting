private CompareU() {
}

public static <T extends Comparable<T>> int compare(final T left, final T right) {
int compare = 0;
if (left == null) {
compare = (right == null) ? 0 : -1;

