int[] numbers = gb.getNumbers();
double fracMine = (numbers[0] - numbers[1]) / (gb.n * gb.n);


if (gb.isOver() || gb.n < 5) {
sum += 1*weight;
}
if (gb.isBlack(i)) {
double weight = (isSafe(i,gb) ? safeWeight: 1);

