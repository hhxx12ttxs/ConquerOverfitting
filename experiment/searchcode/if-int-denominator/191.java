<<<<<<< HEAD
/*   
 *   Remuco - A remote control system for media players.
 *   Copyright (C) 2006-2010 by the Remuco team, see AUTHORS.
 *
 *   This file is part of Remuco.
 *
 *   Remuco is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Remuco is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Remuco.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */
package remuco.client.jme.ui;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import remuco.client.common.util.Log;
import remuco.client.common.util.Tools;
import remuco.client.jme.Config;
import remuco.client.jme.OptionDescriptor;
import remuco.client.jme.ui.screenies.Screeny;
import remuco.client.jme.ui.screenies.ScreenyException;

public final class Theme {

	/** Small font */
	public static final Font FONT_SMALL = Font.getFont(Font.FACE_PROPORTIONAL,
		Font.STYLE_PLAIN, Font.SIZE_SMALL);

	/** Normal font */
	public static final Font FONT_NORMAL = Font.getFont(Font.FACE_PROPORTIONAL,
		Font.STYLE_PLAIN, Font.SIZE_MEDIUM);

	/** Large font */
	public static final Font FONT_LARGE = Font.getFont(Font.FACE_PROPORTIONAL,
		Font.STYLE_PLAIN, Font.SIZE_LARGE);

	/** Font for an item's artist */
	public static final Font FONT_ARTIST = FONT_NORMAL;

	/** Font for an item's album */
	public static final Font FONT_ALBUM = FONT_SMALL;

	/** Font for an item's title */
	public static final Font FONT_TITLE = Font.getFont(Font.FACE_PROPORTIONAL,
		Font.STYLE_BOLD, Font.SIZE_LARGE);

	/** Font for progress value */
	public static final Font FONT_PROGRESS = Font.getFont(Font.FACE_MONOSPACE,
		Font.STYLE_PLAIN, Font.SIZE_SMALL);

	/** Font for a volume level */
	public static final Font FONT_VOLUME = Font.getFont(Font.FACE_MONOSPACE,
		Font.STYLE_PLAIN, Font.SIZE_LARGE);

	/** Half height of small font. */
	public static final int LINE_GAP = FONT_SMALL.getHeight() / 2;

	/** Third height of small font. */
	public static final int LINE_GAP_SMALL = FONT_SMALL.getHeight() / 3;

	public static final OptionDescriptor OD_THEME;

	/**
	 * Theme element ID (<code>RTE_..</code>) or color ID (<code>RTC_..</code>)
	 */
	public static final byte RTE_BUTTONBAR_LEFT = 0, RTE_BUTTONBAR_SPACER = 1,
			RTE_BUTTONBAR_RIGHT = 2, RTE_BUTTON_FULLSCREEN = 3,
			RTE_BUTTON_NEXT = 4, RTE_BUTTON_PREV = 5, RTE_BUTTON_RATE = 6,
			RTE_BUTTON_TAGS = 7, RTE_STATE_LEFT = 8, RTE_STATE_SPACER = 9,
			RTE_STATE_RIGHT = 10, RTE_BUTTON_PLAYBACK_PAUSE = 11,
			RTE_BUTTON_PLAYBACK_PLAY = 12, RTE_BUTTON_PLAYBACK_STOP = 13,
			RTE_BUTTON_REPEAT_OFF = 14, RTE_BUTTON_REPEAT_ON = 15,
			RTE_BUTTON_SHUFFLE_OFF = 16, RTE_BUTTON_SHUFFLE_ON = 17,
			RTE_SLIDER_VOLUME_LEFT = 18, RTE_SLIDER_VOLUME_OFF = 19,
			RTE_SLIDER_VOLUME_ON = 20, RTE_SLIDER_VOLUME_RIGHT = 21,
			RTC_BG = 22, RTC_TEXT_ALBUM = 23, RTC_TEXT_ARTIST = 24,
			RTC_TEXT_OTHER = 25, RTC_TEXT_TITLE = 26, RTE_ICON_RATING_OFF = 27,
			RTE_ICON_RATING_ON = 28;

	private static final Image IMG_FALLBACK;

	/** Theme element file names (without extension) */
	private static final String[] IMG_NAME = {

			// button bar elements
			"rte.buttonbar.left",
			"rte.buttonbar.spacer",
			"rte.buttonbar.right",
			"rte.button.fullscreen",
			"rte.button.next",
			"rte.button.prev",
			"rte.button.rate",
			"rte.button.tags",

			// state bar elements
			"rte.state.left", "rte.state.spacer", "rte.state.right",
			"rte.button.playback.pause", "rte.button.playback.play",
			"rte.button.playback.stop", "rte.button.repeat.off",
			"rte.button.repeat.on", "rte.button.shuffle.off",
			"rte.button.shuffle.on", "rte.slider.volume.left",
			"rte.slider.volume.off", "rte.slider.volume.on",
			"rte.slider.volume.right",

			// colors
			"rte.color.bg", "rte.color.text.album", "rte.color.text.artist",
			"rte.color.text.other", "rte.color.text.title",

			// misc icons
			"rte.icon.rating.off", "rte.icon.rating.on" };

	private static Theme instance = null;

