        if (im == 0 && o.im == 0) {
            return set(MoreMath.combinations(re, o.re), 0);
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
/** 
    A complex value, composed of the real part (re) and the imaginary part (im).
    All the methods that return a Complex (such as add(), mul(), etc)
    modify the object on which they are called and return it (this), in order
    to avoid new object creation.
 */
public class Complex {
    /** The real component. */
    public final Complex combinations(Complex o) {

