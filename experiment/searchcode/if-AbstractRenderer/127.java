package no.ntnu.kpro09.renderer.video;

import no.bouvet.kpro.renderer.AbstractRenderer;
import no.bouvet.kpro.renderer.Instruction;
import no.ntnu.kpro09.Player;

/**
 * This renderer is responsible for adding video instructions to the list of a
 * player's active instructions at the right times. I doesn't have to remove the
 * video instructions since the movie player will take care of that itself.
 * 
 * @author Gaute Nordhaug
 * 
 */
public class VideoRenderer extends AbstractRenderer {
	private Player player;

	public VideoRenderer(Player player) {
		this.player = player;
	}

	@Override
	public void handleInstruction(int time, Instruction instruction) {
		if (instruction instanceof VideoInstruction) {
			player.setInstructionsChanged(true);
			player.getActiveVideoInstructions().add(
					(VideoInstruction) instruction);
		}
	}

}

