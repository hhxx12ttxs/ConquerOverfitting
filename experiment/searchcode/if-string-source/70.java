public abstract class ViewEvent {
private ViewEventSource source;
public ViewEvent(ViewEventSource source){
if(source == null){
StringBuilder msg = new StringBuilder();
msg.append(&quot;Error, event source can&#39;t be null !&quot;);

