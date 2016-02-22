package mw.server.model.spell;

import java.util.ArrayList;

import mw.mtgforge.Constant;
import mw.server.GameManager;
import mw.server.model.Card;
import mw.server.model.Spell;
import mw.server.model.SpellAbility;

public class Suspend extends Spell {

    public final int timeCounters;
    private GameManager game;
    
    public Suspend(GameManager game, Card sourceCard, int timeCounters, String manaCost) {
        super(sourceCard);
        this.game = game; 
        this.timeCounters = timeCounters;
        this.setManaCost(manaCost);
        this.setDescription(sourceCard + " with suspend " + Integer.valueOf(timeCounters) + ": " + manaCost);
    }
    
    /**
     * Put permanent with time counters.
     */
    public void resolve() {
        addSuspendedPermanent(this);
    }
    
    //TODO: reimplement using card.getCopy()
    private void addSuspendedPermanent(Suspend spell) {
        Card card = spell.getSourceCard();
        Card c = new Card();

        c.setOwner(card.getOwnerID());
        c.setController(card.getControllerID());

        c.setName(card.getName());
        c.setManaCost(spell.getManaCost());
        c.setSuspended(true);
        c.setTimeCounters(spell.timeCounters);
        c.setCollectorID(card.getCollectorID());
        c.setSetName(card.getSetName());
        
        c.setColor(Constant.Color.Blue);
        
        ArrayList<SpellAbility> s = card.getSpellAbilities();
        for (SpellAbility a : s) {
            if (!(a instanceof Suspend)) {
                c.addSpellAbility(a);
            }
        }

        game.getBattlefield().addPermanent(c);
    }
    
    /**
     * Default UID.
     */
    private static final long serialVersionUID = 1L;
}