	/** Available list icons sizes. */
	private static final int LICS[];

	static {

		// //// create fall back image for missing theme images //////

		IMG_FALLBACK = Image.createImage(5, 5);

		final Graphics g = IMG_FALLBACK.getGraphics();

		g.setColor(0);
		g.drawLine(0, 0, 20 - 1, 20 - 1);
		g.drawLine(20 - 1, 0, 0, 20 - 1);

		// //// create option descriptor //////

		final String themes[] = Tools.splitString("@THEMES@", ',', true);

		OD_THEME = new OptionDescriptor("theme", "Theme", themes[0], themes);

		// //// list icon sizes //////

		final String sa[] = Tools.splitString("@LICS@", ',', true);

		LICS = new int[sa.length];

		for (int i = 0; i < sa.length; i++) {
			try {
				LICS[i] = Integer.parseInt(sa[i]);
			} catch (NumberFormatException e) {
				Log.bug("Aug 12, 2009.6:17:11 PM");
				LICS[i] = 12;
			}
		}

	}

	public static Font getBestFontForHeight(int h, int buffer) {

		if (FONT_LARGE.getHeight() <= h - 2 * buffer)
			return FONT_LARGE;

		if (FONT_NORMAL.getHeight() <= h - 2 * buffer)
			return FONT_LARGE;

		return FONT_SMALL;
	}

	/** Get the singleton theme instance. */
	public static Theme getInstance() {
		if (instance == null) {
			instance = new Theme();
		}
		return instance;
	}

	/**
	 * Stretch an image by adding transparent pixels to the left and right side.
	 * 
	 * @param img
	 *            the image
	 * @param wNew
	 *            the width of the new image
	 * @return a new Image where transparent pixels has been added to the left
	 *         and right side of the given image so that it has the given width
	 *         and so that the given image is centered in the new image (if
	 *         <i>wNew</i> is less or equal to the width of <i>img</i>,
	 *         <i>img</i> itself returned)
	 */
	public static Image pseudoStretch(Image img, final int wNew) {

		final int wOrig = img.getHeight();

		if (wNew <= wOrig)
			return img;

		final int h = img.getHeight();

		try {
			final int rgbOrig[] = new int[wOrig * h];
			img.getRGB(rgbOrig, 0, wOrig, 0, 0, wOrig, h);

			final int rgbNew[] = new int[wNew * h];

			final int xOffset = (wNew - wOrig) / 2;

			for (int y = 0; y < h; y++) {
				for (int x = 0; x < xOffset; x++) {
					rgbNew[y * wNew + x] = 0x00FFFFFF;
				}
				for (int x = xOffset; x < xOffset + wOrig; x++) {
					rgbNew[y * wNew + x] = rgbOrig[y * wOrig + x - xOffset];
				}
				for (int x = xOffset + wOrig; x < wNew; x++) {
					rgbNew[y * wNew + x] = 0x00FFFFFF;
				}
			}

			return Image.createRGBImage(rgbNew, wNew, h, true);

		} catch (Exception e) {
			Log.bug("Jan 31, 2009.5:16:04 PM", e);
			return img;
		}

	}

	/**
	 * Scale an image. The width and height get scaled by the factor
	 * <code>numerator/denominator</code>.
	 * 
	 * @param img
	 *            the image to get a scaled copy from
	 * @param numerator
	 * @param denominator
	 * 
	 * @return a scaled immutable copy of the source image with
	 *         <code>widthScaled = widthSource * numerator / denominator</code>
	 *         and
	 *         <code>heightScaled = heightSource * numerator / denominator</code>
	 *         or the same image if <code>numerator == denominator</code>
	 */
	public static Image scaleImage(Image img, int numerator, int denominator) {

		if (numerator == denominator)
			return img;

		try {

			final int rgb[] = new int[img.getWidth() * img.getHeight()];

			img.getRGB(rgb, 0, img.getWidth(), 0, 0, img.getWidth(),
				img.getHeight());

			final int wOrig = img.getWidth();
			final int w = wOrig * numerator / denominator;
			final int h = img.getHeight() * numerator / denominator;
			final int rgbSchrink[] = new int[w * h];

			for (int y = 0; y < h; y++) {
				final int yOrig = y * denominator / numerator * wOrig;
				final int yScaled = y * w;
				for (int x = 0; x < w; x++) {
					rgbSchrink[x + yScaled] = rgb[x * denominator / numerator
							+ yOrig];
				}
			}

			return Image.createRGBImage(rgbSchrink, w, h, false);

		} catch (Exception e) {
			Log.bug("Jan 31, 2009.5:05:48 PM", e);
			return img;
		}

	}

	/**
	 * Shrinks an image if its width or height exceeds the boundaries given by
	 * <code>maxWidth</code> and <code>maxHeight</code>.
	 * 
	 * @param img
	 *            the image to shrink
	 * @param maxWidth
	 *            the maximum allowed width of the image
	 * @param maxHeight
	 *            the maximum allowed height of the image
	 * @return a shrunk copy of the image (<i>immutable!</i>) or
	 *         <code>img</code> if shrinking is not needed
	 */
	public static Image shrinkImageIfNeeded(Image img, int maxWidth,
			int maxHeight) {

		if (img.getHeight() > maxHeight)
			img = Theme.scaleImage(img, 10 * maxHeight / img.getHeight(), 10);
		if (img.getWidth() > maxWidth)
			img = Theme.scaleImage(img, 10 * maxWidth / img.getWidth(), 10);

		return img;

	}

