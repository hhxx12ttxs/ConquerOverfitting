checkAtElement(xpp, name, context);
String val = null;
if (complex) {
val = readText(xpp);
} else if (xpp.getEventType() == TEXT) {
val = xpp.getText();
private void checkAtElement(XmlPullParser xpp, String name, String context) throws XmlPullParserException {
if (xpp.getEventType() != START_TAG)
}
if (xpp.getEventType() != END_TAG) {
throw new XmlPullParserException(Messages.DefinitionParser_54+context);
while (xpp.getEventType() != END_TAG) {
if (xpp.getEventType() == TEXT) {
bldr.append(xpp.getText());
throw new XmlPullParserException(Messages.DefinitionParser_55+name+Messages.DefinitionParser_56+Integer.toString(xpp.getEventType())+Messages.DefinitionParser_57+context);
private String readElement(XmlPullParser xpp, String name, String context, boolean complex) throws XmlPullParserException, IOException {

