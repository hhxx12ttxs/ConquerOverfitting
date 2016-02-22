package mw.server.model.cost;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import mw.mtgforge.Constant;
import mw.server.model.Card;

public class ManaCost implements Cost, Serializable {
    static final long serialVersionUID = 1;

    /**
     * Contains common part of manacost.
     */
    private HashMap<String, Integer> monoColor = new HashMap<String, Integer>();

    /**
     * Contains hybrid part of manacost.
     */
    private HashMap<String, Integer> twoColor = new HashMap<String, Integer>();

    private int xOcurrences = 0;
    private int X = 0;

    private ArrayList<String> choices = new ArrayList<String>();

    public ManaCost() {
    	//super();
    }

    public ArrayList<String> getChoices() {
        if (choices.size() == 0) {
            return null;
        }
        return choices;
    }

    public ManaCost(String cost) {
        parseString(cost);
    }

    public ManaCost(String cost, int xValue) {
        parseString(cost);
        for (int i = 0; i < xValue; i++) {
            this.add("C");
        }
    }

    /**
     * Does nothing yet
     */
    public boolean hasX() {
        return xOcurrences > 0;
    }

    /**
     * Does nothing yet
     * @return
     */
    public int getX() {
        return X;
    }

    /**
     * Correct manacost to make it unified. 
     * 
     * The reason: a developer can write the same hybrid mana in different ways:
     * e.g., B\\G and G\\B should mean the same.
     * 
     * @param Cost manacost to modify
     * @return Unified manacost
     */
    private String correctCost(String Cost) {
        String cost = Cost;
        cost = cost.toUpperCase();

        cost = cost.replace(" \\", "\\");
        cost = cost.replace("\\ ", "\\");

        cost = cost.replace("G\\B", "B\\G");
        cost = cost.replace("R\\B", "B\\R");
        cost = cost.replace("U\\G", "G\\U");
        cost = cost.replace("W\\G", "G\\W");
        cost = cost.replace("R\\U", "U\\R");
        cost = cost.replace("B\\U", "U\\B");
        cost = cost.replace("W\\R", "R\\W");
        cost = cost.replace("G\\R", "R\\G");
        cost = cost.replace("U\\W", "W\\U");
        cost = cost.replace("B\\W", "W\\B");
        cost = cost.replace("B\\2", "2\\B");
        cost = cost.replace("G\\2", "2\\G");
        cost = cost.replace("U\\2", "2\\U");
        cost = cost.replace("R\\2", "2\\R");
        cost = cost.replace("W\\2", "2\\W");

        // this realization could be used in future when, for example, 3\T cost
        // will appear
        // converting other costs to X\T format
        /*
         * StringTokenizer token = new StringTokenizer(cost);
         * while(token.hasMoreTokens()) { String s = token.nextToken(); if
         * (s.length() == 3) if (Character.isDigit(s.charAt(2))) { String s1 =
         * s.charAt(2) + "\\" + s.charAt(0); cost = cost.replace(s, s1); } }
         */
        return cost;
    }

    public void reset() {
        monoColor.clear();
        twoColor.clear();
    }

    /**
     * Convert manacost string representation to monoColor\twoColor view.
     * 
     * @param cost manacost to parse
     */
    public void parseString(String cost) {
        reset();
        StringTokenizer token = new StringTokenizer(correctCost(cost));
        while (token.hasMoreTokens()) {
            String s = token.nextToken();

            if (s.equals("X")) {
            	xOcurrences++;
            } else {
                if (isDigit(s)) {
                    for (int i = 0; i < Integer.parseInt(s); i++) {
                        this.add(Constant.Color.Colorless);
                    }
                } else {
                    this.add(s);
                }
            }
        }
    }
    
    private boolean isDigit(String cost) {
    	for (int i = 0; i < cost.length(); i++) {
    		if (!Character.isDigit(cost.charAt(i))) {
    			return false;
    		}
    	}
    	return true;
    }

