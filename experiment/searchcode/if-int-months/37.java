        }
        int y = (int) (months / 12);
        int m = (int) (months % 12);
        return factory.newDurationYearMonth(sign, y, m);
    public static Duration createDuration(final String literal, DurationType targetType) {
        if(targetType instanceof DayTimeDurationType) {
            return factory.newDurationDayTime(literal);
        } else if(targetType instanceof YearMonthDurationType) {
            return factory.newDurationYearMonth(literal);
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
        final boolean sign;
        if(months < 0L) {
            sign = false;

