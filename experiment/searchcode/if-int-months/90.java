    if (key.equals(_key_calendar_dates_start[0]) && qName.equals(_key_calendar_dates_start[1]) && keyOperationDays.length() == 0) {
    keyNested = _key_calendar_dates_start[1];
    }
    if (key.equals(_key_calendar_dates_start[0]) && keyNested.equals(_key_calendar_dates_start[1]) && qName.equals(_key_calendar_dates_start[2])) {
    keyOperationDays = _key_calendar_dates_start[2];
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
int i;
ArrayList bankHolidays;
HashMap years = new HashMap(); //  years as HashMap to maintain unique entries
ArrayList yearsList = new ArrayList(); // years as list to allow iterating through years
    super.startElement(uri, name, qName, atts);
    if (qName.equals(_key_calendar_dates_start[0])) // also covers no_dates and bank holidays
key = _key_calendar_dates_start[0];

