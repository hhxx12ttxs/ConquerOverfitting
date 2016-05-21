        assertEquals(true, BuddhistChronology.getInstance().weekyears().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().months().isSupported());
        assertEquals(true, BuddhistChronology.getInstance().weeks().isSupported());
    private static int SKIP = 1 * DateTimeConstants.MILLIS_PER_DAY;
    
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
        assertEquals(false, BuddhistChronology.getInstance().weekyears().isPrecise());
        assertEquals(false, BuddhistChronology.getInstance().months().isPrecise());
        assertEquals(false, BuddhistChronology.getInstance().weeks().isPrecise());
        assertEquals(\"weekyears\", BuddhistChronology.getInstance().weekyears().getName());
        assertEquals(\"months\", BuddhistChronology.getInstance().months().getName());
        assertEquals(\"weeks\", BuddhistChronology.getInstance().weeks().getName());

