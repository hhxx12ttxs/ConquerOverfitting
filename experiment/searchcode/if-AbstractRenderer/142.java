package no.ntnu.kpro09.renderer.image;

import no.bouvet.kpro.renderer.AbstractRenderer;
import no.bouvet.kpro.renderer.Instruction;
import no.ntnu.kpro09.Player;
import no.ntnu.kpro09.renderer.Cleaner;

/**
 * This renderer is responsible for adding and removing image instructions to
 * the list of a player's active instructions at the right times.
 * 
 * @author Gaute Nordhaug
 * 
 */

public class ImageRenderer extends AbstractRenderer {

	private Player player;

	public ImageRenderer(Player player) {
		this.player = player;
	}

	@Override
	/**
	 * If the instruction is an image instruction it will be added to the active image instructions list, if it is an image instructions cleaner it will remove the instruction from the list.
	 */
	public void handleInstruction(int time, Instruction instruction) {
		if (instruction instanceof ImageInstruction) {
			player.setInstructionsChanged(true);
			player.getActiveImageInstructions().add(
					(ImageInstruction) instruction);
		} else if (instruction instanceof Cleaner
				&& ((Cleaner) instruction).getToBeCleaned() instanceof ImageInstruction) {
			player.setInstructionsChanged(true);
			player.getActiveImageInstructions().remove(
					((Cleaner) instruction).getToBeCleaned());
		}
	}
}

