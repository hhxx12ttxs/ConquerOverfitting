package mw.client.utils.cache;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import mw.client.constants.Constants;
import mw.client.managers.SettingsManager;
import mw.client.utils.CardImageUtils;
import mw.mtgforge.Constant;
import mw.mtgforge.ImageUtil;
import mw.server.model.bean.CardBean;

import org.apache.log4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.ComputationException;
import com.google.common.collect.MapMaker;
import com.mortennobel.imagescaling.ResampleOp;

/**
 * This class stores ALL card images in a cache with soft values. this means
 * that the images may be collected when they are not needed any more, but will
 * be kept as long as possible.
 * 
 * Key format: "<cardname>#<setname>#<collectorID>#<param>"
 * 
 * where param is:
 * 
 * <ul>
 * <li>#Normal: request for unrotated image</li>
 * <li>#Tapped: request for rotated image</li>
 * <li>#Cropped: request for cropped image that is used for Shandalar like card
 * look</li>
 * </ul>
 */
public class ImageCache {

	private static final Logger log = Logger.getLogger(ImageCache.class);

	private static final Map<String, BufferedImage> imageCache;

	private static final String NORMAL = "#Normal", TAPPED = "#Tapped", CROPPED = "#Cropped";

	/**
	 * Common pattern for keys.
	 * Format: "<cardname>#<setname>#<collectorID>"
	 */
	private static final Pattern KEY_PATTERN = Pattern.compile("(.*)#(.*)#(.*)");
	
	static {
		imageCache = new MapMaker().softValues().makeComputingMap(new Function<String, BufferedImage>() {
			public BufferedImage apply(String key) {
				try {
					if (key.endsWith(NORMAL)) {
						key = key.substring(0, key.length() - NORMAL.length());
						return getNormalSizeImage(imageCache.get(key));
					} else if (key.endsWith(TAPPED)) {
						key = key.substring(0, key.length() - TAPPED.length());
						return getTappedSizeImage(imageCache.get(key));
					} else if (key.endsWith(CROPPED)) {
						key = key.substring(0, key.length() - CROPPED.length());
						return getCroppedSizeImage(imageCache.get(key), key);
					}

					Matcher m = KEY_PATTERN.matcher(key);

					if (m.matches()) {
						String name = m.group(1);
						String set = m.group(2);
						Integer collectorID = Integer.parseInt(m.group(3));

						CardBean dummy = new CardBean();
						dummy.setName(name);
						dummy.setSetName(set);
						dummy.setCollectorID(collectorID);
						if (collectorID == 0) dummy.setToken(true);

						String path = CardImageUtils.getImagePath(dummy);
						if (path == null) return null;
						File file = new File(path);

						BufferedImage image = loadImage(file);
						return image;
					} else {
						throw new RuntimeException(
								"Requested image doesn't fit the requirement for key (<cardname>#<setname>#<collectorID>): " + key);
					}
				} catch (Exception ex) {
					if (ex instanceof ComputationException)
						throw (ComputationException) ex;
					else
						throw new ComputationException(ex);
				}
			}
		});
	}

	/**
	 * Get card image
	 * 
	 * @param card
	 * @return
	 */
	public static BufferedImage getImage(CardBean card) {
		String key = getKey(card);
		if (card.isTapped())
			key += TAPPED;
		else
			key += NORMAL;
		return getImage(key);
	}
	
	public static BufferedImage getImageOriginal(CardBean card) {
		String key = getKey(card) + NORMAL;
		return getImage(key);
	}

	/**
	 * Get cropped image for Shandalar like card look
	 * 
	 * @param card
	 * @return
	 */
	public static Image getCroppedImage(CardBean card) {
		try {
			String key = getKey(card) + CROPPED;
			BufferedImage image = imageCache.get(key);
			return image;
		} catch (NullPointerException ex) {
			// unfortunately NullOutputException, thrown when apply() returns
			// null, is not public
			// NullOutputException is a subclass of NullPointerException
			// legitimate, happens when a card has no image
			return null;
		} catch (ComputationException ex) {
			if (ex.getCause() instanceof NullPointerException)
				return null;
			log.error(ex,ex);
			return null;
		}

	}

	/**
	 * Returns the Image corresponding to the key
	 */
	private static BufferedImage getImage(String key) {
		try {
			BufferedImage image = imageCache.get(key);
			return image;
		} catch (NullPointerException ex) {
			// unfortunately NullOutputException, thrown when apply() returns
			// null, is not public
			// NullOutputException is a subclass of NullPointerException
			// legitimate, happens when a card has no image
			return null;
		} catch (ComputationException ex) {
			if (ex.getCause() instanceof NullPointerException)
				return null;
			log.error(ex,ex);
			return null;
		}
	}

	/**
	 * Returns the map key for a card, without any suffixes for the image size.
	 */
	private static String getKey(CardBean card) {
		String set = card.getSetName();
		String key = card.getName() + "#" + set + "#" + card.getCollectorID();

		return key;
	}

	/**
	 * Load image from file
	 * 
	 * @param file
	 *            file to load image from
	 * @return {@link BufferedImage}
	 */
	public static BufferedImage loadImage(File file) {
		BufferedImage image = null;
		if (!file.exists()) {
			return null;
		}
		try {
			image = ImageIO.read(file);
		} catch (Exception e) {
			log.error(e, e);
		}

		return image;
	}

