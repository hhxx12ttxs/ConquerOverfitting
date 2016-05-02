/**
 * 
 */
package gui.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import ai.AIEngine;
import ai.human.PlayerGroup;
import data.Data.BlockActionType;
import data.Data.Direction;
import data.Data.PlayerState;
import data.Player;
import data.Position;
import data.selection.SelectionObject;
import engine.GameEngine;
import engine.Pitch;

/**
 * This factory prepares the images needed in the game. Currently used only for
 * the pitch GUI.
 * 
 * @author bogdan
 * 
 */
public class IconFactory {
	// private members

	private static IconFactory m_instance = null;

	private static final String m_orcBlitzerPath1 = "data/images/orc_blitzer_1.png";
	private static final String m_orcBlockerPath1 = "data/images/orc_blocker_1.png";
	private static final String m_orcThrowerPath1 = "data/images/orc_thrower_1.png";
	private static final String m_orcLinemanPath1 = "data/images/orc_lineman_1.png";
	private static final String m_orcCatcherPath1 = "data/images/orc_catcher_1.png";

	private static final String m_humanBlitzerPath1 = "data/images/human_blitzer_1.png";
	private static final String m_humanBlockerPath1 = "data/images/human_blocker_1.png";
	private static final String m_humanThrowerPath1 = "data/images/human_thrower_1.png";
	private static final String m_humanLinemanPath1 = "data/images/human_lineman_1.png";
	private static final String m_humanCatcherPath1 = "data/images/human_catcher_1.png";

	private static final String m_elfBlitzerPath1 = "data/images/elf_blitzer_1.png";
	private static final String m_elfBlockerPath1 = "data/images/elf_blocker_1.png";
	private static final String m_elfThrowerPath1 = "data/images/elf_thrower_1.png";
	private static final String m_elfLinemanPath1 = "data/images/elf_lineman_1.png";
	private static final String m_elfCatcherPath1 = "data/images/elf_catcher_1.png";

	private static final String m_pitchPath = "data/images/pitch.jpg";
	private static final String m_pitchLPath = "data/images/pitchL.jpg";
	private static final String m_pitchRPath = "data/images/pitchR.jpg";
	private static final String m_pitchDPath = "data/images/pitchD.jpg";
	private static final String m_pitchUPath = "data/images/pitchU.jpg";
	private static final String m_pitchLDPath = "data/images/pitchLD.jpg";
	private static final String m_pitchRDPath = "data/images/pitchRD.jpg";
	private static final String m_pitchLUPath = "data/images/pitchLU.jpg";
	private static final String m_pitchRUPath = "data/images/pitchRU.jpg";
	private static final String m_ballPath = "data/images/ball.png";

	private static final String m_actionAttackerDownPath = "data/images/attackerDown.png";
	private static final String m_actionBothDownPath = "data/images/bothDown.png";
	private static final String m_actionPushPath = "data/images/push.png";
	private static final String m_actionDefenderStumblesPath = "data/images/defenderStumbles.png";
	private static final String m_actionDefenderDownPath = "data/images/defenderDown.png";

	private static final String m_movePath = "data/images/move.png";
	private static final String m_moveDiagPath = "data/images/move_diagonal.png";

	private static BufferedImage m_orcBlitzer1;
	private static BufferedImage m_orcBlocker1;
	private static BufferedImage m_orcThrower1;
	private static BufferedImage m_orcLineman1;
	private static BufferedImage m_orcCatcher1;

	private static BufferedImage m_humanBlitzer1;
	private static BufferedImage m_humanBlocker1;
	private static BufferedImage m_humanThrower1;
	private static BufferedImage m_humanLineman1;
	private static BufferedImage m_humanCatcher1;

	private static BufferedImage m_elfBlitzer1;
	private static BufferedImage m_elfBlocker1;
	private static BufferedImage m_elfThrower1;
	private static BufferedImage m_elfLineman1;
	private static BufferedImage m_elfCatcher1;