    /**
     * Convert to string representation.
     */
    public String toString() {
        String str = "";

        /**
         * adding mono colors
         */
        if (monoColor.size() > 0) {
            /**
             * adding colorless
             */
            if (monoColor.containsKey(Constant.Color.Colorless) && monoColor.get(Constant.Color.Colorless) > 0) {
                str += monoColor.get(Constant.Color.Colorless) + " ";
            }

            /**
             * adding BRGWU
             */
            for (int j = 0; j < Constant.Color.MonoColorCount - 1; j++) {
                if (monoColor.containsKey(Constant.Color.MonoColors[j])) {
                    for (int k = 0; k < monoColor.get(Constant.Color.MonoColors[j]); k++) {
                        str += Constant.Color.MonoColors[j] + " ";
                    }
                }
            }
        }

        /**
         * adding hybrid
         */
        if (twoColor.size() > 0) {
            /**
             * adding T\\T
             */
            for (int j = 0; j < Constant.Color.TwoColorCount; j++) {
                if (twoColor.containsKey(Constant.Color.TwoColors[j])) {
                    for (int k = 0; k < twoColor.get(Constant.Color.TwoColors[j]); k++) {
                        str += Constant.Color.TwoColors[j] + " ";
                    }
                }
            }

            /**
             * adding 2\\T
             */
            boolean b = true;
            for (String s : twoColor.keySet()) {
                b = true;
                for (int i = 0; i < Constant.Color.TwoColorCount; i++) {
                    if (s.compareTo(Constant.Color.TwoColors[i]) == 0) {
                        b = false;
                    }
                }
                if (b) {
                    for (int i = 0; i < twoColor.get(s); i++) {
                        str += s + " ";
                    }
                }
            }
        }

        for (int i = 0; i < xOcurrences; i++) {
        	str = "X " + str;
        }
        
        str = str.trim();
        if (str.length() == 0) str = "0";
        
        return str;
    }

    /**
     * Add mana to current mana cost.
     * 
     * @param color color mana to add
     * @return false if color is not correct
     */
    public boolean add(String color) {
    	if (color.equals("1")) color = "C";
    	
        if (color.length() == 1) {
            if (monoColor.containsKey(color))
                monoColor.put(color, monoColor.get(color) + 1);
            else {
                monoColor.put(color, 1);
            }
            return true;
        } else if (color.length() == 3) {
            if (twoColor.containsKey(color)) {
                twoColor.put(color, twoColor.get(color) + 1);
            } else {
                twoColor.put(color, 1);
            }
            return true;
        } else {
            //return false;
        	throw new RuntimeException("Couldn't add mana to ManaCost: <" + color + ">.");
        }
    }

    /**
     * Subtract color from hybrid mana token.
     * 
     * example:
     *   manaToken: G\\R
     *   color: R
     *   
     * @param manaToken hybrid mana to subtract from (T\\T or 2\\T, where T :== B|R|G|W|U)
     * @param color color mana to subtract
     * @return true if color mana was subtracted from mana cost successfully
     *         false otherwise
     */
    public boolean subtractFrom(String manaToken, String color) {
        choices.clear();

        if (twoColor.size() > 0) { // hybrid mana exists in mana cost
            if (twoColor.containsKey(manaToken)) { // mana cost contains this type of hybrid mana (mana token)
                /**
                 * Hybrid mana contains color mana
                 * R --> G\\R
                 */
                if (manaToken.contains(color)) {
                    if (twoColor.get(manaToken) == 1) {
                        twoColor.remove(manaToken);
                    } else {
                        twoColor.put(manaToken, twoColor.get(manaToken) - 1);
                    }
                    return true;
                } else {
                    /**
                     * Check if colorless mana exists
                     * we can't pay W for B\\G, only for smth like 2\\G
                     */
                    if (!manaToken.contains("C")) {
                        return false;
                    }

                    /**
                     * Pay colorless mana
                     * Replace C\\T cost by C-1
                     * So if we have 3\\G and pay (1), then we'll need to pay (2) more in future
                     */
                    int n = Integer.parseInt(manaToken.substring(0, 1));
                    if (twoColor.get(manaToken) == 1) {
                        twoColor.remove(manaToken);
                    } else {
                        twoColor.put(manaToken, twoColor.get(manaToken) - 1);
                    }

                    if (monoColor.containsKey(Constant.Color.Colorless)) {
                        monoColor.put(Constant.Color.Colorless, monoColor.get(Constant.Color.Colorless) + n - 1);
                    } else {
                        monoColor.put(Constant.Color.Colorless, n - 1);
                    }

                    return true;
                }
            }
        }

        return false;
    }

