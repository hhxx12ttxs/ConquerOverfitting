        }
        System.out.println(\"\\nTestCopticChronology.testCalendar\");
        DateTime epoch = new DateTime(1, 1, 1, 0, 0, 0, 0, COPTIC_UTC);
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
            int dowValue = dayOfWeek.get(millis);
            int doyValue = dayOfYear.get(millis);
            int dayValue = dayOfMonth.get(millis);
        assertEquals(\"weekyears\", CopticChronology.getInstance().weekyears().getName());
        assertEquals(\"months\", CopticChronology.getInstance().months().getName());
        assertEquals(\"weeks\", CopticChronology.getInstance().weeks().getName());
    public void testCalendar() {
        if (TestAll.FAST) {
            return;

