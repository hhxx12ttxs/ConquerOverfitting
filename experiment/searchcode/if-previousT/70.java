public void indent(final Filter filter, final LIST list, final long targetTaskId, final int delta) {
if(list == null)
return;

beforeIndent(list);
int indent = metadata.getValue(indentProperty());

int previousIndentValue = previoustIndent.get();
if(indent == previousIndentValue) { // sibling

