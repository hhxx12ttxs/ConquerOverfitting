List<IMetadataTable> metadatas = node.getMetadataList();
String CustomPattern = ElementParameterParser.getValue(node, &quot;__PATTERN__&quot;);
boolean bUseCustom = &quot;true&quot;.equals(ElementParameterParser.getValue(node, &quot;__CUSTOM_PATTERN__&quot;));
//may be dq pattern
String PatternList = ElementParameterParser.getValue(node, &quot;__PATTERN_LIST__&quot;);
if (bUseCustom) {

