public char[] getNormalizeFormBuf() {
if(normalizeFormBuf == null) normalizeFormBuf = normalizeForm.toCharArray() ;
public void reset(String text, boolean split) {
this.orginalForm = text ;
this.normalizeForm = orginalForm.toLowerCase() ;
if(split) {

