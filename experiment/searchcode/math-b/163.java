package mw.client.editor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

import mw.server.list.CardBeanList;
import mw.server.model.bean.CardBean;
import mw.server.model.cost.ManaCost;

/**
 * @author mtgforge
 */
public class TableSorter implements Comparator<CardBean> {
    private final int column;
    private boolean ascending;

    private CardBeanList all;
    
    //used if in_column is 7, new cards first - the order is based on cards.txt
    //static because this should only be read once
    //static to try to reduce file io operations
    private static HashMap<String, Integer> cardsTxt = null; 

    public TableSorter(CardBeanList in_all, int in_column, boolean in_ascending) {
        all = new CardBeanList(in_all.toArray());
        column = in_column;
        ascending = in_ascending;
        
        if (cardsTxt == null && in_all.size() > 0) {
        	cardsTxt = new HashMap<String, Integer>(); 
	        for (int i = 0; i < in_all.getCardBeanList().size(); i++) {
	        	cardsTxt.put(in_all.get(i).getName().trim(), Integer.valueOf(i)); 
	        }
        }
    }

    // 0 1 2 3 4 5 6
    // private String column[] = {"Qty", "Name", "Cost", "Color", "Type", "Stats", "Set"}; New cards first - the order is based on cards.txt 
    public int compare(CardBean a, CardBean b) {
        Comparable aCom = null;
        Comparable bCom = null;

        if (column == 0)// Qty
        {
            aCom = Integer.valueOf(countCardName(a.getName(), all));
            bCom = Integer.valueOf(countCardName(b.getName(), all));
        } else if (column == 1)// Name
        {
            aCom = a.getName();
            bCom = b.getName();
            if (aCom.equals(bCom) && a.getSetName().equals(b.getSetName())) {
            	aCom = a.getCollectorID();
            	bCom = b.getCollectorID();
            }
        } else if (column == 2)// Cost
        {
        	ManaCost m = new ManaCost(a.getManaCost());
            aCom = m.getConverted();
            m = new ManaCost(b.getManaCost());
            bCom = m.getConverted();

            if (a.isLand())
                aCom = Integer.valueOf(-1);
            if (b.isLand())
                bCom = Integer.valueOf(-1);
        } else if (column == 3)// Color
        {
            aCom = getColor(a);
            bCom = getColor(b);
        } else if (column == 4)// Type
        {
            aCom = getType(a);
            bCom = getType(b);
        } else if (column == 5)// Stats, attack and defense
        {
            aCom = new Float(-1);
            bCom = new Float(-1);

            if (a.isCreature())
                aCom = new Float(a.getAttack() + "." + a.getDefense());
            if (b.isCreature())
                bCom = new Float(b.getAttack() + "." + b.getDefense());
        } else if (column == 6)// Rarity
        {
            aCom = getRarity(a);
            bCom = getRarity(b);
        }
        else if (column == 7)// Set name
        {
            aCom = a.getSetName();
            bCom = b.getSetName();
        } else if (column == 8) { //New First
            aCom = sortNewFirst(a);
            bCom = sortNewFirst(b);
        } 
        
        if (ascending)
            return aCom.compareTo(bCom);
        else
            return bCom.compareTo(aCom);
    }// compare()

    private int countCardName(String name, CardBeanList c) {
        int count = 0;
        for (int i = 0; i < c.size(); i++)
            if (name.equals(c.get(i).getName()))
                count++;

        return count;
    }

    private Integer getRarity(CardBean c) {
        if (c.getRarity().equals("C"))
            return Integer.valueOf(1);
        else if (c.getRarity().equals("U"))
            return Integer.valueOf(2);
        else if (c.getRarity().equals("R"))
            return Integer.valueOf(3);
        else if (c.getRarity().equals("M"))
            return Integer.valueOf(4);
        else if (c.getRarity().equals("SP"))
            return Integer.valueOf(5);
        else
            return Integer.valueOf(6);
    }

    public static String getColor(CardBean c) {
        ArrayList<String> list = c.getColor();

        if (list.size() == 1) {
            return list.get(0).toString();
        }
        
        if (c.getManaCost().contains("\\")) {
            return "hybrid";
        }

        return "multi";
    }

    private Comparable getType(CardBean c) {
        return c.getType().toString();
    }
    
    final private Comparable sortNewFirst(CardBean c) {
		if (!cardsTxt.containsKey(c.getName().trim())) {
			throw new RuntimeException("TableSorter : sortNewFirst() error, Card not found - " + c.getName() + " in hashmap - " + cardsTxt);
		}

		return cardsTxt.get(c.getName().trim());
	} 
}


