int beginIndex = 0, endIndex = 0;
while (endIndex < str.length()) {
if (str.charAt(endIndex) == separator) {
if (endIndex > beginIndex) {
tmpstr = str.substring(beginIndex, endIndex);
exploded.addElement(tmpstr);

