int[] results = new int[2];
while (range != random) {
if (range > random) {
range /= 2;
if (range == random) {
results[1] += 1;
results[0] = range;
return results;
} else if (range > random) {

