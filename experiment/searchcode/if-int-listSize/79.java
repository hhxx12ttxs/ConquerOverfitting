private void randomize() {
Random random = new Random();
for(int i = 0; i < listSize; i++) {
int randomIndex = random.nextInt(listSize);
while(i == randomIndex) {
randomIndex = random.nextInt(listSize);