	/**
	 * Split a one-line string to multi-line string, depending on available
	 * width and font size.
	 * 
	 * @param s
	 *            the string to split
	 * @param maxWidth
	 *            available width for strings
	 * @param f
	 *            font to use to calculate width of strings
	 * @return a string array with every string not exceeding <code>width</code>
	 *         when displayed in font <code>f</code>
	 */
	public static String[] splitString(String s, int maxWidth, Font f) {

		if (f.stringWidth(s) <= maxWidth) {
			return new String[] { s };
		}

		maxWidth -= f.charWidth('W'); // tweak the algorithm below

		int w, slen, i, goodBreakPos;

		final Vector v = new Vector(3);

		w = f.stringWidth(s);

		while ((slen = s.length()) > 0) {

			goodBreakPos = 0;
			i = 1;
			w = 0;
			while (w < maxWidth && i < slen) {
				if (s.charAt(i) == ' ') {
					goodBreakPos = i;
				}
				w = f.substringWidth(s, 0, i);
				i++;
			}
			if (w >= maxWidth) {
				if (goodBreakPos > 0) {
					v.addElement(s.substring(0, goodBreakPos));
					s = s.substring(goodBreakPos < slen - 1 ? goodBreakPos + 1
							: goodBreakPos);
				} else {
					v.addElement(s.substring(0, i - 1));
					s = s.substring(i - 1);
				}
			} else {
				break;
			}
		}

		if (slen > 0) {
			v.addElement(s);
		}

		final int n = v.size();
		final String[] sa = new String[n];
		for (i = 0; i < n; i++) {
			sa[i] = (String) v.elementAt(i);
		}

		return sa;

	}

	/**
	 * Load an image file.
	 * 
	 * @param file
	 *            path to the file
	 * @param fallBackSize
	 *            size of the fallback image to return if loading fails (if set
	 *            to zero then {@link #IMG_FALLBACK} is used as fallback image)
	 * @return the image (never <code>null</code>)
	 */
	private static Image loadImage(String file, int fallBackSize) {

		try {
			return Image.createImage(file);
		} catch (IOException e) {
			Log.ln("[TH] missing " + file);
			if (fallBackSize == 0) {
				return IMG_FALLBACK;
			} else {
				final Image img = Image.createImage(fallBackSize, fallBackSize);
				img.getGraphics().setColor(0);
				img.getGraphics().drawString("X", 2, 2,
					Graphics.TOP | Graphics.LEFT);
				return img;
			}
		}
	}

	/** Load a list icon for the given list icon size. */
	private static Image loadListIcon(String name, int size) {
		return loadImage("/icons/" + size + "/" + name + "_" + size + ".png",
			size);
	}

	/** Alert icon */
	public final Image aicBluetooth, aicWifi, aicConnecting, aicRefresh,
			aicHmpf, aicYes;

	/** List icon */
	public final Image licBluetooth, licWifi, licItem, licItemMarked, licList,
			licAdd, licThemes, licKeys, licOff, licLog, licDisconnect,
			licQueue, licSearch, licMLib, licFiles;

	private final Config config;

	private String current = null;

	private final Image[] img;

	private final Image logos[];

	private Theme() {

		config = Config.getInstance();

		img = new Image[IMG_NAME.length];

		// alert icons //

		aicBluetooth = loadImage("/icons/uni/bluetooth.png", 48);
		aicWifi = loadImage("/icons/uni/wifi.png", 48);
		aicConnecting = loadImage("/icons/uni/connecting.png", 48);
		aicRefresh = loadImage("/icons/uni/refresh.png", 48);
		aicHmpf = loadImage("/icons/uni/hmpf.png", 48);
		aicYes = loadImage("/icons/uni/yes.png", 48);

		// list icons //

		int size = -1;

		for (int i = LICS.length - 1; i >= 0; i--) {
			size = LICS[i];
			if (config.SUGGESTED_LICS >= LICS[i]) {
				break;
			}
		}

		licBluetooth = loadListIcon("bluetooth", size);
		licWifi = loadListIcon("wifi", size);
		licItem = loadListIcon("item", size);
		licItemMarked = loadListIcon("item_blue", size);
		licList = loadListIcon("list", size);
		licQueue = loadListIcon("queue", size);
		licMLib = loadListIcon("mlib", size);
		licFiles = loadListIcon("files", size);
		licSearch = loadListIcon("search", size);
		licAdd = loadListIcon("add", size);
		licThemes = loadListIcon("theme", size);
		licKeys = loadListIcon("keys", size);
		licOff = loadListIcon("off", size);
		licDisconnect = loadListIcon("disconnect", size);
		licLog = licList;

		// logo icons

		final int sizes[] = { 128, 96, 64, 48, 0 };

		logos = new Image[sizes.length];

		for (int i = 0; i < sizes.length; i++) {
			logos[i] = loadImage("/icons/uni/remuco_" + sizes[i] + ".png",
				sizes[i]);
		}

		// load default theme

		load(config.getOption(OD_THEME));

	}

