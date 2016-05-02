package subString;

import java.util.HashMap;
import java.util.Map;

public class SubStringFinder {

	/*this method will find the length of shortest substring in 
	string xx which contains all the characters from string yy in any order.*/
	public int getShortestSubStringLength(String xx, String yy){
		int minBegin =0, minEnd = 0, minLength = Integer.MAX_VALUE;
		int xxLength = xx.length();
		int yyLength = yy.length();
		
		
		if(xxLength >= yyLength){
			Map<Character, Integer> toFind = new HashMap<Character, Integer>();
			Map<Character, Integer> found = new HashMap<Character, Integer>();
			
			for(int j =0; j< xxLength ; j++){
				 found.put(xx.charAt(j), 0);
				 toFind.put(xx.charAt(j), 0);
			}
			
			for (int i = 0; i < yyLength; i++){
				  Character key = yy.charAt(i);
				  Integer value = toFind.get(key);
				  if(value == null){
					  value = 0;
				  }
				  value++;
				  toFind.put(key, value);
			 }
			 
			int charsFound = 0; 
			
			for(int begin =0, end = 0; end < xxLength ; end++){
				if(toFind.get(xx.charAt(end)) != 0){
					  Character key = xx.charAt(end);
					  Integer value = found.get(key) + 1;
					  found.put(key, value);
					if(found.get(key) <= toFind.get(key)){
						charsFound ++;
					}
				
					// When all chars in yy are found, try to find min length
					if(charsFound == yyLength){
						while(toFind.get(xx.charAt(begin)) == 0 || 
								 found.get(xx.charAt(begin)) > toFind.get(xx.charAt(begin))){
							if(found.get(xx.charAt(begin)) > toFind.get(xx.charAt(begin))){
								Integer value1 = found.get(xx.charAt(begin)) - 1;
								found.put(xx.charAt(begin), value1);
							}
							begin++;
						}
						
						int length = end - begin + 1;
						//update min values if new min length is found.
						if(length < minLength){
							minBegin = begin;
							minEnd = end;
							minLength = length;
						}
					}
				
				}
			}
		}
		if(minLength != Integer.MAX_VALUE){
			System.out.println("beginIndex="+minBegin);
			System.out.println("endIndex="+minEnd);
			System.out.println("SubString="+xx.substring(minBegin, minEnd+1));
		} else {
			System.out.println("substring not found");
		}
		
		return minLength;
	}
	
}