	private static BufferedImage m_pitch;
	private static BufferedImage m_pitchL;
	private static BufferedImage m_pitchR;
	private static BufferedImage m_pitchD;
	private static BufferedImage m_pitchU;
	private static BufferedImage m_pitchLD;
	private static BufferedImage m_pitchRD;
	private static BufferedImage m_pitchLU;
	private static BufferedImage m_pitchRU;
	private static BufferedImage m_ball;

	private static ImageIcon m_actionAttackerDown;
	private static ImageIcon m_actionBothDown;
	private static ImageIcon m_actionPush;
	private static ImageIcon m_actionDefenderStumbles;
	private static ImageIcon m_actionDefenderDown;

	private static BufferedImage m_move;
	private static BufferedImage m_moveDiag;

	private final List<SelectionObject> m_selection;

	// private methods

	/**
	 * Contructor. Loads all images.
	 */
	private IconFactory() {
		// orcs
		m_orcBlitzer1 = createIcon(m_orcBlitzerPath1);
		m_orcBlocker1 = createIcon(m_orcBlockerPath1);
		m_orcThrower1 = createIcon(m_orcThrowerPath1);
		m_orcLineman1 = createIcon(m_orcLinemanPath1);
		m_orcCatcher1 = createIcon(m_orcCatcherPath1);

		// humans
		m_humanBlitzer1 = createIcon(m_humanBlitzerPath1);
		m_humanBlocker1 = createIcon(m_humanBlockerPath1);
		m_humanThrower1 = createIcon(m_humanThrowerPath1);
		m_humanLineman1 = createIcon(m_humanLinemanPath1);
		m_humanCatcher1 = createIcon(m_humanCatcherPath1);

		// elfs
		m_elfBlitzer1 = createIcon(m_elfBlitzerPath1);
		m_elfBlocker1 = createIcon(m_elfBlockerPath1);
		m_elfThrower1 = createIcon(m_elfThrowerPath1);
		m_elfLineman1 = createIcon(m_elfLinemanPath1);
		m_elfCatcher1 = createIcon(m_elfCatcherPath1);

		// Pitch
		m_pitch = createIcon(m_pitchPath);
		m_pitchL = createIcon(m_pitchLPath);
		m_pitchR = createIcon(m_pitchRPath);
		m_pitchD = createIcon(m_pitchDPath);
		m_pitchU = createIcon(m_pitchUPath);
		m_pitchLD = createIcon(m_pitchLDPath);
		m_pitchRD = createIcon(m_pitchRDPath);
		m_pitchLU = createIcon(m_pitchLUPath);
		m_pitchRU = createIcon(m_pitchRUPath);
		m_ball = createIcon(m_ballPath);

		// block dice
		m_actionAttackerDown = new ImageIcon(createIcon(m_actionAttackerDownPath));
		m_actionBothDown = new ImageIcon(createIcon(m_actionBothDownPath));
		m_actionPush = new ImageIcon(createIcon(m_actionPushPath));
		m_actionDefenderStumbles = new ImageIcon(createIcon(m_actionDefenderStumblesPath));
		m_actionDefenderDown = new ImageIcon(createIcon(m_actionDefenderDownPath));

		// move arrows
		m_move = createIcon(m_movePath);
		m_moveDiag = createIcon(m_moveDiagPath);

		m_selection = new ArrayList<SelectionObject>();
	}

	/**
	 * Loads an image from disk and creates a BufferedImage object.
	 * 
	 * @param path
	 *            - the path of the image asset
	 * @return Returns a BufferedImage object.
	 */
	private BufferedImage createIcon(final String path) {
		BufferedImage icon = null;
		try {
			icon = ImageIO.read(new File(path));
		} catch (final IOException e) {
			System.err.println("Couldn't find file: " + path);
		}

		return icon;
	}

	// public methods

	/**
	 * Gets the single instance of the singleton.
	 */
	public static final IconFactory instance() {
		if (m_instance == null) {
			m_instance = new IconFactory();
		}

		return m_instance;
	}

