*  along with this program.  If not, see http://www.gnu.org/licenses/.
*/
package com.rapidminer.operator.performance;
public double countExample(double label, double predictedLabel) {
double diff = Math.abs(label - predictedLabel);
double absLabel = Math.abs(label);
if (Tools.isZero(absLabel)) {