    public boolean subtract(String color) {
        choices.clear();
        
        if (color.equals("1")) color = "C";

        if (Constant.Color.Colorless.equals(color)) {
        	if (monoColor.containsKey(color)) {
        		monoColor.put(color, monoColor.get(color) - 1);
        	} else {
        		monoColor.put(color, -1);
        	}
        	return true;
        }
        
        if (monoColor.size() > 0) {
            if (monoColor.containsKey(color)) {
                if (monoColor.get(color) == 1)
                    monoColor.remove(color);
                else
                    monoColor.put(color, monoColor.get(color) - 1);
                return true;
            }
            if (twoColor.size() == 0) {
                if (monoColor.containsKey("C")) {
                    if (monoColor.get("C") == 1)
                        monoColor.remove("C");
                    else
                        monoColor.put("C", monoColor.get("C") - 1);
                    return true;
                }
            }
        }

        if (twoColor.size() > 0) {
            ArrayList<String> ar = new ArrayList<String>();

            if (!Constant.Color.Colorless.equals(color)) {
                for (String str : twoColor.keySet())
                    if (str.contains(color)) {
                        for (int i = 0; i < twoColor.get(str); i++)
                            ar.add(str);
                    }

                if (ar.size() == 1) {
                    if (twoColor.get(ar.get(0)) == 1)
                        twoColor.remove(ar.get(0));
                    else
                        twoColor.put(ar.get(0), twoColor.get(ar.get(0)) - 1);
                    return true;
                } else if (ar.size() > 1) {
                    // if ar contains only similar elements then just removing
                    // one of them
                    boolean b = true;
                    for (int i = 1; i < ar.size(); i++)
                        if (ar.get(i - 1) != ar.get(i)) {
                            b = false;
                            break;
                        }
                    if (b) {
                        if (twoColor.get(ar.get(0)) == 1)
                            twoColor.remove(ar.get(0));
                        else
                            twoColor.put(ar.get(0), twoColor.get(ar.get(0)) - 1);
                        return true;
                    }
                    choices = ar;
                    return false;
                    // else
                    // different elements exist . User must choose which
                    // combination to remove
                    // TODO: request to user to chose from different elements
                }
            }
            color = Constant.Color.Colorless;

            // let's look if there exist colorless mana in MonoColor
            if (monoColor.size() > 0) {
                if (monoColor.containsKey("C")) {
                    if (monoColor.get("C") == 1)
                        monoColor.remove("C");
                    else
                        monoColor.put("C", monoColor.get("C") - 1);
                    return true;
                }
            }

            ar.clear();
            for (String s : twoColor.keySet()) {
                if (Character.isDigit(s.charAt(0))) {
                    for (int i = 0; i < twoColor.get(s); i++)
                        ar.add(s);
                }

                if (ar.size() == 1) {
                    int n = Integer.parseInt(ar.get(0).substring(0, 1));
                    if (twoColor.get(ar.get(0)) == 1)
                        twoColor.remove(ar.get(0));
                    else
                        twoColor.put(ar.get(0), twoColor.get(ar.get(0)) - 1);

                    if (monoColor.containsKey(Constant.Color.Colorless))
                        monoColor.put(Constant.Color.Colorless, monoColor.get(Constant.Color.Colorless) + n - 1);
                    else
                        monoColor.put(Constant.Color.Colorless, n - 1);
                    return true;
                } else if (ar.size() > 1) {
                    // if ar contains only similar elements then just removing
                    // one of them
                    boolean b = true;
                    for (int i = 1; i < ar.size(); i++)
                        if (ar.get(i - 1) != ar.get(i)) {
                            b = false;
                            break;
                        }
                    if (b) {
                        int n = Integer.parseInt(ar.get(0).substring(0, 1));
                        if (twoColor.get(ar.get(0)) == 1)
                            twoColor.remove(ar.get(0));
                        else
                            twoColor.put(ar.get(0), twoColor.get(ar.get(0)) - 1);

                        if (monoColor.containsKey(Constant.Color.Colorless))
                            monoColor.put(Constant.Color.Colorless, monoColor.get(Constant.Color.Colorless) + n - 1);
                        else
                            monoColor.put(Constant.Color.Colorless, n - 1);
                        return true;
                    }
                    choices = ar;
                    return false;
                    // else
                    // different elements exist . User must choose which
                    // combination to remove
                    // TODO: request to user to choose from different elements
                }

            }
        } // else
        return false;
    }

