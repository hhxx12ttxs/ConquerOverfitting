along with jyang.  If not, see <http://www.gnu.org/licenses/>.

*/
import java.util.regex.Matcher;
import java.util.regex.Pattern;
private String path = null;

private Pattern path_arg = null;

public YANG_Path(int id) {
super(id);
try {
path_arg = Pattern

