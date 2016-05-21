@Test
@Verifies(value = \"should fail validation if prn is null\", method = \"validate(Object,Errors)\")
public void validate_shouldFailValidationIfPrnIsNull() throws Exception {
DrugOrder order = new DrugOrder();
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
DrugOrder order = new DrugOrder();
order.setComplex(null);
order.setDrug(Context.getConceptService().getDrug(3));
@Test
@Verifies(value = \"should fail validation if complex is null\", method = \"validate(Object,Errors)\")
public void validate_shouldFailValidationIfComplexIsNull() throws Exception {
import org.openmrs.test.BaseContextSensitiveTest;
import org.openmrs.test.Verifies;
import org.springframework.validation.BindException;

