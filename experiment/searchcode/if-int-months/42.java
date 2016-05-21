   */
  public static void addDaysToDate(Date date, int days) {
    date.setDate(date.getDate() + days);
  /**
   * Adds the given number of months to a date.
   * 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
   */
  public static void addMonthsToDate(Date date, int months) {
    if (months != 0) {
      int month = date.getMonth();
      int resultMonthCount = year * 12 + month + months;
 * the License.
      int resultYear = resultMonthCount / 12;

