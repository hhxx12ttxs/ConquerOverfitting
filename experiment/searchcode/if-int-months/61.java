        assertEquals(true, ISOChronology.getInstance().weekyears().isSupported());
        assertEquals(true, ISOChronology.getInstance().months().isSupported());
        assertEquals(true, ISOChronology.getInstance().weeks().isSupported());
            type == DurationFieldType.months() ||
        if (type == DurationFieldType.years() ||
        
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
        assertEquals(amt, diff);
        assertEquals(\"weekyears\", ISOChronology.getInstance().weekyears().getName());
        assertEquals(\"months\", ISOChronology.getInstance().months().getName());
        assertEquals(\"weeks\", ISOChronology.getInstance().weeks().getName());
            type == DurationFieldType.days()) {
        DurationField field = type.getField(ISOChronology.getInstanceUTC());
        int diff = field.getDifference(dtEnd.getMillis(), dtStart.getMillis());

