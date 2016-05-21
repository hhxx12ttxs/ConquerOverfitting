    public static boolean isComplex(Element fontElement, BridgeContext ctx) {
        NodeList glyphElements = fontElement.getElementsByTagNameNS
            for (;child != null; child = child.getNextSibling()) {
                if (child.getNodeType() != Node.ELEMENT_NODE)
                    continue;
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
    protected BridgeContext ctx;
    protected Boolean complex = null;
    public boolean isComplex() {
        if (complex != null) return complex.booleanValue();
        boolean ret = isComplex(fontElement, ctx);

