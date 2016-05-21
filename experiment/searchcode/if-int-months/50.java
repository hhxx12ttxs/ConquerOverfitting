 */
int spanMonth = calendar.get(Calendar.MONTH);
int months = (spanYear - firstYear)*12 + (spanMonth - firstMonth);
return months;
public static int dateDiff (Date a, Date b) {
return (int)MathUtils.divLongNotSuck(roundDate(b).getTime() - roundDate(a).getTime() + DAY_IN_MS / 2, DAY_IN_MS);
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
int target_dow = -1, current_dow = -1, diff;
int offset = (includeToday ? 1 : 0);
 */
/*public static int getMonthsDifference(Date earlierDate, Date laterDate) {
Date span = new Date(laterDate.getTime() - earlierDate.getTime());

