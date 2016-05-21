            type = type.withMonthsRemoved();
            cache.put(years(), years());
            cache.put(months(), months());
            cache.put(weeks(), weeks());
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
        Object cached = cache.get(inPartType);
        if (cached instanceof PeriodType) {
            return (PeriodType) cached;
        }
        if (cached != null) {
            throw new IllegalArgumentException(\"PeriodType does not support fields: \" + cached);
        }
        if (list.remove(DurationFieldType.months()) == false) {