	/**
	 * Calculate uniform gaps between images given a fixed overall width for the
	 * images.
	 * 
	 * @param width
	 *            the complete width available
	 * @param imgIDs
	 *            IDs of the images to distribute equally in the given width
	 * @return an array of gaps - gap <em>i</em> is the gap width right of image
	 *         <em>i</em> (hence, this array is one element shorter than
	 *         <em>imgIDs</em>)
	 * @throws ScreenyException
	 *             if there is no space for gaps
	 */
	public int[] calculateGaps(int width, int imgWidth[])
			throws ScreenyException {

		if (imgWidth.length < 2) {
			return new int[0];
		}
		int widthSum = 0;
		for (int i = 0; i < imgWidth.length; i++) {
			widthSum += imgWidth[i];
		}
		if (widthSum >= width + imgWidth.length * 2) {
			throw new ScreenyException("no space for gaps");
		}
		int gaps[] = new int[imgWidth.length - 1];
		for (int i = 0; i < gaps.length; i++) {
			gaps[i] = (width - widthSum) / gaps.length;
		}
		gaps[gaps.length - 1] += (width - widthSum) % gaps.length;

		return gaps;
	}

	/**
	 * Calculate uniform gaps between screenies given a fixed overall width for
	 * the screenies.
	 * 
	 * @param width
	 *            the complete width available
	 * @param screenies
	 *            screenes to distribute equally
	 * @return an array of gaps - gap <em>i</em> is the gap width right of
	 *         screeny <em>i</em> (hence, this array is one element shorter than
	 *         <em>scrennies</em>)
	 * @throws ScreenyException
	 *             if there is no space for gaps
	 */
	public int[] calculateGaps(int width, Vector screenies)
			throws ScreenyException {

		int imgWidth[] = new int[screenies.size()];
		for (int i = 0; i < imgWidth.length; i++) {
			imgWidth[i] = ((Screeny) screenies.elementAt(i)).getWidth();
		}
		return calculateGaps(width, imgWidth);

	}

	/**
	 * Get a specific color of this theme.
	 * 
	 * @param id
	 *            the color id, one of <code>RTC_...</code>
	 * @return the color value
	 */
	public int getColor(int id) {
		final int rgb[] = new int[1];
		img[id].getRGB(rgb, 0, 1, 0, 0, 1, 1);
		return rgb[0];
	}

	/**
	 * Get a specific image of this theme.
	 * 
	 * @param id
	 *            the image id, one of <code>RTE_...</code>
	 * @return the image
	 */
	public Image getImg(int id) {
		return img[id];
	}

	/** Get a logo image which has a maximum height of <em>maxHeight</em>. */
	public Image getLogo(int maxHeight) {

		if (maxHeight <= 0) {
			return logos[0];
		}

		for (int i = 0; i < logos.length; i++) {
			if (maxHeight >= logos[i].getHeight()) {
				return logos[i];
			}
		} // last logo has size 1x1

		Log.bug("Mar 19, 2009.11:01:07 PM");

		return logos[logos.length - 1];

	}

	/**
	 * Load a new theme.
	 * 
	 * @param name
	 *            theme name (one of {@link OptionDescriptor#choices} in
	 *            {@link #OD_THEME})
	 */
	public void load(String name) {

		if (name.equals(current)) {
			return;
		}

		current = name;

		final String themeDir = "/themes/" + current + "/";

		for (int i = 0; i < img.length; i++) {
			img[i] = loadImage(themeDir + IMG_NAME[i] + ".png", 0);
		}

		Log.ln("[TH] loaded theme " + name);
	}

}
=======
/*
    Copyright (c) 2010, NullNoname
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright
          notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
          notice, this list of conditions and the following disclaimer in the
          documentation and/or other materials provided with the distribution.
 * Neither the name of NullNoname nor the names of its
          contributors may be used to endorse or promote products derived from
          this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
    AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
    IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
    ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
    LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
    CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
    SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
    INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
    CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
    ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
    POSSIBILITY OF SUCH DAMAGE.
 */
package mu.nu.nullpo.game.net;

import java.io.Serializable;
import java.util.LinkedList;

import mu.nu.nullpo.game.component.RuleOptions;

/**
 * ?????
 */
public class NetRoomInfo implements Serializable {
	/** Serial version */
	private static final long serialVersionUID = 1L;

	/** ?? number */
	public int roomID = -1;

	/** ???? */
	public String strName = "";

	/** ?????Maximum?count */
	public int maxPlayers = 6;

	/** ????????? time */
	public int autoStartSeconds = 0;

	/** ????(??) */
	public int gravity = 1;

	/** ????(??) */
	public int denominator = 60;

	/** ARE */
	public int are = 30;

	/** ARE after line clear */
	public int areLine = 30;

	/** Line clear time */
	public int lineDelay = 40;

	/** ?? time */
	public int lockDelay = 30;

	/** DAS */
	public int das = 14;

	/** Flag for types of T-Spins allowed (0=none, 1=normal, 2=all spin) */
	public int tspinEnableType = 1;

	/** Spin detection type */
	public static final int SPINTYPE_4POINT = 0, SPINTYPE_IMMOBILE = 1;

