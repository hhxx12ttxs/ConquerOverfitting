Parameters params = RunEnvironment.getInstance().getParameters();
int stopTick = (Integer) params.getValue(&quot;StopTick&quot;);
if(tick == stopTick){
int nec = (int) (normUtility.getScore(Dimension.Necessity, gComplaints) * 100); // Necessity
int level = nNetwork.getGeneralisationLevel(norm);
if(represented){
System.out.println(&quot;n:&quot;+norm.getName()+&quot;\t&quot;+nec+&quot;\t&quot;+level+&quot;\tGeneralised&quot;);

