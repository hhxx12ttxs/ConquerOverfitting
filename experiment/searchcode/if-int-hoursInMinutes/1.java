int newHours = hours + other.hours;
int newMinutes = minutes + other.minutes;
if (newMinutes > MAXIMUM_MINUTES) {
private void cantBeBiggerThan(int input, int maximum) {
if (input > maximum) {
throw new IllegalArgumentException(