	public int spinCheckType = SPINTYPE_4POINT;

	/** Allow EZ-spins in spinCheckType 2 */
	public boolean tspinEnableEZ = false;

	/** Flag for enabling B2B */
	public boolean b2b = true;

	/** b2b adds as a separate garbage chunk */
	public boolean b2bChunk;

	/** Flag for enabling combos */
	public boolean combo = true;

	/** Allow Rensa/Combo Block */
	public boolean rensaBlock = true;

	/** Allow garbage countering */
	public boolean counter = true;

	/** Enable bravo bonus */
	public boolean bravo = true;

	/** ????? flag */
	public boolean ruleLock = false;

	/** Rule name */
	public String ruleName = "";

	/** ??? */
	public RuleOptions ruleOpt = null;

	/** ??????Number of players */
	public int playerSeatedCount = 0;

	/** ??????count */
	public int spectatorCount = 0;

	/** ??????????????(???+???) */
	public int playerListCount = 0;

	/** ???? flag */
	public boolean playing = false;

	/** Start game???Number of players */
	public int startPlayers = 0;

	/** ?????? */
	public int deadCount = 0;

	/** Automatically start timer?????????true */
	public boolean autoStartActive = false;

	/** ??OK????????Cancel???true */
	public boolean isSomeoneCancelled = false;

	/** 3??????????? Attack ????? */
	public boolean reduceLineSend = false;

	/** Rate of change of garbage holes */
	public int garbagePercent = 100;

	/** Hole change style (false=line true=attack) */
	public boolean garbageChangePerAttack = true;

	/** Divide change rate by number of live players/teams to mimic feel of 1v1 */
	public boolean divideChangeRateByPlayers = false;

	/** Garbage send type (false=Send to all, true=Target) */
	public boolean isTarget = false;

	/** Targeting time */
	public int targetTimer = 60;

	//public boolean useTankMode = false;

	/** Hurryup??????count(-1?Hurryup??) */
	public int hurryupSeconds = -1;

	/** Hurryup????Block?????????????? */
	public int hurryupInterval = 5;

	/** Automatically start timer type(false=NullpoMino true=TNET2) */
	public boolean autoStartTNET2 = false;

	/** ??OK????????Cancel???Timer??? */
	public boolean disableTimerAfterSomeoneCancelled = false;

	/** Map is enabled */
	public boolean useMap = false;

	/** ???Map */
	public int mapPrevious = -1;

	/** ??????garbage block??????? */
	public boolean useFractionalGarbage = false;

	/** Mode name */
	public String strMode = "";

	/** Single player flag */
	public boolean singleplayer = false;

	/** Rated-game flag */
	public boolean rated = false;

	/** Custom rated-game flag */
	public boolean customRated = false;

	/** Game style */
	public int style = 0;

	/** ?????? */
	public LinkedList<String> mapList = new LinkedList<String>();

	/** ??????????? */
	public LinkedList<NetPlayerInfo> playerList = new LinkedList<NetPlayerInfo>();

	/** ???? */
	public LinkedList<NetPlayerInfo> playerSeat = new LinkedList<NetPlayerInfo>();

	/** ????(Start game????????????????????????????????????) */
	public LinkedList<NetPlayerInfo> playerSeatNowPlaying = new LinkedList<NetPlayerInfo>();

	/** ???? */
	public LinkedList<NetPlayerInfo> playerQueue = new LinkedList<NetPlayerInfo>();

	/** Dead player list (Pushed from front, winner will be the first entry) */
	public LinkedList<NetPlayerInfo> playerSeatDead = new LinkedList<NetPlayerInfo>();

	/** Chat messages */
	public LinkedList<NetChatMessage> chatList = new LinkedList<NetChatMessage>();

	/**
	 * Constructor
	 */
	public NetRoomInfo() {
	}

	/**
	 * Copy constructor
	 *
	 * @param n
	 *            Copy source
	 */
	public NetRoomInfo(NetRoomInfo n) {
		copy(n);
	}

	/**
	 * String????? data????Constructor
	 *
	 * @param rdata
	 *            String???(String[7])
	 */
	public NetRoomInfo(String[] rdata) {
		importStringArray(rdata);
	}

	/**
	 * String?? data????Constructor
	 *
	 * @param str
	 *            String
	 */
	public NetRoomInfo(String str) {
		importString(str);
	}

