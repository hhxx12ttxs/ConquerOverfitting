DOMUtils.createElementAndText(eMeetingRequest, &quot;Email:&quot;, &quot;InstanceType&quot;, &quot;0&quot;);
if(eventT.getLocation() != null &amp;&amp; !&quot;&quot;.equals(eventT.getLocation())) {
DOMUtils.createElementAndText(eMeetingRequest, &quot;Email:&quot;, &quot;Location&quot;, eventT.getLocation());
}
if(eventT.getOrganizerEmail() != null &amp;&amp; !&quot;&quot;.equals(eventT.getOrganizerEmail())) {

