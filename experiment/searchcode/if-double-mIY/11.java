You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
public static Polygon createBoxPolygon(double mix, double max, double miy, double may) {
return GEOM.createPolygon(new Coordinate[] { coord(mix, miy), coord(mix, may), coord(max, may),

