package mw.client.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import mw.client.constants.Constants;
import mw.client.managers.SettingsManager;
import mw.mtgforge.Constant;
import mw.mtgforge.Deck;
import mw.mtgforge.MyRandom;
import mw.server.list.CardBeanList;
import mw.server.list.CardBeanListFilter;
import mw.server.model.bean.CardBean;
import mw.server.pattern.Command;
import mw.utils.CacheObjectUtil;

import org.apache.log4j.Logger;

interface DeckDisplay {
	public void updateDisplay(CardBeanList top, CardBeanList bottom, CardBeanList sideboard);

	// top shows available card pool
	// if constructed, top shows all cards
	// if sealed, top shows 5 booster packs
	// if draft, top shows cards that were chosen
	public CardBeanList getTop();

	// bottom shows cards that the user has chosen for his library
	public CardBeanList getBottom();

	public CardBeanList getSideboard();

	public void setTitle(String message);

	public boolean isSideboarding();

	public void showAllPrints();
}

@SuppressWarnings("serial")
public class DeckEditorMenu extends JMenuBar {

	private static final Logger log = Logger.getLogger(DeckEditorMenu.class);

	// used by importConstructed() and exportConstructected()
	private static File previousDirectory = null;

	private final boolean debugPrint = false;

	private DeckIO deckIO;

	private boolean isDeckSaved;
	private String currentDeckName;
	private String defaultDeckName;
	private String currentGameType;

	private JMenuItem newDraftItem;
	private DeckDisplay deckDisplay;

	private boolean sideboarding = false;

	private Command exitCommand;

	private HashMap<String, CardBean> allCardMap = new HashMap<String, CardBean>();

	public DeckEditorMenu(DeckDisplay in_display, Command exit) {
		deckDisplay = in_display;
		exitCommand = exit;
		this.sideboarding = in_display.isSideboarding();

		// this is added just to make save() and saveAs() work ok
		// when first started up, just a silly patch
		currentGameType = Constant.GameType.Constructed;
		setDeckData("", false);

		/**
		 * Load file
		 */
		File file = new File(Constants.DEFAULT_DECKS_DIR);
		if (!file.exists()) {
			file.mkdir();
		}
		deckIO = new DeckIO(Constants.DEFAULT_DECKS_DIR + File.separator + Constants.DECKS_FILENAME);

		setupMenu();
	}

	public boolean newConstructed() {

		currentGameType = Constant.GameType.Constructed;
		setDeckData("", false);

		/**
		 * Get objects from cache
		 */
		Object object = CacheObjectUtil.load(Constants.DEFAULT_CACHE_DIR, Constants.CARDS_CACHE_FILENAME);
		if (object != null && object instanceof CardBeanList) {
			CardBeanList allCards = (CardBeanList) object;
			deckDisplay.updateDisplay(allCards, new CardBeanList(), new CardBeanList());

			for (CardBean card : allCards.getCardBeanList()) {
				allCardMap.put(card.getCardKey(), card);
			}
		} else {
			JOptionPane.showMessageDialog(null, "Couldn't find any card. Please login and update card base.");
			return false;
		}

		return true;
	}// new constructed

	private boolean newRandomSealed() {
		currentGameType = Constant.GameType.Sealed;
		setDeckData("", false);

		/**
		 * Get objects from cache
		 */
		Object object = CacheObjectUtil.load(Constants.DEFAULT_CACHE_DIR, Constants.CARDS_CACHE_FILENAME);
		if (object != null && object instanceof CardBeanList) {
			CardBeanList allCards = (CardBeanList) object;
			if (allCards.size() < 10) {
				JOptionPane.showMessageDialog(null, "Not enough cards in database.");
				return false;
			}

			CardBeanList randomCards = new CardBeanList();
			for (int i = 0; i < Constants.RANDOM_CARDPOOL_CONSTRUCTED_COUNT; i++) {
				int index = MyRandom.random.nextInt(allCards.size());
				randomCards.add(allCards.get(index));
			}
			addBasicLands(randomCards, allCards);
			deckDisplay.showAllPrints();
			deckDisplay.updateDisplay(randomCards, new CardBeanList(), new CardBeanList());
			JOptionPane.showMessageDialog(null, "Cardpool with " + String.valueOf(Constants.RANDOM_CARDPOOL_CONSTRUCTED_COUNT)
					+ " cards has been generated. +20*5 basic lands.");
		} else {
			JOptionPane.showMessageDialog(null, "Couldn't find any card. Please login and update card base.");
			return false;
		}

		return true;
	}

