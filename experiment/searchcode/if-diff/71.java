* @see org.eclipse.emf.compare.diff.metamodel.impl.AbstractDiffExtensionImpl#visit(org.eclipse.emf.compare.diff.metamodel.DiffModel)
*/
public void visit(DiffModel diffModel) {
if(diffModel.getOwnedElements().size()==1) {
DiffElement rootDiff = (DiffElement) diffModel.getOwnedElements().get(0);
for(Object diff : rootDiff.getSubDiffElements()) {
if(diff instanceof DiffGroup) {

