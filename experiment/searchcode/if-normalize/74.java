public void normalizeNode(JwXmlNode e)
{
if ( e.isDocument() ) normalizeDocument(e.asDocument());
if ( e.isElement() ) normalizeElement(e.asElement());
if ( e.isText() ) normalizeText(e.asText());

