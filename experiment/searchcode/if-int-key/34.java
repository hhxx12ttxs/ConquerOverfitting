
public class KeyInternal {

private Node node;
private Key key;
private int keyVal;

public KeyInternal(Node node, Key key){
this.node = node;
this.key = key;
if(key != null)
keyVal = (Integer)key.getValue();

