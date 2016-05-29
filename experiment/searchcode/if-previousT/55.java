TodoEffort tef = t.getLatestEffort();
TodoComment tcom = t.getLatestComment();
if(tss!=null &amp;&amp; tcom!=null &amp; tef!=null) {
todoDetailBean.setStatus(tss.getStatus()+&quot; / &quot;+tcom.getComment()+&quot; / &quot;+new SimpleDateFormat(&quot;dd MMM&quot;).format(tss.getDateOfChange()));
//nothing to do.
}


if(t.getAssignedTodos()!=null &amp;&amp; t.getAssignedTodos().size()>0) {
if(t.getAssignedTodos().iterator().next().getPersons()!=null &amp;&amp; t.getAssignedTodos().iterator().next().getPersons().size() > 0) {

