public class serevr<T> implements Runnable{

Queue_by_list<Packet> Q2 ;
long mu;

/** constructor method
* @param list Q2 list is initialized
*/
public serevr(Queue_by_list<Packet> list ,long rate_of_server_)
{
Q2= list;

