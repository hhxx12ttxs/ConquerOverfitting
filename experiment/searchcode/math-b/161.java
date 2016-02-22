package mw.server.list;

import mw.server.model.Card;
import mw.server.model.Damage;
import mw.server.model.bean.CardBean;

public class CardListUtil {
    /*public static CardList filterToughness(CardList in, int atLeastToughness) {
        CardList out = new CardList();
        for (int i = 0; i < in.size(); i++)
            if (in.get(i).getDefense() <= atLeastToughness)
                out.add(in.get(i));

        return out;
    }

    // the higher the defense the better
    public static void sortDefense(CardList list) {
        Comparator com = new Comparator() {
            public int compare(Object a1, Object b1) {
                Card a = (Card) a1;
                Card b = (Card) b1;

                return b.getDefense() - a.getDefense();
            }
        };
        list.sort(com);
    }// sortDefense()

    // the higher the attack the better
    public static void sortAttack(CardList list) {
        Comparator com = new Comparator() {
            public int compare(Object a1, Object b1) {
                Card a = (Card) a1;
                Card b = (Card) b1;

                return b.getAttack() - a.getAttack();
            }
        };
        list.sort(com);
    }// sortAttack()

    // the lower the attack the better
    public static void sortAttackLowFirst(CardList list) {
        Comparator com = new Comparator() {
            public int compare(Object a1, Object b1) {
                Card a = (Card) a1;
                Card b = (Card) b1;

                return a.getAttack() - b.getAttack();
            }
        };
        list.sort(com);
    }// sortAttackLowFirst()

    public static void sortNonFlyingFirst(CardList list) {
        sortFlying(list);
        list.reverse();
    }// sortNonFlyingFirst

    // the creature with flying are better
    public static void sortFlying(CardList list) {
        Comparator com = new Comparator() {
            public int compare(Object a1, Object b1) {
                Card a = (Card) a1;
                Card b = (Card) b1;

                if (a.getKeyword().contains("Flying")
                        && b.getKeyword().contains("Flying"))
                    return 0;
                else if (a.getKeyword().contains("Flying"))
                    return -1;
                else if (b.getKeyword().contains("Flying"))
                    return 1;

                return 0;
            }
        };
        list.sort(com);
    }// sortFlying()

    public static CardList getColor(CardList list, final String color) {
        return list.filter(new CardListFilter() {
            public boolean addCard(Card c) {
                return c.getColor().equals(color);
            }
        });
    }// getColor()
    */
	
    public static CardBeanList getColor(CardBeanList list, final String color) {
        return list.filter(new CardBeanListFilter() {
            public boolean addCard(CardBean c) {
                return c.getColor().contains(color);
            }
        });
    }

    /**
     * Return overall damage that can deal the creatures from the card list
     * 
     * @param c
     * @return
     */
	public static Damage sumAttack(CardList c) {

        Damage damage = new Damage();

        for (int i = 0; i < c.size(); i++) {
            Card card = c.get(i);
            if (card.isCreature()) {                
            	damage.addSource(card, card.getAttack());
            }
        }

        return damage;
    }
    
}
