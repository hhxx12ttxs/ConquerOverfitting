public class AddValueToListPostProcessor implements PostProcessor {

private int index;
private Object valueToAdd;

public AddValueToListPostProcessor(int index, Object valueToAdd) {
this.index = index;