	private static BufferedImage getCroppedImage(BufferedImage original, CardBean card) {

		if (original == null)
			return null;

		Image image = original.getScaledInstance(Constants.DEFAULT_CARD_IMAGE_SIZE.width, Constants.DEFAULT_CARD_IMAGE_SIZE.height,
				java.awt.Image.SCALE_SMOOTH);
		Toolkit tk = Toolkit.getDefaultToolkit();
		Image imageResized = tk.createImage(new FilteredImageSource(image.getSource(), new CropImageFilter(30, 29, 140, 131)));

		HashSet<String> oldSetBorders = new HashSet<String>();
		oldSetBorders.add("ODY");
		oldSetBorders.add("TOR");
		oldSetBorders.add("JUD");
		oldSetBorders.add("4ED");
		oldSetBorders.add("5ED");
		oldSetBorders.add("6ED");
		oldSetBorders.add("7ED");
		oldSetBorders.add("INV");
		oldSetBorders.add("MMQ");
		oldSetBorders.add("NMS");
		oldSetBorders.add("ONS");
		oldSetBorders.add("PLS");
		oldSetBorders.add("SCG");
		oldSetBorders.add("LGN");
		oldSetBorders.add("ULG");
		oldSetBorders.add("USG");
		oldSetBorders.add("VIS");
		oldSetBorders.add("NEM");
		oldSetBorders.add("TSB");
		oldSetBorders.add("TMP");
		oldSetBorders.add("MIR");
		oldSetBorders.add("APC");

		if (oldSetBorders.contains(card.getSetName())) {
			imageResized = tk.createImage(new FilteredImageSource(image.getSource(), new CropImageFilter(19, 22, 163, 135)));
		} 
		/*else if (card.getName().contains("Ancient")) {
			imageResized = tk.createImage(new FilteredImageSource(image.getSource(), new CropImageFilter(16, 28, 170, 135)));
		} */
		else if (card.isBasicLand() && !(card.getCollectorID() >= 250)) {
			imageResized = tk.createImage(new FilteredImageSource(image.getSource(), new CropImageFilter(16, 70, 170, 135)));
		}

		return getResizedImage(ImageUtil.convertImageToBuffered(imageResized), Constants.IMAGE_SIZE_CROPPED);
	}

	/**
	 * Returns an image scaled to the size given in {@link Constant.Runtime}
	 */
	private static BufferedImage getNormalSizeImage(BufferedImage original) {
		int srcWidth = original.getWidth();
		int srcHeight = original.getHeight();
		int tgtWidth = SettingsManager.getManager().getCardSize().width;
		int tgtHeight = SettingsManager.getManager().getCardSize().height;

		if (srcWidth == tgtWidth && srcHeight == tgtHeight)
			return original;

		ResampleOp resampleOp = new ResampleOp(tgtWidth, tgtHeight);
		BufferedImage image = resampleOp.filter(original, null);
		return image;
	}

	/**
	 * Returns cropped image Determines card params and calls
	 * {@link #getCroppedImage(CardBean)}
	 */
	private static BufferedImage getCroppedSizeImage(BufferedImage original, String params) {
		CardBean card = new CardBean();
		return getCroppedImage(original, card);
	}

	/**
	 * Returns an image scaled to the size given in {@link
	 * SettingsManager.getManager().getCardSize()}, but rotated
	 */
	private static BufferedImage getTappedSizeImage(BufferedImage original) {
		int tgtWidth = SettingsManager.getManager().getCardSize().width;
		int tgtHeight = SettingsManager.getManager().getCardSize().height;

		AffineTransform at = new AffineTransform();
		at.translate(tgtHeight, 0);
		at.rotate(Math.PI / 2);

		ResampleOp resampleOp = new ResampleOp(tgtWidth, tgtHeight);
		BufferedImage image = resampleOp.filter(original, null);
		BufferedImage rotatedImage = new BufferedImage(tgtHeight, tgtWidth, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) rotatedImage.getGraphics();
		g2d.drawImage(image, at, null);
		g2d.dispose();
		return rotatedImage;
	}

	/**
	 * Returns an image scaled to the size appropriate for the card picture
	 * panel For future use.
	 */
	private static BufferedImage getFullSizeImage(BufferedImage original, double scale) {
		if (scale == 1)
			return original;
		ResampleOp resampleOp = new ResampleOp((int) (original.getWidth() * scale), (int) (original.getHeight() * scale));
		BufferedImage image = resampleOp.filter(original, null);
		return image;
	}

	/**
	 * Returns an image scaled to the size appropriate for the card picture
	 * panel
	 */
	private static BufferedImage getResizedImage(BufferedImage original, Rectangle sizeNeed) {
		ResampleOp resampleOp = new ResampleOp(sizeNeed.width, sizeNeed.height);
		BufferedImage image = resampleOp.filter(original, null);
		return image;
	}

	/**
	 * Returns the image appropriate to display the card in the picture panel
	 */
	public static BufferedImage getImage(CardBean card, int width, int height) {
		String key = getKey(card);
		BufferedImage original = getImage(key);
		if (original == null)
			return null;

		double scale = Math.min((double) width / original.getWidth(), (double) height / original.getHeight());
		if (scale > 1)
			scale = 1;

		return getFullSizeImage(original, scale);
	}
}