	/**
	 * Gets the arrow representing the direction of the movement
	 * 
	 * @param dir
	 *            - the direction
	 * @return Returns an ImageIcon object
	 */
	public ImageIcon getDirectionIcon(final Direction dir) {
		switch (dir) {
		case NW:
			return new ImageIcon(rotateImage(m_moveDiag, -90));
		case N:
			return new ImageIcon(rotateImage(m_move, -90));
		case NE:
			return new ImageIcon(m_moveDiag);
		case W:
			return new ImageIcon(rotateImage(m_move, 180));
		case E:
			return new ImageIcon(m_move);
		case SW:
			return new ImageIcon(rotateImage(m_moveDiag, 180));
		case S:
			return new ImageIcon(rotateImage(m_move, 90));
		case SE:
			return new ImageIcon(rotateImage(m_moveDiag, 90));
		}

		return null;
	}

	/**
	 * Gets the icon of the block dice face
	 * 
	 * @param action
	 *            - the current block dice face
	 * @return Returns an ImageIcon object
	 */
	public ImageIcon getBlockDiceIcon(final BlockActionType action) {
		switch (action) {
		case ATTACKER_DOWN:
			return m_actionAttackerDown;
		case BOTH_DOWN:
			return m_actionBothDown;
		case PUSHED:
			return m_actionPush;
		case DEFENDER_STUMBLES:
			return m_actionDefenderStumbles;
		case DEFENDER_DOWN:
			return m_actionDefenderDown;
		}
		return null;
	}

	/**
	 * Creates a new icon based on the player code.
	 * 
	 * @param code
	 *            - the code used to determine which icon to create
	 * @return Returns a BufferedImage object
	 */
	public BufferedImage getIcon(final Player player) {
		if (player == null) {
			return null;
		}

		switch (player.race()) {
		case ORC:
			switch (player.role()) {
			case BLITZER:
				return m_orcBlitzer1;
			case BLOCKER:
				return m_orcBlocker1;
			case THROWER:
				return m_orcThrower1;
			case LINEMAN:
				return m_orcLineman1;
			case CATCHER:
				return m_orcCatcher1;
			}
			break;
		case HUMAN:
			switch (player.role()) {
			case BLITZER:
				return m_humanBlitzer1;
			case BLOCKER:
				return m_humanBlocker1;
			case THROWER:
				return m_humanThrower1;
			case LINEMAN:
				return m_humanLineman1;
			case CATCHER:
				return m_humanCatcher1;
			}
			break;
		case ELF:
			switch (player.role()) {
			case BLITZER:
				return m_elfBlitzer1;
			case BLOCKER:
				return m_elfBlocker1;
			case THROWER:
				return m_elfThrower1;
			case LINEMAN:
				return m_elfLineman1;
			case CATCHER:
				return m_elfCatcher1;
			}
			break;
		default:
		}
		return null;
	}

	/**
	 * Returns the standard pitch icon based on the position given.
	 * 
	 * @return BufferedImage The pitch tile.
	 */
	private BufferedImage getPitchIcon(final Position pos) {
		// End lines
		if (pos.x() == 0) {
			return m_pitchR;
		} else if (pos.x() == 25) {
			return m_pitchL;
		} else if (pos.y() == 3) {
			if (pos.x() == 12) {
				return m_pitchRD;
			} else if (pos.x() == 13) {
				return m_pitchLD;
			} else {
				return m_pitchD;
			}
		} else if (pos.y() == 11) {
			if (pos.x() == 12) {
				return m_pitchRU;
			} else if (pos.x() == 13) {
				return m_pitchLU;
			} else {
				return m_pitchU;
			}
		}
		// Scrimmage lines
		else if (pos.x() == 12) {
			return m_pitchR;
		} else if (pos.x() == 13) {
			return m_pitchL;
		} else if (Pitch.isInsidePitch(pos)) {
			return m_pitch;
		} else {
			return null;
		}
	}

