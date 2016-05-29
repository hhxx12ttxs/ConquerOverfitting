Topic previousT = topicmap.getTopic(values.get(&quot;Role topic for the previous node&quot;));
if(previousT == null || previousT.isRemoved()) {
nextT = getOrCreateTopic(topicmap, LIST_GRAPH_SI+&quot;/&quot;+&quot;next&quot;, &quot;Next in list&quot;);
}

if(previousT.mergesWithTopic(nextT)) {