	/**
	 * ??NetRoomInfo?????
	 *
	 * @param n
	 *            Copy source
	 */
	public void copy(NetRoomInfo n) {
		roomID = n.roomID;
		strName = n.strName;
		maxPlayers = n.maxPlayers;
		autoStartSeconds = n.autoStartSeconds;
		gravity = n.gravity;
		denominator = n.denominator;
		are = n.are;
		areLine = n.areLine;
		lineDelay = n.lineDelay;
		lockDelay = n.lockDelay;
		das = n.das;
		tspinEnableType = n.tspinEnableType;
		spinCheckType = n.spinCheckType;
		tspinEnableEZ = n.tspinEnableEZ;
		b2b = n.b2b;
		b2bChunk = n.b2bChunk;
		combo = n.combo;
		rensaBlock = n.rensaBlock;
		counter = n.counter;
		bravo = n.bravo;

		ruleLock = n.ruleLock;
		ruleName = n.ruleName;
		if (n.ruleOpt != null) {
			ruleOpt = new RuleOptions(n.ruleOpt);
		} else {
			ruleOpt = null;
		}

		playerSeatedCount = n.playerSeatedCount;
		spectatorCount = n.spectatorCount;
		playerListCount = n.playerListCount;
		playing = n.playing;
		startPlayers = n.startPlayers;
		deadCount = n.deadCount;
		autoStartActive = n.autoStartActive;
		isSomeoneCancelled = n.isSomeoneCancelled;
		reduceLineSend = n.reduceLineSend;
		hurryupSeconds = n.hurryupSeconds;
		hurryupInterval = n.hurryupInterval;
		autoStartTNET2 = n.autoStartTNET2;
		disableTimerAfterSomeoneCancelled = n.disableTimerAfterSomeoneCancelled;
		useMap = n.useMap;
		mapPrevious = n.mapPrevious;
		useFractionalGarbage = n.useFractionalGarbage;
		garbageChangePerAttack = n.garbageChangePerAttack;
		garbagePercent = n.garbagePercent;
		divideChangeRateByPlayers = n.divideChangeRateByPlayers;
		isTarget = n.isTarget;
		targetTimer = n.targetTimer;
		//useTankMode = n.useTankMode;
		strMode = n.strMode;
		singleplayer = n.singleplayer;
		rated = n.rated;
		customRated = n.customRated;
		style = n.style;

		mapList.clear();
		mapList.addAll(n.mapList);
		playerList.clear();
		playerList.addAll(n.playerList);
		playerSeat.clear();
		playerSeat.addAll(n.playerSeat);
		playerSeatNowPlaying.clear();
		playerSeatNowPlaying.addAll(n.playerSeatNowPlaying);
		playerQueue.clear();
		playerQueue.addAll(n.playerQueue);
		playerSeatDead.clear();
		playerSeatDead.addAll(n.playerSeatDead);
		chatList.clear();
		chatList.addAll(n.chatList);
	}

	/**
	 * String????? data??(Player?????)
	 *
	 * @param rdata
	 *            String???(String[43])
	 */
	public void importStringArray(String[] rdata) {
		roomID = Integer.parseInt(rdata[0]);
		strName = NetUtil.urlDecode(rdata[1]);
		maxPlayers = Integer.parseInt(rdata[2]);
		playerSeatedCount = Integer.parseInt(rdata[3]);
		spectatorCount = Integer.parseInt(rdata[4]);
		playerListCount = Integer.parseInt(rdata[5]);
		playing = Boolean.parseBoolean(rdata[6]);
		ruleLock = Boolean.parseBoolean(rdata[7]);
		ruleName = NetUtil.urlDecode(rdata[8]);
		autoStartSeconds = Integer.parseInt(rdata[9]);
		gravity = Integer.parseInt(rdata[10]);
		denominator = Integer.parseInt(rdata[11]);
		are = Integer.parseInt(rdata[12]);
		areLine = Integer.parseInt(rdata[13]);
		lineDelay = Integer.parseInt(rdata[14]);
		lockDelay = Integer.parseInt(rdata[15]);
		das = Integer.parseInt(rdata[16]);
		tspinEnableType = Integer.parseInt(rdata[17]);
		b2b = Boolean.parseBoolean(rdata[18]);
		combo = Boolean.parseBoolean(rdata[19]);
		rensaBlock = Boolean.parseBoolean(rdata[20]);
		counter = Boolean.parseBoolean(rdata[21]);
		bravo = Boolean.parseBoolean(rdata[22]);
		reduceLineSend = Boolean.parseBoolean(rdata[23]);
		hurryupSeconds = Integer.parseInt(rdata[24]);
		hurryupInterval = Integer.parseInt(rdata[25]);
		autoStartTNET2 = Boolean.parseBoolean(rdata[26]);
		disableTimerAfterSomeoneCancelled = Boolean.parseBoolean(rdata[27]);
		useMap = Boolean.parseBoolean(rdata[28]);
		useFractionalGarbage = Boolean.parseBoolean(rdata[29]);
		garbageChangePerAttack = Boolean.parseBoolean(rdata[30]);
		garbagePercent = Integer.parseInt(rdata[31]);
		spinCheckType = Integer.parseInt(rdata[32]);
		tspinEnableEZ = Boolean.parseBoolean(rdata[33]);
		b2bChunk = Boolean.parseBoolean(rdata[34]);
		strMode = NetUtil.urlDecode(rdata[35]);
		singleplayer = Boolean.parseBoolean(rdata[36]);
		rated = Boolean.parseBoolean(rdata[37]);
		customRated = Boolean.parseBoolean(rdata[38]);
		style = Integer.parseInt(rdata[39]);
		divideChangeRateByPlayers = Boolean.parseBoolean(rdata[40]);
		if(rdata.length > 41) isTarget = Boolean.parseBoolean(rdata[41]);
		if(rdata.length > 42) targetTimer = Integer.parseInt(rdata[42]);
		//useTankMode = Boolean.parseBoolean(rdata[43]);
	}

