* GNU General Public License for more details.

* You should have received a copy of the GNU General Public License
* along with SuperPalitos. If not, see <http://www.gnu.org/licenses/>.
return stick1 < stick2 ? new Move(stick1, stick2) : new Move(stick2, stick1);
}

public static Move fromGaps(int gap1, int gap2) {
if (gap1 < 0 || gap2 < 0 || gap1 >= GameState.NGAPS || gap2 >= GameState.NGAPS) {

