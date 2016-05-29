writeList(xb, errorMessages, &quot;errorMessages&quot;);
xb.closeElement(&quot;div&quot;);
}

if(!warningMessages.isEmpty()) {
writeList(xb, warningMessages, &quot;warningMessages&quot;);
xb.closeElement(&quot;div&quot;);
}

if(!infoMessages.isEmpty()) {

