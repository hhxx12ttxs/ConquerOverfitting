base += Math.min(skipped, lCount - skipped);
}
if (lCount == 1)
return 0;
if (lCount % 2 == 0) {
int middle = -1;
if (lCount == 2 * skipped) {
middle = i;
break;
}
}
long answer = base;
for (int i = 0; i < N; i++) {

