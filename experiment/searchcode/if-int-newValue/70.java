public boolean isHappy(int n) {
Set<Integer> existingValues = new HashSet<>();
existingValues.add(n);
int newValue = n;
while (true) {
int oldValue = newValue;
newValue = 0;
while (oldValue != 0) {