	/**
	 * String(;????)?? data??(Player?????)
	 *
	 * @param str
	 *            String
	 */
	public void importString(String str) {
		importStringArray(str.split(";"));
	}

	/**
	 * String??????(Player?????)
	 *
	 * @return String???(String[43])
	 */
	public String[] exportStringArray() {
		String[] rdata = new String[43];
		rdata[0] = Integer.toString(roomID);
		rdata[1] = NetUtil.urlEncode(strName);
		rdata[2] = Integer.toString(maxPlayers);
		rdata[3] = Integer.toString(playerSeatedCount);
		rdata[4] = Integer.toString(spectatorCount);
		rdata[5] = Integer.toString(playerListCount);
		rdata[6] = Boolean.toString(playing);
		rdata[7] = Boolean.toString(ruleLock);
		rdata[8] = NetUtil.urlEncode(ruleName);
		rdata[9] = Integer.toString(autoStartSeconds);
		rdata[10] = Integer.toString(gravity);
		rdata[11] = Integer.toString(denominator);
		rdata[12] = Integer.toString(are);
		rdata[13] = Integer.toString(areLine);
		rdata[14] = Integer.toString(lineDelay);
		rdata[15] = Integer.toString(lockDelay);
		rdata[16] = Integer.toString(das);
		rdata[17] = Integer.toString(tspinEnableType);
		rdata[18] = Boolean.toString(b2b);
		rdata[19] = Boolean.toString(combo);
		rdata[20] = Boolean.toString(rensaBlock);
		rdata[21] = Boolean.toString(counter);
		rdata[22] = Boolean.toString(bravo);
		rdata[23] = Boolean.toString(reduceLineSend);
		rdata[24] = Integer.toString(hurryupSeconds);
		rdata[25] = Integer.toString(hurryupInterval);
		rdata[26] = Boolean.toString(autoStartTNET2);
		rdata[27] = Boolean.toString(disableTimerAfterSomeoneCancelled);
		rdata[28] = Boolean.toString(useMap);
		rdata[29] = Boolean.toString(useFractionalGarbage);
		rdata[30] = Boolean.toString(garbageChangePerAttack);
		rdata[31] = Integer.toString(garbagePercent);
		rdata[32] = Integer.toString(spinCheckType);
		rdata[33] = Boolean.toString(tspinEnableEZ);
		rdata[34] = Boolean.toString(b2bChunk);
		rdata[35] = NetUtil.urlEncode(strMode);
		rdata[36] = Boolean.toString(singleplayer);
		rdata[37] = Boolean.toString(rated);
		rdata[38] = Boolean.toString(customRated);
		rdata[39] = Integer.toString(style);
		rdata[40] = Boolean.toString(divideChangeRateByPlayers);
		rdata[41] = Boolean.toString(isTarget);
		rdata[42] = Integer.toString(targetTimer);
		//rdata[43] = Boolean.toString(useTankMode);

		return rdata;
	}

	/**
	 * String???(;????)(Player?????)
	 *
	 * @return String
	 */
	public String exportString() {
		String[] data = exportStringArray();
		String strResult = "";

		for (int i = 0; i < data.length; i++) {
			strResult += data[i];
			if (i < data.length - 1)
				strResult += ";";
		}

		return strResult;
	}

	/**
	 * Number of players???????
	 */
	public void updatePlayerCount() {
		playerSeatedCount = getNumberOfPlayerSeated();
		playerListCount = playerList.size();
		spectatorCount = playerListCount - playerSeatedCount;
	}

	/**
	 * ??????????count?count??(null?????????)
	 *
	 * @return ??????????count
	 */
	public int getNumberOfPlayerSeated() {
		int count = 0;
		for (int i = 0; i < playerSeat.size(); i++) {
			if (playerSeat.get(i) != null)
				count++;
		}
		return count;
	}

	/**
	 * ????Player???????????????
	 *
	 * @param pInfo
	 *            Player
	 * @return ????Player??????????true
	 */
	public boolean isPlayerInSeat(NetPlayerInfo pInfo) {
		return playerSeat.contains(pInfo);
	}

