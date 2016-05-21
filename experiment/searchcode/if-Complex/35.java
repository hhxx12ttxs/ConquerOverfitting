public Boolean getComplex() {
return this.dosingType != DosingType.SIMPLE;
/**
 * Sets whether this drug is complex
 * 
 * @param complex
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
public void setComplex(Boolean complex) {
if (complex) {
setDosingType(DosingType.FREE_TEXT);
 * @deprecated use {@link #setComplex(Boolean)}
@Deprecated
@Deprecated

