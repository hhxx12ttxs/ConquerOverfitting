package howto.constrainedOptimization.mathprog;

import howto.constrainedOptimization.Settings;

public class Parameter {
	public static enum Type{
		JOIN, ADJ, KEY, CONST
	};
	
	private String name = null;
	private Set[] set = null;			//we assume that this can be of size 2 max
	private String selectAttribute = "";	//store the attribute names the parameter corresponds to.
	private Type parameterType = null;
	protected String upperBound = null;		//we assume that bounds are given with the inequality symbol: "<", "<=" etc
	protected String lowerBound = null;
	protected String fixedValue = null;
	
	private String partitionPredicate = null;
	
	public Parameter(){
		
	}
	
	/**
	 * Create a parameter based on a name and set it corresponds to.
	 * @param name
	 * @param set
	 */
	public Parameter(String name, String set){
		this.name = name;
		this.set = new Set[1];
		this.set[1] = new Set(set);
	}
	
	/**
	 * Create a parameter based on a name and sets it corresponds to.
	 * @param name
	 * @param set
	 */
	public Parameter(String name, Set[] set){
		this.name = name;
		this.set = set;
	}
	
	/**
	 * Create a parameter based on a name and sets it corresponds to.
	 * @param name
	 * @param set
	 */
	public Parameter(String name, Set set){
		this.name = name;
		this.set = new Set[1];
		this.set[0] = set;
	}
	
	/**
	 * Create a parameter based on a name and sets it corresponds to.
	 * @param name
	 * @param set
	 * @param attr
	 */
	public Parameter(String name, Set set, String attr){
		this.name = name;
		this.set = new Set[1];
		this.set[0] = set;
		this.selectAttribute = attr;
	}
	
	public String getSelectAttribute(){
		return this.selectAttribute;
	}
	
	/**
	 * Returns the name of the parameter
	 * @return
	 */
	public String getName(){
		return this.name;
	}
	
	/**
	 * Returns the name of the parameter augmented by s, e.g LI2tid[j] (s="[j]")
	 * or "1" if the parameter is always 1
	 * @return
	 */
	public String getNameValue(String s){
		if((this.fixedValue != null) && Settings.optimizeParameters)
			return this.fixedValue;
		else
			return this.name + s;
	}
	
	public Set[] getSet(){
		return this.set;
	}
	
	/**
	 * Retrieves the name of the ith set (index) of this parameter
	 * @param i
	 * @return
	 */
	public String getSetName(int i){
		if(this.set == null || this.getSetSize() < i - 1)
			return null;
		
		return this.set[i].getName();
	}
	
	public int getSetSize(){
		if(this.set == null)
			return 0;
		
		return this.set.length;
	}
	
	public void addSet(Set set){
		Set[] s = new Set[this.getSetSize() + 1];
		
		if(this.getSetSize() > 0)
			System.arraycopy(this.set, 0, s, 0, this.getSetSize());
		
		s[this.getSetSize()] = set;
		this.set = new Set[this.getSetSize() + 1];
		System.arraycopy(s, 0, this.set, 0, this.getSetSize());
	}
	
	public void setSelectAttribute(String attr){
		this.selectAttribute = attr;
	}
	
	public String toString(){
		if(this.name == null || this.set == null)
			return null;	
		
		if((this.fixedValue != null) && Settings.optimizeParameters)
			return "";					//do not print the parameter if it is equal to 1 always
		
		if(this.getSetSize() > 1)
			return "param " + this.name + " {i in " + this.getSetName(0) +", j in " + this.getSetName(1) +"} " +
					this.getBounds() + ";\n";
		else
			return "param " + this.name + " {i in " + this.getSetName(0) +"} " +
					this.getBounds() + ";\n";
	}
	
	/**
	 * Prints the header for the data section for this parameter
	 * @return
	 */
	public String dataSectionString(){
		if(this.getSetSize() > 1)
			return "param " + this.name + ": ";
		else
			return "param " + this.name + ":=\n";
	}
	
	public String getMainParameterTable(){
		if(this.parameterType == Type.CONST || this.parameterType == Type.KEY){
			return this.getSetName(0);
		}
		else{
			return this.getSetName(0).replace("CORE", "MAP");
		}
	}
	
	public Type getType(){
		return this.parameterType;
	}
	
	public void setType(Type t){
		this.parameterType = t;
	}
	
	/**
	 * Checks if the value of this variable is always 1.
	 * @return
	 */
	public boolean isAlwaysOne(){
		if(this.fixedValue == null )
			return false;
		
		return (this.fixedValue == "1");
	}
	
	public String getLowerBound(){
		if(this.lowerBound == null)
			return "";
		
		return this.lowerBound;
	}
	
	public String getUpperBound(){
		if(this.upperBound == null)
			return "";
		
		return this.upperBound;
	}
	
	public String getfixedValue(){
		if(this.fixedValue == null)
			return "";
		return this.fixedValue;
	}
	
	public void setLowerBound(String s){
		this.lowerBound = s;
	}
	
	public void setUpperBound(String s){
		this.upperBound = s;
	}
	
	public void setValue(String s){
		this.fixedValue = s;
	}
	
	public String getBounds(){
		if(this.fixedValue == null)
			return  this.getLowerBound() + " " + this.getUpperBound();
		else
			return "=" + this.fixedValue;
	}
	
	public String getPartitionPredicate(){
		return this.partitionPredicate;
	}
	
	public void setPartitionPredicate(String s){
		this.partitionPredicate = s;
	}
	
}

