package mw.server.card.gold.uw;

import mw.server.model.Ability;
import mw.server.model.Card;
import mw.server.model.SpellAbility;
import mw.server.GameManager;
import mw.server.pattern.CommandEx;

@SuppressWarnings("serial")
public class SwansofBrynArgoll {

    public static Card getCard(final GameManager game, final Card card) {

        final CommandEx DealtDamage = new CommandEx() {
        	
            public void execute() {
                if (game.getBattlefield().isCardInPlay(card.getTableID())) {
                    Card c = game.getBattlefield().getPermanent(card.getTableID());
                    
                    if (!game.isDamageCantBePreventedThisTurn()) {
	                    Card source = (Card)getTarget();
	                    if (source != null) {
	                    	final int count = c.getLatestDealtDamage();
	                    	final int pid = source.getControllerID();
	                    	final SpellAbility drawAbility = new Ability(card, "0") {
	                    		public void resolve() {
	                    			for (int i = 0; i < count; i++) {
	                    				game.getPlayerById(pid).drawCard();
	                    			}
	                    		}
	                    	};
	                    	c.setLatestDealtDamage(0); // prevent damage
	                    	drawAbility.setStackDescription("If a source would deal damage to Swans of Bryn Argoll, prevent that damage. The source's controller draws cards equal to the damage prevented this way.");
	                    	game.getStack().add(drawAbility);
	                    }
                    } else {
                    	if (game.getManager().isDamageCantBePrevented()) {
                    		game.getManager().addSystemMessage(card + ": damage can't be prevented.");
                    	} else {
                    		game.getManager().addSystemMessage(card + ": damage can't be prevented this turn.");
                    	}
                    }
                }
            }           
        };
        card.setDealtDamageCommandEx(DealtDamage);

        return card;
    }
}


