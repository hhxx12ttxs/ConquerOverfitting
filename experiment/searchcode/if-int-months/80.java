        }
        System.out.println(\"\\nTestEthiopicChronology.testCalendar\");
        DateTime epoch = new DateTime(1, 1, 1, 0, 0, 0, 0, ETHIOPIC_UTC);
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
            int dowValue = dayOfWeek.get(millis);
            int doyValue = dayOfYear.get(millis);
            int dayValue = dayOfMonth.get(millis);
        assertEquals(\"weekyears\", EthiopicChronology.getInstance().weekyears().getName());
        assertEquals(\"months\", EthiopicChronology.getInstance().months().getName());
        assertEquals(\"weeks\", EthiopicChronology.getInstance().weeks().getName());
    public void testCalendar() {
        if (TestAll.FAST) {
            return;

