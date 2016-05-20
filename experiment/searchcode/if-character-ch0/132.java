/*
 * LookupState.java
 *
 * Created on January 23, 2007, 9:00 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */


package hash;

 import java.util.StringTokenizer;

/**
 *
 * @author Kyle Johnson
 */
public class LookupState
{
    private static String stateNames[] = new String[676];
    
    /** Creates a new instance of LookupState 
     *
     * Currently, a list from USPS.com has been copy and pasted into the code
     * and is parsed using a StringTokenizer while being added to the hash
     * table.
     */
    public LookupState()
    {
	String namesUnparsed = 
	"ALABAMA                         AL\n" +
	"ALASKA                          AK\n" +
	"ARIZONA                         AZ\n" +
	"ARKANSAS                        AR\n" +
	"CALIFORNIA                      CA\n" +
	"COLORADO                        CO\n" +
	"CONNECTICUT                     CT\n" +
	"DELAWARE                        DE\n" +
	"FLORIDA                         FL\n" +
	"GEORGIA                         GA\n" +
	"GUAM                            GU\n" +
	"HAWAII                          HI\n" +
	"IDAHO                           ID\n" +
	"ILLINOIS                        IL\n" +
	"INDIANA                         IN\n" +
	"IOWA                            IA\n" +
	"KANSAS                          KS\n" +
	"KENTUCKY                        KY\n" +
	"LOUISIANA                       LA\n" +
	"MAINE                           ME\n" +
	"MARYLAND                        MD\n" +
	"MASSACHUSETTS                   MA\n" +
	"MICHIGAN                        MI\n" +
	"MINNESOTA                       MN\n" +
	"MISSISSIPPI                     MS\n" +
	"MISSOURI                        MO\n" +
	"MONTANA                         MT\n" +
	"NEBRASKA                        NE\n" +
	"NEVADA                          NV\n" +
	"NEW_HAMPSHIRE                   NH\n" +
	"NEW_JERSEY                      NJ\n" +
	"NEW_MEXICO                      NM\n" +
	"NEW_YORK                        NY\n" +
	"NORTH_CAROLINA                  NC\n" +
	"NORTH_DAKOTA                    ND\n" +
	"OHIO                            OH\n" +
	"OKLAHOMA                        OK\n" +
	"OREGON                          OR\n" +
	"PENNSYLVANIA                    PA\n" +
	"RHODE_ISLAND                    RI\n" +
	"SOUTH_CAROLINA                  SC\n" +
	"SOUTH_DAKOTA                    SD\n" +
	"TENNESSEE                       TN\n" +
	"TEXAS                           TX\n" +
	"UTAH                            UT\n" +
	"VERMONT                         VT\n" +
	"VIRGINIA                        VA\n" +
	"WASHINGTON                      WA\n" +
	"WEST_VIRGINIA                   WV\n" +
	"WISCONSIN                       WI\n" +
	"WYOMING                         WY";
	
	StringTokenizer tok = new StringTokenizer(namesUnparsed);
	
	while (tok.hasMoreTokens())
	{
	    add(tok.nextToken(), tok.nextToken());
	}
	
    }
    
    /**
     *
     */
    public static void add (String name, String abbr)
    {
	abbr = abbr.toUpperCase();
	int index = lookupIndex(abbr);
	if(stateNames[index] != null)
	    stateNames[index] = name;
	else
	{
	    for (int i = index; i < stateNames.length; i++)
	    {
		if(stateNames[index] == null)
		{
		    stateNames[index] = name;
		    return;
		}
	    }
	}
    }

    public static String find (String abbr)
    {
	if(stateNames[lookupIndex(abbr)] != null)
	    return stateNames[lookupIndex(abbr)].replaceAll("_", " ");
	return "";
    }

    private static int lookupIndex (String abbr)
    {
	if(abbr.length() != 2)
	    return 0;
	
	int ch0 = Character.getNumericValue(abbr.charAt(0)),
	    ch1 = Character.getNumericValue(abbr.charAt(1)),
	    a = Character.getNumericValue('A');
	
	int index = (ch0 - a) + 26 * (ch1 - a);
	return index;
    }
    
    public static void debug_printAll()
    {
	for (int i = 0; i < stateNames.length; i++)
	{
	    for (int j = 0; j < stateNames.length; j++)
	    {
		String name = find(((char)i) + "" + ((char)j));
		if(name != null)
		    System.out.println(" " + name);
	    }
	}
    }
    
}

