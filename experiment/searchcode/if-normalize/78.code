public int rotateToNearest(int n, int current, int[] target) {
MODULO = n;
current = normalize(current);
int result = calcDistance(current, normalize(target[0]));
result = min(result, calcDistance(current, normalize(target[i])));
}
return result;
}

int normalize(int pos) {
return (int) (((long) pos % MODULO + MODULO) % MODULO);