	/**
	 * This method creates the appropriate icon based on the parameters.
	 * 
	 * @param player
	 *            Can be null. If not, the image will contain the player icon
	 *            and annotations.
	 * @param isBall
	 *            Will add the ball token drawing if true.
	 * @param component
	 *            Component that will contain the image. Used to get dimensions.
	 * @return BufferedImage - The image corresponding to that pitch cell.
	 */
	public BufferedImage getPitchIcon(final Player player, final boolean isBall, final Position pos, final Component component) {
		// Get necessary data from the component
		final int w = component.getWidth();
		final int h = component.getHeight();

		if (w == 0 || h == 0) {
			return null;
		}

		// First create an image with the appropriate dimensions
		final BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2d = image.createGraphics();

		// Clear it
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, w, h);
		g2d.setColor(Color.black);

		// First, draw the pitch icon
		final BufferedImage pitchImage = getPitchIcon(pos);
		if (pitchImage != null) {
			drawImageToImage(pitchImage, image, 1.0, 0.5, 0.5, 0, false); // full-size,
																			// centered
		}

		// Then, if there is a player, draw it
		if (player != null) {
			int quadrant = 0;

			if (player.state() == PlayerState.PRONE) {
				quadrant = 1;
			} else if (player.state() == PlayerState.STUNNED) {
				quadrant = 2;
			}

			drawImageToImage(getIcon(player), image, 1.0, 0.5, 0.5, quadrant, true); // full-size,
																						// centered
		}

		// Then, draw the ball if it is present
		if (isBall) {
			drawImageToImage(m_ball, image, 0.5, 1.0, 1.0, 0, true); // half-size,
																		// bottom
																		// right
		}

		// If it was a player, add annotations (player type + player number)
		if (player != null) {

			// Group number
			{
				final PlayerGroup playerGroup = player.getPlayerGroup();
				if (playerGroup != null) {
					int rgb = Color.HSBtoRGB(playerGroup.getId() / 5f, 1f, 1f);
					g2d.setColor(new Color(rgb));
					g2d.drawOval(1, 1, w - 1, h - 1);
					g2d.drawOval(2, 2, w - 2, h - 2);
				}
			}

			// If the player is selected, we MUST show it visually
			if (player.isSelected()) {
				final BufferedImage mask = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
				final Graphics2D maskGraphics = mask.createGraphics();
				maskGraphics.setColor(new Color(0f, 0f, 0f, 0.40f));
				maskGraphics.fillRect(0, 0, w, h);

				drawImageToImage(mask, image, 1.0, 0.0, 0.0, 0, false);
			}

			final String typeString = player.typeString();
			final int fontSize = 16;
			final int offset = 2;
			// Could use metrics in here...

			g2d.setColor(Color.RED);
			g2d.setFont(new Font("Dialog", Font.BOLD, fontSize));

			// Write role
			g2d.drawString(typeString, offset, h - offset);

			// Write number
			g2d.setColor(Color.WHITE);
			g2d.drawString(Integer.toString(player.number()), w - 1.5f * fontSize, fontSize);

		} else {
			final int fontSize = 10;
			g2d.setColor(Color.YELLOW);
			g2d.setFont(new Font("Dialog", Font.PLAIN, fontSize));
			g2d.drawString(pos.x() + ":" + pos.y(), (w - fontSize) / 2, h - fontSize / 2);
			AIEngine aiEngine = GameEngine.getInstance() != null ? GameEngine.getInstance().getAiEngine(
					GameEngine.getInstance().currentTeamID()) : null;
			if (aiEngine != null) {
				g2d.drawString(aiEngine.getDangerValue(pos) + "", fontSize, fontSize);
			}

		}

		// Finally, draw thin lines to make cells differentiable.
		if (pitchImage != null) {
			g2d.setColor(new Color(0f, 0f, 0f, 0.25f));
			g2d.drawRect(0, 0, w, h);
		}

