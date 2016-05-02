package agents;

import java.util.*;

public class KitStand {

    /*** Data Structures **/

    public int count;
    private int completeKits;

    public Kit[] kits;
    public InspectionStand inspection;

    public class InspectionStand {

        public Kit kit;
        public InspectionStand() { }

        synchronized public void placeOnInspection(Kit k)
        {
            kit = k;
        }

        synchronized public Kit inspectKit()
        {
            return kit;
        }

        synchronized public Kit removeFromInspection()
        {
            Kit k = kit;
            kit = null;
            return k;
        }
    }
    
    /*** Constructor **/

    public KitStand()
    {
        completeKits = 0;
        count = 0;
        kits = new Kit[2];
        inspection = new InspectionStand();
    } 

    /*** Actions ***/
    
    synchronized public void insertEmptyKit(Kit emptyKit) {
        kits[count] = emptyKit;
        count++;        
    }
    
    //for missing parts non normative scenario
    synchronized public void reinsertEmptyKit(Kit brokenKit) {
    	if (kits[0] == null){
    		kits[0] = brokenKit;
    	}
    	else
    		kits[1] = brokenKit;
    	count++;
    }

    synchronized public List<Map<String, Integer>> getPartConfig() {

        List<Map<String, Integer>> configs = new ArrayList<Map<String, Integer>>();
        if(kits[0] != null) 
    	{
        	configs.add(kits[0].config);
    	}
        if(kits[1] != null) 
        {
        	configs.add(kits[1].config);
        }
        return configs;
    }

    // called by PartRobot
    synchronized public Map<Part, Integer> insertPartsIntoKits(List<Part> send) {
    	
        Map<Part, Integer> parts = new HashMap<Part,Integer>();

        for(Part p: send)
        {
            String type = p.getPartName();
            if(kits[0]!=null && kits[0].config.containsKey(type) && !kits[0].bad)
            {
                int zero_num = kits[0].config.get(type);
                int zero_count = 0;
                for(Part zero_p : kits[0].parts)
                {
                    if(zero_p.getPartName().equals(type)) zero_count++;
                }

                if(zero_count < zero_num)
                {
                    kits[0].parts.add(p);
                    parts.put(p,2); // bottom kistand
                }
                else
                {
                    kits[1].parts.add(p);
                    parts.put(p,1);
                }
            }
            else
            {
                kits[1].parts.add(p);
                parts.put(p,1);
            }
        }
    
        return parts;
    }
    
    // called by PartRobot
    // updates the # of complete kits for the PartRobot
    synchronized public int updateCompleteKits()
    {
        int complete0 = 1;
        if(kits[0]!=null && !kits[0].bad)
        {
            for (String key : kits[0].config.keySet())
            {
                int num = kits[0].config.get(key);
                int count = 0;
                for(Part p : kits[0].parts)
                {
                    if(p.getPartName().equals(key)) count ++;
                }
                if(count < num)
                {
                    complete0 = 0;
                    break;
                }
            } 
            kits[0].complete = true; // this isn't true... but it doesn't really matter
        }

        int complete1 = 1;
        if(kits[1]!=null && !kits[1].bad)
        {
            for (String key : kits[1].config.keySet())
            {
                int num = kits[1].config.get(key);
                int count = 0;
                for(Part p : kits[1].parts)
                {
                    if(p.getPartName().equals(key)) count ++;
                }
                if(count < num)
                {
                    complete1 = 0;
                    break;
                }
            }
            kits[1].complete = true; // this isn't true... but it doesn't really matter
        }

        completeKits = complete0 + complete1;
        return completeKits;
    }

    // 0 or 1 for missing parts non normative scenario
    synchronized public void setKitBad(int num)
    {
    	if(num > 1 || num < 0) System.out.println("error");
    	else kits[num].setKitBad();
    }
    
    synchronized public void setKitGood(int num)
    {
    	if(num > 1 || num < 0) System.out.println("error");
    	else kits[num].setKitGood();
    }
    
    synchronized public int completeKits() { return completeKits; }

    // should only do when PartRobot tells we have complete kits!
    synchronized public Kit removeCompleteKit() {
    	
        if(count == 0) return null;

        Kit data = null;
        if(kits[0] != null){
	        if(kits[0].complete) 
	        {
	        	System.out.println("Taking kitstand[0]");
	            data = kits[0];
	            kits[0] = null;
	        }
	        else if (kits[1] != null){
	        	if (kits[1].complete)
	        	{
	        		System.out.println("Taking kitstand[1]");
	                data = kits[1];
	                kits[1] = null;
	        	}
	        }
        }
        else 
        {
            data = kits[1];
            kits[1] = null;
        }

        count--;

        return data;
    }

    public void placeInspection(Kit kit)
    {
        inspection.placeOnInspection(kit);    
    }
    public Kit inspectKit()
    {
        return inspection.inspectKit();
    }

    public Kit removeInspection()
    {
        return inspection.removeFromInspection();
    }    
}