	private boolean generateCards(CardBeanList container, CardBeanList allCards, Set<String> allowedSets, String rarity, String rarity2,
			int count) {
		CardBeanList prepared = new CardBeanList();
		for (CardBean c : allCards) {
			if (allowedSets.contains(c.getSetName())) {
				if (c.getRarity().equals(rarity) || c.getRarity().equals(rarity2)) {
					prepared.add(c);
				}
			}
		}
		if (prepared.size() < count) {
			JOptionPane.showMessageDialog(null,
					"There is not enough Rare and Mythic cards in your cardpool for Sealed for chosen sets. Exciting.", "Error",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}
		for (int i = 0; i < count; i++) {
			int index = MyRandom.random.nextInt(prepared.size());
			container.add(prepared.get(index));
		}

		return true;
	}

	private void newGenerateConstructed() {
		/*
		 * if (debugPrint) System.out.println("Generate Constructed");
		 * 
		 * // if(! isDeckSaved) // save();
		 * 
		 * currentGameType = Constant.GameType.Constructed; setDeckData("", false);
		 * 
		 * GenerateConstructedDeck gen = new GenerateConstructedDeck();
		 * 
		 * deckDisplay.updateDisplay(AllZone.CardFactory.getAllCards(), gen.generateDeck());
		 */
	}// new sealed

	private void newSealed() {
		// if(! isDeckSaved)
		// save();

		currentGameType = Constant.GameType.Sealed;
		//setDeckData("Sealed card pool (5 boosters, 75 cards)", false);

		CardBeanList random = new CardBeanList();
		Set<String> allowedSets = SettingsManager.getManager().getSealedSets();
		boolean generated = false;

		try {
			Object object = CacheObjectUtil.load(Constants.DEFAULT_CACHE_DIR, Constants.CARDS_CACHE_FILENAME);
			CardBeanList allCards = (CardBeanList) object;

			if (allCards.size() == 0) {
				JOptionPane.showMessageDialog(null, "There is no card in your local database.", "Error", JOptionPane.WARNING_MESSAGE);
				return;
			}

			if (!generateCards(random, allCards, allowedSets, "R", "M", 5))
				return;
			if (!generateCards(random, allCards, allowedSets, "U", null, 15))
				return;
			if (!generateCards(random, allCards, allowedSets, "C", null, 55))
				return;
			addBasicLands(random, allCards);
			generated = true;
		} finally {
			if (generated) {
				JOptionPane.showMessageDialog(null, "Card pool has been successfully generated.", "Info", JOptionPane.INFORMATION_MESSAGE);
			} else {
				setDeckData("", false);
			}
		}

		deckDisplay.updateDisplay(random, new CardBeanList(), new CardBeanList());
	}

	private void addBasicLands(CardBeanList container, CardBeanList cards) {
		CardBeanList cardList = cards.getNameAndSet("Forest", "ZEN");
		if (cardList.size() == 0)
			throw new RuntimeException("Couldn't find Forest in data base to use for Sealed.");
		for (int i = 0; i < 20; i++)
			container.add(cardList.get(0));
		cardList = cards.getNameAndSet("Island", "ZEN");
		if (cardList.size() == 0)
			throw new RuntimeException("Couldn't find Island in data base to use for Sealed.");
		for (int i = 0; i < 20; i++)
			container.add(cardList.get(0));
		cardList = cards.getNameAndSet("Swamp", "ZEN");
		if (cardList.size() == 0)
			throw new RuntimeException("Couldn't find Swamp in data base to use for Sealed.");
		for (int i = 0; i < 20; i++)
			container.add(cardList.get(0));
		cardList = cards.getNameAndSet("Mountain", "ZEN");
		if (cardList.size() == 0)
			throw new RuntimeException("Couldn't find Mountain in data base to use for Sealed.");
		for (int i = 0; i < 20; i++)
			container.add(cardList.get(0));
		cardList = cards.getNameAndSet("Plains", "ZEN");
		if (cardList.size() == 0)
			throw new RuntimeException("Couldn't find Plains in data base to use for Sealed.");
		for (int i = 0; i < 20; i++)
			container.add(cardList.get(0));
	}

	private void newDraft() {
		/*
		 * if (debugPrint) System.out.println("New Draft");
		 * 
		 * // if(! isDeckSaved) // save();
		 * 
		 * currentGameType = Constant.GameType.Draft;
		 * 
		 * // move all cards from deck main and sideboard to CardList Deck deck = deckIO.readBoosterDeck(currentDeckName)[0];
		 * setDeckData("", false);
		 * 
		 * CardList top = new CardList();
		 * 
		 * for (int i = 0; i < deck.countMain(); i++) top.add(AllZone.CardFactory.getCard(deck.getMain(i), Constant.Player.Human));
		 * 
		 * for (int i = 0; i < deck.countSideboard(); i++) top.add(AllZone.CardFactory.getCard(deck.getSideboard(i),
		 * Constant.Player.Human));
		 * 
		 * deckDisplay.updateDisplay(top, new CardList());
		 */
	}// new draft

	private FileFilter getFileFilter() {
		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				return f.getName().endsWith(".deck") || f.isDirectory();
			}

			public String getDescription() {
				return "Deck File .deck";
			}
		};

