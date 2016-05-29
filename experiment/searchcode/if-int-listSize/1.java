ArrayList<String> stack = new ArrayList<String>();
int listSize;

public void push(String pusher) {
stack.add(pusher);
return null;
}
public String peek(int index) {
if (index < listSize &amp;&amp; index >= 0) {
return stack.get(listSize - index - 1);

