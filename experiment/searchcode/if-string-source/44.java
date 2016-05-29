int initialSourceIndex = 0;
ArrayList<String> newOrderings = orderings(new ArrayList<String>(), data, initialSourceIndex);
public static ArrayList<String> orderings(ArrayList<String> result, ArrayList<String> source,int sourceKeyIndex ) {

if(sourceKeyIndex==source.size())

