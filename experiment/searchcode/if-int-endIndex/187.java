package au.edu.mq.comp.junitGrading;



import au.edu.mq.comp.common.Sscanf;
import java.util.HashMap;


/**
 * 
 * @author pongsak suvanpong psksvp@gmail.com
 * parse iLearn download for id no. and name
 * 
 * the parse the following format
 * 40317471 Murray Doyle_car_129064.java
 * 
 * the pattern is below(base one c stdio.h scanf
 * (%s %s_%s_%s.java)-(id, name, class, magic)
 * 
 * TODO: need to write more doc on here
 *
 */
public class MQiLearnIdentificationTokenizer extends IdentificationTokenizer 
{
    String format;
    String[] symbols;
    
    public MQiLearnIdentificationTokenizer(String formatInfo)
    {   
        //TODO: need to put error handling
        String[] components = formatInfo.trim().split("-");
        this.format = components[0].substring(1, components[0].length() - 1);
        symbols = components[1].substring(1, components[1].length() - 1).split(",");
        for(int i = 0; i < symbols.length; i++)
        {
           this.symbols[i] = this.symbols[i].trim();
        }
    }
    
    @Override
    public Identification parse(String data) 
    {
        //TODO: need to put error handling
        Object[] variables = Sscanf.scan(data, this.format, (Object[])this.symbols);
        HashMap<String, String> symbolsMap = new HashMap<String, String>();
        for(int i = 0; i < this.symbols.length; i++)
        {
          symbolsMap.put(this.symbols[i], (String)variables[i]);
        }
        
        Identification id = new Identification(symbolsMap.get("id"), 
                                               symbolsMap.get("name"),
                                               symbolsMap.get("magic"));
        
        return id;     
    }
    
	
	
	/*
	public static void main(String[] args)
	{
	    {
	        String testMe = "42471117 Caniute Francelie Panuraj_Car_32010.java";
	        MQiLearnIdentificationTokenizer p = new MQiLearnIdentificationTokenizer("(%s %s_%s_%s.java)-(id, name, class, magic)");
	        Identification id = p.parse(testMe);
	        System.out.println(id);
	    }
	    
	    {
            String testMe = "David Politis_23213_assignsubmission_file_GameBarriers.java";
            MQiLearnIdentificationTokenizer p = new MQiLearnIdentificationTokenizer("(%s_%s_%s.java)-(name, magic, dontcare)");
            Identification id = p.parse(testMe);
            System.out.println(id);
        }
	}  */

}



/*
@Override
public Identification parse(String data) 
{
    if(data.length() < 8)
        return new Identification("", data, data);
    else
    {
        String id = data.substring(0, 8); 
        int endIndex = data.indexOf('_', 9);
        if(endIndex < 0)
            endIndex = data.length();
        
        int startIndex = 9; 
        if(startIndex >= data.length())
            startIndex = 0;
        
        String firstName = data.substring(startIndex, endIndex);
        
        startIndex = endIndex + 1;
        if(startIndex >= data.length())
            startIndex = 0;
        
        endIndex = data.indexOf('_', startIndex);
        if(endIndex < 0)
            endIndex = data.length();
        
        String lastName = data.substring(startIndex, endIndex);
        return new Identification(id, firstName, lastName);
    }
} */

