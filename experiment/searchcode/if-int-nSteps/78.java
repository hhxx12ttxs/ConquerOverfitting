private static int getWater(int goal, int maxA, int maxB) {
if (goal > maxA &amp;&amp; goal > maxB) return Integer.MAX_VALUE;

int nSteps = 0;
NextStep next = NextStep.A_POUR_TO_B;
int jugA = maxA;

