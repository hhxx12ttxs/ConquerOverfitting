public void setReplacementLength( int replacementLength ) {

if (replacementLength >= 0) {
this.replacementLength = replacementLength;
public boolean isValidFor( IDocument document, int offset ) {
if (offset < replacementOffset)
return false;

int replacementLength = replacementString == null ? 0 : replacementString.length();