		return filter;
	}// getFileFilter()

	private FileFilter getTextDeckFilter() {
		FileFilter filter = new FileFilter() {
			public boolean accept(File f) {
				return f.getName().endsWith(".txt") || f.getName().endsWith(".mwDeck") || f.isDirectory();
			}

			public String getDescription() {
				return "Deck File .txt";
			}
		};

		return filter;
	}// getFileFilter()

	private FileFilter getMwDeckFilter() {
		FileFilter filter = new FileFilter() {

			@Override
			public String getDescription() {
				return "Magic Workstation Deck .mwDeck";
			}

			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".mwDeck") || f.isDirectory();
			}
		};
		return filter;
	}

	private File getImportFilename() {
		JFileChooser chooser = new JFileChooser(previousDirectory);

		//chooser.addChoosableFileFilter(getFileFilter());
		chooser.addChoosableFileFilter(getMwDeckFilter());
		chooser.addChoosableFileFilter(getTextDeckFilter());
		int returnVal = chooser.showOpenDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			previousDirectory = file.getParentFile();
			return file;
		}

		return null;

	}// openFileDialog()

	public void showDeck(Deck deck) {
		String gameType = deck.getDeckType();

		if (gameType.equals(Constant.GameType.Constructed))
			showConstructedDeck(deck);

		if (gameType.equals(Constant.GameType.Draft))
			showDraftDeck(deck);

		if (gameType.equals(Constant.GameType.Sealed))
			showSealedDeck(deck);

		this.defaultDeckName = deck.getNameFromFile();
	}// showDeck()

	private void importDeck() {
		File file = getImportFilename();

		if (file == null) {
			return;
		}

		DeckImporter importer = new DeckImporter();

		Deck deck = importer.importDeck(file);

		if (deck != null) {
			showDeck(deck);
			save();
			JOptionPane.showMessageDialog(null, "Deck was imported and saved.", "Import", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(null, "Couldn't import deck", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void importDecks() {
		DecksImporter di = new DecksImporter(false, true);
		try {
			di.importDecks();
		} catch (Exception ioe) {
			ioe.printStackTrace();
			JOptionPane.showMessageDialog(null, "Couldn't import decks. Look into log for more details.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
		deckIO = new DeckIO(Constants.DEFAULT_DECKS_DIR + File.separator + Constants.DECKS_FILENAME);
	}

	private void exportDeck() {
		File filename = getExportFilename();

		if (filename == null) {
			return;
		}

		// write is an Object variable because you might just
		// write one Deck object or
		// many Deck objects if it is a draft deck
		Deck deck = getDeck();
		//Object write = deck;

		String name = getFileNameWithoutExtension(filename.getName());
		deck.setName(name);

		// export Draft decks, this is a little hacky
		// a Draft deck holds 8 decks, [0] is the player's deck
		// and the other 7 are the computer's deck
		/*
		 * if (currentGameType.equals(Constant.GameType.Draft)) { // read all draft decks Deck d[] =
		 * deckIO.readBoosterDeck(currentDeckName);
		 * 
		 * // replace your deck d[0] = deck; write = d; }
		 */

		/*
		 * try { ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename)); out.writeObject(write); out.flush();
		 * out.close(); } catch (Exception ex) { JOptionPane.showMessageDialog(null, "Sorry there has been an error - " + ex.getMessage());
		 * throw new RuntimeException("DeckEditorMenu : exportDeck() error, " + ex); }
		 */

		exportDeckText(deck, filename.getAbsolutePath());

	}// exportDeck()

	private void exportDeckText(Deck aDeck, String filename) {

		// convert Deck into CardList
		CardBeanList all = new CardBeanList();
		for (int i = 0; i < aDeck.countMain(); i++) {
			String cardId = aDeck.getMain(i);
			CardBean c = this.allCardMap.get(cardId);
			if (c != null) {
				all.add(c);
			}
		}

		// sort by card name
		all.sort(new TableSorter(all, 1, true));

		// remove all copies of cards
		// make a singleton
		CardBeanList noCopies = new CardBeanList();
		for (int i = 0; i < all.size(); i++) {
			CardBean c = all.get(i);
			if (!noCopies.containsNameAndSet(c.getName(), c.getSetName())) {
				noCopies.add(c);
			}
		}

		String text = "";
		String newLine = "\r\n";
		int count = 0;

		text = all.size() + " Total Cards" + newLine + newLine;

		// creatures
		text += all.getType("Creature").size() + " Creatures" + newLine;
		text += "-------------" + newLine;

		for (int i = 0; i < noCopies.size(); i++) {
			CardBean c = noCopies.get(i);
			if (c.isCreature()) {
				count = all.getNameAndSet(c.getName(), c.getSetName()).size();
				//text += count + "x " + c.getName() + newLine;
				text += count + "x [" + c.getSetName() + "] " + c.getName() + newLine;
			}
		}

		// count spells, arg! this is tough
		CardBeanListFilter cf = new CardBeanListFilter() {
			public boolean addCard(CardBean c) {
				return !(c.isCreature() || c.isLand());
			}
		};// CardListFilter
		count = all.filter(cf).size();

		// spells
		text += newLine + count + " Spells" + newLine;
		text += "----------" + newLine;

		for (int i = 0; i < noCopies.size(); i++) {
			CardBean c = noCopies.get(i);
			if (!(c.isCreature() || c.isLand())) {
				count = all.getNameAndSet(c.getName(), c.getSetName()).size();
				//text += count + "x " + c.getName() + newLine;
				text += count + "x [" + c.getSetName() + "] " + c.getName() + newLine;
			}
		}

		// land
		text += newLine + all.getType("Land").size() + " Land" + newLine;
		text += "--------" + newLine;

		Map<CardBean, Integer> groupedBasic = new HashMap<CardBean, Integer>();
		for (CardBean c : all) {
			if (c.isLand()) {
				if (c.isBasicLand()) { // group
					boolean found = false;
					for (Entry<CardBean, Integer> entry : groupedBasic.entrySet()) {
						if (entry.getKey().getName().equals(c.getName()) && entry.getKey().getSetName().equals(c.getSetName())
								&& entry.getKey().getCollectorID() == c.getCollectorID()) {
							groupedBasic.put(c, entry.getValue() + 1);
							found = true;
						}
					}
					if (!found) groupedBasic.put(c, 1);
				} else {
					count = all.getNameAndSet(c.getName(), c.getSetName()).size();
					text += count + "x [" + c.getSetName() + "] " + c.getName() + newLine;
				}
			}
		}

		for (Entry<CardBean, Integer> entry : groupedBasic.entrySet()) {
			CardBean c = entry.getKey();
			text += entry.getValue() + "x [" + c.getSetName() + ":" + c.getCollectorID() + "] " + c.getName() + newLine;
		}
		
		CardBeanList side = new CardBeanList();
		for (int i = 0; i < aDeck.countSideboard(); i++) {
			String cardId = aDeck.getSideboard(i);
			CardBean c = this.allCardMap.get(cardId);

			side.add(c);
		}

		// sort by card name
		side.sort(new TableSorter(all, 1, true));

		text += newLine + "Sideboard" + newLine;
		text += "--------" + newLine;
		Set<String> alreadyAdded = new HashSet<String>();
		for (CardBean card : side) {
			if (card.isBasicLand()) {
				String info = "[" + card.getSetName() + ":" + card.getCollectorID() + "] " + card.getName();
				if (!alreadyAdded.contains(info)) {
					count = side.getNameSetAndCid(card.getName(), card.getSetName(), card.getCollectorID()).size();
					text += "s:" + count + "x " + info + newLine;
					alreadyAdded.add(info);
				}
			} else {
				String info = "[" + card.getSetName() + "] " + card.getName();
				if (!alreadyAdded.contains(info)) {
					count = side.getNameAndSet(card.getName(), card.getSetName()).size();
					text += "s:" + count + "x " + info  + newLine;
					alreadyAdded.add(info);
				}
			}
		}

		// remove ".deck" extension
		int cut = filename.indexOf(".");
		filename = filename.substring(0, cut);

		try {
			FileWriter writer = new FileWriter(filename + ".txt");
			writer.write(text);

			writer.flush();
			writer.close();
		} catch (Exception ex) {
			throw new RuntimeException("DeckEditorMenu : exportDeckText() error, " + ex.getMessage() + " : " + ex.getStackTrace());
		}

	}// exportDeckText()

	public static String getFileNameWithoutExtension(String fileName) {
		File tmpFile = new File(fileName);
		tmpFile.getName();
		int whereDot = tmpFile.getName().lastIndexOf('.');
		if (0 < whereDot && whereDot <= tmpFile.getName().length() - 2) {
			return tmpFile.getName().substring(0, whereDot);
		}
		return "";
	}

	private File getExportFilename() {

		JFileChooser save = new JFileChooser(previousDirectory);

		save.setDialogTitle("Export Deck Filename");
		save.setDialogType(JFileChooser.SAVE_DIALOG);
		save.addChoosableFileFilter(getFileFilter());
		save.setSelectedFile(new File(currentDeckName + ".deck"));

		int returnVal = save.showSaveDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = save.getSelectedFile();
			String check = file.getAbsolutePath();

			previousDirectory = file.getParentFile();

			if (check.endsWith(".deck")) {
				return file;
			} else {
				return new File(check + ".deck");
			}
		}

		return null;
	}// getExportFilename()

	private void openConstructed() {
		if (debugPrint)
			System.out.println("Open Constructed");

		// if(! isDeckSaved)
		// save();

		String name = getUserInput_OpenDeck(Constant.GameType.Constructed);

		if (name.equals(""))
			return;

		// must be AFTER get user input, since user could cancel
		currentGameType = Constant.GameType.Constructed;
		newDraftItem.setEnabled(false);

		Deck deck = deckIO.readDeck(name);
		showConstructedDeck(deck);
	}// open constructed

	private void showConstructedDeck(Deck deck) {
		setDeckData(deck.getName(), true);

		Object object = CacheObjectUtil.load(Constants.DEFAULT_CACHE_DIR, Constants.CARDS_CACHE_FILENAME);
		if (object != null && object instanceof CardBeanList) {
			CardBeanList allCards = (CardBeanList) object;

			//deckDisplay.updateDisplay(allCards, new CardBeanList(), new CardBeanList());

			CardBeanList main = new CardBeanList();
			for (int i = 0; i < deck.countMain(); i++) {
				main.add(allCards.getByKey(deck.getMain(i)));
			}
			CardBeanList side = new CardBeanList();
			for (int i = 0; i < deck.countSideboard(); i++) {
				side.add(allCards.getByKey(deck.getSideboard(i)));
			}

			if (!sideboarding) {
				deckDisplay.updateDisplay(allCards, main, side);
			} else {
				deckDisplay.updateDisplay(main, side, new CardBeanList());
			}
		} else {
			log.error("Couldn't read cards from the cache.");
		}
	}

	private void openSealed() {
		// if(! isDeckSaved)
		// save();

		String name = getUserInput_OpenDeck(Constant.GameType.Sealed);

		if (name.equals(""))
			return;

		// must be AFTER get user input, since user could cancel
		currentGameType = Constant.GameType.Sealed;
		newDraftItem.setEnabled(false);

		Deck deck = deckIO.readDeck(name);
		showSealedDeck(deck);
	}// open sealed

	private void showSealedDeck(Deck deck) {
		setDeckData(deck.getName(), true);

		Object object = CacheObjectUtil.load(Constants.DEFAULT_CACHE_DIR, Constants.CARDS_CACHE_FILENAME);
		if (object != null && object instanceof CardBeanList) {
			CardBeanList allCards = (CardBeanList) object;

			CardBeanList main = new CardBeanList();
			for (int i = 0; i < deck.countMain(); i++) {
				main.add(allCards.getByKey(deck.getMain(i)));
			}

			CardBeanList side = new CardBeanList();
			for (int i = 0; i < deck.countSideboard(); i++) {
				side.add(allCards.getByKey(deck.getSideboard(i)));
			}

			deckDisplay.updateDisplay(new CardBeanList(), main, side);
		} else {
			log.error("Couldn't read cards from the cache.");
		}
	}

	private void openDraft() {
		if (debugPrint)
			System.out.println("Open Draft");

		String name = getUserInput_OpenDeck(Constant.GameType.Draft);

		if (name.equals(""))
			return;

		// must be AFTER get user input, since user could cancel
		currentGameType = Constant.GameType.Draft;
		newDraftItem.setEnabled(true);

		Deck deck = deckIO.readBoosterDeck(name)[0];
		showDraftDeck(deck);
	}// open draft

	private void showDraftDeck(Deck deck) {
		/*
		 * setDeckData(deck.getName(), true);
		 * 
		 * CardList top = new CardList(); for (int i = 0; i < deck.countSideboard(); i++)
		 * top.add(AllZone.CardFactory.getCard(deck.getSideboard(i), Constant.Player.Human));
		 * 
		 * CardList bottom = new CardList(); for (int i = 0; i < deck.countMain(); i++)
		 * bottom.add(AllZone.CardFactory.getCard(deck.getMain(i), Constant.Player.Human));
		 * 
		 * deckDisplay.updateDisplay(top, bottom);
		 */
	}// showDraftDeck()

	private void save() {
		if (currentDeckName.equals(""))
			saveAs();
		else if (currentGameType.equals(Constant.GameType.Draft)) {
			setDeckData(currentDeckName, true);
			// write booster deck
			Deck[] all = deckIO.readBoosterDeck(currentDeckName);
			all[0] = getDeck();
			deckIO.writeBoosterDeck(all);
		} else// constructed or sealed
		{
			setDeckData(currentDeckName, true);
			deckIO.deleteDeck(currentDeckName);
			deckIO.writeDeck(getDeck());
			deckIO.writeFile();
		}
	}

	private void saveAs() {
		if (debugPrint)
			System.out.println("Save As");

		String name = getUserInput_GetDeckName();

		if (name.equals(""))
			return;
		else if (currentGameType.equals(Constant.GameType.Draft)) {
			// MUST copy array
			Deck[] read = deckIO.readBoosterDeck(currentDeckName);
			Deck[] all = new Deck[read.length];

			System.arraycopy(read, 0, all, 0, read.length);

			setDeckData(name, true);

			all[0] = getDeck();
			deckIO.writeBoosterDeck(all);
		} else// constructed and sealed
		{
			setDeckData(name, true);
			deckIO.writeDeck(getDeck());
			deckIO.writeFile();
		}
	}// save as

	private void delete() {
		if (debugPrint) {
			System.out.println("Delete");
		}

		if (currentGameType.equals("") || currentDeckName.equals("")) {
			return;
		}

		int n = JOptionPane.showConfirmDialog(null, "Do you want to delete this deck " + currentDeckName + " ?", "Delete",
				JOptionPane.YES_NO_OPTION);
		if (n == JOptionPane.NO_OPTION) {
			return;
		}

		if (currentGameType.equals(Constant.GameType.Draft)) {
			deckIO.deleteBoosterDeck(currentDeckName);
		} else {
			deckIO.deleteDeck(currentDeckName);
		}

		deckIO.writeFile();

		setDeckData("", true);
		deckDisplay.updateDisplay(new CardBeanList(), new CardBeanList(), new CardBeanList());
	}// delete

	public void sideboardDone() {
		exitCommand.execute();
	}

	public void close() {
		if (debugPrint) {
			System.out.println("Close");
		}

		// if(! isDeckSaved)
		// save();

		//deckIO.close();
		exitCommand.execute();
	}// close

	private void setDeckData(String deckName, boolean in_isDeckSaved) {
		currentDeckName = deckName;
		isDeckSaved = in_isDeckSaved;

		if (!sideboarding) {
			deckDisplay.setTitle("Deck Editor : " + currentDeckName);
		}
	}

	public String getDeckName() {
		return currentDeckName;
	}

	public String getGameType() {
		return currentGameType;
	}

	public boolean isDeckSaved() {
		return isDeckSaved;
	}

	private String getUserInput_GetDeckName() {
		Object o = JOptionPane.showInputDialog(null, "Deck name:", "Save as", JOptionPane.OK_CANCEL_OPTION, null, null,
				this.defaultDeckName);

		if (o == null)
			return "";

		String deckName = cleanString(o.toString());

		boolean isUniqueName;
		if (currentGameType.equals(Constant.GameType.Draft))
			isUniqueName = deckIO.isUniqueDraft(deckName);
		else
			isUniqueName = deckIO.isUnique(deckName);

		if ((!isUniqueName) || deckName.equals("")) {
			JOptionPane.showMessageDialog(null, "Please pick another deck name, a deck currently has that name.");
			return getUserInput_GetDeckName();
		}

		return deckName;
	}// getUserInput_GetDeckName()

	// only accepts numbers, letters or dashes up to 10 characters in length
	private String cleanString(String in) {
		String out = "";
		char[] c = in.toCharArray();

		String allowed = "-[] ";

		for (int i = 0; i < c.length && i < 60; i++)
			if (Character.isLetterOrDigit(c[i]) || allowed.contains(c[i] + ""))
				out += c[i];

		return out;
	}

	private String getUserInput_OpenDeck(String deckType) {
		ArrayList<String> choices = getDeckNames(deckType);
		if (choices.size() == 0) {
			JOptionPane.showMessageDialog(null, "No decks found", "Open Deck", JOptionPane.PLAIN_MESSAGE);
			return "";
		}
		Object o = JOptionPane.showInputDialog(null, "Deck Name", "Open Deck", JOptionPane.OK_CANCEL_OPTION, null, choices.toArray(),
				choices.toArray()[0]);

		if (o == null)
			return "";

		return o.toString();
	}// getUserInput_OpenDeck()

	private ArrayList<String> getDeckNames(String deckType) {
		ArrayList<String> list = new ArrayList<String>();

		// only get decks accoring to the Gui_NewGame screen option
		if (deckType.equals(Constant.GameType.Draft)) {
			Iterator<String> it = deckIO.getBoosterDecks().keySet().iterator();

			while (it.hasNext())
				list.add(it.next().toString());
		} else {
			Deck[] d = deckIO.getDecks();
			for (int i = 0; i < d.length; i++)
				if (deckType.equals(d[i].getDeckType()))
					list.add(d[i].toString());
		}

		Collections.sort(list);
		return list;
	}

	private Deck getDeck() {
		Deck deck = new Deck(currentGameType);
		deck.setName(currentDeckName);
		CardBeanList list;
		String cardId;

		// always move "bottom" to main
		list = deckDisplay.getBottom();
		for (int i = 0; i < list.size(); i++) {
			cardId = list.get(i).getCardKey();
			deck.addMain(cardId);
		}

		list = deckDisplay.getSideboard();
		for (int i = 0; i < list.size(); i++) {
			cardId = list.get(i).getCardKey();
			deck.addSideboard(cardId);
		}

		return deck;
	}// getDeck()

	private void setupMenu() {
		//JMenuItem newConstructed = new JMenuItem("New Deck - Constructed");
		JMenuItem newConstructed = new JMenuItem("New Constructed (All cards)");

		JMenuItem newSealed = new JMenuItem("New Sealed (5 boosters)");
		JMenuItem newDraft = new JMenuItem("New Deck - Draft");

		JMenuItem newRandomSealed = new JMenuItem("Generate Random Sealed Cardpool");
		JMenuItem newGenerateConstructed = new JMenuItem("New Deck - Generate Constructed Deck");

		JMenuItem importDeck = new JMenuItem("Import Deck");
		JMenuItem importDecks = new JMenuItem("Import Decks");
		JMenuItem exportDeck = new JMenuItem("Export Deck");

		JMenuItem openConstructed = new JMenuItem("Open Deck - Constructed");
		JMenuItem openSealed = new JMenuItem("Open Deck - Sealed");
		JMenuItem openDraft = new JMenuItem("Open Deck - Draft");

		newDraftItem = newDraft;
		newDraftItem.setEnabled(false);

		JMenuItem save = new JMenuItem("Save");
		JMenuItem saveAs = new JMenuItem("Save As...");
		JMenuItem delete = new JMenuItem("Delete");
		JMenuItem close = new JMenuItem("Close");
		if (sideboarding) {
			close.setText("Done");
		}

		//JMenu fileMenu = new JMenu("Deck Actions");
		JMenu fileMenu = new JMenu(sideboarding ? "Sideboard" : "Deck");

		if (!sideboarding) {
			fileMenu.add(newConstructed);

			fileMenu.add(newSealed);
			//fileMenu.add(newDraft);
			//fileMenu.addSeparator();

			fileMenu.add(openConstructed);
			fileMenu.add(openSealed);
			//fileMenu.add(openDraft);
			fileMenu.addSeparator();

			fileMenu.add(importDeck);
			fileMenu.add(importDecks);
			fileMenu.add(exportDeck);
			fileMenu.addSeparator();

			fileMenu.add(newRandomSealed);
			//fileMenu.add(newGenerateConstructed);
			//fileMenu.addSeparator();

			fileMenu.addSeparator();

			fileMenu.add(save);
			fileMenu.add(saveAs);
			fileMenu.add(delete);
			fileMenu.addSeparator();
		}

		fileMenu.add(close);

		this.add(fileMenu);

		// add listeners
		exportDeck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							exportDeck();
						}
					});
				} catch (Exception ex) {
					throw new RuntimeException("DeckEditorMenu : exportDeck() error - " + ex);
				}
			}
		});

		importDeck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							importDeck();
						}
					});
				} catch (Exception ex) {
					throw new RuntimeException("DeckEditorMenu : importDeck() error - " + ex);
				}
			}
		});

		importDecks.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							importDecks();
						}
					});
				} catch (Exception ex) {
					throw new RuntimeException("DeckEditorMenu : importDecks() error - " + ex);
				}
			}
		});

		newConstructed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							newConstructed();
						}
					});
				} catch (Exception ex) {
					throw new RuntimeException("DeckEditorMenu : newConstructed() error - " + ex);
				}
			}
		});

		newRandomSealed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							newRandomSealed();
						}
					});
				} catch (Exception ex) {
					throw new RuntimeException("DeckEditorMenu : newRandomConstructed() error - " + ex);
				}
			}
		});

		newGenerateConstructed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							newGenerateConstructed();
						}
					});
				} catch (Exception ex) {
					throw new RuntimeException("DeckEditorMenu : newRandomConstructed() error - " + ex);
				}
			}
		});

		newSealed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							newSealed();
						}
					});
				} catch (Exception ex) {
					throw new RuntimeException("DeckEditorMenu : newSealed() error - " + ex);
				}
			}
		});

		newDraft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							newDraft();
						}
					});
				} catch (Exception ex) {
					throw new RuntimeException("DeckEditorMenu : newDraft() error - " + ex);
				}
			}
		});

		openConstructed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							openConstructed();
						}
					});
				} catch (Exception ex) {
					throw new RuntimeException("DeckEditorMenu : openConstructed() error - " + ex);
				}
			}
		});

		openSealed.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							openSealed();
						}
					});
				} catch (Exception ex) {
					throw new RuntimeException("DeckEditorMenu : openSealed() error - " + ex);
				}
			}
		});

		openDraft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							openDraft();
						}
					});
				} catch (Exception ex) {
					throw new RuntimeException("DeckEditorMenu : openDraft() error - " + ex);
				}
			}
		});

		save.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							save();
						}
					});
				} catch (Exception ex) {
					throw new RuntimeException("DeckEditorMenu : save() error - " + ex);
				}
			}
		});

		saveAs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							saveAs();
						}
					});
				} catch (Exception ex) {
					throw new RuntimeException("DeckEditorMenu : saveAs() error - " + ex);
				}
			}
		});

		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							delete();
						}
					});
				} catch (Exception ex) {
					throw new RuntimeException("DeckEditorMenu : delete() error - " + ex);
				}
			}
		});

		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ev) {
				try {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							if (!sideboarding) {
								close();
							} else {
								sideboardDone();
							}
						}
					});
				} catch (Exception ex) {
					throw new RuntimeException("DeckEditorMenu : close() error - " + ex);
				}
			}
		});
	}//setupMenu()

	public void setSideboarding(boolean sideboarding) {
		this.sideboarding = sideboarding;
	}
}
