* You should have received a copy of the GNU Lesser General Public License
* along with jzwave.  If not, see <http://www.gnu.org/licenses/>.
*/
package net.rauros.jzwave.utils;
public static String parseASCIINullTerm(byte rawDatap[], int offset, int length)
{
int searchLength = 0;
while(searchLength < length &amp;&amp; rawDatap[offset + searchLength] != 0)

