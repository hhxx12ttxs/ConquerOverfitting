for (int i = input; i > 0; ) {
int iterationCount = i / 1000;
converted += appendCharacter(thousandCharacter, iterationCount);
i -= iterationCount * 1000;
if (i >= 900) {

