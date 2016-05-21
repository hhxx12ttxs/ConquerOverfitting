IScope room1 = ScopeUtils.resolveScope(appScope, \"/junit/room13/subroomA\");
if (room1 == null) {
assertTrue(room.createChildScope(\"subroomA\"));
IScope room2 = ScopeUtils.resolveScope(appScope, \"/junit/room13/subroomB\");
if (room2 == null) {
assertTrue(room.createChildScope(\"subroomB\"));
if (complex == null) {
complex = new Complex();
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
log.debug(\"testPersistentCreation\");
if (appScope == null) {
appScope = (WebScope) applicationContext.getBean(\"web.scope\");
Complex complex = (Complex) so.getAttribute(\"complex\");

