List<String> infoMessages = com.manydesigns.elements.messages.SessionMessages.consumeInfoMessages();

if (!errorMessages.isEmpty()) {
xb.openElement(&quot;div&quot;);
xb.addAttribute(&quot;class&quot;, &quot;alert alert-danger alert-dismissable fade in&quot;);
writeCloseButton(xb);
writeList(xb, errorMessages, &quot;errorMessages&quot;);
xb.closeElement(&quot;div&quot;);
}

if (!warningMessages.isEmpty()) {

