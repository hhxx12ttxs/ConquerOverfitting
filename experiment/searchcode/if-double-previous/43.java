public static ListDoublePoints getSorted( ListDoublePoints list ) {

DoublePoint current = list.first;
DoublePoint previous = current.next;
while (current != null)
{
if ((previous.x + previous.y) > (current.x + current.y))