		return image;
	}

	/**
	 * Draws the source image (src) in the destination image (dst) according to
	 * the parameters given. Takes care of scaling, offsets and aspect ratio.
	 * 
	 * @param src
	 *            The source image (BufferedImage)
	 * @param dst
	 *            The destination image (BufferedImage)
	 * @param srcScale
	 *            The scale we wish to draw the src image to. Between 0 and 1,
	 *            where a value of 1 means the source image will fit the
	 *            destination image completely.
	 * @param srcOffsetX
	 *            The relative offset vs. the center of the image. Between 0 and
	 *            1.
	 * @param srcOffsetY
	 *            The relative Y offset vs. the center of the destination image.
	 *            Between 0 and 1.
	 * @param keepAspectRatio
	 *            Hints whether the image needs to preserve aspect ratio or not.
	 *            Impacts the scales.
	 */
	private void drawImageToImage(final BufferedImage src, final BufferedImage dst, final double srcScale, final double srcOffsetX,
			final double srcOffsetY, final int quadrant, final boolean keepAspectRatio) {
		if (src == null) {
			return;
		}

		assert (dst != null);

		final AffineTransform transform = new AffineTransform();

		final double dW = dst.getWidth();
		final double dH = dst.getHeight();
		final double sW = src.getWidth();
		final double sH = src.getHeight();

		// First step : scale object to target size
		double srcWScale = dW / sW;
		double srcHScale = dH / sH;

		// Normalize if we want to preserve aspect ratio
		if (keepAspectRatio) {
			final double scale = (srcWScale < srcHScale ? srcWScale : srcHScale);
			srcWScale = scale;
			srcHScale = scale;
		}

		// Scale according to src scale (needed for images that won't fit the
		// target entirely)
		srcWScale *= srcScale;
		srcHScale *= srcScale;

		// Apply scale
		final AffineTransform scaleMat = AffineTransform.getScaleInstance(srcWScale, srcHScale);

		// Second step : rotate
		final double scaledW = sW * srcWScale;
		final double scaledH = sH * srcHScale;

		final double radAngle = quadrant * Math.PI / 2.0;

		// transform.rotate( radAngle, scaledW / 2.0, scaledH / 2.0 );
		final AffineTransform rotMat = AffineTransform.getRotateInstance(radAngle, scaledW / 2.0, scaledH / 2.0);

		// Third step : translate as needed
		final double offsetW = (dW - scaledW) * srcOffsetX;
		final double offsetH = (dH - scaledH) * srcOffsetY;

		final AffineTransform transMat = AffineTransform.getTranslateInstance(offsetW, offsetH);

		transform.concatenate(transMat);
		transform.concatenate(rotMat);
		transform.concatenate(scaleMat);

		// Paint the background to the image
		final Graphics2D g2d = dst.createGraphics();
		g2d.drawImage(src, transform, null);
	}

	/**
	 * Returns the highlight color for a specific position
	 * 
	 * @param pos
	 *            The position for which we want the color
	 * @return The highlight color or null if there is none.
	 */
	public Color getHighlight(final Position pos) {
		// Return first color matching with position
		for (final SelectionObject sel : m_selection) {
			final Color highlight = sel.color(pos);
			if (highlight != null) {
				return highlight;
			}
		}

		return null;
	}

	/**
	 * Adds a selection object that will serve to highlight or darken some cells
	 * 
	 * @param sel
	 *            The selection object telling which cell to change or not.
	 */
	public void addSelection(final SelectionObject sel) {
		m_selection.add(sel);
	}

	/**
	 * Removes a selection object from the highlighted ones.
	 * 
	 * @param sel
	 *            The selection object to remove.
	 */
	public void removeSelection(final SelectionObject sel) {
		m_selection.remove(sel);
	}

	/**
	 * Returns the list of selection objects used in the icon factory
	 * 
	 * @return List of selection objects used.
	 */
	public List<SelectionObject> getSelection() {
		return m_selection;
	}

	/**
	 * Removes all selection object from the icon factory
	 */
	public void clearSelection() {
		m_selection.clear();
	}

	private BufferedImage rotateImage(final BufferedImage src, final int angle) {
		final int w = src.getWidth();
		final int h = src.getHeight();
		final BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		final Graphics2D bg = bi.createGraphics();
		bg.rotate(Math.toRadians(angle), w / 2, h / 2);
		bg.drawImage(src, 0, 0, w, h, 0, 0, w, h, null);

		// clean up used resources
		bg.dispose();

		return bi;
	}
}