    /**
     * Check if certain color mana can be used to play either color or colorless part of the mana cost.
     * @param color
     * @return
     */
    public boolean isNeeded(String color) {
        if (monoColor.size() > 0) {
            if ((monoColor.containsKey(color)) || (monoColor.containsKey(Constant.Color.Colorless))) {
                return true;
            }
        }

        if (twoColor.size() > 0)
            for (String s : twoColor.keySet()) {
                if ((s.contains(color)) || (Character.isDigit(s.charAt(0)))) {
                    return true;
                }
            }
        return false;
    }

    /**
     * Check if certain color mana can be used to pay the color part of the mana cost.
     * 
     * @param color
     * @return
     */
    public boolean isColoredNeeded(String color) {
        if (monoColor.containsKey(color)) {
            return true;
        }
        for (String s : twoColor.keySet()) {
            if (s.contains(color)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if mana cost has been already paid
     * @return
     */
    public boolean isPaid() {
        return !(monoColor.size() > 0) && !(twoColor.size() > 0);
    }

    public boolean containsMonoColor(String color) {
        return monoColor.containsKey(color);
    }
    
    public int getUncolored() {
    	if (monoColor.containsKey(Constant.Color.Colorless)) {
            return monoColor.get(Constant.Color.Colorless);
        } else {
        	return 0;
        }
    }

    public int getConverted() {
        int converted = 0;

        /**
         * adding colorless
         */
        if (monoColor.containsKey(Constant.Color.Colorless)) {
            converted += monoColor.get(Constant.Color.Colorless);
        }

        /**
         * adding BRGWU
         */
        for (int j = 0; j < Constant.Color.MonoColorCount - 1; j++) {
            if (monoColor.containsKey(Constant.Color.MonoColors[j])) {
                converted += monoColor.get(Constant.Color.MonoColors[j]);
            }
        }

        /**
         * adding hybrid
         */
        if (twoColor.size() > 0) {
            /**
             * adding T\\T
             */
            for (int j = 0; j < Constant.Color.TwoColorCount; j++) {
                if (twoColor.containsKey(Constant.Color.TwoColors[j])) {
                    converted += twoColor.get(Constant.Color.TwoColors[j]);
                }
            }

            /**
             * adding 2\\T
             */
            boolean b = true;
            for (String s : twoColor.keySet()) {
                b = true;
                for (int i = 0; i < Constant.Color.TwoColorCount; i++) {
                    if (s.compareTo(Constant.Color.TwoColors[i]) == 0) {
                        b = false;
                    }
                }
                if (b) {
                    converted += 2 * twoColor.get(s);
                }
            }
        }
        
        return converted;
    }

    public void pay(Card card) {} // ignore, handled in other way
    
	public boolean canPayForCard(Card card) {
		return true;
	}
}

