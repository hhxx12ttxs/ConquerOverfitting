* You should have received a copy of the GNU Affero General Public License along with this program.
* If not, see http://www.gnu.org/licenses/.
public double countExample(double label, double predictedLabel) {
double diff = Math.abs(label - predictedLabel);
double absLabel = Math.abs(label);
if (Tools.isZero(absLabel)) {

