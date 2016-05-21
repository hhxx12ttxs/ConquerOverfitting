import com.google.gwt.dev.js.ast.JsFunction;
import com.google.gwt.dev.js.ast.JsIf;
import com.google.gwt.dev.js.ast.JsInvocation;
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
  /**
   * Determines if the evaluation of a JsNode may be affected by side effects.
   */
    /*
     * TODO: Most of the special casing below can be removed if complex
     * statements always use blocks, rather than plain statements.
      double ratio = ((double) inlinedComplexity) / originalComplexity;
      if (ratio > MAX_COMPLEXITY_INCREASE
          && isInvokedMoreThanOnce(invokedFunction)) {

