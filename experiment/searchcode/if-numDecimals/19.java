
public class Header {
	String type;
	String name;
	int places;
	int dec;
	boolean notNull;
	
	public Header(String dataType, String fieldName, int numPlaces, boolean isNotNull){
		type = dataType;
		name = fieldName;
		places = numPlaces;
		notNull = isNotNull;
	}
	
	public Header(String dataType, String fieldName, int numPlaces, int numDecimals, boolean isNotNull){
		type = dataType;
		name = fieldName;
		places = numPlaces;
		dec = numDecimals;
		notNull = isNotNull;
	}
	
	public String getType(){
		return type;
	}
	
	public String getName(){
		return name;
	}
	
	public int getPlaces(){
		return places;
	}
	
	public int getDec(){
		return dec;
	}
	
	public boolean notNull(){
		return notNull;
	}
	
	public String toString(){
		String retString = name;
		if((places + dec) > 0)
			while(retString.length() < (places + dec))
				retString = retString + " ";
		else if(type.contentEquals("DATE"))
			while(retString.length() < 10)
				retString = retString + " ";
		else
			retString = retString + "   ";
		return retString;
	}
}

