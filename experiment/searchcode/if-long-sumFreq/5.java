public class FrequencyList implements Iterable<Frequency>, ICopy, Serializable{

/**
* Serializable ID.
*/
private static final long serialVersionUID = -8303954416976988023L;
private boolean setFreq(Hashtable<Integer,Frequency> table,int value,float freq){
if(table.containsKey(value)==false)
return false;

Frequency t=table.get(value);

