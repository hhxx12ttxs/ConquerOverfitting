        int index = resultAsList.indexOf(destValue);
        int index = result.indexOf(destValue);
 * Internal Mapping Engine. Not intended for direct use by Application code.
 * This class does most of the heavy lifting and is very recursive in nature.
 * <p/>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
   * @param destObj   destination object
   * @param mapId     mapping identifier
   * @param <T>       destination object type
        // perform an update if complex type - can't map strings
        Object obj = resultAsList.get(index);
 * limitations under the License.
        // perform an update if complex type - can't map strings
        Object obj = result.get(index);

