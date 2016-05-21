        assertEquals(true, IslamicChronology.getInstance().weekyears().isSupported());
        assertEquals(true, IslamicChronology.getInstance().months().isSupported());
        assertEquals(true, IslamicChronology.getInstance().weeks().isSupported());
        }
        System.out.println(\"\\nTestIslamicChronology.testCalendar\");
        DateTime epoch = new DateTime(1, 1, 1, 0, 0, 0, 0, ISLAMIC_UTC);
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
        assertEquals(false, IslamicChronology.getInstance().weekyears().isPrecise());
        assertEquals(false, IslamicChronology.getInstance().weeks().isPrecise());
        assertEquals(\"weekyears\", IslamicChronology.getInstance().weekyears().getName());
        assertEquals(\"months\", IslamicChronology.getInstance().months().getName());
        assertEquals(\"weeks\", IslamicChronology.getInstance().weeks().getName());
        assertEquals(false, IslamicChronology.getInstance().months().isPrecise());

