* This the the superclass for all the request objects.
*/
public class Request extends Packet {

/** The idkey of the server, this must always be included with the packet,
* If its not set, the packet will instantly fail. */
private String idKey;

