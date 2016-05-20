package no.ntnu.kpro09.renderer.text;

import no.bouvet.kpro.renderer.AbstractRenderer;
import no.bouvet.kpro.renderer.Instruction;
import no.ntnu.kpro09.Player;
import no.ntnu.kpro09.renderer.Cleaner;

/**
 * This renderer is responsible for adding and removing textInstructions to the
 * list of a player's active instructions at the right times.
 * 
 * @author Gaute Nordhaug
 * 
 */

public class TextRenderer extends AbstractRenderer {

	private Player player;

	public TextRenderer(Player player) {
		this.player = player;
	}

	/**
	 * If the instruction is a text instruction it will be added to the active
	 * text instructions list, if it is a text instructions cleaner it will
	 * remove the instruction from the list.
	 */
	@Override
	public void handleInstruction(int time, Instruction instruction) {
		if (instruction instanceof TextInstruction) {
			player.setInstructionsChanged(true);
			player.getActiveTextInstructions().add(
					(TextInstruction) instruction);
		} else if (instruction instanceof Cleaner
				&& ((Cleaner) instruction).getToBeCleaned() instanceof TextInstruction) {
			player.setInstructionsChanged(true);
			player.getActiveTextInstructions().remove(
					((Cleaner) instruction).getToBeCleaned());
		}
	}
}