	/**
	 * ????Player??? number????????????
	 *
	 * @param pInfo
	 *            Player
	 * @return ???? number(?????-1)
	 */
	public int getPlayerSeatNumber(NetPlayerInfo pInfo) {
		for (int i = 0; i < playerSeat.size(); i++) {
			if (playerSeat.get(i) == pInfo) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * @return ????????????????????true
	 */
	public boolean canJoinSeat() {
		return (getNumberOfPlayerSeated() < maxPlayers);
	}

	/**
	 * ???????
	 *
	 * @param pInfo
	 *            Player
	 * @return ????? number(??????-1)
	 */
	public int joinSeat(NetPlayerInfo pInfo) {
		if (canJoinSeat()) {
			exitQueue(pInfo);

			for (int i = 0; i < playerSeat.size(); i++) {
				if (playerSeat.get(i) == null) {
					playerSeat.set(i, pInfo);
					return i;
				}
			}

			playerSeat.add(pInfo);
			return playerSeat.size() - 1;
		}
		return -1;
	}

	/**
	 * ????Player?????????
	 *
	 * @param pInfo
	 *            Player
	 */
	public void exitSeat(NetPlayerInfo pInfo) {
		for (int i = 0; i < playerSeat.size(); i++) {
			if (playerSeat.get(i) == pInfo) {
				playerSeat.set(i, null);
			}
		}
	}

	/**
	 * ???????
	 *
	 * @param pInfo
	 *            Player
	 * @return ???? number
	 */
	public int joinQueue(NetPlayerInfo pInfo) {
		if (playerQueue.contains(pInfo)) {
			return playerQueue.indexOf(pInfo);
		}
		playerQueue.add(pInfo);
		return playerQueue.size() - 1;
	}

	/**
	 * ????Player?????????
	 *
	 * @param pInfo
	 *            Player
	 */
	public void exitQueue(NetPlayerInfo pInfo) {
		playerQueue.remove(pInfo);
	}

	/**
	 * ???Player????????count??
	 *
	 * @return ??????Number of players
	 */
	public int getHowManyPlayersReady() {
		int count = 0;
		for (NetPlayerInfo pInfo : playerSeat) {
			if (pInfo != null) {
				if (pInfo.ready)
					count++;
			}
		}
		return count;
	}

	/**
	 * ???Player??????count??(??????????????????????)
	 *
	 * @return ?????Number of players
	 */
	public int getHowManyPlayersPlaying() {
		int count = 0;
		for (NetPlayerInfo pInfo : playerSeatNowPlaying) {
			if (pInfo != null) {
				if (pInfo.playing && playerSeat.contains(pInfo))
					count++;
			}
		}
		return count;
	}

	/**
	 * ????????Player??????
	 *
	 * @return ????????Player???(??2???????????, ??????????????????null)
	 */
	public NetPlayerInfo getWinner() {
		if ((startPlayers >= 2) && (getHowManyPlayersPlaying() < 2)
				&& (playing == true)) {
			for (NetPlayerInfo pInfo : playerSeatNowPlaying) {
				if (pInfo != null) {
					if (pInfo.playing && pInfo.connected
							&& playerSeat.contains(pInfo))
						return pInfo;
				}
			}
		}
		return null;
	}

	/**
	 * ????????Team name???
	 *
	 * @return ????????Team name
	 */
	public String getWinnerTeam() {
		if ((startPlayers >= 2) && (getHowManyPlayersPlaying() >= 2)
				&& (playing == true)) {
			for (NetPlayerInfo pInfo : playerSeatNowPlaying) {
				if ((pInfo != null) && pInfo.playing && pInfo.connected
						&& playerSeat.contains(pInfo)) {
					if (pInfo.strTeam.length() <= 0) {
						return null;
					} else {
						return pInfo.strTeam;
					}
				}
			}
		}

		return null;
	}

	/**
	 * @return 1??????????????????true
	 */
	public boolean isTeamWin() {
		String teamname = null;

		if ((startPlayers >= 2) && (getHowManyPlayersPlaying() >= 2)
				&& (playing == true)) {
			for (NetPlayerInfo pInfo : playerSeatNowPlaying) {
				if ((pInfo != null) && pInfo.playing && pInfo.connected
						&& playerSeat.contains(pInfo)) {
					if (pInfo.strTeam.length() <= 0) {
						return false;
					} else if (teamname == null) {
						teamname = pInfo.strTeam;
					} else if (!teamname.equals(pInfo.strTeam)) {
						return false;
					}
				}
			}
		}

		return (teamname != null);
	}

	/**
	 * @return true if it's a team game
	 */
	public boolean isTeamGame() {
		LinkedList<String> teamList = new LinkedList<String>();

		if (startPlayers >= 2) {
			for (NetPlayerInfo pInfo : playerSeatNowPlaying) {
				if ((pInfo != null) && (pInfo.strTeam.length() > 0)) {
					if (teamList.contains(pInfo.strTeam)) {
						return true;
					} else {
						teamList.add(pInfo.strTeam);
					}
				}
			}
		}

		return false;
	}

	/**
	 * @return true if 2 or more people have same IP
	 */
	public boolean hasSameIPPlayers() {
		LinkedList<String> ipList = new LinkedList<String>();

		if (startPlayers >= 2) {
			for (NetPlayerInfo pInfo : playerSeatNowPlaying) {
				if ((pInfo != null) && (pInfo.strRealIP.length() > 0)) {
					if (ipList.contains(pInfo.strRealIP)) {
						return true;
					} else {
						ipList.add(pInfo.strRealIP);
					}
				}
			}
		}

		return false;
	}

	/**
	 * Start game????????
	 */
	public void gameStart() {
		updatePlayerCount();
		playerSeatNowPlaying.clear();
		playerSeatNowPlaying.addAll(playerSeat);
		playerSeatDead.clear();
		chatList.clear();
		startPlayers = playerSeatedCount;
		deadCount = 0;
		autoStartActive = false;
		isSomeoneCancelled = false;
	}

	/**
	 * ?????????
	 */
	public void delete() {
		ruleOpt = null;
		mapList.clear();
		playerList.clear();
		playerSeat.clear();
		playerSeatNowPlaying.clear();
		playerQueue.clear();
		playerSeatDead.clear();
		chatList.clear();
	}
}
>>>>>>> 76aa07461566a5976980e6696204781271955163

