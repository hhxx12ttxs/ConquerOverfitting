public XmNodePreOrderIterator(XmNode n)
{
stack = new Stack<XmNode>();
if(n != null) {
public boolean hasNext()
{
return !stack.empty();
}

@Override
public XmNode next()
{
if(stack.empty()) {

