DoublePoint  current = list.first; 			// αρχή της λίστας
DoublePoint  previous = current.next; 		// το δεύτερο στοιχείο της λίστας
while (current != null)
{
counter++;

if ((current.x > previous.x &amp;&amp; current.y >= previous.y) || (current.x >= previous.x &amp;&amp; current.y > previous.y))

