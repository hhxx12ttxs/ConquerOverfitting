for (int i = 0; i < US_STATES.length - 1; i++) {
if (cummulative.get(US_STATES[i]) == null) {
continue;
} else {
double value = cummulative.get(US_STATES[i]);
return false;
}

/**
* @param cn
*/
private static boolean checkForMax(CityNode cn) {
if (maxGrowth[0] == null) {

