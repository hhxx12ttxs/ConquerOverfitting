public class DoubleNodeOperations<Item> {

public void insertAtBeginning(Item item, DoubleNode<Item> current) {
if(current.getPrevious() == null) {
current.setPrevious(new DoubleNode<Item>(item));
} else {
insertAtBeginning(item, current.getPrevious());

