* it into reduced row echelon form
*
*/
public void eliminate() {
int startColumn = 0;
for (int row=0; row<augmentedMatrix.length; row++) {
while (!switched &amp;&amp; i<augmentedMatrix.length) {
if(augmentedMatrix[i][startColumn]!=0.0){
double[] temp = augmentedMatrix[i];

