items.add(item);
return this;
}

public String asXml() {
XmlStringBuilder xb = new XmlStringBuilder();
for (Representation item : items) {
xb.openContainerNode(&quot;item&quot;);
xb.newline();

if (item.hasRelation(&quot;self&quot;)) {

