// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>
public interface EventListener<EventT> {
public void onEvent(EventT event);
}

public final static class RouteUpdatedEvent{

