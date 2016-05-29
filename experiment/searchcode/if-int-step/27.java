* @param totalStep
* @param allowStep 允许的步伐，按从小到大排序
* @return
*/
private static int compute(int currentStep, int totalStep, int[] allowStep) {
int compute = 0;
for(int i=0; i<allowStep.length; i++) {

