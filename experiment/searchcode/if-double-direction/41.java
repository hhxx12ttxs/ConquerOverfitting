this.direction = minDirection
+ (rnd.nextDouble() * (maxDirection - minDirection));
if (rnd.nextBoolean())
this.direction = -this.direction;
updateValue();
}

double getValue() {
count++;
if (count >= changeInterval) {

